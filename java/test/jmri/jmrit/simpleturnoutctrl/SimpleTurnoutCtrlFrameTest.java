package jmri.jmrit.simpleturnoutctrl;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SimpleTurnoutCtrlFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SimpleTurnoutCtrlFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SimpleTurnoutCtrlFrame action = new SimpleTurnoutCtrlFrame();
        Assert.assertNotNull("exists", action);
        JUnitUtil.dispose(action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
