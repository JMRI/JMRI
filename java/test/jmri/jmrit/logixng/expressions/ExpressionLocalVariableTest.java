package jmri.jmrit.logixng.expressions;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Local variable '' is equal to \"\"", expression2.getLongDescription());
        
        expression2 = new ExpressionLocalVariable("IQDE321", "My memory");
        expression2.setLocalVariable("");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My memory", expression2.getUserName());
        Assert.assertEquals("String matches", "Local variable '' is equal to \"\"", expression2.getLongDescription());
        
        expression2 = new ExpressionLocalVariable("IQDE321", null);
        expression2.setLocalVariable("myVar");
        Assert.assertEquals("variable is correct", "myVar", expression2.getLocalVariable());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Local variable myVar is equal to \"\"", expression2.getLongDescription());
        
        Memory l = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        expression2 = new ExpressionLocalVariable("IQDE321", "My memory");
        expression2.setLocalVariable("someVar");
        expression2.setMemory(l);
        Assert.assertTrue("memory is correct", l == expression2.getMemory().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My memory", expression2.getUserName());
        Assert.assertEquals("String matches", "Local variable someVar is equal to \"\"", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionLocalVariable("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ExpressionLocalVariable("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionLocalVariable.getChildCount());
        
        boolean hasThrown = false;
        try {
            expressionLocalVariable.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testVariableOperation() {
        Assert.assertEquals("String matches", "is less than", ExpressionLocalVariable.VariableOperation.LessThan.toString());
        Assert.assertEquals("String matches", "is less than or equal", ExpressionLocalVariable.VariableOperation.LessThanOrEqual.toString());
        Assert.assertEquals("String matches", "is equal to", ExpressionLocalVariable.VariableOperation.Equal.toString());
        Assert.assertEquals("String matches", "is greater than or equal to", ExpressionLocalVariable.VariableOperation.GreaterThanOrEqual.toString());
        Assert.assertEquals("String matches", "is greater than", ExpressionLocalVariable.VariableOperation.GreaterThan.toString());
        Assert.assertEquals("String matches", "is not equal to", ExpressionLocalVariable.VariableOperation.NotEqual.toString());
        Assert.assertEquals("String matches", "is null", ExpressionLocalVariable.VariableOperation.IsNull.toString());
        Assert.assertEquals("String matches", "is not null", ExpressionLocalVariable.VariableOperation.IsNotNull.toString());
        Assert.assertEquals("String matches", "does match regular expression", ExpressionLocalVariable.VariableOperation.MatchRegex.toString());
        Assert.assertEquals("String matches", "does not match regular expression", ExpressionLocalVariable.VariableOperation.NotMatchRegex.toString());
        
        Assert.assertTrue("operation has extra value", ExpressionLocalVariable.VariableOperation.LessThan.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionLocalVariable.VariableOperation.LessThanOrEqual.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionLocalVariable.VariableOperation.Equal.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionLocalVariable.VariableOperation.GreaterThanOrEqual.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionLocalVariable.VariableOperation.GreaterThan.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionLocalVariable.VariableOperation.NotEqual.hasExtraValue());
        Assert.assertFalse("operation has not extra value", ExpressionLocalVariable.VariableOperation.IsNull.hasExtraValue());
        Assert.assertFalse("operation has not extra value", ExpressionLocalVariable.VariableOperation.IsNotNull.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionLocalVariable.VariableOperation.MatchRegex.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionLocalVariable.VariableOperation.NotMatchRegex.hasExtraValue());
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
        
        expressionLocalVariable.setLocalVariable("someVar");
        Assert.assertEquals("Local variable", expressionLocalVariable.getShortDescription());
        Assert.assertEquals("Local variable someVar is equal to \"\"", expressionLocalVariable.getLongDescription());
        expressionLocalVariable.setLocalVariable("myVar");
        expressionLocalVariable.setConstantValue("A value");
        Assert.assertEquals("Local variable myVar is equal to \"A value\"", expressionLocalVariable.getLongDescription());
        expressionLocalVariable.setConstantValue("Another value");
        Assert.assertEquals("Local variable myVar is equal to \"Another value\"", expressionLocalVariable.getLongDescription());
        Assert.assertEquals("Local variable myVar is equal to \"Another value\"", expressionLocalVariable.getLongDescription());
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
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the memory. This should execute the conditional.
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
        
        
        // Test regular expression match
        conditionalNG.setEnabled(false);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.MatchRegex);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Value);
        expressionLocalVariable.setConstantValue("Hello.*");
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Hello world");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertTrue("The expression returns true",atomicBoolean.get());
        
        conditionalNG.setEnabled(false);
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Some world");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertFalse("The expression returns false",atomicBoolean.get());
        
        // Test regular expressions
        conditionalNG.setEnabled(false);
        expressionLocalVariable.setConstantValue("\\w\\w\\d+\\s\\d+");
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Ab213 31");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertTrue("The expression returns true",atomicBoolean.get());
        
        conditionalNG.setEnabled(false);
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Ab213_31");     // Underscore instead of space
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertFalse("The expression returns false",atomicBoolean.get());
        
        
        // Test regular expression not match
        conditionalNG.setEnabled(false);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.NotMatchRegex);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Value);
        expressionLocalVariable.setConstantValue("Hello.*");
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Hello world");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertFalse("The expression returns false",atomicBoolean.get());
        
        conditionalNG.setEnabled(false);
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Some world");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertTrue("The expression returns true",atomicBoolean.get());
        
        // Test regular expressions
        conditionalNG.setEnabled(false);
        expressionLocalVariable.setConstantValue("\\w\\w\\d+\\s\\d+");
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Ab213 31");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertFalse("The expression returns false",atomicBoolean.get());
        
        conditionalNG.setEnabled(false);
        // Set the local variable
        localVariableMaleSocket.clearLocalVariables();
        localVariableMaleSocket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Ab213_31");     // Underscore instead of space
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertTrue("The expression returns true",atomicBoolean.get());
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
        conditionalNG.setSymbolTable(new DefaultSymbolTable(conditionalNG));
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        
        logixNG.addConditionalNG(conditionalNG);
        
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        ifThenElse.setType(IfThenElse.Type.AlwaysExecute);
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
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
