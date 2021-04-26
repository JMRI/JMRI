package jmri.jmrit.logixng.tools;

import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logix.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 This test tests expression warrant
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportExpressionWarrantTest {

    private LogixManager logixManager;
    private Logix logix;
    private Conditional conditional;
    private ArrayList<ConditionalVariable> variables;
    private ArrayList<ConditionalAction> actions;
    ConditionalVariable cv;
    
    
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
        
        
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
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
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
