package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;

/**
 * This class test the jmri.util.JUnitUtil class in the 'test' tree
 * 
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class JUnitUtilTest {

    @Test
    // Test JUnitUtil.verifyInstanceManagerBeansAreEqual
    public void testVerifyInstanceManagerItemsIsEqual() throws JmriException {
        
        String turnoutDeviceName = "3";
        
        // Get a new instance manager
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initLogixManager();
        jmri.util.JUnitUtil.initIdTagManager();
        InstanceManager layoutEditorInstanceManager = InstanceManager.getDefault();
        
        
        // Get another new instance manager
        jmri.util.JUnitUtil.resetInstanceManager();
        // We want to test that the order of managers doesn't matter so create
        // managers in a different order.
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initLogixManager();
        jmri.util.JUnitUtil.initIdTagManager();
        InstanceManager manualInstanceManager = InstanceManager.getDefault();
        
        
        jmri.util.JUnitUtil.setInstanceManager(layoutEditorInstanceManager);
        Turnout layoutManagerTurnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        layoutManagerTurnout.setState(Turnout.THROWN);
        
        jmri.util.JUnitUtil.setInstanceManager(manualInstanceManager);
        Turnout manualTurnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        manualTurnout.setState(Turnout.CLOSED);
        
        jmri.util.JUnitUtil.setInstanceManager(layoutEditorInstanceManager);
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        Assert.assertTrue("Turnout is thrown", turnout.getState() == Turnout.THROWN);
        
        jmri.util.JUnitUtil.setInstanceManager(manualInstanceManager);
        turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(turnoutDeviceName);
        Assert.assertFalse("Turnout is thrown", turnout.getState() == Turnout.THROWN);
        
        // Verify that the beans match each other in both instance managers
        Assert.assertFalse("the beans differ in the instance managers",
                jmri.util.JUnitUtil.verifyInstanceManagerBeansAreEqual(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
        jmri.util.JUnitAppender.assertErrorMessage(
                "InstanceManagerA has item IT3 with state 4 and InstanceManagerB has item IT3 with state 2 but they differ in state");
        
        
        // Set turnout
        manualTurnout.setState(Turnout.THROWN);
        
        // Verify that the beans match each other in both instance managers
        Assert.assertTrue("the beans in both instance managers are equal",
                jmri.util.JUnitUtil.verifyInstanceManagerBeansAreEqual(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
        
        
        // Test missing turnout in manualInstanceManager
        jmri.util.JUnitUtil.setInstanceManager(layoutEditorInstanceManager);
        layoutManagerTurnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("7");
        layoutManagerTurnout.setState(Turnout.CLOSED);
        
        // Verify that the beans don't match each other in both instance managers
        Assert.assertFalse("the beans differ in the instance managers",
                jmri.util.JUnitUtil.verifyInstanceManagerBeansAreEqual(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
        jmri.util.JUnitAppender.assertErrorMessage(
                "InstanceManagerA has item IT7 which is missing in instanceManagerB");
        
        // Add the missing turnout in manualInstanceManager
        jmri.util.JUnitUtil.setInstanceManager(manualInstanceManager);
        manualTurnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("7");
        manualTurnout.setState(Turnout.CLOSED);
        
        // Verify that the beans match each other in both instance managers
        Assert.assertTrue("the beans in both instance managers are equal",
                jmri.util.JUnitUtil.verifyInstanceManagerBeansAreEqual(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
        
        // Test missing sensor in layoutEditorInstanceManager
        jmri.util.JUnitUtil.setInstanceManager(manualInstanceManager);
        Sensor manualSensor = InstanceManager.getDefault(SensorManager.class).provideSensor("57");
        manualSensor.setState(Sensor.ACTIVE);
        
        // Verify that the beans match each other in both instance managers
        Assert.assertFalse("the beans differ in the instance managers",
                jmri.util.JUnitUtil.verifyInstanceManagerBeansAreEqual(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
        jmri.util.JUnitAppender.assertErrorMessage(
                "InstanceManagerB has item IS57 which is missing in instanceManagerA");
        
        // Add the missing turnout in layoutEditorInstanceManager
        jmri.util.JUnitUtil.setInstanceManager(layoutEditorInstanceManager);
        Sensor layoutManagerSensor = InstanceManager.getDefault(SensorManager.class).provideSensor("57");
        layoutManagerSensor.setState(Sensor.ACTIVE);
        
        // Verify that the beans match each other in both instance managers
        Assert.assertTrue("the beans in both instance managers are equal",
                jmri.util.JUnitUtil.verifyInstanceManagerBeansAreEqual(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
    }
    
    // from here down is testing infrastructure
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }
    
    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }
    
}
