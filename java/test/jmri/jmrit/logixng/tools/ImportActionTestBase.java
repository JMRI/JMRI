package jmri.jmrit.logixng.tools;

import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 * This class is base class for the expression tests
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public abstract class ImportActionTestBase {
    
    public enum State {
        ON,     // Sensor.ACTIVE, Turnout.CLOSED, and so on
        OFF,    // Sensor.INACTIVE, Turnout.THROWN, and so on
        TOGGLE, // Toggle state
    }
    
    private LogixManager logixManager;
    private Logix logix;
    private Conditional conditional;
    private ArrayList<ConditionalVariable> variables;
    private ArrayList<ConditionalAction> actions;
    private Turnout t1;
    
    /**
     * Set the sensor, turnout, or other bean to the desired state
     * @param on true if the sensor, turnout and so on should be on/thrown/...
     * @throws jmri.JmriException in case of an error
     */
    abstract public void setNamedBeanState(boolean on) throws JmriException;
    
    /**
     * Check that the sensor, turnout, or other bean has the desired state
     * @param on true if the sensor, turnout and so on should be on/thrown/...
     * @return true if the state is correct, false otherwise
     */
    abstract public boolean checkNamedBeanState(boolean on);
    
    /**
     * Set the conditional variable to check for the desired state
     * @param state the state
     */
    abstract public void setConditionalActionState(State state);
    
    /**
     * Create a new conditional action of the desired type
     * @return the new conditional action
     */
    abstract public ConditionalAction newConditionalAction();
    
    
    public void assertBoolean(String message, boolean expectSuccess, boolean result) {
        if (expectSuccess) {
            Assert.assertTrue(message, result);
        } else {
            Assert.assertFalse(message, result);
        }
    }
    
    // This method is used by some tests that tests delayed actions
    public void doWait(boolean expectSuccess, boolean on) {
        // Do nothing by default
    }
    
    // Test that state ON is imported correctly
    @Test
    public void testOn() throws JmriException {
        RunTestScaffold check = (message, expectSuccess) -> {
            t1.setState(Turnout.CLOSED);
            setConditionalActionState(State.ON);
            setNamedBeanState(false);
            assertBoolean(message, true, checkNamedBeanState(false));
            t1.setState(Turnout.THROWN);
            doWait(expectSuccess, true);
            assertBoolean(message, expectSuccess, checkNamedBeanState(true));
        };
        
        check.runTest("Logix is not activated", false);
        
        logixManager.activateAllLogixs();
        
        check.runTest("Logix is activated", true);
        
        logix.deActivateLogix();
        
        // Import the logix to LogixNG
        ImportLogix importLogix = new ImportLogix(logix);
        importLogix.doImport();
        
        logix.setEnabled(false);
        logixManager.deleteLogix(logix);
        
        check.runTest("Logix is removed and LogixNG is not activated", false);
        
        // We want the conditionalNGs run immediately during this test
        InstanceManager.getDefault(ConditionalNG_Manager.class).setRunOnGUIDelayed(false);
        
        importLogix.getLogixNG().setEnabled(true);
        InstanceManager.getDefault(LogixNG_Manager.class)
                .activateAllLogixNGs(false, false);
        
        check.runTest("LogixNG is activated", true);
        
        importLogix.getLogixNG().setEnabled(false);
        InstanceManager.getDefault(LogixNG_Manager.class).deleteLogixNG(importLogix.getLogixNG());
        
        check.runTest("LogixNG is removed", false);
    }
    
    // Test that state OFF is imported correctly
    @Test
    public void testOff() throws JmriException {
        RunTestScaffold check = (message, expectSuccess) -> {
            t1.setState(Turnout.CLOSED);
            setNamedBeanState(true);
            setConditionalActionState(State.OFF);
            assertBoolean(message, true, checkNamedBeanState(true));
            t1.setState(Turnout.THROWN);
            doWait(expectSuccess, false);
            assertBoolean(message, expectSuccess, checkNamedBeanState(false));
        };
        
        check.runTest("Logix is not activated", false);
        
        logixManager.activateAllLogixs();
        
        check.runTest("Logix is activated", true);
        
        logix.deActivateLogix();
        
        // Import the logix to LogixNG
        ImportLogix importLogix = new ImportLogix(logix);
        importLogix.doImport();
        
        logix.setEnabled(false);
        logixManager.deleteLogix(logix);
        
        check.runTest("Logix is removed and LogixNG is not activated", false);
        
        // We want the conditionalNGs run immediately during this test
        InstanceManager.getDefault(ConditionalNG_Manager.class).setRunOnGUIDelayed(false);
        
        importLogix.getLogixNG().setEnabled(true);
        InstanceManager.getDefault(LogixNG_Manager.class)
                .activateAllLogixNGs(false, false);
        
        check.runTest("LogixNG is activated", true);
        
        importLogix.getLogixNG().setEnabled(false);
        InstanceManager.getDefault(LogixNG_Manager.class).deleteLogixNG(importLogix.getLogixNG());
        
        check.runTest("LogixNG is removed", false);
    }
    
    // Test that state TOGGLE is imported correctly
    @Test
    public void testToggle() throws JmriException {
        RunTestScaffold check = (message, expectSuccess) -> {
            t1.setState(Turnout.CLOSED);
            setNamedBeanState(false);
            setConditionalActionState(State.ON);
            assertBoolean(message, true, checkNamedBeanState(false));
            t1.setState(Turnout.THROWN);
            doWait(expectSuccess, true);
            assertBoolean(message, expectSuccess, checkNamedBeanState(true));
            
            t1.setState(Turnout.CLOSED);
            setNamedBeanState(true);
            setConditionalActionState(State.TOGGLE);
            assertBoolean(message, true, checkNamedBeanState(true));
            t1.setState(Turnout.THROWN);
            doWait(expectSuccess, false);
            assertBoolean(message, expectSuccess, checkNamedBeanState(false));
            
            t1.setState(Turnout.CLOSED);
            setNamedBeanState(false);
            setConditionalActionState(State.TOGGLE);
            assertBoolean(message, true, checkNamedBeanState(false));
            t1.setState(Turnout.THROWN);
            doWait(expectSuccess, true);
            assertBoolean(message, expectSuccess, checkNamedBeanState(true));
            
            t1.setState(Turnout.CLOSED);
            setNamedBeanState(true);
            setConditionalActionState(State.TOGGLE);
            assertBoolean(message, true, checkNamedBeanState(true));
            t1.setState(Turnout.THROWN);
            doWait(expectSuccess, false);
            assertBoolean(message, expectSuccess, checkNamedBeanState(false));
        };
        
        check.runTest("Logix is not activated", false);
        
        logixManager.activateAllLogixs();
        
        check.runTest("Logix is activated", true);
        
        logix.deActivateLogix();
        
        // Import the logix to LogixNG
        ImportLogix importLogix = new ImportLogix(logix);
        importLogix.doImport();
        
        logix.setEnabled(false);
        logixManager.deleteLogix(logix);
        
        check.runTest("Logix is removed and LogixNG is not activated", false);
        
        // We want the conditionalNGs run immediately during this test
        InstanceManager.getDefault(ConditionalNG_Manager.class).setRunOnGUIDelayed(false);
        
        importLogix.getLogixNG().setEnabled(true);
        InstanceManager.getDefault(LogixNG_Manager.class)
                .activateAllLogixNGs(false, false);
        
        check.runTest("LogixNG is activated", true);
        
        importLogix.getLogixNG().setEnabled(false);
        InstanceManager.getDefault(LogixNG_Manager.class).deleteLogixNG(importLogix.getLogixNG());
        
        check.runTest("LogixNG is removed", false);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initLogixNGManager();
        
        t1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        
        logixManager = InstanceManager.getDefault(LogixManager.class);
        ConditionalManager conditionalManager = InstanceManager.getDefault(ConditionalManager.class);
        
        logix = logixManager.createNewLogix("IX1", null);
        
        conditional = conditionalManager.createNewConditional("IX1C1", "First conditional");
        logix.addConditional(conditional.getSystemName(), 0);
        
        conditional.setTriggerOnChange(true);
        conditional.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
        
        variables = new ArrayList<>();
        ConditionalVariable cv = new ConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.TURNOUT_THROWN);
        cv.setName("IT1");
        variables.add(cv);
        conditional.setStateVariables(variables);
        
        actions = new ArrayList<>();
        ConditionalAction ca = newConditionalAction();
        actions.add(ca);
        conditional.setAction(actions);
    }

    @After
    public void tearDown() {
        // JUnitAppender.clearBacklog();    REMOVE THIS!!!
        
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
