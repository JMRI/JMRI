package jmri.jmrit.timetable.configurexml;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

import jmri.jmrit.timetable.swing.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the TimeTableXml Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableXmlTest {

    @Test
    public void testCreate() {
        new TimeTableXml();
    }

    @Test
    public void testLoadAndStore() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TimeTableFrame f = new TimeTableFrame("");
        Assert.assertNotNull(f);
        boolean loadResult = TimeTableXml.doLoad();
        Assert.assertTrue("Load Failed", loadResult);  // NOI18N
        boolean storeResult = TimeTableXml.doStore();
        Assert.assertTrue("Store Failed", storeResult);  // NOI18N
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
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
