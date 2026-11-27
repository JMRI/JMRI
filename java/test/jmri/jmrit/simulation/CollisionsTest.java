package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.annotation.Nonnull;

import jmri.Block;
import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of {@link Collisions}: two trains in one Block on ways that foul each
 * other touch only where they foul, when that is known.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CollisionsTest {

    private Block block;

    /** A way through the Block, fouling the others where {@link #pieces} says. */
    private static final class Way implements Route {

        private final double[][] pieces;

        Way(double[][] pieces) {
            this.pieces = pieces;
        }

        @Override
        public boolean sameAs(@Nonnull Route other) {
            return other == this;
        }

        @Override
        public boolean reverseOf(@Nonnull Route other) {
            return false;
        }

        @Override
        public boolean fouls(@Nonnull Route other) {
            return other != this;
        }

        @Override
        @Nonnull
        public double[][] foulingPieces(@Nonnull Route other) {
            return pieces == null ? new double[0][] : pieces;
        }

        @Override
        @Nonnull
        public Route reversed() {
            return this;
        }
    }

    /** Where a train entered the Block. */
    private final class Entry extends TrackEntry {

        @Override
        Block getBlock() {
            return block;
        }

        @Override
        Passage follow() {
            throw new UnsupportedOperationException();
        }

        @Override
        TrackEntry reverse() {
            return this;
        }

        @Override
        String describe() {
            return "entry";
        }
    }

    /** A 200 mm train whose span in the 1000 mm Block follows a way. */
    private TrainState train(String name, Route way) {
        VirtualTrain t = VirtualTrain.builder(name)
                .loco(SimulatedLoco.builder(new DccLocoAddress(3, false)).length(100).build())
                .length(200).build();
        TrainState s = new TrainState(t, 0);
        s.spans.add(new TrainState.Span(new Passage(new Entry(), 1000, way, null, null, Passage.Stop.NONE, null),
                0, 1000));
        return s;
    }

    @Test
    public void testSharedTrackTheSameWayTouchesOnlyWhereTheyMeet() {
        // the first 400 mm of both ways are shared, the same way; then they part at a turnout
        Way main = new Way(new double[][]{{0, 0.4, 0, 0.4, 0}, {0.4, 0.4, 0.4, 0.4, 0}});
        Way branch = new Way(new double[][]{{0, 0.4, 0, 0.4, 0}, {0.4, 0.4, 0.4, 0.4, 0}});
        TrainState ahead = train("Ahead", branch);
        TrainState behind = train("Behind", main);
        // ahead's rear at 500, past the turnout; behind's front at 300: apart
        assertNull(Collisions.overlapAt(behind, 300, ahead, 700), "following, not touching");
        // ahead over the shared track from 250 to 450, behind's front at 300
        assertEquals(block, Collisions.overlapAt(behind, 300, ahead, 450), "the one behind has run into it");
    }

    @Test
    public void testTurnoutFoulsOnlyWhenBothAreOverIt() {
        Way main = new Way(new double[][]{{0.5, 0.5, 0.5, 0.5, 0}});
        Way branch = new Way(new double[][]{{0.5, 0.5, 0.5, 0.5, 0}});
        TrainState a = train("A", main);
        TrainState b = train("B", branch);
        assertNull(Collisions.overlapAt(a, 300, b, 900), "both in the Block, neither over the turnout");
        assertNull(Collisions.overlapAt(a, 600, b, 900), "only one over it");
        assertEquals(block, Collisions.overlapAt(a, 600, b, 550), "both over it");
    }

    @Test
    public void testFoulingNotKnownStillFoulsTheWholeBlock() {
        TrainState a = train("A", new Way(null));
        TrainState b = train("B", new Way(null));
        assertEquals(block, Collisions.overlapAt(a, 300, b, 900), "as before, both in the Block");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        block = new Block("IB1", "B");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
