package net.quantumaidan.itemLore.mixin.compat;

import com.natamus.collective.functions.BlockFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.quantumaidan.itemLore.util.statTrackLore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockFunctions.class)
public class CollectiveMixin {

	@Inject(method = "dropBlock", at = @At("HEAD"), remap = false)
	private static void onDropBlock(Level level, BlockPos pos, CallbackInfo ci) {
		Player player = TreeHarvesterMixin.TH_PLAYER.get();
		if (player instanceof ServerPlayer serverPlayer) {
			BlockState state = level.getBlockState(pos);
			// Verify this is a valid block to track (checking state before it's broken)
			statTrackLore.onBlockBrokenWithTool(serverPlayer, state, serverPlayer.getMainHandItem());
		}
	}
}
