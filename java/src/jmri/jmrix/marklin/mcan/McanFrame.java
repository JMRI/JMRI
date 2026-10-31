package jmri.jmrix.marklin.mcan;

import java.util.Objects;

/**
 * Base type of every classified 13-byte M&auml;rklin CAN frame.
 * <p>
 * Classification always yields exactly one of these subtypes:
 * <ul>
 *   <li>{@link jmri.jmrix.marklin.mcan.cs2.Cs2Frame}: CS2 native protocol, possibly with an
 *       unrecognised command ({@code Cs2Command.UNKNOWN})</li>
 *   <li>{@link jmri.jmrix.marklin.mcan.cdb.CdBFrameParser.CdBFrame}: CdB configuration bus,
 *       possibly with an unrecognised frame type ({@code CdBFrameType.UNKNOWN})</li>
 *   <li>{@link UnknownFrame}: neither protocol family matched</li>
 * </ul>
 * The hierarchy is closed by convention. New protocol families get a new direct subtype
 * and a new {@link FrameProtocol} value. Instances are produced by
 *
 * External extenders who do not control the library code should catch and evaluate UnknownFrame instances.
 *
 * {@link jmri.jmrix.marklin.mcan.classify.ProtocolClassifier#classify(int[])}.
 */
public abstract class McanFrame {

    private final FrameProtocol protocol;
    private final RawFrame raw;

    protected McanFrame(FrameProtocol protocol, RawFrame raw) {
        this.protocol = Objects.requireNonNull(protocol);
        this.raw      = Objects.requireNonNull(raw);
    }

    /** @return protocol family matches the concrete subtype */
    public final FrameProtocol protocol() { return protocol; }

    /** @return the unmodified 13-byte frame */
    public final RawFrame raw() { return raw; }

    /** @return true unless this is an {@link UnknownFrame} */
    public final boolean isKnownProtocol() {
        return protocol != FrameProtocol.UNKNOWN;
    }
}
