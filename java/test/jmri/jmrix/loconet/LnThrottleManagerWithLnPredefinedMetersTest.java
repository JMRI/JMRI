package jmri.jmrix.loconet;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * This test class is adapted from LnThrottleManagerTest
 * for tests which require both LnThrottleManager AND LnPredefinedMeters.
 * LnPredefinedMeters is disabled in LnThrottleManagerTest for test reliability.
 * @author Paul Bender, Copyright (C) 2017
 * @author B. Milhaupt, Copyright (C) 2018
 * @author Steve Young, Copyright (C) 2025
 */
public class LnThrottleManagerWithLnPredefinedMetersTest {

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;
    private ThrottleManager tm;

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

        throtListen.getThrottle().setSpeedSetting(0.5f);

        lnis.outbound.clear();
        tm.releaseThrottle(throtListen2.getThrottle(), throtListen2);
        tm.releaseThrottle(throtListen.getThrottle(), throtListen);

        // Wait for 2 messages, the set slot common and then the get slot refresh
        JUnitUtil.waitFor(() -> {
            return !lnis.outbound.isEmpty();
        },"wait for message");
        JUnitUtil.waitFor(() -> {
            return !lnis.outbound.elementAt(lnis.outbound.size() -1).toString().equals("B5 09 10 00");
        }, "COMMON");
        JUnitUtil.waitFor(() -> {
            return !lnis.outbound.elementAt(lnis.outbound.size() -1).toString().equals("BB 09 00 00");
        }, "Slot read");
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

        tm.dispatchThrottle(throtListen2.getThrottle(), throtListen2); // this fails as loco is in use on multiple throttles
        tm.releaseThrottle(throtListen.getThrottle(), throtListen);

        //reset outbound.
        lnis.outbound.clear();
        tm.dispatchThrottle(throtListen2.getThrottle(), throtListen2);

        // Wait for 3 messages, the set slot common and then the get slot refresh
        JUnitUtil.waitFor(() -> {
            return !lnis.outbound.isEmpty();
        },"wait for message");
        JUnitUtil.waitFor(() -> {
            return !lnis.outbound.elementAt(lnis.outbound.size() -1).toString().equals("B5 09 10 00");
        }, "COMMON");
        JUnitUtil.waitFor(() -> {
            return !lnis.outbound.elementAt(lnis.outbound.size() -1).toString().equals("BA 09 00 00");
        }, "DISPATCH");
        JUnitUtil.waitFor(() -> {
            return !lnis.outbound.elementAt(lnis.outbound.size() -1).toString().equals("BB 09 00 00");
        }, "Slot read");

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
