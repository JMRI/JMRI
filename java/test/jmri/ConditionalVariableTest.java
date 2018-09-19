package jmri;

import static jmri.Conditional.*;

import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the Path class
 *
 * @author	Bob Jacobsen Copyright (C) 2016
 */
public class ConditionalVariableTest {

    @Test
    public void testCtor() {
        NamedBean bean;
        NamedBean otherBean;
        String deviceName = "3";
        String otherDeviceName = "4";
        
        bean = InstanceManager.getDefault(SensorManager.class).provideSensor(deviceName);
        otherBean = InstanceManager.getDefault(SensorManager.class).provideSensor(otherDeviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, ITEM_TYPE_SENSOR, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName(otherDeviceName);
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        bean = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(deviceName);
        otherBean = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_TURNOUT_THROWN, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName(otherDeviceName);
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        bean = InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        otherBean = InstanceManager.getDefault(MemoryManager.class).provideMemory(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName(otherDeviceName);
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        bean = InstanceManager.getDefault(LightManager.class).provideLight(deviceName);
        otherBean = InstanceManager.getDefault(LightManager.class).provideLight(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_LIGHT_ON, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName(otherBean.getSystemName());
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        // Note that the signal head IH1 created here are also used to test the signal mast.
        SignalHead signalHeadIH1 = new VirtualSignalHead("IH1");
        SignalHead signalHeadIH2 = new VirtualSignalHead("IH2");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH1);
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH2);
        bean = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH1");
        otherBean = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH2");
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_SIGNAL_HEAD_RED, "IH1", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("IH2");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        bean = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        otherBean = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH2)");
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_SIGNAL_MAST_ASPECT_EQUALS, "IF$shsm:AAR-1946:CPL(IH1)", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("IF$shsm:AAR-1946:CPL(IH2)");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        InstanceManager.getDefault(LogixManager.class).createNewLogix("IX:AUTO:0002");
        bean = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX:AUTO:0001C1", "Conditional");
        otherBean = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX:AUTO:0001C2", "Conditional");
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_CONDITIONAL_TRUE, "IX:AUTO:0001C1", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("IX:AUTO:0001C2");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        bean = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW3");
        otherBean = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW4");
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_ROUTE_OCCUPIED, "IW3", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("IW4");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        bean = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB3");
        otherBean = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB4");
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_BLOCK_STATUS_EQUALS, "OB3", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("OB4");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
    }
    
    @Test
    public void testConstants() {
        // It might be a good idea to change constants into enums.
        // These tests ensures that the values of the constants stay the same
        // if that change is done.
        
        Assert.assertEquals(ConditionalVariable.NUM_COMPARE_OPERATIONS, 5);
        Assert.assertEquals(ConditionalVariable.LESS_THAN, 1);
        Assert.assertEquals(ConditionalVariable.LESS_THAN_OR_EQUAL, 2);
        Assert.assertEquals(ConditionalVariable.EQUAL, 3);
        Assert.assertEquals(ConditionalVariable.GREATER_THAN_OR_EQUAL, 4);
        Assert.assertEquals(ConditionalVariable.GREATER_THAN, 5);
    }
    
    @Test
    public void testEquals() {
        ConditionalVariable c1 = new ConditionalVariable(false, 1, 2, "name", false);
        ConditionalVariable c2 = new ConditionalVariable(false, 1, 2, "name", false);

        Assert.assertTrue("identity", c1.equals(c1));
        Assert.assertFalse("object equals, not content equals", c1.equals(c2));
    }
    
    @Test
    public void testDataString() {
        NamedBean bean;
        NamedBean otherBean;
        String deviceName = "3";
        String otherDeviceName = "4";
        
        bean = InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        bean.setUserName("BeanUserName");
        otherBean = InstanceManager.getDefault(MemoryManager.class).provideMemory(otherDeviceName);
        otherBean.setUserName("OtherBeanUserName");
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("getDataString() returns empty string", "".equals(cv.getDataString()));
        Assert.assertTrue("getNamedBeanData() returns null", cv.getNamedBeanData() == null);
        cv.setDataString(otherBean.getUserName());
        cv.setName(otherDeviceName);
        Assert.assertTrue("getDataString() returns correct string", otherBean.getUserName().equals(cv.getDataString()));
        Assert.assertTrue("getNamedBeanData() returns correct bean", otherBean.equals(cv.getNamedBeanData()));
    }
    
    @Test
    public void testState() {
        String deviceName = "3";
        InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("state is unknown", cv.getState() == NamedBean.UNKNOWN);
        cv.setState(Conditional.TRUE);
        Assert.assertTrue("state is TRUE", cv.getState() == Conditional.TRUE);
        cv.setState(Conditional.FALSE);
        Assert.assertTrue("state is FALSE", cv.getState() == Conditional.FALSE);
        cv.setState(true);
        Assert.assertTrue("state is TRUE", cv.getState() == Conditional.TRUE);
        cv.setState(false);
        Assert.assertTrue("state is FALSE", cv.getState() == Conditional.FALSE);
    }
    
    
    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
