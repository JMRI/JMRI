package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import jmri.DccLocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for CbusSimulatedCab: a handset on the simulated bus, whose requests
 * the simulated command station acts on as it would a real cab's.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusSimulatedCabTest {

    private static final DccLocoAddress LOCO = SimulationTestUtil.address(3);

    private CanSystemConnectionMemo memo;
    private CbusDummyCS cs;
    private final List<CbusSimulatedCab> cabs = new ArrayList<>();

    private CbusSimulatedCab cab() {
        AtomicReference<CbusSimulatedCab> c = new AtomicReference<>();
        ThreadingUtil.runOnLayout(() -> c.set(new CbusSimulatedCab(memo)));
        cabs.add(c.get());
        return c.get();
    }

    private CbusSimulatedCab cabWithSession(DccLocoAddress loco) {
        CbusSimulatedCab cab = cab();
        ThreadingUtil.runOnLayout(() -> cab.acquire(loco));
        JUnitUtil.waitFor(() -> cab.getSession() >= 0, "the cab's session for " + loco);
        return cab;
    }

    /**
     * The command station's sessions, read on the layout thread (Swing
     * thread), where it handles frames: {@code invokeAndWait} makes what it
     * did there visible to this thread.
     */
    private List<CbusDummyCSSession> sessions() {
        return ThreadingUtil.runOnGUIwithReturn(cs::getSessions);
    }

    private SpeedStepMode mode(CbusDummyCSSession s) {
        return ThreadingUtil.runOnGUIwithReturn(s::getSpeedStepMode);
    }

    private CbusDummyCSSession session(int number) {
        for (CbusDummyCSSession s : sessions()) {
            if (s.getSessionNumber() == number) {
                return s;
            }
        }
        return null;
    }

    @Test
    public void testSpeedStepModeChangesTheSession() {
        CbusSimulatedCab cab = cabWithSession(LOCO);
        CbusDummyCSSession s = session(cab.getSession());
        Assertions.assertNotNull(s, "the command station has the cab's session");
        Assertions.assertEquals(SpeedStepMode.NMRA_DCC_128, mode(s), "128 steps until asked otherwise");
        for (SpeedStepMode mode : new SpeedStepMode[]{SpeedStepMode.NMRA_DCC_28, SpeedStepMode.NMRA_DCC_14,
                SpeedStepMode.NMRA_DCC_128}) {
            ThreadingUtil.runOnLayout(() -> cab.setSpeedStepMode(mode));
            JUnitUtil.waitFor(() -> mode(s) == mode, () -> "the session in " + mode + ", has " + mode(s));
        }
        Assertions.assertFalse(cab.isRefused());
    }

    @Test
    public void testReleaseEndsTheSession() {
        CbusSimulatedCab cab = cabWithSession(LOCO);
        int number = cab.getSession();
        ThreadingUtil.runOnLayout(cab::release);
        Assertions.assertEquals(-1, cab.getSession(), "the cab has no session once released");
        JUnitUtil.waitFor(() -> session(number) == null, "the command station ended the session");
    }

    @Test
    public void testLocoInUseIsRefused() {
        CbusSimulatedCab first = cabWithSession(LOCO);
        CbusSimulatedCab second = cab();
        ThreadingUtil.runOnLayout(() -> second.acquire(LOCO));
        JUnitUtil.waitFor(second::isRefused, "the second cab refused, as the loco is in use");
        Assertions.assertEquals(-1, second.getSession(), "no session for the refused cab");
        Assertions.assertFalse(first.isRefused(), "the first cab keeps its session");
        Assertions.assertTrue(first.getSession() >= 0);
        Assertions.assertEquals(1, sessions().size(), "one session for the loco");

        // once released, the loco can be had again
        ThreadingUtil.runOnLayout(first::release);
        JUnitUtil.waitFor(() -> sessions().isEmpty(), "the first session ended");
        ThreadingUtil.runOnLayout(() -> second.acquire(LOCO));
        JUnitUtil.waitFor(() -> second.getSession() >= 0, "the second cab's session");
        Assertions.assertFalse(second.isRefused(), "asking again clears the refusal");
    }

    @Test
    public void testCommandsNeedASession() {
        CbusSimulatedCab cab = cab();
        Assertions.assertEquals(-1, cab.getSession(), "no session before asking");
        List<Runnable> commands = List.of(() -> cab.setSpeedDirection(0x80),
                () -> cab.setSpeedStepMode(SpeedStepMode.NMRA_DCC_28), cab::release);
        for (Runnable command : commands) {
            AtomicReference<RuntimeException> thrown = new AtomicReference<>();
            ThreadingUtil.runOnLayout(() -> {
                try {
                    command.run();
                } catch (IllegalStateException e) {
                    thrown.set(e);
                }
            });
            Assertions.assertNotNull(thrown.get(), "refused without a session");
        }
        Assertions.assertTrue(sessions().isEmpty(), "nothing reached the command station");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        SimulationTestUtil.setUpSimulatedProfile();
        memo = CbusSimulationTestUtil.addCanSimulation("M");
        cs = memo.get(CbusSimulator.class).getCS(0);
    }

    @AfterEach
    public void tearDown() {
        ThreadingUtil.runOnLayout(() -> cabs.forEach(CbusSimulatedCab::dispose));
        cabs.clear();
        CbusSimulationTestUtil.dispose(memo);
        memo = null;
        cs = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
