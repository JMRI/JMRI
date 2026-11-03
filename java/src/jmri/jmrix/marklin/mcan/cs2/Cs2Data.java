package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.RawFrame;
import jmri.jmrix.marklin.mcan.McanUid;

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
     *
     * @param frame the raw 13-byte CAN frame to extract data bytes from
     * @return a new {@code Cs2Data} wrapping bytes 5–12 of the frame
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
     *
     * @param index the data byte position (0–7)
     * @return the unsigned byte value (0x00–0xFF) at that position
     */
    public int byteAt(int index) {
        return data[index] & 0xFF;
    }

    /**
     * Returns a big-endian unsigned 16-bit value from data[index..index+1].
     *
     * @param index the first data byte position of the 16-bit value
     * @return the unsigned 16-bit value (0–65535) decoded big-endian
     */
    public int int16At(int index) {
        return ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
    }

    /**
     * Returns a big-endian unsigned 32-bit value from data[index..index+3]
     * as a long to avoid sign issues.
     *
     * @param index the first data byte position of the 32-bit value
     * @return the unsigned 32-bit value decoded big-endian, as a long
     */
    public long int32At(int index) {
        return ((long)(data[index]     & 0xFF) << 24)
             | ((long)(data[index + 1] & 0xFF) << 16)
             | ((long)(data[index + 2] & 0xFF) <<  8)
             | (data[index + 3] & 0xFF);
    }

    /**
     * Returns the big-endian device UID stored in data[index..index+3].
     *
     * @param index first data byte
     * @return typed UID
     */
    public McanUid uidAt(int index) {
        return McanUid.of(int32At(index));
    }

    /**
     * Returns a defensive copy of all 8 data bytes.
     *
     * @return a copy of the backing byte array
     */
    public int[] toArray() {
        return data.clone();
    }
}
