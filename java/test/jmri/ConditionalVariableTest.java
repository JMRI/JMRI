package jmri;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;

import org.junit.jupiter.api.*;

import static jmri.Conditional.*;
import static jmri.ConditionalVariable.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the ConditionalVariable class
 *
 * @author Bob Jacobsen Copyright (C) 2016
 */
public class ConditionalVariableTest {

    @Test
    public void testCtor() {

        NamedBean bean;
        NamedBean otherBean;
        String deviceName = "3";
        String otherDeviceName = "4";

        // Start with testing the exception handling in the constructor
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManagerThrowException();
        JUnitUtil.initLightManagerThrowException();
        JUnitUtil.initMemoryManagerThrowException();
        JUnitUtil.initInternalSensorManagerThrowException();
        JUnitUtil.initSignalHeadManagerThrowException();
        JUnitUtil.initSignalMastManagerThrowException();
        JUnitUtil.initWarrantManagerThrowException();
        JUnitUtil.initOBlockManagerThrowException();

        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SENSOR_ACTIVE, deviceName, false);
        assertNull( cv.getNamedBean(), "getNamedBean() returns null");
        JUnitAppender.assertErrorMessage("invalid sensor name= \"3\" in state variable");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_THROWN, deviceName, false);
        assertNull( cv.getNamedBean(), "getNamedBean() returns null");
        JUnitAppender.assertErrorMessage("invalid turnout name= \"3\" in state variable");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        assertNull( cv.getNamedBean(), "getNamedBean() returns null");
        JUnitAppender.assertErrorMessage("invalid memory name= \"3\" in state variable");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.LIGHT_ON, deviceName, false);
        assertNull( cv.getNamedBean(), "getNamedBean() returns null");
        JUnitAppender.assertErrorMessage("invalid light name= \"3\" in state variable");

        // Note that the signal head IH1 created here are also used to test the signal mast.
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_HEAD_RED, "IH1", false);
        assertNull( cv.getNamedBean(), "getNamedBean() returns null");
        JUnitAppender.assertWarnMessage("could not provide \"IH1\" in constructor");

        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "IF$shsm:AAR-1946:CPL(IH1)", false);
        assertNull( cv.getNamedBean(), "getNamedBean() returns null");
        JUnitAppender.assertErrorMessage("invalid signalmast name= \"IF$shsm:AAR-1946:CPL(IH1)\" in state variable");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.CONDITIONAL_TRUE, "IX:AUTO:0001C1", false);
        assertNull( cv.getNamedBean(), "getNamedBean() returns null");
        JUnitAppender.assertErrorMessage("invalid conditional; name= \"IX:AUTO:0001C1\" in state variable");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.ROUTE_OCCUPIED, "IW3", false);
        assertNull( cv.getNamedBean(), "getNamedBean() returns null");
        JUnitAppender.assertWarnMessage("could not provide \"IW3\" in constructor");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.BLOCK_STATUS_EQUALS, "OB3", false);
        assertNull( cv.getNamedBean(), "getNamedBean() returns null");
        JUnitAppender.assertWarnMessage("could not provide \"OB3\" in constructor");


        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initIdTagManager();

        bean = InstanceManager.getDefault(SensorManager.class).provideSensor(deviceName);
        otherBean = InstanceManager.getDefault(SensorManager.class).provideSensor(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SENSOR_ACTIVE, deviceName, false);
        assertTrue( bean.equals(cv.getNamedBean().getBean()), "getNamedBean() returns correct bean");
        cv.setName(otherDeviceName);
        assertTrue( otherBean.equals(cv.getNamedBean().getBean()), "setName() sets correct bean");
        assertTrue( "Sensor \"4\" state is \"Sensor Active\"".equals(cv.toString()),
            "toString() returns correct value");

        bean = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(deviceName);
        otherBean = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_THROWN, deviceName, false);
        assertTrue( bean.equals(cv.getNamedBean().getBean()), "getNamedBean() returns correct bean");
        cv.setName(otherDeviceName);
        assertTrue( otherBean.equals(cv.getNamedBean().getBean()), "setName() sets correct bean");
        assertTrue( "Turnout \"4\" state is \"Turnout Thrown\"".equals(cv.toString()),
            "toString() returns correct value");

        bean = InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        otherBean = InstanceManager.getDefault(MemoryManager.class).provideMemory(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        assertTrue( bean.equals(cv.getNamedBean().getBean()), "getNamedBean() returns correct bean");
        cv.setName(otherDeviceName);
        assertTrue( otherBean.equals(cv.getNamedBean().getBean()), "setName() sets correct bean");
        cv.setDataString("A desired memory value");
        assertTrue( "Memory \"4\" = value \"A desired memory value\"".equals(cv.toString()),
            "toString() returns correct value");
        cv.setType(Conditional.Type.MEMORY_COMPARE);
        cv.setDataString("MemoryVariable");
        assertTrue( "Memory \"4\" = Memory \"MemoryVariable\"".equals(cv.toString()),
            "toString() returns correct value");

        bean = InstanceManager.getDefault(LightManager.class).provideLight(deviceName);
        otherBean = InstanceManager.getDefault(LightManager.class).provideLight(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.LIGHT_ON, deviceName, false);
        assertTrue( bean.equals(cv.getNamedBean().getBean()), "getNamedBean() returns correct bean");
        cv.setName(otherBean.getSystemName());
        assertTrue( otherBean.equals(cv.getNamedBean().getBean()), "setName() sets correct bean");
        assertTrue( "Light \"IL4\" state is \"Light On\"".equals(cv.toString()),
            "toString() returns correct value");

        // Note that the signal head IH1 created here are also used to test the signal mast.
        SignalHead signalHeadIH1 = new VirtualSignalHead("IH1");
        SignalHead signalHeadIH2 = new VirtualSignalHead("IH2");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH1);
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH2);
        bean = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH1");
        otherBean = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH2");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_HEAD_RED, "IH1", false);
        assertEquals( bean, cv.getNamedBean().getBean(), "getNamedBean() returns correct bean");
        cv.setName("IH2");
        assertEquals( otherBean, cv.getNamedBean().getBean(),"setName() sets correct bean");
        assertTrue( "Signal Head \"IH2\" Appearance is \"Red\"".equals(cv.toString()),
            "toString() returns correct value");
        cv.setType(Conditional.Type.SIGNAL_HEAD_LIT);
        assertTrue( "Signal Head \"IH2\" state is \"Lit\"".equals(cv.toString()),
            "toString() returns correct value");

        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        bean = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        otherBean = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH2)");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "IF$shsm:AAR-1946:CPL(IH1)", false);
        assertTrue( bean.equals(cv.getNamedBean().getBean()), "getNamedBean() returns correct bean");
        cv.setName("IF$shsm:AAR-1946:CPL(IH2)");
        assertTrue( otherBean.equals(cv.getNamedBean().getBean()), "setName() sets correct bean");
        cv.setDataString("Approach");
        assertTrue( "Signal Mast \"IF$shsm:AAR-1946:CPL(IH2)\" Aspect is \"Approach\"".equals(cv.toString()),
            "toString() returns correct value");
        cv.setType(Conditional.Type.SIGNAL_MAST_LIT);
        assertTrue( "Signal Mast \"IF$shsm:AAR-1946:CPL(IH2)\" state is \"Lit\"".equals(cv.toString()),
            "toString() returns correct value");

        InstanceManager.getDefault(LogixManager.class).createNewLogix("IX:AUTO:0002");
        bean = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX:AUTO:0001C1", "Conditional");
        otherBean = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX:AUTO:0001C2", "Conditional");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.CONDITIONAL_TRUE, "IX:AUTO:0001C1", false);
        assertTrue( bean.equals(cv.getNamedBean().getBean()), "getNamedBean() returns correct bean");
        cv.setName("IX:AUTO:0001C2");
        assertTrue( otherBean.equals(cv.getNamedBean().getBean()), "setName() sets correct bean");
        cv.setGuiName("A Gui name");
        assertTrue( "Conditional \"A Gui name\" state is \"Conditional True\"".equals(cv.toString()),
            "toString() returns correct value");

        bean = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW3");
        otherBean = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW4");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.ROUTE_OCCUPIED, "IW3", false);
        assertTrue( bean.equals(cv.getNamedBean().getBean()), "getNamedBean() returns correct bean");
        cv.setName("IW4");
        assertTrue( otherBean.equals(cv.getNamedBean().getBean()), "setName() sets correct bean");
        assertTrue( "WarrantRoute \"IW4\" state is \"Occupied\"".equals(cv.toString()),
            "toString() returns correct value");

        bean = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB3");
        otherBean = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB4");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.BLOCK_STATUS_EQUALS, "OB3", false);
        assertTrue( bean.equals(cv.getNamedBean().getBean()), "getNamedBean() returns correct bean");
        cv.setName("OB4");
        assertTrue( otherBean.equals(cv.getNamedBean().getBean()), "setName() sets correct bean");
        cv.setDataString("block error");
        assertTrue( "OBlock Status \"OB4\" state is \"block error\"".equals(cv.toString()),
            "toString() returns correct value");

        // Test a bad device name
        cv.setName("A bad device name");
        // setName should not change the bean if called with wrong name. For example, it should not set the bean to null.
        assertTrue( otherBean.equals(cv.getNamedBean().getBean()), "getName() still has correct bean");
    }

    @Test
    public void testEnumOperator() {
        // OPERATOR_NONE = 4;
        assertEquals( 4, Operator.NONE.getIntValue(), "Operator.getIntValue() returns correct value");
        // OPERATOR_AND = 1;
        assertEquals( 1, Operator.AND.getIntValue(), "Operator.getIntValue() returns correct value");
        // OPERATOR_OR = 5;
        assertEquals( 5, Operator.OR.getIntValue(), "Operator.getIntValue() returns correct value");

        // OPERATOR_AND = 1;
        assertTrue( Operator.getOperatorFromIntValue(1) == Operator.AND, "Operator.getOperatorFromIntValue() returns correct value");
        // OPERATOR_NONE = 4;
        assertTrue( Operator.getOperatorFromIntValue(4) == Operator.NONE, "Operator.getOperatorFromIntValue() returns correct value");
        // OPERATOR_OR = 5;
        assertTrue( Operator.getOperatorFromIntValue(5) == Operator.OR, "Operator.getOperatorFromIntValue() returns correct value");

        // Test illegal operator
        Exception ex = assertThrows( IllegalArgumentException.class,
            () -> Operator.getOperatorFromIntValue(-1),
            "Operator.getOperatorFromIntValue(-1) throws IllegalArgumentException");
        assertNotNull(ex);
    }

    @Test
    public void testConstants() {
        // It might be a good idea to change constants into enums.
        // These tests ensures that the values of the constants stay the same
        // if that change is done.

        assertEquals( 5, ConditionalVariable.NUM_COMPARE_OPERATIONS);
        assertEquals( 1, ConditionalVariable.LESS_THAN);
        assertEquals( 2, ConditionalVariable.LESS_THAN_OR_EQUAL);
        assertEquals( 3, ConditionalVariable.EQUAL);
        assertEquals( 4, ConditionalVariable.GREATER_THAN_OR_EQUAL);
        assertEquals( 5, ConditionalVariable.GREATER_THAN);
    }

    @Test
    public void testEquals() {
        ConditionalVariable c1 = new ConditionalVariable(false, Operator.AND, Conditional.Type.SENSOR_INACTIVE, "name", false);
        ConditionalVariable c2 = new ConditionalVariable(false, Operator.AND, Conditional.Type.SENSOR_INACTIVE, "name", false);

        assertTrue( c1.equals(c1), "identity");
        assertFalse( c1.equals(c2), "object equals, not content equals");
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
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        assertEquals( "", cv.getDataString(), "getDataString() returns empty string");
        assertNull( cv.getNamedBeanData(), "getNamedBeanData() returns null");
        cv.setDataString(otherBean.getUserName());
        cv.setName(otherDeviceName);
        assertEquals( otherBean.getUserName(), cv.getDataString(), "getDataString() returns correct string");
        assertTrue( otherBean.equals(cv.getNamedBeanData()), "getNamedBeanData() returns correct bean");
    }

    @Test
    public void testState() {
        String deviceName = "3";
        InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        assertTrue( cv.getState() == Conditional.State.UNKNOWN.getIntValue(), "state is unknown");
        cv.setState(Conditional.State.TRUE.getIntValue());
        assertTrue( cv.getState() == Conditional.State.TRUE.getIntValue(), "state is TRUE");
        cv.setState(Conditional.State.FALSE.getIntValue());
        assertTrue( cv.getState() == Conditional.State.FALSE.getIntValue(), "state is FALSE");
        cv.setState(true);
        assertTrue( cv.getState() == Conditional.State.TRUE.getIntValue(), "state is TRUE");
        cv.setState(false);
        assertTrue( cv.getState() == Conditional.State.FALSE.getIntValue(), "state is FALSE");
    }

    @Test
    public void testGetOpernString() {
        String deviceName = "3";
        InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        assertEquals( "Memory Compare to Value (Case Sensitive)",cv.getTestTypeString(),
            "getTestTypeString() returns correct value");

        cv.setNegation(false);
        cv.setOpern(Operator.AND);
        assertEquals( "AND", cv.getOpernString(),
            "getOpernString() returns correct value");
        assertFalse( cv.isNegated(), "isNegated() returns false");

        cv.setNegation(true);
        cv.setOpern(Operator.NONE);
        assertTrue( "".equals(cv.getOpernString()),
            "getOpernString() returns correct value");
        assertTrue( cv.isNegated(), "isNegated() returns true");

        cv.setNegation(true);
        cv.setOpern(Operator.AND);
        assertEquals( "AND", cv.getOpernString(),
            "getOpernString() returns correct value");
        assertTrue( cv.isNegated(), "isNegated() returns true");

        cv.setNegation(false);
        cv.setOpern(Operator.NONE);
        assertTrue( "".equals(cv.getOpernString()),
            "getOpernString() returns correct value");
        assertFalse( cv.isNegated(), "isNegated() returns false");

        cv.setNegation(false);
        cv.setOpern(Operator.OR);
        assertEquals( "OR", cv.getOpernString(),
            "getOpernString() returns correct value");
        assertFalse( cv.isNegated(), "isNegated() returns false");

        cv.setNegation(true);
        cv.setOpern(Operator.OR);
        assertEquals( "OR", cv.getOpernString(),
            "getOpernString() returns correct value");
        assertTrue( cv.isNegated(), "isNegated() returns true");
    }

    @Test
    public void testGetTestTypeString() {
        String deviceName = "3";
        InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        assertEquals( "Memory Compare to Value (Case Sensitive)", cv.getTestTypeString(),
            "getTestTypeString() returns correct value");

        assertEquals( "Sensor Active", Conditional.Type.SENSOR_ACTIVE.getTestTypeString(),
            "Sensor Active");
        assertEquals( "Sensor Inactive", Conditional.Type.SENSOR_INACTIVE.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Turnout Thrown", Conditional.Type.TURNOUT_THROWN.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Turnout Closed", Conditional.Type.TURNOUT_CLOSED.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Conditional True", Conditional.Type.CONDITIONAL_TRUE.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Conditional False", Conditional.Type.CONDITIONAL_FALSE.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Light On", Conditional.Type.LIGHT_ON.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Light Off", Conditional.Type.LIGHT_OFF.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Memory Compare to Value (Case Sensitive)", Conditional.Type.MEMORY_EQUALS.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Memory Compare to Memory (Case Sensitive)", Conditional.Type.MEMORY_COMPARE.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Fast Clock Range", Conditional.Type.FAST_CLOCK_RANGE.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Red", Conditional.Type.SIGNAL_HEAD_RED.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Yellow", Conditional.Type.SIGNAL_HEAD_YELLOW.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Green", Conditional.Type.SIGNAL_HEAD_GREEN.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Dark", Conditional.Type.SIGNAL_HEAD_DARK.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Flashing Red", Conditional.Type.SIGNAL_HEAD_FLASHRED.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Flashing Yellow", Conditional.Type.SIGNAL_HEAD_FLASHYELLOW.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Flashing Green", Conditional.Type.SIGNAL_HEAD_FLASHGREEN.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Lit", Conditional.Type.SIGNAL_HEAD_LIT.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Held", Conditional.Type.SIGNAL_HEAD_HELD.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Lunar", Conditional.Type.SIGNAL_HEAD_LUNAR.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Flashing Lunar", Conditional.Type.SIGNAL_HEAD_FLASHLUNAR.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Memory Compare to Value (Case Insensitive)",
            Conditional.Type.MEMORY_EQUALS_INSENSITIVE.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Memory Compare to Memory (Case Insensitive)",
            Conditional.Type.MEMORY_COMPARE_INSENSITIVE.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Free", Conditional.Type.ROUTE_FREE.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Occupied", Conditional.Type.ROUTE_OCCUPIED.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Allocated", Conditional.Type.ROUTE_ALLOCATED.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Set", Conditional.Type.ROUTE_SET.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Running", Conditional.Type.TRAIN_RUNNING.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Signal Mast Aspect equals",
            Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Signal Head Appearance equals",
            Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Lit", Conditional.Type.SIGNAL_MAST_LIT.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Held", Conditional.Type.SIGNAL_MAST_HELD.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Active", Conditional.Type.ENTRYEXIT_ACTIVE.getTestTypeString(),
            "getTestTypeString() returns correct value");
        assertEquals( "Inactive", Conditional.Type.ENTRYEXIT_INACTIVE.getTestTypeString(),
            "getTestTypeString() returns correct value");

        // Test invalid value
//        Assert.assertTrue("getTestTypeString() returns correct value",
//                "(None)".equals(ConditionalVariable.getTestTypeString(-1)));
    }

    @Test
    public void testGetCompareOperationString() {
        assertEquals( "Less Than",
            ConditionalVariable.getCompareOperationString(ConditionalVariable.LESS_THAN),
            "getCompareOperationString() returns correct value");
        assertEquals( "Less Than Or Equal",
            ConditionalVariable.getCompareOperationString(ConditionalVariable.LESS_THAN_OR_EQUAL),
            "getCompareOperationString() returns correct value");
        assertEquals( "Equal",
            ConditionalVariable.getCompareOperationString(0),
            "getCompareOperationString() returns correct value");
        assertEquals( "Equal",
            ConditionalVariable.getCompareOperationString(ConditionalVariable.EQUAL),
            "getCompareOperationString() returns correct value");
        assertEquals( "Greater Than Or Equal",
            ConditionalVariable.getCompareOperationString(ConditionalVariable.GREATER_THAN_OR_EQUAL),
            "getCompareOperationString() returns correct value");
        assertEquals( "Greater Than",
            ConditionalVariable.getCompareOperationString(ConditionalVariable.GREATER_THAN),
            "getCompareOperationString() returns correct value");
    }

    @Test
    public void testEvaluate() throws JmriException {
        String deviceName = "3";
        String otherDeviceName = "5";

        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provideSensor(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SENSOR_ACTIVE, deviceName, false);
        sensor.setState(Sensor.ACTIVE);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        sensor.setState(Sensor.INACTIVE);
        assertFalse( cv.evaluate(), "evaluate() returns false");
        cv = new ConditionalVariable_BeanAlwaysNull(false, Conditional.Operator.AND, Conditional.Type.SENSOR_ACTIVE, deviceName, false);
        assertFalse( cv.evaluate(), "evaluate() returns false");
        JUnitAppender.assertErrorMessage("invalid sensor name= \"3\" in state variable");


        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(deviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_THROWN, deviceName, false);
        turnout.setState(Turnout.THROWN);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        turnout.setState(Turnout.CLOSED);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_CLOSED, deviceName, false);
        turnout.setState(Turnout.THROWN);
        assertFalse( cv.evaluate(), "evaluate() returns false");
        turnout.setState(Turnout.CLOSED);
        assertTrue( cv.evaluate(), "evaluate() returns true");

        cv = new ConditionalVariable_BeanAlwaysNull(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_CLOSED, deviceName, false);
        assertFalse( cv.evaluate(), "evaluate() returns false");
        JUnitAppender.assertErrorMessage("invalid turnout name= \"3\" in state variable");


        Memory memory = InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        cv.setDataString("2");
        memory.setState(Sensor.ACTIVE);     // Sensor.ACTIVE = 0x02
        assertTrue( cv.evaluate(), "evaluate() returns true");
        memory.setState(Sensor.INACTIVE);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provideMemory(otherDeviceName);
        otherMemory.setState(Sensor.ACTIVE);    // Remove this???
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_COMPARE, deviceName, false);
        cv.setDataString(otherDeviceName);
        memory.setState(Sensor.ACTIVE);     // Sensor.ACTIVE = 0x02
        assertTrue( cv.evaluate(), "evaluate() returns true");
        memory.setState(Sensor.INACTIVE);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv = new ConditionalVariable_BeanAlwaysNull(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        assertFalse( cv.evaluate(), "evaluate() returns false");
        JUnitAppender.assertErrorMessage("invalid memory name= \"3\" in state variable");


        Light light = InstanceManager.getDefault(LightManager.class).provideLight(deviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.LIGHT_ON, deviceName, false);
        light.setState(Light.ON);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        light.setState(Light.OFF);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.LIGHT_OFF, deviceName, false);
        light.setState(Light.ON);
        assertFalse( cv.evaluate(), "evaluate() returns false");
        light.setState(Light.OFF);
        assertTrue( cv.evaluate(), "evaluate() returns true");

        cv = new ConditionalVariable_BeanAlwaysNull(false, Conditional.Operator.AND, Conditional.Type.LIGHT_OFF, deviceName, false);
        assertFalse( cv.evaluate(), "evaluate() returns false");
        JUnitAppender.assertErrorMessage("invalid light name= \"3\" in state variable");


        // Note that the signal head IH1 created here are also used to test the signal mast.
        SignalHead signalHeadIH1 = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH1);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_HEAD_YELLOW, "IH1", false);

        cv.setType(Conditional.Type.SIGNAL_HEAD_RED);
        signalHeadIH1.setAppearance(SignalHead.RED);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalHeadIH1.setAppearance(SignalHead.DARK);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv.setType(Conditional.Type.SIGNAL_HEAD_YELLOW);
        signalHeadIH1.setAppearance(SignalHead.YELLOW);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalHeadIH1.setAppearance(SignalHead.DARK);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv.setType(Conditional.Type.SIGNAL_HEAD_GREEN);
        signalHeadIH1.setAppearance(SignalHead.GREEN);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalHeadIH1.setAppearance(SignalHead.DARK);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv.setType(Conditional.Type.SIGNAL_HEAD_DARK);
        signalHeadIH1.setAppearance(SignalHead.DARK);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalHeadIH1.setAppearance(SignalHead.GREEN);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv.setType(Conditional.Type.SIGNAL_HEAD_FLASHRED);
        signalHeadIH1.setAppearance(SignalHead.FLASHRED);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalHeadIH1.setAppearance(SignalHead.DARK);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv.setType(Conditional.Type.SIGNAL_HEAD_FLASHYELLOW);
        signalHeadIH1.setAppearance(SignalHead.FLASHYELLOW);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalHeadIH1.setAppearance(SignalHead.DARK);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv.setType(Conditional.Type.SIGNAL_HEAD_FLASHGREEN);
        signalHeadIH1.setAppearance(SignalHead.FLASHGREEN);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalHeadIH1.setAppearance(SignalHead.DARK);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv.setType(Conditional.Type.SIGNAL_HEAD_LUNAR);
        signalHeadIH1.setAppearance(SignalHead.LUNAR);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalHeadIH1.setAppearance(SignalHead.DARK);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv.setType(Conditional.Type.SIGNAL_HEAD_FLASHLUNAR);
        signalHeadIH1.setAppearance(SignalHead.FLASHLUNAR);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalHeadIH1.setAppearance(SignalHead.DARK);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv.setType(Conditional.Type.SIGNAL_HEAD_LIT);
        signalHeadIH1.setLit(true);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalHeadIH1.setLit(false);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv.setType(Conditional.Type.SIGNAL_HEAD_HELD);
        signalHeadIH1.setHeld(true);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalHeadIH1.setHeld(false);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv = new ConditionalVariable_BeanAlwaysNull(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_HEAD_RED, "IH1", false);
        assertFalse( cv.evaluate(), "evaluate() returns false");
        JUnitAppender.assertErrorMessage("invalid signalhead name= \"IH1\" in state variable");


        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_MAST_LIT, "IF$shsm:AAR-1946:CPL(IH1)", false);

        cv.setDataString("Clear");
        cv.setType(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS);
        // The null check is only to ensure that the evaluate() tests aspect == null
        assertNull( signalMast.getAspect(), "aspect is null");
        assertFalse( cv.evaluate(), "evaluate() returns false");
        signalMast.setAspect("Clear");
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalMast.setAspect("Approach");
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv.setType(Conditional.Type.SIGNAL_MAST_LIT);
        signalMast.setLit(true);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalMast.setLit(false);
        assertFalse( cv.evaluate(), "evaluate() returns false");

        cv.setType(Conditional.Type.SIGNAL_MAST_HELD);
        signalMast.setHeld(true);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        signalMast.setHeld(false);
        assertFalse( cv.evaluate(), "evaluate() returns false");


        InstanceManager.getDefault(LogixManager.class).createNewLogix("IX:AUTO:0002");
        Conditional conditional = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX:AUTO:0001C1", "Conditional");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.CONDITIONAL_TRUE, "IX:AUTO:0001C1", false);
        conditional.setState(Conditional.TRUE);
        assertTrue( cv.evaluate(), "evaluate() returns true");
        conditional.setState(Conditional.FALSE);
        assertFalse( cv.evaluate(), "evaluate() returns false");


        // This is not yet implemented. The code below is only a non working sketch.
