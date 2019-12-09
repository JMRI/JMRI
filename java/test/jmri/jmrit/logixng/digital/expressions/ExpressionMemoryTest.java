package jmri.jmrit.logixng.digital.expressions;

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
import jmri.jmrit.logixng.digital.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
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
    public String getExpectedPrintedTree() {
        return String.format("Memory IM1 is equal to \"\"%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         If E then A1 else A2%n" +
                "            ? E%n" +
                "               Memory IM1 is equal to \"\"%n" +
                "            ! A1%n" +
                "               Set the atomic boolean to true%n" +
                "            ! A2%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionMemory(systemName, null);
    }
    
    @Test
    public void testCtor() {
        ExpressionMemory t = new ExpressionMemory("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        expressionMemory.setMemory((Memory)null);
        Assert.assertEquals("Compare memory", expressionMemory.getShortDescription());
        Assert.assertEquals("Memory Not selected is equal to \"\"", expressionMemory.getLongDescription());
        expressionMemory.setMemory(memory);
        expressionMemory.setConstantValue("A value");
        Assert.assertEquals("Memory IM1 is equal to \"A value\"", expressionMemory.getLongDescription());
        expressionMemory.setConstantValue("Another value");
        Assert.assertEquals("Memory IM1 is equal to \"Another value\"", expressionMemory.getLongDescription());
        Assert.assertEquals("Memory IM1 is equal to \"Another value\"", expressionMemory.getLongDescription());
    }
    
    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.VALUE);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.EQUAL);
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
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Set the memory. This should execute the conditional.
        memory.setValue("Other value");
        memory.setValue("New value");
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
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
        expressionMemory.setMemory((Memory)null);
        Assert.assertNull("Memory is null", expressionMemory.getMemory());
        expressionMemory.setMemory(otherMemoryHandle);
        Assert.assertEquals("Memorys are equal", otherMemory, expressionMemory.getMemory().getBean());
        Assert.assertEquals("MemoryHandles are equal", otherMemoryHandle, expressionMemory.getMemory());
    }
    
    @Test
    public void testSetMemoryException() {
        // Test setMemory() when listeners are registered
        Assert.assertNotNull("Memory is not null", memory);
        Assert.assertNotNull("Memory is not null", expressionMemory.getMemory());
        expressionMemory.registerListeners();
        boolean thrown = false;
        try {
            expressionMemory.setMemory((String)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setMemory must not be called when listeners are registered");
        
        thrown = false;
        try {
            expressionMemory.setMemory((NamedBeanHandle<Memory>)null);
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
        JUnitUtil.initMemoryManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        logixNG.activateLogixNG();
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
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
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
