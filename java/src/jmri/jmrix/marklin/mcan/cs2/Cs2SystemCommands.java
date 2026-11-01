package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.RawFrame;
import jmri.jmrix.marklin.mcan.McanUid;

/**
 * Encoder, parser and typed result classes for CS2 system commands (command code 0x00).
 * <p>
 * System commands carry a 32-bit UID in data bytes 0–3 (0x00000000 = broadcast to all
 * devices) and a sub-command byte in data byte 4. Additional parameters follow in
 * data bytes 5–7 depending on the sub-command.
 * <p>
 * Sub-command layout summary:
 * <pre>
 *   0x00 System Stop              DLC=5  data[0..3]=UID, data[4]=0x00
 *   0x01 System Go                DLC=5  data[0..3]=UID, data[4]=0x01
 *   0x02 System Halt              DLC=5  data[0..3]=UID, data[4]=0x02
 *   0x03 Loco Emergency Stop      DLC=5  data[0..3]=Loc-ID, data[4]=0x03
 *   0x04 Loco Cycle Stop          DLC=5  data[0..3]=Loc-ID, data[4]=0x04
 *   0x05 Loco Data Protocol       DLC=6  data[0..3]=Loc-ID, data[4]=0x05, data[5]=protocol
 *   0x06 Accessory Switch Time    DLC=7  data[0..3]=UID, data[4]=0x06, data[5..6]=time(16-bit)
 *   0x07 Fast Read MFX            DLC=7  data[0..3]=senderUID, data[4]=0x07, data[5..6]=MFX-SID
 *   0x08 Enable Track Protocol    DLC=6  data[0..3]=UID, data[4]=0x08, data[5]=protocol-bitmask
 *   0x09 MFX Re-registration      DLC=7  data[0..3]=UID, data[4]=0x09, data[5..6]=counter(16-bit)
 *   0x0A System Overload          DLC=6  data[0..3]=senderUID, data[4]=0x0A, data[5]=channel
 *   0x0B System Status            DLC=6  data[0..3]=UID, data[4]=0x0B, data[5]=channel
 *   0x0C Device Identifier        DLC=5  data[0..3]=UID, data[4]=0x0C  (query)
 *                                  DLC=7  ...data[5..6]=identifier(16-bit)  (set/reply)
 *   0x80 System Reset             DLC=6  data[0..3]=UID, data[4]=0x80, data[5]=reset-target
 * </pre>
 */
public final class Cs2SystemCommands {

    public static final int COMMAND_CODE = 0x00;

    public static final int SUB_STOP                 = 0x00;
    public static final int SUB_GO                   = 0x01;
    public static final int SUB_HALT                 = 0x02;
    public static final int SUB_LOCO_EMERGENCY_STOP  = 0x03;
    public static final int SUB_LOCO_CYCLE_STOP      = 0x04;
    public static final int SUB_LOCO_DATA_PROTOCOL   = 0x05;
    public static final int SUB_ACCESSORY_SWITCH_TIME = 0x06;
    public static final int SUB_FAST_READ_MFX        = 0x07;
    public static final int SUB_ENABLE_TRACK_PROTOCOL = 0x08;
    public static final int SUB_MFX_REREGISTRATION   = 0x09;
    public static final int SUB_SYSTEM_OVERLOAD      = 0x0A;
    public static final int SUB_SYSTEM_STATUS        = 0x0B;
    public static final int SUB_DEVICE_IDENTIFIER    = 0x0C;
    public static final int SUB_MFX_SEEK             = 0x30;
    public static final int SUB_SYSTEM_RESET         = 0x80;

    /**
     * Loco Data Protocol sub-protocol values for MM2.
     * Used as the {@code protocol} parameter of {@link #buildLocoDataProtocol}.
     */
    public static final int PROTOCOL_MM2_2040  = 0;
    public static final int PROTOCOL_MM2_LOK20 = 1;
    public static final int PROTOCOL_MM2_FKT40 = 2;

