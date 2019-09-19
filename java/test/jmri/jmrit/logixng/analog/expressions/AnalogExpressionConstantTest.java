package jmri.jmrit.logixng.analog.expressions;

import java.util.Locale;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionBean;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.analog.actions.AnalogActionMemory;
import jmri.jmrit.logixng.digital.actions.DoAnalogAction;
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
    public String getExpectedPrintedTree() {
        return String.format("Get constant value 10.2%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Read analog E1 and set analog A1%n" +
                "            ?~ E1%n" +
                "               Get constant value 10.2%n" +
                "            !~ A1%n" +
                "               Set memory IM2%n");
    }
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        AnalogExpressionConstant expression2;
        
        expression2 = new AnalogExpressionConstant("IQAE11", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", null, expression2.getUserName());
        Assert.assertEquals("String matches", "Get constant value 0", expression2.getLongDescription(Locale.ENGLISH));
        
        expression2 = new AnalogExpressionConstant("IQAE11", "My constant value");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My constant value", expression2.getUserName());
        Assert.assertEquals("String matches", "Get constant value 0", expression2.getLongDescription(Locale.ENGLISH));
        
        expression2 = new AnalogExpressionConstant("IQAE11", null);
        expression2.setValue(12.34);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", null, expression2.getUserName());
        Assert.assertEquals("String matches", "Get constant value 12.34", expression2.getLongDescription(Locale.ENGLISH));
        
        expression2 = new AnalogExpressionConstant("IQAE11", "My memory");
        expression2.setValue(98.76);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My memory", expression2.getUserName());
        Assert.assertEquals("String matches", "Get constant value 98.76", expression2.getLongDescription(Locale.ENGLISH));
        
        // Test template
        expression2 = (AnalogExpressionConstant)_base.getNewObjectBasedOnTemplate();
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username is null", expression2.getUserName());
//        Assert.assertTrue("Username matches", "My memory".equals(expression2.getUserName()));
        Assert.assertEquals("String matches", "Get constant value 10.2", expression2.getLongDescription(Locale.ENGLISH));
        
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
    
    @Ignore("This test doesn't work on a Swedish computer due to locale")
    @Test
    @Override
    public void testGetPrintTreeWithStandardLocale() {
    }
    
    @Ignore("This test doesn't work on a Swedish computer due to locale")
    @Test
    @Override
    public void testMaleSocketGetPrintTreeWithStandardLocale() {
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
        AnalogExpressionConstant expression = (AnalogExpressionConstant)_base;
        expression.setValue(0.0d);
        Assert.assertTrue("Evaluate matches", 0.0d == expression.evaluate());
        expression.setValue(10.0d);
        Assert.assertTrue("Evaluate matches", 10.0d == expression.evaluate());
        expression.reset();
    }
    
    @Test
    public void testEvaluateAndAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        
        // The action is not yet executed so the double should be 0.0
        Assert.assertTrue("memory is 0.0", 0.0 == (Double)_memoryOut.getValue());
        // Execute the logixNG
        conditionalNG.setEnabled(true);
        logixNG.calculateConditionalNGs();
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the double should be 0.0
        Assert.assertTrue("memory is 10.2", 10.2 == (Double)_memoryOut.getValue());
        
        // Set the value of the constant.
        expressionConstant.setValue(1.0);
        // Execute the logixNG
        conditionalNG.setEnabled(true);
        logixNG.calculateConditionalNGs();
        conditionalNG.setEnabled(false);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the memory should be 0.0
        Assert.assertTrue("memory is 1.0", 1.0 == (Double)_memoryOut.getValue());
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
        Assert.assertEquals("String matches", "Get constant value 10.2", _base.getShortDescription(Locale.ENGLISH));
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Get constant value 10.2", _base.getLongDescription(Locale.ENGLISH));
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
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initMemoryManager();
        
        expressionConstant = new AnalogExpressionConstant("IQAE321", "AnalogIO_Constant");
        expressionConstant.setValue(10.2);
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        
        logixNG.addConditionalNG(conditionalNG);
        logixNG.activateLogixNG();
        
        DigitalActionBean actionDoAnalog =
                new DoAnalogAction(InstanceManager.getDefault(DigitalActionManager.class).getNewSystemName(), null);
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
    }

    @After
    public void tearDown() {
        _base.dispose();
        JUnitUtil.tearDown();
    }
    
}
