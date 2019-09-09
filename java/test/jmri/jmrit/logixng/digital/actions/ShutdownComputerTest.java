package jmri.jmrit.logixng.digital.actions;

import jmri.jmrit.logixng.LogixNG;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionTurnout
 * 
 * @author Daniel Bergqvist 2018
 */
public class ShutdownComputerTest extends AbstractDigitalActionTestBase {

    @Override
    public LogixNG getLogixNG() {
        return null;
    }
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new ShutdownComputer("IQDA321", null, 0));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        _base = new ShutdownComputer("IQDA321", null, 0);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
