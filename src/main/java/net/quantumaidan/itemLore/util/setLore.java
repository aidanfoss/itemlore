package net.quantumaidan.itemLore.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

        // Use the static config field from MidnightLib
        DateFormat df = new SimpleDateFormat(itemLoreConfig.dateTimeFormatConfig);
        df.setTimeZone(TimeZone.getTimeZone(itemLoreConfig.timeZone));

        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);

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
