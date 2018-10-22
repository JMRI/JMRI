package jmri.jmrit.timetable;

import org.junit.*;

/**
 * Tests for the Station Class
 * @author Dave Sand Copyright (C) 2018
 */
public class StationTest {

    @Test
    public void testCreate() {
        new Station(1, 1);
    }

    @Test
    public void testSettersAndGetters() {
        Station s = new Station(1, 1);
        Assert.assertEquals(1, s.getStationId());
        Assert.assertEquals(1, s.getSegmentId());
        s.setStationName("New Station");  // NOI18N
        Assert.assertEquals("New Station", s.getStationName());  // NOI18N
        s.setDistance(123.0);
        Assert.assertEquals(123.0, s.getDistance(), 1.0);
        s.setDoubleTrack(true);
        Assert.assertTrue(s.getDoubleTrack());
        s.setSidings(2);
        Assert.assertEquals(2, s.getSidings());
        s.setStaging(4);
        Assert.assertEquals(4, s.getStaging());
        Assert.assertEquals("New Station", s.toString());  // NOI18N
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