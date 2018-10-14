package jmri.jmrit.timetable.swing;

import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 * Tests for the TimeTableStartup Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableStartupTest {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new TimeTableStartup();
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