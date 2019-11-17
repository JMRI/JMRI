package jmri.implementation;

import jmri.InstanceManager;
import jmri.Light;
import jmri.Sensor;
import jmri.util.JUnitAppender;
import jmri.Timebase;
import jmri.TimebaseRateException;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
/**
 * Tests for the LightControl class
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class LightControlTest {

    @Test
    public void testCtor() {
        LightControl l = new LightControl();
        Assert.assertNotNull("LightControl not null", l);
    }

    @Test
    public void testCLighttor() {
        Light o = new AbstractLight("IL1", "test light") {
        };
        LightControl l = new LightControl(o);
        Assert.assertNotNull("LightControl not null", l);
    }

    @Test
    public void testLightControlCopyCtor() {
        LightControl l = new LightControl();
        LightControl copyOfl = new LightControl(l);
        Assert.assertNotNull("LightControl Copy not null", copyOfl);
    }

    @Test
    @SuppressWarnings("unlikely-arg-type") // String seems to be unrelated to LightControl
    public void testEquals() {
        Light o = new AbstractLight("IL1", "test light") {
        };
        LightControl l1 = new LightControl(o);

        Assert.assertFalse(l1.equals(null));
        Assert.assertTrue(l1.equals(l1));
        Assert.assertFalse(l1.equals(""));

        LightControl l2 = new LightControl(o);
        Assert.assertTrue(l1.equals(l2));

        l1.setControlType(999);
        Assert.assertFalse(l1.equals(l2));
        l2.setControlType(999);
        Assert.assertTrue(l1.equals(l2));
        JUnitAppender.assertWarnMessage("Unexpected _controlType = 999");

        l1.setControlType(Light.SENSOR_CONTROL);
        Assert.assertFalse(l1.equals(l2));
        l2.setControlType(Light.SENSOR_CONTROL);
        Assert.assertTrue(l1.equals(l2));

        l1.setControlSensorName("S2");
        Assert.assertFalse(l1.equals(l2));
        l2.setControlSensorName("S2");
        Assert.assertTrue(l1.equals(l2));

        l1.setControlSensorSense(Sensor.ACTIVE);
        Assert.assertTrue(l1.equals(l2)); // default is ACTIVE

        l1.setControlSensorSense(Sensor.INACTIVE);
        Assert.assertFalse(l1.equals(l2));
        l2.setControlSensorSense(Sensor.INACTIVE);
        Assert.assertTrue(l1.equals(l2));

        l1.setControlType(Light.FAST_CLOCK_CONTROL);
        Assert.assertFalse(l1.equals(l2));
        l2.setControlType(Light.FAST_CLOCK_CONTROL);
        Assert.assertTrue(l1.equals(l2));

        l1.setFastClockControlSchedule(0, 0, 0, 0); // onHr, OnMin, OffHr, OffMin  default
        Assert.assertTrue(l1.equals(l2));

        l1.setFastClockControlSchedule(1, 0, 0, 0); // onHr, OnMin, OffHr, OffMin
        Assert.assertFalse(l1.equals(l2));
        l1.setFastClockControlSchedule(0, 1, 0, 0); // onHr, OnMin, OffHr, OffMin
        Assert.assertFalse(l1.equals(l2));
        l1.setFastClockControlSchedule(0, 0, 1, 0); // onHr, OnMin, OffHr, OffMin
        Assert.assertFalse(l1.equals(l2));
        l1.setFastClockControlSchedule(0, 0, 0, 1); // onHr, OnMin, OffHr, OffMin
        Assert.assertFalse(l1.equals(l2));

        l1.setControlType(Light.TURNOUT_STATUS_CONTROL);
        Assert.assertFalse(l1.equals(l2));
        l2.setControlType(Light.TURNOUT_STATUS_CONTROL);
        Assert.assertTrue(l1.equals(l2));

        l1.setControlTurnout("T1");
        Assert.assertFalse(l1.equals(l2));
        l2.setControlTurnout("T1");
        Assert.assertTrue(l1.equals(l2));

        l1.setControlTurnoutState(Turnout.CLOSED); // default CLOSED
        Assert.assertTrue(l1.equals(l2));

        l1.setControlTurnoutState(Turnout.THROWN);
        Assert.assertFalse(l1.equals(l2));
        l2.setControlTurnoutState(Turnout.THROWN);
        Assert.assertTrue(l1.equals(l2));

        l1.setControlType(Light.TIMED_ON_CONTROL);
        Assert.assertFalse(l1.equals(l2));
        l2.setControlType(Light.TIMED_ON_CONTROL);
        Assert.assertTrue(l1.equals(l2));

        l1.setControlTimedOnSensorName("S1");
        Assert.assertFalse(l1.equals(l2));
        l2.setControlTimedOnSensorName("S1");
        Assert.assertTrue(l1.equals(l2));

        l1.setTimedOnDuration(77);
        Assert.assertFalse(l1.equals(l2));
        l2.setTimedOnDuration(77);
        Assert.assertTrue(l1.equals(l2));

        l1.setControlType(Light.TWO_SENSOR_CONTROL);
        Assert.assertFalse(l1.equals(l2));
        l2.setControlType(Light.TWO_SENSOR_CONTROL);
        Assert.assertTrue(l1.equals(l2));

        l1.setControlSensorName("S1");
        Assert.assertFalse(l1.equals(l2));
        l2.setControlSensorName("S1");
        Assert.assertTrue(l1.equals(l2));

        l1.setControlSensor2Name("S2");
        Assert.assertFalse(l1.equals(l2));
        l2.setControlSensor2Name("S2");
        Assert.assertTrue(l1.equals(l2));

        l1.setControlSensorSense(Sensor.ACTIVE);
        Assert.assertFalse(l1.equals(l2));
        l2.setControlSensorSense(Sensor.ACTIVE);
        Assert.assertTrue(l1.equals(l2));

        Assert.assertNotNull("Has Hashcode", l1.hashCode());

    }

    @Test
    public void testSetGetNames() {
        // used while editing the control with no Sensors / turnouts etc. attached
        LightControl t = new LightControl();
        t.setControlSensorName("MySensor");
        Assert.assertEquals("Same Name", "MySensor", t.getControlSensorName());

        t.setControlTimedOnSensorName("Shirley");
        Assert.assertEquals("Same Name", "Shirley", t.getControlTimedOnSensorName());

        t.setControlSensor2Name("DownMain7");
        Assert.assertEquals("Same Name", "DownMain7", t.getControlSensor2Name());
    }

    @Test
    public void testInvalidControlType() {
        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        lc = new LightControl(l);
        lc.activateLightControl();
        JUnitAppender.assertErrorMessage("Unexpected control type when activating Light: ILL1");
    }

    @Test
    public void testActivateNoLight() {
        lc = new LightControl();
        lc.activateLightControl();
        JUnitAppender.assertErrorMessage("No Parent Light when activating LightControl");
    }

    @Test
    public void testSingleSensorFollower() throws jmri.JmriException {

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        Sensor s = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("S2");

        int startListeners = s.getPropertyChangeListeners().length;

        lc = new LightControl(l);
        lc.setControlType(Light.SENSOR_CONTROL);
        lc.setControlSensorName("S2");
        lc.setControlSensorSense(Sensor.ACTIVE);

        l.addLightControl(lc);
        l.activateLight();

        Assert.assertEquals("+1 listener", startListeners + 1, s.getPropertyChangeListeners().length);

        Assert.assertEquals("Sensor unknown state", Sensor.UNKNOWN, s.getState());
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState()); // lights are OFF by default

        s.setState(Sensor.ON);
        Assert.assertEquals("ON state", Light.ON, l.getState());

        s.setState(Sensor.OFF);
        Assert.assertEquals("OFF state", Light.OFF, l.getState());

        s.setState(Sensor.ON);
        Assert.assertEquals("ON state", Light.ON, l.getState());

        l.deactivateLight();
        Assert.assertEquals("releases listener", startListeners, s.getPropertyChangeListeners().length);

        lc.setControlSensorSense(Sensor.INACTIVE);
        Assert.assertEquals("ON state", Light.ON, l.getState());

        l.activateLight();
        Assert.assertEquals("OFF state", Light.OFF, l.getState());

        s.setState(Sensor.OFF);
        Assert.assertEquals("ON state", Light.ON, l.getState());

        s.setState(Sensor.ON);
        Assert.assertEquals("OFF state", Light.OFF, l.getState());

        l.setEnabled(false);
        s.setState(Sensor.OFF);
        Assert.assertEquals("does not change", Light.OFF, l.getState());

        l.deactivateLight();
        l.dispose();

    }

    @Test
    public void testNoSensor() {

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        lc = new LightControl(l);
        lc.setControlType(Light.SENSOR_CONTROL);

        l.addLightControl(lc);
        l.activateLight();
        JUnitAppender.assertErrorMessage("Light ILL1 is linked to a Sensor that does not exist:");

    }

    @Test
    public void testNoTurnout() {

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        lc = new LightControl(l);
        lc.setControlType(Light.TURNOUT_STATUS_CONTROL);

        l.addLightControl(lc);
        l.activateLight();
        JUnitAppender.assertErrorMessageStartsWith("Invalid system name for Turnout: System name must start with \"IT\".");
        JUnitAppender.assertErrorMessageStartsWith("Light ILL1 is linked to a Turnout that does not exist");

        lc.setControlTurnoutState(999);
        JUnitAppender.assertErrorMessageStartsWith("Incorrect Turnout State Set");

    }

    @Test
    public void testTurnoutFollower() throws jmri.JmriException {

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        Turnout t = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("T1");

        int startListeners = t.getPropertyChangeListeners().length;

        lc = new LightControl(l);
        lc.setControlType(Light.TURNOUT_STATUS_CONTROL);
        lc.setControlTurnout("T1");
        lc.setControlTurnoutState(Turnout.THROWN);

        l.addLightControl(lc);
        l.activateLight();

        Assert.assertEquals("+1 listener", startListeners + 1, t.getPropertyChangeListeners().length);

        Assert.assertEquals("Turnout unknown state", Turnout.UNKNOWN, t.getState());
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState()); // lights are OFF by default

        t.setState(Turnout.THROWN);
        Assert.assertEquals("Light ON state", Light.ON, l.getState());

        t.setState(Turnout.CLOSED);
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState());

        t.setState(Turnout.THROWN);
        Assert.assertEquals("Light ON state", Light.ON, l.getState());

        t.setState(Turnout.UNKNOWN);
        Assert.assertEquals("Light ON state", Light.ON, l.getState());

        t.setState(Turnout.CLOSED);
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState());

        l.deactivateLight();
        Assert.assertEquals("releases turnout listener", startListeners, t.getPropertyChangeListeners().length);

        lc.setControlTurnoutState(Turnout.CLOSED);

        l.activateLight();
        Assert.assertEquals("Activation polls the Turnout", Light.ON, l.getState());

        t.setState(Turnout.THROWN);
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState());

        t.setState(Turnout.CLOSED);
        Assert.assertEquals("Light ON state", Light.ON, l.getState());

        l.setEnabled(false);
        t.setState(Turnout.THROWN);
        Assert.assertEquals("does not update when light not enabled", Light.ON, l.getState());

        l.deactivateLight();
        l.dispose();

    }

    @Test
    public void testFastClockFollowingOneControl() throws TimebaseRateException {
        Timebase timebase = InstanceManager.getDefault(Timebase.class);
        timebase.setRun(false);
        java.util.Calendar cal = new java.util.GregorianCalendar();
        cal.set(2018, 1, 12, 2, 00, 00); // 02:00:00
        timebase.setTime(cal.getTime());

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");

        Assert.assertEquals("OFF state by default", Light.OFF, l.getState()); // lights are OFF by default
        Assert.assertEquals("enabled by default", true, l.getEnabled()); // lights are enabled by default

        int startListeners = timebase.getMinuteChangeListeners().length;

        lc = new LightControl();
        lc.setParentLight(l);
        lc.setControlType(Light.FAST_CLOCK_CONTROL);
        lc.setFastClockControlSchedule(3, 0, 4, 0); // onHr, OnMin, OffHr, OffMin
        
        Assert.assertTrue("Total On Time",180==lc.getFastClockOnCombined());
        Assert.assertTrue("Total Off Time",240==lc.getFastClockOffCombined());

        l.addLightControl(lc);
        l.activateLight();
        Assert.assertEquals("+1 listener", startListeners + 1, timebase.getMinuteChangeListeners().length);
        // JUnitUtil.waitFor(()->{return l.getState()==Light.OFF;},"Light goes OFF at 02:00");
        Assert.assertEquals("OFF at 02:00 when control 03:00 - 04:00", Light.OFF, l.getState());

        cal.set(2018, 1, 12, 2, 59, 00); // 02:59:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("OFF at 02:59 when control 03:00 - 04:00", Light.OFF, l.getState());

        cal.set(2018, 1, 12, 3, 00, 00); // 03:00:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("ON at 03:00 when control 03:00 - 04:00", Light.ON, l.getState());

        cal.set(2018, 1, 12, 3, 59, 00); // 03:59:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("ON at 03:59 when control 03:00 - 04:00", Light.ON, l.getState());

        cal.set(2018, 1, 12, 4, 00, 00); // 04:00:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("OFF at 04:00 when control 03:00 - 04:00", Light.OFF, l.getState());

        cal.set(2018, 1, 12, 4, 01, 00); // 04:01:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("OFF at 04:01 when control 03:00 - 04:00", Light.OFF, l.getState());

        l.deactivateLight();

        Assert.assertEquals("listener removed", startListeners, timebase.getMinuteChangeListeners().length);

        l.dispose();
    }

    @Test
    public void testFastClockFollowingOneControlStartOn() throws TimebaseRateException {
        Timebase timebase = InstanceManager.getDefault(Timebase.class);
        timebase.setRun(false);
        java.util.Calendar cal = new java.util.GregorianCalendar();
        cal.set(2018, 1, 12, 21, 00, 00); // 21:00:00
        timebase.setTime(cal.getTime());

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        Assert.assertEquals("OFF state by default", Light.OFF, l.getState()); // lights are OFF by default

        lc = new LightControl(l);
        lc.setControlType(Light.FAST_CLOCK_CONTROL);
        lc.setFastClockControlSchedule(18, 0, 7, 0); // onHr, OnMin, OffHr, OffMin

        l.addLightControl(lc);
        l.activateLight();
        Assert.assertEquals("ON when starting at 21:00 control 18:00 - 07:00", Light.ON, l.getState());

        l.setState(Light.OFF);
        Assert.assertEquals("goes OFF when set by something else", Light.OFF, l.getState());

        cal.set(2018, 1, 12, 21, 01, 00); // 21:01:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("goes back ON on next minute update", Light.ON, l.getState());

        l.setEnabled(false);

        cal.set(2018, 1, 12, 7, 59, 00); // 07:59:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("Light still on", Light.ON, l.getState());

        cal.set(2018, 1, 12, 8, 00, 00); // 08:00:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("Light still on as not enabled", Light.ON, l.getState());

        l.setEnabled(true);

        cal.set(2018, 1, 12, 8, 01, 00); // 08:01:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("Light goes off on next update re-enabled", Light.OFF, l.getState());

        l.deactivateLight();
        l.dispose();
    }

    @Test
    public void testFastClockFollowingTwoControls() throws TimebaseRateException {
        Timebase timebase = InstanceManager.getDefault(Timebase.class);
        timebase.setRun(false);
        java.util.Calendar cal = new java.util.GregorianCalendar();
        cal.set(2018, 1, 12, 21, 00, 00); // 21:00:00
        timebase.setTime(cal.getTime());

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        Assert.assertEquals("OFF state by default", Light.OFF, l.getState()); // lights are OFF by default

        lc = new LightControl(l);
        lc.setControlType(Light.FAST_CLOCK_CONTROL);
        lc.setFastClockControlSchedule(3, 0, 4, 0); // onHr, OnMin, OffHr, OffMin

        LightControl lcb = new LightControl(l);
        lcb.setControlType(Light.FAST_CLOCK_CONTROL);
        lcb.setFastClockControlSchedule(5, 0, 6, 0); // onHr, OnMin, OffHr, OffMin

        l.addLightControl(lc);
        l.addLightControl(lcb);
        l.activateLight();
        Assert.assertEquals("OFF starting at 21:00 controls 03:00-04:00, 05:00-06:00", Light.OFF, l.getState());

        // adding the PCL to check that we don't get flickering on the Light
        // ie "normal" amount of PCEvents for a state change.
        // NOT testing the actual PCListeners
        l.addPropertyChangeListener(new ControlListen());

        cal.set(2018, 1, 12, 2, 59, 00); // 02:59:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("still OFF", Light.OFF, l.getState());
        Assert.assertEquals("0 Light PropertyChangeEvents", 0, _listenerkicks);

        cal.set(2018, 1, 12, 3, 00, 00); // 03:00:00
        timebase.setTime(cal.getTime());
        // JUnitUtil.waitFor(()->{return l.getState()==Light.ON;},"Light goes ON at 03:00");
        Assert.assertEquals("goes ON", Light.ON, l.getState());

        // At time of writing there are 2 PCE's on Light state change
        // "TargetIntensity" and "KnownState"
        Assert.assertEquals("2 Light PropertyChangeEvents", 2, _listenerkicks);

        cal.set(2018, 1, 12, 3, 59, 00); // 03:59:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("still ON", Light.ON, l.getState());
        Assert.assertEquals("2 Light PropertyChangeEvents", 2, _listenerkicks);

        cal.set(2018, 1, 12, 4, 00, 00); // 04:00:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("goes OFF", Light.OFF, l.getState());
        Assert.assertEquals("4 Light PropertyChangeEvents", 4, _listenerkicks);

        cal.set(2018, 1, 12, 4, 59, 00); // 04:00:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("still OFF", Light.OFF, l.getState());
        Assert.assertEquals("4 Light PropertyChangeEvents", 4, _listenerkicks);

        cal.set(2018, 1, 12, 5, 00, 00); // 05:00:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("goes ON", Light.ON, l.getState());
        Assert.assertEquals("6 Light PropertyChangeEvents", 6, _listenerkicks);

        cal.set(2018, 1, 12, 5, 59, 00); // 05:59:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("still ON", Light.ON, l.getState());
        Assert.assertEquals("6 Light PropertyChangeEvents", 6, _listenerkicks);

        cal.set(2018, 1, 12, 6, 00, 00); // 06:00:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("goes OFF", Light.OFF, l.getState());
        Assert.assertEquals("8 Light PropertyChangeEvents for 4 actual changes", 8, _listenerkicks);

        l.deactivateLight();
        l.dispose();
    }

    @Test
    public void testFastClockFollowingTwoControlsOverlap() throws TimebaseRateException {
        Timebase timebase = InstanceManager.getDefault(Timebase.class);
        timebase.setRun(false);
        java.util.Calendar cal = new java.util.GregorianCalendar();
        cal.set(2018, 1, 12, 02, 59, 00); // 02:59:00
        timebase.setTime(cal.getTime());

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        Assert.assertEquals("OFF state by default", Light.OFF, l.getState()); // lights are OFF by default

        lc = new LightControl(l);
        lc.setControlType(Light.FAST_CLOCK_CONTROL);
        lc.setFastClockControlSchedule(3, 0, 4, 0); // onHr, OnMin, OffHr, OffMin

        LightControl lcb = new LightControl(l);
        lcb.setControlType(Light.FAST_CLOCK_CONTROL);
        lcb.setFastClockControlSchedule(3, 30, 4, 30); // onHr, OnMin, OffHr, OffMin

        Assert.assertTrue("Total On Time",210==lcb.getFastClockOnCombined());
        Assert.assertTrue("Total Off Time",270==lcb.getFastClockOffCombined());

        l.addLightControl(lc);
        l.addLightControl(lcb);
        l.activateLight();
        Assert.assertEquals("OFF starting at 02:59 controls 03:00-04:00, 03:30-04:30", Light.OFF, l.getState());

        // adding the PCL to check that we don't get flickering on the Light
        // ie "normal" amount of PCEvents for a state change.
        // NOT testing the actual PCListeners
        l.addPropertyChangeListener(new ControlListen());

        cal.set(2018, 1, 12, 3, 00, 00); // 03:00:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("goes ON", Light.ON, l.getState());
        Assert.assertEquals("2 Light PropertyChangeEvents", 2, _listenerkicks);

        cal.set(2018, 1, 12, 3, 29, 00); // 03:29:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("still ON", Light.ON, l.getState());
        Assert.assertEquals("2 Light PropertyChangeEvents", 2, _listenerkicks);

        cal.set(2018, 1, 12, 3, 30, 00); // 03:30:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("still ON", Light.ON, l.getState());
        Assert.assertEquals("2 Light PropertyChangeEvents", 2, _listenerkicks);

        cal.set(2018, 1, 12, 3, 59, 00); // 03:59:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("still ON", Light.ON, l.getState());
        Assert.assertEquals("2 Light PropertyChangeEvents", 2, _listenerkicks);

        cal.set(2018, 1, 12, 4, 00, 00); // 04:00:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("goes OFF", Light.OFF, l.getState());
        Assert.assertEquals("4 Light PropertyChangeEvents for 2 actual changes", 4, _listenerkicks);

        cal.set(2018, 1, 12, 4, 29, 00); // 04:29:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("still OFF", Light.OFF, l.getState());
        Assert.assertEquals("4 Light PropertyChangeEvents for 2 actual changes", 4, _listenerkicks);

        cal.set(2018, 1, 12, 4, 30, 00); // 06:00:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("still OFF", Light.OFF, l.getState());
        Assert.assertEquals("4 Light PropertyChangeEvents for 2 actual changes", 4, _listenerkicks);

        cal.set(2018, 1, 12, 4, 31, 00); // 04:31:00
        timebase.setTime(cal.getTime());
        Assert.assertEquals("still OFF", Light.OFF, l.getState());
        Assert.assertEquals("4 Light PropertyChangeEvents for 2 actual changes", 4, _listenerkicks);

        l.deactivateLight();
        l.dispose();
    }

    @Test
    public void testTimedSensorFollowing() throws jmri.JmriException {

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        Sensor s = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("S2");

        int startListeners = s.getPropertyChangeListeners().length;

        lc = new LightControl(l);
        lc.setControlType(Light.TIMED_ON_CONTROL);
        lc.setControlTimedOnSensorName("S2");

        l.setState(Light.ON); // should go OFF when light enabled and Timed Sensor Control activated

        // adding the PCL to check the Light changes
        // ie "normal" amount of PCEvents for a state change.
        // NOT testing the actual PCListeners
        l.addPropertyChangeListener(new ControlListen());

        l.addLightControl(lc);
        l.activateLight();

        Assert.assertEquals("+1 listener", startListeners + 1, s.getPropertyChangeListeners().length);

        Assert.assertEquals("Sensor unknown state", Sensor.UNKNOWN, s.getState());
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState()); // light set OFF by default
        Assert.assertEquals("2 Light PropertyChangeEvents", 2, _listenerkicks);

        s.setState(Sensor.ON);
        JUnitUtil.waitFor(() -> {
            return 6 == _listenerkicks;
        }, "Light goes ON, then OFF v quickly as time is 0");
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState()); // light set OFF after timeout

        s.setState(Sensor.OFF);
        Assert.assertEquals("still 6 Light PropertyChangeEvents", 6, _listenerkicks);

        l.deactivateLight();
        Assert.assertEquals("releases listener", startListeners, s.getPropertyChangeListeners().length);

        lc.setTimedOnDuration(40); // ms
        l.activateLight();

        s.setState(Sensor.ON);
        s.setState(Sensor.OFF);
        Assert.assertEquals("Light goes ON at start timer", Light.ON, l.getState());
        Assert.assertEquals("8 Light PropertyChangeEvents", 8, _listenerkicks);

        JUnitUtil.waitFor(() -> {
            return Light.OFF == l.getState();
        }, "Light goes back OFF after timer");
        Assert.assertEquals("10 Light PropertyChangeEvents", 10, _listenerkicks);

        l.setEnabled(false);
        s.setState(Sensor.ON);
        s.setState(Sensor.OFF);
        Assert.assertEquals("Light not enabled", Light.OFF, l.getState());
        l.deactivateLight();

        l.setState(Light.ON);
        l.activateLight();
        Assert.assertEquals("Light not enabled so not switched off", Light.ON, l.getState());
        l.deactivateLight();

        // deactivate light mid-timing
        l.setEnabled(true);
        lc.setTimedOnDuration(4000); // ms
        l.activateLight();
        Assert.assertEquals("Light enabled", Light.OFF, l.getState());
        s.setState(Sensor.ON);
        s.setState(Sensor.OFF);
        Assert.assertEquals("Light triggered", Light.ON, l.getState());
        l.deactivateLight();
        Assert.assertEquals("Light still on", Light.ON, l.getState());

        l.activateLight();
        Assert.assertEquals("Light enabled", Light.OFF, l.getState());
        
        lc.activateLightControl();
        Assert.assertEquals("Light still off", Light.OFF, l.getState());

        l.dispose();

    }

    @Test
    public void testNoTimedSensor() {

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        lc = new LightControl(l);
        lc.setControlType(Light.TIMED_ON_CONTROL);

        l.addLightControl(lc);
        l.activateLight();
        JUnitAppender.assertErrorMessage("Light ILL1 is linked to a Sensor that does not exist:");

    }

    @Test
    public void testTwoSensorFollowingNoSensorSet() {

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");

        lc = new LightControl(l);
        lc.setControlType(Light.TWO_SENSOR_CONTROL);

        lc.setControlSensorName("");
        lc.setControlSensor2Name("");

        l.addLightControl(lc);
        l.activateLight();
        JUnitAppender.assertErrorMessage("Light ILL1 with 2 Sensor Control is linked to a Sensor that does not exist.");

        lc.setControlSensorName("S1");
        lc.setControlSensor2Name("");

        l.activateLight();
        JUnitAppender.assertErrorMessage("Light ILL1 with 2 Sensor Control is linked to a Sensor that does not exist.");

        lc.setControlSensorName("");
        lc.setControlSensor2Name("S2");

        l.activateLight();
        JUnitAppender.assertErrorMessage("Light ILL1 with 2 Sensor Control is linked to a Sensor that does not exist.");

        lc.setControlSensorSense(999);
        JUnitAppender.assertErrorMessage("Incorrect Sensor State Set");

    }

    @Test
    public void testTwoSensorFollowing() throws jmri.JmriException {

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        Sensor sOne = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("S1");
        Sensor sTwo = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("S2");

        lc = new LightControl(l);
        lc.setControlType(Light.TWO_SENSOR_CONTROL);
        lc.setControlSensorName("S1");
        lc.setControlSensor2Name("S2");
        lc.setControlSensorSense(Sensor.ACTIVE);

        int startListenersOne = sOne.getPropertyChangeListeners().length;
        int startListenersTwo = sTwo.getPropertyChangeListeners().length;

        l.addLightControl(lc);
        l.activateLight();

        Assert.assertEquals("+1 listener", startListenersOne + 1, sOne.getPropertyChangeListeners().length);
        Assert.assertEquals("+1 listener", startListenersTwo + 1, sTwo.getPropertyChangeListeners().length);

        Assert.assertEquals("Sensor unknown state", Sensor.UNKNOWN, sOne.getState());
        Assert.assertEquals("Sensor unknown state", Sensor.UNKNOWN, sTwo.getState());
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState()); // lights are OFF by default

        sOne.setState(Sensor.ON);
        Assert.assertEquals("Light ON state", Light.ON, l.getState());

        sOne.setState(Sensor.OFF);
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState());

        sTwo.setState(Sensor.ON);
        Assert.assertEquals("Light ON state", Light.ON, l.getState());

        sTwo.setState(Sensor.OFF);
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState());

        sOne.setState(Sensor.ON);
        sTwo.setState(Sensor.ON);
        Assert.assertEquals("Light ON state", Light.ON, l.getState());

        sOne.setState(Sensor.OFF);
        Assert.assertEquals("Light ON state as Stwo still ACTIVE", Light.ON, l.getState());
        sOne.setState(Sensor.ON);

        sTwo.setState(Sensor.OFF);
        Assert.assertEquals("Light ON state as sOne still ACTIVE", Light.ON, l.getState());
        sOne.setState(Sensor.OFF);
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState());

        l.deactivateLight();
        Assert.assertEquals("releases listener", startListenersOne, sOne.getPropertyChangeListeners().length);
        Assert.assertEquals("releases listener", startListenersTwo, sTwo.getPropertyChangeListeners().length);

        sOne.setState(Sensor.ON);
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState());

        l.activateLight();
        Assert.assertEquals("Light ON state", Light.ON, l.getState());

        l.setEnabled(false);
        sOne.setState(Sensor.OFF);
        Assert.assertEquals("does not change", Light.ON, l.getState());

        l.deactivateLight();
        l.dispose();

    }

    @Test
    public void testTwoSensorFollowingInactive() throws jmri.JmriException {

        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        Sensor sOne = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("S1");
        Sensor sTwo = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("S2");

        lc = new LightControl(l);
        lc.setControlType(Light.TWO_SENSOR_CONTROL);
        lc.setControlSensorName("S1");
        lc.setControlSensor2Name("S2");
        lc.setControlSensorSense(Sensor.INACTIVE);

        l.addLightControl(lc);
        l.activateLight();

        // adding the PCL to check the Light changes
        // ie "normal" amount of PCEvents for a state change.
        // NOT testing the actual PCListeners
        l.addPropertyChangeListener(new ControlListen());

        Assert.assertEquals("Sensor unknown state", Sensor.UNKNOWN, sOne.getState());
        Assert.assertEquals("Sensor unknown state", Sensor.UNKNOWN, sTwo.getState());
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState()); // lights are OFF by default

        sOne.setState(Sensor.ACTIVE);
        sTwo.setState(Sensor.ACTIVE);
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState());
        Assert.assertEquals("0 Light PropertyChangeEvents", 0, _listenerkicks);

        sOne.setState(Sensor.INACTIVE);
        Assert.assertEquals("Light ON state", Light.ON, l.getState());
        Assert.assertEquals("2 Light PropertyChangeEvents", 2, _listenerkicks);

        sTwo.setState(Sensor.INACTIVE);
        Assert.assertEquals("Light ON state", Light.ON, l.getState());
        Assert.assertEquals("2 Light PropertyChangeEvents", 2, _listenerkicks);

        sOne.setState(Sensor.ACTIVE);
        Assert.assertEquals("Light ON state", Light.ON, l.getState());
        Assert.assertEquals("2 Light PropertyChangeEvents", 2, _listenerkicks);

        sOne.setState(Sensor.ACTIVE);
        sTwo.setState(Sensor.INACTIVE);
        Assert.assertEquals("Light ON state", Light.ON, l.getState());
        Assert.assertEquals("2 Light PropertyChangeEvents", 2, _listenerkicks);

        sTwo.setState(Sensor.ACTIVE);
        Assert.assertEquals("Light OFF state", Light.OFF, l.getState());
        Assert.assertEquals("4 Light PropertyChangeEvents, 2 actual changes", 4, _listenerkicks);

        l.deactivateLight();
        l.dispose();
        
    }
    
    @Test
    public void testUniqueTimes() {
        l = InstanceManager.getDefault(jmri.LightManager.class).provideLight("L1");
        Assert.assertEquals("OFF state by default", Light.OFF, l.getState()); // lights are OFF by default

        lc = new LightControl(l);
        lc.setControlType(Light.FAST_CLOCK_CONTROL);
        lc.setFastClockControlSchedule(0, 0, 0, 0); // onHr, OnMin, OffHr, OffMin

        Assert.assertTrue(lc.onOffTimesFaulty());
        lc.setFastClockControlSchedule(1, 2, 3, 4); // onHr, OnMin, OffHr, OffMin
        Assert.assertFalse(lc.onOffTimesFaulty());
 
        LightControl lcb = new LightControl(l);
        lcb.setControlType(Light.FAST_CLOCK_CONTROL);
        lcb.setFastClockControlSchedule(1, 2, 0, 0); // onHr, OnMin, OffHr, OffMin

        l.addLightControl(lc);
        Assert.assertFalse(lc.areFollowerTimesFaulty(l.getLightControlList()));
        
        l.addLightControl(lcb);
        
        Assert.assertTrue(lcb.areFollowerTimesFaulty(l.getLightControlList()));
        lcb.setFastClockControlSchedule(0, 0, 0, 0); // onHr, OnMin, OffHr, OffMin
        Assert.assertTrue(lcb.areFollowerTimesFaulty(l.getLightControlList()));
        
        lcb.setFastClockControlSchedule(9, 0, 10, 0); // onHr, OnMin, OffHr, OffMin
        Assert.assertFalse(lcb.areFollowerTimesFaulty(l.getLightControlList()));
        
        lcb.setFastClockControlSchedule(0, 0, 3, 4); // onHr, OnMin, OffHr, OffMin
        Assert.assertTrue(lcb.areFollowerTimesFaulty(l.getLightControlList()));
        l.activateLight();
        
        JUnitAppender.assertErrorMessage("Light has multiple actions for the same time in Light Controller ILL1 ON at 01:02, OFF at 03:04.");
        JUnitAppender.assertErrorMessage("Light has multiple actions for the same time in Light Controller ILL1 ON at 00:00, OFF at 03:04.");        
        
    }

    private int _listenerkicks;

    // internal PCL event counter
    private class ControlListen implements java.beans.PropertyChangeListener {

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            _listenerkicks++;
            // log.warn("{}",e);
        }
    }

    private Light l;
    private LightControl lc;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        _listenerkicks = 0;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LightControlTest.class);
}
