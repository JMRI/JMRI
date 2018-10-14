package jmri.jmrit.timetable;

import org.junit.*;

/**
 * Tests for the Segment Class
 * @author Dave Sand Copyright (C) 2018
 */
public class SegmentTest {

    @Test
    public void testCreate() {
        new Segment(1, 1, "");
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