package jmri.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class test the jmri.util.JUnitUtil class in the 'test' tree
 * 
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class JUnitUtilTest {
    
    /**
     * Enum used to select which managers to initialize.
     */
    enum Mngr {
        CONFIGURE,
        SHUTDOWN,
        COMMAND_STATION,
        TURNOUT,
        LIGHT,
        SENSOR,
        REPORTER,
        THROTTLE,
        TURNOUT_OPERATION,
        USER_MESSAGES_PREF,
        ROUTE,
        MEMORY,
        OBLOCK,
        WARRANT,
        SIGNAL_MAST_LOGIC,
        LAYOUT_BLOCK,
        SECTION,
        SIGNAL_HEAD,
        SIGNAL_MAST,
        PROGRAMMER,
        POWER,
        RAILCOM,
        CONDITIONAL,
        SIGNAL_SPEED_MAP,
        LOGIX,
        ID_TAG,
    }

    /**
     * Get a new instance manager.
     * This method initializes all the instance managers getNewInstanceManager(Mngr[])
     * knows about.
     * The instance manager and the profile manager is always initialized by
     * this method.
     * @return a new instance manager
     */
    private InstanceManager getNewInstanceManager() {
        return getNewInstanceManager(null);
    }
    
    /**
     * Get a new instance manager.
     * The instance manager and the profile manager is always initialized by
     * this method.
     * @return a new instance manager
     */
    private InstanceManager getNewInstanceManager(Mngr[] managersToInit) {
        boolean initAllManagers;
        Set<Mngr> managersToInitSet;
        
        if (managersToInit == null) {
            initAllManagers = true;
            managersToInitSet = new HashSet<>();
        } else {
            initAllManagers = false;
            managersToInitSet = new HashSet<>(Arrays.asList(managersToInit));
        }
        
        // Always reset the instance manager
        jmri.util.JUnitUtil.resetInstanceManager();
        
        // Reset the instance manager twice since managers may still be left
        // after the first reset.
        jmri.util.JUnitUtil.resetInstanceManager();
        
        // Always reset the profile manager
        jmri.util.JUnitUtil.resetProfileManager();
        
        if (initAllManagers || managersToInitSet.contains(Mngr.CONFIGURE)) {
            jmri.util.JUnitUtil.initConfigureManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.SHUTDOWN)) {
            jmri.util.JUnitUtil.initShutDownManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.COMMAND_STATION)) {
            jmri.util.JUnitUtil.initDebugCommandStation();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.TURNOUT)) {
            jmri.util.JUnitUtil.initInternalTurnoutManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.LIGHT)) {
            jmri.util.JUnitUtil.initInternalLightManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.SENSOR)) {
            jmri.util.JUnitUtil.initInternalSensorManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.REPORTER)) {
            jmri.util.JUnitUtil.initReporterManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.THROTTLE)) {
            jmri.util.JUnitUtil.initDebugThrottleManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.TURNOUT_OPERATION)) {
            jmri.util.JUnitUtil.resetTurnoutOperationManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.USER_MESSAGES_PREF)) {
            jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.ROUTE)) {
            jmri.util.JUnitUtil.initRouteManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.MEMORY)) {
            jmri.util.JUnitUtil.initMemoryManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.OBLOCK)) {
            jmri.util.JUnitUtil.initOBlockManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.WARRANT)) {
            jmri.util.JUnitUtil.initWarrantManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.SIGNAL_MAST_LOGIC)) {
            jmri.util.JUnitUtil.initSignalMastLogicManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.LAYOUT_BLOCK)) {
            jmri.util.JUnitUtil.initLayoutBlockManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.SECTION)) {
            jmri.util.JUnitUtil.initSectionManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.SIGNAL_HEAD)) {
            jmri.util.JUnitUtil.initInternalSignalHeadManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.SIGNAL_MAST)) {
            jmri.util.JUnitUtil.initDefaultSignalMastManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.PROGRAMMER)) {
            jmri.util.JUnitUtil.initDebugProgrammerManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.POWER)) {
            jmri.util.JUnitUtil.initDebugPowerManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.RAILCOM)) {
            jmri.util.JUnitUtil.initRailComManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.CONDITIONAL)) {
            jmri.util.JUnitUtil.initConditionalManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.SIGNAL_SPEED_MAP)) {
            jmri.util.JUnitUtil.initSignalSpeedMap();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.LOGIX)) {
            jmri.util.JUnitUtil.initLogixManager();
        }
        if (initAllManagers || managersToInitSet.contains(Mngr.ID_TAG)) {
            jmri.util.JUnitUtil.initIdTagManager();
        }
        
        InstanceManager instanceManager = InstanceManager.getDefault();
        
        return instanceManager;
    }
    
    @Test
    public void testNumManagers() {
        InstanceManager instanceManager;
        
        instanceManager = getNewInstanceManager(new Mngr[]{});
        // We now have all 1 manager
        Assert.assertTrue("we have 1 managers", instanceManager.getAllManagers().size() == 1);
        
        instanceManager = getNewInstanceManager(new Mngr[]{
            Mngr.TURNOUT, Mngr.LIGHT, Mngr.SENSOR, Mngr.REPORTER, Mngr.TURNOUT_OPERATION,
            Mngr.ROUTE, Mngr.MEMORY, Mngr.OBLOCK, Mngr.WARRANT, Mngr.SIGNAL_MAST_LOGIC,
            Mngr.LAYOUT_BLOCK, Mngr.SECTION, Mngr.SIGNAL_HEAD, Mngr.SIGNAL_MAST
        });
        // We now have all 16 managers
        Assert.assertTrue("we have 16 managers", instanceManager.getAllManagers().size() == 16);
        
        instanceManager = getNewInstanceManager();
        // We now have all 42 managers
        Assert.assertTrue("we have 42 managers", instanceManager.getAllManagers().size() == 42);
        
        instanceManager = getNewInstanceManager(new Mngr[]{});
        // We now have 1 manager
        Assert.assertTrue("we have 1 manager", instanceManager.getAllManagers().size() == 1);
    }
    
    @Test
    // Test JUnitUtil.verifyInstanceManagersAreEqual
    public void testSameNumberOfManagersOfEachType() throws JmriException {
        
        // Get a new instance manager
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initSignalSpeedMap();
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
        jmri.util.JUnitUtil.initSignalSpeedMap();
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
    
//    private final static Logger log = LoggerFactory.getLogger(JUnitUtilTest.class);
    
}
