package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import jmri.Turnout;
import jmri.Sensor;
import jmri.InstanceManager;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the {@link jmri.jmrix.lenz.XNetTurnout} class.
 *
 * @author	Bob Jacobsen
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

    @Override
    public void checkThrownMsgSent() {
        Assert.assertEquals("thrown message", "52 05 89 DE",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void checkIncoming() {
        t.setFeedbackMode(Turnout.MONITORING);
        jmri.util.JUnitUtil.waitFor(() -> {
            return t.getFeedbackMode() == Turnout.MONITORING;
        }, "Feedback mode set");

	    listenStatus = Turnout.UNKNOWN;
	    t.addPropertyChangeListener(new Listen());

        // notify the object that somebody else changed it...
        XNetReply m = new XNetReply("42 05 01 46"); // set CLOSED
        ((XNetTurnout) t).message(m);
        jmri.util.JUnitUtil.waitFor(() -> {
            return listenStatus != Turnout.UNKNOWN;
        }, "Turnout state changed");
        Assert.assertEquals("state after CLOSED message",Turnout.CLOSED,t.getKnownState());

	    listenStatus = Turnout.UNKNOWN;

        m = new XNetReply("42 05 02 45"); // set THROWN
        ((XNetTurnout) t).message(m);
        jmri.util.JUnitUtil.waitFor(() -> {
            return listenStatus != Turnout.UNKNOWN;
        }, "Turnout state changed");
        Assert.assertEquals("state after THROWN message",Turnout.THROWN,t.getKnownState());
    }

    // Test the XNetTurnout message sequence.
    @Test
    public void testXNetTurnoutMsgSequence() {
        t.setFeedbackMode(Turnout.DIRECT);
        // set closed
        try {
            t.setCommandedState(Turnout.CLOSED);
        } catch (Exception e) {
            log.error("TO exception: " + e);
        }

        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);

        Assert.assertEquals("on message sent", "52 05 88 DF",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

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

        // the turnout will not set its state until it sees an OK message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = lnis.outbound.size();

        ((jmri.jmrix.lenz.XNetTurnout) t).message(m);

        while (n == lnis.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        Assert.assertEquals("off message sent", "52 05 80 D7",
                lnis.outbound.elementAt(n).toString());

        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        ((jmri.jmrix.lenz.XNetTurnout) t).message(m);

        // no wait here.  The last reply should cause the turnout to
        // set it's state, but it will not cause another reply.
        Assert.assertTrue(t.getKnownState() == Turnout.CLOSED);
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
            log.error("TO exception: " + e);
        }
        Assert.assertTrue(t.getCommandedState() == Turnout.THROWN);

        t.setFeedbackMode(Turnout.ONESENSOR);
        jmri.Sensor s = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
        try {
            s.setState(jmri.Sensor.INACTIVE);
            t.provideFirstFeedbackSensor("IS1");
        } catch (Exception x1) {
            log.error("TO exception: " + x1);
        }
        try {
            s.setState(jmri.Sensor.ACTIVE);
        } catch (Exception x) {
            log.error("TO exception: " + x);
        }
        // check to see if the turnout state changes.
        jmri.util.JUnitUtil.waitFor(() -> {
            return t.getKnownState() == Turnout.THROWN;
        }, "Turnout goes THROWN");
    }

    @Override
    @Test
    public void testDispose() {
        t.setCommandedState(Turnout.CLOSED);    // in case registration with TrafficController

        //is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 1, numListeners());
    }

    @Test
    @Override
    public void testDirectFeedback() throws jmri.JmriException {
        t.setFeedbackMode(Turnout.DIRECT);
        Assert.assertEquals("Feedback Mode after set",Turnout.DIRECT, t.getFeedbackMode());

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        checkThrownMsgSent();
        ((XNetTurnout)t).message(new XNetReply("01 04 05"));
        ((XNetTurnout)t).message(new XNetReply("01 04 05"));
        jmri.util.JUnitUtil.waitFor(() -> {
            return listenStatus != Turnout.UNKNOWN;
        }, "Turnout state changed");
        Assert.assertEquals(t.getState(), Turnout.THROWN);
        Assert.assertEquals("listener notified of change for DIRECT feedback",Turnout.THROWN,listenStatus);

        listenStatus = Turnout.UNKNOWN;
        t.setCommandedState(Turnout.CLOSED);
        checkClosedMsgSent();
        ((XNetTurnout)t).message(new XNetReply("01 04 05"));                            ((XNetTurnout)t).message(new XNetReply("01 04 05"));
        jmri.util.JUnitUtil.waitFor(() -> {
            return listenStatus != Turnout.UNKNOWN;
        }, "Turnout state changed");
        Assert.assertEquals(t.getState(), Turnout.CLOSED);
	Assert.assertEquals("listener notified of change for DIRECT feedback",Turnout.CLOSED,listenStatus);
    }

    @Test
    public void testMonitoringFeedback() throws jmri.JmriException {
        Assert.assertEquals("Feedback Mode after set",Turnout.MONITORING, t.getFeedbackMode());

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        checkThrownMsgSent();
        ((XNetTurnout)t).message(new XNetReply("42 05 02 46"));
        ((XNetTurnout)t).message(new XNetReply("01 04 05"));
        ((XNetTurnout)t).message(new XNetReply("01 04 05"));
        jmri.util.JUnitUtil.waitFor(() -> {
            return listenStatus != Turnout.UNKNOWN;
        }, "Turnout state changed");
        Assert.assertEquals(t.getState(), Turnout.THROWN);
        Assert.assertEquals("listener notified of change for DIRECT feedback",Turnout.THROWN,listenStatus);

        listenStatus = Turnout.UNKNOWN;
        t.setCommandedState(Turnout.CLOSED);
        checkClosedMsgSent();
        ((XNetTurnout)t).message(new XNetReply("42 05 01 46"));
        ((XNetTurnout)t).message(new XNetReply("01 04 05"));                            ((XNetTurnout)t).message(new XNetReply("01 04 05"));
        jmri.util.JUnitUtil.waitFor(() -> {
            return listenStatus != Turnout.UNKNOWN;
        }, "Turnout state changed");
        Assert.assertEquals(t.getState(), Turnout.CLOSED);
	Assert.assertEquals("listener notified of change for DIRECT feedback",Turnout.CLOSED,listenStatus);
    }


    // The minimal setup for log4J
    @Override
    @Before
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

    @After
    public void tearDown() {
        t = null;
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(XNetTurnoutTest.class);

}
