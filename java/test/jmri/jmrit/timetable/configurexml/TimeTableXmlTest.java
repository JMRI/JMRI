package jmri.jmrit.timetable.configurexml;

import java.io.File;
import java.io.IOException;

import jmri.jmrit.timetable.swing.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the TimeTableXml Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableXmlTest {

    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testLoadAndStore() {
        TimeTableFrame f = new TimeTableFrame("");
        Assert.assertNotNull(f);
        boolean loadResult = TimeTableXml.doLoad();
        Assert.assertTrue("Load Failed", loadResult);  // NOI18N
        boolean storeResult = TimeTableXml.doStore();
        Assert.assertTrue("Store Failed", storeResult);  // NOI18N
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
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
