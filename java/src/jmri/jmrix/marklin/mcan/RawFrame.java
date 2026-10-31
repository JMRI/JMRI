package jmri.jmrix.marklin.mcan;

/**
 * Immutable value object wrapping a 13-byte CAN frame.
 * Carries no interpretation. Classification is applied by {@link jmri.jmrix.marklin.mcan.classify.ProtocolClassifier}.
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

    /** Space-separated upper-case hex bytes, e.g. {@code "00 31 0A 80 ..."}. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(13 * 3);
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(String.format("%02X", bytes[i] & 0xFF));
        }
        return sb.toString();
    }
}
