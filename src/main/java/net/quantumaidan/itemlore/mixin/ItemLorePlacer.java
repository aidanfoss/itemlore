package net.quantumaidan.itemlore.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
//import net.minecraft.item.ItemUsage;
//import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import net.quantumaidan.itemlore.ItemLore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Mixin(AnvilScreenHandler.class)

public class ItemLorePlacer{
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    Date today = Calendar.getInstance().getTime();
    String reportDate = df.format(today);
    public final DefaultedList<Slot> slots = DefaultedList.of();
    //private String newItemName;
    public static final Logger LOGGER = LoggerFactory.getLogger(ItemLore.MOD_ID);
    public Slot getSlot(int index) {
        return this.slots.get(index);
    }
    @Inject(method = "setNewItemName", at = @At("HEAD"))
    private void injectMethod(){
        LOGGER.info("Injected = True"); //what
        if (this.getSlot(2).hasStack()) {
            LOGGER.info("hasStack = true");
            ItemStack itemStack = this.getSlot(2).getStack();

            NbtList lore = itemStack.getOrCreateSubNbt("display").getList("Lore", NbtElement.STRING_TYPE);
            lore.add(NbtString.of(Text.Serializer.toJson(Text.empty().append(reportDate).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)))));
            itemStack.getOrCreateSubNbt("display").put("Lore", lore);
        }
    }



}
