package jmri.jmrit.operations.locations.schedules;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2012
 * 
 *
 */
public class Xml {

    // Common to operation xml files
    static final String ID = "id"; // NOI18N
    static final String NAME = "name"; // NOI18N
    static final String COMMENT = "comment"; // NOI18N
    static final String SCHEDULE = "schedule"; // NOI18N

    // SccheduleManager.java
    static final String SCHEDULES = "schedules"; // NOI18N

    // ScheduleItem.java
    static final String ITEM = "item"; // NOI18N
    static final String SEQUENCE_ID = "sequenceId"; // NOI18N
    static final String RANDOM = "random"; // NOI18N
    static final String TRAIN_SCHEDULE_ID = "trainScheduleId"; // NOI18N
    static final String PICKUP_TRAIN_SCHEDULE_ID = "pickupTrainScheduleId"; // NOI18N
    static final String COUNT = "count"; // NOI18N
    static final String WAIT = "wait"; // NOI18N
    static final String TYPE = "type"; // NOI18N
    static final String ROAD = "road"; // NOI18N
    static final String LOAD = "load"; // NOI18N
    static final String SHIP = "ship"; // NOI18N
    static final String DESTINATION_ID = "destinationId"; // NOI18N
    static final String DEST_TRACK_ID = "destTrackId"; // NOI18N
    static final String HITS = "hits"; // NOI18N

}
