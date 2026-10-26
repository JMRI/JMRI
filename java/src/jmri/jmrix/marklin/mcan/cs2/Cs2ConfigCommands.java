package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2Data;
import jmri.jmrix.marklin.mcan.classify.Cs2Header;
import jmri.jmrix.marklin.mcan.classify.FrameProtocol;
import jmri.jmrix.marklin.mcan.protocol.NativeDeviceInfo;
import java.util.Optional;

/**
 * Encoder/decoder for CS2 software update and configuration commands (codes 0x18–0x1C).
 * Covers device ping, firmware update offer, config data read, CAN boot (Gleisbox
 * activation) and bootloader mode.
 */
public final class Cs2ConfigCommands {

    public static final int COMMAND_PING             = 0x18;
    public static final int COMMAND_UPDATE_OFFER     = 0x19;
    public static final int COMMAND_READ_CONFIG_DATA = 0x1A;
    public static final int COMMAND_BOOT_CAN         = 0x1B;
    public static final int COMMAND_BOOT_REQUEST     = 0x1C;

    static final int DEFAULT_PRIORITY = 0;
    static final int BROADCAST_HASH   = 0x0300;

    /** Magic value for Gleisbox activation (CAN BOOT normal-ops variant). */
    static final int GLEISBOX_MAGIC = 0x11;

    private Cs2ConfigCommands() {}

