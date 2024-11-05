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

        BooleanListEntry enabled = entryBuilder
                .startBooleanToggle(Text.literal("Force Join"), config.getEnabled())
                .setTooltip(Text.literal("Whether ItemLore should apply to anvilled items or not"))
                .build();

        StringListEntry dateTimeFormatConfig = entryBuilder
                .startStrField(Text.literal("DateTime Format"), config.getDateTimeFormatConfig())
                .setTooltip(Text.literal("uses SimpleDateFormat (ex 'HH:mm' will do military time, 'hh:mm a' will do AM/PM"))
                .build();

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        general.addEntry(enabled);
        general.addEntry(dateTimeFormatConfig);


        builder.setSavingRunnable(() -> {
            config.setEnabled(enabled.getValue());
            config.setDateTimeFormatConfig(dateTimeFormatConfig.getValue());

        });

        return builder.build();
    }
}