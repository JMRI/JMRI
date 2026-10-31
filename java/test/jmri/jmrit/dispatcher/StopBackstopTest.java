package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jmri.Block;
import jmri.EntryPoint;
import jmri.JmriException;
import jmri.Section;
import jmri.implementation.DefaultSection;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link StopBackstop}.
 * <p>
 * The Section has Blocks A and B. Block P is behind it, where a train enters
 * travelling forward; Block N is beyond it, where a train enters travelling in
 * reverse.
 */
public class StopBackstopTest {

    private Block p;
    private Block a;
    private Block b;
    private Block n;
    private Section section;
    private final List<String> stops = new ArrayList<>();
    private StopBackstop backstop;

    @Test
    public void testBlocksBeyondEachDirection() {
        assertEquals(List.of(n), StopBackstop.blocksBeyond(section, Section.FORWARD), "forward: beyond is N");
        assertEquals(List.of(p), StopBackstop.blocksBeyond(section, Section.REVERSE), "reverse: beyond is P");
    }

    @Test
    public void testBlockBeyondGoingOccupiedStopsTheTrainOnce() throws JmriException {
        backstop = new StopBackstop(section, List.of(n), a, block -> 1000.0, null, stops::add);
        n.setState(Block.OCCUPIED);
        n.setState(Block.UNOCCUPIED);
        n.setState(Block.OCCUPIED);
        assertEquals(1, stops.size(), "stopped once");
        assertTrue(stops.get(0).contains("N"), stops.get(0));
        assertTrue(backstop.isFinished());
    }

    @Test
    public void testDisposedBackstopDoesNothing() throws JmriException {
        backstop = new StopBackstop(section, List.of(n), a, block -> 1000.0, null, stops::add);
        backstop.dispose();
        n.setState(Block.OCCUPIED);
        assertEquals(List.of(), stops, "no stop after dispose");
    }

    @Test
    public void testHeadEnteringABlockOfTheSectionDoesNotStop() {
        backstop = new StopBackstop(section, List.of(), a, block -> 1000.0, null, stops::add);
        backstop.headEntered(b);
        assertEquals(List.of(), stops);
        assertTrue(!backstop.isFinished());
    }

    @Test
    public void testHeadEnteringABlockBeyondTheSectionStops() {
        backstop = new StopBackstop(section, List.of(), a, block -> 1000.0, null, stops::add);
        backstop.headEntered(n);
        assertEquals(1, stops.size());
        assertTrue(stops.get(0).contains("N"), stops.get(0));
    }

    /**
     * At 1000 mm/s, 100 mm takes 100 ms: reckoning must stop the train soon
     * after.
     */
    @Test
    public void testReckoningStopsAtTheEndOfTheSection() {
        backstop = new StopBackstop(section, List.of(), a, block -> 100.0, () -> 1000.0, stops::add);
        JUnitUtil.waitFor(() -> !stops.isEmpty(), "reckoning did not stop the train");
        assertEquals(1, stops.size());
    }

    /**
     * A stationary train is never stopped by reckoning.
     */
    @Test
    public void testReckoningDoesNotStopAStandingTrain() {
        backstop = new StopBackstop(section, List.of(), a, block -> 100.0, () -> 0.0, stops::add);
        JUnitUtil.waitFor(5 * (int) StopBackstop.RECKON_INTERVAL_MS);
        assertEquals(List.of(), stops);
    }

    /**
     * Each Block entered starts the reckoning again from its start: the length
     * still to go is that from the Block entered.
     */
    @Test
    public void testHeadEnteringABlockRestartsTheReckoning() {
        Map<Block, Double> remaining = Map.of(a, 1.0e9, b, 50.0);
        backstop = new StopBackstop(section, List.of(), a, remaining::get, () -> 1000.0, stops::add);
        JUnitUtil.waitFor(3 * (int) StopBackstop.RECKON_INTERVAL_MS);
        assertEquals(List.of(), stops, "a long way to go from A");
        backstop.headEntered(b);
        JUnitUtil.waitFor(() -> !stops.isEmpty(), "reckoning from B did not stop the train");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        p = new Block("IB_P", "P");
        a = new Block("IB_A", "A");
        b = new Block("IB_B", "B");
        n = new Block("IB_N", "N");
        section = new DefaultSection("TS1", "Section AB");
        section.addBlock(a);
        section.addBlock(b);
        EntryPoint enterForward = new EntryPoint(a, p, "East");
        enterForward.setTypeForward();
        section.addToForwardList(enterForward);
        EntryPoint enterReverse = new EntryPoint(b, n, "West");
        enterReverse.setTypeReverse();
        section.addToReverseList(enterReverse);
    }

    @AfterEach
    public void tearDown() {
        if (backstop != null) {
            backstop.dispose();
        }
        JUnitUtil.tearDown();
    }
}
