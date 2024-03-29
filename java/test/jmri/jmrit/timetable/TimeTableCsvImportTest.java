package jmri.jmrit.timetable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the TimeTableCsvImport Class
 * @author Dave Sand Copyright (C) 2019
 */
public class TimeTableCsvImportTest {

    @Test
    public void testImport() {
        TimeTableDataManager dm = new TimeTableDataManager(false);
        TimeTableCsvImport imp = new TimeTableCsvImport();
        List<String> feedback;
        try {
            File file = FileUtil.getFile("preference:TestCsvImport.csv");  // NOI18N
            createCsvFile(file);
            feedback = imp.importCsv(file);
        } catch (IOException ex) {
            Assertions.fail("Unable to test the CSV import process: " + ex); // NOI18N
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
        List<String> feedback;
        try {
            File file = FileUtil.getFile("preference:TestMinimalCsvImport.csv");  // NOI18N
            createMinimalCsvFile(file);
            feedback = imp.importCsv(file);
        } catch (IOException ex) {
            Assertions.fail("Unable to test the CSV minimal import process: " + ex ); // NOI18N
            return;
        }
        Assert.assertEquals("Minimal:", 0, feedback.size());
    }

    @Test
    public void testBadImport() {
        TimeTableCsvImport imp = new TimeTableCsvImport();
        List<String> feedback;
        try {
            File file = FileUtil.getFile("preference:TestBadCsvImport.csv");  // NOI18N
            createBadCsvFile(file);
            feedback = imp.importCsv(file);
        } catch (IOException ex) {
            Assertions.fail("Unable to test the CSV bad import process: " + ex ); // NOI18N
            return;
        }
        jmri.util.JUnitAppender.assertWarnMessage("Unable to process record 2, content = [Layout]");  // NOI18N
        Assert.assertEquals("Bad:", 1, feedback.size());
    }

    public void createCsvFile(File file) {
        // Create a test CSV file for the import test
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file.getAbsolutePath()), StandardCharsets.UTF_8))) {
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
            Assertions.fail("Unable to create the test import CSV file " + ex );
        }
    }

    public void createMinimalCsvFile(File file) {
        // Create a test CSV file for the import test
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file.getAbsolutePath()), StandardCharsets.UTF_8))) {
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
            Assertions.fail("Unable to create the minimal test import CSV file " + ex );
        }
    }

    public void createBadCsvFile(File file) {
        // Create a test CSV file for the import test
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file.getAbsolutePath()), StandardCharsets.UTF_8))) {
           writer.write("Layout\n");
           writer.write("Layout\n");
           writer.write("TrainType, UseLayoutTypes\n");
           writer.write("Segment\n");
           writer.write("Station, UseSegmentStations\n");
           writer.close();
        } catch (IOException ex) {
            Assertions.fail("Unable to create the bad test import CSV file " + ex );
        }
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

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableCsvImportTest.class);
}
