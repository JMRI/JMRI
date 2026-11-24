package jmri.jmrix.can.cbus.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.jmrit.simulation.link.Latency;
import jmri.jmrit.simulation.link.LinkPreset;
import jmri.jmrit.simulation.link.ManualClock;
import jmri.jmrit.simulation.link.SimulatedLink;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for CbusSimulatedLink and CbusWireTiming.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusSimulatedLinkTest {

    private static final long MS = 1_000_000L;

    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    private ManualClock clock;

    private static CanMessage fiveBytes(int canId) {
        CanMessage m = new CanMessage(new int[]{CbusConstants.CBUS_ACON, 0, 1, 0, 9}, canId);
        return m;
    }

    @Test
    public void testFrameTimeOnTheWire() {
        CbusWireTiming timing = new CbusWireTiming();
        // (47 + 8 x 5) bits, a fifth more for stuffing, at 8 microseconds a bit
        assertEquals(835_200L, timing.nanosOnWire(fiveBytes(10)));
        assertEquals(Math.round(47 * 1.2 * 8000), timing.nanosOnWire(new CanMessage(0, 10)), "no data bytes");
        assertEquals(jmri.jmrit.simulation.link.ArbitrationPolicy.PRIORITY, timing.policy());
    }

    @Test
    public void testPriorityIsTheElevenBitIdentifier() {
        CbusWireTiming timing = new CbusWireTiming();
        CanMessage m = fiveBytes(0x0A);
        assertEquals(m.getHeader() & 0x7FF, timing.priority(m));
        CanMessage lowerId = fiveBytes(0x05);
        assertTrue(timing.priority(lowerId) < timing.priority(m), "same priority bits: the lower CAN ID wins");
    }

    @Test
    public void testOffChangesNothing() {
        CbusSimulatedLink link = new CbusSimulatedLink(memo, () -> clock);
        AtomicInteger handled = new AtomicInteger();
        link.towardsLayout(fiveBytes(10), handled::incrementAndGet);
        assertEquals(1, handled.get(), "handled at once, in the call");
        assertFalse(link.towardsJmri(new CanReply(fiveBytes(10)), System.nanoTime(), t -> {}),
                "the caller delivers the frame itself, as before");
        assertSame(link.getLink(), SimulatedLink.forConnection(memo));
        assertEquals(3, link.getLink().getPresets().size(), "None, CANUSB4 and a network gateway");
    }

    @Test
    public void testAFrameEveryModuleHearsCrossesOnce() {
        CbusSimulatedLink link = new CbusSimulatedLink(memo, () -> clock);
        link.getLink().setPreset(new LinkPreset("test", () -> List.of(new Latency(MS, 0, 1)), List::of));
        clock.runDue();
        CanMessage m = fiveBytes(10);
        AtomicInteger first = new AtomicInteger();
        AtomicInteger second = new AtomicInteger();
        link.towardsLayout(m, first::incrementAndGet);
        link.towardsLayout(m, second::incrementAndGet);
        clock.runDue();
        assertEquals(0, first.get() + second.get(), "not before it has come through");
        clock.advanceTo(clock.now() + 10 * MS);
        JUnitUtil.waitFor(() -> first.get() == 1 && second.get() == 1, "both modules handle it");
        assertEquals(1, link.getLink().getMedium().getCarried(), "on the bus once");
    }

    @Test
    public void testAFrameFromAModuleIsReceivedWhenItComesThrough() {
        CbusSimulatedLink link = new CbusSimulatedLink(memo, () -> clock);
        link.getLink().setPreset(new LinkPreset("test", List::of, () -> List.of(new Latency(MS, 0, 1))));
        clock.runDue();
        List<Long> received = new ArrayList<>();
        long sent = clock.now() + 2 * MS;
        assertTrue(link.towardsJmri(new CanReply(fiveBytes(10)), sent, received::add));
        clock.runDue();
        clock.advanceTo(sent + 10 * MS);
        assertEquals(List.of(sent + 835_200L + MS), received, "its time on the bus, then the interface's");
    }

    @Test
    public void testSimulatorMakesTheLinkAndItsMenu() {
        CbusSimulator sim = new CbusSimulator(memo);
        assertEquals(null, sim.getLinkIfMade(), "none until asked for");
        CbusSimulatedLink link = sim.getLink();
        assertSame(link, sim.getLinkIfMade());
        assertSame(link.getLink(), SimulatedLink.forConnection(memo));
        javax.swing.JMenu menu = sim.makeLinkMenu();
        assertEquals(3, menu.getItemCount());
        sim.dispose();
        assertFalse(link.getLink().isOn());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        clock = new ManualClock(System.nanoTime());
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        tcis.terminateThreads();
        tcis = null;
        memo = null;
        JUnitUtil.tearDown();
    }
}
