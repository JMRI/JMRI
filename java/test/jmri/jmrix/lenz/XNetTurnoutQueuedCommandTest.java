package jmri.jmrix.lenz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Checks that feedback received while an {@link XNetTurnout}'s command is
 * still waiting to be sent does not change the turnout.
 * <p>
 * Turnouts 21 and 22 share a feedback nibble, so the reply to a command for
 * turnout 21 also reports turnout 22. When commands for both go out one after
 * the other, that reply is received before turnout 22's command is sent, and
 * still reports turnout 22's old position.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class XNetTurnoutQueuedCommandTest {

    private XNetInterfaceScaffold lnis;
    private XNetTurnout t;

    private static final String CLOSE = "52 05 8A DD";
    private static final String CLOSE_OFF = "52 05 82 D5";

    @Test
    public void testMonitoringIgnoresFeedbackBeforeCommandSent() {
        checkFeedbackBeforeCommandSent(Turnout.MONITORING);
    }

    @Test
    public void testExactIgnoresFeedbackBeforeCommandSent() {
        checkFeedbackBeforeCommandSent(Turnout.EXACT);
    }

    @Test
    public void testDirectIgnoresFeedbackBeforeCommandSent() {
        checkFeedbackBeforeCommandSent(Turnout.DIRECT);
    }

    private void checkFeedbackBeforeCommandSent(int mode) {
        t.setFeedbackMode(mode);
        t.newKnownState(Turnout.THROWN);
        assertEquals(Turnout.THROWN, t.getCommandedState(), "starts thrown");

        t.setCommandedState(Turnout.CLOSED);
        assertEquals(CLOSE, lnis.outbound.lastElement().toString(), "close command queued");
        int sent = lnis.outbound.size();

        // the reply to turnout 21's command: 21 closed, 22 still thrown
        t.message(feedback(0x29));
        assertEquals(Turnout.CLOSED, t.getCommandedState(), "commanded state kept");
        assertEquals(Turnout.INCONSISTENT, t.getKnownState(), "known state waits for the command");
        assertEquals(sent, lnis.outbound.size(), "nothing sent ahead of the command");

        // the command is sent, and its reply reports 22 closed
        t.message(lnis.outbound.elementAt(sent - 1));
        t.message(feedback(0x25));
        assertEquals(Turnout.CLOSED, t.getKnownState(), "closed from feedback");
        // acknowledge every off message; each one must be for closed
        for (int i = sent; i < lnis.outbound.size(); i++) {
            assertEquals(CLOSE_OFF, lnis.outbound.elementAt(i).toString(), "off message " + (i - sent + 1));
            t.message(lnis.outbound.elementAt(i));
            t.message(new XNetReply("01 04 05"));
        }
        assertEquals(Turnout.CLOSED, t.getKnownState(), "known state at end");
        assertEquals(Turnout.CLOSED, t.getCommandedState(), "commanded state at end");
        assertEquals(XNetTurnout.IDLE, t.internalState, "turnout idle");
    }

    /**
     * Feedback broadcast for turnouts 21 and 22 (turnouts with feedback, motion
     * complete).
     *
     * @param data the data byte: bits 0-1 turnout 21, bits 2-3 turnout 22
     * @return the reply
     */
    private static XNetReply feedback(int data) {
        XNetReply reply = new XNetReply("42 05 00 00");
        reply.setElement(2, data);
        reply.setParity();
        return reply;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        lnis = new XNetInterfaceScaffold(new LenzCommandStation());
        t = new XNetTurnout("XT", 22, lnis);
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @AfterEach
    public void tearDown() {
        lnis.terminateThreads();
        lnis = null;
        t.dispose();
        t = null;
        JUnitUtil.tearDown();
    }

}
