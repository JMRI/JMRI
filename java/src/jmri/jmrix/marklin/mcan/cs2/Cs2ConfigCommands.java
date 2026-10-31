package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.McanUid;
import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2Data;
import jmri.jmrix.marklin.mcan.classify.Cs2Header;
import jmri.jmrix.marklin.mcan.classify.FrameProtocol;
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
        return buildPing(BROADCAST_HASH);
    }

    /**
     * Builds a broadcast ping with the caller's sender hash.
     * @param hash 16-bit sender hash
     * @return 13-byte CAN frame
     */
    public static int[] buildPing(int hash) {
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_PING,
                false, hash, 0, new int[0]);
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
    public static int[] buildUpdateOffer(McanUid uid, int version) {
        int[] data = new int[]{
            (int)((uid.toLong() >> 24) & 0xFF),
            (int)((uid.toLong() >> 16) & 0xFF),
            (int)((uid.toLong() >>  8) & 0xFF),
            (int)( uid.toLong() & 0xFF),
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
    public static int[] buildPingReply(McanUid uid, int softwareVersion, int deviceType) {
        int[] data = new int[]{
            (int)((uid.toLong() >> 24) & 0xFF),
            (int)((uid.toLong() >> 16) & 0xFF),
            (int)((uid.toLong() >>  8) & 0xFF),
            (int)( uid.toLong() & 0xFF),
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
    public static int[] buildUpdateOfferReply(McanUid uid, int version, int deviceType) {
        int[] data = new int[]{
            (int)((uid.toLong() >> 24) & 0xFF),
            (int)((uid.toLong() >> 16) & 0xFF),
            (int)((uid.toLong() >>  8) & 0xFF),
            (int)( uid.toLong() & 0xFF),
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
    public static int[] buildReadConfigDataReply(McanUid uid, int channelIndex, int subIndex, int packetCount) {
        int[] data = new int[]{
            (int)((uid.toLong() >> 24) & 0xFF),
            (int)((uid.toLong() >> 16) & 0xFF),
            (int)((uid.toLong() >>  8) & 0xFF),
            (int)( uid.toLong() & 0xFF),
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
    public static int[] buildBootRequestReply(McanUid uid) {
        int[] data = new int[]{
            (int)((uid.toLong() >> 24) & 0xFF),
            (int)((uid.toLong() >> 16) & 0xFF),
            (int)((uid.toLong() >>  8) & 0xFF),
            (int)( uid.toLong() & 0xFF)
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_BOOT_REQUEST,
                true, BROADCAST_HASH, 4, data);
    }

    // --- typed result classes ---

    /**
     * Typed result for a parsed ping request frame (DLC=0, broadcast).
     * Sent periodically by CS2-capable devices to announce their presence.
     */
    public static final class PingRequestFrame extends Cs2ClassifiedFrame {
        PingRequestFrame(Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
            super(header, data, parent);
        }
    }

    /**
     * A CAN BOOT request frame (0x1B).
     * <ul>
     *   <li>DLC=0: enter firmware update (bootloader) mode.</li>
     *   <li>DLC=5: normal Gleisbox activation (data byte 4 = 0x11 magic).</li>
     * </ul>
     */
    public static final class BootCanRequestFrame extends Cs2ClassifiedFrame {
        BootCanRequestFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** Returns true when this is a normal Gleisbox activation (DLC=5, magic byte present). */
        public boolean isGleisboxActivation() {
            return header().dlc() == 5 && data().byteAt(4) == GLEISBOX_MAGIC;
        }
    }

    /**
     * A firmware Update Offer request (0x19, DLC=6): UID + 2-byte version.
     * Sent to a device to announce that a firmware update is available.
     */
    public static final class UpdateOfferRequestFrame extends Cs2ClassifiedFrame {
        UpdateOfferRequestFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 32-bit target device UID. */
        public McanUid uid() { return data().uidAt(0); }

        /** 16-bit firmware version being offered. */
        public int version() { return data().int16At(4); }
    }

    /**
     * A Read Config Data request (0x1A, DLC=6 or DLC=8).
     * Requests the device identified by {@code uid()} to stream its configuration data
     * for the given channel and sub-index.
     */
    public static final class ReadConfigDataRequestFrame extends Cs2ClassifiedFrame {
        ReadConfigDataRequestFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 32-bit target device UID. */
        public McanUid uid() { return data().uidAt(0); }

        /** Channel index to read. */
        public int channelIndex() { return data().byteAt(4); }

        /** Sub-index within the channel. */
        public int subIndex() { return data().byteAt(5); }
    }

    /**
     * Typed result for a parsed ping reply frame.
     * Data layout: UID (4 bytes) + software version (2 bytes) + device type (2 bytes).
     */
    public static final class PingReplyFrame extends Cs2ClassifiedFrame {
        PingReplyFrame(Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
            super(header, data, parent);
        }

        /** 32-bit device UID. */
        public McanUid uid() {
            return data().uidAt(0);
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
        public McanUid uid() { return data().uidAt(0); }

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
        public McanUid uid() { return data().uidAt(0); }

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
        public McanUid uid() { return data().uidAt(0); }
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
        McanUid uid = McanUid.of(((long)(raw[5] & 0xFF) << 24)
                 | ((long)(raw[6] & 0xFF) << 16)
                 | ((long)(raw[7] & 0xFF) <<  8)
                 | (raw[8] & 0xFF));
        if (uid.isBroadcast() || uid.isUninitialized()) {
            return Optional.empty();
        }
        int softwareVersion = ((raw[9]  & 0xFF) << 8) | (raw[10] & 0xFF);
        int deviceType      = ((raw[11] & 0xFF) << 8) | (raw[12] & 0xFF);
        return Optional.of(new NativeDeviceInfo(uid, deviceType, softwareVersion, frame.cs2Header().hash()));
    }

    static Cs2ClassifiedFrame parse(Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
        switch (header.resolvedCommand()) {
            case PING:
                return new PingRequestFrame(header, data, parent);
            case PING_REPLY:
                return parsePingReply(header, data, parent);
            case BOOT_CAN:
                return new BootCanRequestFrame(header, data, parent);
            case BOOT_CAN_REPLY:
                return new CanBootReplyFrame(header, data, parent);
            case UPDATE_OFFER:
                return new UpdateOfferRequestFrame(header, data, parent);
            case UPDATE_OFFER_REPLY:
                return new UpdateOfferReplyFrame(header, data, parent);
            case READ_CONFIG_DATA:
                return new ReadConfigDataRequestFrame(header, data, parent);
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
        McanUid uid = data.uidAt(0);
        if (uid.isBroadcast() || uid.isUninitialized()) {
            return new Cs2ClassifiedFrame(header, data, parent);
        }
        return new PingReplyFrame(header, data, parent);
    }
}
