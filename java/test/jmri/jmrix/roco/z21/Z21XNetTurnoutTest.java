package jmri.jmrix.roco.z21;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.Turnout;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTurnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the {@link jmri.jmrix.roco.z21.Z21XNetTurnout} class.
 *
 * @author Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2016
 */
public class Z21XNetTurnoutTest extends jmri.jmrix.lenz.XNetTurnoutTest {

    @Override
    public void checkClosedMsgSent() {
        assertEquals( "53 00 14 88 CF",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "closed message");
    }

    @Override
    public void checkThrownMsgSent() {
        assertEquals( "53 00 14 89 CE",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "thrown message");
    }

    @Override
    protected void checkClosedOffSent() {
        assertEquals( "53 00 14 80 C7",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "thrown message");
    }

    @Override
    protected void checkThrownOffSent() {
        assertEquals( "53 00 14 81 C6",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "thrown message");
    }

    // Test the Z21XNetTurnout message sequence.
    @Test
    @Override
    public void testXNetTurnoutMsgSequence() {
        t.setFeedbackMode(jmri.Turnout.DIRECT);
        // prepare an interface
        // set closed
        assertDoesNotThrow( () -> t.setCommandedState( Turnout.CLOSED),
            "TO exception: ");

        assertEquals(Turnout.CLOSED, t.getCommandedState());

        assertEquals( "53 00 14 88 CF",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "on message sent");

        ((Z21XNetTurnout)t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        // notify that the command station received the reply
        XNetReply m = new XNetReply();
        m.setElement(0, 0x43);
        m.setElement(1, 0x00);
        m.setElement(2, 0x14);
        m.setElement(3, 0x00);     // set CLOSED
        m.setElement(4, 0x56);

        int n = lnis.outbound.size();

        ((jmri.jmrix.roco.z21.Z21XNetTurnout) t).message(m);

        JUnitUtil.waitFor( () -> n != lnis.outbound.size(), "Wait for outbound size to change");

        assertEquals( "53 00 14 80 C7",
                lnis.outbound.elementAt(n).toString(),
            "off message sent");

        ((Z21XNetTurnout)t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        // the turnout will not set its state until it sees a reply message.
        m = new XNetReply();
        m.setElement(0, 0x43);
        m.setElement(1, 0x00);
        m.setElement(2, 0x14);
        m.setElement(3, 0x00);
        m.setElement(4, 0x56);

        ((jmri.jmrix.roco.z21.Z21XNetTurnout) t).message(m);

        // no wait here.  The last reply should cause the turnout to 
        // set it's state, but it will not cause another reply.
        assertEquals(Turnout.CLOSED, t.getKnownState());
    }

    // Test that property change events are properly sent from the parent
    // to the propertyChange listener (this handles events for one sensor 
    // and twosensor feedback).
    @Test
    @Override
    public void testXNetTurnoutPropertyChange() {
        // prepare an interface
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        t = new Z21XNetTurnout("X", 21, lnis);

        // set thrown
        assertDoesNotThrow( () -> t.setCommandedState( Turnout.THROWN),
            "TO exception: ");
        assertEquals(Turnout.THROWN, t.getCommandedState());

        t.setFeedbackMode( Turnout.ONESENSOR);
        jmri.Sensor s = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
        assertDoesNotThrow( () -> {
                s.setState(jmri.Sensor.INACTIVE);
                t.provideFirstFeedbackSensor("IS1");
            }, "TO exception: ");

        assertDoesNotThrow( () -> s.setState(jmri.Sensor.ACTIVE),
            "SO exception: ");

        // check to see if the turnout state changes.
        assertEquals(Turnout.THROWN, t.getKnownState());
    }

    @Test
    @Override
    public void testDirectFeedback() {
        t.setFeedbackMode(Turnout.DIRECT);
        assertEquals( Turnout.DIRECT, t.getFeedbackMode(),
            "Feedback Mode after set");

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());

        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        checkThrownMsgSent();
        ((jmri.jmrix.lenz.XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
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
        ((jmri.jmrix.lenz.XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkClosedOffSent();
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkClosedOffSent();
        JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        assertEquals(Turnout.CLOSED,t.getState());
        assertEquals( Turnout.CLOSED, listenStatus,
            "listener notified of change for DIRECT feedback");
    }

    @Override
    @Test
    public void testMonitoringFeedback() {
        assertEquals( Turnout.MONITORING, t.getFeedbackMode(),
            "Feedback Mode after set");

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());

        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        checkThrownMsgSent();
        ((jmri.jmrix.lenz.XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
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
        ((jmri.jmrix.lenz.XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
        ((XNetTurnout) t).message(new XNetReply("42 05 01 46"));
        checkClosedOffSent();
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkClosedOffSent();
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkClosedOffSent();
        JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        assertEquals(Turnout.CLOSED,t.getState());
        assertEquals( Turnout.CLOSED, listenStatus,
            "listener notified of change for DIRECT feedback");
    }

    @Test
    @Override
    public void testDispose() {
        t.setCommandedState( Turnout.CLOSED);
        // in case registration with TrafficController is deferred to after first use

        t.dispose();
        assertEquals( 1, numListeners(), "controller listeners remaining");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        lnis = new XNetInterfaceScaffold(new RocoZ21CommandStation());

        t = new Z21XNetTurnout("X", 21, lnis);
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @AfterEach
    @Override
    public void tearDown() {
        lnis.terminateThreads();
        lnis = null;
        JUnitUtil.tearDown();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Z21XNetTurnoutTest.class);

}
