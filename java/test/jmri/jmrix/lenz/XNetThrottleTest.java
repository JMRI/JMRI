package jmri.jmrix.lenz;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetThrottleTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetThrottle class
 *
 * @author	Paul Bender Copyright (C) 2008-2016
 */
public class XNetThrottleTest{

    private XNetInterfaceScaffold tc = null;
    private XNetSystemConnectionMemo memo = null;
 
    @Test(timeout=1000)
    public void testCtor() {
        XNetThrottle t = new XNetThrottle(memo, tc);
        Assert.assertNotNull(t);
        t.throttleDispose();
    }

    // Test the constructor with an address specified.
    @Test(timeout=1000)
    public void testCtorWithArg() throws Exception {
        XNetThrottle t = new XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
        Assert.assertNotNull(t);
        t.throttleDispose();
    }

    // Test the initilization sequence.
    @Test(timeout=1000)
    public void testInitSequenceNormalUnitSpeedStep128() throws Exception {
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
        Assert.assertNotNull(t);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
        //The first thing on the outbound queue should be a request for status.
        Assert.assertEquals("Throttle Information Request Message", "E3 00 00 03 E0", tc.outbound.elementAt(n).toString());

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
        //The first thing on the outbound queue should be a request for status.
        Assert.assertEquals("Throttle Information Request Message", "E3 07 00 03 E7", tc.outbound.elementAt(n).toString());

        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);

        t.message(m);
	// Sending the not supported message should make the throttle change
        // to the idle state.

        // now we're going to wait and verify the throttle eventually has 
        // its status set to idle.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");
        jmri.util.JUnitUtil.releaseThread(this);  // give the messages
        // some time to process;

        Assert.assertEquals("Throttle in THROTTLEIDLE state", XNetThrottle.THROTTLEIDLE, t.requestState);

        // and verify all the data was set correctly.

        // getSpeedStepMode returns the right mode and
        Assert.assertEquals("SpeedStepMode",jmri.DccThrottle.SpeedStepMode128,t.getSpeedStepMode());
        // get speedIncrement reports the correct value.
        Assert.assertEquals("SpeedStep Increment",jmri.jmrix.AbstractThrottle.SPEED_STEP_128_INCREMENT,t.getSpeedIncrement(),0.0); // the speed increments are constants, so if there is deviation, that is an error.

        // test that the speed value is the expected value
        Assert.assertEquals("Speed 0.0",0.0,t.getSpeedSetting(),0.0);

        // test that the direction value is the expected value
        Assert.assertFalse("Direction Reverse",t.getIsForward());

