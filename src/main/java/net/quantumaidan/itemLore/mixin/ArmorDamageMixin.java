package net.quantumaidan.itemLore.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.quantumaidan.itemLore.util.ArmorStat;
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
        net.minecraft.server.level.ServerPlayer player = (net.minecraft.server.level.ServerPlayer) (Object) this;

        EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };
        int equippedArmorCount = 0;

        // Count equipped armor pieces
        for (EquipmentSlot slot : armorSlots) {
            @SuppressWarnings("null")
            net.minecraft.world.item.ItemStack armorStack = player.getItemBySlot(slot);
            if (!armorStack.isEmpty() && ArmorStat.hasArmorValue(armorStack, slot)) {
                equippedArmorCount++;
            }
        }

        boolean isFallDamage = "fall".equals(source.getMsgId());
        boolean bypassesArmor = source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR);

        if (bypassesArmor && !isFallDamage) {
            return;
        }

        if (isFallDamage) {
            ArmorStat.handleFallDamage(player, amount, armorSlots, world);
        } else if (equippedArmorCount > 0) {
            ArmorStat.handleMacroscopicArmorDamage(player, amount, source, armorSlots, world);
        }
    }
}