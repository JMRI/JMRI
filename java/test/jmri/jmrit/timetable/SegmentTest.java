package jmri.jmrit.timetable;

import org.junit.*;

import jmri.util.JUnitUtil;

/**
 * Tests for the Segment Class
 * @author Dave Sand Copyright (C) 2018
 */
public class SegmentTest {

    @Test
    public void testCreate() {
        try {
            new Segment(0);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "SegmentAddFail");  // NOI18N
        }
    }

    @Test
    public void testSettersAndGetters() {
        Layout layout = new Layout();
        int layoutId = layout.getLayoutId();
        Segment segment = new Segment(layoutId);
        Assert.assertTrue(segment.getSegmentId() > 0);
        Assert.assertTrue(segment.getLayoutId() > 0);
        segment.setSegmentName("New Segment");  // NOI18N
        Assert.assertEquals("New Segment", segment.getSegmentName());  // NOI18N
        Assert.assertEquals("New Segment", segment.toString());  // NOI18N
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}