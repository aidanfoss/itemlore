package net.quantumaidan.itemLore.util;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.enchantment.Enchantments;
import java.util.Map;
import java.util.HashMap;

public class ArmorStat {

    public static void handleFallDamage(net.minecraft.server.level.ServerPlayer player, float amount,
            EquipmentSlot[] armorSlots, net.minecraft.server.level.ServerLevel world) {
        // Handle fall damage - reduction from enchantments, not armor toughness
        float totalFeatherLevel = 0;
        float totalProtectionLevel = 0;
        Map<net.minecraft.world.item.ItemStack, float[]> armorEnchantments = new HashMap<>();

        for (EquipmentSlot slot : armorSlots) {
            @SuppressWarnings("null")
            net.minecraft.world.item.ItemStack armorStack = player.getItemBySlot(slot);
            if (!armorStack.isEmpty()) {
                int feather = getEnchantmentLevelOn(armorStack, Enchantments.FEATHER_FALLING, world);
                int protection = getEnchantmentLevelOn(armorStack, Enchantments.PROTECTION, world);
                totalFeatherLevel += feather;
                totalProtectionLevel += protection;
                armorEnchantments.put(armorStack, new float[] { feather, protection });
            }
        }

        if (totalFeatherLevel + totalProtectionLevel > 0) {
            // Feather Falling: 12% per level, max 48%
            float featherEffect = Math.max(0, Math.min(totalFeatherLevel * 0.12f, 0.48f));
            // Protection for fall damage: 4% per level, up to 20 points (80%)
            int protectionPointsFall = Math.min(20, Math.max(0, (int) totalProtectionLevel));
            float protectionEffect = 0.04f * protectionPointsFall;
            float totalEnchantmentEffect = featherEffect + protectionEffect; // Approximate additive effect

            float estimatedReduction = amount * totalEnchantmentEffect;

            if (estimatedReduction > 0) {
                // Distribute fall damage prevention proportionally to enchantment levels
                distributeFallDamagePrevention(player, estimatedReduction, armorEnchantments);
            }
        }
    }