    /**
     * Loco Data Protocol sub-protocol values for DCC.
     */
    public static final int PROTOCOL_DCC_SHORT_28  = 0;
    public static final int PROTOCOL_DCC_SHORT_14  = 1;
    public static final int PROTOCOL_DCC_SHORT_126 = 2;
    public static final int PROTOCOL_DCC_LONG_28   = 3;
    public static final int PROTOCOL_DCC_LONG_126  = 4;

    /**
     * Enable Track Protocol bitmask values.
     * OR together to enable multiple protocols.
     */
    public static final int TRACK_PROTOCOL_MM2 = 0x01;
    public static final int TRACK_PROTOCOL_MFX = 0x02;
    public static final int TRACK_PROTOCOL_DCC = 0x04;

    /** Default CS2 fingerprint hash used for broadcast system commands. */
    static final int BROADCAST_HASH   = 0x0300;
    static final int DEFAULT_PRIORITY = 0;

    private Cs2SystemCommands() {}

    // --- encoders: power control ---

    /** Builds a broadcast system-stop (track power off) command frame. */
    public static int[] buildSystemStop() {
        return buildSystemCommand(McanUid.BROADCAST.toLong(), SUB_STOP, false, 5);
    }

    /** Builds a broadcast system-go (track power on) command frame. */
    public static int[] buildSystemGo() {
        return buildSystemCommand(McanUid.BROADCAST.toLong(), SUB_GO, false, 5);
    }

    /** Builds a broadcast system-halt (all locos to speed 0) command frame. */
    public static int[] buildSystemHalt() {
        return buildSystemCommand(McanUid.BROADCAST.toLong(), SUB_HALT, false, 5);
    }

    /** Builds a system-stop reply frame. */
    public static int[] buildSystemStopReply() {
        return buildSystemCommand(McanUid.BROADCAST.toLong(), SUB_STOP, true, 5);
    }

    /** Builds a system-go reply frame. */
    public static int[] buildSystemGoReply() {
        return buildSystemCommand(McanUid.BROADCAST.toLong(), SUB_GO, true, 5);
    }

    /** Builds a system-halt reply frame. */
    public static int[] buildSystemHaltReply() {
        return buildSystemCommand(McanUid.BROADCAST.toLong(), SUB_HALT, true, 5);
    }

    /** Builds a loco emergency stop reply (sub-cmd 0x03, DLC=5). */
    public static int[] buildLocoEmergencyStopReply(long locId) {
        return buildSystemCommand(locId, SUB_LOCO_EMERGENCY_STOP, true, 5);
    }

    /** Builds a loco cycle stop reply (sub-cmd 0x04, DLC=5). */
    public static int[] buildLocoCycleStopReply(long locId) {
        return buildSystemCommand(locId, SUB_LOCO_CYCLE_STOP, true, 5);
    }

    /** Builds a loco data protocol reply (sub-cmd 0x05, DLC=6). */
    public static int[] buildLocoDataProtocolReply(long locId, int protocol) {
        int[] data = uidAndBytes(locId, SUB_LOCO_DATA_PROTOCOL, protocol & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE, true, BROADCAST_HASH, 6, data);
    }

    /** Builds an accessory switch time reply (sub-cmd 0x06, DLC=7). */
    public static int[] buildAccessorySwitchTimeReply(McanUid targetUid, int time10ms) {
        int[] data = uidAndBytes(targetUid.toLong(), SUB_ACCESSORY_SWITCH_TIME,
                (time10ms >> 8) & 0xFF, time10ms & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE, true, BROADCAST_HASH, 7, data);
    }

    /** Builds a Fast Read MFX reply (sub-cmd 0x07, DLC=7). */
    public static int[] buildFastReadMfxReply(McanUid senderUid, int mfxSid) {
        int[] data = uidAndBytes(senderUid.toLong(), SUB_FAST_READ_MFX,
                (mfxSid >> 8) & 0xFF, mfxSid & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE, true, BROADCAST_HASH, 7, data);
    }

