package net.quantumaidan.itemLore.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import net.quantumaidan.itemLore.util.statTrackLore;

@Mixin(ServerPlayerGameMode.class)
public class BlockBreakMixin {

    @Shadow
    @Final
    private ServerPlayer player;

    @Shadow
    @Final
    private ServerLevel level;

    private BlockState lastBrokenState;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void beforeTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.lastBrokenState = this.level.getBlockState(pos);
    }

    @Inject(method = "destroyBlock", at = @At("TAIL"))
    private void afterTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            if (net.quantumaidan.itemLore.config.itemLoreConfig.forceLore) {
                net.quantumaidan.itemLore.util.setLore.applyNewLore(this.player, this.player.getMainHandItem());
            }
            statTrackLore.onBlockBrokenWithLoredTool(pos, this.lastBrokenState, this.player.getMainHandItem());
        }
    }
}
