package jmri.jmrit.logixng.digital.actions.configureswing;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.digital.actions.HoldAnything;
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
public class HoldAnythingSwingTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        HoldAnythingSwing t = new HoldAnythingSwing();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Assert.assertTrue("panel is not null",
            null != new HoldAnythingSwing().getConfigPanel());
        Assert.assertTrue("panel is not null",
            null != new HoldAnythingSwing().getConfigPanel(new HoldAnything("IQDA1", null)));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
