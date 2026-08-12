package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ExpressionLocalVariable
 *
 * @author Daniel Bergqvist 2021
 */
public class ExpressionLocalVariableTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionLocalVariable expressionLocalVariable;
    private MaleSocket localVariableMaleSocket;
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
        return null;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format("Local variable myVar is equal to \"\" ::: Use default%n   ::: Local variable \"myVar\", init to String \"\"%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Always execute ::: Use default%n" +
                "            ? If%n" +
                "               Local variable myVar is equal to \"\" ::: Use default%n" +
                "                  ::: Local variable \"myVar\", init to String \"\"%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionLocalVariable(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        ExpressionLocalVariable expression2;

        expression2 = new ExpressionLocalVariable("IQDE321", null);
        expression2.setLocalVariable("");
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Local variable '' is equal to \"\"", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionLocalVariable("IQDE321", "My memory");
        expression2.setLocalVariable("");
        assertNotNull( expression2, "object exists");
        assertEquals( "My memory", expression2.getUserName(), "Username matches");
        assertEquals( "Local variable '' is equal to \"\"", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionLocalVariable("IQDE321", null);
        expression2.setLocalVariable("myVar");
        assertEquals( "myVar", expression2.getLocalVariable(), "variable is correct");
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Local variable myVar is equal to \"\"", expression2.getLongDescription(), "String matches");

        Memory l = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        expression2 = new ExpressionLocalVariable("IQDE321", "My memory");
        expression2.setLocalVariable("someVar");
        expression2.getSelectMemoryNamedBean().setNamedBean(l);
        assertEquals( l, expression2.getSelectMemoryNamedBean().getNamedBean().getBean(), "memory is correct");
        assertNotNull( expression2, "object exists");
        assertEquals( "My memory", expression2.getUserName(), "Username matches");
        assertEquals( "Local variable someVar is equal to \"\"", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionLocalVariable("IQE55:12:XY11", null);
            fail("Should have thrown, not created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionLocalVariable("IQE55:12:XY11", "A name");
            fail("Should have thrown, not created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionLocalVariable.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            expressionLocalVariable.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testVariableOperation() {
        assertEquals( "is less than", ExpressionLocalVariable.VariableOperation.LessThan.toString(), "String matches");
        assertEquals( "is less than or equal", ExpressionLocalVariable.VariableOperation.LessThanOrEqual.toString(), "String matches");
        assertEquals( "is equal to", ExpressionLocalVariable.VariableOperation.Equal.toString(), "String matches");
        assertEquals( "is greater than or equal to", ExpressionLocalVariable.VariableOperation.GreaterThanOrEqual.toString(), "String matches");
        assertEquals( "is greater than", ExpressionLocalVariable.VariableOperation.GreaterThan.toString(), "String matches");
        assertEquals( "is not equal to", ExpressionLocalVariable.VariableOperation.NotEqual.toString(), "String matches");
        assertEquals( "is null", ExpressionLocalVariable.VariableOperation.IsNull.toString(), "String matches");
        assertEquals( "is not null", ExpressionLocalVariable.VariableOperation.IsNotNull.toString(), "String matches");
        assertEquals( "does match regular expression", ExpressionLocalVariable.VariableOperation.MatchRegex.toString(), "String matches");
        assertEquals( "does not match regular expression", ExpressionLocalVariable.VariableOperation.NotMatchRegex.toString(), "String matches");

        assertTrue( ExpressionLocalVariable.VariableOperation.LessThan.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionLocalVariable.VariableOperation.LessThanOrEqual.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionLocalVariable.VariableOperation.Equal.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionLocalVariable.VariableOperation.GreaterThanOrEqual.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionLocalVariable.VariableOperation.GreaterThan.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionLocalVariable.VariableOperation.NotEqual.hasExtraValue(), "operation has extra value");
        assertFalse( ExpressionLocalVariable.VariableOperation.IsNull.hasExtraValue(), "operation has not extra value");
        assertFalse( ExpressionLocalVariable.VariableOperation.IsNotNull.hasExtraValue(), "operation has not extra value");
        assertTrue( ExpressionLocalVariable.VariableOperation.MatchRegex.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionLocalVariable.VariableOperation.NotMatchRegex.hasExtraValue(), "operation has extra value");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionLocalVariable.setLocalVariable("someVar");
        assertEquals("Local variable", expressionLocalVariable.getShortDescription());
        assertEquals("Local variable someVar is equal to \"\"", expressionLocalVariable.getLongDescription());
        expressionLocalVariable.setLocalVariable("myVar");
        expressionLocalVariable.setConstantValue("A value");
        assertEquals("Local variable myVar is equal to \"A value\"", expressionLocalVariable.getLongDescription());
        expressionLocalVariable.setConstantValue("Another value");
        assertEquals("Local variable myVar is equal to \"Another value\"", expressionLocalVariable.getLongDescription());
        assertEquals("Local variable myVar is equal to \"Another value\"", expressionLocalVariable.getLongDescription());
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Clear flag
        atomicBoolean.set(false);
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "New value");

        // Disable the conditionalNG
        conditionalNG.setEnabled(false);

        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Value);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.Equal);
        expressionLocalVariable.setConstantValue("New value");

        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the memory. This should execute the conditional.
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");


        // Test regular expression match
        conditionalNG.setEnabled(false);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.MatchRegex);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Value);
        expressionLocalVariable.setRegEx("Hello.*");
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Hello world");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertTrue( atomicBoolean.get(), "The expression returns true");

        conditionalNG.setEnabled(false);
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Some world");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertFalse( atomicBoolean.get(), "The expression returns false");

        // Test regular expressions
        conditionalNG.setEnabled(false);
        expressionLocalVariable.setRegEx("\\w\\w\\d+\\s\\d+");
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Ab213 31");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertTrue( atomicBoolean.get(), "The expression returns true");

        conditionalNG.setEnabled(false);
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Ab213_31");     // Underscore instead of space
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertFalse( atomicBoolean.get(), "The expression returns false");


        // Test regular expression not match
        conditionalNG.setEnabled(false);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.NotMatchRegex);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Value);
        expressionLocalVariable.setRegEx("Hello.*");
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Hello world");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertFalse( atomicBoolean.get(), "The expression returns false");

        conditionalNG.setEnabled(false);
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Some world");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertTrue( atomicBoolean.get(), "The expression returns true");

        // Test regular expressions
        conditionalNG.setEnabled(false);
        expressionLocalVariable.setRegEx("\\w\\w\\d+\\s\\d+");
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Ab213 31");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertFalse( atomicBoolean.get(), "The expression returns false");

        conditionalNG.setEnabled(false);
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Ab213_31");     // Underscore instead of space
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertTrue( atomicBoolean.get(), "The expression returns true");
    }
