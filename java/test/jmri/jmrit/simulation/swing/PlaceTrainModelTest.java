package jmri.jmrit.simulation.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jmri.Block;
import jmri.Sensor;
import jmri.jmrit.simulation.Detection;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrit.simulation.SimulationTruth;
import jmri.jmrit.simulation.VirtualTrain;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * What is offered when a loco marker is dropped on a Block, and the placing.
 * Positions are worked out here by hand from the Blocks' lengths.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class PlaceTrainModelTest {

    private Block[] blocks;

    private static Sensor sensor(Block b) {
        return java.util.Objects.requireNonNull(b.getSensor(), "the Block's sensor");
    }

    @Test
    public void testNameAndAddressFromMarkerText() {
        assertEquals("47012", new PlaceTrainModel(null, " 47012 ", blocks[1]).getName());
        assertEquals(0, new PlaceTrainModel(null, "47012", blocks[1]).suggestedAddress(), "not a DCC address");
        assertEquals(3012, new PlaceTrainModel(null, "3012 Mail", blocks[1]).suggestedAddress());
        assertEquals(3, new PlaceTrainModel(null, "3", blocks[1]).suggestedAddress());
        assertEquals(0, new PlaceTrainModel(null, "Mail 3012", blocks[1]).suggestedAddress());
        assertEquals(0, new PlaceTrainModel(null, null, blocks[1]).suggestedAddress());
        assertEquals("", new PlaceTrainModel(null, null, blocks[1]).getName());
        assertFalse(new PlaceTrainModel(null, "3", blocks[1]).hasRosterEntry());
        assertFalse(new PlaceTrainModel(null, "3", blocks[1]).hasMeasuredSpeeds());
    }

    @Test
    public void testNeighboursAreTheBlocksNextToIt() {
        assertEquals(List.of(blocks[0], blocks[2]), new PlaceTrainModel(null, "3", blocks[1]).getNeighbours());
        assertEquals(List.of(blocks[1]), new PlaceTrainModel(null, "3", blocks[0]).getNeighbours());
    }

    @Test
    public void testFrontOfferedPutsTheTrainInTheMiddle() {
        PlaceTrainModel m = new PlaceTrainModel(null, "3", blocks[1]);
        // Block 1000 mm, train 200 mm: rear at 400, front at 600
        assertEquals(600.0, m.suggestedFrontMm(200.0));
        // a train as long as the Block fills it
        assertEquals(1000.0, m.suggestedFrontMm(1000.0));
        // a longer train has its front at the far end
        assertEquals(1000.0, m.suggestedFrontMm(1500.0));
        blocks[1].setLength(0);
        assertEquals(0.0, m.suggestedFrontMm(200.0), "a Block with no length");
    }

    @Test
    public void testBuildDescribesTheTrain() {
        PlaceTrainModel m = new PlaceTrainModel(null, "3012 Mail", blocks[1]);
        VirtualTrain t = m.build(3012, 300.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0);
        assertEquals("3012 Mail", t.getName());
        assertEquals(300.0, t.getLengthMm());
        assertEquals(Detection.Mode.ENTIRE_TRAIN, t.getDetection().getMode());
        assertEquals(3012, t.getLocos().get(0).getAddress().getNumber());
        assertTrue(t.getLocos().get(0).getAddress().isLongAddress());
        assertFalse(m.build(3, 300.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0)
                .getLocos().get(0).getAddress().isLongAddress());

        VirtualTrain head = m.build(3012, 300.0, PlaceTrainModel.DetectionChoice.HEAD_ONLY, 150.0, 0.0);
        assertEquals(Detection.Mode.HEAD_ONLY, head.getDetection().getMode());
        assertEquals(150.0, head.getLocos().get(0).getLengthMm());

        VirtualTrain both = m.build(3012, 300.0, PlaceTrainModel.DetectionChoice.HEAD_AND_TAIL, 150.0, 40.0);
        assertEquals(Detection.Mode.HEAD_AND_TAIL, both.getDetection().getMode());

        assertEquals("7", new PlaceTrainModel(null, null, blocks[1])
                .build(7, 300.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0).getName(),
                "a marker with no text is named by its address");
    }

    @Test
    public void testBuildRefusesWhatCanNotBe() {
        PlaceTrainModel m = new PlaceTrainModel(null, "Mail", blocks[1]);
        assertThrows(IllegalArgumentException.class,
                () -> m.build(0, 300.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0), "no address");
        assertThrows(IllegalArgumentException.class,
                () -> m.build(10240, 300.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> m.build(3, 0.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0), "no length");
        assertThrows(IllegalArgumentException.class,
                () -> m.build(3, 300.0, PlaceTrainModel.DetectionChoice.HEAD_ONLY, 0.0, 0.0), "no loco length");
        assertThrows(IllegalArgumentException.class,
                () -> m.build(3, 300.0, PlaceTrainModel.DetectionChoice.HEAD_AND_TAIL, 150.0, 0.0), "no tail marker");
    }

    @Test
    public void testPlacePutsTheTrainWhereAsked() {
        PlaceTrainModel m = new PlaceTrainModel(null, "3 Mail", blocks[1]);
        VirtualTrain t = m.build(3, 200.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0);
        m.place(t, blocks[0], 600.0);

        SimulationTruth.TrainTruth truth = SimulationTestUtil.truth(t);
        assertNotNull(truth);
        assertEquals(blocks[1], truth.getFrontBlock());
        assertEquals(600.0, truth.getFrontPositionMm(), 0.001);
        assertEquals(List.of(blocks[1]), truth.getBlocks(), "rear at 400 mm, so wholly in the Block");
        JUnitUtil.waitFor(() -> sensor(blocks[1]).getKnownState() == Sensor.ACTIVE, "the Block's sensor active");
        assertEquals(Sensor.INACTIVE, sensor(blocks[0]).getKnownState());

        assertEquals(t, PlaceTrainModel.placedTrain("3 Mail"));
        assertNull(PlaceTrainModel.placedTrain("Mail"));
        assertEquals(List.of(t), PlaceTrainModel.trainsIn(blocks[1]));
        assertTrue(PlaceTrainModel.trainsIn(blocks[0]).isEmpty());
        assertEquals(blocks[1], PlaceTrainModel.frontBlock(t));
    }

    @Test
    public void testTrainLongerThanTheBlockReachesBackTheWayItFaces() {
        PlaceTrainModel m = new PlaceTrainModel(null, "3 Mail", blocks[1]);
        VirtualTrain t = m.build(3, 1500.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0);
        // front at the far end of the 1000 mm Block; rear 500 mm back into the Block it faces away from
        m.place(t, blocks[2], m.suggestedFrontMm(1500.0));
        assertEquals(List.of(blocks[1], blocks[2]), SimulationTestUtil.truth(t).getBlocks(), "front to rear");
    }

    @Test
    public void testPlacingAgainMovesTheTrain() {
        PlaceTrainModel first = new PlaceTrainModel(null, "3 Mail", blocks[0]);
        VirtualTrain a = first.build(3, 200.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0);
        first.place(a, null, 600.0);
        assertEquals(List.of(a), PlaceTrainModel.trainsIn(blocks[0]));

        PlaceTrainModel second = new PlaceTrainModel(null, "3 Mail", blocks[2]);
        VirtualTrain b = second.build(3, 200.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0);
        second.place(b, blocks[1], 600.0);

        assertTrue(PlaceTrainModel.trainsIn(blocks[0]).isEmpty(), "taken out of where it was");
        assertEquals(List.of(b), PlaceTrainModel.trainsIn(blocks[2]));
        assertEquals(1, SimulationTestUtil.layout().getTrains().size(), "one train of that name");
        JUnitUtil.waitFor(() -> sensor(blocks[0]).getKnownState() == Sensor.INACTIVE
                && sensor(blocks[2]).getKnownState() == Sensor.ACTIVE, "sensors follow the move");
    }

    @Test
    public void testPlaceOnAnotherTrainIsRefusedWithTheReason() {
        PlaceTrainModel m = new PlaceTrainModel(null, "3 Mail", blocks[1]);
        m.place(m.build(3, 200.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0), blocks[0], 600.0);

        PlaceTrainModel other = new PlaceTrainModel(null, "4 Coal", blocks[1]);
        VirtualTrain coal = other.build(4, 200.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> other.place(coal, blocks[0], 600.0));
        assertTrue(e.getMessage().contains("3 Mail"), e.getMessage());
        assertNull(PlaceTrainModel.placedTrain("4 Coal"));
    }

    @Test
    public void testMoveThatCanNotBeMadeSaysTheTrainIsOffTheTrack() {
        PlaceTrainModel m = new PlaceTrainModel(null, "3 Mail", blocks[1]);
        m.place(m.build(3, 200.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0), blocks[0], 600.0);

        VirtualTrain again = m.build(3, 200.0, PlaceTrainModel.DetectionChoice.ENTIRE_TRAIN, 0.0, 0.0);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> m.place(again, blocks[0], 5000.0), "front beyond the Block");
        assertTrue(e.getMessage().contains("taken off the track"), e.getMessage());
        assertNull(PlaceTrainModel.placedTrain("3 Mail"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        SimulationTestUtil.setUpSimulatedProfile();
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
