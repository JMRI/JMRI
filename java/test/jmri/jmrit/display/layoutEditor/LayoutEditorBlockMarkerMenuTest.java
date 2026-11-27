package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
 * The Marker menu on a Layout Editor Block's pop-up menu: a loco marker added
 * from it is put where the track was clicked, and the train is in that Block.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutEditorBlockMarkerMenuTest {

    private LayoutEditor le;

    @Test
    public void testBlockMenuAddsAMarkerThere() {
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

        Point2D clicked = new Point2D.Double(200.0, 101.0);
        javax.swing.JMenu menu = ThreadingUtil.runOnGUIwithReturn(() -> le.blockMarkerMenu(clicked));
        assertNotNull(menu, "a Marker menu on the Block's pop-up menu");
        assertEquals(2, menu.getItemCount(), "Add Loco and Add Loco from Roster");

        LocoIcon marker = ThreadingUtil.runOnGUIwithReturn(() -> le.addLocoIcon("60009 Mail"));
        ThreadingUtil.runOnGUI(() -> le.placeMarker(marker, clicked));
        assertEquals(200, marker.getX() + marker.getWidth() / 2, "the marker is centred where the track was clicked");
        assertEquals(101, marker.getY() + marker.getHeight() / 2);
        List<TrainRecord> records = InstanceManager.getDefault(TrainRecordManager.class).getRecords();
        assertEquals(1, records.size());
        assertEquals(List.of(lb.getBlock()), records.get(0).getBlocks(), "the train is in the Block clicked");

        le.setLocoMarkerPopupDisabled(true);
        assertNull(ThreadingUtil.runOnGUIwithReturn(() -> le.blockMarkerMenu(clicked)),
                "no Marker menu when loco marker pop-up menus are disabled");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
        le = ThreadingUtil.runOnGUIwithReturn(() -> new LayoutEditor("Block Marker Menu Test"));
        // a connection to a layout, so a placed marker places no simulated train
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
