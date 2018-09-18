package jmri.implementation;

import static jmri.Conditional.*;

import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.SensorManager;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;
import jmri.TurnoutManager;
import jmri.RouteManager;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
/**
 * Test the DefaultConditionalAction implementation class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class DefaultConditionalActionTest {

    @Test
    public void testCtor() {
        new DefaultConditionalAction();
    }

    @Test
    public void testBasicBeanOperations() {
        ConditionalAction ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE,ACTION_SET_TURNOUT,"3",4,"5");
        ConditionalAction ix2 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE,ACTION_SET_TURNOUT,"3",4,"5");

        ConditionalAction ix3 = new DefaultConditionalAction(0,ACTION_SET_TURNOUT,"3",4,"5");
        ConditionalAction ix4 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE,0,"3",4,"5");
        ConditionalAction ix5 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE,ACTION_SET_TURNOUT,"0",4,"5");
        ConditionalAction ix6 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE,ACTION_SET_TURNOUT,"3",0,"5");
        ConditionalAction ix7 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE,ACTION_SET_TURNOUT,"3",4,"0");

        ConditionalAction ix8 = new DefaultConditionalAction(0,Conditional.ACTION_NONE,null,4,"5");
        
        Assert.assertTrue(!ix1.equals(null));
        Assert.assertTrue(ix1.equals(ix1));
        Assert.assertTrue(ix1.equals(ix2));

        Assert.assertTrue(!ix1.equals(ix3));
        Assert.assertTrue(!ix1.equals(ix4));
        Assert.assertTrue(!ix1.equals(ix5));
        Assert.assertTrue(!ix1.equals(ix6));
        Assert.assertTrue(!ix1.equals(ix7));

        // Test equal with different class
        Assert.assertTrue(!ix1.equals(new Object()));
        
        // Test deviceName == null
        Assert.assertTrue(!ix1.equals(ix8));
        Assert.assertTrue(!ix8.equals(ix1));
        
        Assert.assertTrue(ix1.hashCode() == ix2.hashCode());
    }
    
    @Test
    public void testGetActionBean() {
        ConditionalAction ix1;
        NamedBean bean;
        String devName = "3";
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_SENSOR, devName,4,"5");
        bean = InstanceManager.getDefault(SensorManager.class).provideSensor(devName);
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, devName,4,"5");
        bean = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(devName);
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_MEMORY, devName,4,"5");
        bean = InstanceManager.getDefault(MemoryManager.class).provideMemory(devName);
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_LIGHT, devName,4,"5");
        bean = InstanceManager.getDefault(LightManager.class).getLight(devName);
//DANIEL        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
//DANIEL        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_SIGNALMAST_HELD, devName,4,"5");
//DANIEL        bean = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast(devName);
//DANIEL        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_SIGNAL_APPEARANCE, devName,4,"5");
        bean = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(devName);
//DANIEL        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_MANUAL_RUN_WARRANT, devName,4,"5");
        bean = InstanceManager.getDefault(WarrantManager.class).getWarrant(devName);
//DANIEL        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_BLOCK_VALUE, devName,4,"5");
        bean = InstanceManager.getDefault(OBlockManager.class).getOBlock(devName);
//DANIEL        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_NXPAIR_ENABLED, devName,4,"5");
        bean = InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean(devName);
//DANIEL        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_TRIGGER_ROUTE, devName,4,"5");
        bean = InstanceManager.getDefault(RouteManager.class).getRoute(devName);
//DANIEL        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
    }
    

    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDefaultSignalMastManager();
        jmri.util.JUnitUtil.initSignalMastLogicManager();
        jmri.util.JUnitUtil.initWarrantManager();
        jmri.util.JUnitUtil.initOBlockManager();
        jmri.util.JUnitUtil.initRouteManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

}
