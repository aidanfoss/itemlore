package net.quantumaidan.itemLore.mixin.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.quantumaidan.itemLore.util.statTrackLore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.natamus.treeharvester_common_fabric.events.TreeCutEvents", remap = false)
public class TreeHarvesterMixin {
    private static final ThreadLocal<Player> capturingPlayer = new ThreadLocal<>();

    @Inject(method = "onTreeHarvest", at = @At("HEAD"), remap = false)
    private static void onTreeHarvestHead(Level world, Player player, BlockPos pos, BlockState state,
            BlockEntity blockEntity, CallbackInfoReturnable<Boolean> cir) {
        capturingPlayer.set(player);
    }

    @Inject(method = "onTreeHarvest", at = @At("RETURN"), remap = false)
    private static void onTreeHarvestReturn(Level world, Player player, BlockPos pos, BlockState state,
            BlockEntity blockEntity, CallbackInfoReturnable<Boolean> cir) {
        capturingPlayer.remove();
    }

    @Redirect(method = "onTreeHarvest", at = @At(value = "INVOKE", target = "Lcom/natamus/collective_common_fabric/functions/BlockFunctions;dropBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", remap = false), remap = false)
    private static void onDropBlock(Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        level.destroyBlock(pos, true);

        Player player = capturingPlayer.get();
        if (player instanceof ServerPlayer serverPlayer) {
            statTrackLore.onBlockBrokenWithTool(serverPlayer, blockState, player.getMainHandItem());
        }
    }
}
