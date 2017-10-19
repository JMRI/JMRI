package jmri.jmrix.dccpp;

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
public class DCCppMessageTest {

    @Test
    public void testCtor() {
        DCCppMessage m = new DCCppMessage(3);
     	Assert.assertNotNull(m);
    }

    // check opcode inclusion in message
    @Test
    public void testOpCode() {
        DCCppMessage m = new DCCppMessage(5);
	Assert.assertNotNull(m);
        //m.setOpCode('i');
        //Assert.assertEquals("read=back op code", 'i', m.getOpCode());
        //Assert.assertEquals("stored op code", 'i', m.getElement(0));
    }

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        DCCppMessage m = DCCppMessage.parseDCCppMessage("T 42 1");
        Assert.assertEquals("length", 6, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'T', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '4', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '2', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', m.getElement(5) & 0xFF);
    }

    // Test the canned messages.
    @Test
    public void testGetAccessoryDecoderMsg() {
	    DCCppMessage m = DCCppMessage.makeAccessoryDecoderMsg(23, 2, true);
	    log.debug("accessory decoder message = {}", m.toString());
        Assert.assertEquals("length", 8, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '2', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '1', m.getElement(7) & 0xFF);
    }

    @Test
    public void testMonitorStringAccessoryDecoderMsg() {
	    DCCppMessage m = DCCppMessage.makeAccessoryDecoderMsg(23, 2, true);
        Assert.assertEquals("Monitor string","Accessory Decoder Cmd: \n\tAddress: 23\n\tSubaddr: 2\n\tState: ON",m.toMonitorString());
    }

