package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.LnTurnout class
 *
 * @author	Bob Jacobsen
 */
public class LnTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

    @Override
    public int numListeners() {
        return lnis.numListeners();
    }

    /**
     * Check that last two messages correspond to closed/on, then closed/off.
     * Why last two? For unknown reason(s), this test gets _three_ messages,
     * with the first one being a set 21 closed and off. Is it left over from
     * some previous test?
     */
    @Override
    public void checkClosedMsgSent() throws InterruptedException {
        // Make sure that timed message has fired by waiting
        synchronized (this) {
            this.wait(LnTurnout.METERINTERVAL + 25);
        }

        // check results
        Assert.assertTrue("at least two messages", lnis.outbound.size() >= 2);
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 2).toString(),
                "B0 14 30 00");  // CLOSED/ON loconet message
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "B0 14 20 00");  // CLOSED/OFF loconet message
    }

    /**
     * Check that last two messages correspond to thrown/on, then thrown/off
     */
    @Override
    public void checkThrownMsgSent() throws InterruptedException {
        // Make sure that timed message has fired by waiting
        synchronized (this) {
            this.wait(LnTurnout.METERINTERVAL + 25);
        }

        // check for messages
        Assert.assertTrue("just two messages", lnis.outbound.size() == 2);
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 2).toString(),
                "B0 14 10 00");  // THROWN/ON loconet message
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "B0 14 00 00");  // THROWN/OFF loconet message
    }

    @Test
    public void checkIncoming() {
        // notify the Ln that somebody else changed it...
        LocoNetMessage m = new LocoNetMessage(4);
        m.setOpCode(0xb0);
        m.setElement(1, 0x14);     // set CLOSED
        m.setElement(2, 0x30);
        m.setElement(3, 0x00);
        lnis.sendTestMessage(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

        m = new LocoNetMessage(4);
        m.setOpCode(0xb0);
        m.setElement(1, 0x14);     // set THROWN
        m.setElement(2, 0x10);
        m.setElement(3, 0x00);
        lnis.sendTestMessage(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
    }
    @Test
    public void checkIncomingWithAck() {
        // notify the Ln that somebody else changed it...using OPC_SW_ACK
        LocoNetMessage m = new LocoNetMessage(4);
        m.setOpCode(0xbd);
        m.setElement(1, 0x14);     // set CLOSED
        m.setElement(2, 0x30);
        m.setElement(3, 0x00);
        lnis.sendTestMessage(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

        m = new LocoNetMessage(4);
        m.setOpCode(0xbd);
        m.setElement(1, 0x14);     // set THROWN
        m.setElement(2, 0x10);
        m.setElement(3, 0x00);
        lnis.sendTestMessage(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
    }

    // LnTurnout test for incoming status message
    @Test
    public void testLnTurnoutStatusMsg() {
        // prepare an interface
        // set closed
        try {
            t.setCommandedState(jmri.Turnout.CLOSED);
        } catch (Exception e) {
            log.error("TO exception: " + e);
        }
        Assert.assertTrue(lnis.outbound.elementAt(0)
                .toString().equals("B0 14 30 00"));  // CLOSED loconet message
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

        // notify the Ln that somebody else changed it...
        LocoNetMessage m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);     // set CLOSED
        m.setElement(2, 0x20);
        m.setElement(3, 0x7b);
        lnis.sendTestMessage(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

    }

    // LnTurnout test for incoming status message
    @Test
    public void testLnTurnoutStatusMsgAck() {
        // prepare an interface
        // set closed
        try {
            t.setProperty(LnTurnoutManager.BYPASSBUSHBYBITKEY, true);
            t.setCommandedState(jmri.Turnout.THROWN);
        } catch (Exception e) {
            log.error("TO exception: " + e);
        }
        Assert.assertTrue(lnis.outbound.elementAt(0)
                .toString().equals("BD 14 10 00"));  // thrown loconet message
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);

        // notify the Ln that somebody else changed it...
        LocoNetMessage m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);     // set thrown
        m.setElement(2, 0x10);
        m.setElement(3, 0x7b);
        lnis.sendTestMessage(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);

    }

    // LnTurnout test for exact feedback
    @Test
    public void testLnTurnoutExactFeedback() {
        LocoNetMessage m;
        // prepare a specific test
        t.setBinaryOutput(true);
        t.setCommandedState(jmri.Turnout.CLOSED);
        t.setFeedbackMode(jmri.Turnout.EXACT);
        Assert.assertEquals("CommandedState after set CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        // because this is the first time, the state is UNKNOWN; never goes back to that (in this test)
        Assert.assertEquals("KnownState after set CLOSED is UNKNOWN", jmri.Turnout.UNKNOWN, t.getKnownState());

        // notify the Ln of first feedback - AUX is thrown, so moved off 
        log.debug("notify of 1st feedback");
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x40);
        m.setElement(3, 0x1A);     // AUX reports THROWN
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after AUX report THROWN is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report THROWN is INCONSISTENT", jmri.Turnout.INCONSISTENT, t.getKnownState());

        // notify the Ln of second feedback - SWITCH is closed, so moved on
        log.debug("notify of 2nd feedback");
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x70);
        m.setElement(3, 0x2A);     // SWITCH reports CLOSED
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getKnownState());

        // test transition to THROWN
        t.setCommandedState(jmri.Turnout.THROWN);
        Assert.assertEquals("CommandedState after set THROWN is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after set THROWN is UNKNOWN", jmri.Turnout.CLOSED, t.getKnownState());

        // notify the Ln of first feedback - SWITCH is thrown, so moved off 
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x60);
        m.setElement(3, 0x3A);     // AUX reports THROWN
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after SWITCH report THROWN is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after SWITCH report THROWN is INCONSISTENT", jmri.Turnout.INCONSISTENT, t.getKnownState());

        // notify the Ln of second feedback - AUX is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x50);
        m.setElement(3, 0x0A);     // SWITCH reports CLOSED
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after AUX report CLOSED is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report CLOSED is THROWN", jmri.Turnout.THROWN, t.getKnownState());

        // test transition back to CLOSED
        t.setCommandedState(jmri.Turnout.CLOSED);
        Assert.assertEquals("CommandedState after 2nd set CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after 2nd set CLOSED is THROWN", jmri.Turnout.THROWN, t.getKnownState());

        // notify the Ln of first feedback - AUX is thrown, so moved off 
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x40);
        m.setElement(3, 0x1A);     // AUX reports THROWN
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after AUX report THROWN is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report THROWN is INCONSISTENT", jmri.Turnout.INCONSISTENT, t.getKnownState());

        // notify the Ln of second feedback - SWITCH is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x70);
        m.setElement(3, 0x2A);     // SWITCH reports CLOSED
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getKnownState());

        // test transition to back to THROWN in wrong order 
        t.setCommandedState(jmri.Turnout.THROWN);
        Assert.assertEquals("CommandedState after 2nd set THROWN is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after 2nd set THROWN is CLOSED", jmri.Turnout.CLOSED, t.getKnownState());

        // notify the Ln of second feedback (out of order) - AUX is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x50);
        m.setElement(3, 0x0A);     // SWITCH reports CLOSED
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after AUX report CLOSED is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report CLOSED is THROWN", jmri.Turnout.THROWN, t.getKnownState());

        // notify the Ln of first feedback (out of order) - SWITCH is thrown, so moved off - ignored
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x60);
        m.setElement(3, 0x3A);     // AUX reports THROWN
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after SWITCH report THROWN is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after SWITCH report THROWN is THROWN", jmri.Turnout.THROWN, t.getKnownState());

        // test transition back to CLOSED in wrong order
        t.setCommandedState(jmri.Turnout.CLOSED);
        Assert.assertEquals("CommandedState after 2nd set CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after 2nd set CLOSED is THROWN", jmri.Turnout.THROWN, t.getKnownState());

        // notify the Ln of second feedback (out of order) - SWITCH is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x70);
        m.setElement(3, 0x2A);     // SWITCH reports CLOSED
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getKnownState());

        // notify the Ln of first feedback (out of order) - AUX is thrown, so moved off 
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x40);
        m.setElement(3, 0x1A);     // AUX reports THROWN
        lnis.sendTestMessage(m);
        Assert.assertEquals("CommandedState after AUX report THROWN is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report THROWN is CLOSED", jmri.Turnout.CLOSED, t.getKnownState());

    }

    // test that only one message is sent when binaryOutput is set
    @Test
    public void testBasicSet() throws InterruptedException {
        t.setBinaryOutput(true);
        t.setCommandedState(jmri.Turnout.THROWN);

        // Make sure that timed message has fired by waiting
        synchronized (this) {
            this.wait(LnTurnout.METERINTERVAL + 25);
        }

        // check for messages
        Assert.assertTrue("just one messages", lnis.outbound.size() == 1);
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "B0 14 10 00");  // THROWN/ON loconet message
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
    }

    // test that only one message is sent when property SendOnAndOff is false.
    @Test
    public void testPropertySet() throws InterruptedException {
        t.setBinaryOutput(false);
        t.setProperty(LnTurnoutManager.SENDONANDOFFKEY, false);
        t.setCommandedState(jmri.Turnout.THROWN);

        // Make sure that timed message has fired by waiting
        synchronized (this) {
            this.wait(LnTurnout.METERINTERVAL + 25);
        }

        // check for messages
        Assert.assertTrue("just one messages", lnis.outbound.size() == 1);
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "B0 14 10 00");  // THROWN/ON loconet message
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
    }

    // test that only two messages are sent when property SendOnAndOff is true.
    @Test
    public void testPropertySet1() throws InterruptedException {
        t.setBinaryOutput(false);
        t.setProperty(LnTurnoutManager.SENDONANDOFFKEY, true);
        t.setCommandedState(jmri.Turnout.THROWN);

        // Make sure that timed message has fired by waiting
        synchronized (this) {
            this.wait(LnTurnout.METERINTERVAL + 25);
        }

        // check for messages
        Assert.assertTrue("just two messages", lnis.outbound.size() == 2);
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "B0 14 00 00");  // THROWN/OFF loconet message
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
    }

    // test that only two messages are sent when property SendOnAndOff is true, even if (ulenbook) binary set.
    @Test
    public void testPropertySet2() throws InterruptedException {
        t.setBinaryOutput(true);
        t.setProperty(LnTurnoutManager.SENDONANDOFFKEY, true);
        t.setCommandedState(jmri.Turnout.THROWN);

        // Make sure that timed message has fired by waiting
        synchronized (this) {
            this.wait(LnTurnout.METERINTERVAL + 25);
        }

        // check for messages
        Assert.assertTrue("just two messages", lnis.outbound.size() == 2);
        Assert.assertEquals(lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "B0 14 00 00");  // THROWN/OFF loconet message
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
    }

    LocoNetInterfaceScaffold lnis;
    LocoNetSystemConnectionMemo memo;

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);

        // outwait any pending delayed sends
        try {
            synchronized (this) {
                this.wait(LnTurnout.METERINTERVAL + 25);
            }
        } catch (InterruptedException e) {
        }

        // create object under test
        t = new LnTurnout("L", 21, lnis);
    }

    @After
    public void tearDown(){
        t.dispose();
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LnTurnoutTest.class);

}
