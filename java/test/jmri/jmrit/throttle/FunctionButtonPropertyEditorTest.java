package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of FunctionButtonPropertyEditor
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class FunctionButtonPropertyEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        FunctionButton button = new FunctionButton();
        FunctionButtonPropertyEditor dialog = new FunctionButtonPropertyEditor(button);
        Assert.assertNotNull("exists", dialog);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
