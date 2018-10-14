package jmri.jmrit.timetable.swing;

import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 * Tests for the TimeTableFrame Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableFrameTest {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new TimeTableFrame();
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