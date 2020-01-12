package jmri.server.json.block;

import jmri.server.json.JSON;

/**
 * Constants used by the internal JMRI JSON Block service.
 *
 * @author Randall Wood (C) 2016
 */
public class JsonBlock {

    /**
     * {@value #BLOCK}
     */
    public static final String BLOCK = "block"; // NOI18N
    /**
     * {@value #BLOCKS}
     */
    public static final String BLOCKS = "blocks"; // NOI18N
    /**
     * {@value #CURVATURE}
     */
    public static final String CURVATURE = "curvature"; // NOI18N
    /**
     * {@value #DENIED}
     */
    public static final String DENIED = "denied"; // NOI18N
    /**
     * {@value #PERMISSIVE}
     */
    public static final String PERMISSIVE = "permissive"; // NOI18N
    /**
     * {@value #SPEED_LIMIT}
     */
    public static final String SPEED_LIMIT = JSON.SPEED + "Limit"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonBlock() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}
