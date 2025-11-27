package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.layoutEditor.*;
import jmri.swing.NamedBeanComboBox;
import jmri.tracktiles.NotATile;
import jmri.tracktiles.TrackTile;
import jmri.tracktiles.UnknownTile;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

/**
 * MVC Editor component for TrackSegment objects.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class TrackSegmentEditor extends LayoutTrackEditor {

    /**
     * constructor method.
     * @param layoutEditor main layout editor.
     */
    public TrackSegmentEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    // ********** Members and methods from LayoutTrackEditors 
    // ********** specific to TrackSegment

    // variables for Edit Track Segment pane
    private TrackSegmentView trackSegmentView;
    private TrackSegment trackSegment;

    private JmriJFrame editTrackSegmentFrame = null;
    private final JComboBox<String> editTrackSegmentMainlineComboBox = new JComboBox<>();
    private final JComboBox<String> editTrackSegmentDashedComboBox = new JComboBox<>();
    private final JCheckBox editTrackSegmentHiddenCheckBox = new JCheckBox(Bundle.getMessage("HideTrack"));  // NOI18N
    private final NamedBeanComboBox<Block> editTrackSegmentBlockNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private final JTextField editTrackSegmentArcTextField = new JTextField(5);
    private final JLabel editTrackSegmentConnect1OrientationLabel = new JLabel();
    private final JTextField editTrackSegmentConnect1OrientationField = new JTextField(8);
    private final JLabel editTrackSegmentConnect2OrientationLabel = new JLabel();
    private final JTextField editTrackSegmentConnect2OrientationField = new JTextField(8);
    private final JLabel editTrackSegmentTileInfoLabel = new JLabel();
    private JButton editTrackSegmentSegmentEditBlockButton;

    private int editTrackSegmentMainlineTrackIndex;
    private int editTrackSegmentSideTrackIndex;
    private int editTrackSegmentDashedIndex;
    private int editTrackSegmentSolidIndex;
    private boolean editTrackSegmentOpen = false;
    private boolean editTrackSegmentNeedsRedraw = false;


    /**
     * Edit a Track Segment.
     */
    @InvokeOnGuiThread
    @Override
    public void editLayoutTrack(@Nonnull LayoutTrackView layoutTrackView) {
        if ( layoutTrackView instanceof TrackSegmentView ) {
            this.trackSegmentView = (TrackSegmentView) layoutTrackView;
            this.trackSegment = this.trackSegmentView.getTrackSegment();
        } else {
            log.error("editLayoutTrack received type {} content {}", 
                    layoutTrackView.getClass(), layoutTrackView, 
                    new Exception("traceback"));
        }
        sensorList.clear();

        if (editTrackSegmentOpen) {
            editTrackSegmentFrame.setVisible(true);
        } else if (editTrackSegmentFrame == null) { // Initialize if needed
            editTrackSegmentFrame = new JmriJFrame(Bundle.getMessage("EditTrackSegment"), false, true); // key moved to DisplayBundle to be found by CircuitBuilder.java   // NOI18N
            editTrackSegmentFrame.addHelpMenu("package.jmri.jmrit.display.EditTrackSegment", true);  // NOI18N
            editTrackSegmentFrame.setLocation(50, 30);
            Container contentPane = editTrackSegmentFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

            // add dashed choice
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            editTrackSegmentDashedComboBox.removeAllItems();
            editTrackSegmentDashedComboBox.addItem(Bundle.getMessage("Solid"));  // NOI18N
            editTrackSegmentSolidIndex = 0;
            editTrackSegmentDashedComboBox.addItem(Bundle.getMessage("Dashed"));  // NOI18N
            editTrackSegmentDashedIndex = 1;
            editTrackSegmentDashedComboBox.setToolTipText(Bundle.getMessage("DashedToolTip"));  // NOI18N
            JLabel label31a = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Style")));
            panel31.add(label31a);
            label31a.setLabelFor(editTrackSegmentDashedComboBox);
            panel31.add(editTrackSegmentDashedComboBox);
            contentPane.add(panel31);

            // add mainline choice
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());

            editTrackSegmentMainlineComboBox.removeAllItems();
            editTrackSegmentMainlineComboBox.addItem(Bundle.getMessage("Mainline"));  // NOI18N
            editTrackSegmentMainlineTrackIndex = 0;
            editTrackSegmentMainlineComboBox.addItem(Bundle.getMessage("NotMainline"));  // NOI18N
            editTrackSegmentSideTrackIndex = 1;
            editTrackSegmentMainlineComboBox.setToolTipText(Bundle.getMessage("MainlineToolTip"));  // NOI18N
            editTrackSegmentMainlineComboBox.setName(Bundle.getMessage("Mainline"));
            panel32.add(editTrackSegmentMainlineComboBox);
            contentPane.add(panel32);

            // add hidden choice
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            editTrackSegmentHiddenCheckBox.setToolTipText(Bundle.getMessage("HiddenToolTip"));  // NOI18N
            panel33.add(editTrackSegmentHiddenCheckBox);
            contentPane.add(panel33);

            // setup block name
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel blockNameLabel = new JLabel(Bundle.getMessage("BlockID"));  // NOI18N
            panel2.add(blockNameLabel);
            blockNameLabel.setLabelFor(editTrackSegmentBlockNameComboBox);
            LayoutEditor.setupComboBox(editTrackSegmentBlockNameComboBox, false, true, true);
            editTrackSegmentBlockNameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));  // NOI18N

            panel2.add(editTrackSegmentBlockNameComboBox);

            contentPane.add(panel2);

            // add tile information display
            JPanel panelTile = new JPanel();
            panelTile.setLayout(new FlowLayout());
            JLabel tileLabel = new JLabel(Bundle.getMessage("TrackTile") + ": ");  // NOI18N
            panelTile.add(tileLabel);
            editTrackSegmentTileInfoLabel.setToolTipText(Bundle.getMessage("TrackTileInfoHint"));  // NOI18N
            panelTile.add(editTrackSegmentTileInfoLabel);
            contentPane.add(panelTile);

            JPanel panel20 = new JPanel();
            panel20.setLayout(new FlowLayout());
            JLabel arcLabel = new JLabel(Bundle.getMessage("SetArcAngle"));  // NOI18N
            panel20.add(arcLabel);
            arcLabel.setLabelFor(editTrackSegmentArcTextField);
            panel20.add(editTrackSegmentArcTextField);
            editTrackSegmentArcTextField.setToolTipText(Bundle.getMessage("SetArcAngleHint"));  // NOI18N
            contentPane.add(panel20);

            // Add orientation display fields (read-only)
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
            panel21.add(editTrackSegmentConnect1OrientationLabel);
            editTrackSegmentConnect1OrientationLabel.setLabelFor(editTrackSegmentConnect1OrientationField);
            editTrackSegmentConnect1OrientationField.setEditable(false);
            panel21.add(editTrackSegmentConnect1OrientationField);
            contentPane.add(panel21);

            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
            panel22.add(editTrackSegmentConnect2OrientationLabel);
            editTrackSegmentConnect2OrientationLabel.setLabelFor(editTrackSegmentConnect2OrientationField);
            editTrackSegmentConnect2OrientationField.setEditable(false);
            panel22.add(editTrackSegmentConnect2OrientationField);
            contentPane.add(panel22);

            // set up Edit Block, Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());

            // Edit Block
            panel5.add(editTrackSegmentSegmentEditBlockButton = new JButton(Bundle.getMessage("EditBlock", "")));  // NOI18N
            editTrackSegmentSegmentEditBlockButton.addActionListener(this::editTrackSegmentEditBlockPressed);
            editTrackSegmentSegmentEditBlockButton.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1  // NOI18N

            addDoneCancelButtons(panel5, editTrackSegmentFrame.getRootPane(),
                    this::editTracksegmentDonePressed, this::editTrackSegmentCancelPressed);
            contentPane.add(panel5);
        }
        // Set up for Edit
        if (trackSegment.isMainline()) {
            editTrackSegmentMainlineComboBox.setSelectedIndex(editTrackSegmentMainlineTrackIndex);
        } else {
            editTrackSegmentMainlineComboBox.setSelectedIndex(editTrackSegmentSideTrackIndex);
        }
        if (trackSegmentView.isDashed()) {
            editTrackSegmentDashedComboBox.setSelectedIndex(editTrackSegmentDashedIndex);
        } else {
            editTrackSegmentDashedComboBox.setSelectedIndex(editTrackSegmentSolidIndex);
        }
        editTrackSegmentHiddenCheckBox.setSelected(trackSegmentView.isHidden());
        Block block = InstanceManager.getDefault(BlockManager.class).getBlock(trackSegment.getBlockName());
        editTrackSegmentBlockNameComboBox.getEditor().setItem(block);   // Select the item via the editor, empty text field if null
        editTrackSegmentBlockNameComboBox.setEnabled(!hasNxSensorPairs(trackSegment.getLayoutBlock()));

        // Update tile information display
        TrackTile tile = trackSegment.getTrackTile();
        if (tile instanceof NotATile) {
            editTrackSegmentTileInfoLabel.setText(Bundle.getMessage("NoTile"));  // NOI18N
        } else if (tile instanceof UnknownTile) {
            editTrackSegmentTileInfoLabel.setText(Bundle.getMessage("UnknownTile") + " (" + tile.getVendor() + " / " + tile.getFamily() + " / " + tile.getPartCode() + ")");  // NOI18N
        } else {
            editTrackSegmentTileInfoLabel.setText(tile.getVendor() + " / " + tile.getFamily() + " / " + tile.getPartCode());
        }

        if (trackSegmentView.isArc() && trackSegmentView.isCircle()) {
            editTrackSegmentArcTextField.setText("" + trackSegmentView.getAngle());
            editTrackSegmentArcTextField.setEnabled(true);
            
            // Calculate and display tangent orientations at endpoints
            java.awt.geom.Point2D center = trackSegmentView.getCentre();
            java.awt.geom.Point2D connect1Point = layoutEditor.getCoords(trackSegment.getConnect1(), trackSegment.getType1());
            java.awt.geom.Point2D connect2Point = layoutEditor.getCoords(trackSegment.getConnect2(), trackSegment.getType2());
            
            if (center != null && connect1Point != null && connect2Point != null) {
                // Calculate radius angle from each point to center
                double radiusAngle1 = Math.toDegrees(Math.atan2(
                    center.getY() - connect1Point.getY(),
                    center.getX() - connect1Point.getX()));
                
                double radiusAngle2 = Math.toDegrees(Math.atan2(
                    center.getY() - connect2Point.getY(),
                    center.getX() - connect2Point.getX()));
                
                // The tangent is perpendicular to the radius
                double arc = trackSegmentView.getAngle();
                boolean isFlip = trackSegmentView.isFlip();
                boolean counterClockwise = (arc > 0) != isFlip;
                
                double tangent1, tangent2;
                if (counterClockwise) {
                    // CCW motion: tangent is 90° behind the radius
                    tangent1 = radiusAngle1 - 90.0;
                    tangent2 = radiusAngle2 - 90.0;
                } else {
                    // CW motion: tangent is 90° ahead of the radius
                    tangent1 = radiusAngle1 + 90.0;
                    tangent2 = radiusAngle2 + 90.0;
                }
                
                // Normalize to 0-360
                tangent1 = (tangent1 % 360 + 360) % 360;
                tangent2 = (tangent2 % 360 + 360) % 360;
                
                // Set labels with point IDs and coordinates
                String connect1Id = trackSegment.getConnect1().getId();
                String connect2Id = trackSegment.getConnect2().getId();
                editTrackSegmentConnect1OrientationLabel.setText(
                    String.format("%s (%.1f, %.1f) Orientation:", 
                        connect1Id, connect1Point.getX(), connect1Point.getY()));
                editTrackSegmentConnect2OrientationLabel.setText(
                    String.format("%s (%.1f, %.1f) Orientation:", 
                        connect2Id, connect2Point.getX(), connect2Point.getY()));
                
                // Display the tangent orientations
                editTrackSegmentConnect1OrientationField.setText(String.format("%.2f°", tangent1));
                editTrackSegmentConnect2OrientationField.setText(String.format("%.2f°", tangent2));
            } else {
                editTrackSegmentConnect1OrientationLabel.setText("Connect1 Orientation:");
                editTrackSegmentConnect2OrientationLabel.setText("Connect2 Orientation:");
                editTrackSegmentConnect1OrientationField.setText("-");
                editTrackSegmentConnect2OrientationField.setText("-");
            }
        } else {
            // Straight segment or not a circle
            editTrackSegmentArcTextField.setEnabled(false);
            
            // For straight segments, calculate orientation from the two endpoints
            java.awt.geom.Point2D connect1Point = layoutEditor.getCoords(trackSegment.getConnect1(), trackSegment.getType1());
            java.awt.geom.Point2D connect2Point = layoutEditor.getCoords(trackSegment.getConnect2(), trackSegment.getType2());
            
            if (connect1Point != null && connect2Point != null) {
                // Calculate orientation (angle from connect1 to connect2)
                double orientation = Math.toDegrees(Math.atan2(
                    connect2Point.getY() - connect1Point.getY(),
                    connect2Point.getX() - connect1Point.getX()));
                orientation = (orientation % 360 + 360) % 360; // Normalize to 0-360
                
                // For straight segments, orientation is the same at both ends
                String connect1Id = trackSegment.getConnect1().getId();
                String connect2Id = trackSegment.getConnect2().getId();
                editTrackSegmentConnect1OrientationLabel.setText(
                    String.format("%s (%.1f, %.1f) Orientation:", 
                        connect1Id, connect1Point.getX(), connect1Point.getY()));
                editTrackSegmentConnect2OrientationLabel.setText(
                    String.format("%s (%.1f, %.1f) Orientation:", 
                        connect2Id, connect2Point.getX(), connect2Point.getY()));
                
                editTrackSegmentConnect1OrientationField.setText(String.format("%.2f°", orientation));
                editTrackSegmentConnect2OrientationField.setText(String.format("%.2f°", orientation));
            } else {
                editTrackSegmentConnect1OrientationLabel.setText("Connect1 Orientation:");
                editTrackSegmentConnect2OrientationLabel.setText("Connect2 Orientation:");
                editTrackSegmentConnect1OrientationField.setText("-");
                editTrackSegmentConnect2OrientationField.setText("-");
            }
        }

        editTrackSegmentFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                editTrackSegmentCancelPressed(null);
            }
        });
        editTrackSegmentFrame.pack();
        editTrackSegmentFrame.setVisible(true);
        editTrackSegmentOpen = true;

        showSensorMessage();
    }   // editTrackSegment

    @InvokeOnGuiThread
    private void editTrackSegmentEditBlockPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editTrackSegmentBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if ((trackSegment.getBlockName().isEmpty())
                || !trackSegment.getBlockName().equals(newName)) {
            // get new block, or null if block has been removed
            trackSegment.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editTrackSegmentNeedsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            trackSegment.updateBlockInfo();
        }
        // check if a block exists to edit
        if (trackSegment.getLayoutBlock() == null) {
            JmriJOptionPane.showMessageDialog(editTrackSegmentFrame,
                    Bundle.getMessage("Error1"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);  // NOI18N
            return;
        }
        trackSegment.getLayoutBlock().editLayoutBlock(editTrackSegmentFrame);
        layoutEditor.setDirty();
        editTrackSegmentNeedsRedraw = true;
    }

    @InvokeOnGuiThread
    private void editTracksegmentDonePressed(ActionEvent a) {
        // set dashed
        boolean oldDashed = trackSegmentView.isDashed();
        trackSegmentView.setDashed(editTrackSegmentDashedComboBox.getSelectedIndex() == editTrackSegmentDashedIndex);

        // set mainline
        boolean oldMainline = trackSegment.isMainline();
        trackSegment.setMainline(editTrackSegmentMainlineComboBox.getSelectedIndex() == editTrackSegmentMainlineTrackIndex);

        // set hidden
        boolean oldHidden = trackSegmentView.isHidden();
        trackSegmentView.setHidden(editTrackSegmentHiddenCheckBox.isSelected());

        if (trackSegmentView.isArc()) {
            try {
                double newAngle = Double.parseDouble(editTrackSegmentArcTextField.getText());
                trackSegmentView.setAngle(newAngle);
                editTrackSegmentNeedsRedraw = true;
            } catch (NumberFormatException e) {
                editTrackSegmentArcTextField.setText("" + trackSegmentView.getAngle());
            }
        }
        // check if anything changed
        if ((oldDashed != trackSegmentView.isDashed())
                || (oldMainline != trackSegment.isMainline())
                || (oldHidden != trackSegmentView.isHidden())) {
            editTrackSegmentNeedsRedraw = true;
        }
        // check if Block changed
        String newName = editTrackSegmentBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if ((trackSegment.getBlockName().isEmpty())
                || !trackSegment.getBlockName().equals(newName)) {
            // get new block, or null if block has been removed
            trackSegment.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editTrackSegmentNeedsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            trackSegment.updateBlockInfo();
        }
        editTrackSegmentOpen = false;

        editTrackSegmentFrame.setVisible(false);
        editTrackSegmentFrame.dispose();
        editTrackSegmentFrame = null;

        if (editTrackSegmentNeedsRedraw) {
            layoutEditor.redrawPanel();
            editTrackSegmentNeedsRedraw = false;
        }
        layoutEditor.setDirty();
    }

    @InvokeOnGuiThread
    private void editTrackSegmentCancelPressed(ActionEvent a) {
        editTrackSegmentOpen = false;
        editTrackSegmentFrame.setVisible(false);
        editTrackSegmentFrame.dispose();
        editTrackSegmentFrame = null;
        if (editTrackSegmentNeedsRedraw) {
            layoutEditor.setDirty();
            layoutEditor.redrawPanel();
            editTrackSegmentNeedsRedraw = false;
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackSegmentEditor.class);
}
