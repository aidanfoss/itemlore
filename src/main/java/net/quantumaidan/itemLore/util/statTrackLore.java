package net.quantumaidan.itemLore.util;

import net.quantumaidan.itemLore.ItemLore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.*;

public class statTrackLore {

    /**
     * Checks if an ItemStack has lore.
     * @param itemStack The item stack to check.
     * @return true if the item has lore, false otherwise.
     */
    public static boolean hasLore(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        LoreComponent lore = itemStack.get(DataComponentTypes.LORE);
        return lore != null && !lore.lines().isEmpty();
    }

    /**
     * Gets the mining stats for a tool.
     * @param tool The tool item stack.
     * @return A map of block types to mined counts.
     */
    public static Map<String, Integer> getMiningStats(ItemStack tool) {
        if (hasLore(tool)) return Collections.emptyMap();

        NbtComponent nbt = tool.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return Collections.emptyMap();

        NbtCompound customData = nbt.copyNbt();
        Optional<NbtCompound> statsOpt = customData.getCompound("mining_stats");
        if (statsOpt.isEmpty()) return Collections.emptyMap();

        NbtCompound stats = statsOpt.get();
        Map<String, Integer> result = new HashMap<>();
        for (String key : stats.getKeys()) {
            int val = stats.getInt(key).orElse(0);
            if (val > 0) result.put(key, val);
        }
        return result;
    }

    /**
     * Gets the item type.
     * @param item The tool item.
     * @return The item type: "pickaxe", "axe", "shovel", "hoe", "sword", "bow", or null if not a tool.
     */
    public static String getItemType(Item item) {
        if (item == null) {
	    return null;
        }
        Identifier id = Registries.ITEM.getId(item);
        String path = id.getPath();
        String[] parts = path.split("_");
        if (parts.length == 0) return null;
        String last = parts[parts.length - 1];
        if ("pickaxe".equals(last) || "axe".equals(last) || "shovel".equals(last) || "hoe".equals(last) || "sword".equals(last) || "bow".equals(last) || "crossbow".equals(last)) {
            return last;
        }
        return null;
    }

    /**
     * Gets the kill stats for a tool.
     * @param tool The tool item stack.
     * @return A map of mob types to killed counts.
     */
    public static Map<String, Integer> getKillStats(ItemStack tool) {
        if (hasLore(tool)) return Collections.emptyMap();

        NbtComponent nbt = tool.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return Collections.emptyMap();

        NbtCompound customData = nbt.copyNbt();
        Optional<NbtCompound> statsOpt = customData.getCompound("kill_stats");
        if (statsOpt.isEmpty()) return Collections.emptyMap();

        NbtCompound stats = statsOpt.get();
        Map<String, Integer> result = new HashMap<>();
        for (String key : stats.getKeys()) {
            int val = stats.getInt(key).orElse(0);
            if (val > 0) result.put(key, val);
        }
        return result;
    }

    /**
     * Checks if an item is a mining tool.
     * @param item The tool item.
     * @return true if it's a mining tool.
     */
    public static boolean isMiningTool(Item item) {
        String type = getItemType(item);
        return ("pickaxe".equals(type) || "axe".equals(type) || "shovel".equals(type) || "hoe".equals(type));
    }

    /**
     * Checks if an item is an armor item.
     * @param item The item.
     * @return true if it's armor (has armor stats).
     */
    public static boolean isArmor(Item item) {
        // Check if this item has armor stats stored
        return !getArmorStats(item.getDefaultStack()).isEmpty();
    }

    /**
     * Checks if an item is an attack tool.
     * @param item The tool item.
     * @return true if it's an attack tool.
     */
    public static boolean isAttackTool(Item item) {
        String type = getItemType(item);
        return ("sword".equals(type) || "axe".equals(type) || "bow".equals(type) || "crossbow".equals(type));
    }

