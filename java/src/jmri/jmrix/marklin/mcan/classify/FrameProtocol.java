package jmri.jmrix.marklin.mcan.classify;

/**
 * Top-level protocol family of a CAN frame as determined by {@link ProtocolClassifier}.
 */
public enum FrameProtocol {

    /** Native Märklin CS2/MS2 protocol frame. CS2 header fingerprint present, DLC within expected range. */
    CS2_NATIVE,

    /** CdB (Can-Digital-Bahn) device configuration frame. Hash pattern 0x_A80 present. */
    CDB_CONFIG,

    /**
     * Frame not recognised by either classifier.
     * Includes CS1/ECOS (until implemented), unexplored modules and future protocol extensions.
     * Raw bytes are preserved. No interpretation is attempted.
     */
    UNKNOWN
}
