package jmri.jmrix.marklin.mcan;

/**
 * Typed wrapper for a 32-bit MCAN device UID.
 */
public final class McanUid {

    /** Broadcast UID (0x00000000)  -  addresses all devices simultaneously. */
    public static final McanUid BROADCAST    = new McanUid(0x00000000L);

    /** Sentinel for an uninitialised UID (0xFFFFFFFF). */
    public static final McanUid UNINITIALIZED = new McanUid(0xFFFFFFFFL);

    private final long value;

    private McanUid(long value) {
        this.value = value & 0xFFFFFFFFL;
    }

    public static McanUid of(long value) {
        return new McanUid(value);
    }

    public long toLong() {
        return value;
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
