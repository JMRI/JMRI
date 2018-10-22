package jmri.jmrit.timetable;

import org.junit.*;

/**
 * Tests for the Train Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TrainTest {

    @Test
    public void testCreate() {
        new Train(1, 1);
    }

    @Test
    public void testSettersAndGetters() {
        Train t = new Train(1, 1);
        Assert.assertEquals(1, t.getTrainId());
        Assert.assertEquals(1, t.getScheduleId());
        t.setTypeId(2);
        Assert.assertEquals(2, t.getTypeId());
        t.setTrainName("TR1");  // NOI18N
        Assert.assertEquals("TR1", t.getTrainName());  // NOI18N
        t.setTrainDesc("New Train");  // NOI18N
        Assert.assertEquals("New Train", t.getTrainDesc());  // NOI18N
        t.setDefaultSpeed(45);
        Assert.assertEquals(45, t.getDefaultSpeed());
        t.setStartTime(720);  // Noon
        Assert.assertEquals(720, t.getStartTime());
        t.setThrottle(2);
        Assert.assertEquals(2, t.getThrottle());
        t.setRouteDuration(120); // two hours
        Assert.assertEquals(120, t.getRouteDuration());
        t.setTrainNotes("none");  // NOI18N
        Assert.assertEquals("none", t.getTrainNotes());  // NOI18N
        Assert.assertEquals("TR1", t.toString());  // NOI18N
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}