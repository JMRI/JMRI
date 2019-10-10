package jmri;

import static jmri.Conditional.*;
import static jmri.ConditionalVariable.*;

import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import org.junit.*;

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

        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SENSOR_ACTIVE, deviceName, false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid sensor name= \"3\" in state variable");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_THROWN, deviceName, false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid turnout name= \"3\" in state variable");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid memory name= \"3\" in state variable");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.LIGHT_ON, deviceName, false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid light name= \"3\" in state variable");

        // Note that the signal head IH1 created here are also used to test the signal mast.
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_HEAD_RED, "IH1", false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertWarnMessage("could not provide \"IH1\" in constructor");

        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "IF$shsm:AAR-1946:CPL(IH1)", false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid signalmast name= \"IF$shsm:AAR-1946:CPL(IH1)\" in state variable");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.CONDITIONAL_TRUE, "IX:AUTO:0001C1", false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid conditional; name= \"IX:AUTO:0001C1\" in state variable");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.ROUTE_OCCUPIED, "IW3", false);
        Assert.assertTrue("getNamedBean() returns null", cv.getNamedBean() == null);
        jmri.util.JUnitAppender.assertWarnMessage("could not provide \"IW3\" in constructor");

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.BLOCK_STATUS_EQUALS, "OB3", false);
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
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SENSOR_ACTIVE, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName(otherDeviceName);
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        Assert.assertTrue("toString() returns correct value",
                "Sensor \"4\" state is \"Sensor Active\"".equals(cv.toString()));

        bean = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(deviceName);
        otherBean = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_THROWN, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName(otherDeviceName);
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        Assert.assertTrue("toString() returns correct value",
                "Turnout \"4\" state is \"Turnout Thrown\"".equals(cv.toString()));

        bean = InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        otherBean = InstanceManager.getDefault(MemoryManager.class).provideMemory(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName(otherDeviceName);
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setDataString("A desired memory value");
        Assert.assertTrue("toString() returns correct value",
                "Memory \"4\" = value \"A desired memory value\"".equals(cv.toString()));
        cv.setType(Conditional.Type.MEMORY_COMPARE);
        cv.setDataString("MemoryVariable");
        Assert.assertTrue("toString() returns correct value",
                "Memory \"4\" = Memory \"MemoryVariable\"".equals(cv.toString()));

        bean = InstanceManager.getDefault(LightManager.class).provideLight(deviceName);
        otherBean = InstanceManager.getDefault(LightManager.class).provideLight(otherDeviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.LIGHT_ON, deviceName, false);
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
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_HEAD_RED, "IH1", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("IH2");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        Assert.assertTrue("toString() returns correct value",
                "Signal Head \"IH2\" Appearance is \"Red\"".equals(cv.toString()));
        cv.setType(Conditional.Type.SIGNAL_HEAD_LIT);
        Assert.assertTrue("toString() returns correct value",
                "Signal Head \"IH2\" state is \"Lit\"".equals(cv.toString()));

        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        bean = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        otherBean = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH2)");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "IF$shsm:AAR-1946:CPL(IH1)", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("IF$shsm:AAR-1946:CPL(IH2)");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setDataString("Approach");
        Assert.assertTrue("toString() returns correct value",
                "Signal Mast \"IF$shsm:AAR-1946:CPL(IH2)\" Aspect is \"Approach\"".equals(cv.toString()));
        cv.setType(Conditional.Type.SIGNAL_MAST_LIT);
        Assert.assertTrue("toString() returns correct value",
                "Signal Mast \"IF$shsm:AAR-1946:CPL(IH2)\" state is \"Lit\"".equals(cv.toString()));

        InstanceManager.getDefault(LogixManager.class).createNewLogix("IX:AUTO:0002");
        bean = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX:AUTO:0001C1", "Conditional");
        otherBean = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX:AUTO:0001C2", "Conditional");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.CONDITIONAL_TRUE, "IX:AUTO:0001C1", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("IX:AUTO:0001C2");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setGuiName("A Gui name");
        Assert.assertTrue("toString() returns correct value",
                "Conditional \"A Gui name\" state is \"Conditional True\"".equals(cv.toString()));

        bean = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW3");
        otherBean = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW4");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.ROUTE_OCCUPIED, "IW3", false);
        Assert.assertTrue("getNamedBean() returns correct bean", bean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        cv.setName("IW4");
        Assert.assertTrue("setName() sets correct bean", otherBean.equals(((NamedBeanHandle)cv.getNamedBean()).getBean()));
        Assert.assertTrue("toString() returns correct value",
                "WarrantRoute \"IW4\" state is \"Occupied\"".equals(cv.toString()));

        bean = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB3");
        otherBean = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB4");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.BLOCK_STATUS_EQUALS, "OB3", false);
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
    public void testEnumOperator() {
        // OPERATOR_NONE = 4;
        Assert.assertTrue("Operator.getIntValue() returns correct value", Operator.NONE.getIntValue() == 4);
        // OPERATOR_AND = 1;
        Assert.assertTrue("Operator.getIntValue() returns correct value", Operator.AND.getIntValue() == 1);
        // OPERATOR_OR = 5;
        Assert.assertTrue("Operator.getIntValue() returns correct value", Operator.OR.getIntValue() == 5);

        // OPERATOR_AND = 1;
        Assert.assertTrue("Operator.getOperatorFromIntValue() returns correct value", Operator.getOperatorFromIntValue(1) == Operator.AND);
        // OPERATOR_NONE = 4;
        Assert.assertTrue("Operator.getOperatorFromIntValue() returns correct value", Operator.getOperatorFromIntValue(4) == Operator.NONE);
        // OPERATOR_OR = 5;
        Assert.assertTrue("Operator.getOperatorFromIntValue() returns correct value", Operator.getOperatorFromIntValue(5) == Operator.OR);

        // Test illegal operator
        boolean exceptionThrown = false;
        try {
            Operator.getOperatorFromIntValue(-1);
        } catch (java.lang.IllegalArgumentException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue("Operator.getOperatorFromIntValue(-1) throws IllegalArgumentException", exceptionThrown);
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
        ConditionalVariable c1 = new ConditionalVariable(false, Operator.AND, Conditional.Type.SENSOR_INACTIVE, "name", false);
        ConditionalVariable c2 = new ConditionalVariable(false, Operator.AND, Conditional.Type.SENSOR_INACTIVE, "name", false);

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
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
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
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("state is unknown", cv.getState() == Conditional.State.UNKNOWN.getIntValue());
        cv.setState(Conditional.State.TRUE.getIntValue());
        Assert.assertTrue("state is TRUE", cv.getState() == Conditional.State.TRUE.getIntValue());
        cv.setState(Conditional.State.FALSE.getIntValue());
        Assert.assertTrue("state is FALSE", cv.getState() == Conditional.State.FALSE.getIntValue());
        cv.setState(true);
        Assert.assertTrue("state is TRUE", cv.getState() == Conditional.State.TRUE.getIntValue());
        cv.setState(false);
        Assert.assertTrue("state is FALSE", cv.getState() == Conditional.State.FALSE.getIntValue());
    }

    @Test
    public void testGetOpernString() {
        String deviceName = "3";
        InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
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
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Memory Compare to Value (Case Sensitive)".equals(cv.getTestTypeString()));

        Assert.assertTrue("Sensor Active",
                "Sensor Active".equals(Conditional.Type.SENSOR_ACTIVE.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Sensor Inactive".equals(Conditional.Type.SENSOR_INACTIVE.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Turnout Thrown".equals(Conditional.Type.TURNOUT_THROWN.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Turnout Closed".equals(Conditional.Type.TURNOUT_CLOSED.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Conditional True".equals(Conditional.Type.CONDITIONAL_TRUE.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Conditional False".equals(Conditional.Type.CONDITIONAL_FALSE.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Light On".equals(Conditional.Type.LIGHT_ON.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Light Off".equals(Conditional.Type.LIGHT_OFF.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Memory Compare to Value (Case Sensitive)".equals(Conditional.Type.MEMORY_EQUALS.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Memory Compare to Memory (Case Sensitive)".equals(Conditional.Type.MEMORY_COMPARE.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Fast Clock Range".equals(Conditional.Type.FAST_CLOCK_RANGE.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Red".equals(Conditional.Type.SIGNAL_HEAD_RED.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Yellow".equals(Conditional.Type.SIGNAL_HEAD_YELLOW.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Green".equals(Conditional.Type.SIGNAL_HEAD_GREEN.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Dark".equals(Conditional.Type.SIGNAL_HEAD_DARK.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Flashing Red".equals(Conditional.Type.SIGNAL_HEAD_FLASHRED.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Flashing Yellow".equals(Conditional.Type.SIGNAL_HEAD_FLASHYELLOW.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Flashing Green".equals(Conditional.Type.SIGNAL_HEAD_FLASHGREEN.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Lit".equals(Conditional.Type.SIGNAL_HEAD_LIT.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Held".equals(Conditional.Type.SIGNAL_HEAD_HELD.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Lunar".equals(Conditional.Type.SIGNAL_HEAD_LUNAR.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Flashing Lunar".equals(Conditional.Type.SIGNAL_HEAD_FLASHLUNAR.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Memory Compare to Value (Case Insensitive)".equals(Conditional.Type.MEMORY_EQUALS_INSENSITIVE.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Memory Compare to Memory (Case Insensitive)".equals(Conditional.Type.MEMORY_COMPARE_INSENSITIVE.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Free".equals(Conditional.Type.ROUTE_FREE.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Occupied".equals(Conditional.Type.ROUTE_OCCUPIED.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Allocated".equals(Conditional.Type.ROUTE_ALLOCATED.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Set".equals(Conditional.Type.ROUTE_SET.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Running".equals(Conditional.Type.TRAIN_RUNNING.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Signal Mast Aspect equals".equals(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Signal Head Appearance equals".equals(Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Lit".equals(Conditional.Type.SIGNAL_MAST_LIT.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Held".equals(Conditional.Type.SIGNAL_MAST_HELD.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Active".equals(Conditional.Type.ENTRYEXIT_ACTIVE.getTestTypeString()));
        Assert.assertTrue("getTestTypeString() returns correct value",
                "Inactive".equals(Conditional.Type.ENTRYEXIT_INACTIVE.getTestTypeString()));

        // Test invalid value
//        Assert.assertTrue("getTestTypeString() returns correct value",
//                "(None)".equals(ConditionalVariable.getTestTypeString(-1)));
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
        String otherDeviceName = "5";

        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provideSensor(deviceName);
        ConditionalVariable cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SENSOR_ACTIVE, deviceName, false);
        sensor.setState(Sensor.ACTIVE);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        sensor.setState(Sensor.INACTIVE);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        cv = new ConditionalVariable_BeanAlwaysNull(false, Conditional.Operator.AND, Conditional.Type.SENSOR_ACTIVE, deviceName, false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        jmri.util.JUnitAppender.assertErrorMessage("invalid sensor name= \"3\" in state variable");


        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(deviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_THROWN, deviceName, false);
        turnout.setState(Turnout.THROWN);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        turnout.setState(Turnout.CLOSED);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_CLOSED, deviceName, false);
        turnout.setState(Turnout.THROWN);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        turnout.setState(Turnout.CLOSED);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());

        cv = new ConditionalVariable_BeanAlwaysNull(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_CLOSED, deviceName, false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        jmri.util.JUnitAppender.assertErrorMessage("invalid turnout name= \"3\" in state variable");


        Memory memory = InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        cv.setDataString("2");
        memory.setState(Sensor.ACTIVE);     // Sensor.ACTIVE = 0x02
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        memory.setState(Sensor.INACTIVE);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provideMemory(otherDeviceName);
        otherMemory.setState(Sensor.ACTIVE);    // Remove this???
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.MEMORY_COMPARE, deviceName, false);
        cv.setDataString(otherDeviceName);
        memory.setState(Sensor.ACTIVE);     // Sensor.ACTIVE = 0x02
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        memory.setState(Sensor.INACTIVE);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv = new ConditionalVariable_BeanAlwaysNull(false, Conditional.Operator.AND, Conditional.Type.MEMORY_EQUALS, deviceName, false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        jmri.util.JUnitAppender.assertErrorMessage("invalid memory name= \"3\" in state variable");


        Light light = InstanceManager.getDefault(LightManager.class).provideLight(deviceName);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.LIGHT_ON, deviceName, false);
        light.setState(Light.ON);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        light.setState(Light.OFF);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.LIGHT_OFF, deviceName, false);
        light.setState(Light.ON);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        light.setState(Light.OFF);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());

        cv = new ConditionalVariable_BeanAlwaysNull(false, Conditional.Operator.AND, Conditional.Type.LIGHT_OFF, deviceName, false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        jmri.util.JUnitAppender.assertErrorMessage("invalid light name= \"3\" in state variable");


        // Note that the signal head IH1 created here are also used to test the signal mast.
        SignalHead signalHeadIH1 = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH1);
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_HEAD_YELLOW, "IH1", false);

        cv.setType(Conditional.Type.SIGNAL_HEAD_RED);
        signalHeadIH1.setAppearance(SignalHead.RED);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv.setType(Conditional.Type.SIGNAL_HEAD_YELLOW);
        signalHeadIH1.setAppearance(SignalHead.YELLOW);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv.setType(Conditional.Type.SIGNAL_HEAD_GREEN);
        signalHeadIH1.setAppearance(SignalHead.GREEN);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv.setType(Conditional.Type.SIGNAL_HEAD_DARK);
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.GREEN);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv.setType(Conditional.Type.SIGNAL_HEAD_FLASHRED);
        signalHeadIH1.setAppearance(SignalHead.FLASHRED);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv.setType(Conditional.Type.SIGNAL_HEAD_FLASHYELLOW);
        signalHeadIH1.setAppearance(SignalHead.FLASHYELLOW);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv.setType(Conditional.Type.SIGNAL_HEAD_FLASHGREEN);
        signalHeadIH1.setAppearance(SignalHead.FLASHGREEN);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv.setType(Conditional.Type.SIGNAL_HEAD_LUNAR);
        signalHeadIH1.setAppearance(SignalHead.LUNAR);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv.setType(Conditional.Type.SIGNAL_HEAD_FLASHLUNAR);
        signalHeadIH1.setAppearance(SignalHead.FLASHLUNAR);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setAppearance(SignalHead.DARK);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv.setType(Conditional.Type.SIGNAL_HEAD_LIT);
        signalHeadIH1.setLit(true);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setLit(false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv.setType(Conditional.Type.SIGNAL_HEAD_HELD);
        signalHeadIH1.setHeld(true);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalHeadIH1.setHeld(false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv = new ConditionalVariable_BeanAlwaysNull(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_HEAD_RED, "IH1", false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        jmri.util.JUnitAppender.assertErrorMessage("invalid signalhead name= \"IH1\" in state variable");


        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.SIGNAL_MAST_LIT, "IF$shsm:AAR-1946:CPL(IH1)", false);

        cv.setDataString("Clear");
        cv.setType(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS);
        // The null check is only to ensure that the evaluate() tests aspect == null
        Assert.assertTrue("aspect is null", signalMast.getAspect() == null);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());
        signalMast.setAspect("Clear");
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalMast.setAspect("Approach");
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv.setType(Conditional.Type.SIGNAL_MAST_LIT);
        signalMast.setLit(true);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalMast.setLit(false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());

        cv.setType(Conditional.Type.SIGNAL_MAST_HELD);
        signalMast.setHeld(true);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        signalMast.setHeld(false);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());


        InstanceManager.getDefault(LogixManager.class).createNewLogix("IX:AUTO:0002");
        Conditional conditional = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX:AUTO:0001C1", "Conditional");
        cv = new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.CONDITIONAL_TRUE, "IX:AUTO:0001C1", false);
        conditional.setState(Conditional.TRUE);
        Assert.assertTrue("evaluate() returns true", cv.evaluate());
        conditional.setState(Conditional.FALSE);
        Assert.assertFalse("evaluate() returns false", cv.evaluate());


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
                "Sensor".equals(ConditionalVariable.getItemTypeString(ItemType.SENSOR)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Turnout".equals(ConditionalVariable.getItemTypeString(ItemType.TURNOUT)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Light".equals(ConditionalVariable.getItemTypeString(ItemType.LIGHT)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Signal Head".equals(ConditionalVariable.getItemTypeString(ItemType.SIGNALHEAD)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Signal Mast".equals(ConditionalVariable.getItemTypeString(ItemType.SIGNALMAST)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Memory".equals(ConditionalVariable.getItemTypeString(ItemType.MEMORY)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Conditional".equals(ConditionalVariable.getItemTypeString(ItemType.CONDITIONAL)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Warrant".equals(ConditionalVariable.getItemTypeString(ItemType.WARRANT)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Fast Clock".equals(ConditionalVariable.getItemTypeString(ItemType.CLOCK)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Occupancy Block".equals(ConditionalVariable.getItemTypeString(ItemType.OBLOCK)));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Entry Exit".equals(ConditionalVariable.getItemTypeString(ItemType.ENTRYEXIT)));

        // Test wrong value
//        Assert.assertTrue("getItemTypeString() returns correct value",
//                "".equals(ConditionalVariable.getItemTypeString(-1)));
    }

    @Test
    public void testConditionalType_toString() {
        Assert.assertTrue("getItemTypeString() returns correct value",
                "".equals(Conditional.Type.NONE.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Active".equals(Conditional.Type.SENSOR_ACTIVE.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Inactive".equals(Conditional.Type.SENSOR_INACTIVE.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Thrown".equals(Conditional.Type.TURNOUT_THROWN.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Closed".equals(Conditional.Type.TURNOUT_CLOSED.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "True".equals(Conditional.Type.CONDITIONAL_TRUE.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "False".equals(Conditional.Type.CONDITIONAL_FALSE.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "On".equals(Conditional.Type.LIGHT_ON.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Off".equals(Conditional.Type.LIGHT_OFF.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "(case sensitive) Value".equals(Conditional.Type.MEMORY_EQUALS.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "(case sensitive) Memory".equals(Conditional.Type.MEMORY_COMPARE.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Fast Clock Range".equals(Conditional.Type.FAST_CLOCK_RANGE.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Red".equals(Conditional.Type.SIGNAL_HEAD_RED.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Yellow".equals(Conditional.Type.SIGNAL_HEAD_YELLOW.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Green".equals(Conditional.Type.SIGNAL_HEAD_GREEN.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Dark".equals(Conditional.Type.SIGNAL_HEAD_DARK.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Flashing Red".equals(Conditional.Type.SIGNAL_HEAD_FLASHRED.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Flashing Yellow".equals(Conditional.Type.SIGNAL_HEAD_FLASHYELLOW.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Flashing Green".equals(Conditional.Type.SIGNAL_HEAD_FLASHGREEN.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Held".equals(Conditional.Type.SIGNAL_HEAD_HELD.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Lunar".equals(Conditional.Type.SIGNAL_HEAD_LUNAR.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Flashing Lunar".equals(Conditional.Type.SIGNAL_HEAD_FLASHLUNAR.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Lit".equals(Conditional.Type.SIGNAL_HEAD_LIT.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "(case insensitive) Value".equals(Conditional.Type.MEMORY_EQUALS_INSENSITIVE.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "(case insensitive) Memory".equals(Conditional.Type.MEMORY_COMPARE_INSENSITIVE.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Route Free".equals(Conditional.Type.ROUTE_FREE.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Route Occupied".equals(Conditional.Type.ROUTE_OCCUPIED.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Route Allocated".equals(Conditional.Type.ROUTE_ALLOCATED.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Route Set".equals(Conditional.Type.ROUTE_SET.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Train Running".equals(Conditional.Type.TRAIN_RUNNING.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Signal Mast Aspect equals".equals(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Signal Head Appearance equals".equals(Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Lit".equals(Conditional.Type.SIGNAL_MAST_LIT.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Held".equals(Conditional.Type.SIGNAL_MAST_HELD.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Active".equals(Conditional.Type.ENTRYEXIT_ACTIVE.toString()));
        Assert.assertTrue("getItemTypeString() returns correct value",
                "Inactive".equals(Conditional.Type.ENTRYEXIT_INACTIVE.toString()));

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
                ConditionalVariable.stringToVariableTest("Red") == Conditional.Type.SIGNAL_HEAD_RED);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Yellow") == Conditional.Type.SIGNAL_HEAD_YELLOW);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Green") == Conditional.Type.SIGNAL_HEAD_GREEN);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Dark") == Conditional.Type.SIGNAL_HEAD_DARK);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Flashing Red") == Conditional.Type.SIGNAL_HEAD_FLASHRED);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Flashing Yellow") == Conditional.Type.SIGNAL_HEAD_FLASHYELLOW);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Flashing Green") == Conditional.Type.SIGNAL_HEAD_FLASHGREEN);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Lunar") == Conditional.Type.SIGNAL_HEAD_LUNAR);
        Assert.assertTrue("getItemTypeString() returns correct value",
                ConditionalVariable.stringToVariableTest("Flashing Lunar") == Conditional.Type.SIGNAL_HEAD_FLASHLUNAR);

        // Check bad string. This gives a warning message.
        Assert.assertTrue("getItemTypeString() returns -1 for wrong string",
                ConditionalVariable.stringToVariableTest("Bad signal head") == Conditional.Type.ERROR);
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected parameter to stringToVariableTest(Bad signal head)");

        // Check empty string. This doesn't give any warning.
        Assert.assertTrue("getItemTypeString() returns -1 for wrong string",
                ConditionalVariable.stringToVariableTest("") == Conditional.Type.ERROR);
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



    /**
     * A conditional variable there the method getBean() always return null.
     * Used to test ConditionalVariable.evaluate().
     */
    private class ConditionalVariable_BeanAlwaysNull extends ConditionalVariable {

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
