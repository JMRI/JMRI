package jmri.jmrit.logixng.tools;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.*;

import jmri.*;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class loads a panel file with some Logix, test that the Logix works,
 * then import the Logixs to LogixNG, then removes all the Logixs, and then
 * test that the LogixNGs works.
 * <P>
 This test tests expression warrant
 * 
 * @author Daniel Bergqvist  (C) 2021
 * @author Dave Sand         (C) 2021 (Dave Sand created the panel file)
 */
public class ImportTest {

    @Rule
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

    private LogixManager logixManager;
    private LogixNG_Manager logixNG_Manager;
    
//    private Logix logix;
//    private Conditional conditional;
//    private ArrayList<ConditionalVariable> variables;
//    private ArrayList<ConditionalAction> actions;
//    ConditionalVariable cv;
    
    private void deleteAllLogixs() {
        // Remove all Logixs. Avoid concurrent modification by calling
        // getNamedBeanSet() on each iteration.
        while (! logixManager.getNamedBeanSet().isEmpty()) {
            Logix l = logixManager.getNamedBeanSet().first();
            l.setEnabled(false);
            logixManager.deleteLogix(l);
        }
    }
    
    private boolean destinationPointsIsEnabled(DestinationPoints dp) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        Method retrieveItems = dp.getClass().getDeclaredMethod("isEnabled", String.class);
        Method isEnabled = dp.getClass().getDeclaredMethod("isEnabled");
        isEnabled.setAccessible(true);
        return (boolean)isEnabled.invoke(dp);
    }
    
    private void runTestEntryExit(DestinationPoints dp, Sensor sensor) throws JmriException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Assert.assertFalse(destinationPointsIsEnabled(dp));
        sensor.setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> (destinationPointsIsEnabled(dp)));
        Assert.assertTrue(destinationPointsIsEnabled(dp));
        sensor.setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> (!destinationPointsIsEnabled(dp)));
        Assert.assertFalse(destinationPointsIsEnabled(dp));
    }
    
    @Test
    @Ignore
    public void testEntryExit() throws InterruptedException, JmriException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
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
        
        DestinationPoints dp = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean("NX-Left-TO-A (Left-TO-A) to NX-Right-TO-B (Right-TO-B)");
        Assert.assertNotNull(dp);
        
        // Test entry/exit
        runTestEntryExit(dp, sensor201);
        
        for (Logix l : logixManager.getNamedBeanSet()) {
            ImportLogix il = new ImportLogix(l, true);
            il.doImport();
            il.getLogixNG().setEnabled(true);
        }
/*        
        final String treeIndent = "   ";
        java.io.StringWriter stringWriter = new java.io.StringWriter();
        java.io.PrintWriter printWriter = new java.io.PrintWriter(stringWriter);
        logixNG_Manager.printTree(Locale.ENGLISH, printWriter, treeIndent);
        System.out.println(stringWriter.toString());
*/        
        deleteAllLogixs();
        
        // Test entry/exit
        runTestEntryExit(dp, sensor201);
    }
    
    private void runTestSetRoute(Turnout turnout101, Turnout turnout102, Sensor sensor) throws JmriException {
        turnout101.setState(Turnout.THROWN);
        Assert.assertEquals(Turnout.THROWN, turnout101.getState());
        turnout102.setState(Turnout.THROWN);
        Assert.assertEquals(Turnout.THROWN, turnout102.getState());
        
        sensor.setState(Sensor.ACTIVE);
        
        JUnitUtil.waitFor(() -> (turnout101.getState() == Turnout.CLOSED));
        JUnitUtil.waitFor(() -> (turnout102.getState() == Turnout.CLOSED));
        
        Assert.assertEquals(Turnout.CLOSED, turnout101.getState());
        Assert.assertEquals(Turnout.CLOSED, turnout102.getState());
    }
    
    @Test
    public void testSetRoute() throws InterruptedException, JmriException {
        // TRIGGER_ROUTE
        Sensor sensor210 = InstanceManager.getDefault(SensorManager.class).getByUserName("Trigger Route");
        Assert.assertNotNull(sensor210);
//        for (Route r : InstanceManager.getDefault(RouteManager.class).getNamedBeanSet()) {
//            System.out.format("Route: %s, %s%n", r.getSystemName(), r.getUserName());
//        }
        
        Route routeTurnouts = InstanceManager.getDefault(RouteManager.class).getRoute("Turnouts");
        Assert.assertNotNull(routeTurnouts);
        
        Turnout turnout101 = InstanceManager.getDefault(TurnoutManager.class).getBySystemName("IT101");
        Assert.assertNotNull(turnout101);
        
        Turnout turnout102 = InstanceManager.getDefault(TurnoutManager.class).getBySystemName("IT101");
        Assert.assertNotNull(turnout102);
        
        // Test route
        runTestSetRoute(turnout101, turnout102, sensor210);
        
        for (Logix l : logixManager.getNamedBeanSet()) {
            ImportLogix il = new ImportLogix(l, true);
            il.doImport();
            il.getLogixNG().setEnabled(true);
        }
        
        deleteAllLogixs();
        
        // Test route
        runTestSetRoute(turnout101, turnout102, sensor210);
    }
    
    
    
    // The minimal setup for log4J
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
        logixNG_Manager.activateAllLogixNGs(false, false);
        
        
        
        
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
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
