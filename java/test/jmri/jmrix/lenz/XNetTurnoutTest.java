package jmri.jmrix.lenz;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.Turnout;

import org.junit.jupiter.api.*;

/**
 * Tests for the {@link jmri.jmrix.lenz.XNetTurnout} class.
 *
 * @author Bob Jacobsen
 */
public class XNetTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

    @Override
    public int numListeners() {
        return lnis.numListeners();
    }

    protected XNetInterfaceScaffold lnis;

    @Override
    public void checkClosedMsgSent() {
        assertEquals( "52 05 88 DF",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "closed message");
    }

    protected void checkClosedOffSent() {
        assertEquals( "52 05 80 D7",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "closed message OFF");
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
    }

    @Override
    public void checkThrownMsgSent() {
        assertEquals( "52 05 89 DE",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "thrown message");
    }

    protected void checkThrownOffSent() {
        assertEquals( "52 05 81 D6",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "thrown message OFF");
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
    }

    @Test
    public void checkIncoming() {
        t.setFeedbackMode(Turnout.MONITORING);
        JUnitUtil.waitFor(() -> t.getFeedbackMode() == Turnout.MONITORING, "Feedback mode set");

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());

        // notify the object that somebody else changed it...
        XNetReply m = new XNetReply("42 05 01 46"); // set CLOSED
        ((XNetTurnout) t).message(m);
        JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        assertEquals( Turnout.CLOSED, t.getKnownState(), "state after CLOSED message");

        listenStatus = Turnout.UNKNOWN;

        m = new XNetReply("42 05 02 45"); // set THROWN
        ((XNetTurnout) t).message(m);
        JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        assertEquals( Turnout.THROWN, t.getKnownState(), "state after THROWN message");
    }

    // Test the XNetTurnout message sequence.
    @Test
    public void testXNetTurnoutMsgSequence() {
        t.setFeedbackMode(Turnout.DIRECT);
        // set closed
        assertDoesNotThrow( () -> t.setCommandedState(Turnout.CLOSED),
            "TO exception:");

        assertEquals(Turnout.CLOSED, t.getCommandedState());

        assertEquals( "52 05 88 DF",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "on message sent");
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        // notify that the command station received the reply
        XNetReply m = new XNetReply();
        m.setElement(0, 0x42);
        m.setElement(1, 0x05);
        m.setElement(2, 0x01);     // set CLOSED
        m.setElement(3, 0x46);

        int n = lnis.outbound.size();

        ((XNetTurnout) t).message(m);

        while (n == lnis.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
        assertEquals( "52 05 80 D7",
                lnis.outbound.elementAt(n).toString(),
                "off message sent");
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        ((XNetTurnout) t).message(lnis.outbound.elementAt(n-1));

        // the turnout will not set its state until it sees an OK message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        ((XNetTurnout) t).message(m);

      //  while (n == lnis.outbound.size()) {
      //  } // busy loop.  Wait for
        // outbound size to change.

        checkClosedOffSent();
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        ((XNetTurnout) t).message(m);

        // no wait here.  The last reply should cause the turnout to
        // set it's state, but it will not cause another reply.
        assertEquals(Turnout.CLOSED, t.getKnownState());
    }

    // Test that property change events are properly sent from the parent
    // to the propertyChange listener (this handles events for one sensor
    // and twosensor feedback).
    @Test
    public void testXNetTurnoutPropertyChange() {
        // set thrown
        assertDoesNotThrow( () -> t.setCommandedState(Turnout.THROWN),
            "TO exception:");

        assertEquals(Turnout.THROWN, t.getCommandedState());
        t.setFeedbackMode(Turnout.ONESENSOR);
        jmri.Sensor s = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
        assertDoesNotThrow( () -> {
            s.setState(jmri.Sensor.INACTIVE);
            t.provideFirstFeedbackSensor("IS1");
        }, "TO exception:");

        assertDoesNotThrow( () -> s.setState(jmri.Sensor.ACTIVE),
            "SO exception:");

        // check to see if the turnout state changes.
        JUnitUtil.waitFor(() -> t.getKnownState() == Turnout.THROWN, "Turnout goes THROWN");
    }

    /**
     * Number of OFF messages this system puts on the wire for a single
     * turnout operation. Systems that never send OFF messages (e.g. the
     * Hornby Elite and the ZTC611) override this to return 0.
     * @return the expected OFF message count
     */
    protected int expectedOffMessages() {
        return 1;
    }

    // An unsolicited broadcast reply (e.g. the feedback broadcast generated
    // by our own ON command, delivered again while we're waiting on the OFF
    // acknowledgement) must not be treated as a failed OFF and trigger a
    // resend; nor should sendOffMessage()'s own KnownState change re-enter
    // and send a second OFF. Exactly one ON message, plus the number of OFF
    // messages the system normally uses (see expectedOffMessages()), should
    // ever reach the traffic controller for a single throw.
    @Test
    public void testMonitoringModeNoSpuriousOffOnUnsolicitedFeedback() {
        assertEquals( Turnout.MONITORING, t.getFeedbackMode(), "Feedback Mode after set");

        int outboundBase = lnis.outbound.size();

        t.setCommandedState(Turnout.THROWN);
        checkThrownMsgSent();
        assertEquals( outboundBase + 1, lnis.outbound.size(), "only the ON message queued so far");
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size() - 1));

        // command station acknowledges the ON message; in MONITORING mode
        // this is what triggers the OFF message on systems that send one
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        int expectedOutbound = outboundBase + 1 + expectedOffMessages();
        assertEquals( expectedOutbound, lnis.outbound.size(),
                "only the expected OFF messages queued after the ON is acknowledged");
        checkThrownOffSent();

        // the same broadcast is delivered twice while the OFF is still
        // unacknowledged, as happens in AbstractMRTrafficController.notifyReply(),
        // which notifies both the general FEEDBACK listener and the pending
        // request's "dest" listener (this turnout, in both cases)
        XNetReply broadcast = new XNetReply("42 05 02 46");
        broadcast.setUnsolicited();
        ((XNetTurnout) t).message(broadcast);
        ((XNetTurnout) t).message(broadcast);
        assertEquals( expectedOutbound, lnis.outbound.size(),
                "unsolicited broadcast feedback received before the OFF is acknowledged must not trigger extra OFF messages");

        // command station finally acknowledges the OFF message
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        assertEquals( expectedOutbound, lnis.outbound.size(),
                "OFF acknowledgement must not trigger another OFF message");

        JUnitUtil.waitFor(() -> t.getKnownState() == Turnout.THROWN, "Turnout goes THROWN");
    }

    // When the OK reply to an OFF message is genuinely lost - replaced on the
    // wire by an unsolicited broadcast, which is now ignored rather than
    // treated as a failed OFF (see testMonitoringModeNoSpuriousOffOnUnsolicitedFeedback)
    // - nothing else would ever nudge the turnout out of OFFSENT: the traffic
    // controller already considers that exchange closed, so no transport level
    // timeout fires either. The per-turnout watchdog is the only thing left to
    // resend the OFF and keep the turnout responsive to later commands.
    @Test
    public void testOffWatchdogResendsWhenReplyNeverArrives() {
        if (expectedOffMessages() == 0) {
            return; // this system never sends an OFF message, nothing for the watchdog to guard
        }
        assertEquals( Turnout.MONITORING, t.getFeedbackMode(), "Feedback Mode after set");
        ((XNetTurnout) t).setOffWatchdogInterval(100);

        int outboundBase = lnis.outbound.size();

        t.setCommandedState(Turnout.THROWN);
        checkThrownMsgSent();
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size() - 1));

        // command station acknowledges the ON message; this is what triggers
        // the OFF message and arms the watchdog
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        int expectedOutbound = outboundBase + 1 + expectedOffMessages();
        assertEquals( expectedOutbound, lnis.outbound.size(), "OFF message sent after ON acknowledged");
        checkThrownOffSent();

        // only an unsolicited broadcast arrives after that - the real OK is lost
        XNetReply broadcast = new XNetReply("42 05 02 46");
        broadcast.setUnsolicited();
        ((XNetTurnout) t).message(broadcast);
        assertEquals( expectedOutbound, lnis.outbound.size(),
                "unsolicited broadcast must not itself trigger a resend");

        // the watchdog has to give up waiting and resend the OFF message itself
        JUnitUtil.waitFor(() -> lnis.outbound.size() > expectedOutbound, "watchdog resent the OFF message");
        checkThrownOffSent();
        JUnitAppender.assertWarnMessageStartsWith("Turnout 21 - no reply to OFF message after");

        // command station finally acknowledges the resent OFF
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        JUnitUtil.waitFor(() -> t.getKnownState() == Turnout.THROWN, "Turnout goes THROWN");
    }

    // Turnout number the watchdog's warn message logs. Normally the same
    // number the turnout was constructed with (21, see setUp()), but
    // EliteXNetTurnout shifts mNumber by one internally to work around a
    // hardware off-by-one, so its subclass test overrides this.
    protected int watchdogTurnoutNumber() {
        return 21;
    }

    // Mirrors testOffWatchdogResendsWhenReplyNeverArrives, but for the wait
    // one step earlier: COMMANDSENT, between sending the initial ON command
    // and receiving whatever reply would trigger the OFF message. If that
    // reply is genuinely lost, nothing else nudges the turnout out of
    // COMMANDSENT either - same missing self-healing path, just at the
    // other end of the sequence.
    @Test
    public void testCommandWatchdogResendsWhenReplyNeverArrives() {
        assertEquals( Turnout.MONITORING, t.getFeedbackMode(), "Feedback Mode after set");
        ((XNetTurnout) t).setOffWatchdogInterval(100);

        int outboundBase = lnis.outbound.size();

        t.setCommandedState(Turnout.THROWN);
        checkThrownMsgSent();
        // command station transmits the ON message; this is what moves the
        // turnout into COMMANDSENT and arms the watchdog
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size() - 1));

        // nothing else ever arrives - no OK, no matching feedback broadcast
        int expectedOutbound = outboundBase + 1;
        assertEquals( expectedOutbound, lnis.outbound.size(), "no further message sent yet");

        // the watchdog has to give up waiting and resend the ON message itself
        JUnitUtil.waitFor(() -> lnis.outbound.size() > expectedOutbound, "watchdog resent the command message");
        checkThrownMsgSent();
        JUnitAppender.assertWarnMessageStartsWith("Turnout " + watchdogTurnoutNumber() + " - no reply to command message after");

        // command station finally acknowledges the resent ON message
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size() - 1));
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkThrownOffSent();
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));

        JUnitUtil.waitFor(() -> t.getKnownState() == Turnout.THROWN, "Turnout goes THROWN");
    }

    @Override
    @Test
    public void testDispose() {
        t.setCommandedState(Turnout.CLOSED);    // in case registration with TrafficController

        //is deferred to after first use
        t.dispose();
        assertEquals( 1, numListeners(), "controller listeners remaining");
    }

    @Test
    @Override
    public void testDirectFeedback() {
        t.setFeedbackMode(Turnout.DIRECT);
        assertEquals( Turnout.DIRECT, t.getFeedbackMode(), "Feedback Mode after set");

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());

        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        checkThrownMsgSent();
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkThrownOffSent();
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkThrownOffSent();
        JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        assertEquals(Turnout.THROWN,t.getState());
        assertEquals( Turnout.THROWN, listenStatus,
            "listener notified of change for DIRECT feedback");

        listenStatus = Turnout.UNKNOWN;
        t.setCommandedState(Turnout.CLOSED);
        checkClosedMsgSent();
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkClosedOffSent();
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkClosedOffSent();
        JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        assertEquals(Turnout.CLOSED, t.getState());
        assertEquals( Turnout.CLOSED, listenStatus,
            "listener notified of change for DIRECT feedback");
    }

    @Test
    public void testMonitoringFeedback() {
        assertEquals( Turnout.MONITORING, t.getFeedbackMode(), "Feedback Mode after set");

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());

        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        checkThrownMsgSent();
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
        ((XNetTurnout) t).message(new XNetReply("42 05 02 46"));
        checkThrownOffSent();
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkThrownOffSent();
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        assertEquals(Turnout.THROWN,t.getState());
        assertEquals( Turnout.THROWN, listenStatus,
            "listener notified of change for DIRECT feedback");

        listenStatus = Turnout.UNKNOWN;
        t.setCommandedState(Turnout.CLOSED);
        checkClosedMsgSent();
        ((XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
        ((XNetTurnout) t).message(new XNetReply("42 05 01 46"));
        checkClosedOffSent();
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkClosedOffSent();
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkClosedOffSent();
        JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        assertEquals(Turnout.CLOSED, t.getState());
        assertEquals( Turnout.CLOSED, listenStatus, "listener notified of change for DIRECT feedback");
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        lnis = new XNetInterfaceScaffold(new LenzCommandStation());

        t = new XNetTurnout("XT", 21, lnis);
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @Override
    @AfterEach
    public void tearDown() {
        lnis.terminateThreads();
        lnis = null;
        t.dispose();
        t = null;
        JUnitUtil.tearDown();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XNetTurnoutTest.class);

}
