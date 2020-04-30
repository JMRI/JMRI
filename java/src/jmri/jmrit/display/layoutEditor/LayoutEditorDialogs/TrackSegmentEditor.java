package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import java.util.*;
import java.util.List;

import javax.annotation.*;
import javax.swing.*;
import javax.swing.border.*;

import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.layoutEditor.*;
import jmri.swing.NamedBeanComboBox;
import jmri.util.*;

/**
 * MVC Editor component for TrackSegment objects.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class TrackSegmentEditor extends LayoutTrackEditor {

    /**
     * constructor method
     */
    public TrackSegmentEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    // ********** Members and methods found elsewhere in LayoutTrackEditors 
    // ********** Promote to a superclass?

    List<String> sensorList = new ArrayList<>();

    /**
     * Display a message describing the reason for the block selection combo box
     * being disabled. An option is provided to hide the message. Note: The
     * PanelMenu class is being used to satisfy the showInfoMessage requirement
     * for a default manager type class.
     *
     * @since 4.11.2
     */
    @InvokeOnGuiThread
    void showSensorMessage() {
        if (sensorList.isEmpty()) {
            return;
        }
        StringBuilder msg = new StringBuilder(Bundle.getMessage("BlockSensorLine1"));  // NOI18N
        msg.append(Bundle.getMessage("BlockSensorLine2"));  // NOI18N
        String chkDup = "";
        sensorList.sort(null);
        for (String sName : sensorList) {
            if (!sName.equals(chkDup)) {
                msg.append("<br>&nbsp;&nbsp;&nbsp; " + sName);  // NOI18N
            }
            chkDup = sName;
        }
        msg.append("<br>&nbsp;</html>");  // NOI18N
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                showInfoMessage(
                        Bundle.getMessage("BlockSensorTitle"), // NOI18N
                        msg.toString(),
                        "jmri.jmrit.display.PanelMenu", // NOI18N
                        "BlockSensorMessage");  // NOI18N
    }


    /**
     * Create a list of NX sensors that refer to the current layout block. This
     * is used to disable block selection in the edit dialog. The list is built
     * by {@link jmri.jmrit.entryexit.EntryExitPairs#layoutBlockSensors}.
     *
     * @since 4.11.2
     * @param loBlk The current layout block.
     * @return true if sensors are affected.
     */
    boolean hasNxSensorPairs(LayoutBlock loBlk) {
        if (loBlk == null) {
            return false;
        }
        List<String> blockSensors = InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class)
                .layoutBlockSensors(loBlk);
        if (blockSensors.isEmpty()) {
            return false;
        }
        sensorList.addAll(blockSensors);
        return true;
    }

    private void addDoneCancelButtons(JPanel target, JRootPane rp, ActionListener doneCallback, ActionListener cancelCallback) {
        // Done
        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        target.add(doneButton);  // NOI18N
        doneButton.addActionListener(doneCallback);
        doneButton.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));  // NOI18N

        // Cancel
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel")); // NOI18N
        target.add(cancelButton);
        cancelButton.addActionListener(cancelCallback);
        cancelButton.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));  // NOI18N

        rp.setDefaultButton(doneButton);
        // bind ESC to close window
        rp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close"); // NOI18N
    }

    // ********** Members and methods from LayoutTrackEditors 
    // ********** specific to TrackSegment

    // variables for Edit Track Segment pane
    private TrackSegment trackSegment;

    private JmriJFrame editTrackSegmentFrame = null;
    private final JComboBox<String> editTrackSegmentMainlineComboBox = new JComboBox<>();
//    private JCheckBox editTrackSegmentMainlineCheckBox = new JCheckBox(Bundle.getMessage("Mainline"));
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
    public void editTrackSegment(@Nonnull TrackSegment trackSegment) {
        this.trackSegment = trackSegment;
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
        if (trackSegment.isDashed()) {
            editTrackSegmentDashedComboBox.setSelectedIndex(editTrackSegmentDashedIndex);
        } else {
            editTrackSegmentDashedComboBox.setSelectedIndex(editTrackSegmentSolidIndex);
        }
        editTrackSegmentHiddenCheckBox.setSelected(trackSegment.isHidden());
        Block block = InstanceManager.getDefault(BlockManager.class).getBlock(trackSegment.getBlockName());
        editTrackSegmentBlockNameComboBox.getEditor().setItem(block);   // Select the item via the editor, empty text field if null
        editTrackSegmentBlockNameComboBox.setEnabled(!hasNxSensorPairs(trackSegment.getLayoutBlock()));

        if (trackSegment.isArc() && trackSegment.isCircle()) {
            editTrackSegmentArcTextField.setText("" + trackSegment.getAngle());
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
        boolean oldDashed = trackSegment.isDashed();
        trackSegment.setDashed(editTrackSegmentDashedComboBox.getSelectedIndex() == editTrackSegmentDashedIndex);

        // set mainline
        boolean oldMainline = trackSegment.isMainline();
        trackSegment.setMainline(editTrackSegmentMainlineComboBox.getSelectedIndex() == editTrackSegmentMainlineTrackIndex);

        // set hidden
        boolean oldHidden = trackSegment.isHidden();
        trackSegment.setHidden(editTrackSegmentHiddenCheckBox.isSelected());

        if (trackSegment.isArc()) {
            try {
                double newAngle = Double.parseDouble(editTrackSegmentArcTextField.getText());
                trackSegment.setAngle(newAngle);
                editTrackSegmentNeedsRedraw = true;
            } catch (NumberFormatException e) {
                editTrackSegmentArcTextField.setText("" + trackSegment.getAngle());
            }
        }
        // check if anything changed
        if ((oldDashed != trackSegment.isDashed())
                || (oldMainline != trackSegment.isMainline())
                || (oldHidden != trackSegment.isHidden())) {
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
