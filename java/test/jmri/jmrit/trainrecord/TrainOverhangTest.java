package jmri.jmrit.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Objects;

import jmri.InstanceManager;
import jmri.TrainRecord;
import jmri.TrainRecord.Detection;
import jmri.TrainRecord.DetectionMarker;
import jmri.TrainRecord.End;
import jmri.TrainRecord.LengthSource;
import jmri.TrainRecord.TrainComponent;
import jmri.TrainRecordManager;
import jmri.implementation.SignalSpeedMap;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Test how far a train reaches beyond its detected part at each end.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainOverhangTest {

    private static final double MM = 0.001;
    private static final double DEFAULT = 50;

    private TrainRecord record;

    private void set(Runnable change) {
        ThreadingUtil.runOnLayout(change::run);
    }

    private static TrainOverhang at(TrainRecord r, End end) {
        return Objects.requireNonNull(TrainOverhang.of(r, end, DEFAULT), "an overhang at the " + end);
    }

    private static void assertRange(double min, double max, TrainOverhang o, String why) {
        assertEquals(min, o.getMinMm(), MM, why + ": least");
        assertEquals(max, o.getMaxMm(), MM, why + ": most");
    }

    @Test
    public void testEntireTrainHasItsEndComponentsWheelOverhangs() {
        set(() -> record.setTrainComponents(List.of(new TrainComponent("loco", 220.0, 18.0, 12.0),
                new TrainComponent("coaches", 700.0, 25.0, 30.0))));
        assertRange(18, 18, at(record, End.FRONT), "the first component's front overhang");
        assertRange(30, 30, at(record, End.REAR), "the last component's rear overhang");
    }

    @Test
    public void testUnknownWheelOverhangIsTheDefault() {
        assertRange(DEFAULT, DEFAULT, at(record, End.FRONT), "a generous default where none is known");
        assertRange(DEFAULT, DEFAULT, at(record, End.REAR), "at both ends");
    }

    @Test
    public void testHeadOnlyHasTheRestOfTheTrainBehind() {
        set(() -> {
            record.setDetection(Detection.HEAD_ONLY);
            record.setTrainComponents(List.of(new TrainComponent("loco", 220.0, 18.0, 12.0),
                    new TrainComponent("wagons", null)));
        });
        assertRange(18, 18, at(record, End.FRONT), "the loco's front overhang");
        assertNull(TrainOverhang.of(record, End.REAR, DEFAULT), "behind a head, with no length for the train");
        set(() -> record.setLength(LengthSource.ENTERED, 1000.0));
        assertRange(DEFAULT, 1000, at(record, End.REAR), "a propelled train: anywhere up to the whole train");
        set(() -> record.setCustomMarkers(List.of(DetectionMarker.front(200.0))));
        assertRange(800, 800, at(record, End.REAR), "the train less its 200 mm head");
    }

    @Test
    public void testHeadAndTailHasSmallOverhangsAtBothEnds() {
        set(() -> {
            record.setDetection(Detection.HEAD_AND_TAIL);
            record.setTrainComponents(List.of(new TrainComponent("loco", 220.0, 18.0, 12.0),
                    new TrainComponent("wagons", 780.0, 10.0, 15.0)));
        });
        assertRange(18, 18, at(record, End.FRONT), "the loco's front overhang");
        assertRange(15, 15, at(record, End.REAR), "the last wagon's rear overhang");
    }

    @Test
    public void testMidTrainLocoHasALargeOverhangAtBothEnds() {
        set(() -> {
            record.setLength(LengthSource.ENTERED, 1000.0);
            record.setCustomMarkers(List.of(DetectionMarker.atOffset(300.0, 200.0)));
        });
        assertRange(300, 300, at(record, End.FRONT), "the stock ahead of the loco");
        assertRange(500, 500, at(record, End.REAR), "the stock behind it");

        set(() -> record.setCustomMarkers(List.of(DetectionMarker.atOffset(null, 200.0))));
        assertRange(DEFAULT, 800, at(record, End.FRONT), "a loco somewhere in the train, not known where");
        assertRange(DEFAULT, 800, at(record, End.REAR), "the same behind");

        set(() -> record.setCustomMarkers(List.of(DetectionMarker.atOffset(300.0, null))));
        assertRange(300, 300, at(record, End.FRONT), "its front is known");
        assertRange(DEFAULT, 700, at(record, End.REAR), "its length isn't, so its rear may be anywhere behind");
    }

    @Test
    public void testMarkerAtTheRearOnly() {
        set(() -> {
            record.setLength(LengthSource.ENTERED, 1000.0);
            record.setCustomMarkers(List.of(DetectionMarker.rear(150.0)));
        });
        assertRange(850, 850, at(record, End.FRONT), "a loco propelling from the rear: the train ahead of it");
        assertRange(DEFAULT, DEFAULT, at(record, End.REAR), "the loco's own rear overhang");
    }

    @Test
    public void testDefaultFollowsTheLayoutScale() {
        InstanceManager.getDefault(SignalSpeedMap.class).setLayoutScale(87.1f);
        assertEquals(TrainOverhang.DEFAULT_PROTOTYPE_MM / 87.1, TrainOverhang.defaultMm(), 0.01, "HO");
        InstanceManager.getDefault(SignalSpeedMap.class).setLayoutScale(148f);
        assertEquals(TrainOverhang.DEFAULT_PROTOTYPE_MM / 148, TrainOverhang.defaultMm(), 0.01, "N");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        record = ThreadingUtil.runOnGUIwithReturn(
                () -> InstanceManager.getDefault(TrainRecordManager.class).newRecord("Goods"));
    }

    @AfterEach
    public void tearDown() {
        record = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
