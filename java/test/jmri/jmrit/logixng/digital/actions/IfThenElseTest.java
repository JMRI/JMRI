package jmri.jmrit.logixng.digital.actions;

import jmri.jmrit.logixng.DigitalAction;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.DigitalActionBean;
import jmri.jmrit.logixng.DigitalActionWithEnableExecution;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;

/**
 * Test IfThenElse
 * 
 * @author Daniel Bergqvist 2018
 */
public class IfThenElseTest extends AbstractDigitalActionTestBase {

    @Test
    public void testCtor() {
        DigitalActionBean t = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        Assert.assertNotNull("exists",t);
        t = new IfThenElse("IQDA321", null, IfThenElse.Type.CONTINOUS_ACTION);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testToString() {
        DigitalActionBean a1 = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        Assert.assertEquals("strings are equal", "If then else", a1.getShortDescription());
        DigitalActionBean a2 = new IfThenElse("IQDA321", null, IfThenElse.Type.CONTINOUS_ACTION);
        Assert.assertEquals("strings are equal", "If E then A1 else A2", a2.getLongDescription());
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
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        _base = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
