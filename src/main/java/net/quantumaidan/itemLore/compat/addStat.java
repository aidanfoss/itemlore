package net.quantumaidan.itemLore.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.quantumaidan.itemLore.util.statTrackLore;

/*
This class is used for compatibility, mostly as a tool for other external mods to call/use if wanted/needed
More info about how to do this later
 */
public class addStat {
    public static void addBlockMinedStat(ServerPlayer player, net.minecraft.world.level.block.Block block, ItemStack tool, int num) {

    }

    public static void addMobKilledStat(ServerPlayer player, net.minecraft.world.entity.LivingEntity entity, ItemStack tool, int num) {

    }

    public static void addArmorDamagePreventionStat(ServerPlayer player, ItemStack armor, int num) {

    }

    //unused yet, eventually want to add unique trackers based on armor piece/enchantment (ex: swiftsneak extra distance, distance swum with depth strider, etc)
    @SuppressWarnings("unused")
    public static void addArmorUniqueStat(ServerPlayer player, ItemStack tool, int num, String uniqueStat) {
    }
}
