package jmri.jmrit.timetable.swing;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the TimeTablePrintGraph Class
 * @author Dave Sand Copyright (C) 2019
 */
public class TimeTablePrintGraphTest {

    @Test
    public void testGraph() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        TimeTablePrintGraph g = new TimeTablePrintGraph(1, 1, true, false);
        Assert.assertNotNull(g);

        JUnitAppender.suppressWarnMessage("No scale found, defaulting to HO");

    }
    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
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
        JUnitUtil.tearDown();
    }
}
