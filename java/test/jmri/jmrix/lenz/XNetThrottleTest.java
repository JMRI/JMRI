package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetThrottleTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetThrottle class
 *
 * @author	Paul Bender
 */
public class XNetThrottleTest extends TestCase {

    public void testCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), tc);
        Assert.assertNotNull(t);
    }

    // Test the constructor with an address specified.
    public void testCtorWithArg() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
        Assert.assertNotNull(t);
    }

    // Test the initilization sequence.
    public void testInitSequence() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    }

    public void testSendStatusInformationRequest() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    }

    public void testSendFunctionStatusInformationRequest() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    public void testSendFunctionGroup1() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    }

    public void testSendFunctionGroup2() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    }

    public void testSendFunctionGroup3() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    }

   public void testSendFunctionGroup4() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    }

    public void testSendFunctionGroup5() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    }

    public void testSendMomentaryFunctionGroup1() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    }

    public void testSendMomentaryFunctionGroup2() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    }

    public void testSendMomentaryFunctionGroup3() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    }

   public void testSendMomentaryFunctionGroup4() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    }

    public void testSendMomentaryFunctionGroup5() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
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

    }





    public void testGetDccAddress(){
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
        assertEquals("XNetThrottle getDccAddress()",3,t.getDccAddress());
    }

    public void testGetDccAddressLow(){
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
        assertEquals("XNetThrottle getDccAddressLow()",3,t.getDccAddressLow());
    }

    public void testGetDccAddressHigh(){
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
        assertEquals("XNetThrottle getDccAddressHigh()",0,t.getDccAddressHigh());
    }

    public void testGetLocoAddress(){
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
        assertEquals("XNetThrottle getLocoAddress()",
                     new jmri.DccLocoAddress(3,false),t.getLocoAddress());
    }



    // from here down is testing infrastructure
    public XNetThrottleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetThrottleTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetThrottleTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
