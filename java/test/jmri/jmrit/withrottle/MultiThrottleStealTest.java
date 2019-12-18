package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;
import org.junit.*;

/**
 * Test stealing behavior of MultiThrottle
 *
 * @author	Paul Bender Copyright (C) 2018
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
        // and the sequence continues as normal.
        jmri.util.JUnitUtil.waitFor(() -> {
            return tcls.hasAddressBeenFound();
        }, " Address not found");
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
        // and the sequence continues as normal.
        Assert.assertTrue("Address Found", tcls.hasAddressBeenFound());
        // then release it.
        throttle.handleMessage("-L4321<;>r");
        Assert.assertTrue("Address Released", tcls.hasAddressBeenReleased());
        Assert.assertEquals("outgoing yymessage after throttle release", "MA-L4321<;>", cis.getLastPacket());
        JUnitAppender.assertWarnMessage("Throttle request failed for 1234(L) because Steal Required.");
        JUnitAppender.assertWarnMessage("Throttle request failed for 4321(L) because Steal Required.");
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initRosterConfigManager();
        // these tests use the StealingThrottleManager.
        jmri.ThrottleManager m = new jmri.managers.StealingThrottleManager();
        jmri.InstanceManager.setThrottleManager(m);
        cis = new ControllerInterfaceScaffold();
        tcls = new ThrottleControllerListenerScaffold();
        throttle = new MultiThrottle('A', tcls, cis);
    }

    @After
    public void tearDown() {
        cis = null;
        tcls = null;
        throttle = null;
        JUnitUtil.tearDown();
    }
}
