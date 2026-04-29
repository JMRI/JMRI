package jmri.jmrit.operations.trains.schedules;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2013
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

    // TrainSchedule.java
    protected static final String SCHEDULE = "schedule"; // NOI18N
    protected static final String TRAIN_IDS = "trainIds"; // NOI18N

    // TrainScheduleManager.java
    public static final String TRAIN_SCHEDULE_OPTIONS = "trainScheduleOptions"; // NOI18N
    public static final String ACTIVE_ID = "activeId"; // NOI18N
    protected static final String SCHEDULES = "schedules"; // NOI18N

}
