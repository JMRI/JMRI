package jmri.jmrit.timetable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the TimeTableCsvExport Class
 * @author Dave Sand Copyright (C) 2019
 */
public class TimeTableCsvExportTest {

    @Rule
    public org.junit.rules.TemporaryFolder folder = new org.junit.rules.TemporaryFolder();

    @Test
    public void testExport() {
        TimeTableDataManager dm = new TimeTableDataManager(true);
        int layoutId = 0;
        int segmentId = 0;
        int scheduleId = 0;
        boolean errorOccurred = false;
        File file = null;

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
        try {
            file = FileUtil.getFile("preference:TestCsvExport.csv");  // NOI18N
            errorOccurred = exp.exportCsv(file, layoutId, segmentId, scheduleId);
        } catch (IOException ex) {
            log.error("Unable to test the CSV export process");  // NOI18N
            return;
        }
        Assert.assertFalse("No error", errorOccurred);
        Assert.assertEquals("Line count:", 23, lineCount(file));
    }

    public int lineCount(File file) {
        int count = 0;
        try {
            Scanner input = new Scanner(file);
            while (input.hasNextLine()) {
                String line = input.nextLine();
                log.debug("line = {}", line);
                count++;
            }
            input.close();
        } catch (IOException ex) {
            log.error("Scanner exception: ", ex);
        }
        return count;
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableCsvExportTest.class);
}
