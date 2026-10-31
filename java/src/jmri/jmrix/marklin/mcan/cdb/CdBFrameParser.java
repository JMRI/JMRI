package jmri.jmrix.marklin.mcan.cdb;

import jmri.jmrix.marklin.mcan.classify.CdBFrameType;
import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.FrameProtocol;

import java.util.Optional;

/**
 * Second-pass parser for CdB configuration-bus frames.
 * <p>
 * Input must be a {@link ClassifiedFrame} with {@link FrameProtocol#CDB_CONFIG}.
 * Returns a typed subclass of {@link CdBFrame} that callers can dispatch on via
 * {@code instanceof} or by switching on {@link CdBFrame#frameType()}.
 */
public final class CdBFrameParser {

    private CdBFrameParser() {}

    /**
     * Parses a first-pass classified frame into a typed CdB result.
     *
     * @param frame first-pass result; protocol must be CDB_CONFIG
     * @return typed frame; {@link CdBFrame#frameType()} is {@link CdBFrameType#UNKNOWN}
     *         for unrecognised CAN ID offsets
     * @throws IllegalArgumentException if the frame is not CDB_CONFIG
     */
    public static CdBFrame parse(ClassifiedFrame frame) {
        if (frame.protocol() != FrameProtocol.CDB_CONFIG) {
            throw new IllegalArgumentException(
                "CdBFrameParser.parse() requires CDB_CONFIG, got " + frame.protocol());
        }
        int[] raw = frame.raw().toArray();

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
    public static class CdBFrame {

        private final CdBFrameType frameType;
        private final int classId;
        private final int moduleId;
        private final ClassifiedFrame parent;

        CdBFrame(CdBFrameType frameType, int classId, int moduleId, ClassifiedFrame parent) {
            this.frameType = frameType;
            this.classId   = classId;
            this.moduleId  = moduleId;
            this.parent    = parent;
        }

        public CdBFrameType frameType() { return frameType; }

        /** 8-bit CdB device class ID. */
        public int classId() { return classId; }

        /** Module slot (0 = broadcast ping, 1–127 = addressed). */
        public int moduleId() { return moduleId; }

        /** The raw first-pass classified frame. */
        public ClassifiedFrame parent() { return parent; }

        protected int[] raw() { return parent.raw().toArray(); }
    }

    /** A NEW-protocol broadcast ping (DLC=0). */
    public static final class PingFrame extends CdBFrame {
        PingFrame(int classId, int moduleId, ClassifiedFrame parent) {
            super(CdBFrameType.PING, classId, moduleId, parent);
        }
    }

    /**
     * A ping reply from a CdB module.
     * <p>
     * Two shapes:
     * <ul>
     *   <li>Operational (moduleId &gt; 0, DLC=3): module is commissioned.</li>
     *   <li>Factory-fresh (moduleId=0, DLC=6): module needs commissioning;
     *       challenge bytes are at data positions 1 and 2.</li>
     * </ul>
     */
    public static final class PingReplyFrame extends CdBFrame {
        PingReplyFrame(int classId, int moduleId, ClassifiedFrame parent) {
            super(CdBFrameType.PING_REPLY, classId, moduleId, parent);
        }

        /** True when the module is factory-fresh (moduleId=0, DLC=6). */
        public boolean isFactoryFresh() {
            return moduleId() == 0 && parent().raw().dlc() == 6;
        }

        /** True when the module is commissioned (moduleId &gt; 0, DLC=3). */
        public boolean isOperational() {
            return moduleId() > 0 && parent().raw().dlc() == 3;
        }

        /**
         * Challenge bytes from a factory-fresh reply (data positions 1 and 2,
         * frame bytes[6] and [7]). Must be echoed in {@link CdBConfigCommands#buildFactorySelect}.
         *
         * @return two-element array [challenge0, challenge1] or empty if not factory-fresh
         */
        public Optional<int[]> challengeBytes() {
            if (!isFactoryFresh()) return Optional.empty();
            int[] raw = raw();
            return Optional.of(new int[]{raw[6] & 0xFF, raw[7] & 0xFF});
        }
    }

    /** A register read request (DLC=2): slot + register. */
    public static final class ReadFrame extends CdBFrame {
        ReadFrame(int classId, int moduleId, ClassifiedFrame parent) {
            super(CdBFrameType.READ, classId, moduleId, parent);
        }

        /** Sub-register slot (data byte 0, frame byte[5]). */
        public int slot() { return raw()[5] & 0xFF; }

        /** Register number (data byte 1, frame byte[6]). */
        public int reg()  { return raw()[6] & 0xFF; }
    }

    /** A register read reply (DLC=4): slot + register + 16-bit value. */
    public static final class ReadReplyFrame extends CdBFrame {
        ReadReplyFrame(int classId, int moduleId, ClassifiedFrame parent) {
            super(CdBFrameType.READ_REPLY, classId, moduleId, parent);
        }

        /** Sub-register slot echoed from the request (frame byte[5]). */
        public int slot()  { return raw()[5] & 0xFF; }

        /** Register number echoed from the request (frame byte[6]). */
        public int reg()   { return raw()[6] & 0xFF; }

        /** 16-bit register value (frame bytes[7..8], big-endian). */
        public int value() { return ((raw()[7] & 0xFF) << 8) | (raw()[8] & 0xFF); }
    }

    /** A register write command (DLC=4): slot + register + 16-bit value. */
    public static final class WriteFrame extends CdBFrame {
        WriteFrame(int classId, int moduleId, ClassifiedFrame parent) {
            super(CdBFrameType.WRITE, classId, moduleId, parent);
        }

        /** Sub-register slot (frame byte[5]). */
        public int slot()  { return raw()[5] & 0xFF; }

        /** Register number (frame byte[6]). */
        public int reg()   { return raw()[6] & 0xFF; }

        /** 16-bit value to write (frame bytes[7..8], big-endian). */
        public int value() { return ((raw()[7] & 0xFF) << 8) | (raw()[8] & 0xFF); }
    }
}
