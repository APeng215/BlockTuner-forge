package com.apeng.blocktuner.network;

import com.apeng.blocktuner.BlockTuner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "2";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BlockTuner.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetID = 0;

    private static int id() {
        return packetID++;
    }

    public static void register() {
        INSTANCE.registerMessage(
                id(),
                TuningC2SPacket.class,
                TuningC2SPacket::encode,
                TuningC2SPacket::new,
                TuningC2SPacket::handler
        );

    }

    public static <MSG> void sendToServer(MSG packet) {
        INSTANCE.sendToServer(packet);
    }

    public static <MSG> void sendToClient(MSG packet, ServerPlayer targetPlayer) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> targetPlayer), packet);
    }
}
