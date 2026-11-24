package jmri.jmrit.simulation.link;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SimulatedLink, its stages and its medium, against times worked out
 * by hand, on a clock advanced by hand.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SimulatedLinkTest {

    private static final long T0 = 1_000_000_000L;
    private static final long MS = 1_000_000L;

    /** A made-up system's message: a name, a priority and a time on the wire. */
    private static final class Msg {

        final String name;
        final int priority;
        final long wireNanos;

        Msg(String name, int priority, long wireNanos) {
            this.name = name;
            this.priority = priority;
            this.wireNanos = wireNanos;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static WireTiming timing(ArbitrationPolicy policy) {
        return new WireTiming() {
            @Override
            public long nanosOnWire(@Nonnull Object message) {
                return ((Msg) message).wireNanos;
            }

            @Override
            public int priority(@Nonnull Object message) {
                return ((Msg) message).priority;
            }

            @Override
            @Nonnull
            public ArbitrationPolicy policy() {
                return policy;
            }
        };
    }

    private ManualClock clock;
    private final Map<String, Long> arrived = new LinkedHashMap<>();

    /** A link with one preset, turned on. */
    private SimulatedLink on(ArbitrationPolicy policy, List<Stage> towardsLayout, List<Stage> towardsJmri) {
        LinkPreset preset = new LinkPreset("test", () -> towardsLayout, () -> towardsJmri);
        SimulatedLink link = new SimulatedLink(timing(policy), List.of(preset), () -> clock);
        link.setPreset(preset);
        clock.runDue();
        return link;
    }

    private void send(SimulatedLink link, SimulatedLink.Direction direction, Msg m, long at) {
        clock.advanceTo(at);
        link.send(direction, m, at, t -> arrived.put(m.name, t));
        clock.runDue();
    }

    private void layout(SimulatedLink link, Msg m, long at) {
        send(link, SimulatedLink.Direction.TOWARDS_LAYOUT, m, at);
    }

    private void jmri(SimulatedLink link, Msg m, long at) {
        send(link, SimulatedLink.Direction.TOWARDS_JMRI, m, at);
    }

    private static Msg msg(String name) {
        return new Msg(name, 0, 0);
    }

    @Test
    public void testOffPassesAtOnceOnTheCallersThread() {
        SimulatedLink link = new SimulatedLink(timing(ArbitrationPolicy.FIFO), List.of(), () -> clock);
        assertFalse(link.isOn());
        assertSame(LinkPreset.NONE, link.getPreset());
        assertEquals(List.of(LinkPreset.NONE), link.getPresets(), "None is always offered, first");
        link.send(SimulatedLink.Direction.TOWARDS_LAYOUT, msg("a"), T0 + 5, t -> arrived.put("a", t));
        assertEquals(Map.of("a", T0 + 5), arrived, "arrived in the call, at the time sent");
        assertTrue(clock.isDisposed() == false && link.getMedium() == null, "no clock used, no medium made");
    }

    @Test
    public void testLatencyKeepsOrderAndAddsItsDelay() {
        SimulatedLink link = on(ArbitrationPolicy.FIFO, List.of(new Latency(3 * MS, 0, 1)), List.of());
        layout(link, msg("a"), T0);
        layout(link, msg("b"), T0 + MS);
        assertTrue(arrived.isEmpty(), "nothing before its delay");
        clock.advanceTo(T0 + 10 * MS);
        assertEquals(T0 + 3 * MS, arrived.get("a"));
        assertEquals(T0 + 4 * MS, arrived.get("b"));
    }

    @Test
    public void testLatencyJitterStaysWithinItsBoundsAndKeepsOrder() {
        SimulatedLink link = on(ArbitrationPolicy.FIFO, List.of(new Latency(3 * MS, MS, 7)), List.of());
        for (int i = 0; i < 50; i++) {
            layout(link, msg("m" + i), T0 + i * 100_000L);
        }
        clock.advanceTo(T0 + 100 * MS);
        long last = 0;
        for (int i = 0; i < 50; i++) {
            long t = arrived.get("m" + i);
            long sent = T0 + i * 100_000L;
            assertTrue(t - sent >= 2 * MS - 1, "m" + i + " not before mean minus jitter: " + (t - sent));
            assertTrue(t >= last, "m" + i + " not before the one in front");
            last = t;
        }
    }

    @Test
    public void testBatchingByTimeAndByCount() {
        SimulatedLink link = on(ArbitrationPolicy.FIFO, List.of(), List.of(new Batching(9, 40 * MS)));
        jmri(link, msg("alone"), T0);
        clock.advanceTo(T0 + 39 * MS);
        assertTrue(arrived.isEmpty(), "held until 40 ms");
        clock.advanceTo(T0 + 40 * MS);
        assertEquals(T0 + 40 * MS, arrived.get("alone"), "one message: released at 40 ms");

        arrived.clear();
        long start = T0 + 100 * MS;
        for (int i = 1; i <= 9; i++) {
            jmri(link, msg("m" + i), start + i * MS);
        }
        for (int i = 1; i <= 9; i++) {
            assertEquals(start + 9 * MS, arrived.get("m" + i), "nine: all released with the ninth");
        }
        clock.advanceTo(start + 200 * MS);
        assertEquals(9, arrived.size(), "and the 40 ms timer of that batch releases nothing more");
    }

    @Test
    public void testAckHoldSendsTheFirstAndHoldsTheRestUntilTheAck() {
        SimulatedLink link = on(ArbitrationPolicy.FIFO, List.of(new AckHold(100 * MS)), List.of());
        layout(link, msg("a"), T0);
        layout(link, msg("b"), T0 + MS);
        layout(link, msg("c"), T0 + 2 * MS);
        assertEquals(Map.of("a", T0), arrived, "the first goes at once");
        clock.advanceTo(T0 + 100 * MS);
        assertEquals(T0 + 100 * MS, arrived.get("b"), "the rest when the first is acknowledged");
        assertEquals(T0 + 100 * MS, arrived.get("c"));
        layout(link, msg("d"), T0 + 150 * MS);
        clock.advanceTo(T0 + 199 * MS);
        assertFalse(arrived.containsKey("d"), "b and c are unacknowledged in turn");
        clock.advanceTo(T0 + 200 * MS);
        assertEquals(T0 + 200 * MS, arrived.get("d"));
    }

    @Test
    public void testLostWakeupHoldsAMessageUntilTheNextAfterItIsFree() {
        LostWakeup stage = new LostWakeup(2 * MS);
        SimulatedLink link = on(ArbitrationPolicy.FIFO, List.of(), List.of(stage));
        jmri(link, msg("a"), T0);
        jmri(link, msg("b"), T0 + MS); // while busy with a
        assertEquals(Map.of("a", T0), arrived);
        clock.advanceTo(T0 + 500 * MS);
        assertFalse(arrived.containsKey("b"), "stuck, however long");
        assertEquals(1, stage.getStuck());
        jmri(link, msg("c"), T0 + 500 * MS);
        assertEquals(T0 + 500 * MS, arrived.get("b"), "the next message wakes it");
        assertEquals(T0 + 500 * MS, arrived.get("c"));
        assertEquals(0, stage.getStuck());
    }

    @Test
    public void testLossDropsWhenToldAndByProbability() {
        Loss loss = new Loss(0, 1);
        SimulatedLink link = on(ArbitrationPolicy.FIFO, List.of(loss), List.of());
        loss.dropNext(1);
        layout(link, msg("a"), T0);
        layout(link, msg("b"), T0 + MS);
        assertEquals(Map.of("b", T0 + MS), arrived);
        assertEquals(1, loss.getDropped());

        Loss always = new Loss(1, 1);
        SimulatedLink lossy = on(ArbitrationPolicy.FIFO, List.of(always), List.of());
        layout(lossy, msg("c"), T0 + 2 * MS);
        assertFalse(arrived.containsKey("c"));
        assertThrows(IllegalArgumentException.class, () -> new Loss(1.5, 1));
    }

    @Test
    public void testMediumOneAtATimeInOrderOfArrival() {
        SimulatedLink link = on(ArbitrationPolicy.FIFO, List.of(), List.of());
        layout(link, new Msg("a", 9, 835_000), T0);
        layout(link, new Msg("b", 1, 835_000), T0);
        layout(link, new Msg("c", 5, 835_000), T0);
        clock.advanceTo(T0 + 10 * MS);
        assertEquals(T0 + 835_000, arrived.get("a"));
        assertEquals(T0 + 2 * 835_000, arrived.get("b"), "FIFO: priority doesn't count");
        assertEquals(T0 + 3 * 835_000, arrived.get("c"));
        assertEquals(3, link.getMedium().getCarried());
    }

    @Test
    public void testMediumByPriorityWhenTheWireComesFree() {
        SimulatedLink link = on(ArbitrationPolicy.PRIORITY, List.of(), List.of());
        layout(link, new Msg("first", 9, MS), T0); // takes the free wire at once
        layout(link, new Msg("low", 9, MS), T0 + 100_000);
        jmri(link, new Msg("high", 1, MS), T0 + 200_000); // from the layout side, on the same wire
        clock.advanceTo(T0 + 10 * MS);
        assertEquals(T0 + MS, arrived.get("first"));
        assertEquals(T0 + 2 * MS, arrived.get("high"), "the higher priority goes next, whichever side");
        assertEquals(T0 + 3 * MS, arrived.get("low"));
    }

    @Test
    public void testBufferBlocksWhenFull() {
        SimulatedLink link = on(ArbitrationPolicy.FIFO, List.of(new Buffer(1, Buffer.WhenFull.BLOCK)), List.of());
        layout(link, new Msg("a", 0, MS), T0);
        layout(link, new Msg("b", 0, MS), T0);
        layout(link, new Msg("c", 0, MS), T0);
        clock.advanceTo(T0 + 10 * MS);
        assertEquals(T0 + MS, arrived.get("a"));
        assertEquals(T0 + 2 * MS, arrived.get("b"));
        assertEquals(T0 + 3 * MS, arrived.get("c"));
    }

    @Test
    public void testBufferDropsWhenFull() {
        Buffer buffer = new Buffer(1, Buffer.WhenFull.DROP);
        SimulatedLink link = on(ArbitrationPolicy.FIFO, List.of(buffer), List.of());
        layout(link, new Msg("a", 0, MS), T0); // straight onto the wire: its place is free
        layout(link, new Msg("b", 0, MS), T0); // waits for the wire, in the buffer
        layout(link, new Msg("c", 0, MS), T0); // no room
        clock.advanceTo(T0 + 10 * MS);
        assertEquals(List.of("a", "b"), new ArrayList<>(arrived.keySet()));
        assertEquals(1, buffer.getDropped());
    }

    @Test
    public void testTurningOffLetsOutWhatIsHeld() {
        LostWakeup stage = new LostWakeup(2 * MS);
        SimulatedLink link = on(ArbitrationPolicy.FIFO, List.of(), List.of(stage));
        jmri(link, msg("a"), T0);
        jmri(link, msg("b"), T0 + MS);
        assertFalse(arrived.containsKey("b"));
        link.setPreset(LinkPreset.NONE);
        clock.runDue();
        assertTrue(arrived.containsKey("b"), "let out when the link is turned off");
        assertFalse(link.isOn());
        assertTrue(clock.isDisposed());
        link.send(SimulatedLink.Direction.TOWARDS_JMRI, msg("c"), T0 + 2 * MS, t -> arrived.put("c", t));
        assertEquals(T0 + 2 * MS, arrived.get("c"), "off again: at once");
    }

    @Test
    public void testStagesCountWhatTheyHeld() {
        Latency latency = new Latency(3 * MS, 0, 1);
        SimulatedLink link = on(ArbitrationPolicy.FIFO, List.of(latency), List.of());
        layout(link, msg("a"), T0);
        layout(link, msg("b"), T0);
        clock.advanceTo(T0 + 10 * MS);
        assertEquals(2, latency.getPassed());
        assertEquals(6 * MS, latency.getHeldNanos());
        assertEquals(3 * MS, latency.getLongestHeldNanos());
        assertEquals(List.of(latency), link.getStages(SimulatedLink.Direction.TOWARDS_LAYOUT));
        assertEquals("Latency 3.0 ms ± 0.0 ms", latency.describe());
    }

    @Test
    public void testForConnection() {
        jmri.jmrix.internal.InternalSystemConnectionMemo memo = new jmri.jmrix.internal.InternalSystemConnectionMemo();
        assertEquals(null, SimulatedLink.forConnection(memo));
        SimulatedLink link = new SimulatedLink(timing(ArbitrationPolicy.FIFO), List.of(), () -> clock);
        SimulatedLink.register(memo, link);
        assertSame(link, SimulatedLink.forConnection(memo));
        memo.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        clock = new ManualClock(T0);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
