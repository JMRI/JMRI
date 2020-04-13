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

    protected Turnout t = null;	// holds object under test; set by setUp()

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
    
    private final static Logger log = LoggerFactory.getLogger(LenzXNetTurnoutCommandTest.class);

}
