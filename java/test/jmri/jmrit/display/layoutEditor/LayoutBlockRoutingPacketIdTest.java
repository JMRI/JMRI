package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;

import jmri.Block;
import jmri.InstanceManager;
import jmri.jmrit.display.EditorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Check that a routing update is not ignored because its packet ID is the
 * same as one used for an earlier update, as happens once the packet IDs have
 * gone round after many occupancy changes.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutBlockRoutingPacketIdTest {

    private Object lastPacketId = null;

    private static int metricOfRouteTo(LayoutBlock owner, Block neighbour) {
        for (int i = 0; i < owner.getNumberOfRoutes(); i++) {
            if (owner.getRouteDestBlockAtIndex(i) == neighbour && owner.getRouteNextBlockAtIndex(i) == owner.getBlock()) {
                return owner.getRouteMetric(i);
            }
        }
        return -1;
    }

    @Test
    public void testMetricUpdateWithReusedPacketId() throws Exception {
        LayoutBlockManager lbm = LayoutBlockRoutingChecker.loadPanel("LayoutEditorTest-4-19-6.xml");

        // a block and a neighbour it holds a route to, and another block
        LayoutBlock owner = null;
        LayoutBlock neighbour = null;
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            for (int i = 0; i < lb.getNumberOfNeighbours() && neighbour == null; i++) {
                LayoutBlock n = lbm.getLayoutBlock(lb.getNeighbourAtIndex(i));
                if (n != null && lb.isNeighbourMutual(i) && metricOfRouteTo(lb, n.getBlock()) >= 0) {
                    owner = lb;
                    neighbour = n;
                }
            }
        }
        if (owner == null || neighbour == null) {
            throw new AssertionError("no pair of neighbouring blocks");
        }
        LayoutBlock other = null;
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            if (lb != owner && lb != neighbour) {
                other = lb;
            }
        }
        if (other == null) {
            throw new AssertionError("no third block");
        }

        // the neighbour sends a routing update for each occupancy change
        Field packetRef = Class.forName(LayoutBlock.class.getName() + "$RoutingPacket").getDeclaredField("packetRef");
        packetRef.setAccessible(true);
        neighbour.addPropertyChangeListener(e -> {
            if (LayoutBlock.PROPERTY_ROUTING.equals(e.getPropertyName())) {
                try {
                    lastPacketId = packetRef.get(e.getNewValue());
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        });
        Block block = neighbour.getBlock();
        ThreadingUtil.runOnLayout(() -> {
            for (int i = 0; i < 300; i++) {
                block.setState(Block.OCCUPIED);
                block.setState(Block.UNOCCUPIED);
            }
        });
        assertNotNull(lastPacketId, "routing updates sent");
        int reused = (Integer) lastPacketId;

        // use up packet IDs until the next one would be the last one received
        int previous = (reused == 0) ? 2000 : reused - 1;
        for (int i = 0; i < 5000; i++) {
            if (other.getNextPacketID() == previous) {
                break;
            }
        }

        LayoutBlock changed = neighbour;
        int newMetric = changed.getBlockMetric() + 13;
        ThreadingUtil.runOnLayout(() -> changed.setBlockMetric(newMetric));
        assertEquals(newMetric, metricOfRouteTo(owner, changed.getBlock()),
                "route from " + owner.getDisplayName() + " to " + changed.getDisplayName() + " has the new metric");
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
