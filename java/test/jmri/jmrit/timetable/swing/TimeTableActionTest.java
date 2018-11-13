package jmri.jmrit.timetable.swing;

import java.awt.GraphicsEnvironment;
import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Tests for the TimeTableAction Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableActionTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new TimeTableAction();
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new TimeTableAction().actionPerformed(null);
        new TimeTableAction().actionPerformed(null);
    }

    @Test
    public void testMakePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        thrown.expect(IllegalArgumentException.class);
        new TimeTableAction().makePanel();
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