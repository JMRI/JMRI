package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the jmri.jmrix.roco.z21.z21Message class
 *
 * @author	Bob Jacobsen
 */
public class Z21MessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private Z21Message msg = null;

    @Override
    @Test
    public void testCtor() {
        msg = new Z21Message(3);
        Assert.assertEquals("length", 3, msg.getNumDataElements());
        jmri.util.JUnitAppender.assertErrorMessage("invalid length in call to ctor");
    }

    // check opcode inclusion in message
    @Test
    public void testOpCode() {
        msg = new Z21Message(5);
        msg.setOpCode(4);
        Assert.assertEquals("read=back op code", 4, msg.getOpCode());
        //opcode is stored in two bytes, lsb first.
        Assert.assertEquals("stored op code", 0x0004, msg.getElement(2) + (msg.getElement(3) << 8));
    }

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        Assert.assertEquals("length", 12, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 0x0D, msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x04, msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", 0x12, msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", 0x34, msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", 0xAB, msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 0x03, msg.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", 0x19, msg.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", 0x06, msg.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", 0x0B, msg.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", 0xB1, msg.getElement(11) & 0xFF);
    }

    //Test some canned messages.
    @Test
    public void SerialNumberRequest(){
        msg = Z21Message.getSerialNumberRequestMessage();
        Assert.assertEquals("length", 4, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 0x04, msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x10, msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, msg.getElement(3) & 0xFF);
    }

    @Test
    public void toMonitorStringSerialNumberRequest(){
        msg = Z21Message.getSerialNumberRequestMessage();
        Assert.assertEquals("Monitor String","Z21 Serial Number Request",msg.toMonitorString());
    }

    @Test
    public void GetHardwareInfoRequest(){
        msg = Z21Message.getLanGetHardwareInfoRequestMessage();
        Assert.assertEquals("length", 4, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 0x04, msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x1A, msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, msg.getElement(3) & 0xFF);
    }

    @Test
    public void toMonitorStringGetHardwareInfoRequest(){
        msg = Z21Message.getLanGetHardwareInfoRequestMessage();
        Assert.assertEquals("Monitor String","Z21 Version Request",msg.toMonitorString());
    }

    @Test
    public void LanLogoffRequest(){
        msg = Z21Message.getLanLogoffRequestMessage();
        Assert.assertEquals("length", 4, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 0x04, msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x30, msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, msg.getElement(3) & 0xFF);
        Assert.assertFalse("reply expected",msg.replyExpected());
    }

    @Test
    public void toMonitorStringLanLogoffRequest(){
        msg = Z21Message.getLanLogoffRequestMessage();
        Assert.assertEquals("Monitor String","04 00 30 00",msg.toMonitorString());
    }

    @Test
    public void GetBroadCastFlagsRequest(){
        msg = Z21Message.getLanGetBroadcastFlagsRequestMessage();
        Assert.assertEquals("length", 4, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 0x04, msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x51, msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, msg.getElement(3) & 0xFF);
    }

    @Test
    public void toMonitorStringGetBroadCastFlagsRequest(){
        msg = Z21Message.getLanGetBroadcastFlagsRequestMessage();
        Assert.assertEquals("Monitor String","Request Z21 Broadcast flags",msg.toMonitorString());
    }

    @Test
    public void SetBroadCastFlagsRequest(){
        msg = Z21Message.getLanSetBroadcastFlagsRequestMessage(0x01020304);
        Assert.assertEquals("length", 8, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 0x08, msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x50, msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, msg.getElement(3) & 0xFF);
        Assert.assertEquals("5th byte", 0x04, msg.getElement(4) & 0xFF);
        Assert.assertEquals("6st byte", 0x03, msg.getElement(5) & 0xFF);
        Assert.assertEquals("7nd byte", 0x02, msg.getElement(6) & 0xFF);
        Assert.assertEquals("8rd byte", 0x01, msg.getElement(7) & 0xFF);
        Assert.assertFalse("reply expected",msg.replyExpected());
    }

    @Test
    public void toMonitorStringSetBroadCastFlagsRequest(){
        msg = Z21Message.getLanSetBroadcastFlagsRequestMessage(0x01020304);
        Assert.assertEquals("Monitor String","Set Z21 Broadcast flags to 16909060",msg.toMonitorString());
    }

    @Test
    public void GetRailComDataRequest(){
        msg = Z21Message.getLanRailComGetDataRequestMessage();
        Assert.assertEquals("length", 4, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 0x04, msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x89, msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, msg.getElement(3) & 0xFF);
    }

    @Test
    public void toMonitorStringRailComDataRequest(){
        msg = Z21Message.getLanRailComGetDataRequestMessage();
        Assert.assertEquals("Monitor String",Bundle.getMessage("Z21_RAILCOM_GETDATA"),msg.toMonitorString());
    }

    @Test
    public void GetSystemStateDataChangedRequest(){
        msg = Z21Message.getLanSystemStateDataChangedRequestMessage();
        Assert.assertEquals("length", 4, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 0x04, msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x85, msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, msg.getElement(3) & 0xFF);
    }

    @Test
    public void toMonitorStringSystemStateDataChangedRequest(){
        msg = Z21Message.getLanSystemStateDataChangedRequestMessage();
        Assert.assertEquals("Monitor String","04 00 85 00",msg.toMonitorString());
    }

    @Test
    public void getLocoNetMessage(){
        byte message[]={
           (byte)0xEF,(byte)0x0E,(byte)0x03,(byte)0x00,(byte)0x03,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00};
        jmri.jmrix.loconet.LocoNetMessage l = new jmri.jmrix.loconet.LocoNetMessage(message);
        msg = new Z21Message(l);
        Assert.assertEquals("right length",17,msg.getNumDataElements());
        jmri.jmrix.loconet.LocoNetMessage x = msg.getLocoNetMessage();
        Assert.assertEquals("0th byte", 0xEF, x.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x0E, x.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x03, x.getElement(2) & 0xFF);
        Assert.assertEquals("4nd byte", 0x03, x.getElement(4) & 0xFF);
        Assert.assertEquals("two messaes the same",l,x);
    }

    @Test
    public void getNullLocoNetMessage(){
        byte message[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        Z21Message msg = new Z21Message(message,17);
        Assert.assertNull("non-LocoNetTunnel LocoNet Message",msg.getLocoNetMessage());
    }

    @Test
    public void MonitorStringLocoNetMessage(){
        byte message[]={
           (byte)0xEF,(byte)0x0E,(byte)0x03,(byte)0x00,(byte)0x03,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00};
        jmri.jmrix.loconet.LocoNetMessage l = new jmri.jmrix.loconet.LocoNetMessage(message);
        msg = new Z21Message(l);
        Assert.assertEquals("Monitor String","LocoNet Tunnel Message: Write slot 3 information:\n\tLoco 3 (short) is Not Consisted, Free, operating in 28 SS mode, and is moving Forward at speed 0,\n\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n\tMaster supports DT200; Track Status: Off/Paused; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x00 0x00 (0).\n",msg.toMonitorString());
    }

    @Test
    public void MonitorStringLocoNetMessage2(){
        byte message[]={
           (byte)0xD0,(byte)0x20,(byte)0x04,
           (byte)0x7D,(byte)0x0A,(byte)0x7C};
        jmri.jmrix.loconet.LocoNetMessage l = new jmri.jmrix.loconet.LocoNetMessage(message);
        msg = new Z21Message(l);
        Assert.assertEquals("Monitor String","LocoNet Tunnel Message: Transponder address 10 (short) (or long address 16010) present at LR5 () (BDL16x Board ID 1 RX4 zone C or BXP88 Board ID 1 section 5 or the BXPA1 Board ID 5 section).\n",msg.toMonitorString());
    }

    @Test
    public void toMonitorStringLanRMBusGetDataRequest(){
        msg = Z21Message.getLanRMBusGetDataRequestMessage(0);
        Assert.assertEquals("Monitor String","Z21 RM Bus Data Request for group 0",msg.toMonitorString());
    }

    @Test
    public void toMonitorStringLanRMBusProgramModule(){
        msg = Z21Message.getLanRMBusProgramModuleMessage(0);
        Assert.assertEquals("Monitor String","Z21 RM Bus Program Module to Address 0",msg.toMonitorString());
    }


    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new Z21Message("0D 00 04 00 12 34 AB 3 19 6 B B1");
    }

    @After 
    public void tearDown() {
	m = msg = null;
        JUnitUtil.tearDown();
    }

}
