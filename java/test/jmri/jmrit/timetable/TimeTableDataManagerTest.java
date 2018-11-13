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

    @Test
    public void testExercise() {
        TimeTableDataManager dm = new TimeTableDataManager();

        // Test adds
        int layoutId = dm.getNextId("Layout");  // NOI18N
        Layout layout = new Layout(layoutId);
        dm.addLayout(layoutId, layout);

        int typeId = dm.getNextId("TrainType");  // NOI18N
        TrainType type = new TrainType(typeId, layoutId, "Passenger", "#000000");  // NOI18N
        dm.addTrainType(typeId, type);

        int segmentId = dm.getNextId("Segment");  // NOI18N
        Segment segment = new Segment(segmentId, layoutId, "Mainline");  // NOI18N
        dm.addSegment(segmentId, segment);

        int stationId = dm.getNextId("Station");  // NOI18N
        Station station1 = new Station(stationId, segmentId);
        Station station2 = new Station(stationId + 1, segmentId);
        dm.addStation(stationId, station1);
        dm.addStation(stationId + 1, station2);

        int scheduleId = dm.getNextId("Schedule");  // NOI18N
        Schedule schedule = new Schedule(scheduleId, layoutId);
        dm.addSchedule(scheduleId, schedule);

        int trainId = dm.getNextId("Train");  // NOI18N
        Train train = new Train(trainId, scheduleId);
        train.setTypeId(typeId);
        dm.addTrain(trainId, train);

        int stopId = dm.getNextId("Stop");  // NOI18N
        Stop stop1 = new Stop(stopId, trainId, 1);
        Stop stop2 = new Stop(stopId + 2, trainId, 2);
        stop1.setStationId(stationId);
        stop2.setStationId(stationId + 1);
        dm.addStop(stopId, stop1);
        dm.addStop(stopId + 1, stop2);

        // Test gets
        Assert.assertNotNull(dm.getLayout(layoutId));
        Assert.assertNotNull(dm.getTrainType(typeId));
        Assert.assertNotNull(dm.getSegment(segmentId));
        Assert.assertNotNull(dm.getStation(stationId));
        Assert.assertNotNull(dm.getSchedule(scheduleId));
        Assert.assertNotNull(dm.getTrain(trainId));
        Assert.assertNotNull(dm.getStop(stopId));

        // Test array lists
        Assert.assertEquals(1, dm.getLayouts(true).size());
        Assert.assertEquals(1, dm.getTrainTypes(layoutId, true).size());
        Assert.assertEquals(1, dm.getSegments(layoutId, true).size());
        Assert.assertEquals(2, dm.getStations(segmentId, true).size());
        Assert.assertEquals(1, dm.getSchedules(layoutId, true).size());
        Assert.assertEquals(1, dm.getTrains(scheduleId, 0, true).size());
        Assert.assertEquals(1, dm.getTrains(0, typeId, true).size());
        Assert.assertEquals(2, dm.getStops(trainId, 0, true).size());
        Assert.assertEquals(1, dm.getStops(0, stationId, true).size());

        // Test special cases
        Assert.assertNotNull(dm.getLayoutForStop(stopId));
        Assert.assertEquals(2, dm.getSegmentStations(layoutId).size());
        int nextId = dm.getNextId("ErrorMsg");  // NOI18N
        jmri.util.JUnitAppender.assertErrorMessage("getNextId: Invalid record type: ErrorMsg");  // NOI18N
        Assert.assertEquals(0, nextId);
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