package jmri.jmrit.simpleturnoutctrl;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SimpleTurnoutCtrlAction
 *
 * @author	Paul Bender Copyright (C) 2016
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
