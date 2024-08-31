package jmri.jmrix.roco;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetThrottle;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.roco.RocoXNetThrottle class
 *
 * @author Paul Bender
 */
public class RocoXNetThrottleTest extends jmri.jmrix.lenz.XNetThrottleTest {

    @Test
    @Timeout(1000)
    @Override
    public void testCtor() {
        // infrastructure objects
        RocoXNetThrottle t = new RocoXNetThrottle(memo, tc);
        Assert.assertNotNull(t);
    }

    // run the throttle through the initialization sequence,
    // without assertions, so post initialization tests can be
    // performed.
    @Override
    protected void initThrottle(XNetThrottle t, int n) {
        // before we send any commands, make sure the hardware type is set
        // correctly (0x10 is a MultiMaus).
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 36 10 64"));
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

        // n = tc.outbound.size();
        t.message(m);

        // this should put the throttle into idle state,
        // and then we can test what we really want to.
    }

    // Test the constructor with an address specified.
    @Test
    @Timeout(1000)
    @Override
    public void testCtorWithArg() throws Exception {
        Assert.assertNotNull(instance);
    }

    // Test the initialization sequence.
    @Override
    @Test
    @Timeout(1000)
    @Disabled("Test requires further development")
    public void testInitSequenceNormalUnitSpeedStep128() throws Exception {
    }

    @Override
    @Test
    @Timeout(1000)
    @Disabled("Test requires further development")
    public void initSequenceNormalUnitSpeedStep14() throws Exception {
    }

    @Override
    @Test
    @Timeout(1000)
    @Disabled("Test requires further development")
    public void initSequenceMUAddress28SpeedStep() throws Exception {
    }

    @Override
    @Test
    @Timeout(1000)
    @Disabled("Test requires further development")
    public void initSequenceMuedUnitSpeedStep128() throws Exception {
    }

    @Override
    @Test
    @Timeout(1000)
    @Disabled("Test requires further development")
    public void initSequenceDHUnitSpeedStep27() throws Exception {
    }

    @Override
    @NotApplicable("only one software version for Roco")
    @Test
    @Timeout(1000)
    public void testSendFunctionGroup5v35() throws Exception {
    }

    @Override
    @NotApplicable("only one software version for Roco")
    @Test
    @Timeout(1000)
    public void testSendFunctionGroup4v35() {
    }

    @Override
    @NotApplicable("not supported by Roco")
    @Test
    @Timeout(1000)
    public void testSendMomentaryFunctionGroup1() {
    }

    @Override
    @NotApplicable("not supported by Roco")
    @Test
    @Timeout(1000)
    public void testSendMomentaryFunctionGroup2() {
    }

    @Override
    @NotApplicable("not supported by Roco")
    @Test
    @Timeout(1000)
    public void testSendMomentaryFunctionGroup3() {
    }

    @Override
    @NotApplicable("not supported by Roco")
    @Test
    @Timeout(1000)
    public void testSendMomentaryFunctionGroup4() {
    }

    @Override
    @NotApplicable("not supported by Roco")
    @Test
    @Timeout(1000)
    public void testSendMomentaryFunctionGroup5() {
    }

    @Override
    @NotApplicable("not supported by Roco")
    @Test
    @Timeout(1000)
    public void testSendFunctionHighMomentaryStatusRequest() throws Exception {
    }

    @Override
    @NotApplicable("never sent by Roco throttle support")
    @Test
    @Timeout(1000)
    public void testSendFunctionStatusInformationRequest() {
    }

    @Override
    @NotApplicable("never sent by Roco throttle support")
    @Test
    @Timeout(1000)
    public void testSendFunctionHighStatusInformationRequest() {
    }

    @Override
    @Test
    @Timeout(1000)
    public void sendEmergencyStop() throws Exception {
        int n = tc.outbound.size();
        RocoXNetThrottle t = (RocoXNetThrottle) instance;
        initThrottle(t, n);
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

        // n = tc.outbound.size();
        t.message(m);
        // which sets the status back state back to idle..
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        tc = new XNetInterfaceScaffold(new RocoCommandStation());
        tc.getCommandStation().setCommandStationSoftwareVersion(new XNetReply("63 21 35 10 67"));
        memo = new XNetSystemConnectionMemo(tc);
        memo.setThrottleManager(new RocoXNetThrottleManager(memo));
        jmri.InstanceManager.setDefault(jmri.ThrottleManager.class, memo.getThrottleManager());
        instance = new RocoXNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        // no need to dispose of instance
        if (memo.getThrottleManager() != null) {
            memo.getThrottleManager().dispose();
        }
        memo.dispose();
        memo = null;
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

}
