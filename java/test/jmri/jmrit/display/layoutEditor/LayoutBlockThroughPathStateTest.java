package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;

import jmri.*;
import jmri.jmrit.display.EditorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Check the through path state and the listeners kept by LayoutBlock routing
 * on a loaded panel.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutBlockThroughPathStateTest {

    private LayoutBlockManager lbm;

    private void loadPanel(boolean routingBeforeTurnouts) throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        File f = new File("java/test/jmri/jmrit/display/layoutEditor/load/DS-Sample-Layout-4-19-6.xml");
        assertTrue(cm.load(f), "panel loaded");
        lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        if (!routingBeforeTurnouts) {
            lbm.enableAdvancedRouting(false);
        }
        ThreadingUtil.runOnGUI(() -> lbm.initializeLayoutBlockPaths());
        if (!routingBeforeTurnouts) {
            // turnout feedback known before the routing builds its through paths
            setAllTurnouts(Turnout.CLOSED);
            ThreadingUtil.runOnGUI(() -> lbm.enableAdvancedRouting(true));
        }
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised");
    }

    private void setAllTurnouts(int state) {
        for (Turnout t : InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet()) {
            ThreadingUtil.runOnLayout(() -> t.setCommandedState(state));
        }
    }

    private Map<String, Boolean> throughPathStates() {
        Map<String, Boolean> states = new TreeMap<>();
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            for (int i = 0; i < lb.getNumberOfThroughPaths(); i++) {
                states.put(lb.getDisplayName() + ": " + lb.getThroughPathSource(i).getDisplayName()
                        + " > " + lb.getThroughPathDestination(i).getDisplayName(), lb.isThroughPathActive(i));
            }
        }
        return states;
    }

    @Test
    public void testThroughPathsActiveWhenTurnoutsSetBeforeRouting() throws Exception {
        loadPanel(false);
        Map<String, Boolean> atStart = throughPathStates();
        assertTrue(atStart.size() > 10, "through paths built");

        // drive every turnout through a change so each path is worked out from turnout events
        setAllTurnouts(Turnout.THROWN);
        setAllTurnouts(Turnout.CLOSED);
        Map<String, Boolean> afterEvents = throughPathStates();

        assertEquals(afterEvents, atStart, "through path state at start matches the turnouts");
    }

    private void checkRouteValidWhileAnyPathActive() {
        // a route is valid when a through path using its next block is active
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            Set<Block> activeEnds = new HashSet<>();
            for (int i = 0; i < lb.getNumberOfThroughPaths(); i++) {
                if (lb.isThroughPathActive(i)) {
                    activeEnds.add(lb.getThroughPathSource(i));
                    activeEnds.add(lb.getThroughPathDestination(i));
                }
            }
            for (int i = 0; i < lb.getNumberOfRoutes(); i++) {
                Block next = lb.getRouteNextBlockAtIndex(i);
                if (next == lb.getBlock() || lb.getNumberOfThroughPaths() == 0) {
                    continue;
                }
                assertEquals(activeEnds.contains(next), lb.getRouteValid(i),
                        lb.getDisplayName() + " route to " + lb.getRouteDestBlockAtIndex(i).getDisplayName()
                        + " via " + next.getDisplayName());
            }
        }
    }

    private static int countListeners(PropertyChangeListener[] listeners, String className) {
        int n = 0;
        for (PropertyChangeListener l : listeners) {
            if (l.getClass().getName().equals(className)) {
                n++;
            }
        }
        return n;
    }

    private int routeListenersOnBlocks() {
        int n = 0;
        for (Block b : InstanceManager.getDefault(BlockManager.class).getNamedBeanSet()) {
            n += countListeners(b.getPropertyChangeListeners(), LayoutBlock.class.getName() + "$Routes");
        }
        return n;
    }

    private int routesInTables() {
        int n = 0;
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            n += lb.getNumberOfRoutes();
        }
        return n;
    }

    private int throughPathListenersOnTurnouts() {
        int n = 0;
        for (Turnout t : InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet()) {
            n += countListeners(t.getPropertyChangeListeners(), LayoutBlock.class.getName() + "$ThroughPaths");
        }
        return n;
    }

    private LayoutBlock findBlockWithNeighbours(int count) {
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            if (lb.getNumberOfNeighbours() >= count) {
                return lb;
            }
        }
        return null;
    }

    @Test
    public void testRouteValidityAndListeners() throws Exception {
        loadPanel(true);
        setAllTurnouts(Turnout.CLOSED);
        checkRouteValidWhileAnyPathActive();
        assertEquals(routesInTables(), routeListenersOnBlocks(), "one block listener per route after load");

        // cut a block with three or more neighbours away from one neighbour
        LayoutBlock target = findBlockWithNeighbours(3);
        assertNotNull(target, "block with three neighbours");
        LayoutBlock neighbour = lbm.getLayoutBlock(target.getNeighbourAtIndex(0));
        assertNotNull(neighbour, "neighbour");
        int pathsBefore = target.getNumberOfThroughPaths();
        int turnoutListenersBefore = throughPathListenersOnTurnouts();

        // the neighbour drops the adjacency and tells the target block to do the same
        ThreadingUtil.runOnLayout(() -> neighbour.notifiedNeighbourNoLongerMutual(target));

        assertTrue(target.getNumberOfThroughPaths() < pathsBefore, "through paths removed");
        checkRouteValidWhileAnyPathActive();
        assertEquals(routesInTables(), routeListenersOnBlocks(), "one block listener per route after removal");
        assertTrue(throughPathListenersOnTurnouts() < turnoutListenersBefore,
                "removed through paths no longer listen to their turnouts");
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
        lbm = null;
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
