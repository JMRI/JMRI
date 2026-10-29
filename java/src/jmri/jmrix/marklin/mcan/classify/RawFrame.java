package jmri.jmrix.marklin.mcan.classify;

/**
 * Immutable value object wrapping a 13-byte CAN frame.
 * Carries no interpretation. Classification is applied by {@link ProtocolClassifier}.
 */
public final class RawFrame {

    private final int[] bytes;

    public RawFrame(int[] raw13) {
        if (raw13 == null || raw13.length != 13) {
            throw new IllegalArgumentException("CAN frame must be exactly 13 bytes");
        }
        this.bytes = raw13.clone();
    }

    /** Zero-indexed byte access, value in range 0x00–0xFF. */
    public int byteAt(int index) {
        return bytes[index] & 0xFF;
    }

    /** Data length code: byte[4] masked to 4 bits (0–8). */
    public int dlc() {
        return bytes[4] & 0x0F;
    }

    /** Defensive copy of the raw 13-byte array. */
    public int[] toArray() {
        return bytes.clone();
    }
}
