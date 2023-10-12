package com.apeng.blocktuner.events;

import com.apeng.blocktuner.BlockTuner;
import com.apeng.blocktuner.BlockTunerCommands;
import com.apeng.blocktuner.BlockTunerConfig;
import com.apeng.blocktuner.gui.TuningScreen;
import com.apeng.blocktuner.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class EventBus {

    @Mod.EventBusSubscriber(modid = BlockTuner.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class MainEvents {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            BlockTunerCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        }

        @SubscribeEvent
        public static void openTuneScreen(PlayerInteractEvent.RightClickBlock event) {
            Player player = event.getEntity();
            Level world = event.getLevel();
            if (BlockTunerConfig.onBlockTunerServer
                    && Screen.hasControlDown()
                    && !player.isSpectator()
                    && !player.isShiftKeyDown()
                    && world.getBlockState(event.getPos()).getBlock() == Blocks.NOTE_BLOCK
                    && player.getMainHandItem().getItem() != Items.BLAZE_ROD) {
                Minecraft client = Minecraft.getInstance();
                client.execute(() -> client.setScreen(new TuningScreen(Component.empty(), event.getPos())));
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void playUsingBlazeRod(PlayerInteractEvent.RightClickBlock event) {
            // Allows playing with right clicks while holding blaze rods
            Player player = event.getEntity();
            Level world = event.getLevel();
            BlockPos pos = event.getPos();
            if (world.isClientSide()) {
                event.setCancellationResult(InteractionResult.SUCCESS);
            } else if (player.getMainHandItem().getItem() == Items.BLAZE_ROD && world.getBlockState(pos.above()).isAir() && !player.isShiftKeyDown()) {
                world.blockEvent(pos, world.getBlockState(pos).getBlock(), 0, 0);
                event.setCanceled(true);
            }
        }


    }

    @Mod.EventBusSubscriber(modid = BlockTuner.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerNetwork(FMLCommonSetupEvent event) {
            NetworkHandler.register();
        }

    }
}
