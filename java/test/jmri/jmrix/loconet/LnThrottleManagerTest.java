package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.ThrottleListener;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author B. Milhaupt, Copyright (C) 2018
 */
public class LnThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private DccThrottle throttle;
    private DccThrottle throttle2;
    private DccThrottle throttle3;

    boolean failedThrottleRequest = false;
    boolean failedThrottleRequest2 = false;
    boolean failedThrottleRequest3 = false;

    int flagGotStealRequest = -1;
    int flagGotStealRequest2 = -1;
    int flagGotStealRequest3 = -1;

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
                // this is a never-stealing impelementation.
                InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, false);
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

        JUnitUtil.waitFor(()->{return 4 < lnis.outbound.size();},"didn't get the 6th LocoNet message");

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
                // this is a never-stealing impelementation.
                InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, false);
            }
        };
        tm.requestThrottle(1203, throtListen);

        Assert.assertEquals("address request message",
                "BF 09 33 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertEquals("count is correct", 1, lnis.outbound.size());
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x00, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x53});
        lnis.sendTestMessage(cmdStationReply);
        memo.getSlotManager().message(lnis.outbound.elementAt(1));
        Assert.assertEquals("count is correct", 2, lnis.outbound.size());

        Assert.assertEquals("Throttle manager sends a 'null move'",
                "BA 11 11 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x30, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x00});
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertEquals("write Throttle ID",
                "EF 0E 11 30 33 00 00 07 00 09 00 71 02 00",
                lnis.outbound.elementAt(2).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        cmdStationReply = new LocoNetMessage(new int[] {
                0xb4, 0x6f, 0x7f, 0x5B});
        lnis.sendTestMessage(cmdStationReply);
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertNotNull("have created a throttle", throttle);
        Assert.assertEquals("is LnThrottle", throttle.getClass(), jmri.jmrix.loconet.LocoNetThrottle.class);
        Assert.assertEquals("throttleId is set", ((jmri.jmrix.loconet.LocoNetThrottle) throttle).slot.id(),0x171);
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");

        throttle.setSpeedSetting(0.125f);

        Assert.assertEquals("set speed to one eighth",
                "A0 11 1A 00",
                lnis.outbound.elementAt(3).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(3));
        Assert.assertEquals("check that speed was set", 26, memo.getSlotManager()._slots[17].speed());

        throttle.dispatch(throtListen);

        Assert.assertEquals("slot is set to 'common' status",
                "B5 11 10 00",
                lnis.outbound.elementAt(4).toString());

        JUnitUtil.waitFor(()->{return 5 < lnis.outbound.size();},"didn't get the 5th LocoNet message");
        Assert.assertEquals("check count of sent messages", 5, lnis.outbound.size()-1);

        Assert.assertEquals("Expect the slot to be dispatched",
                "BA 11 00 00",
                lnis.outbound.elementAt(5).toString());

        Assert.assertEquals("slot speed not zeroed", 26, memo.getSlotManager()._slots[17].speed());
    }


    @Test
    public void testCreateLnThrottleRunAndDispose() {
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
                // this is a never-stealing impelementation.
                InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, false);
            }
        };
        tm.requestThrottle(1203, throtListen);

        Assert.assertEquals("address request message",
                "BF 09 33 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertEquals("count is correct", 1, lnis.outbound.size());
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x00, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x53});
        lnis.sendTestMessage(cmdStationReply);
        memo.getSlotManager().message(lnis.outbound.elementAt(1));
        Assert.assertEquals("count is correct", 2, lnis.outbound.size());

        Assert.assertEquals("Throttle manager sends a 'null move'",
                "BA 11 11 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x30, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x00});
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertEquals("write Throttle ID",
                "EF 0E 11 30 33 00 00 07 00 09 00 71 02 00",
                lnis.outbound.elementAt(2).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        cmdStationReply = new LocoNetMessage(new int[] {
                0xb4, 0x6f, 0x7f, 0x5B});
        lnis.sendTestMessage(cmdStationReply);
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertNotNull("have created a throttle", throttle);
        Assert.assertEquals("is LnThrottle", throttle.getClass(), jmri.jmrix.loconet.LocoNetThrottle.class);
        Assert.assertEquals("throttleId is set", ((jmri.jmrix.loconet.LocoNetThrottle) throttle).slot.id(),0x171);
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");

        throttle.setSpeedSetting(0.125f);

        JUnitUtil.waitFor(()->{return 3 < lnis.outbound.size();},"didn't get the 4th LocoNet message");
        Assert.assertEquals("set speed to one eighth",
                "A0 11 1A 00",
                lnis.outbound.elementAt(3).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(3));
        Assert.assertEquals("check that speed was set", 26, memo.getSlotManager()._slots[17].speed());

        ((LocoNetThrottle)throttle).throttleDispose();

        JUnitUtil.waitFor(()->{return 4 < lnis.outbound.size();},"didn't get the 5th LocoNet message");
        Assert.assertEquals("slot is set to 'common' status",
                "B5 11 10 00",
                lnis.outbound.elementAt(4).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(4));

        Assert.assertEquals("check count of sent messages", 5, lnis.outbound.size());

        Assert.assertEquals("slot speed not zeroed", 26, memo.getSlotManager()._slots[17].speed());
    }



    @Test
    public void testCreateLnThrottleStealScenario1() {

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
                log.error("Throttle request failed for " + address + " because " + reason);
            }

            @Override
            public void notifyStealThrottleRequired(jmri.LocoAddress address){
                // this is a never-stealing impelementation.
                flagGotStealRequest = address.getNumber();
                InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, false);
                log.error("1: Got a steal request");
            }
        };
        tm.requestThrottle(129, throtListen);

        Assert.assertEquals("address request message",
                "BF 01 01 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertEquals("count is correct", 1, lnis.outbound.size());
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x30, 0x01, 0x0, 0x0, 0x7, 0x0, 0x01, 0x0, 0x0, 0x1, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertEquals("Got a steal request", 129, flagGotStealRequest);
        jmri.util.JUnitAppender.assertWarnMessage("slot 17 address 129 is already in-use.");
        jmri.util.JUnitAppender.assertWarnMessage("failedThrottleRequest with zero-length listeners: 129(L)");
        jmri.util.JUnitAppender.assertErrorMessage("1: Got a steal request");

        throtListen = null;

    }

    @Test
    public void testCreateLnThrottleStealScenario2() {

        ThrottleListener throtListen = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                log.error("created a throttle");
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                failedThrottleRequest = true;
                log.error("Throttle request failed for " + address + " because " + reason);
            }

            @Override
            public void notifyStealThrottleRequired(jmri.LocoAddress address){
                // this is a never-stealing impelementation.
                flagGotStealRequest = address.getNumber();
                InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, false);
                log.error("2: Got a steal request");
            }
        };
        tm.requestThrottle(5, throtListen);

        Assert.assertEquals("address request message",
                "BF 00 05 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertEquals("count is correct", 1, lnis.outbound.size());
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x0c, 0x30, 0x05, 0x0, 0x0, 0x7, 0x0, 0x00, 0x0, 0x2, 0x0, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertEquals("Got a steal request", 5, flagGotStealRequest);
        jmri.util.JUnitAppender.assertWarnMessage("slot 12 address 5 is already in-use.");
        jmri.util.JUnitAppender.assertWarnMessage("failedThrottleRequest with zero-length listeners: 5(S)");
        jmri.util.JUnitAppender.assertErrorMessage("2: Got a steal request");
        throtListen = null;
    }


    @Test
    public void testCreateLnThrottleStealScenario3() {

        ThrottleListener throtListen = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                log.error("created a throttle");
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                failedThrottleRequest = true;
                log.error("Throttle request failed for " + address + " because " + reason);
            }

            @Override
            public void notifyStealThrottleRequired(jmri.LocoAddress address){
                // this is a never-stealing impelementation.
                flagGotStealRequest = address.getNumber();
                InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, false);
                log.error("3: Got a steal request");
            }
        };
        tm.requestThrottle(85, throtListen);

        Assert.assertEquals("address request message",
                "BF 00 55 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertEquals("count is correct", 1, lnis.outbound.size());
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x60, 0x30, 0x55, 0x0, 0x0, 0x7, 0x0, 0x00, 0x0, 0x2, 0x70, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertEquals("Got a steal request", 85, flagGotStealRequest);
        jmri.util.JUnitAppender.assertWarnMessage("slot 96 address 85 is already in-use.");
        jmri.util.JUnitAppender.assertWarnMessage("failedThrottleRequest with zero-length listeners: 85(S)");
        jmri.util.JUnitAppender.assertErrorMessage("3: Got a steal request");
        throtListen = null;
    }

    @Test
    public void testCreateLnThrottleStealScenario4() {
        throttle = null;
        tm = memo.throttleManager;

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
                // this is an always-stealing impelementation.
                flagGotStealRequest = address.getNumber();
                log.debug("going to steal loco {}", address);
                tm.stealThrottleRequest(address, this, true);
            }
        };
        tm.requestThrottle(256, throtListen);

        Assert.assertEquals("address request message",
                "BF 02 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertEquals("count is correct", 1, lnis.outbound.size());
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x8, 0x30, 0x00, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertEquals("Got a steal request", 256, flagGotStealRequest);
        jmri.util.JUnitAppender.assertWarnMessage("slot 8 address 256 is already in-use.");
        Assert.assertNotNull("Throttle should be created and non-null", throttle);
        jmri.util.JUnitAppender.assertWarnMessage("user agreed to steal address 256, but no code is in-place to handle the 'steal' (yet)");
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");
        tm.releaseThrottle(throttle, throtListen);
        throtListen = null;
    }

    @Test
    public void testCreateLnThrottleStealScenario5() {
        throttle = null;
        tm = memo.throttleManager;

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
                // this is an always-stealing impelementation.
                flagGotStealRequest = address.getNumber();
                log.debug("going to steal loco {}", address);
                tm.stealThrottleRequest(address, this, true);
            }
        };
        tm.requestThrottle(257, throtListen);

        Assert.assertEquals("address request message",
                "BF 02 01 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertEquals("count is correct", 1, lnis.outbound.size());
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x8, 0x10, 0x01, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertNotNull("Throttle should be created and non-null", throttle);
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");
        tm.releaseThrottle(throttle, throtListen);
        throtListen = null;
    }

    @Test
    public void testCreateLnThrottleStealScenario7() {
        throttle = null;
        tm = memo.throttleManager;

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
                // this is a never-stealing impelementation.
                flagGotStealRequest = address.getNumber();
                log.debug("going to steal loco {}", address);
                tm.stealThrottleRequest(address, this, false);
            }
        };
        tm.requestThrottle(259, throtListen);

        Assert.assertEquals("address request message",
                "BF 02 03 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertEquals("count is correct", 1, lnis.outbound.size());
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x8, 0x00, 0x03, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertNotNull("Throttle should be created and non-null", throttle);
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");
        tm.releaseThrottle(throttle, throtListen);
        throtListen = null;
    }



    @Test
    public void testShareSingleLnThrottleScenario1() {
        // test case:
        // . acquire loco X to "throttle"
        // . acquire loco X to "throttle2"
        // . set speed for loco X
        // . dispatch "throttle2"
        // . release "throttle2"
        // . release "throttle"

        throttle = null;
        tm = memo.throttleManager;

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
                // this is a never-stealing impelementation.
                flagGotStealRequest = address.getNumber();
                log.debug("going to steal loco {}", address);
                tm.stealThrottleRequest(address, this, false);
            }
        };

        ThrottleListener throtListen2 = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle2 = t;
                log.error("created a throttle2");
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                log.error("Throttle2 request failed for " + address + " because " + reason);
                failedThrottleRequest2 = true;
            }

            @Override
            public void notifyStealThrottleRequired(jmri.LocoAddress address){
                // this is a never-stealing impelementation.
                flagGotStealRequest2 = address.getNumber();
                log.debug("Throttle2 going to steal loco {}", address);
                tm.stealThrottleRequest(address, this, false);
            }
        };

        tm.requestThrottle(260, throtListen);

        Assert.assertEquals("address request message",
                "BF 02 04 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertEquals("count is correct", 1, lnis.outbound.size());
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x9, 0x00, 0x04, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertNotNull("Throttle should be created and non-null", throttle);
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");

        int netTxMsgCount = lnis.outbound.size()-1;

        tm.requestThrottle(260, throtListen2);  // An additional user of the same throttle
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle2");

        Assert.assertNotNull("Throttle should be created and non-null", throttle2);
        Assert.assertEquals("both throttle users point to the same throttle object",
                throttle, throttle2);
        Assert.assertEquals("no new LocoNet traffic generated", netTxMsgCount, lnis.outbound.size()-1);

        throttle.setSpeedSetting(0.5f);
        Assert.assertEquals("sent speed message",
                "A0 09 44 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString());
        Assert.assertEquals("only one new loconet message", netTxMsgCount +1, lnis.outbound.size()-1);
        tm.releaseThrottle(throttle2, throtListen2);
        Assert.assertEquals("No loconet traffic at throttle2 release", netTxMsgCount +1, lnis.outbound.size()-1);

        Assert.assertTrue("Address still required",
                InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(260, true)));

        tm.releaseThrottle(throttle, throtListen);
        Assert.assertFalse("Address no longer required",
                InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(260, true)));

        Assert.assertEquals("One loconet message sent at throttle release", netTxMsgCount +2, lnis.outbound.size()-1);
        Assert.assertEquals("sent set slot status to COMMON message",
                "B5 09 10 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString());

        throtListen = null;
        throtListen2 = null;
    }

    @Test
    public void testShareSingleLnThrottleScenario2() {
        // test case:
        // . acquire loco X to "throttle"
        // . acquire loco X to "throttle2"
        // . set speed for loco X
        // . dispatch "throttle2"
        // . set speed for loco X
        // . release "throttle"
        // . release "throttle2"

        throttle = null;
        tm = memo.throttleManager;

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
                // this is a never-stealing impelementation.
                flagGotStealRequest = address.getNumber();
                log.debug("going to steal loco {}", address);
                tm.stealThrottleRequest(address, this, false);
            }
        };

        ThrottleListener throtListen2 = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle2 = t;
                log.error("created a throttle2");
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                log.error("Throttle2 request failed for " + address + " because " + reason);
                failedThrottleRequest2 = true;
            }

            @Override
            public void notifyStealThrottleRequired(jmri.LocoAddress address){
                // this is a never-stealing impelementation.
                flagGotStealRequest2 = address.getNumber();
                log.debug("Throttle2 going to steal loco {}", address);
                tm.stealThrottleRequest(address, this, false);
            }
        };

        tm.requestThrottle(260, throtListen);

        Assert.assertEquals("address request message",
                "BF 02 04 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertEquals("count is correct", 1, lnis.outbound.size());
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x9, 0x00, 0x04, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertNotNull("Throttle should be created and non-null", throttle);
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");

        int netTxMsgCount = lnis.outbound.size()-1;

        tm.requestThrottle(260, throtListen2);  // An additional user of the same throttle
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle2");

        Assert.assertNotNull("Throttle should be created and non-null", throttle2);
        Assert.assertEquals("both throttle users point to the same throttle object",
                throttle, throttle2);
        Assert.assertEquals("no new LocoNet traffic generated", netTxMsgCount, lnis.outbound.size()-1);

        throttle.setSpeedSetting(0.5f);
        Assert.assertEquals("sent speed message",
                "A0 09 44 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString());

        Assert.assertEquals("only one new loconet message", 2, lnis.outbound.size()-1);
        tm.dispatchThrottle(throttle2, throtListen2);
        Assert.assertEquals("first new loconet message at dispatchThrottle", 3, lnis.outbound.size()-1);
        Assert.assertEquals("set slot as 'common' LocoNet message received", "B5 09 10 00", lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        // propagate the slot status write to the slot manager
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertTrue("Address still required",
                InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(260, true)));

        JUnitUtil.waitFor(()->{return 4 < lnis.outbound.size();},"didn't get the 4th LocoNet message");

        Assert.assertEquals("first new loconet message at dispatchThrottle", 4, lnis.outbound.size()-1);
        Assert.assertEquals("dispatch the slot LocoNet message received", "BA 09 00 00", lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        // propagate the slot dispatch to the slot manager
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        throttle.setSpeedSetting(0.25f);
        Assert.assertEquals("sent speed message",
                "A0 09 28 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString());

        Assert.assertTrue("Address still required",
                InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(260, true)));
        Assert.assertEquals("throttle setting 'took' in the throttle", 0.25f, throttle.getSpeedSetting(), .001);

        tm.releaseThrottle(throttle, throtListen);
        Assert.assertTrue("Address still required",
                InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(260, true)));

        tm.releaseThrottle(throttle2, throtListen2);
        Assert.assertFalse("Address no longer required",
                InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(260, true)));

        Assert.assertEquals("No more loconet messages sent at throttle release", 5, lnis.outbound.size()-1);

        throtListen = null;
        throtListen2 = null;
    }

    @Test
    public void testShareSingleLnThrottleScenario3() {
        // test case:
        // . acquire loco X to "throttle"
        // . acquire loco X to "throttle2"
        // . set speed for loco X
        // . release "throttle2"
        // . acquire loco X to "throttle3"
        // . release "throttle"
        // . set speed for loco X
        // release "throttle3"

        throttle = null;
        tm = memo.throttleManager;

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
                // this is a never-stealing impelementation.
                flagGotStealRequest = address.getNumber();
                log.debug("going to steal loco {}", address);
                tm.stealThrottleRequest(address, this, false);
            }
        };

        ThrottleListener throtListen2 = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle2 = t;
                log.error("created a throttle2");
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                log.error("Throttle2 request failed for " + address + " because " + reason);
                failedThrottleRequest2 = true;
            }

            @Override
            public void notifyStealThrottleRequired(jmri.LocoAddress address){
                // this is a never-stealing impelementation.
                flagGotStealRequest2 = address.getNumber();
                log.debug("Throttle2 going to steal loco {}", address);
                tm.stealThrottleRequest(address, this, false);
            }
        };

        ThrottleListener throtListen3 = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle3 = t;
                log.error("created a throttle3");
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                log.error("Throttle3 request failed for " + address + " because " + reason);
                failedThrottleRequest3 = true;
            }

            @Override
            public void notifyStealThrottleRequired(jmri.LocoAddress address){
                // this is a never-stealing impelementation.
                flagGotStealRequest3 = address.getNumber();
                log.debug("Throttle3 going to steal loco {}", address);
                tm.stealThrottleRequest(address, this, false);
            }
        };

        tm.requestThrottle(261, throtListen);

        Assert.assertEquals("address request message",
                "BF 02 05 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertEquals("count is correct", 1, lnis.outbound.size());
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x0A, 0x00, 0x05, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertNotNull("Throttle should be created and non-null", throttle);
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");

        int netTxMsgCount = lnis.outbound.size()-1;

        tm.requestThrottle(261, throtListen2);  // An additional user of the same throttle
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle2");

        Assert.assertNotNull("Throttle should be created and non-null", throttle2);
        Assert.assertEquals("both throttle users point to the same throttle object",
                throttle, throttle2);
        Assert.assertEquals("no new LocoNet traffic generated", netTxMsgCount, lnis.outbound.size()-1);

        throttle.setSpeedSetting(0.5f);
        Assert.assertEquals("sent speed message",
                "A0 0A 44 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString());
        Assert.assertEquals("only one new loconet message", netTxMsgCount +1, lnis.outbound.size()-1);
        tm.releaseThrottle(throttle2, throtListen2);
        Assert.assertEquals("No loconet traffic at throttle2 release", netTxMsgCount +1, lnis.outbound.size()-1);

        Assert.assertTrue("Address still required",
                InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(261, true)));

        tm.requestThrottle(261, throtListen3);  // An additional user of the same throttle

        jmri.util.JUnitAppender.assertErrorMessage("created a throttle3");

        Assert.assertNotNull("Throttle should be created and non-null", throttle3);
        Assert.assertEquals("both throttle users point to the same throttle object",
                throttle, throttle3);
        Assert.assertEquals("no new LocoNet traffic generated", netTxMsgCount+1, lnis.outbound.size()-1);

        Assert.assertEquals("got correct number of LocoNet messages", netTxMsgCount+1, lnis.outbound.size()-1);
        throttle.setSpeedSetting(1.0f);
        Assert.assertEquals("got correct number of LocoNet messages", netTxMsgCount+2, lnis.outbound.size()-1);
        Assert.assertEquals("sent speed message",
                "A0 0A 7C 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString());

        tm.releaseThrottle(throttle, throtListen);

        Assert.assertTrue("Address still required",
                InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(261, true)));

        throttle.setSpeedSetting(-1.0f);
        Assert.assertEquals("got correct number of LocoNet messages", netTxMsgCount+3, lnis.outbound.size()-1);
        Assert.assertEquals("sent speed message",
                "A0 0A 01 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString());

        tm.releaseThrottle(throttle, throtListen3);

        Assert.assertFalse("Address no longer required",
                InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(261, true)));

        Assert.assertEquals("One loconet message sent at throttle release", netTxMsgCount +4, lnis.outbound.size()-1);
        Assert.assertEquals("sent set slot status to COMMON message",
                "B5 0A 10 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString());

        throtListen = null;
        throtListen2 = null;
        throtListen3 = null;
    }

    @Test
    public void testShareSingleLnThrottleScenario4() {
        // test case:
        // . attempt acquire loco X to "throttle"
        // . notified that steal is required
        // . steal is agreed to by throttle user
        // . acquisition is completed under steal protocol
        // . acquire loco X to "throttle2"
        // . (Steal not required!)
        // . set speed for loco X
        // . release "throttle2"
        // . set speed for loco X
        // . release "throttle"

        throttle = null;
        tm = memo.throttleManager;

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
                // this is a never-stealing impelementation.
                flagGotStealRequest = address.getNumber();
                log.debug("going to steal loco {}", address);
                tm.stealThrottleRequest(address, this, true);
            }
        };

        ThrottleListener throtListen2 = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle2 = t;
                log.error("created a throttle2");
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                log.error("Throttle2 request failed for " + address + " because " + reason);
                failedThrottleRequest2 = true;
            }

            @Override
            public void notifyStealThrottleRequired(jmri.LocoAddress address){
                // this is a never-stealing impelementation.
                flagGotStealRequest2 = address.getNumber();
                log.debug("Throttle2 going to steal loco {}", address);
                tm.stealThrottleRequest(address, this, false);
            }
        };

        tm.requestThrottle(262, throtListen);

        Assert.assertEquals("address request message",
                "BF 02 06 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        Assert.assertEquals("count is correct", 1, lnis.outbound.size());
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x0B, 0x30, 0x06, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);

        Assert.assertEquals("Got a steal request", 262, flagGotStealRequest);
        jmri.util.JUnitAppender.assertWarnMessage("slot 11 address 262 is already in-use.");
        Assert.assertNotNull("Throttle should be created and non-null", throttle);
        jmri.util.JUnitAppender.assertWarnMessage("user agreed to steal address 262, but no code is in-place to handle the 'steal' (yet)");
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");

        int netTxMsgCount = lnis.outbound.size()-1;

        tm.requestThrottle(262, throtListen2);  // An additional user of the same throttle
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle2");

        Assert.assertNotNull("Throttle should be created and non-null", throttle2);
        Assert.assertEquals("both throttle users point to the same throttle object",
                throttle, throttle2);
        Assert.assertEquals("no new LocoNet traffic generated", netTxMsgCount, lnis.outbound.size()-1);

        throttle.setSpeedSetting(0.1f);
        Assert.assertEquals("sent speed message",
                "A0 0B 17 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString());
        Assert.assertEquals("only one new loconet message", netTxMsgCount +1, lnis.outbound.size()-1);
        tm.releaseThrottle(throttle2, throtListen2);
        Assert.assertEquals("No loconet traffic at throttle2 release", netTxMsgCount +1, lnis.outbound.size()-1);

        Assert.assertTrue("Address still required",
                InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(262, true)));

        Assert.assertEquals("got correct number of LocoNet messages", netTxMsgCount+1, lnis.outbound.size()-1);
        throttle.setSpeedSetting(0.2f);
        Assert.assertEquals("got correct number of LocoNet messages", netTxMsgCount+2, lnis.outbound.size()-1);
        Assert.assertEquals("sent speed message",
                "A0 0B 22 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString());

        tm.releaseThrottle(throttle, throtListen);

        Assert.assertFalse("Address no longer required",
                InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(262, true)));

        Assert.assertEquals("One loconet message sent at throttle release", netTxMsgCount +3, lnis.outbound.size()-1);
        Assert.assertEquals("sent set slot status to COMMON message",
                "B5 0B 10 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString());

        throtListen = null;
        throtListen2 = null;
    }

    LocoNetInterfaceScaffold lnis;
    LocoNetSystemConnectionMemo memo;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnis = new LocoNetInterfaceScaffold();
        memo = new LocoNetSystemConnectionMemo();
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100,false,false);
        memo.configureManagers();
        tm = new LnThrottleManager(memo);
        log.debug("new throttle manager is {}", tm.toString());
        memo.getSensorManager().dispose(); // get rid of sensor manager to prevent it from sending interrogation messages
        memo.getPowerManager().dispose(); // get rid of power manager to prevent it from sending slot 0 read message
        flagGotStealRequest = -1;
    }

    @After
    public void tearDown() {
        ((LnThrottleManager)tm).dispose();
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LnThrottleManagerTest.class);

}
