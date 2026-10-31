package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.McanUid;
import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2Data;
import jmri.jmrix.marklin.mcan.classify.Cs2Header;

/**
 * Encoder, parser and typed result classes for CS2 feedback commands (codes 0x10–0x12).
 * <p>
 * Covers S88 bus polling and S88 contact change events.
 * <pre>
 *   S88 Poll (0x10, DLC=5):             data[0..3]=MY_UID, data[4]=module number
 *   S88 Poll Reply (0x10, resp, DLC=5): same layout
 *   S88 Event (0x11, DLC=8):            data[0..1]=contactId (16-bit),
 *                                        data[2]=oldState, data[3]=newState,
 *                                        data[4..5]=time (16-bit)
 * </pre>
 */
public final class Cs2FeedbackCommands {

    public static final int COMMAND_S88_POLL           = 0x10;
    public static final int COMMAND_S88_EVENT          = 0x11;
    public static final int COMMAND_FEEDBACK_BROADCAST = 0x12;

    static final int DEFAULT_PRIORITY = 0;
    static final int BROADCAST_HASH   = 0x0300;

    /** UID used as the sender identification in S88 poll requests. */
    public static final McanUid MY_UID = McanUid.of(0x12345678L);

    private Cs2FeedbackCommands() {}

    // --- encoders ---

    /**
     * Builds an S88 poll command (0x10, DLC=5) querying one S88 module.
     * Uses {@link #MY_UID} as the requester UID in bytes 0–3.
     *
     * @param moduleNumber S88 module index to poll
     * @return 13-byte CAN frame
     */
    public static int[] buildS88Poll(int moduleNumber) {
        return buildS88Poll(moduleNumber, MY_UID);
    }

