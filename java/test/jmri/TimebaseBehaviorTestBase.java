package jmri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jmri.implementation.DefaultClockControl;
import jmri.jmrit.simpleclock.configurexml.SimpleTimebaseXml;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.jupiter.api.*;

// Behavior every Timebase must share with SimpleTimebase; subclasses run it against each implementation
public abstract class TimebaseBehaviorTestBase {

    protected InternalSystemConnectionMemo memo = null;
    protected Timebase clock = null;
    protected RecordingClockControl hardware = null;

    // create the Timebase under test
    protected abstract Timebase createTimebase(InternalSystemConnectionMemo memo);

    // a ClockControl that records what it's told, standing in for a hardware clock
    protected static class RecordingClockControl extends DefaultClockControl {

        final List<String> calls = Collections.synchronizedList(new ArrayList<>());

        @Override
        public String getHardwareClockName() {
            return "Recording";
        }

        @Override
        public void setRate(double newRate) {
            calls.add("setRate " + newRate);
        }

        @Override
        public void setTime(Date now) {
            calls.add("setTime");
        }

        @Override
        public void startHardwareClock(Date now) {
            calls.add("start");
        }

        @Override
        public void stopHardwareClock() {
            calls.add("stop");
        }

        @Override
        public void initializeHardwareClock(double rate, Date now, boolean getTime) {
            calls.add("initialize");
        }
    }

    // records the names of the events a listener gets
    protected static class EventRecorder implements PropertyChangeListener {

