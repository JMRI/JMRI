package jmri.jmrit.timetable.configurexml;

import java.awt.GraphicsEnvironment;
import org.junit.*;
import jmri.jmrit.timetable.*;
import jmri.jmrit.timetable.swing.*;
import jmri.util.JUnitUtil;

/**
 * Tests for the TimeTableXml Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableXmlTest {

    @Rule
    public org.junit.rules.TemporaryFolder folder = new org.junit.rules.TemporaryFolder();

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
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
