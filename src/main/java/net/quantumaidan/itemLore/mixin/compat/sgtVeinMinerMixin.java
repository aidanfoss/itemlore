//this is mixing into the veinminer mod, source code is available to view at https://github.com/sambam05/Veinminer/
//I also have a local copy of the source code
package net.quantumaidan.itemLore.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.quantumaidan.itemLore.util.statTrackLore;

@Mixin(targets = "com.sheath.veinminer.logic.VeinMinerController")
public class sgtVeinMinerMixin {
    @Redirect(method = "applyPlan", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;awardStat(Lnet/minecraft/stats/Stat;)V", remap = false))
    private static void redirectIncrementStat(ServerPlayer player, Stat<?> stat) {
        // Call the original method so standard tracking works
        player.awardStat(stat);
        // Inject ItemLore tracking logic
        Object value = stat.getValue();
        if (value instanceof Block block) {
            // We use the player's main hand item.
            // In Veinminer's applyPlan, the tool is passed in, but it should be the same as
            // MainHand
            // (or close enough for this context, as Veinminer damages the main hand tool).
            ItemStack tool = player.getMainHandItem();

            // Construct a default state since we only need the block for the stat key
            BlockState state = block.defaultBlockState();

            // Pass player position as a dummy/proxy position (it's not used for the logic
            // we need)
            if (tool != null && !tool.isEmpty()) {
                statTrackLore.onBlockBrokenWithLoredTool(player.blockPosition(), state, tool);
            }
        }
    }
}
