package jmri.jmrit.logixng.actions.swing;

import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;

import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.actions.DigitalBooleanLogixAction;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DigitalBooleanLogixActionSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class DigitalBooleanLogixActionSwingTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        DigitalBooleanLogixActionSwing t = new DigitalBooleanLogixActionSwing();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertTrue("panel is not null",
            null != new DigitalBooleanLogixActionSwing().getConfigPanel(new JPanel()));
        Assert.assertTrue("panel is not null",
            null != new DigitalBooleanLogixActionSwing().getConfigPanel(new DigitalBooleanLogixAction("IQDB1", null, DigitalBooleanLogixAction.When.Either), new JPanel()));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
