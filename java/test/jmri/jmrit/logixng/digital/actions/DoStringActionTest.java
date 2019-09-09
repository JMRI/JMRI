package jmri.jmrit.logixng.digital.actions;

import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.LogixNG;
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
                "Read string E1 and set string A1%n" +
                "   ?s E1%n" +
                "   !s A1%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "Read string E1 and set string A1%n" +
                "   ?s E1%n" +
                "   !s A1%n");
    }
    
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
