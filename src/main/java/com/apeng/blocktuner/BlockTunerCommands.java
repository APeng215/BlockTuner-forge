package com.apeng.blocktuner;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.gameevent.GameEvent;

public class BlockTunerCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext access, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("tune")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("note", IntegerArgumentType.integer(0, 24))
                                .executes(context -> tune(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"), IntegerArgumentType.getInteger(context, "note"))))
                ));
    }

    private static int tune(CommandSourceStack source, BlockPos pos, int note) {
        ServerLevel world = source.getLevel();
        if (world.getBlockState(pos).getBlock() != Blocks.NOTE_BLOCK || (!source.hasPermission(2) && pos.closerToCenterThan(source.getPosition(), 5.0d))) {
            return -1;
        }
        world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(NoteBlock.NOTE, note));
        // please do not change this to world.addSyncedBlockEvent() as it does not allow chords to be played.
        world.getBlockState(pos).triggerEvent(world, pos, 0, 0);
        world.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 0, (double) note / 24.0D, 0.0D, 0.0D, 1.0D);
        world.gameEvent(source.getEntity(), GameEvent.NOTE_BLOCK_PLAY, pos);
        return note;
    }
}
