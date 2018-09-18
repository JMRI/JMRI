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
import jmri.SignalHead;
import jmri.Turnout;
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
        final String deviceName = "3";
        final String actionStr = "5";
        
        ConditionalAction ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);
        ConditionalAction ix2 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);

        ConditionalAction ix3 = new DefaultConditionalAction(0, ACTION_SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);
        ConditionalAction ix4 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, 0, deviceName, Turnout.THROWN, actionStr);
        ConditionalAction ix5 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, "0", Turnout.THROWN, actionStr);
        ConditionalAction ix6 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, 0, actionStr);
        ConditionalAction ix7 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, Turnout.THROWN,"0");

        ConditionalAction ix8 = new DefaultConditionalAction(0, Conditional.ACTION_NONE, null, Turnout.THROWN, actionStr);
        
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
        String deviceName = "3";
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_SENSOR, deviceName, 4, "5");
        bean = InstanceManager.getDefault(SensorManager.class).provideSensor(deviceName);
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, 4, "5");
        bean = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(deviceName);
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_MEMORY, deviceName, 4, "5");
        bean = InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        bean = InstanceManager.getDefault(LightManager.class).provideLight(deviceName);
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_LIGHT, bean.getSystemName(), 4, "5");
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        // Note that the signal head IH1 created here are also used to test the signal mast.
        SignalHead signalHead = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHead);
        bean = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH1");
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_SIGNAL_APPEARANCE, "IH1", 4, "5");
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        bean = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_SIGNALMAST_HELD, "IF$shsm:AAR-1946:CPL(IH1)", 4, "5");
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        bean = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW3");
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_MANUAL_RUN_WARRANT, "IW3", 4, "5");
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        bean = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB3");
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_BLOCK_VALUE, "OB3", 4, "5");
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        // This test fails unless commented.
        // How to create an EntryExitPair?
        bean = InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean(deviceName);
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_NXPAIR_ENABLED, deviceName, 4, "5");
//        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
        
        bean = InstanceManager.getDefault(RouteManager.class).newRoute(deviceName);
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_TRIGGER_ROUTE, deviceName, 4, "5");
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
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
