package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Transit;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrix.debugthrottle.DebugThrottle;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 * Terminating an automatic train stops it at once and returns, even while
 * another thread is still setting its speed, as an allocation completing for
 * the train does.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AutoActiveTrainTerminateTest {

    private DispatcherFrame dispatcher;
    private DebugThrottle throttle;
    private AutoActiveTrain aat;
    private AutoActiveTrain.AutoEngineer engineer;

    private void setField(String name, Object value) throws ReflectiveOperationException {
        Field f = AutoActiveTrain.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(aat, value);
    }

    @Test
    public void testTerminateReturnsWhileSpeedIsBeingSet() throws Exception {
        AtomicBoolean terminated = new AtomicBoolean();
        // another thread keeps asking for speed until the train is terminated
        Thread other = new Thread(() -> {
            while (!terminated.get()) {
                engineer.setTargetSpeed(0.8f);
                Thread.yield();
            }
        }, "speed setter");
        other.start();
        JUnitUtil.waitFor(() -> throttle.getSpeedSetting() > 0.0f, "train moving");

        Thread terminating = new Thread(() -> {
            aat.terminate();
            terminated.set(true);
        }, "terminating");
        terminating.start();
        // terminate waits for nothing, so a margin of seconds is ample
        terminating.join(10000);
        Assertions.assertFalse(terminating.isAlive(), "terminate returned");
        other.join(10000);

        assertEquals(0.0f, throttle.getSpeedSetting(), 0.0001f, "train stopped, and stays stopped");
    }

    @Test
    public void testTerminateWaitsForHornToBeTurnedOff() throws Exception {
        Method inc = AutoActiveTrain.class.getDeclaredMethod("incrementHornExecution");
        Method dec = AutoActiveTrain.class.getDeclaredMethod("decrementHornExecution");
        inc.invoke(aat); // a horn is being sounded

        Thread terminating = new Thread(aat::terminate, "terminating");
        terminating.start();
        JUnitUtil.waitFor(() -> terminating.getState() == Thread.State.WAITING
                || terminating.getState() == Thread.State.TIMED_WAITING, "terminate waits for the horn");

        dec.invoke(aat); // the horn is turned off
        terminating.join(10000);
        Assertions.assertFalse(terminating.isAlive(), "terminate returned once the horn was off");
        assertEquals(0.0f, throttle.getSpeedSetting(), 0.0001f, "train stopped");
    }

    @Test
    public void testPhysicsRampCancelledBeforeItStartsDoesNotRun() throws Exception {
        RosterEntry re = new RosterEntry();
        RosterSpeedProfile profile = Mockito.mock(RosterSpeedProfile.class);
        Mockito.when(profile.getRosterEntry()).thenReturn(re);
        Mockito.when(profile.hasForwardSpeeds()).thenReturn(true);
        Mockito.when(profile.hasReverseSpeeds()).thenReturn(true);
        re.setPhysicsWeightKg(80000.0f);
        re.setSpeedProfile(profile);
        setField("re", re);
        Mockito.when(dispatcher.getScale()).thenReturn(jmri.ScaleManager.getScale("HO"));
        engineer = aat.new AutoEngineer(throttle, re);
        engineer.setSpeedLimits(0.0f, 1.0f, 1.0f);
        engineer.setRamping(AutoActiveTrain.RAMP_PHYSICS, 1000, 100, 1);
        setField("_autoEngineer", engineer);

        synchronized (engineer) {
            // the ramp's thread starts, and waits for the engineer
            engineer.setTargetSpeed(0.5f);
            JUnitUtil.waitFor(() -> isAlive("PhysicsRamp Train"), "ramp thread started");
            // the train is terminated before the ramp has begun
            engineer.stopForGood();
        }
        JUnitUtil.waitFor(() -> !isAlive("PhysicsRamp Train"), "ramp thread ended");
        Mockito.verify(profile, Mockito.never()).runPhysicsAccelerationToTargetThrottle(
                Mockito.any(), Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat(),
                Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat());

        // nor is the train slowed by its speed profile once stopped for good
        engineer.setTargetSpeed(200.0f, 0.5f);
        Mockito.verify(profile, Mockito.never()).changeLocoSpeed(Mockito.any(), Mockito.anyFloat(), Mockito.anyFloat());
        assertEquals(0.0f, throttle.getSpeedSetting(), 0.0001f, "train stopped");
    }

    private static boolean isAlive(String threadName) {
        return Thread.getAllStackTraces().keySet().stream()
                .anyMatch(t -> t.isAlive() && threadName.equals(t.getName()));
    }

    @BeforeEach
    public void setUp() throws ReflectiveOperationException {
        JUnitUtil.setUp();
        dispatcher = Mockito.mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, dispatcher);
        InternalSystemConnectionMemo memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        JUnitUtil.initDebugThrottleManager(memo);
        throttle = new DebugThrottle(new DccLocoAddress(3, false), memo);

        Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        aat = new AutoActiveTrain(at);
        engineer = aat.new AutoEngineer(throttle, null);
        engineer.setSpeedLimits(0.0f, 1.0f, 1.0f);
        setField("_autoEngineer", engineer);
        setField("_throttle", throttle);
        setField("_dispatcher", dispatcher);
    }

    @AfterEach
    public void tearDown() {
        engineer.abort();
        JUnitUtil.tearDown();
    }

}
