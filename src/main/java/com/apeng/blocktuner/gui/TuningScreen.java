package com.apeng.blocktuner.gui;

import com.apeng.blocktuner.BlockTuner;
import com.apeng.blocktuner.BlockTunerConfig;
import com.apeng.blocktuner.MidiManager;
import com.apeng.blocktuner.NoteNames;
import com.apeng.blocktuner.network.NetworkHandler;
import com.apeng.blocktuner.network.TuningC2SPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;


public class TuningScreen extends Screen {

    protected static final Component PLAY_MODE_TOGGLE_TOOLTIP = Component.translatable("settings.blocktuner.play_mode");
    protected static final Component KEY_TO_PIANO_TOGGLE_TOOLTIP = Component.translatable("settings.blocktuner.key_to_piano");
    protected static final Component EMPTY_MIDI_DEVICE = Component.translatable("midi_device.empty");
    protected static final Component MIDI_DEVICE_REFRESH_TOOLTIP = Component.translatable("settings.blocktuner.refresh");
    static final ResourceLocation TEXTURE = new ResourceLocation("blocktuner", "textures/gui/container/tune.png");
    private final BlockPos pos;
    private final PianoKeyWidget[] pianoKeys = new PianoKeyWidget[25];
    private final MidiManager midiManager;
    private final MidiReceiver receiver;
    protected int backgroundWidth = 256;
    protected int backgroundHeight = 112;
    protected int x;
    protected int y;
    private PianoKeyWidget pressedKey = null;
    private MidiDevice currentDevice;
    private boolean configChanged = false;
    private Component deviceName;
    private boolean deviceAvailable = true;

    public TuningScreen(Component title, BlockPos pos) {
        super(title);
        this.pos = pos;

        midiManager = MidiManager.getMidiManager();
        currentDevice = midiManager.getCurrentDevice();
        receiver = new MidiReceiver();
    }

    public static void sendTuningC2SPacket(BlockPos pos, int note) {
        note = Mth.clamp(note, 0, 24);
        TuningC2SPacket tuningC2SPacket = new TuningC2SPacket(pos, note);
        NetworkHandler.sendToServer(tuningC2SPacket);
    }

    protected static int keyToNote(int scanCode) {
        return switch (scanCode) {
            case 3, 38 -> 7;
            case 4, 39 -> 9;
            case 6 -> 12;
            case 7 -> 14;
            case 8 -> 16;
            case 10 -> 19;
            case 11 -> 21;
            case 13 -> 24;
            case 16, 51 -> 6;
            case 17, 52 -> 8;
            case 18, 53 -> 10;
            case 19 -> 11;
            case 20 -> 13;
            case 21 -> 15;
            case 22 -> 17;
            case 23 -> 18;
            case 24 -> 20;
            case 25 -> 22;
            case 26 -> 23;
            case 34 -> 0;
            case 35 -> 2;
            case 36 -> 4;
            case 48 -> 1;
            case 49 -> 3;
            case 50 -> 5;
            default -> -1;
        };
    }

