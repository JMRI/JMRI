package jmri.jmrit.timetable;

import org.junit.*;

/**
 * Tests for the Segment Class
 * @author Dave Sand Copyright (C) 2018
 */
public class SegmentTest {

    @Test
    public void testCreate() {
        new Segment(1, 1, "");  // NOI18N
    }

    @Test
    public void testSettersAndGetters() {
        Segment s = new Segment(1, 1, "Test Segment");  // NOI18N
        Assert.assertEquals(1, s.getSegmentId());  // NOI18N
        Assert.assertEquals(1, s.getLayoutId());
        s.setSegmentName("New Segment");  // NOI18N
        Assert.assertEquals("New Segment", s.getSegmentName());  // NOI18N
        Assert.assertEquals("New Segment", s.toString());  // NOI18N
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