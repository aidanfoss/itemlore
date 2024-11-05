package net.quantumaidan.itemLore.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.text.Text;
import net.quantumaidan.itemLore.util.setLore;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;




@Mixin(AnvilScreenHandler.class)
public abstract class ItemLoreMixin extends ForgingScreenHandler {
    public ItemLoreMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ForgingSlotsManager forgingSlotsManager) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Inject(at = @At("TAIL"), method = "onTakeOutput")
    private void init(PlayerEntity player, ItemStack itemStack, CallbackInfo ci) {
        setLore.applyNewLore(player, itemStack);
    }



    @Inject(at = @At("TAIL"), method = "updateResult")
    private void init(CallbackInfo ci) {
        //1. get the itemStack that is the output of the anvil, we need to edit this itemStack to add the lore
        ItemStack itemStack = this.output.getStack(3);

        //3. attempt to setLore on given itemStack
        setLore.applyNewLore(player, itemStack);
    }

    @Unique
    private static void applyLore(PlayerEntity player, ItemStack itemStack) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);
        //If the passed item doesn't have any lore, or has lore, but it's for some reason empty, then apply the datetime+UID lore tag
        if (itemStack.get(DataComponentTypes.LORE) == null){
            itemStack.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal(reportDate), Text.literal("UID: ").append(player.getDisplayName()))));
        }
    }
}


//lore.lines().set(1,Text.empty().append(reportDate).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)));
//Objects.requireNonNull(itemStack.get(DataComponentTypes.LORE)).lines().set(1,Text.empty().append(reportDate));
//lore.lines().set(2,Text.literal("UID: ").append(this.player.getDisplayName()).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)));
