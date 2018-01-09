package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.roco.z21.Z21Reply class
 *
 * @author	Bob Jacobsen
 */
public class Z21ReplyTest {

    @Test
    public void testCtor() {
        Z21Reply m = new Z21Reply();
        Assert.assertNotNull(m);
    }

    @Test
    public void prefixSkip() {
        Z21Reply m = new Z21Reply();
        Assert.assertEquals("prefix skip",0,m.skipPrefix(5));
    }

    // test the byte array  constructor.
    @Test
    public void testStringCtor() {
        byte msg[]={(byte)0x0D,(byte)0x00,(byte)0x04,(byte)0x00,(byte)0x12,(byte)0x34,(byte)0xAB,(byte)0x03,(byte)0x19,(byte)0x06,(byte)0x0B,(byte)0xB1};
        Z21Reply m = new Z21Reply(msg,12);
        Assert.assertEquals("length", 12, m.getNumDataElements());
        Assert.assertEquals("OpCode", 0x0004, m.getOpCode());
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

    @Test
    public void getBCDElement() {
        byte msg[]={(byte)0x0D,(byte)0x00,(byte)0x04,(byte)0x00,(byte)0x12,(byte)0x34,(byte)0xAB,(byte)0x03,(byte)0x19,(byte)0x06,(byte)0x0B,(byte)0xB1};
        Z21Reply m = new Z21Reply(msg,12);
        Assert.assertEquals("4th byte BCD", Integer.valueOf(12), m.getElementBCD(4));
        Assert.assertEquals("5th byte BCD", Integer.valueOf(34), m.getElementBCD(5));
    }

    // Test XpressNet Tunnel related methods.
    @Test
    public void tunnelXPressNet(){
        byte msg[]={(byte)0x07,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x61,(byte)0x82,(byte)0xE3};
        Z21Reply m = new Z21Reply(msg,7);
        Assert.assertTrue("XpressNet Tunnel Message",m.isXPressNetTunnelMessage());
        byte msg1[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        m = new Z21Reply(msg1,17);
        Assert.assertFalse("Not XpressNet Tunnel Message",m.isXPressNetTunnelMessage());
    }

    @Test
    public void getXPressNetReply(){
        byte msg[]={(byte)0x07,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x61,(byte)0x82,(byte)0xE3};
        Z21Reply m = new Z21Reply(msg,7);
        jmri.jmrix.lenz.XNetReply x = m.getXNetReply();
        Assert.assertEquals("0th byte", 0x61, x.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x82, x.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0xE3, x.getElement(2) & 0xFF);
    }

    @Test
    public void getNullXPressNetReply(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        Z21Reply m = new Z21Reply(msg,17);
        Assert.assertNull("non-XNetTunnel XpressNet Reply",m.getXNetReply());
    }

    @Test
    public void MonitorStringXPressNetReply(){
        byte msg[]={(byte)0x07,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x61,(byte)0x82,(byte)0xE3};
        Z21Reply m = new Z21Reply(msg,7);
        Assert.assertEquals("Monitor String","XpressNet Tunnel Reply: 61 82 E3",m.toMonitorString());
    }

    @Test
    public void getXPressNetThrottleReply(){
        // this test comes from a user log, where the last byte in the 
        // Z21 message incorrectly became the first byte of the XpressNet Reply.
        byte msg[]={(byte)0x0E,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0xEF,(byte)0x00,(byte)0x03,(byte)0x04,(byte)0x80,(byte)0x10,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x78};
        Z21Reply m = new Z21Reply(msg,14);
        jmri.jmrix.lenz.XNetReply x = m.getXNetReply();
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

    //Test RailCom related methods.
    @Test
    public void railComReply(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        Z21Reply m = new Z21Reply(msg,17);
        Assert.assertTrue("RailCom Reply",m.isRailComDataChangedMessage());
        byte msg1[]={(byte)0x07,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x61,(byte)0x82,(byte)0xE3};
        m = new Z21Reply(msg1,7);
        Assert.assertFalse("Not RailCom Reply",m.isRailComDataChangedMessage());
    }

    @Test
    public void railComEntries(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        Z21Reply m = new Z21Reply(msg,17);
        Assert.assertEquals("RailCom Entries",1,m.getNumRailComDataEntries());
        byte msg1[]={(byte)0x07,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x61,(byte)0x82,(byte)0xE3};
        m = new Z21Reply(msg1,7);
        Assert.assertEquals("RailCom Entries",0,m.getNumRailComDataEntries());
    } 

    @Test
    public void railCom2Entries(){
        byte msg[]={(byte)0x1E,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x20,(byte)0x21,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        Z21Reply m = new Z21Reply(msg,30);
        Assert.assertEquals("RailCom Entries",2,m.getNumRailComDataEntries());
    } 

    @Test
    public void railComAddress(){
        byte msg[]={(byte)0x1E,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x20,(byte)0x21,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        Z21Reply m = new Z21Reply(msg,30);
        Assert.assertTrue("RailCom Address",(new jmri.DccLocoAddress(256,true)).equals(m.getRailComLocoAddress(0)));
        Assert.assertTrue("RailCom Address 2",(new jmri.DccLocoAddress(8480,true)).equals(m.getRailComLocoAddress(1)));
    }

    @Test
    public void railComRcvCount(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        Z21Reply m = new Z21Reply(msg,17);
        Assert.assertEquals("RailCom Rcv Count",1,m.getRailComRcvCount(0));
    }

    @Test
    public void railComErrCount(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x06,(byte)0x07,(byte)0x08};
        Z21Reply m = new Z21Reply(msg,17);
        Assert.assertEquals("RailCom Err Count",5,m.getRailComErrCount(0));
    }

    @Test
    public void railComSpeed(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        Z21Reply m = new Z21Reply(msg,17);
        Assert.assertEquals("RailCom Speed",6,m.getRailComSpeed(0));
    }

    @Test
    public void railComOptions(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        Z21Reply m = new Z21Reply(msg,17);
        Assert.assertEquals("RailCom Options",7,m.getRailComOptions(0));
    }

    @Test
    public void railComTemp(){
        byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
        Z21Reply m = new Z21Reply(msg,17);
        Assert.assertEquals("RailCom Temp",8,m.getRailComTemp(0));
    }

    //Test System Data related methods.
    @Test
    public void systemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x85,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        Z21Reply m = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",m.isSystemDataChangedReply());
        byte msg1[]={(byte)0x07,(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x61,(byte)0x82,(byte)0xE3};
        m = new Z21Reply(msg1,7);
        Assert.assertFalse("Not System Data Changed Reply",m.isSystemDataChangedReply());
    }

    @Test
    public void mainCurrentFromSystemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x85,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        Z21Reply m = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",m.isSystemDataChangedReply());
        Assert.assertEquals("Main Current",256,m.getSystemDataMainCurrent());
    }

    @Test
    public void progCurrentFromSystemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x85,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        Z21Reply m = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",m.isSystemDataChangedReply());
        Assert.assertEquals("Programming Track Current",0,m.getSystemDataProgCurrent());
    }

    @Test
    public void filteredMainCurrentFromSystemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x85,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        Z21Reply m = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",m.isSystemDataChangedReply());
        Assert.assertEquals("Filtered Main Current",256,m.getSystemDataFilteredMainCurrent());
    }

    @Test
    public void temperatureFromSystemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x85,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        Z21Reply m = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",m.isSystemDataChangedReply());
        Assert.assertEquals("Temperature",0,m.getSystemDataTemperature());
    }

    @Test
    public void supplyVoltageFromSystemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x85,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        Z21Reply m = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",m.isSystemDataChangedReply());
        Assert.assertEquals("Supply Voltage",1280,m.getSystemDataSupplyVoltage());
    }

    @Test
    public void vccVoltageFromSystemDataChangedReply(){
        byte msg[]={(byte)0x14,(byte)0x00,(byte)0x85,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00};
        Z21Reply m = new Z21Reply(msg,20);
        Assert.assertTrue("System Data Changed Reply",m.isSystemDataChangedReply());
        Assert.assertEquals("Main Current",1798,m.getSystemDataVCCVoltage());
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
