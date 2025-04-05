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

    private static DateFormat cachedDateFormat = null;
    private static TimeZone cachedTimeZone = null;
    private static boolean hasLoggedFormatError = false;
    private static boolean hasLoggedZoneError = false;
    private static itemLoreConfig cachedConfig = null;

    public static boolean applyNewLore(PlayerEntity player, ItemStack itemStack) {
        if (!itemLoreConfig.enabled) return false;

        // Set up timezone once
        if (cachedTimeZone == null) {
            TimeZone tz = TimeZone.getTimeZone(itemLoreConfig.timeZone);
            if (tz.getID().equals("GMT") && !itemLoreConfig.timeZone.equalsIgnoreCase("GMT")) {
                tz = TimeZone.getTimeZone("UTC");
                if (!hasLoggedZoneError) {
                    ItemLore.LOGGER.warn("[ItemLore] Invalid time zone '{}', defaulting to UTC", itemLoreConfig.timeZone);
                    hasLoggedZoneError = true;
                }
            }
            cachedTimeZone = tz;
        }

        // Set up date format once
        if (cachedDateFormat == null) {
            try {
                SimpleDateFormat df = new SimpleDateFormat(itemLoreConfig.dateTimeFormatConfig);
                df.setTimeZone(cachedTimeZone);
                String preview = df.format(new Date());
                if (preview.equals(itemLoreConfig.dateTimeFormatConfig) || preview.matches("[AP]M?[0-9APM]*")) {
                    throw new IllegalArgumentException("Garbage-looking output");
                }
                cachedDateFormat = df;
            } catch (IllegalArgumentException e) {
                if (!hasLoggedFormatError) {
                    ItemLore.LOGGER.warn("[ItemLore] Invalid date format '{}', using fallback", itemLoreConfig.dateTimeFormatConfig);
                    hasLoggedFormatError = true;
                }
                SimpleDateFormat fallback = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                fallback.setTimeZone(cachedTimeZone);
                cachedDateFormat = fallback;
            }
        }
        // Format the date using cached, validated formatter
        String reportDate = cachedDateFormat.format(new Date());

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

    public static boolean reloadConfig() {
        cachedDateFormat = null;
        cachedTimeZone = null;
        hasLoggedZoneError = false;
        hasLoggedFormatError = false;
        return true;
    }
}
