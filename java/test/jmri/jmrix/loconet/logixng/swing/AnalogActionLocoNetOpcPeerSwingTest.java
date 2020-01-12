package jmri.jmrix.loconet.logixng.swing;

import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Bundle
 * 
 * @author Daniel Bergqvist 2018
 */
public class AnalogActionLocoNetOpcPeerSwingTest {

    @Test
    public void testCtor() {
        SwingConfiguratorInterface sci = new AnalogActionLocoNetOpcPeerSwing();
        Assert.assertNotNull("object exists", sci);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
