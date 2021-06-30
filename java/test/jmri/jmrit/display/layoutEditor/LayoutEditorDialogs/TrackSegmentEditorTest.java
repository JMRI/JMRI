package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import javax.swing.*;

import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * Test simple functioning of TrackSegmentEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class TrackSegmentEditorTest extends LayoutTrackEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new TrackSegmentEditor(null);
    }

    @Test
    public void testEditTrackSegmentDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        trackSegmentView.setArc(true);
        trackSegmentView.setCircle(true);
        createBlocks();

        TrackSegmentEditor editor = new TrackSegmentEditor(layoutEditor);

        // Edit the track trackSegment
        editor.editLayoutTrack(trackSegmentView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTrackSegment"));

        // Select dashed
        JLabelOperator styleLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("Style")));
        JComboBoxOperator styleComboBoxOperator = new JComboBoxOperator(
                (JComboBox) styleLabelOperator.getLabelFor());
        styleComboBoxOperator.selectItem(Bundle.getMessage("Dashed"));

        // Select mainline
        JComboBoxOperator mainlineComboboxOperator = new JComboBoxOperator(
                jFrameOperator, new NameComponentChooser(Bundle.getMessage("Mainline")));
        mainlineComboboxOperator.selectItem(Bundle.getMessage("Mainline"));

        // Enable Hide
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("HideTrack")).doClick();

        // Select block
        JLabelOperator blockNameLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("BlockID"));
        JComboBoxOperator blockComboBoxOperator = new JComboBoxOperator(
                (JComboBox) blockNameLabelOperator.getLabelFor());
        blockComboBoxOperator.getTextField().setText("Blk 2");

        // Set arc angle
        JLabelOperator setArcAngleLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("SetArcAngle"));
        JTextFieldOperator jtxt = new JTextFieldOperator(
                (JTextField) setArcAngleLabelOperator.getLabelFor());
        jtxt.setText("35");

        // Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();

        //TODO: frame (dialog) titles hard coded here...
        // it should be based on Bundle.getMessage("EditBean", "Block", "DX Blk A"));
        // but that isn't working...
        JFrameOperator blkFO = new JFrameOperator("Edit Block Blk 2");
        new JButtonOperator(blkFO, Bundle.getMessage("ButtonOK")).doClick();

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEditTrackSegmentCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        trackSegmentView.setArc(false);
        trackSegmentView.setCircle(false);

        TrackSegmentEditor editor = new TrackSegmentEditor(layoutEditor);

        // Edit the track trackSegment
        editor.editLayoutTrack(trackSegmentView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTrackSegment"));

        // Create empty block edit dialog
        Thread segmentBlockError = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(segmentBlockError.isAlive());
        }, "segmentBlockError finished");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed

    }

    @Test
    public void testEditTrackSegmentClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        TrackSegmentEditor editor = new TrackSegmentEditor(layoutEditor);

        // Edit the track trackSegment
        editor.editLayoutTrack(trackSegmentView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTrackSegment"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed

    }

    @Test
    public void testEditTrackSegmentError() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        trackSegmentView.setArc(true);
        trackSegmentView.setCircle(true);

        TrackSegmentEditor editor = new TrackSegmentEditor(layoutEditor);

        // Edit the track trackSegment
        editor.editLayoutTrack(trackSegmentView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTrackSegment"));

        // Set arc angle
        JTextFieldOperator jtxt = new JTextFieldOperator(jFrameOperator, 1);
        jtxt.setText("abc");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed

    }



    private LayoutEditor layoutEditor = null;
    private TrackSegment trackSegment = null;
    private TrackSegmentView trackSegmentView = null;

    @BeforeEach
    public void setUp() {
        super.setUp();

        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        if (!GraphicsEnvironment.isHeadless()) {

            layoutEditor = new LayoutEditor();
            layoutEditor.setVisible(true);

            Point2D point = new Point2D.Double(150.0, 100.0);
            Point2D delta = new Point2D.Double(50.0, 10.0);

            // Track Segment
            PositionablePoint pp1 = new PositionablePoint("a", PositionablePoint.PointType.ANCHOR, layoutEditor);
            PositionablePointView pp1v = new PositionablePointView(pp1, point, layoutEditor);
            layoutEditor.addLayoutTrack(pp1, pp1v);

            point = MathUtil.add(point, delta);
            PositionablePoint pp2 = new PositionablePoint("b", PositionablePoint.PointType.ANCHOR, layoutEditor);
            PositionablePointView pp2v = new PositionablePointView(pp2, point, layoutEditor);
            layoutEditor.addLayoutTrack(pp2, pp2v);

            trackSegment = new TrackSegment("Segment", pp1, HitPointType.POS_POINT, pp2, HitPointType.POS_POINT,
                                            false, layoutEditor);
            trackSegmentView = new TrackSegmentView(trackSegment, layoutEditor);
            layoutEditor.addLayoutTrack(trackSegment, trackSegmentView);
        }
    }

    @AfterEach
    public void tearDown() {
        if (trackSegment != null) {
            trackSegmentView.dispose();
            trackSegment.remove();
        }

        if (layoutEditor != null) {
            EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
            efo.closeFrameWithConfirmations();
        }
        trackSegment = null;
        trackSegmentView = null;
        layoutEditor = null;

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackSegmentEditorTest.class);
}
