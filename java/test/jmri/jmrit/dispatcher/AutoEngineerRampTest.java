package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Transit;
import jmri.jmrix.debugthrottle.DebugThrottle;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 * Tests for throttle ramping in {@link AutoActiveTrain.AutoEngineer}.
 * <p>
 * The ramp steps run from a Swing Timer on the GUI thread, while speed changes
 * and halts come from other threads. These tests let a ramp step fall due
 * while another thread is changing the speed, and check that the late step
 * does not undo or speed up the change.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AutoEngineerRampTest {

    private DebugThrottle throttle;
    private AutoActiveTrain.AutoEngineer engineer;

    private void setRamp(int stepIntervalMs) {
        // ten steps from stop to full speed
        engineer.setRamping(AutoActiveTrain.RAMP_MEDIUM, 10 * stepIntervalMs, stepIntervalMs, 1);
    }

    /**
     * Run a speed change as another thread does, holding the AutoEngineer
     * while the next ramp step is already running and waiting for it.
     *
     * @param change the speed change
     */
    private void changeWhileRampStepIsDue(Runnable change) {
        synchronized (engineer) {
            JUnitUtil.waitFor(() -> isGuiThreadWaitingFor(engineer), "ramp step due");
            change.run();
        }
        // let the late ramp step run
        ThreadingUtil.runOnGUI(() -> { });
    }

    /**
     * Is the Swing Timer's ramp step running and blocked on the lock?
     * <p>
     * A Swing Timer runs its tasks on the Swing event dispatch thread, which
     * Java names "AWT-EventQueue-n". The ramp step cannot be paused at the
     * right moment from outside, so the test holds the lock and uses
     * ThreadMXBean to see when that thread is blocked waiting for it. The
     * step is then due but has not run yet.
     *
     * @param lock the lock the ramp step needs
     * @return true if the event dispatch thread is blocked on the lock
     */
    private static boolean isGuiThreadWaitingFor(Object lock) {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        for (ThreadInfo info : mx.getThreadInfo(mx.getAllThreadIds())) {
            if (info != null && info.getThreadName().startsWith("AWT-EventQueue")
                    && info.getThreadState() == Thread.State.BLOCKED
                    && info.getLockInfo() != null
                    && info.getLockInfo().getIdentityHashCode() == System.identityHashCode(lock)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testHaltIsNotUndoneByLateRampStep() throws InterruptedException {
        setRamp(200);
        engineer.setTargetSpeed(1.0f);
        assertEquals(0.1f, throttle.getSpeedSetting(), 0.0001f, "first step of the ramp");

        changeWhileRampStepIsDue(() -> engineer.setHalt(true));

        assertEquals(0.0f, throttle.getSpeedSetting(), 0.0001f, "still halted after the late ramp step");
        Thread.sleep(500); // two more ramp intervals
        assertEquals(0.0f, throttle.getSpeedSetting(), 0.0001f, "ramp did not resume after the halt");
    }

    @Test
    public void testNewTargetDoesNotStartSecondRamp() {
        setRamp(1000);
        engineer.setTargetSpeed(0.5f);
        assertEquals(0.1f, throttle.getSpeedSetting(), 0.0001f, "first step of the ramp");

        changeWhileRampStepIsDue(() -> {
            engineer.setTargetSpeed(1.0f); // new target, first step at once
            assertEquals(0.2f, throttle.getSpeedSetting(), 0.0001f, "first step of the new ramp");
        });

        // the next step of the new ramp is not due for another second
        assertEquals(0.2f, throttle.getSpeedSetting(), 0.0001f,
                "late step of the old ramp did not advance the new ramp");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        DispatcherFrame df = Mockito.mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, df);
        InternalSystemConnectionMemo memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        JUnitUtil.initDebugThrottleManager(memo);
        throttle = new DebugThrottle(new DccLocoAddress(3, false), memo);

        Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        AutoActiveTrain aat = new AutoActiveTrain(at);
        engineer = aat.new AutoEngineer(throttle, null);
        engineer.setSpeedLimits(0.0f, 1.0f, 1.0f);
    }

    @AfterEach
    public void tearDown() {
        engineer.abort();
        JUnitUtil.tearDown();
    }

}
