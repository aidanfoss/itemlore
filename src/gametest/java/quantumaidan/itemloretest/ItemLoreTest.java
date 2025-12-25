package quantumaidan.itemloretest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.quantumaidan.itemLore.config.itemLoreConfig;

public class ItemLoreTest {

    @GameTest
    public void breakBlockTest(GameTestHelper context) {
        // Enable lore tracking on any tool
        itemLoreConfig.forceLoreMode = itemLoreConfig.ForceLoreMode.UNSTACKABLE;

        BlockPos netherrackPos = new BlockPos(2, 2, 1);
        BlockPos absolutePos = context.absolutePos(netherrackPos);
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(absolutePos.getX(), absolutePos.getY() + 1, absolutePos.getZ());

        // Create Gold Pickaxe (instant mines netherrack)
        ItemStack pickaxeStack = new ItemStack(Items.GOLDEN_PICKAXE);
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxeStack);

        // Apply initial Lore so the mod tracks stats
        net.quantumaidan.itemLore.util.setLore.applyNewLore(player, pickaxeStack);

        // Place netherrack block
        context.setBlock(netherrackPos, net.minecraft.world.level.block.Blocks.NETHERRACK);

        // Verify block is there
        if (!context.getBlockState(netherrackPos).is(net.minecraft.world.level.block.Blocks.NETHERRACK)) {
            context.fail(Component.literal("Netherrack block was not placed"), netherrackPos);
            return;
        }

        // Break the block - manually call the stat tracking since we can't use gameMode
        BlockState blockState = context.getBlockState(netherrackPos);
        context.getLevel().destroyBlock(absolutePos, true, player);

        // Manually trigger the stat tracking that would normally happen via events
        net.quantumaidan.itemLore.util.statTrackLore.onBlockBrokenWithTool(
                null, blockState, pickaxeStack);

        // Wait a few ticks for the block break to process and stats to update
        context.runAtTickTime(5, () -> {
            // Verify block is gone
            if (!context.getBlockState(netherrackPos).isAir()) {
                context.fail(Component.literal("Block was not broken"), netherrackPos);
                return;
            }

            // Verify stats
            java.util.Map<String, Integer> blockStats = net.quantumaidan.itemLore.util.statTrackLore
                    .getMiningStats(pickaxeStack);
            int netherrackMined = blockStats.getOrDefault("Netherrack", 0);

            if (netherrackMined == 0) {
                context.fail(
                        Component.literal(
                                "Expected 1 Netherrack mined, but found 0. Stats may not be updating correctly."),
                        netherrackPos);
                return;
            }

            if (netherrackMined != 1) {
                context.fail(Component.literal("Expected 1 Netherrack mined, but found " + netherrackMined),
                        netherrackPos);
                return;
            }

            context.succeed();
            System.out.println("Block break test passed! Mined " + netherrackMined + " Netherrack");
        });
    }

    @GameTest
    public void sweepingEdgeTest(GameTestHelper context) {

        // Enable lore tracking on any tool
        itemLoreConfig.forceLoreMode = itemLoreConfig.ForceLoreMode.UNSTACKABLE;

        BlockPos chickenPos = new BlockPos(2, 2, 1);
        BlockPos absolutePos = context.absolutePos(chickenPos);
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(absolutePos.getX(), absolutePos.getY(), absolutePos.getZ());

        // Create Diamond Sword
        ItemStack swordStack = new ItemStack(Items.DIAMOND_SWORD);

        // Enchant with Sweeping Edge (Level 3)
        var registries = context.getLevel().registryAccess();
        var enchantments = registries.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var sweepingEdge = enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SWEEPING_EDGE);
        swordStack.enchant(sweepingEdge, 3);

        player.setItemInHand(InteractionHand.MAIN_HAND, swordStack);
        player.setOnGround(true);

        // Apply initial Lore so the mod tracks stats
        net.quantumaidan.itemLore.util.setLore.applyNewLore(player, swordStack);

        // Spawn 3 chickens with 1 HP at the same location
        LivingEntity chicken1 = context.spawn(EntityType.CHICKEN, chickenPos);
        chicken1.setHealth(1.0f);
        LivingEntity chicken2 = context.spawn(EntityType.CHICKEN, chickenPos);
        chicken2.setHealth(1.0f);
        LivingEntity chicken3 = context.spawn(EntityType.CHICKEN, chickenPos);
        chicken3.setHealth(1.0f);

        // Wait for cooldown to recharge (ensure sweeping attack works)
        context.runAtTickTime(15, () -> {
            // Force set damage attribute since mock player ignores item modifiers
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(10.0);

            // Force tick to recharge attack strength (mock player might not be ticking)
            for (int i = 0; i < 40; i++) {
                player.tick();
            }

            player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, chicken1.position());
            player.attack(chicken1);
        });

        // Manually trigger stat tracking for any chickens that die
        context.runAtTickTime(17, () -> {
            // Check which chickens died and manually track kills
            if (!chicken1.isAlive()) {
                net.quantumaidan.itemLore.util.statTrackLore.onEntityKilledWithLoredTool(
                        context.getLevel(), chicken1, swordStack);
            }
            if (!chicken2.isAlive()) {
                net.quantumaidan.itemLore.util.statTrackLore.onEntityKilledWithLoredTool(
                        context.getLevel(), chicken2, swordStack);
            }
            if (!chicken3.isAlive()) {
                net.quantumaidan.itemLore.util.statTrackLore.onEntityKilledWithLoredTool(
                        context.getLevel(), chicken3, swordStack);
            }
        });

        // Wait for stats to update and verify
        context.runAtTickTime(18, () -> {
            // Verify stats
            java.util.Map<String, Integer> killStats = net.quantumaidan.itemLore.util.statTrackLore
                    .getKillStats(swordStack);
            int chickenKills = killStats.getOrDefault("Chicken", 0);

            // Check if we got any kills
            if (chickenKills == 0) {
                context.fail(Component.literal(
                        "Expected kills, but found 0. Stats may not be updating correctly. (Check attack connection/damage)"),
                        chickenPos);
                return;
            }

            // Verify target chicken is dead
            if (chicken1.isAlive()) {
                context.fail(Component.literal("Target Chicken 1 is still alive after attack"), chickenPos);
                return;
            }

            // Check if sweeping edge worked (at least one other chicken should be dead)
            if (chicken2.isAlive() && chicken3.isAlive()) {
                context.fail(Component.literal(
                        "Sweeping edge failed: only primary target died. Killed: " + chickenKills),
                        chickenPos);
                return;
            }

            // Success! Log what happened
            int deadCount = (chicken1.isAlive() ? 0 : 1) +
                    (chicken2.isAlive() ? 0 : 1) +
                    (chicken3.isAlive() ? 0 : 1);

            context.succeed();

            // Optional: print success details
            System.out.println("Sweeping edge test passed! Killed " + chickenKills +
                    " chickens (" + deadCount + " entities dead)");
        });
    }
}