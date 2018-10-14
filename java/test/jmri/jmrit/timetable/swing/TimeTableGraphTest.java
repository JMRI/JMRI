package jmri.jmrit.timetable.swing;

import org.junit.*;

/**
 * Tests for the TimeTableGraph Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableGraphTest {

    @Test
    public void testCreate() {
        new TimeTableGraph();
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