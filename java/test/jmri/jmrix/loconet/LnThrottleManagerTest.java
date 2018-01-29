package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.ThrottleListener;
import jmri.DccThrottle;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private DccThrottle throttle;

    boolean failedThrottleRequest = false;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",tm);
    }

    @Test
    public void testCreateLnThrottleRunAndRelease() {
        ThrottleListener throtListen = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                log.error("created a throttle");
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                log.error("Throttle request failed for " + address + " because " + reason);
                failedThrottleRequest = true;
            }

            @Override
            public void notifyStealThrottleRequired(jmri.LocoAddress address){
                // this is an automatically stealing impelementation.
                InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, true);
            }
        };
        tm.requestThrottle(1203, throtListen);

        Assert.assertEquals("address request message",
                "BF 09 33 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x00, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x53});
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertEquals("null move",
                "BA 11 11 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x30, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x00});
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertEquals("write Throttle ID",
                "EF 0E 11 30 33 00 00 07 00 09 00 71 02 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        cmdStationReply = new LocoNetMessage(new int[] {
                0xb4, 0x6f, 0x7f, 0x5B});
        lnis.sendTestMessage(cmdStationReply);

        Assert.assertNotNull("have created a throttle", throttle);
        Assert.assertEquals("is LnThrottle", throttle.getClass(), jmri.jmrix.loconet.LocoNetThrottle.class);
        Assert.assertEquals("throttleId is set", ((jmri.jmrix.loconet.LocoNetThrottle) throttle).slot.id(),0x171);
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");

        throttle.setSpeedSetting(0.125f);

        Assert.assertEquals("set speed to one eighth",
                "A0 11 1A 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        throttle.release(throtListen);

        Assert.assertEquals("slot is set to 'common' status",
                "B5 11 10 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testCreateLnThrottleRunAndDispatch() {
        ThrottleListener throtListen = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                log.error("created a throttle");
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                log.error("Throttle request failed for " + address + " because " + reason);
                failedThrottleRequest = true;
            }

            @Override
            public void notifyStealThrottleRequired(jmri.LocoAddress address){
                // this is an automatically stealing impelementation.
                InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, true);
            }
        };
        tm.requestThrottle(1203, throtListen);

        Assert.assertEquals("address request message",
                "BF 09 33 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x00, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x53});
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertEquals("null move",
                "BA 11 11 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x30, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x00});
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertEquals("write Throttle ID",
                "EF 0E 11 30 33 00 00 07 00 09 00 71 02 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        cmdStationReply = new LocoNetMessage(new int[] {
                0xb4, 0x6f, 0x7f, 0x5B});
        lnis.sendTestMessage(cmdStationReply);

        Assert.assertNotNull("have created a throttle", throttle);
        Assert.assertEquals("is LnThrottle", throttle.getClass(), jmri.jmrix.loconet.LocoNetThrottle.class);
        Assert.assertEquals("throttleId is set", ((jmri.jmrix.loconet.LocoNetThrottle) throttle).slot.id(),0x171);
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");

        throttle.setSpeedSetting(0.125f);

        Assert.assertEquals("set speed to one eighth",
                "A0 11 1A 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        throttle.dispatch(throtListen);

        Assert.assertEquals("slot is set to 'common' status",
                "B5 11 10 00",
                lnis.outbound.elementAt(lnis.outbound.size()-1).toString());

        JUnitUtil.waitFor(()->{return 5 < lnis.outbound.size();},"didn't get the 6th LocoNet message");

        Assert.assertEquals("Expect the slot to be dispatched",
                "BA 11 00 00",
                lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    LocoNetInterfaceScaffold lnis;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnis = new LocoNetInterfaceScaffold();
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100,false,false);
        memo.configureManagers();
        tm = new LnThrottleManager(memo);
    }

    @After
    public void tearDown() {
        ((LnThrottleManager)tm).dispose();
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LnThrottleManagerTest.class);

}
