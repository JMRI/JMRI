package jmri.jmrit.logixng.tools;

import java.util.ArrayList;
import java.util.SortedSet;

import jmri.*;
import jmri.implementation.DefaultConditionalAction;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 * This class is base class for the expression tests
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public abstract class ImportExpressionComplexTestBase {
    
    public enum Setup {
        Init,
        Fail1,
        Fail2,
        Fail3,
        Succeed1,
        Succeed2,
        Succeed3,
        Succeed4,
    }
    
    private LogixManager logixManager;
    protected Logix logix;
    protected Conditional conditional;
    private ArrayList<ConditionalVariable> variables;
    private ArrayList<ConditionalAction> actions;
    private Turnout t1;
    
    /**
     * Some NamedBeans, for example Light, does not support other states than ON and OFF
     * @return true if other states than ON and OFF is allowed
     */
    public boolean isStateOtherAllowed() {
        return true;
    }
    
    /**
     * Set the sensor, turnout, or other bean to the desired state.
     * <P>
     * If the parameter expectSuccess is true, this method should setup the
     * bean so that the Logix/LogixNG will be successfull if the Logix/LogixNG
     * is executed.
     * 
     * @param e the enum
     * @param setup the setup
     * @throws jmri.JmriException in case of an exception
     */
    abstract public void setNamedBeanState(Enum e, Setup setup) throws JmriException;
    
    /**
     * Create a new conditional variable of the desired type
     * @return the conditional variable
     */
    abstract public ConditionalVariable newConditionalVariable();
    
    
    public void assertBoolean(String message, boolean expectSuccess, boolean result) {
        if (expectSuccess) {
            Assert.assertTrue(message, result);
        } else {
            Assert.assertFalse(message, result);
        }
    }
    
    abstract public Enum[] getEnums();
    
    public Enum getOtherEnum(Enum e) {
        Enum[] enums = getEnums();
        int value = e.ordinal() + 1;
        if (value >= enums.length) value -= enums.length;
        return enums[value];
    }
    
    public Enum getThirdEnum(Enum e) {
        Enum[] enums = getEnums();
        int value = e.ordinal() + 2;
        if (value >= enums.length) value -= enums.length;
        return enums[value];
    }
    
    public void testEnum(Enum e) throws JmriException {
//        Enum otherE = getOtherEnum(e);
//        Enum thirdE = getThirdEnum(e);
        
        RunTestScaffold check = (message, expectSuccess) -> {
            setNamedBeanState(e, Setup.Init);
//            setConditionalVariableState(e);
            t1.setState(Turnout.CLOSED);
            
            // This should not throw the turnout
            setNamedBeanState(e, Setup.Fail1);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
            // This should not throw the turnout
            setNamedBeanState(e, Setup.Fail2);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
            // This should not throw the turnout
            setNamedBeanState(e, Setup.Fail3);
            assertBoolean(message, true, t1.getState() == Turnout.CLOSED);
            
//            setNamedBeanState(e, Setup.Init);
//            setConditionalVariableState(e);
            setNamedBeanState(e, Setup.Fail1);
            t1.setState(Turnout.CLOSED);
            // This should throw the turnout if Logix/LogixNG is activated
            setNamedBeanState(e, Setup.Succeed1);
            if (expectSuccess) JUnitUtil.waitFor(() -> t1.getState() == Turnout.THROWN);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
            setNamedBeanState(e, Setup.Fail1);
            t1.setState(Turnout.CLOSED);
            // This should throw the turnout if Logix/LogixNG is activated
            setNamedBeanState(e, Setup.Succeed2);
            if (expectSuccess) JUnitUtil.waitFor(() -> t1.getState() == Turnout.THROWN);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
            
            setNamedBeanState(e, Setup.Fail1);
            t1.setState(Turnout.CLOSED);
            // This should throw the turnout if Logix/LogixNG is activated
            setNamedBeanState(e, Setup.Succeed3);
            if (expectSuccess) JUnitUtil.waitFor(() -> t1.getState() == Turnout.THROWN);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
            
            setNamedBeanState(e, Setup.Fail1);
            t1.setState(Turnout.CLOSED);
            // This should throw the turnout if Logix/LogixNG is activated
            setNamedBeanState(e, Setup.Succeed4);
            if (expectSuccess) JUnitUtil.waitFor(() -> t1.getState() == Turnout.THROWN);
            assertBoolean(message, expectSuccess, t1.getState() == Turnout.THROWN);
        };
        
        
        check.runTest("Logix is not activated. Enum: "+e.name(), false);
        
        logixManager.activateAllLogixs();
        
        check.runTest("Logix is activated. Enum: "+e.name(), true);
        
        logix.deActivateLogix();
        conditional = null;
        
        // Import the logix to LogixNG
        ImportLogix importLogix = new ImportLogix(logix);
        importLogix.doImport();
        
//        logix.setEnabled(false);
//        logixManager.deleteLogix(logix);
//        ConditionalManager conditionalManager = InstanceManager.getDefault(ConditionalManager.class);
//        SortedSet<Conditional> set = conditionalManager.getNamedBeanSet();
//        for (Conditional c : set) conditionalManager.deleteConditional(c);
        
        check.runTest("Logix is deactivated and LogixNG is not activated. Enum: "+e.name(), false);
//        check.runTest("Logix is removed and LogixNG is not activated. Enum: "+e.name(), false);
        
        // We want the conditionalNGs run immediately during this test
        InstanceManager.getDefault(ConditionalNG_Manager.class).setRunOnGUIDelayed(false);
        
        importLogix.getLogixNG().setEnabled(true);
        InstanceManager.getDefault(LogixNG_Manager.class)
                .activateAllLogixNGs(false, false);
        
        check.runTest("LogixNG is activated. Enum: "+e.name(), true);
        
        importLogix.getLogixNG().setEnabled(false);
        InstanceManager.getDefault(LogixNG_Manager.class).deleteLogixNG(importLogix.getLogixNG());
        
        check.runTest("LogixNG is removed. Enum: "+e.name(), false);
    }
    
    @Test
    public void testAll() throws JmriException {
        for (Enum e : getEnums()) {
//            if (e.name().startsWith("Memory")) continue;
//            if (e.name().equals("MemoryEquals")) continue;
//            if (e.name().equals("ConstantEquals")) continue;
//            if (e.name().equals("ConstantCompareLessThan")) continue;
            setupTest();
//            System.out.format("Test enum: %s%n", e.name());
            testEnum(e);
//            System.out.format("Test enum: %s done%n", e.name());
            teardownTest();
        }
    }
    
//    // The minimal setup for log4J
//    @Before
//    public void setUp() {
    public void setupTest() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initLogixNGManager();
        
        t1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        
        logixManager = InstanceManager.getDefault(LogixManager.class);
        ConditionalManager conditionalManager = InstanceManager.getDefault(ConditionalManager.class);
        
        logix = logixManager.createNewLogix("IX1", null);
        logix.setEnabled(true);
        
        conditional = conditionalManager.createNewConditional("IX1C1", "First conditional");
        logix.addConditional(conditional.getSystemName(), 0);
        
        conditional.setTriggerOnChange(true);
        conditional.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
        
        variables = new ArrayList<>();
        ConditionalVariable cv = newConditionalVariable();
        cv.setTriggerActions(true);
        cv.setNegation(false);
        cv.setNum1(0);
        cv.setNum2(0);
        cv.setOpern(Conditional.Operator.AND);
        cv.setType(Conditional.Type.SENSOR_ACTIVE);
        variables.add(cv);
        conditional.setStateVariables(variables);
        
        actions = new ArrayList<>();
        ConditionalAction ca = new DefaultConditionalAction();
        ca.setType(Conditional.Action.SET_TURNOUT);
        ca.setActionData(Turnout.THROWN);
        ca.setDeviceName("IT1");
        actions.add(ca);
        conditional.setAction(actions);
        
        InstanceManager.getDefault(LogixNG_Manager.class)
                .activateAllLogixNGs(false, false);
    }

//    @After
//    public void tearDown() {
    public void teardownTest() {
        // JUnitAppender.clearBacklog();    REMOVE THIS!!!
        
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
