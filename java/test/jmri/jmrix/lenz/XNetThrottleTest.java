package jmri.jmrix.lenz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.SpeedStepMode;

import org.junit.jupiter.api.*;

/**
 * Test for the jmri.jmrix.lenz.XNetThrottle class
 *
 * @author Paul Bender Copyright (C) 2008-2016
 */
@Timeout(1)
public class XNetThrottleTest extends jmri.jmrix.AbstractThrottleTest {

    protected XNetInterfaceScaffold tc = null;
    protected XNetSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        XNetThrottle t = new XNetThrottle(memo, tc);
        assertNotNull(t);
        t.throttleDispose();
    }

    // Test the constructor with an address specified.
    @Test
    public void testCtorWithArg() {
        XNetThrottle t = new XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
        assertNotNull(t);
        t.throttleDispose();
    }

    // Test the initialization sequence.
    @Test
    public void testInitSequenceNormalUnitSpeedStep128() {
        int n = tc.outbound.size();
        // this test requires a new throttle.
        XNetThrottle t = new XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
        assertNotNull(t);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait forboutbound size to change.

        // The first thing on the outbound queue should be a request for status.
        assertEquals( "E3 00 00 03 E0", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this is a message with the status.
        XNetReply m = new XNetReply();
        m.setElement(0, 0xE4);
        m.setElement(1, 0x04);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0x00);
        m.setElement(5, 0xE0);

        n = tc.outbound.size();
        t.message(m);

        // which we're going to get a request for function momentary status in response to.
        // We're just going to make sure this is there and respond with not supported.
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for outbound size to change.
        // The first thing on the outbound queue should be a request for status.
        assertEquals( "E3 07 00 03 E7", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        t.message(m);
        // Sending the not supported message should make the throttle change
        // to the idle state.

        // now we're going to wait and verify the throttle eventually has
        // its status set to idle.
        JUnitUtil.waitFor(JUnitUtil.WAITFOR_DEFAULT_DELAY);  // give the messages
        // some time to process;

        JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

        // and verify all the data was set correctly.
        // getSpeedStepMode returns the right mode and
        assertEquals( SpeedStepMode.NMRA_DCC_128, t.getSpeedStepMode(), "SpeedStepMode");
        // get speedIncrement reports the correct value.
        assertEquals( (1.0f / 126.0f), t.getSpeedIncrement(), 0.0,
            "SpeedStep Increment"); // the speed increments are constants, so if there is deviation, that is an error.

        // test that the speed value is the expected value
        assertEquals( 0.0, t.getSpeedSetting(), 0.0, "Speed 0.0");

        // test that the direction value is the expected value
        assertFalse( t.getIsForward(), "Direction Reverse");

        // function getters return the right values (f0-f12).
        assertFalse( t.getFunction(0), "F0 off");
        assertFalse( t.getFunction(1), "F1 off");
        assertFalse( t.getFunction(2), "F2 off");
        assertFalse( t.getFunction(3), "F3 off");
        assertFalse( t.getFunction(4), "F4 off");
        assertFalse( t.getFunction(5), "F5 off");
        assertFalse( t.getFunction(6), "F6 off");
        assertFalse( t.getFunction(7), "F7 off");
        assertFalse( t.getFunction(8), "F8 off");
        assertFalse( t.getFunction(9), "F9 off");
        assertFalse( t.getFunction(10), "F10 off");
        assertFalse( t.getFunction(11), "F11 off");
        assertFalse( t.getFunction(12), "F12 off");
        t.throttleDispose();
    }

    @Test
    public void initSequenceNormalUnitSpeedStep14() {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        // this test requires a new throttle.
        XNetThrottle t = new XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
        //The first thing on the outbound queue should be a request for status.

        // And the response to this is a message with the status.
        XNetReply m = new XNetReply();
        m.setElement(0, XNetConstants.LOCO_INFO_NORMAL_UNIT);
        m.setElement(1, 0x00);  // speed step mode and availablility
        m.setElement(2, 0x00);  //speed and direction
        m.setElement(3, 0x00);  // function info f0-f7
        m.setElement(4, 0x00);  // function info f8-f12
        m.setElement(5, 0xE4);

        n = tc.outbound.size();
        t.message(m);

        // which we're going to get a request for function momentary status in response to.
        // We're just going to make sure this is there and respond with not supported.
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        // And the response to this message with the status.
        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

        // Sending the not supported message should make the throttle send a
        // request for the high function status information.
        // We're just going to make sure this is there and respond with not supported.
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        // n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

        // Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.
        // in this case, we are just checking for proper initialization.
        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        assertEquals( SpeedStepMode.NMRA_DCC_14, t.getSpeedStepMode(), "SpeedStepMode");
        assertEquals( (1.0f / 14.0f), t.getSpeedIncrement(), 0.0,
            "SpeedStep Increment"); // the speed increments are constants, so if there is deviation, that is an error.

        // test that the speed value is the expected value
        assertEquals( 0.0, t.getSpeedSetting(), 0.0, "Speed 0.0");

        // test that the direction value is the expected value
        assertFalse( t.getIsForward(), "Direction Reverse");

        // function getters return the right values (f0-f12).
        assertFalse( t.getFunction(0), "F0 off");
        assertFalse( t.getFunction(1), "F1 off");
        assertFalse( t.getFunction(2), "F2 off");
        assertFalse( t.getFunction(3), "F3 off");
        assertFalse( t.getFunction(4), "F4 off");
        assertFalse( t.getFunction(5), "F5 off");
        assertFalse( t.getFunction(6), "F6 off");
        assertFalse( t.getFunction(7), "F7 off");
        assertFalse( t.getFunction(8), "F8 off");
        assertFalse( t.getFunction(9), "F9 off");
        assertFalse( t.getFunction(10), "F10 off");
        assertFalse( t.getFunction(11), "F11 off");
        t.throttleDispose();
    }

    @Test
    public void initSequenceMUAddress28SpeedStep() {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        // this test requires a new throttle.
        XNetThrottle t = new XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
        //The first thing on the outbound queue should be a request for status.

        // And the response to this is a message with the status.
        XNetReply m = new XNetReply();
        m.setElement(0, XNetConstants.LOCO_INFO_MU_ADDRESS);
        m.setElement(1, 0x02);  // speed step mode and availablility
        m.setElement(2, 0x00);  //speed and direction
        m.setElement(3, 0xE6);

        n = tc.outbound.size();
        t.message(m);

        // which we're going to get a request for function momentary status in response to.
        // We're just going to make sure this is there and respond with not supported.
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        // And the response to this message with the status.
        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

        // Sending the not supported message should make the throttle send a
        // request for the high function status information.
        // We're just going to make sure this is there and respond with not supported.
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        // n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

        // Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.
        // in this case, we are just checking for proper initialization.
        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        assertEquals( SpeedStepMode.NMRA_DCC_28, t.getSpeedStepMode(),
            "SpeedStepMode");
        assertEquals( (1.0f / 28.0f), t.getSpeedIncrement(), 0.0,
            "SpeedStep Increment"); // the speed increments are constants, so if there is deviation, that is an error.

        // test that the speed value is the expected value
        assertEquals( 0.0, t.getSpeedSetting(), 0.0, "Speed 0.0");

        // test that the direction value is the expected value
        assertFalse( t.getIsForward(), "Direction Reverse");
        t.throttleDispose();
    }

    @Test
    public void initSequenceMuedUnitSpeedStep128() {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        // this test requires a new throttle.
        XNetThrottle t = new XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
        //The first thing on the outbound queue should be a request for status.

        // And the response to this is a message with the status.
        XNetReply m = new XNetReply();
        m.setElement(0, XNetConstants.LOCO_INFO_MUED_UNIT);
        m.setElement(1, 0x04);  // speed step mode and availablility
        m.setElement(2, 0x00);  //speed and direction
        m.setElement(3, 0x00);  // function info f0-f7
        m.setElement(4, 0x00);  // function info f8-f12
        m.setElement(5, 0x05);  // consist address
        m.setElement(6, 0xE4);

        n = tc.outbound.size();
        t.message(m);

        // which we're going to get a request for function momentary status in response to.
        // We're just going to make sure this is there and respond with not supported.
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        // And the response to this message with the status.
        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

        // Sending the not supported message should make the throttle send a
        // request for the high function status information.
        // We're just going to make sure this is there and respond with not supported.
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        // n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

        // Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.
        // in this case, we are just checking for proper initialization.
        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        assertEquals( SpeedStepMode.NMRA_DCC_128, t.getSpeedStepMode(), "SpeedStepMode");
        assertEquals( (1.0f / 126.0f), t.getSpeedIncrement(), 0.0,
            "SpeedStep Increment"); // the speed increments are constants, so if there is deviation, that is an error.

        // test that the speed value is the expected value
        assertEquals( 0.0, t.getSpeedSetting(), 0.0, "Speed 0.0");

        // test that the direction value is the expected value
        assertFalse( t.getIsForward(), "Direction Reverse");

        // function getters return the right values (f0-f12).
        assertFalse( t.getFunction(0), "F0 off");
        assertFalse( t.getFunction(1), "F1 off");
        assertFalse( t.getFunction(2), "F2 off");
        assertFalse( t.getFunction(3), "F3 off");
        assertFalse( t.getFunction(4), "F4 off");
        assertFalse( t.getFunction(5), "F5 off");
        assertFalse( t.getFunction(6), "F6 off");
        assertFalse( t.getFunction(7), "F7 off");
        assertFalse( t.getFunction(8), "F8 off");
        assertFalse( t.getFunction(9), "F9 off");
        assertFalse( t.getFunction(10), "F10 off");
        assertFalse( t.getFunction(11), "F11 off");
        t.throttleDispose();
    }

    @Test
    public void initSequenceDHUnitSpeedStep27() {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        // this test requires a new throttle.
        XNetThrottle t = new XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
        //The first thing on the outbound queue should be a request for status.

        // And the response to this is a message with the status.
        XNetReply m = new XNetReply();
        m.setElement(0, XNetConstants.LOCO_INFO_DH_UNIT);
        m.setElement(1, 0x01);  // speed step mode and availablility
        m.setElement(2, 0x00);  //speed and direction
        m.setElement(3, 0x00);  // function info f0-f7
        m.setElement(4, 0x00);  // function info f8-f12
        m.setElement(5, 0x00);  // Other DH address high
        m.setElement(6, 0x05);  // Other DH address low
        m.setElement(6, 0xE7);

        n = tc.outbound.size();
        t.message(m);

        // which we're going to get a request for function momentary status in response to.
        // We're just going to make sure this is there and respond with not supported.
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        // And the response to this message with the status.
        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

        // Sending the not supported message should make the throttle send a
        // request for the high function status information.
        // We're just going to make sure this is there and respond with not supported.
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        // n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

        // Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.
        // in this case, we are just checking for proper initialization.
        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        assertEquals( SpeedStepMode.NMRA_DCC_27, t.getSpeedStepMode(), "SpeedStepMode");
        assertEquals( (1.0f / 27.0f), t.getSpeedIncrement(), 0.0,
            "SpeedStep Increment"); // the speed increments are constants, so if there is deviation, that is an error.

        // test that the speed value is the expected value
        assertEquals( 0.0, t.getSpeedSetting(), 0.0, "Speed 0.0");

        // test that the direction value is the expected value
        assertFalse( t.getIsForward(), "Direction Reverse");

        // function getters return the right values (f0-f12).
        assertFalse( t.getFunction(0), "F0 off");
        assertFalse( t.getFunction(1), "F1 off");
        assertFalse( t.getFunction(2), "F2 off");
        assertFalse( t.getFunction(3), "F3 off");
        assertFalse( t.getFunction(4), "F4 off");
        assertFalse( t.getFunction(5), "F5 off");
        assertFalse( t.getFunction(6), "F6 off");
        assertFalse( t.getFunction(7), "F7 off");
        assertFalse( t.getFunction(8), "F8 off");
        assertFalse( t.getFunction(9), "F9 off");
        assertFalse( t.getFunction(10), "F10 off");
        assertFalse( t.getFunction(11), "F11 off");
        t.throttleDispose();
    }

    @Test
    public void testSendStatusInformationRequest() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();
        // in this case, we are sending a status information request.

        t.sendStatusInformationRequest();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
        //The first thing on the outbound queue should be a request for status.
        assertEquals( "E3 00 00 03 E0", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this is a message with the status.
        XNetReply m = new XNetReply();
        m.setElement(0, 0xE4);
        m.setElement(1, 0x04);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0x00);
        m.setElement(5, 0xE0);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test
    public void testSendFunctionStatusInformationRequest() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending a status information request.
        t.sendFunctionStatusInformationRequest();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a request for status.
        assertEquals( "E3 07 00 03 E7", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message with the status.
        XNetReply m = new XNetReply();
        m.setElement(0, 0xE3);
        m.setElement(1, 0x50);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0xB3);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

    }

    @Test
    public void testSendFunctionHighStatusInformationRequest() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending a status information request.
        t.sendFunctionHighInformationRequest();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a request for status.
        assertEquals( "E3 09 00 03 E9", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message with the status.
        XNetReply m = new XNetReply();
        m.setElement(0, 0xE3);
        m.setElement(1, 0x52);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0xB3);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test
    public void testSendFunctionHighMomentaryStatusRequest() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending a status information request.
        t.sendFunctionHighMomentaryStatusRequest();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a request for status.
        assertEquals( "E3 08 00 03 E8", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message with the status.
        XNetReply m = new XNetReply();
        m.setElement(0, 0xE3);
        m.setElement(1, 0x51);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0xB3);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();
    }

    @Test
    @Override
    public void testSendFunctionGroup1() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending function group 1.
        t.sendFunctionGroup1();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 1 request.
        assertEquals( "E4 20 00 03 00 C7", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test
    @Override
    public void testSendFunctionGroup2() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending function group 2.
        t.sendFunctionGroup2();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 2 request.
        assertEquals( "E4 21 00 03 00 C6", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test
    @Override
    public void testSendFunctionGroup3() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending function group 3.
        t.sendFunctionGroup3();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 3 request.
        assertEquals( "E4 22 00 03 00 C5", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test
    @Override
    public void testSendFunctionGroup4() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending function group 4.
        t.sendFunctionGroup4();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 4 request.
        assertEquals( "E4 23 00 03 00 C4", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test
    public void testSendFunctionGroup4v35() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottlev35(t, n);
        n = tc.outbound.size();

        // in this case, we are sending function group 4.
        t.sendFunctionGroup4();
        int count = 0;
        while (n == tc.outbound.size() && count < 1000) {
            count++;
        }

        // if the loop exited early, we sent the message, and we
        // shouldn't do that in this case.
        assertEquals( 1000, count, "loop exited");

    }

    @Test
    @Override
    public void testSendFunctionGroup5() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending function group 5.
        t.sendFunctionGroup5();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 5 request.
        assertEquals( "E4 28 00 03 00 CF", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test
    public void testSendFunctionGroup5v35() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottlev35(t, n);
        n = tc.outbound.size();

        // in this case, we are sending function group 5.
        t.sendFunctionGroup5();

        int count = 0;
        while (n == tc.outbound.size() && count < 1000) {
            count++;
        }

        // if the loop exited early, we sent the message, and we
        // shouldn't do that in this case.
        assertEquals( 1000, count, "loop exited");

    }

    @Test
    @Override
    public void testSendMomentaryFunctionGroup1() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending momentary function group 1.
        t.sendMomentaryFunctionGroup1();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a momentary group 1 request.
        assertEquals( "E4 24 00 03 00 C3", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test
    @Override
    public void testSendMomentaryFunctionGroup2() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending momentary function group 2.
        t.sendMomentaryFunctionGroup2();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a momentary group 2 request.
        assertEquals( "E4 25 00 03 00 C2", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test
    @Override
    public void testSendMomentaryFunctionGroup3() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending momentary function group 3.
        t.sendMomentaryFunctionGroup3();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a monentary group 3 request.
        assertEquals( "E4 26 00 03 00 C1", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test
    @Override
    public void testSendMomentaryFunctionGroup4() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending function momentary group 4.
        t.sendMomentaryFunctionGroup4();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a momentary group 4 request.
        assertEquals( "E4 27 00 03 00 C0", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test
    @Override
    public void testSendMomentaryFunctionGroup5() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending momentary function group 5.
        t.sendMomentaryFunctionGroup5();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a momentary group 5 request.
        assertEquals( "E4 2C 00 03 00 CB", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test
    public void testGetDccAddress() {
        XNetThrottle t = (XNetThrottle) instance;
        assertEquals( 3, t.getDccAddress(), "XNetThrottle getDccAddress()");
    }

    @Test
    public void testGetDccAddressLow() {
        XNetThrottle t = (XNetThrottle) instance;
        assertEquals( 3, t.getDccAddressLow(), "XNetThrottle getDccAddressLow()");
    }

    @Test
    public void testGetDccAddressHigh() {
        XNetThrottle t = (XNetThrottle) instance;
        assertEquals( 0, t.getDccAddressHigh(), "XNetThrottle getDccAddressHigh()");
    }

    @Test
    public void testGetLocoAddress() {
        XNetThrottle t = (XNetThrottle) instance;
        assertEquals( new jmri.DccLocoAddress(3, false), t.getLocoAddress(),
            "XNetThrottle getLocoAddress()");
    }

    @Test
    public void testSetReverse() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending a request to change the direction.
        t.setIsForward(false);

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        assertEquals( "E4 13 00 03 00 F4", tc.outbound.elementAt(n).toString(),
            "Throttle Set Speed Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

        // and finaly, verify that getIsForward() returns false, like we set it.
        assertFalse( t.getIsForward(), "Direction Set");
        t.throttleDispose();
    }

    @Test
    public void testSetForward() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending a request to change the direction.
        t.setIsForward(true);

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        assertEquals( "E4 13 00 03 80 74", tc.outbound.elementAt(n).toString(),
            "Throttle Set Speed Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

        // and finaly, verify that getIsForward() returns false, like we set it.
        assertTrue( t.getIsForward(), "Direction Set");
        t.throttleDispose();
    }

    @Test
    public void testSendEmergencyStop() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending an emergency stop message.
        t.sendEmergencyStop();

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        assertEquals( "92 00 03 91", tc.outbound.elementAt(n).toString(),
            "Throttle Emergency Stop Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();
    }

    @Test
    public void testSetSpeedStep128() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending a request to change the speed step mode.
        t.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_128);

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        assertEquals( "E4 13 00 03 00 F4", tc.outbound.elementAt(n).toString(),
            "Throttle Set Speed Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        assertEquals( SpeedStepMode.NMRA_DCC_128, t.getSpeedStepMode(), "SpeedStepMode");
        assertEquals( (1.0f / 126.0f), t.getSpeedIncrement(), 0.0,
            "SpeedStep Increment"); // the speed increments are constants, so if there is deviation, that is an error.
        t.throttleDispose();
    }

    @Test
    public void testSetSpeedStep28() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending a request to change the speed step mode.
        t.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_28);

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        assertEquals( "E4 12 00 03 00 F5", tc.outbound.elementAt(n).toString(),
            "Throttle Set Speed Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        assertEquals( SpeedStepMode.NMRA_DCC_28, t.getSpeedStepMode(), "SpeedStepMode");
        assertEquals( (1.0f / 28.0f), t.getSpeedIncrement(), 0.0,
            "SpeedStep Increment"); // the speed increments are constants, so if there is deviation, that is an error.
        t.throttleDispose();
    }

    @Test
    public void testSetSpeedStep27() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending a request to change the speed step mode.
        t.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_27);

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        assertEquals( "E4 11 00 03 00 F6", tc.outbound.elementAt(n).toString(),
            "Throttle Set Speed Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        assertEquals( SpeedStepMode.NMRA_DCC_27, t.getSpeedStepMode(), "SpeedStepMode");
        assertEquals( (1.0f / 27.0f), t.getSpeedIncrement(), 0.0,
            "SpeedStep Increment"); // the speed increments are constants, so if there is deviation, that is an error.
        t.throttleDispose();
    }

    @Test
    public void testSetSpeedStep14() {
        int n = tc.outbound.size();
        XNetThrottle t = (XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // in this case, we are sending a request to change the speed step mode.
        t.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_14);

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        assertEquals( "E4 10 00 03 00 F7", tc.outbound.elementAt(n).toString(),
            "Throttle Set Speed Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        assertEquals( SpeedStepMode.NMRA_DCC_14, t.getSpeedStepMode(), "SpeedStepMode");
        assertEquals( (1.0f / 14.0f), t.getSpeedIncrement(), 0.0,
            "SpeedStep Increment"); // the speed increments are constants, so if there is deviation, that is an error.
        t.throttleDispose();
    }

    /**
     * Test of getSpeedStepMode method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetSpeedStepMode() {
        SpeedStepMode expResult = SpeedStepMode.NMRA_DCC_128;
        SpeedStepMode result = instance.getSpeedStepMode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSpeedIncrement method, of class AbstractThrottle.
     */
    @Override
    @Test
    public void testGetSpeedIncrement() {
        float expResult = 1.0F / 126.0F;
        float result = instance.getSpeedIncrement();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of setF0 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF0() {
        boolean f0 = false;
        instance.setF0(f0);
    }

    /**
     * Test of setF1 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF1() {
        boolean f1 = false;
        instance.setF1(f1);
    }

    /**
     * Test of setF2 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF2() {
        boolean f2 = false;
        instance.setF2(f2);
    }

    /**
     * Test of setF3 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF3() {
        boolean f3 = false;
        instance.setF3(f3);
    }

    /**
     * Test of setF4 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF4() {
        boolean f4 = false;
        instance.setF4(f4);
    }

    /**
     * Test of setF5 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF5() {
        boolean f5 = false;
        instance.setF5(f5);
    }

    /**
     * Test of setF6 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF6() {
        boolean f6 = false;
        instance.setF6(f6);
    }

    /**
     * Test of setF7 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF7() {
        boolean f7 = false;
        instance.setF7(f7);
    }

    /**
     * Test of setF8 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF8() {
        boolean f8 = false;
        instance.setF8(f8);
    }

    /**
     * Test of setF9 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF9() {
        boolean f9 = false;
        instance.setF9(f9);
    }

    /**
     * Test of setF10 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF10() {
        boolean f10 = false;
        instance.setF10(f10);
    }

    /**
     * Test of setF11 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF11() {
        boolean f11 = false;
        instance.setF11(f11);
    }

    /**
     * Test of setF12 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF12() {
        boolean f12 = false;
        instance.setF12(f12);
    }

    /**
     * Test of setF13 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF13() {
        boolean f13 = false;
        instance.setF13(f13);
    }

    /**
     * Test of setF14 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF14() {
        boolean f14 = false;
        instance.setF14(f14);
    }

    /**
     * Test of setF15 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF15() {
        boolean f15 = false;
        instance.setF15(f15);
    }

    /**
     * Test of setF16 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF16() {
        boolean f16 = false;
        instance.setF16(f16);
    }

    /**
     * Test of setF17 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF17() {
        boolean f17 = false;
        instance.setF17(f17);
    }

    /**
     * Test of setF18 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF18() {
        boolean f18 = false;
        instance.setF18(f18);
    }

    /**
     * Test of setF19 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF19() {
        boolean f19 = false;
        instance.setF19(f19);
    }

    /**
     * Test of setF20 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF20() {
        boolean f20 = false;
        instance.setF20(f20);
    }

    /**
     * Test of setF21 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF21() {
        boolean f21 = false;
        instance.setF21(f21);
    }

    /**
     * Test of setF22 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF22() {
        boolean f22 = false;
        instance.setF22(f22);
    }

    /**
     * Test of setF23 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF23() {
        boolean f23 = false;
        instance.setF23(f23);
    }

    /**
     * Test of setF24 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF24() {
        boolean f24 = false;
        instance.setF24(f24);
    }

    /**
     * Test of setF25 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF25() {
        boolean f25 = false;
        instance.setF25(f25);
    }

    /**
     * Test of setF26 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF26() {
        boolean f26 = false;
        instance.setF26(f26);
    }

    /**
     * Test of setF27 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF27() {
        boolean f27 = false;
        instance.setF27(f27);
    }

    /**
     * Test of setF28 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF28() {
        boolean f28 = false;
        instance.setF28(f28);
    }

    // run the throttle through the initialization sequence,
    // without assertions, so post initialization tests can be
    // performed.
    protected void initThrottle(XNetThrottle t, int n) {
        // before we send any commands, make sure the software version is
        // set to version 3.6.
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        if (n == 0) {
            while (n == tc.outbound.size()) {
            } // busy loop.  Wait for
            // outbound size to change.
        }
        //The first thing on the outbound queue should be a request for status.
        // And the response to this is a message with the status.
        XNetReply m = new XNetReply();
        m.setElement(0, 0xE4);
        m.setElement(1, 0x04);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0x00);
        m.setElement(5, 0xE0);

        n = tc.outbound.size();
        t.message(m);

        // which we're going to get a request for function momentary status in response to.
        // We're just going to make sure this is there and respond with not supported.
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        // And the response to this message with the status.
        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

        // Sending the not supported message should make the throttle send a
        // request for the high function status information.
        // We're just going to make sure this is there and respond with not supported.
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        // n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

        // Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.
    }

    // run the throttle through the initialization sequence,
    // without assertions, so post initialization tests can be
    // performed.  This version sets the command station to version 3.5
    protected void initThrottlev35(XNetThrottle t, int n) {
        if (n == 0) {
            while (n == tc.outbound.size()) {
            } // busy loop.  Wait for
            // outbound size to change.
        }
        //The first thing on the outbound queue should be a request for status.

        // And the response to this is a message with the status.
        XNetReply m = new XNetReply();
        m.setElement(0, 0xE4);
        m.setElement(1, 0x04);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0x00);
        m.setElement(5, 0xE0);

        n = tc.outbound.size();
        t.message(m);

        // which we're going to get a request for function momentary status in response to.
        // We're just going to make sure this is there and respond with not supported.
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        // And the response to this message with the status.
        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        // n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

        // Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        // infrastructure objects
        tc = new XNetInterfaceScaffold(new LenzCommandStation() {
            @Override
            public float getCommandStationSoftwareVersionBCD() {
                return 0x36;
            }
        });
        memo = new XNetSystemConnectionMemo(tc);
        XNetThrottleManager tm = new XNetThrottleManager(memo);
        memo.setThrottleManager(tm);
        jmri.InstanceManager.setDefault(jmri.ThrottleManager.class, memo.getThrottleManager());
        XNetThrottle t = new XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
        instance = t;
    }

    @AfterEach
    @Override
    public void tearDown() {
        try {
            ((XNetThrottle) instance).throttleDispose();
        }
        finally {
            tc.terminateThreads();
            tc = null;
            memo.dispose();
            memo = null;
            instance = null;
            JUnitUtil.tearDown();
        }
    }

}
