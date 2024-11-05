package net.quantumaidan.itemLore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.quantumaidan.itemLore.util.setLore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ItemLore implements ModInitializer {
	public static final String MOD_ID = "itemLore";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("ItemLore Initialized");

		//ApplyLore Command
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("applylore")
					.executes(context -> {
						if (context.getSource().getPlayer() == null){ //not a player
							context.getSource().sendError(Text.literal("Something attempted to run ApplyLore"));
						}
						else if (context.getSource().getPlayer().getActiveItem() == null){ //player is not holding an item
							context.getSource().getPlayer().sendMessage(Text.literal("You are not holding anything!"), false);
						}
						else { //player is holding item
							if (setLore.applyNewLore(context.getSource().getPlayer(), Objects.requireNonNull(context.getSource().getPlayer()).getActiveItem())){
								context.getSource().sendMessage(Text.literal("Lore applied!"));
							}
							else {
								context.getSource().sendMessage(Text.literal("Lore already exists."));
							}
						}
						return 1;
					}));
		});
	}
}