package jmri.jmrit.timetable.swing;

import java.io.File;
import java.io.IOException;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the TimeTablePrintGraph Class
 * @author Dave Sand Copyright (C) 2019
 */
public class TimeTablePrintGraphTest {

    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testGraph() {

        TimeTablePrintGraph g = new TimeTablePrintGraph(1, 1, true, false);
        Assert.assertNotNull(g);

        JUnitAppender.suppressWarnMessage("No scale found, defaulting to HO");

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
}
