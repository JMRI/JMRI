package jmri.jmrit.logixng.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.logixng.DigitalActionBean;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test SwingToolsTest
 * 
 * @author Daniel Bergqvist 2018
 */
public class SwingToolsTest {

    @Test
    public void testSwingTools() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        DigitalActionBean action = new ActionTurnout("IQDA1", null);
        Class actionClass = ActionTurnout.class;
        
        Assert.assertTrue("Class name is correct",
                "jmri.jmrit.logixng.actions.swing.ActionTurnoutSwing"
                        .equals(SwingTools.adapterNameForObject(action)));
        
        Assert.assertTrue("Class name is correct",
                "jmri.jmrit.logixng.actions.swing.ActionTurnoutSwing"
                        .equals(SwingTools.adapterNameForClass(actionClass)));
        
        Assert.assertTrue("Class is correct",
                "jmri.jmrit.logixng.actions.swing.ActionTurnoutSwing"
                        .equals(SwingTools.getSwingConfiguratorForObject(action).getClass().getName()));
        
        Assert.assertTrue("Class is correct",
                "jmri.jmrit.logixng.actions.swing.ActionTurnoutSwing"
                        .equals(SwingTools.getSwingConfiguratorForClass(actionClass).getClass().getName()));
        
        // The class SwingToolsTest does not have a swing configurator
        SwingConfiguratorInterface iface = SwingTools.getSwingConfiguratorForObject(this);
        Assert.assertNull("interface is null", iface);
        jmri.util.JUnitAppender.assertErrorMessage("Cannot load SwingConfiguratorInterface adapter for jmri.jmrit.logixng.swing.SwingToolsTest");
        jmri.util.JUnitAppender.assertErrorMessage("Cannot load SwingConfiguratorInterface for jmri.jmrit.logixng.swing.SwingToolsTest");
        
        // The class SwingToolsTest does not have a swing configurator
        iface = SwingTools.getSwingConfiguratorForClass(this.getClass());
        Assert.assertNull("interface is null", iface);
        jmri.util.JUnitAppender.assertErrorMessage("Cannot load SwingConfiguratorInterface adapter for jmri.jmrit.logixng.swing.SwingToolsTest");
        jmri.util.JUnitAppender.assertErrorMessage("Cannot load SwingConfiguratorInterface for jmri.jmrit.logixng.swing.SwingToolsTest");
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
