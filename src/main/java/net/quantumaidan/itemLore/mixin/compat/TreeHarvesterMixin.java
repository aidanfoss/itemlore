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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.natamus.treeharvester.events.TreeCutEvents", remap = false)
public class TreeHarvesterMixin {
    @Inject(method = "onTreeHarvest", at = @At("HEAD"), remap = false)
    private static void onTreeHarvest(Level world, Player player, BlockPos pos, BlockState state,
            BlockEntity blockEntity, CallbackInfoReturnable<Boolean> cir) {

        if (player instanceof ServerPlayer serverPlayer) {
            statTrackLore.onBlockBrokenWithTool(serverPlayer, state, player.getMainHandItem());
        }
    }
}
