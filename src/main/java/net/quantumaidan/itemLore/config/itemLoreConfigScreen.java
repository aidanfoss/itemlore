package net.quantumaidan.itemLore.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class itemLoreConfigScreen {
    public Screen createScreen(Screen parent) {
        itemLoreConfig config = itemLoreConfig.loadConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("ItemLore Config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        builder.getOrCreateCategory(Text.of("General"))
                .addEntry(entryBuilder
                        .startBooleanToggle(Text.of("Enable ItemLore"), config.getEnabled())
                        .setDefaultValue(true) // default value
                        .setTooltip(Text.of("Enable or disable ItemLore"))
                        .setSaveConsumer(config::setEnabled) // save value to config
                        .build());

        // String field option
        builder.getOrCreateCategory(Text.of("General"))
                .addEntry(entryBuilder
                        .startStrField(Text.of("Date Time Format"), config.getDateTimeFormatConfig())
                        .setDefaultValue("MM/dd/yyyy hh:mm a") // default value
                        .setTooltip(Text.of("Format for date and time in item lore"))
                        .setSaveConsumer(config::setDateTimeFormatConfig) // save value to config
                        .build());


        builder.setSavingRunnable(config::saveConfig);

        return builder.build();
    }
}