package net.quantumaidan.itemLore.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.quantumaidan.itemLore.util.statTrackLore;

@Mixin(ServerPlayerInteractionManager.class)
public class BlockBreakMixin {

    @Shadow
    @Final
    private ServerPlayerEntity player;

    @Shadow
    @Final
    private ServerWorld world;

    private BlockState lastBrokenState;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void beforeTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.lastBrokenState = this.world.getBlockState(pos);
    }

    @Inject(method = "tryBreakBlock", at = @At("TAIL"))
    private void afterTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            net.quantumaidan.itemLore.util.setLore.applyForcedLore(this.player, this.player.getMainHandStack());
            statTrackLore.onBlockBrokenWithLoredTool(pos, this.lastBrokenState, this.player.getMainHandStack());
        }
    }
}
