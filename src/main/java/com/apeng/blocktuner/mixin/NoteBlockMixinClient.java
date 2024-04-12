package com.apeng.blocktuner.mixin;

import com.apeng.blocktuner.BlockTuner;
import com.apeng.blocktuner.gui.TuningScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NoteBlock.class)
public class NoteBlockMixinClient extends Block {
    public NoteBlockMixinClient(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack stack = new ItemStack((NoteBlock) (Object) this);

        if (Screen.hasControlDown()) {
            int note = state.getValue(NoteBlock.NOTE);
            CompoundTag tag = new CompoundTag();

            tag.putInt("note", note);
            stack.addTagElement(BlockTuner.BLOCK_STATE_KEY, tag);
        }
        return stack;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (!world.isClientSide) return;
        Minecraft client = Minecraft.getInstance();
        if (placer != null && placer == client.player && Screen.hasControlDown()) {
            client.execute(() -> client.setScreen(new TuningScreen(Component.empty(), pos)));
        }
        super.setPlacedBy(world, pos, state, placer, itemStack);
    }
}