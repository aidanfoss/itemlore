//this is mixing into the veinminer mod, source code is available to view at https://github.com/sambam05/Veinminer/
package net.quantumaidan.itemLore.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.level.block.Block;
import net.quantumaidan.itemLore.util.statTrackLore;

@Mixin(targets = "com.sheath.veinminer.logic.VeinMinerController")
public class sgtVeinMinerMixin {
    @Redirect(method = "applyPlan", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;awardStat(Lnet/minecraft/stats/Stat;)V", remap = false))
    private static void redirectIncrementStat(ServerPlayer player, Stat<?> stat) {
        // Call the original method so standard tracking works
        player.awardStat(stat);
        // Inject ItemLore tracking logic
        if (stat.getValue() instanceof Block block) {
            if (player.getMainHandItem() != null && !player.getMainHandItem().isEmpty()) {
                statTrackLore.onBlockBrokenWithTool(player, block.defaultBlockState(), player.getMainHandItem());
            }
        }
    }
}
