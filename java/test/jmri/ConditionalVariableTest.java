package jmri;

import static jmri.Conditional.*;
import static jmri.ConditionalVariable.*;

import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Warrant;
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
        
        // Start with testing the exception handling in the constructor
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManagerThrowException();
        jmri.util.JUnitUtil.initLightManagerThrowException();
        jmri.util.JUnitUtil.initMemoryManagerThrowException();
        jmri.util.JUnitUtil.initInternalSensorManagerThrowException();
        jmri.util.JUnitUtil.initSignalHeadManagerThrowException();
        jmri.util.JUnitUtil.initSignalMastManagerThrowException();
        jmri.util.JUnitUtil.initWarrantManagerThrowException();
        jmri.util.JUnitUtil.initOBlockManagerThrowException();
        
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, ITEM_TYPE_SENSOR, deviceName, false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid sensor name= \"3\" in state variable");
        
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_TURNOUT_THROWN, deviceName, false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid turnout name= \"3\" in state variable");
        
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid memory name= \"3\" in state variable");
        
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_LIGHT_ON, deviceName, false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid light name= \"3\" in state variable");
        
        // Note that the signal head IH1 created here are also used to test the signal mast.
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_SIGNAL_HEAD_RED, "IH1", false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertWarnMessage("could not provide \"IH1\" in constructor");
        
        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_SIGNAL_MAST_ASPECT_EQUALS, "IF$shsm:AAR-1946:CPL(IH1)", false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid signalmast name= \"IF$shsm:AAR-1946:CPL(IH1)\" in state variable");
        
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_CONDITIONAL_TRUE, "IX:AUTO:0001C1", false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid conditional; name= \"IX:AUTO:0001C1\" in state variable");
        
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_ROUTE_OCCUPIED, "IW3", false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertWarnMessage("could not provide \"IW3\" in constructor");
        
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_BLOCK_STATUS_EQUALS, "OB3", false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertWarnMessage("could not provide \"OB3\" in constructor");
        
        
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initLogixManager();
        jmri.util.JUnitUtil.initIdTagManager();
        
        bean = InstanceManager.getDefault(SensorManager.class).provideSensor(deviceName);
        otherBean = InstanceManager.getDefault(SensorManager.class).provideSensor(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, ITEM_TYPE_SENSOR, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName(otherDeviceName);
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        Assert.assertTrue("toString() returns correct value",
                "Sensor \"4\" state is \"Sensor Active\"".equals(cv.toString()));
        
        bean = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(deviceName);
        otherBean = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_TURNOUT_THROWN, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName(otherDeviceName);
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        Assert.assertTrue("toString() returns correct value",
                "Turnout \"4\" state is \"Turnout Thrown\"".equals(cv.toString()));
        
        bean = InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        otherBean = InstanceManager.getDefault(MemoryManager.class).provideMemory(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName(otherDeviceName);
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setDataString("A desired memory value");
        Assert.assertTrue("toString() returns correct value",
                "Memory \"4\" = value \"A desired memory value\"".equals(cv.toString()));
        
        bean = InstanceManager.getDefault(LightManager.class).provideLight(deviceName);
        otherBean = InstanceManager.getDefault(LightManager.class).provideLight(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_LIGHT_ON, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName(otherBean.getSystemName());
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        Assert.assertTrue("toString() returns correct value",
                "Light \"IL4\" state is \"Light On\"".equals(cv.toString()));
        
        // Note that the signal head IH1 created here are also used to test the signal mast.
        SignalHead signalHeadIH1 = new VirtualSignalHead("IH1");
        SignalHead signalHeadIH2 = new VirtualSignalHead("IH2");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH1);
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH2);
        bean = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH1");
        otherBean = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH2");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_SIGNAL_HEAD_RED, "IH1", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("IH2");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        Assert.assertTrue("toString() returns correct value",
                "Signal Head \"IH2\" Appearance is \"Red\"".equals(cv.toString()));
        
        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        bean = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        otherBean = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH2)");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_SIGNAL_MAST_ASPECT_EQUALS, "IF$shsm:AAR-1946:CPL(IH1)", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("IF$shsm:AAR-1946:CPL(IH2)");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setDataString("Approach");
        Assert.assertTrue("toString() returns correct value",
                "Signal Mast \"IF$shsm:AAR-1946:CPL(IH2)\" Aspect is \"Approach\"".equals(cv.toString()));
        
        InstanceManager.getDefault(LogixManager.class).createNewLogix("IX:AUTO:0002");
        bean = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX:AUTO:0001C1", "Conditional");
        otherBean = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX:AUTO:0001C2", "Conditional");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_CONDITIONAL_TRUE, "IX:AUTO:0001C1", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("IX:AUTO:0001C2");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setGuiName("A Gui name");
        Assert.assertTrue("toString() returns correct value",
                "Conditional \"A Gui name\" state is \"Conditional True\"".equals(cv.toString()));
        
        bean = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW3");
        otherBean = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW4");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_ROUTE_OCCUPIED, "IW3", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("IW4");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        Assert.assertTrue("toString() returns correct value",
                "WarrantRoute \"IW4\" state is \"Occupied\"".equals(cv.toString()));
        
        bean = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB3");
        otherBean = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB4");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_BLOCK_STATUS_EQUALS, "OB3", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("OB4");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setDataString("block error");
        Assert.assertTrue("toString() returns correct value",
                "OBlock Status \"OB4\" state is \"block error\"".equals(cv.toString()));
        
        // Test a bad device name
        cv.setName("A bad device name");
        // setName should not change the bean if called with wrong name. For example, it should not set the bean to null.
        Assert.assertTrue("getName() still has correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
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
        ConditionalVariable c1 = new ConditionalVariable(false, Operator.AND, 2, "name", false);
        ConditionalVariable c2 = new ConditionalVariable(false, Operator.AND, 2, "name", false);

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
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_MEMORY_EQUALS, deviceName, false);
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
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_MEMORY_EQUALS, deviceName, false);
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
    
    @Test
    public void testGetOpernString() {
        String deviceName = "3";
        InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Memory Compare to Value (Case Sensitive)".equals(cv.getTestTypeString()));
        
        cv.setNegation(false);
        cv.setOpern(Operator.AND);
        Assert.assertTrue("getOpernString() returns correct value",
                "AND".equals(cv.getOpernString()));
        Assert.assertFalse("isNegated() returns false", cv.isNegated());
        
        cv.setNegation(true);
        cv.setOpern(Operator.NONE);
        Assert.assertTrue("getOpernString() returns correct value",
                "".equals(cv.getOpernString()));
        Assert.assertTrue("isNegated() returns true", cv.isNegated());
        
        cv.setNegation(true);
        cv.setOpern(Operator.AND);
        Assert.assertTrue("getOpernString() returns correct value",
                "AND".equals(cv.getOpernString()));
        Assert.assertTrue("isNegated() returns true", cv.isNegated());
        
        cv.setNegation(false);
        cv.setOpern(Operator.NONE);
        Assert.assertTrue("getOpernString() returns correct value",
                "".equals(cv.getOpernString()));
        Assert.assertFalse("isNegated() returns false", cv.isNegated());
        
        cv.setNegation(false);
        cv.setOpern(Operator.OR);
        Assert.assertTrue("getOpernString() returns correct value",
                "OR".equals(cv.getOpernString()));
        Assert.assertFalse("isNegated() returns false", cv.isNegated());
        
        cv.setNegation(true);
        cv.setOpern(Operator.OR);
        Assert.assertTrue("getOpernString() returns correct value",
                "OR".equals(cv.getOpernString()));
        Assert.assertTrue("isNegated() returns true", cv.isNegated());
    }
    
    @Test
    public void testGetTestTypeString() {
        String deviceName = "3";
        InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Memory Compare to Value (Case Sensitive)".equals(cv.getTestTypeString()));
        
        Assert.assertTrue("Sensor Active",
                "Sensor Active".equals(ConditionalVariable.getTestTypeString(TYPE_SENSOR_ACTIVE)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Sensor Inactive".equals(ConditionalVariable.getTestTypeString(TYPE_SENSOR_INACTIVE)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Turnout Thrown".equals(ConditionalVariable.getTestTypeString(TYPE_TURNOUT_THROWN)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Turnout Closed".equals(ConditionalVariable.getTestTypeString(TYPE_TURNOUT_CLOSED)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Conditional True".equals(ConditionalVariable.getTestTypeString(TYPE_CONDITIONAL_TRUE)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Conditional False".equals(ConditionalVariable.getTestTypeString(TYPE_CONDITIONAL_FALSE)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Light On".equals(ConditionalVariable.getTestTypeString(TYPE_LIGHT_ON)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Light Off".equals(ConditionalVariable.getTestTypeString(TYPE_LIGHT_OFF)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Memory Compare to Value (Case Sensitive)".equals(ConditionalVariable.getTestTypeString(TYPE_MEMORY_EQUALS)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Memory Compare to Memory (Case Sensitive)".equals(ConditionalVariable.getTestTypeString(TYPE_MEMORY_COMPARE)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Fast Clock Range".equals(ConditionalVariable.getTestTypeString(TYPE_FAST_CLOCK_RANGE)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Red".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_HEAD_RED)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Yellow".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_HEAD_YELLOW)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Green".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_HEAD_GREEN)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Dark".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_HEAD_DARK)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Flashing Red".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_HEAD_FLASHRED)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Flashing Yellow".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_HEAD_FLASHYELLOW)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Flashing Green".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_HEAD_FLASHGREEN)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Lit".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_HEAD_LIT)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Held".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_HEAD_HELD)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Lunar".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_HEAD_LUNAR)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Flashing Lunar".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_HEAD_FLASHLUNAR)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Memory Compare to Value (Case Insensitive)".equals(ConditionalVariable.getTestTypeString(TYPE_MEMORY_EQUALS_INSENSITIVE)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Memory Compare to Memory (Case Insensitive)".equals(ConditionalVariable.getTestTypeString(TYPE_MEMORY_COMPARE_INSENSITIVE)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Free".equals(ConditionalVariable.getTestTypeString(TYPE_ROUTE_FREE)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Occupied".equals(ConditionalVariable.getTestTypeString(TYPE_ROUTE_OCCUPIED)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Allocated".equals(ConditionalVariable.getTestTypeString(TYPE_ROUTE_ALLOCATED)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Set".equals(ConditionalVariable.getTestTypeString(TYPE_ROUTE_SET)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Running".equals(ConditionalVariable.getTestTypeString(TYPE_TRAIN_RUNNING)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Signal Mast Aspect equals".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_MAST_ASPECT_EQUALS)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Signal Head Appearance equals".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Lit".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_MAST_LIT)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Held".equals(ConditionalVariable.getTestTypeString(TYPE_SIGNAL_MAST_HELD)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Active".equals(ConditionalVariable.getTestTypeString(TYPE_ENTRYEXIT_ACTIVE)));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Inactive".equals(ConditionalVariable.getTestTypeString(TYPE_ENTRYEXIT_INACTIVE)));
        
        // Test invalid value
        Assert.assertTrue("getTestTypeString() returns correct value",
                "(None)".equals(ConditionalVariable.getTestTypeString(-1)));
    }
    
    @Test
    public void testGetCompareOperationString() {
        Assert.assertTrue("getCompareOperationString() returns correct value",
                "Less Than".equals(ConditionalVariable.getCompareOperationString(ConditionalVariable.LESS_THAN)));
        Assert.assertTrue("getCompareOperationString() returns correct value",
                "Less Than Or Equal".equals(ConditionalVariable.getCompareOperationString(ConditionalVariable.LESS_THAN_OR_EQUAL)));
        Assert.assertTrue("getCompareOperationString() returns correct value",
                "Equal".equals(ConditionalVariable.getCompareOperationString(0)));
        Assert.assertTrue("getCompareOperationString() returns correct value",
                "Equal".equals(ConditionalVariable.getCompareOperationString(ConditionalVariable.EQUAL)));
        Assert.assertTrue("getCompareOperationString() returns correct value",
                "Greater Than Or Equal".equals(ConditionalVariable.getCompareOperationString(ConditionalVariable.GREATER_THAN_OR_EQUAL)));
        Assert.assertTrue("getCompareOperationString() returns correct value",
                "Greater Than".equals(ConditionalVariable.getCompareOperationString(ConditionalVariable.GREATER_THAN)));
    }
    
    @Test
    public void testEvaluate() throws JmriException {
        String deviceName = "3";
        
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provideSensor(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, ITEM_TYPE_SENSOR, deviceName, false);
        sensor.setState(Sensor.ACTIVE);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        sensor.setState(Sensor.INACTIVE);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(deviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_TURNOUT_THROWN, deviceName, false);
        turnout.setState(Turnout.THROWN);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        turnout.setState(Turnout.CLOSED);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_TURNOUT_CLOSED, deviceName, false);
        turnout.setState(Turnout.THROWN);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        turnout.setState(Turnout.CLOSED);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        
        
        // This is not yet implemented. The code below is only a non working sketch.
//        Memory memory = InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
//        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_MEMORY_EQUALS, deviceName, false);
//        memory.setState(Sensor.ACTIVE);
//        Assert.assertTrue("evaluate() returns true", cv.evaluate());
//        memory.setState(Sensor.INACTIVE);
//        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        
        Light light = InstanceManager.getDefault(LightManager.class).provideLight(deviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_LIGHT_ON, deviceName, false);
        light.setState(Light.ON);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        light.setState(Light.OFF);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_LIGHT_OFF, deviceName, false);
        light.setState(Light.ON);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        light.setState(Light.OFF);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        
        
        // Note that the signal head IH1 created here are also used to test the signal mast.
        SignalHead signalHeadIH1 = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH1);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_SIGNAL_HEAD_RED, "IH1", false);
        
        cv.setType(TYPE_SIGNAL_HEAD_RED);
        signalHeadIH1.setAppearance(SignalHead.RED);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv.setType(TYPE_SIGNAL_HEAD_YELLOW);
        signalHeadIH1.setAppearance(SignalHead.YELLOW);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv.setType(TYPE_SIGNAL_HEAD_GREEN);
        signalHeadIH1.setAppearance(SignalHead.GREEN);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv.setType(TYPE_SIGNAL_HEAD_DARK);
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.GREEN);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv.setType(TYPE_SIGNAL_HEAD_FLASHRED);
        signalHeadIH1.setAppearance(SignalHead.FLASHRED);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv.setType(TYPE_SIGNAL_HEAD_FLASHYELLOW);
        signalHeadIH1.setAppearance(SignalHead.FLASHYELLOW);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv.setType(TYPE_SIGNAL_HEAD_FLASHGREEN);
        signalHeadIH1.setAppearance(SignalHead.FLASHGREEN);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv.setType(TYPE_SIGNAL_HEAD_LUNAR);
        signalHeadIH1.setAppearance(SignalHead.LUNAR);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv.setType(TYPE_SIGNAL_HEAD_FLASHLUNAR);
        signalHeadIH1.setAppearance(SignalHead.FLASHLUNAR);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv.setType(TYPE_SIGNAL_HEAD_LIT);
        signalHeadIH1.setLit(true);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setLit(false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv.setType(TYPE_SIGNAL_HEAD_HELD);
        signalHeadIH1.setHeld(true);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setHeld(false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        
        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_SIGNAL_MAST_ASPECT_EQUALS, "IF$shsm:AAR-1946:CPL(IH1)", false);
        
        cv.setDataString("Clear");
        cv.setType(TYPE_SIGNAL_MAST_ASPECT_EQUALS);
        // The null check is only to ensure that the evaluate() tests aspect == null
        Assert.assertTrue("aspect is null", signalMast.getAspect() == null);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        signalMast.setAspect("Clear");
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalMast.setAspect("Approach");
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv.setType(TYPE_SIGNAL_MAST_LIT);
        signalMast.setLit(true);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalMast.setLit(false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv.setType(TYPE_SIGNAL_MAST_HELD);
        signalMast.setHeld(true);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalMast.setHeld(false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        
        InstanceManager.getDefault(LogixManager.class).createNewLogix("IX:AUTO:0002");
        Conditional conditional = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX:AUTO:0001C1", "Conditional");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_CONDITIONAL_TRUE, "IX:AUTO:0001C1", false);
        conditional.setState(Conditional.TRUE);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        conditional.setState(Conditional.FALSE);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        
        // This is not yet implemented. The code below is only a non working sketch.
//        Warrant warrant = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW3");
//        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_ROUTE_OCCUPIED, "IW3", false);
//        cv.setType(TYPE_ROUTE_FREE);
//        warrant.setState(Sensor.ACTIVE);
//        Assert.assertTrue("evaluate() returns true", cv.evaluate());
//        warrant.setState(Sensor.INACTIVE);
//        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        
        // This is not yet implemented. The code below is only a non working sketch.
//        OBlock oblock = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB3");
//        cv = new ConditionalVariable(false, Conditional.Operator.AND, TYPE_BLOCK_STATUS_EQUALS, "OB3", false);
//        cv.setDataString("TRUE");
//        oblock.setState(Sensor.ACTIVE);
//        Assert.assertTrue("evaluate() returns true", cv.evaluate());
//        oblock.setState(Sensor.INACTIVE);
//        Assert.assertFalse("evaluate() returns false", cv.evaluate());
    }
    
    @Test
    public void testCompare() {
        String deviceName = "3";
        InstanceManager.getDefault(SensorManager.class).provideSensor(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, ITEM_TYPE_SENSOR, deviceName, false);
        
        Assert.assertTrue("evaluate() returns true", cv.compare(null, null, false));
        Assert.assertFalse("evaluate() returns false", cv.compare("10", null, false));
        Assert.assertFalse("evaluate() returns false", cv.compare(null, "20", false));
        
        cv.setNum1(LESS_THAN);
        Assert.assertTrue("evaluate() returns true", cv.compare("10", "20", false));
        Assert.assertFalse("evaluate() returns false", cv.compare("15", "15", false));
        Assert.assertFalse("evaluate() returns false", cv.compare("20", "10", false));
        
        cv.setNum1(LESS_THAN_OR_EQUAL);
        Assert.assertTrue("evaluate() returns true", cv.compare("10", "20", false));
        Assert.assertTrue("evaluate() returns true", cv.compare("15", "15", false));
        Assert.assertFalse("evaluate() returns false", cv.compare("20", "10", false));
        
        cv.setNum1(EQUAL);
        Assert.assertFalse("evaluate() returns false", cv.compare("10", "20", false));
        Assert.assertTrue("evaluate() returns true", cv.compare("15", "15", false));
        Assert.assertFalse("evaluate() returns false", cv.compare("20", "10", false));
        
        cv.setNum1(GREATER_THAN_OR_EQUAL);
        Assert.assertFalse("evaluate() returns false", cv.compare("10", "20", false));
        Assert.assertTrue("evaluate() returns true", cv.compare("15", "15", false));
        Assert.assertTrue("evaluate() returns true", cv.compare("20", "10", false));
        
        cv.setNum1(GREATER_THAN);
        Assert.assertFalse("evaluate() returns false", cv.compare("10", "20", false));
        Assert.assertFalse("evaluate() returns false", cv.compare("15", "15", false));
        Assert.assertTrue("evaluate() returns true", cv.compare("20", "10", false));
        
        
        cv.setNum1(LESS_THAN);
        Assert.assertTrue("evaluate() returns true", cv.compare("aaa", "ccc", false));
        Assert.assertFalse("evaluate() returns false", cv.compare("bbb", "bbb", false));
        Assert.assertFalse("evaluate() returns false", cv.compare("ccc", "aaa", false));
        
        cv.setNum1(LESS_THAN_OR_EQUAL);
        Assert.assertTrue("evaluate() returns true", cv.compare("aaa", "ccc", false));
        Assert.assertTrue("evaluate() returns true", cv.compare("bbb", "bbb", false));
        Assert.assertFalse("evaluate() returns false", cv.compare("ccc", "aaa", false));
        
        cv.setNum1(EQUAL);
        Assert.assertFalse("evaluate() returns false", cv.compare("aaa", "ccc", false));
        Assert.assertTrue("evaluate() returns true", cv.compare("bbb", "bbb", false));
        Assert.assertFalse("evaluate() returns false", cv.compare("ccc", "aaa", false));
        
        cv.setNum1(GREATER_THAN_OR_EQUAL);
        Assert.assertFalse("evaluate() returns false", cv.compare("aaa", "ccc", false));
        Assert.assertTrue("evaluate() returns true", cv.compare("bbb", "bbb", false));
        Assert.assertTrue("evaluate() returns true", cv.compare("ccc", "aaa", false));
        
        cv.setNum1(GREATER_THAN);
        Assert.assertFalse("evaluate() returns false", cv.compare("aaa", "ccc", false));
        Assert.assertFalse("evaluate() returns false", cv.compare("bbb", "bbb", false));
        Assert.assertTrue("evaluate() returns true", cv.compare("ccc", "aaa", false));
        
        // Test case
        cv.setNum1(GREATER_THAN);
        Assert.assertTrue("evaluate() returns true", cv.compare("aaa", "Ccc", false));
        Assert.assertTrue("evaluate() returns true", cv.compare("bbb", "Bbb", false));
        Assert.assertFalse("evaluate() returns false", cv.compare("Ccc", "aaa", false));
        
        // Test case
        cv.setNum1(GREATER_THAN);
        Assert.assertFalse("evaluate() returns false", cv.compare("aaa", "Ccc", true));
        Assert.assertFalse("evaluate() returns false", cv.compare("bbb", "Bbb", true));
        Assert.assertTrue("evaluate() returns true", cv.compare("Ccc", "aaa", true));
    }
    
    @Test
    public void testGetItemTypeString() {
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Sensor".equals(ConditionalVariable.getItemTypeString(ITEM_TYPE_SENSOR)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Turnout".equals(ConditionalVariable.getItemTypeString(ITEM_TYPE_TURNOUT)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Light".equals(ConditionalVariable.getItemTypeString(ITEM_TYPE_LIGHT)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Signal Head".equals(ConditionalVariable.getItemTypeString(ITEM_TYPE_SIGNALHEAD)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Signal Mast".equals(ConditionalVariable.getItemTypeString(ITEM_TYPE_SIGNALMAST)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Memory".equals(ConditionalVariable.getItemTypeString(ITEM_TYPE_MEMORY)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Conditional".equals(ConditionalVariable.getItemTypeString(ITEM_TYPE_CONDITIONAL)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Warrant".equals(ConditionalVariable.getItemTypeString(ITEM_TYPE_WARRANT)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Fast Clock".equals(ConditionalVariable.getItemTypeString(ITEM_TYPE_CLOCK)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Occupancy Block".equals(ConditionalVariable.getItemTypeString(ITEM_TYPE_OBLOCK)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Entry Exit".equals(ConditionalVariable.getItemTypeString(ITEM_TYPE_ENTRYEXIT)));
        
        // Test wrong value
        Assert.assertTrue("getItemTypeString() returns correct value",
                "".equals(ConditionalVariable.getItemTypeString(-1)));
    }
    
    @Test
    public void testDescribeState() {
        Assert.assertTrue("getItemTypeString() returns correct value",
                "".equals(ConditionalVariable.describeState(TYPE_NONE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Active".equals(ConditionalVariable.describeState(TYPE_SENSOR_ACTIVE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Inactive".equals(ConditionalVariable.describeState(TYPE_SENSOR_INACTIVE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Thrown".equals(ConditionalVariable.describeState(TYPE_TURNOUT_THROWN)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Closed".equals(ConditionalVariable.describeState(TYPE_TURNOUT_CLOSED)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "True".equals(ConditionalVariable.describeState(TYPE_CONDITIONAL_TRUE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "False".equals(ConditionalVariable.describeState(TYPE_CONDITIONAL_FALSE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "On".equals(ConditionalVariable.describeState(TYPE_LIGHT_ON)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Off".equals(ConditionalVariable.describeState(TYPE_LIGHT_OFF)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "(case sensitive) Value".equals(ConditionalVariable.describeState(TYPE_MEMORY_EQUALS)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "(case sensitive) Memory".equals(ConditionalVariable.describeState(TYPE_MEMORY_COMPARE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "".equals(ConditionalVariable.describeState(TYPE_FAST_CLOCK_RANGE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Red".equals(ConditionalVariable.describeState(TYPE_SIGNAL_HEAD_RED)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Yellow".equals(ConditionalVariable.describeState(TYPE_SIGNAL_HEAD_YELLOW)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Green".equals(ConditionalVariable.describeState(TYPE_SIGNAL_HEAD_GREEN)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Dark".equals(ConditionalVariable.describeState(TYPE_SIGNAL_HEAD_DARK)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Flashing Red".equals(ConditionalVariable.describeState(TYPE_SIGNAL_HEAD_FLASHRED)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Flashing Yellow".equals(ConditionalVariable.describeState(TYPE_SIGNAL_HEAD_FLASHYELLOW)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Flashing Green".equals(ConditionalVariable.describeState(TYPE_SIGNAL_HEAD_FLASHGREEN)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Held".equals(ConditionalVariable.describeState(TYPE_SIGNAL_HEAD_HELD)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Lunar".equals(ConditionalVariable.describeState(TYPE_SIGNAL_HEAD_LUNAR)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Flashing Lunar".equals(ConditionalVariable.describeState(TYPE_SIGNAL_HEAD_FLASHLUNAR)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Lit".equals(ConditionalVariable.describeState(TYPE_SIGNAL_HEAD_LIT)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "(case insensitive) Value".equals(ConditionalVariable.describeState(TYPE_MEMORY_EQUALS_INSENSITIVE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "(case insensitive) Memory".equals(ConditionalVariable.describeState(TYPE_MEMORY_COMPARE_INSENSITIVE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Route Free".equals(ConditionalVariable.describeState(TYPE_ROUTE_FREE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Route Occupied".equals(ConditionalVariable.describeState(TYPE_ROUTE_OCCUPIED)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Route Allocated".equals(ConditionalVariable.describeState(TYPE_ROUTE_ALLOCATED)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Route Set".equals(ConditionalVariable.describeState(TYPE_ROUTE_SET)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Train Running".equals(ConditionalVariable.describeState(TYPE_TRAIN_RUNNING)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Signal Mast Aspect equals".equals(ConditionalVariable.describeState(TYPE_SIGNAL_MAST_ASPECT_EQUALS)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Signal Head Appearance equals".equals(ConditionalVariable.describeState(TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Lit".equals(ConditionalVariable.describeState(TYPE_SIGNAL_MAST_LIT)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Held".equals(ConditionalVariable.describeState(TYPE_SIGNAL_MAST_HELD)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Active".equals(ConditionalVariable.describeState(TYPE_ENTRYEXIT_ACTIVE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Inactive".equals(ConditionalVariable.describeState(TYPE_ENTRYEXIT_INACTIVE)));
        
        // Test invalid value
        Assert.assertTrue("getItemTypeString() returns correct value",
                "<none>".equals(ConditionalVariable.describeState(-1)));
        jmri.util.JUnitAppender.assertWarnMessage("Unhandled condition type: -1");
    }
    
    @Test
    public void testGetCompareSymbols() {
        Assert.assertTrue("getItemTypeString() returns correct value",
                "<".equals(ConditionalVariable.getCompareSymbols(LESS_THAN)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "<=".equals(ConditionalVariable.getCompareSymbols(LESS_THAN_OR_EQUAL)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "=".equals(ConditionalVariable.getCompareSymbols(EQUAL)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                ">=".equals(ConditionalVariable.getCompareSymbols(GREATER_THAN_OR_EQUAL)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                ">".equals(ConditionalVariable.getCompareSymbols(GREATER_THAN)));
    }
    
    @Test
    public void testStringToVariableTest() {
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Red") == TYPE_SIGNAL_HEAD_RED);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Yellow") == TYPE_SIGNAL_HEAD_YELLOW);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Green") == TYPE_SIGNAL_HEAD_GREEN);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Dark") == TYPE_SIGNAL_HEAD_DARK);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Flashing Red") == TYPE_SIGNAL_HEAD_FLASHRED);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Flashing Yellow") == TYPE_SIGNAL_HEAD_FLASHYELLOW);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Flashing Green") == TYPE_SIGNAL_HEAD_FLASHGREEN);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Lunar") == TYPE_SIGNAL_HEAD_LUNAR);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Flashing Lunar") == TYPE_SIGNAL_HEAD_FLASHLUNAR);
        
        // Check bad string. This gives a warning message.
        Assert.assertTrue("getItemTypeString() returns -1 for wrong string",
                ConditionalVariable.stringToVariableTest("Bad signal head") == -1);
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected parameter to stringToVariableTest(Bad signal head)");
        
        // Check empty string. This doesn't give any warning.
        Assert.assertTrue("getItemTypeString() returns -1 for wrong string",
                ConditionalVariable.stringToVariableTest("") == -1);
    }
    
    
    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initLogixManager();
        jmri.util.JUnitUtil.initIdTagManager();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
