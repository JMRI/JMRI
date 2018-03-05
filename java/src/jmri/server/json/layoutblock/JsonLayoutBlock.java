package jmri.server.json.layoutblock;

/**
 * Tokens used in the JMRI JSON Layout Block service.
 *
 * @author Randall Wood (C) 2016
 */
public class JsonLayoutBlock {

    /**
     * {@value #LAYOUTBLOCK}
     */
    public static final String LAYOUTBLOCK = "layoutBlock"; // NOI18N
    /**
     * {@value #LAYOUTBLOCKS}
     */
    public static final String LAYOUTBLOCKS = "layoutBlocks"; // NOI18N
    /**
     * {@value #BLOCK_COLOR}
     */
    public static final String BLOCK_COLOR = "blockColor"; // NOI18N
    /**
     * {@value #USE_EXTRA_COLOR}
     */
    public static final String USE_EXTRA_COLOR = "useExtraColor"; // NOI18N
    /**
     * {@value #TRACK_COLOR}
     */
    public static final String TRACK_COLOR = "trackColor"; // NOI18N
    /**
     * {@value #OCCUPIED_COLOR}
     */
    public static final String OCCUPIED_COLOR = "occupiedColor"; // NOI18N
    /**
     * {@value #EXTRA_COLOR}
     */
    public static final String EXTRA_COLOR = "extraColor"; // NOI18N
    /**
     * {@value #OCCUPANCY_SENSOR}
     */
    public static final String OCCUPANCY_SENSOR = "occupancySensor"; // NOI18N
    /**
     * {@value #OCCUPIED_SENSE}
     */
    public static final String OCCUPIED_SENSE = "occupiedSense"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonLayoutBlock() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}
