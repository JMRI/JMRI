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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class test the jmri.util.JUnitUtil class in the 'test' tree
 * 
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class JUnitUtilAdditionalTest {
    
    /**
     * Enum used to select which managers to initialize.
     */
    enum Mngr {
        CONFIGURE(17),
        SHUTDOWN(2),
        COMMAND_STATION(2),
        TURNOUT(2),
        LIGHT(2),
        SENSOR(2),
        REPORTER(2),
        THROTTLE(2),
        TURNOUT_OPERATION(2),
        USER_MESSAGES_PREF(1),
        ROUTE(5),
        MEMORY(2),
        OBLOCK(2),
        WARRANT(2),
        SIGNAL_MAST_LOGIC(10),
        LAYOUT_BLOCK(4),
        SECTION(6),
        SIGNAL_HEAD(4),
        SIGNAL_MAST(5),
        PROGRAMMER(3),
        POWER(2),
        RAILCOM(2),
        CONDITIONAL(2),
        SIGNAL_SPEED_MAP(3),
        LOGIX(17),
        ID_TAG(2);
        
        private final int numManagers;
        
        private Mngr(int numManagers) {
            this.numManagers = numManagers;
        }
        
        public int getNumManagers() {
            return numManagers;
        }
        
    }

    /**
     * Get a new instance manager.
     * The instance manager and the profile manager is always initialized by
     * this method.
     * @return a new instance manager
     */
    private InstanceManager getNewInstanceManager(Mngr[] managersToInit) {
        Set<Mngr> managersToInitSet;
        
        managersToInitSet = new HashSet<>(Arrays.asList(managersToInit));
        
        // Always reset the instance manager
        jmri.util.JUnitUtil.resetInstanceManager();
        
        // Always reset the profile manager
        jmri.util.JUnitUtil.resetProfileManager();
        
        if (managersToInitSet.contains(Mngr.CONFIGURE)) {
            jmri.util.JUnitUtil.initConfigureManager();
        }
        if (managersToInitSet.contains(Mngr.SHUTDOWN)) {
            jmri.util.JUnitUtil.initShutDownManager();
        }
        if (managersToInitSet.contains(Mngr.COMMAND_STATION)) {
            jmri.util.JUnitUtil.initDebugCommandStation();
        }
        if (managersToInitSet.contains(Mngr.TURNOUT)) {
            jmri.util.JUnitUtil.initInternalTurnoutManager();
        }
        if (managersToInitSet.contains(Mngr.LIGHT)) {
            jmri.util.JUnitUtil.initInternalLightManager();
        }
        if (managersToInitSet.contains(Mngr.SENSOR)) {
            jmri.util.JUnitUtil.initInternalSensorManager();
        }
        if (managersToInitSet.contains(Mngr.REPORTER)) {
            jmri.util.JUnitUtil.initReporterManager();
        }
        if (managersToInitSet.contains(Mngr.THROTTLE)) {
            jmri.util.JUnitUtil.initDebugThrottleManager();
        }
        if (managersToInitSet.contains(Mngr.TURNOUT_OPERATION)) {
            jmri.util.JUnitUtil.resetTurnoutOperationManager();
        }
        if (managersToInitSet.contains(Mngr.USER_MESSAGES_PREF)) {
            jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        }
        if (managersToInitSet.contains(Mngr.ROUTE)) {
            jmri.util.JUnitUtil.initRouteManager();
        }
        if (managersToInitSet.contains(Mngr.MEMORY)) {
            jmri.util.JUnitUtil.initMemoryManager();
        }
        if (managersToInitSet.contains(Mngr.OBLOCK)) {
            jmri.util.JUnitUtil.initOBlockManager();
        }
        if (managersToInitSet.contains(Mngr.WARRANT)) {
            jmri.util.JUnitUtil.initWarrantManager();
        }
        if (managersToInitSet.contains(Mngr.SIGNAL_MAST_LOGIC)) {
            jmri.util.JUnitUtil.initSignalMastLogicManager();
        }
        if (managersToInitSet.contains(Mngr.LAYOUT_BLOCK)) {
            jmri.util.JUnitUtil.initLayoutBlockManager();
        }
        if (managersToInitSet.contains(Mngr.SECTION)) {
            jmri.util.JUnitUtil.initSectionManager();
        }
        if (managersToInitSet.contains(Mngr.SIGNAL_HEAD)) {
            jmri.util.JUnitUtil.initInternalSignalHeadManager();
        }
        if (managersToInitSet.contains(Mngr.SIGNAL_MAST)) {
            jmri.util.JUnitUtil.initDefaultSignalMastManager();
        }
        if (managersToInitSet.contains(Mngr.PROGRAMMER)) {
            jmri.util.JUnitUtil.initDebugProgrammerManager();
        }
        if (managersToInitSet.contains(Mngr.POWER)) {
            jmri.util.JUnitUtil.initDebugPowerManager();
        }
        if (managersToInitSet.contains(Mngr.RAILCOM)) {
            jmri.util.JUnitUtil.initRailComManager();
        }
        if (managersToInitSet.contains(Mngr.CONDITIONAL)) {
            jmri.util.JUnitUtil.initConditionalManager();
        }
        if (managersToInitSet.contains(Mngr.SIGNAL_SPEED_MAP)) {
            jmri.util.JUnitUtil.initSignalSpeedMap();
        }
        if (managersToInitSet.contains(Mngr.LOGIX)) {
            jmri.util.JUnitUtil.initLogixManager();
        }
        if (managersToInitSet.contains(Mngr.ID_TAG)) {
            jmri.util.JUnitUtil.initIdTagManager();
        }
        
        InstanceManager instanceManager = InstanceManager.getDefault();
        
        return instanceManager;
    }
    
    @Test
    public void testEachManager() {
        
        Set<Mngr> failedManagers = new HashSet<>();
        
        for (Mngr m : Mngr.values()) {
            // Reset the instance manager twice to ensure other tests don't
            // interfere with this test.
            jmri.util.JUnitUtil.resetInstanceManager();
            jmri.util.JUnitUtil.resetInstanceManager();
            
            // We now have all 1 manager
            Assert.assertTrue("we have 1 managers", InstanceManager.getDefault().getAllManagers().size() == 1);
            
            getNewInstanceManager(new Mngr[]{m});
            System.out.format("Num managers for %s: %d%n", m.name(), InstanceManager.getDefault().getAllManagers().size());
            String message = String.format("we have %d managers for manager %s", m.getNumManagers(), m.name());
            // We now have all N managers
            Assert.assertTrue(message, InstanceManager.getDefault().getAllManagers().size() == m.getNumManagers());
            
            jmri.util.JUnitUtil.resetInstanceManager();
//            System.out.format("Num managers after reset for %s: %d%n", m.name(), InstanceManager.getDefault().getAllManagers().size());
            
            if (InstanceManager.getDefault().getAllManagers().size() != 1) {
//                log.error("JUnitUtil.resetInstanceManager() doesn't do a complete reset after {}. Num remaining managers: {}", m.name(), InstanceManager.getDefault().getAllManagers().size());
                log.warn("JUnitUtil.resetInstanceManager() doesn't do a complete reset after {}. Num remaining managers: {}", m.name(), InstanceManager.getDefault().getAllManagers().size());
                failedManagers.add(m);
            } else {
//                System.out.format("resetInstanceManager() do a complete reset after %s%n", m.name());
//                log.info("resetInstanceManager() do a complete reset after {}", m.name());
                log.warn("resetInstanceManager() do a complete reset after {}", m.name());
            }
        }
        
        StringBuilder sb = new StringBuilder();
        for (Mngr m : failedManagers) {
            sb.append(m.name());
            sb.append(", ");
        }
        
        if (sb.length() > 0) {
//            log.error("Failing managers: {}", sb.toString());
            log.warn("Failing managers: {}", sb.toString());
        }
    }
    
    // from here down is testing infrastructure
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        
        // Reset the instance manager twice to ensure other tests in other
        // test classes don't interfere with this test.
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetInstanceManager();
    }
    
    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }
    
    private final static Logger log = LoggerFactory.getLogger(JUnitUtilAdditionalTest.class);
    
}
