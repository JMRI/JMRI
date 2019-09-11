package jmri.jmrit.logixng.digital.actions;

import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.LogixNG;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionMany
 * 
 * @author Daniel Bergqvist 2018
 */
public class DoAnalogActionTest extends AbstractDigitalActionTestBase {

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
                "Read analog E1 and set analog A1%n" +
                "   ?~ E1%n" +
                "   !~ A1%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "Read analog E1 and set analog A1%n" +
                "   ?~ E1%n" +
                "   !~ A1%n");
    }
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new DoAnalogAction("IQDA321", null));
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
        JUnitUtil.initAnalogExpressionManager();
        JUnitUtil.initAnalogActionManager();
        _base = new DoAnalogAction("IQDA321", null);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
