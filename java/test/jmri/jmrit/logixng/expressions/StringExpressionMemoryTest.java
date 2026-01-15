package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.DoStringAction;
import jmri.jmrit.logixng.actions.StringActionMemory;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test StringExpressionMemory
 *
 * @author Daniel Bergqvist 2018
 */
public class StringExpressionMemoryTest extends AbstractStringExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private StringExpressionMemory stringExpressionMemory;
    protected Memory _memory;
    protected Memory _memoryOut;

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
        return String.format("Get memory IM1 as string value ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Read string E and set string A ::: Use default%n" +
                "            ?s E%n" +
                "               Get memory IM1 as string value ::: Use default%n" +
                "            !s A%n" +
                "               Set memory IM2 ::: Use default%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new StringExpressionMemory(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        assertNotNull( _base, "object exists");

        StringExpressionMemory expression2;
        assertNotNull( _memory, "memory is not null");
        _memory.setValue(10.2);

        expression2 = new StringExpressionMemory("IQSE11", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Get memory '' as string value", expression2.getLongDescription(), "String matches");

        expression2 = new StringExpressionMemory("IQSE11", "My memory");
        assertNotNull( expression2, "object exists");
        assertEquals( "My memory", expression2.getUserName(), "Username matches");
        assertEquals( "Get memory '' as string value", expression2.getLongDescription(), "String matches");

        expression2 = new StringExpressionMemory("IQSE11", null);
        expression2.getSelectNamedBean().setNamedBean(_memory);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Get memory IM1 as string value", expression2.getLongDescription(), "String matches");

        expression2 = new StringExpressionMemory("IQSE11", "My memory");
        expression2.getSelectNamedBean().setNamedBean(_memory);
        assertNotNull( expression2, "object exists");
        assertEquals( "My memory", expression2.getUserName(), "Username matches");
        assertEquals( "Get memory IM1 as string value", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new StringExpressionMemory("IQA55:12:XY11", null);
            fail("should not have got here " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new StringExpressionMemory("IQA55:12:XY11", "A name");
            fail("should not have got here " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

    }

    @Test
    public void testEvaluate() throws JmriException, SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        StringExpressionMemory expression = (StringExpressionMemory)_base;
        _memory.setValue("");
        assertEquals( "", expression.evaluate(), "Evaluate matches");
        _memory.setValue("Other");
        assertEquals( "Other", expression.evaluate(), "Evaluate matches");
        expression.getSelectNamedBean().removeNamedBean();
        assertEquals( "", expression.evaluate(), "Evaluate matches");
    }

    @Test
    public void testEvaluateAndAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);

        // Set the memory
        _memoryOut.setValue("");
        // The memory should have the value ""
        assertEquals( "", _memoryOut.getValue(), "memory is \"\"");
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue("Test");
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the memory should be 0.0
        assertEquals( "", _memoryOut.getValue(), "memory is \"\"");
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue("Other test");
        // Enable the logixNG and all its children.
        logixNG.setEnabled(true);
        // The action is not yet executed so the memory should be 0.0
        assertEquals( "", _memoryOut.getValue(), "memory is \"\"");
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the value of the memory. This should execute the conditional.
        _memory.setValue("Something else");
        // The action should now be executed so the memory should be 3.0
        assertEquals( "Something else", _memoryOut.getValue(), "memory is \"Something else\"");
        // Disable the conditionalNG and all its children.
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the memory should be 0.0
        assertEquals( "Something else", _memoryOut.getValue(), "memory is \"something else\"");
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue("Something new");
        // The action should not be executed so the memory should still be 3.0
        assertEquals( "Something else", _memoryOut.getValue(), "memory is \"something else\"");
        // Unregister listeners. This should do nothing since the listeners are
        // already unregistered.
        stringExpressionMemory.unregisterListeners();
        // The action is not yet executed so the memory should be 0.0
        assertEquals( "Something else", _memoryOut.getValue(), "memory is \"something else\"");
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue("Something different");
        // The action should not be executed so the memory should still be 3.0
        assertEquals( "Something else", _memoryOut.getValue(), "memory is \"something else\"");

        // Test register listeners when there is no memory.
        stringExpressionMemory.getSelectNamedBean().removeNamedBean();
        stringExpressionMemory.registerListeners();
    }

    @Test
    public void testMemory() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        StringExpressionMemory expressionMemory = (StringExpressionMemory)_base;
        expressionMemory.getSelectNamedBean().removeNamedBean();
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is null");
        expressionMemory.getSelectNamedBean().setNamedBean(_memory);
        assertEquals( _memory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        expressionMemory.getSelectNamedBean().removeNamedBean();
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is null");
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        assertNotNull( otherMemory, "memory is not null");
        NamedBeanHandle<Memory> memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        expressionMemory.getSelectNamedBean().setNamedBean(memoryHandle);
        assertEquals( memoryHandle, expressionMemory.getSelectNamedBean().getNamedBean(), "Memory matches");
        assertEquals( otherMemory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        expressionMemory.getSelectNamedBean().removeNamedBean();
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is null");
        expressionMemory.getSelectNamedBean().setNamedBean(memoryHandle.getName());
        assertEquals( memoryHandle, expressionMemory.getSelectNamedBean().getNamedBean(), "Memory matches");

        // Test getSelectNamedBean().setNamedBean with a memory name that doesn't exists
        expressionMemory.getSelectNamedBean().setNamedBean("Non existent memory");
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is null");
        JUnitAppender.assertErrorMessage("Memory \"Non existent memory\" is not found");

        // Test getSelectNamedBean().setNamedBean() when listeners are registered
        expressionMemory.getSelectNamedBean().setNamedBean(_memory);
        assertNotNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is null");
        // Enable the conditionalNG. This will register the listeners
        conditionalNG.setEnabled(true);
        RuntimeException ex = assertThrows( RuntimeException.class, () ->
            expressionMemory.getSelectNamedBean().setNamedBean(otherMemory),
            "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () ->
            expressionMemory.getSelectNamedBean().removeNamedBean(),
            "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () ->
            expressionMemory.getSelectNamedBean().removeNamedBean(),
            "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");
    }

    @Test
    public void testRegisterListeners() {
        StringExpressionMemory expressionString = (StringExpressionMemory)_base;
        // Test registerListeners() when the ExpressionLight has no light
        conditionalNG.setEnabled(false);
        expressionString.getSelectNamedBean().removeNamedBean();
        conditionalNG.setEnabled(true);
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Get some other memory for later use
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        assertNotNull( otherMemory, "Memory is not null");
        assertNotEquals( _memory, otherMemory, "Memory is not equal");

        // Get the stringExpressionMemory and set the memory
        StringExpressionMemory expression = (StringExpressionMemory)_base;
        expression.getSelectNamedBean().setNamedBean(_memory);
        assertEquals( _memory, expression.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for some other propery
        expression.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( _memory, expression.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for a string
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( _memory, expression.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( _memory, expression.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for another memory
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        assertEquals( _memory, expression.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        assertEquals( _memory, expression.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        // Test vetoableChange() for its own memory
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            expression.getSelectNamedBean().vetoableChange(
                new PropertyChangeEvent(this, "CanDelete", _memory, null)),
            "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( _memory, expression.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        expression.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", _memory, null));
        assertNull( expression.getSelectNamedBean().getNamedBean(), "Memory is null");
    }

    @Test
    public void testCategory() {
        assertEquals( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testShortDescription() {
        assertEquals("Memory as string value", _base.getShortDescription());
    }

    @Test
    public void testLongDescription() {
        assertEquals("Get memory IM1 as string value", _base.getLongDescription());
    }

    @Test
    public void testChild() {
        assertEquals( 0, _base.getChildCount(), "Num children is zero");
        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            _base.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Before
    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLogixNGManager();

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        logixNG.addConditionalNG(conditionalNG);

        DoStringAction doStringAction = new DoStringAction("IQDA321", null);
        MaleSocket maleSocketDoStringAction =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(doStringAction);
        conditionalNG.getChild(0).connect(maleSocketDoStringAction);
        _memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        stringExpressionMemory = new StringExpressionMemory("IQSE321", null);
        MaleSocket maleSocketStringExpressionMemory =
                InstanceManager.getDefault(StringExpressionManager.class).registerExpression(stringExpressionMemory);
        doStringAction.getChild(0).connect(maleSocketStringExpressionMemory);
        stringExpressionMemory.getSelectNamedBean().setNamedBean(_memory);
        _base = stringExpressionMemory;
        _baseMaleSocket = maleSocketStringExpressionMemory;

        _memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        assertNotNull( _memory, "memory is not null");
        _memory.setValue(10.2);

        _memoryOut = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        _memoryOut.setValue("");
        StringActionMemory actionMemory = new StringActionMemory("IQSA1", null);
        actionMemory.getSelectNamedBean().setNamedBean(_memoryOut);
        MaleSocket socketAction = InstanceManager.getDefault(StringActionManager.class).registerAction(actionMemory);
        maleSocketDoStringAction.getChild(1).connect(socketAction);

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
        _base.dispose();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