/*
    @Test
    public void testSetMemory() {
        expressionLocalVariable.unregisterListeners();

        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotEquals("Memorys are different", otherMemory, expressionLocalVariable.getMemory().getBean());
        expressionLocalVariable.setMemory(otherMemory);
        Assert.assertEquals("Memorys are equal", otherMemory, expressionLocalVariable.getMemory().getBean());

        NamedBeanHandle<Memory> otherMemoryHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        expressionLocalVariable.removeMemory();
        Assert.assertNull("Memory is null", expressionLocalVariable.getMemory());
        expressionLocalVariable.setMemory(otherMemoryHandle);
        Assert.assertEquals("Memorys are equal", otherMemory, expressionLocalVariable.getMemory().getBean());
        Assert.assertEquals("MemoryHandles are equal", otherMemoryHandle, expressionLocalVariable.getMemory());
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

        expressionLocalVariable.removeMemory();
        Assert.assertNull("memory handle is null", expressionLocalVariable.getMemory());

        expressionLocalVariable.setMemory(memory11);
        Assert.assertTrue("memory is correct", memory11 == expressionLocalVariable.getMemory().getBean());

        expressionLocalVariable.removeMemory();
        Assert.assertNull("memory handle is null", expressionLocalVariable.getMemory());

        expressionLocalVariable.setMemory(memoryHandle12);
        Assert.assertTrue("memory handle is correct", memoryHandle12 == expressionLocalVariable.getMemory());

        expressionLocalVariable.setMemory("A non existent memory");
        Assert.assertNull("memory handle is null", expressionLocalVariable.getMemory());
        JUnitAppender.assertWarnMessage("memory \"A non existent memory\" is not found");

        expressionLocalVariable.setMemory(memory13.getSystemName());
        Assert.assertTrue("memory is correct", memory13 == expressionLocalVariable.getMemory().getBean());

        String userName = memory14.getUserName();
        Assert.assertNotNull("memory is not null", userName);
        expressionLocalVariable.setMemory(userName);
        Assert.assertTrue("memory is correct", memory14 == expressionLocalVariable.getMemory().getBean());
    }

    @Test
    public void testSetMemoryException() {
        // Test setMemory() when listeners are registered
        Assert.assertNotNull("Memory is not null", memory);
        Assert.assertNotNull("Memory is not null", expressionLocalVariable.getMemory());
        expressionLocalVariable.registerListeners();
        boolean thrown = false;
        try {
            expressionLocalVariable.setMemory("A memory");
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
            expressionLocalVariable.setMemory(memoryHandle99);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setMemory must not be called when listeners are registered");

        thrown = false;
        try {
            expressionLocalVariable.setMemory((Memory)null);
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
        expressionLocalVariable.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Memory matches", memory, expressionLocalVariable.getMemory().getBean());

        // Test vetoableChange() for a string
        expressionLocalVariable.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Memory matches", memory, expressionLocalVariable.getMemory().getBean());
        expressionLocalVariable.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Memory matches", memory, expressionLocalVariable.getMemory().getBean());

        // Test vetoableChange() for another memory
        expressionLocalVariable.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", memory, expressionLocalVariable.getMemory().getBean());
        expressionLocalVariable.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", memory, expressionLocalVariable.getMemory().getBean());

        // Test vetoableChange() for its own memory
        boolean thrown = false;
        try {
            expressionLocalVariable.vetoableChange(new PropertyChangeEvent(this, "CanDelete", memory, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        Assert.assertEquals("Memory matches", memory, expressionLocalVariable.getMemory().getBean());
        expressionLocalVariable.vetoableChange(new PropertyChangeEvent(this, "DoDelete", memory, null));
        Assert.assertNull("Memory is null", expressionLocalVariable.getMemory());
    }
*/

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
        conditionalNG.setSymbolTable(new DefaultSymbolTable(conditionalNG));
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        logixNG.addConditionalNG(conditionalNG);

        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        ifThenElse.setExecuteType(IfThenElse.ExecuteType.AlwaysExecute);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);

        expressionLocalVariable = new ExpressionLocalVariable("IQDE321", null);
        localVariableMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionLocalVariable);
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "");
        ifThenElse.getChild(0).connect(localVariableMaleSocket);

        _base = expressionLocalVariable;
        _baseMaleSocket = localVariableMaleSocket;

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        expressionLocalVariable.setLocalVariable("myVar");

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
