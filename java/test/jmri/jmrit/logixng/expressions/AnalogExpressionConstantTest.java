package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AnalogActionMemory;
import jmri.jmrit.logixng.actions.DoAnalogAction;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test AnalogExpressionConstant
 *
 * @author Daniel Bergqvist 2018
 */
public class AnalogExpressionConstantTest extends AbstractAnalogExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private AnalogExpressionConstant expressionConstant;
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
        return String.format("Get analog constant 10.2 ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Read analog E and set analog A ::: Use default%n" +
                "            ?~ E%n" +
                "               Get analog constant 10.2 ::: Use default%n" +
                "            !~ A%n" +
                "               Set memory IM2 ::: Use default%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new AnalogExpressionConstant(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        assertNotNull( _base, "object exists");

        AnalogExpressionConstant expression2;

        expression2 = new AnalogExpressionConstant("IQAE11", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Get analog constant 0", expression2.getLongDescription(Locale.ENGLISH), "String matches");

        expression2 = new AnalogExpressionConstant("IQAE11", "My constant value");
        assertNotNull( expression2, "object exists");
        assertEquals( "My constant value", expression2.getUserName(), "Username matches");
        assertEquals( "Get analog constant 0", expression2.getLongDescription(Locale.ENGLISH), "String matches");

        expression2 = new AnalogExpressionConstant("IQAE11", null);
        expression2.setValue(12.34);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Get analog constant 12.34", expression2.getLongDescription(Locale.ENGLISH), "String matches");

        expression2 = new AnalogExpressionConstant("IQAE11", "My constant");
        expression2.setValue(98.76);
        assertNotNull( expression2, "object exists");
        assertEquals( "My constant", expression2.getUserName(), "Username matches");
        assertEquals( "Get analog constant 98.76", expression2.getLongDescription(Locale.ENGLISH), "String matches");

        // Call setup(). It doesn't do anything, but we call it for coverage
        expression2.setup();

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> {
                AnalogExpressionConstant aec = new AnalogExpressionConstant("IQA55:12:XY11", null);
                assertNull(aec, "Should not reach here");
            },
            "Expected Illegal system name exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class,
            () -> {
                AnalogExpressionConstant aec = new AnalogExpressionConstant("IQA55:12:XY11", "A name");
                assertNull(aec, "Should not reach here");
            },
            "Expected Illegal system name exception thrown");
        assertNotNull(ex);

    }

    @Test
    public void testSetValueWithListenersRegistered() {
        RuntimeException e = assertThrows( RuntimeException.class,
            () -> {
                AnalogExpressionConstant expression = (AnalogExpressionConstant)_base;
                expression.registerListeners();
                expression.setValue(1.2);
            }, "Exception thrown");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("setValue must not be called when listeners are registered");
    }

    @Test
    public void testEvaluate() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        AnalogExpressionConstant expression = (AnalogExpressionConstant)_base;
        expression.setValue(0.0d);
        assertEquals( 0.0d, expression.evaluate(), "Evaluate matches");
        expression.setValue(10.0d);
        assertEquals( 10.0d, expression.evaluate(), "Evaluate matches");
    }

    @Test
    public void testEvaluateAndAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {

        // Set the memory
        _memoryOut.setValue(0.0);
        // The double should be 0.0
        assertEquals( 0.0, (Double)_memoryOut.getValue(), "memory is 0.0");
        // Execute the logixNG
        logixNG.execute();
        // The action is executed so the double should be 10.2
        assertEquals( 10.2, (Double)_memoryOut.getValue(), "memory is 10.2");

        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        // Set the value of the constant.
        expressionConstant.setValue(1.0);
        // Enable the conditionalNG
        conditionalNG.setEnabled(true);
        // Execute the logixNG
        logixNG.execute();
        conditionalNG.setEnabled(false);
        // The action is executed so the double should be 1.0
        assertEquals( 1.0, (Double)_memoryOut.getValue(), "memory is 1.0");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testShortDescription() {
        assertEquals( "Analog constant", _base.getShortDescription(), "String matches");
    }

    @Test
    public void testLongDescription() {
        assertEquals( "Get analog constant 10.2", _base.getLongDescription(Locale.ENGLISH), "String matches");
    }

    @Test
    public void testChild() {
        assertEquals( 0, _base.getChildCount(), "Num children is zero");
        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class,
            () -> _base.getChild(0),
            "Exception is thrown");
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

        expressionConstant = new AnalogExpressionConstant("IQAE321", null);
        expressionConstant.setValue(10.2);

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
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(expressionConstant);
        socketDoAnalog.getChild(0).connect(socketExpression);

        _memoryOut = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        _memoryOut.setValue(0.0);
        actionMemory = new AnalogActionMemory("IQAA1", null);
        actionMemory.getSelectNamedBean().setNamedBean(_memoryOut);
        MaleSocket socketAction =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(actionMemory);
        socketDoAnalog.getChild(1).connect(socketAction);

        _base = expressionConstant;
        _baseMaleSocket = socketExpression;

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
//        JUnitAppender.clearBacklog();
        _base.dispose();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
