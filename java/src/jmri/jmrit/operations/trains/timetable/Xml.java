package jmri.jmrit.operations.trains.timetable;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 22156 $
 *
 */
public class Xml {

    // Common to operation xml files
    static final String ID = "id"; // NOI18N
    static final String NAME = "name"; // NOI18N
    static final String COMMENT = "comment"; // NOI18N

    // TrainSchedule.java
    static final String SCHEDULE = "schedule"; // NOI18N
    static final String TRAIN_IDS = "trainIds"; // NOI18N

    // TrainScheduleManager.java
    static final String SCHEDULES = "schedules"; // NOI18N

}
