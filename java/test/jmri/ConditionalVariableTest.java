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
        String deviceName = "3";
        
        bean = InstanceManager.getDefault(SensorManager.class).provideSensor(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, ITEM_TYPE_SENSOR, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        bean = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(deviceName);
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_TURNOUT_THROWN, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        bean = InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        bean = InstanceManager.getDefault(LightManager.class).provideLight(deviceName);
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_LIGHT_ON, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        // Note that the signal head IH1 created here are also used to test the signal mast.
        SignalHead signalHead = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHead);
        bean = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH1");
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_SIGNAL_HEAD_RED, "IH1", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        bean = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_SIGNAL_MAST_ASPECT_EQUALS, "IF$shsm:AAR-1946:CPL(IH1)", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        InstanceManager.getDefault(LogixManager.class).createNewLogix("IX:AUTO:0002");
        bean = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX:AUTO:0001C1", "Conditional");
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_CONDITIONAL_TRUE, "IX:AUTO:0001C1", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        bean = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW3");
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_ROUTE_OCCUPIED, "IW3", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        
        bean = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB3");
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_BLOCK_STATUS_EQUALS, "OB3", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
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
