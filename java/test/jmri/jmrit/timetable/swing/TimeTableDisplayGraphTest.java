package jmri.jmrit.timetable.swing;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import jmri.util.JmriJFrame;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the TimeTableDisplayGraph Class
 * @author Dave Sand Copyright (C) 2019
 */
public class TimeTableDisplayGraphTest {

    @Test
    public void testGraph() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

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
