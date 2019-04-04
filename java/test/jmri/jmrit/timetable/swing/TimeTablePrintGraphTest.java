package jmri.jmrit.timetable.swing;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import jmri.jmrit.timetable.*;
import jmri.util.JmriJFrame;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the TimeTablePrintGraph Class
 * @author Dave Sand Copyright (C) 2019
 */
public class TimeTablePrintGraphTest {

    @Rule
    public org.junit.rules.TemporaryFolder folder = new org.junit.rules.TemporaryFolder();

    @Test
    public void testGraph() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        TimeTablePrintGraph g = new TimeTablePrintGraph(1, 1, true, false);
        Assert.assertNotNull(g);

        JUnitAppender.suppressWarnMessage("No scale found, defaulting to HO");

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
        JUnitUtil.tearDown();
    }
}
