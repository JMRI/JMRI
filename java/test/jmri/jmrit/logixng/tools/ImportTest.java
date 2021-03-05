package jmri.jmrit.logixng.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import jmri.*;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logix.*;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
/*    
    @Test
    public void testRouteFree() throws JmriException {
        cv.setType(Conditional.Type.ROUTE_FREE);
        ImportLogix importLogix = new ImportLogix(logix);
        importLogix.doImport();
        Assert.assertNotNull(importLogix.getLogixNG().getConditionalNG(0));
    }
    
    @Test
    public void testRouteOccupied() throws JmriException {
        cv.setType(Conditional.Type.ROUTE_OCCUPIED);
        ImportLogix importLogix = new ImportLogix(logix);
        importLogix.doImport();
        Assert.assertNotNull(importLogix.getLogixNG().getConditionalNG(0));
    }
    
    @Test
    public void testRouteAllocated() throws JmriException {
        cv.setType(Conditional.Type.ROUTE_ALLOCATED);
        ImportLogix importLogix = new ImportLogix(logix);
        importLogix.doImport();
        Assert.assertNotNull(importLogix.getLogixNG().getConditionalNG(0));
    }
    
    @Test
    public void testRouteSet() throws JmriException {
        cv.setType(Conditional.Type.ROUTE_SET);
        ImportLogix importLogix = new ImportLogix(logix);
        importLogix.doImport();
        Assert.assertNotNull(importLogix.getLogixNG().getConditionalNG(0));
    }
    
    @Test
    public void testTrainRunning() throws JmriException {
        cv.setType(Conditional.Type.TRAIN_RUNNING);
        ImportLogix importLogix = new ImportLogix(logix);
        importLogix.doImport();
        Assert.assertNotNull(importLogix.getLogixNG().getConditionalNG(0));
    }
*/    
    
//    @Ignore
//    @Test
//    public void testSomething() throws InterruptedException, JmriException {
//        
//    }
    
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
/*    
    private void doSomething() {
        java.beans.PropertyChangeEvent evt = null;
        Conditional c = logixManager.getBySystemName("IX:AUTO:0001").getConditional("IX:AUTO:0001C1");
//        c.calculate(true, evt);
        ConditionalVariable cv2 = c.getCopyOfStateVariables().get(0);
        System.out.format("Logic: %s, %s, triggerOnChange: %b%n", c.getLogicType().name(), c.getAntecedentExpression(), c.getTriggerOnChange());
        System.out.format("trig: %b, bean: %s, data: %s, beanHandle: %s, beanData: %s, n1: %d, n2: %d, oper: %s, operStr: %s, state: %d, typeStr: %s, type: %s, neg: %b%n",
            cv2.doTriggerActions(),
            cv2.getBean() != null ? cv2.getBean().getSystemName() : null,
            cv2.getDataString(),
            cv2.getNamedBean() != null ? cv2.getNamedBean().getBean().getSystemName() : null,
            cv2.getNamedBeanData(),
            cv2.getNum1(),
            cv2.getNum2(),
            cv2.getOpern().name(),
            cv2.getOpernString(),
            cv2.getState(),
            cv2.getTestTypeString(),
            cv2.getType().name(),
            cv2.isNegated()
        );
        ConditionalAction ca = c.getCopyOfActions().get(0);
        System.out.format("data: %s, dataStr: %s, str: %s, bean: %s, device: %s, namedBean: %s, option: %d, optionStr: %s, type: %s, typeStr: %s%n",
            ca.getActionData(),
            ca.getActionDataString(),
            ca.getActionString(),
            ca.getBean() != null ? ca.getBean().getSystemName() : null,
            ca.getDeviceName(),
            ca.getNamedBean() != null ? ca.getNamedBean().getBean().getSystemName() : null,
            ca.getOption(),
            ca.getOptionString(true),
            ca.getType().name(),
            ca.getTypeString()
        );
        ca = c.getCopyOfActions().get(1);
        System.out.format("data: %s, dataStr: %s, str: %s, bean: %s, device: %s, namedBean: %s, option: %d, optionStr: %s, type: %s, typeStr: %s%n",
            ca.getActionData(),
            ca.getActionDataString(),
            ca.getActionString(),
            ca.getBean() != null ? ca.getBean().getSystemName() : null,
            ca.getDeviceName(),
            ca.getNamedBean() != null ? ca.getNamedBean().getBean().getSystemName() : null,
            ca.getOption(),
            ca.getOptionString(true),
            ca.getType().name(),
            ca.getTypeString()
        );
    }
    
    @Ignore
    @Test
    public void testOBlock() throws InterruptedException, JmriException {
        // OBLOCK
        // SET_BLOCK_ERROR
        
    }
    
    @Ignore
    @Test
    public void testWarrant() throws InterruptedException, JmriException {
        // WARRANT
        // ALLOCATE_WARRANT_ROUTE
        for (Sensor r : InstanceManager.getDefault(SensorManager.class).getNamedBeanSet()) {
            System.out.format("Sensor: %s, %s%n", r.getSystemName(), r.getUserName());
        }
        
        
        Sensor sensor203 = InstanceManager.getDefault(SensorManager.class).getBySystemName("IS203");
        Assert.assertNotNull(sensor203);
        
        for (Warrant r : InstanceManager.getDefault(WarrantManager.class).getNamedBeanSet()) {
            System.out.format("Warrant: %s, %s%n", r.getSystemName(), r.getUserName());
        }
//        Warrant w = InstanceManager.getDefault(WarrantManager.class).getWarrant(_name);
    }
*/    
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
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
