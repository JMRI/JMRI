package jmri.jmrit.logixng.string.expressions;

import java.util.Locale;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionBean;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.StringExpressionManager;
import jmri.jmrit.logixng.digital.actions.DoStringAction;
import jmri.jmrit.logixng.string.actions.StringActionMemory;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test StringExpressionConstant
 * 
 * @author Daniel Bergqvist 2018
 */
public class StringExpressionConstantTest extends AbstractStringExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private StringExpressionConstant expressionConstant;
    private Memory _memoryOut;
    private StringActionMemory actionMemory;
    
    
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
        return String.format("Get constant value \"Something\"%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Read string E and set string A%n" +
                "            ?s E%n" +
                "               Get constant value \"Something\"%n" +
                "            !s A%n" +
                "               Set memory IM2%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new StringExpressionConstant(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        StringExpressionConstant expression2;
        
        expression2 = new StringExpressionConstant("IQSE11", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", null, expression2.getUserName());
        Assert.assertEquals("String matches", "Get constant value null", expression2.getLongDescription(Locale.ENGLISH));
        
        expression2 = new StringExpressionConstant("IQSE11", "My constant value");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My constant value", expression2.getUserName());
        Assert.assertEquals("String matches", "Get constant value null", expression2.getLongDescription(Locale.ENGLISH));
        
        expression2 = new StringExpressionConstant("IQSE11", null);
        expression2.setValue("A value");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", null, expression2.getUserName());
        Assert.assertEquals("String matches", "Get constant value \"A value\"", expression2.getLongDescription(Locale.ENGLISH));
        
        expression2 = new StringExpressionConstant("IQSE11", "My constant");
        expression2.setValue("Some other value");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My constant", expression2.getUserName());
        Assert.assertEquals("String matches", "Get constant value \"Some other value\"", expression2.getLongDescription(Locale.ENGLISH));
        
        // Call setup(). It doesn't do anything, but we call it for coverage
        expression2.setup();
        
        boolean thrown = false;
        try {
            // Illegal system name
            new StringExpressionConstant("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new StringExpressionConstant("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        // Call setup() for coverage. setup() doesn't do anything
        expression2.setup();
    }
    
    @Test
    public void testSetValue() {
        conditionalNG.setEnabled(false);
        StringExpressionConstant expression = (StringExpressionConstant)_base;
        expression.setValue("");
        Assert.assertEquals("getValue() returns correct value", "", expression.getValue());
        expression.setValue("A value");
        Assert.assertEquals("getValue() returns correct value", "A value", expression.getValue());
        expression.setValue("Some other value");
        Assert.assertEquals("getValue() returns correct value", "Some other value", expression.getValue());
    }
    
    @Test
    public void testSetValueWithListenersRegistered() {
        boolean exceptionThrown = false;
        try {
            StringExpressionConstant expression = (StringExpressionConstant)_base;
            expression.registerListeners();
            expression.setValue("A value");
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
        StringExpressionConstant expression = (StringExpressionConstant)_base;
        expression.setValue("");
        Assert.assertEquals("Evaluate matches", "", expression.evaluate());
        expression.setValue("");
        Assert.assertEquals("Evaluate matches", "", expression.evaluate());
        expression.reset();
    }
    
    @Test
    public void testEvaluateAndAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        
        // Set the memory
        _memoryOut.setValue("");
        // The string should be ""
        Assert.assertEquals("memory is \"\"", "", _memoryOut.getValue());
        // Execute the logixNG
        logixNG.execute();
        // The action is executed so the string should be "Something"
        Assert.assertEquals("memory is \"Something\"", "Something", _memoryOut.getValue());
        
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        // Set the value of the constant.
        expressionConstant.setValue("A value");
        // Enable the conditionalNG
        conditionalNG.setEnabled(true);
        // Execute the logixNG
        logixNG.execute();
        // The action is executed so the string should be "A value"
        Assert.assertEquals("memory is \"A value\"", "A value", _memoryOut.getValue());
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
        Assert.assertEquals("String matches", "Get constant value \"Something\"", _base.getShortDescription(Locale.ENGLISH));
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Get constant value \"Something\"", _base.getLongDescription(Locale.ENGLISH));
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
        
        expressionConstant = new StringExpressionConstant("IQSE321", "StringIO_Constant");
        expressionConstant.setValue("Something");
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        conditionalNG.setEnabled(true);
        
        logixNG.addConditionalNG(conditionalNG);
        
        DigitalActionBean actionDoString =
                new DoStringAction(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        MaleSocket socketDoString =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionDoString);
        conditionalNG.getChild(0).connect(socketDoString);
        
        MaleSocket socketExpression =
                InstanceManager.getDefault(StringExpressionManager.class).registerExpression(expressionConstant);
        socketDoString.getChild(0).connect(socketExpression);
        
        _memoryOut = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        _memoryOut.setValue(0.0);
        actionMemory = new StringActionMemory("IQSA1", null);
        actionMemory.setMemory(_memoryOut);
        MaleSocket socketAction =
                InstanceManager.getDefault(StringActionManager.class).registerAction(actionMemory);
        socketDoString.getChild(1).connect(socketAction);
        
        _base = expressionConstant;
        _baseMaleSocket = socketExpression;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        _base.dispose();
        JUnitUtil.tearDown();
    }
    
}
