package jmri.jmrit.operations.locations.schedules;

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
    protected static final String SCHEDULE = "schedule"; // NOI18N

    // SccheduleManager.java
    protected static final String SCHEDULES = "schedules"; // NOI18N

    // ScheduleItem.java
    protected static final String ITEM = "item"; // NOI18N
    protected static final String SEQUENCE_ID = "sequenceId"; // NOI18N
    protected static final String RANDOM = "random"; // NOI18N
    protected static final String TRAIN_SCHEDULE_ID = "trainScheduleId"; // NOI18N
    protected static final String PICKUP_TRAIN_SCHEDULE_ID = "pickupTrainScheduleId"; // NOI18N
    protected static final String COUNT = "count"; // NOI18N
    protected static final String WAIT = "wait"; // NOI18N
    protected static final String TYPE = "type"; // NOI18N
    protected static final String ROAD = "road"; // NOI18N
    protected static final String LOAD = "load"; // NOI18N
    protected static final String SHIP = "ship"; // NOI18N
    protected static final String DESTINATION_ID = "destinationId"; // NOI18N
    protected static final String DEST_TRACK_ID = "destTrackId"; // NOI18N
    protected static final String HITS = "hits"; // NOI18N

}
