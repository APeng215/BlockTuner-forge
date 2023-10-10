package com.apeng.blocktuner.events;

import com.apeng.blocktuner.BlockTuner;
import net.minecraftforge.fml.common.Mod;

public class EventBus {

    @Mod.EventBusSubscriber(modid = BlockTuner.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class MainEvents {

    }

    @Mod.EventBusSubscriber(modid = BlockTuner.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

    }
}
