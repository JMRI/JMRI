package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AnalogActionMemory;
import jmri.jmrit.logixng.actions.DoAnalogAction;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    public MaleSocket getConnectableChild() {
        return null;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format("Get memory IM1 as analog value ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Read analog E and set analog A ::: Use default%n" +
                "            ?~ E%n" +
                "               Get memory IM1 as analog value ::: Use default%n" +
                "            !~ A%n" +
                "               Set memory IM2 ::: Use default%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new AnalogExpressionMemory(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        assertNotNull( _base, "object exists");

        AnalogExpressionMemory expression2;
        assertNotNull( _memory, "memory is not null");
        _memory.setValue(10.2);

        expression2 = new AnalogExpressionMemory("IQAE11", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Get memory '' as analog value", expression2.getLongDescription(), "String matches");

        expression2 = new AnalogExpressionMemory("IQAE11", "My memory");
        assertNotNull( expression2, "object exists");
        assertEquals( "My memory", expression2.getUserName(), "Username matches");
        assertEquals( "Get memory '' as analog value", expression2.getLongDescription(), "String matches");

        expression2 = new AnalogExpressionMemory("IQAE11", null);
        expression2.getSelectNamedBean().setNamedBean(_memory);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Get memory IM1 as analog value", expression2.getLongDescription(), "String matches");

        expression2 = new AnalogExpressionMemory("IQAE11", "My memory");
        expression2.getSelectNamedBean().setNamedBean(_memory);
        assertNotNull( expression2, "object exists");
        assertEquals( "My memory", expression2.getUserName(), "Username matches");
        assertEquals( "Get memory IM1 as analog value", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new AnalogExpressionMemory("IQA55:12:XY11", null);
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new AnalogExpressionMemory("IQA55:12:XY11", "A name");
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testEvaluate() throws JmriException, SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        AnalogExpressionMemory expression = (AnalogExpressionMemory)_base;
        _memory.setValue(0.0d);
        assertEquals( 0.0d, expression.evaluate(), "Evaluate matches");
        _memory.setValue(10.0d);
        assertEquals( 10.0d, expression.evaluate(), "Evaluate matches");
        expression.getSelectNamedBean().removeNamedBean();
        assertEquals( 0.0d, expression.evaluate(), "Evaluate matches");
    }

    @Test
    public void testEvaluateAndAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        // Set the memory
        _memoryOut.setValue(0.0);
        // The double should be 0.0
        assertEquals( 0.0, (Double)_memoryOut.getValue(), "memory is 0.0");
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue(1.0);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the memory should be 0.0
        assertEquals( 0.0, (Double)_memoryOut.getValue(), "memory is 0.0");
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue(2.0);
        // The action is not yet executed so the memory should be 0.0
        assertEquals( 0.0, (Double)_memoryOut.getValue(), "memory is 0.0");
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the value of the memory. This should execute the conditional.
        _memory.setValue(3.0);
        // The action should now be executed so the memory should be 3.0
        assertEquals( 3.0, (Double)_memoryOut.getValue(), "memory is 3.0");
        // Disable the conditionalNG and all its children.
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the memory should be 0.0
        assertEquals( 3.0, (Double)_memoryOut.getValue(), "memory is 3.0");
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue(4.0);
        // The action should not be executed so the memory should still be 3.0
        assertEquals( 3.0, (Double)_memoryOut.getValue(), "memory is 3.0");
        // Unregister listeners. This should do nothing since the listeners are
        // already unregistered.
        expressionMemory.unregisterListeners();
        // The memory should be 3.0
        assertEquals( 3.0, (Double)_memoryOut.getValue(), "memory is 3.0");
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue(5.0);
        // The action should not be executed so the memory should still be 3.0
        assertEquals( 3.0, (Double)_memoryOut.getValue(), "memory is 3.0");

        // Test register listeners when there is no memory.
        expressionMemory.getSelectNamedBean().removeNamedBean();
        expressionMemory.registerListeners();
    }

    @Test
    public void testMemory() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionMemory = (AnalogExpressionMemory)_base;
        expressionMemory.getSelectNamedBean().removeNamedBean();
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is null");
        expressionMemory.getSelectNamedBean().setNamedBean(_memory);
        assertSame( _memory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        expressionMemory.getSelectNamedBean().removeNamedBean();
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is null");
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        assertNotNull( otherMemory, "memory is not null");
        NamedBeanHandle<Memory> memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        expressionMemory.getSelectNamedBean().setNamedBean(memoryHandle);
        assertSame( memoryHandle, expressionMemory.getSelectNamedBean().getNamedBean(), "Memory matches");
        assertSame( otherMemory, expressionMemory.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");

        expressionMemory.getSelectNamedBean().removeNamedBean();
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is null");
        expressionMemory.getSelectNamedBean().setNamedBean(memoryHandle.getName());
        assertSame( memoryHandle, expressionMemory.getSelectNamedBean().getNamedBean(), "Memory matches");

        // Test getSelectNamedBean().setNamedBean with a memory name that doesn't exists
        expressionMemory.getSelectNamedBean().setNamedBean("Non existent memory");
        assertNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is null");
        JUnitAppender.assertErrorMessage("Memory \"Non existent memory\" is not found");

        // Test getSelectNamedBean().setNamedBean() when listeners are registered
        expressionMemory.getSelectNamedBean().setNamedBean(_memory);
        assertNotNull( expressionMemory.getSelectNamedBean().getNamedBean(), "Memory is Not null");
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
        // Test registerListeners() when the ExpressionMemory has no memory
        conditionalNG.setEnabled(false);
        expressionMemory.getSelectNamedBean().removeNamedBean();
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

        // Get the expression and set the memory
        AnalogExpressionMemory expression = (AnalogExpressionMemory)_base;
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
            expression.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", _memory, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( _memory, expression.getSelectNamedBean().getNamedBean().getBean(), "Memory matches");
        expression.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", _memory, null));
        assertNull( expression.getSelectNamedBean().getNamedBean(), "Memory is null");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testShortDescription() {
        assertEquals( "Memory as analog value", _base.getShortDescription(), "String matches");
    }

    @Test
    public void testLongDescription() {
        assertEquals("Get memory IM1 as analog value", _base.getLongDescription());
    }

    @Test
    public void testChild() {
        assertEquals( 0, _base.getChildCount(), "Num children is zero");
        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            _base.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

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

        _memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        assertNotNull( _memory, "memory is not null");
        _memory.setValue(10.2);
        expressionMemory = new AnalogExpressionMemory("IQAE321", null);
        expressionMemory.getSelectNamedBean().setNamedBean(_memory);

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        logixNG.addConditionalNG(conditionalNG);

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
        actionMemory.getSelectNamedBean().setNamedBean(_memoryOut);
        MaleSocket socketAction =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(actionMemory);
        socketDoAnalog.getChild(1).connect(socketAction);

        _base = expressionMemory;
        _baseMaleSocket = socketExpression;

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        _base.dispose();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
