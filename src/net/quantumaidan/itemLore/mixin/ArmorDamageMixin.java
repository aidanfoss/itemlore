package net.quantumaidan.itemLore.mixin;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.quantumaidan.itemLore.ItemLore;
import net.quantumaidan.itemLore.util.statTrackLore;
import java.util.Map;
import java.util.HashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.server.level.ServerPlayer.class)
public class ArmorDamageMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void onPlayerDamageApplied(net.minecraft.server.level.ServerLevel world,
                                       DamageSource source,
                                       float amount,
                                       CallbackInfoReturnable<Boolean> cir) {
        net.minecraft.server.level.ServerPlayer player =
                (net.minecraft.server.level.ServerPlayer) (Object) this;

        ItemLore.LOGGER.info("[ArmorDamageMixin] PLAYER DAMAGE DETECTED - entity: {}, source: {}, amount: {}",
                player.getName().getString(), source.getMsgId(), amount);

        EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };
        int equippedArmorCount = 0;

        // Count equipped armor pieces
        for (EquipmentSlot slot : armorSlots) {
            net.minecraft.world.item.ItemStack armorStack = player.getItemBySlot(slot);
            if (!armorStack.isEmpty() && hasArmorValue(armorStack, slot)) {
                equippedArmorCount++;
                ItemLore.LOGGER.info("[ArmorDamageMixin] Found armor in slot {}: {}",
                        slot, armorStack.getHoverName().getString());
            }
        }

        boolean isFallDamage = "fall".equals(source.getMsgId());
        boolean isFireDamage = source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE);
        boolean isExplosion = source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION);
        boolean isProjectile = source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE);
        boolean bypassesArmor = source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR);

        ItemLore.LOGGER.info("[ArmorDamageMixin] Damage type - fall: {}, fire: {}, explosion: {}, projectile: {}, bypasses armor: {}, equippedArmorCount: {}",
                isFallDamage, isFireDamage, isExplosion, isProjectile, bypassesArmor, equippedArmorCount);

        // For fall damage, enchantments still apply even if it bypasses armor
        if (bypassesArmor && !isFallDamage) {
            ItemLore.LOGGER.info("[ArmorDamageMixin] Skipping non-fall damage that bypasses armor");
            return;
        }

        if (isFallDamage) {
            // Handle fall damage - reduction from enchantments, not armor toughness
            float totalFeatherLevel = 0;
            float totalProtectionLevel = 0;
            Map<net.minecraft.world.item.ItemStack, float[]> armorEnchantments = new HashMap<>();

            for (EquipmentSlot slot : armorSlots) {
                net.minecraft.world.item.ItemStack armorStack = player.getItemBySlot(slot);
                if (!armorStack.isEmpty()) {
                    int feather = getEnchantmentLevelOn(armorStack, Enchantments.FEATHER_FALLING, world);
                    int protection = getEnchantmentLevelOn(armorStack, Enchantments.PROTECTION, world);
                    totalFeatherLevel += feather;
                    totalProtectionLevel += protection;
                    armorEnchantments.put(armorStack, new float[]{feather, protection});
                    ItemLore.LOGGER.info("[ArmorDamageMixin] Armor {} has Feather Falling {} and Protection {}",
                            armorStack.getHoverName().getString(), feather, protection);
                }
            }

            ItemLore.LOGGER.info("[ArmorDamageMixin] Total Feather Falling level: {}, Total Protection level: {}",
                    totalFeatherLevel, totalProtectionLevel);

            if (totalFeatherLevel + totalProtectionLevel > 0) {
                // Feather Falling: 12% per level, max 48%
                float featherEffect = Math.max(0, Math.min(totalFeatherLevel * 0.12f, 0.48f));
                // Protection for fall damage: 4% per level, up to 20 points (80%)
                int protectionPointsFall = Math.min(20, Math.max(0, (int) totalProtectionLevel));
                float protectionEffect = 0.04f * protectionPointsFall;
                float totalEnchantmentEffect = featherEffect + protectionEffect; // Approximate additive effect

                float estimatedReduction = amount * totalEnchantmentEffect;

                ItemLore.LOGGER.info("[ArmorDamageMixin] Enchantment effectiveness: feather {} + protection {} = {}, estimated reduction: {}",
                        featherEffect, protectionEffect, totalEnchantmentEffect, estimatedReduction);

                if (estimatedReduction > 0) {
                    ItemLore.LOGGER.info("[ArmorDamageMixin] Calling onDamagePreventedByArmor with reduction: {}", estimatedReduction);
                    statTrackLore.onDamagePreventedByArmor(player, estimatedReduction);

                    // Distribute fall damage prevention proportionally to enchantment levels
                    distributeFallDamagePrevention(player, estimatedReduction, armorEnchantments);
                } else {
                    ItemLore.LOGGER.info("[ArmorDamageMixin] Estimated reduction is 0 or negative, skipping stat tracking");
                }
            } else {
                ItemLore.LOGGER.info("[ArmorDamageMixin] No relevant enchantments for fall damage, skipping tracking");
            }
        } else if (equippedArmorCount > 0) {
            // Calculate total armor, toughness, and protection
            float totalArmor = 0;
            float totalToughness = 0;
            int totalProtection = 0;
            int totalSpecializedProtection = 0; // For fire/blast/projectile protection

            for (EquipmentSlot slot : armorSlots) {
                net.minecraft.world.item.ItemStack armorStack = player.getItemBySlot(slot);
                if (!armorStack.isEmpty()) {
                    totalArmor += getArmorValue(armorStack, slot);
                    totalToughness += getToughnessValue(armorStack, slot);
                    totalProtection += getEnchantmentLevelOn(armorStack, Enchantments.PROTECTION, world);

                    // Add specialized protection based on damage type
                    if (isFireDamage) {
                        totalSpecializedProtection += getEnchantmentLevelOn(armorStack, Enchantments.FIRE_PROTECTION, world);
                    } else if (isExplosion) {
                        totalSpecializedProtection += getEnchantmentLevelOn(armorStack, Enchantments.BLAST_PROTECTION, world);
                    } else if (isProjectile) {
                        totalSpecializedProtection += getEnchantmentLevelOn(armorStack, Enchantments.PROJECTILE_PROTECTION, world);
                    }
                }
            }

            ItemLore.LOGGER.info("[ArmorDamageMixin] Total armor: {}, toughness: {}, protection: {}, specialized protection: {}",
                    totalArmor, totalToughness, totalProtection, totalSpecializedProtection);

            float totalPrevented = calculateDamagePrevented(amount, totalArmor, totalToughness, totalProtection, totalSpecializedProtection);

            ItemLore.LOGGER.info("[ArmorDamageMixin] Total damage prevented: {} out of {}", totalPrevented, amount);

            if (totalPrevented > 0) {
                ItemLore.LOGGER.info("[ArmorDamageMixin] Calling onDamagePreventedByArmor with reduction: {}", totalPrevented);
                statTrackLore.onDamagePreventedByArmor(player, totalPrevented);

                // Calculate per-piece contributions using marginal removal
                for (EquipmentSlot slot : armorSlots) {
                    net.minecraft.world.item.ItemStack armorStack = player.getItemBySlot(slot);
                    if (!armorStack.isEmpty() && hasArmorValue(armorStack, slot)) {
                        float armorValue = getArmorValue(armorStack, slot);
                        float toughnessValue = getToughnessValue(armorStack, slot);
                        int protectionValue = getEnchantmentLevelOn(armorStack, Enchantments.PROTECTION, world);
                        int specializedProtectionValue = 0;

                        // Get specialized protection for this piece
                        if (isFireDamage) {
                            specializedProtectionValue = getEnchantmentLevelOn(armorStack, Enchantments.FIRE_PROTECTION, world);
                        } else if (isExplosion) {
                            specializedProtectionValue = getEnchantmentLevelOn(armorStack, Enchantments.BLAST_PROTECTION, world);
                        } else if (isProjectile) {
                            specializedProtectionValue = getEnchantmentLevelOn(armorStack, Enchantments.PROJECTILE_PROTECTION, world);
                        }

                        float armorWithout = totalArmor - armorValue;
                        float toughnessWithout = totalToughness - toughnessValue;
                        int protectionWithout = totalProtection - protectionValue;
                        int specializedProtectionWithout = totalSpecializedProtection - specializedProtectionValue;

                        float preventedWithout = calculateDamagePrevented(amount, armorWithout, toughnessWithout, protectionWithout, specializedProtectionWithout);
                        float contribution = totalPrevented - preventedWithout;

                        ItemLore.LOGGER.info("[ArmorDamageMixin] Armor piece {} prevented {} damage",
                                armorStack.getHoverName().getString(), contribution);

                        statTrackLore.onArmorPiecePreventedDamage(armorStack, contribution);
                    }
                }
            } else {
                ItemLore.LOGGER.info("[ArmorDamageMixin] Estimated reduction is 0 or negative, skipping stat tracking");
            }
        } else {
            ItemLore.LOGGER.info("[ArmorDamageMixin] No armor equipped, skipping damage prevention tracking");
        }
    }

    private void distributeFallDamagePrevention(net.minecraft.server.level.ServerPlayer player,
                                                float totalPrevented,
                                                Map<net.minecraft.world.item.ItemStack, float[]> armorEnchantments) {
        float totalContribution = 0.0f;

        // Calculate total enchantment contribution (weighted: feather * 3 + protection * 1 for proportional distribution)
        for (float[] levels : armorEnchantments.values()) {
            totalContribution += levels[0] * 3 + levels[1]; // feather weighted higher to match vanilla max reductions
        }

        ItemLore.LOGGER.info("[ArmorDamageMixin] Total weighted enchantment contribution: {}", totalContribution);

        if (totalContribution > 0.0f) {
            // Distribute damage prevention proportionally based on each piece's weighted enchantment contribution
            for (java.util.Map.Entry<net.minecraft.world.item.ItemStack, float[]> entry : armorEnchantments.entrySet()) {
                net.minecraft.world.item.ItemStack armorStack = entry.getKey();
                float featherLevel = entry.getValue()[0];
                float protectionLevel = entry.getValue()[1];
                float contribution = featherLevel * 3 + protectionLevel;
                float proportionalPrevention = (contribution / totalContribution) * totalPrevented;
                ItemLore.LOGGER.info(
                        "[ArmorDamageMixin] Distributing {} fall damage protection to {} (weighted contribution: {}, feather: {}, protection: {})",
                        proportionalPrevention, armorStack.getHoverName().getString(), contribution, featherLevel,
                        protectionLevel
                );
                statTrackLore.onArmorPiecePreventedDamage(armorStack, proportionalPrevention);
            }
        } else {
            ItemLore.LOGGER.warn("[ArmorDamageMixin] No enchantment contribution for fall damage distribution");
        }
    }

    /**
     * Check if an item stack has armor value in the given slot
     */
    private boolean hasArmorValue(net.minecraft.world.item.ItemStack stack, EquipmentSlot slot) {
        return getArmorValue(stack, slot) > 0;
    }

    /**
     * Get armor protection value from attribute modifiers
     */
    private float getArmorValue(net.minecraft.world.item.ItemStack stack, EquipmentSlot slot) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return (float) modifiers.modifiers().stream()
                .filter(entry -> entry.slot().test(slot) && entry.attribute().equals(Attributes.ARMOR))
                .mapToDouble(entry -> entry.modifier().amount())
                .sum();
    }

    /**
     * Get armor toughness value from attribute modifiers
     */
    private float getToughnessValue(net.minecraft.world.item.ItemStack stack, EquipmentSlot slot) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return (float) modifiers.modifiers().stream()
                .filter(entry -> entry.slot().test(slot) && entry.attribute().equals(Attributes.ARMOR_TOUGHNESS))
                .mapToDouble(entry -> entry.modifier().amount())
                .sum();
    }

    /**
     * 1.21+–compatible enchantment lookup:
     * - Use the world's registry manager
     * - Use RegistryKeys.ENCHANTMENT
     * - Get a RegistryEntry<Enchantment> and feed it to EnchantmentHelper
     */
    private static int getEnchantmentLevelOn(net.minecraft.world.item.ItemStack stack, ResourceKey<Enchantment> key, net.minecraft.world.level.Level world) {
        ItemLore.LOGGER.info("[ArmorDamageMixin] Getting enchantment level for {} on {}", key, stack.getHoverName().getString());
        Registry<Enchantment> registry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        java.util.Optional<Enchantment> enchantmentOpt = registry.getOptional(key);
        if (enchantmentOpt.isPresent()) {
            ItemLore.LOGGER.info("[ArmorDamageMixin] Enchantment {} is present in registry", key);
            net.minecraft.core.Holder<Enchantment> entry = registry.wrapAsHolder(enchantmentOpt.get());
            int level = EnchantmentHelper.getItemEnchantmentLevel(entry, stack);
            ItemLore.LOGGER.info("[ArmorDamageMixin] Level: {}", level);
            return level;
        } else {
            ItemLore.LOGGER.info("[ArmorDamageMixin] Enchantment {} not present in registry", key);
        }
        return 0;
    }

    private float calculateDamagePrevented(float damage, float totalArmor, float totalToughness, int totalProtection, int totalSpecializedProtection) {
        // Calculate armor effectiveness (vanilla formula)
        float armorTerm = Math.max(totalArmor / 5.0f, totalArmor - damage / (2.0f + totalToughness / 4.0f));
        float armorEffective = Math.min(20.0f, armorTerm);

        // Calculate protection enchantment effectiveness
        // In Minecraft, each level of protection gives 4% damage reduction (EPF = Enchantment Protection Factor)
        // General protection: 1 EPF per level
        // Specialized protection: 2 EPF per level (but only for matching damage types)
        int protectionEPF = totalProtection; // 1 EPF per level
        int specializedEPF = totalSpecializedProtection * 2; // 2 EPF per level for specialized
        int totalEPF = protectionEPF + specializedEPF;

        // Cap at 20 EPF (80% reduction max from enchantments)
        int effectiveEPF = Math.min(20, totalEPF);

        // Armor reduction
        float armorMultiplier = 25.0f / (25.0f + armorEffective);
        float damageAfterArmor = damage * armorMultiplier;

        // Enchantment reduction (4% per EPF point)
        float enchantmentReduction = effectiveEPF * 0.04f;
        float damageAfterEnchantments = damageAfterArmor * (1.0f - enchantmentReduction);

        float totalPrevented = damage - damageAfterEnchantments;

        ItemLore.LOGGER.info("[ArmorDamageMixin] Damage calculation - original: {}, after armor: {}, after enchants: {}, prevented: {}, EPF: {} (prot: {}, specialized: {})",
                damage, damageAfterArmor, damageAfterEnchantments, totalPrevented, effectiveEPF, protectionEPF, specializedEPF);

        return totalPrevented;
    }
}