package jmri;

import static jmri.Conditional.*;

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
    
    @Test
    public void testGetOpernString() {
        String deviceName = "3";
        InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Memory Compare to Value (Case Sensitive)".equals(cv.getTestTypeString()));
        
        cv.setNegation(false);
        cv.setOpern(OPERATOR_AND);
        Assert.assertTrue("getOpernString() returns correct value",
                "AND".equals(cv.getOpernString()));
        Assert.assertFalse("isNegated() returns false", cv.isNegated());
        
        cv.setNegation(false);
        cv.setOpern(OPERATOR_NOT);
        Assert.assertTrue("getOpernString() returns correct value",
                "".equals(cv.getOpernString()));
        Assert.assertTrue("isNegated() returns true", cv.isNegated());
        
        cv.setNegation(false);
        cv.setOpern(OPERATOR_AND_NOT);
        Assert.assertTrue("getOpernString() returns correct value",
                "AND".equals(cv.getOpernString()));
        Assert.assertTrue("isNegated() returns true", cv.isNegated());
        
        cv.setNegation(false);
        cv.setOpern(OPERATOR_NONE);
        Assert.assertTrue("getOpernString() returns correct value",
                "".equals(cv.getOpernString()));
        Assert.assertFalse("isNegated() returns false", cv.isNegated());
        
        cv.setNegation(false);
        cv.setOpern(OPERATOR_OR);
        Assert.assertTrue("getOpernString() returns correct value",
                "OR".equals(cv.getOpernString()));
        Assert.assertFalse("isNegated() returns false", cv.isNegated());
        
        cv.setNegation(false);
        cv.setOpern(OPERATOR_OR_NOT);
        Assert.assertTrue("getOpernString() returns correct value",
                "".equals(cv.getOpernString()));
        Assert.assertFalse("isNegated() returns false", cv.isNegated());
    }
    
    @Test
    public void testGetTestTypeString() {
        String deviceName = "3";
        InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_MEMORY_EQUALS, deviceName, false);
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
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, ITEM_TYPE_SENSOR, deviceName, false);
        sensor.setState(Sensor.ACTIVE);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        sensor.setState(Sensor.INACTIVE);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(deviceName);
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_TURNOUT_THROWN, deviceName, false);
        turnout.setState(Turnout.THROWN);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        turnout.setState(Turnout.CLOSED);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_TURNOUT_CLOSED, deviceName, false);
        turnout.setState(Turnout.THROWN);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        turnout.setState(Turnout.CLOSED);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        
        
        // This is not yet implemented. The code below is only a non working sketch.
//        Memory memory = InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
//        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_MEMORY_EQUALS, deviceName, false);
//        memory.setState(Sensor.ACTIVE);
//        Assert.assertTrue("evaluate() returns true", cv.evaluate());
//        memory.setState(Sensor.INACTIVE);
//        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        
        Light light = InstanceManager.getDefault(LightManager.class).provideLight(deviceName);
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_LIGHT_ON, deviceName, false);
        light.setState(Light.ON);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        light.setState(Light.OFF);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_LIGHT_OFF, deviceName, false);
        light.setState(Light.ON);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        light.setState(Light.OFF);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        
        
        // Note that the signal head IH1 created here are also used to test the signal mast.
        SignalHead signalHeadIH1 = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH1);
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_SIGNAL_HEAD_RED, "IH1", false);
        
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
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_SIGNAL_MAST_ASPECT_EQUALS, "IF$shsm:AAR-1946:CPL(IH1)", false);
        
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
        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_CONDITIONAL_TRUE, "IX:AUTO:0001C1", false);
        conditional.setState(Conditional.TRUE);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        conditional.setState(Conditional.FALSE);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        
        // This is not yet implemented. The code below is only a non working sketch.
//        Warrant warrant = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW3");
//        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_ROUTE_OCCUPIED, "IW3", false);
//        cv.setType(TYPE_ROUTE_FREE);
//        warrant.setState(Sensor.ACTIVE);
//        Assert.assertTrue("evaluate() returns true", cv.evaluate());
//        warrant.setState(Sensor.INACTIVE);
//        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        
        
        // This is not yet implemented. The code below is only a non working sketch.
//        OBlock oblock = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB3");
//        cv = new ConditionalVariable(false, Conditional.OPERATOR_AND, TYPE_BLOCK_STATUS_EQUALS, "OB3", false);
//        cv.setDataString("TRUE");
//        oblock.setState(Sensor.ACTIVE);
//        Assert.assertTrue("evaluate() returns true", cv.evaluate());
//        oblock.setState(Sensor.INACTIVE);
//        Assert.assertFalse("evaluate() returns false", cv.evaluate());
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
