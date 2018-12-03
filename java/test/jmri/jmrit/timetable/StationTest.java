package jmri.jmrit.timetable;

import org.junit.*;

/**
 * Tests for the Station Class
 * @author Dave Sand Copyright (C) 2018
 */
public class StationTest {

    @Test
    public void testCreate() {
        try {
            new Station(0);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "StationAddFail");  // NOI18N
        }
    }

    @Test
    public void testSettersAndGetters() {
        Layout layout = new Layout();
        int layoutId = layout.getLayoutId();
        Segment segment = new Segment(layoutId);
        int segmentId = segment.getSegmentId();
        Station station = new Station(segmentId);
        Assert.assertTrue(station.getStationId() > 0);
        Assert.assertTrue(station.getSegmentId() > 0);
        station.setStationName("New Station");  // NOI18N
        Assert.assertEquals("New Station", station.getStationName());  // NOI18N
        try {
            station.setDistance(-10.0);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "DistanceLt0");  // NOI18N
        }
        station.setDistance(123.0);
        Assert.assertEquals(123.0, station.getDistance(), 1.0);
        station.setDoubleTrack(true);
        Assert.assertTrue(station.getDoubleTrack());
        try {
            station.setSidings(-1);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "SidingsLt0");  // NOI18N
        }
        station.setSidings(2);
        Assert.assertEquals(2, station.getSidings());
        try {
            station.setStaging(-1);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "StagingLt0");  // NOI18N
        }

        station.setStaging(4);
        Assert.assertEquals(4, station.getStaging());
        Assert.assertEquals("New Station", station.toString());  // NOI18N
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