//        Warrant warrant = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW3");
//        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.ROUTE_OCCUPIED, "IW3", false);
//        cv.setType(Conditional.Type.ROUTE_FREE);
//        warrant.setState(Sensor.ACTIVE);
//        Assert.assertTrue("evaluate() returns true", cv.evaluate());
//        warrant.setState(Sensor.INACTIVE);
//        Assert.assertFalse("evaluate() returns false", cv.evaluate());


        // This is not yet implemented. The code below is only a non working sketch.
//        OBlock oblock = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB3");
//        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.BLOCK_STATUS_EQUALS, "OB3", false);
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
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SENSOR_ACTIVE, deviceName, false);

        assertTrue( cv.compare(null, null, false), "evaluate() returns true");
        assertFalse( cv.compare("10", null, false), "evaluate() returns false");
        assertFalse( cv.compare(null, "20", false), "evaluate() returns false");

        cv.setNum1(LESS_THAN);
        assertTrue( cv.compare("10", "20", false), "evaluate() returns true");
        assertFalse( cv.compare("15", "15", false), "evaluate() returns false");
        assertFalse( cv.compare("20", "10", false), "evaluate() returns false");

        cv.setNum1(LESS_THAN_OR_EQUAL);
        assertTrue( cv.compare("10", "20", false), "evaluate() returns true");
        assertTrue( cv.compare("15", "15", false), "evaluate() returns true");
        assertFalse( cv.compare("20", "10", false), "evaluate() returns false");

        cv.setNum1(EQUAL);
        assertFalse( cv.compare("10", "20", false), "evaluate() returns false");
        assertTrue( cv.compare("15", "15", false), "evaluate() returns true");
        assertFalse( cv.compare("20", "10", false), "evaluate() returns false");

        cv.setNum1(GREATER_THAN_OR_EQUAL);
        assertFalse( cv.compare("10", "20", false), "evaluate() returns false");
        assertTrue( cv.compare("15", "15", false), "evaluate() returns true");
        assertTrue( cv.compare("20", "10", false), "evaluate() returns true");

        cv.setNum1(GREATER_THAN);
        assertFalse( cv.compare("10", "20", false), "evaluate() returns false");
        assertFalse( cv.compare("15", "15", false), "evaluate() returns false");
        assertTrue( cv.compare("20", "10", false), "evaluate() returns true");


        cv.setNum1(LESS_THAN);
        assertTrue( cv.compare("aaa", "ccc", false), "evaluate() returns true");
        assertFalse( cv.compare("bbb", "bbb", false), "evaluate() returns false");
        assertFalse( cv.compare("ccc", "aaa", false), "evaluate() returns false");

        cv.setNum1(LESS_THAN_OR_EQUAL);
        assertTrue( cv.compare("aaa", "ccc", false), "evaluate() returns true");
        assertTrue( cv.compare("bbb", "bbb", false), "evaluate() returns true");
        assertFalse( cv.compare("ccc", "aaa", false), "evaluate() returns false");

        cv.setNum1(EQUAL);
        assertFalse( cv.compare("aaa", "ccc", false), "evaluate() returns false");
        assertTrue( cv.compare("bbb", "bbb", false), "evaluate() returns true");
        assertFalse( cv.compare("ccc", "aaa", false), "evaluate() returns false");

        cv.setNum1(GREATER_THAN_OR_EQUAL);
        assertFalse( cv.compare("aaa", "ccc", false), "evaluate() returns false");
        assertTrue( cv.compare("bbb", "bbb", false), "evaluate() returns true");
        assertTrue( cv.compare("ccc", "aaa", false), "evaluate() returns true");

        cv.setNum1(GREATER_THAN);
        assertFalse( cv.compare("aaa", "ccc", false), "evaluate() returns false");
        assertFalse( cv.compare("bbb", "bbb", false), "evaluate() returns false");
        assertTrue( cv.compare("ccc", "aaa", false), "evaluate() returns true");

        // Test case
        cv.setNum1(GREATER_THAN);
        assertTrue( cv.compare("aaa", "Ccc", false), "evaluate() returns true");
        assertTrue( cv.compare("bbb", "Bbb", false), "evaluate() returns true");
        assertFalse( cv.compare("Ccc", "aaa", false), "evaluate() returns false");

        // Test case
        cv.setNum1(GREATER_THAN);
        assertFalse( cv.compare("aaa", "Ccc", true), "evaluate() returns false");
        assertFalse( cv.compare("bbb", "Bbb", true), "evaluate() returns false");
        assertTrue( cv.compare("Ccc", "aaa", true), "evaluate() returns true");
    }

    @Test
    public void testGetItemTypeString() {
        assertEquals( "Sensor", ConditionalVariable.getItemTypeString(ItemType.SENSOR),
            "getItemTypeString() returns correct value");
        assertEquals( "Turnout", ConditionalVariable.getItemTypeString(ItemType.TURNOUT),
            "getItemTypeString() returns correct value");
        assertEquals( "Light", ConditionalVariable.getItemTypeString(ItemType.LIGHT),
            "getItemTypeString() returns correct value");
        assertEquals( "Signal Head", ConditionalVariable.getItemTypeString(ItemType.SIGNALHEAD),
            "getItemTypeString() returns correct value");
        assertEquals( "Signal Mast", ConditionalVariable.getItemTypeString(ItemType.SIGNALMAST),
            "getItemTypeString() returns correct value");
        assertEquals( "Memory", ConditionalVariable.getItemTypeString(ItemType.MEMORY),
            "getItemTypeString() returns correct value");
        assertEquals( "Conditional", ConditionalVariable.getItemTypeString(ItemType.CONDITIONAL),
            "getItemTypeString() returns correct value");
        assertEquals( "Warrant", ConditionalVariable.getItemTypeString(ItemType.WARRANT),
            "getItemTypeString() returns correct value");
        assertEquals( "Fast Clock", ConditionalVariable.getItemTypeString(ItemType.CLOCK),
            "getItemTypeString() returns correct value");
        assertEquals( "Occupancy Block", ConditionalVariable.getItemTypeString(ItemType.OBLOCK),
            "getItemTypeString() returns correct value");
        assertEquals( "Entry Exit", ConditionalVariable.getItemTypeString(ItemType.ENTRYEXIT),
            "getItemTypeString() returns correct value");

        // Test wrong value
//        Assert.assertTrue("getItemTypeString() returns correct value",
//                "".equals(ConditionalVariable.getItemTypeString(-1)));
    }

    @Test
    public void testConditionalType_toString() {
        assertEquals( "", Conditional.Type.NONE.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Active", Conditional.Type.SENSOR_ACTIVE.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Inactive", Conditional.Type.SENSOR_INACTIVE.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Thrown", Conditional.Type.TURNOUT_THROWN.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Closed", Conditional.Type.TURNOUT_CLOSED.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "True", Conditional.Type.CONDITIONAL_TRUE.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "False", Conditional.Type.CONDITIONAL_FALSE.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "On", Conditional.Type.LIGHT_ON.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Off", Conditional.Type.LIGHT_OFF.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "(case sensitive) Value", Conditional.Type.MEMORY_EQUALS.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "(case sensitive) Memory", Conditional.Type.MEMORY_COMPARE.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Fast Clock Range", Conditional.Type.FAST_CLOCK_RANGE.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Red", Conditional.Type.SIGNAL_HEAD_RED.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Yellow", Conditional.Type.SIGNAL_HEAD_YELLOW.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Green", Conditional.Type.SIGNAL_HEAD_GREEN.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Dark", Conditional.Type.SIGNAL_HEAD_DARK.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Flashing Red", Conditional.Type.SIGNAL_HEAD_FLASHRED.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Flashing Yellow", Conditional.Type.SIGNAL_HEAD_FLASHYELLOW.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Flashing Green", Conditional.Type.SIGNAL_HEAD_FLASHGREEN.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Held", Conditional.Type.SIGNAL_HEAD_HELD.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Lunar", Conditional.Type.SIGNAL_HEAD_LUNAR.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Flashing Lunar", Conditional.Type.SIGNAL_HEAD_FLASHLUNAR.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Lit", Conditional.Type.SIGNAL_HEAD_LIT.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "(case insensitive) Value", Conditional.Type.MEMORY_EQUALS_INSENSITIVE.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "(case insensitive) Memory", Conditional.Type.MEMORY_COMPARE_INSENSITIVE.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Route Free", Conditional.Type.ROUTE_FREE.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Route Occupied", Conditional.Type.ROUTE_OCCUPIED.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Route Allocated", Conditional.Type.ROUTE_ALLOCATED.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Route Set", Conditional.Type.ROUTE_SET.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Train Running", Conditional.Type.TRAIN_RUNNING.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Signal Mast Aspect equals", Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Signal Head Appearance equals", Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Lit", Conditional.Type.SIGNAL_MAST_LIT.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Held", Conditional.Type.SIGNAL_MAST_HELD.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Active", Conditional.Type.ENTRYEXIT_ACTIVE.toString(),
            "getItemTypeString() returns correct value");
        assertEquals( "Inactive", Conditional.Type.ENTRYEXIT_INACTIVE.toString(),
            "getItemTypeString() returns correct value");

        // Test invalid value
//        Assert.assertTrue("getItemTypeString() returns correct value",
//                "<none>".equals(-1)));
//        jmri.util.JUnitAppender.assertWarnMessage("Unhandled condition type: -1");
    }
