package jmri.jmrit.logixng;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DigitalExpressionBean
 * 
 * @author Daniel Bergqvist 2018
 */
public class DigitalExpressionTest {

    @Test
    public void testEnum() {
        Assert.assertTrue("TRUE".equals(DigitalExpressionBean.TriggerCondition.TRUE.name()));
        Assert.assertTrue("FALSE".equals(DigitalExpressionBean.TriggerCondition.FALSE.name()));
        Assert.assertTrue("CHANGE".equals(DigitalExpressionBean.TriggerCondition.CHANGE.name()));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