    /**
     * Builds a CS2 native ping request (broadcast, DLC=0).
     *
     * @return 13-byte CAN frame
     */
    public static int[] buildPing() {
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_PING,
                false, BROADCAST_HASH, 0, new int[0]);
    }

    /**
     * Builds a CAN BOOT activation command (0x1B) for normal Gleisbox startup.
     * DLC=5, data byte 4 = 0x11 (magic for normal operations mode).
     *
     * @return 13-byte CAN frame
     */
    public static int[] buildCanBoot() {
        int[] data = new int[]{0x00, 0x00, 0x00, 0x00, GLEISBOX_MAGIC};
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_BOOT_CAN,
                false, BROADCAST_HASH, 5, data);
    }

    /**
     * Builds a CAN BOOT bootloader mode command (0x1B).
     * DLC=0, no data  -  invokes firmware update sequence.
     * Equivalent to {@code MarklinMessage.getCanBootloaderMode()}.
     *
     * @return 13-byte CAN frame
     */
    public static int[] buildBootloaderMode() {
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_BOOT_CAN,
                false, BROADCAST_HASH, 0, new int[0]);
    }

    /**
     * Builds a firmware update offer command (0x19).
     * DLC=6: UID (4 bytes) + version (2 bytes, big-endian).
     *
     * @param uid     32-bit device UID
     * @param version 16-bit firmware version
     * @return 13-byte CAN frame
     */
    public static int[] buildUpdateOffer(long uid, int version) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF),
            (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF),
            (int)( uid        & 0xFF),
            (version >> 8) & 0xFF,
            version & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_UPDATE_OFFER,
                false, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds a ping reply (0x18, DLC=8): UID + software version + device type.
     *
     * @param uid             32-bit device UID
     * @param softwareVersion 16-bit software version
     * @param deviceType      16-bit device type code
     * @return 13-byte CAN frame
     */
    public static int[] buildPingReply(long uid, int softwareVersion, int deviceType) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF),
            (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF),
            (int)( uid        & 0xFF),
            (softwareVersion >> 8) & 0xFF,
            softwareVersion & 0xFF,
            (deviceType >> 8) & 0xFF,
            deviceType & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_PING,
                true, BROADCAST_HASH, 8, data);
    }

    /**
     * Builds a CAN boot reply (0x1B, DLC=0).
     *
     * @return 13-byte CAN frame
     */
    public static int[] buildCanBootReply() {
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_BOOT_CAN,
                true, BROADCAST_HASH, 0, new int[0]);
    }

    /**
     * Builds an update offer reply (0x19, DLC=8): UID + firmware version + device type.
     *
     * @param uid        32-bit device UID
     * @param version    16-bit firmware version
     * @param deviceType 16-bit device type code
     * @return 13-byte CAN frame
     */
    public static int[] buildUpdateOfferReply(long uid, int version, int deviceType) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF),
            (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF),
            (int)( uid        & 0xFF),
            (version >> 8) & 0xFF,
            version & 0xFF,
            (deviceType >> 8) & 0xFF,
            deviceType & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_UPDATE_OFFER,
                true, BROADCAST_HASH, 8, data);
    }

    /**
     * Builds a Read Config Data reply (0x1A, DLC=8): UID + channel + sub-index + packet count.
     *
     * @param uid          32-bit target device UID
     * @param channelIndex channel index being read
     * @param subIndex     sub-index within the channel
     * @param packetCount  number of data packets that will follow
     * @return 13-byte CAN frame
     */
    public static int[] buildReadConfigDataReply(long uid, int channelIndex, int subIndex, int packetCount) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF),
            (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF),
            (int)( uid        & 0xFF),
            channelIndex & 0xFF,
            subIndex & 0xFF,
            packetCount & 0xFF,
            0x00
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_READ_CONFIG_DATA,
                true, BROADCAST_HASH, 8, data);
    }

    /**
     * Builds a Boot Request reply (0x1C, DLC=4): echoes the target UID as confirmation.
     *
     * @param uid 32-bit target device UID
     * @return 13-byte CAN frame
     */
    public static int[] buildBootRequestReply(long uid) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF),
            (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF),
            (int)( uid        & 0xFF)
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_BOOT_REQUEST,
                true, BROADCAST_HASH, 4, data);
    }

    // --- typed result classes ---

    /**
     * Typed result for a parsed ping reply frame.
     * Data layout: UID (4 bytes) + software version (2 bytes) + device type (2 bytes).
     */
    public static final class PingReplyFrame extends Cs2ClassifiedFrame {
        PingReplyFrame(Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
            super(header, data, parent);
        }

        /** 32-bit device UID. */
        public long uid() {
            return data().int32At(0);
        }

        /** 16-bit software version. */
        public int softwareVersion() {
            return data().int16At(4);
        }

        /** 16-bit device type code. */
        public int deviceType() {
            return data().int16At(6);
        }
    }

    /** A CAN boot reply frame. */
    public static final class CanBootReplyFrame extends Cs2ClassifiedFrame {
        CanBootReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }
    }

    /**
     * An Update Offer reply (0x19, DLC=8): echoes the Update Offer request.
     * Layout: Device-UID (4 bytes) + firmware version (2 bytes) + device type (2 bytes).
     */
    public static final class UpdateOfferReplyFrame extends Cs2ClassifiedFrame {
        UpdateOfferReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 32-bit device UID. */
        public long uid() { return data().int32At(0); }

        /** 16-bit firmware version offered. */
        public int version() { return data().int16At(4); }

        /** 16-bit device type code. */
        public int deviceType() { return data().int16At(6); }
    }

    /**
     * A Read Config Data reply (0x1A, DLC=8).
     * Layout: Device-UID (4 bytes) + channel index (1 byte) + sub-index (1 byte) +
     *         packet count (1 byte) + reserved (1 byte).
     * Corresponds to a device's internal configuration data stream; the actual data
     * arrives as subsequent DLC=8 packets via the config data stream mechanism.
     */
    public static final class ReadConfigDataReplyFrame extends Cs2ClassifiedFrame {
        ReadConfigDataReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 32-bit target device UID. */
        public long uid() { return data().int32At(0); }

        /** Channel index being read. */
        public int channelIndex() { return data().byteAt(4); }

        /** Sub-index within the channel. */
        public int subIndex() { return data().byteAt(5); }

        /** Number of data packets that will follow. */
        public int packetCount() { return data().byteAt(6); }
    }

    /**
     * A Boot Request / Bootloader Track frame (0x1C).
     * Request (DLC=4): target device UID  -  requests the device enter bootloader mode.
     * Reply (DLC=4): echoes the target UID as confirmation.
     * Higher DLC variants carry device-specific bootloader exchange data.
     */
    public static final class BootRequestFrame extends Cs2ClassifiedFrame {
        BootRequestFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 32-bit target device UID. */
        public long uid() { return data().int32At(0); }
    }

    /**
     * Parses a first-pass classified frame as a ping reply, returning a {@link NativeDeviceInfo}
     * if the frame is a valid CS2 native ping reply with a non-reserved UID.
     *
     * @param frame first-pass classified frame
     * @return device info or empty if the frame is not a valid ping reply
     */
    public static Optional<NativeDeviceInfo> parsePingReply(ClassifiedFrame frame) {
        if (frame.protocol() != FrameProtocol.CS2_NATIVE) return Optional.empty();
        if (!frame.cs2Header().response())                 return Optional.empty();
        if (frame.cs2Header().commandCode() != COMMAND_PING) return Optional.empty();
        if (frame.cs2Header().dlc() != 8)                  return Optional.empty();

        int[] raw = frame.raw().toArray();
        long uid = ((long)(raw[5] & 0xFF) << 24)
                 | ((long)(raw[6] & 0xFF) << 16)
                 | ((long)(raw[7] & 0xFF) <<  8)
                 | (raw[8] & 0xFF);
        if (uid == NativeDeviceInfo.UID_BROADCAST || uid == NativeDeviceInfo.UID_UNINITIALIZED) {
            return Optional.empty();
        }
        int softwareVersion = ((raw[9]  & 0xFF) << 8) | (raw[10] & 0xFF);
        int deviceType      = ((raw[11] & 0xFF) << 8) | (raw[12] & 0xFF);
        return Optional.of(new NativeDeviceInfo(uid, deviceType, softwareVersion, frame.cs2Header().hash()));
    }

    static Cs2ClassifiedFrame parse(Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
        switch (header.resolvedCommand()) {
            case PING_REPLY:
                return parsePingReply(header, data, parent);
            case BOOT_CAN_REPLY:
                return new CanBootReplyFrame(header, data, parent);
            case UPDATE_OFFER_REPLY:
                return new UpdateOfferReplyFrame(header, data, parent);
            case READ_CONFIG_DATA_REPLY:
                return header.dlc() == 8 ? new ReadConfigDataReplyFrame(header, data, parent)
                        : new Cs2ClassifiedFrame(header, data, parent);
            case BOOT_REQUEST:
            case BOOT_REQUEST_REPLY:
                return new BootRequestFrame(header, data, parent);
            default:
                return new Cs2ClassifiedFrame(header, data, parent);
        }
    }

    private static Cs2ClassifiedFrame parsePingReply(Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
        if (header.dlc() != 8) {
            return new Cs2ClassifiedFrame(header, data, parent);
        }
        long uid = data.int32At(0);
        if (uid == 0x00000000L || uid == 0xFFFFFFFFL) {
            return new Cs2ClassifiedFrame(header, data, parent);
        }
        return new PingReplyFrame(header, data, parent);
    }
}
