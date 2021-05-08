package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ExpressionMemory
 * 
 * @author Daniel Bergqvist 2018
 */
public class ExpressionMemoryTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionMemory expressionMemory;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    private Memory memory;
    
    
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
        return String.format("Memory IM1 is equal to \"\" ::: Use default%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Always execute ::: Use default%n" +
                "            ? If%n" +
                "               Memory IM1 is equal to \"\" ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionMemory(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        ExpressionMemory expression2;
        Assert.assertNotNull("memory is not null", memory);
        
        expression2 = new ExpressionMemory("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Memory '' is equal to \"\"", expression2.getLongDescription());
        
        expression2 = new ExpressionMemory("IQDE321", "My memory");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My memory", expression2.getUserName());
        Assert.assertEquals("String matches", "Memory '' is equal to \"\"", expression2.getLongDescription());
        
        expression2 = new ExpressionMemory("IQDE321", null);
        expression2.setMemory(memory);
        Assert.assertTrue("memory is correct", memory == expression2.getMemory().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Memory IM1 is equal to \"\"", expression2.getLongDescription());
        
        Memory l = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        expression2 = new ExpressionMemory("IQDE321", "My memory");
        expression2.setMemory(l);
        Assert.assertTrue("memory is correct", l == expression2.getMemory().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My memory", expression2.getUserName());
        Assert.assertEquals("String matches", "Memory IM2 is equal to \"\"", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionMemory("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ExpressionMemory("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionMemory.getChildCount());
        
        boolean hasThrown = false;
        try {
            expressionMemory.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testMemoryOperation() {
        Assert.assertEquals("String matches", "is less than", ExpressionMemory.MemoryOperation.LessThan.toString());
        Assert.assertEquals("String matches", "is less than or equal", ExpressionMemory.MemoryOperation.LessThanOrEqual.toString());
        Assert.assertEquals("String matches", "is equal to", ExpressionMemory.MemoryOperation.Equal.toString());
        Assert.assertEquals("String matches", "is greater than or equal to", ExpressionMemory.MemoryOperation.GreaterThanOrEqual.toString());
        Assert.assertEquals("String matches", "is greater than", ExpressionMemory.MemoryOperation.GreaterThan.toString());
        Assert.assertEquals("String matches", "is not equal to", ExpressionMemory.MemoryOperation.NotEqual.toString());
        Assert.assertEquals("String matches", "is null", ExpressionMemory.MemoryOperation.IsNull.toString());
        Assert.assertEquals("String matches", "is not null", ExpressionMemory.MemoryOperation.IsNotNull.toString());
        Assert.assertEquals("String matches", "does match regular expression", ExpressionMemory.MemoryOperation.MatchRegex.toString());
        Assert.assertEquals("String matches", "does not match regular expression", ExpressionMemory.MemoryOperation.NotMatchRegex.toString());
        
        Assert.assertTrue("operation has extra value", ExpressionMemory.MemoryOperation.LessThan.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionMemory.MemoryOperation.LessThanOrEqual.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionMemory.MemoryOperation.Equal.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionMemory.MemoryOperation.GreaterThanOrEqual.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionMemory.MemoryOperation.GreaterThan.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionMemory.MemoryOperation.NotEqual.hasExtraValue());
        Assert.assertFalse("operation has not extra value", ExpressionMemory.MemoryOperation.IsNull.hasExtraValue());
        Assert.assertFalse("operation has not extra value", ExpressionMemory.MemoryOperation.IsNotNull.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionMemory.MemoryOperation.MatchRegex.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionMemory.MemoryOperation.NotMatchRegex.hasExtraValue());
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
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        expressionMemory.removeMemory();
        Assert.assertEquals("Memory", expressionMemory.getShortDescription());
        Assert.assertEquals("Memory '' is equal to \"\"", expressionMemory.getLongDescription());
        expressionMemory.setMemory(memory);
        expressionMemory.setConstantValue("A value");
        Assert.assertEquals("Memory IM1 is equal to \"A value\"", expressionMemory.getLongDescription());
        expressionMemory.setConstantValue("Another value");
        Assert.assertEquals("Memory IM1 is equal to \"Another value\"", expressionMemory.getLongDescription());
        Assert.assertEquals("Memory IM1 is equal to \"Another value\"", expressionMemory.getLongDescription());
    }
    
    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Clear flag
        atomicBoolean.set(false);
        // Set the memory
        memory.setValue("New value");
        
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Value);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.Equal);
        expressionMemory.setConstantValue("New value");
        
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Set the memory. This should not execute the conditional.
        memory.setValue("Other value");
        memory.setValue("New value");
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Set the memory. This should not execute the conditional.
        memory.setValue("Other value");
        memory.setValue("New value");
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the memory. This should execute the conditional.
        memory.setValue("Other value");
        memory.setValue("New value");
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
        
        
        // Test regular expression match
        conditionalNG.setEnabled(false);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.MatchRegex);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Value);
        expressionMemory.setConstantValue("Hello.*");
        memory.setValue("Hello world");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertTrue("The expression returns true",atomicBoolean.get());
        
        conditionalNG.setEnabled(false);
        memory.setValue("Some world");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertFalse("The expression returns false",atomicBoolean.get());
        
        // Test regular expressions
        conditionalNG.setEnabled(false);
        expressionMemory.setConstantValue("\\w\\w\\d+\\s\\d+");
        memory.setValue("Ab213 31");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertTrue("The expression returns true",atomicBoolean.get());
        
        conditionalNG.setEnabled(false);
        memory.setValue("Ab213_31");    // Underscore instead of space
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertFalse("The expression returns false",atomicBoolean.get());
        
        
        // Test regular expression not match
        conditionalNG.setEnabled(false);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.NotMatchRegex);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Value);
        expressionMemory.setConstantValue("Hello.*");
        memory.setValue("Hello world");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertFalse("The expression returns false",atomicBoolean.get());
        
        conditionalNG.setEnabled(false);
        memory.setValue("Some world");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertTrue("The expression returns true",atomicBoolean.get());
        
        // Test regular expressions
        conditionalNG.setEnabled(false);
        expressionMemory.setConstantValue("\\w\\w\\d+\\s\\d+");
        memory.setValue("Ab213 31");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertFalse("The expression returns false",atomicBoolean.get());
        
        conditionalNG.setEnabled(false);
        memory.setValue("Ab213_31");    // Underscore instead of space
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertTrue("The expression returns true",atomicBoolean.get());
    }
    
    @Test
    public void testSetMemory() {
        expressionMemory.unregisterListeners();
        
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotEquals("Memorys are different", otherMemory, expressionMemory.getMemory().getBean());
        expressionMemory.setMemory(otherMemory);
        Assert.assertEquals("Memorys are equal", otherMemory, expressionMemory.getMemory().getBean());
        
        NamedBeanHandle<Memory> otherMemoryHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        expressionMemory.removeMemory();
        Assert.assertNull("Memory is null", expressionMemory.getMemory());
        expressionMemory.setMemory(otherMemoryHandle);
        Assert.assertEquals("Memorys are equal", otherMemory, expressionMemory.getMemory().getBean());
        Assert.assertEquals("MemoryHandles are equal", otherMemoryHandle, expressionMemory.getMemory());
    }
    
    @Test
    public void testSetMemory2() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        Memory memory11 = InstanceManager.getDefault(MemoryManager.class).provide("IM11");
        Memory memory12 = InstanceManager.getDefault(MemoryManager.class).provide("IM12");
        NamedBeanHandle<Memory> memoryHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memory12.getDisplayName(), memory12);
        Memory memory13 = InstanceManager.getDefault(MemoryManager.class).provide("IM13");
        Memory memory14 = InstanceManager.getDefault(MemoryManager.class).provide("IM14");
        memory14.setUserName("Some user name");
        
        expressionMemory.removeMemory();
        Assert.assertNull("memory handle is null", expressionMemory.getMemory());
        
        expressionMemory.setMemory(memory11);
        Assert.assertTrue("memory is correct", memory11 == expressionMemory.getMemory().getBean());
        
        expressionMemory.removeMemory();
        Assert.assertNull("memory handle is null", expressionMemory.getMemory());
        
        expressionMemory.setMemory(memoryHandle12);
        Assert.assertTrue("memory handle is correct", memoryHandle12 == expressionMemory.getMemory());
        
        expressionMemory.setMemory("A non existent memory");
        Assert.assertNull("memory handle is null", expressionMemory.getMemory());
        JUnitAppender.assertWarnMessage("memory \"A non existent memory\" is not found");
        
        expressionMemory.setMemory(memory13.getSystemName());
        Assert.assertTrue("memory is correct", memory13 == expressionMemory.getMemory().getBean());
        
        String userName = memory14.getUserName();
        Assert.assertNotNull("memory is not null", userName);
        expressionMemory.setMemory(userName);
        Assert.assertTrue("memory is correct", memory14 == expressionMemory.getMemory().getBean());
    }
    
    @Test
    public void testSetMemoryException() {
        // Test setMemory() when listeners are registered
        Assert.assertNotNull("Memory is not null", memory);
        Assert.assertNotNull("Memory is not null", expressionMemory.getMemory());
        expressionMemory.registerListeners();
        boolean thrown = false;
        try {
            expressionMemory.setMemory("A memory");
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setMemory must not be called when listeners are registered");
        
        thrown = false;
        try {
            Memory memory99 = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
            NamedBeanHandle<Memory> memoryHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memory99.getDisplayName(), memory99);
            expressionMemory.setMemory(memoryHandle99);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setMemory must not be called when listeners are registered");
        
        thrown = false;
        try {
            expressionMemory.setMemory((Memory)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setMemory must not be called when listeners are registered");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        // Get the expression and set the memory
        Assert.assertNotNull("Memory is not null", memory);
        
        // Get some other memory for later use
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("Memory is not null", otherMemory);
        Assert.assertNotEquals("Memory is not equal", memory, otherMemory);
        
        // Test vetoableChange() for some other propery
        expressionMemory.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Memory matches", memory, expressionMemory.getMemory().getBean());
        
        // Test vetoableChange() for a string
        expressionMemory.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Memory matches", memory, expressionMemory.getMemory().getBean());
        expressionMemory.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Memory matches", memory, expressionMemory.getMemory().getBean());
        
        // Test vetoableChange() for another memory
        expressionMemory.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", memory, expressionMemory.getMemory().getBean());
        expressionMemory.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", memory, expressionMemory.getMemory().getBean());
        
        // Test vetoableChange() for its own memory
        boolean thrown = false;
        try {
            expressionMemory.vetoableChange(new PropertyChangeEvent(this, "CanDelete", memory, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Memory matches", memory, expressionMemory.getMemory().getBean());
        expressionMemory.vetoableChange(new PropertyChangeEvent(this, "DoDelete", memory, null));
        Assert.assertNull("Memory is null", expressionMemory.getMemory());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLogixNGManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        
        logixNG.addConditionalNG(conditionalNG);
        
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        ifThenElse.setType(IfThenElse.Type.AlwaysExecute);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        expressionMemory = new ExpressionMemory("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionMemory);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expressionMemory;
        _baseMaleSocket = maleSocket2;
        
        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);
        
        memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        expressionMemory.setMemory(memory);
        memory.setValue("");
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
