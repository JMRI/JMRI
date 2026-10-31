package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.McanUid;
import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2Data;
import jmri.jmrix.marklin.mcan.classify.Cs2Header;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Encoder, parser and typed result classes for CS2 GUI information transfer
 * commands (codes 0x1D, 0x20 and 0x21).
 * <p>
 * These commands synchronise configuration data between MS2 throttles or CS2/CS3 GUI processors.
 * <ul>
 *   <li>Status Data Config (0x1D): query measurement/configuration channel
 *       descriptions from a device. Request: DLC=5 (UID + index). Response
 *       arrives as a data stream (Hash field carries the packet number).</li>
 *   <li>Request Config Data (0x20): request a named configuration file.
 *       DLC=8, payload is an 8-byte ASCII filename zero-padded.</li>
 *   <li>Config Data Stream (0x21): carries the actual file payload.
 *       DLC=6 (first packet, response to request), DLC=7 (first packet,
 *       broadcast change), DLC=8 (subsequent data packets).</li>
 * </ul>
 */
public final class Cs2GuiCommands {

    public static final int COMMAND_STATUS_DATA_CONFIG  = 0x1D;
    public static final int COMMAND_REQUEST_CONFIG_DATA = 0x20;
    public static final int COMMAND_CONFIG_DATA_STREAM  = 0x21;

    /** Maximum filename length in a Request Config Data frame. */
    public static final int FILENAME_MAX_BYTES = 8;

    static final int DEFAULT_PRIORITY = 0;
    static final int BROADCAST_HASH   = 0x0300;

    private Cs2GuiCommands() {}

    // --- encoders ---

    /**
     * Builds a Status Data Config request (0x1D, DLC=5).
     * Queries the measurement/configuration channel description at {@code index}
     * from the device identified by {@code deviceUid}.
     * Index 0 returns the device description.
     * Indices 1+ return per-channel descriptions.
     *
     * @param deviceUid 32-bit target device UID
     * @param index     channel index to query (0 = device description)
     * @return 13-byte CAN frame
     */
    public static int[] buildStatusDataConfig(McanUid deviceUid, int index) {
        int[] data = new int[]{
            (int)((deviceUid.toLong() >> 24) & 0xFF),
            (int)((deviceUid.toLong() >> 16) & 0xFF),
            (int)((deviceUid.toLong() >>  8) & 0xFF),
            (int)( deviceUid.toLong() & 0xFF),
            index & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_STATUS_DATA_CONFIG,
                false, BROADCAST_HASH, 5, data);
    }

    /**
     * Builds a Request Config Data command (0x20, DLC=8).
     * Requests the named configuration file from the CS2.
     * The filename is ASCII-encoded and zero-padded to 8 bytes.
     *
     * Known filenames: "lokinfo", "loknamen", "maginfo", "lokdb", "ldbver",
     * "loks", "mags", "gbs", "fs", "lokstat", "magstat", "gbsstat".
     *
     * Not all filenames may be supported across MS2/CS2/CS3/CS3+ devices.
     * Data formats may differ across devices and firmware versions.
     *
     * @param filename configuration file name (max 8 ASCII characters)
     * @return 13-byte CAN frame
     * @throws IllegalArgumentException if filename exceeds 8 characters
     */
    public static int[] buildRequestConfigData(String filename) {
        return buildRequestConfigData(filename, BROADCAST_HASH);
    }

