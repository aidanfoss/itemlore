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

    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    Date today = Calendar.getInstance().getTime();
    String reportDate = df.format(today);

    @Inject(at = @At("TAIL"), method = "updateResult")
    private void init(CallbackInfo info) {
        ItemStack itemStack = this.output.getStack(3);

        NbtList lore = itemStack.getOrCreateSubNbt("display").getList("Lore", NbtElement.STRING_TYPE);
        if (lore.isEmpty()) {
            lore.add(NbtString.of(Text.Serializer.toJson(Text.empty().append(reportDate).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)))));
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("UID: ").append(this.player.getDisplayName()).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)))));
            itemStack.getOrCreateSubNbt("display").put("Lore", lore);
        }
    }
}