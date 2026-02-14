package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2Data;
import jmri.jmrix.marklin.mcan.classify.Cs2Header;

/**
 * Encoder/decoder for CS2 management/decoder control commands (codes 0x03–0x0A).
 * Covers loco emergency stop, speed, direction, function, CV read/write,
 * and MFX bind/verify operations.
 * <p>
 * Address encoding: the 32-bit loco address is split into data bytes 0–3 (big-endian).
 * Additional command parameters follow in bytes 4–7.
 */
public final class Cs2DecoderCommands {

    public static final int COMMAND_LOCO_EMERGENCY_STOP = 0x03;
    public static final int COMMAND_LOCO_SPEED          = 0x04;
    public static final int COMMAND_LOCO_DIRECTION      = 0x05;
    public static final int COMMAND_LOCO_FUNCTION       = 0x06;
    public static final int COMMAND_READ_CONFIG         = 0x07;
    public static final int COMMAND_WRITE_CONFIG        = 0x08;
    public static final int COMMAND_MFX_BIND            = 0x09;
    public static final int COMMAND_MFX_VERIFY          = 0x0A;

    static final int DEFAULT_PRIORITY = 0;
    static final int BROADCAST_HASH   = 0x0300;

    private Cs2DecoderCommands() {}

    /**
     * Builds a loco emergency stop command (0x03).
     * DLC=4 (address only, no speed data).
     *
     * @param locoAddress 32-bit loco address
     * @return 13-byte CAN frame
     */
    public static int[] buildLocoEmergencyStop(long locoAddress) {
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_LOCO_EMERGENCY_STOP,
                false, BROADCAST_HASH, 4, addressBytes(locoAddress));
    }

    /**
     * Builds a loco speed set command (0x04).
     * DLC=6: address (4 bytes) + speed (2 bytes, big-endian).
     * Query variant (DLC=4, speed omitted) uses speed=-1 sentinel.
     *
     * @param locoAddress 32-bit loco address
     * @param speed       speed value (0–1023); use -1 to build a speed query (DLC=4)
     * @return 13-byte CAN frame
     */
    public static int[] buildLocoSpeed(long locoAddress, int speed) {
        if (speed < 0) {
            return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_LOCO_SPEED,
                    false, BROADCAST_HASH, 4, addressBytes(locoAddress));
        }
        int[] data = addressAndBytes(locoAddress, (speed >> 8) & 0xFF, speed & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_LOCO_SPEED,
                false, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds a loco direction set command (0x05).
     * DLC=5: address (4 bytes) + direction (1 byte).
     * Query variant (DLC=4) uses direction=-1 sentinel.
     *
     * @param locoAddress 32-bit loco address
     * @param direction   direction value (0=keep, 1=fwd, 2=rev, 3=toggle); use -1 for query
     * @return 13-byte CAN frame
     */
    public static int[] buildLocoDirection(long locoAddress, int direction) {
        if (direction < 0) {
            return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_LOCO_DIRECTION,
                    false, BROADCAST_HASH, 4, addressBytes(locoAddress));
        }
        int[] data = addressAndBytes(locoAddress, direction & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_LOCO_DIRECTION,
                false, BROADCAST_HASH, 5, data);
    }

    /**
     * Builds a loco function set command (0x06).
     * DLC=6: address (4 bytes) + function number (1 byte) + state (1 byte, 0=off 1=on).
     * Query variant (DLC=5, state omitted) uses active=null (via overloaded method).
     *
     * @param locoAddress 32-bit loco address
     * @param function    function number (0–31)
     * @param active      true=on, false=off
     * @return 13-byte CAN frame
     */
    public static int[] buildLocoFunction(long locoAddress, int function, boolean active) {
        int[] data = addressAndBytes(locoAddress, function & 0xFF, active ? 1 : 0);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_LOCO_FUNCTION,
                false, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds a loco function query command (0x06, DLC=5, function known, state omitted).
     *
     * @param locoAddress 32-bit loco address
     * @param function    function number (0–31)
     * @return 13-byte CAN frame
     */
    public static int[] buildLocoFunctionQuery(long locoAddress, int function) {
        int[] data = addressAndBytes(locoAddress, function & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_LOCO_FUNCTION,
                false, BROADCAST_HASH, 5, data);
    }

    /**
     * Builds a loco speed query command (0x04, DLC=4, address only).
     *
     * @param locoAddress 32-bit loco address
     * @return 13-byte CAN frame
     */
    public static int[] buildQueryLocoSpeed(long locoAddress) {
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_LOCO_SPEED,
                false, BROADCAST_HASH, 4, addressBytes(locoAddress));
    }

    /**
     * Builds a loco direction query command (0x05, DLC=4, address only).
     *
     * @param locoAddress 32-bit loco address
     * @return 13-byte CAN frame
     */
    public static int[] buildQueryLocoDirection(long locoAddress) {
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_LOCO_DIRECTION,
                false, BROADCAST_HASH, 4, addressBytes(locoAddress));
    }

    /**
     * Builds a loco function query command (0x06, DLC=5).
     * Alias for {@link #buildLocoFunctionQuery(long, int)}.
     *
     * @param locoAddress 32-bit loco address
     * @param function    function number (0–31)
     * @return 13-byte CAN frame
     */
    public static int[] buildQueryLocoFunction(long locoAddress, int function) {
        return buildLocoFunctionQuery(locoAddress, function);
    }

    /**
     * Builds a loco speed reply frame (0x04, response, DLC=6).
     *
     * @param locoAddress 32-bit loco address
     * @param speed       speed value (0–1023)
     * @return 13-byte CAN frame
     */
    public static int[] buildLocoSpeedReply(long locoAddress, int speed) {
        int[] data = addressAndBytes(locoAddress, (speed >> 8) & 0xFF, speed & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_LOCO_SPEED,
                true, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds a loco direction reply frame (0x05, response, DLC=5).
     *
     * @param locoAddress 32-bit loco address
     * @param direction   direction value (0=keep, 1=fwd, 2=rev, 3=toggle)
     * @return 13-byte CAN frame
     */
    public static int[] buildLocoDirectionReply(long locoAddress, int direction) {
        int[] data = addressAndBytes(locoAddress, direction & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_LOCO_DIRECTION,
                true, BROADCAST_HASH, 5, data);
    }

    /**
     * Builds a loco function reply frame (0x06, response, DLC=6).
     *
     * @param locoAddress 32-bit loco address
     * @param function    function number (0–31)
     * @param active      true=on, false=off
     * @return 13-byte CAN frame
     */
    public static int[] buildLocoFunctionReply(long locoAddress, int function, boolean active) {
        int[] data = addressAndBytes(locoAddress, function & 0xFF, active ? 1 : 0);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_LOCO_FUNCTION,
                true, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds a CV read config command (0x07).
     * DLC=7: address (4 bytes) + CV index (2 bytes, big-endian) + count (1 byte).
     *
     * @param locoAddress 32-bit loco address
     * @param cv          CV index
     * @param count       number of bytes to read
     * @return 13-byte CAN frame
     */
    public static int[] buildReadConfig(long locoAddress, int cv, int count) {
        int[] data = addressAndBytes(locoAddress, (cv >> 8) & 0xFF, cv & 0xFF, count & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_READ_CONFIG,
                false, BROADCAST_HASH, 7, data);
    }

    /**
     * Builds a CV write config command (0x08).
     * DLC=8: address (4 bytes) + CV index (2 bytes, big-endian) + value (1 byte) + index (1 byte).
     *
     * @param locoAddress 32-bit loco address
     * @param cv          CV index
     * @param index       byte index within the CV
     * @param value       value to write
     * @return 13-byte CAN frame
     */
    public static int[] buildWriteConfig(long locoAddress, int cv, int index, int value) {
        int[] data = addressAndBytes(locoAddress,
                (cv >> 8) & 0xFF, cv & 0xFF, value & 0xFF, index & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_WRITE_CONFIG,
                false, BROADCAST_HASH, 8, data);
    }

    /**
     * Builds an MFX bind command (0x09).
     * DLC=6: UID (4 bytes) + SID (2 bytes, big-endian).
     *
     * @param uid 32-bit MFX UID
     * @param sid 16-bit session ID to assign
     * @return 13-byte CAN frame
     */
    public static int[] buildMfxBind(long uid, int sid) {
        int[] data = addressAndBytes(uid, (sid >> 8) & 0xFF, sid & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_MFX_BIND,
                false, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds an MFX verify command (0x0A).
     * DLC=6: UID (4 bytes) + SID (2 bytes, big-endian).
     *
     * @param uid 32-bit MFX UID
     * @param sid 16-bit session ID to verify
     * @return 13-byte CAN frame
     */
    public static int[] buildMfxVerify(long uid, int sid) {
        int[] data = addressAndBytes(uid, (sid >> 8) & 0xFF, sid & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_MFX_VERIFY,
                false, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds a CV read config reply (0x07, DLC=7): address + packed CV + value.
     *
     * @param locoAddress 32-bit loco address
     * @param cvIndex     6-bit CV index (upper 6 bits of packed field; MFX only)
     * @param cvNumber    10-bit CV number (lower 10 bits of packed field)
     * @param value       byte value read from the CV
     * @return 13-byte CAN frame
     */
    public static int[] buildReadConfigReply(long locoAddress, int cvIndex, int cvNumber, int value) {
        int packed4 = ((cvIndex & 0x3F) << 2) | ((cvNumber >> 8) & 0x03);
        int packed5 = cvNumber & 0xFF;
        int[] data = addressAndBytes(locoAddress, packed4, packed5, value & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_READ_CONFIG,
                true, BROADCAST_HASH, 7, data);
    }

    /**
     * Builds a CV write config reply (0x08, DLC=8): address + packed CV + value + flags.
     *
     * @param locoAddress      32-bit loco address
     * @param cvIndex          6-bit CV index (MFX only)
     * @param cvNumber         10-bit CV number
     * @param value            byte value that was written
     * @param writeAcknowledged true if the decoder acknowledged the write
     * @param verifySucceeded  true if post-write read-back verify passed
     * @return 13-byte CAN frame
     */
    public static int[] buildWriteConfigReply(long locoAddress, int cvIndex, int cvNumber,
                                               int value, boolean writeAcknowledged, boolean verifySucceeded) {
        int packed4 = ((cvIndex & 0x3F) << 2) | ((cvNumber >> 8) & 0x03);
        int packed5 = cvNumber & 0xFF;
        int flags   = (writeAcknowledged ? 0x80 : 0) | (verifySucceeded ? 0x40 : 0);
        int[] data  = addressAndBytes(locoAddress, packed4, packed5, value & 0xFF, flags);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_WRITE_CONFIG,
                true, BROADCAST_HASH, 8, data);
    }

    /**
     * Builds an MFX bind reply (0x09, DLC=6): echoes UID and assigned SID.
     *
     * @param uid 32-bit MFX UID
     * @param sid 16-bit MFX track SID that was assigned
     * @return 13-byte CAN frame
     */
    public static int[] buildMfxBindReply(long uid, int sid) {
        int[] data = addressAndBytes(uid, (sid >> 8) & 0xFF, sid & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_MFX_BIND,
                true, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds a positive MFX verify reply (0x0A, DLC=7): UID + SID + ASK ratio.
     *
     * @param uid      32-bit MFX UID
     * @param sid      16-bit MFX SID (non-zero = decoder found)
     * @param askRatio ASK signal quality (0–255)
     * @return 13-byte CAN frame
     */
    public static int[] buildMfxVerifyReply(long uid, int sid, int askRatio) {
        int[] data = addressAndBytes(uid, (sid >> 8) & 0xFF, sid & 0xFF, askRatio & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_MFX_VERIFY,
                true, BROADCAST_HASH, 7, data);
    }

    /**
     * Builds a negative MFX verify reply (0x0A, DLC=6): UID + SID=0 (decoder not found).
     *
     * @param uid 32-bit MFX UID from the request
     * @return 13-byte CAN frame
     */
    public static int[] buildMfxVerifyNegativeReply(long uid) {
        int[] data = addressAndBytes(uid, 0x00, 0x00);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_MFX_VERIFY,
                true, BROADCAST_HASH, 6, data);
    }

    // --- parser ---

    /**
     * Parses a decoder command frame into a typed result.
     * Called by {@link Cs2FrameParser} after command resolution.
     */
    static Cs2ClassifiedFrame parse(Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
        switch (header.resolvedCommand()) {
            case LOCO_EMERGENCY_STOP:
            case LOCO_EMERGENCY_STOP_REPLY:
                return new LocoEmergencyStopFrame(header, data, parent);
            case LOCO_SPEED:
                return new LocoSpeedFrame(header, data, parent);
            case LOCO_SPEED_REPLY:
                return new LocoSpeedReplyFrame(header, data, parent);
            case LOCO_DIRECTION:
                return new LocoDirectionFrame(header, data, parent);
            case LOCO_DIRECTION_REPLY:
                return new LocoDirectionReplyFrame(header, data, parent);
            case LOCO_FUNCTION:
                return new LocoFunctionFrame(header, data, parent);
            case LOCO_FUNCTION_REPLY:
                return new LocoFunctionReplyFrame(header, data, parent);
            case READ_CONFIG:
                return new ReadConfigFrame(header, data, parent);
            case READ_CONFIG_REPLY:
                return parseReadConfigReply(header, data, parent);
            case WRITE_CONFIG:
                return new WriteConfigFrame(header, data, parent);
            case WRITE_CONFIG_REPLY:
                return new WriteConfigReplyFrame(header, data, parent);
            case MFX_BIND:
                return new MfxBindFrame(header, data, parent);
            case MFX_BIND_REPLY:
                return new MfxBindReplyFrame(header, data, parent);
            case MFX_VERIFY:
                return new MfxVerifyFrame(header, data, parent);
            case MFX_VERIFY_REPLY:
                return parseMfxVerifyReply(header, data, parent);
            default:
                return new Cs2ClassifiedFrame(header, data, parent);
        }
    }

    private static Cs2ClassifiedFrame parseReadConfigReply(
            Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
        // DLC=7: success (carries value byte); DLC=6: failure (no value)
        if (header.dlc() == 7) return new ReadConfigReplyFrame(header, data, parent);
        return new Cs2ClassifiedFrame(header, data, parent);
    }

    private static Cs2ClassifiedFrame parseMfxVerifyReply(
            Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
        // DLC=7: positive reply (carries ASK byte); DLC=6: negative (SID=0)
        return new MfxVerifyReplyFrame(header, data, parent);
    }

    // --- typed result classes ---

    /** A loco emergency stop frame (request or reply). */
    public static final class LocoEmergencyStopFrame extends Cs2ClassifiedFrame {
        LocoEmergencyStopFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 32-bit Loc-ID of the target loco. */
        public long locoAddress() { return data().int32At(0); }
    }

    /** A loco speed request (DLC=6: address + speed; DLC=4: query with speed omitted). */
    public static final class LocoSpeedFrame extends Cs2ClassifiedFrame {
        LocoSpeedFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 32-bit loco address. */
        public long locoAddress() { return data().int32At(0); }

        /** The requested speed (0–1023). Returns -1 for query frames (DLC=4). */
        public int speed() { return header().dlc() >= 6 ? data().int16At(4) : -1; }
    }

    /** A loco speed reply carrying the loco address and current speed. */
    public static final class LocoSpeedReplyFrame extends Cs2ClassifiedFrame {
        LocoSpeedReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 32-bit loco address. */
        public long locoAddress() { return data().int32At(0); }

        /** The 16-bit speed value (0–1023). */
        public int speed() { return data().int16At(4); }
    }

    /** A loco direction reply carrying the loco address and current direction. */
    public static final class LocoDirectionReplyFrame extends Cs2ClassifiedFrame {
        LocoDirectionReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 32-bit loco address. */
        public long locoAddress() { return data().int32At(0); }

        /** The direction value (0=keep, 1=fwd, 2=rev, 3=toggle). */
        public int direction() { return data().byteAt(4); }
    }

    /** A loco direction request (DLC=5: address + direction; DLC=4: query). */
    public static final class LocoDirectionFrame extends Cs2ClassifiedFrame {
        LocoDirectionFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 32-bit loco address. */
        public long locoAddress() { return data().int32At(0); }

        /** The requested direction (0=keep, 1=fwd, 2=rev, 3=toggle). Returns -1 for query (DLC=4). */
        public int direction() { return header().dlc() >= 5 ? data().byteAt(4) : -1; }
    }

    /** A loco function reply carrying the loco address, function number and state. */
    public static final class LocoFunctionReplyFrame extends Cs2ClassifiedFrame {
        LocoFunctionReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 32-bit loco address. */
        public long locoAddress() { return data().int32At(0); }

        /** The function number (0–31). */
        public int function() { return data().byteAt(4); }

        /** True if the function is currently active. */
        public boolean active() { return data().byteAt(5) != 0; }
    }

    /** A loco function request (DLC=6: set; DLC=5: query with state omitted). */
    public static final class LocoFunctionFrame extends Cs2ClassifiedFrame {
        LocoFunctionFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 32-bit loco address. */
        public long locoAddress() { return data().int32At(0); }

        /** The function number (0–31). */
        public int function() { return data().byteAt(4); }

        /** True if the function is requested active. Returns false for query frames (DLC=5). */
        public boolean active() { return header().dlc() >= 6 && data().byteAt(5) != 0; }
    }

    /**
     * A successful CV read reply (DLC=7).
     * Data layout: Loc-ID (4 bytes) + packed CV-Index/Num (2 bytes) + value (1 byte).
     * CV-Index occupies the upper 6 bits of byte 4; CV-Num the lower 2 bits of byte 4
     * plus all 8 bits of byte 5 (total 10 bits).
     */
    public static final class ReadConfigReplyFrame extends Cs2ClassifiedFrame {
        ReadConfigReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 32-bit loco/decoder address. */
        public long locoAddress() { return data().int32At(0); }

        /** CV-Index (6-bit, upper bits of data byte 4). Only meaningful for MFX. */
        public int cvIndex() { return (data().byteAt(4) >> 2) & 0x3F; }

        /** CV number (10-bit: lower 2 bits of byte 4 + all of byte 5). */
        public int cvNumber() { return ((data().byteAt(4) & 0x03) << 8) | data().byteAt(5); }

        /** The byte value read from the CV. */
        public int value() { return data().byteAt(6); }
    }

    /**
     * A CV read request (0x07, DLC=7): address + CV index/number + count.
     */
    public static final class ReadConfigFrame extends Cs2ClassifiedFrame {
        ReadConfigFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 32-bit loco/decoder address. */
        public long locoAddress() { return data().int32At(0); }

        /** CV-Index (6-bit, upper bits of data byte 4). Only meaningful for MFX. */
        public int cvIndex() { return (data().byteAt(4) >> 2) & 0x3F; }

        /** CV number (10-bit: lower 2 bits of byte 4 + all of byte 5). */
        public int cvNumber() { return ((data().byteAt(4) & 0x03) << 8) | data().byteAt(5); }

        /** Number of bytes requested (data byte 6). */
        public int count() { return data().byteAt(6); }
    }

    /**
     * A CV write reply (DLC=8).
     */
    public static final class WriteConfigReplyFrame extends Cs2ClassifiedFrame {
        WriteConfigReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 32-bit loco/decoder address. */
        public long locoAddress() { return data().int32At(0); }

        /** CV-Index (6-bit). Only meaningful for MFX. */
        public int cvIndex() { return (data().byteAt(4) >> 2) & 0x3F; }

        /** CV number (10-bit). */
        public int cvNumber() { return ((data().byteAt(4) & 0x03) << 8) | data().byteAt(5); }

        /** The byte value that was written. */
        public int value() { return data().byteAt(6); }

        /** True if the decoder acknowledged the write. */
        public boolean writeAcknowledged() { return (data().byteAt(7) & 0x80) != 0; }

        /** True if the post-write read-back verify succeeded. */
        public boolean verifySucceeded() { return (data().byteAt(7) & 0x40) != 0; }
    }

    /**
     * A CV write request (0x08, DLC=8): address + CV index/number + value + byte-index.
     */
    public static final class WriteConfigFrame extends Cs2ClassifiedFrame {
        WriteConfigFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 32-bit loco/decoder address. */
        public long locoAddress() { return data().int32At(0); }

        /** CV-Index (6-bit). Only meaningful for MFX. */
        public int cvIndex() { return (data().byteAt(4) >> 2) & 0x3F; }

        /** CV number (10-bit). */
        public int cvNumber() { return ((data().byteAt(4) & 0x03) << 8) | data().byteAt(5); }

        /** The byte value to write. */
        public int value() { return data().byteAt(6); }

        /** Byte index within the CV (data byte 7). */
        public int byteIndex() { return data().byteAt(7); }
    }

    /**
     * An MFX bind reply (DLC=6): echoes the MFX-UID and assigned SID.
     */
    public static final class MfxBindReplyFrame extends Cs2ClassifiedFrame {
        MfxBindReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 32-bit MFX UID. */
        public long mfxUid() { return data().int32At(0); }

        /** 16-bit MFX track SID that was assigned. */
        public int mfxSid() { return data().int16At(4); }
    }

    /**
     * An MFX verify reply.
     * DLC=7 (positive): UID matched  -  carries UID, SID and ASK quality ratio.
     * DLC=6 (negative): UID not found  -  SID is 0x0000.
     */
    public static final class MfxVerifyReplyFrame extends Cs2ClassifiedFrame {
        MfxVerifyReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 32-bit MFX UID from the request. */
        public long mfxUid() { return data().int32At(0); }

        /** 16-bit MFX SID: 0x0000 means negative (decoder not found). */
        public int mfxSid() { return data().int16At(4); }

        /** True if the decoder was found (SID != 0). */
        public boolean positive() { return data().int16At(4) != 0; }

        /**
         * ASK signal quality ratio (0–255).
         * Only valid when {@link #positive()} is true (DLC=7).
         * Returns 0 for negative replies.
         */
        public int askRatio() { return header().dlc() == 7 ? data().byteAt(6) : 0; }
    }

    /** An MFX bind request (0x09, DLC=6): UID + SID to assign. */
    public static final class MfxBindFrame extends Cs2ClassifiedFrame {
        MfxBindFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 32-bit MFX UID. */
        public long mfxUid() { return data().int32At(0); }

        /** 16-bit MFX track SID to assign. */
        public int mfxSid() { return data().int16At(4); }
    }

    /** An MFX verify request (0x0A, DLC=6 or 8): UID + SID + optional ASK ratio. */
    public static final class MfxVerifyFrame extends Cs2ClassifiedFrame {
        MfxVerifyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 32-bit MFX UID to look up. */
        public long mfxUid() { return data().int32At(0); }

        /** 16-bit MFX SID. */
        public int mfxSid() { return data().int16At(4); }

        /** ASK signal quality ratio (0–255). Only present for DLC=8 frames; returns 0 for DLC=6. */
        public int askRatio() { return header().dlc() == 8 ? data().byteAt(6) : 0; }
    }

    // --- helpers ---

    private static int[] addressBytes(long address) {
        return new int[]{
            (int)((address >> 24) & 0xFF),
            (int)((address >> 16) & 0xFF),
            (int)((address >>  8) & 0xFF),
            (int)( address        & 0xFF)
        };
    }

    private static int[] addressAndBytes(long address, int... extra) {
        int[] result = new int[4 + extra.length];
        result[0] = (int)((address >> 24) & 0xFF);
        result[1] = (int)((address >> 16) & 0xFF);
        result[2] = (int)((address >>  8) & 0xFF);
        result[3] = (int)( address        & 0xFF);
        System.arraycopy(extra, 0, result, 4, extra.length);
        return result;
    }
}