        // function getters return the right values (f0-f12).
        Assert.assertFalse("F0 off",t.getF0());
        Assert.assertFalse("F1 off",t.getF1());
        Assert.assertFalse("F2 off",t.getF2());
        Assert.assertFalse("F3 off",t.getF3());
        Assert.assertFalse("F4 off",t.getF4());
        Assert.assertFalse("F5 off",t.getF5());
        Assert.assertFalse("F6 off",t.getF6());
        Assert.assertFalse("F7 off",t.getF7());
        Assert.assertFalse("F8 off",t.getF8());
        Assert.assertFalse("F9 off",t.getF9());
        Assert.assertFalse("F10 off",t.getF10());
        Assert.assertFalse("F11 off",t.getF11());
        Assert.assertFalse("F12 off",t.getF12());
        t.throttleDispose();
    }

    @Test(timeout=1000)
    public void initSequenceNormalUnitSpeedStep14() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	    // Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are just checking for proper initilization.

        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        Assert.assertEquals("SpeedStepMode",jmri.DccThrottle.SpeedStepMode14,t.getSpeedStepMode());
        Assert.assertEquals("SpeedStep Increment",jmri.jmrix.AbstractThrottle.SPEED_STEP_14_INCREMENT,t.getSpeedIncrement(),0.0); // the speed increments are constants, so if there is deviation, that is an error.

        // test that the speed value is the expected value
        Assert.assertEquals("Speed 0.0",0.0,t.getSpeedSetting(),0.0);

        // test that the direction value is the expected value
        Assert.assertFalse("Direction Reverse",t.getIsForward());

        // function getters return the right values (f0-f12).
        Assert.assertFalse("F0 off",t.getF0());
        Assert.assertFalse("F1 off",t.getF1());
        Assert.assertFalse("F2 off",t.getF2());
        Assert.assertFalse("F3 off",t.getF3());
        Assert.assertFalse("F4 off",t.getF4());
        Assert.assertFalse("F5 off",t.getF5());
        Assert.assertFalse("F6 off",t.getF6());
        Assert.assertFalse("F7 off",t.getF7());
        Assert.assertFalse("F8 off",t.getF8());
        Assert.assertFalse("F9 off",t.getF9());
        Assert.assertFalse("F10 off",t.getF10());
        Assert.assertFalse("F11 off",t.getF11());
        t.throttleDispose();
    }

    @Test(timeout=1000)
    public void initSequenceMUAddress28SpeedStep() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	    // Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are just checking for proper initilization.

        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        Assert.assertEquals("SpeedStepMode",jmri.DccThrottle.SpeedStepMode28,t.getSpeedStepMode());
        Assert.assertEquals("SpeedStep Increment",jmri.jmrix.AbstractThrottle.SPEED_STEP_28_INCREMENT,t.getSpeedIncrement(),0.0); // the speed increments are constants, so if there is deviation, that is an error.

        // test that the speed value is the expected value
        Assert.assertEquals("Speed 0.0",0.0,t.getSpeedSetting(),0.0);

        // test that the direction value is the expected value
        Assert.assertFalse("Direction Reverse",t.getIsForward());
        t.throttleDispose();
    }

    @Test(timeout=1000)
    public void initSequenceMuedUnitSpeedStep128() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	    // Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are just checking for proper initilization.

        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        Assert.assertEquals("SpeedStepMode",jmri.DccThrottle.SpeedStepMode128,t.getSpeedStepMode());
        Assert.assertEquals("SpeedStep Increment",jmri.jmrix.AbstractThrottle.SPEED_STEP_128_INCREMENT,t.getSpeedIncrement(),0.0); // the speed increments are constants, so if there is deviation, that is an error.

        // test that the speed value is the expected value
        Assert.assertEquals("Speed 0.0",0.0,t.getSpeedSetting(),0.0);

        // test that the direction value is the expected value
        Assert.assertFalse("Direction Reverse",t.getIsForward());

        // function getters return the right values (f0-f12).
        Assert.assertFalse("F0 off",t.getF0());
        Assert.assertFalse("F1 off",t.getF1());
        Assert.assertFalse("F2 off",t.getF2());
        Assert.assertFalse("F3 off",t.getF3());
        Assert.assertFalse("F4 off",t.getF4());
        Assert.assertFalse("F5 off",t.getF5());
        Assert.assertFalse("F6 off",t.getF6());
        Assert.assertFalse("F7 off",t.getF7());
        Assert.assertFalse("F8 off",t.getF8());
        Assert.assertFalse("F9 off",t.getF9());
        Assert.assertFalse("F10 off",t.getF10());
        Assert.assertFalse("F11 off",t.getF11());
        t.throttleDispose();
    }

    @Test(timeout=1000)
    public void initSequenceDHUnitSpeedStep27() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	    // Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are just checking for proper initilization.

        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        Assert.assertEquals("SpeedStepMode",jmri.DccThrottle.SpeedStepMode27,t.getSpeedStepMode());
        Assert.assertEquals("SpeedStep Increment",jmri.jmrix.AbstractThrottle.SPEED_STEP_27_INCREMENT,t.getSpeedIncrement(),0.0); // the speed increments are constants, so if there is deviation, that is an error.

        // test that the speed value is the expected value
        Assert.assertEquals("Speed 0.0",0.0,t.getSpeedSetting(),0.0);

        // test that the direction value is the expected value
        Assert.assertFalse("Direction Reverse",t.getIsForward());

        // function getters return the right values (f0-f12).
        Assert.assertFalse("F0 off",t.getF0());
        Assert.assertFalse("F1 off",t.getF1());
        Assert.assertFalse("F2 off",t.getF2());
        Assert.assertFalse("F3 off",t.getF3());
        Assert.assertFalse("F4 off",t.getF4());
        Assert.assertFalse("F5 off",t.getF5());
        Assert.assertFalse("F6 off",t.getF6());
        Assert.assertFalse("F7 off",t.getF7());
        Assert.assertFalse("F8 off",t.getF8());
        Assert.assertFalse("F9 off",t.getF9());
        Assert.assertFalse("F10 off",t.getF10());
        Assert.assertFalse("F11 off",t.getF11());
        t.throttleDispose();
    }

    @Test(timeout=1000)
    public void testSendStatusInformationRequest() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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

        m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x82);
        m.setElement(2, 0xE3);
        
        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.
        // in this case, we are sending a status information request.

        t.sendStatusInformationRequest();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
        //The first thing on the outbound queue should be a request for status.
        Assert.assertEquals("Throttle Information Request Message", "E3 00 00 03 E0", tc.outbound.elementAt(n).toString());

        // And the response to this is a message with the status.
        m = new XNetReply();
        m.setElement(0, 0xE4);
        m.setElement(1, 0x04);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0x00);
        m.setElement(5, 0xE0);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test(timeout=1000)
    public void testSendFunctionStatusInformationRequest() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending a status information request.

        t.sendFunctionStatusInformationRequest();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a request for status.
        Assert.assertEquals("Throttle Information Request Message", "E3 07 00 03 E7", tc.outbound.elementAt(n).toString());

        // And the response to this message with the status.
        m = new XNetReply();
        m.setElement(0, 0xE3);
        m.setElement(1, 0x50);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0xB3);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

    }

    @Test(timeout=1000)
    public void testSendFunctionHighStatusInformationRequest() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending a status information request.

        t.sendFunctionHighInformationRequest();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a request for status.
        Assert.assertEquals("Throttle Information Request Message", "E3 09 00 03 E9", tc.outbound.elementAt(n).toString());

        // And the response to this message with the status.
        m = new XNetReply();
        m.setElement(0, 0xE3);
        m.setElement(1, 0x52);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0xB3);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test(timeout=1000)
    public void testSendFunctionHighMomentaryStatusRequest() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo,new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending a status information request.

        t.sendFunctionHighMomentaryStatusRequest();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a request for status.
        Assert.assertEquals("Throttle Information Request Message", "E3 08 00 03 E8", tc.outbound.elementAt(n).toString());

        // And the response to this message with the status.
        m = new XNetReply();
        m.setElement(0, 0xE3);
        m.setElement(1, 0x51);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0xB3);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();
    }


    @Test(timeout=1000)
    public void testSendFunctionGroup1() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending function group 1.

        t.sendFunctionGroup1();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 1 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 20 00 03 00 C7", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test(timeout=1000)
    public void testSendFunctionGroup2() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending function group 2.

        t.sendFunctionGroup2();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 2 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 21 00 03 00 C6", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test(timeout=1000)
    public void testSendFunctionGroup3() throws Exception {
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending function group 3.

        t.sendFunctionGroup3();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 3 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 22 00 03 00 C5", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test(timeout=1000)
   public void testSendFunctionGroup4() throws Exception {
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // before we send function group 4, make sure the software version is
        // set to version 3.6.  This test will hang otherwise.
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));


        // in this case, we are sending function group 4.

        t.sendFunctionGroup4();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 4 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 23 00 03 00 C4", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test(timeout=1000)
    public void testSendFunctionGroup4V35() throws Exception {
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending function group 4.

        t.sendFunctionGroup4();
        int count=0;
        while (n == tc.outbound.size() && count < 1000) {
          count++;
        } 

        // if the loop exited early, we sent the message, and we
        // shouldn't do that in this case.
        Assert.assertEquals("loop exited",1000,count);

    }

    @Test(timeout=1000)
    public void testSendFunctionGroup5() throws Exception {
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending function group 5.

        // before we send function group 5, make sure the software version is
        // set to version 3.6.  This test will hang otherwise.
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));

        t.sendFunctionGroup5();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 5 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 28 00 03 00 CF", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test(timeout=1000)
    public void testSendFunctionGroup5v35() throws Exception {
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending function group 5.

        t.sendFunctionGroup5();

        int count=0;
        while (n == tc.outbound.size() && count < 1000) {
          count++;
        } 

        // if the loop exited early, we sent the message, and we
        // shouldn't do that in this case.
        Assert.assertEquals("loop exited",1000,count);

    }

    @Test(timeout=1000)
    public void testSendMomentaryFunctionGroup1() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending momentary function group 1.

        t.sendMomentaryFunctionGroup1();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a momentary group 1 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 24 00 03 00 C3", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test(timeout=1000)
    public void testSendMomentaryFunctionGroup2() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending momentary function group 2.

        t.sendMomentaryFunctionGroup2();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a momentary group 2 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 25 00 03 00 C2", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test(timeout=1000)
    public void testSendMomentaryFunctionGroup3() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending momentary function group 3.

        t.sendMomentaryFunctionGroup3();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a monentary group 3 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 26 00 03 00 C1", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test(timeout=1000)
   public void testSendMomentaryFunctionGroup4() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // before we send function momentary group 4, make sure the software version is
        // set to version 3.6.  This test will hang otherwise.
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));


        // in this case, we are sending function momentary group 4.

        t.sendMomentaryFunctionGroup4();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a momentary group 4 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 27 00 03 00 C0", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test(timeout=1000)
    public void testSendMomentaryFunctionGroup5() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending momentary function group 5.

        // before we send function momentary group 5, make sure the software version is
        // set to version 3.6.  This test will hang otherwise.
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));

        t.sendMomentaryFunctionGroup5();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a momentary group 5 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 2C 00 03 00 CB", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();

    }

    @Test(timeout=1000)
    public void testGetDccAddress(){
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        Assert.assertEquals("XNetThrottle getDccAddress()",3,t.getDccAddress());
    }

    @Test(timeout=1000)
    public void testGetDccAddressLow(){
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        Assert.assertEquals("XNetThrottle getDccAddressLow()",3,t.getDccAddressLow());
    }

    @Test(timeout=1000)
    public void testGetDccAddressHigh(){
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        Assert.assertEquals("XNetThrottle getDccAddressHigh()",0,t.getDccAddressHigh());
    }

    @Test(timeout=1000)
    public void testGetLocoAddress(){
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        Assert.assertEquals("XNetThrottle getLocoAddress()",
                     new jmri.DccLocoAddress(3,false),t.getLocoAddress());
    }

    @Test(timeout=1000)
    public void setReverse() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending a request to change the direction.

        t.setIsForward(false);

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        Assert.assertEquals("Throttle Set Speed Message", "E4 13 00 03 00 F4", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

        // and finaly, verify that getIsForward() returns false, like we set it.
        Assert.assertFalse("Direction Set",t.getIsForward());
        t.throttleDispose();
    }

    @Test(timeout=1000)
    public void setForward() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending a request to change the direction.

        t.setIsForward(true);

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        Assert.assertEquals("Throttle Set Speed Message", "E4 13 00 03 80 74", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

        // and finaly, verify that getIsForward() returns false, like we set it.
        Assert.assertTrue("Direction Set",t.getIsForward());
        t.throttleDispose();
    }

    @Test(timeout=1000)
    public void sendEmergencyStop() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending an emergency stop message.

        t.sendEmergencyStop();

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        Assert.assertEquals("Throttle Emergency Stop Message", "92 00 03 91", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
        t.throttleDispose();
    }

    @Test(timeout=1000)
    public void setSpeedStep128() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending a request to change the speed step mode.

        t.setSpeedStepMode(jmri.DccThrottle.SpeedStepMode128);

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        Assert.assertEquals("Throttle Set Speed Message", "E4 13 00 03 00 F4", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        Assert.assertEquals("SpeedStepMode",jmri.DccThrottle.SpeedStepMode128,t.getSpeedStepMode());
        Assert.assertEquals("SpeedStep Increment",jmri.jmrix.AbstractThrottle.SPEED_STEP_128_INCREMENT,t.getSpeedIncrement(),0.0); // the speed increments are constants, so if there is deviation, that is an error.
        t.throttleDispose();
    }

    @Test(timeout=1000)
    public void setSpeedStep28() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending a request to change the speed step mode.

        t.setSpeedStepMode(jmri.DccThrottle.SpeedStepMode28);

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        Assert.assertEquals("Throttle Set Speed Message", "E4 12 00 03 00 F5", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        Assert.assertEquals("SpeedStepMode",jmri.DccThrottle.SpeedStepMode28,t.getSpeedStepMode());
        Assert.assertEquals("SpeedStep Increment",jmri.jmrix.AbstractThrottle.SPEED_STEP_28_INCREMENT,t.getSpeedIncrement(),0.0); // the speed increments are constants, so if there is deviation, that is an error.
        t.throttleDispose();
    }

    @Test(timeout=1000)
    public void setSpeedStep27() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending a request to change the speed step mode.

        t.setSpeedStepMode(jmri.DccThrottle.SpeedStepMode27);

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        Assert.assertEquals("Throttle Set Speed Message", "E4 11 00 03 00 F6", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        Assert.assertEquals("SpeedStepMode",jmri.DccThrottle.SpeedStepMode27,t.getSpeedStepMode());
        Assert.assertEquals("SpeedStep Increment",jmri.jmrix.AbstractThrottle.SPEED_STEP_27_INCREMENT,t.getSpeedIncrement(),0.0); // the speed increments are constants, so if there is deviation, that is an error.
        t.throttleDispose();
    }

    @Test(timeout=1000)
    public void setSpeedStep14() throws Exception {
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 00 74"));
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(memo , new jmri.DccLocoAddress(3, false), tc);
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
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
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");

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

        n = tc.outbound.size();
        t.message(m);

        // consume the error messge.
        jmri.util.JUnitAppender.assertErrorMessage("Unsupported Command Sent to command station");


	// Sending the not supported message should make the throttle change
        // state to idle, and then we can test what we really want to.

        // in this case, we are sending a request to change the speed step mode.

        t.setSpeedStepMode(jmri.DccThrottle.SpeedStepMode14);

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        Assert.assertEquals("Throttle Set Speed Message", "E4 10 00 03 00 F7", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..

        // and finaly, verify that getSpeedStepMode returns the right mode and
        // get speedIncrement reports the correct value.
        Assert.assertEquals("SpeedStepMode",jmri.DccThrottle.SpeedStepMode14,t.getSpeedStepMode());
        Assert.assertEquals("SpeedStep Increment",jmri.jmrix.AbstractThrottle.SPEED_STEP_14_INCREMENT,t.getSpeedIncrement(),0.0); // the speed increments are constants, so if there is deviation, that is an error.
        t.throttleDispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        // infrastructure objects
        tc = new XNetInterfaceScaffold(new LenzCommandStation());
        memo = new XNetSystemConnectionMemo(tc);
    }

    @After
    public void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
    }

}
