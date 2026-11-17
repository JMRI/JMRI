package jmri.jmrit.simulation.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.display.layoutEditor.TrackMarkerDropHandler;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.simulation.SimulationTestLayout;
import jmri.jmrit.simulation.SimulationTestLayouts;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrit.simulation.VirtualTrain;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * The simulator's handler for loco markers dropped on a Block's track: when
 * it is active, what its dialog does, and its item in the Block's menu. On
 * the standard terminus layout.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class SimulatedTrainMarkerHandlerTest {

    private static final String TITLE = "Place Simulated Train";

    private SimulationTestLayout layout;
    private SimulatedTrainMarkerHandler handler;
    private final AtomicInteger told = new AtomicInteger();

    private static Sensor sensor(Block b) {
        return java.util.Objects.requireNonNull(b.getSensor(), "the Block's sensor");
    }

    private LocoIcon marker(String text) {
        LocoIcon m = new LocoIcon(layout.getEditor());
        m.setText(text);
        return m;
    }

    /** Press buttons of the modal dialogs with the handler's title, in order, on another thread. */
    private Thread press(String... buttons) {
        Thread t = new Thread(() -> {
            for (String button : buttons) {
                JDialogOperator dialog = new JDialogOperator(TITLE);
                new JButtonOperator(dialog, button).doClick();
                dialog.waitClosed();
            }
        }, "press dialog buttons");
        t.start();
        return t;
    }

    private void drop(LocoIcon marker, Block block, Thread pressing) {
        ThreadingUtil.runOnGUI(() -> handler.markerDropped(marker, block, layout.getEditor(), told::incrementAndGet));
        JUnitUtil.waitFor(() -> !pressing.isAlive(), "dialogs answered");
    }

    @Test
    public void testFoundAsALayoutEditorService() {
        List<String> found = new ArrayList<>();
        for (TrackMarkerDropHandler h : ServiceLoader.load(TrackMarkerDropHandler.class)) {
            found.add(h.getClass().getName());
        }
        assertTrue(found.contains(SimulatedTrainMarkerHandler.class.getName()), found.toString());
    }

    @Test
    public void testActiveOnlyInASimulatedProfile() {
        assertTrue(handler.isActive(), "every connection is a simulator");
        ConnectionConfigManager ccm = InstanceManager.getDefault(ConnectionConfigManager.class);
        ConnectionConfig real = new jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig();
        ccm.add(real);
        assertFalse(handler.isActive(), "a connection to a layout");
        ccm.remove(real);
        assertTrue(handler.isActive());
    }

    @Test
    public void testOkPlacesTheTrainAndTellsTrainTracking() {
        drop(marker("3 Mail"), layout.block("Platform1"), press("OK"));
        assertEquals(1, told.get(), "train tracking told once");
        assertEquals(List.of(layout.block("Platform1")),
                SimulationTestUtil.truth(java.util.Objects.requireNonNull(PlaceTrainModel.placedTrain("3 Mail"))).getBlocks());
    }

    @Test
    public void testMarkerFromTheRosterUsesItsEntry() throws java.io.IOException {
        RosterEntry tank = SimulationTestLayouts.rosterEntry(SimulationTestLayouts.Loco.TANK);
        LocoIcon m = new LocoIcon(layout.getEditor());
        m.setRosterEntry(tank);
        Block platform = layout.block("Platform1");

        PlaceTrainModel model = new PlaceTrainModel(m.getRosterEntry(), m.getText(), platform);
        assertTrue(model.hasRosterEntry());
        assertEquals(tank.getId(), model.getName(), "named by its roster entry");
        assertTrue(model.hasMeasuredSpeeds(), "the entry's speed profile");
        PlaceTrainPanel panel = ThreadingUtil.runOnGUIwithReturn(() -> new PlaceTrainPanel(model));
        for (java.awt.Component c : panel.getComponents()) {
            assertFalse("PlaceAddress".equals(c.getName()), "the address is the entry's, so it is not asked");
        }

        drop(m, platform, press("OK"));
        VirtualTrain t = java.util.Objects.requireNonNull(PlaceTrainModel.placedTrain(tank.getId()), "placed");
        assertEquals(tank.getDccLocoAddress(), t.getLocos().get(0).getAddress());
        assertEquals(tank.getId(), t.getLocos().get(0).getRosterId());
        assertTrue(t.getLocos().get(0).hasMeasuredSpeeds());
        assertEquals(1, told.get());
    }

    @Test
    public void testCancelDoesNothing() {
        drop(marker("3 Mail"), layout.block("Platform1"), press("Cancel"));
        assertEquals(0, told.get());
        assertNull(PlaceTrainModel.placedTrain("3 Mail"));
    }

    @Test
    public void testRefusalIsShownAndTheDialogAskedAgain() {
        drop(marker("3 Mail"), layout.block("Platform1"), press("OK"));
        told.set(0);
        // a second train offered the same place: refused, shown, asked again, given up
        drop(marker("4 Coal"), layout.block("Platform1"), press("OK", "OK", "Cancel"));
        assertEquals(0, told.get(), "train tracking not told of a train that was not placed");
        assertNull(PlaceTrainModel.placedTrain("4 Coal"));
        assertEquals(1, SimulationTestUtil.layout().getTrains().size());
    }

    @Test
    public void testBlockMenuRemovesTheTrain() {
        Block platform = layout.block("Platform1");
        JPopupMenu empty = new JPopupMenu();
        ThreadingUtil.runOnGUI(() -> handler.addBlockMenuItems(empty, platform));
        assertEquals(0, empty.getComponentCount(), "no simulated train there");

        drop(marker("3 Mail"), platform, press("OK"));
        JUnitUtil.waitFor(() -> sensor(platform).getKnownState() == Sensor.ACTIVE, "the Block's sensor active");

        JPopupMenu other = new JPopupMenu();
        ThreadingUtil.runOnGUI(() -> handler.addBlockMenuItems(other, layout.block("Platform2")));
        assertEquals(0, other.getComponentCount(), "not in that Block");

        JPopupMenu popup = new JPopupMenu();
        ThreadingUtil.runOnGUI(() -> handler.addBlockMenuItems(popup, platform));
        assertEquals(1, popup.getComponentCount());
        JMenuItem item = (JMenuItem) popup.getComponent(0);
        assertEquals("Remove Simulated Train \"3 Mail\"", item.getText());
        ThreadingUtil.runOnGUI(item::doClick);
        assertNull(PlaceTrainModel.placedTrain("3 Mail"));
        JUnitUtil.waitFor(() -> sensor(platform).getKnownState() == Sensor.INACTIVE, "the Block's sensor inactive");
    }

    @BeforeEach
    public void setUp(@TempDir File folder) {
        JUnitUtil.setUp();
        SimulationTestLayouts.setUpProfile(folder);
        layout = SimulationTestLayouts.terminus();
        // lengths in millimetres, as Block lengths in centimetres (LengthUnitsTest has inches)
        InstanceManager.getDefault(jmri.UserPreferencesManager.class)
                .setSimplePreferenceState(jmri.jmrit.beantable.BlockTableAction.BLOCK_METRIC_PREF, true);
        handler = new SimulatedTrainMarkerHandler();
        told.set(0);
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        if (layout != null) {
            layout.dispose();
        }
        layout = null;
        handler = null;
        jmri.jmrit.display.EditorFrameOperator.clearEditorFrameOperatorThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
