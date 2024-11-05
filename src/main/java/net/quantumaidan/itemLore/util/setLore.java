package net.quantumaidan.itemLore.util;


import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class setLore {
    public static boolean applyNewLore(PlayerEntity player, ItemStack itemStack) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);
        //If the passed item doesn't have any lore, or has lore, but it's for some reason empty, then apply the datetime+UID lore tag
//        if (itemStack.get(DataComponentTypes.LORE) == null|!Objects.requireNonNull(itemStack.get(DataComponentTypes.LORE)).toString().contains("UID: ")){
//            itemStack.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal(reportDate), Text.literal("UID: ").append(player.getDisplayName()))));
//            return true;
//        }
        if (itemStack.get(DataComponentTypes.LORE) == null|(Objects.requireNonNull(itemStack.get(DataComponentTypes.LORE))).toString().contains("UID: ")){
            //4. Create the new LoreComponent that will be applied to the itemStack
            LoreComponent newLoreComponent = new LoreComponent(List.of(Text.literal(reportDate), Text.literal("UID: ").append(player.getDisplayName())));

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