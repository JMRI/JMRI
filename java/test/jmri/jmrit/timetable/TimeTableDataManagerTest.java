package jmri.jmrit.timetable;

import org.junit.*;

/**
 * Tests for the TimeTableDataManager Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableDataManagerTest {

    @Test
    public void testCreate() {
        new TimeTableDataManager();
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