package jmri.jmrit.operations.trains.timetable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainScheduleTest {

    @Test
    public void testCTor() {
        TrainSchedule t = new TrainSchedule("TS1","1");
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(TrainScheduleTest.class);

}
