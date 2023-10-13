package com.apeng.blocktuner.events;

import com.apeng.blocktuner.BlockTuner;
import com.apeng.blocktuner.NoteNameHud;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientEventBus {
    @Mod.EventBusSubscriber(modid = BlockTuner.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientMainEvents {
        @SubscribeEvent
        public static void renderNoteBlockInfo2Hud(RenderGuiEvent.Post event) {
            NoteNameHud.render(event.getGuiGraphics());
        }

    }

    @Mod.EventBusSubscriber(modid = BlockTuner.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

    }


}
