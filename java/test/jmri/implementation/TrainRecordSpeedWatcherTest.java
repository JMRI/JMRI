package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.LocoSpeedSource;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.TrainRecord;
import jmri.TrainRecord.SpeedCommand;
import jmri.TrainRecordManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.managers.DefaultTrainRecordManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A train record keeps its locos' speed commands, from every source.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainRecordSpeedWatcherTest {

    private DefaultTrainRecordManager manager;

    /** A source tests drive by hand, counting its listeners. */
    private static final class HandSource implements LocoSpeedSource {

        final List<LocoSpeedSource.Listener> listeners = new ArrayList<>();

        @Override
        public void addLocoSpeedListener(@Nonnull LocoSpeedSource.Listener listener) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }

        @Override
        public void removeLocoSpeedListener(@Nonnull LocoSpeedSource.Listener listener) {
            listeners.remove(listener);
        }

        void speed(LocoAddress a, float speed, boolean forward, long timeNanos) {
            for (LocoSpeedSource.Listener l : new ArrayList<>(listeners)) {
                l.speedCommanded(a, speed, forward, timeNanos);
            }
        }

        void release(LocoAddress a, long timeNanos) {
            for (LocoSpeedSource.Listener l : new ArrayList<>(listeners)) {
                l.released(a, timeNanos);
            }
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

    private static RosterEntry loco(String id, int address, boolean isLong) {
        RosterEntry e = new RosterEntry();
        e.setId(id);
        e.setDccAddress(Integer.toString(address));
        e.setProtocol(isLong ? LocoAddress.Protocol.DCC_LONG : LocoAddress.Protocol.DCC_SHORT);
        return e;
    }

    private TrainRecord record(String name, RosterEntry entry) {
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            TrainRecord r = manager.newRecord(name);
            if (entry != null) {
                r.insertFirstRosterEntry(entry);
            }
            return r;
        });
    }

    private HandSource handSource() {
        HandSource s = new HandSource();
        InstanceManager.store(s, LocoSpeedSource.class);
        return s;
    }

    @Test
    public void testCommandsFromJmrisThrottles() {
        TrainRecord r = record("Up goods", loco("Class 37", 3, false));
        assertEquals(List.of(new DccLocoAddress(3, false)), r.getLocoAddresses());
        User user = new User();
        assertTrue(InstanceManager.getDefault(ThrottleManager.class)
                .requestThrottle(new DccLocoAddress(3, false), user, false));
        DccThrottle t = Objects.requireNonNull(user.throttle);
        long before = System.nanoTime();
        t.setSpeedSetting(0.5f); // on this (test) thread: handed to the layout thread
        JUnitUtil.waitFor(() -> r.getLastCommand() != null, "the command reached the record");
        SpeedCommand c = Objects.requireNonNull(r.getLastCommand());
        assertEquals(0.5f, c.getSpeedSetting());
        assertEquals(t.getIsForward(), c.isForward());
        assertFalse(c.isRelease());
        assertTrue(c.getTimeNanos() >= before, "the time the source gave");

        t.setIsForward(!t.getIsForward());
        JUnitUtil.waitFor(() -> r.getCommands().size() == 2, "the direction reached the record");
        assertEquals(!c.isForward(), Objects.requireNonNull(r.getLastCommand()).isForward());

        InstanceManager.getDefault(ThrottleManager.class).releaseThrottle(t, user);
        JUnitUtil.waitFor(() -> r.getCommands().size() == 3, "the release reached the record");
        SpeedCommand released = Objects.requireNonNull(r.getLastCommand());
        assertTrue(released.isRelease());
        assertEquals(0.5f, released.getSpeedSetting(), "left at its last speed");
    }

    @Test
    public void testCommandsFromAnotherSourceByAddress() {
        HandSource source = handSource();
        TrainRecord shortThree = record("Short", loco("Short three", 3, false));
        TrainRecord longThree = record("Long", loco("Long three", 3, true));
        ThreadingUtil.runOnLayout(() -> {
            source.speed(new DccLocoAddress(3, true), 0.25f, true, 10);
            source.speed(new DccLocoAddress(4, false), 0.75f, true, 11);
        });
        assertEquals(1, longThree.getCommands().size());
        assertEquals(10L, Objects.requireNonNull(longThree.getLastCommand()).getTimeNanos());
        assertTrue(shortThree.getCommands().isEmpty(), "a short and a long address are different locos");
    }

    @Test
    public void testCommandsForEveryLoco() {
        HandSource source = handSource();
        TrainRecord r = record("Coal", loco("Class 20 A", 20, false));
        RosterEntry second = loco("Class 20 B", 21, false);
        ThreadingUtil.runOnLayout(() -> r.setRosterEntries(List.of(r.getRosterEntries().get(0), second)));
        DccLocoAddress lead = new DccLocoAddress(20, false);
        DccLocoAddress other = new DccLocoAddress(21, false);
        assertEquals(List.of(lead, other), r.getLocoAddresses(), "lead loco first");
        ThreadingUtil.runOnLayout(() -> {
            source.speed(lead, 0.25f, true, 10);
            source.speed(other, 0.25f, true, 11); // the same speed, for the other loco: kept
            source.speed(other, 0.25f, true, 12); // a repeat for that loco: not kept
            source.speed(lead, 0.5f, true, 13);
            source.release(other, 14);
        });
        List<SpeedCommand> commands = r.getCommands();
        assertEquals(4, commands.size());
        assertTrue(commands.get(0).isFor(lead));
        assertTrue(commands.get(1).isFor(other));
        assertEquals(11L, commands.get(1).getTimeNanos());
        List<SpeedCommand> leads = r.getCommands(lead);
        assertEquals(2, leads.size(), "the lead loco's commands");
        assertEquals(10L, leads.get(0).getTimeNanos());
        assertEquals(13L, leads.get(1).getTimeNanos());
        assertEquals(0.5f, Objects.requireNonNull(r.getLastCommand(lead)).getSpeedSetting());
        SpeedCommand released = Objects.requireNonNull(r.getLastCommand(other));
        assertTrue(released.isRelease());
        assertEquals(0.25f, released.getSpeedSetting(), "the other loco left at its own last speed");
        assertSame(released, r.getLastCommand(), "the most recent for any loco");

        ThreadingUtil.runOnLayout(() -> r.setRosterEntries(List.of(r.getRosterEntries().get(0))));
        ThreadingUtil.runOnLayout(() -> source.speed(other, 0.75f, true, 15));
        assertEquals(4, r.getCommands().size(), "a loco taken off the train is no longer followed");
    }

    @Test
    public void testRepeatsNotKept() {
        HandSource source = handSource();
        TrainRecord r = record("Up goods", loco("Class 37", 3, false));
        DccLocoAddress a = new DccLocoAddress(3, false);
        ThreadingUtil.runOnLayout(() -> {
            source.speed(a, 0.25f, true, 10);
            source.speed(a, 0.25f, true, 12); // the same command, seen by a second source
            source.release(a, 13);
            source.release(a, 14);
            source.speed(a, 0.25f, true, 15); // taken again, at the same speed
        });
        List<SpeedCommand> commands = r.getCommands();
        assertEquals(3, commands.size());
        assertEquals(10L, commands.get(0).getTimeNanos(), "the first report is kept");
        assertTrue(commands.get(1).isRelease());
        assertEquals(13L, commands.get(1).getTimeNanos());
        assertEquals(15L, commands.get(2).getTimeNanos());
    }

    @Test
    public void testListensOnlyWhileARecordHasAnAddress() {
        HandSource source = handSource();
        assertTrue(source.listeners.isEmpty(), "no records");
        TrainRecord r = record("Up goods", null);
        assertTrue(source.listeners.isEmpty(), "no record with an address");
        ThreadingUtil.runOnLayout(() -> r.insertFirstRosterEntry(loco("Class 37", 3, false)));
        assertEquals(1, source.listeners.size(), "a record with an address");
        ThreadingUtil.runOnLayout(() -> r.removeFirstRosterEntry());
        assertTrue(source.listeners.isEmpty());
        ThreadingUtil.runOnLayout(() -> r.insertFirstRosterEntry(loco("Class 37", 3, false)));
        ThreadingUtil.runOnLayout(() -> manager.remove(r));
        assertTrue(source.listeners.isEmpty(), "the record removed");
    }

    @Test
    public void testSourceStoredLaterIsFollowed() {
        TrainRecord r = record("Up goods", loco("Class 37", 3, false));
        HandSource later = handSource();
        JUnitUtil.waitFor(() -> later.listeners.size() == 1, "listening to the new source");
        ThreadingUtil.runOnLayout(() -> later.speed(new DccLocoAddress(3, false), 0.4f, false, 20));
        assertEquals(0.4f, Objects.requireNonNull(r.getLastCommand()).getSpeedSetting());
        ThreadingUtil.runOnLayout(() -> InstanceManager.deregister(later, LocoSpeedSource.class));
        JUnitUtil.waitFor(() -> later.listeners.isEmpty(), "a source removed is let go");
    }

    @Test
    public void testCommandsBounded() {
        HandSource source = handSource();
        TrainRecord r = record("Up goods", loco("Class 37", 3, false));
        DccLocoAddress a = new DccLocoAddress(3, false);
        ThreadingUtil.runOnLayout(() -> {
            for (int i = 0; i < TrainRecord.COMMANDS_LENGTH + 10; i++) {
                source.speed(a, (i % 100) / 100f, true, i);
            }
        });
        assertEquals(TrainRecord.COMMANDS_LENGTH, r.getCommands().size());
        assertEquals(10L, r.getCommands().get(0).getTimeNanos(), "the oldest dropped");
    }

    @Test
    public void testEventsAndRemovedRecord() {
        HandSource source = handSource();
        TrainRecord r = record("Up goods", loco("Class 37", 3, false));
        List<Object> told = new ArrayList<>();
        r.addPropertyChangeListener(TrainRecord.PROPERTY_COMMAND, e -> told.add(e.getNewValue()));
        DccLocoAddress a = new DccLocoAddress(3, false);
        ThreadingUtil.runOnLayout(() -> source.speed(a, 0.3f, true, 1));
        assertEquals(1, told.size());
        assertSame(r.getLastCommand(), told.get(0));
        ThreadingUtil.runOnLayout(() -> {
            TrainRecord other = manager.newRecord("Down goods");
            manager.remove(r);
            other.insertFirstRosterEntry(loco("Class 37", 3, false));
            source.speed(a, 0.6f, true, 2);
        });
        assertEquals(1, r.getCommands().size(), "a removed record is left alone");
        assertNull(manager.getByName("Up goods"));
        assertEquals(0.6f, Objects.requireNonNull(Objects.requireNonNull(manager.getByName("Down goods")).getLastCommand()).getSpeedSetting());
    }

    @Test
    public void testEmergencyStopKept() {
        HandSource source = handSource();
        TrainRecord r = record("Up goods", loco("Class 37", 3, false));
        DccLocoAddress a = new DccLocoAddress(3, false);
        ThreadingUtil.runOnLayout(() -> source.speed(a, -1f, true, 1));
        SpeedCommand c = Objects.requireNonNull(r.getLastCommand());
        assertTrue(c.isEmergencyStop(), "a speed below 0 is an emergency stop");
        ThreadingUtil.runOnLayout(() -> source.speed(a, 0.2f, true, 2));
        assertFalse(Objects.requireNonNull(r.getLastCommand()).isEmergencyStop());
    }

    @Test
    public void testNothingKeptAfterRemoveOrDispose() {
        HandSource source = handSource();
        TrainRecord r = record("Up goods", loco("Class 37", 3, false));
        DccLocoAddress a = new DccLocoAddress(3, false);
        // a command made before the record was removed, arriving after
        ThreadingUtil.runOnLayout(() -> {
            manager.remove(r);
            ((DefaultTrainRecord) r).commanded(new SpeedCommand(a, 0.3f, true, false, 1, java.time.Instant.now()));
        });
        assertTrue(r.getCommands().isEmpty(), "a removed record keeps nothing");

        TrainRecord other = record("Down goods", loco("Class 37", 3, false));
        // a command a source reports after the manager is disposed, as one queued before it could
        List<LocoSpeedSource.Listener> listeners = new ArrayList<>(source.listeners);
        ThreadingUtil.runOnLayout(manager::dispose);
        ThreadingUtil.runOnLayout(() -> listeners.forEach(l -> l.speedCommanded(a, 0.5f, true, 2)));
        assertTrue(other.getCommands().isEmpty(), "nothing is kept once the manager is disposed");
    }

    @Test
    public void testDisposeStopsListening() {
        HandSource source = handSource();
        record("Up goods", loco("Class 37", 3, false));
        ThreadingUtil.runOnLayout(manager::dispose);
        assertTrue(source.listeners.isEmpty());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initDebugThrottleManager();
        manager = (DefaultTrainRecordManager) InstanceManager.getDefault(TrainRecordManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
