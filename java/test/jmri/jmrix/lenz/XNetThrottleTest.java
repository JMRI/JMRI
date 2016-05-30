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

    public void testGetDccAddress(){
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
        assertEquals("XNetThrottle getDccAddress()",3,t.getDccAddress());
    }

    public void testGetDccAddressLow(){
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
        assertEquals("XNetThrottle getDccAddressLow()",3,t.getDccAddressLow());
    }

    public void testGetDccAddressHigh(){
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        int n = tc.outbound.size();
        XNetThrottle t = new XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
        assertEquals("XNetThrottle getDccAddressHigh()",0,t.getDccAddressHigh());
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
