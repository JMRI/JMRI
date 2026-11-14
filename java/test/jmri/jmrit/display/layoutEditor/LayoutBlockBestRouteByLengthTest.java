package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import jmri.Block;
import jmri.InstanceManager;
import jmri.jmrit.display.EditorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Check that LayoutBlock finds the shortest route to a destination.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutBlockBestRouteByLengthTest {

    @Test
    public void testBestRouteByLength() throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        File f = new File("java/test/jmri/jmrit/display/layoutEditor/load/DS-Sample-Layout-4-19-6.xml");
        assertTrue(cm.load(f), "panel loaded");
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        ThreadingUtil.runOnGUI(() -> lbm.initializeLayoutBlockPaths());
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised");

        int checked = 0;
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            Set<Block> destinations = new LinkedHashSet<>();
            for (int i = 0; i < lb.getNumberOfRoutes(); i++) {
                destinations.add(lb.getRouteDestBlockAtIndex(i));
            }
            for (Block dest : destinations) {
                float shortest = Float.MAX_VALUE;
                for (int i = 0; i < lb.getNumberOfRoutes(); i++) {
                    if (lb.getRouteDestBlockAtIndex(i) == dest) {
                        shortest = Math.min(shortest, lb.getRouteLengthAtIndex(i));
                    }
                }
                int best = lb.getRouteIndex(lb.getBestRouteByLength(dest));
                assertTrue(best >= 0, lb.getDisplayName() + " has a shortest route to " + dest.getDisplayName());
                assertEquals(shortest, lb.getRouteLengthAtIndex(best), 0.001,
                        lb.getDisplayName() + " shortest route to " + dest.getDisplayName());
                checked++;
            }
        }
        assertTrue(checked > 50, "destinations checked " + checked);

        // a block with no routes to it
        LayoutBlock any = lbm.getNamedBeanSet().first();
        assertNull(any.getBestRouteByLength(new Block("IB_NO_ROUTES")), "no route");
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
