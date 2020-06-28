package jmri.jmrit.logixng.digital.actions;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.string.actions.StringActionMemory;
import jmri.jmrit.logixng.string.expressions.StringExpressionMemory;
import jmri.jmrit.logixng.string.expressions.StringExpressionConstant;
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

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    DoStringAction actionDoStringAction;
    
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
        StringExpressionBean childExpression = new StringExpressionConstant("IQSE999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(StringExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Read string E and set string A%n" +
                "   ?s E%n" +
                "      Socket not connected%n" +
                "   !s A%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Read string E and set string A%n" +
                "            ?s E%n" +
                "               Socket not connected%n" +
                "            !s A%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new DoStringAction(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new DoStringAction("IQDA321", null));
    }
    
    @Test
    public void testCtorAndSetup1() {
        DoStringAction expression = new DoStringAction("IQDA321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setStringExpressionSocketSystemName("IQSE52");
        expression.getChild(1).setName("ZH12");
        expression.setStringActionSocketSystemName("IQSA554");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$StringSocket",
                "jmri.jmrit.logixng.string.implementation.DefaultFemaleStringExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.string.implementation.DefaultFemaleStringActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load string expression IQSE52");
        jmri.util.JUnitAppender.assertMessage("cannot load string action IQSA554");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$StringSocket",
                "jmri.jmrit.logixng.string.implementation.DefaultFemaleStringExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.string.implementation.DefaultFemaleStringActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup2() {
        DoStringAction expression = new DoStringAction("IQDA321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setStringExpressionSocketSystemName(null);
        expression.getChild(1).setName("ZH12");
        expression.setStringActionSocketSystemName(null);
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$StringSocket",
                "jmri.jmrit.logixng.string.implementation.DefaultFemaleStringExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.string.implementation.DefaultFemaleStringActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$StringSocket",
                "jmri.jmrit.logixng.string.implementation.DefaultFemaleStringExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.string.implementation.DefaultFemaleStringActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup3() {
        StringExpressionManager m0 = InstanceManager.getDefault(StringExpressionManager.class);
        StringActionManager m1 = InstanceManager.getDefault(StringActionManager.class);
        
        MaleSocket childSocket0 = m0.registerExpression(new StringExpressionMemory("IQSE52", null));
        MaleSocket childSocket1 = m1.registerAction(new StringActionMemory("IQSA554", null));
        
        DoStringAction expression = new DoStringAction("IQDA321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setStringExpressionSocketSystemName("IQSE52");
        expression.getChild(1).setName("ZH12");
        expression.setStringActionSocketSystemName("IQSA554");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$StringSocket",
                "jmri.jmrit.logixng.string.implementation.DefaultFemaleStringExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.string.implementation.DefaultFemaleStringActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        Assert.assertTrue("expression female socket is connected",
                expression.getChild(0).isConnected());
        Assert.assertEquals("child is correct bean",
                childSocket0,
                expression.getChild(0).getConnectedSocket());
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        
        Assert.assertTrue("expression female socket is connected",
                expression.getChild(1).isConnected());
        Assert.assertEquals("child is correct bean",
                childSocket1,
                expression.getChild(1).getConnectedSocket());
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        expression.setup();
        
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 2", 2 == actionDoStringAction.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                actionDoStringAction.getChild(0));
        Assert.assertNotNull("getChild(1) returns a non null value",
                actionDoStringAction.getChild(1));
        
        boolean hasThrown = false;
        try {
            actionDoStringAction.getChild(2);
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
        actionDoStringAction = new DoStringAction("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionDoStringAction);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = actionDoStringAction;
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
