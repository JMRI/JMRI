package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.List;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.simulation.SimulationTestLayout;
import jmri.jmrit.simulation.SimulationTestLayouts;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrit.simulation.SimulationTruth;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * In a profile whose connections are all simulators, a loco marker dropped on
 * a Block's track on a Layout Editor panel places a simulated train there,
 * and tells train tracking if that is left ticked. On the standard terminus
 * layout, with the marker dropped on Platform1.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutEditorSimulatedTrainTest {

    private static final String TITLE = "Place Simulated Train";
    /** On the track of Platform1, which runs from the Throat turnout at (700, 300) to (1300, 300). */
    private static final Point2D ON_TRACK = new Point2D.Double(1000.0, 300.0);

    private SimulationTestLayout layout;
    private LayoutEditor le;
    private Block block;
    private LocoIcon marker;

    private static Sensor sensor(Block b) {
        return java.util.Objects.requireNonNull(b.getSensor(), "the Block's sensor");
    }

    /** Answer the placing dialog on another thread, as it is modal. */
    private Thread answer(boolean tellTrainTracking, String button) {
        Thread t = new Thread(() -> {
            JDialogOperator dialog = new JDialogOperator(TITLE);
            if (!tellTrainTracking) {
                new JCheckBoxOperator(dialog, "Also tell train tracking it is here").changeSelection(false);
            }
            new JButtonOperator(dialog, button).doClick();
        }, "answer placing dialog");
        t.start();
        return t;
    }

    private void drop(Thread answering) {
        ThreadingUtil.runOnGUI(() -> le.markerDropped(marker, ON_TRACK));
        JUnitUtil.waitFor(() -> !answering.isAlive(), "placing dialog answered");
    }

    @Test
    public void testDropPlacesASimulatedTrainAndTellsTrainTracking() {
        drop(answer(true, "OK"));

        SimulationTruth truth = SimulationTestUtil.truth();
        assertEquals(1, truth.getTrains().size(), "one simulated train");
        SimulationTruth.TrainTruth train = truth.getTrains().get(0);
        assertEquals("3 Mail", train.getTrain().getName());
        assertEquals(3, train.getTrain().getLocos().get(0).getAddress().getNumber(), "the address its name starts with");
        assertEquals(List.of(block), train.getBlocks());
        // 200 mm offered for the train, in the middle of the 1200 mm Block, its rear towards the Throat
        assertEquals(200.0, train.getTrain().getLengthMm());
        assertEquals(700.0, train.getFrontPositionMm(), 0.001);
        JUnitUtil.waitFor(() -> sensor(block).getKnownState() == Sensor.ACTIVE, "the Block's sensor active");

        List<TrainRecord> records = InstanceManager.getDefault(TrainRecordManager.class).getRecords();
        assertEquals(1, records.size(), "train tracking told");
        assertEquals("3 Mail", records.get(0).getName());
        assertEquals(List.of(block), records.get(0).getBlocks());
    }

    @Test
    public void testUntickedLeavesTrainTrackingToFindTheTrain() {
        drop(answer(false, "OK"));

        assertEquals(1, SimulationTestUtil.truth().getTrains().size(), "simulated train placed");
        JUnitUtil.waitFor(() -> sensor(block).getKnownState() == Sensor.ACTIVE, "the Block's sensor active");
        for (TrainRecord r : InstanceManager.getDefault(TrainRecordManager.class).getRecords()) {
            assertTrue(r.getBlocks().isEmpty(), "no train was said to be anywhere");
        }
    }

    @Test
    public void testCancelDoesNothing() {
        drop(answer(true, "Cancel"));

        assertTrue(SimulationTestUtil.truth().getTrains().isEmpty(), "no simulated train");
        assertTrue(InstanceManager.getDefault(TrainRecordManager.class).getRecords().isEmpty(), "no train record");
        assertEquals(Sensor.INACTIVE, sensor(block).getKnownState());
    }

    @Test
    public void testDropOffTheTrackDoesNothing() {
        ThreadingUtil.runOnGUI(() -> le.markerDropped(marker, new Point2D.Double(400.0, 600.0)));
        assertTrue(SimulationTestUtil.truth().getTrains().isEmpty());
    }

    @BeforeEach
    public void setUp(@TempDir File folder) {
        JUnitUtil.setUp();
        SimulationTestLayouts.setUpProfile(folder);
        layout = SimulationTestLayouts.terminus();
        // lengths in millimetres, as Block lengths in centimetres (LengthUnitsTest has inches)
        InstanceManager.getDefault(jmri.UserPreferencesManager.class)
                .setSimplePreferenceState(jmri.jmrit.beantable.BlockTableAction.BLOCK_METRIC_PREF, true);
        le = layout.getEditor();
        block = layout.block("Platform1");
        marker = new LocoIcon(le);
        marker.setText("3 Mail");
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        if (layout != null) {
            layout.dispose();
        }
        layout = null;
        le = null;
        block = null;
        marker = null;
        jmri.jmrit.display.EditorFrameOperator.clearEditorFrameOperatorThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
