package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2Data;
import jmri.jmrix.marklin.mcan.classify.Cs2Header;
import java.nio.charset.StandardCharsets;

/**
 * Encoder, parser and typed result classes for CS2 GUI information transfer
 * commands (codes 0x20–0x22).
 * <p>
 * These commands synchronise configuration data between CS2 GUI processors.
 * <ul>
 *   <li>Status Data Config (0x20): query measurement/configuration channel
 *       descriptions from a device. Request: DLC=5 (UID + index). Response
 *       arrives as a data stream (Hash field carries the packet number).</li>
 *   <li>Request Config Data (0x21): request a named configuration file.
 *       DLC=8, payload is an 8-byte ASCII filename zero-padded.</li>
 *   <li>Config Data Stream (0x22): carries the actual file payload.
 *       DLC=6 (first packet, response to request), DLC=7 (first packet,
 *       broadcast change), DLC=8 (subsequent data packets).</li>
 * </ul>
 */
public final class Cs2GuiCommands {

    public static final int COMMAND_STATUS_DATA_CONFIG  = 0x20;
    public static final int COMMAND_REQUEST_CONFIG_DATA = 0x21;
    public static final int COMMAND_CONFIG_DATA_STREAM  = 0x22;

    /** Maximum filename length in a Request Config Data frame. */
    public static final int FILENAME_MAX_BYTES = 8;

    static final int DEFAULT_PRIORITY = 0;
    static final int BROADCAST_HASH   = 0x0300;

    private Cs2GuiCommands() {}

    // --- encoders ---

    /**
     * Builds a Status Data Config request (0x20, DLC=5).
     * Queries the measurement/configuration channel description at {@code index}
     * from the device identified by {@code deviceUid}.
     * Index 0 returns the device description; index 1+ returns per-channel descriptions.
     *
     * @param deviceUid 32-bit target device UID
     * @param index     channel index to query (0 = device description)
     * @return 13-byte CAN frame
     */
    public static int[] buildStatusDataConfig(long deviceUid, int index) {
        int[] data = new int[]{
            (int)((deviceUid >> 24) & 0xFF),
            (int)((deviceUid >> 16) & 0xFF),
            (int)((deviceUid >>  8) & 0xFF),
            (int)( deviceUid        & 0xFF),
            index & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_STATUS_DATA_CONFIG,
                false, BROADCAST_HASH, 5, data);
    }

