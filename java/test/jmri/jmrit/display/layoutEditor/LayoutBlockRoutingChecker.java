package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jmri.Block;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

/**
 * Check the routes held by every LayoutBlock against a separate shortest path
 * calculation over the same blocks, neighbours and through paths. Used by the
 * block routing tests.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
class LayoutBlockRoutingChecker {

    private static final int UNREACHABLE = Integer.MAX_VALUE;

    private final LayoutBlockManager lbm;

    LayoutBlockRoutingChecker(LayoutBlockManager lbm) {
        this.lbm = lbm;
    }

    /**
     * The best metric and hop count of a route at "from" to "dest" going via
     * the neighbour "next", worked out from the block metrics, neighbours and
     * through paths only.
     */
    private static class Expected {

        // key "from|next" for each neighbour a block can receive routes from
        final Map<String, Integer> metric = new HashMap<>();
        final Map<String, Integer> hops = new HashMap<>();
    }

    private static String key(Block from, Block next) {
        return from.getSystemName() + "|" + next.getSystemName();
    }

    private static boolean receives(LayoutBlock lb, int i) {
        // the neighbour sends us routes unless we only transmit to it
        return !lb.getNeighbourPacketFlowAsString(i).contains("(TX)")
                && !lb.getNeighbourPacketFlowAsString(i).startsWith("None");
    }

    private Expected expectedRoutes(Block dest) {
        Expected e = new Expected();
        LayoutBlock destLb = lbm.getLayoutBlock(dest);
        // a route straight to the destination costs its metric and one hop
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            for (int i = 0; i < lb.getNumberOfNeighbours(); i++) {
                if (lb.getNeighbourAtIndex(i) == dest && receives(lb, i) && destLb != null) {
                    e.metric.put(key(lb.getBlock(), dest), destLb.getBlockMetric());
                    e.hops.put(key(lb.getBlock(), dest), 1);
                }
            }
        }
        // then relax: from -> next -> onward, allowed when next has a through
        // path from "from" to "onward" and is a mutual neighbour of "from"
        boolean changed = true;
        while (changed) {
            changed = false;
            for (LayoutBlock nextLb : lbm.getNamedBeanSet()) {
                Block next = nextLb.getBlock();
                if (next == dest) {
                    continue;
                }
                for (int t = 0; t < nextLb.getNumberOfThroughPaths(); t++) {
                    Block from = nextLb.getThroughPathSource(t);
                    Block onward = nextLb.getThroughPathDestination(t);
                    LayoutBlock fromLb = lbm.getLayoutBlock(from);
                    if (fromLb == null || from == dest) {
                        continue;
                    }
                    Integer onMetric = e.metric.get(key(next, onward));
                    if (onMetric == null || !isMutualReceiver(fromLb, next)) {
                        continue;
                    }
                    int m = nextLb.getBlockMetric() + onMetric;
                    int h = 1 + e.hops.get(key(next, onward));
                    String k = key(from, next);
                    if (m < e.metric.getOrDefault(k, UNREACHABLE)) {
                        e.metric.put(k, m);
                        changed = true;
                    }
                    if (h < e.hops.getOrDefault(k, UNREACHABLE)) {
                        e.hops.put(k, h);
                        changed = true;
                    }
                }
            }
        }
        return e;
    }

    private static boolean isMutualReceiver(LayoutBlock lb, Block neighbour) {
        for (int i = 0; i < lb.getNumberOfNeighbours(); i++) {
            if (lb.getNeighbourAtIndex(i) == neighbour) {
                return lb.isNeighbourMutual(i) && receives(lb, i);
            }
        }
        return false;
    }

    /**
     * Compare every route in every table with the expected values, and look
     * for routes that should be held but are not.
     *
     * @return a description of the routes that differ, empty if none; a
     *         route that can not reach its destination starts "unreachable"
     *         and one that should be held but is not starts "missing"
     */
    List<String> wrongRoutes(int[] count) {
        Map<Block, Expected> byDest = new HashMap<>();
        List<String> wrong = new ArrayList<>();
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            for (int i = 0; i < lb.getNumberOfRoutes(); i++) {
                Block dest = lb.getRouteDestBlockAtIndex(i);
                Block next = lb.getRouteNextBlockAtIndex(i);
                if (next == lb.getBlock()) {
                    next = dest; // a route straight to a neighbour
                }
                Expected e = byDest.computeIfAbsent(dest, this::expectedRoutes);
                count[0]++;
                Integer metric = e.metric.get(key(lb.getBlock(), next));
                Integer hops = e.hops.get(key(lb.getBlock(), next));
                if (metric == null) {
                    wrong.add("unreachable " + lb.getDisplayName() + " to " + dest.getDisplayName()
                            + " via " + next.getDisplayName() + ": hops " + lb.getRouteHopCountAtIndex(i));
                } else if (metric != lb.getRouteMetric(i) || hops != lb.getRouteHopCountAtIndex(i)) {
                    wrong.add(lb.getDisplayName() + " to " + dest.getDisplayName() + " via " + next.getDisplayName()
                            + ": metric " + lb.getRouteMetric(i) + " hops " + lb.getRouteHopCountAtIndex(i)
                            + ", expected " + metric + " hops " + hops);
                }
            }
        }
        // and every route the calculation finds is held
        Map<String, Boolean> held = new HashMap<>();
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            for (int i = 0; i < lb.getNumberOfRoutes(); i++) {
                Block next = lb.getRouteNextBlockAtIndex(i);
                Block dest = lb.getRouteDestBlockAtIndex(i);
                held.put(key(lb.getBlock(), next == lb.getBlock() ? dest : next) + "|" + dest.getSystemName(), true);
            }
        }
        for (Map.Entry<Block, Expected> entry : byDest.entrySet()) {
            for (String k : entry.getValue().metric.keySet()) {
                if (!held.containsKey(k + "|" + entry.getKey().getSystemName())) {
                    wrong.add("missing route " + k + " to " + entry.getKey().getDisplayName());
                }
            }
        }
        return wrong;
    }

    void assertTablesCorrect(String when) {
        int[] count = {0};
        List<String> wrong = wrongRoutes(count);
        assertTrue(count[0] > 0, "routes were built " + when);
        assertTrue(wrong.isEmpty(), wrong.size() + " of " + count[0] + " routes wrong " + when + ", first: "
                + wrong.subList(0, Math.min(5, wrong.size())));
    }

    /**
     * Load a sample panel with block routing on and wait for the routing to
     * settle, using a short settling time.
     *
     * @param panel file name in the layoutEditor load directory
     * @return the LayoutBlockManager
     * @throws Exception if the panel can not be loaded
     */
    static LayoutBlockManager loadPanel(String panel) throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        File f = new File("java/test/jmri/jmrit/display/layoutEditor/load/" + panel);
        assertTrue(cm.load(f), "panel loaded");
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        lbm.setStabilisedDelay(200);
        lbm.enableAdvancedRouting(true);
        ThreadingUtil.runOnGUI(() -> lbm.initializeLayoutBlockPaths());
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised");
        // the stable event is posted to the layout thread; let its listeners
        // finish before the routing tables are examined
        ThreadingUtil.runOnLayout(() -> {});
        return lbm;
    }

    /**
     * Find a block and a neighbour it passes traffic to, where a train can
     * follow through paths from there and come back the same way, as round a
     * reversing loop or a wye.
     *
     * @return the block and its neighbour, or null if there is no such loop
     */
    LayoutBlock[] findLoop() {
        // states are "from|to": a train in "to" having come from "from"
        Map<String, List<String>> next = new HashMap<>();
        Map<String, LayoutBlock[]> states = new HashMap<>();
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            for (int t = 0; t < lb.getNumberOfThroughPaths(); t++) {
                LayoutBlock from = lbm.getLayoutBlock(lb.getThroughPathSource(t));
                LayoutBlock to = lbm.getLayoutBlock(lb.getThroughPathDestination(t));
                if (from == null || to == null) {
                    continue;
                }
                String in = key(from.getBlock(), lb.getBlock());
                String out = key(lb.getBlock(), to.getBlock());
                states.put(in, new LayoutBlock[]{from, lb});
                states.put(out, new LayoutBlock[]{lb, to});
                next.computeIfAbsent(in, k -> new ArrayList<>()).add(out);
            }
        }
        List<String> keys = new ArrayList<>(states.keySet());
        keys.sort(null);
        for (String start : keys) {
            // can a train starting in this state come back to it?
            Set<String> seen = new HashSet<>();
            List<String> todo = new ArrayList<>(next.getOrDefault(start, List.of()));
            while (!todo.isEmpty()) {
                String s = todo.remove(todo.size() - 1);
                if (s.equals(start)) {
                    return states.get(start);
                }
                if (seen.add(s)) {
                    todo.addAll(next.getOrDefault(s, List.of()));
                }
            }
        }
        return null;
    }

    /**
     * @return the block that the most routes go through, by system name if
     *         equal
     */
    LayoutBlock busiestBlock() {
        Map<LayoutBlock, Integer> uses = new HashMap<>();
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            for (int i = 0; i < lb.getNumberOfRoutes(); i++) {
                LayoutBlock nextLb = lbm.getLayoutBlock(lb.getRouteNextBlockAtIndex(i));
                if (nextLb != null && nextLb != lb) {
                    uses.merge(nextLb, 1, Integer::sum);
                }
            }
        }
        LayoutBlock busiest = null;
        for (Map.Entry<LayoutBlock, Integer> entry : uses.entrySet()) {
            if (busiest == null || entry.getValue() > uses.get(busiest)
                    || (entry.getValue().equals(uses.get(busiest))
                        && entry.getKey().getSystemName().compareTo(busiest.getSystemName()) < 0)) {
                busiest = entry.getKey();
            }
        }
        assertNotNull(busiest, "no block is used by routes");
        return busiest;
    }

}
