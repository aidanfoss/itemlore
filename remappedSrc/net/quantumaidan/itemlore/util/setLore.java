package net.quantumaidan.itemLore.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class setLore {
    public static void setLore(ItemStack stack, int lineIndex, Text loreText) {
        LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);

        //if null
        if (lore == null)
            lore = new LoreComponent(new ArrayList<>());

        //get all lores from the array
        List<Text> allLoreLines = new ArrayList<Text>(lore.lines());

        //should fill empty line with null?
        int currentLoreSize = allLoreLines.size();
        while (lineIndex > currentLoreSize) {
            currentLoreSize++;
            allLoreLines.add(Text.of(" "));
        }

        //replace target lore into this item
        allLoreLines.set(lineIndex - 1, loreText);

        //just replace the obj
        lore = new LoreComponent(allLoreLines);
        stack.set(DataComponentTypes.LORE, lore);
    }
}
