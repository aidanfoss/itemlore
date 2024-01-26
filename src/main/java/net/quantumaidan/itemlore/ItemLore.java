package net.quantumaidan.itemlore;

import net.fabricmc.api.ModInitializer;

import net.quantumaidan.itemlore.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemLore implements ModInitializer {
	public static final String MOD_ID = "itemlore";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");

		ModItems.registerModItems();
	}
}