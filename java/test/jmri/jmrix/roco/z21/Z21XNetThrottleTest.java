package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetThrottle;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.roco.z21.z21XNetThrottle class
 *
 * @author	Paul Bender
 */
public class Z21XNetThrottleTest extends jmri.jmrix.roco.RocoXNetThrottleTest {

    @Test(timeout=1000)
    @Override
    public void testCtor() {
        // infrastructure objects
        Z21XNetThrottle t = new Z21XNetThrottle(memo,tc);
        Assert.assertNotNull(t);
    }

    // Test the constructor with an address specified.
    @Test(timeout=1000)
    @Override
    public void testCtorWithArg() throws Exception {
        Assert.assertNotNull(instance);
    }

    // run the throttle through the initilization sequence,
    // without assertions, so post initilization tests can be
    // performed.
    @Override
    protected void initThrottle(XNetThrottle t,int n){
        if(n==0) {
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

        // Sending the reply message should make the throttle change
        // state to idle, and then we can test what we really want to.
    }

    @Test(timeout=1000)
    @Override
    public void testSendFunctionGroup1() {
        int n = tc.outbound.size();
        Z21XNetThrottle t = (Z21XNetThrottle)instance;
        initThrottle(t,n);
        n = tc.outbound.size();

        // in this case, we are sending function group 1.
        t.sendFunctionGroup1();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 1 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 F8 00 03 00 1F", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    @Test(timeout=1000)
    @Override
    public void testSendFunctionGroup2() {
        int n = tc.outbound.size();
        Z21XNetThrottle t = (Z21XNetThrottle)instance;
        initThrottle(t,n);
        n = tc.outbound.size();

        // in this case, we are sending function group 2.

        t.sendFunctionGroup2();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 2 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 F8 00 03 05 1A", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    @Test(timeout=1000)
    @Override
    public void testSendFunctionGroup3() {
        int n = tc.outbound.size();
        Z21XNetThrottle t = (Z21XNetThrottle)instance;
        initThrottle(t,n);
        n = tc.outbound.size();

        // in this case, we are sending function group 3.

        t.sendFunctionGroup3();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 3 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 F8 00 03 09 16", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    @Test(timeout=1000)
    @Override
    public void testSendFunctionGroup4() {
        int n = tc.outbound.size();
        Z21XNetThrottle t = (Z21XNetThrottle)instance;
        initThrottle(t,n);
        n = tc.outbound.size();

        // in this case, we are sending function group 4.

        t.sendFunctionGroup4();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 4 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 F8 00 03 0D 12", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    @Test(timeout=1000)
    @Override
    public void testSendFunctionGroup5(){
        int n = tc.outbound.size();
        Z21XNetThrottle t = (Z21XNetThrottle)instance;
        initThrottle(t,n);
        n = tc.outbound.size();

        // in this case, we are sending function group 5.
        t.sendFunctionGroup5();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a group 5 request.
        Assert.assertEquals("Throttle Information Request Message", "E4 F8 00 03 15 0A", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    @Override
    @Test(timeout=1000)
    public void testSendStatusInformationRequest() throws Exception {
        int n = tc.outbound.size();
        Z21XNetThrottle t = (Z21XNetThrottle)instance;
        initThrottle(t,n);
        n = tc.outbound.size();
        // in this case, we are sending a status information request.

        t.sendStatusInformationRequest();
        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
        //The first thing on the outbound queue should be a request for status.
        Assert.assertEquals("Throttle Information Request Message", "E3 F0 00 03 10", tc.outbound.elementAt(n).toString());

        // And the response to this is a message with the status.
        XNetReply m = new XNetReply();
        m.setElement(0, 0xE7);
        m.setElement(1, 0x00);
        m.setElement(2, 0x03);
        m.setElement(3, 0x00);
        m.setElement(4, 0x00);
        m.setElement(5, 0x00);
        m.setElement(6, 0x00);
        m.setElement(7, 0x00);
        m.setElement(8, 0xE4);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    @Override
    @Test(timeout=1000)
    public void sendEmergencyStop() throws Exception {
        int n = tc.outbound.size();
        Z21XNetThrottle t = (Z21XNetThrottle)instance;
        initThrottle(t,n);
        n = tc.outbound.size();

        // in this case, we are sending an emergency stop message.

        t.sendEmergencyStop();

        while (n == tc.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        //The first thing on the outbound queue should be a throttle set speed message.
        Assert.assertEquals("Throttle Emergency Stop Message", "E4 13 00 03 00 F4", tc.outbound.elementAt(n).toString());

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        tc = new XNetInterfaceScaffold(new RocoZ21CommandStation());
        memo = new XNetSystemConnectionMemo(tc);
        memo.setThrottleManager(new Z21XNetThrottleManager(memo)); 
        jmri.InstanceManager.setDefault(jmri.ThrottleManager.class,memo.getThrottleManager());
        instance = new Z21XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        ((Z21XNetThrottle)instance).throttleDispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
