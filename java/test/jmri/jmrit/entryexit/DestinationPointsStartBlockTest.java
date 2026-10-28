package jmri.jmrit.entryexit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeListener;
import java.util.List;

import jmri.*;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for an NX route whose start block becomes occupied while the
 * turnouts are being set.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class DestinationPointsStartBlockTest {

    private EntryExitPairs eep;
    private LayoutEditor panel;

    @Test
    public void testStartBlockOccupiedWhileSettingTurnouts() throws JmriException {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);
        SignalMastLogicManager smlm = InstanceManager.getDefault(SignalMastLogicManager.class);
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);

        SignalMast sourceMast = smm.getSignalMast("Left-TO-A");
        SignalMast destMast = smm.getSignalMast("Right-TO-B");
        assertNotNull(sourceMast);
        assertNotNull(destMast);

        // Use a temporary signal mast logic, as created by NX when no stored logic exists
        SignalMastLogic stored = smlm.getSignalMastLogic(sourceMast);
        assertNotNull(stored);
        ThreadingUtil.runOnGUI(() -> smlm.removeSignalMastLogic(stored));
        assertNull(smlm.getSignalMastLogic(sourceMast));

        for (String name : new String[]{"S-Left-Approach", "S-Left-TO", "S-Main", "S-Side", "S-Right-TO", "S-Right-Approach"}) {
            sm.provideSensor(name).setState(Sensor.INACTIVE);
        }

        // The route needs the turnout closed, so it will be moved
        Turnout leftTO = InstanceManager.getDefault(TurnoutManager.class).getTurnout("Left TO");
        assertNotNull(leftTO);
        leftTO.setCommandedState(Turnout.THROWN);

        // A train enters the start block as soon as the turnout starts to move
        Sensor startSensor = sm.provideSensor("S-Left-Approach");
        PropertyChangeListener trainArrives = e -> {
            if (Turnout.PROPERTY_KNOWN_STATE.equals(e.getPropertyName())) {
                ThreadingUtil.runOnLayoutEventually(() -> startSensor.setCommandedState(Sensor.ACTIVE));
            }
        };
        leftTO.addPropertyChangeListener(trainArrives);

        PointDetails srcPd = eep.getPointDetails(sm.getSensor("NX-Left-TO-A"), panel);
        PointDetails destPd = eep.getPointDetails(sm.getSensor("NX-Right-TO-B"), panel);
        assertNotNull(srcPd);
        assertNotNull(destPd);
        DestinationPoints dp = eep.getSourceForPoint(srcPd).getDestForPoint(destPd);
        assertNotNull(dp);

        // Set the route and wait for it to complete
        ThreadingUtil.runOnLayout(() -> eep.setSingleSegmentRoute(dp.getSystemName()));
        JUnitUtil.waitFor(() -> {
            SignalMastLogic s = smlm.getSignalMastLogic(sourceMast);
            return s != null && !s.getAutoBlocks(destMast).isEmpty();
        }, "route set");
        leftTO.removePropertyChangeListener(trainArrives);

        assertEquals(Turnout.CLOSED, leftTO.getKnownState(), "turnout set for the route");
        assertEquals(Block.OCCUPIED, lbm.getLayoutBlock("Left Approach").getBlock().getState(), "start block occupied");

        // The first block after the start block must still be protected by the route
        SignalMastLogic sml = smlm.getSignalMastLogic(sourceMast);
        assertNotNull(sml);
        List<Block> autoBlocks = sml.getAutoBlocks(destMast);
        assertTrue(autoBlocks.contains(lbm.getLayoutBlock("Left TO").getBlock()),
                "first block of the route is in the signal logic: " + autoBlocks);
        assertTrue(autoBlocks.contains(lbm.getLayoutBlock("Main").getBlock()),
                "last block of the route is in the signal logic: " + autoBlocks);
        assertEquals(2, autoBlocks.size(), "route blocks in the signal logic: " + autoBlocks);
        assertTrue(dp.isActive(), "route still active");

        // The train moves on into the first block of the route. Only the destination block is left,
        // so the route is cleared down.
        sm.provideSensor("S-Left-TO").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> !dp.isActive(), "route cleared down by the train");
        assertNull(JUnitAppender.checkForMessageStartingWith("Block Left TO"), "block was part of the route");

        // The signal mast logic may have queued a delayed check of the destination before NX removed the
        // temporary destination. Such a check does nothing when it runs after the removal.
    }

    @BeforeEach
    public void setUp() throws JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();

        ConfigureManager cm = InstanceManager.getDefault(ConfigureManager.class);
        java.io.File f = new java.io.File("java/test/jmri/jmrit/logixng/tools/LogixNG_Test_Dave_Sand.xml");
        assertTrue(cm.load(f));
        panel = InstanceManager.getDefault(EditorManager.class).get(LayoutEditor.class, "My Layout");
        assertNotNull(panel);

        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        lbm.initializeLayoutBlockPaths();
        JUnitUtil.waitFor(lbm::routingStablised, "routing stable");
        eep = InstanceManager.getDefault(EntryExitPairs.class);
    }

    @AfterEach
    public void tearDown() {
        if (panel != null) {
            JUnitUtil.dispose(panel);
        }
        panel = null;
        eep = null;
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");
        JUnitUtil.removeMatchingThreads("Entry Exit Route: Release Mast");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
