package net.quantumaidan.itemLore.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/*
This class is used for compatibility, mostly as a tool for other external mods to call/use if wanted/needed
More info about how to do this later
 */
public class addStat {
    /**
     * Increment the block mined stat for the given tool.
     */
    @SuppressWarnings("null")
    public static void addBlockMinedStat(ServerPlayer player, net.minecraft.world.level.block.Block block,
            ItemStack tool, int num) {
        if (!setLore.applyForcedLore(player, tool)) {
            // If forced lore failed (e.g. config issue) but the item might already have
            // lore,
            // we should still proceed if it has lore?
            // Original code: if (!setLore.applyForcedLore(player, tool)) { return; }
            // So we stick to that behavior.
            return;
        }

        String minedKey = getMinedKey(block);

        // Retrieve or create custom data
        net.minecraft.world.item.component.CustomData nbtComp = tool
                .get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        net.minecraft.nbt.CompoundTag customData = (nbtComp != null) ? nbtComp.copyTag()
                : new net.minecraft.nbt.CompoundTag();

        java.util.Optional<net.minecraft.nbt.CompoundTag> optionalStats = customData.getCompound("mining_stats");
        net.minecraft.nbt.CompoundTag stats = optionalStats.orElse(new net.minecraft.nbt.CompoundTag());
        if (optionalStats.isEmpty()) {
            customData.put("mining_stats", stats);
        }
        int count = stats.getInt(minedKey).orElse(0) + num;
        stats.putInt(minedKey, count);
        customData.put("mining_stats", stats);

        tool.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(customData));

        // Update lore with combined stats
        statTrackLore.updateItemLore(tool);
    }

    /**
     * Increment the mob killed stat for the given tool.
     */
    @SuppressWarnings("null")
    public static void addMobKilledStat(ServerPlayer player, net.minecraft.world.entity.LivingEntity entity,
            ItemStack tool, int num) {
        if (!statTrackLore.hasLore(tool)) {
            return;
        }

        String killKey = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        killKey = killKey.substring(0, 1).toUpperCase() + killKey.substring(1); // capitalize

        // Retrieve or create custom data
        net.minecraft.world.item.component.CustomData nbtComp = tool
                .get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        net.minecraft.nbt.CompoundTag customData = (nbtComp != null) ? nbtComp.copyTag()
                : new net.minecraft.nbt.CompoundTag();

        java.util.Optional<net.minecraft.nbt.CompoundTag> optionalStats = customData.getCompound("kill_stats");
        net.minecraft.nbt.CompoundTag stats = optionalStats.orElse(new net.minecraft.nbt.CompoundTag());
        if (optionalStats.isEmpty()) {
            customData.put("kill_stats", stats);
        }
        int count = stats.getInt(killKey).orElse(0) + num;
        stats.putInt(killKey, count);
        customData.put("kill_stats", stats);

        tool.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(customData));

        // Update lore with combined stats
        statTrackLore.updateItemLore(tool);
    }

    /**
     * Increment the damage prevented stat for the given armor piece.
     */
    @SuppressWarnings("null")
    public static void addArmorDamagePreventionStat(ServerPlayer player, ItemStack armor, float num) {
        if (!statTrackLore.hasLore(armor)) {
            return;
        }

        // Retrieve or create custom data
        net.minecraft.world.item.component.CustomData nbtComp = armor
                .get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        net.minecraft.nbt.CompoundTag customData = (nbtComp != null) ? nbtComp.copyTag()
                : new net.minecraft.nbt.CompoundTag();

        // Get or create damage prevention stat
        java.util.Optional<net.minecraft.nbt.CompoundTag> statsOpt = customData.getCompound("armor_stats");
        net.minecraft.nbt.CompoundTag stats = statsOpt.orElse(new net.minecraft.nbt.CompoundTag());
        if (statsOpt.isEmpty()) {
            customData.put("armor_stats", stats);
        }

        float currentPrevention = stats.getFloat("damage_prevented").orElse(0.0f);
        float newPrevention = currentPrevention + num;

        // Round to 1 decimal place for cleaner display
        newPrevention = Math.round(newPrevention * 10.0f) / 10.0f;
        stats.putFloat("damage_prevented", newPrevention);

        armor.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(customData));

        // Update armor lore
        statTrackLore.updateArmorLore(armor);
    }

    // unused yet, eventually want to add unique trackers based on armor
    // piece/enchantment (ex: swiftsneak extra distance, distance swum with depth
    // strider, etc)
    @SuppressWarnings("unused")
    public static void addArmorUniqueStat(ServerPlayer player, ItemStack tool, int num, String uniqueStat) {
    }

    /**
     * Helper to get the key for the block based on its ID path.
     */
    @SuppressWarnings("null")
    private static String getMinedKey(net.minecraft.world.level.block.Block block) {
        String name = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
        // Capitalize the path
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
