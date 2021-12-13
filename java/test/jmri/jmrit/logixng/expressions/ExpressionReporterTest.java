package jmri.jmrit.logixng.expressions;

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
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrit.logixng.Category;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertNotNull("reporter is not null", reporter);
        
        expression2 = new ExpressionReporter("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Reporter '' Current Report is equal to \"\"", expression2.getLongDescription());
        
        expression2 = new ExpressionReporter("IQDE321", "My reporter");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My reporter", expression2.getUserName());
        Assert.assertEquals("String matches", "Reporter '' Current Report is equal to \"\"", expression2.getLongDescription());
        
        expression2 = new ExpressionReporter("IQDE321", null);
        expression2.setReporter(reporter);
        Assert.assertTrue("reporter is correct", reporter == expression2.getReporter().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Reporter IR1 Current Report is equal to \"\"", expression2.getLongDescription());
        
        Reporter l = InstanceManager.getDefault(ReporterManager.class).provide("IR2");
        expression2 = new ExpressionReporter("IQDE321", "My reporter");
        expression2.setReporter(l);
        Assert.assertTrue("reporter is correct", l == expression2.getReporter().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My reporter", expression2.getUserName());
        Assert.assertEquals("String matches", "Reporter IR2 Current Report is equal to \"\"", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionReporter("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ExpressionReporter("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionReporter.getChildCount());
        
        boolean hasThrown = false;
        try {
            expressionReporter.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testReporterOperation() {
        Assert.assertEquals("String matches", "is less than", ExpressionReporter.ReporterOperation.LessThan.toString());
        Assert.assertEquals("String matches", "is less than or equal", ExpressionReporter.ReporterOperation.LessThanOrEqual.toString());
        Assert.assertEquals("String matches", "is equal to", ExpressionReporter.ReporterOperation.Equal.toString());
        Assert.assertEquals("String matches", "is greater than or equal to", ExpressionReporter.ReporterOperation.GreaterThanOrEqual.toString());
        Assert.assertEquals("String matches", "is greater than", ExpressionReporter.ReporterOperation.GreaterThan.toString());
        Assert.assertEquals("String matches", "is not equal to", ExpressionReporter.ReporterOperation.NotEqual.toString());
        Assert.assertEquals("String matches", "is null", ExpressionReporter.ReporterOperation.IsNull.toString());
        Assert.assertEquals("String matches", "is not null", ExpressionReporter.ReporterOperation.IsNotNull.toString());
        Assert.assertEquals("String matches", "does match regular expression", ExpressionReporter.ReporterOperation.MatchRegex.toString());
        Assert.assertEquals("String matches", "does not match regular expression", ExpressionReporter.ReporterOperation.NotMatchRegex.toString());
        
        Assert.assertTrue("operation has extra value", ExpressionReporter.ReporterOperation.LessThan.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionReporter.ReporterOperation.LessThanOrEqual.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionReporter.ReporterOperation.Equal.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionReporter.ReporterOperation.GreaterThanOrEqual.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionReporter.ReporterOperation.GreaterThan.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionReporter.ReporterOperation.NotEqual.hasExtraValue());
        Assert.assertFalse("operation has not extra value", ExpressionReporter.ReporterOperation.IsNull.hasExtraValue());
        Assert.assertFalse("operation has not extra value", ExpressionReporter.ReporterOperation.IsNotNull.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionReporter.ReporterOperation.MatchRegex.hasExtraValue());
        Assert.assertTrue("operation has extra value", ExpressionReporter.ReporterOperation.NotMatchRegex.hasExtraValue());
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.ITEM == _base.getCategory());
    }
    
    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        expressionReporter.removeReporter();
        Assert.assertEquals("Reporter", expressionReporter.getShortDescription());
        Assert.assertEquals("Reporter '' Current Report is equal to \"\"", expressionReporter.getLongDescription());
        expressionReporter.setReporter(reporter);
        expressionReporter.setConstantValue("A value");
        Assert.assertEquals("Reporter IR1 Current Report is equal to \"A value\"", expressionReporter.getLongDescription());
        expressionReporter.setConstantValue("Another value");
        Assert.assertEquals("Reporter IR1 Current Report is equal to \"Another value\"", expressionReporter.getLongDescription());
        Assert.assertEquals("Reporter IR1 Current Report is equal to \"Another value\"", expressionReporter.getLongDescription());
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
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Set the report. This should not execute the conditional.
        reporter.setReport("Other value");
        reporter.setReport("New value");
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Set the report. This should not execute the conditional.
        reporter.setReport("Other value");
        reporter.setReport("New value");
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the report. This should execute the conditional.
        reporter.setReport("Other value");
        reporter.setReport("New value");
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
        
        
        // Test regular expression match
        conditionalNG.setEnabled(false);
        expressionReporter.setReporterOperation(ExpressionReporter.ReporterOperation.MatchRegex);
        expressionReporter.setCompareTo(ExpressionReporter.CompareTo.Value);
        expressionReporter.setRegEx("Hello.*");
        reporter.setReport("Hello world");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertTrue("The expression returns true",atomicBoolean.get());
        
        conditionalNG.setEnabled(false);
        reporter.setReport("Some world");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertFalse("The expression returns false",atomicBoolean.get());
        
        // Test regular expressions
        conditionalNG.setEnabled(false);
        expressionReporter.setRegEx("\\w\\w\\d+\\s\\d+");
        reporter.setReport("Ab213 31");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertTrue("The expression returns true",atomicBoolean.get());
        
        conditionalNG.setEnabled(false);
        reporter.setReport("Ab213_31");    // Underscore instead of space
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertFalse("The expression returns false",atomicBoolean.get());
        
        
        // Test regular expression not match
        conditionalNG.setEnabled(false);
        expressionReporter.setReporterOperation(ExpressionReporter.ReporterOperation.NotMatchRegex);
        expressionReporter.setCompareTo(ExpressionReporter.CompareTo.Value);
        expressionReporter.setRegEx("Hello.*");
        reporter.setReport("Hello world");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertFalse("The expression returns false",atomicBoolean.get());
        
        conditionalNG.setEnabled(false);
        reporter.setReport("Some world");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertTrue("The expression returns true",atomicBoolean.get());
        
        // Test regular expressions
        conditionalNG.setEnabled(false);
        expressionReporter.setRegEx("\\w\\w\\d+\\s\\d+");
        reporter.setReport("Ab213 31");
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertFalse("The expression returns false",atomicBoolean.get());
        
        conditionalNG.setEnabled(false);
        reporter.setReport("Ab213_31");    // Underscore instead of space
        atomicBoolean.set(false);
        Assert.assertFalse("The expression has not executed or returns false",atomicBoolean.get());
        conditionalNG.setEnabled(true);
        Assert.assertTrue("The expression returns true",atomicBoolean.get());
    }
    
    @Test
    public void testSetReporter() {
        expressionReporter.unregisterListeners();
        
        Reporter otherReporter = InstanceManager.getDefault(ReporterManager.class).provide("IR99");
        Assert.assertNotEquals("Reporters are different", otherReporter, expressionReporter.getReporter().getBean());
        expressionReporter.setReporter(otherReporter);
        Assert.assertEquals("Reporters are equal", otherReporter, expressionReporter.getReporter().getBean());
        
        NamedBeanHandle<Reporter> otherReporterHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherReporter.getDisplayName(), otherReporter);
        expressionReporter.removeReporter();
        Assert.assertNull("Reporter is null", expressionReporter.getReporter());
        expressionReporter.setReporter(otherReporterHandle);
        Assert.assertEquals("Reporters are equal", otherReporter, expressionReporter.getReporter().getBean());
        Assert.assertEquals("ReporterHandles are equal", otherReporterHandle, expressionReporter.getReporter());
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
        
        expressionReporter.removeReporter();
        Assert.assertNull("reporter handle is null", expressionReporter.getReporter());
        
        expressionReporter.setReporter(reporter11);
        Assert.assertTrue("reporter is correct", reporter11 == expressionReporter.getReporter().getBean());
        
        expressionReporter.removeReporter();
        Assert.assertNull("reporter handle is null", expressionReporter.getReporter());
        
        expressionReporter.setReporter(memoryHandle12);
        Assert.assertTrue("reporter handle is correct", memoryHandle12 == expressionReporter.getReporter());
        
        expressionReporter.setReporter("A non existent reporter");
        Assert.assertNull("reporter handle is null", expressionReporter.getReporter());
        JUnitAppender.assertWarnMessage("reporter \"A non existent reporter\" is not found");
        
        expressionReporter.setReporter(reporter13.getSystemName());
        Assert.assertTrue("reporter is correct", reporter13 == expressionReporter.getReporter().getBean());
        
        String userName = reporter14.getUserName();
        Assert.assertNotNull("reporter is not null", userName);
        expressionReporter.setReporter(userName);
        Assert.assertTrue("reporter is correct", reporter14 == expressionReporter.getReporter().getBean());
    }
    
    @Test
    public void testSetReporterException() {
        // Test setReporter() when listeners are registered
        Assert.assertNotNull("Reporter is not null", reporter);
        Assert.assertNotNull("Reporter is not null", expressionReporter.getReporter());
        expressionReporter.registerListeners();
        boolean thrown = false;
        try {
            expressionReporter.setReporter("A memory");
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setReporter must not be called when listeners are registered");
        
        thrown = false;
        try {
            Reporter reporter99 = InstanceManager.getDefault(ReporterManager.class).provide("IR99");
            NamedBeanHandle<Reporter> reporterHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(reporter99.getDisplayName(), reporter99);
            expressionReporter.setReporter(reporterHandle99);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setReporter must not be called when listeners are registered");
        
        thrown = false;
        try {
            expressionReporter.setReporter((Reporter)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setReporter must not be called when listeners are registered");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        // Get the expression and set the memory
        Assert.assertNotNull("Reporter is not null", reporter);
        
        // Get some other memory for later use
        Reporter otherReporter = InstanceManager.getDefault(ReporterManager.class).provide("IR99");
        Assert.assertNotNull("Reporter is not null", otherReporter);
        Assert.assertNotEquals("Reporter is not equal", reporter, otherReporter);
        
        // Test vetoableChange() for some other propery
        expressionReporter.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Reporter matches", reporter, expressionReporter.getReporter().getBean());
        
        // Test vetoableChange() for a string
        expressionReporter.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Reporter matches", reporter, expressionReporter.getReporter().getBean());
        expressionReporter.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Reporter matches", reporter, expressionReporter.getReporter().getBean());
        
        // Test vetoableChange() for another memory
        expressionReporter.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherReporter, null));
        Assert.assertEquals("Reporter matches", reporter, expressionReporter.getReporter().getBean());
        expressionReporter.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherReporter, null));
        Assert.assertEquals("Reporter matches", reporter, expressionReporter.getReporter().getBean());
        
        // Test vetoableChange() for its own memory
        boolean thrown = false;
        try {
            expressionReporter.vetoableChange(new PropertyChangeEvent(this, "CanDelete", reporter, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Reporter matches", reporter, expressionReporter.getReporter().getBean());
        expressionReporter.vetoableChange(new PropertyChangeEvent(this, "DoDelete", reporter, null));
        Assert.assertNull("Reporter is null", expressionReporter.getReporter());
    }
    
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
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        
        logixNG.addConditionalNG(conditionalNG);
        
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        ifThenElse.setType(IfThenElse.Type.AlwaysExecute);
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
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);
        
        expressionReporter.setReporterValue(ExpressionReporter.ReporterValue.CurrentReport);
        reporter = InstanceManager.getDefault(ReporterManager.class).provide("IR1");
        expressionReporter.setReporter(reporter);
        reporter.setReport("");
        
        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
