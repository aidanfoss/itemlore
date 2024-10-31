package net.quantumaidan.itemlore.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Mixin(AnvilScreenHandler.class)
abstract class ItemLoreMixin extends ForgingScreenHandler {
    public ItemLoreMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    //Create a string called reportDate that is formatted like MM/dd/yyyy HH:mm
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    Date today = Calendar.getInstance().getTime();
    String reportDate = df.format(today);

    
    @Inject(at = @At("TAIL"), method = "updateResult") //inject at the end of the function that updates the output of the anvil itemstack. In other words, edit the code that edits the item
    private void init(CallbackInfo info) {
        ItemStack itemStack = this.output.getStack(3); //define itemStack as the output of the anvil

        NbtList lore = itemStack.getOrCreateSubNbt("display").getList("Lore", NbtElement.STRING_TYPE);
        if (lore.isEmpty()) { //if the item has no lore
            lore.add(NbtString.of(Text.Serializer.toJson(Text.empty().append(reportDate).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE))))); //add the date and time
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("UID: ").append(this.player.getDisplayName()).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE))))); //add the UID of the player
            itemStack.getOrCreateSubNbt("display").put("Lore", lore);
        }
    }
}
