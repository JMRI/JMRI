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
import java.util.concurrent.atomic.AtomicLong;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.NamedBean;
import jmri.NamedBeanHandleManager;
import jmri.Reporter;
import jmri.ReporterManager;
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

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ExpressionReport
 *
 * @author Daniel Bergqvist 2021
 */
public class ExpressionReporterTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionReporter expressionReporter;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    private AtomicLong atomicCounter;
    private Reporter reporter;


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
        return String.format("Reporter IR1 Current Report is equal to \"\" ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Always execute ::: Use default%n" +
                "            ? If%n" +
                "               Reporter IR1 Current Report is equal to \"\" ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionReporter(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        ExpressionReporter expression2;
        assertNotNull( reporter, "reporter is not null");

        expression2 = new ExpressionReporter("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Reporter '' Current Report is equal to \"\"", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionReporter("IQDE321", "My reporter");
        assertNotNull( expression2, "object exists");
        assertEquals( "My reporter", expression2.getUserName(), "Username matches");
        assertEquals( "Reporter '' Current Report is equal to \"\"", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionReporter("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(reporter);
        assertSame( reporter, expression2.getSelectNamedBean().getNamedBean().getBean(), "reporter is correct");
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Reporter IR1 Current Report is equal to \"\"", expression2.getLongDescription(), "String matches");

        Reporter l = InstanceManager.getDefault(ReporterManager.class).provide("IR2");
        expression2 = new ExpressionReporter("IQDE321", "My reporter");
        expression2.getSelectNamedBean().setNamedBean(l);
        assertEquals( l, expression2.getSelectNamedBean().getNamedBean().getBean(), "reporter is correct");
        assertNotNull( expression2, "object exists");
        assertEquals( "My reporter", expression2.getUserName(), "Username matches");
        assertEquals( "Reporter IR2 Current Report is equal to \"\"", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionReporter("IQE55:12:XY11", null);
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionReporter("IQE55:12:XY11", "A name");
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionReporter.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            expressionReporter.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testReporterOperation() {
        assertEquals( "is less than", ExpressionReporter.ReporterOperation.LessThan.toString(), "String matches");
        assertEquals( "is less than or equal", ExpressionReporter.ReporterOperation.LessThanOrEqual.toString(), "String matches");
        assertEquals( "is equal to", ExpressionReporter.ReporterOperation.Equal.toString(), "String matches");
        assertEquals( "is greater than or equal to", ExpressionReporter.ReporterOperation.GreaterThanOrEqual.toString(), "String matches");
        assertEquals( "is greater than", ExpressionReporter.ReporterOperation.GreaterThan.toString(), "String matches");
        assertEquals( "is not equal to", ExpressionReporter.ReporterOperation.NotEqual.toString(), "String matches");
        assertEquals( "is null", ExpressionReporter.ReporterOperation.IsNull.toString(), "String matches");
        assertEquals( "is not null", ExpressionReporter.ReporterOperation.IsNotNull.toString(), "String matches");
        assertEquals( "does match regular expression", ExpressionReporter.ReporterOperation.MatchRegex.toString(), "String matches");
        assertEquals( "does not match regular expression", ExpressionReporter.ReporterOperation.NotMatchRegex.toString(), "String matches");

        assertTrue( ExpressionReporter.ReporterOperation.LessThan.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionReporter.ReporterOperation.LessThanOrEqual.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionReporter.ReporterOperation.Equal.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionReporter.ReporterOperation.GreaterThanOrEqual.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionReporter.ReporterOperation.GreaterThan.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionReporter.ReporterOperation.NotEqual.hasExtraValue(), "operation has extra value");
        assertFalse( ExpressionReporter.ReporterOperation.IsNull.hasExtraValue(), "operation has not extra value");
        assertFalse( ExpressionReporter.ReporterOperation.IsNotNull.hasExtraValue(), "operation has not extra value");
        assertTrue( ExpressionReporter.ReporterOperation.MatchRegex.hasExtraValue(), "operation has extra value");
        assertTrue( ExpressionReporter.ReporterOperation.NotMatchRegex.hasExtraValue(), "operation has extra value");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionReporter.getSelectNamedBean().removeNamedBean();
        assertEquals("Reporter", expressionReporter.getShortDescription());
        assertEquals("Reporter '' Current Report is equal to \"\"", expressionReporter.getLongDescription());
        expressionReporter.getSelectNamedBean().setNamedBean(reporter);
        expressionReporter.setConstantValue("A value");
        assertEquals("Reporter IR1 Current Report is equal to \"A value\"", expressionReporter.getLongDescription());
        expressionReporter.setConstantValue("Another value");
        assertEquals("Reporter IR1 Current Report is equal to \"Another value\"", expressionReporter.getLongDescription());
        assertEquals("Reporter IR1 Current Report is equal to \"Another value\"", expressionReporter.getLongDescription());
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Clear flag
        atomicBoolean.set(false);
        // Set the report
        reporter.setReport("New value");

        // Disable the conditionalNG
        conditionalNG.setEnabled(false);

        expressionReporter.setCompareTo(ExpressionReporter.CompareTo.Value);
        expressionReporter.setReporterOperation(ExpressionReporter.ReporterOperation.Equal);
        expressionReporter.setConstantValue("New value");

        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Set the report. This should not execute the conditional.
        reporter.setReport("Other value");
        reporter.setReport("New value");
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Set the report. This should not execute the conditional.
        reporter.setReport("Other value");
        reporter.setReport("New value");
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the report. This should execute the conditional.
        reporter.setReport("Other value");
        reporter.setReport("New value");
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");


        // Test regular expression match
        conditionalNG.setEnabled(false);
        expressionReporter.setReporterOperation(ExpressionReporter.ReporterOperation.MatchRegex);
        expressionReporter.setCompareTo(ExpressionReporter.CompareTo.Value);
        expressionReporter.setRegEx("Hello.*");
        reporter.setReport("Hello world");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertTrue( atomicBoolean.get(), "The expression returns true");

        conditionalNG.setEnabled(false);
        reporter.setReport("Some world");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertFalse( atomicBoolean.get(), "The expression returns false");

        // Test regular expressions
        conditionalNG.setEnabled(false);
        expressionReporter.setRegEx("\\w\\w\\d+\\s\\d+");
        reporter.setReport("Ab213 31");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertTrue( atomicBoolean.get(), "The expression returns true");

        conditionalNG.setEnabled(false);
        reporter.setReport("Ab213_31");    // Underscore instead of space
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertFalse( atomicBoolean.get(), "The expression returns false");


        // Test regular expression not match
        conditionalNG.setEnabled(false);
        expressionReporter.setReporterOperation(ExpressionReporter.ReporterOperation.NotMatchRegex);
        expressionReporter.setCompareTo(ExpressionReporter.CompareTo.Value);
        expressionReporter.setRegEx("Hello.*");
        reporter.setReport("Hello world");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertFalse( atomicBoolean.get(), "The expression returns false");

        conditionalNG.setEnabled(false);
        reporter.setReport("Some world");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertTrue( atomicBoolean.get(), "The expression returns true");

        // Test regular expressions
        conditionalNG.setEnabled(false);
        expressionReporter.setRegEx("\\w\\w\\d+\\s\\d+");
        reporter.setReport("Ab213 31");
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertFalse( atomicBoolean.get(), "The expression returns false");

        conditionalNG.setEnabled(false);
        reporter.setReport("Ab213_31");    // Underscore instead of space
        atomicBoolean.set(false);
        assertFalse( atomicBoolean.get(), "The expression has not executed or returns false");
        conditionalNG.setEnabled(true);
        assertTrue( atomicBoolean.get(), "The expression returns true");


        long counter;
        expressionReporter.setReporterOperation(ExpressionReporter.ReporterOperation.Equal);
        expressionReporter.setConstantValue("A report");
        expressionReporter.setReporterValue(ExpressionReporter.ReporterValue.CurrentReport);
        // Clear flag
        atomicBoolean.set(false);
        atomicCounter.set(0);
        counter = 0;
        // Clear report
        reporter.setReport(null);
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Set the report. This should not execute the conditional.
        reporter.setReport("A report");
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter,atomicCounter.get(), "counter is correct");
        // Clear the report. This should not execute the conditional.
        reporter.setReport(null);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Set the report. This should execute the conditional.
        reporter.setReport("A report");
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
        assertEquals( ++counter, atomicCounter.get(), "counter is correct");
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Clear the report. This should not execute the conditional.
        reporter.setReport(null);
        // The action should now not be executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter,atomicCounter.get(), "counter is correct");


        expressionReporter.setReporterOperation(ExpressionReporter.ReporterOperation.IsNull);
        expressionReporter.setReporterValue(ExpressionReporter.ReporterValue.CurrentReport);
        // Clear flag
        atomicBoolean.set(false);
        atomicCounter.set(0);
        counter = 0;
        // Clear report
        reporter.setReport(null);
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Set the report. This should not execute the conditional.
        reporter.setReport("A report");
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Clear the report. This should not execute the conditional.
        reporter.setReport("Some report");
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Clear the report. This should execute the conditional.
        reporter.setReport(null);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
        assertEquals( ++counter, atomicCounter.get(), "counter is correct");
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Set the report. This should not execute the conditional.
        reporter.setReport("A report");
        // The action should now not be executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");

        reporter.setReport("Some other report");


        expressionReporter.setReporterOperation(ExpressionReporter.ReporterOperation.Equal);
        expressionReporter.setConstantValue("A report");
        expressionReporter.setReporterValue(ExpressionReporter.ReporterValue.LastReport);
        // Clear flag
        atomicBoolean.set(false);
        atomicCounter.set(0);
        counter = 0;
        // Clear report
        reporter.setReport(null);
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Set the report. This should not execute the conditional.
        reporter.setReport("A report");
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Clear the report. This should not execute the conditional.
        reporter.setReport("Some report");
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Set the report. This should execute the conditional.
        reporter.setReport("A report");
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
        assertEquals( ++counter, atomicCounter.get(), "counter is correct");
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Clear the report. This should not execute the conditional.
        reporter.setReport(null);
        // The action should now not be executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");


        expressionReporter.setReporterOperation(ExpressionReporter.ReporterOperation.IsNull);
        expressionReporter.setReporterValue(ExpressionReporter.ReporterValue.LastReport);
        // Clear flag
        atomicBoolean.set(false);
        atomicCounter.set(0);
        counter = 0;
        // Clear report
        reporter.setReport(null);
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Set the report. This should not execute the conditional.
        reporter.setReport("A report");
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Clear the report. This should not execute the conditional.
        reporter.setReport("Some report");
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Clear the report. This should not execute the conditional.
        reporter.setReport(null);
        // The action should now not be executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Set the report. This should not execute the conditional.
        reporter.setReport("A report");
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");


        expressionReporter.setReporterOperation(ExpressionReporter.ReporterOperation.Equal);
        expressionReporter.setConstantValue(Integer.toString(jmri.DigitalIO.ON));
        expressionReporter.setReporterValue(ExpressionReporter.ReporterValue.State);
        // Clear flag
        atomicBoolean.set(false);
        atomicCounter.set(0);
        counter = 0;
        // Set state UNKNOWN
        reporter.setState(NamedBean.UNKNOWN);
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Set state ON. This should not execute the conditional.
        reporter.setState(jmri.DigitalIO.ON);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Set state UNKNOWN. This should not execute the conditional.
        reporter.setState(NamedBean.UNKNOWN);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
        // Set state ON. This should execute the conditional.
        reporter.setState(NamedBean.INCONSISTENT);
        reporter.setState(jmri.DigitalIO.ON);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // setState() doesn't give a property change event so we need to call conditionalNG.execute()
        conditionalNG.execute();
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
        assertEquals( ++counter, atomicCounter.get(), "counter is correct");
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Set state UNKNOWN. This should not execute the conditional.
        reporter.setState(NamedBean.UNKNOWN);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        assertEquals( counter, atomicCounter.get(), "counter is correct");
    }

    @Test
    public void testSetReporter() {
        expressionReporter.unregisterListeners();

        Reporter otherReporter = InstanceManager.getDefault(ReporterManager.class).provide("IR99");
        assertNotEquals( otherReporter, expressionReporter.getSelectNamedBean().getNamedBean().getBean(), "Reporters are different");
        expressionReporter.getSelectNamedBean().setNamedBean(otherReporter);
        assertEquals( otherReporter, expressionReporter.getSelectNamedBean().getNamedBean().getBean(), "Reporters are equal");

        NamedBeanHandle<Reporter> otherReporterHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherReporter.getDisplayName(), otherReporter);
        expressionReporter.getSelectNamedBean().removeNamedBean();
        assertNull( expressionReporter.getSelectNamedBean().getNamedBean(), "Reporter is null");
        expressionReporter.getSelectNamedBean().setNamedBean(otherReporterHandle);
        assertEquals( otherReporter, expressionReporter.getSelectNamedBean().getNamedBean().getBean(), "Reporters are equal");
        assertEquals( otherReporterHandle, expressionReporter.getSelectNamedBean().getNamedBean(), "ReporterHandles are equal");
    }

    @Test
    public void testSetReporter2() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        Reporter reporter11 = InstanceManager.getDefault(ReporterManager.class).provide("IR11");
        Reporter reporter12 = InstanceManager.getDefault(ReporterManager.class).provide("IR12");
        NamedBeanHandle<Reporter> memoryHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(reporter12.getDisplayName(), reporter12);
        Reporter reporter13 = InstanceManager.getDefault(ReporterManager.class).provide("IR13");
        Reporter reporter14 = InstanceManager.getDefault(ReporterManager.class).provide("IR14");
        reporter14.setUserName("Some user name");

        expressionReporter.getSelectNamedBean().removeNamedBean();
        assertNull( expressionReporter.getSelectNamedBean().getNamedBean(), "reporter handle is null");

        expressionReporter.getSelectNamedBean().setNamedBean(reporter11);
        assertSame( reporter11, expressionReporter.getSelectNamedBean().getNamedBean().getBean(), "reporter is correct");

        expressionReporter.getSelectNamedBean().removeNamedBean();
        assertNull( expressionReporter.getSelectNamedBean().getNamedBean(), "reporter handle is null");

        expressionReporter.getSelectNamedBean().setNamedBean(memoryHandle12);
        assertSame( memoryHandle12, expressionReporter.getSelectNamedBean().getNamedBean(), "reporter handle is correct");

        expressionReporter.getSelectNamedBean().setNamedBean("A non existent reporter");
        assertNull( expressionReporter.getSelectNamedBean().getNamedBean(), "reporter handle is null");
        JUnitAppender.assertErrorMessage("Reporter \"A non existent reporter\" is not found");

        expressionReporter.getSelectNamedBean().setNamedBean(reporter13.getSystemName());
        assertSame( reporter13, expressionReporter.getSelectNamedBean().getNamedBean().getBean(), "reporter is correct");

        String userName = reporter14.getUserName();
        assertNotNull( userName, "reporter is not null");
        expressionReporter.getSelectNamedBean().setNamedBean(userName);
        assertSame( reporter14, expressionReporter.getSelectNamedBean().getNamedBean().getBean(), "reporter is correct");
    }

    @Test
    public void testSetReporterException() {
        // Test setReporter() when listeners are registered
        assertNotNull( reporter, "Reporter is not null");
        assertNotNull( expressionReporter.getSelectNamedBean().getNamedBean(), "Reporter is not null");
        expressionReporter.registerListeners();
        RuntimeException ex = assertThrows( RuntimeException.class, () ->
            expressionReporter.getSelectNamedBean().setNamedBean("A memory"),
                "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () -> {
            Reporter reporter99 = InstanceManager.getDefault(ReporterManager.class).provide("IR99");
            NamedBeanHandle<Reporter> reporterHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(reporter99.getDisplayName(), reporter99);
            expressionReporter.getSelectNamedBean().setNamedBean(reporterHandle99);
        }, "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Get the expression and set the memory
        assertNotNull( reporter, "Reporter is not null");

        // Get some other memory for later use
        Reporter otherReporter = InstanceManager.getDefault(ReporterManager.class).provide("IR99");
        assertNotNull( otherReporter, "Reporter is not null");
        assertNotEquals( reporter, otherReporter, "Reporter is not equal");

        // Test vetoableChange() for some other propery
        expressionReporter.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( reporter, expressionReporter.getSelectNamedBean().getNamedBean().getBean(), "Reporter matches");

        // Test vetoableChange() for a string
        expressionReporter.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( reporter, expressionReporter.getSelectNamedBean().getNamedBean().getBean(), "Reporter matches");
        expressionReporter.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( reporter, expressionReporter.getSelectNamedBean().getNamedBean().getBean(), "Reporter matches");

        // Test vetoableChange() for another memory
        expressionReporter.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherReporter, null));
        assertEquals( reporter, expressionReporter.getSelectNamedBean().getNamedBean().getBean(), "Reporter matches");
        expressionReporter.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherReporter, null));
        assertEquals( reporter, expressionReporter.getSelectNamedBean().getNamedBean().getBean(), "Reporter matches");

        // Test vetoableChange() for its own memory
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            expressionReporter.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", reporter, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( reporter, expressionReporter.getSelectNamedBean().getNamedBean().getBean(), "Reporter matches");
        expressionReporter.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", reporter, null));
        assertNull( expressionReporter.getSelectNamedBean().getNamedBean(), "Reporter is null");
    }

    @Before
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

        expressionReporter = new ExpressionReporter("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionReporter);
        ifThenElse.getChild(0).connect(maleSocket2);

        _base = expressionReporter;
        _baseMaleSocket = maleSocket2;

        atomicBoolean = new AtomicBoolean(false);
        atomicCounter = new AtomicLong(0);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true, atomicCounter);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        expressionReporter.setReporterValue(ExpressionReporter.ReporterValue.CurrentReport);
        reporter = InstanceManager.getDefault(ReporterManager.class).provide("IR1");
        expressionReporter.getSelectNamedBean().setNamedBean(reporter);
        reporter.setReport("");

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
