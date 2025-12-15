package net.quantumaidan.itemLore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.quantumaidan.itemLore.util.statTrackLore;

@Mixin(LivingEntity.class)
public class EntityDeathMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onLivingDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (damageSource.getAttacker() instanceof ServerPlayerEntity player) {
            if (net.quantumaidan.itemLore.config.itemLoreConfig.forceLore) {
                net.quantumaidan.itemLore.util.setLore.applyNewLore(player, player.getMainHandStack());
            }
            statTrackLore.onEntityKilledWithLoredTool(player.getEntityWorld(), livingEntity, player.getMainHandStack());
        }
    }
}