    /** Builds an MFX re-registration counter reply (sub-cmd 0x09, DLC=7). */
    public static int[] buildMfxReregistrationCounterReply(McanUid targetUid, int counter) {
        int[] data = uidAndBytes(targetUid.toLong(), SUB_MFX_REREGISTRATION,
                (counter >> 8) & 0xFF, counter & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE, true, BROADCAST_HASH, 7, data);
    }

    /** Builds an Enable Track Protocol reply (sub-cmd 0x0A, DLC=6). */
    public static int[] buildEnableTrackProtocolReply(McanUid targetUid, int protocolMask) {
        int[] data = uidAndBytes(targetUid.toLong(), SUB_ENABLE_TRACK_PROTOCOL, protocolMask & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE, true, BROADCAST_HASH, 6, data);
    }

    /** Builds a System Overload reply (sub-cmd 0x0B, DLC=6). */
    public static int[] buildSystemOverloadReply(McanUid senderUid, int channel) {
        int[] data = uidAndBytes(senderUid.toLong(), SUB_SYSTEM_OVERLOAD, channel & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE, true, BROADCAST_HASH, 6, data);
    }

    /** Builds a System Status reply (sub-cmd 0x0C, DLC=8) carrying a measured value. */
    public static int[] buildSystemStatusReply(McanUid deviceUid, int channel, int measuredValue) {
        int[] data = uidAndBytes(deviceUid.toLong(), SUB_SYSTEM_STATUS, channel & 0xFF,
                (measuredValue >> 8) & 0xFF, measuredValue & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE, true, BROADCAST_HASH, 8, data);
    }

    /** Builds a Device Identifier reply (sub-cmd 0x0C, DLC=7). */
    public static int[] buildDeviceIdentifierReply(McanUid targetUid, int identifier) {
        int[] data = uidAndBytes(targetUid.toLong(), SUB_DEVICE_IDENTIFIER,
                (identifier >> 8) & 0xFF, identifier & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE, true, BROADCAST_HASH, 7, data);
    }

    /** Builds an MFX Seek reply (sub-cmd 0x30, DLC=6). */
    public static int[] buildMfxSeekReply(McanUid senderUid, int range) {
        int[] data = uidAndBytes(senderUid.toLong(), SUB_MFX_SEEK, range & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE, true, BROADCAST_HASH, 6, data);
    }

    /** Builds a System Reset reply (sub-cmd 0x80, DLC=6). */
    public static int[] buildSystemResetReply(McanUid targetUid, int resetTarget) {
        int[] data = uidAndBytes(targetUid.toLong(), SUB_SYSTEM_RESET, resetTarget & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE, true, BROADCAST_HASH, 6, data);
    }

    // --- encoders: loco management ---

    /**
     * Builds a loco emergency stop command (sub-cmd 0x03, DLC=5).
     * Sends an immediate protocol-specific stop for the addressed loco.
     *
     * @param locId 32-bit Loc-ID of the target loco
     * @return 13-byte CAN frame
     */
    public static int[] buildLocoEmergencyStop(long locId) {
        return buildSystemCommand(locId, SUB_LOCO_EMERGENCY_STOP, false, 5);
    }

    /**
     * Builds a loco cycle stop command (sub-cmd 0x04, DLC=5).
     * Removes the loco from the GFP refresh cycle.
     * Use Loc-ID 0 to remove all locos from all GFPs.
     *
     * @param locId 32-bit Loc-ID (or 0 for all)
     * @return 13-byte CAN frame
     */
    public static int[] buildLocoCycleStop(long locId) {
        return buildSystemCommand(locId, SUB_LOCO_CYCLE_STOP, false, 5);
    }

