package jmri.jmrix.lenz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.lenz.XNetReply class
 *
 * @author Bob Jacobsen
 * @author  Paul Bender Copyright (C) 2004-2018
 */
public class XNetReplyTest extends jmri.jmrix.AbstractMessageTestBase {
       
    protected XNetReply msg = null;

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        msg = new XNetReply("12 34 AB 03 19 06 0B B1");
        assertEquals( 8, msg.getNumDataElements(), "length");
        assertEquals( 0x12, msg.getElement(0) & 0xFF, "0th byte");
        assertEquals( 0x34, msg.getElement(1) & 0xFF, "1st byte");
        assertEquals( 0xAB, msg.getElement(2) & 0xFF, "2nd byte");
        assertEquals( 0x03, msg.getElement(3) & 0xFF, "3rd byte");
        assertEquals( 0x19, msg.getElement(4) & 0xFF, "4th byte");
        assertEquals( 0x06, msg.getElement(5) & 0xFF, "5th byte");
        assertEquals( 0x0B, msg.getElement(6) & 0xFF, "6th byte");
        assertEquals( 0xB1, msg.getElement(7) & 0xFF, "7th byte");
    }

    // Test the string constructor with an empty string paramter.
    @Test
    public void testStringCtorEmptyString() {
        assertEquals( 0, msg.getNumDataElements(), "length");
        assertEquals( "", msg.toString(), "empty reply");
    }

    // Test the copy constructor.
    @Test
    public void testCopyCtor() {
        XNetReply x = new XNetReply("12 34 AB 03 19 06 0B B1");
        msg = new XNetReply(x);
        assertEquals( x.getNumDataElements(), msg.getNumDataElements(), "length");
        assertEquals( x.getElement(0), msg.getElement(0), "0th byte");
        assertEquals( x.getElement(1), msg.getElement(1), "1st byte");
        assertEquals( x.getElement(2), msg.getElement(2), "2nd byte");
        assertEquals( x.getElement(3), msg.getElement(3), "3rd byte");
        assertEquals( x.getElement(4), msg.getElement(4), "4th byte");
        assertEquals( x.getElement(5), msg.getElement(5), "5th byte");
        assertEquals( x.getElement(6), msg.getElement(6), "6th byte");
        assertEquals( x.getElement(7), msg.getElement(7), "7th byte");
    }

    // Test the XNetMessage constructor.
    @Test
    public void testXNetMessageCtor() {
        XNetMessage x = new XNetMessage("12 34 AB 03 19 06 0B B1");
        msg = new XNetReply(x);
        assertEquals( x.getNumDataElements(), msg.getNumDataElements(), "length");
        assertEquals( x.getElement(0)& 0xFF, msg.getElement(0)& 0xFF, "0th byte");
        assertEquals( x.getElement(1)& 0xFF, msg.getElement(1)& 0xFF, "1st byte");
        assertEquals( x.getElement(2)& 0xFF, msg.getElement(2)& 0xFF, "2nd byte");
        assertEquals( x.getElement(3)& 0xFF, msg.getElement(3)& 0xFF, "3rd byte");
        assertEquals( x.getElement(4)& 0xFF, msg.getElement(4)& 0xFF, "4th byte");
        assertEquals( x.getElement(5)& 0xFF, msg.getElement(5)& 0xFF, "5th byte");
        assertEquals( x.getElement(6)& 0xFF, msg.getElement(6)& 0xFF, "6th byte");
        assertEquals( x.getElement(7)& 0xFF, msg.getElement(7)& 0xFF, "7th byte");
    }

    // check parity operations
    @Test
    public void testParity() {
        msg = new XNetReply("21 21 00");
        assertEquals( 0, msg.getElement(2), "parity set test 1");
        assertTrue( msg.checkParity(), "parity check test 1");

        msg = new XNetReply("21 21 00");
        msg.setElement(0, 0x21);
        msg.setElement(1, ~0x21);
        msg.setParity();
        assertEquals( 0xFF, msg.getElement(2), "parity set test 2");
        assertTrue( msg.checkParity(), "parity check test 2");

        msg = new XNetReply("21 21 00");
        msg.setElement(0, 0x18);
        msg.setElement(1, 0x36);
        msg.setParity();
        assertEquals( 0x2E, msg.getElement(2), "parity set test 3");
        assertTrue( msg.checkParity(), "parity check test 3");

        msg = new XNetReply("21 21 00");
        msg.setElement(0, 0x87);
        msg.setElement(1, 0x31);
        msg.setParity();
        assertEquals( 0xB6, msg.getElement(2), "parity set test 4");
        assertTrue( msg.checkParity(), "parity check test 4");

        msg = new XNetReply("21 21 00");
        msg.setElement(0, 0x18);
        msg.setElement(1, 0x36);
        msg.setElement(2, 0x0e);
        assertFalse( msg.checkParity(), "parity check test 5");

        msg = new XNetReply("21 21 00");
        msg.setElement(0, 0x18);
        msg.setElement(1, 0x36);
        msg.setElement(2, 0x8e);
        assertFalse( msg.checkParity(), "parity check test 6");
    }

    // test accessor methods for elements.
    // check getOpCodeHex
    @Test
    public void testGetOpCodeHex(){
        msg = new XNetReply("63 14 01 04 72");
        assertEquals( "0x63", msg.getOpCodeHex(), "getOpCodeHex Return Value");
    }


    // check getElementBCD
    @Test
    public void testGetElementBCD(){
        msg=new XNetReply("63 14 01 04 72");
        assertEquals( 14,(long)msg.getElementBCD(1), "getElementBCD Return Value");
    }

    // check skipPrefix
    @Test
    public void testSkipPrefix(){
        msg=new XNetReply("63 14 01 04 72");
        // skip prefix currently always returns -1, there is no prefix.
        assertEquals( -1, msg.skipPrefix(0), "skipPrefix return value");
    }


    // get information from specific types of messages.

    // check is service mode response
    @Test
    public void testIsServiceModeResponse() {
        // CV 1 in direct mode.
        XNetReply r = new XNetReply("63 14 01 04 72");
        assertTrue(r.isServiceModeResponse());
        // CV 257 in direct mode.
        r = new XNetReply("63 15 01 04 72");
        assertTrue(r.isServiceModeResponse());
        // CV 513 in direct mode.
        r = new XNetReply("63 16 01 04 72");
        assertTrue(r.isServiceModeResponse());
        // CV 769 in direct mode.
        r = new XNetReply("63 17 01 04 72");
        assertTrue(r.isServiceModeResponse());
        // CV 1 in paged mode.
        r = new XNetReply("63 10 01 04 76");
        assertTrue(r.isServiceModeResponse());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertTrue(r.isServiceModeResponse());
        // CV 286 in direct mode.
        r = new XNetReply("63 15 1E 14 7C");
        assertTrue(r.isServiceModeResponse());
        // not a service mode response.
        r = new XNetReply("01 04 05");
        assertFalse(r.isServiceModeResponse());
        // Command Station Version reply
        r = new XNetReply("63 21 36 00 55");
        assertFalse(r.isServiceModeResponse());
    }

   @Test
    public void testToMonitorStringServiceModeDirectResponse(){
        XNetReply r = new XNetReply("63 14 01 04 72");
        assertEquals("Service Mode: Direct Programming Mode Response: CV:1 Value:4",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringServiceModePagedResponse(){
        XNetReply r = new XNetReply("63 10 01 04 76");
        assertEquals("Service Mode: Register or Paged Mode Response: CV:1 Value:4",
            r.toMonitorString(), "Monitor String");
    }

    // check is paged mode response
    @Test
    public void testIsPagedModeResponse() {
        // CV 1 in direct mode.
        XNetReply r = new XNetReply("63 14 01 04 72");
        assertFalse(r.isPagedModeResponse());
        // CV 1 in paged mode.
        r = new XNetReply("63 10 01 04 76");
        assertTrue(r.isPagedModeResponse());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertTrue(r.isPagedModeResponse());
        // CV 286 in direct mode.
        r = new XNetReply("63 15 1E 14 7C");
        assertFalse(r.isPagedModeResponse());
    }

    // check is direct mode response
    @Test
    public void testIsDirectModeResponse() {
        // CV 1 in direct mode.
        XNetReply r = new XNetReply("63 14 01 04 72");
        assertTrue(r.isDirectModeResponse());
        // CV 257 in direct mode.
        r = new XNetReply("63 15 01 04 72");
        assertTrue(r.isDirectModeResponse());
        // CV 513 in direct mode.
        r = new XNetReply("63 16 01 04 72");
        assertTrue(r.isDirectModeResponse());
        // CV 769 in direct mode.
        r = new XNetReply("63 17 01 04 72");
        assertTrue(r.isDirectModeResponse());
        // CV 1 in paged mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isDirectModeResponse());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isDirectModeResponse());
        // CV 286 in direct mode.
        r = new XNetReply("63 15 1E 14 7C");
        assertTrue(r.isDirectModeResponse());
    }

    // check get service mode CV Number response code.
    @Test
    public void testGetServiceModeCVNumber() {
        // CV 1 in direct mode.
        XNetReply r = new XNetReply("63 14 01 04 72");
        assertEquals( 1, r.getServiceModeCVNumber(), "Direct Mode CV<256");
        // CV 1 in paged mode.
        r = new XNetReply("63 10 01 04 76");
        assertEquals( 1, r.getServiceModeCVNumber(), "Paged Mode CV<256");
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertEquals( 1, r.getServiceModeCVNumber(), "Register Mode CV<256");
        assertTrue(r.isServiceModeResponse());
        // CV 286 in direct mode.
        r = new XNetReply("63 15 1E 14 7C");
        assertEquals( 286, r.getServiceModeCVNumber(), "Direct Mode CV>256");
        // not a service mode response.
        r = new XNetReply("01 04 05");
        assertEquals( -1, r.getServiceModeCVNumber(), "non-ServiceMode message");
    }

    // check get service mode CV Value response code.
    @Test
    public void testGetServiceModeCVValue() {
        // CV 1 in direct mode.
        XNetReply r = new XNetReply("63 14 01 04 72");
        assertEquals( 4, r.getServiceModeCVValue(), "Direct Mode CV<256");
        // CV 1 in paged mode.
        r = new XNetReply("63 10 01 04 76");
        assertEquals( 4, r.getServiceModeCVValue(), "Paged Mode CV<256");
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertEquals( 4, r.getServiceModeCVValue(), "Register Mode CV<256");
        assertTrue(r.isServiceModeResponse());
        // CV 286 in direct mode.
        r = new XNetReply("63 15 1E 14 7C");
        assertEquals( 20, r.getServiceModeCVValue(), "Direct Mode CV>256");
        // not a service mode response.
        r = new XNetReply("01 04 05");
        assertEquals( -1, r.getServiceModeCVValue(), "Non Service Mode Response");
    }

    // From feedback Messages
    // check is this a feedback response
    @Test
    public void testIsFeedbackResponse() {
        // feedback message for turnout
        XNetReply r = new XNetReply("42 05 48 0f");
        assertTrue(r.isFeedbackMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isFeedbackMessage());
    }

    // check is this a broadcast feedback response
    @Test
    public void testIsFeedbackBroadcastResponse() {
        // feedback message for turnout
        XNetReply r = new XNetReply("42 05 48 0f");
        assertTrue(r.isFeedbackBroadcastMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isFeedbackBroadcastMessage());
    }

    // getting the address from a feedback response
    @Test
    public void testGetTurnoutMsgAddr() {
        // feedback message for turnout 21
        XNetReply r = new XNetReply("42 05 01 63");
        assertEquals( 21, r.getTurnoutMsgAddr(1), "Broadcast Turnout Message Address");

        // feedback message for turnout 22, which returns address 21 
        // (addresses are in pairs).
        r = new XNetReply("42 05 04 43");
        assertEquals( 21, r.getTurnoutMsgAddr(), "Turnout Message Address");
        // turnout 22 with feedback 
        r = new XNetReply("42 05 24 63");
        assertEquals( 21, r.getTurnoutMsgAddr(), "Turnout Message Address");
        // turnout 21 with feedback 
        r = new XNetReply("42 05 22 65");
        assertEquals( 21, r.getTurnoutMsgAddr(), "Turnout Message Address");

        // feedback message for turnout 21 or 22,but neither 21 or 22 operated.
        r = new XNetReply("42 05 00 47");
        assertEquals( 21, r.getTurnoutMsgAddr(), "Turnout Message Address");
        // feedback message for turnout 24, returns 23.
        r = new XNetReply("42 05 14 53");
        assertEquals( 23, r.getTurnoutMsgAddr(), "Turnout Message Address");
        // feedback message for turnout 23
        r = new XNetReply("42 05 11 53");
        assertEquals( 23, r.getTurnoutMsgAddr(), "Turnout Message Address");
        // feedback message for turnout 23 or 24,but neither 23 or 24 operated.
        r = new XNetReply("42 05 10 53");
        assertEquals( 23, r.getTurnoutMsgAddr(), "Turnout Message Address");

        // feedback message for turnout 24, returns address 23.
        r = new XNetReply("42 05 18 53");
        assertEquals( 23, r.getTurnoutMsgAddr(), "Turnout Message Address");

        // feedback message for a feedback encoder, should return -1.
        r = new XNetReply("42 05 48 0F");
        assertEquals( -1, r.getTurnoutMsgAddr(), "Turnout Message Address for Feedback encoder");

        // feedback message for a non-feedback message, should return -1.
        r = new XNetReply("63 10 01 04 76");
        assertEquals( -1, r.getTurnoutMsgAddr(), "Turnout Message Address for Feedback Other message");

    }

    // getting the address from a broadcast feedback response
    @Test
    public void testGetBroadcastTurnoutMsgAddr() {
        // feedback for turnout 21  
        XNetReply r = new XNetReply("42 05 01 63");
        assertEquals( 21, r.getTurnoutMsgAddr(1), "Broadcast Turnout Message Address");

        // feedback message for turnout 22, which returns address 21 
        // (addresses are in pairs).
        r = new XNetReply("42 05 04 43");
        assertEquals( 21, r.getTurnoutMsgAddr(1), "Broadcast Turnout Message Address");

        // turnout 22 with feedback 
        r = new XNetReply("42 05 24 63");
        assertEquals( 21, r.getTurnoutMsgAddr(1), "Broadcast Turnout Message Address");

        // feedback message for turnout 21 or 22, but neither 21 or 22 operated.
        r = new XNetReply("42 05 00 47");
        assertEquals( 21, r.getTurnoutMsgAddr(1), "Broadcast Turnout Message Address");

        // feedback message for turnout 23
        r = new XNetReply("42 05 14 53");
        assertEquals( 23, r.getTurnoutMsgAddr(1), "Broadcast Turnout Message Address");

        // feedback message for turnout 24, returns address 23.
        r = new XNetReply("42 05 18 53");
        assertEquals( 23, r.getTurnoutMsgAddr(1), "Broadcast Turnout Message Address");

        // feedback message for turnout 23
        r = new XNetReply("42 05 11 53");
        assertEquals( 23, r.getTurnoutMsgAddr(1), "Turnout Message Address");

        // feedback message for turnout 23 or 24, but neither 23 or 24 operated.
        r = new XNetReply("42 05 10 53");
        assertEquals( 23, r.getTurnoutMsgAddr(1), "Broadcast Turnout Message Address");

        // feedback message for a feedback encoder, should return -1.
        r = new XNetReply("42 05 48 0F");
        assertEquals( -1, r.getTurnoutMsgAddr(1),
                "Broadcast Turnout Message Address for Feedback encoder");

        // feedback message for a non-feedback message, should return -1.
        r = new XNetReply("63 10 01 04 76");
        assertEquals( -1, r.getTurnoutMsgAddr(1),
                "Broadcast Turnout Message Address for Feedback Other message");
    }

    // getting the feedback message type (turnout without feedback, 
    // turnout with feedback, or sensor)
    @Test
    public void testGetFeedbackMessageType() {
        // feedback message for turnout
        XNetReply r = new XNetReply("42 05 04 43");
        assertEquals( 0, r.getFeedbackMessageType(), "Feedback Message Type");
        r = new XNetReply("42 05 24 63");
        assertEquals( 1, r.getFeedbackMessageType(), "Feedback Message Type");
        r = new XNetReply("42 05 48 0F");
        assertEquals( 2, r.getFeedbackMessageType(), "Feedback Message Type");
        r = new XNetReply("63 10 01 04 76"); // not a feedback message.
        assertEquals( -1, r.getFeedbackMessageType(), "Feedback Message Type");
    }

    // getting the feedback message type (turnout without feedback, 
    // turnout with feedback, or sensor) from a broadcast feedback message.
    @Test
    public void testGetBroadcastFeedbackMessageType() {
        // feedback message for turnout
        XNetReply r = new XNetReply("42 05 04 43");
        assertEquals( 0, r.getFeedbackMessageType(1), "Broadcast Feedback Message Type");
        r = new XNetReply("42 05 24 63");
        assertEquals( 1, r.getFeedbackMessageType(1), "Broadcast Feedback Message Type");
        r = new XNetReply("42 05 48 0F");
        assertEquals( 2, r.getFeedbackMessageType(1), "Broadcast Feedback Message Type");
        r = new XNetReply("63 10 01 04 76"); // not a feedback message.
        assertEquals( -1, r.getFeedbackMessageType(1), "Broadcast Feedback Message Type");
    }

    // getting the status from a turnout feedback response
    @Test
    public void testGetTurnoutMessageStatus() {
        // feedback message for turnout 22, closed
        XNetReply r = new XNetReply("42 05 04 43");
        assertEquals( Turnout.CLOSED, r.getTurnoutStatus(0), "Turnout Status");
        // feedback message for turnout 22, thrown
        r = new XNetReply("42 05 08 4F");
        assertEquals( Turnout.THROWN, r.getTurnoutStatus(0), "Turnout Status");

        // ask for address 21
        assertEquals( -1, r.getTurnoutStatus(1), "Turnout Status");
        // feedback message for turnout 22, with invalid state.
        r = new XNetReply("42 05 0C 45");
        assertEquals( -1 , r.getTurnoutStatus(0), "Turnout Status");

        // feedback message for turnout 21, closed
        r = new XNetReply("42 05 01 46");
        assertEquals( Turnout.CLOSED, r.getTurnoutStatus(1), "Turnout Status");
        // feedback message for turnout 21, thrown
        r = new XNetReply("42 05 02 45");
        assertEquals( Turnout.THROWN, r.getTurnoutStatus(1), "Turnout Status");
        // ask for address 22.
        assertEquals( -1, r.getTurnoutStatus(0), "Turnout Status");
        // send invalid value for parameter (only 0 and 1 are valid).
        assertEquals( -1, r.getTurnoutStatus(3), "Turnout Status");
        // feedback message for turnout 21, with invalid state.
        r = new XNetReply("42 05 03 47");
        assertEquals( -1 , r.getTurnoutStatus(1), "Turnout Status");
        // turnout status for a feedback message.
        r = new XNetReply("42 05 48 0F");
        assertEquals( -1, r.getTurnoutStatus(1), "Feedback Message Turnout Status");
        // feedback message for a non-feedback message, should return -1.
        r = new XNetReply("63 10 01 04 76");
        assertEquals( -1, r.getTurnoutStatus(1), "Turnout Message Status for a non-FeedbackMessage");
    }

    // getting the status from a turnout broadcast feedback response
    @Test
    public void testGetBroadcastTurnoutMessageStatus() {
        // feedback message for turnout 22, closed
        XNetReply r = new XNetReply("42 05 04 43");
        assertEquals( Turnout.CLOSED, r.getTurnoutStatus(1,0), "Broadcast Turnout Status");
        // feedback message for turnout 22, thrown
        r = new XNetReply("42 05 08 4F");
        assertEquals( Turnout.THROWN, r.getTurnoutStatus(1,0), "Broadcast Turnout Status");

        // ask for address 21
        assertEquals( -1, r.getTurnoutStatus(1, 1), "Broadcast Turnout Status");
        // feedback message for turnout 22, with invalid state.
        r = new XNetReply("42 05 0C 45");
        assertEquals( -1 , r.getTurnoutStatus(1,0), "Broadcast Turnout Status");
        // feedback message for turnout 21, closed
        r = new XNetReply("42 05 01 46");
        assertEquals( Turnout.CLOSED, r.getTurnoutStatus(1,1), "Broadcast Turnout Status");
        // feedback message for turnout 21, thrown
        r = new XNetReply("42 05 02 45");
        assertEquals( Turnout.THROWN, r.getTurnoutStatus(1,1), "Broadcast Turnout Status");
        // ask for address 22.
        assertEquals( -1, r.getTurnoutStatus(1, 0), "Broadcast Turnout Status");
        // send invalid value for parameter (only 0 and 1 are valid).
        assertEquals( -1, r.getTurnoutStatus(1, 3), "Broadcast Turnout Status");
        // feedback message for turnout 21, with invalid state.
        r = new XNetReply("42 05 03 47");
        assertEquals( -1 , r.getTurnoutStatus(1,1), "Broadcast Turnout Status");
        // turnout status for a feedback message.
        r = new XNetReply("42 05 48 0F");
        assertEquals( -1, r.getTurnoutStatus(1,1), "Broadcast Feedback Message Turnout Status");
        // feedback message for a non-feedback message, should return -1.
        r = new XNetReply("63 10 01 04 76");
        assertEquals( -1, r.getTurnoutStatus(1,1), "Broadcast Turnout Message Status for a non-FeedbackMessage");
    }

    // getting the address from a feedback encoder response
    @Test
    public void testGetEncoderMsgAddr() {
        // feedback message for sensor
        XNetReply r = new XNetReply("42 05 48 0f");
        assertEquals( 5, r.getFeedbackEncoderMsgAddr(), "Feedback Encoder Message Address");
        // turnout
        r = new XNetReply("42 05 08 4F");
        assertEquals( -1, r.getFeedbackEncoderMsgAddr(), "Feedback Encoder Message Address");
        r = new XNetReply("63 10 01 04 76"); // not a feedback message.
        assertEquals( -1, r.getFeedbackEncoderMsgAddr(), "Feedback Encoder Message Address");
    }

    // getting the address from a broadcast feedback encoder response
    @Test
    public void testGetBroadcastEncoderMsgAddr() {
        // feedback message for turnout
        XNetReply r = new XNetReply("42 05 48 0f");
        assertEquals( 5, r.getFeedbackEncoderMsgAddr(1), "Broadcast Feedback Encoder Message Address");
        // turnout
        r = new XNetReply("42 05 08 4F");
        assertEquals( -1, r.getFeedbackEncoderMsgAddr(1), "Feedback Encoder Message Address");
        r = new XNetReply("63 10 01 04 76"); // not a feedback message.
        assertEquals( -1, r.getFeedbackEncoderMsgAddr(1), "Feedback Encoder Message Address");
    }

    @Test
    public void testToMonitorStringFeedbackResponse() {
        // feedback message for turnout
        XNetReply r = new XNetReply("42 05 00 47");
        String targetString =
             "Feedback Response: Turnout without Feedback Turnout: 21 State: Not Operated; Turnout: 22 State: Not Operated";
        assertEquals( targetString, r.toMonitorString(), "Monitor String");
        r = new XNetReply("42 05 05 42");
        targetString =
             "Feedback Response: Turnout without Feedback Turnout: 21 State: Thrown Left; Turnout: 22 State: Thrown Left";
        assertEquals( targetString, r.toMonitorString(), "Monitor String");
        r = new XNetReply("42 05 0A 4C");
        targetString =
             "Feedback Response: Turnout without Feedback Turnout: 21 State: Thrown Right; Turnout: 22 State: Thrown Right";
        assertEquals( targetString, r.toMonitorString(), "Monitor String");
        r = new XNetReply("42 05 0F 48");
        targetString =
             "Feedback Response: Turnout without Feedback Turnout: 21 State: <invalid>; Turnout: 22 State: <invalid>";
        assertEquals( targetString, r.toMonitorString(), "Monitor String");
        r = new XNetReply("42 05 20 67");
        targetString =
              "Feedback Response: Turnout with Feedback Turnout: 21 State: Not Operated Motion Complete; Turnout: 22 State: Not Operated Motion Complete";
        assertEquals( targetString, r.toMonitorString(), "Monitor String");
        r = new XNetReply("42 05 25 62");
        targetString = "Feedback Response: Turnout with Feedback Turnout: 21 State: Thrown Left Motion Complete; Turnout: 22 State: Thrown Left Motion Complete";
        assertEquals( targetString, r.toMonitorString(), "Monitor String");
        r = new XNetReply("42 05 2A 6C");
        targetString =
              "Feedback Response: Turnout with Feedback Turnout: 21 State: Thrown Right Motion Complete; Turnout: 22 State: Thrown Right Motion Complete";
        assertEquals( targetString, r.toMonitorString(), "Monitor String");
        r = new XNetReply("42 05 2F 68");
        targetString =
              "Feedback Response: Turnout with Feedback Turnout: 21 State: <invalid> Motion Complete; Turnout: 22 State: <invalid> Motion Complete";
        assertEquals( targetString, r.toMonitorString(), "Monitor String");
        r = new XNetReply("42 05 48 0F");
        targetString =
              "Feedback Response: Feedback Encoder Base Address: 5 Contact: 1 State: Off; Contact: 2 State: Off; Contact: 3 State: Off; Contact: 4 State: On; ";
        assertEquals( targetString, r.toMonitorString(), "Monitor String");
        r = new XNetReply("42 05 57 0F");
        targetString =
              "Feedback Response: Feedback Encoder Base Address: 5 Contact: 5 State: On; Contact: 6 State: On; Contact: 7 State: On; Contact: 8 State: Off; ";
        assertEquals( targetString, r.toMonitorString(), "Monitor String");
    }

    // throttle related replies.
    @Test
    public void testGetThrottleMsgAddr() {
        // locomotive taken over by another device reply
        // short address
        XNetReply r = new XNetReply("E3 40 00 04 57");
        assertEquals( 4, r.getThrottleMsgAddr(), "Throttle Message Address");
        // long address
        r = new XNetReply("E3 40 C1 04 61");
        assertEquals( 260, r.getThrottleMsgAddr(), "Throttle Message Address");
        // not a throttle message.
        r = new XNetReply("42 05 48 0f");
        assertEquals( -1, r.getThrottleMsgAddr(), "Throttle Message Address");
    }

    // check is this a throttle response
    @Test
    public void testIsThrottleMessage() {
        // MUED Locomotive Address
        XNetReply r= new XNetReply("E2 24 04 C2");
        assertTrue(r.isThrottleMessage());
        // Locomotive Taken Over by another device
        r = new XNetReply("E3 40 C1 04 61");
        assertTrue(r.isThrottleMessage());
        // Normal Locomotive Information reply
        r= new XNetReply("E4 04 00 04 00 E4");
        assertTrue(r.isThrottleMessage());
        // MUED Locomotive Information reply
        r = new XNetReply("E5 14 C1 04 00 00 34");
        assertTrue(r.isThrottleMessage());
        // DH Address
        r = new XNetReply("E6 64 00 64 C1 C1 04 E2");
        assertTrue(r.isThrottleMessage());
        // XNet V1, locomotive available for operation
        r = new XNetReply("83 01 00 00 82");
        assertTrue(r.isThrottleMessage());
        // XNet V1, locomotive not available for operation
        r = new XNetReply("A3 01 00 00 A2");
        assertTrue(r.isThrottleMessage());
        // XNet V2, locomotive available for operation
        r = new XNetReply("84 01 00 00 00 85");
        assertTrue(r.isThrottleMessage());
        // XNet V2, locomotive not available for operation
        r = new XNetReply("A4 01 00 00 00 A5");
        assertTrue(r.isThrottleMessage());


        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isThrottleMessage());
    }

    @Test
    public void testToMonitorStringThrottleTakeoverMsg() {
        XNetReply r = new XNetReply("E3 40 00 04 57");
        String targetString = "Locomotive Information Response: Locomotive 4 is being operated by another device.";
        assertEquals( targetString, r.toMonitorString(), "Monitor String");
        r = new XNetReply("E3 40 C1 04 61");
        targetString = "Locomotive Information Response: Locomotive 260 is being operated by another device.";
        assertEquals( targetString, r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringNormalLocoInfoResponse() {
        XNetReply r= new XNetReply("E4 04 00 04 00 E4");
        assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 128 Speed Step Mode,Speed Step: 0. Address is Free for Operation. F0 Off; F1 Off; F2 Off; F3 On; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E4 04 04 04 00 E0");
        assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 128 Speed Step Mode,Speed Step: 3. Address is Free for Operation. F0 Off; F1 Off; F2 Off; F3 On; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E4 0A 00 04 00 EA");
        assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 28 Speed Step Mode,Speed Step: 0. Address in use by another device. F0 Off; F1 Off; F2 Off; F3 On; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E4 0A 0A 04 00 E0");
        assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 28 Speed Step Mode,Speed Step: 17. Address in use by another device. F0 Off; F1 Off; F2 Off; F3 On; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E4 01 00 1F FF F5");
        assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 27 Speed Step Mode,Speed Step: 0. Address is Free for Operation. F0 On; F1 On; F2 On; F3 On; F4 On; F5 On; F6 On; F7 On; F8 On; F9 On; F10 On; F11 On; F12 On; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E4 01 05 1F FF F0");
        assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 27 Speed Step Mode,Speed Step: 7. Address is Free for Operation. F0 On; F1 On; F2 On; F3 On; F4 On; F5 On; F6 On; F7 On; F8 On; F9 On; F10 On; F11 On; F12 On; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E4 00 00 00 00 E4");
        assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 14 Speed Step Mode,Speed Step: 0. Address is Free for Operation. F0 Off; F1 Off; F2 Off; F3 Off; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E4 00 04 00 00 E0");
        assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 14 Speed Step Mode,Speed Step: 3. Address is Free for Operation. F0 Off; F1 Off; F2 Off; F3 Off; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringMULocoInfoResponse() {
        XNetReply r = new XNetReply("E5 14 C1 04 00 00 34");
        assertEquals("Locomotive Information Response: Locomotive in Multiple Unit,Forward,in 128 Speed Step Mode,Speed Step: 64. Address is Free for Operation.F0 Off; F1 Off; F2 Off; F3 On; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringMUEliteLocoInfoResponse() {
        XNetReply r = new XNetReply("E5 F8 C1 04 00 00 34");
        assertEquals("Elite Speed/Direction Information: Locomotive 260,Reverse,in 14 Speed Step Mode,Speed Step: 0. Address is Free for Operation. ",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringEliteLocoFnInfoResponse() {
        XNetReply r = new XNetReply("E5 F9 C1 04 00 00 34");
        assertEquals("Elite Function Information: Locomotive 260 F0 Off; F1 Off; F2 Off; F3 Off; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringMUBaseAddressInfoResponse() {
        XNetReply r = new XNetReply("E2 14 C1 37");
        assertEquals("Locomotive Information Response: Multi Unit Base Address,Forward,in 128 Speed Step Mode,Speed Step: 64. Address is Free for Operation. ",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHLocoInfoResponse() {
        XNetReply r = new XNetReply("E6 64 00 64 C1 C1 04 E2");
        assertEquals("Locomotive Information Response: Locomotive in Double Header,Reverse,in 128 Speed Step Mode,Speed Step: 0. Address is Free for Operation. F0 Off; F1 Off; F2 Off; F3 On; F4 Off; F5 On; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 On; F12 On;  Second Locomotive in Double Header is: 260",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringV1LocoAvailable() {
        XNetReply r = new XNetReply("83 01 00 00 82");
        // this isn't actually translated to text, since we don't expect
        // to see a version 1 XBus system
        assertEquals("83 01 00 00 82",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringV1LocoNotAvailable() {
        XNetReply r = new XNetReply("A3 01 00 00 A2");
        // this isn't actually translated to text, since we don't expect
        // to see a version 1 XBus system
        assertEquals("A3 01 00 00 A2",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringV2LocoAvailable() {
        XNetReply r = new XNetReply("84 01 00 00 00 85");
        // this isn't actually translated to text, since we don't expect
        // to see a version 2 XBus system
        assertEquals("84 01 00 00 00 85",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringV2LocoNotAvailable() {
        XNetReply r = new XNetReply("A4 01 00 00 00 A5");
        // this isn't actually translated to text, since we don't expect
        // to see a version 2 XBus system
        assertEquals("A4 01 00 00 00 A5",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringNormalLocoFunctionMomentaryResponse() {
        XNetReply r= new XNetReply("E3 50 54 04 E3");
        assertEquals("Locomotive Information Response: Locomotive Function Status: F0 Momentary; F1 Continuous; F2 Continuous; F3 Momentary; F4 Continuous; F5 Continuous; F6 Continuous; F7 Momentary; F8 Continuous; F9 Continuous; F10 Continuous; F11 Continuous; F12 Continuous; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E3 50 00 00 93");
        assertEquals("Locomotive Information Response: Locomotive Function Status: F0 Continuous; F1 Continuous; F2 Continuous; F3 Continuous; F4 Continuous; F5 Continuous; F6 Continuous; F7 Continuous; F8 Continuous; F9 Continuous; F10 Continuous; F11 Continuous; F12 Continuous; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E3 50 5F FF 13");
        assertEquals("Locomotive Information Response: Locomotive Function Status: F0 Momentary; F1 Momentary; F2 Momentary; F3 Momentary; F4 Momentary; F5 Momentary; F6 Momentary; F7 Momentary; F8 Momentary; F9 Momentary; F10 Momentary; F11 Momentary; F12 Momentary; ",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringNormalLocoFunctionHighMomentaryResponse() {
        XNetReply r= new XNetReply("E4 51 00 54 04 E5");
        assertEquals("Locomotive F13-F28 Momentary Status: F13 Continuous; F14 Continuous; F15 Continuous; F16 Continuous; F17 Continuous; F18 Continuous; F19 Continuous; F20 Continuous; F21 Continuous; F22 Continuous; F23 Momentary; F24 Continuous; F25 Momentary; F26 Continuous; F27 Momentary; F28 Continuous; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E4 51 00 00 00 95");
        assertEquals("Locomotive F13-F28 Momentary Status: F13 Continuous; F14 Continuous; F15 Continuous; F16 Continuous; F17 Continuous; F18 Continuous; F19 Continuous; F20 Continuous; F21 Continuous; F22 Continuous; F23 Continuous; F24 Continuous; F25 Continuous; F26 Continuous; F27 Continuous; F28 Continuous; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E4 51 FF FF FF 45");
        assertEquals("Locomotive F13-F28 Momentary Status: F13 Momentary; F14 Momentary; F15 Momentary; F16 Momentary; F17 Momentary; F18 Momentary; F19 Momentary; F20 Momentary; F21 Momentary; F22 Momentary; F23 Momentary; F24 Momentary; F25 Momentary; F26 Momentary; F27 Momentary; F28 Momentary; ",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringNormalLocoFunctionHighResponse() {
        XNetReply r= new XNetReply("E3 52 54 04 E3");
        assertEquals("Locomotive Information Response: Locomotive F13-F28 Status: F13 Off; F14 Off; F15 On; F16 Off; F17 On; F18 Off; F19 On; F20 Off; F21 Off; F22 Off; F23 On; F24 Off; F25 Off; F26 Off; F27 Off; F28 Off; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E3 52 00 00 91");
        assertEquals("Locomotive Information Response: Locomotive F13-F28 Status: F13 Off; F14 Off; F15 Off; F16 Off; F17 Off; F18 Off; F19 Off; F20 Off; F21 Off; F22 Off; F23 Off; F24 Off; F25 Off; F26 Off; F27 Off; F28 Off; ",
            r.toMonitorString(), "Monitor String");
        r= new XNetReply("E3 52 FF FF 91");
        assertEquals("Locomotive Information Response: Locomotive F13-F28 Status: F13 On; F14 On; F15 On; F16 On; F17 On; F18 On; F19 On; F20 On; F21 On; F22 On; F23 On; F24 On; F25 On; F26 On; F27 On; F28 On; ",
            r.toMonitorString(), "Monitor String");
    }


    // check if this is a throttle takeover response
    @Test
    public void testIsThrottleTakenOverMessage() {
        // Normal Locomotive Information reply
        XNetReply r = new XNetReply("E3 40 C1 04 61");
        assertTrue(r.isThrottleTakenOverMessage());
        // Function reply
        r = new XNetReply("E3 08 00 00 E6");
        assertFalse(r.isThrottleTakenOverMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isThrottleTakenOverMessage());
    }

    // check is this a consist response
    @Test
    public void testIsConsistMessage() {
        // MU/DH  Error
        XNetReply r = new XNetReply("E1 81 60");
        assertTrue(r.isConsistMessage());
        // DH Info (XNetV1)
        r = new XNetReply("C5 04 00 00 00 00 C1");
        assertTrue(r.isConsistMessage());
        // DH Info (XNetV2)
        r = new XNetReply("C6 04 00 00 00 00 00 C2");
        assertTrue(r.isConsistMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isConsistMessage());
    }


    // some common messages.
    // check is this an OK message
    @Test
    public void testIsOkMessage() {
        // "OK" message
        XNetReply r = new XNetReply("01 04 05");
        assertTrue(r.isOkMessage());
        // Error message
        r = new XNetReply("01 01 00");
        assertFalse(r.isOkMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isOkMessage());
    }

    @Test
    public void testToMonitorStringOKMessage(){
        XNetReply r = new XNetReply("01 04 05");
        assertEquals("Command Successfully Sent/Normal Operations Resumed after timeout",
            r.toMonitorString(), "Monitor String");
    }

    // check is this an Timeslot Restored message
    @Test
    public void testIsTimeSlotRestoredMessage() {
        // Timeslot restored message
        XNetReply r = new XNetReply("01 07 06");
        assertTrue(r.isTimeSlotRestored());
        // Error message
        r = new XNetReply("01 01 00");
        assertFalse(r.isTimeSlotRestored());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isTimeSlotRestored());
    }

    // check is this an Timeslot Revoked message
    @Test
    public void testIsTimeSlotRevokedMessage() {
        // Timeslot restored message
        XNetReply r = new XNetReply("01 05 04");
        assertTrue(r.isTimeSlotRevoked());
        // Error message
        r = new XNetReply("01 01 00");
        assertFalse(r.isTimeSlotRevoked());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isTimeSlotRevoked());
    }

    // check is this a CS Busy message
    @Test
    public void testIsCSBusyMessage() {
        // CS Busy Message
        XNetReply r = new XNetReply("61 81 e0");
        assertTrue(r.isCSBusyMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isCSBusyMessage());
    }

    @Test
    public void testToMonitorStringCSBusyMessage(){
        XNetReply r = new XNetReply("61 81 e0");
        assertEquals("Command Station Busy",
            r.toMonitorString(), "Monitor String");
    }

    // check is this a CS transfer error message
    @Test
    public void testIsCSTransferError() {
        // Command Station Transfer Error Message
        XNetReply r = new XNetReply("61 80 e1");
        assertTrue(r.isCSTransferError());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isCSTransferError());
    }

    @Test
    public void testToMonitorStringCSTransferError(){
        XNetReply r = new XNetReply("61 80 e1");
        assertEquals("Command Station Reported Transfer Error",
            r.toMonitorString(), "Monitor String");
    }


    // check is this a Communication Error  message
    @Test
    public void testIsCommErrorMessage() {
        // Error between interface and the PC
        XNetReply r = new XNetReply("01 01 00");
        assertTrue(r.isCommErrorMessage());
        // Error between interface and the Command Station
        r = new XNetReply("01 02 03");
        assertTrue(r.isCommErrorMessage());
        // Unkonwn Communication Error
        r = new XNetReply("01 03 02");
        assertTrue(r.isCommErrorMessage());
        // LI10x Buffer Overflow
        r = new XNetReply("01 06 07");
        assertTrue(r.isCommErrorMessage());
        // LIUSB request resend of data.
        r = new XNetReply("01 0A 0B");
        assertTrue(r.isCommErrorMessage());
        // Timeslot Error
        r = new XNetReply("01 05 04");
        assertTrue(r.isCommErrorMessage());
        // Timeslot Restored
        r = new XNetReply("01 07 06");
        assertTrue(r.isCommErrorMessage());
        // Data sent while there is no Timeslot
        r = new XNetReply("01 08 09");
        assertTrue(r.isCommErrorMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isCommErrorMessage());
    }

    @Test
    public void testToMonitorStringErrorPCtoLI(){
        XNetReply r = new XNetReply("01 01 00");
        assertEquals("Error occurred between the interface and the PC",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringErrorLItoCS(){
        XNetReply r = new XNetReply("01 02 03");
        assertEquals("Error occurred between the interface and the command station.",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringErrorUnknown(){
        XNetReply r = new XNetReply("01 03 02");
        assertEquals("Unknown Communication Error",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringErrorNoTimeslot(){
        XNetReply r = new XNetReply("01 05 04");
        assertEquals("The Command Station is no longer providing the LI a timeslot for communication",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringErrorBufferOverflow(){
        XNetReply r = new XNetReply("01 06 07");
        assertEquals("Buffer overflow in the LI",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringTimeSlotRestored(){
        XNetReply r = new XNetReply("01 07 06");
        assertEquals("Timeslot Restored",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDataSentNoTimeslot(){
        XNetReply r = new XNetReply("01 08 09");
        assertEquals("Request Sent while the CS is not providing a Timeslot",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringErrorBadData(){
        XNetReply r = new XNetReply("01 09 08");
        assertEquals( "Bad Data in Request sent to CS",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringRetransmissionRequested(){
        XNetReply r = new XNetReply("01 0A 0B");
        assertEquals( "Retransmission Requested",
            r.toMonitorString(), "Monitor String");
    }

    // check is this a Timeslot message  message
    @Test
    public void testIsTimeSlotErrorMessage() {
        // Timeslot Error
        XNetReply r = new XNetReply("01 05 04");
        assertTrue(r.isTimeSlotErrorMessage());
        // Timeslot Restored
        r = new XNetReply("01 07 06");
        assertTrue(r.isTimeSlotErrorMessage());
        // Data sent while there is no Timeslot
        r = new XNetReply("01 08 09");
        assertTrue(r.isTimeSlotErrorMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        assertFalse(r.isTimeSlotErrorMessage());
    }

    // check if this message is a retransmittable error message.
    @Test
    public void testIsRetransmittableErrorMsg(){
       XNetReply r = new XNetReply("61 81 e3"); // CS Busy Message
       assertTrue(r.isRetransmittableErrorMsg());
       r = new XNetReply("61 80 e1"); // transfer error
       assertTrue(r.isRetransmittableErrorMsg());
       r = new XNetReply("01 06 07"); // Buffer overflow (Comm Error)
       assertTrue(r.isRetransmittableErrorMsg());
       r = new XNetReply("01 04 05"); // OK message
       assertFalse(r.isRetransmittableErrorMsg());
    }

    // check if this is an unsolicited message
    @Test
    public void testIsUnsolicitedMessage() {
        // CV 1 in register mode.
        XNetReply r= new XNetReply("63 10 01 04 76");
        assertFalse(r.isUnsolicited());
        r.setUnsolicited();
        assertTrue(r.isUnsolicited());
        // Throttle taken over message
        r = new XNetReply("E3 40 C1 04 61");
        assertTrue(r.isUnsolicited());
        // feedback message.
        r = new XNetReply("42 05 48 0f");
        assertFalse(r.isUnsolicited());
    }

    // check toMonitor string for informational messages from the command station
    // not covered elsewhere.
    @Test
    public void testToMonitorStringBCEmergencyOff(){
        XNetReply r = new XNetReply("61 00 61");
        assertEquals("Broadcast: Emergency Off (short circuit)",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringBCNormalOpers(){
        XNetReply r = new XNetReply("61 01 60");
        assertEquals("Broadcast: Normal Operations Resumed",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringBCServiceModeEntry(){
        XNetReply r = new XNetReply("61 02 63");
        assertEquals("Broadcast: Service Mode Entry",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringServiceModeCSReady(){
        XNetReply r = new XNetReply("61 11 70");
        assertEquals("Service Mode: Command Station Ready",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringServiceModeShortCircuit(){
        XNetReply r = new XNetReply("61 12 73");
        assertEquals("Service Mode: Short Circuit",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringServiceModeByteNotFound(){
        XNetReply r = new XNetReply("61 13 72");
        assertEquals("Service Mode: Data Byte Not Found",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringServiceModeCSBusy(){
        XNetReply r = new XNetReply("61 1F 7E");
        assertEquals("Service Mode: Command Station Busy",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringCSNotSupported(){
        XNetReply r = new XNetReply("61 82 E3");
        assertEquals("XpressNet Instruction not supported by Command Station",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHV1_V2ErrorNotOperated(){
        XNetReply r = new XNetReply("61 83 E2");
        assertEquals("XBus V1 and V2 MU+DH error: Selected Locomotive has not been operated by this XpressNet device or address 0 selected",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHV1_V2ErrorInUse(){
        XNetReply r = new XNetReply("61 84 E5");
        assertEquals("XBus V1 and V2 MU+DH error: Selected Locomotive is being operated by another XpressNet device",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHV1_V2ErrorAlreadyDH(){
        XNetReply r = new XNetReply("61 85 E4");
        assertEquals("XBus V1 and V2 MU+DH error: Selected Locomotive already in MU or DH",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHV1_V2ErrorNonZeroSpeed(){
        XNetReply r = new XNetReply("61 86 E7");
        assertEquals("XBus V1 and V2 MU+DH error: Unit selected for MU or DH has speed setting other than 0",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringErrorNotOperated(){
        XNetReply r = new XNetReply("E1 81 60");
        assertEquals("XpressNet MU+DH error: Selected Locomotive has not been operated by this XpressNet device or address 0 selected",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHErrorInUse(){
        XNetReply r = new XNetReply("E1 82 63");
        assertEquals("XpressNet MU+DH error: Selected Locomotive is being operated by another XpressNet device",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHErrorAlreadyDH(){
        XNetReply r = new XNetReply("E1 83 62");
        assertEquals("XpressNet MU+DH error: Selected Locomotive already in MU or DH",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHErrorNonZeroSpeed(){
        XNetReply r = new XNetReply("E1 84 65");
        assertEquals("XpressNet MU+DH error: Unit selected for MU or DH has speed setting other than 0",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHErrorLocoNotMUed(){
        XNetReply r = new XNetReply("E1 85 64");
        assertEquals("XpressNet MU+DH error: Locomotive not in a MU",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHErrorAddressNotMUBase(){
        XNetReply r = new XNetReply("E1 86 67");
        assertEquals("XpressNet MU+DH error: Locomotive address not a multi-unit base address",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHErrorCanNotDelete(){
        XNetReply r = new XNetReply("E1 87 66");
        assertEquals("XpressNet MU+DH error: It is not possible to delete the locomotive",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHErrorCSStackFull(){
        XNetReply r = new XNetReply("E1 88 69");
        assertEquals("XpressNet MU+DH error: The Command Station Stack is Full",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringDHErrorOther(){
        XNetReply r = new XNetReply("E1 89 69");
        assertEquals("XpressNet MU+DH error: 9",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringLIVersionReply(){
        XNetReply r = new XNetReply("02 01 36 34");
        assertEquals("LI10x Hardware Version: 0.1 Software Version: 3.6",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringLIAddressReply(){
        XNetReply r = new XNetReply("F2 01 01 F2");
        assertEquals("RESPONSE LI101 Address 1",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringLIBaud1Reply(){
        XNetReply r = new XNetReply("F2 02 01 F1");
        assertEquals("RESPONSE LI101 Baud Rate: 19,200 bps (default)",
            r.toMonitorString(), "Monitor String");
    }
    @Test
    public void testToMonitorStringLIBaud2Reply(){
        XNetReply r = new XNetReply("F2 02 02 F2");
        assertEquals("RESPONSE LI101 Baud Rate: 38,400 bps",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringLIBaud3Reply(){
        XNetReply r = new XNetReply("F2 02 03 F1");
        assertEquals("RESPONSE LI101 Baud Rate: 57,600 bps",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringLIBaud4Reply(){
        XNetReply r = new XNetReply("F2 02 04 F4");
        assertEquals("RESPONSE LI101 Baud Rate: 115,200 bps",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringLIBaud5Reply(){
        XNetReply r = new XNetReply("F2 02 05 F1");
        assertEquals("RESPONSE LI101 Baud Rate: <undefined>",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringCSStatusReply(){
        XNetReply r = new XNetReply("62 22 00 40");
        String targetString =  "Command Station Status: Manual power-up Mode; ";
        assertEquals( targetString,r.toMonitorString(), "Monitor String");
        r = new XNetReply("62 22 FF BF");
        targetString =  "Command Station Status: Emergency Off; Emergency Stop; Service Mode; Powering up; Auto power-up Mode; RAM check error!";
        assertEquals( targetString,r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringCSVersionReply(){
        jmri.util.IntlUtilities.valueOf(3.6);
        XNetReply r = new XNetReply("63 21 36 00 55");
        assertEquals("Command Station Software Version: 3.6 Type: LZ100/LZV100",
                r.toMonitorString(), "Monitor String");
        r = new XNetReply("63 21 36 01 55");
        assertEquals( "Command Station Software Version: 3.6 Type: LH200",
                r.toMonitorString(), "Monitor String");
        r = new XNetReply("63 21 36 02 55");
        assertEquals( "Command Station Software Version: 3.6 Type: Compact or Other",
                r.toMonitorString(), "Monitor String");
        r = new XNetReply("63 21 36 10 55");
        assertEquals( "Command Station Software Version: 3.6 Type: multiMaus",
                r.toMonitorString(), "Monitor String");
        r = new XNetReply("63 21 36 20 55");
        assertEquals( "Command Station Software Version: 3.6 Type: 32",
                r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringCSV1VersionReply(){
        XNetReply r = new XNetReply("62 21 21 62");
        assertEquals("Command Station Software Version: 2.1 Type: Unknown (X-Bus V1 or V2)",
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringBCEmeregncyStop(){
        XNetReply r = new XNetReply("81 00 81");
        assertEquals("Broadcast: Emergency Stop (track power on)",r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringSearchResponseNormalLoco(){
        XNetReply r = new XNetReply("E3 30 C1 04 11");
        String targetString = "Locomotive Information Response: Search Response: Normal Locomotive: 260";
        assertEquals( targetString,r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringSearchResponseDoubleHeaderLoco(){
        XNetReply r = new XNetReply("E3 31 C1 04 17");
        String targetString = "Locomotive Information Response: Search Response: Loco in Double Header: 260";
        assertEquals(targetString,r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringSearchResponseMUBaseLoco(){
        XNetReply r = new XNetReply("E3 32 00 04 C5");
        String targetString = "Locomotive Information Response: Search Response: MU Base Address: 4";
        assertEquals( targetString,r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringSearchResponseMULoco(){
        XNetReply r = new XNetReply("E3 33 C1 04 15");
        String targetString = "Locomotive Information Response: Search Response: Loco in MU: 260";
        assertEquals(targetString,r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringSearchResponseFail(){
        XNetReply r = new XNetReply("E3 34 C1 04 15");
        String targetString = "Locomotive Information Response: Search Response: Search failed for: 260";
        assertEquals(targetString,r.toMonitorString(), "Monitor String");
    }

    // the following are invalid by the XpressNet Standard, but we want to
    // to make sure the code prints out the message contents.
    @Test
    public void testToMonitorStringInvalidLIMessage(){
        XNetReply r = new XNetReply("01 FF FE");
        assertEquals("01 FF FE",r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringInvalidLI101Message(){
        XNetReply r = new XNetReply("F2 FF FF F2");
        assertEquals("F2 FF FF F2",r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringInvalidCSInfoMessage(){
        XNetReply r = new XNetReply("61 FF 9E");
        assertEquals("61 FF 9E",r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringInvalidCSEStopMessage(){
        XNetReply r = new XNetReply("81 FF 7E");
        assertEquals("81 FF 7E",r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringInvalidServiceModeReply(){
        XNetReply r = new XNetReply("63 FF FF 00 63");
        assertEquals("63 FF FF 00 63",r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringInvalidCSStatusReply(){
        XNetReply r = new XNetReply("62 FF FF 62");
        assertEquals("62 FF FF 62",r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringInvalidLocoInfoReply(){
        XNetReply r = new XNetReply("E3 FF FF 00 E3");
        assertEquals("E3 FF FF 00 E3",r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringInvalidFeedbackReply(){
        XNetReply r = new XNetReply("42 FF FF 42");
        assertEquals("Feedback Response: 255 255",r.toMonitorString(), "Monitor String");
    }

    /**
     * Checks that the number of feedback items are correctly returned.
     */
    @Test
    public void testFeedbackItemsCount() {
        XNetReply r = new XNetReply("42 05 48 0f");
        assertEquals( 1, r.getFeedbackMessageItems(), "Feedback has single item");
        r = new XNetReply("46 05 48 06 48 07 48 0f");
        assertEquals( 3, r.getFeedbackMessageItems(), "Feedback has 3 items");
        r = new XNetReply("E3 32 00 04 C5");
        assertEquals(0, r.getFeedbackMessageItems());
    }

    /**
     * Checks that all information is consistent from the Feedback item, 
     * and are consistent with data served from XNetReply.
     * 
     * @param reply the original reply
     * @param startByte start byte where the Feedback item came from
     * @param address the expected address
     * @param aStatus the expected status
     * @param tType the expected feedback type
     * @param fItem the expected feedback address 
     */
    private void assertTurnoutFeedbackData(XNetReply reply, int startByte, int address, 
            int aStatus, int tType, FeedbackItem fItem) {

        // general accessory feedback constraints
        assertFalse( fItem.isEncoder(), "Must not be encoder");
        assertNull( fItem.getEncoderStatus(address), "Encoder functions disabled");
        assertTrue( fItem.isAccessory(), "Must be accessory");

        // info consistent with the reply's original accessors
        assertEquals( reply.isFeedbackMotionComplete(startByte), fItem.isMotionComplete(), "Motion same as reply");

        boolean odd = (address & 0x01) == 1;
        if (odd) {
            assertTrue( fItem.matchesAddress(reply.getTurnoutMsgAddr(startByte)), "Accepts reply's odd address");
            assertEquals(address, reply.getTurnoutMsgAddr(startByte));
        } else {
            assertTrue( fItem.matchesAddress(reply.getTurnoutMsgAddr(startByte) + 1), "Accepts reply's even address");
            assertEquals(address, reply.getTurnoutMsgAddr(startByte) + 1);
        }
        assertEquals( reply.isUnsolicited(), fItem.isUnsolicited(), "Solicited same as reply");
        
        assertEquals( tType, fItem.getType(), "Invalid feedback type");
        assertEquals( aStatus, fItem.getAccessoryStatus(), "Raw accessory status");


        int lowAddress  = odd ? address - 1 : address - 2;
        int pairAddress = odd ? address + 1 : address - 1;
        int highAddress = odd ? address + 2 : address + 1;

        assertTrue( fItem.matchesAddress(address), "Must accept own address");
        assertFalse( fItem.matchesAddress(pairAddress), "Must not accept other pair's address");
        assertFalse( fItem.matchesAddress(lowAddress), "Must not accept other addresses");
        assertFalse( fItem.matchesAddress(highAddress), "Must not accept other addresses");

        int tStatus;
        switch (aStatus) {
            case 0x00: tStatus = -1; break; // not operated; shouldn't be UNKNOWN ?
            case 0x01: tStatus = Turnout.CLOSED; break;
            case 0x02: tStatus = Turnout.THROWN; break;
            case 0x03: tStatus = -1; break; // invalid; shouldn't be INCONSISTENT ?
            default:
                throw new IllegalArgumentException();
        }
        assertEquals( tStatus, fItem.getTurnoutStatus(), "Turnout status");

        // check the paired item:
        FeedbackItem paired = fItem.pairedAccessoryItem();
        assertNotNull( paired, "Accessory fedbacks are always in pairs");
        assertFalse( paired.isEncoder(), "Must not be encoder");
        assertTrue( paired.isAccessory(), "Must be accessory");
        assertEquals( tType, paired.getType(), "Invalid feedback type");
        assertEquals( reply.isUnsolicited(), paired.isUnsolicited(), "Solicited same as reply");
        assertFalse( paired.matchesAddress(address), "Must not accept pair's address");
    }

    /**
     * Checks that information can be read from single-item feedback and
     * the reply are consistent.
     */
    @Test
    public void testSingleFeedbackTurnoutItem() {
        // 5 * 4, lower nibble = 21 (N/A) +22 (T)
        // movement NOT complete; turnout WITH feedback.
        XNetReply r = new XNetReply("42 05 28 0f");
        Optional<FeedbackItem> selected = r.selectTurnoutFeedback(20);
        assertFalse( selected.isPresent(), "Incorrect turnout number");
        selected = r.selectTurnoutFeedback(23);
        assertFalse( selected.isPresent(), "Incorrect turnout number");
        
        selected = r.selectTurnoutFeedback(21);
        assertTrue(selected.isPresent());
        
        FeedbackItem oddItem = selected.get();

        assertTrue( oddItem.isMotionComplete(), "Motion completed");
        assertTurnoutFeedbackData(r, 1, 21, 0, 1, oddItem);
        
        selected = r.selectTurnoutFeedback(22);
        assertTrue(selected.isPresent());
        
        FeedbackItem evenItem = selected.get();
        assertTurnoutFeedbackData(r, 1, 22, 2, 1, evenItem);
        
        assertFalse(r.isUnsolicited());
        
        // 5 * 4, upper nibble = 23 (C) + 24 (T)
        // movement IS complete; turnout WITHOUT feedback.
        r = new XNetReply("42 05 95 0f");
        selected = r.selectTurnoutFeedback(23);
        assertTrue(selected.isPresent());
        
        oddItem = selected.get();
        assertFalse( oddItem.isMotionComplete(), "Motion incomplete");
        assertTurnoutFeedbackData(r, 1, 23, 1, 0, oddItem);
        
        selected = r.selectTurnoutFeedback(24);
        assertTrue(selected.isPresent());
        evenItem = selected.get();
        assertTurnoutFeedbackData(r, 1, 24, 1, 0, evenItem);
    }
    
    /**
     * Checks that feedback module item gives invalid / erroneous / null
     * information when used as accessory.
     */
    @Test
    public void testOtherRepliesAsAccessoryFeedback() {
        XNetReply r = new XNetReply("42 05 58 0f");
        // test directly the item
        FeedbackItem item = new FeedbackItem(r, 45, 0x58);
        assertEquals(45, item.getAddress());
        for (int a = 45; a < 45 + 4; a++) {
            assertTrue(item.matchesAddress(a));
            // last bit is set, all others are false.
            assertEquals( a == 48, item.getEncoderStatus(a), "Bit state for sensor " + a);
        }
        // does not match accessory for 0x05, 0x58
        assertFalse(item.matchesAddress(21));
        assertEquals(3, item.getAccessoryStatus());
        assertNull(item.pairedAccessoryItem());

        // check that no turnout feedback can be selected
        for (int i = 1 ; i < 1024; i++) {
            assertFalse(r.selectTurnoutFeedback(i).isPresent());
        }
        
        // no accessory feedback present
        r = new XNetReply("E3 40 C1 04 61");
        for (int i = 1 ; i < 1024; i++) {
            assertFalse(r.selectTurnoutFeedback(i).isPresent());
        }
    }
    
    /**
     * Checks that encoder feedback will return null for turnout
     * feedbacks.
     */
    @Test
    public void testOtherRepliesAsEncoder() {
        XNetReply r = new XNetReply("42 05 28 0f");
        for (int i = 1 ; i < 1024; i++) {
            assertNull(r.selectModuleFeedback(i));
        }
        r = new XNetReply("E3 40 C1 04 61");
        for (int i = 1 ; i < 1024; i++) {
            assertNull(r.selectModuleFeedback(i));
        }

    }

    
    /**
     * Checks that select will not filter out accessories the
     * invalid state.
     */
    @Test
    public void testInvalidAccessoryStateFiltered() {
        XNetReply r = new XNetReply("42 05 2B 0f");
        Optional<FeedbackItem> opt = r.selectTurnoutFeedback(21);
        assertTrue(opt.isPresent());
        assertEquals(-1, r.getTurnoutStatus(1));
        
        opt = r.selectTurnoutFeedback(22);
        assertTrue(opt.isPresent());
        assertEquals(Turnout.THROWN, opt.get().getTurnoutStatus());
        assertEquals(r.getTurnoutStatus(0), opt.get().getTurnoutStatus());
    }
    
    @Test
    public void testSingleEncoderModuleFeedback() {
        // feedback 5 * 8  + 4 (upper nibble) (+1) = 45
        XNetReply r = new XNetReply("42 05 58 0f");
        
        assertNull(r.selectModuleFeedback(44));
        
        for (int i = 45; i < 45 + 4; i++) {
            Boolean b = r.selectModuleFeedback(i);
            assertNotNull(b);
            // the highest bit in the nibble is set
            assertEquals( i == 48, b, "sensor id " + i);
        }
        
        assertNull(r.selectModuleFeedback(49));
        
        r = new XNetReply("42 04 41 0f");

        assertNull(r.selectModuleFeedback(32));
        for (int i = 33; i < 33 + 4; i++) {
            Boolean b = r.selectModuleFeedback(i);
            assertNotNull(b);
            // the lowest bit in the nibble is set
            assertEquals( i == 33, b, "sensor id " + i);
        }
        assertNull(r.selectModuleFeedback(37));
    }
    
    @Test
    public void testMultipleFeedbackTurnoutItem() {
        // 1st pair: 21 (T) + 22 (C), with feedback
        // 2nd pair: encoder feedback, NOT 25+26!
        // 3rd pair: 31 (C) + 32 (N), without feedback, motion incomplete
        XNetReply r = new XNetReply("46 05 29 06 48 07 91 0f");
        
        Optional<FeedbackItem> selected;
        FeedbackItem odd;
        FeedbackItem even;
        selected = r.selectTurnoutFeedback(21);
        assertTrue(selected.isPresent());
        odd = selected.get();
        selected = r.selectTurnoutFeedback(22);
        assertTrue(selected.isPresent());
        even = selected.get();
        assertTrue(odd.isMotionComplete());
        assertTurnoutFeedbackData(r, 1, 21, 1, 1, odd);
        assertTurnoutFeedbackData(r, 1, 22, 2, 1, even);

        // check that 
        selected = r.selectTurnoutFeedback(25);
        assertFalse(selected.isPresent());
        selected = r.selectTurnoutFeedback(26);
        assertFalse(selected.isPresent());

        selected = r.selectTurnoutFeedback(31);
        assertTrue(selected.isPresent());
        odd = selected.get();
        selected = r.selectTurnoutFeedback(32);
        assertTrue(selected.isPresent());
        even = selected.get();
        
        assertFalse(odd.isMotionComplete());
        assertTurnoutFeedbackData(r, 5, 31, 1, 0, odd);
        assertTurnoutFeedbackData(r, 5, 32, 0, 0, even);
    }
    
    @Test
    public void testMultipleEncoderModuleFeedback() {
        // feedback 6 * 8  (lower nibble) (+1) = 49
        XNetReply r = new XNetReply("46 05 29 06 48 07 91 0f");
        
        // the 05-29 is a turnout feedback: must not be reported
        // as encoder 05 * 8
        assertNull(r.selectModuleFeedback(41));
        assertNull(r.selectModuleFeedback(44));
        assertNull(r.selectModuleFeedback(48));
        
        for (int i = 49; i < 49 + 4; i++) {
            Boolean b = r.selectModuleFeedback(i);
            assertNotNull(b);
            // the highest bit in the nibble is set
            assertEquals( i == (49 + 3), b, "sensor id " + i);
        }
        
        // 07-91 is again a turnout, must not be mistaken for upper nibble of
        // encoder 7
        assertNull(r.selectModuleFeedback(7 * 8 + 4 + 1));
    }

    /*
     * Tests for XNetOpsModeReply helpers
     */

    @Test
    public void testIsOpsModeResultMessage() {
        XNetReply r = new XNetReply("64 24 00 00 00 40");
        assertTrue(r.isOpsModeResultMessage());
    }

    @Test
    public void testGetOpsModeResultAddress() {
        XNetReply r = new XNetReply("64 24 00 00 00 40");
        assertEquals( 0, r.getOpsModeResultAddress(), "address");
    }

    @Test
    public void testGetOpsModeResultValue() {
        XNetReply r = new XNetReply("64 24 00 00 00 40");
        assertEquals( 0, r.getOpsModeResultValue(), "address");
    }

    // The minimal setup for log4J
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        msg = new XNetReply();
        m = msg;
    }

    @Override
    @AfterEach
    public void tearDown() {
        m = null;
        msg = null;
        JUnitUtil.tearDown();
    }

}
