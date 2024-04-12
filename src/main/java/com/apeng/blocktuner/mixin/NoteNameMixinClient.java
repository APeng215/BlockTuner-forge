package com.apeng.blocktuner.mixin;

import com.apeng.blocktuner.BlockTuner;
import com.apeng.blocktuner.NoteNames;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class NoteNameMixinClient {

    private static final String NOTE_KEY = "note";
    private static final Style NOTE_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);

    @Inject(method = "getHoverName", at = @At("TAIL"), cancellable = true)
    private void addPitch2NoteName(CallbackInfoReturnable<Component> cir) {
        if (((ItemStack) (Object) this).getItem() == Items.NOTE_BLOCK) {

            CompoundTag nbtCompound = ((ItemStack) (Object) this).getTagElement(BlockTuner.BLOCK_STATE_KEY);
            int note = 0;

            if (nbtCompound != null && nbtCompound.contains(NOTE_KEY, 3)) {
                note = nbtCompound.getInt(NOTE_KEY);
                if (note > 24 || note < 0) {
                    note = 0;
                }
            }
            cir.setReturnValue(MutableComponent.create(new TranslatableContents(((ItemStack) (Object) this).getDescriptionId(), null, null))
                    .append(MutableComponent.create(new PlainTextContents.LiteralContents(" (" + NoteNames.get(note) + ", " + note + ")")).setStyle(NOTE_STYLE)));
        }
    }
}
