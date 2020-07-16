package jmri.jmrit.logixng.tools;

import java.util.ArrayList;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportTest {
    
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
        RunTest check = (message, expectSuccess) -> {
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
        
        logixManager.activateAllLogixs();
        
        check.runTest("Logix is activated", true);
        
        logix.deActivateLogix();
        logixManager.deleteLogix(logix);
        
        check.runTest("Logix is removed", false);
        
        check.runTest("LogixNG is not activated", false);
        
        // Not implemented yet
        Assume.assumeFalse(true);
        
        check.runTest("LogixNG is activated", true);
        
        check.runTest("LogixNG is removed", false);
    }
    
    // Test that the operator OR is imported correctly
    @Test
    public void testOr() throws JmriException {
        RunTest check = (message, expectSuccess) -> {
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.INACTIVE);
            s3.setState(Sensor.INACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should throw the turnout
            s2.setState(Sensor.ACTIVE);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
            
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.ACTIVE);
            s3.setState(Sensor.INACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should not throw the turnout
            s1.setState(Sensor.ACTIVE);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
            
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
        logixManager.deleteLogix(logix);
        
        check.runTest("Logix is removed", false);
        
        check.runTest("LogixNG is not activated", false);
        
        // Not implemented yet
        Assume.assumeFalse(true);
        
        check.runTest("LogixNG is activated", true);
        
        check.runTest("LogixNG is removed", false);
    }
    
    // Test that the operator MIXED is imported correctly
    @Test
    public void testMixed() throws JmriException {
        RunTest check = (message, expectSuccess) -> {
            s1.setState(Sensor.INACTIVE);
            s2.setState(Sensor.INACTIVE);
            s3.setState(Sensor.INACTIVE);
            t1.setState(Turnout.CLOSED);
            // This should not throw the turnout
            s2.setState(Sensor.ACTIVE);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
            s1.setState(Sensor.ACTIVE);
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
            s3.setState(Sensor.ACTIVE);
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
        logixManager.deleteLogix(logix);
        
        check.runTest("Logix is removed", false);
        
        check.runTest("LogixNG is not activated", false);
        
        // Not implemented yet
        Assume.assumeFalse(true);
        
        check.runTest("LogixNG is activated", true);
        
        check.runTest("LogixNG is removed", false);
    }
    
    // Test that TriggerOnChange is imported correctly
    @Test
    public void testTriggerOnChange() {
        conditional.setTriggerOnChange(false);
        conditional.setTriggerOnChange(true);
    }
    
    @Test
    public void testConditionalVariableTurnout() {
        ConditionalVariable cv = new ConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(true);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.OR);
        cv.setType(Conditional.Type.BLOCK_STATUS_EQUALS);
    }
    
    @Test
    public void testConditionalActionTurnout() {
        Assume.assumeFalse(true);
        ConditionalAction ca = new DefaultConditionalAction();
        ca.setActionData("aaa");
        ca.setActionData(0);
        ca.setActionString("aaa");
        ca.setDeviceName("aaa");
        ca.setListener(null);
        ca.setOption(0);
        ca.setTimer(null);
        ca.setType(Conditional.Action.NONE);
        ca.setType("aaa");
    }
    
    @Test
    public void testAA() {
        
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
        
        conditional.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
        conditional.setTriggerOnChange(false);
        
        variables = new ArrayList<>();
        conditional.setStateVariables(variables);
        
        actions = new ArrayList<>();
        conditional.setAction(actions);
        
//        logixManager.activateAllLogixs();
    }

    @After
    public void tearDown() {
//        JUnitAppender.clearBacklog();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
    
    
    public interface RunTest {
        public void runTest(String message, boolean expectSuccess) throws JmriException;
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportTest.class);
}