    /**
     * Handles when a block is broken with a tool that has lore.
     * Tracks mining stats and updates the item's lore.
     * @param blockPos The position of the broken block.
     * @param blockState The state of the broken block.
     * @param tool The tool used to break the block.
     */
    public static void onBlockBrokenWithLoredTool(BlockPos blockPos, BlockState blockState, ItemStack tool) {
        if (hasLore(tool)) {
            return;
        }

        String minedKey = getMinedKey(blockState.getBlock());

        // Retrieve or create custom data
        NbtComponent nbtComp = tool.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound customData = (nbtComp != null) ? nbtComp.copyNbt() : new NbtCompound();

        Optional<NbtCompound> optionalStats = customData.getCompound("mining_stats");
        NbtCompound stats = optionalStats.orElse(new NbtCompound());
        if (optionalStats.isEmpty()) {
            customData.put("mining_stats", stats);
        }
        int count = stats.getInt(minedKey).orElse(0) + 1;
        stats.putInt(minedKey, count);
        customData.put("mining_stats", stats);

        tool.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));

        // Update lore with combined stats
        updateItemLore(tool);
    }

    /**
     * Handles when an entity is killed with a tool that has lore.
     * Tracks kill stats and updates the item's lore.
     * @param world The world where the entity was killed.
     * @param entity The entity killed.
     * @param tool The tool used to kill the entity.
     */
    public static void onEntityKilledWithLoredTool(World world, LivingEntity entity, ItemStack tool) {
        if (hasLore(tool)) {
            return;
        }

        String killKey = Registries.ENTITY_TYPE.getId(entity.getType()).getPath();
        killKey = killKey.substring(0, 1).toUpperCase() + killKey.substring(1); // capitalize

        // Retrieve or create custom data
        NbtComponent nbtComp = tool.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound customData = (nbtComp != null) ? nbtComp.copyNbt() : new NbtCompound();

        Optional<NbtCompound> optionalStats = customData.getCompound("kill_stats");
        NbtCompound stats = optionalStats.orElse(new NbtCompound());
        if (optionalStats.isEmpty()) {
            customData.put("kill_stats", stats);
        }
        int count = stats.getInt(killKey).orElse(0) + 1;
        stats.putInt(killKey, count);
        customData.put("kill_stats", stats);

        tool.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));

        // Update lore with combined stats
        updateItemLore(tool);
    }

    /**
     * Handles damage prevented by armor set (unused currently - distributed to pieces).
     * @param player The player whose armor prevented damage.
     * @param damagePrevented The total damage prevented.
     */
    public static void onDamagePreventedByArmor(net.minecraft.server.network.ServerPlayerEntity player, float damagePrevented) {
        ItemLore.LOGGER.info("[statTrackLore] onDamagePreventedByArmor called - damagePrevented: {}", damagePrevented);
        // This method is called but currently unused since we're distributing to individual pieces
        // Could be used for total armor stats if needed
    }

    /**
     * Handles damage prevented by a specific armor piece.
     * @param armorPiece The armor item stack.
     * @param damagePrevented The damage prevented by this piece.
     */
    public static void onArmorPiecePreventedDamage(ItemStack armorPiece, float damagePrevented) {
        ItemLore.LOGGER.info("[statTrackLore] onArmorPiecePreventedDamage called - item: {}, damagePrevented: {}", armorPiece.getName().getString(), damagePrevented);
        ItemLore.LOGGER.info("[statTrackLore] Item hasLore: {}", hasLore(armorPiece));

        if (!hasLore(armorPiece)) {
            ItemLore.LOGGER.info("[statTrackLore] Item has no lore, skipping stat tracking");
            return;
        }

        // Retrieve or create custom data
        NbtComponent nbtComp = armorPiece.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound customData = (nbtComp != null) ? nbtComp.copyNbt() : new NbtCompound();
        ItemLore.LOGGER.info("[statTrackLore] Retrieved custom data: {}", customData != null);

        // Get or create damage prevention stat
        Optional<NbtCompound> statsOpt = customData.getCompound("armor_stats");
        NbtCompound stats = statsOpt.orElse(new NbtCompound());
        if (statsOpt.isEmpty()) {
            customData.put("armor_stats", stats);
            ItemLore.LOGGER.info("[statTrackLore] Created new armor_stats compound");
        }

        float currentPrevention = stats.getFloat("damage_prevented").orElse(0.0f);
        float newPrevention = currentPrevention + damagePrevented;

        // Round to 1 decimal place for cleaner display
        newPrevention = Math.round(newPrevention * 10.0f) / 10.0f;
        stats.putFloat("damage_prevented", newPrevention);

        ItemLore.LOGGER.info("[statTrackLore] Updated damage_prevented from {} to {}", currentPrevention, newPrevention);

        armorPiece.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));

        // Update armor lore
        updateArmorLore(armorPiece);
        ItemLore.LOGGER.info("[statTrackLore] Updated armor lore");
    }

    /**
     * Gets the armor stats for an armor piece.
     * @param armorPiece The armor item stack.
     * @return A map of armor stats.
     */
    public static Map<String, Float> getArmorStats(ItemStack armorPiece) {
        if (!hasLore(armorPiece)) return Collections.emptyMap();

        NbtComponent nbt = armorPiece.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return Collections.emptyMap();

        NbtCompound customData = nbt.copyNbt();
        Optional<NbtCompound> statsOpt = customData.getCompound("armor_stats");
        if (statsOpt.isEmpty()) return Collections.emptyMap();

        NbtCompound stats = statsOpt.get();
        Map<String, Float> result = new HashMap<>();
        for (String key : stats.getKeys()) {
            float val = stats.getFloat(key).orElse(0.0f);
            if (val > 0) result.put(key, val);
        }
        return result;
    }

    /**
     * Updates the armor item's lore with damage prevention stats.
     * @param armorPiece The armor item stack.
     */
    private static void updateArmorLore(ItemStack armorPiece) {
        LoreComponent existingLore = armorPiece.get(DataComponentTypes.LORE);
        if (existingLore == null) return;
        List<Text> existingLines = existingLore.lines();

        // Preserve original lore (assuming first up to 2 non-empty lines are date and UID)
        List<Text> newLines = new ArrayList<>();
        for (Text line : existingLines) {
            if (!line.getString().isEmpty()) {
                newLines.add(line);
                if (newLines.size() >= 2) break;
            }
        }

        // Add damage prevention stat
        Map<String, Float> armorStats = getArmorStats(armorPiece);
        float damagePrevented = armorStats.getOrDefault("damage_prevented", 0.0f);
        if (damagePrevented > 0) {
            newLines.add(Text.literal("Damage Prevented: " + damagePrevented).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
        }

        armorPiece.set(DataComponentTypes.LORE, new LoreComponent(newLines));
    }


    /**
     * Gets the key for the block based on its ID path.
     * Tracks all blocks broken.
     * @param block The block broken.
     * @return The key for stats.
     */
    private static String getMinedKey(Block block) {
        Identifier id = Registries.BLOCK.getId(block);
        String name = id.getPath();
        // Capitalize the path
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Gets the base key for grouping in stats display.
     * @param key The raw key from stats.
     * @return The base key for grouping.
     */
    public static String getBaseKey(String key) {
        if (key.contains("_ore")) {
            String base = key.replace("deepslate_", "").toLowerCase();
            base = base.replace("_ore", "");
            return base.substring(0, 1).toUpperCase() + base.substring(1);
        } else {
            return key;
        }
    }

    /**
     * Updates the item's lore with combined stats.
     * Shows blocks mined and mobs killed in gray text, or damage prevented for armor.
     * For weapons, also shows the most killed mob type.
     * @param item The item stack.
     */
    private static void updateItemLore(ItemStack item) {
        LoreComponent existingLore = item.get(DataComponentTypes.LORE);
        if (existingLore == null) return;
        List<Text> existingLines = existingLore.lines();

        // Preserve original lore (assuming first up to 2 non-empty lines are date and UID)
        List<Text> newLines = new ArrayList<>();
        for (Text line : existingLines) {
            if (!line.getString().isEmpty()) {
                newLines.add(line);
                if (newLines.size() >= 2) break;
            }
        }

        // If this item has armor stats, update with armor stats
        NbtComponent nbt = item.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt != null) {
            NbtCompound customData = nbt.copyNbt();
            if (customData.getCompound("armor_stats").isPresent()) {
                updateArmorLore(item);
                return;
            }
        }

        // For tools/weapons: Calculate total blocks mined
        Map<String, Integer> miningStats = getMiningStats(item);
        int totalBlocks = miningStats.values().stream().mapToInt(Integer::intValue).sum();
        if (totalBlocks > 0) {
            newLines.add(Text.literal("Blocks Mined: " + totalBlocks).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
        }

        // Calculate total mobs killed
        Map<String, Integer> killStats = getKillStats(item);
        int totalMobs = killStats.values().stream().mapToInt(Integer::intValue).sum();
        if (totalMobs > 0) {
            newLines.add(Text.literal("Mobs Killed: " + totalMobs).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
        }

        // For weapons, add most killed mob
        if (isAttackTool(item.getItem()) && totalMobs > 0) {
            String mostKilled = "";
            int maxCnt = 0;
            for (Map.Entry<String, Integer> entry : killStats.entrySet()) {
                if (entry.getValue() > maxCnt) {
                    maxCnt = entry.getValue();
                    mostKilled = entry.getKey();
                }
            }
            newLines.add(Text.literal("Most Killed: " + mostKilled + " (" + maxCnt + ")").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
        }

        item.set(DataComponentTypes.LORE, new LoreComponent(newLines));
    }
}
