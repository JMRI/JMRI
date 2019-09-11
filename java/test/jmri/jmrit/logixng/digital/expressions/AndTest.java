package jmri.jmrit.logixng.digital.expressions;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.DigitalExpressionBean;

/**
 * Test And
 * 
 * @author Daniel Bergqvist 2018
 */
public class AndTest {

    @Test
    public void testCtor() {
        DigitalExpressionBean t = new And("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        DigitalExpressionBean e1 = new And("IQDE321", null);
        Assert.assertTrue("And".equals(e1.getShortDescription()));
        Assert.assertTrue("And".equals(e1.getLongDescription()));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
