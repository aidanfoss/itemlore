package net.quantumaidan.itemLore.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.quantumaidan.itemLore.ItemLore;
import net.quantumaidan.itemLore.config.itemLoreConfig;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class setLore {

    public static boolean applyNewLore(PlayerEntity player, ItemStack itemStack) {
        if (!itemLoreConfig.enabled) return false;

        // Safe time zone handling
        TimeZone tz = TimeZone.getTimeZone(itemLoreConfig.timeZone);
        if (tz.getID().equals("GMT") && !itemLoreConfig.timeZone.equalsIgnoreCase("GMT")) {
            tz = TimeZone.getTimeZone("UTC");
            ItemLore.LOGGER.warn("[ItemLore] Invalid time zone '{}', defaulting to UTC", itemLoreConfig.timeZone);
        }

        // Safe date format handling
        String reportDate = "";
        try {
            DateFormat df = new SimpleDateFormat(itemLoreConfig.dateTimeFormatConfig);
            df.setTimeZone(tz);
            reportDate = df.format(new Date());

            if (reportDate.equals(itemLoreConfig.dateTimeFormatConfig)
                    || reportDate.matches("[AP]M?[0-9APM]*")) {
                throw new IllegalArgumentException("Formatted date is likely nonsense");
            }
        } catch (IllegalArgumentException e) {
            reportDate = "Invalid Date Format";
            ItemLore.LOGGER.warn("[ItemLore] Invalid date format '{}', using fallback text", itemLoreConfig.dateTimeFormatConfig);
        }

        LoreComponent inputLore = itemStack.get(DataComponentTypes.LORE);
        if (inputLore == null || inputLore.lines() == null || inputLore.lines().isEmpty()) {
            LoreComponent newLoreComponent = new LoreComponent(List.of(
                    Text.literal(reportDate).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)),
                    Text.literal("UID: ").append(player.getDisplayName()).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE))
            ));

            itemStack.set(DataComponentTypes.LORE, newLoreComponent);
            return true;
        }


        return false;
    }
}
