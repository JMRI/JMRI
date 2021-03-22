package jmri.jmrit.logixng.tools;

import java.util.ArrayList;

import jmri.*;
import jmri.implementation.DefaultConditionalAction;
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
public abstract class ImportExpressionTestBase {
    
    public enum State {
        ON,     // Sensor.ACTIVE, Turnout.CLOSED, and so on
        OFF,    // Sensor.INACTIVE, Turnout.THROWN, and so on
        OTHER,  // Sensor.UNKNOWN, Turnout.UNKNOWN, and so on
    }
    
    private LogixManager logixManager;
    private Logix logix;
    private Conditional conditional;
    private ArrayList<ConditionalVariable> variables;
    private ArrayList<ConditionalAction> actions;
    private Turnout t1;
    
    /**
     * Some NamedBeans, for example Light, does not support other states than ON and OFF
     * @return true if other states than ON and OFF is allowed
     */
    public boolean isStateOtherAllowed() {
        return true;
    }
    
    /**
     * Set the sensor, turnout, or other bean to the desired state
     * @param state the state
     * @throws jmri.JmriException in case of an exception
     */
    abstract public void setNamedBeanState(State state) throws JmriException;
    
    /**
     * Set the conditional variable to check for the desired state
     * @param state the state
     */
    abstract public void setConditionalVariableState(State state);
    
    /**
     * Create a new conditional variable of the desired type
     * @return the new conditional variable
     */
    abstract public ConditionalVariable newConditionalVariable();
    
    
    public void assertBoolean(String message, boolean expectSuccess, boolean result) {
        if (expectSuccess) {
            Assert.assertTrue(message, result);
        } else {
            Assert.assertFalse(message, result);
        }
    }
    
    // Test that state ON is imported correctly
    @Test
    public void testOn() throws JmriException {
        RunTestScaffold check = (message, expectSuccess) -> {
            setNamedBeanState(State.OFF);
            setConditionalVariableState(State.ON);
            t1.setState(Turnout.CLOSED);
            // This should not throw the turnout
            if (isStateOtherAllowed()) setNamedBeanState(State.OTHER);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
            setNamedBeanState(State.OFF);
            setConditionalVariableState(State.ON);
            t1.setState(Turnout.CLOSED);
            // This should throw the turnout if Logix/LogixNG is activated
            setNamedBeanState(State.ON);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
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
        InstanceManager.getDefault(LogixNG_Manager.class).activateAllLogixNGs();
        
        check.runTest("LogixNG is activated", true);
        
        importLogix.getLogixNG().setEnabled(false);
        InstanceManager.getDefault(LogixNG_Manager.class).deleteLogixNG(importLogix.getLogixNG());
        
        check.runTest("LogixNG is removed", false);
    }
    
    // Test that state OFF is imported correctly
    @Test
    public void testOff() throws JmriException {
        RunTestScaffold check = (message, expectSuccess) -> {
            setNamedBeanState(State.ON);
            setConditionalVariableState(State.OFF);
            t1.setState(Turnout.CLOSED);
            // This should not throw the turnout
            if (isStateOtherAllowed()) setNamedBeanState(State.OTHER);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
            setNamedBeanState(State.ON);
            setConditionalVariableState(State.OFF);
            t1.setState(Turnout.CLOSED);
            // This should throw the turnout if Logix/LogixNG is activated
            setNamedBeanState(State.OFF);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
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
        InstanceManager.getDefault(LogixNG_Manager.class).activateAllLogixNGs();
        
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
        ConditionalVariable cv = newConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.SENSOR_ACTIVE);
        variables.add(cv);
        conditional.setStateVariables(variables);
        
        actions = new ArrayList<>();
        ConditionalAction ca = new DefaultConditionalAction();
        ca.setType(Conditional.Action.SET_TURNOUT);
        ca.setActionData(Turnout.THROWN);
        ca.setDeviceName("IT1");
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
