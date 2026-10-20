package jmri.jmrit.display.layoutEditor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jmri.Block;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Path;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Check that the path search in LayoutBlockConnectivityTools remembers the
 * branches it has already tried when it backs out of a dead end.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class LayoutBlockConnectivityToolsBacktrackTest {

    private final Map<Block, LayoutBlock> layoutBlocks = new HashMap<>();
    private final Map<Block, List<Block[]>> routes = new HashMap<>();
    private int nextBestCalls = 0;

    /**
     * Create a mocked LayoutBlock whose routing table holds the given routes,
     * each as {destination, next block}. getNextBestBlock returns the first
     * route to the destination whose index is not in the offset list.
     */
    private LayoutBlock addBlock(Block b) {
        List<Block[]> r = routes.computeIfAbsent(b, k -> new ArrayList<>());
        LayoutBlock lb = mock(LayoutBlock.class);
        when(lb.getBlock()).thenReturn(b);
        when(lb.getDisplayName()).thenReturn(b.getUserName());
        when(lb.getRouteNextBlockAtIndex(anyInt())).thenAnswer(i -> r.get((Integer) i.getArgument(0))[1]);
        when(lb.getRouteDestBlockAtIndex(anyInt())).thenAnswer(i -> r.get((Integer) i.getArgument(0))[0]);
        when(lb.getRouteDirectionAtIndex(anyInt())).thenReturn(Path.EAST);
        when(lb.getNeighbourDirection(any(LayoutBlock.class))).thenReturn(Path.EAST);
        when(lb.getThroughPathIndex(any(), any())).thenReturn(0);
        when(lb.getNextBestBlock(any(), any(), any(), any())).thenAnswer(inv -> {
            nextBestCalls++;
            Block dest = inv.getArgument(1);
            List<?> excluded = inv.getArgument(2);
            for (int i = 0; i < r.size(); i++) {
                if (r.get(i)[0] == dest && !excluded.contains(i)) {
                    return i;
                }
            }
            return -1;
        });
        layoutBlocks.put(b, lb);
        return lb;
    }

    private void addRoute(Block from, Block dest, Block next) {
        routes.computeIfAbsent(from, k -> new ArrayList<>()).add(new Block[]{dest, next});
    }

    @Test
    public void testBacktrackPastTwoDeadEnds() throws JmriException {
        Block s = new Block("IB_S", "S");
        Block p = new Block("IB_P", "P");
        Block a = new Block("IB_A", "A");
        Block a1 = new Block("IB_A1", "A1");
        Block a2 = new Block("IB_A2", "A2");
        Block b = new Block("IB_B", "B");
        Block d = new Block("IB_D", "D");

        // P prefers the branch through A, which splits into two dead ends A1 and A2.
        // The only way to D is the second choice at P, through B.
        addRoute(p, d, a);
        addRoute(p, d, b);
        addRoute(a, d, a1);
        addRoute(a, d, a2);
        addRoute(b, d, b); // D is a neighbour of B
        for (Block blk : List.of(s, p, a, a1, a2, b, d)) {
            addBlock(blk);
        }

        LayoutBlockManager lbm = new LayoutBlockManager() {
            @Override
            public LayoutBlock getLayoutBlock(Block blk) {
                return layoutBlocks.get(blk);
            }

            @Override
            public boolean isAdvancedRoutingEnabled() {
                return true;
            }
        };
        InstanceManager.setDefault(LayoutBlockManager.class, lbm);

        LayoutBlockConnectivityTools tools = new LayoutBlockConnectivityTools();
        List<LayoutBlock> result = tools.getLayoutBlocks(layoutBlocks.get(s), layoutBlocks.get(d),
                layoutBlocks.get(p), true, LayoutBlockConnectivityTools.Routing.NONE);

        List<String> names = new ArrayList<>();
        result.forEach(lb -> names.add(lb.getDisplayName()));
        Assertions.assertEquals(List.of("S", "P", "B", "D"), names, "route found after backing out of A");
        Assertions.assertTrue(nextBestCalls < 15, "each branch tried once, calls=" + nextBestCalls);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
