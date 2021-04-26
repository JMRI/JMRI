package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of FunctionPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class FunctionPanelTest {

    FunctionPanel frame; // not a panel despite class name

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", frame);
    }

    @Test
    public void testGetFunctionButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        FunctionButton fba[] = frame.getFunctionButtons();
        Assert.assertNotNull("Function Button Array exists", fba);        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new FunctionPanel();
        }
    }

    @AfterEach
    public void tearDown() {
        if (frame != null) {
            frame.dispose();
        }
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }
}
