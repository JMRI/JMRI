package jmri.jmrit.logixng.tools;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.lang.reflect.Method;

import jmri.*;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logix.*;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.*;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class loads a panel file with some Logix, test that the Logix works,
 * then import the Logixs to LogixNG, then removes all the Logixs, and then
 * test that the LogixNGs works.
 * <P>
 This test tests both action entry/exit and expression entry/exit
 *
 * @author Daniel Bergqvist  (C) 2021
 * @author Dave Sand         (C) 2021 (Dave Sand created the panel file)
 */
@DisabledIfHeadless
public class ImportEntryExitTest {

    private LogixManager logixManager;
    private LogixNG_Manager logixNG_Manager;

    private void deleteAllLogixs() {
        // Remove all Logixs. Avoid concurrent modification by calling
        // getNamedBeanSet() on each iteration.
        while (! logixManager.getNamedBeanSet().isEmpty()) {
            Logix l = logixManager.getNamedBeanSet().first();
            l.setEnabled(false);
            logixManager.deleteLogix(l);
        }
    }

    private void runTestActionEntryExit(DestinationPoints dp, Sensor sensor, String where) throws JmriException {
        assertFalse(dp.isEnabled());
        sensor.setState(Sensor.INACTIVE);

        JUnitUtil.waitFor(() -> (dp.isEnabled()),
            () -> "destination point " + dp.getDisplayName() + " enabled in " + where);
        assertTrue(dp.isEnabled());
        sensor.setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> (!dp.isEnabled()),
            () -> "destination point " + dp.getDisplayName() + " disabled in " + where);
        assertFalse(dp.isEnabled());
    }

    @Test
    public void testActionEntryExit() throws JmriException {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        // ENTRYEXIT
        // SET_NXPAIR_ENABLED
/*
        for (DestinationPoints dp : InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBeanSet()) {
            System.out.format("DestinationPoints: %s, %s%n", dp.getSystemName(), dp.getUserName());
        }

        for (Sensor r : InstanceManager.getDefault(SensorManager.class).getNamedBeanSet()) {
            System.out.format("Sensor: %s, '%s'%n", r.getSystemName(), r.getUserName());
        }

        for (Turnout r : InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet()) {
            System.out.format("Turnout: %s, '%s'%n", r.getSystemName(), r.getUserName());
        }
*/
        Sensor sensor201 = InstanceManager.getDefault(SensorManager.class).getByUserName("Set NX Enabled");
        assertNotNull(sensor201);
        sensor201.setState(Sensor.ACTIVE);
        assertEquals(Sensor.ACTIVE, sensor201.getState());

        DestinationPoints dp = InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean("NX-Left-TO-A (Left-TO-A) to NX-Right-TO-B (Right-TO-B)");
        assertNotNull(dp);

        // Test entry/exit
        runTestActionEntryExit(dp, sensor201, "run1");

        for (Logix l : logixManager.getNamedBeanSet()) {
            ImportLogix il = new ImportLogix(l, true);
            il.doImport();
            il.getLogixNG().setEnabled(true);
        }

        deleteAllLogixs();

        // Test entry/exit
        runTestActionEntryExit(dp, sensor201, "run2");
    }

    private void setActiveEntryExit(DestinationPoints dp, boolean boo) {
//        Method retrieveItems = dp.getClass().getDeclaredMethod("isEnabled", String.class);
        assertDoesNotThrow( () -> {
            Method setActiveEntryExit = dp.getClass().getDeclaredMethod("setActiveEntryExit", boolean.class);
            setActiveEntryExit.setAccessible(true);
            setActiveEntryExit.invoke(dp, boo);
        });
    }

    private void runTestExpressionEntryExit(DestinationPoints dp, Sensor sensor, String comment) {
        assertEquals(Sensor.INACTIVE, sensor.getState());
        setActiveEntryExit(dp, true);
        JUnitUtil.waitFor(() -> (Sensor.ACTIVE == sensor.getState()),
            () -> "Sensor '" + sensor.getDisplayName() + "' goes active on " + comment);
        assertEquals(Sensor.ACTIVE, sensor.getState());
        setActiveEntryExit(dp, false);
        JUnitUtil.waitFor(() -> (Sensor.INACTIVE == sensor.getState()),
            () -> "Sensor '" + sensor.getDisplayName() + "' goes inactive on " + comment);
        assertEquals(Sensor.INACTIVE, sensor.getState());
    }

    @Test
    public void testExpressionEntryExit() throws JmriException {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        Sensor sensor211 = InstanceManager.getDefault(SensorManager.class).getByUserName("NX Active");
        assertNotNull(sensor211);
        sensor211.setState(Sensor.INACTIVE);
        assertEquals(Sensor.INACTIVE, sensor211.getState());

        DestinationPoints dp = InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean("NX-Left-TO-A (Left-TO-A) to NX-Right-TO-C (Right-TO-C)");
        assertNotNull(dp);

        // Test entry/exit
        runTestExpressionEntryExit(dp, sensor211, "run1");

        JUnitUtil.waitFor(150);

        for (Logix l : logixManager.getNamedBeanSet()) {
            ImportLogix il = new ImportLogix(l, true);
            il.doImport();
            il.getLogixNG().setEnabled(true);
        }

        deleteAllLogixs();
        JUnitUtil.waitFor(150);

        // Test entry/exit
        runTestExpressionEntryExit(dp, sensor211, "run2");
    }

    @BeforeEach
    public void setUp() throws JmriException {

        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initLogixNGManager();

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // We want the conditionalNGs run immediately during this test
        InstanceManager.getDefault(ConditionalNG_Manager.class).setRunOnGUIDelayed(false);

        logixManager = InstanceManager.getDefault(LogixManager.class);
        logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);


        ConfigureManager cm = InstanceManager.getNullableDefault( ConfigureManager.class);
        assertNotNull(cm);
        java.io.File file = new java.io.File("java/test/jmri/jmrit/logixng/tools/LogixNG_Test_Dave_Sand.xml");
        boolean results = ThreadingUtil.runOnGUIwithReturn( () ->
            assertDoesNotThrow( () -> cm.load(file)));
        assertTrue(results);

        logixManager.activateAllLogixs();
        logixNG_Manager.activateAllLogixNGs();


