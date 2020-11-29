package jmri.jmrit.ctc.editor;

import java.awt.GraphicsEnvironment;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Tests for the CtcEditorStartup Class.
 *
 * @author Dave Sand Copyright (C) 2019
 */
public class CtcEditorStartupTest {

    @Test
    public void testGetTitle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("CTC Editor", new CtcEditorStartup().getTitle(CtcEditorAction.class, Locale.US));  // NOI18N
    }

    @Test
    public void testGetClass() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull(new CtcEditorStartup().getActionClasses());
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
