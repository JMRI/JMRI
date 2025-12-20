package jmri.implementation;

import static jmri.Conditional.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.*;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

/**
 * Test the DefaultConditionalAction implementation class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class DefaultConditionalActionTest {

    @Test
    public void testCtor() {
        DefaultConditionalAction t = new DefaultConditionalAction();
        Assertions.assertNotNull(t);
    }

    @Test
    public void testBasicBeanOperations() {
        final String deviceName = "3";
        final String otherDeviceName = "8";
        final String actionStr = "5";
        
        ConditionalAction ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);
        ConditionalAction ix2 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);

        ConditionalAction ix3 = new DefaultConditionalAction(0, Conditional.Action.SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);
        ConditionalAction ix4 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.NONE, deviceName, Turnout.THROWN, actionStr);
        ConditionalAction ix5 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, "0", Turnout.THROWN, actionStr);
        ConditionalAction ix6 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, deviceName, 0, actionStr);
        ConditionalAction ix7 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, deviceName, Turnout.THROWN, "0");
        ConditionalAction ix8 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, otherDeviceName, Turnout.THROWN, "0");
        ConditionalAction ix9 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, deviceName, Turnout.THROWN, "1");

        ConditionalAction ix10 = new DefaultConditionalAction(0, Conditional.Action.NONE, null, Turnout.THROWN, actionStr);

        assertNotNull(ix1);

        assertTrue(ix1.equals(ix1));
        assertTrue(ix1.equals(ix2));

        assertTrue(!ix1.equals(ix3));
        assertTrue(!ix1.equals(ix4));
        assertTrue(!ix1.equals(ix5));
        assertTrue(!ix1.equals(ix6));
        assertTrue(!ix1.equals(ix7));
        assertTrue(!ix1.equals(ix8));
        assertTrue(!ix1.equals(ix9));

        // Test equal with different class
        assertTrue(!ix1.equals(new Object()));
        
        // Test deviceName == null
        assertTrue(!ix1.equals(ix10));
        assertTrue(!ix10.equals(ix1));
        
        assertTrue(ix1.hashCode() == ix2.hashCode());
    }

    @Test
    public void testSetType() {
        final String deviceName = "3";
        final String actionStr = "5";
        
        ConditionalAction ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);
        
        ix1.setType("None");
        assertTrue( ix1.getType() == Conditional.Action.NONE, "setType() sets correct value");
        JUnitAppender.assertWarnMessage("Unexpected parameter to stringToActionType(None)");

        ix1.setType("Set Turnout");
        assertTrue( ix1.getType() == Conditional.Action.SET_TURNOUT, "setType() sets correct value");
        
        ix1.setType("Set Signal Head Appearance");
        assertTrue( ix1.getType() == Conditional.Action.SET_SIGNAL_APPEARANCE, "setType() sets correct value");
        
        ix1.setType("Set Signal Head Held");
        assertTrue( ix1.getType() == Conditional.Action.SET_SIGNAL_HELD, "setType() sets correct value");
        
        ix1.setType("Clear Signal Head Held");
        assertTrue( ix1.getType() == Conditional.Action.CLEAR_SIGNAL_HELD, "setType() sets correct value");
        
        ix1.setType("Set Signal Head Dark");
        assertTrue( ix1.getType() == Conditional.Action.SET_SIGNAL_DARK, "setType() sets correct value");
        
        ix1.setType("Set Signal Head Lit");
        assertTrue( ix1.getType() == Conditional.Action.SET_SIGNAL_LIT, "setType() sets correct value");
        
        ix1.setType("Trigger Route");
        assertTrue( ix1.getType() == Conditional.Action.TRIGGER_ROUTE, "setType() sets correct value");
        
        ix1.setType("Set Sensor");
        assertTrue( ix1.getType() == Conditional.Action.SET_SENSOR, "setType() sets correct value");
        
        ix1.setType("Delayed Set Sensor");
        assertTrue( ix1.getType() == Conditional.Action.DELAYED_SENSOR, "setType() sets correct value");
        
        ix1.setType("Set Light");
        assertTrue( ix1.getType() == Conditional.Action.SET_LIGHT, "setType() sets correct value");
        
        ix1.setType("Set Memory");
        assertTrue( ix1.getType() == Conditional.Action.SET_MEMORY, "setType() sets correct value");
        
        ix1.setType("Enable Logix");
        assertTrue( ix1.getType() == Conditional.Action.ENABLE_LOGIX, "setType() sets correct value");
        
        ix1.setType("Disable Logix");
        assertTrue( ix1.getType() == Conditional.Action.DISABLE_LOGIX, "setType() sets correct value");
        
        ix1.setType("Play Sound File");
        assertTrue( ix1.getType() == Conditional.Action.PLAY_SOUND, "setType() sets correct value");
        
        ix1.setType("Run Script");
        assertTrue( ix1.getType() == Conditional.Action.RUN_SCRIPT, "setType() sets correct value");
        
        ix1.setType("Delayed Set Turnout");
        assertTrue( ix1.getType() == Conditional.Action.DELAYED_TURNOUT, "setType() sets correct value");
        
        ix1.setType("Turnout Lock");
        assertTrue( ix1.getType() == Conditional.Action.LOCK_TURNOUT, "setType() sets correct value");
        
        ix1.setType("Reset Delayed Set Sensor");
        assertTrue( ix1.getType() == Conditional.Action.RESET_DELAYED_SENSOR, "setType() sets correct value");
        
        ix1.setType("Cancel Timers for Sensor");
        assertTrue( ix1.getType() == Conditional.Action.CANCEL_SENSOR_TIMERS, "setType() sets correct value");
        
        ix1.setType("Reset Delayed Set Turnout");
        assertTrue( ix1.getType() == Conditional.Action.RESET_DELAYED_TURNOUT, "setType() sets correct value");
        
        ix1.setType("Cancel Timers for Turnout");
        assertTrue( ix1.getType() == Conditional.Action.CANCEL_TURNOUT_TIMERS, "setType() sets correct value");
        
        ix1.setType("Set Fast Clock Time");
        assertTrue( ix1.getType() == Conditional.Action.SET_FAST_CLOCK_TIME, "setType() sets correct value");
        
        ix1.setType("Start Fast Clock");
        assertTrue( ix1.getType() == Conditional.Action.START_FAST_CLOCK, "setType() sets correct value");
        
        ix1.setType("Stop Fast Clock");
        assertTrue( ix1.getType() == Conditional.Action.STOP_FAST_CLOCK, "setType() sets correct value");
        
        ix1.setType("Copy Memory To Memory");
        assertTrue( ix1.getType() == Conditional.Action.COPY_MEMORY, "setType() sets correct value");
        
        ix1.setType("Set Light Intensity");
        assertTrue( ix1.getType() == Conditional.Action.SET_LIGHT_INTENSITY, "setType() sets correct value");
        
        ix1.setType("Set Light Transition Time");
        assertTrue( ix1.getType() == Conditional.Action.SET_LIGHT_TRANSITION_TIME, "setType() sets correct value");
        
        ix1.setType("Control Audio object");
        assertTrue( ix1.getType() == Conditional.Action.CONTROL_AUDIO, "setType() sets correct value");
        
        ix1.setType("Execute Jython Command");
        assertTrue( ix1.getType() == Conditional.Action.JYTHON_COMMAND, "setType() sets correct value");
        
        ix1.setType("Allocate Warrant Route");
        assertTrue( ix1.getType() == Conditional.Action.ALLOCATE_WARRANT_ROUTE, "setType() sets correct value");
        
        ix1.setType("Deallocate Warrant");
        assertTrue( ix1.getType() == Conditional.Action.DEALLOCATE_WARRANT_ROUTE, "setType() sets correct value");
        
        ix1.setType("Set Route Turnouts");
        assertTrue( ix1.getType() == Conditional.Action.SET_ROUTE_TURNOUTS, "setType() sets correct value");
        
        ix1.setType("Auto Run Train");
        assertTrue( ix1.getType() == Conditional.Action.AUTO_RUN_WARRANT, "setType() sets correct value");
        
        ix1.setType("Manually Run Train");
        assertTrue( ix1.getType() == Conditional.Action.MANUAL_RUN_WARRANT, "setType() sets correct value");
        
        ix1.setType("Control Auto Train");
        assertTrue( ix1.getType() == Conditional.Action.CONTROL_TRAIN, "setType() sets correct value");
        
        ix1.setType("Set Train ID");
        assertTrue( ix1.getType() == Conditional.Action.SET_TRAIN_ID, "setType() sets correct value");
        
        ix1.setType("Set Train Name");
        assertTrue( ix1.getType() == Conditional.Action.SET_TRAIN_NAME, "setType() sets correct value");
        
        ix1.setType("Set Signal Mast Aspect");
        assertTrue( ix1.getType() == Conditional.Action.SET_SIGNALMAST_ASPECT, "setType() sets correct value");
        
        ix1.setType("Put Location of Warrant");
        assertTrue( ix1.getType() == Conditional.Action.GET_TRAIN_LOCATION, "setType() sets correct value");
        
        ix1.setType("Set Signal Mast Held");
        assertTrue( ix1.getType() == Conditional.Action.SET_SIGNALMAST_HELD, "setType() sets correct value");
        
        ix1.setType("Clear Signal Mast Held");
        assertTrue( ix1.getType() == Conditional.Action.CLEAR_SIGNALMAST_HELD, "setType() sets correct value");
        
        ix1.setType("Set Signal Mast Dark");
        assertTrue( ix1.getType() == Conditional.Action.SET_SIGNALMAST_DARK, "setType() sets correct value");
        
        ix1.setType("Clear Signal Mast Dark");
        assertTrue( ix1.getType() == Conditional.Action.SET_SIGNALMAST_LIT, "setType() sets correct value");
        
        ix1.setType("Set Block Value");
        assertTrue( ix1.getType() == Conditional.Action.SET_BLOCK_VALUE, "setType() sets correct value");
        
        ix1.setType("Set Block Error");
        assertTrue( ix1.getType() == Conditional.Action.SET_BLOCK_ERROR, "setType() sets correct value");
        
        ix1.setType("Clear Block Error");
        assertTrue( ix1.getType() == Conditional.Action.CLEAR_BLOCK_ERROR, "setType() sets correct value");
        
        ix1.setType("Deallocate Block");
        assertTrue( ix1.getType() == Conditional.Action.DEALLOCATE_BLOCK, "setType() sets correct value");
        
        ix1.setType("Set Block OutOfService");
        assertTrue( ix1.getType() == Conditional.Action.SET_BLOCK_OUT_OF_SERVICE, "setType() sets correct value");
        
        ix1.setType("Clear Block OutOfService");
        assertTrue( ix1.getType() == Conditional.Action.SET_BLOCK_IN_SERVICE, "setType() sets correct value");
        
        ix1.setType("Set NX Pair Enabled");
        assertTrue( ix1.getType() == Conditional.Action.SET_NXPAIR_ENABLED, "setType() sets correct value");
        
        ix1.setType("Set NX Pair Disabled");
        assertTrue( ix1.getType() == Conditional.Action.SET_NXPAIR_DISABLED, "setType() sets correct value");
        
        ix1.setType("Set NX Pair Segment Active / Inactive");
        assertTrue( ix1.getType() == Conditional.Action.SET_NXPAIR_SEGMENT, "setType() sets correct value");
        
        ix1.setType("This is a bad string");
        JUnitAppender.assertWarnMessage("Unexpected parameter to stringToActionType(This is a bad string)");
        
        ix1.setType("Put Warrant occupying Block");
        assertTrue( ix1.getType() == Conditional.Action.GET_BLOCK_WARRANT, "setType() sets correct value");
        
        ix1.setType("Put Train Name occupying Block");
        assertTrue( ix1.getType() == Conditional.Action.GET_BLOCK_TRAIN_NAME, "setType() sets correct value");
    }

    @Test
    public void testStringToActionData() {
        assertTrue( DefaultConditionalAction.stringToActionData("Closed") == Turnout.CLOSED,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Thrown") == Turnout.THROWN,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Active") == Sensor.ACTIVE,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Inactive") == Sensor.INACTIVE,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("On") == Light.ON,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Off") == Light.OFF,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Unlock") == Turnout.UNLOCKED,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Lock") == Turnout.LOCKED,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Red") == SignalHead.RED,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Yellow") == SignalHead.YELLOW,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Green") == SignalHead.GREEN,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Dark") == SignalHead.DARK,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Flashing Red") == SignalHead.FLASHRED,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Flashing Yellow") == SignalHead.FLASHYELLOW,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Flashing Green") == SignalHead.FLASHGREEN,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Lunar") == SignalHead.LUNAR,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Flashing Lunar") == SignalHead.FLASHLUNAR,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Play") == Audio.CMD_PLAY,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Stop") == Audio.CMD_STOP,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Toggle Play") == Audio.CMD_PLAY_TOGGLE,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Pause") == Audio.CMD_PAUSE,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Resume") == Audio.CMD_RESUME,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Toggle Pause") == Audio.CMD_PAUSE_TOGGLE,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Rewind") == Audio.CMD_REWIND,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Fade-in") == Audio.CMD_FADE_IN,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Fade-out") == Audio.CMD_FADE_OUT,
            "stringToActionData() returns correct value");
        
        assertTrue( DefaultConditionalAction.stringToActionData("Reset Position") == Audio.CMD_RESET_POSITION,
            "stringToActionData() returns correct value");
        
        DefaultConditionalAction.stringToActionData("This is a bad string");
        JUnitAppender.assertWarnMessage("Unexpected parameter to stringToActionData(This is a bad string)");
    }

    @Test
    public void testGetActionDataString() {
        assertEquals( "",DefaultConditionalAction.getActionDataString(
            Conditional.Action.NONE, 0),"getActionDataString() returns correct value");
        assertEquals( "Closed", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_TURNOUT, Turnout.CLOSED),"getActionDataString() returns correct value");
        assertEquals( "Thrown", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_TURNOUT, Turnout.THROWN), "getActionDataString() returns correct value");
        assertEquals( "Toggle", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_TURNOUT, Route.TOGGLE), "getActionDataString() returns correct value");
        assertEquals( "Closed", DefaultConditionalAction.getActionDataString(
            Conditional.Action.DELAYED_TURNOUT, Turnout.CLOSED), "getActionDataString() returns correct value");
        assertEquals( "Thrown", DefaultConditionalAction.getActionDataString(
            Conditional.Action.DELAYED_TURNOUT, Turnout.THROWN), "getActionDataString() returns correct value");
        assertEquals( "Toggle", DefaultConditionalAction.getActionDataString(
            Conditional.Action.DELAYED_TURNOUT, Route.TOGGLE), "getActionDataString() returns correct value");
        assertEquals( "Closed", DefaultConditionalAction.getActionDataString(
            Conditional.Action.RESET_DELAYED_TURNOUT, Turnout.CLOSED), "getActionDataString() returns correct value");
        assertEquals( "Thrown", DefaultConditionalAction.getActionDataString(
            Conditional.Action.RESET_DELAYED_TURNOUT, Turnout.THROWN), "getActionDataString() returns correct value");
        assertEquals( "Toggle", DefaultConditionalAction.getActionDataString(
            Conditional.Action.RESET_DELAYED_TURNOUT, Route.TOGGLE), "getActionDataString() returns correct value");
        // Test invalid data
        assertEquals( "", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_TURNOUT, 0), "getActionDataString() returns correct value");
        
        assertEquals( "Red", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SIGNAL_APPEARANCE, SignalHead.RED), "getActionDataString() returns correct value");
        assertEquals( "Yellow", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SIGNAL_APPEARANCE, SignalHead.YELLOW), "getActionDataString() returns correct value");
        assertEquals( "Green", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SIGNAL_APPEARANCE, SignalHead.GREEN), "getActionDataString() returns correct value");
        assertEquals( "Dark", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SIGNAL_APPEARANCE, SignalHead.DARK), "getActionDataString() returns correct value");
        assertEquals( "Flashing Red", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SIGNAL_APPEARANCE, SignalHead.FLASHRED), "getActionDataString() returns correct value");
        assertEquals( "Flashing Yellow", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SIGNAL_APPEARANCE, SignalHead.FLASHYELLOW), "getActionDataString() returns correct value");
        assertEquals( "Flashing Green", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SIGNAL_APPEARANCE, SignalHead.FLASHGREEN), "getActionDataString() returns correct value");
        assertEquals( "Lunar", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SIGNAL_APPEARANCE, SignalHead.LUNAR), "getActionDataString() returns correct value");
        assertEquals( "Flashing Lunar", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SIGNAL_APPEARANCE, SignalHead.FLASHLUNAR), "getActionDataString() returns correct value");
        // Test invalid data
        assertEquals( "", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SIGNAL_APPEARANCE, -1), "getActionDataString() returns correct value");
        
        assertEquals( "Active", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SENSOR, Sensor.ACTIVE), "getActionDataString() returns correct value");
        assertEquals( "Inactive", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SENSOR, Sensor.INACTIVE), "getActionDataString() returns correct value");
        assertEquals( "Toggle", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SENSOR, Route.TOGGLE), "getActionDataString() returns correct value");
        assertEquals( "Active", DefaultConditionalAction.getActionDataString(
            Conditional.Action.DELAYED_SENSOR, Sensor.ACTIVE), "getActionDataString() returns correct value");
        assertEquals( "Inactive", DefaultConditionalAction.getActionDataString(
            Conditional.Action.DELAYED_SENSOR, Sensor.INACTIVE), "getActionDataString() returns correct value");
        assertEquals( "Toggle", DefaultConditionalAction.getActionDataString(
            Conditional.Action.DELAYED_SENSOR, Route.TOGGLE), "getActionDataString() returns correct value");
        assertEquals( "Active", DefaultConditionalAction.getActionDataString(
            Conditional.Action.RESET_DELAYED_SENSOR, Sensor.ACTIVE), "getActionDataString() returns correct value");
        assertEquals( "Inactive", DefaultConditionalAction.getActionDataString(
            Conditional.Action.RESET_DELAYED_SENSOR, Sensor.INACTIVE), "getActionDataString() returns correct value");
        assertEquals( "Toggle", DefaultConditionalAction.getActionDataString(
            Conditional.Action.RESET_DELAYED_SENSOR, Route.TOGGLE), "getActionDataString() returns correct value");
        // Test invalid data
        assertEquals( "", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_SENSOR, 0), "getActionDataString() returns correct value");
        
        assertEquals( "On", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_LIGHT, Light.ON), "getActionDataString() returns correct value");
        assertEquals( "Off", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_LIGHT, Light.OFF), "getActionDataString() returns correct value");
        assertEquals( "Toggle", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_LIGHT, Route.TOGGLE), "getActionDataString() returns correct value");
        // Test invalid data
        assertEquals( "", DefaultConditionalAction.getActionDataString(
            Conditional.Action.SET_LIGHT, 0), "getActionDataString() returns correct value");
        
        assertEquals( "Unlock", DefaultConditionalAction.getActionDataString(
            Conditional.Action.LOCK_TURNOUT, Turnout.UNLOCKED), "getActionDataString() returns correct value");
        assertEquals( "Lock", DefaultConditionalAction.getActionDataString(
            Conditional.Action.LOCK_TURNOUT, Turnout.LOCKED), "getActionDataString() returns correct value");
        assertEquals( "Toggle", DefaultConditionalAction.getActionDataString(
            Conditional.Action.LOCK_TURNOUT, Route.TOGGLE), "getActionDataString() returns correct value");
        // Test invalid data
        assertEquals( "", DefaultConditionalAction.getActionDataString(
            Conditional.Action.LOCK_TURNOUT, -1), "getActionDataString() returns correct value");
        
        assertEquals( "Play", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_AUDIO, Audio.CMD_PLAY), "getActionDataString() returns correct value");
        assertEquals( "Stop", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_AUDIO, Audio.CMD_STOP), "getActionDataString() returns correct value");
        assertEquals( "Toggle Play", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_AUDIO, Audio.CMD_PLAY_TOGGLE), "getActionDataString() returns correct value");
        assertEquals( "Pause", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_AUDIO, Audio.CMD_PAUSE), "getActionDataString() returns correct value");
        assertEquals( "Resume", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_AUDIO, Audio.CMD_RESUME), "getActionDataString() returns correct value");
        assertEquals( "Toggle Pause", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_AUDIO, Audio.CMD_PAUSE_TOGGLE), "getActionDataString() returns correct value");
        assertEquals( "Rewind", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_AUDIO, Audio.CMD_REWIND), "getActionDataString() returns correct value");
        assertEquals( "Fade-in", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_AUDIO, Audio.CMD_FADE_IN), "getActionDataString() returns correct value");
        assertEquals( "Fade-out", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_AUDIO, Audio.CMD_FADE_OUT), "getActionDataString() returns correct value");
        assertEquals( "Reset Position", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_AUDIO, Audio.CMD_RESET_POSITION), "getActionDataString() returns correct value");
        // Test invalid data
        assertEquals( "", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_AUDIO, 0), "getActionDataString() returns correct value");
        JUnitAppender.assertErrorMessage("Unhandled Audio operation command: 0");
        
        assertEquals( "Slow to Halt", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_TRAIN, Warrant.HALT), "getActionDataString() returns correct value");
        assertEquals( "Resume", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_TRAIN, Warrant.RESUME), "getActionDataString() returns correct value");
        assertEquals( "Move into next block", DefaultConditionalAction.getActionDataString(
            Conditional.Action.CONTROL_TRAIN, Warrant.RETRY_FWD), "getActionDataString() returns correct value");
//        Assert.assertTrue("getActionDataString() returns correct value",
//                "Abort".equals(DefaultConditionalAction.getActionDataString(Conditional.Action.CONTROL_TRAIN, -1)));
        
        // Test invalid type
//        Assert.assertTrue("getActionDataString() returns correct value",
//                "".equals(DefaultConditionalAction.getActionDataString(-1, -1)));
    }

    @Test
    public void testSetActionData() {
        final String deviceName = "3";
        final String actionStr = "5";
        
        ConditionalAction ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);
        assertTrue( ix1.getActionData() == Turnout.THROWN, "getActionData() gets correct value");
        ix1.setActionData(Turnout.CLOSED);
        assertTrue( ix1.getActionData() == Turnout.CLOSED, "getActionData() gets correct value");
        ix1.setActionData("Thrown");
        assertTrue( ix1.getActionData() == Turnout.THROWN, "getActionData() gets correct value");
    }

    @Test
    public void testGetActionBean() {
        ConditionalAction ix1;
        NamedBean bean;
        String deviceName = "3";
        
        // Start with testing the exception handling in getActionBean()
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManagerThrowException();
        JUnitUtil.initLightManagerThrowException();
        JUnitUtil.initMemoryManagerThrowException();
        JUnitUtil.initInternalSensorManagerThrowException();
        JUnitUtil.initSignalHeadManagerThrowException();
        JUnitUtil.initSignalMastManagerThrowException();
        JUnitUtil.initWarrantManagerThrowException();
        JUnitUtil.initOBlockManagerThrowException();
        JUnitUtil.initRouteManagerThrowException();
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_SENSOR, deviceName, 4, "5");
        JUnitAppender.assertErrorMessage("invalid sensor name= \"3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        assertNull( ix1.getBean(), "getActionBean() returns null");
        JUnitAppender.assertErrorMessage("invalid sensor name= \"3\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, deviceName, 4, "5");
        JUnitAppender.assertErrorMessage("invalid turnout name= \"3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        assertNull( ix1.getBean(), "getActionBean() returns null");
        JUnitAppender.assertErrorMessage("invalid turnout name= \"3\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_MEMORY, deviceName, 4, "5");
        JUnitAppender.assertErrorMessage("invalid memory name= \"3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        assertNull( ix1.getBean(), "getActionBean() returns null");
        JUnitAppender.assertErrorMessage("invalid memory name= \"3\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_LIGHT, deviceName, 4, "5");
        JUnitAppender.assertErrorMessage("invalid light name= \"3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        assertNull( ix1.getBean(), "getActionBean() returns null");
        JUnitAppender.assertErrorMessage("invalid light name= \"3\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_SIGNAL_APPEARANCE, "IH1", 4, "5");
        JUnitAppender.assertErrorMessage("invalid signal head name= \"IH1\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        assertNull( ix1.getBean(), "getActionBean() returns null");
        JUnitAppender.assertErrorMessage("invalid signal head name= \"IH1\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_SIGNALMAST_HELD, "IF$shsm:AAR-1946:CPL(IH1)", 4, "5");
        JUnitAppender.assertErrorMessage("invalid signal mast name= \"IF$shsm:AAR-1946:CPL(IH1)\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        assertNull( ix1.getBean(), "getActionBean() returns null");
        JUnitAppender.assertErrorMessage("invalid signal mast name= \"IF$shsm:AAR-1946:CPL(IH1)\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.MANUAL_RUN_WARRANT, "IW3", 4, "5");
        JUnitAppender.assertErrorMessage("invalid Warrant name= \"IW3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        assertNull( ix1.getBean(), "getActionBean() returns null");
        JUnitAppender.assertErrorMessage("invalid Warrant name= \"IW3\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_BLOCK_VALUE, "OB3", 4, "5");
        JUnitAppender.assertErrorMessage("invalid OBlock name= \"OB3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        assertNull( ix1.getBean(), "getActionBean() returns null");
        JUnitAppender.assertErrorMessage("invalid OBlock name= \"OB3\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.TRIGGER_ROUTE, deviceName, 4, "5");
        JUnitAppender.assertErrorMessage("invalid Route name= \"3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        assertNull( ix1.getBean(), "getActionBean() returns null");
        JUnitAppender.assertErrorMessage("invalid Route name= \"3\" in conditional action");
        
        
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initWarrantManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initRouteManager();
        JUnitUtil.initIdTagManager();
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_SENSOR, deviceName, 4, "5");
        bean = InstanceManager.getDefault(SensorManager.class).provideSensor(deviceName);
        assertEquals( bean, ix1.getBean(), "getActionBean() returns correct bean");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, deviceName, 4, "5");
        bean = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(deviceName);
        assertEquals( bean, ix1.getBean(), "getActionBean() returns correct bean");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_MEMORY, deviceName, 4, "5");
        bean = InstanceManager.getDefault(MemoryManager.class).provideMemory(deviceName);
        assertEquals( bean, ix1.getBean(), "getActionBean() returns correct bean");
        
        bean = InstanceManager.getDefault(LightManager.class).provideLight(deviceName);
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_LIGHT, bean.getSystemName(), 4, "5");
        assertEquals( bean, ix1.getBean(), "getActionBean() returns correct bean");
        
        // Note that the signal head IH1 created here are also used to test the signal mast.
        SignalHead signalHead = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHead);
        bean = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH1");
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_SIGNAL_APPEARANCE, "IH1", 4, "5");
        assertEquals( bean, ix1.getBean(), "getActionBean() returns correct bean");
        
        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        bean = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_SIGNALMAST_HELD, "IF$shsm:AAR-1946:CPL(IH1)", 4, "5");
        assertEquals( bean, ix1.getBean(), "getActionBean() returns correct bean");
        
        bean = InstanceManager.getDefault(WarrantManager.class).provideWarrant("IW3");
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.MANUAL_RUN_WARRANT, "IW3", 4, "5");
        assertEquals( bean, ix1.getBean(), "getActionBean() returns correct bean");
        
        bean = InstanceManager.getDefault(OBlockManager.class).provideOBlock("OB3");
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_BLOCK_VALUE, "OB3", 4, "5");
        assertEquals( bean, ix1.getBean(), "getActionBean() returns correct bean");
        
        bean = InstanceManager.getDefault(RouteManager.class).newRoute(deviceName);
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.TRIGGER_ROUTE, deviceName, 4, "5");
        assertEquals( bean, ix1.getBean(), "getActionBean() returns correct bean");
    }

    @Test
    public void testDescription() {
        final String deviceName = "3";
        final String actionStr = "5";
        
        ConditionalAction ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);
        
        ix1.setType(Conditional.Action.CANCEL_TURNOUT_TIMERS);
        ix1.setActionData(Turnout.INDIRECT);
        assertEquals( "When Triggered True, Cancel Timers for Turnout, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Cancel Timers for Turnout, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_SIGNAL_HELD);
        ix1.setActionData(SignalHead.YELLOW);
        assertEquals( "When Triggered True, Set Signal Head Held, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Signal Head Held, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.CLEAR_SIGNAL_HELD);
        ix1.setActionData(SignalHead.YELLOW);
        assertEquals( "When Triggered True, Clear Signal Head Held, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Clear Signal Head Held, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_SIGNAL_DARK);
        ix1.setActionData(SignalHead.YELLOW);
        assertEquals( "When Triggered True, Set Signal Head Dark, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Signal Head Dark, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_SIGNAL_LIT);
        ix1.setActionData(SignalHead.YELLOW);
        assertEquals( "When Triggered True, Set Signal Head Lit, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Signal Head Lit, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.TRIGGER_ROUTE);
        ix1.setActionData(Route.ONTHROWN);
        assertEquals( "When Triggered True, Trigger Route, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Trigger Route, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.CANCEL_SENSOR_TIMERS);
        ix1.setActionData(Sensor.INACTIVE);
        assertEquals( "When Triggered True, Cancel Timers for Sensor, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Cancel Timers for Sensor, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_MEMORY);
        ix1.setActionData(Memory.INCONSISTENT);
        assertEquals( "When Triggered True, Set Memory, \"3\". to 5.",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Memory, \"3\". to 5.",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.ENABLE_LOGIX);
        ix1.setActionData(Logix.LISTENER_TYPE_CONDITIONAL);
        assertEquals( "When Triggered True, Enable Logix, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Enable Logix, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.DISABLE_LOGIX);
        ix1.setActionData(Logix.LISTENER_TYPE_CONDITIONAL);
        assertEquals( "When Triggered True, Disable Logix, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Disable Logix, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.COPY_MEMORY);
        ix1.setActionData(Memory.INCONSISTENT);
        assertEquals( "When Triggered True, Copy Memory To Memory, \"3\". to 5.",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Copy Memory To Memory, \"3\". to 5.",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_LIGHT_INTENSITY);
        ix1.setActionData(Light.TIMED_ON_CONTROL);
        assertEquals( "When Triggered True, Set Light Intensity, \"3\". to 5. to 4.",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Light Intensity, \"3\". to 5. to 4.",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_LIGHT_TRANSITION_TIME);
        ix1.setActionData(Light.TIMED_ON_CONTROL);
        assertEquals( "When Triggered True, Set Light Transition Time, \"3\". to 5. to 4.",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Light Transition Time, \"3\". to 5. to 4.",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.ALLOCATE_WARRANT_ROUTE);
        ix1.setActionData(Warrant.RETRY_FWD);
        assertEquals( "When Triggered True, Allocate Warrant Route, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Allocate Warrant Route, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.DEALLOCATE_WARRANT_ROUTE);
        ix1.setActionData(Warrant.RETRY_FWD);
        assertEquals( "When Triggered True, Deallocate Warrant, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Deallocate Warrant, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_SIGNALMAST_HELD);
        ix1.setActionData(SignalMast.INCONSISTENT);
        assertEquals( "When Triggered True, Set Signal Mast Held, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Signal Mast Held, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.CLEAR_SIGNALMAST_HELD);
        ix1.setActionData(SignalMast.INCONSISTENT);
        assertEquals( "When Triggered True, Clear Signal Mast Held, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Clear Signal Mast Held, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_SIGNALMAST_DARK);
        ix1.setActionData(SignalMast.INCONSISTENT);
        assertEquals( "When Triggered True, Set Signal Mast Dark, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Signal Mast Dark, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_SIGNALMAST_LIT);
        ix1.setActionData(SignalMast.INCONSISTENT);
        assertEquals( "When Triggered True, Clear Signal Mast Dark, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Clear Signal Mast Dark, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_BLOCK_ERROR);
        ix1.setActionData(Block.INCONSISTENT);
        assertEquals( "When Triggered True, Set Block Error, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Block Error, \"3\".",
             ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.CLEAR_BLOCK_ERROR);
        ix1.setActionData(Block.INCONSISTENT);
        assertEquals( "When Triggered True, Clear Block Error, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Clear Block Error, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.DEALLOCATE_BLOCK);
        ix1.setActionData(Block.INCONSISTENT);
        assertEquals( "When Triggered True, Deallocate Block, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Deallocate Block, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_BLOCK_OUT_OF_SERVICE);
        ix1.setActionData(Block.INCONSISTENT);
        assertEquals( "When Triggered True, Set Block OutOfService, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Block OutOfService, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_BLOCK_IN_SERVICE);
        ix1.setActionData(Block.INCONSISTENT);
        assertEquals( "When Triggered True, Clear Block OutOfService, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Clear Block OutOfService, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_NXPAIR_ENABLED);
        assertEquals( "When Triggered True, Set NX Pair Enabled, \"null\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set NX Pair Enabled, \"null\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_NXPAIR_DISABLED);
        assertEquals( "When Triggered True, Set NX Pair Disabled, \"null\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set NX Pair Disabled, \"null\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_NXPAIR_SEGMENT);
        assertEquals( "When Triggered True, Set NX Pair Segment Active / Inactive, \"null\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set NX Pair Segment Active / Inactive, \"null\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_ROUTE_TURNOUTS);
        ix1.setActionData(Route.INCONSISTENT);
        assertEquals( "When Triggered True, Set Route Turnouts of Warrant, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Route Turnouts of Warrant, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.AUTO_RUN_WARRANT);
        ix1.setActionData(Warrant.INCONSISTENT);
        assertEquals( "When Triggered True, Auto Run Train of Warrant, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Auto Run Train of Warrant, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.MANUAL_RUN_WARRANT);
        ix1.setActionData(Warrant.INCONSISTENT);
        assertEquals( "When Triggered True, Manually Run Train of Warrant, \"3\".",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Manually Run Train of Warrant, \"3\".",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_SENSOR);
        ix1.setActionData(Sensor.INACTIVE);
        assertEquals( "When Triggered True, Set Sensor, \"3\" to Inactive",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Sensor, \"3\" to Inactive",
            ix1.description(true));
        
        ix1.setType(Conditional.Action.SET_TURNOUT);
        ix1.setActionData(Turnout.THROWN);
        assertEquals( "When Triggered True, Set Turnout, \"3\" to Thrown",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Turnout, \"3\" to Thrown",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_LIGHT);
        ix1.setActionData(Light.OFF);
        assertEquals( "When Triggered True, Set Light, \"3\" to Off",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Light, \"3\" to Off",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.LOCK_TURNOUT);
        ix1.setActionData(Turnout.LOCKED);
        assertEquals( "When Triggered True, Turnout Lock, \"3\" to Lock",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Turnout Lock, \"3\" to Lock",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.RESET_DELAYED_SENSOR);
        ix1.setActionData(Sensor.INACTIVE);
        assertEquals( "When Triggered True, Reset Delayed Set Sensor, \"3\" to Inactive, after 5 seconds.",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Reset Delayed Set Sensor, \"3\" to Inactive, after 5 seconds.",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_SIGNAL_APPEARANCE);
        ix1.setActionData(SignalHead.YELLOW);
        assertEquals( "When Triggered True, Set Signal Head Appearance, \"3\" to Yellow",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Signal Head Appearance, \"3\" to Yellow",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.RESET_DELAYED_TURNOUT);
        ix1.setActionData(Turnout.THROWN);
        assertEquals( "When Triggered True, Reset Delayed Set Turnout, \"3\" to Thrown, after 5 seconds.",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Reset Delayed Set Turnout, \"3\" to Thrown, after 5 seconds.",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.DELAYED_TURNOUT);
        ix1.setActionData(Turnout.THROWN);
        assertEquals( "When Triggered True, Delayed Set Turnout, \"3\" to Thrown, after 5 seconds.",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Delayed Set Turnout, \"3\" to Thrown, after 5 seconds.",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.DELAYED_SENSOR);
        ix1.setActionData(Sensor.INACTIVE);
        assertEquals( "When Triggered True, Delayed Set Sensor, \"3\" to Inactive, after 5 seconds.",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Delayed Set Sensor, \"3\" to Inactive, after 5 seconds.",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.CONTROL_AUDIO);
        ix1.setActionData(Audio.CMD_PLAY);
        assertEquals( "When Triggered True, Control Audio object, \"3\" to Play",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Control Audio object, \"3\" to Play",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.SET_SIGNALMAST_ASPECT);
        ix1.setActionData(SignalMast.INCONSISTENT);
        assertEquals( "When Triggered True, Set Signal Mast Aspect, \"3\" to 5",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Set Signal Mast Aspect, \"3\" to 5",
            ix1.description(true), "description() returns correct value");
        
        ix1.setType(Conditional.Action.CONTROL_TRAIN);
        ix1.setActionData(Warrant.ABORT);
        assertEquals( "When Triggered True, Control Auto Train of Warrant \"3\" to Abort",
            ix1.description(false), "description() returns correct value");
        assertEquals( "On Change To True, Control Auto Train of Warrant \"3\" to Abort",
            ix1.description(true), "description() returns correct value");
        
//        ix1.setType(-1);
//        ix1.setActionData(NamedBean.INCONSISTENT);
//        Assert.assertTrue("description() returns correct value",
//                "When Triggered True, ".equals(ix1.description(false)));
//        JUnitAppender.assertWarnMessage("Unexpected parameter to getActionTypeString(-1)");
//        Assert.assertTrue("description() returns correct value",
//                "On Change To True, ".equals(ix1.description(true)));
//        JUnitAppender.assertWarnMessage("Unexpected parameter to getActionTypeString(-1)");
    }
    

    // from here down is testing infrastructure

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initWarrantManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initRouteManager();
        JUnitUtil.initIdTagManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
