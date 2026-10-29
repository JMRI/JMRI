package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
 * routes held in their learnt order and in reverse order. The choices are
 * made on the layout thread, where the panel's signal mast logic uses the
 * same routes.
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

    /**
     * One choice made by getNextBestBlock among routes of equal cost.
     */
    private static final class TieChoice {

        final String where;
        final boolean reversed;
        final Block expected;
        final Block chosen;
        final boolean firstHeldIsExpected;

        TieChoice(String where, boolean reversed, Block expected, Block chosen, boolean firstHeldIsExpected) {
            this.where = where;
            this.reversed = reversed;
            this.expected = expected;
            this.chosen = chosen;
            this.firstHeldIsExpected = firstHeldIsExpected;
        }
    }

    /**
     * Find every choice between routes of equal cost and make it with the
     * routes held in their learnt order and in reverse order. The routes are
     * read and reordered here, so this must run on the layout thread: the
     * signal mast logic of the loaded panel searches the same routes there
     * once routing has stabilised, and getNextBestBlock returns an index into
     * them.
     *
     * @param lbm        the layout block manager
     * @param routeLists the routes list of each layout block
     * @return the choices made
     */
    private static List<TieChoice> makeTieChoices(LayoutBlockManager lbm, Map<LayoutBlock, List<?>> routeLists) {
        List<TieChoice> choices = new ArrayList<>();
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
                    String where = "at " + lb.getDisplayName() + " from " + previous.getDisplayName()
                            + " to " + dest.getDisplayName();
                    Block expected = expectedNextBlock(lb, candidates);

                    // the choice must be the same whichever route was learnt first
                    for (int order = 0; order < 2; order++) {
                        List<Integer> inOrder = lowestMetricRoutes(lb, previous, dest);
                        boolean firstHeldIsExpected = lb.getRouteNextBlockAtIndex(inOrder.get(0)) == expected;
                        int chosen = lb.getNextBestBlock(previous, dest, new ArrayList<>(), METRIC);
                        choices.add(new TieChoice(where, order == 1, expected,
                                chosen < 0 ? null : lb.getRouteNextBlockAtIndex(chosen), firstHeldIsExpected));
                        Collections.reverse(routeLists.get(lb));
                    }
                }
            }
        }
        return choices;
    }

    @Test
    public void testEqualCostRoutesChosenByRule() throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        File f = new File("java/test/jmri/jmrit/display/layoutEditor/load/Jlang-Panel-4-19-6.xml");
        assertTrue(cm.load(f), "panel loaded");
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        ThreadingUtil.runOnGUI(() -> lbm.initializeLayoutBlockPaths());
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised");

        Field routesField = LayoutBlock.class.getDeclaredField("routes");
        routesField.setAccessible(true);
        Map<LayoutBlock, List<?>> routeLists = new HashMap<>();
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            routeLists.put(lb, (List<?>) routesField.get(lb));
        }

        List<TieChoice> choices = ThreadingUtil.runOnGUIwithReturn(() -> makeTieChoices(lbm, routeLists));

        assertNotNull(choices, "choices made on the layout thread");
        assertFalse(choices.isEmpty(), "panel has routes of equal cost");
        int firstHeldNotChosen = 0;
        for (TieChoice c : choices) {
            assertNotNull(c.chosen, "a route is chosen " + c.where);
            assertSame(c.expected, c.chosen, c.where + (c.reversed ? " with the routes held in reverse order" : ""));
            if (!c.firstHeldIsExpected) {
                firstHeldNotChosen++;
            }
        }
        assertTrue(firstHeldNotChosen > 0, "some equal cost routes were learnt before the one chosen");
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