    /**
     * Builds an S88 poll command (0x10, DLC=5) querying one S88 module,
     * using a caller-supplied sender UID instead of the default {@link #MY_UID}.
     *
     * @param moduleNumber S88 module index to poll
     * @param senderUid    UID identifying the requester (bytes 0–3)
     * @return 13-byte CAN frame
     */
    public static int[] buildS88Poll(int moduleNumber, McanUid senderUid) {
        long uid = senderUid.toLong();
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF),
            (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF),
            (int)( uid        & 0xFF),
            moduleNumber & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_S88_POLL,
                false, BROADCAST_HASH, 5, data);
    }

    /**
     * Builds an S88 poll reply (0x10, DLC=7): module number + 16-bit contact state.
     *
     * @param moduleNumber S88 module index
     * @param state        16-bit contact state bitfield (bit 15 = contact 1)
     * @return 13-byte CAN frame
     */
    public static int[] buildS88PollReply(int moduleNumber, int state) {
        int[] data = new int[]{
            0x00, 0x00, 0x00, 0x00,
            moduleNumber & 0xFF,
            (state >> 8) & 0xFF,
            state & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_S88_POLL,
                true, BROADCAST_HASH, 7, data);
    }

    /**
     * Builds an S88 contact event (0x11, DLC=8): contact address + state transition + timestamp.
     * This is the same as {@link #buildS88Event}  -  kept as a named alias for reply-side code.
     *
     * @param contactId 16-bit contact address
     * @param oldState  previous state (0 or 1)
     * @param newState  new state (0 or 1)
     * @param time      16-bit timestamp (10ms units)
     * @return 13-byte CAN frame
     */
    public static int[] buildS88EventReply(int contactId, int oldState, int newState, int time) {
        int[] data = new int[]{
            (contactId >> 8) & 0xFF,
            contactId & 0xFF,
            oldState & 0xFF,
            newState & 0xFF,
            (time >> 8) & 0xFF,
            time & 0xFF,
            0x00,
            0x00
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_S88_EVENT,
                true, BROADCAST_HASH, 8, data);
    }

    /**
     * Builds a feedback broadcast frame (0x12, DLC=3): module index + 16-bit contact state.
     *
     * @param moduleIndex 1-based S88 module index
     * @param state       16-bit contact state bitfield (bit 15 = contact 1)
     * @return 13-byte CAN frame
     */
    public static int[] buildFeedbackBroadcast(int moduleIndex, int state) {
        int[] data = new int[]{
            moduleIndex & 0xFF,
            (state >> 8) & 0xFF,
            state & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_FEEDBACK_BROADCAST,
                false, BROADCAST_HASH, 3, data);
    }

    /**
     * Builds an S88 contact event (0x11, DLC=8).
     *
     * @param contactId 16-bit contact address
     * @param oldState  previous state (0 or 1)
     * @param newState  new state (0 or 1)
     * @param time      16-bit timestamp (10ms units)
     * @return 13-byte CAN frame
     */
    public static int[] buildS88Event(int contactId, int oldState, int newState, int time) {
        int[] data = new int[]{
            (contactId >> 8) & 0xFF,
            contactId & 0xFF,
            oldState & 0xFF,
            newState & 0xFF,
            (time >> 8) & 0xFF,
            time & 0xFF,
            0x00,
            0x00
        };
        return Cs2FrameBuilder.buildFrame(DEFAULT_PRIORITY, COMMAND_S88_EVENT,
                false, BROADCAST_HASH, 8, data);
    }

    // --- parser ---

    static Cs2ClassifiedFrame parse(Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
        switch (header.resolvedCommand()) {
            case S88_POLL:
                return new S88PollFrame(header, data, parent);
            case S88_POLL_REPLY:
                return new S88PollReplyFrame(header, data, parent);
            case S88_EVENT:
            case S88_EVENT_REPLY:
                return new S88EventFrame(header, data, parent);
            case FEEDBACK_BROADCAST:
                return new FeedbackBroadcastFrame(header, data, parent);
            default:
                return new Cs2ClassifiedFrame(header, data, parent);
        }
    }

    // --- typed result classes ---

    /** An S88 poll request (0x10, DLC=5): UID of requester + module number. */
    public static final class S88PollFrame extends Cs2ClassifiedFrame {
        S88PollFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 32-bit UID of the requester (data bytes 0–3). */
        public McanUid requesterUid() { return data().uidAt(0); }

        /** The S88 module index to poll (data byte 4). */
        public int module() { return data().byteAt(4); }
    }

    /** An S88 poll reply carrying module number and 16-bit contact state bitfield. */
    public static final class S88PollReplyFrame extends Cs2ClassifiedFrame {
        S88PollReplyFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The S88 module index (from data byte 4). */
        public int module() { return data().byteAt(4); }

        /**
         * 16-bit contact state bitfield from data bytes 5–6 (DLC=7 reply).
         * Each bit represents one S88 contact on the module (bit 15 = contact 1).
         * Returns 0 for request frames (DLC=5).
         */
        public int state() { return data().int16At(5); }
    }

    /** An S88 contact event carrying contact address, state transition and timestamp. */
    public static final class S88EventFrame extends Cs2ClassifiedFrame {
        S88EventFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 16-bit contact address. */
        public int contactId() { return data().int16At(0); }

        /** The previous contact state (0 or 1). */
        public int oldState() { return data().byteAt(2); }

        /** The new contact state (0 or 1). */
        public int newState() { return data().byteAt(3); }

        /** The 16-bit timestamp (10ms units). */
        public int time() { return data().int16At(4); }
    }

    /**
     * A feedback broadcast frame (0x12).
     * Carries a block of S88 state for a range of contacts.
     * Layout: data[0]=module index (1-based), data[1..2]=16-bit contact state bitfield.
     */
    public static final class FeedbackBroadcastFrame extends Cs2ClassifiedFrame {
        FeedbackBroadcastFrame(Cs2Header h, Cs2Data d, ClassifiedFrame p) { super(h, d, p); }

        /** The 1-based S88 module index. */
        public int moduleIndex() { return data().byteAt(0); }

        /** The 16-bit contact state bitfield (bit 15 = contact 1). */
        public int state() { return data().int16At(1); }
    }
}
