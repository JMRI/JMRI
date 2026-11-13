package jmri.jmrit.simulation.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;

import jmri.Block;
import jmri.InstanceManager;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrit.simulation.SimulationTruth;
import jmri.jmrit.simulation.VirtualTrain;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The panel asked when a simulated train is placed from a loco marker: what
 * it offers, and what it places.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class PlaceTrainPanelTest {

    private Block[] blocks;

    private static Component named(Container c, String name) {
        for (Component child : c.getComponents()) {
            if (name.equals(child.getName())) {
                return child;
            }
        }
        return null;
    }

    private static List<String> labels(Container c) {
        List<String> texts = new ArrayList<>();
        for (Component child : c.getComponents()) {
            if (child instanceof JLabel) {
                texts.add(((JLabel) child).getText());
            }
        }
        return texts;
    }

    private PlaceTrainPanel panel(String markerText, Block block) {
        return ThreadingUtil.runOnGUIwithReturn(() -> new PlaceTrainPanel(new PlaceTrainModel(null, markerText, block)));
    }

    @Test
    public void testOffersWhatIsKnown() {
        PlaceTrainPanel p = panel("3012 Mail", blocks[1]);
        assertTrue(labels(p).contains("3012 Mail"), "the train's name");
        assertTrue(labels(p).contains("Middle"), "the Block");
        assertEquals(3012, ((JSpinner) named(p, "PlaceAddress")).getValue(), "the address the name starts with");
        assertEquals(200.0, ((JSpinner) named(p, "PlaceLength")).getValue());
        assertEquals(600.0, ((JSpinner) named(p, "PlaceFront")).getValue(), "in the middle of the 1000 mm Block");
        JComboBox<?> towards = (JComboBox<?>) named(p, "PlaceRearTowards");
        assertEquals(3, towards.getItemCount());
        assertEquals("West", towards.getItemAt(0));
        assertEquals("East", towards.getItemAt(1));
        assertEquals("The end of the track", towards.getItemAt(2));
        assertTrue(((JCheckBox) named(p, "PlaceTellTrainTracking")).isSelected());
        assertTrue(p.isTellTrainTracking());
        assertTrue(labels(p).contains("This loco has no measured speeds, so its speeds are approximate."));
    }

    @Test
    public void testInInchesPlacesTheLengthsShown() {
        PlaceTrainPanel p = ThreadingUtil.runOnGUIwithReturn(
                () -> new PlaceTrainPanel(new PlaceTrainModel(null, "3 Mail", blocks[1]), new LengthUnits(false)));
        assertTrue(labels(p).contains("Train length (in):"), labels(p).toString());
        assertEquals(8.0, ((JSpinner) named(p, "PlaceLength")).getValue(), "8 in offered");
        assertEquals(2.0, ((JSpinner) named(p, "PlaceTailLength")).getValue(), "2 in offered");
        // the 1000 mm Block is 39 3/8 in; an 8 in (203.2 mm) train in its middle has its
        // front at 601.6 mm, 23 11/16 in to the nearest 1/32
        assertEquals(23.6875, ((JSpinner) named(p, "PlaceFront")).getValue());
        ThreadingUtil.runOnGUI(() -> {
            ((JSpinner) named(p, "PlaceLength")).setValue(10.0);
            ((JSpinner) named(p, "PlaceFront")).setValue(20.0);
            p.place();
        });
        VirtualTrain t = java.util.Objects.requireNonNull(PlaceTrainModel.placedTrain("3 Mail"), "placed");
        assertEquals(254.0, t.getLengthMm(), 1e-9, "10 in");
        assertEquals(508.0, SimulationTestUtil.truth(t).getFrontPositionMm(), 1e-6, "20 in");
    }

    @Test
    public void testLengthsAreAskedOnlyWhereDetectionUsesThem() {
        PlaceTrainPanel p = panel("3", blocks[1]);
        JComboBox<?> detection = (JComboBox<?>) named(p, "PlaceDetection");
        assertFalse(named(p, "PlaceLocoLength").isEnabled(), "entire train");
        assertFalse(named(p, "PlaceTailLength").isEnabled());
        ThreadingUtil.runOnGUI(() -> detection.setSelectedIndex(1));
        assertTrue(named(p, "PlaceLocoLength").isEnabled(), "head only");
        assertFalse(named(p, "PlaceTailLength").isEnabled());
        ThreadingUtil.runOnGUI(() -> detection.setSelectedIndex(2));
        assertTrue(named(p, "PlaceLocoLength").isEnabled(), "head and tail");
        assertTrue(named(p, "PlaceTailLength").isEnabled());
    }

    @Test
    public void testFrontFollowsTheLengthUntilItIsChanged() {
        PlaceTrainPanel p = panel("3", blocks[1]);
        JSpinner length = (JSpinner) named(p, "PlaceLength");
        JSpinner front = (JSpinner) named(p, "PlaceFront");
        ThreadingUtil.runOnGUI(() -> length.setValue(400.0));
        assertEquals(700.0, front.getValue(), "a 400 mm train in the middle of 1000 mm");
        ThreadingUtil.runOnGUI(() -> front.setValue(450.0));
        ThreadingUtil.runOnGUI(() -> length.setValue(300.0));
        assertEquals(450.0, front.getValue(), "left where the operator put it");
    }

    @Test
    public void testPlacesWhatItShows() {
        PlaceTrainPanel p = panel("3 Mail", blocks[1]);
        ThreadingUtil.runOnGUI(() -> {
            ((JSpinner) named(p, "PlaceLength")).setValue(300.0);
            ((JComboBox<?>) named(p, "PlaceRearTowards")).setSelectedIndex(1);
            ((JSpinner) named(p, "PlaceFront")).setValue(100.0);
            p.place();
        });
        VirtualTrain t = java.util.Objects.requireNonNull(PlaceTrainModel.placedTrain("3 Mail"), "placed");
        assertEquals(300.0, t.getLengthMm());
        SimulationTruth.TrainTruth truth = SimulationTestUtil.truth(t);
        assertEquals(100.0, truth.getFrontPositionMm(), 0.001);
        // front 100 mm into Middle from East, so its rear is 200 mm back in East
        assertEquals(List.of(blocks[1], blocks[2]), truth.getBlocks(), "rear towards East");
    }

    @Test
    public void testSaysWhenTheTrainIsAlreadyPlaced() {
        PlaceTrainPanel first = panel("3 Mail", blocks[0]);
        ThreadingUtil.runOnGUI(first::place);
        PlaceTrainPanel again = panel("3 Mail", blocks[2]);
        assertTrue(labels(again).contains("\"3 Mail\" is already placed in \"West\" and will be moved."), labels(again).toString());
    }

    @Test
    public void testRefusalIsPassedOnAndNothingIsPlaced() {
        PlaceTrainPanel p = panel("Mail", blocks[1]);
        assertNotNull(named(p, "PlaceAddress"), "no roster entry, so the address is asked");
        ThreadingUtil.runOnGUI(() -> ((JSpinner) named(p, "PlaceFront")).setValue(5000.0));
        java.util.concurrent.atomic.AtomicReference<IllegalArgumentException> refused =
                new java.util.concurrent.atomic.AtomicReference<>();
        ThreadingUtil.runOnGUI(() -> refused.set(assertThrows(IllegalArgumentException.class, p::place)));
        assertNotNull(refused.get(), "refused");
        assertTrue(refused.get().getMessage().contains("Middle"), refused.get().getMessage());
        assertNull(PlaceTrainModel.placedTrain("Mail"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        SimulationTestUtil.setUpSimulatedProfile();
        // lengths in millimetres, as Block lengths in centimetres (LengthUnitsTest has inches)
        InstanceManager.getDefault(jmri.UserPreferencesManager.class)
                .setSimplePreferenceState(jmri.jmrit.beantable.BlockTableAction.BLOCK_METRIC_PREF, true);
        blocks = SimulationTestUtil.line(1000.0, "West", "Middle", "East");
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        blocks = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
