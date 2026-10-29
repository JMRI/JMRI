package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2Data;
import jmri.jmrix.marklin.mcan.classify.Cs2Header;

/**
 * Encoder, parser and typed result classes for CS2 automation commands (code 0x30).
 * <p>
 * Automation functions ("memory" rows) are addressed by a 16-bit device identifier
 * and a 16-bit function code (high byte = row A–z, low byte = column 1–8).
 * <p>
 * Request frame (DLC=6):
 * <pre>
 *   data[0..1] = device identifier (big-endian)
 *   data[2..3] = automation function (big-endian; high byte = row, low byte = column)
 *   data[4]    = position: 0=deactivate, 1=activate
 *   data[5]    = parameter (reserved, send 0)
 * </pre>
 * Response frame (DLC=6) carries the current activation status in data[4]:
 * <pre>
 *   0  = deactivated
 *   1  = activated
 *   2  = running / entering
 *   3  = block free
 *   4  = block occupied
 *   5  = suspended / waiting
 *   6  = reserved / pre-allocated
 *   254 = waiting
 *   255 = error / unknown
 * </pre>
 */
public final class Cs2AutomationCommands {

    public static final int COMMAND_AUTOMATION_TRIGGER = 0x30;

    static final int DEFAULT_PRIORITY = 0;
    static final int BROADCAST_HASH   = 0x0300;

    /** Status: automation function deactivated. */
    public static final int STATUS_DEACTIVATED  = 0;
    /** Status: automation function activated. */
    public static final int STATUS_ACTIVATED    = 1;
    /** Status: automation function running / entering. */
    public static final int STATUS_RUNNING      = 2;
    /** Status: block free. */
    public static final int STATUS_BLOCK_FREE   = 3;
    /** Status: block occupied. */
    public static final int STATUS_BLOCK_OCCUPIED = 4;
    /** Status: suspended / waiting. */
    public static final int STATUS_SUSPENDED    = 5;
    /** Status: reserved / pre-allocated. */
    public static final int STATUS_RESERVED     = 6;
    /** Status: automation function waiting. */
    public static final int STATUS_WAITING      = 254;
    /** Status: error / unknown status. */
    public static final int STATUS_ERROR        = 255;

    private Cs2AutomationCommands() {}

    // --- encoders ---

    /**
     * Builds an automation trigger command (0x30, DLC=6).
     *
     * @param deviceId   16-bit device identifier assigned by the master station
     * @param function   16-bit automation function (high byte = row A–z, low byte = column 1–8)
     * @param activate   true to activate the function, false to deactivate
     * @return 13-byte CAN frame
     */
    public static int[] buildAutomationTrigger(int deviceId, int function, boolean activate) {
        int[] data = new int[]{
            (deviceId  >> 8) & 0xFF,
            deviceId         & 0xFF,
            (function  >> 8) & 0xFF,
            function         & 0xFF,
            activate ? 1 : 0,
            0x00
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_AUTOMATION_TRIGGER,
                false, BROADCAST_HASH, 6, data);
    }

    /**
     * Builds an automation status reply (0x30, DLC=5): device ID + function + status.
     *
     * @param deviceId 16-bit device identifier
     * @param function 16-bit automation function code
     * @param status   current activation status (see STATUS_* constants)
     * @return 13-byte CAN frame
     */
    public static int[] buildAutomationStatusReply(int deviceId, int function, int status) {
        int[] data = new int[]{
            (deviceId  >> 8) & 0xFF,
            deviceId         & 0xFF,
            (function  >> 8) & 0xFF,
            function         & 0xFF,
            status & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_AUTOMATION_TRIGGER,
                true, BROADCAST_HASH, 5, data);
    }

    // --- parser ---

    static Cs2ClassifiedFrame parse(Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
        switch (header.resolvedCommand()) {
            case AUTOMATION_TRIGGER_REPLY:
                return new AutomationStatusFrame(header, data, parent);
            default:
                return new Cs2ClassifiedFrame(header, data, parent);
        }
    }

    // --- typed result classes ---

    /**
     * An automation status reply carrying the current activation state of a function.
     */
    public static final class AutomationStatusFrame extends Cs2ClassifiedFrame {
        AutomationStatusFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** 16-bit device identifier. */
        public int deviceId() { return data().int16At(0); }

        /** 16-bit automation function code (high byte = row, low byte = column). */
        public int function() { return data().int16At(2); }

        /** Current activation status (see STATUS_* constants). */
        public int status() { return data().byteAt(4); }
    }
}
