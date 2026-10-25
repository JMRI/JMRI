package jmri.jmrit.entryexit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import jmri.*;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for the block values of an NX route when a train skips a block.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class DestinationPointsBlockValueTest {

    private HashMap<String, LayoutEditor> panels = new HashMap<>();
    private EntryExitPairs eep;
    private LayoutBlockManager lbm;
    private DestinationPoints dp;
    private Block east;
    private Block main;

    private void setRoute() {
        ThreadingUtil.runOnLayout(() -> eep.setSingleSegmentRoute(dp.getSystemName()));
        JUnitUtil.waitFor(() -> lbm.getLayoutBlock("B-Alpha-Main").getUseExtraColor(), "route set");
        JUnitUtil.waitFor(() -> !routeSettingInProgress(), "route setting finished");
        assertTrue(dp.isActive(), "route active");
    }

    private boolean routeSettingInProgress() {
        return eep.isSettingRoutes();
    }

    private static void occupy(Block b) throws JmriException {
        Sensor s = b.getSensor();
        assertNotNull(s, b.getDisplayName());
        s.setState(Sensor.ACTIVE);
    }

    @Test
    public void testSkippedBlockKeepsItsOwnValue() throws JmriException {
        setRoute();
        east.setValue("Train 1");
        occupy(east);

        // The train is reported in the last block without being seen in the block before it,
        // and that block already knows which train it is
        main.setValue("Train 1 by RFID");
        occupy(main);

        assertEquals("Train 1 by RFID", main.getValue(), "value of the block is kept");
        JUnitUtil.waitFor(() -> !dp.isActive(), "route cleared down");
    }

    @Test
    public void testSkippedBlockValueNotSetToNull() throws JmriException {
        setRoute();
        occupy(east);   // no value known for the train in the start block

        main.setValue("Train 2");
        occupy(main);

        assertEquals("Train 2", main.getValue(), "value of the block is not cleared");
        JUnitUtil.waitFor(() -> !dp.isActive(), "route cleared down");
    }

    @Test
    public void testSkippedBlockWithoutValueGetsTrainValue() throws JmriException {
        setRoute();
        east.setValue("Train 3");
        occupy(east);

        assertNull(main.getValue());
        occupy(main);

        assertEquals("Train 3", main.getValue(), "block gets the value of the train");
        JUnitUtil.waitFor(() -> !dp.isActive(), "route cleared down");
    }

    @Test
    public void testBlockNotInRouteKeepsItsValue() throws JmriException {
        setRoute();
        east.setValue("Train 4");
        occupy(east);

        LayoutBlock other = lbm.getLayoutBlock("B-Alpha-Side");
        assertNotNull(other);
        other.getBlock().setValue("Train 5");
        dp.removeBlockFromRoute(other);
        JUnitAppender.assertErrorMessage("Block B-Alpha-Side went active but it is not part of our NX path");
        assertEquals("Train 5", other.getBlock().getValue(), "value of a block outside the route is kept");
        assertTrue(dp.isActive(), "route still active");

        // The route still knows the train in the start block
        occupy(main);
        assertEquals("Train 4", main.getValue(), "block gets the value of the train");
        JUnitUtil.waitFor(() -> !dp.isActive(), "route cleared down");
    }

    /**
     * Wait for the routes of the test panel that run when it is loaded: they close the turnouts
     * and set the NX and block detection sensors inactive.
     */
    static void waitForPanelReset() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        JUnitUtil.waitFor(() -> {
            for (Sensor s : sm.getNamedBeanSet()) {
                String name = s.getUserName();
                if (name != null && (name.startsWith("NX-") || name.startsWith("BD-") || name.equals("Reset"))
                        && s.getKnownState() != Sensor.INACTIVE) {
                    return false;
                }
            }
            for (Turnout t : tm.getNamedBeanSet()) {
                if (t.getKnownState() != Turnout.CLOSED) {
                    return false;
                }
            }
            return true;
        }, "test panel reset");
    }

    @BeforeEach
    public void setUp() throws JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        panels = EntryExitTestTools.getPanels();
        assertTrue(panels.containsKey("Alpha"));
        eep = InstanceManager.getDefault(EntryExitPairs.class);
        lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        JUnitUtil.waitFor(lbm::routingStablised, "routing stable");

        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        dp = new EntryExitTestTools().getDestinationPoint(sm.getSensor("NX-AE"),
                sm.getSensor("NX-AW-Main"), panels.get("Alpha"), eep);
        assertNotNull(dp);

        east = lbm.getLayoutBlock("B-Alpha-East").getBlock();
        main = lbm.getLayoutBlock("B-Alpha-Main").getBlock();
        waitForPanelReset();
    }

    @AfterEach
    public void tearDown() {
        if (panels != null) {
            panels.forEach((name, panel) -> JUnitUtil.dispose(panel));
        }
        panels = null;
        eep = null;
        lbm = null;
        dp = null;
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
