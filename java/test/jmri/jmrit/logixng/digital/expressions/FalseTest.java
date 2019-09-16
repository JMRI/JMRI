package jmri.jmrit.logixng.digital.expressions;

import java.util.concurrent.atomic.AtomicBoolean;
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
public class FalseTest {

    @Test
    public void testCtor() {
        DigitalExpressionBean t = new False("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        DigitalExpressionBean e1 = new False("IQDE321", null);
        Assert.assertTrue("Always false".equals(e1.getShortDescription()));
        Assert.assertTrue("Always false".equals(e1.getLongDescription()));
    }
    
    @Test
    public void testExpression() {
        DigitalExpressionBean t = new False("IQDE321", null);
        Assert.assertFalse("Expression is false",t.evaluate());
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
