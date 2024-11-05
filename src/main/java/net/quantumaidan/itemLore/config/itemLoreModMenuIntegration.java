package net.quantumaidan.itemLore.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.quantumaidan.itemLore.ItemLore;
import net.quantumaidan.itemLore.config.itemLoreConfigScreen;


public class itemLoreModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        //ItemLore.LOGGER.info("ItemLore ConfigScreenFactory Called------------------------");
        return parent -> new itemLoreConfigScreen().createScreen(parent);
    }
}
