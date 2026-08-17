package jmri.jmrit.operations.trains.manualtrainbuilder;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2012
 * 
 *
 */
public class Xml {

    private Xml(){
       // class of constants
    }

    // Common to operation xml files
    protected static final String ID = "id"; // NOI18N
    protected static final String NAME = "name"; // NOI18N
    protected static final String COMMENT = "comment"; // NOI18N
    protected static final String TRUE = "true"; // NOI18N
    protected static final String FALSE = "false"; // NOI18N
    
    // TrainManualBuild.java
    protected static final String TRAIN_ID = "trainId"; // NOI18N
    
    protected static final String MANUAL_BUILD = "manualBuild"; // NOI18N
    protected static final String MANUAL_BUILDS = "manualBuilds"; // NOI18N

    // TrainManualBuildItem.java
    protected static final String MANUAL_BUILD_ITEM = "mbItem"; // NOI18N
    protected static final String SEQUENCE_ID = "sequenceId"; // NOI18N
    protected static final String TRAIN_SCHEDULE_ID = "trainScheduleId"; // NOI18N
    protected static final String TYPE = "type"; // NOI18N
    protected static final String ROAD = "road"; // NOI18N
    protected static final String LOAD = "load"; // NOI18N
    protected static final String ROUTE_LOCATION_ID = "routeLocationId"; // NOI18N
    protected static final String LOC_TRACK_ID = "locTrackId"; // NOI18N
    protected static final String DESTINATION_ID = "destinationId"; // NOI18N
    protected static final String DEST_TRACK_ID = "destTrackId"; // NOI18N
    protected static final String COUNT = "count"; // NOI18N
    protected static final String WARN = "warn"; // NOI18N
    protected static final String FAIL = "fail"; // NOI18N
    protected static final String REMOVE = "remove"; // NOI18N
    
}
