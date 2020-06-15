package jmri.jmrit.logixng.digital.expressions;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrit.logixng.digital.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.jmrit.logixng.string.expressions.StringExpressionMemory;

/**
 * Test ResetOnTrue
 * 
 * @author Daniel Bergqvist 2018
 */
public class ResetOnTrueTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ResetOnTrue expressionResetOnTrue;
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
        DigitalExpressionBean childExpression = new True("IQDE999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Reset on true%n" +
                "   ? E1%n" +
                "      Socket not connected%n" +
                "   ? E2%n" +
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
                "               Reset on true%n" +
                "                  ? E1%n" +
                "                     Socket not connected%n" +
                "                  ? E2%n" +
                "                     Socket not connected%n" +
                "            ! A1%n" +
                "               Set the atomic boolean to true%n" +
                "            ! A2%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ResetOnTrue(systemName, null);
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
        
        ResetOnTrue expression2;
        
        expression2 = new ResetOnTrue("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Reset on true", expression2.getLongDescription());
        
        expression2 = new ResetOnTrue("IQDE321", "My expression");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
        Assert.assertEquals("String matches", "Reset on true", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ResetOnTrue("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ResetOnTrue("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    // Test action when at least one child socket is not connected
    @Test
    public void testCtorAndSetup1() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);
        
//        MaleSocket childSocket0 = m.registerExpression(new ExpressionMemory("IQDE52", null));
//        MaleSocket childSocket1 = m.registerExpression(new ExpressionMemory("IQDE554", null));
        
        ResetOnTrue expression = new ResetOnTrue("IQDE321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setPrimaryExpressionSocketSystemName("IQDE52");
        expression.getChild(1).setName("ZH12");
        expression.setSecondaryExpressionSocketSystemName("IQDE554");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE52");
        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE554");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());
    }
    
    @Test
    public void testCtorAndSetup2() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);
        
        MaleSocket childSocket0 = m.registerExpression(new ExpressionMemory("IQDE52", null));
        MaleSocket childSocket1 = m.registerExpression(new ExpressionMemory("IQDE554", null));
        
        ResetOnTrue expression = new ResetOnTrue("IQDE321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 2 female sockets", 2, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setPrimaryExpressionSocketSystemName("IQDE52");
        expression.getChild(1).setName("ZH12");
        expression.setSecondaryExpressionSocketSystemName("IQDE554");
        
        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());
        
        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
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
    }
/*    
    // Test calling setActionSystemNames() twice
    @Test
    public void testCtorAndSetup3() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException {
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQDE52"));
        
        ResetOnTrue expression = new ResetOnTrue("IQDE321", null, actionSystemNames);
        
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
    public void testReset() throws SocketAlreadyConnectedException {
        ResetOnTrue a = (ResetOnTrue)_base;
        AtomicBoolean ab1 = new AtomicBoolean(false);
        AtomicBoolean ab2 = new AtomicBoolean(false);
        
        DigitalExpressionBean expr1 = new True("IQDE"+Integer.toString(beanID++), null) {
            @Override
            public void reset() {
                ab1.set(true);
            }
        };
        
        DigitalExpressionBean expr2 = new True("IQDE"+Integer.toString(beanID++), null) {
            @Override
            public void reset() {
                ab2.set(true);
            }
        };
        
        a.getChild(0).disconnect();
        a.getChild(0).connect(
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(expr1));
        
        a.getChild(1).disconnect();
        a.getChild(1).connect(
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(expr2));
        
        a.reset();
        Assert.assertTrue("Child 0 is reset", ab1.get());
        Assert.assertTrue("Child 1 is reset", ab2.get());
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 2", 2 == expressionResetOnTrue.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                expressionResetOnTrue.getChild(0));
        Assert.assertNotNull("getChild(1) returns a non null value",
                expressionResetOnTrue.getChild(1));
        
        boolean hasThrown = false;
        try {
            expressionResetOnTrue.getChild(2);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 2", ex.getMessage());
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
    public void testDescription() throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, NamedBean.BadSystemNameException, SocketAlreadyConnectedException {
        DigitalExpressionBean e1 = new ResetOnTrue("IQDE353", null);
        Assert.assertTrue("Reset on true".equals(e1.getShortDescription()));
        Assert.assertTrue("Reset on true".equals(e1.getLongDescription()));
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
        
        expressionResetOnTrue = new ResetOnTrue("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionResetOnTrue);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expressionResetOnTrue;
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
