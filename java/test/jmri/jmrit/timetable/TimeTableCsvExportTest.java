package jmri.jmrit.timetable;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import jmri.profile.NullProfile;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the TimeTableCsvExport Class
 *
 * @author Dave Sand Copyright (C) 2019
 */
public class TimeTableCsvExportTest {

    /**
     * Test exporting to CSV.
     *
     * @throws IOException under unexpected in testing conditions
     */
    @Test
    public void testExport() throws IOException {
        TimeTableDataManager dm = new TimeTableDataManager(true);
        int layoutId = 0;
        int segmentId = 0;
        int scheduleId = 0;
        boolean errorOccurred;
        File file;

        for (Layout layout : dm.getLayouts(true)) {
            if (layout.getLayoutName().equals("Sample")) {
                layoutId = layout.getLayoutId();
                break;
            }
        }
        for (Segment segment : dm.getSegments(layoutId, true)) {
            if (segment.getSegmentName().equals("Mainline")) {
                segmentId = segment.getSegmentId();
                break;
            }
        }
        for (Schedule schedule : dm.getSchedules(layoutId, true)) {
            if (schedule.getScheduleName().equals("114")) {
                scheduleId = schedule.getScheduleId();
                break;
            }
        }
        TimeTableCsvExport exp = new TimeTableCsvExport();
        file = FileUtil.getFile("preference:TestCsvExport.csv");  // NOI18N
        errorOccurred = exp.exportCsv(file, layoutId, segmentId, scheduleId);
        Assert.assertFalse("No error", errorOccurred);
        Assert.assertEquals("Line count:", 23, lineCount(file));
    }

    public int lineCount(File file) {
        int count = 0;
        try (Scanner input = new Scanner(file)) {
            while (input.hasNextLine()) {
                String line = input.nextLine();
                log.debug("line = {}", line);
                count++;
            }
        } catch (IOException ex) {
            log.error("Scanner exception: ", ex);
        }
        return count;
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new NullProfile(folder));
    }

    @AfterEach
    public void tearDown() throws Exception {
        // use reflection to reset the static file location.
        Class<?> c = jmri.jmrit.timetable.configurexml.TimeTableXml.TimeTableXmlFile.class;
        java.lang.reflect.Field f = c.getDeclaredField("fileLocation");
        f.setAccessible(true);
        f.set(new String(), null);
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableCsvExportTest.class);
}