    @Test
    public void testGetTurnoutCommandMsg() {
	    DCCppMessage m = DCCppMessage.makeTurnoutCommandMsg(23, true);
	    log.debug("turnout message = {}", m.toString());
        Assert.assertEquals("length", 6, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'T', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', m.getElement(5) & 0xFF);
    }

    @Test
    public void testMonitorStringTurnoutCommandMsg() {
	    DCCppMessage m = DCCppMessage.makeTurnoutCommandMsg(23, true);
        Assert.assertEquals("Monitor string","Turnout Cmd: \n\tT/O ID: 23\n\tState: THROWN",m.toMonitorString());
    }

    @Test
    public void testGetWriteDirectCVMsg() {
	    DCCppMessage m = DCCppMessage.makeWriteDirectCVMsg(29, 12, 1, 2);
	    log.debug("write cv message = {}", m.toString());
        Assert.assertEquals("length", 11, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'W', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '9', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '2', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", ' ', m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '1', m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '2', m.getElement(10) & 0xFF);
    }

    @Test
    public void testMonitorStringWriteDirectCVMsg() {
	    DCCppMessage m = DCCppMessage.makeWriteDirectCVMsg(29, 12, 1, 2);
        Assert.assertEquals("Monitor string","Prog Write Byte Cmd: \n\tCV : 29\n\tValue: 12\n\tCallback Num: 1\n\tCallback Sub: 2",m.toMonitorString());
    }

    @Test
    public void testGetBitWriteDirectCVMsg() {
	    DCCppMessage m = DCCppMessage.makeBitWriteDirectCVMsg(17, 4, 1, 3, 4);
	    log.debug("write cv bit message = {}", m.toString());
        Assert.assertEquals("length", 12, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'B', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '7', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '1', m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", ' ', m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", '3', m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", ' ', m.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", '4', m.getElement(11) & 0xFF);
    }

    @Test
    public void testMonitorStringBitWriteDirectCVMsg() {
	    DCCppMessage m = DCCppMessage.makeBitWriteDirectCVMsg(17, 4, 1, 3, 4);
        Assert.assertEquals("Monitor string","Prog Write Bit Cmd: \n\tCV : 17\n\tBit : 4\n\tValue: 1\n\tCallback Num: 3\n\tCallback Sub: 4",m.toMonitorString());
    }

    @Test
    public void testGetReadDirectCVMsg() {
	    DCCppMessage m = DCCppMessage.makeReadDirectCVMsg(17, 4, 3);
	    log.debug("read cv message = {}", m.toString());
        Assert.assertEquals("length", 8, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'R', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '7', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '3', m.getElement(7) & 0xFF);
    }

    @Test
    public void testMonitorStringReadDirectCVMsg() {
	    DCCppMessage m = DCCppMessage.makeReadDirectCVMsg(17, 4, 3);
        Assert.assertEquals("Monitor string","Prog Read Cmd: \n\tCV: 17\n\tCallback Num: 4\n\tCallback Sub: 3",m.toMonitorString());
    }

    @Test
    public void testGetWriteOpsModeCVMsg() {
	    DCCppMessage m = DCCppMessage.makeWriteOpsModeCVMsg(17, 4, 3);
	    log.debug("write ops cv message = {}", m.toString());
        Assert.assertEquals("length", 8, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'w', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '7', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '3', m.getElement(7) & 0xFF);
    }

    @Test
    public void testMonitorStringWriteOpsModeCVMsg() {
	    DCCppMessage m = DCCppMessage.makeWriteOpsModeCVMsg(17, 4, 3);
        Assert.assertEquals("Monitor string","Ops Write Byte Cmd: \n\tAddress: 17\n\tCV: 4\n\tValue: 3",m.toMonitorString());
    }

    @Test
    public void testGetBitWriteOpsModeCVMsg() {
	    DCCppMessage m = DCCppMessage.makeBitWriteOpsModeCVMsg(17, 4, 3, 1);
	    log.debug("write ops bit cv message = {}", m.toString());
        Assert.assertEquals("length", 10, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'b', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '7', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '3', m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", ' ', m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", '1', m.getElement(9) & 0xFF);
    }

    @Test
    public void testMonitorStringBitWriteOpsModeCVMsg() {
	    DCCppMessage m = DCCppMessage.makeBitWriteOpsModeCVMsg(17, 4, 3, 1);
        Assert.assertEquals("Monitor string","Ops Write Bit Cmd: \n\tAddress: 17\n\tCV: 4\n\tBit: 3\n\tValue: 1",m.toMonitorString());
    }

    @Test
    public void testSetTrackPowerMsg() {
	    DCCppMessage m = DCCppMessage.makeSetTrackPowerMsg(true);
	    log.debug("track power on message = {}", m.toString());
        Assert.assertEquals("length", 1, m.getNumDataElements());
        Assert.assertEquals("0th byte", '1', m.getElement(0) & 0xFF);

	    DCCppMessage m2 = DCCppMessage.makeSetTrackPowerMsg(false);
	    log.debug("track power off message = {}", m2.toString());
        Assert.assertEquals("length", 1, m2.getNumDataElements());
        Assert.assertEquals("0th byte", '0', m2.getElement(0) & 0xFF);
    }

    @Test
    public void testMonitorStringSetTrackPowerMsg() {
	    DCCppMessage m = DCCppMessage.makeSetTrackPowerMsg(true);
        Assert.assertEquals("Monitor string","Track Power ON Cmd ",m.toMonitorString());
    }

    @Test
    public void testReadTrackCurrentMsg() {
	    DCCppMessage m = DCCppMessage.makeReadTrackCurrentMsg();
	    log.debug("read track current message = {}", m.toString());
        Assert.assertEquals("length", 1, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'c', m.getElement(0) & 0xFF);
    }

    @Test
    public void testMonitorStringReadTrackCurrentMsg() {
	    DCCppMessage m = DCCppMessage.makeReadTrackCurrentMsg();
        Assert.assertEquals("Monitor string","Read Track Current Cmd ",m.toMonitorString());
    }

    @Test
    public void testGetCSStatusMsg() {
	    DCCppMessage m = DCCppMessage.makeCSStatusMsg();
	    log.debug("get status message = {}", m.toString());
        Assert.assertEquals("length", 1, m.getNumDataElements());
        Assert.assertEquals("0th byte", 's', m.getElement(0) & 0xFF);
    }

    @Test
    public void testMonitorStringCSStatusMsg() {
	    DCCppMessage m = DCCppMessage.makeCSStatusMsg();
        Assert.assertEquals("Monitor string","Status Cmd ",m.toMonitorString());
    }

    @Test
    public void testGetAddressedEmergencyStopMsg() {
	    DCCppMessage m = DCCppMessage.makeAddressedEmergencyStop(5, 24);
	    log.debug("emergency stop message = {}", m.toString());
        Assert.assertEquals("length", 11, m.getNumDataElements());
        Assert.assertEquals("0th byte", 't', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', m.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', m.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", '2', m.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '-', m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '1', m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '1', m.getElement(10) & 0xFF);
    }

    @Test
    public void testMonitorStringAddressedEmergencyStopMsg() {
	    DCCppMessage m = DCCppMessage.makeAddressedEmergencyStop(5, 24);
        Assert.assertEquals("Monitor string","Throttle Cmd: \n\tRegister: 5\n\tAddress: 24\n\tSpeed: -1\n\t:Direction: Forward",m.toMonitorString());
    }

    @Test
    public void testGetSpeedAndDirectionMsg() {
	    DCCppMessage m = DCCppMessage.makeSpeedAndDirectionMsg(5, 24, 0.5f, false);
	    log.debug("Speed message 1 = {}", m.toString());
        Assert.assertEquals("length", 11, m.getNumDataElements());
        Assert.assertEquals("0th byte", 't', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', m.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', m.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", '2', m.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '6', m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '3', m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '0', m.getElement(10) & 0xFF);

	    DCCppMessage m2 = DCCppMessage.makeSpeedAndDirectionMsg(5, 24, 1.0f, true);
	    log.debug("Speed message 2 = {}", m2.toString());
        Assert.assertEquals("length", 12, m2.getNumDataElements());
        Assert.assertEquals("0th byte", 't', m2.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m2.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', m2.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', m2.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", '2', m2.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', m2.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m2.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '1', m2.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '2', m2.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", '6', m2.getElement(9) & 0xFF); // Speed steps capped at 126!
        Assert.assertEquals("10th byte", ' ', m2.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", '1', m2.getElement(11) & 0xFF);

	    DCCppMessage m3 = DCCppMessage.makeSpeedAndDirectionMsg(5, 24, -1, true);
	    log.debug("Speed message 3 = {}", m3.toString());
        Assert.assertEquals("length", 11, m3.getNumDataElements());
        Assert.assertEquals("0th byte", 't', m3.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m3.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', m3.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', m3.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", '2', m3.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', m3.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m3.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '-', m3.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '1', m3.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', m3.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '1', m3.getElement(10) & 0xFF);
    }

    @Test
    public void testMonitorStringSpeedAndDirectionMsg() {
	    DCCppMessage m = DCCppMessage.makeSpeedAndDirectionMsg(5, 24, 0.5f, false);
        Assert.assertEquals("Monitor string","Throttle Cmd: \n\tRegister: 5\n\tAddress: 24\n\tSpeed: 63\n\t:Direction: Reverse",m.toMonitorString());
    }

    @Test
    public void testgetWriteDCCPacketMainMsg() {
        byte packet[]={(byte)0xC4,(byte)0xD2,(byte)0x12,(byte)0x0C,(byte)0x08};
	    DCCppMessage m = DCCppMessage.makeWriteDCCPacketMainMsg(0, 5, packet);
	    log.debug("DCC packet main message = {}", m.toString());
        Assert.assertEquals("length", 18, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'M', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '0', m.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', m.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", 'C', m.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 'D', m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '2', m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '1', m.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", '2', m.getElement(11) & 0xFF);
        Assert.assertEquals("12th byte", ' ', m.getElement(12) & 0xFF);
        Assert.assertEquals("13th byte", '0', m.getElement(13) & 0xFF);
        Assert.assertEquals("14th byte", 'C', m.getElement(14) & 0xFF);
        Assert.assertEquals("15th byte", ' ', m.getElement(15) & 0xFF);
        Assert.assertEquals("16th byte", '0', m.getElement(16) & 0xFF);
        Assert.assertEquals("17th byte", '8', m.getElement(17) & 0xFF);
    }

    @Test
    public void testMonitorStringWriteDccPacketMainMsg() {
        byte packet[]={(byte)0xC4,(byte)0xD2,(byte)0x12,(byte)0x0C,(byte)0x08};
	    DCCppMessage m = DCCppMessage.makeWriteDCCPacketMainMsg(0, 5, packet);
        Assert.assertEquals("Monitor string","Write DCC Packet Main Cmd: \n\tRegister: 0\n\tPacket: C4 D2 12 0C 08",m.toMonitorString());
    }

    @Test
    public void testgetWriteDCCPacketProgMsg() {
        byte packet[]={(byte)0xC4,(byte)0xD2,(byte)0x12,(byte)0x0C,(byte)0x08};
	    DCCppMessage m = DCCppMessage.makeWriteDCCPacketProgMsg(0, 5, packet);
	    log.debug("DCC packet main message = {}", m.toString());
        Assert.assertEquals("length", 18, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'P', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '0', m.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', m.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", 'C', m.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 'D', m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '2', m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '1', m.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", '2', m.getElement(11) & 0xFF);
        Assert.assertEquals("12th byte", ' ', m.getElement(12) & 0xFF);
        Assert.assertEquals("13th byte", '0', m.getElement(13) & 0xFF);
        Assert.assertEquals("14th byte", 'C', m.getElement(14) & 0xFF);
        Assert.assertEquals("15th byte", ' ', m.getElement(15) & 0xFF);
        Assert.assertEquals("16th byte", '0', m.getElement(16) & 0xFF);
        Assert.assertEquals("17th byte", '8', m.getElement(17) & 0xFF);
    }

    @Test
    public void testMonitorStringWriteDccPacketProgMsg() {
        byte packet[]={(byte)0xC4,(byte)0xD2,(byte)0x12,(byte)0x0C,(byte)0x08};
	    DCCppMessage m = DCCppMessage.makeWriteDCCPacketProgMsg(0, 5, packet);
        Assert.assertEquals("Monitor string","Write DCC Packet Prog Cmd: \n\tRegister: 0\n\tPacket: C4 D2 12 0C 08",m.toMonitorString());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppMessageTest.class);

}
