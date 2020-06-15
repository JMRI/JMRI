package jmri.jmrit.logixng.digital.expressions;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrit.logixng.digital.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.digital.actions.IfThenElse;

/**
 * Test TriggerOnce
 * 
 * @author Daniel Bergqvist 2018
 */
public class TriggerOnceTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private TriggerOnce expressionTriggerOnce;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    
    
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
        DigitalExpressionBean childExpression = new True("IQDE"+Integer.toString(beanID++), null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Trigger once%n" +
                "   ? E%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         If E then A1 else A2%n" +
                "            ? E%n" +
                "               Trigger once%n" +
                "                  ? E%n" +
                "                     Socket not connected%n" +
                "            ! A1%n" +
                "               Set the atomic boolean to true%n" +
                "            ! A2%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new TriggerOnce(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor()
            throws NamedBean.BadUserNameException,
                    NamedBean.BadSystemNameException,
                    SocketAlreadyConnectedException {
        
        TriggerOnce expression2;
        
        expression2 = new TriggerOnce("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Trigger once", expression2.getLongDescription());
        
        expression2 = new TriggerOnce("IQDE321", "My sensor");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My sensor", expression2.getUserName());
        Assert.assertEquals("String matches", "Trigger once", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new TriggerOnce("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new TriggerOnce("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    // Test action when at least one child socket is not connected
    @Test
    public void testCtorAndSetup1() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);
        
//        MaleSocket childSocket = m.registerExpression(new ExpressionMemory("IQDE52", null));
        
        TriggerOnce expression = new TriggerOnce("IQDE321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setChildSocketSystemName("IQDE52");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE52");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
//        Assert.assertTrue("expression female socket is connected",
//                expression.getChild(0).isConnected());
//        Assert.assertEquals("child is correct bean",
//                childSocket,
//                expression.getChild(0).getConnectedSocket());
//        } else {
//            Assert.assertFalse("expression female socket is not connected",
//                    expression.getChild(i).isConnected());
//        }
        
        Assert.assertEquals("expression has 1 female sockets", 1, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup2() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);
        
        MaleSocket childSocket = m.registerExpression(new ExpressionMemory("IQDE52", null));
        
        TriggerOnce expression = new TriggerOnce("IQDE321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setChildSocketSystemName("IQDE52");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertTrue("expression female socket is connected",
                expression.getChild(0).isConnected());
        Assert.assertEquals("child is correct bean",
                childSocket,
                expression.getChild(0).getConnectedSocket());
        
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        expression.setup();
        
        Assert.assertEquals("expression has 1 female socket", 1, expression.getChildCount());
    }
/*    
    // Test calling setActionSystemNames() twice
    @Test
    public void testCtorAndSetup3() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException {
        TriggerOnce expression = new TriggerOnce("IQDE321", null);
        expression.getChild(0).setName("XYZ123");
        expression.setChildSocketSystemName("IQDE52");
        
        java.lang.reflect.Method method =
                expression.getClass().getDeclaredMethod("setExpressionSystemNames", new Class<?>[]{List.class});
        method.setAccessible(true);
        
        boolean hasThrown = false;
        try {
            method.invoke(expression, new Object[]{null});
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                hasThrown = true;
                Assert.assertEquals("Exception message is correct",
                        "expression system names cannot be set more than once",
                        e.getCause().getMessage());
            }
        }
        Assert.assertTrue("Exception thrown", hasThrown);
    }
*/    
    private static int beanID = 901;
    
    @Test
    public void testReset() throws SocketAlreadyConnectedException, JmriException {
        TriggerOnce a = (TriggerOnce)_base;
        AtomicBoolean ab = new AtomicBoolean(false);
        
        DigitalExpressionBean expr = new True("IQDE"+Integer.toString(beanID++), null) {
            @Override
            public void reset() {
                ab.set(true);
            }
        };
        
        a.getChild(0).disconnect();
        a.getChild(0).connect(
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(expr));
        
        a.reset();
        Assert.assertTrue("Child is reset", ab.get());
        
        // Test that reset will run the expression again
        // For this test to work, make sure the child always return true
        Assert.assertTrue("Expression returns True", a.evaluate());
        Assert.assertFalse("Expression returns False", a.evaluate());
        Assert.assertFalse("Expression returns False", a.evaluate());
        Assert.assertFalse("Expression returns False", a.evaluate());
        Assert.assertFalse("Expression returns False", a.evaluate());
        Assert.assertFalse("Expression returns False", a.evaluate());
        a.reset();
        Assert.assertTrue("Expression returns True", a.evaluate());
        Assert.assertFalse("Expression returns False", a.evaluate());
        Assert.assertFalse("Expression returns False", a.evaluate());
        Assert.assertFalse("Expression returns False", a.evaluate());
        Assert.assertFalse("Expression returns False", a.evaluate());
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 1", 1 == expressionTriggerOnce.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                expressionTriggerOnce.getChild(0));
        
        boolean hasThrown = false;
        try {
            expressionTriggerOnce.getChild(1);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 1", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.OTHER == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertFalse("is external", _base.isExternal());
    }
    
    @Test
    public void testDescription()
            throws NamedBean.BadUserNameException,
                    NamedBean.BadSystemNameException,
                    SocketAlreadyConnectedException {
        DigitalExpressionBean e1 = new TriggerOnce("IQDE321", null);
        Assert.assertTrue("Trigger once".equals(e1.getShortDescription()));
        Assert.assertTrue("Trigger once".equals(e1.getLongDescription()));
    }
    
    @Test
    @Override
    public void testEnableAndEvaluate() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
    }
    
    @Test
    @Override
    public void testDebugConfig() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
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
        conditionalNG.setRunOnGUIDelayed(false);
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        expressionTriggerOnce = new TriggerOnce("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTriggerOnce);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expressionTriggerOnce;
        _baseMaleSocket = maleSocket2;
        
        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