/*


        InstanceManager.getDefault(WarrantManager.class).register(new Warrant("IW1", null));

        logixManager = InstanceManager.getDefault(LogixManager.class);
        ConditionalManager conditionalManager = InstanceManager.getDefault(ConditionalManager.class);

        logix = logixManager.createNewLogix("IX1", null);

        conditional = conditionalManager.createNewConditional("IX1C1", "First conditional");
        logix.addConditional(conditional.getSystemName(), 0);

        conditional.setTriggerOnChange(true);
        conditional.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");

        variables = new ArrayList<>();

        cv = new ConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.NONE);
        cv.setName("IW1");
        variables.add(cv);
        conditional.setStateVariables(variables);

        actions = new ArrayList<>();
        conditional.setAction(actions);
*/
    }

    @AfterEach
    public void tearDown() {

        JUnitUtil.disposeFrame("My Layout", false, true);

        JUnitAppender.suppressWarnMessage("Import Conditional 'IX:AUTO:0001C1' to LogixNG 'IQ:AUTO:0001'");
        JUnitAppender.suppressWarnMessage("Import Conditional 'IX:AUTO:0002C1' to LogixNG 'IQ:AUTO:0002'");
        JUnitAppender.suppressWarnMessage("Import Conditional 'IX:AUTO:0003C1' to LogixNG 'IQ:AUTO:0003'");
        JUnitAppender.suppressWarnMessage("Import Conditional 'IX:AUTO:0004C1' to LogixNG 'IQ:AUTO:0004'");
        JUnitAppender.suppressWarnMessage("Import Conditional 'IX:RTXINITIALIZER1T' to LogixNG 'IQ:AUTO:0005'");

        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.clearRouteThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
