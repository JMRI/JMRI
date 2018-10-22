package jmri.jmrit.timetable.swing;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import jmri.jmrit.timetable.*;
import jmri.util.JmriJFrame;
import org.junit.*;

/**
 * Tests for the TimeTableGraph Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableGraphTest {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new TimeTableGraph();
    }

    @Test
    public void testGraph() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TimeTableFrame f = new TimeTableFrame("");
        TimeTableDataManager dm = f.getDataManager();


        TimeTableGraph g = new TimeTableGraph();
        g.init(1, 1, dm, true);

        JmriJFrame gf = new JmriJFrame(Bundle.getMessage("TitleTimeTableGraph"), true, true);  // NOI18N
        gf.setMinimumSize(new Dimension(600, 300));
        gf.getContentPane().add(g);
        gf.pack();
        gf.addHelpMenu("package.jmri.jmrit.timetable.TimeTableGraph", true);  // NOI18N
        gf.setVisible(true);

        gf.dispose();
        f.dispose();
    }
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}