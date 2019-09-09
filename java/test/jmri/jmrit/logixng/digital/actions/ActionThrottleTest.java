package jmri.jmrit.logixng.digital.actions;

import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.LogixNG;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionTimer
 * 
 * @author Daniel Bergqvist 2019
 */
public class ActionThrottleTest extends AbstractDigitalActionTestBase {

    @Override
    public ConditionalNG getConditionalNG() {
        return null;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return null;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Throttle%n" +
                "   ?~ E1%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "Throttle%n" +
                "   ?~ E1%n");
    }
    
    @Test
    public void testCtor() {
        ActionThrottle t = new ActionThrottle("IQDA321");
        Assert.assertNotNull("exists",t);
        t = new ActionThrottle("IQDA321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testToString() {
        ActionThrottle a1 = new ActionThrottle("IQDA321", null);
        Assert.assertEquals("strings are equal", "Throttle", a1.getShortDescription());
        ActionThrottle a2 = new ActionThrottle("IQDA321", null);
        Assert.assertEquals("strings are equal", "Throttle", a2.getLongDescription());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        _base = new ActionThrottle("IQDA321");
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
