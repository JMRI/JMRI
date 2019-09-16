package jmri.jmrit.logixng.digital.expressions;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.DigitalExpressionBean;

/**
 * Test Or
 * 
 * @author Daniel Bergqvist 2018
 */
public class OrTest {

    @Test
    public void testCtor() {
        DigitalExpressionBean t = new Or("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        DigitalExpressionBean e1 = new Or("IQDE321", null);
        Assert.assertTrue("Or".equals(e1.getShortDescription()));
        Assert.assertTrue("Or".equals(e1.getLongDescription()));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
