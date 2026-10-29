package jmri.jmrix.marklin.mcan.classify;

/**
 * Immutable value object wrapping the 8 data bytes (frame bytes 5–12) of a
 * CS2 native CAN frame.
 * <p>
 * Provides typed read accessors. The encode path is handled by the
 * {@code cs2} family encoder classes, not here.
 */
public final class Cs2Data {

    private final int[] data; // always length 8

    private Cs2Data(int[] data) {
        this.data = data.clone();
    }

    /**
     * Extracts the 8 data bytes from a raw 13-byte frame (bytes 5–12).
     */
    public static Cs2Data from(RawFrame frame) {
        int[] d = new int[8];
        int[] raw = frame.toArray();
        for (int i = 0; i < 8; i++) {
            d[i] = raw[5 + i] & 0xFF;
        }
        return new Cs2Data(d);
    }

    /**
     * Returns the data byte at {@code index} (0–7), value 0x00–0xFF.
     */
    public int byteAt(int index) {
        return data[index] & 0xFF;
    }

    /**
     * Returns a big-endian unsigned 16-bit value from data[index..index+1].
     */
    public int int16At(int index) {
        return ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
    }

    /**
     * Returns a big-endian unsigned 32-bit value from data[index..index+3]
     * as a long to avoid sign issues.
     */
    public long int32At(int index) {
        return ((long)(data[index]     & 0xFF) << 24)
             | ((long)(data[index + 1] & 0xFF) << 16)
             | ((long)(data[index + 2] & 0xFF) <<  8)
             | (data[index + 3] & 0xFF);
    }

    /** Returns a defensive copy of all 8 data bytes. */
    public int[] toArray() {
        return data.clone();
    }
}