    public static void handleMacroscopicArmorDamage(net.minecraft.server.level.ServerPlayer player, float amount,
            DamageSource source, EquipmentSlot[] armorSlots, net.minecraft.server.level.ServerLevel world) {
        // Calculate total armor, toughness, and protection
        float totalArmor = 0;
        float totalToughness = 0;
        int totalProtection = 0;
        int totalSpecializedProtection = 0; // For fire/blast/projectile protection

        boolean isFireDamage = source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE);
        boolean isExplosion = source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION);
        boolean isProjectile = source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE);

        for (EquipmentSlot slot : armorSlots) {
            @SuppressWarnings("null")
            net.minecraft.world.item.ItemStack armorStack = player.getItemBySlot(slot);
            if (!armorStack.isEmpty()) {
                totalArmor += getArmorValue(armorStack, slot);
                totalToughness += getToughnessValue(armorStack, slot);
                totalProtection += getEnchantmentLevelOn(armorStack, Enchantments.PROTECTION, world);

                // Add specialized protection based on damage type
                if (isFireDamage) {
                    totalSpecializedProtection += getEnchantmentLevelOn(armorStack,
                            Enchantments.FIRE_PROTECTION,
                            world);
                } else if (isExplosion) {
                    totalSpecializedProtection += getEnchantmentLevelOn(armorStack,
                            Enchantments.BLAST_PROTECTION,
                            world);
                } else if (isProjectile) {
                    totalSpecializedProtection += getEnchantmentLevelOn(armorStack,
                            Enchantments.PROJECTILE_PROTECTION, world);
                }
            }
        }

        float totalPrevented = calculateDamagePrevented(amount, totalArmor, totalToughness,
                totalProtection,
                totalSpecializedProtection);

        if (totalPrevented > 0) {
            // Calculate per-piece contributions using marginal removal
            for (EquipmentSlot slot : armorSlots) {
                @SuppressWarnings("null")
                net.minecraft.world.item.ItemStack armorStack = player.getItemBySlot(slot);
                if (!armorStack.isEmpty() && hasArmorValue(armorStack, slot)) {
                    float armorValue = getArmorValue(armorStack, slot);
                    float toughnessValue = getToughnessValue(armorStack, slot);
                    int protectionValue = getEnchantmentLevelOn(armorStack, Enchantments.PROTECTION,
                            world);
                    int specializedProtectionValue = 0;

                    // Get specialized protection for this piece
                    if (isFireDamage) {
                        specializedProtectionValue = getEnchantmentLevelOn(armorStack,
                                Enchantments.FIRE_PROTECTION,
                                world);
                    } else if (isExplosion) {
                        specializedProtectionValue = getEnchantmentLevelOn(armorStack,
                                Enchantments.BLAST_PROTECTION, world);
                    } else if (isProjectile) {
                        specializedProtectionValue = getEnchantmentLevelOn(armorStack,
                                Enchantments.PROJECTILE_PROTECTION, world);
                    }

                    float armorWithout = totalArmor - armorValue;
                    float toughnessWithout = totalToughness - toughnessValue;
                    int protectionWithout = totalProtection - protectionValue;
                    int specializedProtectionWithout = totalSpecializedProtection - specializedProtectionValue;

                    float preventedWithout = calculateDamagePrevented(amount, armorWithout,
                            toughnessWithout,
                            protectionWithout, specializedProtectionWithout);
                    float contribution = totalPrevented - preventedWithout;

                    statTrackLore.onArmorPiecePreventedDamage(armorStack, contribution);
                }
            }
        }
    }

    public static void distributeFallDamagePrevention(net.minecraft.server.level.ServerPlayer player,
            float totalPrevented,
            Map<net.minecraft.world.item.ItemStack, float[]> armorEnchantments) {
        float totalContribution = 0.0f;

        // Calculate total enchantment contribution (weighted: feather * 3 + protection
        // * 1 for proportional distribution)
        for (float[] levels : armorEnchantments.values()) {
            totalContribution += levels[0] * 3 + levels[1]; // feather weighted higher to match vanilla max reductions
        }

        if (totalContribution > 0.0f) {
            // Distribute damage prevention proportionally based on each piece's weighted
            // enchantment contribution
            for (java.util.Map.Entry<net.minecraft.world.item.ItemStack, float[]> entry : armorEnchantments
                    .entrySet()) {
                net.minecraft.world.item.ItemStack armorStack = entry.getKey();
                float featherLevel = entry.getValue()[0];
                float protectionLevel = entry.getValue()[1];
                float contribution = featherLevel * 3 + protectionLevel;
                float proportionalPrevention = (contribution / totalContribution) * totalPrevented;
                statTrackLore.onArmorPiecePreventedDamage(armorStack, proportionalPrevention);
            }
        }
    }

    /**
     * Check if an item stack has armor value in the given slot
     */
    public static boolean hasArmorValue(net.minecraft.world.item.ItemStack stack, EquipmentSlot slot) {
        return getArmorValue(stack, slot) > 0;
    }

    /**
     * Get armor protection value from attribute modifiers
     */
    @SuppressWarnings("null")
    public static float getArmorValue(net.minecraft.world.item.ItemStack stack, EquipmentSlot slot) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);
        return (float) modifiers.modifiers().stream()
                .filter(entry -> entry.slot().test(slot) && entry.attribute().equals(Attributes.ARMOR))
                .mapToDouble(entry -> entry.modifier().amount())
                .sum();
    }

    /**
     * Get armor toughness value from attribute modifiers
     */
    @SuppressWarnings("null")
    public static float getToughnessValue(net.minecraft.world.item.ItemStack stack, EquipmentSlot slot) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);
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
    public static int getEnchantmentLevelOn(net.minecraft.world.item.ItemStack stack, ResourceKey<Enchantment> key,
            net.minecraft.world.level.Level world) {
        Registry<Enchantment> registry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        java.util.Optional<Enchantment> enchantmentOpt = registry.getOptional(key);
        if (enchantmentOpt.isPresent()) {
            net.minecraft.core.Holder<Enchantment> entry = registry.wrapAsHolder(enchantmentOpt.get());
            return EnchantmentHelper.getItemEnchantmentLevel(entry, stack);
        }
        return 0;
    }

    public static float calculateDamagePrevented(float damage, float totalArmor, float totalToughness,
            int totalProtection,
            int totalSpecializedProtection) {
        // Calculate armor effectiveness (vanilla formula)
        float armorTerm = Math.max(totalArmor / 5.0f, totalArmor - damage / (2.0f + totalToughness / 4.0f));
        float armorEffective = Math.min(20.0f, armorTerm);

        // Calculate protection enchantment effectiveness
        // In Minecraft, each level of protection gives 4% damage reduction (EPF =
        // Enchantment Protection Factor)
        // General protection: 1 EPF per level
        // Specialized protection: 2 EPF per level (but only for matching damage types)
        // 1 EPF per level
        int specializedEPF = totalSpecializedProtection * 2; // 2 EPF per level for specialized
        int totalEPF = totalProtection + specializedEPF;

        // Cap at 20 EPF (80% reduction max from enchantments)
        int effectiveEPF = Math.min(20, totalEPF);

        // Armor reduction
        float armorMultiplier = 25.0f / (25.0f + armorEffective);
        float damageAfterArmor = damage * armorMultiplier;

        // Enchantment reduction (4% per EPF point)
        float enchantmentReduction = effectiveEPF * 0.04f;
        float damageAfterEnchantments = damageAfterArmor * (1.0f - enchantmentReduction);

        return damage - damageAfterEnchantments;
    }
}
