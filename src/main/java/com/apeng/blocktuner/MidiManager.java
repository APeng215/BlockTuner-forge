package com.apeng.blocktuner;

import org.jetbrains.annotations.Nullable;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import java.util.Objects;
import java.util.Vector;

public class MidiManager {
    public static final Vector<MidiDevice> transmitters = new Vector<MidiDevice>(0, 1);
    private static final MidiManager midiManager = new MidiManager();
    static int deviceIndex = 0;

    private MidiManager() {
    }

    public static MidiManager getMidiManager() {
        return midiManager;
    }

    public int getDeviceIndex() {
        return deviceIndex;
    }

    public void loopDeviceIndex() {
        if (deviceIndex == 0) {
            refreshMidiDevice();
        }
        if (deviceIndex < transmitters.size() - 1) {
            deviceIndex += 1;
        } else {
            deviceIndex = 0;
            BlockTunerConfig.setMidiDeviceName("");
        }
    }

    @Nullable
    public MidiDevice getCurrentDevice() {
        return transmitters.get(deviceIndex);
    }

    public void refreshMidiDevice() {
        MidiDevice device;
        transmitters.clear();
        transmitters.add(null);
        deviceIndex = 0;

        // Get a list of MIDI input device.
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {

            try {
                device = MidiSystem.getMidiDevice(info);

                if (device.getMaxTransmitters() != 0 && !Objects.equals(info.getVendor(), "Oracle Corporation")) {

                    transmitters.add(device);

                    if (info.getName().equals(BlockTunerConfig.getMidiDeviceName())) {
                        deviceIndex = transmitters.size() - 1;
                    }
                }
            } catch (MidiUnavailableException ignored) {
            }
        }
    }
}