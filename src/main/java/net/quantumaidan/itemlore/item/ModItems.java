package net.quantumaidan.itemlore.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
//import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.quantumaidan.itemlore.ItemLore;

public class ModItems {
    //im following https://www.youtube.com/watch?v=5ms6RiR4SQ4&list=PLKGarocXCE1EO43Dlf5JGh7Yk-kRAXUEJ&index=2
    //KaupenJoes Tutorial for making mods, not sure if this is committed, im also learning about GIT
    public static final Item RUBY = registerItem("ruby", new Item(new FabricItemSettings()));

    public static void addItemsToIngredientTabItemsGroup(FabricItemGroupEntries entries){
        entries.add(RUBY);
    }
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(ItemLore.MOD_ID, name), item);
    }

    public static void registerModItems() {
        ItemLore.LOGGER.info("registering mod items for " + ItemLore.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemsToIngredientTabItemsGroup);
    }
}
