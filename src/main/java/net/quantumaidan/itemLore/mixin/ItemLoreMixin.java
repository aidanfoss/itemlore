package net.quantumaidan.itemLore.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.quantumaidan.itemLore.util.setLore;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class ItemLoreMixin extends ItemCombinerMenu {
    public ItemLoreMixin(@Nullable MenuType<?> type, int syncId, Inventory playerInventory,
            ContainerLevelAccess context, ItemCombinerMenuSlotDefinition forgingSlotsManager) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Inject(at = @At("TAIL"), method = "onTake")
    private void init(Player player, ItemStack itemStack, CallbackInfo ci) {
        setLore.applyForcedLore(player, itemStack);
    }

    @Inject(at = @At("TAIL"), method = "createResult")
    private void init(CallbackInfo ci) {
        // 1. get the itemStack that is the output of the anvil, we need to edit this
        // itemStack to add the lore
        ItemStack itemStack = this.resultSlots.getItem(0);

        // 3. attempt to setLore on given itemStack
        setLore.applyForcedLore(this.player, itemStack);
    }
}

// lore.lines().set(1,Text.empty().append(reportDate).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)));
// Objects.requireNonNull(itemStack.get(DataComponentTypes.LORE)).lines().set(1,Text.empty().append(reportDate));
// lore.lines().set(2,Text.literal("UID:
// ").append(this.player.getDisplayName()).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)));
