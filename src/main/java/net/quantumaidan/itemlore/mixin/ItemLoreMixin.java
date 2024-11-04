package net.quantumaidan.itemlore.mixin;

import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.component.*;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static net.quantumaidan.itemlore.util.setLore.setLore;

@Mixin(AnvilScreenHandler.class)
public abstract class ItemLoreMixin extends ForgingScreenHandler {
    public ItemLoreMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Shadow protected abstract ForgingSlotsManager getForgingSlotsManager();

    @Unique
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    @Unique
    Date today = Calendar.getInstance().getTime();
    @Unique
    String reportDate = df.format(today);

    @Inject(at = @At("TAIL"), method = "updateResult")
    private void init(CallbackInfo info) {
        //1. get the itemStack that is the output of the anvil, we need to edit this itemStack to add the lore
        ItemStack itemStack = this.output.getStack(3);

        //2. grab the existing LoreComponent, if it exists
        LoreComponent loreComponent = itemStack.get(DataComponentTypes.LORE);

        //3. check to see if the loreComponent is empty. This means the item we are editing doesn't have an LC yet, and we can proceed with adding the date, time, and displayname
        if (loreComponent == null){
            //4. Create the new LoreComponent that will be applied to the itemStack
            LoreComponent newLoreComponent = new LoreComponent(List.of(
                    Text.literal(reportDate),
                    Text.literal("UID: ").append(this.player.getDisplayName())
            ));

            //5. Add the new LoreComponent to the itemStack
            itemStack.set(DataComponentTypes.LORE, newLoreComponent);
        }
    }
}


//lore.lines().set(1,Text.empty().append(reportDate).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)));
//Objects.requireNonNull(itemStack.get(DataComponentTypes.LORE)).lines().set(1,Text.empty().append(reportDate));
//lore.lines().set(2,Text.literal("UID: ").append(this.player.getDisplayName()).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)));
