package jmri.jmrit.timetable.swing;

import java.awt.GraphicsEnvironment;
import java.util.Locale;
import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Tests for the TimeTableStartup Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableStartupTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new TimeTableStartup();
    }

    @Test
    public void testGetTitle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Open Timetable", new TimeTableStartup().getTitle(TimeTableAction.class, Locale.US));  // NOI18N
    }

    @Test
    public void testGetTitleException() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        thrown.expect(IllegalArgumentException.class);
        Assert.assertEquals("Open Timetable Exception", new TimeTableStartup().getTitle(TimeTableFrame.class, Locale.US));  // NOI18N
    }

    @Test
    public void testGetClass() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull(new TimeTableStartup().getActionClasses());
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