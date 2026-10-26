package jmri.jmrix.marklin.mcan.classify;

/**
 * Parsed CS2 29-bit CAN header.
 * <p>
 * Layout in bytes 0–4 of a 13-byte CAN frame:
 * <pre>
 *   bits 28–27: bus priority    (upper 2 of the 4 priority bits)
 *   bits 26–25: message priority (lower 2 of the 4 priority bits; tiers 1–4)
 *   bits 24–17: command          (8 bits)
 *   bit  16:    response flag
 *   bits 15–0:  hash             (16 bits)
 *   byte 4:     DLC              (low 4 bits, 0–8)
 * </pre>
 * The 4-bit {@link #priority()} field combines both sub-fields.
 * Use {@link #busPriority()} and {@link #messagePriority()} to access them
 * individually.
 * <p>
 * A valid CS2 frame carries the fingerprint {@code (hash >> 7) & 0b111 == 0b110}.
 */
public final class Cs2Header {

    private final int priority;
    private final int command;
    private final boolean response;
    private final int hash;
    private final int dlc;

    public Cs2Header(int priority, int command, boolean response, int hash, int dlc) {
        this.priority = priority;
        this.command  = command;
        this.response = response;
        this.hash     = hash;
        this.dlc      = dlc;
    }

    public int     priority()    { return priority; }
    public int     commandCode() { return command; }
    public Cs2Command resolvedCommand() { return Cs2Command.from(command, response); }
    public boolean response() { return response; }
    public int     hash()     { return hash; }
    public int     dlc()      { return dlc; }

    /**
     * Upper 2 bits of the 4-bit priority field (bits 28–27 of the CAN ID).
     * Controls bus-level arbitration on the physical CAN segment.
     */
    public int busPriority() { return (priority >> 2) & 0b11; }

    /**
     * Lower 2 bits of the 4-bit priority field (bits 26–25 of the CAN ID).
     * Application-level message priority tiers:
     * <ol start="0">
     *   <li>0  -  Stop / Go / short-circuit</li>
     *   <li>1  -  Feedback messages</li>
     *   <li>2  -  Locomotive halt</li>
     *   <li>3  -  Locomotive / accessory commands</li>
     * </ol>
     */
    public int messagePriority() { return priority & 0b11; }

    /**
     * Attempt to parse a CS2 header from a raw 13-byte frame.
     * Returns {@code null}  -  without throwing  -  if the frame is too short,
     * the CS2 fingerprint is absent or the DLC is out of range.
     * Callers treat a null return as "not a CS2 frame".
     */
    public static Cs2Header tryParse(int[] packet) {
        if (packet == null || packet.length < 5) return null;

        long canId = ((long)(packet[0] & 0xFF) << 24)
                   | ((long)(packet[1] & 0xFF) << 16)
                   | ((long)(packet[2] & 0xFF) <<  8)
                   | (packet[3] & 0xFF);

        int dlc      = packet[4] & 0x0F;
        int priority = (int)((canId >> 25) & 0x0F);
        int command  = (int)((canId >> 17) & 0xFF);
        boolean response = ((canId >> 16) & 0x01) != 0;
        int hash     = (int)(canId & 0xFFFF);

        if (!hasCs2Fingerprint(hash)) return null;
        if (!isValidDlc(dlc))        return null;

        return new Cs2Header(priority, command, response, hash, dlc);
    }

    /**
     * True when bits 7–9 of the hash equal {@code 0b110},
     * which is the mandatory Märklin CS2 fingerprint.
     */
    public static boolean hasCs2Fingerprint(int hash) {
        return ((hash >> 7) & 0b111) == 0b110;
    }

    /** True when DLC is in the valid range 0–8. */
    public static boolean isValidDlc(int dlc) {
        return dlc >= 0 && dlc <= 8;
    }
}
