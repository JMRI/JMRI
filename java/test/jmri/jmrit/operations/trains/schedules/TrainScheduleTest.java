package jmri.jmrit.operations.trains.schedules;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainScheduleTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainSchedule t = new TrainSchedule("TS1", "1");
        Assert.assertNotNull("exists", t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainScheduleTest.class);

}
