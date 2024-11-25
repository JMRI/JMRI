package jmri.jmrit.logixng.tools;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jmri.*;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logix.*;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.*;

import org.junit.*;

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
public class ImportEntryExitTest {

    @Rule
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

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

    private void runTestActionEntryExit(DestinationPoints dp, Sensor sensor) throws JmriException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Assert.assertFalse(dp.isEnabled());
        sensor.setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> (dp.isEnabled()),"destination point enabled");
        Assert.assertTrue(dp.isEnabled());
        sensor.setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> (!dp.isEnabled()),"destination point disabled");
        Assert.assertFalse(dp.isEnabled());
    }

    @Test
    public void testActionEntryExit() throws InterruptedException, JmriException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

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
        Assert.assertNotNull(sensor201);
        sensor201.setState(Sensor.ACTIVE);
        Assert.assertEquals(Sensor.ACTIVE, sensor201.getState());

        DestinationPoints dp = InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean("NX-Left-TO-A (Left-TO-A) to NX-Right-TO-B (Right-TO-B)");
        Assert.assertNotNull(dp);

        // Test entry/exit
        runTestActionEntryExit(dp, sensor201);

        for (Logix l : logixManager.getNamedBeanSet()) {
            ImportLogix il = new ImportLogix(l, true);
            il.doImport();
            il.getLogixNG().setEnabled(true);
        }

        deleteAllLogixs();

        // Test entry/exit
        runTestActionEntryExit(dp, sensor201);
    }

    private void setActiveEntryExit(DestinationPoints dp, boolean boo) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        Method retrieveItems = dp.getClass().getDeclaredMethod("isEnabled", String.class);
        Method setActiveEntryExit = dp.getClass().getDeclaredMethod("setActiveEntryExit", boolean.class);
        setActiveEntryExit.setAccessible(true);
        setActiveEntryExit.invoke(dp, boo);
    }

    private void runTestExpressionEntryExit(DestinationPoints dp, Sensor sensor) throws JmriException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Assert.assertEquals(Sensor.INACTIVE, sensor.getState());
        setActiveEntryExit(dp, true);
        JUnitUtil.waitFor(() -> (Sensor.ACTIVE == sensor.getState()),"Sensor goes active");
        Assert.assertEquals(Sensor.ACTIVE, sensor.getState());
        setActiveEntryExit(dp, false);
        JUnitUtil.waitFor(() -> (Sensor.INACTIVE == sensor.getState()),"Sensor goes inactive");
        Assert.assertEquals(Sensor.INACTIVE, sensor.getState());
    }

    @Test
    public void testExpressionEntryExit() throws InterruptedException, JmriException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        Sensor sensor211 = InstanceManager.getDefault(SensorManager.class).getByUserName("NX Active");
        Assert.assertNotNull(sensor211);
        sensor211.setState(Sensor.INACTIVE);
        Assert.assertEquals(Sensor.INACTIVE, sensor211.getState());

        DestinationPoints dp = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean("NX-Left-TO-A (Left-TO-A) to NX-Right-TO-C (Right-TO-C)");
        Assert.assertNotNull(dp);

        // Test entry/exit
        runTestExpressionEntryExit(dp, sensor211);

        for (Logix l : logixManager.getNamedBeanSet()) {
            ImportLogix il = new ImportLogix(l, true);
            il.doImport();
            il.getLogixNG().setEnabled(true);
        }

        deleteAllLogixs();

        // Test entry/exit
        runTestExpressionEntryExit(dp, sensor211);
    }

    @Before
    public void setUp() throws JmriException {

        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

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


        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        Assert.assertNotNull(cm);
        java.io.File file = new java.io.File("java/test/jmri/jmrit/logixng/tools/LogixNG_Test_Dave_Sand.xml");
        boolean results = cm.load(file);
        Assert.assertTrue(results);

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

    @After
    public void tearDown() {

        JUnitUtil.resetWindows(false, false);

        JUnitAppender.suppressWarnMessageStartsWith("Import Conditional 'IX1C1' to LogixNG 'IQ:AUTO:000'");
        JUnitAppender.suppressWarnMessage("Import Conditional 'IX:RTXINITIALIZER1T' to LogixNG 'IQ:AUTO:0005'");

        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.clearRouteThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
