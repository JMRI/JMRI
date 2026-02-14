package jmri.jmrix.marklin.mcan.classify;

/**
 * Result of the first-pass protocol classification performed by {@link ProtocolClassifier}.
 * Always non-null; use {@link #protocol()} to dispatch to the appropriate second-pass decoder.
 */
public final class ClassifiedFrame {

    private final FrameProtocol protocol;
    private final RawFrame raw;
    private final Cs2Header cs2Header; // non-null iff protocol == CS2_NATIVE

    ClassifiedFrame(FrameProtocol protocol, RawFrame raw, Cs2Header cs2Header) {
        this.protocol  = protocol;
        this.raw       = raw;
        this.cs2Header = cs2Header;
    }

    public FrameProtocol protocol() { return protocol; }
    public RawFrame raw()           { return raw; }

    /**
     * The parsed CS2 header. Only valid when {@link #protocol()} is {@link FrameProtocol#CS2_NATIVE}.
     *
     * @throws IllegalStateException if called on a non-CS2_NATIVE frame
     */
    public Cs2Header cs2Header() {
        if (protocol != FrameProtocol.CS2_NATIVE) {
            throw new IllegalStateException("cs2Header() is only valid for CS2_NATIVE frames, got " + protocol);
        }
        return cs2Header;
    }

    /**
     * TODO: CS1 / MS1 / Ecos protocol
     */

    /** Convenience: true when {@link #protocol()} is not {@link FrameProtocol#UNKNOWN}. */
    public boolean isKnownProtocol() {
        return protocol != FrameProtocol.UNKNOWN;
    }
}
