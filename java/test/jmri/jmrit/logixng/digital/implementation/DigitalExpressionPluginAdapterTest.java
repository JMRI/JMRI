package jmri.jmrit.logixng.digital.implementation;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.digital.expressions.And;
import jmri.jmrit.logixng.DigitalExpressionBean;

/**
 * Test ExpressionTimer
 * 
 * @author Daniel Bergqvist 2018
 */
public class DigitalExpressionPluginAdapterTest {

    @Test
    public void testCtor() {
        DigitalExpressionBean expression = new And("IQDE321", null);
        Assert.assertNotNull("exists", new DigitalExpressionPluginAdapter("IQDE1", expression));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