        final List<String> names = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            names.add(evt.getPropertyName());
        }
    }

    protected static Date at(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1957, Calendar.JUNE, 14, hour, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    protected Memory memory(String suffix) {
        return InstanceManager.getDefault(MemoryManager.class).getMemory(memo.getSystemPrefix() + "M" + suffix);
    }

    protected Sensor clockRunningSensor() {
        return InstanceManager.getDefault(SensorManager.class).getSensor(memo.getSystemPrefix() + "SCLOCKRUNNING");
    }

    // make the clock send every change to the hardware clocks, as a layout with sync turned on does
    protected void syncWithHardware() {
        clock.setInternalMaster(true, false);
        clock.setSynchronize(true, false);
        hardware.calls.clear();
    }

    // reading and changing the clock

    @Test
    public void testStopAndStart() {
        clock.setRun(false);
        Assertions.assertFalse(clock.getRun(), "stopped");
        clock.setRun(true);
        Assertions.assertTrue(clock.getRun(), "running again");
    }

    @Test
    public void testSetTimeWhileStopped() {
        clock.setRun(false);
        clock.setTime(at(6, 0));
        Assertions.assertEquals(at(6, 0), clock.getTime(), "stopped clock keeps the time it was set to");
    }

    @Test
    public void testUserSetRate() throws TimebaseRateException {
        clock.userSetRate(4.0);
        Assertions.assertEquals(4.0, clock.userGetRate(), 0.0001);
    }

    @Test
    public void testRateOutOfRangeThrows() {
        Assertions.assertThrows(TimebaseRateException.class, () -> clock.userSetRate(1000.0));
        JUnitAppender.suppressErrorMessageStartsWith("rate of");
    }

    // events

    @Test
    public void testRateEvent() throws TimebaseRateException {
        EventRecorder recorder = new EventRecorder();
        clock.addPropertyChangeListener(Timebase.PROPERTY_CHANGE_RATE, recorder);
        clock.userSetRate(4.0);
        JUnitUtil.waitFor(() -> recorder.names.contains(Timebase.PROPERTY_CHANGE_RATE), "rate event");
    }

    @Test
    public void testRunEvent() {
        EventRecorder recorder = new EventRecorder();
        clock.addPropertyChangeListener(Timebase.PROPERTY_CHANGE_RUN, recorder);
        clock.setRun(false);
        JUnitUtil.waitFor(() -> recorder.names.contains(Timebase.PROPERTY_CHANGE_RUN), "run event");
    }

    @Test
    public void testMinutesEventOnSetTime() {
        clock.setRun(false);
        clock.setTime(at(6, 0));
        EventRecorder recorder = new EventRecorder();
        clock.addPropertyChangeListener(Timebase.PROPERTY_CHANGE_MINUTES, recorder);
        clock.setTime(at(6, 5));
        JUnitUtil.waitFor(() -> recorder.names.contains(Timebase.PROPERTY_CHANGE_MINUTES), "minutes event");
    }

    @Test
    public void testTimeEventOnSetTime() {
        clock.setRun(false);
        clock.setTime(at(6, 0));
        EventRecorder recorder = new EventRecorder();
        clock.addPropertyChangeListener(Timebase.PROPERTY_CHANGE_TIME, recorder);
        clock.setTime(at(6, 5));
        JUnitUtil.waitFor(() -> recorder.names.contains(Timebase.PROPERTY_CHANGE_TIME), "time event");
    }

    @Test
    public void testMinuteChangeListenerOnSetTime() {
        clock.setRun(false);
        clock.setTime(at(6, 0));
        EventRecorder recorder = new EventRecorder();
        clock.addMinuteChangeListener(recorder);
        clock.setTime(at(6, 5));
        JUnitUtil.waitFor(() -> !recorder.names.isEmpty(), "minute change listener called");
    }

    // clock memories and the run sensor

    @Test
    public void testRateMemoryFollowsRate() throws TimebaseRateException {
        clock.userSetRate(4.0);
        Assertions.assertEquals(4.0, memory("RATEFACTOR").getValue());
    }

    @Test
    public void testTimeMemoryFollowsTime() {
        clock.setRun(false);
        clock.setTime(at(6, 5));
        JUnitUtil.waitFor(() -> "6:05 AM".equals(memory("CURRENTTIME").getValue()), "time memory shows 6:05 AM");
    }

    @Test
    public void testSetRunUpdatesSensor() {
        clock.setRun(false);
        Assertions.assertEquals(Sensor.INACTIVE, clockRunningSensor().getKnownState(), "sensor off when stopped");
        clock.setRun(true);
        Assertions.assertEquals(Sensor.ACTIVE, clockRunningSensor().getKnownState(), "sensor on when running");
    }

    @Test
    public void testSensorStopsClock() throws JmriException {
        clockRunningSensor().setKnownState(Sensor.INACTIVE);
        Assertions.assertFalse(clock.getRun());
    }

    @Test
    public void testSensorRestartsClock() throws JmriException {
        clockRunningSensor().setKnownState(Sensor.INACTIVE);
        clockRunningSensor().setKnownState(Sensor.ACTIVE);
        Assertions.assertTrue(clock.getRun());
    }

    // hardware clocks

    @Test
    public void testHardwareToldRate() throws TimebaseRateException {
        syncWithHardware();
        clock.userSetRate(4.0);
        Assertions.assertTrue(hardware.calls.contains("setRate 4.0"), "calls: " + hardware.calls);
    }

    @Test
    public void testHardwareToldTime() {
        syncWithHardware();
        clock.userSetTime(at(6, 0));
        Assertions.assertTrue(hardware.calls.contains("setTime"), "calls: " + hardware.calls);
    }

    @Test
    public void testHardwareToldStop() {
        syncWithHardware();
        clock.setRun(false);
        Assertions.assertTrue(hardware.calls.contains("stop"), "calls: " + hardware.calls);
    }

    @Test
    public void testHardwareToldStart() {
        clock.setRun(false);
        syncWithHardware();
        clock.setRun(true);
        Assertions.assertTrue(hardware.calls.contains("start"), "calls: " + hardware.calls);
    }

    // old panel files

    @Test
    public void testLoadOldTimebaseElement() {
        Element element = new Element("timebase");
        element.setAttribute("class", "jmri.jmrit.simpleclock.configurexml.SimpleTimebaseXml");
        element.setAttribute("time", at(6, 0).toString());
        element.setAttribute("rate", "5.0");
        element.setAttribute("startrate", "5.0");
        element.setAttribute("master", "yes");
        element.setAttribute("sync", "yes");
        element.setAttribute("correct", "no");
        element.setAttribute("display", "yes");
        element.setAttribute("startstopped", "yes");
        element.setAttribute("startrunning", "no");
        element.setAttribute("startsettime", "yes");
        element.setAttribute("startclockoption", "0"); // any other value opens a clock window
        element.setAttribute("showbutton", "yes");
        element.setAttribute("startsetrate", "yes");

        Assertions.assertTrue(new SimpleTimebaseXml().load(element, null), "loaded");

        Assertions.assertEquals(5.0, clock.userGetRate(), 0.0001, "rate");
        Assertions.assertEquals(5.0, clock.getStartRate(), 0.0001, "start rate");
        Assertions.assertFalse(clock.getRun(), "stopped");
        Assertions.assertTrue(clock.getSynchronize(), "sync");
        Assertions.assertTrue(clock.use12HourDisplay(), "12 hour display");
        Assertions.assertEquals(Timebase.ClockInitialRunState.DO_STOP, clock.getClockInitialRunState(), "start stopped");
        Assertions.assertTrue(clock.getStartSetTime(), "set time at start");
        Assertions.assertEquals(Timebase.NONE, clock.getStartClockOption(), "start clock option");
        Assertions.assertTrue(clock.getShowStopButton(), "show stop button");
        Assertions.assertTrue(clock.getSetRateAtStart(), "set rate at start");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initTimeProviderManager();
        memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        hardware = new RecordingClockControl();
        InstanceManager.setDefault(ClockControl.class, hardware);
        clock = createTimebase(memo);
        InstanceManager.setDefault(Timebase.class, clock);
    }

    @AfterEach
    public void tearDown() {
        clock.dispose();
        clock = null;
        hardware = null;
        memo = null;
        JUnitUtil.tearDown();
    }

}
