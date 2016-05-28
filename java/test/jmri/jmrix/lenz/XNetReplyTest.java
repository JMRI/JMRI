package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetReply class
 *
 * @author	Bob Jacobsen
 */
public class XNetReplyTest extends TestCase {

    public void testCtor() {
        XNetReply m = new XNetReply();
        Assert.assertNotNull(m);
    }

    // Test the string constructor.
    public void testStringCtor() {
        XNetReply m = new XNetReply("12 34 AB 03 19 06 0B B1");
        Assert.assertEquals("length", 8, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x12, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x34, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0xAB, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x03, m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", 0x19, m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", 0x06, m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", 0x0B, m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 0xB1, m.getElement(7) & 0xFF);
    }

    // Test the string constructor with an empty string paramter.
    public void testStringCtorEmptyString() {
        XNetReply m = new XNetReply("");
        Assert.assertEquals("length", 0, m.getNumDataElements());
        Assert.assertTrue("empty reply",m.toString().equals(""));
    }

    // Test the copy constructor.
    public void testCopyCtor() {
        XNetReply x = new XNetReply("12 34 AB 03 19 06 0B B1");
        XNetReply m = new XNetReply(x);
        Assert.assertEquals("length", x.getNumDataElements(), m.getNumDataElements());
        Assert.assertEquals("0th byte", x.getElement(0), m.getElement(0));
        Assert.assertEquals("1st byte", x.getElement(1), m.getElement(1));
        Assert.assertEquals("2nd byte", x.getElement(2), m.getElement(2));
        Assert.assertEquals("3rd byte", x.getElement(3), m.getElement(3));
        Assert.assertEquals("4th byte", x.getElement(4), m.getElement(4));
        Assert.assertEquals("5th byte", x.getElement(5), m.getElement(5));
        Assert.assertEquals("6th byte", x.getElement(6), m.getElement(6));
        Assert.assertEquals("7th byte", x.getElement(7), m.getElement(7));
    }

    // Test the XNetMessage constructor.
    public void testXNetMessageCtor() {
        XNetMessage x = new XNetMessage("12 34 AB 03 19 06 0B B1");
        XNetReply m = new XNetReply(x);
        Assert.assertEquals("length", x.getNumDataElements(), m.getNumDataElements());
        Assert.assertEquals("0th byte", x.getElement(0)& 0xFF, m.getElement(0)& 0xFF);
        Assert.assertEquals("1st byte", x.getElement(1)& 0xFF, m.getElement(1)& 0xFF);
        Assert.assertEquals("2nd byte", x.getElement(2)& 0xFF, m.getElement(2)& 0xFF);
        Assert.assertEquals("3rd byte", x.getElement(3)& 0xFF, m.getElement(3)& 0xFF);
        Assert.assertEquals("4th byte", x.getElement(4)& 0xFF, m.getElement(4)& 0xFF);
        Assert.assertEquals("5th byte", x.getElement(5)& 0xFF, m.getElement(5)& 0xFF);
        Assert.assertEquals("6th byte", x.getElement(6)& 0xFF, m.getElement(6)& 0xFF);
        Assert.assertEquals("7th byte", x.getElement(7)& 0xFF, m.getElement(7)& 0xFF);
    }

    // check parity operations
    public void testParity() {
        XNetReply m;
        m = new XNetReply("21 21 00");
        Assert.assertEquals("parity set test 1", 0, m.getElement(2));
        Assert.assertEquals("parity check test 1", true, m.checkParity());

        m = new XNetReply("21 21 00");
        m.setElement(0, 0x21);
        m.setElement(1, ~0x21);
        m.setParity();
        Assert.assertEquals("parity set test 2", 0xFF, m.getElement(2));
        Assert.assertEquals("parity check test 2", true, m.checkParity());

        m = new XNetReply("21 21 00");
        m.setElement(0, 0x18);
        m.setElement(1, 0x36);
        m.setParity();
        Assert.assertEquals("parity set test 3", 0x2E, m.getElement(2));
        Assert.assertEquals("parity check test 3", true, m.checkParity());

        m = new XNetReply("21 21 00");
        m.setElement(0, 0x87);
        m.setElement(1, 0x31);
        m.setParity();
        Assert.assertEquals("parity set test 4", 0xB6, m.getElement(2));
        Assert.assertEquals("parity check test 4", true, m.checkParity());

        m = new XNetReply("21 21 00");
        m.setElement(0, 0x18);
        m.setElement(1, 0x36);
        m.setElement(2, 0x0e);
        Assert.assertEquals("parity check test 5", false, m.checkParity());

        m = new XNetReply("21 21 00");
        m.setElement(0, 0x18);
        m.setElement(1, 0x36);
        m.setElement(2, 0x8e);
        Assert.assertEquals("parity check test 6", false, m.checkParity());
    }

// test accessor methods for elements.
    // check getOpCodeHex
    public void testGetOpCodeHex(){
       XNetReply m=new XNetReply("63 14 01 04 72");
       Assert.assertEquals("getOpCodeHex Return Value","0x63",m.getOpCodeHex());
    }


    // check getElementBCD
    public void testGetElementBCD(){
       XNetReply m=new XNetReply("63 14 01 04 72");
       Assert.assertEquals("getElementBCD Return Value",(long)14,(long)m.getElementBCD(1));
    }

    // check skipPrefix
    public void testSkipPrefix(){
       XNetReply m=new XNetReply("63 14 01 04 72");
       // skip prefix currently always returns -1, there is no prefix.
       Assert.assertEquals("skipPrefix return value",-1,(long)m.skipPrefix(0));
    }


// get information from specific types of messages.

    // check is service mode response
    public void testIsServiceModeResponse() {
        // CV 1 in direct mode.
        XNetReply r = new XNetReply("63 14 01 04 72");
        Assert.assertTrue(r.isServiceModeResponse());
        // CV 257 in direct mode.
        r = new XNetReply("63 15 01 04 72");
        Assert.assertTrue(r.isServiceModeResponse());
        // CV 513 in direct mode.
        r = new XNetReply("63 16 01 04 72");
        Assert.assertTrue(r.isServiceModeResponse());
        // CV 769 in direct mode.
        r = new XNetReply("63 16 01 04 72");
        Assert.assertTrue(r.isServiceModeResponse());
        // CV 1 in paged mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertTrue(r.isServiceModeResponse());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertTrue(r.isServiceModeResponse());
        // CV 286 in direct mode.
        r = new XNetReply("63 15 1E 14 7C");
        Assert.assertTrue(r.isServiceModeResponse());
        // CV 1 in paged mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertTrue(r.isServiceModeResponse());
        // not a service mode response.
        r = new XNetReply("01 04 05");
        Assert.assertFalse(r.isServiceModeResponse());
    }

    // check is paged mode response
    public void testIsPagedModeResponse() {
        // CV 1 in direct mode.
        XNetReply r = new XNetReply("63 14 01 04 72");
        Assert.assertFalse(r.isPagedModeResponse());
        // CV 1 in paged mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertTrue(r.isPagedModeResponse());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertTrue(r.isPagedModeResponse());
        // CV 286 in direct mode.
        r = new XNetReply("63 15 1E 14 7C");
        Assert.assertFalse(r.isPagedModeResponse());
    }

    // check is direct mode response
    public void testIsDirectModeResponse() {
        // CV 1 in direct mode.
        XNetReply r = new XNetReply("63 14 01 04 72");
        Assert.assertTrue(r.isDirectModeResponse());
        // CV 257 in direct mode.
        r = new XNetReply("63 15 01 04 72");
        Assert.assertTrue(r.isDirectModeResponse());
        // CV 513 in direct mode.
        r = new XNetReply("63 16 01 04 72");
        Assert.assertTrue(r.isDirectModeResponse());
        // CV 769 in direct mode.
        r = new XNetReply("63 16 01 04 72");
        Assert.assertTrue(r.isDirectModeResponse());
        // CV 1 in paged mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isDirectModeResponse());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isDirectModeResponse());
        // CV 286 in direct mode.
        r = new XNetReply("63 15 1E 14 7C");
        Assert.assertTrue(r.isDirectModeResponse());
    }

    // check get service mode CV Number response code.
    public void testGetServiceModeCVNumber() {
        // CV 1 in direct mode.
        XNetReply r = new XNetReply("63 14 01 04 72");
        Assert.assertEquals("Direct Mode CV<256", 1, r.getServiceModeCVNumber());
        // CV 1 in paged mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertEquals("Paged Mode CV<256", 1, r.getServiceModeCVNumber());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertEquals("Register Mode CV<256", 1, r.getServiceModeCVNumber());
        Assert.assertTrue(r.isServiceModeResponse());
        // CV 286 in direct mode.
        r = new XNetReply("63 15 1E 14 7C");
        Assert.assertEquals("Direct Mode CV>256", 286, r.getServiceModeCVNumber());
        // not a service mode response.
        r = new XNetReply("01 04 05");
        Assert.assertEquals("non-ServiceMode message", -1, r.getServiceModeCVNumber());
    }

    // check get service mode CV Value response code.
    public void testGetServiceModeCVValue() {
        // CV 1 in direct mode.
        XNetReply r = new XNetReply("63 14 01 04 72");
        Assert.assertEquals("Direct Mode CV<256", 4, r.getServiceModeCVValue());
        // CV 1 in paged mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertEquals("Paged Mode CV<256", 4, r.getServiceModeCVValue());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertEquals("Register Mode CV<256", 4, r.getServiceModeCVValue());
        Assert.assertTrue(r.isServiceModeResponse());
        // CV 286 in direct mode.
        r = new XNetReply("63 15 1E 14 7C");
        Assert.assertEquals("Direct Mode CV>256", 20, r.getServiceModeCVValue());
        // not a service mode response.
        r = new XNetReply("01 04 05");
        Assert.assertEquals("Non Service Mode Response", -1, r.getServiceModeCVValue());
    }

    // From feedback Messages
    // check is this a feedback response
    public void testIsFeedbackResponse() {
        // feedback message for turnout
        XNetReply r = new XNetReply("42 05 48 0f");
        Assert.assertTrue(r.isFeedbackMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isFeedbackMessage());
    }

    // check is this a broadcast feedback response
    public void testIsFeedbackBroadcastResponse() {
        // feedback message for turnout
        XNetReply r = new XNetReply("42 05 48 0f");
        Assert.assertTrue(r.isFeedbackBroadcastMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isFeedbackBroadcastMessage());
    }

    // getting the address from a feedback response
    public void testGetTurnoutMsgAddr() {
        // feedback message for turnout 21
        XNetReply r = new XNetReply("42 05 04 43");
        Assert.assertEquals("Turnout Message Address", 21, r.getTurnoutMsgAddr());
        // feedback message for turnout 23
        r = new XNetReply("42 05 14 53");
        Assert.assertEquals("Turnout Message Address", 23, r.getTurnoutMsgAddr());
        // feedback message for a feedback encoder, should return -1.
        r = new XNetReply("42 05 48 0F");
        Assert.assertEquals("Turnout Message Address for Feedback encoder", -1, r.getTurnoutMsgAddr() );

        // feedback message for a non-feedback message, should return -1.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertEquals("Turnout Message Address for Feedback Other message", -1, r.getTurnoutMsgAddr() );

    }

    // getting the address from a broadcast feedback response
    public void testGetBroadcastTurnoutMsgAddr() {
        // feedback message for turnout 21
        XNetReply r = new XNetReply("42 05 04 43");
        Assert.assertEquals("Broadcast Turnout Message Address", 21, r.getTurnoutMsgAddr(1));

        // feedback message for turnout 23
        r = new XNetReply("42 05 14 53");
        Assert.assertEquals("Broadcast Turnout Message Address", 23, r.getTurnoutMsgAddr(1));
        // feedback message for a feedback encoder, should return -1.
        r = new XNetReply("42 05 48 0F");
        Assert.assertEquals("Broadcast Turnout Message Address for Feedback encoder", -1, r.getTurnoutMsgAddr(1) );

        // feedback message for a non-feedback message, should return -1.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertEquals("Broadcast Turnout Message Address for Feedback Other message", -1, r.getTurnoutMsgAddr() );
    }

    // getting the feedback message type (turnout without feedback, 
    // turnout with feedback, or sensor)
    public void testGetFeedbackMessageType() {
        // feedback message for turnout
        XNetReply r = new XNetReply("42 05 04 43");
        Assert.assertEquals("Feedback Message Type", 0, r.getFeedbackMessageType() );
        r = new XNetReply("42 05 24 63");
        Assert.assertEquals("Feedback Message Type", 1, r.getFeedbackMessageType() );
        r = new XNetReply("42 05 48 0F");
        Assert.assertEquals("Feedback Message Type", 2, r.getFeedbackMessageType() );
        r = new XNetReply("63 10 01 04 76"); // not a feedback message.
        Assert.assertEquals("Feedback Message Type", -1, r.getFeedbackMessageType() );
    }

    // getting the feedback message type (turnout without feedback, 
    // turnout with feedback, or sensor) from a broadcast feedback message.
    public void testGetBroadcastFeedbackMessageType() {
        // feedback message for turnout
        XNetReply r = new XNetReply("42 05 04 43");
        Assert.assertEquals("Broadcast Feedback Message Type", 0, r.getFeedbackMessageType(1) );
        r = new XNetReply("42 05 24 63");
        Assert.assertEquals("Broadcast Feedback Message Type", 1, r.getFeedbackMessageType(1) );
        r = new XNetReply("42 05 48 0F");
        Assert.assertEquals("Broadcast Feedback Message Type", 2, r.getFeedbackMessageType(1) );
        r = new XNetReply("63 10 01 04 76"); // not a feedback message.
        Assert.assertEquals("Broadcast Feedback Message Type", -1, r.getFeedbackMessageType(1) );
    }

    // getting the status from a turnout feedback response
    public void testGetTurnoutMmessageStatus() {
        // feedback message for turnout 22, closed
        XNetReply r = new XNetReply("42 05 04 43");
        Assert.assertEquals("Turnout Status", jmri.Turnout.CLOSED, r.getTurnoutStatus(0));
        // feedback message for turnout 22, thrown
        r = new XNetReply("42 05 08 4F");
        Assert.assertEquals("Turnout Status", jmri.Turnout.THROWN, r.getTurnoutStatus(0));

	// ask for address 21
	Assert.assertEquals("Turnout Status", -1 , r.getTurnoutStatus(1));
        // feedback message for turnout 22, with invalid state.
        r = new XNetReply("42 05 0C 45");
        Assert.assertEquals("Turnout Status", -1 , r.getTurnoutStatus(0));

        // feedback message for turnout 21, closed
        r = new XNetReply("42 05 01 46");
        Assert.assertEquals("Turnout Status", jmri.Turnout.CLOSED, r.getTurnoutStatus(1));
        // feedback message for turnout 21, thrown
        r = new XNetReply("42 05 02 45");
        Assert.assertEquals("Turnout Status", jmri.Turnout.THROWN, r.getTurnoutStatus(1));
	// ask for address 22.
	Assert.assertEquals("Turnout Status", -1 , r.getTurnoutStatus(0));
	// send invalid value for parameter (only 0 and 1 are valid).
	Assert.assertEquals("Turnout Status", -1 , r.getTurnoutStatus(3));
        // feedback message for turnout 21, with invalid state.
        r = new XNetReply("42 05 03 47");
        Assert.assertEquals("Turnout Status", -1 , r.getTurnoutStatus(1));
    }

    // getting the status from a turnout broadcast feedback response
    public void testGetBroadcastTurnoutMessageStatus() {
        // feedback message for turnout 22, closed
        XNetReply r = new XNetReply("42 05 04 43");
        Assert.assertEquals("Broadcast Turnout Status", jmri.Turnout.CLOSED, r.getTurnoutStatus(1,0));
        // feedback message for turnout 22, thrown
        r = new XNetReply("42 05 08 4F");
        Assert.assertEquals("Broadcast Turnout Status", jmri.Turnout.THROWN, r.getTurnoutStatus(1,0));

	// ask for address 21
	Assert.assertEquals("Broadcast Turnout Status", -1 , r.getTurnoutStatus(1,1));
        // feedback message for turnout 22, with invalid state.
        r = new XNetReply("42 05 0C 45");
        Assert.assertEquals("Broadcast Turnout Status", -1 , r.getTurnoutStatus(1,0));
        // feedback message for turnout 21, closed
        r = new XNetReply("42 05 01 46");
        Assert.assertEquals("Broadcast Turnout Status", jmri.Turnout.CLOSED, r.getTurnoutStatus(1,1));
        // feedback message for turnout 21, thrown
        r = new XNetReply("42 05 02 45");
        Assert.assertEquals("Broadcast Turnout Status", jmri.Turnout.THROWN, r.getTurnoutStatus(1,1));
	// ask for address 22.
	Assert.assertEquals("Broadcast Turnout Status", -1 , r.getTurnoutStatus(1,0));
	// send invalid value for parameter (only 0 and 1 are valid).
	Assert.assertEquals("Broadcast Turnout Status", -1 , r.getTurnoutStatus(1,3));
        // feedback message for turnout 21, with invalid state.
        r = new XNetReply("42 05 03 47");
        Assert.assertEquals("Broadcast Turnout Status", -1 , r.getTurnoutStatus(1,1));
    }

    // getting the address from a feedback encoder response
    public void testGetEncoderMsgAddr() {
        // feedback message for sensor
        XNetReply r = new XNetReply("42 05 48 0f");
        Assert.assertEquals("Feedback Encoder Message Address", 5, r.getFeedbackEncoderMsgAddr());
        // turnout
        r = new XNetReply("42 05 08 4F");
        Assert.assertEquals("Feedback Encoder Message Address", -1, r.getFeedbackEncoderMsgAddr());
        r = new XNetReply("63 10 01 04 76"); // not a feedback message.
        Assert.assertEquals("Feedback Encoder Message Address", -1, r.getFeedbackEncoderMsgAddr());
    }

    // getting the address from a broadcast feedback encoder response
    public void testGetBroadcastEncoderMsgAddr() {
        // feedback message for turnout
        XNetReply r = new XNetReply("42 05 48 0f");
        Assert.assertEquals("Broadcast Feedback Encoder Message Address", 5, r.getFeedbackEncoderMsgAddr(1));
        // turnout
        r = new XNetReply("42 05 08 4F");
        Assert.assertEquals("Feedback Encoder Message Address", -1, r.getFeedbackEncoderMsgAddr(1));
        r = new XNetReply("63 10 01 04 76"); // not a feedback message.
        Assert.assertEquals("Feedback Encoder Message Address", -1, r.getFeedbackEncoderMsgAddr(1));
    }

    // throttle related replies.
    public void testGetThrottleMsgAddr() {
        // locomotive taken over by another device reply
        // short address
        XNetReply r = new XNetReply("E3 40 00 04 57");
        Assert.assertEquals("Throttle Message Address", 4, r.getThrottleMsgAddr());
        // long address
        r = new XNetReply("E3 40 C1 04 61");
        Assert.assertEquals("Throttle Message Address", 260, r.getThrottleMsgAddr());
        // not a throttle message.
        r = new XNetReply("42 05 48 0f");
        Assert.assertEquals("Throttle Message Address", -1, r.getThrottleMsgAddr());
    }

    // check is this a throttle response
    public void testIsThrottleMessage() {
        // MUED Locomotive Address
        XNetReply r= new XNetReply("E2 24 04 C2");
        Assert.assertTrue(r.isThrottleMessage());
        // Locomotive Taken Over by another device
        r = new XNetReply("E3 40 C1 04 61");
        Assert.assertTrue(r.isThrottleMessage());
        // Normal Locomotive Information reply
        r= new XNetReply("E4 04 00 04 00 E4");
        Assert.assertTrue(r.isThrottleMessage());
        // MUED Locomotive Information reply
        r = new XNetReply("E5 14 C1 04 00 00 34");
        Assert.assertTrue(r.isThrottleMessage());
        // DH Address
        r = new XNetReply("E6 64 00 64 C1 C1 04 E2");
        Assert.assertTrue(r.isThrottleMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isThrottleMessage());
    }
 
   // check is this a throttle takeover response
    public void testIsThrottleTakenOverMessage() {
        // Normal Locomotive Information reply
        XNetReply r = new XNetReply("E3 40 C1 04 61");
        Assert.assertTrue(r.isThrottleTakenOverMessage());
        // Function reply 
        r = new XNetReply("E3 08 00 00 E6");
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isThrottleTakenOverMessage());
    }

