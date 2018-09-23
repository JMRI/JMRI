package jmri.implementation;

import static jmri.Conditional.*;

import jmri.Audio;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.Route;
import jmri.SensorManager;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;
import jmri.TurnoutManager;
import jmri.RouteManager;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Warrant;
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
        final String otherDeviceName = "8";
        final String actionStr = "5";
        
        ConditionalAction ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);
        ConditionalAction ix2 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);

        ConditionalAction ix3 = new DefaultConditionalAction(0, ACTION_SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);
        ConditionalAction ix4 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, 0, deviceName, Turnout.THROWN, actionStr);
        ConditionalAction ix5 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, "0", Turnout.THROWN, actionStr);
        ConditionalAction ix6 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, 0, actionStr);
        ConditionalAction ix7 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, Turnout.THROWN, "0");
        ConditionalAction ix8 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, otherDeviceName, Turnout.THROWN, "0");
        ConditionalAction ix9 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, Turnout.THROWN, "1");

        ConditionalAction ix10 = new DefaultConditionalAction(0, Conditional.ACTION_NONE, null, Turnout.THROWN, actionStr);
        
        Assert.assertFalse(ix1.equals(null));
        
        Assert.assertTrue(ix1.equals(ix1));
        Assert.assertTrue(ix1.equals(ix2));

        Assert.assertTrue(!ix1.equals(ix3));
        Assert.assertTrue(!ix1.equals(ix4));
        Assert.assertTrue(!ix1.equals(ix5));
        Assert.assertTrue(!ix1.equals(ix6));
        Assert.assertTrue(!ix1.equals(ix7));
        Assert.assertTrue(!ix1.equals(ix8));
        Assert.assertTrue(!ix1.equals(ix9));

        // Test equal with different class
        Assert.assertTrue(!ix1.equals(new Object()));
        
        // Test deviceName == null
        Assert.assertTrue(!ix1.equals(ix10));
        Assert.assertTrue(!ix10.equals(ix1));
        
        Assert.assertTrue(ix1.hashCode() == ix2.hashCode());
    }
    
    @Test
    public void testSetType() {
        final String deviceName = "3";
        final String actionStr = "5";
        
        ConditionalAction ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);
        
        ix1.setType("None");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_NONE);
        
        ix1.setType("Set Turnout");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_TURNOUT);
        
        ix1.setType("Set Signal Head Appearance");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_SIGNAL_APPEARANCE);
        
        ix1.setType("Set Signal Head Held");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_SIGNAL_HELD);
        
        ix1.setType("Clear Signal Head Held");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_CLEAR_SIGNAL_HELD);
        
        ix1.setType("Set Signal Head Dark");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_SIGNAL_DARK);
        
        ix1.setType("Set Signal Head Lit");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_SIGNAL_LIT);
        
        ix1.setType("Trigger Route");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_TRIGGER_ROUTE);
        
        ix1.setType("Set Sensor");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_SENSOR);
        
        ix1.setType("Delayed Set Sensor");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_DELAYED_SENSOR);
        
        ix1.setType("Set Light");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_LIGHT);
        
        ix1.setType("Set Memory");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_MEMORY);
        
        ix1.setType("Enable Logix");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_ENABLE_LOGIX);
        
        ix1.setType("Disable Logix");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_DISABLE_LOGIX);
        
        ix1.setType("Play Sound File");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_PLAY_SOUND);
        
        ix1.setType("Run Script");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_RUN_SCRIPT);
        
        ix1.setType("Delayed Set Turnout");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_DELAYED_TURNOUT);
        
        ix1.setType("Turnout Lock");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_LOCK_TURNOUT);
        
        ix1.setType("Reset Delayed Set Sensor");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_RESET_DELAYED_SENSOR);
        
        ix1.setType("Cancel Timers for Sensor");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_CANCEL_SENSOR_TIMERS);
        
        ix1.setType("Reset Delayed Set Turnout");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_RESET_DELAYED_TURNOUT);
        
        ix1.setType("Cancel Timers for Turnout");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_CANCEL_TURNOUT_TIMERS);
        
        ix1.setType("Set Fast Clock Time");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_FAST_CLOCK_TIME);
        
        ix1.setType("Start Fast Clock");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_START_FAST_CLOCK);
        
        ix1.setType("Stop Fast Clock");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_STOP_FAST_CLOCK);
        
        ix1.setType("Copy Memory To Memory");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_COPY_MEMORY);
        
        ix1.setType("Set Light Intensity");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_LIGHT_INTENSITY);
        
        ix1.setType("Set Light Transition Time");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_LIGHT_TRANSITION_TIME);
        
        ix1.setType("Control Audio object");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_CONTROL_AUDIO);
        
        ix1.setType("Execute Jython Command");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_JYTHON_COMMAND);
        
        ix1.setType("Allocate Warrant Route");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_ALLOCATE_WARRANT_ROUTE);
        
        ix1.setType("Deallocate Warrant");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_DEALLOCATE_WARRANT_ROUTE);
        
        ix1.setType("Set Route Turnouts");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_ROUTE_TURNOUTS);
        
        ix1.setType("Auto Run Train");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_AUTO_RUN_WARRANT);
        
        ix1.setType("Manually Run Train");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_MANUAL_RUN_WARRANT);
        
        ix1.setType("Control Auto Train");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_CONTROL_TRAIN);
        
        ix1.setType("Set Train ID");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_TRAIN_ID);
        
        ix1.setType("Set Train Name");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_TRAIN_NAME);
        
        ix1.setType("Set Signal Mast Aspect");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_SIGNALMAST_ASPECT);
        
        ix1.setType("Set Throttle Factor");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_THROTTLE_FACTOR);
        
        ix1.setType("Set Signal Mast Held");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_SIGNALMAST_HELD);
        
        ix1.setType("Clear Signal Mast Held");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_CLEAR_SIGNALMAST_HELD);
        
        ix1.setType("Set Signal Mast Dark");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_SIGNALMAST_DARK);
        
        ix1.setType("Clear Signal Mast Dark");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_SIGNALMAST_LIT);
        
        ix1.setType("Set Block Value");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_BLOCK_VALUE);
        
        ix1.setType("Set Block Error");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_BLOCK_ERROR);
        
        ix1.setType("Clear Block Error");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_CLEAR_BLOCK_ERROR);
        
        ix1.setType("Deallocate Block");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_DEALLOCATE_BLOCK);
        
        ix1.setType("Set Block OutOfService");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_BLOCK_OUT_OF_SERVICE);
        
        ix1.setType("Clear Block OutOfService");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_BLOCK_IN_SERVICE);
        
        ix1.setType("Set NX Pair Enabled");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_NXPAIR_ENABLED);
        
        ix1.setType("Set NX Pair Disabled");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_NXPAIR_DISABLED);
        
        ix1.setType("Set NX Pair Segment Active / Inactive");
        Assert.assertTrue("setType() sets correct value", ix1.getType() == ACTION_SET_NXPAIR_SEGMENT);
        
        ix1.setType("This is a bad string");
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected parameter to stringToActionType(This is a bad string)");
    }
    
    @Test
    public void testStringToActionData() {
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Closed") == Turnout.CLOSED);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Thrown") == Turnout.THROWN);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Active") == Sensor.ACTIVE);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Inactive") == Sensor.INACTIVE);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("On") == Light.ON);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Off") == Light.OFF);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Unlock") == Turnout.UNLOCKED);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Lock") == Turnout.LOCKED);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Red") == SignalHead.RED);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Yellow") == SignalHead.YELLOW);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Green") == SignalHead.GREEN);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Dark") == SignalHead.DARK);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Flashing Red") == SignalHead.FLASHRED);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Flashing Yellow") == SignalHead.FLASHYELLOW);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Flashing Green") == SignalHead.FLASHGREEN);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Lunar") == SignalHead.LUNAR);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Flashing Lunar") == SignalHead.FLASHLUNAR);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Play") == Audio.CMD_PLAY);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Stop") == Audio.CMD_STOP);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Toggle Play") == Audio.CMD_PLAY_TOGGLE);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Pause") == Audio.CMD_PAUSE);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Resume") == Audio.CMD_RESUME);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Toggle Pause") == Audio.CMD_PAUSE_TOGGLE);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Rewind") == Audio.CMD_REWIND);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Fade-in") == Audio.CMD_FADE_IN);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Fade-out") == Audio.CMD_FADE_OUT);
        
        Assert.assertTrue("stringToActionData() returns correct value",
                DefaultConditionalAction.stringToActionData("Reset Position") == Audio.CMD_RESET_POSITION);
        
        DefaultConditionalAction.stringToActionData("This is a bad string");
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected parameter to stringToActionData(This is a bad string)");
    }
    
    @Test
    public void testGetActionDataString() {
        DefaultConditionalAction.getActionDataString(0, 0);
        
        Assert.assertTrue("getActionDataString() returns correct value",
                "Closed".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_TURNOUT, Turnout.CLOSED)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Thrown".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_TURNOUT, Turnout.THROWN)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Toggle".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_TURNOUT, Route.TOGGLE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Closed".equals(DefaultConditionalAction.getActionDataString(ACTION_DELAYED_TURNOUT, Turnout.CLOSED)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Thrown".equals(DefaultConditionalAction.getActionDataString(ACTION_DELAYED_TURNOUT, Turnout.THROWN)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Toggle".equals(DefaultConditionalAction.getActionDataString(ACTION_DELAYED_TURNOUT, Route.TOGGLE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Closed".equals(DefaultConditionalAction.getActionDataString(ACTION_RESET_DELAYED_TURNOUT, Turnout.CLOSED)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Thrown".equals(DefaultConditionalAction.getActionDataString(ACTION_RESET_DELAYED_TURNOUT, Turnout.THROWN)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Toggle".equals(DefaultConditionalAction.getActionDataString(ACTION_RESET_DELAYED_TURNOUT, Route.TOGGLE)));
        // Test invalid data
        Assert.assertTrue("getActionDataString() returns correct value",
                "".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_TURNOUT, 0)));
        
        Assert.assertTrue("getActionDataString() returns correct value",
                "Red".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SIGNAL_APPEARANCE, SignalHead.RED)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Yellow".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SIGNAL_APPEARANCE, SignalHead.YELLOW)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Green".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SIGNAL_APPEARANCE, SignalHead.GREEN)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Dark".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SIGNAL_APPEARANCE, SignalHead.DARK)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Flashing Red".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SIGNAL_APPEARANCE, SignalHead.FLASHRED)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Flashing Yellow".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SIGNAL_APPEARANCE, SignalHead.FLASHYELLOW)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Flashing Green".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SIGNAL_APPEARANCE, SignalHead.FLASHGREEN)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Lunar".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SIGNAL_APPEARANCE, SignalHead.LUNAR)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Flashing Lunar".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SIGNAL_APPEARANCE, SignalHead.FLASHLUNAR)));
        // Test invalid data
        Assert.assertTrue("getActionDataString() returns correct value",
                "".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SIGNAL_APPEARANCE, -1)));
        
        Assert.assertTrue("getActionDataString() returns correct value",
                "Active".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SENSOR, Sensor.ACTIVE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Inactive".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SENSOR, Sensor.INACTIVE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Toggle".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SENSOR, Route.TOGGLE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Active".equals(DefaultConditionalAction.getActionDataString(ACTION_DELAYED_SENSOR, Sensor.ACTIVE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Inactive".equals(DefaultConditionalAction.getActionDataString(ACTION_DELAYED_SENSOR, Sensor.INACTIVE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Toggle".equals(DefaultConditionalAction.getActionDataString(ACTION_DELAYED_SENSOR, Route.TOGGLE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Active".equals(DefaultConditionalAction.getActionDataString(ACTION_RESET_DELAYED_SENSOR, Sensor.ACTIVE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Inactive".equals(DefaultConditionalAction.getActionDataString(ACTION_RESET_DELAYED_SENSOR, Sensor.INACTIVE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Toggle".equals(DefaultConditionalAction.getActionDataString(ACTION_RESET_DELAYED_SENSOR, Route.TOGGLE)));
        // Test invalid data
        Assert.assertTrue("getActionDataString() returns correct value",
                "".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_SENSOR, 0)));
        
        Assert.assertTrue("getActionDataString() returns correct value",
                "On".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_LIGHT, Light.ON)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Off".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_LIGHT, Light.OFF)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Toggle".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_LIGHT, Route.TOGGLE)));
        // Test invalid data
        Assert.assertTrue("getActionDataString() returns correct value",
                "".equals(DefaultConditionalAction.getActionDataString(ACTION_SET_LIGHT, 0)));
        
        Assert.assertTrue("getActionDataString() returns correct value",
                "Unlock".equals(DefaultConditionalAction.getActionDataString(ACTION_LOCK_TURNOUT, Turnout.UNLOCKED)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Lock".equals(DefaultConditionalAction.getActionDataString(ACTION_LOCK_TURNOUT, Turnout.LOCKED)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Toggle".equals(DefaultConditionalAction.getActionDataString(ACTION_LOCK_TURNOUT, Route.TOGGLE)));
        // Test invalid data
        Assert.assertTrue("getActionDataString() returns correct value",
                "".equals(DefaultConditionalAction.getActionDataString(ACTION_LOCK_TURNOUT, -1)));
        
        Assert.assertTrue("getActionDataString() returns correct value",
                "Play".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_AUDIO, Audio.CMD_PLAY)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Stop".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_AUDIO, Audio.CMD_STOP)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Toggle Play".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_AUDIO, Audio.CMD_PLAY_TOGGLE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Pause".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_AUDIO, Audio.CMD_PAUSE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Resume".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_AUDIO, Audio.CMD_RESUME)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Toggle Pause".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_AUDIO, Audio.CMD_PAUSE_TOGGLE)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Rewind".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_AUDIO, Audio.CMD_REWIND)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Fade-in".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_AUDIO, Audio.CMD_FADE_IN)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Fade-out".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_AUDIO, Audio.CMD_FADE_OUT)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Reset Position".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_AUDIO, Audio.CMD_RESET_POSITION)));
        // Test invalid data
        Assert.assertTrue("getActionDataString() returns correct value",
                "".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_AUDIO, 0)));
        jmri.util.JUnitAppender.assertWarnMessage("Unhandled Audio operation command: 0");
        
        Assert.assertTrue("getActionDataString() returns correct value",
                "Halt".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_TRAIN, Warrant.HALT)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Resume".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_TRAIN, Warrant.RESUME)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Abort".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_TRAIN, Warrant.ABORT)));
        Assert.assertTrue("getActionDataString() returns correct value",
                "Abort".equals(DefaultConditionalAction.getActionDataString(ACTION_CONTROL_TRAIN, -1)));
        
        // Test invalid type
        Assert.assertTrue("getActionDataString() returns correct value",
                "".equals(DefaultConditionalAction.getActionDataString(-1, -1)));
    }
    
    @Test
    public void testSetActionData() {
        final String deviceName = "3";
        final String actionStr = "5";
        
        ConditionalAction ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, Turnout.THROWN, actionStr);
        Assert.assertTrue("getActionData() gets correct value", ix1.getActionData() == Turnout.THROWN);
        ix1.setActionData(Turnout.CLOSED);
        Assert.assertTrue("getActionData() gets correct value", ix1.getActionData() == Turnout.CLOSED);
        ix1.setActionData("Thrown");
        Assert.assertTrue("getActionData() gets correct value", ix1.getActionData() == Turnout.THROWN);
    }
    
    @Test
    public void testGetActionBean() {
        ConditionalAction ix1;
        NamedBean bean;
        String deviceName = "3";
        
        // Start with testing the exception handling in getActionBean()
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManagerThrowException();
        jmri.util.JUnitUtil.initLightManagerThrowException();
        jmri.util.JUnitUtil.initMemoryManagerThrowException();
        jmri.util.JUnitUtil.initInternalSensorManagerThrowException();
        jmri.util.JUnitUtil.initSignalHeadManagerThrowException();
        jmri.util.JUnitUtil.initSignalMastManagerThrowException();
        jmri.util.JUnitUtil.initWarrantManagerThrowException();
        jmri.util.JUnitUtil.initOBlockManagerThrowException();
        jmri.util.JUnitUtil.initRouteManagerThrowException();
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_SENSOR, deviceName, 4, "5");
        jmri.util.JUnitAppender.assertErrorMessage("invalid sensor name= \"3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        Assert.assertTrue("getActionBean() returns null", ix1.getBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid sensor name= \"3\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_TURNOUT, deviceName, 4, "5");
        jmri.util.JUnitAppender.assertErrorMessage("invalid turnout name= \"3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        Assert.assertTrue("getActionBean() returns null", ix1.getBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid turnout name= \"3\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_MEMORY, deviceName, 4, "5");
        jmri.util.JUnitAppender.assertErrorMessage("invalid memory name= \"3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        Assert.assertTrue("getActionBean() returns null", ix1.getBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid memory name= \"3\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_LIGHT, deviceName, 4, "5");
        jmri.util.JUnitAppender.assertErrorMessage("invalid light name= \"3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        Assert.assertTrue("getActionBean() returns null", ix1.getBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid light name= \"3\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_SIGNAL_APPEARANCE, "IH1", 4, "5");
        jmri.util.JUnitAppender.assertErrorMessage("invalid signal head name= \"IH1\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        Assert.assertTrue("getActionBean() returns null", ix1.getBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid signal head name= \"IH1\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_SIGNALMAST_HELD, "IF$shsm:AAR-1946:CPL(IH1)", 4, "5");
        jmri.util.JUnitAppender.assertErrorMessage("invalid signal mast name= \"IF$shsm:AAR-1946:CPL(IH1)\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        Assert.assertTrue("getActionBean() returns null", ix1.getBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid signal mast name= \"IF$shsm:AAR-1946:CPL(IH1)\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_MANUAL_RUN_WARRANT, "IW3", 4, "5");
        jmri.util.JUnitAppender.assertErrorMessage("invalid Warrant name= \"IW3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        Assert.assertTrue("getActionBean() returns null", ix1.getBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid Warrant name= \"IW3\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_SET_BLOCK_VALUE, "OB3", 4, "5");
        jmri.util.JUnitAppender.assertErrorMessage("invalid OBlock name= \"OB3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        Assert.assertTrue("getActionBean() returns null", ix1.getBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid OBlock name= \"OB3\" in conditional action");
        
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_TRIGGER_ROUTE, deviceName, 4, "5");
        jmri.util.JUnitAppender.assertErrorMessage("invalid Route name= \"3\" in conditional action");
        // getBean() tries to set the bean if bean == null. This generates a new error message.
        Assert.assertTrue("getActionBean() returns null", ix1.getBean() == null);
        jmri.util.JUnitAppender.assertErrorMessage("invalid Route name= \"3\" in conditional action");
        
        
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
        
        bean = InstanceManager.getDefault(RouteManager.class).newRoute(deviceName);
        ix1 = new DefaultConditionalAction(ACTION_OPTION_ON_CHANGE_TO_TRUE, ACTION_TRIGGER_ROUTE, deviceName, 4, "5");
        Assert.assertTrue("getActionBean() returns correct bean", ix1.getBean().equals(bean));
    }
    

    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
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
