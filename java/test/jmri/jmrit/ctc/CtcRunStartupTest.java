package jmri.jmrit.ctc;

import java.awt.GraphicsEnvironment;
import java.util.Locale;
import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Tests for the CtcRunStartup Class.
 *
 * @author Dave Sand Copyright (C) 2019
 */
public class CtcRunStartupTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetTitle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Start CTC Runtime", new CtcRunStartup().getTitle(CtcRunAction.class, Locale.US));  // NOI18N
    }

    @Test
    public void testGetClass() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull(new CtcRunStartup().getActionClasses());
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