    /**
     * Builds a Request Config Data command (0x21, DLC=8).
     * Requests the named configuration file from the CS2.
     * The filename is ASCII-encoded and zero-padded to 8 bytes.
     * Known filenames: "lokinfo", "loknamen", "maginfo", "lokdb", "ldbver",
     * "loks", "mags", "gbs", "fs", "lokstat", "magstat", "gbsstat".
     *
     * @param filename configuration file name (max 8 ASCII characters)
     * @return 13-byte CAN frame
     * @throws IllegalArgumentException if filename exceeds 8 characters
     */
    public static int[] buildRequestConfigData(String filename) {
        byte[] nameBytes = filename.getBytes(StandardCharsets.US_ASCII);
        if (nameBytes.length > FILENAME_MAX_BYTES) {
            throw new IllegalArgumentException(
                    "filename exceeds " + FILENAME_MAX_BYTES + " bytes: " + filename);
        }
        int[] data = new int[FILENAME_MAX_BYTES];
        for (int i = 0; i < nameBytes.length; i++) {
            data[i] = nameBytes[i] & 0xFF;
        }
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_REQUEST_CONFIG_DATA,
                false, BROADCAST_HASH, 8, data);
    }

    /**
     * Builds the first packet of a Config Data Stream response (0x22, DLC=6).
     * Sent by the CS2 immediately after acknowledging a Request Config Data command.
     * Contains the total file size and CRC of the data to follow.
     * Subsequent packets use DLC=8 and carry 8 data bytes each.
     *
     * @param fileLength total byte count of the config data to stream
     * @param crc        16-bit CRC of the complete data (polynomial 0x1021, seed 0xFFFF)
     * @return 13-byte CAN frame
     */
    public static int[] buildConfigDataStreamHeader(long fileLength, int crc) {
        int[] data = new int[]{
            (int)((fileLength >> 24) & 0xFF),
            (int)((fileLength >> 16) & 0xFF),
            (int)((fileLength >>  8) & 0xFF),
            (int)( fileLength        & 0xFF),
            (crc >> 8) & 0xFF,
            crc & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CONFIG_DATA_STREAM,
                false, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds a Config Data Stream data packet (0x22, DLC=8).
     * The {@code packetNumber} is placed in the Hash field for sequencing;
     * bits 7–9 of the hash are masked off per the CS2 protocol specification.
     *
     * @param packetNumber sequence number (0–8191)
     * @param data8        exactly 8 data bytes (zero-pad the final packet if needed)
     * @return 13-byte CAN frame
     * @throws IllegalArgumentException if data8 does not have exactly 8 elements
     */
    public static int[] buildConfigDataStreamPacket(int packetNumber, int[] data8) {
        if (data8.length != 8) {
            throw new IllegalArgumentException("data8 must have exactly 8 elements");
        }
        // Packet number lives in non-fingerprint bits; bits 9-8 carry the CS2 fingerprint (0x0300).
        int hash = (packetNumber & 0x1F8F) | BROADCAST_HASH;
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CONFIG_DATA_STREAM,
                false, hash, 8, data8);
    }

    // --- parser ---

    static Cs2ClassifiedFrame parse(Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
        switch (header.resolvedCommand()) {
            case STATUS_DATA_CONFIG_REPLY:
                return new StatusDataConfigReplyFrame(header, data, parent);
            case REQUEST_CONFIG_DATA_REPLY:
                return new RequestConfigDataReplyFrame(header, data, parent);
            case CONFIG_DATA_STREAM:
            case CONFIG_DATA_STREAM_REPLY:
                return parseConfigDataStream(header, data, parent);
            default:
                return new Cs2ClassifiedFrame(header, data, parent);
        }
    }

    private static Cs2ClassifiedFrame parseConfigDataStream(
            Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
        switch (header.dlc()) {
            case 6: return new ConfigDataStreamHeaderFrame(header, data, parent);
            case 8: return new ConfigDataStreamPacketFrame(header, data, parent);
            default: return new Cs2ClassifiedFrame(header, data, parent);
        }
    }

    // --- typed result classes ---

    /**
     * A Status Data Config reply  -  arrives as a data stream; this frame is
     * the final acknowledgement carrying the device UID, index and packet count.
     */
    public static final class StatusDataConfigReplyFrame extends Cs2ClassifiedFrame {
        StatusDataConfigReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 32-bit target device UID. */
        public long deviceUid() { return data().int32At(0); }

        /** Channel index this reply relates to. */
        public int index() { return data().byteAt(4); }

        /** Number of stream packets that were sent. */
        public int packetCount() { return data().byteAt(5); }
    }

    /**
     * A Request Config Data acknowledgement reply (echoes the request with Response=1).
     */
    public static final class RequestConfigDataReplyFrame extends Cs2ClassifiedFrame {
        RequestConfigDataReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 8-byte filename from the acknowledged request. */
        public String filename() {
            byte[] buf = new byte[FILENAME_MAX_BYTES];
            int len = 0;
            for (int i = 0; i < FILENAME_MAX_BYTES; i++) {
                int b = data().byteAt(i);
                if (b == 0) break;
                buf[i] = (byte) b;
                len++;
            }
            return new String(buf, 0, len, StandardCharsets.US_ASCII);
        }
    }

    /**
     * The header packet of a Config Data Stream (DLC=6): carries total file length and CRC.
     */
    public static final class ConfigDataStreamHeaderFrame extends Cs2ClassifiedFrame {
        ConfigDataStreamHeaderFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** Total byte count of the config data stream. */
        public long fileLength() { return data().int32At(0); }

        /** 16-bit CRC of the complete data (polynomial 0x1021, seed 0xFFFF). */
        public int crc() { return data().int16At(4); }
    }

    /**
     * A data packet in a Config Data Stream (DLC=8): carries 8 payload bytes.
     * The sequence number is in the Hash field of the header.
     */
    public static final class ConfigDataStreamPacketFrame extends Cs2ClassifiedFrame {
        ConfigDataStreamPacketFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** Sequence number extracted from the Hash field (bits 7–9 and fingerprint bits masked off). */
        public int packetNumber() { return header().hash() & 0x1F8F & ~BROADCAST_HASH; }
    }
}
