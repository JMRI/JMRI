package jmri.jmrit.timetable;

import java.io.File;
import java.io.IOException;

import jmri.util.FileUtil;

import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the TimeTableImport Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableImportTest {

    @Test
    public void testCreate() {
        new TimeTableImport();
    }

    @Test
    public void testImport() {
        TimeTableDataManager dm = new TimeTableDataManager(false);
        TimeTableImport imp = new TimeTableImport();
        try {
            File file = FileUtil.getFile("program:xml/demoTimetable/TestSample.sgn");  // NOI18N
            imp.importSgn(dm, file);
        } catch (IOException ex) {
            log.error("Unable to test the import process");  // NOI18N
            return;
        }

        // Verify import results
        List<Layout> layouts = dm.getLayouts(true);
        Assert.assertEquals(1, layouts.size());
        int layoutId = layouts.get(0).getLayoutId();
        Assert.assertEquals(8, dm.getTrainTypes(layoutId, true).size());

        List<Segment> segments = dm.getSegments(layoutId, true);
        Assert.assertEquals(1, segments.size());
        int segmentId = segments.get(0).getSegmentId();
        Assert.assertEquals(5, dm.getStations(segmentId, true).size());

        List<Schedule> schedules = dm.getSchedules(layoutId, true);
        Assert.assertEquals(1, schedules.size());
        int scheduleId = schedules.get(0).getScheduleId();

        List<Train> trains = dm.getTrains(scheduleId, 0, true);
        Assert.assertEquals(10, trains.size());
        int trainId = trains.get(0).getTrainId();
        Assert.assertEquals(5, dm.getStops(trainId, 0, true).size());
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
    }

    @AfterEach
    public void tearDown() {
       // use reflection to reset the static file location.
       try {
            Class<?> c = jmri.jmrit.timetable.configurexml.TimeTableXml.TimeTableXmlFile.class;
            java.lang.reflect.Field f = c.getDeclaredField("fileLocation");
            f.setAccessible(true);
            f.set(new String(), null);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException x) {
            Assert.fail("Failed to reset TimeTableXml static fileLocation " + x);
        }
        jmri.util.JUnitUtil.tearDown();
    }
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableImportTest.class);
}
