package jmri.jmrit.logixng.tools;

import java.util.ArrayList;

import jmri.*;
import jmri.implementation.*;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 * This test tests And, Or, Mixed
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class Import_AndOrMixed_Test {
    
    Sensor s1;
    Sensor s2;
    Sensor s3;
    Turnout t1;
    private LogixManager logixManager;
    private Logix logix;
    private Conditional conditional;
    private ArrayList<ConditionalVariable> variables;
    private ArrayList<ConditionalAction> actions;
    
    
    public void assertBoolean(String message, boolean expectSuccess, boolean result) {
        if (expectSuccess) {
            Assert.assertTrue(message, result);
        } else {
            Assert.assertFalse(message, result);
        }
    }
    
    // Test that the operator AND is imported correctly
    @Test
    public void testAnd() throws JmriException {
        RunTestScaffold check = (message, expectSuccess) -> {
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.INACTIVE);
            s3.setState(Sensor.INACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should not throw the turnout
            s2.setState(Sensor.ACTIVE);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.ACTIVE);
            s3.setState(Sensor.INACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should not throw the turnout
            s1.setState(Sensor.ACTIVE);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
            s1.setState(Sensor.ACTIVE);
            s2.setState(Sensor.INACTIVE);
            s3.setState(Sensor.ACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should throw the turnout if the logix/logixng is active
            s2.setState(Sensor.ACTIVE);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
            
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.ACTIVE);
            s3.setState(Sensor.ACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should throw the turnout if the logix/logixng is active
            s1.setState(Sensor.ACTIVE);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
        };
        
        check.runTest("Logix is not activated", false);
        
        conditional.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
        ConditionalVariable cv = new ConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.SENSOR_ACTIVE);
        cv.setName("IS1");
        variables.add(cv);
        
        cv = new ConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.SENSOR_ACTIVE);
        cv.setName("IS2");
        variables.add(cv);
        
        cv = new ConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.SENSOR_ACTIVE);
        cv.setName("IS3");
        variables.add(cv);
        
        ConditionalAction ca = new DefaultConditionalAction();
        ca.setType(Conditional.Action.SET_TURNOUT);
        ca.setActionData(Turnout.THROWN);
        ca.setDeviceName("IT1");
        actions.add(ca);
        
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
/*        
        System.err.println("-------------------------------------------");
        java.io.PrintWriter p = new java.io.PrintWriter(System.err);
        for (jmri.jmrit.logixng.LogixNG l : InstanceManager.getDefault(LogixNG_Manager.class).getNamedBeanSet()) {
            System.err.println("LogixNG: " + l.getSystemName());
            System.err.println("ConditionalNG: " + l.getConditionalNG(0).getSystemName());
            l.printTree(p, "   ");
            p.flush();
        }
        System.err.println("-------------------------------------------");
*/        
        check.runTest("LogixNG is activated", true);
        
        importLogix.getLogixNG().setEnabled(false);
        InstanceManager.getDefault(LogixNG_Manager.class).deleteLogixNG(importLogix.getLogixNG());
        
        check.runTest("LogixNG is removed", false);
    }
    
    // Test that the operator OR is imported correctly
    @Test
    public void testOr() throws JmriException {
        RunTestScaffold check = (message, expectSuccess) -> {
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.INACTIVE);
            s3.setState(Sensor.INACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should throw the turnout if the logix/logixng is active
            s2.setState(Sensor.ACTIVE);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
            
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.ACTIVE);
            s3.setState(Sensor.INACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should not throw the turnout
            s1.setState(Sensor.ACTIVE);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
            s1.setState(Sensor.ACTIVE);
            s2.setState(Sensor.INACTIVE);
            s3.setState(Sensor.ACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should not throw the turnout
            s2.setState(Sensor.ACTIVE);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.ACTIVE);
            s3.setState(Sensor.ACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should not throw the turnout
            s1.setState(Sensor.ACTIVE);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
        };
        
        check.runTest("Logix is not activated", false);
        
        conditional.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ConditionalVariable cv = new ConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.SENSOR_ACTIVE);
        cv.setName("IS1");
        variables.add(cv);
        
        cv = new ConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.SENSOR_ACTIVE);
        cv.setName("IS2");
        variables.add(cv);
        
        cv = new ConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.SENSOR_ACTIVE);
        cv.setName("IS3");
        variables.add(cv);
        
        ConditionalAction ca = new DefaultConditionalAction();
        ca.setType(Conditional.Action.SET_TURNOUT);
        ca.setActionData(Turnout.THROWN);
        ca.setDeviceName("IT1");
        actions.add(ca);
        
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
    
    // Test that the operator MIXED is imported correctly
    @Test
    public void testMixed() throws JmriException {
        RunTestScaffold check = (message, expectSuccess) -> {
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.INACTIVE);
            s3.setState(Sensor.INACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should not throw the turnout
            s2.setState(Sensor.ACTIVE);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.INACTIVE);
            s3.setState(Sensor.INACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should not throw the turnout
            s1.setState(Sensor.ACTIVE);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.ACTIVE);
            s3.setState(Sensor.INACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should not throw the turnout
            s3.setState(Sensor.ACTIVE);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
            s1.setState(Sensor.ACTIVE);
            s2.setState(Sensor.INACTIVE);
            s3.setState(Sensor.INACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should throw the turnout if the logix/logixng is active
            s2.setState(Sensor.ACTIVE);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
            
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.INACTIVE);
            s3.setState(Sensor.ACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should throw the turnout if the logix/logixng is active
            s1.setState(Sensor.ACTIVE);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
            
        };
        
        check.runTest("Logix is not activated", false);
        
        conditional.setLogicType(Conditional.AntecedentOperator.MIXED, "R1 AND (R2 OR R3)");
        ConditionalVariable cv = new ConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.SENSOR_ACTIVE);
        cv.setName("IS1");
        variables.add(cv);
        
        cv = new ConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.SENSOR_ACTIVE);
        cv.setName("IS2");
        variables.add(cv);
        
        cv = new ConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.SENSOR_ACTIVE);
        cv.setName("IS3");
        variables.add(cv);
        
        ConditionalAction ca = new DefaultConditionalAction();
        ca.setType(Conditional.Action.SET_TURNOUT);
        ca.setActionData(Turnout.THROWN);
        ca.setDeviceName("IT1");
        actions.add(ca);
        
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
        
        s1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        s2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        s3 = InstanceManager.getDefault(SensorManager.class).provide("IS3");
        t1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        
        logixManager = InstanceManager.getDefault(LogixManager.class);
        ConditionalManager conditionalManager = InstanceManager.getDefault(ConditionalManager.class);
        
        logix = logixManager.createNewLogix("IX1", null);
        
        conditional = conditionalManager.createNewConditional("IX1C1", "First conditional");
        logix.addConditional(conditional.getSystemName(), 0);
        
        conditional.setTriggerOnChange(true);
        conditional.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
        
        variables = new ArrayList<>();
        conditional.setStateVariables(variables);
        
        actions = new ArrayList<>();
        conditional.setAction(actions);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
