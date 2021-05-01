package jmri.jmrit.logixng.actions;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DoAnalogAction
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
                "Read analog E and set analog A ::: Use default%n" +
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
                "      ! A%n" +
                "         Read analog E and set analog A ::: Use default%n" +
                "            ?~ E%n" +
                "               Socket not connected%n" +
                "            !~ A%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new DoAnalogAction(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new DoAnalogAction("IQDA321", null));
    }
    
    @Test
    public void testCtorAndSetup1() {
        DoAnalogAction expression = new DoAnalogAction("IQDA321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setAnalogExpressionSocketSystemName("IQAE52");
        expression.getChild(1).setName("ZH12");
        expression.setAnalogActionSocketSystemName("IQAA554");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load analog expression IQAE52");
        jmri.util.JUnitAppender.assertMessage("cannot load analog action IQAA554");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup2() {
        DoAnalogAction expression = new DoAnalogAction("IQDA321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setAnalogExpressionSocketSystemName(null);
        expression.getChild(1).setName("ZH12");
        expression.setAnalogActionSocketSystemName(null);
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup3() {
        AnalogExpressionManager m0 = InstanceManager.getDefault(AnalogExpressionManager.class);
        AnalogActionManager m1 = InstanceManager.getDefault(AnalogActionManager.class);
        
        m0.registerExpression(new AnalogExpressionMemory("IQAE52", null));
        m1.registerAction(new AnalogActionMemory("IQAA554", null));
        
        DoAnalogAction expression = new DoAnalogAction("IQDA321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setAnalogExpressionSocketSystemName("IQAE52");
        expression.getChild(1).setName("ZH12");
        expression.setAnalogActionSocketSystemName("IQAA554");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        Assert.assertTrue("expression female socket is connected",
                expression.getChild(0).isConnected());
//        Assert.assertEquals("child is correct bean",
//                childSocket0,
//                expression.getChild(0).getConnectedSocket());
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        
        Assert.assertTrue("expression female socket is connected",
                expression.getChild(1).isConnected());
//        Assert.assertEquals("child is correct bean",
//                childSocket1,
//                expression.getChild(1).getConnectedSocket());
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        expression.setup();
        
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
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
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
        _category = Category.OTHER;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        actionDoAnalogAction = new DoAnalogAction("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionDoAnalogAction);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = actionDoAnalogAction;
        _baseMaleSocket = maleSocket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
