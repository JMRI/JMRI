package jmri.jmrix.can.cbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.LocoSpeedSource;
import jmri.SpeedStepMode;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Every cab's speed and direction commands, from the sessions on the bus.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusLocoSpeedSourceTest {

    private TrafficControllerScaffold tc;
    private CanSystemConnectionMemo memo;
    private CbusLocoSpeedSource source;
    private final List<String> told = new ArrayList<>();
    private final List<Long> times = new ArrayList<>();

    private final LocoSpeedSource.Listener recorder = new LocoSpeedSource.Listener() {
        @Override
        public void speedCommanded(@Nonnull LocoAddress address, float speed, boolean forward, long timeNanos) {
            told.add(address + " " + speed + (forward ? " forward" : " reverse"));
            times.add(timeNanos);
        }

        @Override
        public void released(@Nonnull LocoAddress address, long timeNanos) {
            told.add(address + " released");
            times.add(timeNanos);
        }
    };

    private static CanReply reply(long timeNanos, int... bytes) {
        CanReply r = new CanReply(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            r.setElement(i, bytes[i]);
        }
        r.setReceivedTime(timeNanos);
        return r;
    }

    private static CanMessage message(int... bytes) {
        CanMessage m = new CanMessage(bytes.length, 0x12);
        for (int i = 0; i < bytes.length; i++) {
            m.setElement(i, bytes[i]);
        }
        return m;
    }

    /** The command station reports a session: short address 3, stopped, forward. */
    private void sessionFor3(int session) {
        source.reply(reply(100, CbusConstants.CBUS_PLOC, session, 0x00, 3, 0x80, 0, 0, 0));
    }

    @Test
    public void testOtherCabsCommandsReportedByAddress() {
        sessionFor3(7);
        assertEquals(List.of("3(S) 0.0 forward"), told, "the session's starting speed");
        source.reply(reply(200, CbusConstants.CBUS_DSPD, 7, 0x80 | 64));
        assertEquals("3(S) " + (63 / 126f) + " forward", told.get(1));
        assertEquals(200L, times.get(1), "timed when the frame was received");
        source.reply(reply(300, CbusConstants.CBUS_DSPD, 7, 64));
        assertEquals("3(S) " + (63 / 126f) + " reverse", told.get(2));
        source.reply(reply(400, CbusConstants.CBUS_DSPD, 7, 0x80 | 1));
        assertEquals("3(S) -1.0 forward", told.get(3), "an emergency stop");
    }

    @Test
    public void testJmrisOwnCommandsReported() {
        sessionFor3(7);
        long before = System.nanoTime();
        source.message(message(CbusConstants.CBUS_DSPD, 7, 0x80 | 20));
        long after = System.nanoTime();
        assertEquals("3(S) " + (19 / 126f) + " forward", told.get(1));
        assertTrue(times.get(1) >= before && times.get(1) <= after, "timed when JMRI sent it");
    }

    @Test
    public void testLongAddress() {
        source.reply(reply(1, CbusConstants.CBUS_PLOC, 9, 0xc0 | 0x10, 0x92, 0x80 | 30, 0, 0, 0));
        assertEquals(List.of("4242(L) " + (29 / 126f) + " forward"), told, "address 0x1092, already moving");
        assertEquals(new DccLocoAddress(4242, true), source.getAddress(9));
    }

    @Test
    public void testUnknownSessionAndRepeatsIgnored() {
        source.reply(reply(1, CbusConstants.CBUS_DSPD, 5, 0x80 | 40));
        assertTrue(told.isEmpty(), "a session not yet reported: which loco is unknown");
        sessionFor3(5);
        source.reply(reply(2, CbusConstants.CBUS_DSPD, 5, 0x80 | 40));
        source.reply(reply(3, CbusConstants.CBUS_DSPD, 5, 0x80 | 40));
        source.message(message(CbusConstants.CBUS_DSPD, 5, 0x80 | 40));
        assertEquals(2, told.size(), "repeats, as cabs send, are not changes");
    }

    @Test
    public void testSessionEnds() {
        sessionFor3(7);
        source.reply(reply(5, CbusConstants.CBUS_KLOC, 7));
        assertEquals("3(S) released", told.get(1));
        assertEquals(5L, times.get(1));
        source.reply(reply(6, CbusConstants.CBUS_DSPD, 7, 0x80 | 40));
        assertEquals(2, told.size(), "the session has ended");

        sessionFor3(8);
        source.reply(reply(7, CbusConstants.CBUS_ERR, 8, 0, CbusConstants.ERR_LOCO_STACK_FULL));
        assertEquals(3, told.size(), "other errors don't end a session");
        source.reply(reply(8, CbusConstants.CBUS_ERR, 8, 0, CbusConstants.ERR_SESSION_CANCELLED));
        assertEquals("3(S) released", told.get(3));

        sessionFor3(9);
        source.reply(reply(9, CbusConstants.CBUS_ERR, 9, 0, CbusConstants.ERR_SESSION_NOT_PRESENT));
        assertEquals("3(S) released", told.get(5));
        assertNull(source.getAddress(9));

        source.reply(reply(10, CbusConstants.CBUS_KLOC, 9));
        assertEquals(6, told.size(), "a session not known is not released");
    }

    @Test
    public void testEmergencyStopOfAll() {
        sessionFor3(1);
        source.reply(reply(1, CbusConstants.CBUS_PLOC, 2, 0x00, 4, 0x80 | 50, 0, 0, 0));
        source.reply(reply(1, CbusConstants.CBUS_PLOC, 3, 0x00, 5, 30, 0, 0, 0));
        told.clear();
        source.reply(reply(2, CbusConstants.CBUS_ESTOP));
        assertEquals(3, told.size());
        assertTrue(told.containsAll(List.of("3(S) -1.0 forward", "4(S) -1.0 forward", "5(S) -1.0 reverse")));
        source.message(message(CbusConstants.CBUS_RESTP));
        assertEquals(3, told.size(), "already stopped");
    }

    @Test
    public void testStepModeFollowed() {
        sessionFor3(7);
        source.reply(reply(1, CbusConstants.CBUS_STMOD, 7, CbusConstants.CBUS_SS_28));
        source.reply(reply(2, CbusConstants.CBUS_DSPD, 7, 0x80 | 17));
        assertEquals("3(S) 0.5 forward", told.get(1), "28 steps: step 14 is half");
        source.reply(reply(3, CbusConstants.CBUS_STMOD, 7, CbusConstants.CBUS_SS_14));
        source.reply(reply(4, CbusConstants.CBUS_DSPD, 7, 0x80 | 8));
        assertEquals("3(S) 0.5 forward", told.get(1));
        assertEquals(2, told.size(), "14 steps: step 7 is half, the same speed");
        source.reply(reply(5, CbusConstants.CBUS_DSPD, 7, 0x80 | 15));
        assertEquals("3(S) 1.0 forward", told.get(2));
    }

    /**
     * Speeds are read as JMRI's own CBUS throttle reads them, in each mode.
     */
    @Test
    public void testReadAsCbusThrottleReadsThem() {
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tc2 = new TrafficControllerScaffold();
        memo.setTrafficController(tc2);
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.MERGCBUS);
        memo.configureManagers();
        CbusThrottle throttle = new CbusThrottle(memo, new DccLocoAddress(3, false), 1);
        throttle.setSpeedStepMode(SpeedStepMode.NMRA_DCC_128);
        for (int s = 0; s < 128; s++) {
            assertEquals(throttle.floatSpeed(s), CbusLocoSpeedSource.toSpeedSetting(s, CbusConstants.CBUS_SS_128),
                    "128 steps, " + s);
        }
        throttle.setSpeedStepMode(SpeedStepMode.NMRA_DCC_28);
        for (int s = 0; s < 32; s++) {
            assertEquals(throttle.floatSpeed(s), CbusLocoSpeedSource.toSpeedSetting(s, CbusConstants.CBUS_SS_28),
                    "28 steps, " + s);
        }
        throttle.setSpeedStepMode(SpeedStepMode.NMRA_DCC_14);
        for (int s = 0; s < 16; s++) {
            assertEquals(throttle.floatSpeed(s), CbusLocoSpeedSource.toSpeedSetting(s, CbusConstants.CBUS_SS_14),
                    "14 steps, " + s);
        }
        throttle.dispose(null);
        memo.dispose();
        tc2.terminateThreads();
    }

    @Test
    public void testOtherFramesIgnored() {
        sessionFor3(7);
        CanReply extended = reply(1, CbusConstants.CBUS_DSPD, 7, 0x80 | 40);
        extended.setExtended(true);
        source.reply(extended);
        source.reply(reply(1, CbusConstants.CBUS_DSPD, 7)); // too short
        source.reply(reply(1, CbusConstants.CBUS_DFUN, 7, 1, 1));
        assertEquals(1, told.size());
    }

    @Test
    public void testFailingListenerDoesNotStopTheOthers() {
        source.removeLocoSpeedListener(recorder);
        source.addLocoSpeedListener(new LocoSpeedSource.Listener() {
            @Override
            public void speedCommanded(@Nonnull LocoAddress address, float speed, boolean forward, long timeNanos) {
                throw new IllegalStateException("failing on purpose");
            }

            @Override
            public void released(@Nonnull LocoAddress address, long timeNanos) {
                throw new IllegalStateException("failing on purpose");
            }
        });
        source.addLocoSpeedListener(recorder);
        source.addLocoSpeedListener(recorder);
        sessionFor3(7);
        JUnitAppender.assertErrorMessageStartsWith("A loco speed listener failed on 3(S)");
        source.reply(reply(1, CbusConstants.CBUS_KLOC, 7));
        JUnitAppender.assertErrorMessageStartsWith("A loco speed listener failed on the release of 3(S)");
        assertEquals(List.of("3(S) 0.0 forward", "3(S) released"), told, "told once each");
    }

    @Test
    public void testDisposeStopsListening() {
        int before = tc.numListeners();
        source.dispose();
        assertEquals(before - 1, tc.numListeners());
        source = new CbusLocoSpeedSource(memo);
    }

    @Test
    public void testDisabledConnectionHasNone() {
        CanSystemConnectionMemo memo2 = new CanSystemConnectionMemo();
        TrafficControllerScaffold tc2 = new TrafficControllerScaffold();
        memo2.setTrafficController(tc2);
        memo2.setProtocol(jmri.jmrix.can.ConfigurationManager.MERGCBUS);
        memo2.setDisabled(true);
        memo2.configureManagers();
        assertNull(memo2.get(LocoSpeedSource.class));
        assertEquals(0, tc2.numListeners(), "nothing listening to a disabled connection");
        memo2.dispose();
        tc2.terminateThreads();
    }

    @Test
    public void testConnectionWithNoTrafficController() {
        CanSystemConnectionMemo memo2 = new CanSystemConnectionMemo();
        memo2.setProtocol(jmri.jmrix.can.ConfigurationManager.MERGCBUS);
        memo2.configureManagers();
        assertTrue(memo2.get(LocoSpeedSource.class) instanceof CbusLocoSpeedSource);
        // and it is disposed of with the connection, having nothing to stop listening to
        memo2.dispose();
    }

    @Test
    public void testEachCbusConnectionHasOne() {
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tc2 = new TrafficControllerScaffold();
        memo.setTrafficController(tc2);
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.MERGCBUS);
        memo.configureManagers();
        LocoSpeedSource fromMemo = memo.get(LocoSpeedSource.class);
        assertTrue(fromMemo instanceof CbusLocoSpeedSource);
        assertTrue(InstanceManager.getList(LocoSpeedSource.class).contains(fromMemo));
        int listening = tc2.numListeners();
        memo.dispose();
        assertTrue(tc2.numListeners() < listening, "stops listening when the connection is disposed");
        assertTrue(!InstanceManager.getList(LocoSpeedSource.class).contains(fromMemo));
        tc2.terminateThreads();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.MERGCBUS);
        source = new CbusLocoSpeedSource(memo);
        source.addLocoSpeedListener(recorder);
    }

    @AfterEach
    public void tearDown() {
        source.dispose();
        memo.dispose();
        tc.terminateThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
