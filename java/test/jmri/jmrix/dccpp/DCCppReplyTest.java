package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 * DCCppReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppReply class
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood (C) 2015
 */
public class DCCppReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    private DCCppReply msg = null;

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        msg = DCCppReply.parseDCCppReply("H 23 1");
        Assert.assertEquals("length", 6, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'H', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', msg.getElement(5) & 0xFF);
    }

    // check is direct mode response
    @Test
    public void testIsDirectModeResponse() {
        // CV 1 in direct mode.
        DCCppReply r = DCCppReply.parseDCCppReply("r 1234|87|23 12");
        Assert.assertTrue(r.isProgramReply());
        r = DCCppReply.parseDCCppReply("r 1234|66|23 4 1");
        Assert.assertTrue(r.isProgramBitReply());
        r = DCCppReply.parseDCCppReply("r 1234|82|23 4");
        Assert.assertTrue(r.isProgramReply());
    }

    // check get service mode CV Number response code.
    @Test
    @NotApplicable("Method under test is not implemented for DCC++")
    public void testGetServiceModeCVNumber() {
    }

    // check get service mode CV Value response code.
    @Test
    @NotApplicable("Method under test is not implemented for DCC++")
    public void testGetServiceModeCVValue() {
    }
    
    // Test Comm Type Reply
    @Test
    public void testCommTypeReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("N0: SERIAL");
        Assert.assertTrue(l.isCommTypeReply());
        Assert.assertEquals('N', l.getOpCodeChar());
        Assert.assertEquals(0, l.getCommTypeInt());
        Assert.assertEquals("SERIAL", l.getCommTypeValueString());
        
        l = DCCppReply.parseDCCppReply("N1: 192.168.0.1");
        Assert.assertTrue(l.isCommTypeReply());
        Assert.assertEquals('N', l.getOpCodeChar());
        Assert.assertEquals(1, l.getCommTypeInt());
        Assert.assertEquals("192.168.0.1", l.getCommTypeValueString());
        
    }

    // Test named power districts
    @Test
    public void testNamedPowerDistrictReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("p 0 MAIN");
        Assert.assertTrue(l.isNamedPowerReply());
        Assert.assertEquals('p', l.getOpCodeChar());
        Assert.assertEquals("MAIN", l.getPowerDistrictName());
        Assert.assertEquals("OFF", l.getPowerDistrictStatus());

        l = DCCppReply.parseDCCppReply("p 1 MAIN");
        Assert.assertTrue(l.isNamedPowerReply());
        Assert.assertEquals('p', l.getOpCodeChar());
        Assert.assertEquals("MAIN", l.getPowerDistrictName());
        Assert.assertEquals("ON", l.getPowerDistrictStatus());

        l = DCCppReply.parseDCCppReply("p 2 MAIN");
        Assert.assertTrue(l.isNamedPowerReply());
        Assert.assertEquals('p', l.getOpCodeChar());
        Assert.assertEquals("MAIN", l.getPowerDistrictName());
        Assert.assertEquals("OVERLOAD", l.getPowerDistrictStatus());
    }

    // Test named power districts
    @Test
    public void testNamedCurrentReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("a MAIN 0");
        Assert.assertTrue(l.isCurrentReply());
        Assert.assertTrue(l.isNamedCurrentReply());
        Assert.assertEquals('a', l.getOpCodeChar());
        Assert.assertEquals("0", l.getCurrentString());

        l = DCCppReply.parseDCCppReply("a MAIN 100");
        Assert.assertTrue(l.isCurrentReply());
        Assert.assertTrue(l.isNamedCurrentReply());
        Assert.assertEquals('a', l.getOpCodeChar());
        Assert.assertEquals("100", l.getCurrentString());

        l = DCCppReply.parseDCCppReply("aMAIN0");
        Assert.assertTrue(l.isCurrentReply());
        Assert.assertTrue(l.isNamedCurrentReply());
        Assert.assertEquals('a', l.getOpCodeChar());
        Assert.assertEquals("0", l.getCurrentString());

        l = DCCppReply.parseDCCppReply("aMAIN41");
        Assert.assertTrue(l.isCurrentReply());
        Assert.assertTrue(l.isNamedCurrentReply());
        Assert.assertEquals('a', l.getOpCodeChar());
        Assert.assertEquals("41", l.getCurrentString());

        l = DCCppReply.parseDCCppReply("a41");
        Assert.assertTrue(l.isCurrentReply());
        Assert.assertFalse(l.isNamedCurrentReply());
        Assert.assertEquals('a', l.getOpCodeChar());
        Assert.assertEquals("41", l.getCurrentString());

        l = DCCppReply.parseDCCppReply("a 41");
        Assert.assertTrue(l.isCurrentReply());
        Assert.assertFalse(l.isNamedCurrentReply());
        Assert.assertEquals('a', l.getOpCodeChar());
        Assert.assertEquals("41", l.getCurrentString());
    }

    @Test
    public void testMonitorStringCurrentReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("a MAIN 0");
        Assert.assertEquals("Named Current Monitor string","Current: 0 / 1024",l.toMonitorString());
        l = DCCppReply.parseDCCppReply("a 41");
        Assert.assertEquals("Current Monitor string","Current: 41 / 1024",l.toMonitorString());
    }

    @Test
    public void testMonitorStringThrottleSpeedReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("T 123 59 1");
        Assert.assertEquals("Monitor string","Throttle Reply: \n\tRegister: 123\n\tSpeed: 59\n\tDirection: Forward",l.toMonitorString());
    }

    @Test
    public void testMonitorStringTurnoutReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("H 1234 0");
        Assert.assertEquals("Monitor string","Turnout Reply: \n\tT/O Number: 1234\n\tDirection: CLOSED",l.toMonitorString());
    }

    @Test
    public void testMonitorStringOutputPinReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("Y 1234 0");
        Assert.assertEquals("Monitor string","Output Command Reply: \n\tOutput Number: 1234\n\tOutputState: LOW",l.toMonitorString());
    }

    @Test
    public void testMonitorStringSensorStatusReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("Q 1234");
        Assert.assertEquals("Monitor string","Sensor Reply (Active): \n\tSensor Number: 1234\n\tState: ACTIVE",l.toMonitorString());
        l = DCCppReply.parseDCCppReply("q 1234");
        Assert.assertEquals("Monitor string","Sensor Reply (Inactive): \n\tSensor Number: 1234\n\tState: INACTIVE",l.toMonitorString());
    }

    @Test
    public void testMonitorStringCVWriteByteReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("r 1234|4321|5 123");
        Assert.assertEquals("Monitor string","Program Reply: \n\tCallback Num: 1234\n\tCallback Sub: 4321\n\tCV: 5\n\tValue: 123",l.toMonitorString());
    }

    @Test
    public void testMonitorStringBitWriteReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("r 1234|4321|5 3 1");
        Assert.assertEquals("Monitor string","Program Bit Reply: \n\tCallback Num: 1234\n\tCallback Sub: 4321\n\tCV: 5\n\tCV Bit: 3\n\tValue: 1",l.toMonitorString());
    }

    @Test
    public void testMonitorStringPowerReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("p0");
        Assert.assertEquals("Monitor string","Power Status: OFF",l.toMonitorString());
        l = DCCppReply.parseDCCppReply("p1");
        Assert.assertEquals("Monitor string","Power Status: ON",l.toMonitorString());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new DCCppReply();
    }

    @After
    public void tearDown() {
	m = msg = null;
        JUnitUtil.tearDown();
    }

}