    /**
     * Builds a loco data protocol command (sub-cmd 0x05, DLC=6).
     * Changes the sub-protocol for the addressed loco without changing the base protocol.
     *
     * @param locId    32-bit Loc-ID of the target loco
     * @param protocol sub-protocol value (see PROTOCOL_* constants)
     * @return 13-byte CAN frame
     */
    public static int[] buildLocoDataProtocol(long locId, int protocol) {
        int[] data = uidAndBytes(locId, SUB_LOCO_DATA_PROTOCOL, protocol & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE,
                false, BROADCAST_HASH, 6, data);
    }

    // --- encoders: accessory ---

    /**
     * Builds an accessory switch time command (sub-cmd 0x06, DLC=7).
     * Sets the default switching duration for accessory decoders.
     * Time is in 10 ms increments; value 0 resets to factory default (~1 s).
     *
     * @param targetUid 32-bit target device UID ({@link McanUid#BROADCAST} for all)
     * @param time10ms  switch time in 10 ms steps (0 = reset to default)
     * @return 13-byte CAN frame
     */
    public static int[] buildAccessorySwitchTime(McanUid targetUid, int time10ms) {
        int[] data = uidAndBytes(targetUid.toLong(), SUB_ACCESSORY_SWITCH_TIME,
                (time10ms >> 8) & 0xFF, time10ms & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE,
                false, BROADCAST_HASH, 7, data);
    }

    // --- encoders: MFX ---

    /**
     * Builds a Fast Read MFX command (sub-cmd 0x07, DLC=7).
     * Suppresses inter-read wait time for the given MFX SID (GFP V2+).
     * Requires a prior successful MFX Verify for the same SID.
     *
     * @param senderUid 32-bit UID of the sending device
     * @param mfxSid    16-bit MFX track address (SID)
     * @return 13-byte CAN frame
     */
    public static int[] buildFastReadMfx(McanUid senderUid, int mfxSid) {
        int[] data = uidAndBytes(senderUid.toLong(), SUB_FAST_READ_MFX,
                (mfxSid >> 8) & 0xFF, mfxSid & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE,
                false, BROADCAST_HASH, 7, data);
    }

    /**
     * Builds an MFX re-registration counter command (sub-cmd 0x09, DLC=7).
     * Sets the MFX new-registration counter in the GFP.
     *
     * @param targetUid 32-bit target device UID
     * @param counter   new 16-bit re-registration counter value
     * @return 13-byte CAN frame
     */
    public static int[] buildMfxReregistrationCounter(McanUid targetUid, int counter) {
        int[] data = uidAndBytes(targetUid.toLong(), SUB_MFX_REREGISTRATION,
                (counter >> 8) & 0xFF, counter & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE,
                false, BROADCAST_HASH, 7, data);
    }

    // --- encoders: track protocol ---

    /**
     * Builds an enable track protocol command (sub-cmd 0x08, DLC=6).
     * A set bit enables the corresponding protocol; a cleared bit suppresses it.
     * Setting is not stored  -  must be re-sent after every GFP reset.
     *
     * @param targetUid     32-bit target device UID
     * @param protocolMask  bitmask of protocols to enable (see TRACK_PROTOCOL_* constants)
     * @return 13-byte CAN frame
     */
    public static int[] buildEnableTrackProtocol(McanUid targetUid, int protocolMask) {
        int[] data = uidAndBytes(targetUid.toLong(), SUB_ENABLE_TRACK_PROTOCOL, protocolMask & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE,
                false, BROADCAST_HASH, 6, data);
    }

    // --- encoders: status / overload ---

