package jmri.jmrit.timetable;

import org.junit.*;

/**
 * Tests for the Schedule Class
 * @author Dave Sand Copyright (C) 2018
 */
public class ScheduleTest {

    @Test
    public void testCreate() {
        try {
            new Schedule(0);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "ScheduleAddFail");  // NOI18N
        }
    }

    @Test
    public void testSettersAndGetters() {
        Layout layout = new Layout();
        int layoutId = layout.getLayoutId();
        Schedule schedule = new Schedule(layoutId);

        Assert.assertTrue(schedule.getScheduleId() > 0);
        Assert.assertTrue(schedule.getLayoutId() > 0);
        schedule.setScheduleName("New Schedule");  // NOI18N
        Assert.assertEquals("New Schedule", schedule.getScheduleName());  // NOI18N
        schedule.setEffDate("today");  // NOI18N
        Assert.assertEquals("today", schedule.getEffDate());  // NOI18N
        try {
            schedule.setStartHour(24);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "StartHourRange");  // NOI18N
        }
        schedule.setStartHour(12);
        Assert.assertEquals(12, schedule.getStartHour());
        try {
            schedule.setDuration(25);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "DurationRange");  // NOI18N
        }
        schedule.setDuration(8);
        Assert.assertEquals(8, schedule.getDuration());
        Assert.assertEquals("New Schedule", schedule.toString());  // NOI18N
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