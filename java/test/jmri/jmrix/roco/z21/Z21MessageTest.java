package jmri.jmrix.roco.z21;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.roco.z21.z21Message class
 *
 * @author Bob Jacobsen
 */
public class Z21MessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private Z21Message msg = null;

    @Override
    @Test
    public void testCtor() {
        msg = new Z21Message(3);
        assertEquals( 3, msg.getNumDataElements(), "length");
        jmri.util.JUnitAppender.assertErrorMessage("invalid length in call to ctor");
    }

    // check opcode inclusion in message
    @Test
    public void testOpCode() {
        msg = new Z21Message(5);
        msg.setOpCode(4);
        assertEquals( 4, msg.getOpCode(), "read=back op code");
        //opcode is stored in two bytes, lsb first.
        assertEquals( 0x0004, msg.getElement(2) + (msg.getElement(3) << 8), "stored op code");
    }

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        assertEquals( 12, msg.getNumDataElements(), "length");
        assertEquals( 0x0D, msg.getElement(0) & 0xFF, "0th byte");
        assertEquals( 0x00, msg.getElement(1) & 0xFF, "1st byte");
        assertEquals( 0x04, msg.getElement(2) & 0xFF, "2nd byte");
        assertEquals( 0x00, msg.getElement(3) & 0xFF, "3rd byte");
        assertEquals( 0x12, msg.getElement(4) & 0xFF, "4th byte");
        assertEquals( 0x34, msg.getElement(5) & 0xFF, "5th byte");
        assertEquals( 0xAB, msg.getElement(6) & 0xFF, "6th byte");
        assertEquals( 0x03, msg.getElement(7) & 0xFF, "7th byte");
        assertEquals( 0x19, msg.getElement(8) & 0xFF, "8th byte");
        assertEquals( 0x06, msg.getElement(9) & 0xFF, "9th byte");
        assertEquals( 0x0B, msg.getElement(10) & 0xFF, "10th byte");
        assertEquals( 0xB1, msg.getElement(11) & 0xFF, "11th byte");
    }

    //Test some canned messages.
    @Test
    public void testSerialNumberRequest() {
        msg = Z21Message.getSerialNumberRequestMessage();
        assertEquals( 4, msg.getNumDataElements(), "length");
        assertEquals( 0x04, msg.getElement(0) & 0xFF, "0th byte");
        assertEquals( 0x00, msg.getElement(1) & 0xFF, "1st byte");
        assertEquals( 0x10, msg.getElement(2) & 0xFF, "2nd byte");
        assertEquals( 0x00, msg.getElement(3) & 0xFF, "3rd byte");
    }

    @Test
    public void testToMonitorStringSerialNumberRequest() {
        msg = Z21Message.getSerialNumberRequestMessage();
        assertEquals( "Z21 Serial Number Request", msg.toMonitorString(), "Monitor String");
    }

    @Test
    public void testGetHardwareInfoRequest() {
        msg = Z21Message.getLanGetHardwareInfoRequestMessage();
        assertEquals( 4, msg.getNumDataElements(), "length");
        assertEquals( 0x04, msg.getElement(0) & 0xFF, "0th byte");
        assertEquals( 0x00, msg.getElement(1) & 0xFF, "1st byte");
        assertEquals( 0x1A, msg.getElement(2) & 0xFF, "2nd byte");
        assertEquals( 0x00, msg.getElement(3) & 0xFF, "3rd byte");
    }

    @Test
    public void testToMonitorStringGetHardwareInfoRequest() {
        msg = Z21Message.getLanGetHardwareInfoRequestMessage();
        assertEquals( "Z21 Version Request", msg.toMonitorString(), "Monitor String");
    }

    @Test
    public void testLanLogoffRequest() {
        msg = Z21Message.getLanLogoffRequestMessage();
        assertEquals( 4, msg.getNumDataElements(), "length");
        assertEquals( 0x04, msg.getElement(0) & 0xFF, "0th byte");
        assertEquals( 0x00, msg.getElement(1) & 0xFF, "1st byte");
        assertEquals( 0x30, msg.getElement(2) & 0xFF, "2nd byte");
        assertEquals( 0x00, msg.getElement(3) & 0xFF, "3rd byte");
        assertFalse( msg.replyExpected(), "reply expected");
    }

    @Test
    public void toMonitorStringLanLogoffRequest() {
        msg = Z21Message.getLanLogoffRequestMessage();
        assertEquals( "Z21 Lan Logoff Request", msg.toMonitorString(), "Monitor String");
    }

    @Test
    public void testGetBroadCastFlagsRequest() {
        msg = Z21Message.getLanGetBroadcastFlagsRequestMessage();
        assertEquals( 4, msg.getNumDataElements(), "length");
        assertEquals( 0x04, msg.getElement(0) & 0xFF, "0th byte");
        assertEquals( 0x00, msg.getElement(1) & 0xFF, "1st byte");
        assertEquals( 0x51, msg.getElement(2) & 0xFF, "2nd byte");
        assertEquals( 0x00, msg.getElement(3) & 0xFF, "3rd byte");
    }

    @Test
    public void toMonitorStringGetBroadCastFlagsRequest() {
        msg = Z21Message.getLanGetBroadcastFlagsRequestMessage();
        assertEquals( "Request Z21 Broadcast flags", msg.toMonitorString(), "Monitor String");
    }

    @Test
    public void testSetBroadCastFlagsRequest() {
        msg = Z21Message.getLanSetBroadcastFlagsRequestMessage(0x01020304);
        assertEquals( 8, msg.getNumDataElements(), "length");
        assertEquals( 0x08, msg.getElement(0) & 0xFF, "0th byte");
        assertEquals( 0x00, msg.getElement(1) & 0xFF, "1st byte");
        assertEquals( 0x50, msg.getElement(2) & 0xFF, "2nd byte");
        assertEquals( 0x00, msg.getElement(3) & 0xFF, "3rd byte");
        assertEquals( 0x04, msg.getElement(4) & 0xFF, "4th byte");
        assertEquals( 0x03, msg.getElement(5) & 0xFF, "5th byte");
        assertEquals( 0x02, msg.getElement(6) & 0xFF, "6th byte");
        assertEquals( 0x01, msg.getElement(7) & 0xFF, "7th byte");
        assertFalse( msg.replyExpected(), "reply expected");
    }

    @Test
    public void toMonitorStringSetBroadCastFlagsRequest() {
        msg = Z21Message.getLanSetBroadcastFlagsRequestMessage(0x01020304);
        assertEquals( "Set Z21 Broadcast flags to Railcom Messages\nSystem State Messages\nLocoNet Messages\nCAN Booster Status Messages\n",
            msg.toMonitorString(), "Monitor String");
    }

    @Test
    public void testGetRailComDataRequest() {
        msg = Z21Message.getLanRailComGetDataRequestMessage();
        assertEquals( 4, msg.getNumDataElements(), "length");
        assertEquals( 0x04, msg.getElement(0) & 0xFF, "0th byte");
        assertEquals( 0x00, msg.getElement(1) & 0xFF, "1st byte");
        assertEquals( 0x89, msg.getElement(2) & 0xFF, "2nd byte");
        assertEquals( 0x00, msg.getElement(3) & 0xFF, "3rd byte");
    }

    @Test
    public void toMonitorStringRailComDataRequest() {
        msg = Z21Message.getLanRailComGetDataRequestMessage();
        assertEquals( Bundle.getMessage("Z21_RAILCOM_GETDATA"),
            msg.toMonitorString(), "Monitor String");
    }

    @Test
    public void testGetSystemStateDataChangedRequest() {
        msg = Z21Message.getLanSystemStateDataChangedRequestMessage();
        assertEquals( 4, msg.getNumDataElements(), "length");
        assertEquals( 0x04, msg.getElement(0) & 0xFF, "0th byte");
        assertEquals( 0x00, msg.getElement(1) & 0xFF, "1st byte");
        assertEquals( 0x85, msg.getElement(2) & 0xFF, "2nd byte");
        assertEquals( 0x00, msg.getElement(3) & 0xFF, "3rd byte");
    }

    @Test
    public void testToMonitorStringSystemStateDataChangedRequest() {
        msg = Z21Message.getLanSystemStateDataChangedRequestMessage();
        assertEquals( "Z21 Request System State Change Data",
            msg.toMonitorString(), "Monitor String");
    }

    @Test
    public void testGetLocoNetMessage() {
        byte message[] = {
            (byte) 0xEF, (byte) 0x0E, (byte) 0x03, (byte) 0x00, (byte) 0x03,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00};
        LocoNetMessage l = new LocoNetMessage(message);
        msg = new Z21Message(l);
        assertEquals( 17, msg.getNumDataElements(), "right length");
        LocoNetMessage x = msg.getLocoNetMessage();
        assertEquals( 0xEF, x.getElement(0) & 0xFF, "0th byte");
        assertEquals( 0x0E, x.getElement(1) & 0xFF, "1st byte");
        assertEquals( 0x03, x.getElement(2) & 0xFF, "2nd byte");
        assertEquals( 0x03, x.getElement(4) & 0xFF, "4th byte");
        assertEquals( l, x, "two messages the same");
    }

    @Test
    public void testGetNullLocoNetMessage() {
        // Pre 5.15.6 the values passed to the Z21Message byte Ctor were ignored.
        // If we create a LoconetMessage 1st using these byte values, then create a Z21Message from it
        // Z21Message returns a Tunnel message, not null.
        // byte message[] = {(byte) 0x11, (byte) 0x00, (byte) 0x88, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08};
        // Z21Message z21msg = new Z21Message(message, 17);
        Z21Message z21msg = new Z21Message("01020304"); // not supposed to be an actual tunnel message format.
        assertNull( z21msg.getLocoNetMessage(), "non-LocoNetTunnel LocoNet Message");
    }

    @Test
    public void testMonitorStringLocoNetMessage() {
        byte message[] = {
            (byte) 0xEF, (byte) 0x0E, (byte) 0x03, (byte) 0x00, (byte) 0x03,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00};
        LocoNetMessage l = new LocoNetMessage(message);
        msg = new Z21Message(l);
        assertEquals( "LocoNet Tunnel Message: Write slot 3 information:\n\tLoco 3 (short) is Not Consisted, Free, operating in 28 SS mode, and is moving Forward at speed 0,\n\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n\tMaster supports DT200; Track Status: Off/Paused; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x00 0x00 (0).\n",
            msg.toMonitorString(), "Monitor String");
    }

    @Test
    public void testMonitorStringLocoNetMessage2() {
        byte message[] = {
            (byte) 0xD0, (byte) 0x20, (byte) 0x04,
            (byte) 0x7D, (byte) 0x0A, (byte) 0x7C};
        LocoNetMessage l = new LocoNetMessage(message);
        msg = new Z21Message(l);
        assertEquals( "LocoNet Tunnel Message: Transponder address 10 (short) (or long address 16010) present at LR5 () (BDL16x Board ID 1 RX4 zone C or BXP88 Board ID 1 section 5 or the BXPA1 Board ID 5 section).\n",
            msg.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringLanRMBusGetDataRequest() {
        msg = Z21Message.getLanRMBusGetDataRequestMessage(0);
        assertEquals( "Z21 RM Bus Data Request for group 0",
            msg.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringLanRMBusProgramModule() {
        msg = Z21Message.getLanRMBusProgramModuleMessage(0);
        assertEquals( "Z21 RM Bus Program Module to Address 0",
            msg.toMonitorString(), "Monitor String");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        msg = new Z21Message("0D 00 04 00 12 34 AB 3 19 6 B B1");
        m = msg;
    }

    @AfterEach
    @Override
    public void tearDown() {
        m = null;
        msg = null;
        JUnitUtil.tearDown();
    }

}
