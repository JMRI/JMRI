package jmri.jmrit.logixng.digital.expressions;

import jmri.jmrit.logixng.Category;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.DigitalExpressionBean;

/**
 * Test Timer
 * 
 * @author Daniel Bergqvist 2018
 */
public class TimerTest {

    @Test
    public void testCtor() {
        DigitalExpressionBean t = new Timer("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        DigitalExpressionBean e1 = new Timer("IQDE321", null);
        Assert.assertTrue("Timer".equals(e1.getShortDescription()));
        Assert.assertTrue("Timer".equals(e1.getLongDescription()));
    }
    
    @Test
    public void testGetCategory() {
        Assert.assertTrue(Category.COMMON.equals(new Timer("IQDE321", null).getCategory()));
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
