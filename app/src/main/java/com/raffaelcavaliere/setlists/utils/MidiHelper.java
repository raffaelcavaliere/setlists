package com.raffaelcavaliere.setlists.utils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by raffaelcavaliere on 2017-07-08.
 */

public class MidiHelper {

    public static final byte NOTE_OFF = (byte)0x80;
    public static final byte NOTE_ON = (byte)0x90;
    public static final byte POLYPHONIC_AFTERTOUCH = (byte)0xA0;
    public static final byte CONTROL_CHANGE = (byte)0xB0;
    public static final byte PROGRAM_CHANGE = (byte)0xC0;
    public static final byte CHANNEL_PRESSURE = (byte)0xD0;
    public static final byte PITCH_BEND = (byte)0xE0;

    public static final MidiStatus STATUS_NOTE_OFF = new MidiStatus(NOTE_OFF, "Note off", 3);
    public static final MidiStatus STATUS_NOTE_ON = new MidiStatus(NOTE_ON, "Note on", 3);
    public static final MidiStatus STATUS_POLYPHONIC_AFTERTOUCH = new MidiStatus(POLYPHONIC_AFTERTOUCH, "Polyphonic aftertouch", 3);
    public static final MidiStatus STATUS_CONTROL_CHANGE = new MidiStatus(CONTROL_CHANGE, "Control change", 3);
    public static final MidiStatus STATUS_PROGRAM_CHANGE = new MidiStatus(PROGRAM_CHANGE, "Program change", 2);
    public static final MidiStatus STATUS_CHANNEL_PRESSURE = new MidiStatus(CHANNEL_PRESSURE, "Channel pressure", 2);
    public static final MidiStatus STATUS_PITCH_BEND = new MidiStatus(PITCH_BEND, "Pitch bend", 3);

    public static MidiStatus[] getMidiStatuses() {
        MidiStatus[] statuses = new MidiStatus[7];
        statuses[0] = STATUS_NOTE_OFF;
        statuses[1] = STATUS_NOTE_ON;
        statuses[2] = STATUS_POLYPHONIC_AFTERTOUCH;
        statuses[3] = STATUS_CONTROL_CHANGE;
        statuses[4] = STATUS_PROGRAM_CHANGE;
        statuses[5] = STATUS_CHANNEL_PRESSURE;
        statuses[6] = STATUS_PITCH_BEND;
        return statuses;
    }

    public static class MidiStatus {
        private byte status;
        private String label;
        private int datalength;

        MidiStatus(byte status, String label, int datalength) {
            this.status = status;
            this.label = label;
            this.datalength = datalength;
        }

        public byte getStatus() { return this.status; }
        public String getLabel() { return this.label; }
        public int getDatalength() { return this.datalength; }

        @Override
        public String toString() {
            return getLabel();
        }
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
