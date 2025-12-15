package net.quantumaidan.itemLore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.context.CommandContext;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.quantumaidan.itemLore.config.itemLoreConfig;
import net.quantumaidan.itemLore.util.setLore;
import net.quantumaidan.itemLore.util.statTrackLore;

public class ItemLore implements ModInitializer {
    public static final String MOD_ID = "itemLore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @SuppressWarnings({ "null"})
    @Override
    public void onInitialize() {
        MidnightConfig.init("itemLore", itemLoreConfig.class);

        LOGGER.info(MOD_ID + " Initialized");

        // Itemlore Command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("itemlore")
                    // Root: Apply lore or show stats if exists
                    .executes(context -> {
                        // feature is disabled
                        if (!itemLoreConfig.enabled) {
                            context.getSource().sendFailure(Component.literal("ItemLore is currently disabled."));
                            return 0;
                        }
                        //player doesnt exist
                        var player = context.getSource().getPlayer();
                        if (player == null)
                            return 0;

                        //item not in hand
                        ItemStack stack = player.getMainHandItem();
                        if (stack.isEmpty()) {
                            context.getSource().sendFailure(Component.literal("You must be holding an item."));
                            return 0;
                        }

                        // Try to apply lore
                        if (setLore.applyNewLore(player, stack)) {
                            context.getSource().sendSuccess(() -> Component.literal("Lore applied!"), false);
                            return 1;
                        } else {
                            // Lore exists, show all stats
                            return handleStatsCommand(context, StatMode.ALL);
                        }
                    })

                    // /itemlore apply
                    .then(Commands.literal("apply")
                            .executes(context -> {
                                if (!itemLoreConfig.enabled) {
                                    context.getSource()
                                            .sendFailure(Component.literal("ItemLore is currently disabled."));
                                    return 0;
                                }
                                var player = context.getSource().getPlayer();
                                if (player == null)
                                    return 0;
                                if (setLore.applyNewLore(player, player.getMainHandItem())) {
                                    context.getSource().sendSuccess(() -> Component.literal("Lore applied!"), false);
                                } else {
                                    context.getSource().sendFailure(Component.literal("Lore already exists."));
                                }
                                return 1;
                            }))

                    // /itemlore stats {all,kills,blocks}
                    .then(Commands.literal("stats")
                            .executes(context -> handleStatsCommand(context, StatMode.DEFAULT))
                            .then(Commands.literal("all")
                                    .executes(context -> handleStatsCommand(context, StatMode.ALL)))
                            .then(Commands.literal("kills")
                                    .executes(context -> handleStatsCommand(context, StatMode.MOBS)))
                            .then(Commands.literal("blocks")
                                    .executes(context -> handleStatsCommand(context, StatMode.BLOCKS))))

                    // /itemlore forceLore {all, nonstackables, off}
                    .then(Commands.literal("forceLore")
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(context -> {
                                switch (itemLoreConfig.forceLoreMode) {
                                    case OFF:
                                        itemLoreConfig.forceLoreMode = itemLoreConfig.ForceLoreMode.UNSTACKABLE;
                                        break;
                                    case UNSTACKABLE:
                                        itemLoreConfig.forceLoreMode = itemLoreConfig.ForceLoreMode.ALL;
                                        break;
                                    case ALL:
                                        itemLoreConfig.forceLoreMode = itemLoreConfig.ForceLoreMode.OFF;
                                        break;
                                }
                                context.getSource().sendSuccess(() -> Component.literal(
                                        "ForceLore Mode set to: " + itemLoreConfig.forceLoreMode), false);
                                return 1;
                            })
                            .then(Commands.literal("all")
                                    .executes(context -> setForceLore(context, itemLoreConfig.ForceLoreMode.ALL)))
                            .then(Commands.literal("nonstackables").executes(
                                    context -> setForceLore(context, itemLoreConfig.ForceLoreMode.UNSTACKABLE)))
                            .then(Commands.literal("off")
                                    .executes(context -> setForceLore(context, itemLoreConfig.ForceLoreMode.OFF))))

                    // /itemlore toggle {on/true, off/false}
                    .then(Commands.literal("toggle")
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(context -> {
                                itemLoreConfig.enabled = !itemLoreConfig.enabled;
                                context.getSource().sendSuccess(
                                        () -> Component.literal("ItemLore enabled: " + itemLoreConfig.enabled), false);
                                return 1;
                            })
                            .then(Commands.literal("on").executes(context -> setEnabled(context, true)))
                            .then(Commands.literal("true").executes(context -> setEnabled(context, true)))
                            .then(Commands.literal("off").executes(context -> setEnabled(context, false)))
                            .then(Commands.literal("false").executes(context -> setEnabled(context, false))))

                    // /itemlore remove
                    .then(Commands.literal("remove")
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(context -> {
                                var player = context.getSource().getPlayer();
                                if (player == null)
                                    return 0;
                                ItemStack stack = player.getMainHandItem();
                                if (!stack.isEmpty()) {
                                    // Remove lore component
                                    stack.set(DataComponents.LORE,
                                            new net.minecraft.world.item.component.ItemLore(List.of()));
                                    context.getSource().sendSuccess(() -> Component.literal("Lore removed."), true);
                                    return 1;
                                }
                                context.getSource().sendFailure(Component.literal("Hold an item."));
                                return 0;
                            }))

                    // /itemlore debug
                    .then(Commands.literal("debug")
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(context -> {
                                var player = context.getSource().getPlayer();
                                if (player == null)
                                    return 0;
                                ItemStack stack = player.getMainHandItem();
                                context.getSource().sendSystemMessage(
                                        Component.literal("Lore Component: " + stack.get(DataComponents.LORE)));
                                return 1;
                            })));
        });
    }

    private enum StatMode {
        DEFAULT, ALL, BLOCKS, MOBS
    }

    private static int setForceLore(CommandContext<CommandSourceStack> context, itemLoreConfig.ForceLoreMode mode) {
        itemLoreConfig.forceLoreMode = mode;
        context.getSource().sendSuccess(
                () -> Component.literal("ForceLore Mode set to: " + itemLoreConfig.forceLoreMode),
                false);
        return 1;
    }

    private static int setEnabled(CommandContext<CommandSourceStack> context, boolean enabled) {
        itemLoreConfig.enabled = enabled;
        context.getSource().sendSuccess(() -> Component.literal("ItemLore enabled: " + itemLoreConfig.enabled), false);
        return 1;
    }

    private static String formatName(String key) {
        if (key == null)
            return "";
        String[] words = key.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                sb.append(word.substring(1).toLowerCase());
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    private static int handleStatsCommand(CommandContext<CommandSourceStack> context, StatMode mode) {
        var player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Only players can use this command"));
            return 1;
        }
        ItemStack tool = player.getMainHandItem();
        if (tool.isEmpty() || !statTrackLore.hasLore(tool)) {
            player.displayClientMessage(Component.literal("You must be holding a tool with lore."), false);
            return 1;
        }
        Map<String, Integer> miningStats = statTrackLore.getMiningStats(tool);
        Map<String, Integer> killStats = statTrackLore.getKillStats(tool);
        if (miningStats.isEmpty() && killStats.isEmpty()) {
            player.displayClientMessage(Component.literal("No stats found."), false);
            return 0;
        }

        Map<String, Integer> displayStats;
        String header, totalText;
        int total;

        if (mode == StatMode.BLOCKS) {
            displayStats = miningStats;
            header = "=== Block Stats ===";
            total = miningStats.values().stream().mapToInt(Integer::intValue).sum();
            totalText = "Total Blocks: " + total;
        } else if (mode == StatMode.MOBS) {
            displayStats = killStats;
            header = "=== Mob Stats ===";
            total = killStats.values().stream().mapToInt(Integer::intValue).sum();
            totalText = "Total Kills: " + total;
        } else if (mode == StatMode.ALL) {
            displayStats = new HashMap<>(miningStats);
            displayStats.putAll(killStats);
            header = "=== All Stats ===";
            total = displayStats.values().stream().mapToInt(Integer::intValue).sum();
            totalText = "Total Actions: " + total;
        } else { // DEFAULT
            if (statTrackLore.isMiningTool(tool.getItem())) {
                displayStats = new HashMap<>();
                for (Map.Entry<String, Integer> entry : miningStats.entrySet()) {
                    String baseKey = statTrackLore.getBaseKey(entry.getKey());
                    displayStats.put(baseKey, displayStats.getOrDefault(baseKey, 0) + entry.getValue());
                }
                List<String> relevantBlocks = Arrays.asList(itemLoreConfig.relevantBlocks.split(","));
                displayStats.entrySet().removeIf(e -> !relevantBlocks.contains(e.getKey().trim()));
                header = "=== Mining Stats ===";
                total = displayStats.values().stream().mapToInt(Integer::intValue).sum();
                totalText = "Total Blocks: " + total;
            } else if (statTrackLore.isAttackTool(tool.getItem())) {
                total = killStats.values().stream().mapToInt(Integer::intValue).sum();
                player.displayClientMessage(Component.literal("=== Combat Stats ===").withStyle(ChatFormatting.GOLD), false);
                player.displayClientMessage(Component.literal("Total Kills: " + total).withStyle(ChatFormatting.AQUA), false);
                return 1;
            } else {
                player.displayClientMessage(Component.literal("No specific stats to show."), false);
                return 0;
            }
        }

        if (mode == StatMode.ALL) {
            // Display blocks and mobs separately
            if (!miningStats.isEmpty()) {
                player.displayClientMessage(Component.literal("=== Block Stats ===").withStyle(ChatFormatting.GOLD), false);
                List<String> keys = new ArrayList<>(miningStats.keySet());
                keys.sort(String.CASE_INSENSITIVE_ORDER);
                for (String key : keys) {
                    int count = miningStats.get(key);
                    player.displayClientMessage(Component.literal(formatName(key) + ": " + count).withStyle(ChatFormatting.AQUA), false);
                }
                int totalBlocks = miningStats.values().stream().mapToInt(Integer::intValue).sum();
                player.displayClientMessage(Component.literal("Total Blocks: " + totalBlocks).withStyle(ChatFormatting.YELLOW), false);
            }
            if (!killStats.isEmpty()) {
                player.displayClientMessage(Component.literal("=== Mob Stats ===").withStyle(ChatFormatting.GOLD), false);
                List<String> keys = new ArrayList<>(killStats.keySet());
                keys.sort(String.CASE_INSENSITIVE_ORDER);
                for (String key : keys) {
                    int count = killStats.get(key);
                    player.displayClientMessage(Component.literal(formatName(key) + ": " + count).withStyle(ChatFormatting.AQUA), false);
                }
                int totalMobs = killStats.values().stream().mapToInt(Integer::intValue).sum();
                player.displayClientMessage(Component.literal("Total Kills: " + totalMobs).withStyle(ChatFormatting.YELLOW), false);
            }
            if (!miningStats.isEmpty() || !killStats.isEmpty()) {
                int totalActions = miningStats.values().stream().mapToInt(Integer::intValue).sum()
                        + killStats.values().stream().mapToInt(Integer::intValue).sum();
                player.displayClientMessage(Component.literal("Total Actions: " + totalActions).withStyle(ChatFormatting.YELLOW), false);
            }
        } else {
            List<String> keys = new ArrayList<>(displayStats.keySet());
            keys.sort(String.CASE_INSENSITIVE_ORDER);
            player.displayClientMessage(Component.literal(header).withStyle(ChatFormatting.GOLD), false);
            for (String key : keys) {
                int count = displayStats.get(key);
                player.displayClientMessage(Component.literal(formatName(key) + ": " + count).withStyle(ChatFormatting.AQUA), false);
            }
            player.displayClientMessage(Component.literal(totalText).withStyle(ChatFormatting.YELLOW), false);
        }
        return 1;
    }
}
// to move the custom commands to a util folder, you would need to define
// everything the function does there, and initialize it into the
// commandregistration register here.
// or, in other words split it up a lot. might be nice if a lot of these are
// added.

// to move the custom commands to a util folder, you would need to define
// everything the function does there, and initialize it into the
// commandregistration register here.
// or, in other words split it up a lot. might be nice if a lot of these are
// added.