package jmri.jmrit.logixng.digital.expressions.configureswing;

import java.awt.GraphicsEnvironment;
import javax.swing.JPanel;
import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.digital.expressions.Timer;
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
public class TimerSwingTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        TimerSwing t = new TimerSwing();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        TimerSwing t = new TimerSwing();
        JPanel panel = t.getConfigPanel();
        Assert.assertNotNull("exists",panel);
    }
    
    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Assert.assertTrue("panel is not null",
            null != new TimerSwing().getConfigPanel());
        Assert.assertTrue("panel is not null",
            null != new TimerSwing().getConfigPanel(new Timer("IQDE1", null)));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
