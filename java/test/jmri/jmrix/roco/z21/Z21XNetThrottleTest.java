package jmri.jmrix.roco.z21;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetThrottle;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.roco.z21.z21XNetThrottle class
 *
 * @author Paul Bender
 */
public class Z21XNetThrottleTest extends jmri.jmrix.roco.RocoXNetThrottleTest {

    @Test
    @Timeout(1000)
    @Override
    public void testCtor() {
        // infrastructure objects
        Z21XNetThrottle t = new Z21XNetThrottle(memo,tc);
        assertNotNull(t);
    }

    // Test the constructor with an address specified.
    @Test
    @Timeout(1000)
    @Override
    public void testCtorWithArg() {
        assertNotNull(instance);
    }

    // run the throttle through the initialization sequence,
    // without assertions, so post initialization tests can be
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

        // n = tc.outbound.size();
        t.message(m);

        // Sending the reply message should make the throttle change
        // state to idle, and then we can test what we really want to.
    }

    @Test
    @Timeout(1000)
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
        assertEquals( "E4 F8 00 03 00 1F", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    @Test
    @Timeout(1000)
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
        assertEquals( "E4 F8 00 03 05 1A", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    @Test
    @Timeout(1000)
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
        assertEquals( "E4 F8 00 03 09 16", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    @Test
    @Timeout(1000)
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
        assertEquals( "E4 F8 00 03 0D 12", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    @Test
    @Timeout(1000)
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
        assertEquals( "E4 F8 00 03 15 0A", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    @Override
    @Test
    @Timeout(1000)
    public void testSendStatusInformationRequest() {
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
        assertEquals( "E3 F0 00 03 10", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

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

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    /**
     * Z21 flavour of the parent test: the status request message and the
     * commands queued behind it differ, but the stuck-state behaviour being
     * checked is the one inherited from XNetThrottle.message(XNetReply).
     */
    @Override
    @Test
    @Timeout(1000)
    public void testUnknownLocoInfoResponseSubtypeDoesNotStallQueue() {
        int n = tc.outbound.size();
        Z21XNetThrottle t = (Z21XNetThrottle) instance;
        initThrottle(t, n);
        n = tc.outbound.size();

        // request the status, which leaves the throttle in THROTTLESTATSENT.
        t.sendStatusInformationRequest();
        assertEquals( "E3 F0 00 03 10", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // answer with an unrecognized LOCO_INFO_RESPONSE sub-type, which the
        // Z21 throttle hands over to the standard XpressNet handling.
        XNetReply m = new XNetReply();
        m.setElement(0, 0xE3);
        m.setElement(1, 0x60);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0x83);
        t.message(m);

        // the throttle has to be back to idle, so the next command reaches the
        // traffic controller instead of piling up in the internal queue.
        n = tc.outbound.size();
        t.setSpeedSetting(0.5f);

        assertEquals( n + 1, tc.outbound.size(),
            "Speed message sent after unrecognized LOCO_INFO_RESPONSE sub-type");
    }

    /**
     * Z21 flavour of the parent test. Speed and function commands are queued
     * with the THROTTLEIDLE state here, so the outstanding request holding the
     * queue back is a status request rather than a speed command.
     */
    @Override
    @Test
    @Timeout(1000)
    public void testWatchdogRestartsQueueWhenReplyNeverArrives() {
        int n = tc.outbound.size();
        Z21XNetThrottle t = (Z21XNetThrottle) instance;
        initThrottle(t, n);
        t.setWatchdogInterval(100);
        n = tc.outbound.size();

        // request the status, which leaves the throttle in THROTTLESTATSENT.
        t.sendStatusInformationRequest();
        assertEquals( "E3 F0 00 03 10", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // no reply at all: the next command is held back in the internal queue.
        final int held = tc.outbound.size();
        t.setSpeedSetting(0.5f);
        assertEquals( held, tc.outbound.size(),
            "Speed message held back while waiting for the status reply");

        // the watchdog has to give up and restart the queue.
        JUnitUtil.waitFor(() -> tc.outbound.size() > held, "watchdog restarted the queue");
        t.throttleDispose();
        JUnitAppender.assertWarnMessageStartsWith(
            "Throttle 3 - traffic controller at rest with a reply still due");
    }

    /**
     * Z21 flavour of the parent test, using a status request as the message
     * left unanswered for the same reason.
     */
    @Override
    @Test
    @Timeout(1000)
    public void testWatchdogRecoversFromUnretransmittedCsBusy() {
        int n = tc.outbound.size();
        Z21XNetThrottle t = (Z21XNetThrottle) instance;
        initThrottle(t, n);
        t.setWatchdogInterval(100);
        n = tc.outbound.size();

        // request the status, which leaves the throttle in THROTTLESTATSENT.
        t.sendStatusInformationRequest();
        assertEquals( "E3 F0 00 03 10", tc.outbound.elementAt(n).toString(),
            "Throttle Information Request Message");

        // the command station answers busy, and nothing else ever comes.
        t.message(new XNetReply("61 81 E0"));

        final int held = tc.outbound.size();
        t.setSpeedSetting(0.5f);
        assertEquals( held, tc.outbound.size(),
            "Speed message held back while waiting for the status reply");

        JUnitUtil.waitFor(() -> tc.outbound.size() > held, "watchdog restarted the queue");
        t.throttleDispose();
        JUnitAppender.assertWarnMessageStartsWith(
            "Throttle 3 - traffic controller at rest with a reply still due");
    }

    @Override
    @Test
    @Timeout(1000)
    public void testSendEmergencyStop() {
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
        assertEquals( "E4 13 00 03 00 F4", tc.outbound.elementAt(n).toString(),
            "Throttle Emergency Stop Message");

        // And the response to this message is a command successfully received message.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new XNetInterfaceScaffold(new RocoZ21CommandStation());
        memo = new XNetSystemConnectionMemo(tc);
        memo.setThrottleManager(new Z21XNetThrottleManager(memo)); 
        jmri.InstanceManager.setDefault(jmri.ThrottleManager.class,memo.getThrottleManager());
        instance = new Z21XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
    }

    @AfterEach
    @Override
    public void tearDown() {
        ((Z21XNetThrottle)instance).throttleDispose();
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

}
