package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of FunctionPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class FunctionPanelTest {

    FunctionPanel frame = null; // not a panel despite class name

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", frame);
    }

    @Test
    public void testGetFunctionButtons() {
        Assert.assertNotNull(frame);
        FunctionButton fba[] = frame.getFunctionButtons();
        Assert.assertNotNull("Function Button Array exists", fba);        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        frame = new FunctionPanel();
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
