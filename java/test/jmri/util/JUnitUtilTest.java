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

    private InstanceManager getNewInstanceManager() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initLogixManager();
        jmri.util.JUnitUtil.initIdTagManager();
        
        InstanceManager instanceManager = InstanceManager.getDefault();
        
        System.out.format("--------------------------------%n");
        System.out.format("Num managers: %d%n", instanceManager.getAllManagers().size());
        for (Class<?> type : instanceManager.getAllManagers()) {
            System.out.format("%s%n", type.getName());
        }
        
        return instanceManager;
    }
    
    @Test
    public void testNumManagers() {
        InstanceManager instanceManager;
        
        instanceManager = getNewInstanceManager();
        // We now have all 21 managers
        Assert.assertTrue("we have 21 managers", instanceManager.getAllManagers().size() == 21);
        
        instanceManager = getNewInstanceManager();
        // jmri.jmrit.logix.WarrantPreferences is missing
        Assert.assertTrue("we have 20 managers", instanceManager.getAllManagers().size() == 20);
        
        instanceManager = getNewInstanceManager();
        // jmri.implementation.SignalSpeedMap is missing
        Assert.assertTrue("we have 19 managers", instanceManager.getAllManagers().size() == 19);
        
        instanceManager = getNewInstanceManager();
        // Same as previous
        Assert.assertTrue("we have 19 managers", instanceManager.getAllManagers().size() == 19);
        
        instanceManager = getNewInstanceManager();
        // Same as previous
        Assert.assertTrue("we have 19 managers", instanceManager.getAllManagers().size() == 19);
    }
    
    @Ignore
    @Test
    public void thisTestWorks() throws JmriException {
        
        // Get a new instance manager
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
//        jmri.util.JUnitUtil.initLogixManager();
        InstanceManager layoutEditorInstanceManager = InstanceManager.getDefault();
        
        // Get another new instance manager
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
//        jmri.util.JUnitUtil.initLogixManager();
        InstanceManager manualInstanceManager = InstanceManager.getDefault();
        
        // Verify that both instance managers has the same number of managers of each type
        Assert.assertTrue("there are the same number managers of each type in both instance managers",
                jmri.util.JUnitUtil.verifyInstanceManagersHasSameNumberOfManangersOfEachType(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
    }
    
    @Ignore
    @Test
    public void thisTestFails() throws JmriException {
        
        // Get a new instance manager
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initLogixManager();
        InstanceManager layoutEditorInstanceManager = InstanceManager.getDefault();
        
        // Get another new instance manager
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initLogixManager();
        InstanceManager manualInstanceManager = InstanceManager.getDefault();
        
        // Verify that both instance managers has the same number of managers of each type
        Assert.assertTrue("there are the same number managers of each type in both instance managers",
                jmri.util.JUnitUtil.verifyInstanceManagersHasSameNumberOfManangersOfEachType(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
    }
    
    @Ignore
    @Test
    // Test JUnitUtil.verifyInstanceManagersAreEqual
    public void testSameNumberOfManagersOfEachType__AAA() throws JmriException {
        
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
        
        // Verify that both instance managers has the same number of managers of each type
        Assert.assertTrue("there are the same number managers of each type in both instance managers",
                jmri.util.JUnitUtil.verifyInstanceManagersHasSameNumberOfManangersOfEachType(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
    }
    
    @Ignore
    @Test
    // Test JUnitUtil.verifyInstanceManagersAreEqual
    public void testVerifyInstanceManagerBeansAreEqual() throws JmriException {
        
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
        
        
        // Verify that both instance managers has the same number of managers of each type
        Assert.assertTrue("there are the same number managers of each type in both instance managers",
                jmri.util.JUnitUtil.verifyInstanceManagersHasSameNumberOfManangersOfEachType(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
        
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
        
        // Verify that both instance managers has the same number of managers of each type
        Assert.assertTrue("there are the same number managers of each type in both instance managers",
                jmri.util.JUnitUtil.verifyInstanceManagersHasSameNumberOfManangersOfEachType(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
        
        // Verify that the beans match each other in both instance managers
        Assert.assertFalse("the beans differ in the instance managers",
                jmri.util.JUnitUtil.verifyInstanceManagersAreEqual(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
        jmri.util.JUnitAppender.assertErrorMessage(
                "InstanceManagerA has item IT3 with state 4 and InstanceManagerB has item IT3 with state 2 but they differ in state");
        
        
        // Set turnout
        manualTurnout.setState(Turnout.THROWN);
        
        // Verify that the beans match each other in both instance managers
        Assert.assertTrue("the beans in both instance managers are equal",
                jmri.util.JUnitUtil.verifyInstanceManagersAreEqual(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
        
        
        // Test missing turnout in manualInstanceManager
        jmri.util.JUnitUtil.setInstanceManager(layoutEditorInstanceManager);
        layoutManagerTurnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("7");
        layoutManagerTurnout.setState(Turnout.CLOSED);
        
        // Verify that the beans don't match each other in both instance managers
        Assert.assertFalse("the beans differ in the instance managers",
                jmri.util.JUnitUtil.verifyInstanceManagersAreEqual(
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
                jmri.util.JUnitUtil.verifyInstanceManagersAreEqual(
                        layoutEditorInstanceManager,
                        manualInstanceManager)
                );
        
        // Test missing sensor in layoutEditorInstanceManager
        jmri.util.JUnitUtil.setInstanceManager(manualInstanceManager);
        Sensor manualSensor = InstanceManager.getDefault(SensorManager.class).provideSensor("57");
        manualSensor.setState(Sensor.ACTIVE);
        
        // Verify that the beans match each other in both instance managers
        Assert.assertFalse("the beans differ in the instance managers",
                jmri.util.JUnitUtil.verifyInstanceManagersAreEqual(
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
                jmri.util.JUnitUtil.verifyInstanceManagersAreEqual(
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
