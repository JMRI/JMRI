package jmri.jmrit.simpleturnoutctrl;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of SimpleTurnoutCtrlAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimpleTurnoutCtrlActionTest {

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SimpleTurnoutCtrlAction action = new SimpleTurnoutCtrlAction("Turnout Control Action");
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SimpleTurnoutCtrlAction action = new SimpleTurnoutCtrlAction(); 
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
