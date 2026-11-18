package jmri.jmrit.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.Block;
import jmri.InstanceManager;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Finding;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Kind;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The set-up checks of lengths: a train's lengths that disagree, and a
 * Block whose length its measurements doubt.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainTrackingChecksLengthsTest {

    private TrainRecord train(String name, Double entered, Double operations, Double measured, boolean consistent) {
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            TrainRecord r = manager.newRecord(name);
            r.setLength(TrainRecord.LengthSource.ENTERED, entered);
            r.setLength(TrainRecord.LengthSource.OPERATIONS, operations);
            if (measured != null) {
                r.setMeasuredLength(measured, consistent);
            }
            return r;
        });
    }

    private static List<Finding> of(List<Finding> found, Kind kind) {
        List<Finding> those = new ArrayList<>();
        for (Finding f : found) {
            if (f.getKind() == kind) {
                those.add(f);
            }
        }
        return those;
    }

    @Test
    public void testTrainLengthsThatDisagree() {
        TrainRecord agree = train("Agree", 1000.0, 950.0, 1040.0, true); // within a tenth
        TrainRecord disagree = train("Disagree", 1200.0, null, 900.0, true);
        TrainRecord unsure = train("Unsure", 1200.0, null, 900.0, false); // a measured length not yet consistent
        TrainRecord shortOne = train("Short", 200.0, 180.0, null, false); // a fifth, but only 20 mm
        List<Finding> found = of(TrainTrackingChecks.run(List.of(), List.of(agree, disagree, unsure, shortOne)),
                Kind.TRAIN_LENGTHS_DISAGREE);
        assertEquals(1, found.size(), "only the train whose lengths disagree");
        assertSame(disagree, found.get(0).getSubject());
        assertEquals("Train \"Disagree\"'s lengths disagree: entered 1,200 mm, measured 900 mm. Train tracking holds "
                + "Blocks for the longest, 1,200 mm, and Dispatcher decides where the train fits by its Max Train "
                + "Length: correct whichever is wrong.", found.get(0).getMessage());
    }

    @Test
    public void testRosterLengthIsNotCompared() {
        // the roster gives only the locos' length, which is bound to be shorter than the train
        TrainRecord r = train("Goods", 1200.0, null, null, false);
        ThreadingUtil.runOnLayout(() -> r.setLength(TrainRecord.LengthSource.ROSTER, 300.0));
        assertTrue(of(TrainTrackingChecks.run(List.of(), List.of(r)), Kind.TRAIN_LENGTHS_DISAGREE).isEmpty());
    }

    private static TrainTrackingMeasurements.Measurement measured(Block b, Block from, Block to, double... ratios) {
        TrainTrackingMeasurements.Measurement m = new TrainTrackingMeasurements.Measurement(b, from, to);
        for (double ratio : ratios) {
            m.add(300 * ratio, 300.0);
        }
        return m;
    }

    @Test
    public void testBlockLengthDoubted() {
        Block a = new Block("IBA", "A");
        Block b = new Block("IBB", "B");
        Block c = new Block("IBC", "C");
        Block d = new Block("IBD", "D");
        Block e = new Block("IBE", "E");
        b.setLength(1000);
        // trains run at their profiles' speed (about 1.05) through A, C and D, but 1.5 times faster through B:
        // B's length that way is too long by half, and about 700 mm would fit
        List<TrainTrackingMeasurements.Measurement> ms = List.of(
                measured(a, e, b, 1.05, 1.05, 1.05),
                measured(b, a, c, 1.5, 1.6, 1.55),
                measured(c, b, d, 1.0, 1.1, 1.05),
                measured(d, c, e, 1.1, 1.0, 1.05),
                measured(e, d, a, 2.0, 2.0)); // too few crossings compared to count
        List<Finding> found = TrainTrackingChecks.checkMeasuredLengths(ms);
        assertEquals(1, found.size(), "only B");
        assertEquals(Kind.BLOCK_LENGTH_DOUBTFUL, found.get(0).getKind());
        assertSame(b, found.get(0).getSubject());
        assertEquals(List.of(a, c), found.get(0).getRelated());
        assertEquals("Trains cross Block \"B\" from \"A\" to \"C\" at 1.55 times the speed their profiles give, "
                + "against 1.05 on the rest of the layout, so its length that way, 1,000 mm, may be wrong: about "
                + "677 mm would fit. Check the Block's length, or its route length that way.",
                found.get(0).getMessage());
    }

    @Test
    public void testNothingToCompareWith() {
        Block a = new Block("IBA", "A");
        Block b = new Block("IBB", "B");
        Block c = new Block("IBC", "C");
        // one other way through a Block is too little to say what is usual on this layout
        assertTrue(TrainTrackingChecks.checkMeasuredLengths(List.of(
                measured(b, a, c, 1.6, 1.6, 1.6),
                measured(a, c, b, 1.0, 1.0, 1.0))).isEmpty());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
