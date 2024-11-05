package net.quantumaidan.itemLore.mixin;

import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static net.quantumaidan.itemLore.util.setLore.setLore;

@Mixin(AnvilScreenHandler.class)
abstract class ItemLoreMixin extends ForgingScreenHandler {
    public ItemLoreMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Unique
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    @Unique
    Date today = Calendar.getInstance().getTime();
    @Unique
    String reportDate = df.format(today);

    @Inject(at = @At("TAIL"), method = "updateResult")
    private void init(CallbackInfo info) {
        ItemStack itemStack = this.output.getStack(3);

        LoreComponent lore = itemStack.get(DataComponentTypes.LORE);

        Log.debug(LogCategory.LOG,"before the if statement");
        if ((lore != null) && (lore.lines().isEmpty())) {

            Text dateLore = Text.of(reportDate);
            Text nameLore = Text.literal("UID: ").append(this.player.getDisplayName());

            setLore(itemStack, 1, dateLore);
            setLore(itemStack,2,nameLore);

        }
    }
}


//lore.lines().set(1,Text.empty().append(reportDate).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)));
//Objects.requireNonNull(itemStack.get(DataComponentTypes.LORE)).lines().set(1,Text.empty().append(reportDate));
//lore.lines().set(2,Text.literal("UID: ").append(this.player.getDisplayName()).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)));
