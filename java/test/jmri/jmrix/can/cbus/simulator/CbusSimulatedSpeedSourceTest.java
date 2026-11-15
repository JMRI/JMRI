package jmri.jmrix.can.cbus.simulator;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jmri.DccLocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrit.simulation.SimulatedSpeedSource.Command;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for CbusSimulatedSpeedSource.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusSimulatedSpeedSourceTest {

    private static final DccLocoAddress LOCO = new DccLocoAddress(1234, true);

    /**
     * Every speed and direction byte, in each mode, read as the NMRA standard
     * for the DCC speed and direction instruction gives it, worked out here
     * from the standard's table rather than from a formula shared with the
     * code.
     */
    @Test
    public void testEveryByteReadAsTheNmraStandardGivesIt() {
        for (int b = 0; b < 256; b++) {
            int v = b & 0x7f;
            boolean forward = (b & 0x80) != 0;

            Command c = CbusSimulatedSpeedSource.command(b, SpeedStepMode.NMRA_DCC_128, 5L);
            Assertions.assertEquals(SpeedStepMode.NMRA_DCC_128, c.getMode());
            Assertions.assertEquals(forward, c.isForward(), "direction of " + b);
            Assertions.assertEquals(v == 1, c.isEmergencyStop(), "128: emergency stop is 1 only, byte " + b);
            int expected128 = (v == 0 || v == 1) ? 0 : v - 1; // 2 is step 1, 127 step 126
            Assertions.assertEquals(expected128, c.getSpeedStep(), "128 steps, byte " + b);
            Assertions.assertEquals(5L, c.getTimeNanos());

            c = CbusSimulatedSpeedSource.command(b, SpeedStepMode.NMRA_DCC_28, 0L);
            Assertions.assertEquals(SpeedStepMode.NMRA_DCC_28, c.getMode());
            Assertions.assertEquals(v == 2 || v == 3, c.isEmergencyStop(), "28: emergency stop is 2 and 3, byte " + b);
            int expected28 = v < 4 ? 0 : Math.min(v - 3, 28); // 4 is step 1, 31 step 28
            Assertions.assertEquals(expected28, c.getSpeedStep(), "28 steps, byte " + b);

            c = CbusSimulatedSpeedSource.command(b, SpeedStepMode.NMRA_DCC_14, 0L);
            Assertions.assertEquals(SpeedStepMode.NMRA_DCC_14, c.getMode());
            Assertions.assertEquals(v == 1, c.isEmergencyStop(), "14: emergency stop is 1, byte " + b);
            int expected14 = v < 2 ? 0 : Math.min(v - 1, 14); // 2 is step 1, 15 step 14
            Assertions.assertEquals(expected14, c.getSpeedStep(), "14 steps, byte " + b);
        }
    }

    @Test
    public void testFollowsSessionsOnlyWhileWatching() {
        CbusSimulator sim = new CbusSimulator(memo);
        CbusDummyCS cs = sim.getCS(0);
        CbusSimulatedSpeedSource source = new CbusSimulatedSpeedSource(sim);
        List<DccLocoAddress> told = new CopyOnWriteArrayList<>();
        source.addCommandListener(told::add);

        cs.message(new CanMessage(new int[]{CbusConstants.CBUS_RLOC, 0xC4, 0xD2}, 0x12)); // 1234 long, session 1
        cs.message(new CanMessage(new int[]{CbusConstants.CBUS_DSPD, 1, 0x80 | 41}, 0x12));
        Assertions.assertFalse(source.isFollowing(), "nothing followed before an address is watched");
        Assertions.assertNull(source.getCommand(LOCO));

        source.watch(Set.of(LOCO));
        Assertions.assertTrue(source.isFollowing());
        Command c = Objects.requireNonNull(source.getCommand(LOCO), "the session already open is read when watching starts");
        Assertions.assertEquals(40, c.getSpeedStep());
        Assertions.assertTrue(c.isForward());
        Assertions.assertEquals(List.of(LOCO), told);

        long before = System.nanoTime();
        cs.message(new CanMessage(new int[]{CbusConstants.CBUS_DSPD, 1, 61}, 0x12)); // reverse, step 60
        c = Objects.requireNonNull(source.getCommand(LOCO), "a command");
        Assertions.assertEquals(60, c.getSpeedStep());
        Assertions.assertFalse(c.isForward());
        Assertions.assertTrue(c.getTimeNanos() - before >= 0, "received when the command station changed the session");
        Assertions.assertEquals(2, told.size());

        cs.message(new CanMessage(new int[]{CbusConstants.CBUS_DSPD, 1, 61}, 0x12)); // the same again
        Assertions.assertEquals(2, told.size(), "a repeat changes nothing for the decoder, so is not told");

        cs.message(new CanMessage(new int[]{CbusConstants.CBUS_STMOD, 1, CbusConstants.CBUS_SS_28}, 0x12));
        c = Objects.requireNonNull(source.getCommand(LOCO), "a command");
        Assertions.assertEquals(SpeedStepMode.NMRA_DCC_28, c.getMode());
        Assertions.assertEquals(28, c.getSpeedStep(), "61 in 28 steps is beyond the range: the top step");

        cs.message(new CanMessage(new int[]{CbusConstants.CBUS_KLOC, 1}, 0x12));
        Assertions.assertEquals(c, source.getCommand(LOCO), "the loco keeps its last command when its session ends");

        source.watch(Set.of());
        Assertions.assertFalse(source.isFollowing());
        int count = told.size();
        cs.message(new CanMessage(new int[]{CbusConstants.CBUS_RLOC, 0xC4, 0xD2}, 0x12));
        cs.message(new CanMessage(new int[]{CbusConstants.CBUS_DSPD, 1, 0x80 | 10}, 0x12)); // session 1 again
        Assertions.assertEquals(count, told.size(), "not told once nothing is watched");

        source.dispose();
        Assertions.assertNull(source.getCommand(LOCO));
        source.watch(Set.of(LOCO));
        Assertions.assertFalse(source.isFollowing(), "never follows again once disposed");
        sim.dispose();
    }

    @Test
    public void testEmergencyStopOfEveryLoco() {
        CbusSimulator sim = new CbusSimulator(memo);
        CbusDummyCS cs = sim.getCS(0);
        CbusSimulatedSpeedSource source = new CbusSimulatedSpeedSource(sim);
        source.watch(Set.of(LOCO));
        cs.message(new CanMessage(new int[]{CbusConstants.CBUS_RLOC, 0xC4, 0xD2}, 0x12));
        cs.message(new CanMessage(new int[]{CbusConstants.CBUS_STMOD, 1, CbusConstants.CBUS_SS_28}, 0x12));
        cs.message(new CanMessage(new int[]{CbusConstants.CBUS_DSPD, 1, 0x80 | 20}, 0x12));
        Command c = Objects.requireNonNull(source.getCommand(LOCO), "a command");
        Assertions.assertFalse(c.isEmergencyStop());
        cs.message(new CanMessage(new int[]{CbusConstants.CBUS_RESTP}, 0x12));
        c = Objects.requireNonNull(source.getCommand(LOCO), "a command");
        Assertions.assertTrue(c.isEmergencyStop(), "an emergency stop in 28 steps too");
        sim.dispose();
    }

    private TrafficControllerScaffold tc;
    private CanSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
    }

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        memo.dispose();
        tc = null;
        memo = null;
        JUnitUtil.tearDown();
    }
}
