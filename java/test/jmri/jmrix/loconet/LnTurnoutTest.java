package jmri.jmrix.loconet;

import static jmri.Turnout.CABLOCKOUT;

import jmri.util.JUnitUtil;

import static jmri.Turnout.PUSHBUTTONLOCKOUT;
import static jmri.Turnout.CABLOCKOUT;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.LnTurnout class
 *
 * @author	Bob Jacobsen
 */
public class LnTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

    LnTurnout lnt;

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
        lnt.messageFromManager(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

        m = new LocoNetMessage(4);
        m.setOpCode(0xb0);
        m.setElement(1, 0x14);     // set THROWN
        m.setElement(2, 0x10);
        m.setElement(3, 0x00);
        lnt.messageFromManager(m);
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
        lnt.messageFromManager(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

        m = new LocoNetMessage(4);
        m.setOpCode(0xbd);
        m.setElement(1, 0x14);     // set THROWN
        m.setElement(2, 0x10);
        m.setElement(3, 0x00);
        lnt.messageFromManager(m);
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
        lnt.messageFromManager(m);
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
        lnt.messageFromManager(m);
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
        m.setElement(3, 0x1A);     // AUX reports THROWN\

        lnt.messageFromManager(m);
        Assert.assertEquals("CommandedState after AUX report THROWN is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report THROWN is INCONSISTENT", jmri.Turnout.INCONSISTENT, t.getKnownState());

        // notify the Ln of second feedback - SWITCH is closed, so moved on
        log.debug("notify of 2nd feedback");
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x70);
        m.setElement(3, 0x2A);     // SWITCH reports CLOSED
        lnt.messageFromManager(m);
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
        lnt.messageFromManager(m);
        Assert.assertEquals("CommandedState after SWITCH report THROWN is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after SWITCH report THROWN is INCONSISTENT", jmri.Turnout.INCONSISTENT, t.getKnownState());

        // notify the Ln of second feedback - AUX is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x50);
        m.setElement(3, 0x0A);     // SWITCH reports CLOSED
        lnt.messageFromManager(m);
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
        lnt.messageFromManager(m);
        Assert.assertEquals("CommandedState after AUX report THROWN is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report THROWN is INCONSISTENT", jmri.Turnout.INCONSISTENT, t.getKnownState());

        // notify the Ln of second feedback - SWITCH is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x70);
        m.setElement(3, 0x2A);     // SWITCH reports CLOSED
        lnt.messageFromManager(m);
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
        lnt.messageFromManager(m);
        Assert.assertEquals("CommandedState after AUX report CLOSED is THROWN", jmri.Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals("KnownState after AUX report CLOSED is THROWN", jmri.Turnout.THROWN, t.getKnownState());

        // notify the Ln of first feedback (out of order) - SWITCH is thrown, so moved off - ignored
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x60);
        m.setElement(3, 0x3A);     // AUX reports THROWN
        lnt.messageFromManager(m);
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
        lnt.messageFromManager(m);
        Assert.assertEquals("CommandedState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals("KnownState after SWITCH report CLOSED is CLOSED", jmri.Turnout.CLOSED, t.getKnownState());

        // notify the Ln of first feedback (out of order) - AUX is thrown, so moved off
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x40);
        m.setElement(3, 0x1A);     // AUX reports THROWN
        lnt.messageFromManager(m);
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

    @Test
    public void testTurnoutLocks() {
        Assert.assertFalse("check t.canLock(CABLOCKOUT)",t.canLock(CABLOCKOUT));
        Assert.assertFalse("check t.canLock(PUSHBUTTONLOCKOUT)",t.canLock(PUSHBUTTONLOCKOUT));

        Assert.assertFalse("check turnoutPushbuttonLockout(false) is false 1", lnt.getLocked(PUSHBUTTONLOCKOUT));
        Assert.assertFalse("check turnoutPushbuttonLockout(false) is false 2", lnt.getLocked(CABLOCKOUT));
        Assert.assertFalse("check turnoutPushbuttonLockout(false) is false 3", lnt.getLocked(CABLOCKOUT + PUSHBUTTONLOCKOUT));
        lnt.turnoutPushbuttonLockout(false);
        Assert.assertFalse("check turnoutPushbuttonLockout(false) is false 4", lnt.getLocked(PUSHBUTTONLOCKOUT));
        Assert.assertFalse("check turnoutPushbuttonLockout(false) is false 5", lnt.getLocked(CABLOCKOUT));
        Assert.assertFalse("check turnoutPushbuttonLockout(false) is false 6", lnt.getLocked(CABLOCKOUT + PUSHBUTTONLOCKOUT));
        lnt.turnoutPushbuttonLockout(true);
        Assert.assertFalse("check turnoutPushbuttonLockout(false) is false 7", lnt.getLocked(PUSHBUTTONLOCKOUT));
        Assert.assertFalse("check turnoutPushbuttonLockout(false) is false 8", lnt.getLocked(CABLOCKOUT));
        Assert.assertFalse("check turnoutPushbuttonLockout(false) is false 9", lnt.getLocked(CABLOCKOUT + PUSHBUTTONLOCKOUT));
    }

    @Test
    public void testMessageFromManagerWrongType() {
        Assert.assertEquals("check default known state", jmri.Turnout.UNKNOWN, t.getKnownState());
        Assert.assertEquals("check default commanded state", jmri.Turnout.UNKNOWN, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xd0, 0x00, 0x00, 0x00, 0x00, 0x00}));
        Assert.assertEquals("check default known state", jmri.Turnout.UNKNOWN, t.getKnownState());
        Assert.assertEquals("check default commanded state", jmri.Turnout.UNKNOWN, t.getKnownState());
    }

    @Test
    public void testMyAddress() {
        LocoNetMessage m;
        Assert.assertEquals("get initial turnout 21 known state as unknown", jmri.Turnout.UNKNOWN, lnt.getKnownState());
        Assert.assertEquals("get initial turnout 21 commanded state as unknown", jmri.Turnout.UNKNOWN, lnt.getCommandedState());

        m = new LocoNetMessage(new int[] {0xb0, 0x0, 0x30, 00});
        for (int i=0; i < 2048; ++i) {
            if (i != 20) {
            m.setElement(1, i&0x7f); m.setElement(2, ((i>>7)&0xf)+0x30);
            lnt.messageFromManager(m);
            Assert.assertEquals("check turnout 21 known state after message turnout "+i+" unknown.", jmri.Turnout.UNKNOWN, lnt.getKnownState());
            Assert.assertEquals("check turnout 21 commanded state after message turnout "+i+" unknown.", jmri.Turnout.UNKNOWN, lnt.getCommandedState());
            }
        }
        Assert.assertEquals("get final turnout 21 known state after bunch of opc_sw_req messages", jmri.Turnout.UNKNOWN, lnt.getKnownState());
        Assert.assertEquals("get final turnout 21 commanded state after bunch of opc_sw_req messages", jmri.Turnout.UNKNOWN, lnt.getCommandedState());

        m.setElement(1, 20&0x7f); m.setElement(2, ((20>>7)&0xf)+0x30);
        lnt.messageFromManager(m);
        Assert.assertEquals("check turnout 21 known state closed after opc_sw_req message turnout 20 closed.", jmri.Turnout.CLOSED, lnt.getKnownState());
        Assert.assertEquals("check turnout 21 commanded state closed after opc_sw_req message turnout 20 closed.", jmri.Turnout.CLOSED, lnt.getCommandedState());

        // same basic test using OPC_SW_REQ (output report form)

        m = new LocoNetMessage(new int[] {0xb1, 0x0, 0x00, 00});
        for (int i=0; i < 2048; ++i) {
            if (i != 20) {
            m.setElement(1, i&0x7f); m.setElement(2, ((i>>7)&0xf)+0x10);
            lnt.messageFromManager(m);
            Assert.assertEquals("check turnout 21 known state after opc_sw_rep (output rep) message turnout "+i+" thrown.", jmri.Turnout.CLOSED, lnt.getKnownState());
            Assert.assertEquals("check turnout 21 commanded state after opc_sw_rep (output rep) message turnout "+i+" thrown.", jmri.Turnout.CLOSED, lnt.getCommandedState());
            }
        }
        Assert.assertEquals("get turnout 21 known state after bunch of opc_sw_rep (output rep) messages", jmri.Turnout.CLOSED, lnt.getKnownState());
        Assert.assertEquals("get turnout 21 commanded state after bunch of opc_sw_rep (output rep) messages", jmri.Turnout.CLOSED, lnt.getCommandedState());

        m.setElement(1, 20&0x7f); m.setElement(2, ((20>>7)&0xf)+0x10);
        lnt.messageFromManager(m);
        Assert.assertEquals("check turnout 21 known state closed after opc_sw_req message turnout 21 thrown.", jmri.Turnout.THROWN, lnt.getKnownState());
        Assert.assertEquals("check turnout 21 commanded state closed after opc_sw_req message turnout 21 thrown.", jmri.Turnout.THROWN, lnt.getCommandedState());

        m.setElement(1, 20&0x7f); m.setElement(2, ((20>>7)&0xf)+0x30);
        lnt.messageFromManager(m);
        Assert.assertEquals("check turnout 21 known state closed after opc_sw_req message turnout 21 closed+thrown.", jmri.Turnout.THROWN + jmri.Turnout.CLOSED, lnt.getKnownState());
        Assert.assertEquals("check turnout 21 commanded state closed+thrown after opc_sw_req message turnout 21 closed+thrown.", jmri.Turnout.THROWN + jmri.Turnout.CLOSED, lnt.getCommandedState());
    }

    @Test
    public void testCtorNumberOutOfBounds() {
        boolean excep = false;
        try {
            LnTurnout t = new LnTurnout("L", 0, lnis);
        } catch (java.lang.IllegalArgumentException e) {
            excep = true;
        }
        Assert.assertTrue("expected exception happened (1)", excep);

        excep = false;
        try {
            LnTurnout t = new LnTurnout("L", 2049, lnis);
        } catch (java.lang.IllegalArgumentException e) {
            excep = true;
        }
        Assert.assertTrue("expected exception happened (2)", excep);

        excep = false;
        try {
            LnTurnout t = new LnTurnout("L", 2048, lnis);
        } catch (java.lang.IllegalArgumentException e) {
            excep = true;
        }
        Assert.assertFalse("exception did not happen (3)", excep);
    }

    @Test
    public void testSetFeedback() {
        boolean excep = false;
        try {
            t.setFeedbackMode("poSitive");
        } catch (IllegalArgumentException e) {
            excep = true;
        }
        Assert.assertTrue("expected illegal argument exception happened (1)", excep);

        excep = false;
        try {
            t.setFeedbackMode("NEGATIVE");
        } catch (IllegalArgumentException e) {
            excep = true;
        }
        Assert.assertTrue("expected illegal argument exception happened (2)", excep);

        excep = false;
        try {
            t.setFeedbackMode("DIRECT");
        } catch (IllegalArgumentException e) {
            excep = true;
        }
        Assert.assertFalse("Did not expect or get an exception (3)", excep);
        Assert.assertEquals("Check direct feedback mode set (3)", "DIRECT", t.getFeedbackModeName());

        try {
            t.setFeedbackMode("MONITORING");
        } catch (IllegalArgumentException e) {
            excep = true;
        }
        Assert.assertFalse("Did not expect or get an exception (4)", excep);
        Assert.assertEquals("Check direct feedback mode set (4)", "MONITORING", t.getFeedbackModeName());

        try {
            t.setFeedbackMode("EXACT");
        } catch (IllegalArgumentException e) {
            excep = true;
        }
        Assert.assertFalse("Did not expect or get an exception (5)", excep);
        Assert.assertEquals("Check direct feedback mode set (5)", "EXACT", t.getFeedbackModeName());

        try {
            t.setFeedbackMode("INDIRECT");
        } catch (IllegalArgumentException e) {
            excep = true;
        }
        Assert.assertFalse("Did not expect or get an exception (6)", excep);
        Assert.assertEquals("Check direct feedback mode set (6)", "INDIRECT", t.getFeedbackModeName());

        try {
            t.setFeedbackMode("ONESENSOR");
        } catch (IllegalArgumentException e) {
            excep = true;
        }
        Assert.assertFalse("Did not expect or get an exception (7)", excep);
        Assert.assertEquals("Check direct feedback mode set (7)", "ONESENSOR", t.getFeedbackModeName());

        jmri.util.JUnitAppender.assertWarnMessage("expected Sensor 1 not defined - LT21");


        try {
            t.setFeedbackMode("TWOSENSOR");
        } catch (IllegalArgumentException e) {
            excep = true;
        }
        Assert.assertFalse("Did not expect or get an exception (8)", excep);
        Assert.assertEquals("Check direct feedback mode set (8)", "TWOSENSOR", t.getFeedbackModeName());
        jmri.util.JUnitAppender.assertWarnMessage("expected Sensor 1 not defined - LT21");
        jmri.util.JUnitAppender.assertWarnMessage("expected Sensor 2 not defined - LT21");
    }

    @Test
    public void testGetNumber() {
        Assert.assertEquals("check test's default turnout address nubmer", 21, lnt.getNumber());
        LnTurnout t2 = new LnTurnout("L", 5, lnis);
        Assert.assertEquals("check test's default turnout address nubmer", 5, t2.getNumber());
        t2 = new LnTurnout("L", 2047, lnis);
        Assert.assertEquals("check test's default turnout address nubmer", 2047, t2.getNumber());
    }

    @Test
    public void testSetUseOffSwReqAsConfirmation() {
        Assert.assertFalse("check default offSwReqAsConfirmation", lnt._useOffSwReqAsConfirmation);
        lnt.setUseOffSwReqAsConfirmation(true);
        Assert.assertTrue("check first offSwReqAsConfirmation", lnt._useOffSwReqAsConfirmation);
        lnt.setUseOffSwReqAsConfirmation(false);
        Assert.assertFalse("check first offSwReqAsConfirmation", lnt._useOffSwReqAsConfirmation);
    }

    @Test
    public void testSetStateClosedAndThrown() {
        Assert.assertEquals("checking initial known state", t.UNKNOWN, t.getKnownState());
        t.setCommandedState(t.CLOSED + t.THROWN);
        jmri.util.JUnitAppender.assertErrorMessage("LocoNet turnout logic can't handle both THROWN and CLOSED yet");
        Assert.assertEquals("checking commanded state is Unknown after trying to send THROWN AND CLOSED", t.UNKNOWN, t.getKnownState());
        Assert.assertEquals("checking known state is Unknown after trying to send THROWN AND CLOSED", t.UNKNOWN, t.getKnownState());
        Assert.assertEquals("Checking to see if a LocoNet message was generated", 1, lnis.outbound.size());
        Assert.assertEquals("Check OpCode", 0xb0, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("Check byte 1", 0x14, lnis.outbound.get(0).getElement(1));
        Assert.assertEquals("Check byte 2", 0x30, lnis.outbound.get(0).getElement(2));
    }

    @Test
    public void testWarningSendingOffWhenUsingOffAsConfirmation() {
        lnt._useOffSwReqAsConfirmation = true;
        lnt.sendOpcSwReqMessage(t.CLOSED, false);
        jmri.util.JUnitAppender.assertWarnMessage("Turnout 21 is using OPC_SWREQ off as confirmation, but is sending OFF commands itself anyway");
        Assert.assertEquals("check message sent", 1, lnis.outbound.size());
    }

    @Test
    public void testFeedbackLateResend() {
        lnt.setFeedbackMode("INDIRECT");
        lnt._useOffSwReqAsConfirmation=true;
        lnt.setCommandedState(t.CLOSED);
        Assert.assertEquals("check message sent", 1, lnis.outbound.size());
        Assert.assertEquals("check known state before feedback received", t.UNKNOWN, t.getKnownState());
        Assert.assertEquals("check initial message Opcode", 0xB0, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("check initial message element 1", 20, lnis.outbound.get(0).getElement(1));
        Assert.assertEquals("check initial message element 2", 0x30, lnis.outbound.get(0).getElement(2));
        JUnitUtil.waitFor(()->{return lnis.outbound.size()==2;},"2nd message not received");
        jmri.util.JUnitAppender.assertWarnMessage("Turnout 21 is using OPC_SWREQ off as confirmation, but is sending OFF commands itself anyway");
        Assert.assertEquals("check second message Opcode", 0xB0, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("check second message element 1", 20, lnis.outbound.get(1).getElement(1));
        Assert.assertEquals("check second message element 2", 0x20, lnis.outbound.get(1).getElement(2));
        JUnitUtil.waitFor(()->{return lnis.outbound.size()==3;},"3rd message not received");
        // check for resend of original message
        Assert.assertEquals("check second message Opcode", 0xB0, lnis.outbound.get(2).getOpCode());
        Assert.assertEquals("check second message element 1", 20, lnis.outbound.get(2).getElement(1));
        Assert.assertEquals("check second message element 2", 0x30, lnis.outbound.get(2).getElement(2));
        Assert.assertEquals("check known state got updated", t.UNKNOWN, t.getKnownState());
    }

    @Test
    public void testFeedbackLateResendAborted() {
        lnt.setFeedbackMode("INDIRECT");
        lnt._useOffSwReqAsConfirmation=true;
        lnt.setCommandedState(t.CLOSED);
        Assert.assertEquals("check message sent (2)", 1, lnis.outbound.size());
        Assert.assertEquals("check known state before feedback received (2)", t.UNKNOWN, t.getKnownState());
        Assert.assertEquals("check initial message Opcode (2)", 0xB0, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("check initial message element 1 (2)", 20, lnis.outbound.get(0).getElement(1));
        Assert.assertEquals("check initial message element 2 (2)", 0x30, lnis.outbound.get(0).getElement(2));

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xB1, 0x14, 0x60, 0x00}));
        Assert.assertEquals("check known state got updated", t.THROWN, t.getKnownState());
        JUnitUtil.waitFor(()->{return lnis.outbound.size()==2;},"2nd message not received (2)");
        jmri.util.JUnitAppender.assertWarnMessage("Turnout 21 is using OPC_SWREQ off as confirmation, but is sending OFF commands itself anyway");
        Assert.assertEquals("check second message Opcode", 0xB0, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("check second message element 1", 20, lnis.outbound.get(1).getElement(1));
        Assert.assertEquals("check second message element 2", 0x20, lnis.outbound.get(1).getElement(2));
        JUnitUtil.waitFor(3500);
        Assert.assertEquals("still only 2 sent messages", 2, lnis.outbound.size());
    }

    @Test
    public void testComputeKnownStateOpSwAckReq() {
        lnt.setFeedbackMode("DIRECT");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x20, 0x00}));
        Assert.assertEquals("check message sent(1)", 0, lnis.outbound.size());
        Assert.assertEquals("check known state after echoed (1)", t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x00, 0x00}));
        Assert.assertEquals("check message sent(2)", 0, lnis.outbound.size());
        Assert.assertEquals("check known state after echoed (2)", t.THROWN, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x30, 0x00}));
        Assert.assertEquals("check message sent(3)", 0, lnis.outbound.size());
        Assert.assertEquals("check known state after echoed (3)", t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x10, 0x00}));
        Assert.assertEquals("check message sent(4)", 0, lnis.outbound.size());
        Assert.assertEquals("check known state after echoed (4)", t.THROWN, t.getKnownState());

        lnt.setFeedbackMode("MONITORING");
        lnt._useOffSwReqAsConfirmation = true;
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x30, 0x00}));
        Assert.assertEquals("check message sent(5)", 0, lnis.outbound.size());
        Assert.assertEquals("check known state after echoed (5)", t.THROWN, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x20, 0x00}));
        Assert.assertEquals("check message sent(6)", 0, lnis.outbound.size());
        Assert.assertEquals("check known state after echoed (6)", t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x10, 0x00}));
        Assert.assertEquals("check message sent(7)", 0, lnis.outbound.size());
        Assert.assertEquals("check known state after echoed (7)", t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x00, 0x00}));
        Assert.assertEquals("check message sent(8)", 0, lnis.outbound.size());
        Assert.assertEquals("check known state after echoed (8)", t.THROWN, t.getKnownState());

        lnt.setFeedbackMode("INDIRECT");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x10, 0x00}));
        Assert.assertEquals("check message sent(9)", 0, lnis.outbound.size());
        Assert.assertEquals("check known state after echoed (9)", t.THROWN, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x00, 0x00}));
        Assert.assertEquals("check message sent(10)", 0, lnis.outbound.size());
        Assert.assertEquals("check known state after echoed (10)", t.THROWN, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x10, 0x00}));
        Assert.assertEquals("check message sent(11)", 0, lnis.outbound.size());
        Assert.assertEquals("check known state after echoed (11)", t.THROWN, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x00, 0x00}));
        Assert.assertEquals("check message sent(12)", 0, lnis.outbound.size());
        Assert.assertEquals("check known state after echoed (12)", t.THROWN, t.getKnownState());
    }

    @Test
    public void testSetKnownStateFromOutputStateReport() {
        lnt.setFeedbackMode("DIRECT");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x00, 0x00} ));
        Assert.assertEquals("check known state after message (1)", 0, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x14, 0x10, 0x00}));
        Assert.assertEquals("check known state after message (1)", t.THROWN, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x20, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x30, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.THROWN+t.CLOSED, t.getKnownState());

        lnt.setFeedbackMode("INDIRECT");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x00, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.THROWN+t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x14, 0x10, 0x00}));
        Assert.assertEquals("check known state after message (1)", t.THROWN+t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x20, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.THROWN+t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x30, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.THROWN+t.CLOSED, t.getKnownState());

        lnt.setFeedbackMode("MONITORING");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x00, 0x00} ));
        Assert.assertEquals("check known state after message (1)", 0, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x14, 0x10, 0x00}));
        Assert.assertEquals("check known state after message (1)", t.THROWN, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x20, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x30, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.THROWN+t.CLOSED, t.getKnownState());

    }

    @Test
    public void testComputeFeedbackFromSwitchOffReport() {
        lnt.setFeedbackMode("INDIRECT");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x40, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.UNKNOWN, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x14, 0x50, 0x00}));
        Assert.assertEquals("check known state after message (1)", t.UNKNOWN, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x60, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.THROWN, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x70, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x40, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x14, 0x50, 0x00}));
        Assert.assertEquals("check known state after message (1)", t.CLOSED, t.getKnownState());

        lnt.setFeedbackMode("EXACT");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x60, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.INCONSISTENT, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x40, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.INCONSISTENT, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x14, 0x50, 0x00}));
        Assert.assertEquals("check known state after message (1)", t.THROWN, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x70, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.CLOSED, t.getKnownState());

        lnt = new LnTurnout("L", 22, lnis);
        lnt.setFeedbackMode("MONITORING");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x15, 0x60, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x15, 0x40, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x15, 0x50, 0x00}));
        Assert.assertEquals("check known state after message (1)", t.CLOSED, t.getKnownState());

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x15, 0x70, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.CLOSED, t.getKnownState());

        lnt = new LnTurnout("L", 23, lnis);
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x16, 0x60, 0x00} ));
        Assert.assertEquals("check known state after message (1)", t.CLOSED, t.getKnownState());
    }
    
    @Test
    public void testAdjustStateForInversion() {
        Assert.assertFalse("check default inversion", lnt.getInverted());
        lnt.setBinaryOutput(true);
        lnt.setUseOffSwReqAsConfirmation(false);
        lnt.setCommandedState(t.CLOSED);
        Assert.assertEquals("check commanded state after forward closed to layout (1)", t.CLOSED, t.getCommandedState());
        Assert.assertEquals("check num messages sent after forward closed to layout (1)",1, lnis.outbound.size());
        Assert.assertEquals("check byte 2 of message (1)", 0x30, lnis.outbound.get(0).getElement(2));

        lnt.setCommandedState(t.THROWN);
        Assert.assertEquals("check commanded state after forward thrown to layout (2)", t.THROWN, t.getCommandedState());
        Assert.assertEquals("check num messages sent after forward thrown to layout (2)",2, lnis.outbound.size());
        Assert.assertEquals("check byte 2 of message (2)", 0x10, lnis.outbound.get(1).getElement(2));
        
        lnt.setInverted(true);
        // when inverted, the commanded state remains unmodified; only the LocoNet 
        // message sent gets state inverted.
        lnt.setCommandedState(t.THROWN);
        Assert.assertEquals("check commanded state after forward closed to layout (3)", t.THROWN, t.getCommandedState());
        Assert.assertEquals("check num messages sent after forward closed to layout (3)",3, lnis.outbound.size());
        Assert.assertEquals("check byte 2 of message (1)", 0x30, lnis.outbound.get(2).getElement(2));

        lnt.setCommandedState(t.CLOSED);
        Assert.assertEquals("check commanded state after forward thrown to layout (4)", t.CLOSED, t.getCommandedState());
        Assert.assertEquals("check num messages sent after forward thrown to layout (4)",4, lnis.outbound.size());
        Assert.assertEquals("check byte 2 of message (2)", 0x10, lnis.outbound.get(3).getElement(2));
        
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
        lnt=(LnTurnout) t;
    }

    @After
    public void tearDown(){
        t.dispose();
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LnTurnoutTest.class);

}
