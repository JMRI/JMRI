package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.LogixNG_Category;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertNotNull( memory, "memory is not null");

        expression2 = new ExpressionMemory("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Memory '' is equal to \"\"", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionMemory("IQDE321", "My memory");
        assertNotNull( expression2, "object exists");
        assertEquals( "My memory", expression2.getUserName(), "Username matches");
        assertEquals( "Memory '' is equal to \"\"", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionMemory("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(memory);
        assertSame( memory, expression2.getSelectNamedBean().getNamedBean().getBean(), "memory is correct");
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Memory IM1 is equal to \"\"", expression2.getLongDescription(), "String matches");

        Memory l = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        expression2 = new ExpressionMemory("IQDE321", "My memory");
        expression2.getSelectNamedBean().setNamedBean(l);
        assertSame( l, expression2.getSelectNamedBean().getNamedBean().getBean(), "memory is correct");
        assertNotNull( expression2, "object exists");
        assertEquals( "My memory", expression2.getUserName(), "Username matches");
        assertEquals( "Memory IM2 is equal to \"\"", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionMemory("IQE55:12:XY11", null);
            fail("Should hane thrown, not created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionMemory("IQE55:12:XY11", "A name");
            fail("Should hane thrown, not created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionMemory.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            expressionMemory.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testMemoryOperation() {
        assertEquals( "is less than", ExpressionMemory.MemoryOperation.LessThan.toString(), "String matches");
        assertEquals( "is less than or equal", ExpressionMemory.MemoryOperation.LessThanOrEqual.toString(), "String matches");
        assertEquals( "is equal to", ExpressionMemory.MemoryOperation.Equal.toString(), "String matches");
        assertEquals( "is greater than or equal to", ExpressionMemory.MemoryOperation.GreaterThanOrEqual.toString(), "String matches");
        assertEquals( "is greater than", ExpressionMemory.MemoryOperation.GreaterThan.toString(), "String matches");
        assertEquals( "is not equal to", ExpressionMemory.MemoryOperation.NotEqual.toString(), "String matches");
        assertEquals( "is null", ExpressionMemory.MemoryOperation.IsNull.toString(), "String matches");
        assertEquals( "is not null", ExpressionMemory.MemoryOperation.IsNotNull.toString(), "String matches");
        assertEquals( "does match regular expression", ExpressionMemory.MemoryOperation.MatchRegex.toString(), "String matches");
        assertEquals( "does not match regular expression", ExpressionMemory.MemoryOperation.NotMatchRegex.toString(), "String matches");

        assertTrue( ExpressionMemory.MemoryOperation.LessThan.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionMemory.MemoryOperation.LessThanOrEqual.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionMemory.MemoryOperation.Equal.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionMemory.MemoryOperation.GreaterThanOrEqual.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionMemory.MemoryOperation.GreaterThan.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionMemory.MemoryOperation.NotEqual.hasExtraValue(), "operation has extra value");
        assertFalse( ExpressionMemory.MemoryOperation.IsNull.hasExtraValue(), "operation has not extra value");
        assertFalse( ExpressionMemory.MemoryOperation.IsNotNull.hasExtraValue(), "operation has not extra value");
        assertTrue( ExpressionMemory.MemoryOperation.MatchRegex.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionMemory.MemoryOperation.NotMatchRegex.hasExtraValue(), "operation has extra value");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionMemory.getSelectNamedBean().removeNamedBean();
        assertEquals("Memory", expressionMemory.getShortDescription());
        assertEquals("Memory '' is equal to \"\"", expressionMemory.getLongDescription());
        expressionMemory.getSelectNamedBean().setNamedBean(memory);
        expressionMemory.setConstantValue("A value");
        assertEquals("Memory IM1 is equal to \"A value\"", expressionMemory.getLongDescription());
        expressionMemory.setConstantValue("Another value");
        assertEquals("Memory IM1 is equal to \"Another value\"", expressionMemory.getLongDescription());
        assertEquals("Memory IM1 is equal to \"Another value\"", expressionMemory.getLongDescription());
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
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Set the memory. This should not execute the conditional.
        memory.setValue("Other value");
        memory.setValue("New value");
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Set the memory. This should not execute the conditional.
        memory.setValue("Other value");
        memory.setValue("New value");
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the memory. This should execute the conditional.
        memory.setValue("Other value");
        memory.setValue("New value");
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");


        // Test regular expression match
        conditionalNG.setEnabled(false);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.MatchRegex);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Value);
        expressionMemory.setRegEx("Hello.*");
        memory.setValue("Hello world");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertTrue( atomicBoolean.get(), "The expression returns true");

        conditionalNG.setEnabled(false);
        memory.setValue("Some world");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertFalse( atomicBoolean.get(), "The expression returns false");

        // Test regular expressions
        conditionalNG.setEnabled(false);
        expressionMemory.setRegEx("\\w\\w\\d+\\s\\d+");
        memory.setValue("Ab213 31");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertTrue( atomicBoolean.get(), "The expression returns true");

        conditionalNG.setEnabled(false);
        memory.setValue("Ab213_31");    // Underscore instead of space
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertFalse( atomicBoolean.get(), "The expression returns false");


        // Test regular expression not match
        conditionalNG.setEnabled(false);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.NotMatchRegex);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Value);
        expressionMemory.setRegEx("Hello.*");
        memory.setValue("Hello world");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertFalse( atomicBoolean.get(), "The expression returns false");

        conditionalNG.setEnabled(false);
        memory.setValue("Some world");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertTrue( atomicBoolean.get(), "The expression returns true");

        // Test regular expressions
        conditionalNG.setEnabled(false);
        expressionMemory.setRegEx("\\w\\w\\d+\\s\\d+");
        memory.setValue("Ab213 31");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertFalse( atomicBoolean.get(), "The expression returns false");

        conditionalNG.setEnabled(false);
        memory.setValue("Ab213_31");    // Underscore instead of space
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertTrue( atomicBoolean.get(), "The expression returns true");
    }

    @Test
    public void testSetMemory() {
        expressionMemory.unregisterListeners();

        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        assertNotEquals( otherMemory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memories are different");
        expressionMemory.getSelectNamedBean().setNamedBean(otherMemory);
        assertEquals( otherMemory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memories are equal");

        NamedBeanHandle<Memory> otherMemoryHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        expressionMemory.getSelectNamedBean().removeNamedBean();
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is null");
        expressionMemory.getSelectNamedBean().setNamedBean(otherMemoryHandle);
        assertEquals( otherMemory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memorys are equal");
        assertEquals( otherMemoryHandle, expressionMemory.getSelectNamedBean().getNamedBean(), "MemoryHandles are equal");
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

        expressionMemory.getSelectNamedBean().removeNamedBean();
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "memory handle is null");

        expressionMemory.getSelectNamedBean().setNamedBean(memory11);
        assertSame( memory11, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "memory is correct");

        expressionMemory.getSelectNamedBean().removeNamedBean();
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "memory handle is null");

        expressionMemory.getSelectNamedBean().setNamedBean(memoryHandle12);
        assertSame( memoryHandle12, expressionMemory.getSelectNamedBean().getNamedBean(), "memory handle is correct");

        expressionMemory.getSelectNamedBean().setNamedBean("A non existent memory");
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "memory handle is null");
        JUnitAppender.assertErrorMessage("Memory \"A non existent memory\" is not found");

        expressionMemory.getSelectNamedBean().setNamedBean(memory13.getSystemName());
        assertSame( memory13, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "memory is correct");

        String userName = memory14.getUserName();
        assertNotNull( userName, "memory is not null");
        expressionMemory.getSelectNamedBean().setNamedBean(userName);
        assertSame( memory14, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "memory is correct");
    }

    @Test
    public void testSetMemoryException() {
        // Test getSelectNamedBean().setNamedBean() when listeners are registered
        assertNotNull( memory, "Memory is not null");
        assertNotNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is not null");
        expressionMemory.registerListeners();
        RuntimeException ex = assertThrows( RuntimeException.class, () ->
            expressionMemory.getSelectNamedBean().setNamedBean("A memory"), "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () -> {
            Memory memory99 = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
            NamedBeanHandle<Memory> memoryHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memory99.getDisplayName(), memory99);
            expressionMemory.getSelectNamedBean().setNamedBean(memoryHandle99);
        }, "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Get the expression and set the memory
        assertNotNull( memory, "Memory is not null");

        // Get some other memory for later use
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        assertNotNull( otherMemory, "Memory is not null");
        assertNotEquals( memory, otherMemory, "Memory is not equal");

        // Test vetoableChange() for some other propery
        expressionMemory.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( memory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for a string
        expressionMemory.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( memory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        expressionMemory.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( memory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for another memory
        expressionMemory.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        assertEquals( memory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        expressionMemory.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        assertEquals( memory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for its own memory
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            expressionMemory.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", memory, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( memory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        expressionMemory.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", memory, null));
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is null");
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        logixNG.addConditionalNG(conditionalNG);

        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        ifThenElse.setExecuteType(IfThenElse.ExecuteType.AlwaysExecute);
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
        expressionMemory.getSelectNamedBean().setNamedBean(memory);
        memory.setValue("");

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
