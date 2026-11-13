package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    public void testCommandsAtIntervalKeepOrder() {
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
