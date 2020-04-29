package jmri.jmrit.logixng.digital.actions;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.AnalogExpressionBean;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.digital.expressions.True;
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

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    DoAnalogAction actionDoAnalogAction;
    
    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }
    
    @Override
    public MaleSocket getConnectableChild() {
        AnalogExpressionBean childExpression = new AnalogExpressionConstant("IQAE999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Read analog E and set analog A%n" +
                "   ?~ E%n" +
                "      Socket not connected%n" +
                "   !~ A%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Read analog E and set analog A%n" +
                "            ?~ E%n" +
                "               Socket not connected%n" +
                "            !~ A%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new DoAnalogAction(systemName, null);
    }
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new DoAnalogAction("IQDA321", null));
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 2", 2 == actionDoAnalogAction.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                actionDoAnalogAction.getChild(0));
        Assert.assertNotNull("getChild(1) returns a non null value",
                actionDoAnalogAction.getChild(1));
        
        boolean hasThrown = false;
        try {
            actionDoAnalogAction.getChild(2);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 2", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testCategory() {
        Assert.assertEquals("Category matches", Category.COMMON, _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertFalse("is external", _base.isExternal());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        _category = Category.OTHER;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setEnabled(true);
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        actionDoAnalogAction = new DoAnalogAction("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionDoAnalogAction);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = actionDoAnalogAction;
        _baseMaleSocket = maleSocket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
