package jmri.jmrit.logixng.tools;

import java.util.ArrayList;

import jmri.*;
import jmri.implementation.*;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
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
        // Remove this later
        JUnitAppender.clearBacklog();
        
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
    
    
    public interface RunTest {
        public void runTest(String message, boolean expectSuccess) throws JmriException;
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportTest.class);
}
