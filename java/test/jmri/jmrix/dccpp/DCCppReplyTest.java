package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import jmri.util.JUnitAppender;
import java.util.LinkedHashMap;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * DCCppReplyTest.java
 * <p>
 * Test for the jmri.jmrix.dccpp.DCCppReply class
 *
 * @author Bob Jacobsen
 * @author Mark Underwood (C) 2015
 * @author mstevetodd (C) 2021
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

    // Test Meter replies
    @Test
    public void testMeterReply() {
        DCCppReply r = DCCppReply.parseDCCppReply("c MAINCurrent 1.7 C NoPrefix 0.0 100.0 0.1 80");
        Assert.assertTrue(r.isMeterReply());
        Assert.assertFalse(r.isCurrentReply());
        Assert.assertFalse(r.isNamedCurrentReply());
        Assert.assertEquals("MAINCurrent", r.getMeterName());
        Assert.assertEquals(1.7,   r.getMeterValue(), 0.00001);
        Assert.assertEquals(jmri.Meter.Unit.NoPrefix, r.getMeterUnit());
        Assert.assertEquals(0.0,   r.getMeterMinValue(),   0.00001);
        Assert.assertEquals(100.0, r.getMeterMaxValue(),   0.00001);
        Assert.assertEquals(0.1,   r.getMeterResolution(), 0.00001);
        Assert.assertEquals(80.0,  r.getMeterWarnValue(),  0.00001);
        Assert.assertFalse(r.isMeterTypeVolt());
        Assert.assertTrue(r.isMeterTypeCurrent());

        r = DCCppReply.parseDCCppReply("c CurrentMAIN -5 C Milli 0 4996 1 4996");
        Assert.assertTrue(r.isMeterReply());
        Assert.assertFalse(r.isCurrentReply());
        Assert.assertFalse(r.isNamedCurrentReply());
        Assert.assertEquals("CurrentMAIN", r.getMeterName());
        Assert.assertEquals(-5,   r.getMeterValue(), 0.00001);
        Assert.assertEquals(jmri.Meter.Unit.Milli, r.getMeterUnit());
        Assert.assertEquals(0.0,   r.getMeterMinValue(),   0.00001);
        Assert.assertEquals(4996.0, r.getMeterMaxValue(),   0.00001);
        Assert.assertEquals(1.0,   r.getMeterResolution(), 0.00001);
        Assert.assertEquals(4996.0,  r.getMeterWarnValue(),  0.00001);
        Assert.assertFalse(r.isMeterTypeVolt());
        Assert.assertTrue(r.isMeterTypeCurrent());

        r = DCCppReply.parseDCCppReply("c PROGVolts 18.2 V Milli 9.0 24.0 0.1 22.0");
        Assert.assertEquals("PROGVolts", r.getMeterName());
        Assert.assertEquals(18.2,  r.getMeterValue(), 0.00001);
        Assert.assertEquals(jmri.Meter.Unit.Milli, r.getMeterUnit());
        Assert.assertEquals(9.0,   r.getMeterMinValue(),   0.00001);
        Assert.assertEquals(24.0,  r.getMeterMaxValue(),   0.00001);
        Assert.assertEquals(0.1,   r.getMeterResolution(), 0.00001);
        Assert.assertEquals(22.0,  r.getMeterWarnValue(),  0.00001);
        Assert.assertTrue(r.isMeterTypeVolt());
        Assert.assertFalse(r.isMeterTypeCurrent());
        
        r = DCCppReply.parseDCCppReply("c testmeter99.99 12.34 C NoPrefix 0 99.99 0.01 77.77");
        Assert.assertTrue(r.isMeterReply());
        Assert.assertEquals("testmeter99.99", r.getMeterName());
        Assert.assertEquals(12.34,  r.getMeterValue(), 0.00001);
        Assert.assertEquals(jmri.Meter.Unit.NoPrefix, r.getMeterUnit());
        Assert.assertEquals(0.0,   r.getMeterMinValue(),   0.00001);
        Assert.assertEquals(99.99, r.getMeterMaxValue(),   0.00001);
        Assert.assertEquals(0.01,  r.getMeterResolution(), 0.00001);
        Assert.assertEquals(77.77, r.getMeterWarnValue(),  0.00001);
        Assert.assertFalse(r.isMeterTypeVolt());
        Assert.assertTrue(r.isMeterTypeCurrent());

        r = DCCppReply.parseDCCppReply("c CurrentMAIN 19701 C Milli 0 -31744 1 -31744"); //sometimes returns negatives
        Assert.assertTrue(r.isMeterReply());
        Assert.assertFalse(r.isCurrentReply());
        Assert.assertFalse(r.isNamedCurrentReply());
        Assert.assertEquals("CurrentMAIN", r.getMeterName());
        Assert.assertEquals(19701.0,  r.getMeterValue(), 0.00001);
        Assert.assertEquals(jmri.Meter.Unit.Milli, r.getMeterUnit());
        Assert.assertEquals(0.0,      r.getMeterMinValue(),   0.00001);
        Assert.assertEquals(-31744.0, r.getMeterMaxValue(),   0.00001);
        Assert.assertEquals(1.0,      r.getMeterResolution(), 0.00001);
        Assert.assertEquals(-31744.0, r.getMeterWarnValue(),  0.00001);
        Assert.assertFalse(r.isMeterTypeVolt());
        Assert.assertTrue(r.isMeterTypeCurrent());
        
        r = DCCppReply.parseDCCppReply("c BadMeterType 0.3 X NoPrefix 0.0 5.0 0.01 5.0"); //bad meter type 'X' passed
        Assert.assertTrue( r.isMeterReply());
        Assert.assertFalse(r.isMeterTypeCurrent());
        JUnitAppender.assertWarnMessageStartingWith("Meter Type 'X' is not valid type in message 'c BadMeterType 0.3 X NoPrefix 0.0 5.0 0.01 5.0'");
        
        Assert.assertFalse(r.isMeterTypeVolt());
        JUnitAppender.assertWarnMessageStartingWith("Meter Type 'X' is not valid type in message 'c BadMeterType 0.3 X NoPrefix 0.0 5.0 0.01 5.0'");
        
        Assert.assertEquals("", r.getMeterType()); //invalid meter types returned as empty string
        JUnitAppender.assertWarnMessageStartingWith("Meter Type 'X' is not valid type in message 'c BadMeterType 0.3 X NoPrefix 0.0 5.0 0.01 5.0'");
        
        Assert.assertEquals(jmri.Meter.Unit.NoPrefix, r.getMeterUnit());

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
                "iDCC-EX V-5.4.4 / ESP32 / EXCSB1 G-c389fe9");
        Assert.assertTrue(r.isStatusReply());
        Assert.assertTrue(r.matches(DCCppConstants.STATUS_REPLY_DCCEX_REGEX));
        Assert.assertEquals("5.4.4",   r.getVersion());
        Assert.assertEquals("c389fe9", r.getBuildString());
        Assert.assertEquals("DCC-EX",  r.getStationType());
        
        r = DCCppReply.parseDCCppReply(
                "iDCC-EX V-5.0.0 / UNO / IoTT WiThServer 1.0.4 / STANDARD_MOTOR_SHIELD G-3bddf4d");
        Assert.assertTrue(r.isStatusReply());
        Assert.assertTrue(r.matches(DCCppConstants.STATUS_REPLY_DCCEX_REGEX));
        Assert.assertEquals("5.0.0",   r.getVersion());
        Assert.assertEquals("3bddf4d", r.getBuildString());
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

        //RosterID replies
        r = DCCppReply.parseDCCppReply("jR 1 2 3 4");
        Assert.assertTrue(r.isRosterIDsReply());
        r = DCCppReply.parseDCCppReply("jR 123 \"desc\" \"fkeys go here\"");
        Assert.assertTrue(r.isRosterIDReply());

        //AutomationID replies
        r = DCCppReply.parseDCCppReply("jA 4 3 2 1");
        Assert.assertTrue(r.isAutomationIDsReply());
        r = DCCppReply.parseDCCppReply("jA 456 R \"description\"");
        Assert.assertTrue(r.isAutomationIDReply());

        //max Num Slots
        r = DCCppReply.parseDCCppReply("# 50");
        Assert.assertTrue(r.isMaxNumSlotsReply());

        //DIAG message
        r = DCCppReply.parseDCCppReply("* this is a test diagnostic message 12345 *");
        Assert.assertTrue(r.isDiagReply());
        r = DCCppReply.parseDCCppReply("* this is not, missing trailing asterisk ");
        Assert.assertFalse(r.isDiagReply());

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
        Assert.assertTrue(r.isOutputDefReply());
        Assert.assertEquals("123", r.getOutputNumString());
        Assert.assertEquals(123, r.getOutputNumInt());
        Assert.assertEquals(44, r.getOutputListPinInt());
        Assert.assertEquals(111, r.getOutputListIFlagInt());
        Assert.assertEquals(1, r.getOutputListStateInt());

        //EEPROM reply
        r = DCCppReply.parseDCCppReply("e 12 34 56");
        Assert.assertTrue(r.isWriteEepromReply());

        //Throttle Command replies
        r = DCCppReply.parseDCCppReply("jT 123 456 789");
        Assert.assertTrue(r.isTurnoutIDsReply());
        Assert.assertFalse(r.isTurnoutIDReply());
        Assert.assertEquals(789, (int) r.getTurnoutIDList().get(2));
        Assert.assertEquals(3, r.getTurnoutIDList().size());
        Assert.assertEquals("Monitor string", "Turnout IDs:[123, 456, 789]", r.toMonitorString());
        r = DCCppReply.parseDCCppReply("jT 123 C \"turnout description\"");
        Assert.assertTrue(r.isTurnoutIDReply());
        Assert.assertFalse(r.isTurnoutIDsReply());
        Assert.assertEquals(123, r.getTOIDInt());
        Assert.assertEquals("C", r.getTurnoutStateString());
        Assert.assertEquals("turnout description", r.getTurnoutDescString());
        Assert.assertEquals("Monitor string", "Turnout ID:123 State:C Desc:'turnout description'", r.toMonitorString());
        r = DCCppReply.parseDCCppReply("jT 456 T \"\"");
        Assert.assertEquals("T", r.getTurnoutStateString());
        Assert.assertEquals("", r.getTurnoutDescString());
        Assert.assertEquals("Monitor string", "Turnout ID:456 State:T Desc:''", r.toMonitorString());
        r = DCCppReply.parseDCCppReply("jT");
        Assert.assertTrue(r.isTurnoutIDsReply());
        Assert.assertEquals(0, r.getTurnoutIDList().size());
        Assert.assertEquals("Monitor string", "Turnout IDs:[]", r.toMonitorString());
        r = DCCppReply.parseDCCppReply("jR");
        Assert.assertTrue(r.isRosterIDsReply());
        Assert.assertEquals(0, r.getRosterIDList().size());
        Assert.assertEquals("Monitor string", "RosterIDs:[]", r.toMonitorString());
        r = DCCppReply.parseDCCppReply("jR 123 456 789");
        Assert.assertTrue(r.isRosterIDsReply());
        Assert.assertFalse(r.isRosterIDReply());
        Assert.assertEquals(789, (int) r.getRosterIDList().get(2));
        Assert.assertEquals(3, r.getRosterIDList().size());
        Assert.assertEquals("Monitor string", "RosterIDs:[123, 456, 789]", r.toMonitorString());
        r = DCCppReply.parseDCCppReply("jA");
        Assert.assertTrue(r.isAutomationIDsReply());
        Assert.assertEquals(0, r.getAutomationIDList().size());
        Assert.assertEquals("Monitor string", "AutomationIDs:[]", r.toMonitorString());
        r = DCCppReply.parseDCCppReply("jA 123 456 789");
        Assert.assertTrue(r.isAutomationIDsReply());
        Assert.assertFalse(r.isAutomationIDReply());
        Assert.assertEquals(789, (int) r.getAutomationIDList().get(2));
        Assert.assertEquals(3, r.getAutomationIDList().size());
        Assert.assertEquals("Monitor string", "AutomationIDs:[123, 456, 789]", r.toMonitorString());
        r = DCCppReply.parseDCCppReply("jC 222 4"); //time and rate
        Assert.assertTrue(r.isClockReply());
        Assert.assertEquals(222, r.getClockMinutesInt());
        Assert.assertEquals(4,   r.getClockRateInt());
        Assert.assertEquals("Monitor string", "FastClock Reply: 03:42, Rate:4", r.toMonitorString());
        r = DCCppReply.parseDCCppReply("jC 333"); //just time
        Assert.assertTrue(r.isClockReply());
        Assert.assertEquals(333, r.getClockMinutesInt());
        Assert.assertEquals("Monitor string", "FastClock Reply: 05:33", r.toMonitorString());
        //verify that bad syntax fails
        r = DCCppReply.parseDCCppReply("jT 123 456 789 xx");        
        Assert.assertFalse(r.isTurnoutIDsReply());
        r = DCCppReply.parseDCCppReply("jT 123 X \"turnout description\"");
        Assert.assertFalse(r.isTurnoutIDReply());
        r = DCCppReply.parseDCCppReply("jR 123 456 789 notint");        
        Assert.assertFalse(r.isRosterIDsReply());
        r = DCCppReply.parseDCCppReply("jR 123 noquotes \"\\F1\\F2\\F3\\\"");
        Assert.assertFalse(r.isRosterIDReply());
        r = DCCppReply.parseDCCppReply("jA 123 456 789 notint");        
        Assert.assertFalse(r.isAutomationIDsReply());
        r = DCCppReply.parseDCCppReply("jA 123 toolong \"gooddescription\"");
        Assert.assertFalse(r.isAutomationIDReply());
        r = DCCppReply.parseDCCppReply("jC 222 4 xx"); //time and rate
        Assert.assertFalse(r.isClockReply());
        r = DCCppReply.parseDCCppReply("jC x222 4 xx"); //time and rate
        Assert.assertFalse(r.isClockReply());
        
        //TrackManager
        r = DCCppReply.parseDCCppReply("= B PROG 123");
        Assert.assertTrue(r.isTrackManagerReply());
        Assert.assertEquals("Monitor string", "TrackManager:= B PROG 123", r.toMonitorString());

        //LCD message
        r = DCCppReply.parseDCCppReply("@ 0 12 \"this is a test lcd message 12345\"");
        Assert.assertTrue(r.isLCDTextReply());
        Assert.assertEquals("this is a test lcd message 12345", r.getLCDTextString());
        Assert.assertEquals(0, r.getLCDDisplayNumInt());
        Assert.assertEquals(12, r.getLCDLineNumInt());
        r = DCCppReply.parseDCCppReply("@ 12 \"this is not, missing display#\"");
        Assert.assertFalse(r.isLCDTextReply());
        r = DCCppReply.parseDCCppReply("@ 0 4 \"123 456.789.001 test initial digits\"");
        Assert.assertTrue(r.isLCDTextReply());
        Assert.assertEquals("123 456.789.001 test initial digits", r.getLCDTextString());
        Assert.assertEquals(0, r.getLCDDisplayNumInt());
        Assert.assertEquals(4, r.getLCDLineNumInt());
        r = DCCppReply.parseDCCppReply("@ 0 4 test without quotes");
        Assert.assertFalse(r.isLCDTextReply());
        r = DCCppReply.parseDCCppReply("@ 0 4 \"    test leading and trailing spaces   \"");
        Assert.assertTrue(r.isLCDTextReply());
        Assert.assertEquals("    test leading and trailing spaces   ", r.getLCDTextString());

    }

    @Test
    public void testMonitorStringCurrentReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("a MAIN 0");
        Assert.assertEquals("Named Current Monitor string", "Current: 0 / 1024", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("a 41");
        Assert.assertEquals("Current Monitor string", "Current: 41 / 1024", l.toMonitorString());
    }
    
    @Test
    public void testMonitorStringMeterReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("c PROGVolts 18.2 V Percent 9.0 24.0 0.1 19");
        Assert.assertEquals("Meter reply: name PROGVolts, value 18.20, type V, unit Percent, min 9.00, max 24.00, resolution 0.10, warn 19.00", 
                l.toMonitorString());
        Assert.assertTrue( l.isMeterReply());
        Assert.assertTrue( l.isMeterTypeVolt());
        Assert.assertFalse(l.isMeterTypeCurrent());
        l = DCCppReply.parseDCCppReply("c MAINCurrent 0.3 C Kilo 0.0 5.0 0.01 4");
        Assert.assertEquals("Meter reply: name MAINCurrent, value 0.30, type C, unit Kilo, min 0.00, max 5.00, resolution 0.01, warn 4.00", 
                l.toMonitorString());
        Assert.assertTrue( l.isMeterReply());
        Assert.assertTrue( l.isMeterTypeCurrent());
        Assert.assertFalse(l.isMeterTypeVolt());
    }

    @Test
    public void testMonitorStringDiagReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("* This is a test *");
        Assert.assertEquals("Monitor string", "DIAG: This is a test ", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("*This is a test with a \nnewline*");
        Assert.assertEquals("Monitor string", "DIAG: This is a test with a \nnewline", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("**");
        Assert.assertEquals("Monitor string", "DIAG: ", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("*****");
        Assert.assertEquals("Monitor string", "DIAG: ***", l.toMonitorString());
    }

    @Test
    public void testMaxNumSlotsReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("# 47");
        Assert.assertEquals("Monitor string", "Number of slots reply: 47", l.toMonitorString());
    }

    @Test
    public void testMonitorStringTurnoutReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("H 1234 0");
        Assert.assertEquals("Monitor string", "Turnout Reply: ID: 1234, Dir: CLOSED", l.toMonitorString());
        Assert.assertFalse(l.getTOIsThrown());
        Assert.assertTrue(l.getTOIsClosed());
        l = DCCppReply.parseDCCppReply("H 1234 1");
        Assert.assertEquals("Monitor string", "Turnout Reply: ID: 1234, Dir: THROWN", l.toMonitorString());
        Assert.assertTrue(l.getTOIsThrown());
        Assert.assertFalse(l.getTOIsClosed());
    }

    @Test
    public void testTurnoutDefReplies() {
        DCCppReply l = DCCppReply.parseDCCppReply("H 23 DCC 5 0 1");
        Assert.assertEquals("Monitor string", "Turnout Def DCC Reply: ID:23, Address:5, Index:0, DCC Address:17, Dir:THROWN", l.toMonitorString());
        Assert.assertTrue(l.getTOIsThrown());
        Assert.assertFalse(l.getTOIsClosed());
        l = DCCppReply.parseDCCppReply("H 24 SERVO 100 410 205 2 1");
        Assert.assertTrue(l.getTOIsThrown());
        Assert.assertEquals("Monitor string", "Turnout Def SERVO Reply: ID:24, Pin:100, ThrownPos:410, ClosedPos:205, Profile:2, Dir:THROWN", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("H 1124 SERVO 100 410 205 2 0");
        Assert.assertEquals("Monitor string", "Turnout Def SERVO Reply: ID:1124, Pin:100, ThrownPos:410, ClosedPos:205, Profile:2, Dir:CLOSED", l.toMonitorString());
        Assert.assertTrue(l.getTOIsClosed());
        l = DCCppReply.parseDCCppReply("H 12345 VPIN 50 1");
        Assert.assertEquals("Monitor string", "Turnout Def VPIN Reply: ID:12345, Pin:50, Dir:THROWN", l.toMonitorString());
        Assert.assertTrue(l.getTOIsThrown());
        l = DCCppReply.parseDCCppReply("H 12345 VPIN 150 0");
        Assert.assertEquals("Monitor string", "Turnout Def VPIN Reply: ID:12345, Pin:150, Dir:CLOSED", l.toMonitorString());
        Assert.assertTrue(l.getTOIsClosed());
        l = DCCppReply.parseDCCppReply("H 30000 LCN 0");
        Assert.assertEquals("Monitor string", "Turnout Def LCN Reply: ID:30000, Dir:CLOSED", l.toMonitorString());
        Assert.assertTrue(l.getTOIsClosed());
        l = DCCppReply.parseDCCppReply("H 30001 LCN 1");
        Assert.assertEquals("Monitor string", "Turnout Def LCN Reply: ID:30001, Dir:THROWN", l.toMonitorString());
        Assert.assertTrue(l.getTOIsThrown());
        l = DCCppReply.parseDCCppReply("H 12345 VPIN 150 0 ignore unexpected values at the end");
        Assert.assertEquals("Monitor string", "Turnout Def VPIN Reply: ID:12345, Pin:150, Dir:CLOSED", l.toMonitorString());
        Assert.assertTrue(l.getTOIsClosed());
    }


    @Test
    public void testTurnoutDefProperties() {
        DCCppReply l = DCCppReply.parseDCCppReply("H 23 DCC 5 0 1");
        Assert.assertEquals("Type:DCC,ID:23,Address:5,Index:0,DCC Address:17", l.getPropertiesAsString());
        l = DCCppReply.parseDCCppReply("H 24 SERVO 100 410 205 2 1");
        Assert.assertEquals("Type:SERVO,ID:24,Pin:100,ThrownPos:410,ClosedPos:205,Profile:2", l.getPropertiesAsString());
        l = DCCppReply.parseDCCppReply("H 1124 SERVO 100 410 205 2 0");
        Assert.assertEquals("Type:SERVO,ID:1124,Pin:100,ThrownPos:410,ClosedPos:205,Profile:2", l.getPropertiesAsString());
        l = DCCppReply.parseDCCppReply("H 12345 VPIN 50 1");
        Assert.assertEquals("Type:VPIN,ID:12345,Pin:50", l.getPropertiesAsString());
        l = DCCppReply.parseDCCppReply("H 12345 VPIN 150 0");
        Assert.assertEquals("Type:VPIN,ID:12345,Pin:150", l.getPropertiesAsString());
        l = DCCppReply.parseDCCppReply("H 30000 LCN 0");
        Assert.assertEquals("Type:LCN,ID:30000", l.getPropertiesAsString());
        l = DCCppReply.parseDCCppReply("H 30001 LCN 1");
        Assert.assertEquals("Type:LCN,ID:30001", l.getPropertiesAsString());
        l = DCCppReply.parseDCCppReply("H 12345 VPIN 150 0 ignore unexpected values at the end");
        Assert.assertEquals("Type:VPIN,ID:12345,Pin:150", l.getPropertiesAsString());
    }

    @Test
    public void testOutputProperties() {
        DCCppReply r = DCCppReply.parseDCCppReply("Y 181 181 1 0");
        LinkedHashMap<String, Object> p = r.getProperties();
        Assert.assertEquals(4, p.size());
        Assert.assertEquals("OUTPUT", p.get("Type"));
        Assert.assertEquals(181,   p.get("ID"));
        Assert.assertEquals(181,   p.get("Pin"));
        Assert.assertEquals(1,     p.get("IFlag"));
        Assert.assertEquals("Type:OUTPUT,ID:181,Pin:181,IFlag:1", r.getPropertiesAsString());
    }


    @Test
    public void testSensorProperties() {
        DCCppReply r = DCCppReply.parseDCCppReply("Q 111 222 0");
        LinkedHashMap<String, Object> p = r.getProperties();
        Assert.assertEquals(4, p.size());
        Assert.assertEquals("SENSOR", p.get("Type"));
        Assert.assertEquals(111,   p.get("ID"));
        Assert.assertEquals(222,   p.get("Pin"));
        Assert.assertFalse((boolean) p.get("Pullup"));
        Assert.assertEquals("Type:SENSOR,ID:111,Pin:222,Pullup:false", r.getPropertiesAsString());
        r = DCCppReply.parseDCCppReply("Q 111 222 1");
        p = r.getProperties();
        Assert.assertTrue((boolean) p.get("Pullup"));
    }

    @Test
    public void testTurnoutProperties() {
        DCCppReply r = DCCppReply.parseDCCppReply("H 23 DCC 5 0 1");
        LinkedHashMap<String, Object> p = r.getProperties();
        Assert.assertEquals(5, p.size());
        Assert.assertEquals(23,    p.get("ID"));
        Assert.assertEquals("DCC", p.get("Type"));
        Assert.assertEquals(5,     p.get("Address"));
        Assert.assertEquals(0,     p.get("Index"));
        Assert.assertEquals(17,    p.get("DCC Address"));
        r = DCCppReply.parseDCCppReply("H 1124 SERVO 100 410 205 2 0");
        p = r.getProperties();
        Assert.assertEquals(6, p.size());
        Assert.assertEquals(1124,    p.get("ID"));
        Assert.assertEquals("SERVO", p.get("Type"));
        Assert.assertEquals(100,     p.get("Pin"));
        Assert.assertEquals(410,     p.get("ThrownPos"));
        Assert.assertEquals(205,     p.get("ClosedPos"));
        Assert.assertEquals(2,       p.get("Profile"));
        r = DCCppReply.parseDCCppReply("H 12345 VPIN 150 0");
        p = r.getProperties();
        Assert.assertEquals(3, p.size());
        Assert.assertEquals(12345,  p.get("ID"));
        Assert.assertEquals("VPIN", p.get("Type"));
        Assert.assertEquals(150,    p.get("Pin"));
        r = DCCppReply.parseDCCppReply("H 30000 LCN 0");
        p = r.getProperties();
        Assert.assertEquals(2, p.size());
        Assert.assertEquals(30000,  p.get("ID"));
        Assert.assertEquals("LCN",  p.get("Type"));
    }
    
    @Test
    public void testMonitorStringOutputPinReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("Y 1234 0");
        Assert.assertEquals("Monitor string", "Output Command Reply: Number: 1234, State: LOW", l.toMonitorString());
    }

    @Test
    public void testMonitorStringSensorStatusReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("Q 1234");
        Assert.assertEquals("Monitor string", "Sensor Reply (Active): Number: 1234, State: ACTIVE", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("q 1234");
        Assert.assertEquals("Monitor string", "Sensor Reply (Inactive): Number: 1234, State: INACTIVE", l.toMonitorString());
    }

    @Test
    public void testMonitorStringCVWriteByteReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("r 1234|4321|5 123");
        Assert.assertEquals("Monitor string", "Program Reply: CallbackNum:1234, Sub:4321, CV:5, Value:123", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("r 5 123"); // <r cv value>
        Assert.assertEquals("Monitor string", "Program Reply: CV:5, Value:123", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("r 3 -1"); // <r cv value>
        Assert.assertEquals("Monitor string", "Program Reply: CV:3, Value:-1", l.toMonitorString());
    }

    @Test
    public void testMonitorStringLocoIdReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("r 456"); // <r locoId>
        Assert.assertEquals("Monitor string", "Program LocoId Reply: LocoId:456", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("r -1"); // <r locoId> (-1 for error)
        Assert.assertEquals("Monitor string", "Program LocoId Reply: LocoId:-1", l.toMonitorString());
    }

    @Test
    public void testMonitorStringVerifyReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("v 78 99"); // <v cv byteValue>
        Assert.assertEquals("Monitor string", "Prog Verify Reply: CV: 78, Value: 99", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("v 90 -1"); // <v cv byteValue>
        Assert.assertEquals("Monitor string", "Prog Verify Reply: CV: 90, Value: -1", l.toMonitorString());
    }

    @Test
    public void testMonitorStringBitWriteReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("r 1234|4321|5 3 1");
        Assert.assertEquals("Monitor string", "Program Bit Reply: CallbackNum:1234, Sub:4321, CV:5, Bit:3, Value:1", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("r 5 3 1"); // <r cv bit value>
        Assert.assertEquals("Monitor string", "Program Bit Reply: CV:5, Bit:3, Value:1", l.toMonitorString());
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

    @Test
    public void testMonitorStringThrottleCommandsReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("jT 3456 C \"turnout desc\"");
        Assert.assertEquals("Monitor string", "Turnout ID:3456 State:C Desc:'turnout desc'", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("jT 111 222 3456");
        Assert.assertEquals("Monitor string", "Turnout IDs:[111, 222, 3456]", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("jT"); //no turnouts defined
        Assert.assertEquals("Monitor string", "Turnout IDs:[]", l.toMonitorString());
        l = DCCppReply.parseDCCppReply("jT 3456 C \"\""); //no description defined
        Assert.assertEquals("Monitor string", "Turnout ID:3456 State:C Desc:''", l.toMonitorString());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new DCCppReply();
    }

    @Override
    @AfterEach
    public void tearDown() {
        m = msg = null;
        JUnitUtil.tearDown();
    }

}
