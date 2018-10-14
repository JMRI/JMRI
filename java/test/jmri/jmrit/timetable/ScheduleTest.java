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

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}