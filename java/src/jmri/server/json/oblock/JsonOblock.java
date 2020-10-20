package jmri.server.json.oblock;

import jmri.server.json.JSON;

/**
 * Constants used by the internal JMRI JSON Oblock service. Copied from jmri/server/json/blocks/
 *
 * @author Randall Wood (C) 2016
 * @author Egbert Broerse Copyright 2020
 */
public class JsonOblock {

    /**
     * {@value #OBLOCK}
     */
    public static final String OBLOCK = "oblock"; // NOI18N
    /**
     * {@value #OBLOCKS}
     */
    public static final String OBLOCKS = "oblocks"; // NOI18N
    /**
     * {@value #WARRANT}
     */
    public static final String WARRANT = "warrant"; // NOI18N
    /**
     * {@value #TRAIN}
     */
    public static final String TRAIN = "train"; // NOI18N
//    /**
//     * {@value #CURVATURE}
//     */
//    public static final String CURVATURE = "curvature"; // NOI18N
//    /**
//     * {@value #DENIED}
//     */
//    public static final String DENIED = "denied"; // NOI18N
//    /**
//     * {@value #PERMISSIVE}
//     */
//    public static final String PERMISSIVE = "permissive"; // NOI18N
//    /**
//     * {@value #SPEED_LIMIT}
//     */
//    public static final String SPEED_LIMIT = JSON.SPEED + "Limit"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonOblock() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }

}
