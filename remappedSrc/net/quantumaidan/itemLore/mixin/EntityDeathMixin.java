package net.quantumaidan.itemLore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.quantumaidan.itemLore.util.statTrackLore;

@Mixin(LivingEntity.class)
public class EntityDeathMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void onLivingDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (damageSource.getEntity() instanceof ServerPlayer player) {
            if (net.quantumaidan.itemLore.config.itemLoreConfig.forceLore) {
                net.quantumaidan.itemLore.util.setLore.applyNewLore(player, player.getMainHandItem());
            }
            statTrackLore.onEntityKilledWithLoredTool(player.level(), livingEntity, player.getMainHandItem());
        }
    }
}
