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
import jmri.util.JmriJFrame;

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

            JPanel panel20 = new JPanel();
            panel20.setLayout(new FlowLayout());
            JLabel arcLabel = new JLabel(Bundle.getMessage("SetArcAngle"));  // NOI18N
            panel20.add(arcLabel);
            arcLabel.setLabelFor(editTrackSegmentArcTextField);
            panel20.add(editTrackSegmentArcTextField);
            editTrackSegmentArcTextField.setToolTipText(Bundle.getMessage("SetArcAngleHint"));  // NOI18N
            contentPane.add(panel20);

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

        if (trackSegmentView.isArc() && trackSegmentView.isCircle()) {
            editTrackSegmentArcTextField.setText("" + trackSegmentView.getAngle());
            editTrackSegmentArcTextField.setEnabled(true);
        } else {
            editTrackSegmentArcTextField.setEnabled(false);
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
            JOptionPane.showMessageDialog(editTrackSegmentFrame,
                    Bundle.getMessage("Error1"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);  // NOI18N
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
