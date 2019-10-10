package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCCppMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppMessage class
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood
 */
public class DCCppMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private DCCppMessage msg = null;

    // check opcode inclusion in message
    @Test
    public void testOpCode() {
        msg = new DCCppMessage(5);
	Assert.assertNotNull(msg);
        //msg.setOpCode('i');
        //Assert.assertEquals("read=back op code", 'i', msg.getOpCode());
        //Assert.assertEquals("stored op code", 'i', msg.getElement(0));
    }

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        msg = DCCppMessage.parseDCCppMessage("T 42 1");
        Assert.assertEquals("length", 6, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'T', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '4', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '2', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', msg.getElement(5) & 0xFF);
    }

    @Test
    public void testMakeAccessoryDecoderMsgAddr1ActivateTrue() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(1, true);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 7, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", ' ', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", '0', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", ' ', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '1', msg.getElement(6) & 0xFF);
    }

    @Test
    public void testMakeAccessoryDecoderMsgAddr1ActivateFalse() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(1, false);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 7, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", ' ', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", '0', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", ' ', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '0', msg.getElement(6) & 0xFF);
    }

    @Test
    public void testMakeAccessoryDecoderMsgAddr4ActivateTrue() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(4, true);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 7, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", ' ', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", '3', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", ' ', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '1', msg.getElement(6) & 0xFF);
    }

    @Test
    public void testMakeAccessoryDecoderMsgAddr4ActivateFalse() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(4, false);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 7, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", ' ', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", '3', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", ' ', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '0', msg.getElement(6) & 0xFF);
    }

    @Test
    public void testMakeAccessoryDecoderMsgAddr5ActivateTrue() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(5, true);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 7, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", ' ', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", '0', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", ' ', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '1', msg.getElement(6) & 0xFF);
    }

    @Test
    public void testMakeAccessoryDecoderMsgAddr5ActivateFalse() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(5, false);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 7, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", ' ', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", '0', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", ' ', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '0', msg.getElement(6) & 0xFF);
    }

    @Test
    public void testMakeAccessoryDecoderMsgAddr40ActivateTrue() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(40, true);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 8, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '0', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '3', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '1', msg.getElement(7) & 0xFF);
    }

    @Test
    public void testMakeAccessoryDecoderMsgAddr40ActivateFalse() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(40, false);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 8, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '0', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '3', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '0', msg.getElement(7) & 0xFF);
    }

     @Test
    public void testMakeAccessoryDecoderMsgAddr41ActivateTrue() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(41, true);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 8, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '1', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '0', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '1', msg.getElement(7) & 0xFF);
    }

    @Test
    public void testMakeAccessoryDecoderMsgAddr41ActivateFalse() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(41, false);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 8, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '1', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '0', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '0', msg.getElement(7) & 0xFF);
    }

     @Test
    public void testMakeAccessoryDecoderMsgAddr2040ActivateTrue() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(2040, true);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 9, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '1', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", '0', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", ' ', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '3', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", ' ', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '1', msg.getElement(8) & 0xFF);
    }

    @Test
    public void testMakeAccessoryDecoderMsgAddr2040ActivateFalse() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(2040, false);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 9, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '1', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", '0', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", ' ', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '3', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", ' ', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '0', msg.getElement(8) & 0xFF);
    }

     @Test
    public void testMakeAccessoryDecoderMsgAddr2041ActivateTrue() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(2041, true);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 9, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '1', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", '1', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", ' ', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '0', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", ' ', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '1', msg.getElement(8) & 0xFF);
    }

    @Test
    public void testMakeAccessoryDecoderMsgAddr2041ActivateFalse() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(2041, false);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 9, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '1', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", '1', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", ' ', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '0', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", ' ', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '0', msg.getElement(8) & 0xFF);
    }

     @Test
    public void testMakeAccessoryDecoderMsgAddr2044ActivateTrue() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(2044, true);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 9, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '1', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", '1', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", ' ', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '3', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", ' ', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '1', msg.getElement(8) & 0xFF);
    }

    @Test
    public void testMakeAccessoryDecoderMsgAddr2044ActivateFalse() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(2044, false);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 9, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '1', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", '1', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", ' ', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '3', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", ' ', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '0', msg.getElement(8) & 0xFF);
    }

    // Test the canned messages.
    @Test
    public void testGetAccessoryDecoderMsgActivateTrue() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(23, 2, true);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 8, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '2', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '1', msg.getElement(7) & 0xFF);
    }

    @Test
    public void testGetAccessoryDecoderMsgActivateFalse() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(23, 2, false);
	log.debug("accessory decoder message = {}", msg.toString());
        Assert.assertEquals("length", 8, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '2', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '0', msg.getElement(7) & 0xFF);
    }

    @Test
    public void testMonitorStringAccessoryDecoderMsgActivateTrue() {
	msg = DCCppMessage.makeAccessoryDecoderMsg(23, 2, true);
        Assert.assertEquals("Monitor string","Accessory Decoder Cmd: \n\tAddress: 23\n\tSubaddr: 2\n\tState: ON",msg.toMonitorString());
    }

    @Test
    public void testMonitorStringAccessoryDecoderMsgActivateFalse() {
	    msg = DCCppMessage.makeAccessoryDecoderMsg(23, 2, false);
        Assert.assertEquals("Monitor string","Accessory Decoder Cmd: \n\tAddress: 23\n\tSubaddr: 2\n\tState: OFF",msg.toMonitorString());
    }

    @Test
    public void testGetTurnoutCommandMsgThrown() {
	msg = DCCppMessage.makeTurnoutCommandMsg(23, true);
	log.debug("turnout message = {}", msg.toString());
        Assert.assertEquals("length", 6, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'T', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', msg.getElement(5) & 0xFF);
    }

    @Test
    public void testGetTurnoutCommandMsgClosed() {
	msg = DCCppMessage.makeTurnoutCommandMsg(23, false);
	log.debug("turnout message = {}", msg.toString());
        Assert.assertEquals("length", 6, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'T', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '0', msg.getElement(5) & 0xFF);
    }

    @Test
    public void testMonitorStringTurnoutCommandMsgThrown() {
	msg = DCCppMessage.makeTurnoutCommandMsg(23, true);
        Assert.assertEquals("Monitor string","Turnout Cmd: \n\tT/O ID: 23\n\tState: THROWN",msg.toMonitorString());
    }
    @Test
    public void testMonitorStringTurnoutCommandMsgClosed() {
	msg = DCCppMessage.makeTurnoutCommandMsg(23, false);
        Assert.assertEquals("Monitor string","Turnout Cmd: \n\tT/O ID: 23\n\tState: CLOSED",msg.toMonitorString());
    }

    @Test
    public void testGetWriteDirectCVMsg() {
	msg = DCCppMessage.makeWriteDirectCVMsg(29, 12, 1, 2);
	log.debug("write cv message = {}", msg.toString());
        Assert.assertEquals("length", 11, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'W', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '9', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '2', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", ' ', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '1', msg.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', msg.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '2', msg.getElement(10) & 0xFF);
    }

    @Test
    public void testMonitorStringWriteDirectCVMsg() {
	msg = DCCppMessage.makeWriteDirectCVMsg(29, 12, 1, 2);
        Assert.assertEquals("Monitor string","Prog Write Byte Cmd: \n\tCV : 29\n\tValue: 12\n\tCallback Num: 1\n\tCallback Sub: 2",msg.toMonitorString());
    }

    @Test
    public void testGetBitWriteDirectCVMsg() {
	msg = DCCppMessage.makeBitWriteDirectCVMsg(17, 4, 1, 3, 4);
	log.debug("write cv bit message = {}", msg.toString());
        Assert.assertEquals("length", 12, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'B', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '7', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '4', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '1', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", ' ', msg.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", '3', msg.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", ' ', msg.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", '4', msg.getElement(11) & 0xFF);
    }

    @Test
    public void testMonitorStringBitWriteDirectCVMsg() {
	msg = DCCppMessage.makeBitWriteDirectCVMsg(17, 4, 1, 3, 4);
        Assert.assertEquals("Monitor string","Prog Write Bit Cmd: \n\tCV : 17\n\tBit : 4\n\tValue: 1\n\tCallback Num: 3\n\tCallback Sub: 4",msg.toMonitorString());
    }

    @Test
    public void testGetReadDirectCVMsg() {
        msg = DCCppMessage.makeReadDirectCVMsg(17, 4, 3);
	log.debug("read cv message = {}", msg.toString());
        Assert.assertEquals("length", 8, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'R', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '7', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '4', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '3', msg.getElement(7) & 0xFF);
    }

    @Test
    public void testMonitorStringReadDirectCVMsg() {
	msg = DCCppMessage.makeReadDirectCVMsg(17, 4, 3);
        Assert.assertEquals("Monitor string","Prog Read Cmd: \n\tCV: 17\n\tCallback Num: 4\n\tCallback Sub: 3",msg.toMonitorString());
    }

    @Test
    public void testGetWriteOpsModeCVMsg() {
	msg = DCCppMessage.makeWriteOpsModeCVMsg(17, 4, 3);
	log.debug("write ops cv message = {}", msg.toString());
        Assert.assertEquals("length", 8, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'w', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '7', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '4', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '3', msg.getElement(7) & 0xFF);
    }

    @Test
    public void testMonitorStringWriteOpsModeCVMsg() {
	msg = DCCppMessage.makeWriteOpsModeCVMsg(17, 4, 3);
        Assert.assertEquals("Monitor string","Ops Write Byte Cmd: \n\tAddress: 17\n\tCV: 4\n\tValue: 3",msg.toMonitorString());
    }

    @Test
    public void testGetBitWriteOpsModeCVMsg() {
	msg = DCCppMessage.makeBitWriteOpsModeCVMsg(17, 4, 3, 1);
	log.debug("write ops bit cv message = {}", msg.toString());
        Assert.assertEquals("length", 10, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'b', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '7', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '4', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '3', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", ' ', msg.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", '1', msg.getElement(9) & 0xFF);
    }

    @Test
    public void testMonitorStringBitWriteOpsModeCVMsg() {
	msg = DCCppMessage.makeBitWriteOpsModeCVMsg(17, 4, 3, 1);
        Assert.assertEquals("Monitor string","Ops Write Bit Cmd: \n\tAddress: 17\n\tCV: 4\n\tBit: 3\n\tValue: 1",msg.toMonitorString());
    }

    @Test
    public void testSetTrackPowerMsg() {
	msg = DCCppMessage.makeSetTrackPowerMsg(true);
	log.debug("track power on message = {}", msg.toString());
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("0th byte", '1', msg.getElement(0) & 0xFF);

	msg = DCCppMessage.makeSetTrackPowerMsg(false);
	log.debug("track power off message = {}", msg.toString());
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("0th byte", '0', msg.getElement(0) & 0xFF);
    }

    @Test
    public void testMonitorStringSetTrackPowerMsg() {
	msg = DCCppMessage.makeSetTrackPowerMsg(true);
        Assert.assertEquals("Monitor string","Track Power ON Cmd ",msg.toMonitorString());
    }

    @Test
    public void testReadTrackCurrentMsg() {
	msg = DCCppMessage.makeReadTrackCurrentMsg();
	log.debug("read track current message = {}", msg.toString());
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'c', msg.getElement(0) & 0xFF);
    }

    @Test
    public void testMonitorStringReadTrackCurrentMsg() {
	msg = DCCppMessage.makeReadTrackCurrentMsg();
        Assert.assertEquals("Monitor string","Read Track Current Cmd ",msg.toMonitorString());
    }

    @Test
    public void testGetCSStatusMsg() {
	msg = DCCppMessage.makeCSStatusMsg();
	log.debug("get status message = {}", msg.toString());
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 's', msg.getElement(0) & 0xFF);
    }

    @Test
    public void testMonitorStringCSStatusMsg() {
	msg = DCCppMessage.makeCSStatusMsg();
        Assert.assertEquals("Monitor string","Status Cmd ",msg.toMonitorString());
    }

    @Test
    public void testGetAddressedEmergencyStopMsg() {
	msg = DCCppMessage.makeAddressedEmergencyStop(5, 24);
	log.debug("emergency stop message = {}", msg.toString());
        Assert.assertEquals("length", 11, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 't', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", '2', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '-', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '1', msg.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', msg.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '1', msg.getElement(10) & 0xFF);
    }

    @Test
    public void testMonitorStringAddressedEmergencyStopMsg() {
	msg = DCCppMessage.makeAddressedEmergencyStop(5, 24);
        Assert.assertEquals("Monitor string","Throttle Cmd: \n\tRegister: 5\n\tAddress: 24\n\tSpeed: -1\n\t:Direction: Forward",msg.toMonitorString());
    }

    @Test
    public void testGetSpeedAndDirectionMsg() {
	msg = DCCppMessage.makeSpeedAndDirectionMsg(5, 24, 0.5f, false);
	log.debug("Speed message 1 = {}", msg.toString());
        Assert.assertEquals("length", 11, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 't', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", '2', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '6', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '3', msg.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', msg.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '0', msg.getElement(10) & 0xFF);

	msg = DCCppMessage.makeSpeedAndDirectionMsg(5, 24, 1.0f, true);
	log.debug("Speed message 2 = {}", msg.toString());
        Assert.assertEquals("length", 12, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 't', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", '2', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '1', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '2', msg.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", '6', msg.getElement(9) & 0xFF); // Speed steps capped at 126!
        Assert.assertEquals("10th byte", ' ', msg.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", '1', msg.getElement(11) & 0xFF);

	msg = DCCppMessage.makeSpeedAndDirectionMsg(5, 24, -1, true);
	log.debug("Speed message 3 = {}", msg.toString());
        Assert.assertEquals("length", 11, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 't', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", '2', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '-', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '1', msg.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', msg.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '1', msg.getElement(10) & 0xFF);
    }

    @Test
    public void testMonitorStringSpeedAndDirectionMsg() {
	msg = DCCppMessage.makeSpeedAndDirectionMsg(5, 24, 0.5f, false);
        Assert.assertEquals("Monitor string","Throttle Cmd: \n\tRegister: 5\n\tAddress: 24\n\tSpeed: 63\n\t:Direction: Reverse",msg.toMonitorString());
    }

    @Test
    public void testgetWriteDCCPacketMainMsg() {
        byte packet[]={(byte)0xC4,(byte)0xD2,(byte)0x12,(byte)0x0C,(byte)0x08};
	msg = DCCppMessage.makeWriteDCCPacketMainMsg(0, 5, packet);
	log.debug("DCC packet main message = {}", msg.toString());
        Assert.assertEquals("length", 18, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'M', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '0', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", 'C', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 'D', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '2', msg.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', msg.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '1', msg.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", '2', msg.getElement(11) & 0xFF);
        Assert.assertEquals("12th byte", ' ', msg.getElement(12) & 0xFF);
        Assert.assertEquals("13th byte", '0', msg.getElement(13) & 0xFF);
        Assert.assertEquals("14th byte", 'C', msg.getElement(14) & 0xFF);
        Assert.assertEquals("15th byte", ' ', msg.getElement(15) & 0xFF);
        Assert.assertEquals("16th byte", '0', msg.getElement(16) & 0xFF);
        Assert.assertEquals("17th byte", '8', msg.getElement(17) & 0xFF);
    }

    @Test
    public void testMonitorStringWriteDccPacketMainMsg() {
        byte packet[]={(byte)0xC4,(byte)0xD2,(byte)0x12,(byte)0x0C,(byte)0x08};
	msg = DCCppMessage.makeWriteDCCPacketMainMsg(0, 5, packet);
        Assert.assertEquals("Monitor string","Write DCC Packet Main Cmd: \n\tRegister: 0\n\tPacket: C4 D2 12 0C 08",msg.toMonitorString());
    }

    @Test
    public void testgetWriteDCCPacketProgMsg() {
        byte packet[]={(byte)0xC4,(byte)0xD2,(byte)0x12,(byte)0x0C,(byte)0x08};
	msg = DCCppMessage.makeWriteDCCPacketProgMsg(0, 5, packet);
	log.debug("DCC packet main message = {}", msg.toString());
        Assert.assertEquals("length", 18, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'P', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '0', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", 'C', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 'D', msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '2', msg.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', msg.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '1', msg.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", '2', msg.getElement(11) & 0xFF);
        Assert.assertEquals("12th byte", ' ', msg.getElement(12) & 0xFF);
        Assert.assertEquals("13th byte", '0', msg.getElement(13) & 0xFF);
        Assert.assertEquals("14th byte", 'C', msg.getElement(14) & 0xFF);
        Assert.assertEquals("15th byte", ' ', msg.getElement(15) & 0xFF);
        Assert.assertEquals("16th byte", '0', msg.getElement(16) & 0xFF);
        Assert.assertEquals("17th byte", '8', msg.getElement(17) & 0xFF);
    }

    @Test
    public void testMonitorStringWriteDccPacketProgMsg() {
        byte packet[]={(byte)0xC4,(byte)0xD2,(byte)0x12,(byte)0x0C,(byte)0x08};
	msg = DCCppMessage.makeWriteDCCPacketProgMsg(0, 5, packet);
        Assert.assertEquals("Monitor string","Write DCC Packet Prog Cmd: \n\tRegister: 0\n\tPacket: C4 D2 12 0C 08",msg.toMonitorString());
    }

    @Test
    public void testGetOutputCmdMsgOn() {
	msg = DCCppMessage.makeOutputCmdMsg(23, true);
	log.debug("turnout message = {}", msg.toString());
        Assert.assertEquals("length", 6, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'Z', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', msg.getElement(5) & 0xFF);
    }

    @Test
    public void testGetOutputCmdMsgOff() {
	msg = DCCppMessage.makeOutputCmdMsg(23, false);
	log.debug("turnout message = {}", msg.toString());
        Assert.assertEquals("length", 6, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'Z', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '0', msg.getElement(5) & 0xFF);
    }

    @Test
    public void testMonitorStringOutputCmdMsgOn() {
	msg = DCCppMessage.makeOutputCmdMsg(23, true);
        Assert.assertEquals("Monitor string","Output Cmd: \n\tOutput ID: 23\n\tState: HIGH",msg.toMonitorString());
    }
    @Test
    public void testMonitorStringOutputCmdMsgOff() {
	msg = DCCppMessage.makeOutputCmdMsg(23, false);
        Assert.assertEquals("Monitor string","Output Cmd: \n\tOutput ID: 23\n\tState: LOW",msg.toMonitorString());
    }

    @Test

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new DCCppMessage("T 42 1");
    }

    @After
    public void tearDown() {
	m = msg = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppMessageTest.class);

}
