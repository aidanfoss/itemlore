package net.quantumaidan.itemLore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

	@Override
	public void onInitialize() {
		MidnightConfig.init("itemLore", itemLoreConfig.class);

		LOGGER.info(MOD_ID + " Initialized");

		// ApplyLore Command
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("applylore")
					.executes(context -> {
						if (context.getSource().getPlayer() == null) { // not a player
							context.getSource().sendFailure(Component.literal("Something attempted to run ApplyLore"));
							return 1;
						} else if (!itemLoreConfig.enabled) { // feature is disabled
							context.getSource().getPlayer().displayClientMessage(Component.literal("ItemLore is currently disabled."),
									false);
							return 1;
						} else if (context.getSource().getPlayer().getMainHandItem() == null) { // player is not
																									// holding an item
							context.getSource().getPlayer().displayClientMessage(Component.literal("You are not holding anything!"),
									false);
							return 1;
						} else if (context.getSource().getPlayer().getMainHandItem() != null) { // player is holding
																									// item
							if (setLore.applyNewLore(context.getSource().getPlayer(),
									Objects.requireNonNull(context.getSource().getPlayer()).getMainHandItem())) {
								context.getSource().sendSystemMessage(Component.literal("Lore applied!"));
								return 1;
							} else {
								context.getSource().sendSystemMessage(Component.literal("Lore already exists."));
								return 1;
							}
						}
						context.getSource().sendFailure(Component.literal("Error: fell off the end of the function."));
						context.getSource().getPlayer().displayClientMessage(Component.literal("lore application attempted"), false);
						context.getSource().getPlayer()
								.displayClientMessage(Component.literal(context.getSource().getPlayer().toString()), false);
						context.getSource().getPlayer().displayClientMessage(
								Component.literal(context.getSource().getPlayer().getMainHandItem().toString()), false);
						return 1;
					}));
		});

		// getComponents
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("getComponents")
					.requires(source -> source.hasPermission(2))
					.executes(context -> {
						ItemStack stack = Objects.requireNonNull(context.getSource().getPlayer()).getMainHandItem();
						net.minecraft.world.item.component.ItemLore loreComponent = new net.minecraft.world.item.component.ItemLore(List.of());
						context.getSource().getPlayer()
								.sendSystemMessage(Component.literal(stack.get(DataComponents.LORE).toString()));
						context.getSource().getPlayer().sendSystemMessage(Component.literal(loreComponent.toString()));
						return 0;
					}));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("toggleItemLore")
					.requires(source -> source.hasPermission(2))
					.executes(context -> {
						itemLoreConfig.enabled = !itemLoreConfig.enabled;
						context.getSource().sendSuccess(
								() -> Component.literal("ItemLore Toggle set to: " + itemLoreConfig.enabled), false);
						return 1;
					}));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("toggleForceLore")
					.requires(source -> source.hasPermission(2))
					.executes(context -> {
						itemLoreConfig.forceLore = !itemLoreConfig.forceLore;
						context.getSource().sendSuccess(
								() -> Component.literal("ForceLore Toggle set to: " + itemLoreConfig.forceLore), false);
						return 1;
					}));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("stats")
					.executes(context -> handleStatsCommand(context, StatMode.DEFAULT))
					.then(Commands.literal("all")
							.executes(context -> handleStatsCommand(context, StatMode.ALL)))
					.then(Commands.literal("blocks")
							.executes(context -> handleStatsCommand(context, StatMode.BLOCKS)))
					.then(Commands.literal("mobs")
							.executes(context -> handleStatsCommand(context, StatMode.MOBS))));
		});
	}

	private enum StatMode {
		DEFAULT, ALL, BLOCKS, MOBS
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
