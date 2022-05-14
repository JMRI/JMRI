package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCCppMessageTest.java
 * <p>
 * Test for the jmri.jmrix.dccpp.DCCppMessage class
 *
 * @author Bob Jacobsen
 * @author Mark Underwood
 */
public class DCCppMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private DCCppMessage msg = null;

    // check opcode inclusion in message
    @Test
    public void testOpCode() {
        msg = new DCCppMessage(5);
        Assert.assertNotNull(msg);
    }

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        msg = new DCCppMessage("T 42 1");
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        log.debug("accessory decoder message = '{}'", msg);
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
        Assert.assertEquals("Monitor string", "Accessory Decoder Cmd: Address: 23, Subaddr: 2, State: ON", msg.toMonitorString());
    }

    @Test
    public void testMonitorStringAccessoryDecoderMsgActivateFalse() {
        msg = DCCppMessage.makeAccessoryDecoderMsg(23, 2, false);
        Assert.assertEquals("Monitor string", "Accessory Decoder Cmd: Address: 23, Subaddr: 2, State: OFF", msg.toMonitorString());
    }

    @Test
    public void testMakesAndMonitors() {
        msg = new DCCppMessage("F 123 22 1");
        Assert.assertEquals("Monitor string", "Function Cmd: CAB: 123, FUNC: 22, State: 1", msg.toMonitorString());
        msg = DCCppMessage.makeFunctionV4Message(123, 4, true);
        Assert.assertEquals("Monitor string", "Function Cmd: CAB: 123, FUNC: 4, State: 1", msg.toMonitorString());
        msg = DCCppMessage.makeFunctionV4Message(123, 5, false);
        Assert.assertEquals("Monitor string", "Function Cmd: CAB: 123, FUNC: 5, State: 0", msg.toMonitorString());
        msg = DCCppMessage.makeForgetCabMessage(1234);
        Assert.assertEquals("Monitor string", "Forget Cab: CAB: 1234, (No Reply Expected)", msg.toMonitorString());
        msg = new DCCppMessage("- 1234");
        Assert.assertEquals("Monitor string", "Forget Cab: CAB: 1234, (No Reply Expected)", msg.toMonitorString());
        msg = new DCCppMessage("-");
        Assert.assertEquals("Monitor string", "Forget Cab: CAB: [ALL], (No Reply Expected)", msg.toMonitorString());
        msg = DCCppMessage.makeForgetCabMessage(12345);
        Assert.assertNull("null on invalid address", msg);
    }

    @Test
    public void testGetTurnoutCommandMsgThrown() {
        msg = DCCppMessage.makeTurnoutCommandMsg(23, true);
        log.debug("turnout message = '{}'", msg);
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
        log.debug("turnout message = '{}'", msg);
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
        Assert.assertEquals("Monitor string", "Turnout Cmd: ID: 23, State: THROWN", msg.toMonitorString());
    }

    @Test
    public void testMonitorStringTurnoutCommandMsgClosed() {
        msg = DCCppMessage.makeTurnoutCommandMsg(23, false);
        Assert.assertEquals("Monitor string", "Turnout Cmd: ID: 23, State: CLOSED", msg.toMonitorString());
    }

    @Test
    public void testTurnoutAddCommands() { /* test turnout add commands (new in DCC++EX 3.1.7) */
        msg = new DCCppMessage("T 23 DCC 5 0");
        Assert.assertEquals("Monitor string", "Add Turnout DCC: ID:23, Address:5, Subaddr:0", msg.toMonitorString());
        msg = new DCCppMessage("T 24 SERVO 100 410 205 2");
        Assert.assertEquals("Monitor string", "Add Turnout Servo: ID:24, Pin:100, ThrownPos:410, ClosedPos:205, Profile:2"
                , msg.toMonitorString());
        msg = new DCCppMessage("T 25 VPIN 50");
        Assert.assertEquals("Monitor string", "Add Turnout Vpin: ID:25, Pin:50", msg.toMonitorString());
        msg = new DCCppMessage("T 23 DCC 5 T");
        Assert.assertEquals("Monitor string", "Unmatched Turnout Cmd: T 23 DCC 5 T", msg.toMonitorString());
    }

    @Test
    public void testDiagAndControlCommands() { /* test diagnostic and control commands (new in DCC++EX 3.1.7) */
        msg = new DCCppMessage("D EXRAIL ON");
        Assert.assertEquals("Monitor string", "Diag Cmd: 'D EXRAIL ON'",       msg.toMonitorString());
        msg = new DCCppMessage("/START 1224 4");
        Assert.assertEquals("Monitor string", "Control Cmd: '/START 1224 4'",  msg.toMonitorString());
        msg = new DCCppMessage("/ START 1224 4");
        Assert.assertEquals("Monitor string", "Control Cmd: '/ START 1224 4'", msg.toMonitorString());
        msg = new DCCppMessage("/PAUSE");
        Assert.assertEquals("Monitor string", "Control Cmd: '/PAUSE'",         msg.toMonitorString());
    }

    @Test
    public void testGetWriteDirectCVMsg() {
        msg = DCCppMessage.makeWriteDirectCVMsg(29, 12, 1, 2);
        log.debug("write cv message = '{}'", msg);
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
        Assert.assertEquals("Monitor string", "Prog Write Byte Cmd: CV: 29, Value: 12, Callback Num: 1, Sub: 2", msg.toMonitorString());
    }

    @Test
    public void testMonitorStringWriteDirectCVMsgV4() {
        msg = DCCppMessage.makeWriteDirectCVMsgV4(29, 12);
        Assert.assertEquals("Monitor string", "Prog Write Byte Cmd: CV: 29, Value: 12", msg.toMonitorString());
    }

    @Test
    public void testGetBitWriteDirectCVMsg() {
        msg = DCCppMessage.makeBitWriteDirectCVMsg(17, 4, 1, 3, 4);
        log.debug("write cv bit message = '{}'", msg);
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
        Assert.assertEquals("Monitor string", "Prog Write Bit Cmd: CV: 17, Bit: 4, Value: 1, Callback Num: 3, Sub: 4", msg.toMonitorString());
    }

    @Test
    public void testMonitorStringBitWriteDirectCVMsgV4() {
        msg = DCCppMessage.makeBitWriteDirectCVMsgV4(17, 4, 1);
        Assert.assertEquals("Monitor string", "Prog Write Bit Cmd: CV: 17, Bit: 4, Value: 1", msg.toMonitorString());
    }

    @Test
    public void testGetReadDirectCVMsg() {
        msg = DCCppMessage.makeReadDirectCVMsg(17, 4, 3);
        log.debug("read cv message = '{}'", msg);
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
        Assert.assertEquals("Monitor string", "Prog Read Cmd: CV: 17, Callback Num: 4, Sub: 3", msg.toMonitorString());
    }

    @Test
    public void testMonitorStringReadCVV4Msg() {
        msg = new DCCppMessage("R 123");
        Assert.assertEquals("Monitor string", "Prog Read CV: CV:123", msg.toMonitorString());
    }

    @Test
    public void testMonitorStringReadLocoIDMsg() {
        msg = new DCCppMessage("R");
        Assert.assertEquals("Monitor string", "Prog Read LocoID Cmd", msg.toMonitorString());
    }

    @Test
    public void testGetWriteOpsModeCVMsg() {
        msg = DCCppMessage.makeWriteOpsModeCVMsg(17, 4, 3);
        log.debug("write ops cv message = '{}'", msg);
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
        Assert.assertEquals("Monitor string", "Ops Write Byte Cmd: Address: 17, CV: 4, Value: 3", msg.toMonitorString());
    }

    @Test
    public void testGetBitWriteOpsModeCVMsg() {
        msg = DCCppMessage.makeBitWriteOpsModeCVMsg(17, 4, 3, 1);
        log.debug("write ops bit cv message = '{}'", msg);
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
        Assert.assertEquals("Monitor string", "Ops Write Bit Cmd: Address: 17, CV: 4, Bit: 3, Value: 1", msg.toMonitorString());
    }

    @Test
    public void testSetTrackPowerMsg() {
        msg = DCCppMessage.makeSetTrackPowerMsg(true);
        log.debug("track power on message = '{}'", msg);
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("0th byte", '1', msg.getElement(0) & 0xFF);

        msg = DCCppMessage.makeSetTrackPowerMsg(false);
        log.debug("track power off message = '{}'", msg);
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("0th byte", '0', msg.getElement(0) & 0xFF);
    }

    @Test
    public void testMonitorStringSetTrackPowerMsg() {
        msg = DCCppMessage.makeSetTrackPowerMsg(true);
        Assert.assertEquals("Monitor string", "Track Power ON Cmd ", msg.toMonitorString());
    }

    @Test
    public void testReadTrackCurrentMsg() {
        msg = DCCppMessage.makeReadTrackCurrentMsg();
        log.debug("read track current message = '{}'", msg);
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'c', msg.getElement(0) & 0xFF);
    }

    @Test
    public void testMonitorStringReadTrackCurrentMsg() {
        msg = DCCppMessage.makeReadTrackCurrentMsg();
        Assert.assertEquals("Monitor string", "Read Track Current Cmd ", msg.toMonitorString());
    }

    @Test
    public void testGetCSStatusMsg() {
        msg = DCCppMessage.makeCSStatusMsg();
        log.debug("get status message = '{}'", msg);
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 's', msg.getElement(0) & 0xFF);
    }

    @Test
    public void testMonitorStringCSStatusMsg() {
        msg = DCCppMessage.makeCSStatusMsg();
        Assert.assertEquals("Monitor string", "Status Cmd ", msg.toMonitorString());
    }

    @Test
    public void testGetAddressedEmergencyStopMsg() {
        msg = DCCppMessage.makeAddressedEmergencyStop(5, 24);
        log.debug("emergency stop message = '{}'", msg);
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
    public void testEStopAllMsg() {
        msg = DCCppMessage.makeEmergencyStopAllMsg();
        log.debug("eStop All message = '{}'", msg);
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("0th byte", '!', msg.getElement(0) & 0xFF);
    }

    @Test
    public void testMonitorStringAddressedEmergencyStopMsg() {
        msg = DCCppMessage.makeAddressedEmergencyStop(5, 24);
        Assert.assertEquals("Monitor string", "Throttle Cmd: Register: 5, Address: 24, Speed: -1, Direction: Forward", msg.toMonitorString());
        msg = DCCppMessage.makeAddressedEmergencyStop(24);
        Assert.assertEquals("Monitor string", "Throttle Cmd: Address: 24, Speed: -1, Direction: Forward", msg.toMonitorString());
    }

    @Test
    public void testGetSpeedAndDirectionMsg() {
        msg = DCCppMessage.makeSpeedAndDirectionMsg(5, 24, 0.5f, false);
        log.debug("Speed message 1 = '{}'", msg);
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
        log.debug("Speed message 2 = '{}'", msg);
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
        log.debug("Speed message 3 = '{}'", msg);
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
        Assert.assertEquals("Monitor string", "Throttle Cmd: Register: 5, Address: 24, Speed: 63, Direction: Reverse", msg.toMonitorString());        
        //newer version without register
        msg = DCCppMessage.makeSpeedAndDirectionMsg(24, 0.5f, false);
        Assert.assertEquals("Monitor string", "Throttle Cmd: Address: 24, Speed: 63, Direction: Reverse", msg.toMonitorString());        
    }

    @Test
    public void testMonitorMakeAddressedEmergencyStop() { 
        msg = DCCppMessage.makeAddressedEmergencyStop(5, 24);
        Assert.assertEquals("Monitor string", "Throttle Cmd: Register: 5, Address: 24, Speed: -1, Direction: Forward", msg.toMonitorString());        
        //newer version without register
        msg = DCCppMessage.makeAddressedEmergencyStop(24);
        Assert.assertEquals("Monitor string", "Throttle Cmd: Address: 24, Speed: -1, Direction: Forward", msg.toMonitorString());        
    }

    @Test
    public void testgetWriteDCCPacketMainMsg() {
        byte packet[] = {(byte) 0xC4, (byte) 0xD2, (byte) 0x12, (byte) 0x0C, (byte) 0x08};
        msg = DCCppMessage.makeWriteDCCPacketMainMsg(0, 5, packet);
        log.debug("DCC packet main message = '{}'", msg);
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
        byte packet[] = {(byte) 0xC4, (byte) 0xD2, (byte) 0x12, (byte) 0x0C, (byte) 0x08};
        msg = DCCppMessage.makeWriteDCCPacketMainMsg(0, 5, packet);
        Assert.assertEquals("Monitor string", "Write DCC Packet Main Cmd: Register: 0, Packet: C4 D2 12 0C 08", msg.toMonitorString());
    }

    @Test
    public void testgetWriteDCCPacketProgMsg() {
        byte packet[] = {(byte) 0xC4, (byte) 0xD2, (byte) 0x12, (byte) 0x0C, (byte) 0x08};
        msg = DCCppMessage.makeWriteDCCPacketProgMsg(0, 5, packet);
        log.debug("DCC packet main message = '{}'", msg);
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
        byte packet[] = {(byte) 0xC4, (byte) 0xD2, (byte) 0x12, (byte) 0x0C, (byte) 0x08};
        msg = DCCppMessage.makeWriteDCCPacketProgMsg(0, 5, packet);
        Assert.assertEquals("Monitor string", "Write DCC Packet Prog Cmd: Register: 0, Packet: C4 D2 12 0C 08", msg.toMonitorString());
    }

    @Test
    public void testGetOutputCmdMsgOn() {
        msg = DCCppMessage.makeOutputCmdMsg(23, true);
        log.debug("turnout message = '{}'", msg);
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
        log.debug("turnout message = '{}'", msg);
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
        Assert.assertEquals("Monitor string", "Output Cmd: ID: 23, State: HIGH", msg.toMonitorString());
    }

    @Test
    public void testMonitorStringOutputCmdMsgOff() {
        msg = DCCppMessage.makeOutputCmdMsg(23, false);
        Assert.assertEquals("Monitor string", "Output Cmd: ID: 23, State: LOW", msg.toMonitorString());
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        m = msg = new DCCppMessage("T 42 1");
    }

    @AfterEach
    public void tearDown() {
        m = msg = null;
        JUnitUtil.resetWindows(false, false);
        super.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppMessageTest.class);

}
