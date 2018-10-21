package jmri.jmrit.timetable;

import org.junit.*;

/**
 * Tests for the Schedule Class
 * @author Dave Sand Copyright (C) 2018
 */
public class ScheduleTest {

    @Test
    public void testCreate() {
        new Schedule(1, 1);
    }

    @Test
    public void testSettersAndGetters() {
        Schedule s = new Schedule(1, 1);
        Assert.assertEquals(1, s.getScheduleId());
        Assert.assertEquals(1, s.getLayoutId());
        s.setScheduleName("New Schedule");  // NOI18N
        Assert.assertEquals("New Schedule", s.getScheduleName());  // NOI18N
        s.setEffDate("today");  // NOI18N
        Assert.assertEquals("today", s.getEffDate());  // NOI18N
        s.setStartHour(12);
        Assert.assertEquals(12, s.getStartHour());
        s.setDuration(8);
        Assert.assertEquals(8, s.getDuration());
        Assert.assertEquals("New Schedule", s.toString());  // NOI18N
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