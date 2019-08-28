package jmri.jmrit.logixng.digital.actions;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DoStringAction
 * 
 * @author Daniel Bergqvist 2019
 */
public class DoStringActionTest extends AbstractDigitalActionTestBase {

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new DoStringAction("IQDA321"));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        JUnitUtil.initDigitalExpressionManager();
        JUnitUtil.initDigitalActionManager();
        JUnitUtil.initStringExpressionManager();
        JUnitUtil.initStringActionManager();
        _base = new DoStringAction("IQDA321");
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
