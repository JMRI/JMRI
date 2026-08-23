package jmri.jmrit.withrottle;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test stealing behavior of MultiThrottle
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class MultiThrottleStealTest {

    private ControllerInterfaceScaffold cis = null;
    private ThrottleControllerListenerScaffold tcls = null;
    private MultiThrottle throttle = null;

    @Test
    public void testSetAndReleaseLongAddressWithSteal() {
        // set the address
        throttle.handleMessage("+L1234<;>L1234");
        Assert.assertFalse("Address Found", tcls.hasAddressBeenFound());
        // the throttle manager send a steal request, which triggers a message 
        // from the controller to the device 
        Assert.assertEquals("outgoing message after throttle request", "MASL1234<;>L1234", cis.getLastPacket());
        // normally the ThrottleControllerListener notifies the throttle of 
        // a canceled request.
        throttle.canceledThrottleRequest("L1234");
        // the device then confirms the steal.
        throttle.handleMessage("SL1234<;>L1234");
        // FIELD REPORT (Andrew Deak): waiting on hasAddressBeenFound() alone
        // is NOT enough -- see the field report in testRefuseOneStealOne()
        // below for why. Wait for the actual last packet sent instead.
        jmri.util.JUnitUtil.waitFor(() -> {
            return "MAAL1234<;>s1".equals(cis.getLastPacket());
        }, "wait for throttle status packet");
        Assert.assertTrue("Address Found", tcls.hasAddressBeenFound());
        // then release it.
        throttle.handleMessage("-L1234<;>r");
        Assert.assertEquals("outgoing yymessage after throttle release", "MA-L1234<;>", cis.getLastPacket());
        Assert.assertTrue("Address Released", tcls.hasAddressBeenReleased());
        JUnitAppender.assertWarnMessage("Throttle request failed for 1234(L) because Steal Required.");
    }

    @Test
    public void testRefuseOneStealOne() {
        // set the address
        throttle.handleMessage("+L1234<;>L1234");
        Assert.assertFalse("Address Found", tcls.hasAddressBeenFound());
        // the throttle manager send a steal request, which triggers a message 
        // from the controller to the device 
        Assert.assertEquals("outgoing message after throttle request", "MASL1234<;>L1234", cis.getLastPacket());
        // to refuse the steal, we have to send a different address
        throttle.handleMessage("+L4321<;>L4321");
        Assert.assertFalse("Address Found", tcls.hasAddressBeenFound());
        // from the controller to the device 
        Assert.assertEquals("outgoing message after throttle request", "MASL4321<;>L4321", cis.getLastPacket());
        // normally the ThrottleControllerListener notifies the throttle of 
        // a canceled request.
        throttle.canceledThrottleRequest("L4321");
        // the device then confirms the steal.
        throttle.handleMessage("SL4321<;>L4321");
        // FIELD REPORT (Andrew Deak): waiting on hasAddressBeenFound() alone
        // is NOT enough -- ThrottleController.notifyThrottleFound() (invoked
        // from the now-deferred AbstractThrottleManager callback) notifies
        // listeners (setting this flag) BEFORE its own later
        // sendCurrentSpeed()/sendCurrentDirection()/sendSpeedStepMode() calls
        // run, all synchronously within the same method but AFTER the flag
        // flips. Confirmed live: intermittently observed a stray in-progress
        // packet (e.g. a function-state packet) as the "last packet" instead
        // of the release ack sent moments later in this test, depending
        // purely on exactly when this wait's polling happened to land
        // relative to that still-in-progress call. Wait for the actual last
        // packet that method sends instead.
        JUnitUtil.waitFor(() -> "MAAL4321<;>s1".equals(cis.getLastPacket()), "wait for throttle status packet");
        // and the sequence continues as normal.
        Assert.assertTrue("Address Found", tcls.hasAddressBeenFound());
        // then release it.
        throttle.handleMessage("-L4321<;>r");
        Assert.assertTrue("Address Released", tcls.hasAddressBeenReleased());
        Assert.assertEquals("outgoing yymessage after throttle release", "MA-L4321<;>", cis.getLastPacket());
        JUnitAppender.assertWarnMessage("Throttle request failed for 1234(L) because Steal Required.");
        JUnitAppender.assertWarnMessage("Throttle request failed for 4321(L) because Steal Required.");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initRosterConfigManager();
        // these tests use the StealingThrottleManager.
        var memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        jmri.ThrottleManager m = new jmri.managers.StealingThrottleManager(memo);
        memo.store(m, jmri.ThrottleManager.class);
        jmri.InstanceManager.setThrottleManager(m);
        cis = new ControllerInterfaceScaffold();
        tcls = new ThrottleControllerListenerScaffold();
        throttle = new MultiThrottle('A', tcls, cis);
    }

    @AfterEach
    public void tearDown() {
        cis = null;
        tcls = null;
        throttle = null;
        JUnitUtil.tearDown();
    }
}