    /**
     * Builds a System Status query command (sub-cmd 0x0B, DLC=6).
     * Requests the current measurement value for a specific channel from a device.
     *
     * @param targetUid 32-bit target device UID
     * @param channel   channel number to query
     * @return 13-byte CAN frame
     */
    public static int[] buildSystemStatusQuery(McanUid targetUid, int channel) {
        int[] data = uidAndBytes(targetUid.toLong(), SUB_SYSTEM_STATUS, channel & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE,
                false, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds a System Overload notification (sub-cmd 0x0A, DLC=6).
     * Sent by a GFP to report that a channel has exceeded its operating limit.
     * This is a response-only message under normal operation; the encoder is
     * provided for simulation and test purposes.
     *
     * @param senderUid 32-bit UID of the reporting device
     * @param channel   channel number that caused the overload
     * @return 13-byte CAN frame
     */
    public static int[] buildSystemOverload(McanUid senderUid, int channel) {
        int[] data = uidAndBytes(senderUid.toLong(), SUB_SYSTEM_OVERLOAD, channel & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE,
                true, BROADCAST_HASH, 6, data);
    }

    // --- encoders: device identifier ---

    /**
     * Builds a Device Identifier query (sub-cmd 0x0C, DLC=5).
     * Asks the addressed device to report its current 16-bit device identifier.
     *
     * @param targetUid 32-bit target device UID
     * @return 13-byte CAN frame
     */
    public static int[] buildDeviceIdentifierQuery(McanUid targetUid) {
        return buildSystemCommand(targetUid.toLong(), SUB_DEVICE_IDENTIFIER, false, 5);
    }

    /**
     * Builds a Device Identifier set command (sub-cmd 0x0C, DLC=7).
     * Assigns a 16-bit device identifier to the addressed device.
     *
     * @param targetUid  32-bit target device UID
     * @param identifier 16-bit identifier to assign (0 and 0xFFFF are invalid)
     * @return 13-byte CAN frame
     */
    public static int[] buildDeviceIdentifierSet(McanUid targetUid, int identifier) {
        int[] data = uidAndBytes(targetUid.toLong(), SUB_DEVICE_IDENTIFIER,
                (identifier >> 8) & 0xFF, identifier & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE,
                false, BROADCAST_HASH, 7, data);
    }

    // --- encoders: reset ---

    /**
     * Builds an MFX Seek command (sub-cmd 0x30, DLC=6).
     * Initiates an MFX seek cycle for a specific UID range. Valid DLC values
     * are 6, 7 and 8; DLC=6 (range only) is the most common form.
     *
     * @param senderUid 32-bit UID of the requesting device
     * @param range     6-bit MFX range value (0 = force re-registration)
     * @return 13-byte CAN frame
     */
    public static int[] buildMfxSeek(McanUid senderUid, int range) {
        int[] data = uidAndBytes(senderUid.toLong(), SUB_MFX_SEEK, range & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE,
                false, BROADCAST_HASH, 6, data);
    }

    // --- encoders: reset ---

    /**
     * Builds a System Reset command (sub-cmd 0x80, DLC=6).
     *
     * @param targetUid   32-bit target device UID
     * @param resetTarget reset target parameter (device-specific)
     * @return 13-byte CAN frame
     */
    public static int[] buildSystemReset(McanUid targetUid, int resetTarget) {
        int[] data = uidAndBytes(targetUid.toLong(), SUB_SYSTEM_RESET, resetTarget & 0xFF);
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE,
                false, BROADCAST_HASH, 6, data);
    }

    // --- parser ---

    /**
     * Parses a system command frame into a typed result.
     * Called by {@link Cs2FrameParser} after command resolution.
     */
    static Cs2Frame parse(Cs2Header header, Cs2Data data, RawFrame raw) {
        boolean rep = header.response();
        int sub = data.byteAt(4);
        switch (sub) {
            case SUB_STOP:
                return rep ? new SystemStopReplyFrame(header, data, raw)
                           : new SystemStopRequestFrame(header, data, raw);
            case SUB_GO:
                return rep ? new SystemGoReplyFrame(header, data, raw)
                           : new SystemGoRequestFrame(header, data, raw);
            case SUB_HALT:
                return rep ? new SystemHaltReplyFrame(header, data, raw)
                           : new SystemHaltRequestFrame(header, data, raw);
            case SUB_LOCO_EMERGENCY_STOP:
                return rep ? new LocoEmergencyStopReplyFrame(header, data, raw)
                           : new LocoEmergencyStopRequestFrame(header, data, raw);
            case SUB_LOCO_CYCLE_STOP:
                return rep ? new LocoCycleStopReplyFrame(header, data, raw)
                           : new LocoCycleStopRequestFrame(header, data, raw);
            case SUB_LOCO_DATA_PROTOCOL:
                return rep ? new LocoDataProtocolReplyFrame(header, data, raw)
                           : new LocoDataProtocolRequestFrame(header, data, raw);
            case SUB_ACCESSORY_SWITCH_TIME:
                return rep ? new AccessorySwitchTimeReplyFrame(header, data, raw)
                           : new AccessorySwitchTimeRequestFrame(header, data, raw);
            case SUB_FAST_READ_MFX:
                return rep ? new FastReadMfxReplyFrame(header, data, raw)
                           : new FastReadMfxRequestFrame(header, data, raw);
            case SUB_ENABLE_TRACK_PROTOCOL:
                return rep ? new EnableTrackProtocolReplyFrame(header, data, raw)
                           : new EnableTrackProtocolRequestFrame(header, data, raw);
            case SUB_MFX_REREGISTRATION:
                return rep ? new MfxReregistrationReplyFrame(header, data, raw)
                           : new MfxReregistrationRequestFrame(header, data, raw);
            case SUB_SYSTEM_OVERLOAD:
                return new SystemOverloadFrame(header, data, raw);
            case SUB_SYSTEM_STATUS:
                return rep ? new SystemStatusReplyFrame(header, data, raw)
                           : new SystemStatusRequestFrame(header, data, raw);
            case SUB_DEVICE_IDENTIFIER:
                return rep ? new DeviceIdentifierReplyFrame(header, data, raw)
                           : new DeviceIdentifierRequestFrame(header, data, raw);
            case SUB_MFX_SEEK:
                return rep ? new MfxSeekReplyFrame(header, data, raw)
                           : new MfxSeekRequestFrame(header, data, raw);
            case SUB_SYSTEM_RESET:
                return rep ? new SystemResetReplyFrame(header, data, raw)
                           : new SystemResetRequestFrame(header, data, raw);
            default:
                return new Cs2Frame(header, data, raw);
        }
    }

    // --- typed result classes ---

    /** System Stop request (track power off). */
    public static final class SystemStopRequestFrame extends Cs2Frame {
        SystemStopRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }
    }

