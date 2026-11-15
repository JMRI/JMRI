package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import jmri.InstanceManager;
import jmri.jmrit.display.EditorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Check that a block's routing table is brought back into line when it has
 * lost a route it should hold, as happened on a large panel when a route was
 * withdrawn while it only went round a loop and was not offered again once a
 * way out was learnt (JMRI/JMRI#15883's panel: 34 routes missing after
 * loading).
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutBlockRoutingRepairTest {

    // the block's own list of routes, to lose one from as the passing of
    // routes from block to block could
    @SuppressWarnings("unchecked")
    private static List<Object> routesOf(LayoutBlock lb) throws ReflectiveOperationException {
        Field f = LayoutBlock.class.getDeclaredField("routes");
        f.setAccessible(true);
        return (List<Object>) f.get(lb);
    }

    @Test
    public void testLostRouteLearntAgain() throws Exception {
        LayoutBlockManager lbm = LayoutBlockRoutingChecker.loadPanel("Jlang-Panel-4-19-6.xml");
        LayoutBlockRoutingChecker checker = new LayoutBlockRoutingChecker(lbm);
        checker.assertTablesCorrect("after loading");

        // a block with a route via a neighbour that is not the destination
        LayoutBlock lb = null;
        int index = -1;
        for (LayoutBlock b : lbm.getNamedBeanSet()) {
            for (int i = 0; i < b.getNumberOfRoutes(); i++) {
                if (b.getRouteNextBlockAtIndex(i) != b.getBlock() && b.getRouteHopCountAtIndex(i) > 2) {
                    lb = b;
                    index = i;
                    break;
                }
            }
            if (lb != null) {
                break;
            }
        }
        assertNotNull(lb, "panel has a block with a route of more than two hops");
        LayoutBlock damaged = lb;
        int lost = index;
        String what = damaged.getDisplayName() + " to " + damaged.getRouteDestBlockAtIndex(lost).getDisplayName()
                + " via " + damaged.getRouteNextBlockAtIndex(lost).getDisplayName();

        // the route goes missing, and some other routing change follows
        ThreadingUtil.runOnLayout(() -> {
            try {
                routesOf(damaged).remove(lost);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            lbm.setLastRoutingChange();
        });
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised after losing " + what);
        ThreadingUtil.runOnLayout(() -> {});

        checker.assertTablesCorrect("after losing the route from " + what);
        assertTrue(damaged.getNumberOfRoutes() > 0, "routes held");
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
