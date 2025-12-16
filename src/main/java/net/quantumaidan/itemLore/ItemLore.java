package net.quantumaidan.itemLore;

import com.mojang.brigadier.context.CommandContext;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.quantumaidan.itemLore.config.itemLoreConfig;
import net.quantumaidan.itemLore.util.setLore;
import net.quantumaidan.itemLore.util.statTrackLore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemLore implements ModInitializer {
	public static final String MOD_ID = "itemLore";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MidnightConfig.init("itemLore", itemLoreConfig.class);
		LOGGER.info(MOD_ID + " Initialized");

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("itemlore") // Root command: /itemlore
					.executes(ItemLore::handleRootCommand) // Executes when just /itemlore is typed

					// Subcommand: /itemlore apply
					.then(CommandManager.literal("apply")
							.executes(ItemLore::handleApplyCommand))

					// Subcommand: /itemlore stats {all,kills,blocks}
					.then(CommandManager.literal("stats")
							.executes(context -> handleStatsCommand(context, StatMode.DEFAULT))
							.then(CommandManager.literal("all")
									.executes(context -> handleStatsCommand(context, StatMode.ALL)))
							.then(CommandManager.literal("kills")
									.executes(context -> handleStatsCommand(context, StatMode.MOBS)))
							.then(CommandManager.literal("blocks")
									.executes(context -> handleStatsCommand(context, StatMode.BLOCKS))))

					// Subcommand: /itemlore forceLore {on, off}
					.then(CommandManager.literal("forceLore")
							.requires(source -> source.hasPermissionLevel(2)) // Requires op level 2
							.executes(ItemLore::handleForceLoreToggleCommand) // Toggles between modes
							.then(CommandManager.literal("on")
									.executes(context -> setForceLore(context, true)))
							.then(CommandManager.literal("off")
									.executes(context -> setForceLore(context, false))))

					// Subcommand: /itemlore toggle {on/true, off/false}
					.then(CommandManager.literal("toggle")
							.requires(source -> source.hasPermissionLevel(2)) // Requires op level 2
							.executes(ItemLore::handleToggleCommand) // Toggles enabled state
							.then(CommandManager.literal("on").executes(context -> setEnabled(context, true)))
							.then(CommandManager.literal("true").executes(context -> setEnabled(context, true)))
							.then(CommandManager.literal("off").executes(context -> setEnabled(context, false)))
							.then(CommandManager.literal("false").executes(context -> setEnabled(context, false))))

					// Subcommand: /itemlore remove
					.then(CommandManager.literal("remove")
							.requires(source -> source.hasPermissionLevel(2)) // Requires op level 2
							.executes(ItemLore::handleRemoveCommand))

					// Subcommand: /itemlore debug
					.then(CommandManager.literal("debug")
							.requires(source -> source.hasPermissionLevel(2)) // Requires op level 2
							.executes(ItemLore::handleDebugCommand)));
		});
	}

	private enum StatMode {
		DEFAULT, ALL, BLOCKS, MOBS
	}

	// Command Handlers

	private static int handleRootCommand(CommandContext<ServerCommandSource> context) {
		if (!itemLoreConfig.enabled) {
			context.getSource().sendError(Text.literal("ItemLore is currently disabled."));
			return 0;
		}
		var player = context.getSource().getPlayer();
		if (player == null) return 0;

		ItemStack stack = player.getMainHandStack();
		if (stack.isEmpty()) {
			context.getSource().sendError(Text.literal("You must be holding an item."));
			return 0;
		}

		if (setLore.applyNewLore(player, stack)) {
			context.getSource().sendFeedback(() -> Text.literal("Lore applied!"), false);
			return 1;
		} else {
			return handleStatsCommand(context, StatMode.ALL);
		}
	}

	private static int handleApplyCommand(CommandContext<ServerCommandSource> context) {
		if (!itemLoreConfig.enabled) {
			context.getSource().sendError(Text.literal("ItemLore is currently disabled."));
			return 0;
		}
		var player = context.getSource().getPlayer();
		if (player == null) return 0;

		if (setLore.applyNewLore(player, player.getMainHandStack())) {
			context.getSource().sendFeedback(() -> Text.literal("Lore applied!"), false);
		} else {
			context.getSource().sendError(Text.literal("Lore already exists."));
		}
		return 1;
	}

	private static int handleForceLoreToggleCommand(CommandContext<ServerCommandSource> context) {
		itemLoreConfig.forceLore = !itemLoreConfig.forceLore;
		context.getSource().sendFeedback(() -> Text.literal(
				"ForceLore Mode set to: " + (itemLoreConfig.forceLore ? "ON" : "OFF")), false);
		MidnightConfig.write("itemLore");
		return 1;
	}

	private static int handleToggleCommand(CommandContext<ServerCommandSource> context) {
		itemLoreConfig.enabled = !itemLoreConfig.enabled;
		context.getSource().sendFeedback(
				() -> Text.literal("ItemLore enabled: " + itemLoreConfig.enabled), false);
		MidnightConfig.write("itemLore");
		return 1;
	}

	private static int handleRemoveCommand(CommandContext<ServerCommandSource> context) {
		var player = context.getSource().getPlayer();
		if (player == null) return 0;
		ItemStack stack = player.getMainHandStack();
		if (!stack.isEmpty()) {
			stack.set(DataComponentTypes.LORE, new LoreComponent(List.of()));
			context.getSource().sendFeedback(() -> Text.literal("Lore removed."), true);
			return 1;
		}
		context.getSource().sendError(Text.literal("Hold an item."));
		return 0;
	}

	private static int handleDebugCommand(CommandContext<ServerCommandSource> context) {
		var player = context.getSource().getPlayer();
		if (player == null) return 0;
		ItemStack stack = player.getMainHandStack();
		context.getSource().sendFeedback(
				() -> Text.literal("Lore Component: " + stack.get(DataComponentTypes.LORE)), false);
		return 1;
	}

	private static int setForceLore(CommandContext<ServerCommandSource> context, boolean enabled) {
		itemLoreConfig.forceLore = enabled;
		context.getSource().sendFeedback(
				() -> Text.literal("ForceLore Mode set to: " + (itemLoreConfig.forceLore ? "ON" : "OFF")),
				false);
		MidnightConfig.write("itemLore");
		return 1;
	}

	private static int setEnabled(CommandContext<ServerCommandSource> context, boolean enabled) {
		itemLoreConfig.enabled = enabled;
		context.getSource().sendFeedback(() -> Text.literal("ItemLore enabled: " + itemLoreConfig.enabled), false);
		MidnightConfig.write("itemLore");
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

	private static int handleStatsCommand(CommandContext<ServerCommandSource> context, StatMode mode) {
		var player = context.getSource().getPlayer();
		if (player == null) {
			context.getSource().sendError(Text.literal("Only players can use this command"));
			return 1;
		}
		ItemStack tool = player.getMainHandStack();
		if (tool.isEmpty() || !statTrackLore.hasLore(tool)) {
			player.sendMessage(Text.literal("You must be holding a tool with lore."), false);
			return 1;
		}
		Map<String, Integer> miningStats = statTrackLore.getMiningStats(tool);
		Map<String, Integer> killStats = statTrackLore.getKillStats(tool);
		if (miningStats.isEmpty() && killStats.isEmpty()) {
			player.sendMessage(Text.literal("No stats found."), false);
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
				player.sendMessage(Text.literal("=== Combat Stats ===").formatted(Formatting.GOLD), false);
				player.sendMessage(Text.literal("Total Kills: " + total).formatted(Formatting.AQUA), false);
				return 1;
			} else {
				player.sendMessage(Text.literal("No specific stats to show."), false);
				return 0;
			}
		}

		if (mode == StatMode.ALL) {
			// Display blocks and mobs separately
			if (!miningStats.isEmpty()) {
				player.sendMessage(Text.literal("=== Block Stats ===").formatted(Formatting.GOLD), false);
				List<String> keys = new ArrayList<>(miningStats.keySet());
				keys.sort(String.CASE_INSENSITIVE_ORDER);
				for (String key : keys) {
					int count = miningStats.get(key);
					player.sendMessage(Text.literal(formatName(key) + ": " + count).formatted(Formatting.AQUA), false);
				}
				int totalBlocks = miningStats.values().stream().mapToInt(Integer::intValue).sum();
				player.sendMessage(Text.literal("Total Blocks: " + totalBlocks).formatted(Formatting.YELLOW), false);
			}
			if (!killStats.isEmpty()) {
				player.sendMessage(Text.literal("=== Mob Stats ===").formatted(Formatting.GOLD), false);
				List<String> keys = new ArrayList<>(killStats.keySet());
				keys.sort(String.CASE_INSENSITIVE_ORDER);
				for (String key : keys) {
					int count = killStats.get(key);
					player.sendMessage(Text.literal(formatName(key) + ": " + count).formatted(Formatting.AQUA), false);
				}
				int totalMobs = killStats.values().stream().mapToInt(Integer::intValue).sum();
				player.sendMessage(Text.literal("Total Kills: " + totalMobs).formatted(Formatting.YELLOW), false);
			}
			if (!miningStats.isEmpty() || !killStats.isEmpty()) {
				int totalActions = miningStats.values().stream().mapToInt(Integer::intValue).sum()
						+ killStats.values().stream().mapToInt(Integer::intValue).sum();
				player.sendMessage(Text.literal("Total Actions: " + totalActions).formatted(Formatting.YELLOW), false);
			}
		} else {
			List<String> keys = new ArrayList<>(displayStats.keySet());
			keys.sort(String.CASE_INSENSITIVE_ORDER);
			player.sendMessage(Text.literal(header).formatted(Formatting.GOLD), false);
			for (String key : keys) {
				int count = displayStats.get(key);
				player.sendMessage(Text.literal(formatName(key) + ": " + count).formatted(Formatting.AQUA), false);
			}
			player.sendMessage(Text.literal(totalText).formatted(Formatting.YELLOW), false);
		}
		return 1;
	}
}
