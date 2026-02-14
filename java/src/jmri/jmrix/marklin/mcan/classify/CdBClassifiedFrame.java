package jmri.jmrix.marklin.mcan.classify;

/**
 * Result of the second-pass CdB classification performed by
 * {@link jmri.jmrix.marklin.mcan.protocol.CdBProtocolDecoder}.
 * <p>
 * Only produced from a {@link ClassifiedFrame} whose {@link FrameProtocol} is
 * {@link FrameProtocol#CDB_CONFIG}.
 */
public final class CdBClassifiedFrame {

    /** Sentinel value for canClass and moduleId when the frame type is UNKNOWN. */
    public static final int UNKNOWN_VALUE = -1;

    private final CdBFrameType frameType;
    private final int canClass;  // -1 if UNKNOWN
    private final int moduleId;  // -1 if UNKNOWN or broadcast ping
    private final ClassifiedFrame parent;

    public CdBClassifiedFrame(CdBFrameType frameType, int canClass, int moduleId,
                               ClassifiedFrame parent) {
        this.frameType = frameType;
        this.canClass  = canClass;
        this.moduleId  = moduleId;
        this.parent    = parent;
    }

    public CdBFrameType frameType() { return frameType; }

    /** CdB device class ID or {@value #UNKNOWN_VALUE} when {@link #frameType()} is UNKNOWN. */
    public int canClass()  { return canClass; }

    /**
     * Module ID (1–127), 0 for broadcast pings or {@value #UNKNOWN_VALUE} when
     * {@link #frameType()} is UNKNOWN.
     */
    public int moduleId()  { return moduleId; }

    /** The first-pass result this was derived from. */
    public ClassifiedFrame parent() { return parent; }

    /** True when frameType is not UNKNOWN. */
    public boolean isKnownType() { return frameType != CdBFrameType.UNKNOWN; }
}
