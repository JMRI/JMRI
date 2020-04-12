package jmri.jmrix.lenz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import jmri.util.JUnitUtil;
import jmri.Turnout;
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
        // OK to the 'thrown' accessory command.
        ((XNetTurnout)t).message(new XNetReply("01 04 05"));
        // OK to the first OFF message
        ((XNetTurnout)t).message(new XNetReply("01 04 05"));
        // OK to the second OFF message
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
    
    /**
     * Checks that the turnout produces exactly one off message for
     * a reply, although it receives it several times. 
     * @throws Exception 
     */
    @Test
    public void monitor_testFirstFeedbackMessageOff() throws Exception {
        t.setFeedbackMode(Turnout.MONITORING);
        // set closed
        t.setCommandedState(Turnout.CLOSED);
        XNetReply reply = new XNetReply("42 05 01 46");
        checkFirstReplyMessageOff(reply, 1);
    }
    
    /**
     * Checks that the turnout produces exactly one off message for
     * a reply, although it receives it several times. 
     * @throws Exception 
     */
    @Test
    public void monitor_testTurnoutFirstOKMessageOff() throws Exception {
        t.setFeedbackMode(Turnout.MONITORING);
        // set closed
        t.setCommandedState(Turnout.CLOSED);

        XNetReply reply = new XNetReply("01 04 05");
        checkFirstReplyMessageOff(reply, 1);
    }
    
    private void checkFirstReplyMessageOff(XNetReply reply, int expectOffs) throws Exception {
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, ((XNetTurnout)t).internalState);
        
        lnis.outbound.clear();
        
        // block potential asynchronous add, until we end with message() excecution
        synchronized (lnis.outbound) {
            ((XNetTurnout)t).message(reply);
            assertEquals("Turnout must be in OFFSENT state", XNetTurnout.OFFSENT, ((XNetTurnout)t).internalState);
            assertTrue("First OFF message must be delayed", lnis.outbound.isEmpty());

            ((XNetTurnout)t).message(reply);
            assertTrue("No other OFF message must be posted directly.", lnis.outbound.isEmpty());
        }
        jmri.util.JUnitUtil.waitFor(() -> {
            return !lnis.outbound.isEmpty();
        }, "OFF Message sent");
        
        // allow for additional grace period just to be sure the 2nd message does not appear.
        Thread.sleep(500);
        assertEquals("Sigle feedback must produce proper number of OFFs", expectOffs, lnis.outbound.size());
        for (int i = 0; i < expectOffs; i++) {
            checkOffMessageAt(i);
        }
    }
    
    private void checkOffMessageAt(int index) {
        assertTrue("OFF command should be posted at #" + (index + 1),
                lnis.outbound.get(index).getElement(0) == 0x52 &&
                lnis.outbound.get(index).getElement(2) == 0x80
        );
    }
    
    private void checkTwoOffSequence(XNetReply reply) throws Exception {
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, ((XNetTurnout)t).internalState);
        
        lnis.outbound.clear();
        
        // block potential asynchronous add, until we end with message() excecution
        synchronized (lnis.outbound) {
            ((XNetTurnout)t).message(reply);
            assertEquals("Turnout must be in OFFSENT state", XNetTurnout.OFFSENT, ((XNetTurnout)t).internalState);
            assertTrue("First OFF message must be delayed", lnis.outbound.isEmpty());

            ((XNetTurnout)t).message(new XNetReply("01 04 05"));
            assertEquals("2nd OFF message must be posted directly", 1, lnis.outbound.size());
        }
        jmri.util.JUnitUtil.waitFor(() -> {
            return !lnis.outbound.isEmpty();
        }, "OFF Message sent");
        
        // allow for additional grace period just to be sure the 2nd message does not appear.
        Thread.sleep(500);
        assertEquals("Sigle feedback must produce proper number of OFFs", 2, lnis.outbound.size());
        checkOffMessageAt(0);
        checkOffMessageAt(1);
    }
    
    /**
     * After command issued, the sequence OK, Feedback must generate 2 OFFs,
     * for the correct output.
     * @throws Exception 
     */
    @Test
    public void monitor_testOKAndFeedbackGenerateOffs() throws Exception {
        t.setFeedbackMode(Turnout.MONITORING);
        // set closed
        t.setCommandedState(Turnout.CLOSED);
        
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, ((XNetTurnout)t).internalState);
        XNetReply reply = new XNetReply("01 04 05");
        
        lnis.outbound.clear();
        checkTwoOffSequence(reply);
    }

    /**
     * Check sequence feedback, feedback, OK. Should generate 3 offs in total,
     * since after requirement 2 OFFs is satisfied, the OK that terminates
     * still has to come.
     * @throws Exception 
     */
    @Test
    public void monitor_testOKAndMoreFeedbacksGenerateEachOneOff() throws Exception {
        t.setFeedbackMode(Turnout.MONITORING);
        // set closed
        t.setCommandedState(Turnout.CLOSED);
        
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, ((XNetTurnout)t).internalState);
        XNetReply reply = new XNetReply("01 04 05");
        
        lnis.outbound.clear();
        int sz;
        synchronized (lnis.outbound) {
            ((XNetTurnout)t).message(reply);
            assertEquals("Turnout must be in OFFSENT state", XNetTurnout.OFFSENT, ((XNetTurnout)t).internalState);
            assertTrue("First OFF message must be delayed", lnis.outbound.isEmpty());

            reply = new XNetReply("42 05 01 46");
            ((XNetTurnout)t).message(reply);
            assertFalse("Second OFF should be posted immediately", lnis.outbound.isEmpty());
            checkOffMessageAt(0);

            reply = new XNetReply("42 05 01 46");
            ((XNetTurnout)t).message(reply);
            assertEquals("Second OFF should be posted immediately", 2, lnis.outbound.size());
            checkOffMessageAt(1);
            sz = lnis.outbound.size();
        }
        jmri.util.JUnitUtil.waitFor(() -> {
            return lnis.outbound.size() > sz;
        }, "Scheduled Message sent");
        assertEquals("Total 3 OFF messages must be sent", 3, lnis.outbound.size());
        checkOffMessageAt(2);
    }
    
    /**
     * Checks that despite coming feedbacks, it's sufficient to receive single OK
     * after OFF message is sent to stop OFFing.
     * @throws Exception 
     */
    @Test
    public void monitor_testOKAndManyFeedbacksLimitedOffs() throws Exception {
        t.setFeedbackMode(Turnout.MONITORING);
        // set closed
        t.setCommandedState(Turnout.CLOSED);
        
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, ((XNetTurnout)t).internalState);
        XNetReply reply = new XNetReply("01 04 05");
        
        lnis.outbound.clear();
        int sz;
        synchronized (lnis.outbound) {
            ((XNetTurnout)t).message(reply);
            assertEquals("Turnout must be in OFFSENT state", XNetTurnout.OFFSENT, ((XNetTurnout)t).internalState);
            assertTrue("First OFF message must be delayed", lnis.outbound.isEmpty());

            reply = new XNetReply("42 05 01 46");
            ((XNetTurnout)t).message(reply);
            assertFalse("Second OFF should be posted immediately", lnis.outbound.isEmpty());
            checkOffMessageAt(0);
            assertNotEquals("Turnout cannot IDLE after just one OFF", XNetTurnout.IDLE, ((XNetTurnout)t).internalState);
            
            reply = new XNetReply("01 04 05");
            ((XNetTurnout)t).message(reply);
            assertEquals("Second OFF should be posted immediately", 1, lnis.outbound.size());
            assertEquals("Turnout should go IDLE after OFF + OK", XNetTurnout.IDLE, ((XNetTurnout)t).internalState);

            reply = new XNetReply("42 05 01 46");
            ((XNetTurnout)t).message(reply);
            assertEquals("Feedback after OK must NOT send another OFF", 1, lnis.outbound.size());
            assertEquals("Turnout should remain IDLE", XNetTurnout.IDLE, ((XNetTurnout)t).internalState);

            sz = lnis.outbound.size();
        }
        jmri.util.JUnitUtil.waitFor(() -> {
            return lnis.outbound.size() > sz;
        }, "Scheduled Message sent");
        assertEquals("Just 2 OFF messages must be sent", 2, lnis.outbound.size());
    }
    
    @Test
    public void monitor_testFeedbackAndOKGenerateOffs() throws Exception {
        t.setFeedbackMode(Turnout.MONITORING);
        // set closed
        t.setCommandedState(Turnout.CLOSED);
        
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, ((XNetTurnout)t).internalState);
        XNetReply reply = new XNetReply("42 05 01 46");
        
        lnis.outbound.clear();
        int sz;
        synchronized (lnis.outbound) {
            ((XNetTurnout)t).message(reply);
            assertEquals("Turnout must be in OFFSENT state", XNetTurnout.OFFSENT, ((XNetTurnout)t).internalState);
            assertTrue("First OFF message must be delayed", lnis.outbound.isEmpty());

            reply = new XNetReply("01 04 05");
            ((XNetTurnout)t).message(reply);
            assertFalse("Second OFF should be posted immediately", lnis.outbound.isEmpty());
            checkOffMessageAt(0);
            
            // final OK to the 2nd OFF sent
            reply = new XNetReply("01 04 05");
            ((XNetTurnout)t).message(reply);
            sz = lnis.outbound.size();
        }
        jmri.util.JUnitUtil.waitFor(() -> {
            return lnis.outbound.size() > sz;
        }, "Scheduled Message sent");
        assertEquals("Total 2 OFF messages must be sent", 2, lnis.outbound.size());
        assertEquals("Turnout must be IDLE after receiving OK", XNetTurnout.IDLE, ((XNetTurnout)t).internalState);
    }
    
    /**
     * Check that one OK alone produces 2 OFFs. The complete checked sequence is
     * <ul>
     * <li> -> accessory request ON
     * <li> &lt;- OK
     * <li> -> accessory request OFF
     * <li> &lt;- OK
     * <li> -> accessory request OFF
     * <li> &lt;- OK
     * <li> NO furhter OFFs sent.
     * </ul>
     * @throws Exception 
     */
    @Test
    public void monitor_testOKAloneProduces2OFFs() throws Exception {
        t.setFeedbackMode(Turnout.MONITORING);
        // set closed
        t.setCommandedState(Turnout.CLOSED);
        
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, ((XNetTurnout)t).internalState);
        XNetReply reply = new XNetReply("01 04 05");

        lnis.outbound.clear();
        int sz;
        synchronized (lnis.outbound) {
            ((XNetTurnout)t).message(reply);
            assertEquals("Turnout must be in OFFSENT state", XNetTurnout.OFFSENT, ((XNetTurnout)t).internalState);
            assertTrue("First OFF message must be delayed", lnis.outbound.isEmpty());

            sz = lnis.outbound.size();
        }

        jmri.util.JUnitUtil.waitFor(() -> {
            return lnis.outbound.size() > sz;
        }, "Scheduled Message sent");
        assertEquals("Just one OFF was delay-sent", 1, lnis.outbound.size());
        checkOffMessageAt(0);

        reply = new XNetReply("01 04 05");
        synchronized (lnis.outbound) {
            ((XNetTurnout)t).message(reply);
            assertEquals("Second OFF must be sent immediately", 2, lnis.outbound.size());
            assertNotEquals("Turnout must be in OFFSENT state", XNetTurnout.IDLE, ((XNetTurnout)t).internalState);
            checkOffMessageAt(1);
        }
        reply = new XNetReply("01 04 05");
        synchronized (lnis.outbound) {
            ((XNetTurnout)t).message(reply);
            assertEquals("No futher OFFs after second", 2, lnis.outbound.size());
            assertEquals("Turnout must be in IDLE state", XNetTurnout.IDLE, ((XNetTurnout)t).internalState);
        }
        // allow potential posted OFF to enter:
        Thread.sleep(100);
        assertEquals("No futher delayed OFFs after second", 2, lnis.outbound.size());
    }
    
    /**
     * Checks that DIRECT mode turnout sends 2 OFFs after the first feedback
     * to its address.
     * @throws Exception 
     */
    @Test
    public void direct_testSendOffAfterFeedback() throws Exception {
        t.setFeedbackMode(Turnout.DIRECT);
        // set closed
        t.setCommandedState(Turnout.CLOSED);
        lnis.outbound.clear();
        
        XNetReply reply = new XNetReply("42 05 01 46");
        checkTwoOffSequence(reply);
    }
    
    /**
     * Checks that DIRECT mode turnout sends 2 OFFs after the first feedback
     * to its address.
     * @throws Exception 
     */
    @Test
    public void direct_testSendOffAfterOK() throws Exception {
        t.setFeedbackMode(Turnout.DIRECT);
        // set closed
        t.setCommandedState(Turnout.CLOSED);
        lnis.outbound.clear();
        
        XNetReply reply = new XNetReply("01 04 05");
        checkTwoOffSequence(reply);
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
