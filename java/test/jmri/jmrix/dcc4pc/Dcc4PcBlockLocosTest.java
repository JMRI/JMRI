package jmri.jmrix.dcc4pc;

import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Dcc4PcBlockLocos: telling several locos in one block apart.
 *
 * @author swalton00 Copyright (C) 2026
 */
public class Dcc4PcBlockLocosTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static final int L9328 = Dcc4PcBlockLocos.LONG | 9328;
    private static final int L9333 = Dcc4PcBlockLocos.LONG | 9333;

    // inputs 1-6; input 4 has RailCom (orientation A), the rest unoccupied
    private static final int[] INPUTS = {1, 2, 3, 4, 5, 6};
    private static final int[] J4_A = {0, 0, 0, 2, 0, 0};
    private static final int[] ALL_CLEAR = {0, 0, 0, 0, 0, 0};

    private static final int ACK = 0x0F;
    private static final int[] COLLIDED_CH1 = {0x9C, 0xFF}; // ADR_HIGH, then an invalid code

    private static Dcc4PcRawDecoder.Packet packet(int[] dcc, int[] states, Dcc4PcRawDecoder.InputData... data) {
        return new Dcc4PcRawDecoder.Packet(Dcc4PcRawDecoder.Kind.DCC, dcc, false, states, List.of(data));
    }

    private static Dcc4PcRawDecoder.InputData j4(int[] dcc, int duplicateOf, int... bytes) {
        return new Dcc4PcRawDecoder.InputData(4, duplicateOf, bytes, dcc);
    }

    private static final int[] TO_9328 = {0xE4, 0x70, 0x3F, 0x80};
    private static final int[] TO_9333 = {0xE4, 0x75, 0x3F, 0x80};
    private static final int[] TO_9999 = {0xE7, 0x0F, 0x3F, 0x80};

    @Test
    public void testTwoLocosFromChannel2() {
        Dcc4PcBlockLocos b = new Dcc4PcBlockLocos(INPUTS);
        List<Dcc4PcBlockLocos.Change> c = b.apply(packet(TO_9328, J4_A, j4(TO_9328, 0, 0x9C, 0x47, ACK)), J4_A, 1000);
        assertEquals(1, c.size());
        assertTrue(c.get(0).added);
        assertEquals(L9328, c.get(0).address);
        // the channel 1 address halves collide with two locos present; channel 2 still identifies 9333
        b.apply(packet(TO_9333, J4_A, j4(TO_9333, 0, COLLIDED_CH1[0], COLLIDED_CH1[1], ACK)), J4_A, 1010);
        assertEquals(List.of(L9328, L9333), b.locos(4));
    }

    @Test
    public void testLoneLocoFromCleanChannel1() {
        Dcc4PcBlockLocos b = new Dcc4PcBlockLocos(INPUTS);
        // ADR_HIGH 0xA4 (9C 47) then ADR_LOW 0x70 (95 C6), with no channel 2 reply
        b.apply(packet(TO_9999, J4_A, j4(TO_9999, 0, 0x9C, 0x47)), J4_A, 1000);
        List<Dcc4PcBlockLocos.Change> c = b.apply(packet(TO_9999, J4_A, j4(TO_9999, 0, 0x95, 0xC6)), J4_A, 1010);
        assertEquals(1, c.size());
        assertEquals(L9328, c.get(0).address);
    }

    @Test
    public void testDuplicatesAreNotEvidence() {
        Dcc4PcBlockLocos b = new Dcc4PcBlockLocos(INPUTS);
        // a packet to 9999 "answered" by a copy of a reply that followed a packet to 9333
        b.apply(packet(TO_9999, J4_A, j4(TO_9333, 1, COLLIDED_CH1[0], COLLIDED_CH1[1], ACK)), J4_A, 1000);
        assertTrue(b.locos(4).isEmpty());
        // seen on the layout: packets to short 9 (no such loco) "answered" by a duplicate whose
        // earlier packet also went to 9, so matching addresses don't make a duplicate trustworthy
        int[] to9 = {0x09, 0xB0};
        b.apply(packet(to9, J4_A, j4(new int[]{0x09, 0x80}, 2, COLLIDED_CH1[0], COLLIDED_CH1[1], ACK)), J4_A, 1010);
        assertTrue(b.locos(4).isEmpty());
        // fresh data does count
        b.apply(packet(TO_9333, J4_A, j4(TO_9333, 0, COLLIDED_CH1[0], COLLIDED_CH1[1], ACK)), J4_A, 1020);
        assertEquals(List.of(L9333), b.locos(4));
    }

    @Test
    public void testUnoccupiedRemovesAll() {
        Dcc4PcBlockLocos b = new Dcc4PcBlockLocos(INPUTS);
        b.apply(packet(TO_9328, J4_A, j4(TO_9328, 0, 0x9C, 0x47, ACK)), J4_A, 1000);
        b.apply(packet(TO_9333, J4_A, j4(TO_9333, 0, COLLIDED_CH1[0], COLLIDED_CH1[1], ACK)), J4_A, 1010);
        // a real unoccupied reading comes only after the block has been empty for 300 ms
        long later = 1010 + Dcc4PcBlockLocos.HEARD_PROVES_OCCUPIED_MILLIS;
        List<Dcc4PcBlockLocos.Change> c = b.apply(packet(TO_9328, ALL_CLEAR), ALL_CLEAR, later);
        assertEquals(2, c.size());
        assertFalse(c.get(0).added);
        assertFalse(c.get(1).added);
        assertTrue(b.locos(4).isEmpty());
    }

    @Test
    public void testHeardBeforeBlockReadsOccupied() {
        // seen on the layout: a loco entering a block was heard before the debounced state
        // went occupied, and was added, removed ("block unoccupied") and added again
        Dcc4PcBlockLocos b = new Dcc4PcBlockLocos(INPUTS);
        List<Dcc4PcBlockLocos.Change> c = b.apply(packet(TO_9328, ALL_CLEAR, j4(TO_9328, 0, 0x9C, 0x47, ACK)), ALL_CLEAR, 1000);
        assertEquals(1, c.size());
        assertTrue(c.get(0).added);
        // the next packets still read unoccupied: the loco stays
        c = b.apply(packet(TO_9333, ALL_CLEAR), ALL_CLEAR, 1010);
        assertTrue(c.isEmpty());
        c = b.apply(packet(TO_9328, J4_A, j4(TO_9328, 0, 0x9C, 0x47, ACK)), J4_A, 1020);
        assertTrue(c.isEmpty());
        assertEquals(List.of(L9328), b.locos(4));
    }

    @Test
    public void testRefreshWhileInBlock() {
        Dcc4PcBlockLocos b = new Dcc4PcBlockLocos(INPUTS);
        b.apply(packet(TO_9328, J4_A, j4(TO_9328, 0, 0x9C, 0x47, ACK)), J4_A, 1000);
        // heard again within REFRESH_MILLIS: nothing to report
        assertTrue(b.apply(packet(TO_9328, J4_A, j4(TO_9328, 0, 0x9C, 0x47, ACK)), J4_A, 1500).isEmpty());
        // heard again after it: a refresh, not an add
        List<Dcc4PcBlockLocos.Change> c = b.apply(packet(TO_9328, J4_A, j4(TO_9328, 0, 0x9C, 0x47, ACK)), J4_A, 1000 + Dcc4PcBlockLocos.REFRESH_MILLIS);
        assertEquals(1, c.size());
        assertTrue(c.get(0).refresh);
        assertFalse(c.get(0).added);
        assertEquals(4, c.get(0).input);
        assertEquals(L9328, c.get(0).address);
        // and the next one waits another REFRESH_MILLIS
        assertTrue(b.apply(packet(TO_9328, J4_A, j4(TO_9328, 0, 0x9C, 0x47, ACK)), J4_A, 1100 + Dcc4PcBlockLocos.REFRESH_MILLIS).isEmpty());
    }

    @Test
    public void testMoveToAnotherBlockAfterDelay() {
        Dcc4PcBlockLocos b = new Dcc4PcBlockLocos(INPUTS);
        int[] j4j5 = {0, 0, 0, 2, 2, 0};
        b.apply(packet(TO_9328, j4j5, j4(TO_9328, 0, 0x9C, 0x47, ACK)), j4j5, 1000);
        Dcc4PcRawDecoder.InputData onJ5 = new Dcc4PcRawDecoder.InputData(5, 0, new int[]{0x9C, 0x47, ACK}, TO_9328);
        // seen on J5 soon after: still on J4 too (bridging the gap)
        b.apply(packet(TO_9328, j4j5, onJ5), j4j5, 1500);
        assertEquals(List.of(L9328), b.locos(4));
        // seen on J5 more than MOVE_MILLIS after J4 last saw it: it has moved
        List<Dcc4PcBlockLocos.Change> c = b.apply(packet(TO_9328, j4j5, onJ5), j4j5, 1000 + Dcc4PcBlockLocos.MOVE_MILLIS + 1);
        c.removeIf(x -> x.refresh); // J5 also refreshes 9328, which is still there
        assertEquals(1, c.size());
        assertEquals(4, c.get(0).input);
        assertFalse(c.get(0).added);
        assertEquals(List.of(L9328), b.locos(5));
    }

    @Test
    public void testAddresses() {
        assertEquals(L9328, Dcc4PcBlockLocos.mobileAddress(TO_9328));
        assertEquals(3, Dcc4PcBlockLocos.mobileAddress(new int[]{0x03, 0x3F, 0x80}));
        assertEquals(-1, Dcc4PcBlockLocos.mobileAddress(new int[]{0xFF, 0x00})); // idle
        assertEquals(-1, Dcc4PcBlockLocos.mobileAddress(new int[]{0x00, 0x00})); // broadcast
        assertEquals(L9328, Dcc4PcBlockLocos.address(0xA4, 0x70));
        assertEquals(-1, Dcc4PcBlockLocos.address(0xC4, 0x70));
        assertEquals("9328(L)", Dcc4PcBlockLocos.name(L9328));
    }

}
