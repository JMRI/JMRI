package jmri.jmrit.logixng.digital.expressions.swing;

import java.awt.GraphicsEnvironment;
import javax.swing.JPanel;
import jmri.NamedBean;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.digital.expressions.ResetOnTrue;
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
public class ResetOnTrueSwingTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        ResetOnTrueSwing t = new ResetOnTrueSwing();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        ResetOnTrueSwing t = new ResetOnTrueSwing();
        JPanel panel = t.getConfigPanel(new JPanel());
        Assert.assertNotNull("exists",panel);
    }
    
    @Test
    public void testCreatePanel() throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Assert.assertTrue("panel is not null",
            null != new ResetOnTrueSwing().getConfigPanel(new JPanel()));
        Assert.assertTrue("panel is not null",
            null != new ResetOnTrueSwing().getConfigPanel(new ResetOnTrue("IQDE1", null), new JPanel()));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
