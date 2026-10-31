package jmri.jmrix.marklin.mcan.cdb;

import jmri.jmrix.marklin.mcan.McanFrame;
import jmri.jmrix.marklin.mcan.RawFrame;
import jmri.jmrix.marklin.mcan.FrameProtocol;

import java.util.Optional;

/**
 * Parser for CdB configuration-bus frames.
 * <p>
 * Returns a typed subclass of {@link CdBFrame} that callers can dispatch on via
 * {@code instanceof} or by switching on {@link CdBFrame#frameType()}.
 */
public final class CdBFrameParser {

    private CdBFrameParser() {}

    /**
     * Parses a frame that carries the CdB configuration hash pattern.
     *
     * @param frame the 13-byte frame
     * @return typed frame; {@link CdBFrame#frameType()} is {@link CdBFrameType#UNKNOWN}
     *         for unrecognised CAN ID offsets
     */
    public static CdBFrame parse(RawFrame frame) {
        int[] raw = frame.toArray();

        int canId    = ((raw[0] & 0x1F) << 24) | ((raw[1] & 0xFF) << 16)
                     | ((raw[2] & 0xFF) <<  8)  |  (raw[3] & 0xFF);
        int hash16   = canId & 0xFFFF;
        int moduleId  = (hash16 & 0xFF) - 0x80;
        int classHigh = (canId >> 21) & 0x0F;
        int classLow  = (canId >> 12) & 0x0F;
        int classId   = (classHigh << 4) | classLow;

        int pingId = CdBFrameBuilder.pingCanId(classId, moduleId);
        int delta  = canId - pingId;

        switch (delta) {
            case 0:                             return new PingFrame(classId, moduleId, frame);
            case CdBFrameBuilder.DELTA_REPLY:   return new PingReplyFrame(classId, moduleId, frame);
            case CdBFrameBuilder.DELTA_READ:    return new ReadFrame(classId, moduleId, frame);
            case CdBFrameBuilder.DELTA_READ_REP: return new ReadReplyFrame(classId, moduleId, frame);
            case CdBFrameBuilder.DELTA_WRITE:   return new WriteFrame(classId, moduleId, frame);
            default: return new CdBFrame(CdBFrameType.UNKNOWN, classId, moduleId, frame);
        }
    }

    // =================================================================
    // Typed result classes
    // =================================================================

    /**
     * Base class for all typed CdB frame results.
     */
    public static class CdBFrame extends McanFrame {

        private final CdBFrameType frameType;
        private final int classId;
        private final int moduleId;

        CdBFrame(CdBFrameType frameType, int classId, int moduleId, RawFrame raw) {
            super(FrameProtocol.CDB_CONFIG, raw);
            this.frameType = frameType;
            this.classId   = classId;
            this.moduleId  = moduleId;
        }

        public CdBFrameType frameType() { return frameType; }

        /** 8-bit CdB device class ID. */
        public int classId() { return classId; }

        /** Module slot (0 = broadcast ping, 1–127 = addressed). */
        public int moduleId() { return moduleId; }

        /**
         * Payload byte by zero-based data index (frame byte {@code 5 + index}).
         * Bytes beyond the DLC are returned as transmitted.
         * Replies can carry meaningful bytes there.
         *
         * @param index data index 0–7
         * @return byte value 0x00–0xFF
         */
        public int dataByte(int index) {
            if (index < 0 || index > 7) {
                throw new IndexOutOfBoundsException("CdB data index " + index);
            }
            return raw().byteAt(5 + index);
        }

        /** @return copy of the 13 frame bytes */
        protected int[] bytes() { return raw().toArray(); }
    }

    /** A NEW-protocol broadcast ping (DLC=0). */
    public static final class PingFrame extends CdBFrame {
        PingFrame(int classId, int moduleId, RawFrame raw) {
            super(CdBFrameType.PING, classId, moduleId, raw);
        }
    }

    /**
     * A ping reply from a CdB module.
     * <p>
     * Three shapes:
     * <ul>
     *   <li>Operational (moduleId &gt; 0, DLC=3): Module is commissioned.</li>
     *   <li>Factory-fresh (moduleId=0, DLC=6): Module needs commissioning first.
     *       Challenge bytes are at data positions 1 and 2.</li>
     *   <li>Address carrying scan reply (moduleId &gt; 0, DLC=8): The module embeds its
     *       four configured accessory addresses in data bytes 0–7 as four big-endian
     *       16-bit raw CdB address values. Observed on WeichenChef deLuxe (class 0x2A)
     *       in response to a broadcast discovery ping.</li>
     * </ul>
     */
    public static final class PingReplyFrame extends CdBFrame {
        PingReplyFrame(int classId, int moduleId, RawFrame raw) {
            super(CdBFrameType.PING_REPLY, classId, moduleId, raw);
        }

