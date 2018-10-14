package jmri.jmrit.timetable.swing;

import org.junit.*;

/**
 * Tests for the TimeTableAction Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableActionTest {

    @Test
    public void testCreate() {
        new TimeTableAction();
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