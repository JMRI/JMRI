package jmri.jmrix.loconet;

import java.util.ArrayList;
import java.util.List;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * This test class is adapted from LnThrottleManagerTest
 * for tests which require both LnThrottleManager AND LnPredefinedMeters.
 * LnPredefinedMeters is disabled in LnThrottleManagerTest for test reliability.
 * <p>
 * LnPredefinedMeters sends one query of its own, {@code BB 79 01 00}, a
 * little after it is created. That can be before, among or after the
 * throttle's messages, so the tests look at the throttle's messages without it
 * and check separately that it was sent.
 * <p>
 * The test connection does not echo what is sent, so each test hands the
 * messages that release the slot back to the listeners, as LocoNet would. The
 * slot is read again when the SlotManager sees a slot made common.
 * @author Paul Bender, Copyright (C) 2017
 * @author B. Milhaupt, Copyright (C) 2018
 * @author Steve Young, Copyright (C) 2025
 */
public class LnThrottleManagerWithLnPredefinedMetersTest {

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;
    private ThrottleManager tm;

    /** The query LnPredefinedMeters sends once at start up. */
    private static final String METERS_QUERY = "BB 79 01 00";
    private static final String SLOT_COMMON = "B5 09 10 00";
    private static final String SLOT_DISPATCH = "BA 09 00 00";
    private static final String SLOT_READ = "BB 09 00 00";

    /**
     * The messages sent from a place in the outbound list onwards, without
     * the meters' query.
     * @param from index of the first message to include
     * @return the messages as text, in the order sent
     */
    private List<String> sentSince(int from) {
        List<String> sent = new ArrayList<>();
        Object[] all = lnis.outbound.toArray();
        for (int i = from; i < all.length; i++) {
            String text = all[i].toString();
            if (!METERS_QUERY.equals(text)) {
                sent.add(text);
            }
        }
        return sent;
    }

    /**
     * Hand messages already sent back to the listeners, as LocoNet echoes them.
     * @param from index in the outbound list of the first message to echo
     * @param count how many of the throttle's messages to echo
     */
    private void echoSince(int from, int count) {
        Object[] all = lnis.outbound.toArray();
        int echoed = 0;
        for (int i = from; i < all.length && echoed < count; i++) {
            LocoNetMessage m = (LocoNetMessage) all[i];
            if (!METERS_QUERY.equals(m.toString())) {
                lnis.sendTestMessage(m);
                echoed++;
            }
        }
    }

    private void waitForMetersQuery() {
        JUnitUtil.waitFor(() -> {
            for (Object m : lnis.outbound.toArray()) {
                if (METERS_QUERY.equals(m.toString())) {
                    return true;
                }
            }
            return false;
        }, "LnPredefinedMeters sent its query");
    }