    /** System Stop reply. */
    public static final class SystemStopReplyFrame extends Cs2Frame {
        SystemStopReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }
    }

    /** System Go request (track power on). */
    public static final class SystemGoRequestFrame extends Cs2Frame {
        SystemGoRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }
    }

    /** System Go reply. */
    public static final class SystemGoReplyFrame extends Cs2Frame {
        SystemGoReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }
    }

    /** System Halt request (all locos to speed 0). */
    public static final class SystemHaltRequestFrame extends Cs2Frame {
        SystemHaltRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }
    }

    /** System Halt reply. */
    public static final class SystemHaltReplyFrame extends Cs2Frame {
        SystemHaltReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }
    }

    /** Loco Emergency Stop request (immediate protocol-specific stop). */
    public static final class LocoEmergencyStopRequestFrame extends Cs2Frame {
        LocoEmergencyStopRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** The 32-bit Loc-ID of the target loco. */
        public long locId() { return data().int32At(0); }
    }

    /** Loco Emergency Stop reply. */
    public static final class LocoEmergencyStopReplyFrame extends Cs2Frame {
        LocoEmergencyStopReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** The 32-bit Loc-ID of the target loco. */
        public long locId() { return data().int32At(0); }
    }

    /** Loco Cycle Stop request (removes loco from GFP refresh cycle). */
    public static final class LocoCycleStopRequestFrame extends Cs2Frame {
        LocoCycleStopRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** The 32-bit Loc-ID (or 0 for all). */
        public long locId() { return data().int32At(0); }
    }

    /** Loco Cycle Stop reply. */
    public static final class LocoCycleStopReplyFrame extends Cs2Frame {
        LocoCycleStopReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** The 32-bit Loc-ID (or 0 for all). */
        public long locId() { return data().int32At(0); }
    }

    /** Loco Data Protocol request (selects sub-protocol without changing the base protocol). */
    public static final class LocoDataProtocolRequestFrame extends Cs2Frame {
        LocoDataProtocolRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** The 32-bit Loc-ID. */
        public long locId() { return data().int32At(0); }

        /** Sub-protocol value (see PROTOCOL_* constants). */
        public int locoProtocol() { return data().byteAt(5); }
    }

    /** Loco Data Protocol reply. */
    public static final class LocoDataProtocolReplyFrame extends Cs2Frame {
        LocoDataProtocolReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** The 32-bit Loc-ID. */
        public long locId() { return data().int32At(0); }

        /** Sub-protocol value (see PROTOCOL_* constants). */
        public int locoProtocol() { return data().byteAt(5); }
    }

    /** Accessory Switch Time request (sets the default accessory switching duration). */
    public static final class AccessorySwitchTimeRequestFrame extends Cs2Frame {
        AccessorySwitchTimeRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Target device UID. */
        public McanUid targetUid() { return data().uidAt(0); }

        /** Switch time in 10 ms increments (0 = factory default). */
        public int time10ms() { return data().int16At(5); }
    }

    /** Accessory Switch Time reply. */
    public static final class AccessorySwitchTimeReplyFrame extends Cs2Frame {
        AccessorySwitchTimeReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Target device UID. */
        public McanUid targetUid() { return data().uidAt(0); }

        /** Switch time in 10 ms increments (0 = factory default). */
        public int time10ms() { return data().int16At(5); }
    }

    /** Fast Read MFX request (suppresses inter-read wait for the addressed SID). */
    public static final class FastReadMfxRequestFrame extends Cs2Frame {
        FastReadMfxRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Sender device UID. */
        public McanUid senderUid() { return data().uidAt(0); }

        /** 16-bit MFX track address (SID). */
        public int mfxSid() { return data().int16At(5); }
    }

    /** Fast Read MFX reply. */
    public static final class FastReadMfxReplyFrame extends Cs2Frame {
        FastReadMfxReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Sender device UID. */
        public McanUid senderUid() { return data().uidAt(0); }

        /** 16-bit MFX track address (SID). */
        public int mfxSid() { return data().int16At(5); }
    }

    /** Enable Track Protocol request (selects which protocols the GFP transmits). */
    public static final class EnableTrackProtocolRequestFrame extends Cs2Frame {
        EnableTrackProtocolRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Target device UID. */
        public McanUid targetUid() { return data().uidAt(0); }

        /** Protocol enable bitmask (see TRACK_PROTOCOL_* constants). */
        public int protocolMask() { return data().byteAt(5); }
    }

    /** Enable Track Protocol reply. */
    public static final class EnableTrackProtocolReplyFrame extends Cs2Frame {
        EnableTrackProtocolReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Target device UID. */
        public McanUid targetUid() { return data().uidAt(0); }

        /** Protocol enable bitmask (see TRACK_PROTOCOL_* constants). */
        public int protocolMask() { return data().byteAt(5); }
    }

    /** MFX Re-registration counter request. */
    public static final class MfxReregistrationRequestFrame extends Cs2Frame {
        MfxReregistrationRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Target device UID. */
        public McanUid targetUid() { return data().uidAt(0); }

        /** 16-bit re-registration counter value. */
        public int counter() { return data().int16At(5); }
    }

    /** MFX Re-registration counter reply. */
    public static final class MfxReregistrationReplyFrame extends Cs2Frame {
        MfxReregistrationReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Target device UID. */
        public McanUid targetUid() { return data().uidAt(0); }

        /** 16-bit re-registration counter value. */
        public int counter() { return data().int16At(5); }
    }

    /** System Overload notification (channel exceeded operating limit). Reply-only by spec. */
    public static final class SystemOverloadFrame extends Cs2Frame {
        SystemOverloadFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Reporting device UID. */
        public McanUid senderUid() { return data().uidAt(0); }

        /** Channel number that caused the overload. */
        public int channel() { return data().byteAt(5); }
    }

    /** System Status query (DLC=6): requests measurement value for a channel. */
    public static final class SystemStatusRequestFrame extends Cs2Frame {
        SystemStatusRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Target device UID. */
        public McanUid deviceUid() { return data().uidAt(0); }

        /** Channel number to query. */
        public int channel() { return data().byteAt(5); }
    }

    /** System Status reply (DLC=8): carries the measured value for the queried channel. */
    public static final class SystemStatusReplyFrame extends Cs2Frame {
        SystemStatusReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Sender device UID. */
        public McanUid deviceUid() { return data().uidAt(0); }

        /** Channel number. */
        public int channel() { return data().byteAt(5); }

        /** 16-bit measured value. */
        public int measuredValue() { return data().int16At(6); }
    }

    /** Device Identifier query (DLC=5): asks the device to report its 16-bit identifier. */
    public static final class DeviceIdentifierRequestFrame extends Cs2Frame {
        DeviceIdentifierRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Target device UID. */
        public McanUid targetUid() { return data().uidAt(0); }
    }

    /** Device Identifier set/reply (DLC=7): carries or assigns the 16-bit device identifier. */
    public static final class DeviceIdentifierReplyFrame extends Cs2Frame {
        DeviceIdentifierReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Target device UID. */
        public McanUid targetUid() { return data().uidAt(0); }

        /** 16-bit device identifier. */
        public int identifier() { return data().int16At(5); }
    }

    /**
     * MFX Seek request (sub-cmd 0x30, DLC=6): seek with UID + range.
     * Higher DLC variants carry intermediate recognition results.
     */
    public static final class MfxSeekRequestFrame extends Cs2Frame {
        MfxSeekRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Sender device UID. */
        public McanUid senderUid() { return data().uidAt(0); }

        /** MFX seek range value (data byte 5; 0 = force re-registration). */
        public int range() { return data().byteAt(5); }
    }

    /** MFX Seek reply. */
    public static final class MfxSeekReplyFrame extends Cs2Frame {
        MfxSeekReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Sender device UID. */
        public McanUid senderUid() { return data().uidAt(0); }

        /** MFX seek range value (data byte 5). */
        public int range() { return data().byteAt(5); }
    }

    /** System Reset request. */
    public static final class SystemResetRequestFrame extends Cs2Frame {
        SystemResetRequestFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Target device UID. */
        public McanUid targetUid() { return data().uidAt(0); }

        /** Reset target parameter (device-specific). */
        public int resetTarget() { return data().byteAt(5); }
    }

    /** System Reset reply. */
    public static final class SystemResetReplyFrame extends Cs2Frame {
        SystemResetReplyFrame(Cs2Header h, Cs2Data d, RawFrame p) { super(h, d, p); }

        /** Target device UID. */
        public McanUid targetUid() { return data().uidAt(0); }

        /** Reset target parameter (device-specific). */
        public int resetTarget() { return data().byteAt(5); }
    }

    // --- helpers ---

    private static int[] buildSystemCommand(long uid, int subCommand, boolean response, int dlc) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF),
            (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF),
            (int)( uid        & 0xFF),
            subCommand & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CODE, response,
                BROADCAST_HASH, dlc, data);
    }

    private static int[] uidAndBytes(long uid, int... extra) {
        int[] result = new int[4 + extra.length];
        result[0] = (int)((uid >> 24) & 0xFF);
        result[1] = (int)((uid >> 16) & 0xFF);
        result[2] = (int)((uid >>  8) & 0xFF);
        result[3] = (int)( uid        & 0xFF);
        System.arraycopy(extra, 0, result, 4, extra.length);
        return result;
    }
}
