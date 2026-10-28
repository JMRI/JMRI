package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check that a route search stopped by the user reports the routes found so
 * far, and that a search cancelled by a new search reports nothing.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class RouteFinderStopTest {

    private WarrantRoute caller;
    private int pickReports;
    private int noRouteReports;
    private int routesFound;
    private BlockOrder orig;
    private BlockOrder dest;

    /**
     * A search that runs the given action after each level.
     */
    private class StoppingFinder extends RouteFinder {

        private final int stopAfterLevel;
        private final Runnable action;

        StoppingFinder(int stopAfterLevel, Runnable action) {
            super(caller, orig, dest, null, null, 10);
            this.stopAfterLevel = stopAfterLevel;
            this.action = action;
        }

        @Override
        ArrayList<RouteNode> makeLevel(ArrayList<RouteNode> nodes, int level) {
            ArrayList<RouteNode> children = super.makeLevel(nodes, level);
            if (level == stopAfterLevel) {
                action.run();
            }
            return children;
        }
    }

    @Test
    public void testUnstoppedSearchReports() {
        RouteFinder finder = new RouteFinder(caller, orig, dest, null, null, 10);
        finder.run();
        assertEquals(1, pickReports, "search reports the routes found");
        assertEquals(1, routesFound, "one route found");
    }

    @Test
    public void testStopBeforeStartIsHonoured() {
        RouteFinder finder = new RouteFinder(caller, orig, dest, null, null, 10);
        finder.quit();  // Stop pressed before the search thread got going
        finder.run();
        assertEquals(0, pickReports, "search did not run after Stop");
        assertEquals(1, noRouteReports, "stopped search reports that no route was found yet");
    }

    @Test
    public void testStoppedSearchReportsRoutesFoundSoFar() {
        RouteFinder[] holder = new RouteFinder[1];
        // the destination is found at level 1; Stop is pressed then
        holder[0] = new StoppingFinder(1, () -> holder[0].quit());
        holder[0].run();
        assertEquals(1, pickReports, "stopped search shows the routes found so far");
        assertEquals(1, routesFound, "the route found before Stop is shown");
        assertEquals(0, noRouteReports, "no no-route report");
    }

    @Test
    public void testCancelledBeforeStartDoesNotReport() {
        RouteFinder finder = new RouteFinder(caller, orig, dest, null, null, 10);
        finder.cancel();    // replaced by a new search before it got going
        finder.run();
        assertEquals(0, pickReports + noRouteReports, "a cancelled search does not report");
    }

    @Test
    public void testCancelledSearchDoesNotReport() {
        RouteFinder[] holder = new RouteFinder[1];
        holder[0] = new StoppingFinder(1, () -> holder[0].cancel());
        holder[0].run();
        assertEquals(0, pickReports + noRouteReports,
                "a search replaced by a new one does not report, even with routes found");
    }

    /**
     * Make blocks OB1 - P12 - OB2 - P23 - OB3, with one path through each.
     */
    private void makeLayout() {
        OBlockManager blkMgr = InstanceManager.getDefault(OBlockManager.class);
        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        OBlock b1 = blkMgr.createNewOBlock("OB1", "Origin");
        OBlock b2 = blkMgr.createNewOBlock("OB2", "Middle");
        OBlock b3 = blkMgr.createNewOBlock("OB3", "Destination");
        Portal p12 = portalMgr.providePortal("P12");
        p12.setFromBlock(b1, false);
        p12.setToBlock(b2, false);
        Portal p23 = portalMgr.providePortal("P23");
        p23.setFromBlock(b2, false);
        p23.setToBlock(b3, false);
        addPath(new OPath("p1", b1, null, p12, null));
        addPath(new OPath("p2", b2, p12, p23, null));
        addPath(new OPath("p3", b3, p23, null, null));
        orig = new BlockOrder(b1, "p1", null, "P12");
        dest = new BlockOrder(b3, "p3", "P23", null);
    }

    private static void addPath(OPath path) {
        if (path.getFromPortal() != null) {
            path.getFromPortal().addPath(path);
        }
        if (path.getToPortal() != null) {
            path.getToPortal().addPath(path);
        }
        assertTrue(((OBlock) path.getBlock()).addPath(path), "path added");
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initRosterConfigManager();
        pickReports = 0;
        noRouteReports = 0;
        routesFound = 0;
        makeLayout();
        caller = new WarrantRoute() {
            @Override
            protected void selectedRoute(ArrayList<BlockOrder> orders) {
            }

            @Override
            protected void maxThrottleEventAction() {
            }

            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
            }

            @Override
            protected void pickRoute(List<DefaultMutableTreeNode> destNodes, DefaultTreeModel routeTree) {
                pickReports++;
                routesFound = destNodes.size();
            }

            @Override
            protected void debugRoute(DefaultTreeModel tree, BlockOrder origin, BlockOrder dest) {
                noRouteReports++;
            }
        };
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.dispose(caller);
        caller = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
