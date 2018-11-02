package jmri.jmrit.timetable;

import org.junit.*;

/**
 * Tests for the Layout Class
 * @author Dave Sand Copyright (C) 2018
 */
public class LayoutTest {

    @Test
    public void testCreate() {
        new Layout(1);
    }

    @Test
    public void testSettersAndGetters() {
        Layout l = new Layout(1);
        Assert.assertEquals(1, l.getLayoutId());
        l.setLayoutName("Test Name");  // NOI18N
        Assert.assertEquals("Test Name", l.getLayoutName());  // NOI18N
        Assert.assertEquals(87.1, l.getScale(), 1.0);
        l.setFastClock(6);
        Assert.assertEquals(6, l.getFastClock());
        l.setThrottles(3);
        Assert.assertEquals(3, l.getThrottles());
        l.setMetric(true);
        Assert.assertTrue(l.getMetric());
        l.setScaleMK();
        Assert.assertEquals(1.914, l.getScaleMK(), .1);
        Assert.assertEquals("Test Name", l.toString());  // NOI18N
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTest.class);
}