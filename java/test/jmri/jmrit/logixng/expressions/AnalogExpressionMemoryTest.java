package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AnalogActionMemory;
import jmri.jmrit.logixng.actions.DoAnalogAction;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
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
        Assert.assertTrue("object exists", _base != null);

        AnalogExpressionMemory expression2;
        Assert.assertNotNull("memory is not null", _memory);
        _memory.setValue(10.2);

        expression2 = new AnalogExpressionMemory("IQAE11", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertTrue("Username matches", null == expression2.getUserName());
        Assert.assertEquals("String matches", "Get memory '' as analog value", expression2.getLongDescription());

        expression2 = new AnalogExpressionMemory("IQAE11", "My memory");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertTrue("Username matches", "My memory".equals(expression2.getUserName()));
        Assert.assertEquals("String matches", "Get memory '' as analog value", expression2.getLongDescription());

        expression2 = new AnalogExpressionMemory("IQAE11", null);
        expression2.getSelectNamedBean().setNamedBean(_memory);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertTrue("Username matches", null == expression2.getUserName());
        Assert.assertEquals("String matches", "Get memory IM1 as analog value", expression2.getLongDescription());

        expression2 = new AnalogExpressionMemory("IQAE11", "My memory");
        expression2.getSelectNamedBean().setNamedBean(_memory);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertTrue("Username matches", "My memory".equals(expression2.getUserName()));
        Assert.assertEquals("String matches", "Get memory IM1 as analog value", expression2.getLongDescription());

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
    public void testEvaluate() throws JmriException, SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        AnalogExpressionMemory expression = (AnalogExpressionMemory)_base;
        _memory.setValue(0.0d);
        Assert.assertTrue("Evaluate matches", 0.0d == expression.evaluate());
        _memory.setValue(10.0d);
        Assert.assertTrue("Evaluate matches", 10.0d == expression.evaluate());
        expression.getSelectNamedBean().removeNamedBean();
        Assert.assertTrue("Evaluate matches", 0.0d == expression.evaluate());
    }

    @Test
    public void testEvaluateAndAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        // Set the memory
        _memoryOut.setValue(0.0);
        // The double should be 0.0
        Assert.assertTrue("memory is 0.0", 0.0 == (Double)_memoryOut.getValue());
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue(1.0);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the memory should be 0.0
        Assert.assertTrue("memory is 0.0", 0.0 == (Double)_memoryOut.getValue());
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue(2.0);
        // The action is not yet executed so the memory should be 0.0
        Assert.assertTrue("memory is 0.0", 0.0 == (Double)_memoryOut.getValue());
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
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
        expressionMemory.getSelectNamedBean().removeNamedBean();
        expressionMemory.registerListeners();
    }

    @Test
    public void testMemory() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        AnalogExpressionMemory expressionMemory = (AnalogExpressionMemory)_base;
        expressionMemory.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("Memory is null", expressionMemory.getSelectNamedBean().getNamedBean());
        expressionMemory.getSelectNamedBean().setNamedBean(_memory);
        Assert.assertTrue("Memory matches", _memory == expressionMemory.getSelectNamedBean().getNamedBean().getBean());

        expressionMemory.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("Memory is null", expressionMemory.getSelectNamedBean().getNamedBean());
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("memory is not null", otherMemory);
        NamedBeanHandle<Memory> memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        expressionMemory.getSelectNamedBean().setNamedBean(memoryHandle);
        Assert.assertTrue("Memory matches", memoryHandle == expressionMemory.getSelectNamedBean().getNamedBean());
        Assert.assertTrue("Memory matches", otherMemory == expressionMemory.getSelectNamedBean().getNamedBean().getBean());

        expressionMemory.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("Memory is null", expressionMemory.getSelectNamedBean().getNamedBean());
        expressionMemory.getSelectNamedBean().setNamedBean(memoryHandle.getName());
        Assert.assertTrue("Memory matches", memoryHandle == expressionMemory.getSelectNamedBean().getNamedBean());

        // Test getSelectNamedBean().setNamedBean with a memory name that doesn't exists
        expressionMemory.getSelectNamedBean().setNamedBean("Non existent memory");
        Assert.assertNull("Memory is null", expressionMemory.getSelectNamedBean().getNamedBean());
        JUnitAppender.assertErrorMessage("Memory \"Non existent memory\" is not found");

        // Test getSelectNamedBean().setNamedBean() when listeners are registered
        expressionMemory.getSelectNamedBean().setNamedBean(_memory);
        Assert.assertNotNull("Memory is null", expressionMemory.getSelectNamedBean().getNamedBean());
        // Enable the conditionalNG. This will register the listeners
        conditionalNG.setEnabled(true);
        boolean thrown = false;
        try {
            expressionMemory.getSelectNamedBean().setNamedBean(otherMemory);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        thrown = false;
        try {
            expressionMemory.getSelectNamedBean().removeNamedBean();
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        thrown = false;
        try {
            expressionMemory.getSelectNamedBean().removeNamedBean();
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
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
        Assert.assertNotNull("Memory is not null", otherMemory);
        Assert.assertNotEquals("Memory is not equal", _memory, otherMemory);

        // Get the expression and set the memory
        AnalogExpressionMemory expression = (AnalogExpressionMemory)_base;
        expression.getSelectNamedBean().setNamedBean(_memory);
        Assert.assertEquals("Memory matches", _memory, expression.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for some other propery
        expression.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Memory matches", _memory, expression.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for a string
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Memory matches", _memory, expression.getSelectNamedBean().getNamedBean().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Memory matches", _memory, expression.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for another memory
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", _memory, expression.getSelectNamedBean().getNamedBean().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", _memory, expression.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for its own memory
        boolean thrown = false;
        try {
            expression.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", _memory, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        Assert.assertEquals("Memory matches", _memory, expression.getSelectNamedBean().getNamedBean().getBean());
        expression.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", _memory, null));
        Assert.assertNull("Memory is null", expression.getSelectNamedBean().getNamedBean());
    }

    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.ITEM == _base.getCategory());
    }

    @Test
    public void testShortDescription() {
        Assert.assertEquals("String matches", "Memory as analog value", _base.getShortDescription());
    }

    @Test
    public void testLongDescription() {
        Assert.assertEquals("Get memory IM1 as analog value", _base.getLongDescription());
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

        _memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        Assert.assertNotNull("memory is not null", _memory);
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

        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        _base.dispose();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
