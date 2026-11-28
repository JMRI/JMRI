package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Checks that a later turnout command is not overtaken by an earlier one,
 * for DELAYED feedback and for commands sent at the output interval.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AbstractTurnoutCommandOrderTest {

    private int savedDelay;

    @Test
    public void testDelayedFeedbackKeepsLaterCommand() {
        AbstractTurnout.DELAYED_FEEDBACK_INTERVAL = 150;
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        t.setFeedbackMode(Turnout.DELAYED);

        // On the layout thread, where the DELAYED feedback timers run, so neither
        // can run before the listener is added, however long this thread waits.
        List<String> after = Collections.synchronizedList(new ArrayList<>());
        ThreadingUtil.runOnLayout(() -> {
            t.setCommandedState(Turnout.THROWN);
            t.setCommandedState(Turnout.CLOSED);
            t.addPropertyChangeListener(e -> after.add(e.getPropertyName() + "=" + e.getNewValue()));
        });

        // wait for the report, not the state: the state is set before the change is reported
        String closed = Turnout.PROPERTY_KNOWN_STATE + "=" + Turnout.CLOSED;
        JUnitUtil.waitFor(() -> after.contains(closed), "known state CLOSED reported");
        // the timer of the THROWN command is due before the one of the CLOSED command
        assertEquals("[" + closed + "]", after.toString(),
                "state changes after CLOSED was commanded");
        assertEquals(Turnout.CLOSED, t.getCommandedState());
    }

    @Test
    public void testDelayedFeedbackSingleCommand() {
        AbstractTurnout.DELAYED_FEEDBACK_INTERVAL = 50;
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT2");
        t.setFeedbackMode(Turnout.DELAYED);

        // the known state read on the layout thread, before the timer can run
        int known = ThreadingUtil.runOnGUIwithReturn(() -> {
            t.setCommandedState(Turnout.THROWN);
            return t.getKnownState();
        });
        assertEquals(Turnout.INCONSISTENT, known);
        JUnitUtil.waitFor(() -> t.getKnownState() == Turnout.THROWN, "known state THROWN");
        assertEquals(Turnout.THROWN, t.getCommandedState());
    }

    @Test
    public void testFeedbackModeChangeCancelsDelayedFeedback() {
        AbstractTurnout.DELAYED_FEEDBACK_INTERVAL = 50;
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT3");
        t.setFeedbackMode(Turnout.DELAYED);

        // on the layout thread, so the timer of the THROWN command can't run before
        // the feedback mode changes
        int known = ThreadingUtil.runOnGUIwithReturn(() -> {
            t.setCommandedState(Turnout.THROWN);
            int k = t.getKnownState();
            t.setFeedbackMode(Turnout.DIRECT);
            t.setCommandedState(Turnout.CLOSED);
            return k;
        });
        assertEquals(Turnout.INCONSISTENT, known);
        assertEquals(Turnout.CLOSED, t.getKnownState());

        // queued after the DELAYED feedback timer of the THROWN command
        AtomicBoolean timerPassed = new AtomicBoolean(false);
        ThreadingUtil.runOnLayoutDelayed(() -> timerPassed.set(true), 50);
        JUnitUtil.waitFor(timerPassed::get, "delayed feedback time passed");
        assertEquals(Turnout.CLOSED, t.getKnownState(), "known state after the old timer");
    }

    @Test
    public void testOutputIntervalNotKeptByDefault() {
        Assertions.assertFalse(InstanceManager.getDefault(TurnoutManager.class).isOutputIntervalKept(),
            "off unless the connection's setting is on");
        InstanceManager.getDefault(InternalSystemConnectionMemo.class).setOutputIntervalKept(true);
        Assertions.assertTrue(InstanceManager.getDefault(TurnoutManager.class).isOutputIntervalKept(),
            "the default connection's setting");
    }

    @Test
    public void testCommandsAtIntervalKeepOrder() {
        checkCommandsAtIntervalKeepOrder();
    }

    @Test
    public void testCommandsAtIntervalKeepOrderWhenIntervalKept() {
        InstanceManager.getDefault(InternalSystemConnectionMemo.class).setOutputIntervalKept(true);
        checkCommandsAtIntervalKeepOrder();
    }

    private void checkCommandsAtIntervalKeepOrder() {
        InstanceManager.getDefault(InternalSystemConnectionMemo.class).setOutputInterval(20);
        for (int i = 0; i < 5; i++) {
            Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1" + i);
            List<Integer> sent = Collections.synchronizedList(new ArrayList<>());
            List<Boolean> onLayoutThread = Collections.synchronizedList(new ArrayList<>());
            t.addPropertyChangeListener(Turnout.PROPERTY_COMMANDED_STATE, e -> {
                // the thread first: the test goes on once both commands are in sent
                onLayoutThread.add(ThreadingUtil.isLayoutThread());
                sent.add((Integer) e.getNewValue());
            });

            t.setCommandedStateAtInterval(Turnout.THROWN);
            t.setCommandedStateAtInterval(Turnout.CLOSED);

            JUnitUtil.waitFor(() -> sent.size() == 2, "both commands sent");
            assertEquals(List.of(Turnout.THROWN, Turnout.CLOSED), sent, "commands in order of request");
            assertEquals(Turnout.CLOSED, t.getCommandedState(), "later command wins");
            assertTrue(onLayoutThread.stream().allMatch(b -> b), "commands sent on the layout thread");
        }
    }

    @Test
    public void testCommandsAtIntervalSpacedWhenLayoutThreadBusy() throws InterruptedException {
        int interval = 100;
        InstanceManager.getDefault(InternalSystemConnectionMemo.class).setOutputInterval(interval);
        InstanceManager.getDefault(InternalSystemConnectionMemo.class).setOutputIntervalKept(true);
        List<Turnout> turnouts = new ArrayList<>();
        List<Turnout> sentBy = Collections.synchronizedList(new ArrayList<>());
        List<Long> sentAt = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < 5; i++) {
            Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT2" + i);
            t.addPropertyChangeListener(Turnout.PROPERTY_COMMANDED_STATE, e -> {
                sentAt.add(System.nanoTime());
                sentBy.add(t);
            });
            turnouts.add(t);
        }

        // hold the layout thread (Swing thread), as panels loading at start-up do,
        // until every command's output interval slot has passed
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean held = new AtomicBoolean(false);
        ThreadingUtil.runOnLayoutEventually(() -> {
            held.set(true);
            try {
                release.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
        try {
            JUnitUtil.waitFor(held::get, "layout thread held");
            long start = System.nanoTime();
            for (Turnout t : turnouts) {
                t.setCommandedStateAtInterval(Turnout.THROWN);
            }
            // the last slot is 5 intervals away
            JUnitUtil.waitFor(() -> System.nanoTime() - start > 6L * interval * 1_000_000L,
                "every slot passed");
            assertTrue(sentAt.isEmpty(), "nothing sent while the layout thread is held");
        } finally {
            release.countDown();
        }

        JUnitUtil.waitFor(() -> sentAt.size() == 5, "all commands sent");
        assertEquals(turnouts, sentBy, "commands sent in the order requested");
        // each time is taken just after its send, so allow a little for that;
        // without the fix the commands held back are all sent together, 0 ms apart
        long marginMs = 10;
        for (int i = 1; i < 5; i++) {
            long gapMs = (sentAt.get(i) - sentAt.get(i - 1)) / 1_000_000L;
            assertTrue(gapMs >= interval - marginMs,
                "command " + i + " sent " + gapMs + " ms after the one before, interval " + interval + " ms");
        }
    }

    @Test
    public void testCommandAtIntervalSentAtOnceWithNoInterval() {
        InstanceManager.getDefault(InternalSystemConnectionMemo.class).setOutputInterval(0);
        InstanceManager.getDefault(InternalSystemConnectionMemo.class).setOutputIntervalKept(true);
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT30");

        // with no interval and nothing waiting, sent at once on the caller's thread
        t.setCommandedStateAtInterval(Turnout.THROWN);
        assertEquals(Turnout.THROWN, t.getCommandedState());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        savedDelay = AbstractTurnout.DELAYED_FEEDBACK_INTERVAL;
    }

    @AfterEach
    public void tearDown() {
        AbstractTurnout.DELAYED_FEEDBACK_INTERVAL = savedDelay;
        JUnitUtil.tearDown();
    }

}
