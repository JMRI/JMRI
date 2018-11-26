package jmri.jmrit.timetable;

import org.junit.*;

/**
 * Tests for the Train Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TrainTest {

    @Test
    public void testCreate() {
        try {
            new Train(0);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "TrainAddFail");  // NOI18N
        }
    }

    @Test
    public void testSettersAndGetters() {
        Layout layout = new Layout();
        int layoutId = layout.getLayoutId();
        TrainType type = new TrainType(layoutId);
        int typeId = type.getTypeId();
        Schedule schedule = new Schedule(layoutId);
        int scheduleId = schedule.getScheduleId();

        Train train = new Train(scheduleId);
        Assert.assertNotNull(train);
        Assert.assertTrue(train.getScheduleId() > 0);
        train.setTypeId(typeId);
        Assert.assertTrue(train.getTypeId() == typeId);
        train.setTrainName("TR1");  // NOI18N
        Assert.assertEquals("TR1", train.getTrainName());  // NOI18N
        train.setTrainDesc("New Train");  // NOI18N
        Assert.assertEquals("New Train", train.getTrainDesc());  // NOI18N
        try {
            train.setDefaultSpeed(-5);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "DefaultSpeedLt0");  // NOI18N
        }
        try {
            train.setDefaultSpeed(1);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "TimeOutOfRange");  // NOI18N
        }
        train.setDefaultSpeed(45);
        Assert.assertEquals(45, train.getDefaultSpeed());
        try {
            train.setStartTime(1450);
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("StartTimeRange"));  // NOI18N
        }
        train.setStartTime(720);  // Noon
        Assert.assertEquals(720, train.getStartTime());
        Assert.assertEquals(0, train.getThrottle());
        try {
            train.setThrottle(2);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "ThrottleRange");  // NOI18N
        }
        train.setRouteDuration(120); // two hours
        Assert.assertEquals(120, train.getRouteDuration());
        train.setTrainNotes("none");  // NOI18N
        Assert.assertEquals("none", train.getTrainNotes());  // NOI18N
        Assert.assertEquals("TR1", train.toString());  // NOI18N
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