package jmri.jmrit.logixng.analog.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
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
import org.junit.Test;

/**
 * Test AnalogExpressionMemory
 * 
 * @author Daniel Bergqvist 2018
 */
public class AnalogExpressionMemoryTest extends AbstractAnalogExpressionTestBase {

    protected Memory _memory;
    
    
    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private AnalogExpressionMemory expressionMemory;
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
        return String.format("Get memory IM1%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Read analog E1 and set analog A1%n" +
                "            ?~ E1%n" +
                "               Get memory IM1%n" +
                "            !~ A1%n" +
                "               Set memory IM2%n");
    }
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        AnalogExpressionMemory expression2;
        Assert.assertNotNull("memory is not null", _memory);
        _memory.setValue(10.2);
        
        expression2 = new AnalogExpressionMemory("IQAE11", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertTrue("Username matches", null == expression2.getUserName());
        Assert.assertTrue("String matches", "Get memory none".equals(expression2.getLongDescription()));
        
        expression2 = new AnalogExpressionMemory("IQAE11", "My memory");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertTrue("Username matches", "My memory".equals(expression2.getUserName()));
        Assert.assertTrue("String matches", "Get memory none".equals(expression2.getLongDescription()));
        
        expression2 = new AnalogExpressionMemory("IQAE11", null);
        expression2.setMemory(_memory);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertTrue("Username matches", null == expression2.getUserName());
        Assert.assertTrue("String matches", "Get memory IM1".equals(expression2.getLongDescription()));
        
        expression2 = new AnalogExpressionMemory("IQAE11", "My memory");
        expression2.setMemory(_memory);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertTrue("Username matches", "My memory".equals(expression2.getUserName()));
        Assert.assertTrue("String matches", "Get memory IM1".equals(expression2.getLongDescription()));
        
        boolean thrown = false;
        try {
            // Illegal system name
            new AnalogExpressionMemory("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new AnalogExpressionMemory("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testEvaluate() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        AnalogExpressionMemory expression = (AnalogExpressionMemory)_base;
        _memory.setValue(0.0d);
        Assert.assertTrue("Evaluate matches", 0.0d == expression.evaluate());
        _memory.setValue(10.0d);
        Assert.assertTrue("Evaluate matches", 10.0d == expression.evaluate());
        expression.setMemory((Memory)null);
        Assert.assertTrue("Evaluate matches", 0.0d == expression.evaluate());
        expression.reset();
    }
    
    @Test
    public void testEvaluateAndAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        
        // The action is not yet executed so the double should be 0.0
        Assert.assertTrue("memory is 0.0", 0.0 == (Double)_memoryOut.getValue());
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue(1.0);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the memory should be 0.0
        Assert.assertTrue("memory is 0.0", 0.0 == (Double)_memoryOut.getValue());
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue(2.0);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the memory should be 0.0
        Assert.assertTrue("memory is 0.0", 0.0 == (Double)_memoryOut.getValue());
        // Set the value of the memory. This should execute the conditional.
        _memory.setValue(3.0);
        // The action should now be executed so the memory should be 3.0
        Assert.assertTrue("memory is 3.0", 3.0 == (Double)_memoryOut.getValue());
        // Disable the conditionalNG and all its children.
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the memory should be 0.0
        Assert.assertTrue("memory is 0.0", 3.0 == (Double)_memoryOut.getValue());
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue(4.0);
        // The action should not be executed so the memory should still be 3.0
        Assert.assertTrue("memory is 3.0", 3.0 == (Double)_memoryOut.getValue());
        // Unregister listeners. This should do nothing since the listeners are
        // already unregistered.
        expressionMemory.unregisterListeners();
        // The memory should be 3.0
        Assert.assertTrue("memory is 0.0", 3.0 == (Double)_memoryOut.getValue());
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue(5.0);
        // The action should not be executed so the memory should still be 3.0
        Assert.assertTrue("memory is 3.0", 3.0 == (Double)_memoryOut.getValue());
        
        // Test register listeners when there is no memory.
        expressionMemory.setMemory((Memory)null);
        expressionMemory.registerListeners();
    }
    
    @Test
    public void testMemory() {
        AnalogExpressionMemory expression = (AnalogExpressionMemory)_base;
        expression.setMemory((Memory)null);
        Assert.assertNull("Memory is null", expression.getMemory());
        expression.setMemory(_memory);
        Assert.assertTrue("Memory matches", _memory == expression.getMemory().getBean());
        
        expression.setMemory((NamedBeanHandle<Memory>)null);
        Assert.assertNull("Memory is null", expression.getMemory());
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("memory is not null", otherMemory);
        NamedBeanHandle<Memory> memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        expression.setMemory(memoryHandle);
        Assert.assertTrue("Memory matches", memoryHandle == expression.getMemory());
        Assert.assertTrue("Memory matches", otherMemory == expression.getMemory().getBean());
        
        expression.setMemory((String)null);
        Assert.assertNull("Memory is null", expression.getMemory());
        expression.setMemory(memoryHandle.getName());
        Assert.assertTrue("Memory matches", memoryHandle == expression.getMemory());
        
        // Test setMemory with a memory name that doesn't exists
        expression.setMemory("Non existent memory");
        Assert.assertTrue("Memory matches", memoryHandle == expression.getMemory());
        JUnitAppender.assertWarnMessage("memory 'Non existent memory' does not exists");
        
        // Test setMemory() when listeners are registered
        Assert.assertNotNull("Memory is not null", expression.getMemory());
        expression.registerListeners();
        boolean thrown = false;
        try {
            expression.setMemory((String)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setMemory must not be called when listeners are registered");
        
        thrown = false;
        try {
            expression.setMemory((NamedBeanHandle<Memory>)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setMemory must not be called when listeners are registered");
        
        thrown = false;
        try {
            expression.setMemory((Memory)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setMemory must not be called when listeners are registered");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get some other memory for later use
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("Memory is not null", otherMemory);
        Assert.assertNotEquals("Memory is not equal", _memory, otherMemory);
        
        // Get the expression and set the memory
        AnalogExpressionMemory expression = (AnalogExpressionMemory)_base;
        expression.setMemory(_memory);
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        
        // Test vetoableChange() for some other propery
        expression.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        
        // Test vetoableChange() for a string
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        
        // Test vetoableChange() for another memory
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        
        // Test vetoableChange() for its own memory
        boolean thrown = false;
        try {
            expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", _memory, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", _memory, null));
        Assert.assertNull("Memory is null", expression.getMemory());
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
        Assert.assertTrue("String matches", "Get memory IM1".equals(_base.getShortDescription()));
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertTrue("String matches", "Get memory IM1".equals(_base.getLongDescription()));
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
        
        _memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        Assert.assertNotNull("memory is not null", _memory);
        _memory.setValue(10.2);
        expressionMemory = new AnalogExpressionMemory("IQAE321", "AnalogIO_Memory");
        expressionMemory.setMemory(_memory);
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        
        logixNG.addConditionalNG(conditionalNG);
        logixNG.activateLogixNG();
        
        DigitalActionBean actionDoAnalog =
                new DoAnalogAction(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        MaleSocket socketDoAnalog =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionDoAnalog);
        conditionalNG.getChild(0).connect(socketDoAnalog);
        
        MaleSocket socketExpression =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(expressionMemory);
        socketDoAnalog.getChild(0).connect(socketExpression);
        
        _memoryOut = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        _memoryOut.setValue(0.0);
        actionMemory = new AnalogActionMemory("IQAA1", null);
        actionMemory.setMemory(_memoryOut);
        MaleSocket socketAction =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(actionMemory);
        socketDoAnalog.getChild(1).connect(socketAction);
        
        _base = expressionMemory;
        _baseMaleSocket = socketExpression;
    }

    @After
    public void tearDown() {
        _base.dispose();
        JUnitUtil.tearDown();
    }
    
}