    @Override
    protected void init() {
        super.init();

        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        // Fancy(?) keyboard

        this.addRenderableWidget(new WhiteKeyWidget(this.x + 16, this.y + 65, 1, 1));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 32, this.y + 65, 3, 1));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 48, this.y + 65, 5, 2));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 64, this.y + 65, 6, 0));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 80, this.y + 65, 8, 1));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 96, this.y + 65, 10, 2));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 112, this.y + 65, 11, 0));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 128, this.y + 65, 13, 1));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 144, this.y + 65, 15, 1));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 160, this.y + 65, 17, 2));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 176, this.y + 65, 18, 0));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 192, this.y + 65, 20, 1));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 208, this.y + 65, 22, 2));
        this.addRenderableWidget(new WhiteKeyWidget(this.x + 224, this.y + 65, 23, 0));
        this.addRenderableWidget(new BlackKeyWidget(this.x + 8, this.y + 40, 0));
        this.addRenderableWidget(new BlackKeyWidget(this.x + 24, this.y + 40, 2));
        this.addRenderableWidget(new BlackKeyWidget(this.x + 40, this.y + 40, 4));
        this.addRenderableWidget(new BlackKeyWidget(this.x + 72, this.y + 40, 7));
        this.addRenderableWidget(new BlackKeyWidget(this.x + 88, this.y + 40, 9));
        this.addRenderableWidget(new BlackKeyWidget(this.x + 120, this.y + 40, 12));
        this.addRenderableWidget(new BlackKeyWidget(this.x + 136, this.y + 40, 14));
        this.addRenderableWidget(new BlackKeyWidget(this.x + 152, this.y + 40, 16));
        this.addRenderableWidget(new BlackKeyWidget(this.x + 184, this.y + 40, 19));
        this.addRenderableWidget(new BlackKeyWidget(this.x + 200, this.y + 40, 21));
        this.addRenderableWidget(new BlackKeyWidget(this.x + 232, this.y + 40, 24));

        this.addRenderableWidget(new PlayModeToggle(this.x + 184, this.y + 8));
        this.addRenderableWidget(new KeyToPianoToggle(this.x + 200, this.y + 8));
        this.addRenderableWidget(new MidiSwitch(this.x + 216, this.y + 8));
        this.addRenderableWidget(new MidiDeviceRefreshButton(this.x + 232, this.y + 8));

        this.addRenderableOnly(new KeySignature(this.x + 112, this.y + 8));
        this.addRenderableWidget(new KeyAddSharpButton(this.x + 144, this.y + 8));
        this.addRenderableWidget(new KeyAddFlatButton(this.x + 144, this.y + 16));

        if (currentDevice != null && !currentDevice.isOpen()) {
            openCurrentDevice();
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        this.drawBackground(context, delta, mouseX, mouseY);
    }

    @Override
    public void tick() {
        if (minecraft == null || minecraft.level == null || minecraft.level.getBlockState(pos).getBlock() != Blocks.NOTE_BLOCK) {
            this.onClose();
        }
    }

    protected void drawBackground(GuiGraphics context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        assert this.minecraft != null;
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.blit(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        this.setDragging(false);
        if (pressedKey != null) {
            return pressedKey.mouseReleased(mouseX, mouseY, button);
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (BlockTunerConfig.isKeyToPiano() && keyCode != 256) {
            int note = keyToNote(scanCode);
            if (note >= 0 && note <= 24 && !pianoKeys[note].played) {
                pianoKeys[note].onClick(0, 0);
            }
            return true;
        } else {
            if (keyCode == 69) {
                this.onClose();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        int note = keyToNote(scanCode);
        if (note >= 0 && note <= 24) {
            pianoKeys[note].onRelease(0, 0);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (currentDevice != null && currentDevice.isOpen()) {
            currentDevice.close();
        }
        receiver.close();
        if (configChanged) {
            BlockTunerConfig.save();
        }
        super.onClose();
    }

    protected void openCurrentDevice() {
        try {
            currentDevice.open();
            deviceAvailable = true;
            currentDevice.getTransmitter().setReceiver(receiver);
        } catch (MidiUnavailableException e) {
            deviceAvailable = false;
            BlockTuner.LOGGER.info("[BlockTuner] MIDI device \"" + currentDevice.getDeviceInfo().getName() + "\" is currently unavailable. Is it busy or unplugged?");
        }
    }

    static class KeySignature implements Renderable {

        public int x;
        public int y;

        public KeySignature(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
            int keySignature = BlockTunerConfig.getKeySignature();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            context.blit(TEXTURE, this.x, this.y, (keySignature + 8) % 8 * 32, (keySignature + 8) / 8 * 16 + 224, 32, 16);
        }
    }

    abstract class PianoKeyWidget extends AbstractWidget {
        private final int note;
        protected boolean played;

        protected PianoKeyWidget(int x, int y, int width, int height, int note) {
            super(x, y, width, height, Component.empty());
            this.note = note;
            pianoKeys[note] = this;
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
            if (visible) {
                renderWidget(context, mouseX, mouseY, delta);
                if (isHovered) {
                    context.renderTooltip(TuningScreen.this.font, Component.literal(NoteNames.get(note)), TuningScreen.this.x - 8, TuningScreen.this.y - 2);
                }
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {

            pressedKey = this;
            played = true;

            if (minecraft != null && minecraft.player != null && minecraft.getConnection() != null) {
                sendTuningC2SPacket(pos, note);
                minecraft.player.swing(InteractionHand.MAIN_HAND);
            }

            if (!BlockTunerConfig.isPlayMode()) {
                onClose();
            }
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            played = false;
            pressedKey = null;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.active && this.visible) {
                if (this.isValidClickButton(button)) {
                    boolean bl = this.clicked(mouseX, mouseY);
                    if (bl) {
                        this.onClick(mouseX, mouseY);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }

    class BlackKeyWidget extends PianoKeyWidget {
        public BlackKeyWidget(int x, int y, int note) {
            super(x, y, 16, 38, note);
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
            this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (played) {
                status = 1;
            } else if (this.isHovered()) {
                status = 2;
            }
            context.blit(TEXTURE, this.getX(), this.getY(), 16 * status, 112, 16, 38);
        }
    }

    class WhiteKeyWidget extends PianoKeyWidget {
        private final int keyShape;

        public WhiteKeyWidget(int x, int y, int note, int keyShape) {
            super(x, y, 16, 38, note);
            this.keyShape = keyShape;
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
            boolean mask = mouseX >= this.getX() + 8 - 8 * keyShape && mouseY >= this.getY() && mouseX < this.getX() + 24 - 8 * keyShape && mouseY < this.getY() + 13;
            this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
            this.isHovered = this.isHovered && !mask;
            super.render(context, mouseX, mouseY, delta);
            if (!this.visible) {
                return;
            }
            this.renderWidget(context, mouseX, mouseY, delta);
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (played) {
                status = 1;
            } else if (this.isHovered()) {
                status = 2;
            }

            context.blit(TEXTURE, this.getX(), this.getY(), 16 * status + 48 * keyShape + 48, 112, 16, 38);
        }

        @Override
        protected boolean clicked(double mouseX, double mouseY) {
            return this.active && this.visible && this.isHovered;
        }

    }

    class PlayModeToggle extends AbstractWidget {
        public PlayModeToggle(int x, int y) {
            super(x, y, 16, 16, Component.empty());
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            if (this.isHovered()) {
                context.renderTooltip(TuningScreen.this.font, PLAY_MODE_TOGGLE_TOOLTIP, TuningScreen.this.x - 8, TuningScreen.this.y - 2);
            }
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (BlockTunerConfig.isPlayMode()) {
                status = 2;
            }
            if (this.isHovered()) {
                status += 1;
            }
            context.blit(TEXTURE, this.getX(), this.getY(), 192 + 16 * status, 112, 16, 16);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            BlockTunerConfig.togglePlayMode();
            configChanged = true;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }

    class KeyToPianoToggle extends AbstractWidget {
        public KeyToPianoToggle(int x, int y) {
            super(x, y, 16, 16, Component.empty());
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            if (this.isHovered()) {
                context.renderTooltip(TuningScreen.this.font, KEY_TO_PIANO_TOGGLE_TOOLTIP, TuningScreen.this.x - 8, TuningScreen.this.y - 2);
            }
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (BlockTunerConfig.isKeyToPiano()) {
                status = 2;
            }
            if (this.isHovered()) {
                status += 1;
            }
            context.blit(TEXTURE, this.getX(), this.getY(), 192 + 16 * status, 128, 16, 16);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            BlockTunerConfig.toggleKeyToPiano();
            configChanged = true;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }

    class MidiSwitch extends AbstractWidget {

        public MidiSwitch(int x, int y) {
            super(x, y, 16, 16, Component.empty());
            if (currentDevice == null) {
                deviceName = EMPTY_MIDI_DEVICE;
            } else {
                deviceName = Component.literal(currentDevice.getDeviceInfo().getName());
            }
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            if (this.isHovered()) {
                context.renderTooltip(TuningScreen.this.font, Component.translatable("settings.blocktuner.midi_device", deviceName), TuningScreen.this.x - 8, TuningScreen.this.y - 2);
            }
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (midiManager.getDeviceIndex() > 0) {
                status = 2;
            }
            if (this.isHovered()) {
                status += 1;
            }
            if (!deviceAvailable) {
                status += 4;
            }
            context.blit(TEXTURE, this.getX(), this.getY(), 192 + 16 * (status % 4), 144 + 16 * (status / 4), 16, 16);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {

            if (currentDevice != null && currentDevice.isOpen()) {
                currentDevice.close();
            }

            midiManager.loopDeviceIndex();
            currentDevice = midiManager.getCurrentDevice();

            if (currentDevice != null) {
                BlockTunerConfig.setMidiDeviceName(currentDevice.getDeviceInfo().getName());
                deviceName = Component.literal(BlockTunerConfig.getMidiDeviceName());
                openCurrentDevice();
            } else {
                BlockTunerConfig.setMidiDeviceName("");
                deviceName = EMPTY_MIDI_DEVICE;
            }
            configChanged = true;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }

    class MidiDeviceRefreshButton extends AbstractWidget {
        public MidiDeviceRefreshButton(int x, int y) {
            super(x, y, 16, 16, Component.empty());
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            if (this.isHovered()) {
                context.renderTooltip(TuningScreen.this.font, MIDI_DEVICE_REFRESH_TOOLTIP, TuningScreen.this.x - 8, TuningScreen.this.y - 2);
            }
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (this.isHovered()) {
                status += 1;
            }
            context.blit(TEXTURE, this.getX(), this.getY(), 192 + 16 * status, 176, 16, 16);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            midiManager.refreshMidiDevice();
            if (currentDevice != null) {
                openCurrentDevice();
            }
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }

    class KeyAddSharpButton extends AbstractWidget {
        public KeyAddSharpButton(int x, int y) {
            super(x, y, 8, 8, Component.empty());
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (this.isHovered()) {
                status += 1;
            }
            context.blit(TEXTURE, this.getX(), this.getY(), 8 * status, 152, 8, 8);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            BlockTunerConfig.keyAddSharp();
            configChanged = true;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }

    class KeyAddFlatButton extends AbstractWidget {
        public KeyAddFlatButton(int x, int y) {
            super(x, y, 8, 8, Component.empty());
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (this.isHovered()) {
                status += 1;
            }
            context.blit(TEXTURE, this.getX(), this.getY(), 8 * status + 16, 152, 8, 8);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            BlockTunerConfig.keyAddFlat();
            configChanged = true;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }

    class MidiReceiver implements Receiver {
        public MidiReceiver() {
        }

        public void send(MidiMessage msg, long timeStamp) {
            byte[] message = msg.getMessage();
            if (message.length == 3 && message[0] <= -97 && message[1] >= 54 && message[1] <= 78) {
                assert minecraft != null;
                if (message[0] >= -112 && message[2] != 0) {

                    // MIDI note on
                    minecraft.execute(() -> pianoKeys[message[1] - 54].onClick(0, 0));

                } else {

                    // MIDI note off
                    minecraft.execute(() -> pianoKeys[message[1] - 54].onRelease(0, 0));

                }
            }
        }

        public void close() {
        }
    }
}
