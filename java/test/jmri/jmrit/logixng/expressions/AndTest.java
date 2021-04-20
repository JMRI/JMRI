package jmri.jmrit.logixng.expressions;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;

/**
 * Test And
 * 
 * @author Daniel Bergqvist 2018
 */
public class AndTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    
    
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
                "And ::: Log error%n" +
                "   ? E1%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Log error%n" +
                "            ? If%n" +
                "               And ::: Log error%n" +
                "                  ? E1%n" +
                "                     Socket not connected%n" +
                "            ! Then%n" +
                "               Socket not connected%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new And(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() throws SocketAlreadyConnectedException {
        int count = _base.getChildCount();
        for (int i=0; i < count; i++) {
            if (!_base.getChild(i).isConnected()) {
                _base.getChild(i).connect(getConnectableChild());
            }
        }
        return true;
    }
    
    @Test
    public void testCtor() {
        And expression2;
        
        expression2 = new And("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "And", expression2.getLongDescription());
        
        expression2 = new And("IQDE321", "My expression");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
        Assert.assertEquals("String matches", "And", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new And("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new And("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    // Test action when at least one child socket is not connected
    @Test
    public void testCtorAndSetup1() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);
        
        List<MaleSocket> maleSockets = new ArrayList<>();
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE52", null)));
        maleSockets.add(null);  // This is null by purpose
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE554", null)));
        maleSockets.add(null);  // This is null by purpose
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE3", null)));
        
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQDE52"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("ZH12", null));   // This is null by purpose
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Hello", "IQDE554"));
        // IQDE61232 doesn't exist by purpose
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("SomethingElse", "IQDE61232"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Yes123", "IQDE3"));
        
        And expression = new And("IQDE321", null, actionSystemNames);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 5 female sockets", 5, expression.getChildCount());
        
        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            Assert.assertEquals("expression female socket name is "+entry.getKey(),
                    entry.getKey(), expression.getChild(i).getName());
            Assert.assertEquals("expression female socket is of correct class",
//                    "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                    expression.getChild(i).getClass().getName());
            Assert.assertFalse("expression female socket is not connected",
                    expression.getChild(i).isConnected());
        }
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE61232");
        
        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            Assert.assertEquals("expression female socket name is "+entry.getKey(),
                    entry.getKey(), expression.getChild(i).getName());
            
            if (maleSockets.get(i) != null) {
                Assert.assertTrue("expression female socket is connected",
                        expression.getChild(i).isConnected());
//                Assert.assertEquals("child is correct bean",
//                        maleSockets.get(i),
//                        expression.getChild(i).getConnectedSocket());
            } else {
                Assert.assertFalse("expression female socket is not connected",
                        expression.getChild(i).isConnected());
            }
        }
        
        Assert.assertEquals("expression has 5 female sockets", 5, expression.getChildCount());
    }
    
    // Test action when at least one child socket is not connected.
    // This should never happen, but test it anyway.
    @Test
    public void testCtorAndSetup2() {
        DigitalExpressionManager m = InstanceManager.getDefault(DigitalExpressionManager.class);
        
        List<MaleSocket> maleSockets = new ArrayList<>();
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE52", null)));
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE99", null)));
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE554", null)));
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE61232", null)));
        maleSockets.add(m.registerExpression(new ExpressionMemory("IQDE3", null)));
        
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQDE52"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("ZH12", "IQDE99"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Hello", "IQDE554"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("SomethingElse", "IQDE61232"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Yes123", "IQDE3"));
        
        And expression = new And("IQDE321", null, actionSystemNames);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 5 female sockets", 5, expression.getChildCount());
        
        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            Assert.assertEquals("expression female socket name is "+entry.getKey(),
                    entry.getKey(), expression.getChild(i).getName());
            Assert.assertEquals("expression female socket is of correct class",
//                    "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                    expression.getChild(i).getClass().getName());
            Assert.assertFalse("expression female socket is not connected",
                    expression.getChild(i).isConnected());
        }
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            Assert.assertEquals("expression female socket name is "+entry.getKey(),
                    entry.getKey(), expression.getChild(i).getName());
            
            if (maleSockets.get(i) != null) {
                Assert.assertTrue("expression female socket is connected",
                        expression.getChild(i).isConnected());
//                Assert.assertEquals("child is correct bean",
//                        maleSockets.get(i),
//                        expression.getChild(i).getConnectedSocket());
            } else {
                Assert.assertFalse("expression female socket is not connected",
                        expression.getChild(i).isConnected());
            }
        }
        
        // Since all the sockets are connected, a new socket must have been created.
        Assert.assertEquals("expression has 6 female sockets", 6, expression.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        expression.setup();
        
        Assert.assertEquals("expression has 6 female sockets", 6, expression.getChildCount());
    }
    
    // Test calling setActionSystemNames() twice
    @Test
    public void testCtorAndSetup3() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException {
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQDE52"));
        
        And expression = new And("IQDE321", null, actionSystemNames);
        
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
    
    @Test
    public void testGetChild() throws SocketAlreadyConnectedException {
        And expression2 = new And("IQDE321", null);
        
        for (int i=0; i < 3; i++) {
            Assert.assertTrue("getChildCount() returns "+i, i+1 == expression2.getChildCount());
            
            Assert.assertNotNull("getChild(0) returns a non null value",
                    expression2.getChild(0));
            
            assertIndexOutOfBoundsException(expression2::getChild, i+1, i+1);
            
            // Connect a new child expression
            True expr = new True("IQDE"+i, null);
            MaleSocket maleSocket =
                    InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expr);
            expression2.getChild(i).connect(maleSocket);
        }
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.COMMON == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertFalse("is external", _base.isExternal());
    }
    
    // Test the methods connected(FemaleSocket) and getExpressionSystemName(int)
    @Test
    public void testConnected_getExpressionSystemName() throws SocketAlreadyConnectedException {
        And expression = new And("IQDE121", null);
        
        ExpressionMemory stringExpressionMemory = new ExpressionMemory("IQDE122", null);
        MaleSocket maleSAMSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(stringExpressionMemory);
        
        Assert.assertEquals("Num children is correct", 1, expression.getChildCount());
        
        // Test connect and disconnect
        expression.getChild(0).connect(maleSAMSocket);
        Assert.assertEquals("Num children is correct", 2, expression.getChildCount());
        Assert.assertEquals("getExpressionSystemName(0) is correct", "IQDE122", expression.getExpressionSystemName(0));
        Assert.assertNull("getExpressionSystemName(1) is null", expression.getExpressionSystemName(1));
        expression.getChild(0).disconnect();
        Assert.assertEquals("Num children is correct", 2, expression.getChildCount());
        Assert.assertNull("getExpressionSystemName(0) is null", expression.getExpressionSystemName(0));
        Assert.assertNull("getExpressionSystemName(1) is null", expression.getExpressionSystemName(1));
        
        expression.getChild(1).connect(maleSAMSocket);
        Assert.assertEquals("Num children is correct", 2, expression.getChildCount());
        Assert.assertNull("getExpressionSystemName(0) is null", expression.getExpressionSystemName(0));
        Assert.assertEquals("getExpressionSystemName(1) is correct", "IQDE122", expression.getExpressionSystemName(1));
        expression.getChild(0).disconnect();    // Test removing child with the wrong index.
        Assert.assertEquals("Num children is correct", 2, expression.getChildCount());
        Assert.assertNull("getExpressionSystemName(0) is null", expression.getExpressionSystemName(0));
        Assert.assertEquals("getExpressionSystemName(1) is correct", "IQDE122", expression.getExpressionSystemName(1));
        expression.getChild(1).disconnect();
        Assert.assertEquals("Num children is correct", 2, expression.getChildCount());
        Assert.assertNull("getExpressionSystemName(0) is null", expression.getExpressionSystemName(0));
        Assert.assertNull("getExpressionSystemName(1) is null", expression.getExpressionSystemName(1));
    }
    
    @Test
    public void testDescription() {
        And e1 = new And("IQDE321", null);
        Assert.assertTrue("And".equals(e1.getShortDescription()));
        Assert.assertTrue("And".equals(e1.getLongDescription()));
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
        
        _category = Category.COMMON;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        DigitalExpressionBean expression = new And("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expression;
        _baseMaleSocket = maleSocket2;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