    /**
     * See LnThrottleManagerTest testShareSingleLnThrottleScenario1.
     * Ensures that slot reads are requested following Throttle dispose.
     */
    @Test
    public void testShareSingleLnThrottleScenario1() {
        // test case:
        // . acquire loco X to "throttle"
        // . acquire loco X to "throttle2"
        // . set speed for loco X
        // . dispatch "throttle2"
        // . release "throttle2"
        // . release "throttle"
        // . . wait for Slot Read

       ThrottleListen throtListen = new ThrottleListen();
       ThrottleListen throtListen2 = new ThrottleListen();

        tm.requestThrottle(260, throtListen, true);

        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x9, 0x00, 0x04, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x00, 0x00, 0x53});  // slot is free
        lnis.sendTestMessage(cmdStationReply);

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x9, 0x30, 0x04, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x00, 0x00, 0x53});  // slot is in-use no throttle ID
        lnis.sendTestMessage(cmdStationReply);

        JUnitUtil.waitFor( () -> "EF 0E 09 30 04 00 00 07 00 02 00 71 02 00".equals(
            (lnis.outbound.elementAt(lnis.outbound.size()-1).toString())),
            "Write Throttle ID");

        tm.requestThrottle(260, throtListen2, true);  // An additional user of the same throttle

        // FIELD REPORT (Andrew Deak): notifyThrottleKnown() now defers
        // notifyThrottleFound() via SwingUtilities.invokeLater() (see its
        // field comment -- fixes a real StackOverflow from synchronous
        // re-entrant requestThrottle() calls), so this is no longer
        // available the instant requestThrottle() returns.
        JUnitUtil.waitFor(() -> throtListen.getThrottle() != null, "wait for throttle found");
        throtListen.getThrottle().setSpeedSetting(0.5f);

        int start = lnis.outbound.size();
        tm.releaseThrottle(throtListen2.getThrottle(), throtListen2);
        tm.releaseThrottle(throtListen.getThrottle(), throtListen);

        // the slot is set to common
        JUnitUtil.waitFor(() -> !sentSince(start).isEmpty(), "wait for message");
        assertEquals(List.of(SLOT_COMMON), sentSince(start), "COMMON");

        // LocoNet echoes it, and the slot is then read
        echoSince(start, 1);
        JUnitUtil.waitFor(() -> sentSince(start).size() >= 2, "Slot read");
        assertEquals(List.of(SLOT_COMMON, SLOT_READ), sentSince(start), "COMMON then Slot read");

        waitForMetersQuery();
    }

    /**
     * See LnThrottleManagerTest testShareSingleLnThrottleScenario2.
     * Ensures that slot reads are requested following Throttle dispose.
     */
    @Test
    public void testShareSingleLnThrottleScenario2() {
        // test case:
        // . acquire loco X to "throttle"
        // . acquire loco X to "throttle2"
        // . dispatch "throttle2"
        // . release "throttle"
        // . release "throttle2"
        // . . wait for Slot Read

        ThrottleListen throtListen = new ThrottleListen();
        ThrottleListenStealWhenDecision throtListen2 = new ThrottleListenStealWhenDecision();

        tm.requestThrottle(260, throtListen, true);

        memo.getSlotManager().message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x9, 0x00, 0x04, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x13, 0x01, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);

        // send it to INUSE
        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x09, 0x30, 0x04, 0x0, 0x0, 0x7, 0x0, 0x02, 0x00, 0x00, 0x00, 0x53});  // slot is in-use
        lnis.sendTestMessage(cmdStationReply);

        tm.requestThrottle(260, throtListen2, true);  // An additional user of the same throttle

        // FIELD REPORT (Andrew Deak): see the matching comment in
        // testShareSingleLnThrottleScenario1() above -- notifyThrottleFound()
        // is now deferred, so this isn't available synchronously.
        JUnitUtil.waitFor(() -> throtListen2.getThrottle() != null, "wait for second throttle found");
        tm.dispatchThrottle(throtListen2.getThrottle(), throtListen2); // this fails as loco is in use on multiple throttles
        tm.releaseThrottle(throtListen.getThrottle(), throtListen);

        int start = lnis.outbound.size();
        tm.dispatchThrottle(throtListen2.getThrottle(), throtListen2);

        // the slot is set to common, dispatched, and set to common again
        JUnitUtil.waitFor(() -> sentSince(start).size() >= 3, "wait for 3 messages");
        assertEquals(List.of(SLOT_COMMON, SLOT_DISPATCH, SLOT_COMMON), sentSince(start),
            "COMMON, DISPATCH, COMMON");

        // LocoNet echoes them, and the slot is then read once for each time
        // it was set to common
        echoSince(start, 3);
        JUnitUtil.waitFor(() -> sentSince(start).size() >= 5, "Slot reads");
        assertEquals(List.of(SLOT_COMMON, SLOT_DISPATCH, SLOT_COMMON, SLOT_READ, SLOT_READ),
            sentSince(start), "COMMON, DISPATCH, COMMON then two Slot reads");

        waitForMetersQuery();
    }

    private class ThrottleListen implements ThrottleListener {

        private DccThrottle foundThrottle = null;

        @Override
        public void notifyThrottleFound(DccThrottle t){
            foundThrottle = t;
        }

        @Override
        public void notifyFailedThrottleRequest(LocoAddress address, String reason){
            // do nothing
        }

        @Override
        public void notifyDecisionRequired(LocoAddress address, ThrottleListener.DecisionType question) {
            // does nothing
        }

        DccThrottle getThrottle() {
            return foundThrottle;
        }

    }

    // this is an always-stealing implementation.
    private class ThrottleListenStealWhenDecision extends ThrottleListen {

        @Override
        public void notifyDecisionRequired(LocoAddress address, ThrottleListener.DecisionType question) {
            if ( question == ThrottleListener.DecisionType.STEAL ){
                tm.responseThrottleDecision(address, this, ThrottleListener.DecisionType.STEAL );
            }
        }
    }

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
        memo.getSensorManager().dispose(); // get rid of sensor manager to prevent it from sending interrogation messages
        memo.getPowerManager().dispose(); // get rid of power manager to prevent it from sending slot 0 read message
        // Unlike LnThrottleManagerTest we keep the PredefinedMeters so it can read slots.
    }

    @AfterEach
    public void tearDown() {

        memo.getPredefinedMeters().dispose();

        LocoNetThrottledTransmitter.ServiceThread theServiceThread = memo.tm.theServiceThread;
        tm.dispose();
        memo.dispose();
        if ( theServiceThread != null ) {
            // wait for LocoNetThrottledTransmitter to terminate
            JUnitUtil.waitThreadTerminated(theServiceThread);
        }
        if ( lnis != null ) {
            lnis.dispose();
        }
        lnis = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
