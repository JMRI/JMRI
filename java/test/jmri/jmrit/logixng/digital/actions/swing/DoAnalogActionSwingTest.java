package jmri.jmrit.logixng.digital.actions.swing;

import java.awt.GraphicsEnvironment;
import javax.swing.JPanel;
import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.digital.actions.DoAnalogAction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DoAnalogAction
 * 
 * @author Daniel Bergqvist 2018
 */
public class DoAnalogActionSwingTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        DoAnalogActionSwing t = new DoAnalogActionSwing();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Assert.assertTrue("panel is not null",
            null != new DoAnalogActionSwing().getConfigPanel(new JPanel()));
        Assert.assertTrue("panel is not null",
            null != new DoAnalogActionSwing().getConfigPanel(new DoAnalogAction("IQDA1", null), new JPanel()));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
