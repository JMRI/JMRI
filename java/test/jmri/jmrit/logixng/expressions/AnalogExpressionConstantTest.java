package jmri.jmrit.logixng.expressions;

import java.util.Locale;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AnalogActionMemory;
import jmri.jmrit.logixng.actions.DoAnalogAction;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test AnalogExpressionConstant
 * 
 * @author Daniel Bergqvist 2018
 */
public class AnalogExpressionConstantTest extends AbstractAnalogExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private AnalogExpressionConstant expressionConstant;
    private Memory _memoryOut;
    private AnalogActionMemory actionMemory;
    
    
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
        return null;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("Get analog constant 10.2 ::: Use default%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Read analog E and set analog A ::: Use default%n" +
                "            ?~ E%n" +
                "               Get analog constant 10.2 ::: Use default%n" +
                "            !~ A%n" +
                "               Set memory IM2 ::: Use default%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new AnalogExpressionConstant(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        AnalogExpressionConstant expression2;
        
        expression2 = new AnalogExpressionConstant("IQAE11", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", null, expression2.getUserName());
        Assert.assertEquals("String matches", "Get analog constant 0", expression2.getLongDescription(Locale.ENGLISH));
        
        expression2 = new AnalogExpressionConstant("IQAE11", "My constant value");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My constant value", expression2.getUserName());
        Assert.assertEquals("String matches", "Get analog constant 0", expression2.getLongDescription(Locale.ENGLISH));
        
        expression2 = new AnalogExpressionConstant("IQAE11", null);
        expression2.setValue(12.34);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", null, expression2.getUserName());
        Assert.assertEquals("String matches", "Get analog constant 12.34", expression2.getLongDescription(Locale.ENGLISH));
        
        expression2 = new AnalogExpressionConstant("IQAE11", "My constant");
        expression2.setValue(98.76);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My constant", expression2.getUserName());
        Assert.assertEquals("String matches", "Get analog constant 98.76", expression2.getLongDescription(Locale.ENGLISH));
        
        // Call setup(). It doesn't do anything, but we call it for coverage
        expression2.setup();
        
        boolean thrown = false;
        try {
            // Illegal system name
            new AnalogExpressionConstant("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new AnalogExpressionConstant("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testSetValueWithListenersRegistered() {
        boolean exceptionThrown = false;
        try {
            AnalogExpressionConstant expression = (AnalogExpressionConstant)_base;
            expression.registerListeners();
            expression.setValue(1.2);
        } catch (RuntimeException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        JUnitAppender.assertErrorMessage("setValue must not be called when listeners are registered");
    }
    
    @Test
    public void testEvaluate() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        AnalogExpressionConstant expression = (AnalogExpressionConstant)_base;
        expression.setValue(0.0d);
        Assert.assertTrue("Evaluate matches", 0.0d == expression.evaluate());
        expression.setValue(10.0d);
        Assert.assertTrue("Evaluate matches", 10.0d == expression.evaluate());
    }
    
    @Test
    public void testEvaluateAndAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        
        // Set the memory
        _memoryOut.setValue(0.0);
        // The double should be 0.0
        Assert.assertTrue("memory is 0.0", 0.0 == (Double)_memoryOut.getValue());
        // Execute the logixNG
        logixNG.execute();
        // The action is executed so the double should be 10.2
        Assert.assertTrue("memory is 10.2", 10.2 == (Double)_memoryOut.getValue());
        
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        // Set the value of the constant.
        expressionConstant.setValue(1.0);
        // Enable the conditionalNG
        conditionalNG.setEnabled(true);
        // Execute the logixNG
        logixNG.execute();
        conditionalNG.setEnabled(false);
        // The action is executed so the double should be 1.0
        Assert.assertTrue("memory is 1.0", 1.0 == (Double)_memoryOut.getValue());
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.ITEM == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertFalse("is external", _base.isExternal());
    }
    
    @Test
    public void testShortDescription() {
        Assert.assertEquals("String matches", "Get analog constant", _base.getShortDescription(Locale.ENGLISH));
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Get analog constant 10.2", _base.getLongDescription(Locale.ENGLISH));
    }
    
    @Test
    public void testChild() {
        Assert.assertTrue("Num children is zero", 0 == _base.getChildCount());
        boolean hasThrown = false;
        try {
            _base.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", "Not supported.".equals(ex.getMessage()));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
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
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLogixNGManager();
        
        expressionConstant = new AnalogExpressionConstant("IQAE321", null);
        expressionConstant.setValue(10.2);
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        
        logixNG.addConditionalNG(conditionalNG);
        
        DigitalActionBean actionDoAnalog =
                new DoAnalogAction(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        MaleSocket socketDoAnalog =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionDoAnalog);
        conditionalNG.getChild(0).connect(socketDoAnalog);
        
        MaleSocket socketExpression =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(expressionConstant);
        socketDoAnalog.getChild(0).connect(socketExpression);
        
        _memoryOut = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        _memoryOut.setValue(0.0);
        actionMemory = new AnalogActionMemory("IQAA1", null);
        actionMemory.setMemory(_memoryOut);
        MaleSocket socketAction =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(actionMemory);
        socketDoAnalog.getChild(1).connect(socketAction);
        
        _base = expressionConstant;
        _baseMaleSocket = socketExpression;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
//        JUnitAppender.clearBacklog();
        _base.dispose();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
