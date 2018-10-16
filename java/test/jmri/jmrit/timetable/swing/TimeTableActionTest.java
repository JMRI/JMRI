package jmri.jmrit.timetable.swing;

import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 * Tests for the TimeTableAction Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableActionTest {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new TimeTableAction();
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new TimeTableAction().actionPerformed(null);
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