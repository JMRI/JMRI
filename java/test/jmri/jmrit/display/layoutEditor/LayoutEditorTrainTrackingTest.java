package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.util.List;

import jmri.InstanceManager;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.jmrit.display.LocoIcon;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Train tracking on a Layout Editor panel: a loco marker dropped on a Block's
 * track says the train is there.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutEditorTrainTrackingTest {

    private LayoutEditor le;

    @Test
    public void testMarkerDroppedOnTrack() {
        PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, le);
        le.addLayoutTrack(p1, new PositionablePointView(p1, new Point2D.Double(100.0, 100.0), le));
        PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, le);
        le.addLayoutTrack(p2, new PositionablePointView(p2, new Point2D.Double(300.0, 100.0), le));
        TrackSegment ts = new TrackSegment("TS1", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, false, le);
        le.addLayoutTrack(ts, new TrackSegmentView(ts, le));
        LayoutBlock lb = java.util.Objects.requireNonNull(
                InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock("ILB1", "LB4"));
        lb.initializeLayoutBlock();
        assertNotNull(lb.getBlock());
        ts.setLayoutBlock(lb);

        assertEquals(lb.getBlock(), le.blockOfTrackAt(new Point2D.Double(200.0, 101.0)), "the track's Block");
        assertEquals(null, le.blockOfTrackAt(new Point2D.Double(400.0, 300.0)), "no track there");
        assertTrue(jmri.jmrit.trainrecord.PermissionsTrainTracking.mayCorrect());

        LocoIcon marker = new LocoIcon(le);
        marker.setText("60009 Mail");
        ThreadingUtil.runOnGUI(() -> le.markerDropped(marker, new Point2D.Double(400.0, 300.0)));
        assertTrue(InstanceManager.getDefault(TrainRecordManager.class).getRecords().isEmpty(), "dropped off the track");

        ThreadingUtil.runOnGUI(() -> le.markerDropped(marker, new Point2D.Double(200.0, 101.0)));
        List<TrainRecord> records = InstanceManager.getDefault(TrainRecordManager.class).getRecords();
        assertEquals(1, records.size());
        assertEquals("60009 Mail", records.get(0).getName());
        assertEquals(List.of(lb.getBlock()), records.get(0).getBlocks(), "the train is there");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
        le = ThreadingUtil.runOnGUIwithReturn(() -> new LayoutEditor("Train Tracking Test"));
        // A connection to a layout, so the profile is not a simulated one, where a
        // dropped marker places a simulated train first (LayoutEditorSimulatedTrainTest).
        // After the panel is made: making the first window sets up the preferences,
        // which brings a connection manager of its own.
        JUnitUtil.initConnectionConfigManager();
        InstanceManager.getDefault(jmri.jmrix.ConnectionConfigManager.class)
                .add(new jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig());
    }

    @AfterEach
    public void tearDown() {
        if (le != null) {
            JUnitUtil.dispose(le);
        }
        le = null;
        jmri.jmrit.display.EditorFrameOperator.clearEditorFrameOperatorThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
