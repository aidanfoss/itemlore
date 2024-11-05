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

public class setLore {
    public static boolean applyNewLore(PlayerEntity player, ItemStack itemStack) {
        itemLoreConfig config = itemLoreConfig.loadConfig();
        DateFormat df = new SimpleDateFormat(itemLoreConfig.loadConfig().getDateTimeFormatConfig());
        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);

        LoreComponent inputLore = itemStack.get(DataComponentTypes.LORE);
        if (inputLore == null) {
            inputLore = new LoreComponent(List.of());//fix null issue
        }
        if (inputLore.equals(new LoreComponent(List.of())) && config.getEnabled()) {
            //4. Create the new LoreComponent that will be applied to the itemStack
            LoreComponent newLoreComponent = new LoreComponent(List.of(Text.literal(reportDate).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)), Text.literal("UID: ").append(player.getDisplayName()).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE))));

            //5. Add the new LoreComponent to the itemStack
            itemStack.set(DataComponentTypes.LORE, newLoreComponent);

            return true;
        }
        return false;
    }
}


//if (itemStack.get(DataComponentTypes.LORE) == null|!Objects.requireNonNull(itemStack.get(DataComponentTypes.LORE)).toString().contains("UID: ")){
    //4. Create the new LoreComponent that will be applied to the itemStack
    // LoreComponent newLoreComponent = new LoreComponent(List.of(
        // Text.literal(reportDate),
        // Text.literal("UID: ").append(player.getDisplayName())
    // ));

    //5. Add the new LoreComponent to the itemStack
    // itemStack.set(DataComponentTypes.LORE, newLoreComponent);
// }