package jmri.jmrit.logixng.actions.swing;

import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;

import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.actions.DigitalBooleanOnChange;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionLight
 * 
 * @author Daniel Bergqvist 2018
 */
public class DigitalBooleanOnChangeSwingTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        DigitalBooleanOnChangeSwing t = new DigitalBooleanOnChangeSwing();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Assert.assertTrue("panel is not null",
            null != new DigitalBooleanOnChangeSwing().getConfigPanel(new JPanel()));
        Assert.assertTrue("panel is not null",
            null != new DigitalBooleanOnChangeSwing().getConfigPanel(new DigitalBooleanOnChange("IQDB1", null, DigitalBooleanOnChange.Trigger.CHANGE), new JPanel()));
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
        JUnitUtil.tearDown();
    }
    
}
