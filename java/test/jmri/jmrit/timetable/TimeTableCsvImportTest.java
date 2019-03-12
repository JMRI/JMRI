package jmri.jmrit.timetable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the TimeTableCsvImport Class
 * @author Dave Sand Copyright (C) 2019
 */
public class TimeTableCsvImportTest {

    @Rule
    public org.junit.rules.TemporaryFolder folder = new org.junit.rules.TemporaryFolder();

    @Test
    public void testImport() {
        TimeTableDataManager dm = new TimeTableDataManager(false);
        TimeTableCsvImport imp = new TimeTableCsvImport();
        List<String> feedback = new ArrayList<>();
        try {
            File file = FileUtil.getFile("preference:TestCsvImport.csv");  // NOI18N
            createCsvFile(file);
            feedback = imp.importCsv(file);
        } catch (IOException ex) {
            log.error("Unable to test the CSV export process");  // NOI18N
            return;
        }
        Assert.assertEquals("feedback:", 0, feedback.size());

        // Verify results
        int layoutCount = 0;
        int typeCount = 0;
        int segmentCount = 0;
        int stationCount = 0;
        int scheduleCount = 0;
        int trainCount = 0;
        int stopCount = 0;

        for (Layout layout : dm.getLayouts(false)) {
            layoutCount++;
            int layoutId = layout.getLayoutId();
            typeCount += dm.getTrainTypes(layoutId, false).size();
            for (Segment segment : dm.getSegments(layoutId, false)) {
                segmentCount++;
                stationCount += dm.getStations(segment.getSegmentId(), false).size();
            }
            for (Schedule schedule : dm.getSchedules(layoutId, false)) {
                scheduleCount++;
                for (Train train : dm.getTrains(schedule.getScheduleId(), 0, false)) {
                    trainCount++;
                    stopCount += dm.getStops(train.getTrainId(), 0, false).size();
                }
            }
            Assert.assertEquals("Layouts:", 1, layoutCount);
            Assert.assertEquals("Types:", 1, typeCount);
            Assert.assertEquals("Segments:", 1, segmentCount);
            Assert.assertEquals("Stations:", 3, stationCount);
            Assert.assertEquals("Schedules:", 1, scheduleCount);
            Assert.assertEquals("Trains:", 2, trainCount);
            Assert.assertEquals("Stops:", 6, stopCount);
        }
    }

    @Test
    public void testMinimalImport() {
        TimeTableCsvImport imp = new TimeTableCsvImport();
        List<String> feedback = new ArrayList<>();
        try {
            File file = FileUtil.getFile("preference:TestMinimalCsvImport.csv");  // NOI18N
            createMinimalCsvFile(file);
            feedback = imp.importCsv(file);
        } catch (IOException ex) {
            log.error("Unable to test the CSV export process");  // NOI18N
            return;
        }
        Assert.assertEquals("Minimal:", 0, feedback.size());
    }

    @Test
    public void testBadImport() {
        TimeTableCsvImport imp = new TimeTableCsvImport();
        List<String> feedback = new ArrayList<>();
        try {
            File file = FileUtil.getFile("preference:TestBadCsvImport.csv");  // NOI18N
            createBadCsvFile(file);
            feedback = imp.importCsv(file);
        } catch (IOException ex) {
            log.error("Unable to test the CSV export process");  // NOI18N
            return;
        }
        jmri.util.JUnitAppender.assertWarnMessage("Unable to process record 2, content = [Layout]");  // NOI18N
        Assert.assertEquals("Bad:", 1, feedback.size());
    }

    public void createCsvFile(File file) {
        // Create a test CSV file for the import test
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file.getAbsolutePath()), "utf-8"))) {
           writer.write("Layout,Time Table CSV Test Layout,N,6,0,Yes\n");
           writer.write("TrainType,Freight,123123\n");
           writer.write("Segment,Mainline\n");
           writer.write("Station\n");
           writer.write("Station,Station 2,12,Yes,1,0\n");
           writer.write("Station,Station 3,32,No,1,0\n");
           writer.write("Schedule,,,6,12\n");
           writer.write("Train,XYZ,Test 1,1,30,420\n");
           writer.write("Stop,1,,25\n");
           writer.write("Stop,2,30,Meet QRS\n");
           writer.write("Stop,3\n");
           writer.write("Train,QRS,Test 2,1,30,420,0,Note\n");
           writer.write("Stop,3,,25\n");
           writer.write("Stop,2,30,Meet XYZ\n");
           writer.write("Stop,1\n");
           writer.close();
        } catch (IOException ex) {
            log.warn("Unable to create the test import CSV file ", ex);
        }
    }

    public void createMinimalCsvFile(File file) {
        // Create a test CSV file for the import test
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file.getAbsolutePath()), "utf-8"))) {
           writer.write("Layout\n");
           writer.write("TrainType\n");
           writer.write("Segment\n");
           writer.write("Station\n");
           writer.write("Station,,10\n");
           writer.write("Schedule\n");
           writer.write("Train\n");
           writer.write("Stop,1\n");
           writer.write("Stop,2\n");
           writer.close();
        } catch (IOException ex) {
            log.warn("Unable to create the minimal test import CSV file ", ex);
        }
    }

    public void createBadCsvFile(File file) {
        // Create a test CSV file for the import test
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file.getAbsolutePath()), "utf-8"))) {
           writer.write("Layout\n");
           writer.write("Layout\n");
           writer.write("TrainType, UseLayoutTypes\n");
           writer.write("Segment\n");
           writer.write("Station, UseSegmentStations\n");
           writer.close();
        } catch (IOException ex) {
            log.warn("Unable to create the bad test import CSV file ", ex);
        }
    }
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        try {
            JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE)));
        } catch(java.io.IOException ioe){
          Assert.fail("failed to setup profile for test");
        }
    }

    @After
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
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableCsvImportTest.class);
}
