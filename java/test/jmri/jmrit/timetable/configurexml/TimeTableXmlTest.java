package jmri.jmrit.timetable.configurexml;

import org.junit.*;

/**
 * Tests for the TimeTableXml Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableXmlTest {

    @Test
    public void testCreate() {
        new TimeTableXml();
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