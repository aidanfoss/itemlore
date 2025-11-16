package net.quantumaidan.itemLore;

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
import com.mojang.brigadier.context.CommandContext;
import net.quantumaidan.itemLore.config.itemLoreConfig;
import net.quantumaidan.itemLore.util.setLore;
import net.quantumaidan.itemLore.util.statTrackLore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.lang.String.valueOf;

public class ItemLore implements ModInitializer {
	public static final String MOD_ID = "itemLore";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MidnightConfig.init("itemLore", itemLoreConfig.class);


		LOGGER.info(MOD_ID + " Initialized");

		//ApplyLore Command
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("applylore")
					.executes(context -> {
						if (context.getSource().getPlayer() == null){ //not a player
							context.getSource().sendError(Text.literal("Something attempted to run ApplyLore"));
							return 1;
						}
						else if (!itemLoreConfig.enabled){ //feature is disabled
							context.getSource().getPlayer().sendMessage(Text.literal("ItemLore is currently disabled."), false);
							return 1;
						}
						else if (context.getSource().getPlayer().getMainHandStack() == null){ //player is not holding an item
							context.getSource().getPlayer().sendMessage(Text.literal("You are not holding anything!"), false);
							return 1;
						}
						else if (context.getSource().getPlayer().getMainHandStack() != null){ //player is holding item
							if (setLore.applyNewLore(context.getSource().getPlayer(), Objects.requireNonNull(context.getSource().getPlayer()).getMainHandStack())){
								context.getSource().sendMessage(Text.literal("Lore applied!"));
								return 1;
							}
							else {
								context.getSource().sendMessage(Text.literal("Lore already exists."));
								return 1;
							}
						}
						context.getSource().sendError(Text.literal("Error: fell off the end of the function."));
						context.getSource().getPlayer().sendMessage(Text.literal("lore application attempted"), false);
						context.getSource().getPlayer().sendMessage(Text.literal(context.getSource().getPlayer().toString()), false);
						context.getSource().getPlayer().sendMessage(Text.literal(context.getSource().getPlayer().getMainHandStack().toString()), false);
						return 1;
					}));
		});

		//getComponents
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("getComponents")
					.requires(source -> source.hasPermissionLevel(2))
					.executes(context -> {
						ItemStack stack = Objects.requireNonNull(context.getSource().getPlayer()).getMainHandStack();
						LoreComponent loreComponent = new LoreComponent(List.of());
						context.getSource().getPlayer().sendMessage(Text.literal(stack.get(DataComponentTypes.LORE).toString()));
						context.getSource().getPlayer().sendMessage(Text.literal(loreComponent.toString()));
						return 0;
					}));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("toggleItemLore")
					.requires(source -> source.hasPermissionLevel(2))
					.executes(context -> {
						itemLoreConfig.enabled = !itemLoreConfig.enabled;
						context.getSource().sendFeedback(() -> Text.literal("ItemLore Toggle set to: " + itemLoreConfig.enabled), false);
						return 1;
					}));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("stats")
					.executes(context -> handleStatsCommand(context, StatMode.DEFAULT))
					.then(CommandManager.literal("all")
							.executes(context -> handleStatsCommand(context, StatMode.ALL)))
					.then(CommandManager.literal("blocks")
							.executes(context -> handleStatsCommand(context, StatMode.BLOCKS)))
					.then(CommandManager.literal("mobs")
							.executes(context -> handleStatsCommand(context, StatMode.MOBS))));
		});
	}

	private enum StatMode { DEFAULT, ALL, BLOCKS, MOBS }

	private static String formatName(String key) {
		if (key == null) return "";
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
		if (tool.isEmpty() || statTrackLore.hasLore(tool)) {
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
				int totalActions = miningStats.values().stream().mapToInt(Integer::intValue).sum() + killStats.values().stream().mapToInt(Integer::intValue).sum();
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
//to move the custom commands to a util folder, you would need to define everything the function does there, and initialize it into the commandregistration register here.
//or, in other words split it up a lot. might be nice if a lot of these are added.

//to move the custom commands to a util folder, you would need to define everything the function does there, and initialize it into the commandregistration register here.
//or, in other words split it up a lot. might be nice if a lot of these are added.
