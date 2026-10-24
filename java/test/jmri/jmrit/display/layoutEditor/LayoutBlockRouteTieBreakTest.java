package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jmri.Block;
import jmri.InstanceManager;
import jmri.jmrit.display.EditorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Check that LayoutBlock.getNextBestBlock chooses between routes of equal
 * cost by fewer hops and then by the next block system name, rather than by
 * the order in which the routes were learnt. Each choice is checked with the
 * routes held in their learnt order and in reverse order.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutBlockRouteTieBreakTest {

    private static final LayoutBlockConnectivityTools.Metric METRIC = LayoutBlockConnectivityTools.Metric.METRIC;

    /**
     * Find the lowest metric routes to a destination that can be used when
     * arriving from the previous block, in the order they are held.
     */
    private static List<Integer> lowestMetricRoutes(LayoutBlock lb, Block previous, Block dest) {
        int bestMetric = Integer.MAX_VALUE;
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < lb.getNumberOfRoutes(); i++) {
            Block next = lb.getRouteNextBlockAtIndex(i);
            if (lb.getRouteDestBlockAtIndex(i) != dest || !lb.validThroughPath(previous, next)) {
                continue;
            }
            if (lb.getRouteMetric(i) < bestMetric) {
                bestMetric = lb.getRouteMetric(i);
                candidates.clear();
            }
            if (lb.getRouteMetric(i) == bestMetric) {
                candidates.add(i);
            }
        }
        return candidates;
    }

    /**
     * The next block the rule picks among routes of equal metric: fewer hops,
     * then the lower next block system name.
     */
    private static Block expectedNextBlock(LayoutBlock lb, List<Integer> candidates) {
        int expected = candidates.get(0);
        for (int c : candidates) {
            int hops = lb.getRouteHopCountAtIndex(c);
            int expectedHops = lb.getRouteHopCountAtIndex(expected);
            if ((hops < expectedHops) || ((hops == expectedHops)
                    && (lb.getRouteNextBlockAtIndex(c).getSystemName().compareTo(
                            lb.getRouteNextBlockAtIndex(expected).getSystemName()) < 0))) {
                expected = c;
            }
        }
        return lb.getRouteNextBlockAtIndex(expected);
    }

    private static void reverseRoutes(LayoutBlock lb) throws ReflectiveOperationException {
        Field field = LayoutBlock.class.getDeclaredField("routes");
        field.setAccessible(true);
        List<?> routes = (List<?>) field.get(lb);
        // routing tables are only changed on the layout thread
        ThreadingUtil.runOnLayout(() -> Collections.reverse(routes));
    }

    @Test
    public void testEqualCostRoutesChosenByRule() throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        File f = new File("java/test/jmri/jmrit/display/layoutEditor/load/Jlang-Panel-4-19-6.xml");
        assertTrue(cm.load(f), "panel loaded");
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        ThreadingUtil.runOnGUI(() -> lbm.initializeLayoutBlockPaths());
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised");
        // the stable event is posted to the layout thread; let its listeners,
        // such as Signal Mast Logic, finish with the routing tables before
        // the routes are reordered below
        ThreadingUtil.runOnLayout(() -> {});

        int ties = 0;
        int firstLearntNotChosen = 0;
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            Set<Block> destinations = new LinkedHashSet<>();
            Set<Block> neighbours = new LinkedHashSet<>();
            for (int i = 0; i < lb.getNumberOfRoutes(); i++) {
                destinations.add(lb.getRouteDestBlockAtIndex(i));
                if (lb.getRouteNextBlockAtIndex(i) == lb.getBlock()) {
                    neighbours.add(lb.getRouteDestBlockAtIndex(i));
                }
            }
            for (int p = 0; p < lb.getNumberOfNeighbours(); p++) {
                Block previous = lb.getNeighbourAtIndex(p);
                for (Block dest : destinations) {
                    if (neighbours.contains(dest) || dest == previous) {
                        continue; // directly connected destinations are returned at once
                    }
                    List<Integer> candidates = lowestMetricRoutes(lb, previous, dest);
                    if (candidates.size() < 2) {
                        continue;
                    }
                    ties++;
                    String where = "at " + lb.getDisplayName() + " from " + previous.getDisplayName()
                            + " to " + dest.getDisplayName();
                    Block expected = expectedNextBlock(lb, candidates);

                    // the choice must be the same whichever route was learnt first
                    for (int order = 0; order < 2; order++) {
                        List<Integer> inOrder = lowestMetricRoutes(lb, previous, dest);
                        if (lb.getRouteNextBlockAtIndex(inOrder.get(0)) != expected) {
                            firstLearntNotChosen++;
                        }
                        int chosen = lb.getNextBestBlock(previous, dest, new ArrayList<>(), METRIC);
                        assertTrue(chosen >= 0, "a route is chosen " + where);
                        assertSame(expected, lb.getRouteNextBlockAtIndex(chosen),
                                where + (order == 0 ? "" : " with the routes held in reverse order"));
                        reverseRoutes(lb);
                    }
                }
            }
        }
        assertTrue(ties > 0, "panel has routes of equal cost");
        assertTrue(firstLearntNotChosen > 0, "some equal cost routes were learnt before the one chosen");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initMemoryManager();
    }

    @AfterEach
    public void tearDown() {
        for (LayoutEditor panel : InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class)) {
            JUnitUtil.dispose(panel);
        }
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
