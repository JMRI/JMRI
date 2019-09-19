package jmri.jmrit.logixng.digital.actions;

import jmri.InstanceManager;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor;
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

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    
    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Throttle%n" +
                "   ?~ E1%n" +
                "      Socket not connected%n" +
                "   ?~ E2%n" +
                "      Socket not connected%n" +
                "   ? E3%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Throttle%n" +
                "            ?~ E1%n" +
                "               Socket not connected%n" +
                "            ?~ E2%n" +
                "               Socket not connected%n" +
                "            ? E3%n" +
                "               Socket not connected%n");
    }
    
    @Test
    public void testCtor() {
        ActionThrottle action2;
        
        action2 = new ActionThrottle("IQDA321", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Throttle", action2.getLongDescription());
        
        action2 = new ActionThrottle("IQDA321", "My throttle");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My throttle", action2.getUserName());
        Assert.assertEquals("String matches", "Throttle", action2.getLongDescription());
        
        // Test template
        action2 = (ActionThrottle)_base.getNewObjectBasedOnTemplate();
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username is null", action2.getUserName());
//        Assert.assertTrue("Username matches", "My throttle".equals(expression2.getUserName()));
        Assert.assertEquals("String matches", "Throttle", action2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ActionThrottle("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ActionThrottle("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.ITEM == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    public void testShortDescription() {
        Assert.assertEquals("String matches", "Throttle", _base.getShortDescription());
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Throttle", _base.getLongDescription());
    }
    
    @Test
    public void testChild() {
        MaleSocket analogExpressionMaleSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(new AnalogExpressionConstant("IQAE1", null));
        MaleSocket digitalExpressionMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(new ExpressionSensor("IQDE1", null));
        
        Assert.assertEquals("Num children is correct", 3, _base.getChildCount());
        
        // Socket 0 is loco address
        Assert.assertTrue("Child 0 supports analog male socket",
                _base.getChild(0).isCompatible(analogExpressionMaleSocket));
        // Socket 1 is loco speed
        Assert.assertTrue("Child 1 supports analog male socket",
                _base.getChild(1).isCompatible(analogExpressionMaleSocket));
        // Socket 2 is loco direction
        Assert.assertTrue("Child 2 supports analog male socket",
                _base.getChild(2).isCompatible(digitalExpressionMaleSocket));
        
        boolean hasThrown = false;
        try {
            _base.getChild(3);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", "index has invalid value: 3".equals(ex.getMessage()));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
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
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNG();
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        ActionThrottle action = new ActionThrottle("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = action;
        _baseMaleSocket = maleSocket;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
