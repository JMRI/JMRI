package jmri.jmrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.LocoSpeedSource;
import jmri.SystemConnectionMemo;
import jmri.ThrottleListener;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A throttle manager as a {@link LocoSpeedSource}: each change of a
 * throttle's speed setting or direction is reported, by address, and an
 * address's release when its throttle is disposed.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AbstractThrottleManagerLocoSpeedSourceTest {

    /** One report to a listener. */
    private static final class Report {

        final LocoAddress address;
        final Float speed; // null for a release
        final boolean forward;
        final long timeNanos;

        Report(LocoAddress address, Float speed, boolean forward, long timeNanos) {
            this.address = address;
            this.speed = speed;
            this.forward = forward;
            this.timeNanos = timeNanos;
        }
    }

    /** Keeps what it is told. */
    private static final class Recorder implements LocoSpeedSource.Listener {

        final List<Report> reports = new ArrayList<>();

        @Override
        public synchronized void speedCommanded(@Nonnull LocoAddress address, float speed, boolean forward,
                long timeNanos) {
            reports.add(new Report(address, speed, forward, timeNanos));
        }

        @Override
        public synchronized void released(@Nonnull LocoAddress address, long timeNanos) {
            reports.add(new Report(address, null, false, timeNanos));
        }

        synchronized Report last() {
            return reports.get(reports.size() - 1);
        }

        synchronized int count() {
            return reports.size();
        }
    }

    /** Gets a throttle and keeps it. */
    private static final class User implements ThrottleListener {

        DccThrottle throttle;

        @Override
        public void notifyThrottleFound(DccThrottle t) {
            throttle = t;
        }

        @Override
        public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        }

        @Override
        public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
        }
    }

    private DebugThrottleManager manager;
    private final DccLocoAddress three = new DccLocoAddress(3, false);

    private DccThrottle request(@Nonnull User user, @Nonnull DccLocoAddress address) {
        assertTrue(manager.requestThrottle(address, user, false));
        return Objects.requireNonNull(user.throttle, "the debug throttle manager answers at once");
    }

    @Test
    public void testSpeedAndDirectionReported() {
        Recorder recorder = new Recorder();
        manager.addLocoSpeedListener(recorder);
        User user = new User();
        DccThrottle t = request(user, three);
        assertEquals(0, recorder.count(), "making a throttle is not a change");

        long before = System.nanoTime();
        t.setSpeedSetting(0.5f);
        long after = System.nanoTime();
        assertEquals(1, recorder.count());
        Report r = recorder.last();
        assertEquals(three, r.address);
        assertEquals(0.5f, r.speed);
        assertEquals(t.getIsForward(), r.forward);
        assertTrue(r.timeNanos >= before && r.timeNanos <= after, "timed when the throttle told of it");

        boolean forward = !t.getIsForward();
        t.setIsForward(forward);
        assertEquals(2, recorder.count());
        assertEquals(0.5f, recorder.last().speed, "a change of direction reports the speed too");
        assertEquals(forward, recorder.last().forward);

        t.setSpeedSetting(-1f);
        assertEquals(-1f, recorder.last().speed, "an emergency stop is reported as the throttle gives it");
    }

    @Test
    public void testNoChangeNotReported() {
        Recorder recorder = new Recorder();
        manager.addLocoSpeedListener(recorder);
        DccThrottle t = request(new User(), three);
        t.setSpeedSetting(0.25f);
        t.setSpeedSetting(0.25f);
        t.setIsForward(t.getIsForward());
        t.setFunction(0, true);
        assertEquals(1, recorder.count(), "only changes of speed or direction");
    }

    @Test
    public void testThrottleMadeBeforeTheListenerIsWatched() {
        DccThrottle t = request(new User(), three);
        Recorder recorder = new Recorder();
        manager.addLocoSpeedListener(recorder);
        t.setSpeedSetting(0.3f);
        assertEquals(1, recorder.count());
        assertEquals(three, recorder.last().address);
    }

    @Test
    public void testEachAddressReportedAsItself() {
        Recorder recorder = new Recorder();
        manager.addLocoSpeedListener(recorder);
        DccLocoAddress longThree = new DccLocoAddress(3, true);
        DccThrottle shortOne = request(new User(), three);
        DccThrottle longOne = request(new User(), longThree);
        longOne.setSpeedSetting(0.7f);
        shortOne.setSpeedSetting(0.2f);
        assertEquals(longThree, recorder.reports.get(0).address);
        assertEquals(0.7f, recorder.reports.get(0).speed);
        assertEquals(three, recorder.reports.get(1).address);
        assertEquals(0.2f, recorder.reports.get(1).speed);
    }

    @Test
    public void testReleaseReportedWhenTheThrottleIsDisposed() {
        Recorder recorder = new Recorder();
        manager.addLocoSpeedListener(recorder);
        User first = new User();
        User second = new User();
        DccThrottle t = request(first, three);
        request(second, three);
        t.setSpeedSetting(0.4f);
        int listenersBefore = t.getPropertyChangeListeners().length;

        manager.releaseThrottle(t, first);
        assertEquals(1, recorder.count(), "still in use by the second user: not released");

        manager.releaseThrottle(t, second);
        assertEquals(2, recorder.count());
        assertEquals(three, recorder.last().address);
        assertEquals(null, recorder.last().speed, "a release");
        assertEquals(0, manager.getThrottleUsageCount(three), "disposed: watching didn't keep it");
        assertEquals(listenersBefore, t.getPropertyChangeListeners().length, "no property change listener added");

        t.setSpeedSetting(0.9f);
        assertEquals(2, recorder.count(), "a disposed throttle is no longer watched");

        DccThrottle again = request(new User(), three);
        again.setSpeedSetting(0.1f);
        assertEquals(3, recorder.count(), "the next throttle for the address is watched");
        assertEquals(0.1f, recorder.last().speed);
    }

    @Test
    public void testForcedDisposeReportsRelease() {
        Recorder recorder = new Recorder();
        manager.addLocoSpeedListener(recorder);
        DccThrottle t = request(new User(), three);
        manager.forceDisposeThrottle(three);
        assertEquals(1, recorder.count());
        assertEquals(null, recorder.last().speed);
        t.setSpeedSetting(0.6f);
        assertEquals(1, recorder.count(), "no longer watched");
        manager.forceDisposeThrottle(three);
        assertEquals(1, recorder.count(), "an address with no throttle is not released again");
    }

    @Test
    public void testListenersAddedOnceAndRemoved() {
        Recorder recorder = new Recorder();
        manager.addLocoSpeedListener(recorder);
        manager.addLocoSpeedListener(recorder);
        DccThrottle t = request(new User(), three);
        t.setSpeedSetting(0.5f);
        assertEquals(1, recorder.count(), "added once");
        manager.removeLocoSpeedListener(recorder);
        manager.removeLocoSpeedListener(recorder);
        t.setSpeedSetting(0.6f);
        assertEquals(1, recorder.count(), "removed");
    }

    @Test
    public void testFailingListenerDoesNotStopTheOthers() {
        LocoSpeedSource.Listener failing = new LocoSpeedSource.Listener() {
            @Override
            public void speedCommanded(@Nonnull LocoAddress address, float speed, boolean forward,
                    long timeNanos) {
                throw new IllegalStateException("failing on purpose");
            }

            @Override
            public void released(@Nonnull LocoAddress address, long timeNanos) {
                throw new IllegalStateException("failing on purpose");
            }
        };
        Recorder recorder = new Recorder();
        manager.addLocoSpeedListener(failing);
        manager.addLocoSpeedListener(recorder);
        User user = new User();
        DccThrottle t = request(user, three);
        t.setSpeedSetting(0.5f);
        JUnitAppender.assertErrorMessageStartsWith("A loco speed listener failed on 3(S)");
        assertEquals(1, recorder.count());
        assertEquals(0.5f, t.getSpeedSetting(), "the throttle went on as usual");
        manager.releaseThrottle(t, user);
        JUnitAppender.assertErrorMessageStartsWith("A loco speed listener failed on the release of 3(S)");
        assertEquals(2, recorder.count());
    }

    /**
     * Throttles tell of changes in different ways: some before they set their
     * own field, some with an event object. Each is reported with the new
     * value.
     */
    @Test
    public void testEveryWayAThrottleTellsIsSeen() {
        Recorder recorder = new Recorder();
        manager.addLocoSpeedListener(recorder);
        TellingThrottle t = new TellingThrottle(manager.adapterMemo, three);
        manager.notifyThrottleKnown(t, three);

        t.speedTellingFirst(0.8f);
        assertEquals(0.8f, recorder.last().speed, "told before the field was set: the event's value is used");
        assertEquals(1, recorder.count());

        t.directionTellingFirst(false);
        assertFalse(recorder.last().forward, "told before the field was set: the event's value is used");
        assertEquals(0.8f, recorder.last().speed);

        t.speedByEvent(0.2f);
        assertEquals(0.2f, recorder.last().speed, "an event object is seen too");
        assertEquals(3, recorder.count());

        t.speedByEvent(0.2f);
        assertEquals(3, recorder.count(), "an event with no change is not reported");
    }

    /** A throttle telling of changes as some systems' throttles do. */
    private static final class TellingThrottle extends AbstractThrottle {

        private final DccLocoAddress address;

        TellingThrottle(SystemConnectionMemo memo, DccLocoAddress address) {
            super(memo);
            this.address = address;
            this.isForward = true;
        }

        void speedTellingFirst(float speed) {
            float old;
            synchronized (this) {
                old = this.speedSetting;
            }
            firePropertyChange(SPEEDSETTING, old, speed);
            synchronized (this) {
                this.speedSetting = speed;
            }
        }

        void directionTellingFirst(boolean forward) {
            firePropertyChange(ISFORWARD, this.isForward, forward);
            this.isForward = forward;
        }

        void speedByEvent(float speed) {
            float old;
            synchronized (this) {
                old = this.speedSetting;
                this.speedSetting = speed;
            }
            firePropertyChange(new PropertyChangeEvent(this, SPEEDSETTING, old, speed));
        }

        @Override
        public LocoAddress getLocoAddress() {
            return address;
        }

        @Override
        protected void throttleDispose() {
            finishRecord();
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        manager = new DebugThrottleManager(new InternalSystemConnectionMemo(false));
    }

    @AfterEach
    public void tearDown() {
        manager.dispose();
        JUnitUtil.tearDown();
    }
}
