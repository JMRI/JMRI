package jmri.jmrit.timetable.swing;

import java.io.File;
import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the TimeTableAction Class
 * @author Dave Sand Copyright (C) 2018
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class TimeTableActionTest {

    @Test
    public void testCreate() {
        TimeTableAction t = new TimeTableAction();
        Assertions.assertNotNull(t);
    }

    @Test
    public void testAction() {
        new TimeTableAction().actionPerformed(null);
        new TimeTableAction().actionPerformed(null);
    }

    @Test
    public void testMakePanel() {
        IllegalArgumentException assertThrows = Assert.assertThrows(IllegalArgumentException.class, () -> new TimeTableAction().makePanel());
        Assertions.assertEquals("Should not be invoked", assertThrows.getMessage());
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
