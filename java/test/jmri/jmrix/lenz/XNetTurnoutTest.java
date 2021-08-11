package jmri.jmrix.lenz;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Turnout;
import jmri.util.JUnitUtil;

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
        Assert.assertEquals("closed message", "52 05 88 DF",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    protected void checkClosedOffSent() {
        Assert.assertEquals("closed message OFF", "52 05 80 D7",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        ((jmri.jmrix.lenz.XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertEquals("thrown message", "52 05 89 DE",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    protected void checkThrownOffSent() {
        Assert.assertEquals("thrown message OFF", "52 05 81 D6",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        ((jmri.jmrix.lenz.XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));
    }

    @Test
    public void checkIncoming() {
        t.setFeedbackMode(Turnout.MONITORING);
        jmri.util.JUnitUtil.waitFor(() -> t.getFeedbackMode() == Turnout.MONITORING, "Feedback mode set");

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());

        // notify the object that somebody else changed it...
        XNetReply m = new XNetReply("42 05 01 46"); // set CLOSED
        ((XNetTurnout) t).message(m);
        jmri.util.JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        Assert.assertEquals("state after CLOSED message", Turnout.CLOSED, t.getKnownState());

        listenStatus = Turnout.UNKNOWN;

        m = new XNetReply("42 05 02 45"); // set THROWN
        ((XNetTurnout) t).message(m);
        jmri.util.JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        Assert.assertEquals("state after THROWN message", Turnout.THROWN, t.getKnownState());
    }

    // Test the XNetTurnout message sequence.
    @Test
    public void testXNetTurnoutMsgSequence() {
        t.setFeedbackMode(Turnout.DIRECT);
        // set closed
        try {
            t.setCommandedState(Turnout.CLOSED);
        } catch (Exception e) {
            log.error("TO exception: {}", e);
        }

        Assert.assertEquals(Turnout.CLOSED, t.getCommandedState());

        Assert.assertEquals("on message sent", "52 05 88 DF",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        ((jmri.jmrix.lenz.XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        // notify that the command station received the reply
        XNetReply m = new XNetReply();
        m.setElement(0, 0x42);
        m.setElement(1, 0x05);
        m.setElement(2, 0x01);     // set CLOSED
        m.setElement(3, 0x46);

        int n = lnis.outbound.size();

        ((jmri.jmrix.lenz.XNetTurnout) t).message(m);

        while (n == lnis.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
        Assert.assertEquals("off message sent", "52 05 80 D7",
                lnis.outbound.elementAt(n).toString());
        ((jmri.jmrix.lenz.XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        ((jmri.jmrix.lenz.XNetTurnout) t).message(lnis.outbound.elementAt(n-1));

        // the turnout will not set its state until it sees an OK message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        ((jmri.jmrix.lenz.XNetTurnout) t).message(m);

      //  while (n == lnis.outbound.size()) {
      //  } // busy loop.  Wait for
        // outbound size to change.

        checkClosedOffSent();
        ((jmri.jmrix.lenz.XNetTurnout) t).message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        ((jmri.jmrix.lenz.XNetTurnout) t).message(m);

        // no wait here.  The last reply should cause the turnout to
        // set it's state, but it will not cause another reply.
        Assert.assertEquals(Turnout.CLOSED, t.getKnownState());
    }

    // Test that property change events are properly sent from the parent
    // to the propertyChange listener (this handles events for one sensor
    // and twosensor feedback).
    @Test
    public void testXNetTurnoutPropertyChange() {
        // set thrown
        try {
            t.setCommandedState(Turnout.THROWN);
        } catch (Exception e) {
            log.error("TO exception: {}", e);
        }
        Assert.assertEquals(Turnout.THROWN, t.getCommandedState());

        t.setFeedbackMode(Turnout.ONESENSOR);
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
        jmri.util.JUnitUtil.waitFor(() -> t.getKnownState() == Turnout.THROWN, "Turnout goes THROWN");
    }

    @Override
    @Test
    public void testDispose() {
        t.setCommandedState(Turnout.CLOSED);    // in case registration with TrafficController

        //is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 1, numListeners());
    }
    
    @Override
    @Test
    public void testDirectFeedbackClosed() {
        testDirectFeedback();
    }
    
    @Override
    @Test
    public void testDirectFeedbackThrown() {
        testDirectFeedback();
    }

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
        Assert.assertEquals(Turnout.CLOSED, t.getState());
        Assert.assertEquals("listener notified of change for DIRECT feedback", Turnout.CLOSED, listenStatus);
    }

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
        Assert.assertEquals(Turnout.CLOSED, t.getState());
        Assert.assertEquals("listener notified of change for DIRECT feedback", Turnout.CLOSED, listenStatus);
    }

    @Override
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
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

    private final static Logger log = LoggerFactory.getLogger(XNetTurnoutTest.class);

}
