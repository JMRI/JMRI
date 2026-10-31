package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.RawFrame;

/**
 * Encoder/decoder for CS2 accessory/switch commands (codes 0x0B–0x0D).
 * Covers turnout/accessory control and extended MFX bind operations.
 * <p>
 * Accessory address encoding: 32-bit address in data bytes 0–3 (big-endian).
 * Set command (DLC=6): address + aspect (1 byte) + power (1 byte).
 * Query command (DLC=4): address only.
 */
public final class Cs2AccessoryCommands {

    public static final int COMMAND_ACCESSORY    = 0x0B;
    public static final int COMMAND_MFX_BIND_EX  = 0x0C;

    static final int DEFAULT_PRIORITY = 0;
    static final int BROADCAST_HASH   = 0x0300;

    private Cs2AccessoryCommands() {}

    /**
     * Builds an accessory set command (0x0B).
     * DLC=6: address (4 bytes) + aspect (1 byte) + power (1 byte, 1=on 0=off).
     *
     * @param address 32-bit accessory address
     * @param aspect  aspect/position value
     * @param power   true to energise the accessory output
     * @return 13-byte CAN frame
     */
    public static int[] buildSetAccessory(long address, int aspect, boolean power) {
        int[] data = new int[]{
            (int)((address >> 24) & 0xFF),
            (int)((address >> 16) & 0xFF),
            (int)((address >>  8) & 0xFF),
            (int)( address        & 0xFF),
            aspect & 0xFF,
            power ? 1 : 0
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_ACCESSORY,
                false, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds an accessory set command with explicit switch time (0x0B, DLC=8).
     * Same as {@link #buildSetAccessory} but overrides the system default timeout.
     *
     * @param address    32-bit accessory address
     * @param aspect     aspect/position value
     * @param power      true to energise the accessory output
     * @param time10ms   switch time in 10 ms steps (overrides system default)
     * @return 13-byte CAN frame
     */
    public static int[] buildSetAccessoryTimed(long address, int aspect, boolean power, int time10ms) {
        int[] data = new int[]{
            (int)((address >> 24) & 0xFF),
            (int)((address >> 16) & 0xFF),
            (int)((address >>  8) & 0xFF),
            (int)( address        & 0xFF),
            aspect & 0xFF,
            power ? 1 : 0,
            (time10ms >> 8) & 0xFF,
            time10ms & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_ACCESSORY,
                false, BROADCAST_HASH, 8, data);
    }

    /**
     * Builds an accessory query command (0x0B).
     * DLC=4: address only.
     *
     * @param address 32-bit accessory address
     * @return 13-byte CAN frame
     */
    public static int[] buildQueryAccessory(long address) {
        int[] data = new int[]{
            (int)((address >> 24) & 0xFF),
            (int)((address >> 16) & 0xFF),
            (int)((address >>  8) & 0xFF),
            (int)( address        & 0xFF)
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_ACCESSORY,
                false, BROADCAST_HASH, 4, data);
    }

    /**
     * Builds an extended MFX bind command (0x0C).
     * DLC=6: UID (4 bytes) + SID (2 bytes, big-endian).
     *
     * @param uid 32-bit MFX UID
     * @param sid 16-bit session ID to assign
     * @return 13-byte CAN frame
     */
    public static int[] buildMfxBindEx(long uid, int sid) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF),
            (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF),
            (int)( uid        & 0xFF),
            (sid >> 8) & 0xFF,
            sid & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_MFX_BIND_EX,
                false, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds an accessory reply (0x0B, DLC=6): address + aspect + power.
     *
     * @param address 32-bit accessory address
     * @param aspect  aspect/position value
     * @param power   true=on, false=off
     * @return 13-byte CAN frame
     */
    public static int[] buildAccessoryReply(long address, int aspect, boolean power) {
        int[] data = new int[]{
            (int)((address >> 24) & 0xFF),
            (int)((address >> 16) & 0xFF),
            (int)((address >>  8) & 0xFF),
            (int)( address        & 0xFF),
            aspect & 0xFF,
            power ? 0x01 : 0x00
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_ACCESSORY,
                true, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds an extended MFX bind reply (0x0C, DLC=6): echoes UID and assigned SID.
     *
     * @param uid 32-bit MFX UID
     * @param sid 16-bit MFX track SID that was assigned
     * @return 13-byte CAN frame
     */
    public static int[] buildMfxBindExReply(long uid, int sid) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF),
            (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF),
            (int)( uid        & 0xFF),
            (sid >> 8) & 0xFF,
            sid & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_MFX_BIND_EX,
                true, BROADCAST_HASH, 6, data);
    }

    // --- typed result classes ---

    /**
     * An accessory request frame (0x0B).
     * DLC=6: set (address + aspect + power). DLC=8: set with explicit time. DLC=4: query.
     */
    public static final class AccessoryFrame extends Cs2Frame {
        AccessoryFrame(Cs2Header header, Cs2Data data, RawFrame raw) {
            super(header, data, raw);
        }

        /** 32-bit accessory address from data bytes 0–3. */
        public long address() { return data().int32At(0); }

        /** Aspect/position value from data byte 4 (present when DLC&gt;=5). Returns 0 for query. */
        public int aspect() { return header().dlc() >= 5 ? data().byteAt(4) : 0; }

        /** Power state from data byte 5 (present when DLC&gt;=6): true=on. */
        public boolean power() { return header().dlc() >= 6 && data().byteAt(5) != 0; }

        /**
         * Explicit switch time in 10 ms steps from data bytes 6–7 (DLC=8 only).
         * Returns 0 for DLC=6 frames.
         */
        public int time10ms() { return header().dlc() == 8 ? data().int16At(6) : 0; }
    }

    /**
     * Typed result for a parsed accessory reply frame.
     */
    public static final class AccessoryReplyFrame extends Cs2Frame {
        AccessoryReplyFrame(Cs2Header header, Cs2Data data, RawFrame raw) {
            super(header, data, raw);
        }

        /** 32-bit accessory address from data bytes 0–3. */
        public long address() {
            return data().int32At(0);
        }

        /** Aspect/position value from data byte 4 (present when DLC&gt;=5). */
        public int aspect() {
            return data().byteAt(4);
        }

        /** Power state from data byte 5 (present when DLC=6 or 8): true=on. */
        public boolean power() {
            return data().byteAt(5) != 0;
        }

        /**
         * Explicit switch time in 10 ms steps from data bytes 6–7 (present when DLC=8 only).
         * Returns 0 for DLC=6 frames.
         */
        public int time10ms() {
            return data().int16At(6);
        }
    }

    /**
     * An extended MFX bind reply (0x0C, DLC=6): echoes the MFX-UID and assigned SID.
     * Same payload layout as {@link jmri.jmrix.marklin.mcan.cs2.Cs2DecoderCommands.MfxBindReplyFrame}.
     */
    public static final class MfxBindExReplyFrame extends Cs2Frame {
        MfxBindExReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** 32-bit MFX UID. */
        public long mfxUid() { return data().int32At(0); }

        /** 16-bit MFX track SID that was assigned. */
        public int mfxSid() { return data().int16At(4); }
    }

    static Cs2Frame parse(Cs2Header header, Cs2Data data, RawFrame raw) {
        switch (header.resolvedCommand()) {
            case ACCESSORY:
                return new AccessoryFrame(header, data, raw);
            case ACCESSORY_REPLY:
                return new AccessoryReplyFrame(header, data, raw);
            case MFX_BIND_EX:
                // request has same layout as reply for MFX bind-ex
            case MFX_BIND_EX_REPLY:
                return new MfxBindExReplyFrame(header, data, raw);
            default:
                return new Cs2Frame(header, data, raw);
        }
    }
}
