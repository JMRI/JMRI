package jmri.jmrit.logixng.digital.actions;

import jmri.jmrit.logixng.DigitalAction;
import jmri.jmrit.logixng.DigitalActionWithEnableExecution;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Many
 * 
 * @author Daniel Bergqvist 2018
 */
public class HoldAnythingTest extends AbstractDigitalActionTestBase {

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new HoldAnything("IQDA321"));
    }
    
    @Test
    @Override
    public void testSupportsEnableExecution() throws SocketAlreadyConnectedException {
        Assert.assertTrue("supportsEnableExecution() returns correct value",
                ((DigitalAction)_base).supportsEnableExecution());
        Assert.assertTrue("digital action implements DigitalActionWithEnableExecution",
                _base instanceof DigitalActionWithEnableExecution);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        _base = new HoldAnything("IQDA321");
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
