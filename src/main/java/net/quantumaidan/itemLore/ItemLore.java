package net.quantumaidan.itemLore;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.quantumaidan.itemLore.config.itemLoreConfig;
import net.quantumaidan.itemLore.util.setLore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

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
	}
}

//to move the custom commands to a util folder, you would need to define everything the function does there, and initialize it into the commandregistration register here.
//or, in other words split it up a lot. might be nice if a lot of these are added.
