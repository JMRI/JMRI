package jmri.jmrit.timetable;

import org.junit.*;

/**
 * Tests for the TimeTableDataManager Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableDataManagerTest {

    @Test
    public void testCreate() {
        TimeTableDataManager dx = new TimeTableDataManager(false);
        jmri.InstanceManager.deregister(dx, TimeTableDataManager.class);
        dx = null;
    }

    @Test
    public void testExercise() {
        TimeTableDataManager dm = new TimeTableDataManager(false);
//         log.warn("test dm = {}", dm.hashCode());

        // Test adds
        Layout layout = new Layout();
        int layoutId = layout.getLayoutId();

        TrainType type = new TrainType(layoutId);  // NOI18N
        int typeId = type.getTypeId();

        Segment segment = new Segment(layoutId);  // NOI18N
        int segmentId = segment.getSegmentId();

//         int stationId = dm.getNextId("Station");  // NOI18N
        Station station1 = new Station(segmentId);
        Station station2 = new Station(segmentId);
        int stationId1 = station1.getStationId();
        int stationId2 = station2.getStationId();
//         dm.addStation(stationId, station1);
//         dm.addStation(stationId + 1, station2);

//         int scheduleId = dm.getNextId("Schedule");  // NOI18N
        Schedule schedule = new Schedule(layoutId);
        int scheduleId = schedule.getScheduleId();
//         dm.addSchedule(scheduleId, schedule);

//         int trainId = dm.getNextId("Train");  // NOI18N
        Train train = new Train(scheduleId);
        int trainId = train.getTrainId();
        train.setTypeId(typeId);
//         dm.addTrain(trainId, train);

//         int stopId = dm.getNextId("Stop");  // NOI18N
//         log.warn("test stop station: {}", stationId1);
        Stop stop1 = new Stop(trainId, 1);
        stop1.setStationId(stationId1);
        Stop stop2 = new Stop(trainId, 2);
        stop2.setStationId(stationId2);
//         dm.addStop(stopId, stop1);
        int stopId1 = stop1.getStopId();
//         dm.addStop(stopId + 1, stop2);

        // Test gets
        Assert.assertNotNull(dm.getLayout(layoutId));
        Assert.assertNotNull(dm.getTrainType(typeId));
        Assert.assertNotNull(dm.getSegment(segmentId));
        Assert.assertNotNull(dm.getStation(stationId1));
        Assert.assertNotNull(dm.getSchedule(scheduleId));
        Assert.assertNotNull(dm.getTrain(trainId));
        Assert.assertNotNull(dm.getStop(stopId1));

        // Test array lists
        Assert.assertEquals(1, dm.getLayouts(true).size());
        Assert.assertEquals(1, dm.getTrainTypes(layoutId, true).size());
        Assert.assertEquals(1, dm.getSegments(layoutId, true).size());
        Assert.assertEquals(2, dm.getStations(segmentId, true).size());
        Assert.assertEquals(1, dm.getSchedules(layoutId, true).size());
        Assert.assertEquals(1, dm.getTrains(scheduleId, 0, true).size());
        Assert.assertEquals(1, dm.getTrains(0, typeId, true).size());
        Assert.assertEquals(2, dm.getStops(trainId, 0, true).size());
        Assert.assertEquals(1, dm.getStops(0, stationId1, true).size());

        // Test special cases
        Assert.assertNotNull(dm.getLayoutForStop(stopId1));
        Assert.assertEquals(2, dm.getSegmentStations(layoutId).size());
        int nextId = dm.getNextId("ErrorMsg");  // NOI18N
        jmri.util.JUnitAppender.assertErrorMessage("getNextId: Invalid record type: ErrorMsg");  // NOI18N
        Assert.assertEquals(0, nextId);

        // Release data manager
        jmri.InstanceManager.deregister(dm, TimeTableDataManager.class);
        dm = null;
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableDataManagerTest.class);
}