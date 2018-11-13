package jmri.jmrit.timetable;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.timetable.swing.*;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the Stop Class
 * @author Dave Sand Copyright (C) 2018
 */
public class StopTest {

    @Test
    public void testCreate() {
        new Stop(1, 1, 1);
    }

    @Test
    public void testSettersAndGetters() {
        Stop s = new Stop(1, 1, 1);
        Assert.assertEquals(1, s.getStopId());  // NOI18N
        Assert.assertEquals(1, s.getTrainId());
        s.setSeq(2);
        Assert.assertEquals(2, s.getSeq());

        s.setStationId(1);
        Assert.assertEquals(1, s.getStationId());
        s.setDuration(15);
        Assert.assertEquals(15, s.getDuration());
        s.setNextSpeed(30);
        Assert.assertEquals(30, s.getNextSpeed());
        s.setArriveTime(600);
        Assert.assertEquals(600, s.getArriveTime());
        s.setDepartTime(630);
        Assert.assertEquals(630, s.getDepartTime());
        s.setStagingTrack(2);
        Assert.assertEquals(2, s.getStagingTrack());
        s.setStopNotes("none");  // NOI18N
        Assert.assertEquals("none", s.getStopNotes());  // NOI18N
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TimeTableFrame f = new TimeTableFrame("");
        TimeTableDataManager dm = f.getDataManager();
        Station station = new Station(1, 1);
        station.setStationName("test station");  // NOI18N
        dm.addStation(1, station);
        Stop stop = new Stop(1, 1, 1);
        stop.setStationId(1);
        Assert.assertEquals("1 :: test station", stop.toString());  // NOI18N
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
