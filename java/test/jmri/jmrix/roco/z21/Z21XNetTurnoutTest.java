package jmri.jmrix.roco.z21;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Turnout;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTurnout;
import jmri.util.JUnitUtil;

/**
 * Tests for the {@link jmri.jmrix.roco.z21.Z21XNetTurnout} class.
 *
 * @author Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2016
 */
public class Z21XNetTurnoutTest extends jmri.jmrix.lenz.XNetTurnoutTest {

    @Override
    public void checkClosedMsgSent() {
        Assert.assertEquals("closed message", "53 00 14 88 CF",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertEquals("thrown message", "53 00 14 89 CE",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Override
    protected void checkClosedOffSent() {
        Assert.assertEquals("thrown message", "53 00 14 80 C7",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Override
    protected void checkThrownOffSent() {
        Assert.assertEquals("thrown message", "53 00 14 81 C6",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    // Test the Z21XNetTurnout message sequence.
    @Test
    @Override
    public void testXNetTurnoutMsgSequence() {
        t.setFeedbackMode(jmri.Turnout.DIRECT);
        // prepare an interface
        // set closed
        try {
            t.setCommandedState(jmri.Turnout.CLOSED);
        } catch (Exception e) {
            log.error("TO exception: {}", e);
        }
        Assert.assertEquals(Turnout.CLOSED, t.getCommandedState());

        Assert.assertEquals("on message sent", "53 00 14 88 CF",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

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

        while (n == lnis.outbound.size()) {
        } // busy loop.  Wait for 
        // outbound size to change.
        Assert.assertEquals("off message sent", "53 00 14 80 C7",
                lnis.outbound.elementAt(n).toString());

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
        Assert.assertEquals(Turnout.CLOSED, t.getKnownState());
    }

    // Test that property change events are properly sent from the parent
    // to the propertyChange listener (this handles events for one sensor 
    // and twosensor feedback).
    @Test
    @Override
    public void testXNetTurnoutPropertyChange() {
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        t = new Z21XNetTurnout("X", 21, lnis);

        // set thrown
        try {
            t.setCommandedState(jmri.Turnout.THROWN);
        } catch (Exception e) {
            log.error("TO exception: {}", e);
        }
        Assert.assertEquals(Turnout.THROWN, t.getCommandedState());

        t.setFeedbackMode(jmri.Turnout.ONESENSOR);
        jmri.Sensor s = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
        try {
            s.setState(jmri.Sensor.INACTIVE);
            t.provideFirstFeedbackSensor("IS1");
        } catch (Exception x1) {
            log.error("TO exception: {}", x1);
        }
        try {
            s.setState(jmri.Sensor.ACTIVE);
        } catch (Exception x) {
            log.error("TO exception: {}", x);
        }
        // check to see if the turnout state changes.
        Assert.assertEquals(Turnout.THROWN, t.getKnownState());
    }

    @Override
    public void testDirectFeedback() {
        t.setFeedbackMode(Turnout.DIRECT);
        Assert.assertEquals("Feedback Mode after set", Turnout.DIRECT, t.getFeedbackMode());

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
        jmri.util.JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        Assert.assertEquals(Turnout.THROWN,t.getState());
        Assert.assertEquals("listener notified of change for DIRECT feedback", Turnout.THROWN, listenStatus);

        listenStatus = Turnout.UNKNOWN;
        t.setCommandedState(Turnout.CLOSED);
        checkClosedMsgSent();
        ((jmri.jmrix.lenz.XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkClosedOffSent();
        ((XNetTurnout) t).message(new XNetReply("01 04 05"));
        checkClosedOffSent();
        jmri.util.JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        Assert.assertEquals(Turnout.CLOSED,t.getState());
        Assert.assertEquals("listener notified of change for DIRECT feedback", Turnout.CLOSED, listenStatus);
    }

    @Override
    @Test
    public void testMonitoringFeedback() {
        Assert.assertEquals("Feedback Mode after set", Turnout.MONITORING, t.getFeedbackMode());

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
        jmri.util.JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        Assert.assertEquals(Turnout.THROWN,t.getState());
        Assert.assertEquals("listener notified of change for DIRECT feedback", Turnout.THROWN, listenStatus);

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
        jmri.util.JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        Assert.assertEquals(Turnout.CLOSED,t.getState());
        Assert.assertEquals("listener notified of change for DIRECT feedback", Turnout.CLOSED, listenStatus);
    }

    @Test
    @Override
    public void testDispose() {
        t.setCommandedState(jmri.Turnout.CLOSED);    // in case registration with TrafficController

        //is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 1, numListeners());
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

    private final static Logger log = LoggerFactory.getLogger(Z21XNetTurnoutTest.class);

}