/*
    @Test
    public void testDescribeState() {
        Assert.assertTrue("getItemTypeString() returns correct value",
                "".equals(ConditionalVariable.describeState(Conditional.Type.NONE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Active".equals(ConditionalVariable.describeState(Conditional.Type.SENSOR_ACTIVE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Inactive".equals(ConditionalVariable.describeState(Conditional.Type.SENSOR_INACTIVE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Thrown".equals(ConditionalVariable.describeState(Conditional.Type.TURNOUT_THROWN)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Closed".equals(ConditionalVariable.describeState(Conditional.Type.TURNOUT_CLOSED)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "True".equals(ConditionalVariable.describeState(Conditional.Type.CONDITIONAL_TRUE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "False".equals(ConditionalVariable.describeState(Conditional.Type.CONDITIONAL_FALSE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "On".equals(ConditionalVariable.describeState(Conditional.Type.LIGHT_ON)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Off".equals(ConditionalVariable.describeState(Conditional.Type.LIGHT_OFF)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "(case sensitive) Value".equals(ConditionalVariable.describeState(Conditional.Type.MEMORY_EQUALS)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "(case sensitive) Memory".equals(ConditionalVariable.describeState(Conditional.Type.MEMORY_COMPARE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "".equals(ConditionalVariable.describeState(Conditional.Type.FAST_CLOCK_RANGE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Red".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_HEAD_RED)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Yellow".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_HEAD_YELLOW)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Green".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_HEAD_GREEN)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Dark".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_HEAD_DARK)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Flashing Red".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_HEAD_FLASHRED)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Flashing Yellow".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_HEAD_FLASHYELLOW)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Flashing Green".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_HEAD_FLASHGREEN)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Held".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_HEAD_HELD)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Lunar".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_HEAD_LUNAR)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Flashing Lunar".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_HEAD_FLASHLUNAR)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Lit".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_HEAD_LIT)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "(case insensitive) Value".equals(ConditionalVariable.describeState(Conditional.Type.MEMORY_EQUALS_INSENSITIVE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "(case insensitive) Memory".equals(ConditionalVariable.describeState(Conditional.Type.MEMORY_COMPARE_INSENSITIVE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Route Free".equals(ConditionalVariable.describeState(Conditional.Type.ROUTE_FREE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Route Occupied".equals(ConditionalVariable.describeState(Conditional.Type.ROUTE_OCCUPIED)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Route Allocated".equals(ConditionalVariable.describeState(Conditional.Type.ROUTE_ALLOCATED)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Route Set".equals(ConditionalVariable.describeState(Conditional.Type.ROUTE_SET)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Train Running".equals(ConditionalVariable.describeState(Conditional.Type.TRAIN_RUNNING)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Signal Mast Aspect equals".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Signal Head Appearance equals".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Lit".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_MAST_LIT)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Held".equals(ConditionalVariable.describeState(Conditional.Type.SIGNAL_MAST_HELD)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Active".equals(ConditionalVariable.describeState(Conditional.Type.ENTRYEXIT_ACTIVE)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Inactive".equals(ConditionalVariable.describeState(Conditional.Type.ENTRYEXIT_INACTIVE)));

        // Test invalid value
        Assert.assertTrue("getItemTypeString() returns correct value",
                "<none>".equals(ConditionalVariable.describeState(-1)));
        jmri.util.JUnitAppender.assertWarnMessage("Unhandled condition type: -1");
    }
*/
    @Test
    public void testGetCompareSymbols() {
        assertEquals( "<", ConditionalVariable.getCompareSymbols(LESS_THAN),
            "getItemTypeString() returns correct value");
        assertEquals( "<=", ConditionalVariable.getCompareSymbols(LESS_THAN_OR_EQUAL),
            "getItemTypeString() returns correct value");
        assertEquals( "=", ConditionalVariable.getCompareSymbols(EQUAL),
            "getItemTypeString() returns correct value");
        assertEquals( ">=", ConditionalVariable.getCompareSymbols(GREATER_THAN_OR_EQUAL),
            "getItemTypeString() returns correct value");
        assertEquals( ">", ConditionalVariable.getCompareSymbols(GREATER_THAN),
            "getItemTypeString() returns correct value");
    }

    @Test
    public void testStringToVariableTest() {
        assertTrue( ConditionalVariable.stringToVariableTest("Red") == Conditional.Type.SIGNAL_HEAD_RED,
            "getItemTypeString() returns correct value");
        assertTrue( ConditionalVariable.stringToVariableTest("Yellow") == Conditional.Type.SIGNAL_HEAD_YELLOW,
            "getItemTypeString() returns correct value");
        assertTrue( ConditionalVariable.stringToVariableTest("Green") == Conditional.Type.SIGNAL_HEAD_GREEN,
            "getItemTypeString() returns correct value");
        assertTrue( ConditionalVariable.stringToVariableTest("Dark") == Conditional.Type.SIGNAL_HEAD_DARK,
            "getItemTypeString() returns correct value");
        assertTrue( ConditionalVariable.stringToVariableTest("Flashing Red") == Conditional.Type.SIGNAL_HEAD_FLASHRED,
            "getItemTypeString() returns correct value");
        assertTrue( ConditionalVariable.stringToVariableTest("Flashing Yellow") == Conditional.Type.SIGNAL_HEAD_FLASHYELLOW,
            "getItemTypeString() returns correct value");
        assertTrue( ConditionalVariable.stringToVariableTest("Flashing Green") == Conditional.Type.SIGNAL_HEAD_FLASHGREEN,
            "getItemTypeString() returns correct value");
        assertTrue( ConditionalVariable.stringToVariableTest("Lunar") == Conditional.Type.SIGNAL_HEAD_LUNAR,
            "getItemTypeString() returns correct value");
        assertTrue( ConditionalVariable.stringToVariableTest("Flashing Lunar") == Conditional.Type.SIGNAL_HEAD_FLASHLUNAR,
            "getItemTypeString() returns correct value");

        // Check bad string. This gives a warning message.
        assertTrue( ConditionalVariable.stringToVariableTest("Bad signal head") == Conditional.Type.ERROR,
            "getItemTypeString() returns -1 for wrong string");
        JUnitAppender.assertWarnMessage("Unexpected parameter to stringToVariableTest(Bad signal head)");

        // Check empty string. This doesn't give any warning.
        assertTrue( ConditionalVariable.stringToVariableTest("") == Conditional.Type.ERROR,
            "getItemTypeString() returns -1 for wrong string");
    }


    // from here down is testing infrastructure

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initIdTagManager();
        InstanceManager.store(new NamedBeanHandleManager(), NamedBeanHandleManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }



    /**
     * A conditional variable there the method getBean() always return null.
     * Used to test ConditionalVariable.evaluate().
     */
    private static class ConditionalVariable_BeanAlwaysNull extends ConditionalVariable {

        ConditionalVariable_BeanAlwaysNull(boolean not, Operator opern, Conditional.Type type, String name, boolean trigger) {
            super(not, opern, type, name, trigger);
        }

        /**
         * This method always return null in order to test the caller methods.
         * @return null always
         */
        @Override
        public NamedBean getBean() {
            return null;
        }

    }

}
