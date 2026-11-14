package jmri.jmrix.can.cbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Checks that with DELAYED feedback a turnout event seen on the bus is not
 * overwritten by the timer of an earlier event.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusTurnoutDelayedFeedbackTest {

    private TrafficControllerScaffold tcis;
    private CbusTurnout t;
    private int savedDelay;

    @Test
    public void testLaterBusEventWins() {
        AbstractTurnout.DELAYED_FEEDBACK_INTERVAL = 150;
        t = new CbusTurnout("MT", "+N54321E12345", tcis);
        t.setFeedbackMode(Turnout.DELAYED);

        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(1, 0xd4);
        m.setElement(2, 0x31);
        m.setElement(3, 0x30);
        m.setElement(4, 0x39);

        List<Integer> knownStates = Collections.synchronizedList(new ArrayList<>());
        t.addPropertyChangeListener(Turnout.PROPERTY_KNOWN_STATE, e -> knownStates.add((Integer) e.getNewValue()));

        m.setElement(0, 0x90); // ACON, thrown
        t.message(m);
        m.setElement(0, 0x91); // ACOF, closed
        t.message(m);
        assertEquals(Turnout.CLOSED, t.getCommandedState());

        JUnitUtil.waitFor(() -> knownStates.contains(Turnout.CLOSED), "known state CLOSED reported");
        // give the timer of the earlier event time to run, as it would have
        JUnitUtil.waitFor(3 * AbstractTurnout.DELAYED_FEEDBACK_INTERVAL);

        assertFalse(knownStates.contains(Turnout.THROWN), "earlier event never reported: " + knownStates);
        assertEquals(Turnout.CLOSED, (int) knownStates.get(knownStates.size() - 1), "last known state: " + knownStates);
        assertEquals(Turnout.CLOSED, t.getKnownState());
        assertEquals(Turnout.CLOSED, t.getCommandedState());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new TrafficControllerScaffold();
        savedDelay = AbstractTurnout.DELAYED_FEEDBACK_INTERVAL;
    }

    @AfterEach
    public void tearDown() {
        AbstractTurnout.DELAYED_FEEDBACK_INTERVAL = savedDelay;
        if (t != null) {
            t.dispose();
        }
        tcis.terminateThreads();
        JUnitUtil.tearDown();
    }

}
