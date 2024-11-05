package net.quantumaidan.itemLore.util;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class itemLoreConfig {
    private static String dateTimeFormat = "MM/dd/yyyy hh:mm a";

    public static Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("My Mod Configuration"));

        ConfigCategory general = builder.getOrCreateCategory(Text.of("General"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Create and add the string option
        general.addEntry(entryBuilder
                .startStrField(Text.of("My String Option"), dateTimeFormat)
                .setDefaultValue("default") // Sets the default value
                .setSaveConsumer(newValue -> dateTimeFormat = newValue) // Saves the new value
                .build());

        return builder.build();
    }

    public static String getDTF() {
        return dateTimeFormat;
    }
}
