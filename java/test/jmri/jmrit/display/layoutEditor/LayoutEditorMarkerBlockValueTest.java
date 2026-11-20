package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.geom.Point2D;

import jmri.InstanceManager;
import jmri.jmrit.display.LocoIcon;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A loco marker dropped on a Block's track names the train in the Block, when
 * the panel's option is chosen.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutEditorMarkerBlockValueTest {

    private LayoutEditor le;
    private LayoutBlock lb;

    @Test
    public void testOffByDefault() {
        assertFalse(le.isMarkerNamingTrainInBlock(), "off unless chosen");
        drop("60009 Mail");
        assertNull(lb.getBlock().getValue(), "the Block's value is left alone");
    }

    @Test
    public void testDroppedMarkerNamesTheTrainInTheBlock() {
        le.setMarkerNamingTrainInBlock(true);
        drop("60009 Mail");
        assertEquals("60009 Mail", lb.getBlock().getValue(), "the Block's value is the train's name");
        le.setMarkerNamingTrainInBlock(false);
        assertFalse(le.isMarkerNamingTrainInBlock());
    }

    @Test
    public void testRosterMarkerUsesTheRosterEntrysId() {
        le.setMarkerNamingTrainInBlock(true);
        jmri.jmrit.roster.RosterEntry entry = new jmri.jmrit.roster.RosterEntry();
        entry.setId("A4 Mallard");
        entry.setDccAddress("4468");
        LocoIcon marker = new LocoIcon(le);
        marker.setText("4468");
        marker.setRosterEntry(entry);
        ThreadingUtil.runOnGUI(() -> le.markerDropped(marker, new Point2D.Double(200.0, 101.0)));
        assertEquals("A4 Mallard", lb.getBlock().getValue(), "named as train tracking names it");
    }

    private void drop(String text) {
        LocoIcon marker = new LocoIcon(le);
        marker.setText(text);
        ThreadingUtil.runOnGUI(() -> le.markerDropped(marker, new Point2D.Double(200.0, 101.0)));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
        le = ThreadingUtil.runOnGUIwithReturn(() -> new LayoutEditor("Marker Block Value Test"));
        // a connection to a layout, so a dropped marker places no simulated train
        JUnitUtil.initConnectionConfigManager();
        InstanceManager.getDefault(jmri.jmrix.ConnectionConfigManager.class)
                .add(new jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig());
        PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, le);
        le.addLayoutTrack(p1, new PositionablePointView(p1, new Point2D.Double(100.0, 100.0), le));
        PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, le);
        le.addLayoutTrack(p2, new PositionablePointView(p2, new Point2D.Double(300.0, 100.0), le));
        TrackSegment ts = new TrackSegment("TS1", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, false, le);
        le.addLayoutTrack(ts, new TrackSegmentView(ts, le));
        lb = java.util.Objects.requireNonNull(
                InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock("ILB1", "LB4"));
        lb.initializeLayoutBlock();
        assertNotNull(lb.getBlock());
        ts.setLayoutBlock(lb);
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
