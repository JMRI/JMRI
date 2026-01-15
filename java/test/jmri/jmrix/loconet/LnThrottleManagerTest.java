package jmri.jmrix.loconet;

import jmri.*;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author B. Milhaupt, Copyright (C) 2018
 */
public class LnThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    @Override
    @Disabled("parent class test requires further setup")
    @ToDo("complete initialization and remove this overridden method so that the parent class test can run")
    public void testGetThrottleInfo() {
    }

    // this is an always-stealing implementation.
    private class ThrottleListenStealWhenDecision extends ThrottleListen {

        @Override
        public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
            if ( question == DecisionType.STEAL ){
                
                flagGotStealRequest = address.getNumber();
                log.debug("going to steal loco {}", address);
                tm.responseThrottleDecision(address, this, DecisionType.STEAL );
            }
        }
    }

    @Test
    public void testCreateLnThrottleRunAndRelease() {
        ThrottleListen throtListen = new ThrottleListen(); // this is a never-stealing implementation.

        tm.requestThrottle(1203, throtListen,true);

        assertEquals( "BF 09 33 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");

        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x00, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x53});
        lnis.sendTestMessage(cmdStationReply);
        assertEquals( "BA 11 11 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "null move");

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x30, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x00});
        lnis.sendTestMessage(cmdStationReply);
        assertEquals( "EF 0E 11 30 33 00 00 07 00 09 00 71 02 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "write Throttle ID");

        cmdStationReply = new LocoNetMessage(new int[] {
                0xb4, 0x6f, 0x7f, 0x5B});
        lnis.sendTestMessage(cmdStationReply);

        DccThrottle throttle = throtListen.getThrottle();
        Assertions.assertNotNull(throttle, "have created a throttle");
        Assertions.assertTrue(throttle.getClass() == LocoNetThrottle.class,"is LnThrottle");
        Assertions.assertEquals( 0x171, ((LocoNetThrottle) throttle).slot.id(), "throttleId is set");

        throttle.setSpeedSetting(0.125f);
        assertEquals( "A0 11 1A 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "set speed to one eighth");

        throttle.release(throtListen);
        JUnitUtil.waitFor(()->{return 4 < lnis.outbound.size();},"didn't get the 6th LocoNet message");
        assertEquals( "B5 11 10 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "slot is set to 'common' status");
    }

    @Test
    public void testCreateLnThrottleRunAndDispatch() {

        ThrottleListen throtListen = new ThrottleListen();
        tm.requestThrottle(1204, throtListen, true);

        assertEquals( "BF 09 34 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        assertEquals( 1, lnis.outbound.size(), "count is incorrect");
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x00, 0x34, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x53});
        lnis.sendTestMessage(cmdStationReply);
        memo.getSlotManager().message(lnis.outbound.elementAt(1));
        assertEquals( 2, lnis.outbound.size(), "count is correct");

        assertEquals( "BA 11 11 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "Throttle manager sends a 'null move'");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x30, 0x34, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x00});
        lnis.sendTestMessage(cmdStationReply);
        assertEquals( "EF 0E 11 30 34 00 00 07 00 09 00 71 02 00",
            lnis.outbound.elementAt(2).toString(), "write Throttle ID");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        cmdStationReply = new LocoNetMessage(new int[] {
                0xb4, 0x6f, 0x7f, 0x5B});
        lnis.sendTestMessage(cmdStationReply);
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        DccThrottle throttle = throtListen.getThrottle();

        assertNotNull(throttle, "have created a throttle");
        assertTrue(throttle.getClass() == LocoNetThrottle.class,"is LnThrottle");
        assertEquals( 0x171, ((LocoNetThrottle) throttle).slot.id(), "throttleId is set");

        throttle.setSpeedSetting(0.125f);

        assertEquals( "A0 11 1A 00",
            lnis.outbound.elementAt(3).toString(), "set speed to one eighth");
        memo.getSlotManager().message(lnis.outbound.elementAt(3));
        assertEquals( 26, memo.getSlotManager()._slots[17].speed(), "check that speed was set");

        throttle.dispatch(throtListen);

        assertEquals( "B5 11 10 00",
            lnis.outbound.elementAt(4).toString(), "slot is set to 'common' status");

        assertEquals( "BA 11 00 00",
            lnis.outbound.elementAt(5).toString(), "Expect the slot to be dispatched");
        // common is sent twice due to the way the release works.
        assertEquals( "B5 11 10 00",
            lnis.outbound.elementAt(6).toString(), "slot is set to 'common' status");

        JUnitUtil.waitFor(()->{return 6 < lnis.outbound.size();},"didn't get the 5th LocoNet message");
        assertEquals( 6, lnis.outbound.size()-1, "check count of sent messages");

        assertEquals( 26, memo.getSlotManager()._slots[17].speed(), "slot speed not zeroed");
    }

    @Test
    public void testCreateLnThrottleRunAndDispose() {
        ThrottleListen throtListen = new ThrottleListen();
        tm.requestThrottle(1203, throtListen, true);

        assertEquals( "BF 09 33 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        assertEquals( 1, lnis.outbound.size(), "count is correct");
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x00, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x53});
        lnis.sendTestMessage(cmdStationReply);

        assertEquals( "BA 11 11 00",lnis.outbound.elementAt(1).toString(),
            "Correct message sent");

        memo.getSlotManager().message(lnis.outbound.elementAt(1));
        assertEquals( 2, lnis.outbound.size(), "count is correct");

        assertEquals( "BA 11 11 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "Throttle manager sends a 'null move'");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x30, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x00});
        lnis.sendTestMessage(cmdStationReply);
        assertEquals( "EF 0E 11 30 33 00 00 07 00 09 00 71 02 00",
            lnis.outbound.elementAt(2).toString(), "write Throttle ID");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        cmdStationReply = new LocoNetMessage(new int[] {
                0xb4, 0x6f, 0x7f, 0x5B});
        lnis.sendTestMessage(cmdStationReply);
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        DccThrottle throttle = throtListen.getThrottle();

        Assertions.assertNotNull(throttle, "have created a throttle");
        Assertions.assertTrue(throttle.getClass() == LocoNetThrottle.class,"is LnThrottle");
        Assertions.assertEquals( 0x171, ((LocoNetThrottle) throttle).slot.id(), "throttleId is set");

        throttle.setSpeedSetting(0.125f);

        JUnitUtil.waitFor(()->{return 3 < lnis.outbound.size();},"didn't get the 4th LocoNet message");
        assertEquals( "A0 11 1A 00",
            lnis.outbound.elementAt(3).toString(), "set speed to one eighth");
        memo.getSlotManager().message(lnis.outbound.elementAt(3));
        assertEquals( 26, memo.getSlotManager()._slots[17].speed(), "check that speed was set");

        ((LocoNetThrottle)throttle).throttleDispose();

        JUnitUtil.waitFor(()->{return 4 < lnis.outbound.size();},"didn't get the 5th LocoNet message");
        assertEquals( "B5 11 10 00",
            lnis.outbound.elementAt(4).toString(), "slot is set to 'common' status");
        memo.getSlotManager().message(lnis.outbound.elementAt(4));

        assertTrue( 5<=lnis.outbound.size(), "check count of sent messages");
        assertEquals( 26, memo.getSlotManager()._slots[17].speed(), "slot speed not zeroed");
    }

    @Test
    public void testCreateLnThrottleStealScenario1() {

        ThrottleListen throtListen = new ThrottleListen();
        tm.requestThrottle(129, throtListen, true);

        assertEquals( "BF 01 01 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        assertEquals( 1, lnis.outbound.size(), "count is correct");
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x30, 0x01, 0x0, 0x0, 0x7, 0x0, 0x01, 0x0, 0x0, 0x1, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        assertEquals( 129, throtListen.getFlagGotStealRequest(), "Got a steal request");
        JUnitAppender.assertWarnMessage("slot 17 address 129 is already in-use.");
        assertNull(throtListen.getThrottle());
    }

    @Test
    public void testCreateLnThrottleStealScenario2() {

        ThrottleListen throtListen = new ThrottleListen();
        tm.requestThrottle(5, throtListen, true);

        assertEquals( "BF 00 05 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        assertEquals( 1, lnis.outbound.size(), "count is correct");
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x0c, 0x30, 0x05, 0x0, 0x0, 0x7, 0x0, 0x00, 0x0, 0x2, 0x0, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        assertEquals( 5, throtListen.getFlagGotStealRequest(), "Got a steal request");
        JUnitAppender.assertWarnMessage("slot 12 address 5 is already in-use.");
        Assertions.assertNull(throtListen.getThrottle());
    }

    @Test
    public void testCreateLnThrottleStealScenario3() {

        ThrottleListen throtListen = new ThrottleListen();
        tm.requestThrottle(85, throtListen, true);

        assertEquals("BF 00 55 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        // Throttle request messages may be missed on 1st send so we wait for
        // the 2nd message sent ( in 5.13.5 it's 1000ms after the 1st)
        JUnitUtil.waitFor( () -> lnis.outbound.size() == 2, "throttle request repeated");
        assertEquals( lnis.outbound.get(0), lnis.outbound.get(1), "2nd request matches 1st" );

        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x60, 0x30, 0x55, 0x0, 0x0, 0x7, 0x0, 0x00, 0x0, 0x2, 0x70, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        assertEquals(85, throtListen.getFlagGotStealRequest(), "Got a steal request");
        JUnitAppender.assertWarnMessage("slot 96 address 85 is already in-use.");
    }

    @Test
    public void testCreateLnThrottleStealScenario4() {

        ThrottleListenStealWhenDecision throtListen = new ThrottleListenStealWhenDecision();
        Assertions.assertNull(throtListen.getThrottle());
        tm.requestThrottle(256, throtListen, true);

        assertEquals( "BF 02 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        // Throttle request messages may be missed on 1st send so we wait for
        // the 2nd message sent ( in 5.13.5 it's 1000ms after the 1st)
        JUnitUtil.waitFor( () -> lnis.outbound.size() == 2, "throttle request repeated");
        assertEquals( lnis.outbound.get(0), lnis.outbound.get(1), "2nd request matches 1st" );

        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x8, 0x30, 0x00, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        assertEquals( 256, throtListen.getFlagGotStealRequest(), "Got a steal request");
        JUnitAppender.assertWarnMessage("slot 8 address 256 is already in-use.");

        assertEquals( "BA 08 08 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "In Use");

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x08, 0x30, 0x00, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);

        assertEquals( "EF 0E 08 30 00 00 00 07 00 02 00 71 02 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "Write Throttle ID");

        DccThrottle throttle = throtListen.getThrottle();

        assertNotNull( throttle, "Throttle should be created and non-null");
        tm.releaseThrottle(throttle, throtListen);
    }

    @Test
    public void testCreateLnThrottleStealScenario5() {

        ThrottleListenStealWhenDecision throtListen = new ThrottleListenStealWhenDecision();
        tm.requestThrottle(257, throtListen, true);

        assertEquals( "BF 02 01 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        assertEquals( 1, lnis.outbound.size(), "count is correct");
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x8, 0x10, 0x01, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is common
        lnis.sendTestMessage(cmdStationReply);

        assertEquals( "BA 08 08 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "In Use");

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x08, 0x30, 0x01, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);

        assertEquals( "EF 0E 08 30 01 00 00 07 00 02 00 71 02 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "Write Throttle ID");

        Assertions.assertNotNull(throtListen.getThrottle(), "Throttle should be created and non-null");
        tm.releaseThrottle(throtListen.getThrottle(), throtListen);
    }

    @Test
    public void testCreateLnThrottleStealScenario7() {

        ThrottleListen throtListen = new ThrottleListen();
        tm.requestThrottle(259, throtListen, true);

        assertEquals( "BF 02 03 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        assertEquals( 1, lnis.outbound.size(), "count is correct");
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x8, 0x00, 0x03, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is free
        lnis.sendTestMessage(cmdStationReply);

        assertEquals( "BA 08 08 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "In Use");

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x08, 0x30, 0x03, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);

        assertEquals( "EF 0E 08 30 03 00 00 07 00 02 00 71 02 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "Write Throttle ID");

        assertNotNull( throtListen.getThrottle(), "Throttle should be created and non-null");
        tm.releaseThrottle(throtListen.getThrottle(), throtListen);
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

        ThrottleListen throtListen = new ThrottleListen();
        ThrottleListen throtListen2 = new ThrottleListen();

        tm.requestThrottle(260, throtListen, true);

        assertEquals( "BF 02 04 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        assertEquals( 1, lnis.outbound.size(), "count is correct");
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x9, 0x00, 0x04, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x00, 0x00, 0x53});  // slot is free
        lnis.sendTestMessage(cmdStationReply);
        assertNull( throtListen.getThrottle(), "Throttle should not be created and null");
        JUnitAppender.assertNoErrorMessage();

        // send it to INUSE
        assertEquals( "BA 09 09 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "NUll move");
        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x9, 0x30, 0x04, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x00, 0x00, 0x53});  // slot is in-use no throttle ID
        lnis.sendTestMessage(cmdStationReply);

        JUnitUtil.waitFor( () -> "EF 0E 09 30 04 00 00 07 00 02 00 71 02 00".equals(
            (lnis.outbound.elementAt(lnis.outbound.size()-1).toString())),
            "Write Throttle ID");

        assertNotNull( throtListen.getThrottle(), "Throttle should be created and non-null");

        int netTxMsgCount = lnis.outbound.size()-1;

        tm.requestThrottle(260, throtListen2, true);  // An additional user of the same throttle

        assertNotNull( throtListen2.getThrottle(), "Throttle should be created and non-null");
        assertEquals( throtListen.getThrottle(), throtListen2.getThrottle(),
            "both throttle users point to the same throttle object");
        assertEquals( netTxMsgCount, lnis.outbound.size()-1, "no new LocoNet traffic generated");

        throtListen.getThrottle().setSpeedSetting(0.5f);
        assertEquals( "A0 09 44 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString(),
            "sent speed message");
        assertEquals( netTxMsgCount +1, lnis.outbound.size()-1, "only one new loconet message");
        lnis.outbound.clear();
        tm.releaseThrottle(throtListen2.getThrottle(), throtListen2);
        assertEquals( 0, lnis.outbound.size(), "No loconet traffic at throttle2 release");

        assertTrue(InstanceManager.throttleManagerInstance().addressStillRequired(260),
            "Address still required");

        tm.releaseThrottle(throtListen.getThrottle(), throtListen);
        assertFalse(InstanceManager.throttleManagerInstance().addressStillRequired(
            new DccLocoAddress(260, true)), "Address no longer required");

        // Wait for the set slot common message
        JUnitUtil.waitFor(() -> lnis.outbound.size()==1, "wait for 1 slot COMMON message, was " + lnis.outbound);

        Assertions.assertEquals("B5 09 10 00", lnis.outbound.elementAt(0).toString()
            , "COMMON , was " + lnis.outbound);
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

        ThrottleListen throtListen = new ThrottleListen();

        ThrottleListenStealWhenDecision throtListen2 = new ThrottleListenStealWhenDecision();

        tm.requestThrottle(260, throtListen, true);

        assertEquals( "BF 02 04 00",lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "address request message");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        assertEquals( 1, lnis.outbound.size(), "count is correct");
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x9, 0x00, 0x04, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        assertNull( throtListen.getThrottle(), "Throttle should not be created and null");
        JUnitAppender.assertNoErrorMessage();

        // send it to INUSE
        assertEquals("BA 09 09 00", lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "NUll move");
        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x09, 0x30, 0x04, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x00, 0x00, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        assertEquals( "EF 0E 09 30 04 00 00 07 00 02 00 71 02 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "Write Throttle ID");

        assertNotNull( throtListen.getThrottle(), "Throttle should be created and non-null");

        lnis.outbound.clear();

        int netTxMsgCount = lnis.outbound.size()-1;

        tm.requestThrottle(260, throtListen2, true);  // An additional user of the same throttle

        assertNotNull( throtListen2.getThrottle(), "Throttle should be created and non-null");
        assertEquals( throtListen.getThrottle(), throtListen2.getThrottle(),
            "both throttle users point to the same throttle object");
        assertEquals( netTxMsgCount, lnis.outbound.size()-1, "no new LocoNet traffic generated");

        throtListen.getThrottle().setSpeedSetting(0.5f);
        assertEquals( "A0 09 44 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString(),
            "sent speed message");

        assertEquals( 1, lnis.outbound.size(), "only one new loconet message");
        tm.dispatchThrottle(throtListen2.getThrottle(), throtListen2); // this fails as loco is in use on multiple throttles
        assertEquals( 1, lnis.outbound.size(), "Message Count still 1");
        assertEquals( 2, InstanceManager.throttleManagerInstance().getThrottleUsageCount(
            new DccLocoAddress(260, true)), "Usage Count 2");
        assertTrue(InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(260, true)),
            "Address still required");

        throtListen.getThrottle().setSpeedSetting(0.25f);
        assertEquals("A0 09 28 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString(),
            "sent speed message");

        assertTrue(InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(260, true)),
            "Address still required");
        assertEquals(0.25f, throtListen.getThrottle().getSpeedSetting(), .001,
            "throttle setting 'took' in the throttle");

        tm.releaseThrottle(throtListen.getThrottle(), throtListen);
        assertTrue(InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(260, true)),
            "Address still required");
        //reset outbound.
        lnis.outbound.clear();
        tm.dispatchThrottle(throtListen2.getThrottle(), throtListen2);
        assertFalse( InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(260, true)),
            "Address no longer required");

        // Wait for 3 messages, the set slot common and then the get slot refresh
        JUnitUtil.waitFor(() -> {
            return lnis.outbound.size()==3;
        },"wait for 3 messages");

        assertEquals("B5 09 10 00", lnis.outbound.get(0).toString(),"Slot set to COMMON");
        assertEquals("BA 09 00 00", lnis.outbound.get(1).toString(),"Slot set to DISPATCH");

        // when LnPredefinedMeters is running
        // assertEquals("BB 09 00 00", lnis.outbound.get(2).toString(), "Slot read");

        // when LnPredefinedMeters is NOT running the 3rd message is a repeat of set to common
        assertEquals("B5 09 10 00", lnis.outbound.get(2).toString(),"Slot set to COMMON again");

        JUnitUtil.waitFor(150);
        assertEquals(3, lnis.outbound.size(), "No more loconet messages sent at throttle release, was "+lnis.outbound);

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

        ThrottleListen throtListen = new ThrottleListen();
        ThrottleListen throtListen2 = new ThrottleListen();
        ThrottleListen throtListen3 = new ThrottleListen();

        tm.requestThrottle(261, throtListen, true);

        assertEquals( "BF 02 05 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        // for long delays, BF 02 05 00 is re-sent 10 times before halting attempt.

        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x0A, 0x00, 0x05, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        assertNull(throtListen.getThrottle(), "Throttle should not be created and null");
        JUnitAppender.assertNoErrorMessage();

        // send it to INUSE
        assertEquals("BA 0A 0A 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "NUll move");
        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x0A, 0x30, 0x05, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x00, 0x00, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);
        assertEquals("EF 0E 0A 30 05 00 00 07 00 02 00 71 02 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "Write Throttle ID");

        assertNotNull(throtListen.getThrottle(), "Throttle should be created and non-null");

        int netTxMsgCount = lnis.outbound.size()-1;

        tm.requestThrottle(261, throtListen2, true);  // An additional user of the same throttle

        assertNotNull(throtListen2.getThrottle(), "Throttle should be created and non-null");
        assertEquals(throtListen.getThrottle(), throtListen2.getThrottle(),
            "both throttle users point to the same throttle object");
        assertEquals(netTxMsgCount, lnis.outbound.size()-1, "no new LocoNet traffic generated");

        throtListen.getThrottle().setSpeedSetting(0.5f);
        assertEquals("A0 0A 44 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString(),
            "sent speed message");
        assertEquals(netTxMsgCount +1, lnis.outbound.size()-1, "only one new loconet message");
        tm.releaseThrottle(throtListen2.getThrottle(), throtListen2);
        assertEquals(netTxMsgCount +1, lnis.outbound.size()-1, "No loconet traffic at throttle2 release");

        assertTrue(InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(261, true)),
            "Address still required");

        tm.requestThrottle(261, throtListen3, true);  // An additional user of the same throttle

        assertNotNull(throtListen3.getThrottle(), "Throttle should be created and non-null");
        assertEquals(throtListen.getThrottle(), throtListen3.getThrottle(),
            "both throttle users point to the same throttle object");
        assertEquals(netTxMsgCount+1, lnis.outbound.size()-1, "no new LocoNet traffic generated");
        assertEquals(netTxMsgCount+1, lnis.outbound.size()-1, "got correct number of LocoNet messages");

        throtListen.getThrottle().setSpeedSetting(1.0f);
        assertEquals(netTxMsgCount+2, lnis.outbound.size()-1, "got correct number of LocoNet messages");
        assertEquals("A0 0A 7C 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString(),
            "sent speed message");

        tm.releaseThrottle(throtListen.getThrottle(), throtListen);

        assertTrue(InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(261, true)),
            "Address still required");

        throtListen.getThrottle().setSpeedSetting(-1.0f);
        assertEquals(netTxMsgCount+3, lnis.outbound.size()-1, "got correct number of LocoNet messages");
        assertEquals("A0 0A 01 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString(),
            "sent speed message");

        tm.releaseThrottle(throtListen.getThrottle(), throtListen3);

        assertFalse(InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(261, true)),
            "Address no longer required");

        assertEquals(netTxMsgCount +4, lnis.outbound.size()-1, "One loconet message sent at throttle release");
        assertEquals("B5 0A 10 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString(),
            "sent set slot status to COMMON message");

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

        ThrottleListenStealWhenDecision throtListen = new ThrottleListenStealWhenDecision();

        ThrottleListen throtListen2 = new ThrottleListen();

        tm.requestThrottle(262, throtListen, true);

        assertEquals( "BF 02 06 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");
        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        assertEquals(1, lnis.outbound.size(), "count is correct");
        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x0B, 0x30, 0x06, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);

        assertEquals(262, throtListen.getFlagGotStealRequest(), "Got a steal request");
        JUnitAppender.assertWarnMessage("slot 11 address 262 is already in-use.");

        assertEquals( "BA 0B 0B 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "In Use");

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x0B, 0x30, 0x06, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);

        assertEquals( "EF 0E 0B 30 06 00 00 07 00 02 00 71 02 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "Write Throttle ID");

        assertNotNull(throtListen.getThrottle(), "Throttle should be created and non-null");

        int netTxMsgCount = lnis.outbound.size()-1;

        tm.requestThrottle(262, throtListen2, true);  // An additional user of the same throttle

        assertNotNull(throtListen2.getThrottle(), "Throttle should be created and non-null");
        assertEquals( throtListen.getThrottle(), throtListen2.getThrottle(),
            "both throttle users point to the same throttle object");
        assertEquals(netTxMsgCount, lnis.outbound.size()-1, "no new LocoNet traffic generated");

        throtListen.getThrottle().setSpeedSetting(0.1f);
        assertEquals("A0 0B 17 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString()
            , "sent speed message");
        assertEquals( netTxMsgCount +1, lnis.outbound.size()-1, "only one new loconet message");
        tm.releaseThrottle(throtListen2.getThrottle(), throtListen2);
        assertEquals(netTxMsgCount +1, lnis.outbound.size()-1, "No loconet traffic at throttle2 release");

        assertTrue(InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(262, true))
                , "Address still required");

        assertEquals( netTxMsgCount+1, lnis.outbound.size()-1, "got correct number of LocoNet messages");
        throtListen.getThrottle().setSpeedSetting(0.2f);
        assertEquals(netTxMsgCount+2, lnis.outbound.size()-1, "got correct number of LocoNet messages");
        assertEquals("A0 0B 22 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString()
            , "sent speed message");

        tm.releaseThrottle(throtListen.getThrottle(), throtListen);

        assertFalse( InstanceManager.throttleManagerInstance().addressStillRequired(new DccLocoAddress(262, true)),
            "Address no longer required");

        assertEquals( netTxMsgCount +3, lnis.outbound.size()-1, "One loconet message sent at throttle release");
        assertEquals("B5 0B 10 00", lnis.outbound.elementAt(lnis.outbound.size() -1). toString()
            , "sent set slot status to COMMON message");

    }

    @Test
    public void testUseRosterEntry() {

        org.jdom2.Element e = new org.jdom2.Element("locomotive")
                .setAttribute("id", "our id 1")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .setAttribute("dccAddress", "1234")
                .addContent(
                        new org.jdom2.Element("locoaddress").addContent(
                                new org.jdom2.Element("dcclocoaddress")
                                .setAttribute("number", "1234")
                                .setAttribute("longaddress", "yes")
                        )
                ); // end create element

        RosterEntry re = new RosterEntry(e);

        ThrottleListen throtListen = new ThrottleListen();

        tm.requestThrottle(re, throtListen, true);

        assertEquals("BF 09 52 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "address request message");

        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x04, 0x03, 0x52, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x53});
        lnis.sendTestMessage(cmdStationReply);
        assertEquals("BA 04 04 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "null move");

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x04, 0x33, 0x52, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x63});
        lnis.sendTestMessage(cmdStationReply);
        assertEquals("EF 0E 04 33 52 00 00 07 00 09 00 71 02 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "write Throttle ID");

        cmdStationReply = new LocoNetMessage(new int[] {
                0xb4, 0x6f, 0x7f, 0x5B});
        lnis.sendTestMessage(cmdStationReply);

        DccThrottle throttle4 = throtListen.getThrottle();

        assertNotNull(throttle4, "have created a throttle4");
        assertTrue(throttle4.getClass() == LocoNetThrottle.class,"is LnThrottle");
        assertEquals( 0x171, ((LocoNetThrottle) throttle4).slot.id(), "throttle4Id is set");
        // JUnitAppender.assertErrorMessage("created a throttle4");

        throttle4.setSpeedSetting(0.5f);

        assertEquals("A0 04 40 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "set speed a half");

        assertTrue(InstanceManager.throttleManagerInstance().addressStillRequired(re), "Address still is not still required");

        assertEquals(1,
                InstanceManager.throttleManagerInstance().getThrottleUsageCount(re), "Throttle usage 1");


        throttle4.release(throtListen);

        JUnitUtil.waitFor(()->{return 4 < lnis.outbound.size();},"didn't get the 6th LocoNet message");

        JUnitUtil.waitFor( () -> "B5 04 13 00".equals(
            (lnis.outbound.elementAt(lnis.outbound.size()-1).toString())),
            "slot is set to 'common' status");

    }

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100, false, false, false, false, false);
        memo.getSlotManager().pmManagerGotReply=true; //turn off more probing by saying done.
        memo.configureManagers(); // lnThrottleManager created by memo + added to instancemanager here
        tm = memo.get(ThrottleManager.class);
        assertTrue(tm == InstanceManager.getDefault(ThrottleManager.class),"tm is Instance tm");
        assertInstanceOf(LnThrottleManager.class, tm, "tm is ln tm and not null");
        log.debug("new throttle manager is {}", tm);
        memo.getSensorManager().dispose(); // get rid of sensor manager to prevent it from sending interrogation messages
        memo.getPowerManager().dispose(); // get rid of power manager to prevent it from sending slot 0 read message
        memo.getPredefinedMeters().dispose(); // get rid of meter to prevent it reading slots
    }

    @AfterEach
    public void tearDown() {

        LocoNetThrottledTransmitter.ServiceThread theServiceThread = memo.tm.theServiceThread;
        tm.dispose();
        memo.dispose();
        if ( theServiceThread != null ) {
            // wait for LocoNetThrottledTransmitter to terminate
            JUnitUtil.waitFor(() -> {
                return !theServiceThread.isAlive();
            },"wait for LocoNetThrottledTransmitter to shutdown");
        }
        if ( lnis != null ) {
            log.debug("numListeners on lnis = {}", lnis.numListeners()); // 5? no problem
            lnis.dispose();
        }
        lnis = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnThrottleManagerTest.class);

}
