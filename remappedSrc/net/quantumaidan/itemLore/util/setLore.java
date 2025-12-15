package net.quantumaidan.itemLore.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.quantumaidan.itemLore.ItemLore;
import net.quantumaidan.itemLore.config.itemLoreConfig;
import java.util.List;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class setLore {

    public static boolean applyNewLore(Player player, ItemStack itemStack) {
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

        net.minecraft.world.item.component.ItemLore inputLore = itemStack.get(DataComponents.LORE);
        if (inputLore == null || inputLore.lines() == null || inputLore.lines().isEmpty()) {
            net.minecraft.world.item.component.ItemLore newLoreComponent = new net.minecraft.world.item.component.ItemLore(List.of(
                    Component.literal(reportDate).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)),
                    Component.literal("UID: ").append(player.getDisplayName()).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE))
            ));

            itemStack.set(DataComponents.LORE, newLoreComponent);
            return true;
        }


        return false;
    }
}
