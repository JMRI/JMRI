package jmri.jmrit.logixng.digital.boolean_actions.configureswing;

import java.awt.GraphicsEnvironment;
import javax.swing.JPanel;
import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.digital.boolean_actions.OnChange;
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
public class OnChangeActionSwingTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        OnChangeSwing t = new OnChangeSwing();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Assert.assertTrue("panel is not null",
            null != new OnChangeSwing().getConfigPanel(new JPanel()));
        Assert.assertTrue("panel is not null",
            null != new OnChangeSwing().getConfigPanel(new OnChange("IQDB1", null, OnChange.ChangeType.CHANGE), new JPanel()));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
