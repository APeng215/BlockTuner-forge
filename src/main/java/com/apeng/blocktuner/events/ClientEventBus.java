package com.apeng.blocktuner.events;

import com.apeng.blocktuner.BlockTuner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

public class ClientEventBus {
    @Mod.EventBusSubscriber(modid = BlockTuner.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientMainEvents {

    }

    @Mod.EventBusSubscriber(modid = BlockTuner.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

    }

    
}