    /**
     * Request a named configuration using the caller's transaction hash.
     *
     * @param filename nonempty ASCII token, at most eight bytes
     * @param hash requester hash (16 bits)
     * @return 13-byte CAN frame
     */
    public static int[] buildRequestConfigData(String filename, int hash) {
        if (filename.isEmpty() || !StandardCharsets.US_ASCII.newEncoder().canEncode(filename)
                || filename.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("filename must be a nonempty ASCII token");
        }
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
                false, hash, 8, data);
    }

    /**
     * Request one MS2 locomotive record.
     *
     * The locomotive name is split by byte rather than multibyte character into two zero-padded eight-byte argument frames.
     *
     * @param name exact locomotive name, one to sixteen UTF-8 bytes
     * @param hash requester hash (16 bits)
     * @return token frame followed by both name argument frames
     */
    public static List<int[]> buildRequestLocoInfo(String name, int hash) {
        if (name.isEmpty() || name.indexOf('\0') >= 0
                || !StandardCharsets.UTF_8.newEncoder().canEncode(name)) {
            throw new IllegalArgumentException("Invalid locomotive name");
        }
        byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 16) {
            throw new IllegalArgumentException("Locomotive name exceeds sixteen UTF-8 bytes: " + name);
        }
        List<int[]> frames = new ArrayList<>();
        frames.add(buildRequestConfigData("lokinfo", hash));
        for (int offset = 0; offset < 16; offset += 8) {
            int[] data = new int[8];
            for (int i = 0; i < 8 && offset + i < bytes.length; i++) {
                data[i] = bytes[offset + i] & 0xFF;
            }
            frames.add(Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_REQUEST_CONFIG_DATA,
                    false, hash, 8, data));
        }
        return frames;
    }

    /**
     * Builds the first packet of a Config Data Stream response (0x21, DLC=6).
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
     * Builds a Config Data Stream data packet (0x21, DLC=8).
     * The hash remains constant throughout the stream.
     * The hash is either the receiver's hash for a requested transfer or the sender's hash for a broadcast.
     *
     * @param hash         transaction hash (16 bits)
     * @param data8        exactly 8 data bytes (zero-pad the final packet if needed)
     * @return 13-byte CAN frame
     * @throws IllegalArgumentException if data8 does not have exactly 8 elements
     */
    public static int[] buildConfigDataStreamPacket(int hash, int[] data8) {
        if (data8.length != 8) {
            throw new IllegalArgumentException("data8 must have exactly 8 elements");
        }
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_CONFIG_DATA_STREAM,
                false, hash, 8, data8);
    }

    // --- parser ---

    static Cs2ClassifiedFrame parse(Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
        switch (header.resolvedCommand()) {
            case STATUS_DATA_CONFIG:
                return new StatusDataConfigRequestFrame(header, data, parent);
            case STATUS_DATA_CONFIG_REPLY:
                return new StatusDataConfigReplyFrame(header, data, parent);
            case REQUEST_CONFIG_DATA:
                return new RequestConfigDataRequestFrame(header, data, parent);
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
            case 6:
            case 7: return new ConfigDataStreamHeaderFrame(header, data, parent);
            case 8: return new ConfigDataStreamPacketFrame(header, data, parent);
            default: return new Cs2ClassifiedFrame(header, data, parent);
        }
    }

    // --- typed result classes ---

    /**
     * A Status Data Config request (0x1D, DLC=5).
     * Queries the measurement/configuration channel description at {@code index}
     * from the device identified by {@code deviceUid}.
     */
    public static final class StatusDataConfigRequestFrame extends Cs2ClassifiedFrame {
        StatusDataConfigRequestFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 32-bit target device UID. */
        public McanUid deviceUid() { return data().uidAt(0); }

        /** Channel index to query (0 = device description). */
        public int index() { return data().byteAt(4); }
    }

    /**
     * A Request Config Data request (0x20, DLC=8).
     * Requests the named configuration file from the CS2 / CdB.
     */
    public static final class RequestConfigDataRequestFrame extends Cs2ClassifiedFrame {
        RequestConfigDataRequestFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 8-byte ASCII filename. */
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
     * A Status Data Config reply
     * It arrives as a data stream. This frame is
     * the final acknowledgement carrying the device UID, index and packet count.
     */
    public static final class StatusDataConfigReplyFrame extends Cs2ClassifiedFrame {
        StatusDataConfigReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 32-bit target device UID. */
        public McanUid deviceUid() { return data().uidAt(0); }

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
     * The header packet of a Config Data Stream (DLC=6): Carries total file length and CRC.
     */
    public static final class ConfigDataStreamHeaderFrame extends Cs2ClassifiedFrame {
        ConfigDataStreamHeaderFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** Total byte count of the config data stream. */
        public long fileLength() { return data().int32At(0); }

        /** 16-bit CRC of the complete data (polynomial 0x1021, seed 0xFFFF). */
        public int crc() { return data().int16At(4); }
    }

    /**
     * A data packet in a Config Data Stream (DLC=8): Carries 8 payload bytes.
     * The hash identifies the transaction rather than a packet sequence number.
     */
    public static final class ConfigDataStreamPacketFrame extends Cs2ClassifiedFrame {
        ConfigDataStreamPacketFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The receiver or sender hash retained throughout the stream. */
        public int transactionHash() { return header().hash(); }

        /**
         * Compatibility accessor. This protocol has no per-packet sequence number.
         * @return transaction hash
         * @deprecated use {@link #transactionHash()}
         */
        @Deprecated
        public int packetNumber() { return transactionHash(); }
    }
}
