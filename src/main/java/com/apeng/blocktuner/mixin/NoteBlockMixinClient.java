package com.apeng.blocktuner.mixin;

import com.apeng.blocktuner.BlockTuner;
import com.apeng.blocktuner.gui.TuningScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NoteBlock.class)
public class NoteBlockMixinClient extends Block {
    public NoteBlockMixinClient(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
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
        Minecraft client = Minecraft.getInstance();
        if (placer != null && placer == client.player && Screen.hasControlDown()) {
            client.execute(() -> client.setScreen(new TuningScreen(Component.empty(), pos)));
        }
        super.setPlacedBy(world, pos, state, placer, itemStack);
    }
}