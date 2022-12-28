package jmri.jmrit.timetable.swing;

import java.awt.Dimension;

import java.io.File;
import java.io.IOException;

import jmri.util.JmriJFrame;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the TimeTableDisplayGraph Class
 * @author Dave Sand Copyright (C) 2019
 */
public class TimeTableDisplayGraphTest {

    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testGraph() {

        TimeTableDisplayGraph g = new TimeTableDisplayGraph(1, 1, true);

        JmriJFrame gf = new JmriJFrame(Bundle.getMessage("TitleTimeTableGraph"), true, true);  // NOI18N
        gf.setMinimumSize(new Dimension(600, 300));
        gf.getContentPane().add(g);
        gf.pack();
        gf.addHelpMenu("package.jmri.jmrit.timetable.TimeTableGraph", true);  // NOI18N
        gf.setVisible(true);
        Assert.assertNotNull(gf);

        gf.dispose();

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