   // check is this a consist response
    public void testIsConsistMessage() {
        // MU/DH  Error
        XNetReply r = new XNetReply("E1 81 60");
        Assert.assertTrue(r.isConsistMessage());
        // DH Info (XNetV1)
        r = new XNetReply("C5 04 00 00 00 00 C1");
        Assert.assertTrue(r.isConsistMessage());
        // DH Info (XNetV2)
        r = new XNetReply("C6 04 00 00 00 00 00 C2");
        Assert.assertTrue(r.isConsistMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isConsistMessage());
    }


   // some common messages.
   // check is this an OK message
    public void testIsOkMessage() {
        // "OK" message
        XNetReply r = new XNetReply("01 04 05");
        Assert.assertTrue(r.isOkMessage());
        // Error message
        r = new XNetReply("01 01 00");
        Assert.assertFalse(r.isOkMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isOkMessage());
    }

   // check is this an Timeslot Restored message
    public void testIsTimeSlotRestoredMessage() {
        // Timeslot restored message
        XNetReply r = new XNetReply("01 07 06");
        Assert.assertTrue(r.isTimeSlotRestored());
        // Error message
        r = new XNetReply("01 01 00");
        Assert.assertFalse(r.isTimeSlotRestored());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isTimeSlotRestored());
    }
   
    // check is this a CS Busy message
    public void testIsCSBusyMessage() {
        // CS Busy Message
        XNetReply r = new XNetReply("61 81 e0");
        Assert.assertTrue(r.isCSBusyMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isCSBusyMessage());
    }
   
    // check is this a CS transfer error message
    public void testIsCSTransferError() {
        // Command Station Transfer Error Message
        XNetReply r = new XNetReply("61 80 e1");
        Assert.assertTrue(r.isCSTransferError());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isCSTransferError());
    }


   // check is this a Communication Error  message
    public void testIsCommErrorMessage() {
        // Error between interface and the PC
        XNetReply r = new XNetReply("01 01 00");
        Assert.assertTrue(r.isCommErrorMessage());
        // Error between interface and the Command Station
        r = new XNetReply("01 02 03");
        Assert.assertTrue(r.isCommErrorMessage());
        // Unkonwn Communication Error
        r = new XNetReply("01 03 02");
        Assert.assertTrue(r.isCommErrorMessage());
        // LI10x Buffer Overflow
        r = new XNetReply("01 06 07");
        Assert.assertTrue(r.isCommErrorMessage());
        // LIUSB request resend of data.
        r = new XNetReply("01 0A 0B");
        Assert.assertTrue(r.isCommErrorMessage());
        // Timeslot Error
        r = new XNetReply("01 05 04");
        Assert.assertTrue(r.isCommErrorMessage());
        // Timeslot Restored
        r = new XNetReply("01 07 06");
        Assert.assertTrue(r.isCommErrorMessage());
        // Data sent while there is no Timeslot
        r = new XNetReply("01 08 09");
        Assert.assertTrue(r.isCommErrorMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isCommErrorMessage());
    }
   
    // check is this a Timeslot message  message
    public void testIsTimeSlotErrorMessage() {
        // Timeslot Error
        XNetReply r = new XNetReply("01 05 04");
        Assert.assertTrue(r.isTimeSlotErrorMessage());
        // Timeslot Restored
        r = new XNetReply("01 07 06");
        Assert.assertTrue(r.isTimeSlotErrorMessage());
        // Data sent while there is no Timeslot
        r = new XNetReply("01 08 09");
        Assert.assertTrue(r.isTimeSlotErrorMessage());
        // CV 1 in register mode.
        r = new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isTimeSlotErrorMessage());
    }

    // check if this message is a retransmittable error message.
    public void testIsRetransmittableErrorMsg(){
       XNetReply r = new XNetReply("61 81 e3"); // CS Busy Message
       Assert.assertTrue(r.isRetransmittableErrorMsg());
       r = new XNetReply("61 80 e1"); // transfer error
       Assert.assertTrue(r.isRetransmittableErrorMsg());
       r = new XNetReply("01 06 07"); // Buffer overflow (Comm Error)
       Assert.assertTrue(r.isRetransmittableErrorMsg());
       r = new XNetReply("01 04 05"); // OK message
       Assert.assertFalse(r.isRetransmittableErrorMsg());
    }

    // check if this is an unsolicited message
    public void testIsUnsolicitedMessage() {
        // CV 1 in register mode.
        XNetReply r= new XNetReply("63 10 01 04 76");
        Assert.assertFalse(r.isUnsolicited());
        r.setUnsolicited();
        Assert.assertTrue(r.isUnsolicited());
        // Throttle taken over message
        r = new XNetReply("E3 40 C1 04 61");
        Assert.assertTrue(r.isUnsolicited());
        // feedback message.
        r = new XNetReply("42 05 48 0f");
        Assert.assertTrue(r.isUnsolicited());
        r.resetUnsolicited();
        Assert.assertFalse(r.isUnsolicited()); 
    }


    // from here down is testing infrastructure
    public XNetReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetReplyTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetReplyTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
