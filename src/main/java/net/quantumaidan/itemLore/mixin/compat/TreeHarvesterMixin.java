package net.quantumaidan.itemLore.mixin.compat;

import com.natamus.collective.functions.BlockFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.quantumaidan.itemLore.util.statTrackLore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.natamus.treeharvester.events.TreeCutEvents")
public class TreeHarvesterMixin {

    @Redirect(method = "onTreeHarvest", at = @At(value = "INVOKE", target = "Lcom/natamus/collective/functions/BlockFunctions;dropBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", remap = false))
    private static void redirectDropBlock(Level level, BlockPos pos, Level originalLevel,
            net.minecraft.world.entity.player.Player player) {
        // Call the original method
        BlockFunctions.dropBlock(level, pos);

        // Add ItemLore stats logic
        if (player instanceof ServerPlayer serverPlayer) {
            statTrackLore.onBlockBrokenWithTool(serverPlayer, level.getBlockState(pos), serverPlayer.getMainHandItem());
        }
    }
}
