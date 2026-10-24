package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.InstanceManager;
import jmri.jmrit.display.EditorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Check the block routing tables on a panel with a reversing loop, where a
 * train can follow through paths and come back the way it came, after a
 * block metric in the loop is raised and after a connection in the loop is
 * taken away.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutBlockRoutingLoopTest {

    @Test
    public void testReversingLoop() throws Exception {
        LayoutBlockManager lbm = LayoutBlockRoutingChecker.loadPanel("Jlang-Panel-4-19-6.xml");
        LayoutBlockRoutingChecker checker = new LayoutBlockRoutingChecker(lbm);
        checker.assertTablesCorrect("after loading");

        LayoutBlock[] loop = checker.findLoop();
        assertNotNull(loop, "panel has a reversing loop");
        LayoutBlock from = loop[0];
        LayoutBlock inLoop = loop[1];

        // a cost increase inside the loop
        int original = inLoop.getBlockMetric();
        ThreadingUtil.runOnLayout(() -> inLoop.setBlockMetric(original + 1000));
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised after raising a metric");
        checker.assertTablesCorrect("after raising the metric of " + inLoop.getDisplayName());

        // take away the connection into the loop, so routes through it are withdrawn
        ThreadingUtil.runOnLayout(() -> inLoop.notifiedNeighbourNoLongerMutual(from));
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised after removing a connection");
        for (int i = 0; i < inLoop.getNumberOfNeighbours(); i++) {
            assertFalse(inLoop.getNeighbourAtIndex(i) == from.getBlock(), "connection removed");
        }
        // Every route that can still reach its destination has the right
        // cost. Routes that went round the loop and can no longer get out are
        // withdrawn at the hop count limit or left with a high hop count, so
        // they are never preferred. Taking a connection away does not make
        // blocks learn new routes in place of the ones withdrawn, as before.
        int[] count = {0};
        for (String wrong : checker.wrongRoutes(count)) {
            if (wrong.startsWith("unreachable ")) {
                int hops = Integer.parseInt(wrong.substring(wrong.lastIndexOf(' ') + 1));
                assertTrue(hops >= 200, "route that can not get out of the loop has a high hop count: " + wrong);
            } else {
                assertTrue(wrong.startsWith("missing "), "route has the right cost after removing the connection from "
                        + from.getDisplayName() + " to " + inLoop.getDisplayName() + ": " + wrong);
            }
        }
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            for (int i = 0; i < lb.getNumberOfRoutes(); i++) {
                assertTrue(lb.getRouteHopCountAtIndex(i) < 254, "route at the hop count limit withdrawn at "
                        + lb.getDisplayName() + " to " + lb.getRouteDestBlockAtIndex(i).getDisplayName());
            }
        }
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
