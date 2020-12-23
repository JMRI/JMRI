package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * DCCppReplyTest.java
 * <p>
 * Test for the jmri.jmrix.dccpp.DCCppReply class
 *
 * @author Bob Jacobsen
 * @author Mark Underwood (C) 2015
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

        l = DCCppReply.parseDCCppReply("N1: 192.168.0.1 XYZ 123"); //should ignore undefined values
        Assert.assertTrue(l.isCommTypeReply());
        Assert.assertEquals('N', l.getOpCodeChar());
        Assert.assertEquals(1, l.getCommTypeInt());
        Assert.assertEquals("192.168.0.1", l.getCommTypeValueString());

    }

    // Test power replies
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

        l = DCCppReply.parseDCCppReply("p 1 MAIN XYZ 123"); //should ignore undefined values
        Assert.assertTrue(l.isNamedPowerReply());
        Assert.assertEquals('p', l.getOpCodeChar());
        Assert.assertEquals("MAIN", l.getPowerDistrictName());
        Assert.assertEquals("ON", l.getPowerDistrictStatus());

        l = DCCppReply.parseDCCppReply("p1 MAIN");
        Assert.assertTrue(l.isNamedPowerReply());
        Assert.assertEquals('p', l.getOpCodeChar());
        Assert.assertEquals("MAIN", l.getPowerDistrictName());
        Assert.assertEquals("ON", l.getPowerDistrictStatus());

        l = DCCppReply.parseDCCppReply("p1");
        Assert.assertTrue(l.isPowerReply());
        Assert.assertFalse(l.isNamedPowerReply());
        Assert.assertTrue(l.getPowerBool());
        l = DCCppReply.parseDCCppReply("p0");
        Assert.assertFalse(l.getPowerBool());
        l = DCCppReply.parseDCCppReply("p 0");
        Assert.assertFalse(l.getPowerBool());
        l = DCCppReply.parseDCCppReply("p 1");
        Assert.assertTrue(l.getPowerBool());
    }

    // Test named and unnamed current replies
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

        l = DCCppReply.parseDCCppReply("a   MAIN   410");
        Assert.assertTrue(l.isCurrentReply());
        Assert.assertTrue(l.isNamedCurrentReply());
        Assert.assertEquals('a', l.getOpCodeChar());
        Assert.assertEquals("410", l.getCurrentString());

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

        l = DCCppReply.parseDCCppReply("a 1023 512 XYZ 512"); //should ignore undefined values
        Assert.assertTrue(l.isCurrentReply());
        Assert.assertFalse(l.isNamedCurrentReply());
        Assert.assertEquals('a', l.getOpCodeChar());
        Assert.assertEquals("1023", l.getCurrentString());

        l = DCCppReply.parseDCCppReply("a PROG 1024 512 512 512"); //should ignore undefined values
        Assert.assertTrue(l.isCurrentReply());
        Assert.assertTrue(l.isNamedCurrentReply());
        Assert.assertEquals('a', l.getOpCodeChar());
        Assert.assertEquals("1024", l.getCurrentString());
    }

    @Test
    public void testStatusReplies() {
        DCCppReply r = DCCppReply.parseDCCppReply(
                "iDCC-EX V-3.0.0 / FireBoxMK1 / FIREBOX_MK1 / G-9db6d36");
        Assert.assertTrue(r.isStatusReply());
        Assert.assertTrue(r.matches(DCCppConstants.STATUS_REPLY_DCCEX_REGEX));
        Assert.assertEquals("3.0.0",   r.getVersion());
        Assert.assertEquals("9db6d36", r.getBuildString());
        Assert.assertEquals("DCC-EX",  r.getStationType());
        
        r = DCCppReply.parseDCCppReply(
                "iDCC++ BASE STATION FOR ARDUINO MEGA / ARDUINO MOTOR SHIELD: BUILD 23 Feb 2015 09:23:57");
        Assert.assertTrue(r.isStatusReply());
        Assert.assertTrue(r.matches(DCCppConstants.STATUS_REPLY_REGEX));
        Assert.assertEquals("0.0.0", r.getVersion());
        Assert.assertEquals("23 Feb 2015 09:23:57", r.getBuildString());
        Assert.assertEquals("DCC++ BASE STATION FOR ARDUINO MEGA / ARDUINO MOTOR SHIELD", r.getStationType());
        
        r = DCCppReply.parseDCCppReply(
                "iDCC++ BASE STATION FOR ARDUINO MEGA / ARDUINO MOTOR SHIELD: V-1.2.1+ / Dec 22 2020 20:59:52");
        Assert.assertTrue(r.isStatusReply());
        Assert.assertEquals("1.2.1", r.getVersion());
        Assert.assertEquals("Dec 22 2020 20:59:52", r.getBuildString());
        Assert.assertEquals("DCC++ BASE STATION FOR ARDUINO MEGA / ARDUINO MOTOR SHIELD", r.getStationType());
    }
    
    @Test
    public void testVariousReplies() {
        //Turnout replies
        DCCppReply r = DCCppReply.parseDCCppReply("H 23 24 2 0");
        Assert.assertTrue(r.isTurnoutDefReply());
        Assert.assertTrue(r.isTurnoutReply());
        Assert.assertEquals(23, r.getTurnoutDefNumInt());
        Assert.assertEquals(24, r.getTurnoutDefAddrInt());
        Assert.assertEquals(2, r.getTurnoutDefSubAddrInt());
        r = DCCppReply.parseDCCppReply("H 23 0");
        Assert.assertTrue(r.isTurnoutReply());
        Assert.assertFalse(r.isTurnoutDefReply());
        Assert.assertEquals("CLOSED", r.getTOStateString());
        Assert.assertEquals("23", r.getTOIDString());
        Assert.assertEquals(23, r.getTOIDInt());
        Assert.assertFalse(r.getTOIsThrown());
        Assert.assertTrue(r.getTOIsClosed());
        r = DCCppReply.parseDCCppReply("H 23 1");
        Assert.assertEquals("THROWN", r.getTOStateString());
        Assert.assertTrue(r.getTOIsThrown());
        Assert.assertFalse(r.getTOIsClosed());

        //max Num Slots
        r = DCCppReply.parseDCCppReply("# 50");
        Assert.assertTrue(r.isMaxNumSlotsReply());

        //Sensor replies
        r = DCCppReply.parseDCCppReply("Q 22 33 0");
        Assert.assertTrue(r.isSensorReply());
        Assert.assertTrue(r.isSensorDefReply());
        Assert.assertEquals("22", r.getSensorDefNumString());
        Assert.assertEquals(22, r.getSensorDefNumInt());
        Assert.assertEquals(33, r.getSensorDefPinInt());
        Assert.assertFalse(r.getSensorDefPullupBool());
        r = DCCppReply.parseDCCppReply("Q 22 33 1");
        Assert.assertTrue(r.getSensorDefPullupBool());
        r = DCCppReply.parseDCCppReply("Q 123");
        Assert.assertTrue(r.isSensorReply());
        Assert.assertFalse(r.isSensorDefReply());
        Assert.assertEquals(123, r.getSensorNumInt());
        Assert.assertEquals("Active", r.getSensorStateString());
        Assert.assertTrue(r.getSensorIsActive());
        r = DCCppReply.parseDCCppReply("q 124");
        Assert.assertTrue(r.isSensorReply());
        Assert.assertFalse(r.isSensorDefReply());
        Assert.assertEquals(124, r.getSensorNumInt());
        Assert.assertTrue(r.getSensorIsInactive());

        //Output replies
        r = DCCppReply.parseDCCppReply("Y 123 44 111 1");
        Assert.assertTrue(r.isOutputListReply());
        Assert.assertEquals("123", r.getOutputNumString());
        Assert.assertEquals(123, r.getOutputNumInt());
        Assert.assertEquals(44, r.getOutputListPinInt());
        Assert.assertEquals(111, r.getOutputListIFlagInt());
        Assert.assertEquals(1, r.getOutputListStateInt());

        //EEPROM reply
        r = DCCppReply.parseDCCppReply("e 12 34 56");
        Assert.assertTrue(r.isWriteEepromReply());
    }

    @Test
    public void testMonitorStringCurrentReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("a MAIN 0");
        Assert.assertEquals("Named Current Monitor string", "Current: 0 / 1024", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("a 41");
        Assert.assertEquals("Current Monitor string", "Current: 41 / 1024", l.toMonitorString());
    }

    @Test
    public void testMonitorStringThrottleSpeedReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("T 123 59 1");
        Assert.assertEquals("Monitor string", "Throttle Reply: Register: 123, Speed: 59, Direction: Forward", l.toMonitorString());
    }

    @Test
    public void testMonitorStringTurnoutReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("H 1234 0");
        Assert.assertEquals("Monitor string", "Turnout Reply: T/O Number: 1234, Direction: CLOSED", l.toMonitorString());
    }

    @Test
    public void testMonitorStringOutputPinReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("Y 1234 0");
        Assert.assertEquals("Monitor string", "Output Command Reply: Output Number: 1234, OutputState: LOW", l.toMonitorString());
    }

    @Test
    public void testMonitorStringSensorStatusReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("Q 1234");
        Assert.assertEquals("Monitor string", "Sensor Reply (Active): Sensor Number: 1234, State: ACTIVE", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("q 1234");
        Assert.assertEquals("Monitor string", "Sensor Reply (Inactive): Sensor Number: 1234, State: INACTIVE", l.toMonitorString());
    }

    @Test
    public void testMonitorStringCVWriteByteReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("r 1234|4321|5 123");
        Assert.assertEquals("Monitor string", "Program Reply: Callback Num: 1234, Callback Sub: 4321, CV: 5, Value: 123", l.toMonitorString());
    }

    @Test
    public void testMonitorStringBitWriteReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("r 1234|4321|5 3 1");
        Assert.assertEquals("Monitor string", "Program Bit Reply: Callback Num: 1234, Callback Sub: 4321, CV: 5, CV Bit: 3, Value: 1", l.toMonitorString());
    }

    @Test
    public void testMonitorStringPowerReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("p0");
        Assert.assertEquals("Monitor string", "Power Status: OFF", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("p1");
        Assert.assertEquals("Monitor string", "Power Status: ON", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("p 0");
        Assert.assertEquals("Monitor string", "Power Status: OFF", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("p 1");
        Assert.assertEquals("Monitor string", "Power Status: ON", l.toMonitorString());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new DCCppReply();
    }

    @AfterEach
    public void tearDown() {
        m = msg = null;
        JUnitUtil.tearDown();
    }

}
