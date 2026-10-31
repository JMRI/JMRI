package jmri.jmrix.marklin.mcan;

/**
 * Typed wrapper for a 32-bit MCAN bus device UID (CS2, MS2, Gleisbox etc).
 * <p>
 * Not used for mfx decoder UIDs. These share the wire shape but identify
 * locomotive decoders rather than bus devices. They should be treated as another type.
 */
public final class McanUid implements Comparable<McanUid> {

    /** Broadcast UID (0x00000000) addresses all devices simultaneously. */
    public static final McanUid BROADCAST    = new McanUid(0x00000000L);

    /** Sentinel for an uninitialised UID (0xFFFFFFFF). */
    public static final McanUid UNINITIALIZED = new McanUid(0xFFFFFFFFL);

    private final long value;

    private McanUid(long value) {
        this.value = value & 0xFFFFFFFFL;
    }

    /**
     * @param value UID. Only the low 32 bits are used
     * @return typed UID
     */
    public static McanUid of(long value) {
        return new McanUid(value);
    }

    /** @return the UID as an unsigned 32-bit value */
    public long toLong() {
        return value;
    }

    /** @return true for {@link #BROADCAST} */
    public boolean isBroadcast() {
        return value == BROADCAST.value;
    }

    /** @return true for {@link #UNINITIALIZED} */
    public boolean isUninitialized() {
        return value == UNINITIALIZED.value;
    }

    /**
     * Computes the CS2 hash base from this UID:
     * {@code (uid >> 16) ^ (uid & 0xFFFF)}.
     * The caller must apply the CS2 fingerprint bits before use on the bus.
     *
     * @return 16 bit hash base
     */
    public int calculateHash() {
        return (int) ((value >> 16) & 0xFFFF) ^ (int) (value & 0xFFFF);
    }

    /** @return {@code 0x} followed by eight upper-case hex digits */
    public String toHexString() {
        return String.format("0x%08X", value);
    }

    @Override
    public int compareTo(McanUid other) {
        return Long.compare(value, other.value);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof McanUid && ((McanUid) o).value == value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public String toString() {
        return String.format("McanUid(0x%08X)", value);
    }
}