        /** True when the module is factory-fresh (moduleId=0, DLC=6). */
        public boolean isFactoryFresh() {
            return moduleId() == 0 && raw().dlc() == 6;
        }

        /** True when the module is commissioned (moduleId &gt; 0, DLC=3). */
        public boolean isOperational() {
            return moduleId() > 0 && raw().dlc() == 3;
        }

        /**
         * True when the reply carries a 4-address payload (moduleId &gt; 0, DLC=8).
         * <p>
         * This variant is produced by WeichenChef deLuxe (class 0x2A) in response
         * to a broadcast discovery ping.
         * Each of the four channel addresses occupies two big endian data bytes starting at frame byte[5].
         * Decode with {@link #addressPayload()}.
         */
        public boolean hasAddressPayload() {
            return moduleId() > 0 && raw().dlc() == 8;
        }

        /**
         * Challenge bytes from a factory-fresh reply (data positions 1 and 2,
         * frame bytes[6] and [7]). Must be echoed in {@link CdBConfigCommands#buildFactorySelect}.
         *
         * @return two-element array [challenge0, challenge1] or empty if not factory-fresh
         */
        public Optional<int[]> challengeBytes() {
            if (!isFactoryFresh()) return Optional.empty();
            int[] raw = bytes();
            return Optional.of(new int[]{raw[6] & 0xFF, raw[7] & 0xFF});
        }

        /**
         * Four raw 16 bit accessory addresses from a scan reply with DLC=8
         * <p>
         * Frame bytes 5-12 carry four big-endian 16-bit values. Each value is a raw
         * CdB address which can be decoded by {@link jmri.jmrix.marklin.mcan.address.CdBAddressCodec#decodeType2(int)}.
         * The module number is {@link #moduleId()}.
         *
         * @return four element array of raw 16 bit addresses or empty if {@link #hasAddressPayload()} is false
         */
        public Optional<int[]> addressPayload() {
            if (!hasAddressPayload()) return Optional.empty();
            int[] raw = bytes();
            return Optional.of(new int[]{
                ((raw[5] & 0xFF) << 8) | (raw[6]  & 0xFF),
                ((raw[7] & 0xFF) << 8) | (raw[8]  & 0xFF),
                ((raw[9] & 0xFF) << 8) | (raw[10] & 0xFF),
                ((raw[11] & 0xFF) << 8) | (raw[12] & 0xFF)
            });
        }
    }

    /** A register read request (DLC=2): slot + register. */
    public static final class ReadFrame extends CdBFrame {
        ReadFrame(int classId, int moduleId, RawFrame raw) {
            super(CdBFrameType.READ, classId, moduleId, raw);
        }

        /** Sub-register slot (data byte 0, frame byte[5]). */
        public int slot() { return bytes()[5] & 0xFF; }

        /** Register number (data byte 1, frame byte[6]). */
        public int reg()  { return bytes()[6] & 0xFF; }
    }

    /** A register read reply (DLC=4): slot + register + 16-bit value. */
    public static final class ReadReplyFrame extends CdBFrame {
        ReadReplyFrame(int classId, int moduleId, RawFrame raw) {
            super(CdBFrameType.READ_REPLY, classId, moduleId, raw);
        }

        /** Sub-register slot echoed from the request (frame byte[5]). */
        public int slot()  { return bytes()[5] & 0xFF; }

        /** Register number echoed from the request (frame byte[6]). */
        public int reg()   { return bytes()[6] & 0xFF; }

        /** 16-bit register value (frame bytes[7..8], big-endian). */
        public int value() { return ((bytes()[7] & 0xFF) << 8) | (bytes()[8] & 0xFF); }
    }

    /** A register write command (DLC=4): slot + register + 16-bit value. */
    public static final class WriteFrame extends CdBFrame {
        WriteFrame(int classId, int moduleId, RawFrame raw) {
            super(CdBFrameType.WRITE, classId, moduleId, raw);
        }

        /** Sub-register slot (frame byte[5]). */
        public int slot()  { return bytes()[5] & 0xFF; }

        /** Register number (frame byte[6]). */
        public int reg()   { return bytes()[6] & 0xFF; }

        /** 16-bit value to write (frame bytes[7..8], big-endian). */
        public int value() { return ((bytes()[7] & 0xFF) << 8) | (bytes()[8] & 0xFF); }
    }
}
