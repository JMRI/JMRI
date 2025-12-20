package jmri.jmrix.lenz;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
