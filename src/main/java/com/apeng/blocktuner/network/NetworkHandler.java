package com.apeng.blocktuner.network;

import com.apeng.blocktuner.BlockTuner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

/**
 * Network helper-class
 */
public class NetworkHandler {
    private static final SimpleChannel INSTANCE = ChannelBuilder.named(new ResourceLocation(BlockTuner.MOD_ID, "network_channel")).simpleChannel();
    private static int packetID = 0;

    private static int id() {
        return packetID++;
    }

    public static void register() {
        INSTANCE.messageBuilder(TuningC2SPacket.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TuningC2SPacket::encode)
                .decoder(TuningC2SPacket::new)
                .consumerMainThread(TuningC2SPacket::handler)
                .add();
    }

    public static <MSG> void sendToServer(MSG packet) {
        INSTANCE.send(packet, PacketDistributor.SERVER.noArg());
    }

    public static <MSG> void sendToClient(MSG packet, ServerPlayer targetPlayer) {
        INSTANCE.send(packet, PacketDistributor.PLAYER.with(targetPlayer));
    }
}
