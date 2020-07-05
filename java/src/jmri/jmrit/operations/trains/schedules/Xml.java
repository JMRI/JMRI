package jmri.jmrit.operations.trains.schedules;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 *
 */
@API(status = MAINTAINED)
public class Xml {

    private Xml(){
       // class of constants
    }

    // Common to operation xml files
    static final String ID = "id"; // NOI18N
    static final String NAME = "name"; // NOI18N
    static final String COMMENT = "comment"; // NOI18N

    // TrainSchedule.java
    static final String SCHEDULE = "schedule"; // NOI18N
    static final String TRAIN_IDS = "trainIds"; // NOI18N

    // TrainScheduleManager.java
    public static final String TRAIN_SCHEDULE_OPTIONS = "trainScheduleOptions"; // NOI18N
    public static final String ACTIVE_ID = "activeId"; // NOI18N
    static final String SCHEDULES = "schedules"; // NOI18N

}
