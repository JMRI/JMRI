package jmri.jmrit.timetable;

import java.io.File;
import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the Stop Class
 * @author Dave Sand Copyright (C) 2018
 */
public class StopTest {

    @Test
    public void testCreate() {
        try {
            Stop t = new Stop(0, 1);
            Assertions.fail("stop should have not been created " + t.toString() );
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("StopAddFail", ex.getMessage());  // NOI18N
        }
    }

    @Test
    public void testSettersAndGetters() {
        Layout layout = new Layout();
        int layoutId = layout.getLayoutId();
        Segment segment = new Segment(layoutId);
        int segmentId = segment.getSegmentId();
        Station station = new Station(segmentId);
        int stationId = station.getStationId();
        Schedule schedule = new Schedule(layoutId);
        int scheduleId = schedule.getScheduleId();
        Train train = new Train (scheduleId);
        int trainId = train.getTrainId();

        Stop stop = new Stop(trainId, 1);
        Assert.assertTrue(stop.getStopId() > 0);
        Assert.assertTrue(stop.getTrainId() == trainId);
        stop.setSeq(2);
        Assert.assertEquals(2, stop.getSeq());
        stop.setStationId(stationId);
        Assert.assertTrue(stop.getStationId() == stationId);
        try {
            stop.setDuration(-2);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("StopDurationLt0", ex.getMessage());  // NOI18N
        }
        try {
            stop.setDuration(240);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("TimeOutOfRange", ex.getMessage());  // NOI18N
        }
        stop.setDuration(15);
        Assert.assertEquals(15, stop.getDuration());
        try {
            stop.setNextSpeed(-2);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("NextSpeedLt0", ex.getMessage());  // NOI18N
        }
        try {
            stop.setNextSpeed(1);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("TimeOutOfRange", ex.getMessage());  // NOI18N
        }
        stop.setNextSpeed(30);
        Assert.assertEquals(30, stop.getNextSpeed());
        stop.setArriveTime(600);
        Assert.assertEquals(600, stop.getArriveTime());
        stop.setDepartTime(630);
        Assert.assertEquals(630, stop.getDepartTime());
        try {
            stop.setStagingTrack(2);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("StagingRange", ex.getMessage());  // NOI18N
        }
        stop.setStagingTrack(0);
        Assert.assertEquals(0, stop.getStagingTrack());
        stop.setStopNotes("none");  // NOI18N
        Assert.assertEquals("none", stop.getStopNotes());  // NOI18N
        Assert.assertEquals("2 :: New Station", stop.toString());  // NOI18N
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
    }

    @AfterEach
    public void tearDown() {
        // reset the static file location.
        jmri.jmrit.timetable.configurexml.TimeTableXml.TimeTableXmlFile.resetFileLocation();
        JUnitUtil.tearDown();
    }
}
