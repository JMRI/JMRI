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
public class Z21MessageTest {

    @Test
    public void testCtor() {
        Z21Message m = new Z21Message(3);
        Assert.assertEquals("length", 3, m.getNumDataElements());
        jmri.util.JUnitAppender.assertErrorMessage("invalid length in call to ctor");
    }

    // check opcode inclusion in message
    @Test
    public void testOpCode() {
        Z21Message m = new Z21Message(5);
        m.setOpCode(4);
        Assert.assertEquals("read=back op code", 4, m.getOpCode());
        //opcode is stored in two bytes, lsb first.
        Assert.assertEquals("stored op code", 0x0004, m.getElement(2) + (m.getElement(3) << 8));
    }

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        Z21Message m = new Z21Message("0D 00 04 00 12 34 AB 3 19 6 B B1");
        Assert.assertEquals("length", 12, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x0D, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x04, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", 0x12, m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", 0x34, m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", 0xAB, m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 0x03, m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", 0x19, m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", 0x06, m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", 0x0B, m.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", 0xB1, m.getElement(11) & 0xFF);
    }

    //Test some canned messages.
    @Test
    public void SerialNumberRequest(){
        Z21Message m = Z21Message.getSerialNumberRequestMessage();
        Assert.assertEquals("length", 4, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x04, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x10, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, m.getElement(3) & 0xFF);
    }

    @Test
    public void toMonitorStringSerialNumberRequest(){
        Z21Message m = Z21Message.getSerialNumberRequestMessage();
        Assert.assertEquals("Monitor String","Z21 Serial Number Request",m.toMonitorString());
    }

    @Test
    public void GetHardwareInfoRequest(){
        Z21Message m = Z21Message.getLanGetHardwareInfoRequestMessage();
        Assert.assertEquals("length", 4, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x04, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x1A, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, m.getElement(3) & 0xFF);
    }

    @Test
    public void toMonitorStringGetHardwareInfoRequest(){
        Z21Message m = Z21Message.getLanGetHardwareInfoRequestMessage();
        Assert.assertEquals("Monitor String","Z21 Version Request",m.toMonitorString());
    }

    @Test
    public void LanLogoffRequest(){
        Z21Message m = Z21Message.getLanLogoffRequestMessage();
        Assert.assertEquals("length", 4, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x04, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x30, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, m.getElement(3) & 0xFF);
        Assert.assertFalse("reply expected",m.replyExpected());
    }

    @Test
    public void toMonitorStringLanLogoffRequest(){
        Z21Message m = Z21Message.getLanLogoffRequestMessage();
        Assert.assertEquals("Monitor String","04 00 30 00",m.toMonitorString());
    }

    @Test
    public void GetBroadCastFlagsRequest(){
        Z21Message m = Z21Message.getLanGetBroadcastFlagsRequestMessage();
        Assert.assertEquals("length", 4, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x04, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x51, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, m.getElement(3) & 0xFF);
    }

    @Test
    public void toMonitorStringGetBroadCastFlagsRequest(){
        Z21Message m = Z21Message.getLanGetBroadcastFlagsRequestMessage();
        Assert.assertEquals("Monitor String","04 00 51 00",m.toMonitorString());
    }

    @Test
    public void SetBroadCastFlagsRequest(){
        Z21Message m = Z21Message.getLanSetBroadcastFlagsRequestMessage(0x01020304);
        Assert.assertEquals("length", 8, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x08, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x50, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, m.getElement(3) & 0xFF);
        Assert.assertEquals("5th byte", 0x04, m.getElement(4) & 0xFF);
        Assert.assertEquals("6st byte", 0x03, m.getElement(5) & 0xFF);
        Assert.assertEquals("7nd byte", 0x02, m.getElement(6) & 0xFF);
        Assert.assertEquals("8rd byte", 0x01, m.getElement(7) & 0xFF);
        Assert.assertFalse("reply expected",m.replyExpected());
    }

    @Test
    public void toMonitorStringSetBroadCastFlagsRequest(){
        Z21Message m = Z21Message.getLanSetBroadcastFlagsRequestMessage(0x01020304);
        Assert.assertEquals("Monitor String","08 00 50 00 04 03 02 01",m.toMonitorString());
    }

    @Test
    public void GetRailComDataRequest(){
        Z21Message m = Z21Message.getLanRailComGetDataRequestMessage();
        Assert.assertEquals("length", 4, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x04, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x89, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, m.getElement(3) & 0xFF);
    }

    @Test
    public void toMonitorStringRailComDataRequest(){
        Z21Message m = Z21Message.getLanRailComGetDataRequestMessage();
        Assert.assertEquals("Monitor String",Bundle.getMessage("Z21_RAILCOM_GETDATA"),m.toMonitorString());
    }

    @Test
    public void GetSystemStateDataChangedRequest(){
        Z21Message m = Z21Message.getLanSystemStateDataChangedRequestMessage();
        Assert.assertEquals("length", 4, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x04, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x85, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, m.getElement(3) & 0xFF);
    }

    @Test
    public void toMonitorStringSystemStateDataChangedRequest(){
        Z21Message m = Z21Message.getLanSystemStateDataChangedRequestMessage();
        Assert.assertEquals("Monitor String","04 00 85 00",m.toMonitorString());
    }

    @Test
    public void getLocoNetMessage(){
        byte msg[]={
           (byte)0xEF,(byte)0x0E,(byte)0x03,(byte)0x00,(byte)0x03,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00};
        jmri.jmrix.loconet.LocoNetMessage l = new jmri.jmrix.loconet.LocoNetMessage(msg);
        Z21Message m = new Z21Message(l);
        jmri.jmrix.loconet.LocoNetMessage x = m.getLocoNetMessage();
        Assert.assertEquals("0th byte", 0xEF, x.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x0E, x.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x03, x.getElement(2) & 0xFF);
        Assert.assertEquals("4nd byte", 0x03, x.getElement(4) & 0xFF);
    }

    @Test
    public void getNullLocoNetMessage(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        Z21Message m = new Z21Message(msg,17);
        Assert.assertNull("non-LocoNetTunnel LocoNet Message",m.getLocoNetMessage());
    }

    @Test
    public void MonitorStringLocoNetMessage(){
        byte msg[]={
           (byte)0xEF,(byte)0x0E,(byte)0x03,(byte)0x00,(byte)0x03,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00};
        jmri.jmrix.loconet.LocoNetMessage l = new jmri.jmrix.loconet.LocoNetMessage(msg);
        Z21Message m = new Z21Message(l);
        Assert.assertEquals("Monitor String","LocoNet Tunnel Message: Write slot 3 information:\n\tLoco 3 (short) is Not Consisted, Free, operating in 28 SS mode, and is moving Forward at speed 0,\n\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n\tMaster supports DT200; Track Status: Off/Paused; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x00 0x00 (0).\n",m.toMonitorString());
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After 
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
