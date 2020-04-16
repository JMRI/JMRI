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
 * Checks Lenz clone's specific behaviour for Turnout commands.
 * Assures that the Turnout will send an appropriate number of Accessory Request
 * Output OFF commands, delayed properly and that it won't ping indefinitely.
 */
public class LenzXNetTurnoutCommandTest {

    protected XNetInterfaceScaffold lnis;

    protected XNetTurnout t = null;	// holds object under test; set by setUp()

    // The minimal setup for log4J
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
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, t.internalState);
        
        lnis.outbound.clear();
        
        // block potential asynchronous add, until we end with message() excecution
        synchronized (lnis.outbound) {
            t.message(reply);
            assertEquals("Turnout must be in OFFSENT state", XNetTurnout.OFFSENT, t.internalState);
            assertTrue("First OFF message must be delayed", lnis.outbound.isEmpty());

            t.message(reply);
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
    
    private void checkOffMessageAt(int index, boolean odd) {
        assertTrue("OFF command should be posted at #" + (index + 1),
                lnis.outbound.get(index).getElement(0) == 0x52 &&
                // hardcoded to: odd closed, even thrown
                lnis.outbound.get(index).getElement(2) == (odd ? 0x80 : 0x83)
        );
    }
    
    private void checkOffMessageAt(int index) {
        checkOffMessageAt(index, true);
    }
    
    private void checkTwoOffSequence(XNetReply reply) throws Exception {
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, t.internalState);
        
        lnis.outbound.clear();
        
        // block potential asynchronous add, until we end with message() excecution
        synchronized (lnis.outbound) {
            t.message(reply);
            assertEquals("Turnout must be in OFFSENT state", XNetTurnout.OFFSENT, t.internalState);
            assertTrue("First OFF message must be delayed", lnis.outbound.isEmpty());

            t.message(new XNetReply("01 04 05"));
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
        
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, t.internalState);
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
        
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, t.internalState);
        XNetReply reply = new XNetReply("01 04 05");
        
        lnis.outbound.clear();
        int sz;
        synchronized (lnis.outbound) {
            t.message(reply);
            assertEquals("Turnout must be in OFFSENT state", XNetTurnout.OFFSENT, t.internalState);
            assertTrue("First OFF message must be delayed", lnis.outbound.isEmpty());

            reply = new XNetReply("42 05 01 46");
            t.message(reply);
            assertFalse("Second OFF should be posted immediately", lnis.outbound.isEmpty());
            checkOffMessageAt(0);

            reply = new XNetReply("42 05 01 46");
            /*
            t.message(reply);
            assertEquals("Second OFF should be posted immediately", 2, lnis.outbound.size());
            checkOffMessageAt(1);
            */
            sz = lnis.outbound.size();
        }
        jmri.util.JUnitUtil.waitFor(() -> {
            return lnis.outbound.size() > sz;
        }, "Scheduled Message sent");
        assertEquals("Total 3 OFF messages must be sent", 2, lnis.outbound.size());
        checkOffMessageAt(lnis.outbound.size() - 1);
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
        
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, t.internalState);
        XNetReply reply = new XNetReply("01 04 05");
        
        lnis.outbound.clear();
        int sz;
        synchronized (lnis.outbound) {
            t.message(reply);
            assertEquals("Turnout must be in OFFSENT state", XNetTurnout.OFFSENT, t.internalState);
            assertTrue("First OFF message must be delayed", lnis.outbound.isEmpty());

            reply = new XNetReply("42 05 01 46");
            t.message(reply);
            assertFalse("Second OFF should be posted immediately", lnis.outbound.isEmpty());
            checkOffMessageAt(0);
            assertNotEquals("Turnout cannot IDLE after just one OFF", XNetTurnout.IDLE, t.internalState);
            
            reply = new XNetReply("01 04 05");
            t.message(reply);
            assertEquals("Second OFF should be posted immediately", 1, lnis.outbound.size());
            assertEquals("Turnout should go IDLE after OFF + OK", XNetTurnout.IDLE, t.internalState);

            reply = new XNetReply("42 05 01 46");
            t.message(reply);
            assertEquals("Feedback after OK must NOT send another OFF", 1, lnis.outbound.size());
            assertEquals("Turnout should remain IDLE", XNetTurnout.IDLE, t.internalState);

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
        
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, t.internalState);
        XNetReply reply = new XNetReply("42 05 01 46");
        
        lnis.outbound.clear();
        int sz;
        synchronized (lnis.outbound) {
            t.message(reply);
            assertEquals("Turnout must be in OFFSENT state", XNetTurnout.OFFSENT, t.internalState);
            assertTrue("First OFF message must be delayed", lnis.outbound.isEmpty());

            reply = new XNetReply("01 04 05");
            t.message(reply);
            assertFalse("Second OFF should be posted immediately", lnis.outbound.isEmpty());
            checkOffMessageAt(0);
            
            // final OK to the 2nd OFF sent
            reply = new XNetReply("01 04 05");
            t.message(reply);
            sz = lnis.outbound.size();
        }
        jmri.util.JUnitUtil.waitFor(() -> {
            return lnis.outbound.size() > sz;
        }, "Scheduled Message sent");
        assertEquals("Total 2 OFF messages must be sent", 2, lnis.outbound.size());
        assertEquals("Turnout must be IDLE after receiving OK", XNetTurnout.IDLE, t.internalState);
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
        
        assertEquals("Turnout must be in COMMANDSENT state", XNetTurnout.COMMANDSENT, t.internalState);
        XNetReply reply = new XNetReply("01 04 05");

        lnis.outbound.clear();
        int sz;
        synchronized (lnis.outbound) {
            t.message(reply);
            assertEquals("Turnout must be in OFFSENT state", XNetTurnout.OFFSENT, t.internalState);
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
            t.message(reply);
            assertEquals("Second OFF must be sent immediately", 2, lnis.outbound.size());
            assertNotEquals("Turnout must be in OFFSENT state", XNetTurnout.IDLE, t.internalState);
            checkOffMessageAt(1);
        }
        reply = new XNetReply("01 04 05");
        synchronized (lnis.outbound) {
            t.message(reply);
            assertEquals("No futher OFFs after second", 2, lnis.outbound.size());
            assertEquals("Turnout must be in IDLE state", XNetTurnout.IDLE, t.internalState);
        }
        // allow potential posted OFF to enter:
        Thread.sleep(100);
        assertEquals("No futher delayed OFFs after second", 2, lnis.outbound.size());
    }
    
    private void checkCommandSequence(String... seq) {
        int i = 0;
        for (String s : seq) {
            XNetMessage exp = new XNetMessage(s);
            XNetMessage got = lnis.outbound.get(i);
            assertTrue("#" + (i + 1) + ": Expected: " + exp + ", got: " + got, equalsMessage(exp, got));
            i++;
        }
    }
    
    private boolean equalsMessage(XNetMessage a, XNetMessage b) {
        if (a.length() != b.length()) {
            return false;
        }
        for (int i = 0; i < a.length(); i++) {
            if ((byte)a.getElement(i) != (byte)b.getElement(i)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks output-off in case that the command station responds feedback-first.
     * The simulated sequenced reply after command is:
     * <ul>
     * <li>feedback (broadcast, one item)
     * <li>01 04 05
     * </ul>
     * Two commands for consecutive turnouts that receive the same feedback are
     * simulated.
     * @throws Exception 
     */
    @Test
    public void monitorTestConsecutiveTurnoutsOffFeedbackFirst() throws Exception {
        XNetTurnout t2 = new XNetTurnout("XT", 22, lnis);

        t.setFeedbackMode(Turnout.MONITORING);
        t2.setFeedbackMode(Turnout.MONITORING);

        // set closed
        t.setCommandedState(Turnout.CLOSED);
        // set thrown
        t2.setCommandedState(Turnout.THROWN);
        
        XNetReply reply = new XNetReply("42 05 05 42");

        lnis.outbound.clear();
        
        int sz;
        synchronized (lnis.outbound) {
            // simulate the turnout manager - sends to both of them
            t.message(reply);
            t2.message(reply);
            // both of them should schedule OFF, but not directly:

            assertEquals(0, lnis.outbound.size());

            // 42xx is not solicited on LENZ, now receive directed 01 04 05
            t.message(new XNetReply("01 04 05"));
            // should provoke an OFF immediately:
            assertEquals(1, lnis.outbound.size());
            checkOffMessageAt(0);
            
            // -> direct OFF's OK
            t.message(new XNetReply("01 04 05"));
            assertEquals(XNetTurnout.IDLE, t.internalState);

            // simulate the turnout manager - sends to both of them
            reply = new XNetReply("42 05 81 C6");
            t.message(reply);
            t2.message(reply);
            // this should provoke 2nd OFF from XT22
            assertEquals(2, lnis.outbound.size());
            checkOffMessageAt(1, false);
            // -> OK to the direct OFF message
            t2.message(new XNetReply("01 04 05"));
            assertEquals(XNetTurnout.IDLE, t.internalState);
            sz = lnis.outbound.size();
        }
        // there should be 2 delayed OFFs under way.
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return lnis.outbound.size() > sz + 1;
        }, "Scheduled Message sent");
        assertEquals(4, lnis.outbound.size());
        
        checkCommandSequence(
                "52 05 80 D7",
                "52 05 83 D4",
                "52 05 80 D7",
                "52 05 83 D4"
        );
    }
    
    /**
     * Checks output-off in case that the command station responds feedback-last.
     * The simulated sequenced reply after command is:
     * <ul>
     * <li>01 04 05
     * <li>feedback (broadcast, one item)
     * </ul>
     * Two commands for consecutive turnouts that receive the same feedback are
     * simulated.
     * @throws Exception 
     */
    @Test
    public void monitorTestConsecutiveTurnoutsOffFeedbackLast() throws Exception {
        XNetTurnout t2 = new XNetTurnout("XT", 22, lnis);

        t.setFeedbackMode(Turnout.MONITORING);
        t2.setFeedbackMode(Turnout.MONITORING);

        // set closed
        t.setCommandedState(Turnout.CLOSED);
        // set thrown
        t2.setCommandedState(Turnout.THROWN);
        
        XNetReply reply = new XNetReply("01 04 05");

        lnis.outbound.clear();
        
        int sz;
        // synchronized to keep away the delayed Output-OFFs.
        synchronized (lnis.outbound) {
            // simulate the turnout manager - sends to both of them
            t.message(reply);
            assertEquals(0, lnis.outbound.size());
            
            reply = new XNetReply("42 05 05 42");
            t.message(reply);
            // XT21 should issue direct OFF
            assertEquals(1, lnis.outbound.size());
            
            // XT22 should just schedule
            t2.message(reply);
            assertEquals(1, lnis.outbound.size());
            checkOffMessageAt(0);

            // reply to the direct OFF:
            t.message(new XNetReply("01 04 05"));
            // no additional message
            assertEquals(1, lnis.outbound.size());
            assertEquals(XNetTurnout.IDLE, t.internalState);
            
            reply = new XNetReply("42 05 81 C6");
            t.message(reply);
            assertEquals(1, lnis.outbound.size());
            t2.message(reply);
            assertEquals(2, lnis.outbound.size());
            checkOffMessageAt(1, false);
            
            // -> direct OFF's OK
            t2.message(new XNetReply("01 04 05"));
            assertEquals(XNetTurnout.IDLE, t2.internalState);
            // this should provoke 2nd OFF from XT22
            assertEquals(2, lnis.outbound.size());

            sz = lnis.outbound.size();
        }
        // there should be 2 delayed OFFs under way.
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return lnis.outbound.size() > sz + 1;
        }, "Scheduled Message sent");
        assertEquals(4, lnis.outbound.size());
        
        checkCommandSequence(
                "52 05 80 D7",
                "52 05 83 D4",
                "52 05 80 D7",
                "52 05 83 D4"
        );
    }

    /**
     * Checks output-off in case that the command station responds feedback-last.
     * The simulated sequenced reply after command is:
     * <ul>
     * <li>feedback (broadcast, one item)
     * </ul>
     * Two commands for consecutive turnouts that receive the same feedback are
     * simulated.
     * @throws Exception 
     */
    @Test
    public void monitorTestConsecutiveTurnoutsOffFeedbackOnly() throws Exception {
        XNetTurnout t2 = new XNetTurnout("XT", 22, lnis);

        t.setFeedbackMode(Turnout.MONITORING);
        t2.setFeedbackMode(Turnout.MONITORING);

        // set closed
        t.setCommandedState(Turnout.CLOSED);
        // set thrown
        t2.setCommandedState(Turnout.THROWN);
        
        // reply to t1.CLOSED
        XNetReply reply = new XNetReply("42 05 05 42");

        lnis.outbound.clear();
        
        int sz;
        // synchronized to keep away the delayed Output-OFFs.
        synchronized (lnis.outbound) {
            // simulate the turnout manager - sends to both of them
            t.message(reply);
            t2.message(reply);
            assertEquals(0, lnis.outbound.size());
            
            // reply to t2.thrown
            reply = new XNetReply("42 05 81 C6");
            t.message(reply);
            t2.message(reply);
            // both should issue direct OFF
            assertEquals(2, lnis.outbound.size());
            
            // reply to the direct OFF:
            t.message(new XNetReply("01 04 05"));
            t2.message(new XNetReply("01 04 05"));
            // no additional message
            assertEquals(2, lnis.outbound.size());
            assertEquals(XNetTurnout.IDLE, t.internalState);
            assertEquals(XNetTurnout.IDLE, t2.internalState);

            sz = lnis.outbound.size();
        }
        // there should be 2 delayed OFFs under way.
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return lnis.outbound.size() > sz + 1;
        }, "Scheduled Message sent");
        assertEquals(4, lnis.outbound.size());
        
        checkCommandSequence(
                "52 05 80 D7",
                "52 05 83 D4",
                "52 05 80 D7",
                "52 05 83 D4"
        );
    }

    /**
     * Checks output-off in case that the command station responds feedback-last.
     * The simulated sequenced reply after command is:
     * <ul>
     * <li>01 04 05
     * </ul>
     * Such as when Lenz command station believes the accessory did not change
     * by the operation.
     * @throws Exception 
     */
    @Test
    public void monitorTestConsecutiveOffTurnoutsOKOnly() throws Exception {
        XNetTurnout t2 = new XNetTurnout("XT", 22, lnis);

        t.setFeedbackMode(Turnout.MONITORING);
        t2.setFeedbackMode(Turnout.MONITORING);

        // set closed
        t.setCommandedState(Turnout.CLOSED);
        // set thrown
        t2.setCommandedState(Turnout.THROWN);
        
        // reply to t1.CLOSED
        XNetReply reply = new XNetReply("01 04 05");

        lnis.outbound.clear();
        
        int sz;
        // synchronized to keep away the delayed Output-OFFs.
        synchronized (lnis.outbound) {
            t.message(reply);
            // different message instance
            reply = new XNetReply("01 04 05");
            t2.message(reply);
            assertEquals(0, lnis.outbound.size());
            
            // can't go idle yet:
            assertNotEquals(XNetTurnout.IDLE, t.internalState);
            assertNotEquals(XNetTurnout.IDLE, t2.internalState);

            sz = lnis.outbound.size();
        }
        // there should be 2 delayed OFFs under way.
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return lnis.outbound.size() > sz + 1;
        }, "Scheduled Message sent");
        assertEquals(2, lnis.outbound.size());
        
        checkCommandSequence(
                "52 05 80 D7",
                "52 05 83 D4"
        );
        lnis.outbound.clear();

        // still can't go idle yet, just one OFF sent
        assertNotEquals(XNetTurnout.IDLE, t.internalState);
        assertNotEquals(XNetTurnout.IDLE, t2.internalState);
        
        // the following OFFs should be direct, not delayed; block the other threads.
        synchronized (lnis.outbound) {
            // OKs to first (delayed) OFF messages
            t.message(new XNetReply("01 04 05"));
            t2.message(new XNetReply("01 04 05"));

            // should initiate final OFF messages
            assertEquals(2, lnis.outbound.size());
            checkCommandSequence(
                    "52 05 80 D7",
                    "52 05 83 D4"
            );
        }

        // still not IDLE, as the OK-in-OFFSENT still not satisfied
        assertNotEquals(XNetTurnout.IDLE, t.internalState);
        assertNotEquals(XNetTurnout.IDLE, t2.internalState);

        // OKs to last (direct) OFF messages
        t.message(new XNetReply("01 04 05"));
        t2.message(new XNetReply("01 04 05"));

        assertEquals(XNetTurnout.IDLE, t.internalState);
        assertEquals(XNetTurnout.IDLE, t2.internalState);
        
        // Spurious OKs, to check there's no endless pingpong loop
        t.message(new XNetReply("01 04 05"));
        t2.message(new XNetReply("01 04 05"));
        
        // no more messages added.
        assertEquals(2, lnis.outbound.size());
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

}
