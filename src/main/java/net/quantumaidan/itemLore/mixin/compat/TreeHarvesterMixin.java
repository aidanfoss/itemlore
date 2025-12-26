package net.quantumaidan.itemLore.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(targets = "com.natamus.treeharvester.ModFabric")
public class TreeHarvesterMixin {

    public static final ThreadLocal<Player> TH_PLAYER = new ThreadLocal<>();

    @SuppressWarnings("unchecked")
    @Redirect(method = "loadEvents", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/event/Event;register(Ljava/lang/Object;)V", remap = false))
    private void redirectRegister(Event<?> event, Object listener) {
        if (event == PlayerBlockBreakEvents.BEFORE) {
            PlayerBlockBreakEvents.Before original = (PlayerBlockBreakEvents.Before) listener;
            PlayerBlockBreakEvents.Before wrapper = (Level level, Player player, BlockPos pos, BlockState state,
                    BlockEntity blockEntity) -> {
                TH_PLAYER.set(player);
                try {
                    return original.beforeBlockBreak(level, player, pos, state, blockEntity);
                } finally {
                    TH_PLAYER.remove();
                }
            };
            ((Event<PlayerBlockBreakEvents.Before>) event).register(wrapper);
        } else {
            ((Event<Object>) event).register(listener);
        }
    }
}
