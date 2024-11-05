package net.quantumaidan.itemLore;

import net.fabricmc.api.ModInitializer;
import net.quantumaidan.itemLore.config.itemLoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemLore implements ModInitializer {
	public static final String MOD_ID = "itemLore";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		itemLoreConfig config = itemLoreConfig.loadConfig();

		LOGGER.info(MOD_ID + " Initialized");


	}
}