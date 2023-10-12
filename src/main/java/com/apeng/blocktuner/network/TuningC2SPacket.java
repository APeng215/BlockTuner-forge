package com.apeng.blocktuner.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TuningC2SPacket {
    BlockPos pos;
    int note;

    public TuningC2SPacket(BlockPos pos, int note) {
        this.pos = pos;
        this.note = note;
    }

    // The encoding order is same as decoding order
    public TuningC2SPacket(FriendlyByteBuf buf) {
        this.note = buf.readInt();
        this.pos = buf.readBlockPos();
    }

    // The decoding order is same as encoding order
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.note);
        buf.writeBlockPos(this.pos);
    }

    public void handler(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        Level world = supplier.get().getSender().level();
        ServerPlayer player = context.getSender();

        if (world.getBlockState(pos).getBlock() != Blocks.NOTE_BLOCK) {
            return;
        }
        context.enqueueWork(() -> {
            world.setBlock(pos, world.getBlockState(pos).setValue(NoteBlock.NOTE, note), 3);
            if (world.getBlockState(pos.above()).isAir()) {
                world.getBlockState(pos).getBlock().triggerEvent(world.getBlockState(pos), world, pos, 0, 0);
                ((ServerLevel) world).sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 0, (double) note / 24.0D, 0.0D, 0.0D, 1.0D);
            }
            player.swing(InteractionHand.MAIN_HAND);
        });
    }
}


