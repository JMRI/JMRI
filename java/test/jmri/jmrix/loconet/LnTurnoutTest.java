package jmri.jmrix.loconet;

import static jmri.Turnout.PUSHBUTTONLOCKOUT;
import static jmri.Turnout.CABLOCKOUT;
import static jmri.Turnout.CLOSED;
import static jmri.Turnout.THROWN;
import static jmri.Turnout.UNKNOWN;
import static jmri.Turnout.INCONSISTENT;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.loconet.LnTurnout class
 *
 * @author Bob Jacobsen
 */
public class LnTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

    private LnTurnout lnt;

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
    public void checkClosedMsgSent() {
        // Make sure that timed message has fired by waiting
        JUnitUtil.waitFor(()->{return lnis.outbound.size() == 2;},"just two messages");

        // check results
        assertEquals( "B0 14 30 00", // CLOSED/ON loconet message
            lnis.outbound.elementAt(lnis.outbound.size() - 2).toString());
        assertEquals( "B0 14 20 00",  // CLOSED/OFF loconet message
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        // clear message stack
        lnis.clearReceivedMessages();
    }

    /**
     * Check that last two messages correspond to thrown/on, then thrown/off
     */
    @Override
    public void checkThrownMsgSent() {
        // Make sure that timed message has fired by waiting
        JUnitUtil.waitFor(()->{return lnis.outbound.size() == 2;},"just two messages");

        // check results
        assertEquals( 2, lnis.outbound.size(), "just two messages");
        assertEquals("B0 14 10 00", // THROWN/ON loconet message
            lnis.outbound.elementAt(lnis.outbound.size() - 2).toString());
        assertEquals("B0 14 00 00", // THROWN/OFF loconet message
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        // clear message stack
        lnis.clearReceivedMessages();
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
        assertEquals( CLOSED, t.getCommandedState());

        m = new LocoNetMessage(4);
        m.setOpCode(0xb0);
        m.setElement(1, 0x14);     // set THROWN
        m.setElement(2, 0x10);
        m.setElement(3, 0x00);
        lnt.messageFromManager(m);
        assertEquals( THROWN, t.getCommandedState() );
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
        assertEquals( CLOSED, t.getCommandedState());

        m = new LocoNetMessage(4);
        m.setOpCode(0xbd);
        m.setElement(1, 0x14);     // set THROWN
        m.setElement(2, 0x10);
        m.setElement(3, 0x00);
        lnt.messageFromManager(m);
        assertEquals( THROWN, t.getCommandedState());
    }

    // LnTurnout test for incoming status message
    @Test
    public void testLnTurnoutStatusMsg() {
        // prepare an interface
        // set closed
        assertDoesNotThrow( () -> {
            t.setCommandedState( CLOSED );
        }, "TO exception: ");
        assertEquals( "B0 14 30 00", lnis.outbound.elementAt(0).toString(),
            "CLOSED loconet message");
        assertEquals( CLOSED, t.getCommandedState());

        // notify the Ln that somebody else changed it...
        LocoNetMessage m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);     // set CLOSED
        m.setElement(2, 0x20);
        m.setElement(3, 0x7b);
        lnt.messageFromManager(m);
        assertEquals( CLOSED, t.getCommandedState());

    }

    // LnTurnout test for incoming status message
    @Test
    public void testLnTurnoutStatusMsgAck() {
        // prepare an interface
        // set closed
        assertDoesNotThrow( () -> {
            t.setProperty(LnTurnoutManager.BYPASSBUSHBYBITKEY, true);
            t.setCommandedState(THROWN);
        }, "TO exception: ");
        assertEquals( "BD 14 10 00", lnis.outbound.elementAt(0).toString(),
            "thrown loconet message");
        assertEquals( THROWN, t.getCommandedState());

        // notify the Ln that somebody else changed it...
        LocoNetMessage m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);     // set thrown
        m.setElement(2, 0x10);
        m.setElement(3, 0x7b);
        lnt.messageFromManager(m);
        assertEquals( THROWN, t.getCommandedState());

    }

    // LnTurnout test for exact feedback
    @Test
    public void testLnTurnoutExactFeedback() {
        LocoNetMessage m;
        // prepare a specific test
        t.setBinaryOutput(true);
        t.setCommandedState(CLOSED);
        t.setFeedbackMode(jmri.Turnout.EXACT);
        assertEquals( CLOSED, t.getCommandedState(),
            "CommandedState after set CLOSED is CLOSED");
        // because this is the first time, the state is UNKNOWN; never goes back to that (in this test)
        assertEquals( UNKNOWN, t.getKnownState(),
            "KnownState after set CLOSED is UNKNOWN");

        // notify the Ln of first feedback - AUX is thrown, so moved off
        // log.debug("notify of 1st feedback");
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x40);
        m.setElement(3, 0x1A);     // AUX reports THROWN\

        lnt.messageFromManager(m);
        assertEquals( CLOSED, t.getCommandedState(), "CommandedState after AUX report THROWN is CLOSED");
        assertEquals( INCONSISTENT, t.getKnownState(), "KnownState after AUX report THROWN is INCONSISTENT");

        // notify the Ln of second feedback - SWITCH is closed, so moved on
        // log.debug("notify of 2nd feedback");
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x70);
        m.setElement(3, 0x2A);     // SWITCH reports CLOSED
        lnt.messageFromManager(m);
        assertEquals( CLOSED, t.getCommandedState(), "CommandedState after SWITCH report CLOSED is CLOSED");
        assertEquals( CLOSED, t.getKnownState(), "KnownState after SWITCH report CLOSED is CLOSED");

        // test transition to THROWN
        t.setCommandedState(THROWN);
        assertEquals( THROWN, t.getCommandedState(), "CommandedState after set THROWN is THROWN");
        assertEquals( CLOSED, t.getKnownState(), "KnownState after set THROWN is UNKNOWN");

        // notify the Ln of first feedback - SWITCH is thrown, so moved off
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x60);
        m.setElement(3, 0x3A);     // AUX reports THROWN
        lnt.messageFromManager(m);
        assertEquals( THROWN, t.getCommandedState(), "CommandedState after SWITCH report THROWN is THROWN");
        assertEquals( INCONSISTENT, t.getKnownState(), "KnownState after SWITCH report THROWN is INCONSISTENT");

        // notify the Ln of second feedback - AUX is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x50);
        m.setElement(3, 0x0A);     // SWITCH reports CLOSED
        lnt.messageFromManager(m);
        assertEquals( THROWN, t.getCommandedState(), "CommandedState after AUX report CLOSED is THROWN");
        assertEquals( THROWN, t.getKnownState(), "KnownState after AUX report CLOSED is THROWN");

        // test transition back to CLOSED
        t.setCommandedState(CLOSED);
        assertEquals( CLOSED, t.getCommandedState(), "CommandedState after 2nd set CLOSED is CLOSED");
        assertEquals( THROWN, t.getKnownState(), "KnownState after 2nd set CLOSED is THROWN");

        // notify the Ln of first feedback - AUX is thrown, so moved off
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x40);
        m.setElement(3, 0x1A);     // AUX reports THROWN
        lnt.messageFromManager(m);
        assertEquals( CLOSED, t.getCommandedState(), "CommandedState after AUX report THROWN is CLOSED");
        assertEquals( INCONSISTENT, t.getKnownState(), "KnownState after AUX report THROWN is INCONSISTENT");

        // notify the Ln of second feedback - SWITCH is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x70);
        m.setElement(3, 0x2A);     // SWITCH reports CLOSED
        lnt.messageFromManager(m);
        assertEquals( CLOSED, t.getCommandedState(), "CommandedState after SWITCH report CLOSED is CLOSED");
        assertEquals( CLOSED, t.getKnownState(), "KnownState after SWITCH report CLOSED is CLOSED");

        // test transition to back to THROWN in wrong order
        t.setCommandedState(THROWN);
        assertEquals( THROWN, t.getCommandedState(), "CommandedState after 2nd set THROWN is THROWN");
        assertEquals( CLOSED, t.getKnownState(), "KnownState after 2nd set THROWN is CLOSED");

        // notify the Ln of second feedback (out of order) - AUX is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x50);
        m.setElement(3, 0x0A);     // SWITCH reports CLOSED
        lnt.messageFromManager(m);
        assertEquals( THROWN, t.getCommandedState(), "CommandedState after AUX report CLOSED is THROWN");
        assertEquals( THROWN, t.getKnownState(), "KnownState after AUX report CLOSED is THROWN");

        // notify the Ln of first feedback (out of order) - SWITCH is thrown, so moved off - ignored
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x60);
        m.setElement(3, 0x3A);     // AUX reports THROWN
        lnt.messageFromManager(m);
        assertEquals( THROWN, t.getCommandedState(), "CommandedState after SWITCH report THROWN is THROWN");
        assertEquals( THROWN, t.getKnownState(), "KnownState after SWITCH report THROWN is THROWN");

        // test transition back to CLOSED in wrong order
        t.setCommandedState(CLOSED);
        assertEquals( CLOSED, t.getCommandedState(), "CommandedState after 2nd set CLOSED is CLOSED");
        assertEquals( THROWN, t.getKnownState(), "KnownState after 2nd set CLOSED is THROWN");

        // notify the Ln of second feedback (out of order) - SWITCH is closed, so moved on
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x70);
        m.setElement(3, 0x2A);     // SWITCH reports CLOSED
        lnt.messageFromManager(m);
        assertEquals( CLOSED, t.getCommandedState(), "CommandedState after SWITCH report CLOSED is CLOSED");
        assertEquals( CLOSED, t.getKnownState(), "KnownState after SWITCH report CLOSED is CLOSED");

        // notify the Ln of first feedback (out of order) - AUX is thrown, so moved off
        m = new LocoNetMessage(4);
        m.setOpCode(0xb1);
        m.setElement(1, 0x14);
        m.setElement(2, 0x40);
        m.setElement(3, 0x1A);     // AUX reports THROWN
        lnt.messageFromManager(m);
        assertEquals( CLOSED, t.getCommandedState(), "CommandedState after AUX report THROWN is CLOSED");
        assertEquals( CLOSED, t.getKnownState(), "KnownState after AUX report THROWN is CLOSED");

    }

    // test that only one message is sent when binaryOutput is set
    @Test
    public void testBasicSet() {
        t.setBinaryOutput(true);
        t.setCommandedState(THROWN);

        // Make sure that timed message has fired by waiting
        JUnitUtil.waitFor( LnTurnout.METERINTERVAL + 25 );

        // check for messages
        assertEquals( 1, lnis.outbound.size(), "just one message");
        assertEquals("B0 14 10 00", // THROWN/ON loconet message
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        assertEquals( THROWN, t.getCommandedState());
    }

    // test that only one message is sent when property SendOnAndOff is false.
    @Test
    public void testPropertySet() throws InterruptedException {
        t.setBinaryOutput(false);
        t.setProperty(LnTurnoutManager.SENDONANDOFFKEY, false);
        t.setCommandedState(THROWN);

        // Make sure that timed message has fired by waiting
        JUnitUtil.waitFor( LnTurnout.METERINTERVAL + 25 );

        // check for messages
        assertEquals( 1, lnis.outbound.size(), "just one message");
        assertEquals("B0 14 10 00", // THROWN/ON loconet message
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        assertEquals( THROWN, t.getCommandedState());
    }

    // test that only two messages are sent when property SendOnAndOff is true.
    @Test
    public void testPropertySet1() {
        t.setBinaryOutput(false);
        t.setProperty(LnTurnoutManager.SENDONANDOFFKEY, true);
        t.setCommandedState(THROWN);

        // Make sure that timed message has fired by waiting
        JUnitUtil.waitFor( LnTurnout.METERINTERVAL + 25 );

        // check for messages
        assertEquals( 2, lnis.outbound.size(), "just two messages");
        assertEquals("B0 14 00 00",  // THROWN/OFF loconet message
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        assertEquals( THROWN, t.getCommandedState());
    }

    // test that only two messages are sent when property SendOnAndOff is true, even if (ulenbook) binary set.
    @Test
    public void testPropertySet2() {
        t.setBinaryOutput(true);
        t.setProperty(LnTurnoutManager.SENDONANDOFFKEY, true);
        t.setCommandedState(THROWN);

        // Make sure that timed message has fired by waiting
        JUnitUtil.waitFor( LnTurnout.METERINTERVAL + 25 );

        // check for messages
        assertEquals( 2, lnis.outbound.size(), "just two messages");
        assertEquals("B0 14 00 00",  // THROWN/OFF loconet message
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        assertEquals( THROWN, t.getCommandedState());
    }

    @Test
    public void testTurnoutLocks() {
        assertFalse( t.canLock(CABLOCKOUT), "check t.canLock(CABLOCKOUT)");
        assertFalse( t.canLock(PUSHBUTTONLOCKOUT), "check t.canLock(PUSHBUTTONLOCKOUT)");

        assertFalse( lnt.getLocked(PUSHBUTTONLOCKOUT), "check turnoutPushbuttonLockout(false) is false 1");
        assertFalse( lnt.getLocked(CABLOCKOUT), "check turnoutPushbuttonLockout(false) is false 2");
        assertFalse( lnt.getLocked(CABLOCKOUT + PUSHBUTTONLOCKOUT), "check turnoutPushbuttonLockout(false) is false 3");
        lnt.turnoutPushbuttonLockout(false);
        assertFalse( lnt.getLocked(PUSHBUTTONLOCKOUT), "check turnoutPushbuttonLockout(false) is false 4");
        assertFalse( lnt.getLocked(CABLOCKOUT), "check turnoutPushbuttonLockout(false) is false 5");
        assertFalse( lnt.getLocked(CABLOCKOUT + PUSHBUTTONLOCKOUT), "check turnoutPushbuttonLockout(false) is false 6");
        lnt.turnoutPushbuttonLockout(true);
        assertFalse( lnt.getLocked(PUSHBUTTONLOCKOUT), "check turnoutPushbuttonLockout(false) is false 7");
        assertFalse( lnt.getLocked(CABLOCKOUT), "check turnoutPushbuttonLockout(false) is false 8");
        assertFalse( lnt.getLocked(CABLOCKOUT + PUSHBUTTONLOCKOUT), "check turnoutPushbuttonLockout(false) is false 9");
    }

    @Test
    public void testMessageFromManagerWrongType() {
        assertEquals( UNKNOWN, t.getKnownState(), "check default known state");
        assertEquals( UNKNOWN, t.getKnownState(), "check default commanded state");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xd0, 0x00, 0x00, 0x00, 0x00, 0x00}));
        assertEquals( UNKNOWN, t.getKnownState(), "check default known state");
        assertEquals( UNKNOWN, t.getKnownState(), "check default commanded state");
    }

    @Test
    public void testMyAddress() {
        LocoNetMessage m;
        assertEquals( UNKNOWN, lnt.getKnownState(),
            "get initial turnout 21 known state as unknown");
        assertEquals( UNKNOWN, lnt.getCommandedState(),
            "get initial turnout 21 commanded state as unknown");

        m = new LocoNetMessage(new int[] {0xb0, 0x0, 0x30, 00});
        for (int i=0; i < 2048; ++i) {
            if (i == 20) {
                continue;
            }
            m.setElement(1, i&0x7f); m.setElement(2, ((i>>7)&0xf)+0x30);
            lnt.messageFromManager(m);
            assertEquals( UNKNOWN, lnt.getKnownState(),
                "check turnout 21 known state after message turnout "+i+" unknown.");
            assertEquals( UNKNOWN, lnt.getCommandedState(),
                "check turnout 21 commanded state after message turnout "+i+" unknown.");
        }
        assertEquals( UNKNOWN, lnt.getKnownState(), "get final turnout 21 known state after bunch of opc_sw_req messages");
        assertEquals( UNKNOWN, lnt.getCommandedState(),
            "get final turnout 21 commanded state after bunch of opc_sw_req messages");

        m.setElement(1, 20&0x7f); m.setElement(2, ((20>>7)&0xf)+0x30);
        lnt.messageFromManager(m);
        assertEquals( CLOSED, lnt.getKnownState(),
            "check turnout 21 known state closed after opc_sw_req message turnout 20 closed.");
        assertEquals( CLOSED, lnt.getCommandedState(),
            "check turnout 21 commanded state closed after opc_sw_req message turnout 20 closed.");

        // same basic test using OPC_SW_REQ (output report form)

        m = new LocoNetMessage(new int[] {0xb1, 0x0, 0x00, 00});
        for (int i=0; i < 2048; ++i) {
            if (i == 20) {
                continue;
            }
            m.setElement(1, i&0x7f); m.setElement(2, ((i>>7)&0xf)+0x10);
            lnt.messageFromManager(m);
            assertEquals( CLOSED, lnt.getKnownState(),
                "check turnout 21 known state after opc_sw_rep (output rep) message turnout "+i+" thrown.");
            assertEquals( CLOSED, lnt.getCommandedState(),
                "check turnout 21 commanded state after opc_sw_rep (output rep) message turnout "+i+" thrown.");
        }
        assertEquals( CLOSED, lnt.getKnownState(),
            "get turnout 21 known state after bunch of opc_sw_rep (output rep) messages");
        assertEquals( CLOSED, lnt.getCommandedState(),
            "get turnout 21 commanded state after bunch of opc_sw_rep (output rep) messages");

        m.setElement(1, 20&0x7f); m.setElement(2, ((20>>7)&0xf)+0x10);
        lnt.messageFromManager(m);
        assertEquals( THROWN, lnt.getKnownState(),
            "check turnout 21 known state closed after opc_sw_req message turnout 21 thrown.");
        assertEquals( THROWN, lnt.getCommandedState(),
            "check turnout 21 commanded state closed after opc_sw_req message turnout 21 thrown.");

        m.setElement(1, 20&0x7f); m.setElement(2, ((20>>7)&0xf)+0x30);
        lnt.messageFromManager(m);
        assertEquals( THROWN + CLOSED, lnt.getKnownState(),
            "check turnout 21 known state closed after opc_sw_req message turnout 21 closed+thrown.");
        assertEquals( THROWN + CLOSED, lnt.getCommandedState(),
            "check turnout 21 commanded state closed+thrown after opc_sw_req message turnout 21 closed+thrown.");
    }

    @Test
    public void testCtorNumberOutOfBounds() {

        Exception ex = assertThrows( IllegalArgumentException.class,
            () -> new LnTurnout("L", 0, lnis) );
        assertEquals("Turnout value: 0 not in the range 1 to 2048", ex.getMessage());

        ex = assertThrows( IllegalArgumentException.class,
            () -> new LnTurnout("L", 2049, lnis) );
        assertEquals("Turnout value: 2049 not in the range 1 to 2048", ex.getMessage());

        int value = assertDoesNotThrow( () -> {
            LnTurnout validTurnout = new LnTurnout("L", 2048, lnis);
            return validTurnout._number;
        }, "exception did not happen (3)");
        assertEquals( 2048, value, "check t has number");
    }

    @Test
    public void testSetFeedback() {

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> t.setFeedbackMode("poSitive"),
            "expected illegal argument exception happened (1)");
        assertTrue( ex.getMessage().contains("poSitive"));

        ex = assertThrows( IllegalArgumentException.class,
            () -> t.setFeedbackMode("NEGATIVE"),
            "expected illegal argument exception happened (2)");
        assertTrue( ex.getMessage().contains("NEGATIVE"));

        assertDoesNotThrow( () -> t.setFeedbackMode("DIRECT"),
            "Did not expect or get an exception (3)");
        assertEquals( "DIRECT", t.getFeedbackModeName(),
            "Check direct feedback mode set (3)");

        assertDoesNotThrow( () -> t.setFeedbackMode("MONITORING"),
            "Did not expect or get an exception (4)");
        assertEquals( "MONITORING", t.getFeedbackModeName(),
            "Check direct feedback mode set (4)");

        assertDoesNotThrow( () -> t.setFeedbackMode("EXACT"),
            "Did not expect or get an exception (5)");
        assertEquals( "EXACT", t.getFeedbackModeName(),
            "Check direct feedback mode set (5)");

        assertDoesNotThrow( () -> t.setFeedbackMode("INDIRECT"),
            "Did not expect or get an exception (6)");
        assertEquals( "INDIRECT", t.getFeedbackModeName(),
            "Check direct feedback mode set (6)");

        assertDoesNotThrow( () -> t.setFeedbackMode("ONESENSOR"),
            "Did not expect or get an exception (7)");
        assertEquals( "ONESENSOR", t.getFeedbackModeName(),
            "Check direct feedback mode set (7)");

        JUnitAppender.assertWarnMessage("expected Sensor 1 not defined - LT21");


        assertDoesNotThrow( () -> t.setFeedbackMode("TWOSENSOR"),
            "Did not expect or get an exception (8)");
        assertEquals( "TWOSENSOR", t.getFeedbackModeName(),
            "Check direct feedback mode set (8)");
        JUnitAppender.assertWarnMessage("expected Sensor 1 not defined - LT21");
        JUnitAppender.assertWarnMessage("expected Sensor 2 not defined - LT21");
    }

    @Test
    public void testGetNumber() {
        assertEquals( 21, lnt.getNumber(), "check test's default turnout address nubmer");
        LnTurnout t2 = new LnTurnout("L", 5, lnis);
        assertEquals( 5, t2.getNumber(), "check test's default turnout address nubmer");
        t2 = new LnTurnout("L", 2047, lnis);
        assertEquals( 2047, t2.getNumber(), "check test's default turnout address nubmer");
    }

    @Test
    public void testSetUseOffSwReqAsConfirmation() {
        assertFalse( lnt._useOffSwReqAsConfirmation, "check default offSwReqAsConfirmation");
        lnt.setUseOffSwReqAsConfirmation(true);
        assertTrue( lnt._useOffSwReqAsConfirmation, "check first offSwReqAsConfirmation");
        lnt.setUseOffSwReqAsConfirmation(false);
        assertFalse( lnt._useOffSwReqAsConfirmation, "check first offSwReqAsConfirmation");
    }

    @Test
    public void testSetStateClosedAndThrown() {
        assertEquals( UNKNOWN, t.getKnownState(), "checking initial known state");
        t.setCommandedState(CLOSED + THROWN);
        JUnitAppender.assertErrorMessage("LocoNet turnout logic can't handle both THROWN and CLOSED yet");
        assertEquals( UNKNOWN, t.getKnownState(), "checking commanded state is Unknown after trying to send THROWN AND CLOSED");
        assertEquals( UNKNOWN, t.getKnownState(), "checking known state is Unknown after trying to send THROWN AND CLOSED");
        assertEquals( 1, lnis.outbound.size(), "Checking to see if a LocoNet message was generated");
        assertEquals( 0xb0, lnis.outbound.get(0).getOpCode(), "Check OpCode");
        assertEquals( 0x14, lnis.outbound.get(0).getElement(1), "Check byte 1");
        assertEquals( 0x30, lnis.outbound.get(0).getElement(2), "Check byte 2");
    }

    @Test
    public void testWarningSendingOffWhenUsingOffAsConfirmation() {
        lnt._useOffSwReqAsConfirmation = true;
        lnt.sendOpcSwReqMessage(CLOSED, false);
        JUnitAppender.assertWarnMessage("Turnout 21 is using OPC_SWREQ off as confirmation, but is sending OFF commands itself anyway");
        assertEquals( 1, lnis.outbound.size(), "check message sent");
    }

    @Test
    public void testFeedbackLateResend() {
        lnt.setFeedbackMode("INDIRECT");
        lnt._useOffSwReqAsConfirmation=true;
        lnt.setCommandedState(CLOSED);
        assertEquals( 1, lnis.outbound.size(), "just one message");
        assertEquals( UNKNOWN, t.getKnownState(), "check known state before feedback received");
        assertEquals( 0xB0, lnis.outbound.get(0).getOpCode(), "check initial message Opcode");
        assertEquals( 20, lnis.outbound.get(0).getElement(1), "check initial message element 1");
        assertEquals( 0x30, lnis.outbound.get(0).getElement(2), "check initial message element 2");
        JUnitUtil.waitFor(()->{return lnis.outbound.size()==2;},"2nd message not received");
        JUnitAppender.assertWarnMessage("Turnout 21 is using OPC_SWREQ off as confirmation, but is sending OFF commands itself anyway");
        assertEquals( 0xB0, lnis.outbound.get(1).getOpCode(), "check second message Opcode");
        assertEquals( 20, lnis.outbound.get(1).getElement(1), "check second message element 1");
        assertEquals( 0x20, lnis.outbound.get(1).getElement(2), "check second message element 2");
        JUnitUtil.waitFor(()->{return lnis.outbound.size()==3;},"3rd message not received");
        // check for resend of original message
        assertEquals( 0xB0, lnis.outbound.get(2).getOpCode(), "check second message Opcode");
        assertEquals( 20, lnis.outbound.get(2).getElement(1), "check second message element 1");
        assertEquals( 0x30, lnis.outbound.get(2).getElement(2), "check second message element 2");
        assertEquals( UNKNOWN, t.getKnownState(), "check known state got updated");
    }

    @Test
    public void testFeedbackLateResendAborted() {
        lnt.setFeedbackMode("INDIRECT");
        lnt._useOffSwReqAsConfirmation=true;
        lnt.setCommandedState(CLOSED);
        assertEquals( 1, lnis.outbound.size(), "just one message(2)");
        assertEquals( UNKNOWN, t.getKnownState(), "check known state before feedback received (2)");
        assertEquals( 0xB0, lnis.outbound.get(0).getOpCode(), "check initial message Opcode (2)");
        assertEquals( 20, lnis.outbound.get(0).getElement(1), "check initial message element 1 (2)");
        assertEquals( 0x30, lnis.outbound.get(0).getElement(2), "check initial message element 2 (2)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xB1, 0x14, 0x60, 0x00}));
        assertEquals( THROWN, t.getKnownState(), "check known state got updated");
        JUnitUtil.waitFor(()->{return lnis.outbound.size()==2;},"2nd message not received (2)");
        JUnitAppender.assertWarnMessage("Turnout 21 is using OPC_SWREQ off as confirmation, but is sending OFF commands itself anyway");
        assertEquals( 0xB0, lnis.outbound.get(1).getOpCode(), "check second message Opcode");
        assertEquals( 20, lnis.outbound.get(1).getElement(1), "check second message element 1");
        assertEquals( 0x20, lnis.outbound.get(1).getElement(2), "check second message element 2");
        JUnitUtil.waitFor(3500);
        assertEquals( 2, lnis.outbound.size(), "still only 2 sent messages");
    }

    @Test
    public void testComputeKnownStateOpSwAckReq() {
        lnt.setFeedbackMode("DIRECT");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x20, 0x00}));
        assertEquals( 0, lnis.outbound.size(), "check message sent(1)");
        assertEquals( CLOSED, t.getKnownState(), "check known state after echoed (1)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x00, 0x00}));
        assertEquals( 0, lnis.outbound.size(), "check message sent(2)");
        assertEquals( THROWN, t.getKnownState(), "check known state after echoed (2)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x30, 0x00}));
        assertEquals( 0, lnis.outbound.size(), "check message sent(3)");
        assertEquals( CLOSED, t.getKnownState(), "check known state after echoed (3)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x10, 0x00}));
        assertEquals( 0, lnis.outbound.size(), "check message sent(4)");
        assertEquals( THROWN, t.getKnownState(), "check known state after echoed (4)");

        lnt.setFeedbackMode("MONITORING");
        lnt._useOffSwReqAsConfirmation = true;
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x30, 0x00}));
        assertEquals( 0, lnis.outbound.size(), "check message sent(5)");
        assertEquals( THROWN, t.getKnownState(), "check known state after echoed (5)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x20, 0x00}));
        assertEquals( 0, lnis.outbound.size(), "check message sent(6)");
        assertEquals( CLOSED, t.getKnownState(), "check known state after echoed (6)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x10, 0x00}));
        assertEquals( 0, lnis.outbound.size(), "check message sent(7)");
        assertEquals( CLOSED, t.getKnownState(), "check known state after echoed (7)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x00, 0x00}));
        assertEquals( 0, lnis.outbound.size(), "check message sent(8)");
        assertEquals( THROWN, t.getKnownState(), "check known state after echoed (8)");

        lnt.setFeedbackMode("INDIRECT");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x10, 0x00}));
        assertEquals( 0, lnis.outbound.size(), "check message sent(9)");
        assertEquals( THROWN, t.getKnownState(), "check known state after echoed (9)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x00, 0x00}));
        assertEquals( 0, lnis.outbound.size(), "check message sent(10)");
        assertEquals( THROWN, t.getKnownState(), "check known state after echoed (10)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x10, 0x00}));
        assertEquals( 0, lnis.outbound.size(), "check message sent(11)");
        assertEquals( THROWN, t.getKnownState(), "check known state after echoed (11)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb0, 0x14, 0x00, 0x00}));
        assertEquals( 0, lnis.outbound.size(), "check message sent(12)");
        assertEquals( THROWN, t.getKnownState(), "check known state after echoed (12)");
    }

    @Test
    public void testSetKnownStateFromOutputStateReport() {
        lnt.setFeedbackMode("DIRECT");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x00, 0x00} ));
        assertEquals( 0, t.getKnownState(), "check known state after message (1)");

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x14, 0x10, 0x00}));
        assertEquals( THROWN, t.getKnownState(), "check known state after message (2)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x20, 0x00} ));
        assertEquals( CLOSED, t.getKnownState(), "check known state after message (3)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x30, 0x00} ));
        assertEquals( THROWN+CLOSED, t.getKnownState(), "check known state after message (4)");

        lnt.setFeedbackMode("INDIRECT");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x00, 0x00} ));
        assertEquals( THROWN+CLOSED, t.getKnownState(), "check known state after message (5)");

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x14, 0x10, 0x00}));
        assertEquals( THROWN+CLOSED, t.getKnownState(), "check known state after message (6)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x20, 0x00} ));
        assertEquals( THROWN+CLOSED, t.getKnownState(), "check known state after message (7)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x30, 0x00} ));
        assertEquals( THROWN+CLOSED, t.getKnownState(), "check known state after message (8)");

        lnt.setFeedbackMode("MONITORING");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x00, 0x00} ));
        assertEquals( 0, t.getKnownState(), "check known state after message (9)");

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x14, 0x10, 0x00}));
        assertEquals( THROWN, t.getKnownState(), "check known state after message (10)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x20, 0x00} ));
        assertEquals( CLOSED, t.getKnownState(), "check known state after message (11)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x30, 0x00} ));
        assertEquals( THROWN+CLOSED, t.getKnownState(), "check known state after message (12)");

    }

    @Test
    public void testComputeFeedbackFromSwitchOffReport() {
        lnt.setFeedbackMode("INDIRECT");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x40, 0x00} ));
        assertEquals( UNKNOWN, t.getKnownState(), "check known state after message (1)");

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x14, 0x50, 0x00}));
        assertEquals( UNKNOWN, t.getKnownState(), "check known state after message (2)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x60, 0x00} ));
        assertEquals( THROWN, t.getKnownState(), "check known state after message (3)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x70, 0x00} ));
        assertEquals( CLOSED, t.getKnownState(), "check known state after message (4)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x40, 0x00} ));
        assertEquals( CLOSED, t.getKnownState(), "check known state after message (5)");

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x14, 0x50, 0x00}));
        assertEquals( CLOSED, t.getKnownState(), "check known state after message (6)");

        lnt.setFeedbackMode("EXACT");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x60, 0x00} ));
        assertEquals( INCONSISTENT, t.getKnownState(), "check known state after message (7)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x40, 0x00} ));
        assertEquals( INCONSISTENT, t.getKnownState(), "check known state after message (8)");

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x14, 0x50, 0x00}));
        assertEquals( THROWN, t.getKnownState(), "check known state after message (9)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x14, 0x70, 0x00} ));
        assertEquals( CLOSED, t.getKnownState(), "check known state after message (10)");

        lnt = new LnTurnout("L", 22, lnis);
        lnt.setFeedbackMode("MONITORING");
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x15, 0x60, 0x00} ));
        assertEquals( CLOSED, t.getKnownState(), "check known state after message (11)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x15, 0x40, 0x00} ));
        assertEquals( CLOSED, t.getKnownState(), "check known state after message (12)");

        lnt.messageFromManager(new LocoNetMessage(new int[]{0xb1, 0x15, 0x50, 0x00}));
        assertEquals( CLOSED, t.getKnownState(), "check known state after message (13)");

        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x15, 0x70, 0x00} ));
        assertEquals( CLOSED, t.getKnownState(), "check known state after message (14)");

        lnt = new LnTurnout("L", 23, lnis);
        lnt.messageFromManager(new LocoNetMessage(new int[] {0xb1, 0x16, 0x60, 0x00} ));
        assertEquals( CLOSED, t.getKnownState(), "check known state after message (15)");
    }

    @Test
    public void testAdjustStateForInversion() {
        assertFalse( lnt.getInverted(), "check default inversion");
        lnt.setBinaryOutput(true);
        lnt.setUseOffSwReqAsConfirmation(false);
        lnt.setCommandedState(CLOSED);
        assertEquals( CLOSED, t.getCommandedState(), "check commanded state after forward closed to layout (1)");
        assertEquals( 1, lnis.outbound.size(), "check num messages sent after forward closed to layout (1)");
        assertEquals( 0x30, lnis.outbound.get(0).getElement(2), "check byte 2 of message (1)");

        lnt.setCommandedState(THROWN);
        assertEquals( THROWN, t.getCommandedState(), "check commanded state after forward thrown to layout (2)");
        assertEquals( 2, lnis.outbound.size(), "check num messages sent after forward thrown to layout (2)");
        assertEquals( 0x10, lnis.outbound.get(1).getElement(2), "check byte 2 of message (2)");

        lnt.setInverted(true);
        // when inverted, the commanded state remains unmodified; only the LocoNet
        // message sent gets state inverted.
        lnt.setCommandedState(THROWN);
        assertEquals( THROWN, t.getCommandedState(), "check commanded state after forward closed to layout (3)");
        assertEquals( 3, lnis.outbound.size(), "check num messages sent after forward closed to layout (3)");
        assertEquals( 0x30, lnis.outbound.get(2).getElement(2), "check byte 2 of message (1)");

        lnt.setCommandedState(CLOSED);
        assertEquals( CLOSED, t.getCommandedState(), "check commanded state after forward thrown to layout (2)");
        assertEquals( 4, lnis.outbound.size(), "check num messages sent after forward thrown to layout (4)");
        assertEquals( 0x10, lnis.outbound.get(3).getElement(2), "check byte 2 of message (2)");

    }

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);

        // outwait any pending delayed sends
        JUnitUtil.waitFor( LnTurnout.METERINTERVAL + 25 );

        // create object under test
        t = new LnTurnout("L", 21, lnis);
        lnt=(LnTurnout) t;
    }

    @AfterEach
    @Override
    public void tearDown(){
        t.dispose();
        JUnitUtil.tearDown();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnTurnoutTest.class);

}
