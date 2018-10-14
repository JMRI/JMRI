package jmri.jmrit.operations.trains.timetable;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainScheduleManagerTest {

    @Test
    public void testCTor() {
        TrainScheduleManager t = new TrainScheduleManager();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testGetComboBox(){
        TrainScheduleManager t = new TrainScheduleManager();
        Assert.assertNotNull("ComboBox Available",t.getComboBox());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainScheduleManagerTest.class);

}
