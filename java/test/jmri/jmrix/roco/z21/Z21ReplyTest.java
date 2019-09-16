package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.roco.z21.Z21Reply class
 *
 * @author	Bob Jacobsen
 */
public class Z21ReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    private Z21Reply message = null;

    @Test
    public void prefixSkip() {
        message = new Z21Reply();
        Assert.assertEquals("prefix skip",0,message.skipPrefix(5));
    }

    // test the byte array  constructor.
    @Test
    public void testStringCtor() {
        byte msg[]={(byte)0x0D,(byte)0x00,(byte)0x04,(byte)0x00,(byte)0x12,(byte)0x34,(byte)0xAB,(byte)0x03,(byte)0x19,(byte)0x06,(byte)0x0B,(byte)0xB1};
        message = new Z21Reply(msg,12);
        Assert.assertEquals("length", 12, message.getNumDataElements());
        Assert.assertEquals("OpCode", 0x0004, message.getOpCode());
        Assert.assertEquals("0th byte", 0x0D, message.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, message.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x04, message.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, message.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", 0x12, message.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", 0x34, message.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", 0xAB, message.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 0x03, message.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", 0x19, message.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", 0x06, message.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", 0x0B, message.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", 0xB1, message.getElement(11) & 0xFF);
    }

    @Test
    public void getBCDElement() {
        byte msg[]={(byte)0x0D,(byte)0x00,(byte)0x04,(byte)0x00,(byte)0x12,(byte)0x34,(byte)0xAB,(byte)0x03,(byte)0x19,(byte)0x06,(byte)0x0B,(byte)0xB1};
        message = new Z21Reply(msg,12);
        Assert.assertEquals("4th byte BCD", Integer.valueOf(12), message.getElementBCD(4));
        Assert.assertEquals("5th byte BCD", Integer.valueOf(34), message.getElementBCD(5));
    }

    // Test XpressNet Tunnel related methods.
    @Test
    public void tunnelXPressNet(){
        byte msg[]={(byte)0x07,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x61,(byte)0x82,(byte)0xE3};
        message = new Z21Reply(msg,7);
        Assert.assertTrue("XpressNet Tunnel Message",message.isXPressNetTunnelMessage());
        byte msg1[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        message = new Z21Reply(msg1,17);
        Assert.assertFalse("Not XpressNet Tunnel Message",message.isXPressNetTunnelMessage());
    }

    @Test
    public void getXPressNetReply(){
        byte msg[]={(byte)0x07,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x61,(byte)0x82,(byte)0xE3};
        message = new Z21Reply(msg,7);
        jmri.jmrix.lenz.XNetReply x = message.getXNetReply();
        Assert.assertEquals("0th byte", 0x61, x.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x82, x.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0xE3, x.getElement(2) & 0xFF);
    }

    @Test
    public void getNullXPressNetReply(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        message = new Z21Reply(msg,17);
        Assert.assertNull("non-XNetTunnel XpressNet Reply",message.getXNetReply());
    }

    @Test
    public void MonitorStringXPressNetReply(){
        byte msg[]={(byte)0x07,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x61,(byte)0x82,(byte)0xE3};
        message = new Z21Reply(msg,7);
        Assert.assertEquals("Monitor String","XpressNet Tunnel Reply: 61 82 E3",message.toMonitorString());
    }

    @Test
    public void getXPressNetThrottleReply(){
        // this test comes from a user log, where the last byte in the 
        // Z21 message incorrectly became the first byte of the XpressNet Reply.
        byte msg[]={(byte)0x0E,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0xEF,(byte)0x00,(byte)0x03,(byte)0x04,(byte)0x80,(byte)0x10,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x78};
        message = new Z21Reply(msg,14);
        jmri.jmrix.lenz.XNetReply x = message.getXNetReply();
        Assert.assertEquals("0th byte", 0xEF, x.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, x.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x03, x.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x04, x.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", 0x80, x.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", 0x10, x.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", 0x00, x.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 0x00, x.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", 0x00, x.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", 0x78, x.getElement(9) & 0xFF);
    }

    @Test
    public void xPressNetThrottleReplyToMonitorString(){
        byte msg[]={(byte)0x0E,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0xEF,(byte)0x00,(byte)0x03,(byte)0x04,(byte)0x80,(byte)0x10,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x78};
        message = new Z21Reply(msg,14);
        Assert.assertEquals("Monitor String","XpressNet Tunnel Reply: Z21 Mobile decoder info reply for address 3: Forward,in 128 Speed Step Mode,Speed Step: 0. Address is Free for Operation. F0 On; F1 Off; F2 Off; F3 Off; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off;  F13 Off; F14 Off; F15 Off; F16 Off; F17 Off; F18 Off; F19 Off; F20 Off; F21 Off; F22 Off; F23 Off; F24 Off; F25 Off; F26 Off; F27 Off; F28 Off; ",message.toMonitorString());
    }


    //Test RailCom related methods.
    @Test
    public void railComReply(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        message = new Z21Reply(msg,17);
        Assert.assertTrue("RailCom Reply",message.isRailComDataChangedMessage());
        byte msg1[]={(byte)0x07,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x61,(byte)0x82,(byte)0xE3};
        message = new Z21Reply(msg1,7);
        Assert.assertFalse("Not RailCom Reply",message.isRailComDataChangedMessage());
    }

    @Test
    public void railComEntries(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        message = new Z21Reply(msg,17);
        Assert.assertEquals("RailCom Entries",1,message.getNumRailComDataEntries());
        byte msg1[]={(byte)0x07,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x61,(byte)0x82,(byte)0xE3};
        message = new Z21Reply(msg1,7);
        Assert.assertEquals("RailCom Entries",0,message.getNumRailComDataEntries());
    } 

    @Test
    public void railCom2Entries(){
        byte msg[]={(byte)0x1E,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x20,(byte)0x21,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        message = new Z21Reply(msg,30);
        Assert.assertEquals("RailCom Entries",2,message.getNumRailComDataEntries());
    } 

    @Test
    public void railComAddress(){
        byte msg[]={(byte)0x1E,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x20,(byte)0x21,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        message = new Z21Reply(msg,30);
        Assert.assertTrue("RailCom Address",(new jmri.DccLocoAddress(256,true)).equals(message.getRailComLocoAddress(0)));
        Assert.assertTrue("RailCom Address 2",(new jmri.DccLocoAddress(8480,true)).equals(message.getRailComLocoAddress(1)));
    }

    @Test
    public void railComRcvCount(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        message = new Z21Reply(msg,17);
        Assert.assertEquals("RailCom Rcv Count",1,message.getRailComRcvCount(0));
    }

    @Test
    public void railComErrCount(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x06,(byte)0x07,(byte)0x08};
        message = new Z21Reply(msg,17);
        Assert.assertEquals("RailCom Err Count",5,message.getRailComErrCount(0));
    }

    @Test
    public void railComSpeed(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        message = new Z21Reply(msg,17);
        Assert.assertEquals("RailCom Speed",6,message.getRailComSpeed(0));
    }

    @Test
    public void railComOptions(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        message = new Z21Reply(msg,17);
        Assert.assertEquals("RailCom Options",5,message.getRailComOptions(0));
    }

    @Test
    public void railComQos(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        message = new Z21Reply(msg,17);
        Assert.assertEquals("RailCom Qos",7,message.getRailComQos(0));
    }

    //Test System Data related methods.
    @Test
    public void systemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x84,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        message = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",message.isSystemDataChangedReply());
        byte msg1[]={(byte)0x07,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x61,(byte)0x82,(byte)0xE3};
        message = new Z21Reply(msg1,7);
        Assert.assertFalse("Not System Data Changed Reply",message.isSystemDataChangedReply());
    }

    @Test
    public void mainCurrentFromSystemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x84,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        message = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",message.isSystemDataChangedReply());
        Assert.assertEquals("Main Current",256,message.getSystemDataMainCurrent());
    }

    @Test
    public void progCurrentFromSystemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x84,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        message = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",message.isSystemDataChangedReply());
        Assert.assertEquals("Programming Track Current",0,message.getSystemDataProgCurrent());
    }

    @Test
    public void filteredMainCurrentFromSystemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x84,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        message = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",message.isSystemDataChangedReply());
        Assert.assertEquals("Filtered Main Current",256,message.getSystemDataFilteredMainCurrent());
    }

    @Test
    public void temperatureFromSystemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x84,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        message = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",message.isSystemDataChangedReply());
        Assert.assertEquals("Temperature",0,message.getSystemDataTemperature());
    }

    @Test
    public void supplyVoltageFromSystemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x84,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        message = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",message.isSystemDataChangedReply());
        Assert.assertEquals("Supply Voltage",1280,message.getSystemDataSupplyVoltage());
    }

    @Test
    public void vccVoltageFromSystemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x84,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        message = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",message.isSystemDataChangedReply());
        Assert.assertEquals("Internal Voltage",1798,message.getSystemDataVCCVoltage());
    }

    @Test
    public void getLocoNetReply(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0xA2,(byte)0x00,
           (byte)0xEF,(byte)0x0E,(byte)0x03,(byte)0x00,(byte)0x03,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00};
        message = new Z21Reply(msg,17);
        jmri.jmrix.loconet.LocoNetMessage x = message.getLocoNetMessage();
        Assert.assertEquals("0th byte", 0xEF, x.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x0E, x.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x03, x.getElement(2) & 0xFF);
        Assert.assertEquals("4nd byte", 0x03, x.getElement(4) & 0xFF);
    }

    @Test
    public void getNullLocoNetReply(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        message = new Z21Reply(msg,17);
        Assert.assertNull("non-LocoNetTunnel LocoNet Reply",message.getLocoNetMessage());
    }

    @Test
    public void MonitorStringLocoNetReply(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0xA2,(byte)0x00,
           (byte)0xEF,(byte)0x0E,(byte)0x03,(byte)0x00,(byte)0x03,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00};
        message = new Z21Reply(msg,17);
        Assert.assertEquals("Monitor String","LocoNet Tunnel Reply: Write slot 3 information:\n" +
"\tLoco 3 (short) is Not Consisted, Free, operating in 28 SS mode, and is moving Forward at speed 0,\n" +
"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n" +
"\tMaster supports DT200; Track Status: Off/Paused; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x00 0x00 (0).\n",message.toMonitorString());
    }

    @Test
    public void MonitorStringSerialNumberReply(){
        byte msg[]={(byte)0x08,(byte)0x00,(byte)0x10,(byte)0x00,
           (byte)0xAE,(byte)0xA7,(byte)0x01,(byte)0x00};
        message = new Z21Reply(msg,8);
        Assert.assertEquals("Z21 Serial Number Reply.  Serial Number: 108,462",message.toMonitorString());
    }

    @Test
    public void MonitorStringVersionReply(){
        byte msg[]={(byte)0x0C,(byte)0x00,(byte)0x1A,(byte)0x00,
           (byte)0x00,(byte)0x02,(byte)0x00,(byte)0x00,(byte)0x32,
           (byte)0x01,(byte)0x00,(byte)0x00};
        message = new Z21Reply(msg,12);
        Assert.assertEquals("Z21 Version Reply.  Hardware Version: 0x200 Software Version: 1.32",message.toMonitorString());
    }

    @Test
    public void MonitorStringSystemStateReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x84,(byte)0x00,
           (byte)0x56,(byte)0x00,(byte)0x03,(byte)0x00,(byte)0x5D,
           (byte)0x00,(byte)0x23,(byte)0x00,(byte)0x10,(byte)0x47,
           (byte)0xB3,(byte)0x44,(byte)0x00,(byte)0x00,(byte)0x04,
           (byte)0x00};
        message = new Z21Reply(msg,20);
        Assert.assertEquals("Z21 System State:\n\tmain track current 86mA\n\tprogramming track current 3mA\n\tFiltered Main Track current 93mA\n\tInternal Temperature 35C\n\tSupply Voltage 18,192mV\n\tInternal Voltage 17,587mV\n\tState 0\n\tExtended State 0",message.toMonitorString());
    }

    @Test
    public void MonitorStringRMFeedbackChangedReply(){
        byte msg[]={(byte)0x0F,(byte)0x00,(byte)0x80,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0xFF,(byte)0x00,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00};
        message = new Z21Reply(msg,15);
        Assert.assertEquals("RM Feedback Status for group 0" +
          "\n\tModule 1 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" +
          "\n\tModule 2 Contact 1 On;Contact 2 On;Contact 3 On;Contact 4 On;Contact 5 On;Contact 6 On;Contact 7 On;Contact 8 On" + 
          "\n\tModule 3 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" + 
          "\n\tModule 4 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" + 
          "\n\tModule 5 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" + 
          "\n\tModule 6 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" + 
          "\n\tModule 7 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" + 
          "\n\tModule 8 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" + 
          "\n\tModule 9 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" +
          "\n\tModule 10 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off",message.toMonitorString());

        byte msg2[]={(byte)0x0F,(byte)0x00,(byte)0x80,(byte)0x00,
           (byte)0x01,(byte)0x00,(byte)0xFF,(byte)0x00,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00};
        message = new Z21Reply(msg2,15);
        Assert.assertEquals("RM Feedback Status for group 1" +
          "\n\tModule 11 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" +
          "\n\tModule 12 Contact 1 On;Contact 2 On;Contact 3 On;Contact 4 On;Contact 5 On;Contact 6 On;Contact 7 On;Contact 8 On" + 
          "\n\tModule 13 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" + 
          "\n\tModule 14 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" + 
          "\n\tModule 15 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" + 
          "\n\tModule 16 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" + 
          "\n\tModule 17 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" + 
          "\n\tModule 18 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" + 
          "\n\tModule 19 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off" +
          "\n\tModule 20 Contact 1 Off;Contact 2 Off;Contact 3 Off;Contact 4 Off;Contact 5 Off;Contact 6 Off;Contact 7 Off;Contact 8 Off",message.toMonitorString());
    }

    @Test
    public void checkIsRMFeedbackChangedReply(){
        byte msg[]={(byte)0x0F,(byte)0x00,(byte)0x80,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0xFF,(byte)0x00,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00};
        message = new Z21Reply(msg,15);
        Assert.assertTrue("is RMBus Feedback",message.isRMBusDataChangedReply());
    }

   @Test
   public void testIsCanDetectorReply(){
       byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00};
       Z21Reply reply = new Z21Reply(msg,14);
       Assert.assertTrue("is Can Detector Message",reply.isCanDetectorMessage());
    }

   @Test
   public void testMonitorStringCanDetectorRailComReply(){
       byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00};
       Z21Reply reply = new Z21Reply(msg,14);
       Assert.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Occupancy Info Value1=1(S) direction unknown Value2=end of list",reply.toMonitorString());
    }

   @Test
   public void testMonitorStringCanDetectorStatusReplyFreeWithVolt(){
       byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00};
       Z21Reply reply = new Z21Reply(msg,14);
       Assert.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Free, with voltage Value2=",reply.toMonitorString());
    }

   @Test
   public void testMonitorStringCanDetectorStatusReplyFreeWithoutVolt(){
       byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
       Z21Reply reply = new Z21Reply(msg,14);
       Assert.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Free, without voltage Value2=",reply.toMonitorString());
    }

   @Test
   public void testMonitorStringCanDetectorStatusReplyBusyWithVolt(){
       byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0x00};
       Z21Reply reply = new Z21Reply(msg,14);
       Assert.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Busy, with voltage Value2=",reply.toMonitorString());
    }

   @Test
   public void testMonitorStringCanDetectorStatusReplyBusyWithoutVolt(){
       byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x10,(byte)0x00,(byte)0x00};
       Z21Reply reply = new Z21Reply(msg,14);
       Assert.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Busy, without voltage Value2=",reply.toMonitorString());
    }

   @Test
   public void testMonitorStringCanDetectorStatusReplyBusyOverload1(){
       byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x12,(byte)0x00,(byte)0x00};
       Z21Reply reply = new Z21Reply(msg,14);
       Assert.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Busy, Overload 1 Value2=",reply.toMonitorString());
    }

   @Test
   public void testMonitorStringCanDetectorStatusReplyBusyOverload2(){
       byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0x12,(byte)0x00,(byte)0x00};
       Z21Reply reply = new Z21Reply(msg,14);
       Assert.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Busy, Overload 2 Value2=",reply.toMonitorString());
    }

   @Test
   public void testMonitorStringCanDetectorStatusReplyBusyOverload3(){
       byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x03,(byte)0x12,(byte)0x00,(byte)0x00};
       Z21Reply reply = new Z21Reply(msg,14);
       Assert.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Busy, Overload 3 Value2=",reply.toMonitorString());
    }

    @Test
    public void testMonitorStringZ21BroadcastFlagsReply(){
        byte msg[]={(byte)0x08,(byte)0x00,(byte)0x51,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00};
        Z21Reply reply = new Z21Reply(msg,8);
        Assert.assertEquals("Z21 Broadcast flags 43725",reply.toMonitorString());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = message = new Z21Reply();
    }

    @After
    public void tearDown() {
	m = message = null;
        JUnitUtil.tearDown();
    }

}
