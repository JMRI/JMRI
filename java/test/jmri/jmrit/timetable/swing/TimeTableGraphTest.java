package jmri.jmrit.timetable.swing;

import java.awt.GraphicsEnvironment;
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

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}