package jmri.jmrix.marklin;

/**
 * Configuration profiles for the WeichenChef Magnet (class 0x22) turnout decoder.
 * Firmware and ping replies do not establish which profile is supported.
 */
public enum WeichenChefMagnetGeneration {
    /** Older tool: four timing entries shared by each port's primary/secondary outputs. */
    GEN_001,
    /** Newer tool: eight timing entries, primary n and secondary n+4 per port. */
    GEN_002_PLUS,
    /** No positively identified profile and no explicit user override. */
    UNKNOWN
}
