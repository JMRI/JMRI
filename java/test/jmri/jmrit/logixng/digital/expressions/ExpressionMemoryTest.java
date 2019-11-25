package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
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
public class ExpressionMemoryTest {

    @Test
    public void testCtor() {
        ExpressionMemory t = new ExpressionMemory("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        ExpressionMemory expressionMemory = new ExpressionMemory("IQDE321", null);
        Assert.assertEquals("Compare memory", expressionMemory.getShortDescription());
        Assert.assertEquals("Memory Not selected is equal to \"\"", expressionMemory.getLongDescription());
        Memory memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        expressionMemory.setMemory(memory);
        expressionMemory.setConstantValue("A value");
        Assert.assertEquals("Memory IM1 is equal to \"A value\"", expressionMemory.getLongDescription());
        expressionMemory.setConstantValue("Another value");
        Assert.assertEquals("Memory IM1 is equal to \"Another value\"", expressionMemory.getLongDescription());
        Assert.assertEquals("Memory IM1 is equal to \"Another value\"", expressionMemory.getLongDescription());
    }
    
    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        Memory memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        memory.setValue("A value");
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        logixNG.activateLogixNG();
        
        IfThenElse actionIfThen =
                new IfThenElse(
                        InstanceManager.getDefault(
                                DigitalActionManager.class).getAutoSystemName(), null,
                                IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket socketIfThen = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionIfThen);
        conditionalNG.getChild(0).connect(socketIfThen);
        
        ExpressionMemory expressionMemory =
                new ExpressionMemory(
                        InstanceManager.getDefault(DigitalExpressionManager.class)
                                .getAutoSystemName(), null);
        expressionMemory.setMemory(memory);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.VALUE);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.EQUAL);
        expressionMemory.setConstantValue("New value");
        MaleSocket socketMemory = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionMemory);
        socketIfThen.getChild(0).connect(socketMemory);
        
        ActionAtomicBoolean actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        socketIfThen.getChild(1).connect(socketAtomicBoolean);
        
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
        // Test setMemory() when listeners are registered
        Memory memory = InstanceManager.getDefault(MemoryManager.class).provide("IT1");
        Assert.assertNotNull("Memory is not null", memory);
        ExpressionMemory expression =
                new ExpressionMemory(
                        InstanceManager.getDefault(DigitalExpressionManager.class)
                                .getAutoSystemName(), null);
        expression.setMemory(memory);
        
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
        // Get the expression and set the memory
        Memory memory = InstanceManager.getDefault(MemoryManager.class).provide("IT1");
        Assert.assertNotNull("Memory is not null", memory);
        ExpressionMemory expression =
                new ExpressionMemory(
                        InstanceManager.getDefault(DigitalExpressionManager.class)
                                .getAutoSystemName(), null);
        expression.setMemory(memory);
        
        // Get some other memory for later use
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("Memory is not null", otherMemory);
        Assert.assertNotEquals("Memory is not equal", memory, otherMemory);
        
        // Test vetoableChange() for some other propery
        expression.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Memory matches", memory, expression.getMemory().getBean());
        
        // Test vetoableChange() for a string
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Memory matches", memory, expression.getMemory().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Memory matches", memory, expression.getMemory().getBean());
        
        // Test vetoableChange() for another memory
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", memory, expression.getMemory().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", memory, expression.getMemory().getBean());
        
        // Test vetoableChange() for its own memory
        boolean thrown = false;
        try {
            expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", memory, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Memory matches", memory, expression.getMemory().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", memory, null));
        Assert.assertNull("Memory is null", expression.getMemory());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initMemoryManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
