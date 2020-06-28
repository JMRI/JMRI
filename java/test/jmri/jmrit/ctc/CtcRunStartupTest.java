package jmri.jmrit.ctc;

import java.awt.GraphicsEnvironment;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Tests for the CtcRunStartup Class.
 *
 * @author Dave Sand Copyright (C) 2019
 */
public class CtcRunStartupTest {

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

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
