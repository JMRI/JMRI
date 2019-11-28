package jmri.jmrit.display.layoutEditor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.layoutEditor.LayoutTurntable.RayTrack;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Editors for all layout track objects (PositionablePoint, TrackSegment,
 * LayoutTurnout, LayoutSlip, LevelXing and LayoutTurntable).
 *
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutTrackEditors {

    private LayoutEditor layoutEditor = null;

    /**
     * constructor method
     */
    public LayoutTrackEditors(@Nonnull LayoutEditor layoutEditor) {
        this.layoutEditor = layoutEditor;
    }

    /*=================*\
    | Edit Layout Track |
    \*=================*/
    @InvokeOnGuiThread
    protected void editLayoutTrack(@Nonnull LayoutTrack layoutTrack) {
        sensorList.clear();
        if (layoutTrack instanceof PositionablePoint) {
            // PositionablePoint's don't have an editor...
        } else if (layoutTrack instanceof TrackSegment) {
            editTrackSegment((TrackSegment) layoutTrack);
        } else // this has to be before LayoutTurnout
        if (layoutTrack instanceof LayoutSlip) {
            editLayoutSlip((LayoutSlip) layoutTrack);
        } else if (layoutTrack instanceof LayoutTurnout) {
            editLayoutTurnout((LayoutTurnout) layoutTrack);
        } else if (layoutTrack instanceof LevelXing) {
            editLevelXing((LevelXing) layoutTrack);
        } else if (layoutTrack instanceof LayoutTurntable) {
            editLayoutTurntable((LayoutTurntable) layoutTrack);
        } else {
            log.error("editLayoutTrack unknown LayoutTrack subclass:" + layoutTrack.getClass().getName());  // NOI18N
        }
    }

    List<String> sensorList = new ArrayList<>();

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
        return;
    }

    /*==================*\
    | Edit Track Segment |
    \*==================*/
    // variables for Edit Track Segment pane
    private TrackSegment trackSegment;

    private JmriJFrame editTrackSegmentFrame = null;
    private JComboBox<String> editTrackSegmentMainlineComboBox = new JComboBox<String>();
//    private JCheckBox editTrackSegmentMainlineCheckBox = new JCheckBox(Bundle.getMessage("Mainline"));
    private JComboBox<String> editTrackSegmentDashedComboBox = new JComboBox<String>();
    private JCheckBox editTrackSegmentHiddenCheckBox = new JCheckBox(Bundle.getMessage("HideTrack"));  // NOI18N
    private NamedBeanComboBox<Block> editTrackSegmentBlockNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private JTextField editTrackSegmentArcTextField = new JTextField(5);
    private JButton editTrackSegmentSegmentEditBlockButton;

    private int editTrackSegmentMainlineTrackIndex;
    private int editTrackSegmentSideTrackIndex;
    private int editTrackSegmentDashedIndex;
    private int editTrackSegmentSolidIndex;
    private boolean editTrackSegmentOpen = false;
    private boolean editTrackSegmentNeedsRedraw = false;

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

    /**
     * Edit a Track Segment.
     */
    @InvokeOnGuiThread
    protected void editTrackSegment(@Nonnull TrackSegment trackSegment) {
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
            editTrackSegmentSegmentEditBlockButton.addActionListener((ActionEvent e) -> {
                editTrackSegmentEditBlockPressed(e);
            });
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

    /*===================*\
    | Edit Layout Turnout |
    \*===================*/
    // variables for Edit Layout Turnout pane
    private LayoutTurnout layoutTurnout = null;

    private JmriJFrame editLayoutTurnoutFrame = null;
    private NamedBeanComboBox<Turnout> editLayoutTurnout1stTurnoutComboBox = null;
    private NamedBeanComboBox<Turnout> editLayoutTurnout2ndTurnoutComboBox = null;
    private JLabel editLayoutTurnout2ndTurnoutLabel = null;
    private NamedBeanComboBox<Block> editLayoutTurnoutBlockNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private NamedBeanComboBox<Block> editLayoutTurnoutBlockBNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private NamedBeanComboBox<Block> editLayoutTurnoutBlockCNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private NamedBeanComboBox<Block> editLayoutTurnoutBlockDNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private JComboBox<String> editLayoutTurnoutStateComboBox = new JComboBox<String>();
    private JCheckBox editLayoutTurnoutHiddenCheckBox = null;
    private JButton editLayoutTurnoutBlockButton;
    private JButton editLayoutTurnoutBlockBButton;
    private JButton editLayoutTurnoutBlockCButton;
    private JButton editLayoutTurnoutBlockDButton;
    private JCheckBox editLayoutTurnout2ndTurnoutCheckBox = new JCheckBox(Bundle.getMessage("SupportingTurnout"));  // NOI18N
    private JCheckBox editLayoutTurnout2ndTurnoutInvertCheckBox = new JCheckBox(Bundle.getMessage("SecondTurnoutInvert"));  // NOI18N

    private boolean editLayoutTurnoutOpen = false;
    private boolean editLayoutTurnoutNeedRedraw = false;
    private boolean editLayoutTurnoutNeedsBlockUpdate = false;
    private int editLayoutTurnoutClosedIndex;
    private int editLayoutTurnoutThrownIndex;

    /**
     * Edit a Layout Turnout.
     */
    protected void editLayoutTurnout(@Nonnull LayoutTurnout layoutTurnout) {
        this.layoutTurnout = layoutTurnout;
        sensorList.clear();

        if (editLayoutTurnoutOpen) {
            editLayoutTurnoutFrame.setVisible(true);
        } else if (editLayoutTurnoutFrame == null) { // Initialize if needed
            editLayoutTurnoutFrame = new JmriJFrame(Bundle.getMessage("EditTurnout"), false, true);  // NOI18N

            editLayoutTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.EditLayoutTurnout", true);  // NOI18N
            editLayoutTurnoutFrame.setLocation(50, 30);
            Container contentPane = editLayoutTurnoutFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            // setup turnout name
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel turnoutNameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));  // NOI18N
            panel1.add(turnoutNameLabel);

            // add combobox to select turnout
            editLayoutTurnout1stTurnoutComboBox = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(TurnoutManager.class));
            editLayoutTurnout1stTurnoutComboBox.setToolTipText(Bundle.getMessage("EditTurnoutToolTip"));
            LayoutEditor.setupComboBox(editLayoutTurnout1stTurnoutComboBox, false, true, false);
            turnoutNameLabel.setLabelFor(editLayoutTurnout1stTurnoutComboBox);

            panel1.add(editLayoutTurnout1stTurnoutComboBox);
            contentPane.add(panel1);

            JPanel panel1a = new JPanel();
            panel1a.setLayout(new BoxLayout(panel1a, BoxLayout.Y_AXIS));

            editLayoutTurnout2ndTurnoutComboBox = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(TurnoutManager.class));
            editLayoutTurnout2ndTurnoutComboBox.setToolTipText(Bundle.getMessage("EditTurnoutToolTip"));
            LayoutEditor.setupComboBox(editLayoutTurnout2ndTurnoutComboBox, false, true, false);

            editLayoutTurnout2ndTurnoutCheckBox.addActionListener((ActionEvent e) -> {
                boolean additionalEnabled = editLayoutTurnout2ndTurnoutCheckBox.isSelected();
                editLayoutTurnout2ndTurnoutLabel.setEnabled(additionalEnabled);
                editLayoutTurnout2ndTurnoutComboBox.setEnabled(additionalEnabled);
                editLayoutTurnout2ndTurnoutInvertCheckBox.setEnabled(additionalEnabled);
            });
            panel1a.add(editLayoutTurnout2ndTurnoutCheckBox);
            contentPane.add(panel1a);

            editLayoutTurnout2ndTurnoutLabel = new JLabel(Bundle.getMessage("Supporting", Bundle.getMessage("BeanNameTurnout")));  // NOI18N
            editLayoutTurnout2ndTurnoutLabel.setEnabled(false);
            JPanel panel1b = new JPanel();
            panel1b.add(editLayoutTurnout2ndTurnoutLabel);
            editLayoutTurnout2ndTurnoutLabel.setLabelFor(editLayoutTurnout2ndTurnoutComboBox);
            panel1b.add(editLayoutTurnout2ndTurnoutComboBox);
            editLayoutTurnout2ndTurnoutInvertCheckBox.addActionListener((ActionEvent e) -> {
                layoutTurnout.setSecondTurnoutInverted(editLayoutTurnout2ndTurnoutInvertCheckBox.isSelected());
            });
            editLayoutTurnout2ndTurnoutInvertCheckBox.setEnabled(false);
            panel1b.add(editLayoutTurnout2ndTurnoutInvertCheckBox);
            contentPane.add(panel1b);

            // add continuing state choice, if not crossover
            if (!layoutTurnout.isTurnoutTypeXover()) {
                JPanel panel3 = new JPanel();
                panel3.setLayout(new FlowLayout());
                editLayoutTurnoutStateComboBox.removeAllItems();
                editLayoutTurnoutStateComboBox.addItem(InstanceManager.turnoutManagerInstance().getClosedText());
                editLayoutTurnoutClosedIndex = 0;
                editLayoutTurnoutStateComboBox.addItem(InstanceManager.turnoutManagerInstance().getThrownText());
                editLayoutTurnoutThrownIndex = 1;
                editLayoutTurnoutStateComboBox.setToolTipText(Bundle.getMessage("StateToolTip"));  // NOI18N
                JLabel label3 = new JLabel(Bundle.getMessage("ContinuingState"));
                panel3.add(label3);  // NOI18N
                label3.setLabelFor(editLayoutTurnoutStateComboBox);
                panel3.add(editLayoutTurnoutStateComboBox);
                contentPane.add(panel3);
            }

            JPanel panel3a = new JPanel();
            panel3a.setLayout(new FlowLayout());
            editLayoutTurnoutHiddenCheckBox = new JCheckBox(Bundle.getMessage("HideTurnout"));  // NOI18N
            editLayoutTurnoutHiddenCheckBox.setToolTipText(Bundle.getMessage("HiddenToolTip"));  // NOI18N
            panel3a.add(editLayoutTurnoutHiddenCheckBox);
            contentPane.add(panel3a);

            TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
            border.setTitle(Bundle.getMessage("BeanNameBlock"));  // NOI18N
            // setup block name
            JPanel panel2 = new JPanel();
            panel2.setBorder(border);
            panel2.setLayout(new FlowLayout());
            panel2.add(editLayoutTurnoutBlockNameComboBox);
            LayoutEditor.setupComboBox(editLayoutTurnoutBlockNameComboBox, false, true, true);
            editLayoutTurnoutBlockNameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));  // NOI18N
            panel2.add(editLayoutTurnoutBlockButton = new JButton(Bundle.getMessage("CreateEdit")));  // NOI18N
            editLayoutTurnoutBlockButton.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1  // NOI18N
            editLayoutTurnoutBlockButton.addActionListener((ActionEvent e) -> {
                editLayoutTurnoutEditBlockPressed(e);
            });
            contentPane.add(panel2);

            if (layoutTurnout.isTurnoutTypeXover()) {
                JPanel panel21 = new JPanel();
                panel21.setLayout(new FlowLayout());
                TitledBorder borderblk2 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                borderblk2.setTitle(Bundle.getMessage("BeanNameBlock") + " 2");  // NOI18N
                panel21.setBorder(borderblk2);
                LayoutEditor.setupComboBox(editLayoutTurnoutBlockBNameComboBox, false, true, true);
                editLayoutTurnoutBlockBNameComboBox.setToolTipText(Bundle.getMessage("EditBlockBNameHint"));  // NOI18N
                panel21.add(editLayoutTurnoutBlockBNameComboBox);

                panel21.add(editLayoutTurnoutBlockBButton = new JButton(Bundle.getMessage("CreateEdit")));  // NOI18N
                editLayoutTurnoutBlockBButton.addActionListener((ActionEvent e) -> {
                    editLayoutTurnoutEditBlockBPressed(e);
                });
                editLayoutTurnoutBlockBButton.setToolTipText(Bundle.getMessage("EditBlockHint", "2"));  // NOI18N
                contentPane.add(panel21);

                JPanel panel22 = new JPanel();
                panel22.setLayout(new FlowLayout());
                TitledBorder borderblk3 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                borderblk3.setTitle(Bundle.getMessage("BeanNameBlock") + " 3");  // NOI18N
                panel22.setBorder(borderblk3);
                LayoutEditor.setupComboBox(editLayoutTurnoutBlockCNameComboBox, false, true, true);
                editLayoutTurnoutBlockCNameComboBox.setToolTipText(Bundle.getMessage("EditBlockCNameHint"));  // NOI18N
                panel22.add(editLayoutTurnoutBlockCNameComboBox);
                panel22.add(editLayoutTurnoutBlockCButton = new JButton(Bundle.getMessage("CreateEdit")));  // NOI18N
                editLayoutTurnoutBlockCButton.addActionListener((ActionEvent e) -> {
                    editLayoutTurnoutEditBlockCPressed(e);
                });
                editLayoutTurnoutBlockCButton.setToolTipText(Bundle.getMessage("EditBlockHint", "3"));  // NOI18N
                contentPane.add(panel22);

                JPanel panel23 = new JPanel();
                panel23.setLayout(new FlowLayout());
                TitledBorder borderblk4 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                borderblk4.setTitle(Bundle.getMessage("BeanNameBlock") + " 4");  // NOI18N
                panel23.setBorder(borderblk4);
                LayoutEditor.setupComboBox(editLayoutTurnoutBlockDNameComboBox, false, true, true);
                editLayoutTurnoutBlockDNameComboBox.setToolTipText(Bundle.getMessage("EditBlockDNameHint"));  // NOI18N
                panel23.add(editLayoutTurnoutBlockDNameComboBox);
                panel23.add(editLayoutTurnoutBlockDButton = new JButton(Bundle.getMessage("CreateEdit")));  // NOI18N
                editLayoutTurnoutBlockDButton.addActionListener((ActionEvent e) -> {
                    editLayoutTurnoutEditBlockDPressed(e);
                });
                editLayoutTurnoutBlockDButton.setToolTipText(Bundle.getMessage("EditBlockHint", "4"));  // NOI18N
                contentPane.add(panel23);
            }
            // set up Edit Block, Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            // Edit Block

            addDoneCancelButtons(panel5, editLayoutTurnoutFrame.getRootPane(),
                    this::editLayoutTurnoutDonePressed, this::editLayoutTurnoutCancelPressed);
            contentPane.add(panel5);
        }

        // Set up for Edit
        if (layoutTurnout.isTurnoutTypeXover()) {
            editLayoutTurnoutFrame.setTitle(Bundle.getMessage("EditXover"));
            editLayoutTurnoutHiddenCheckBox.setText(Bundle.getMessage("HideXover"));
        } else {
            editLayoutTurnoutFrame.setTitle(Bundle.getMessage("EditTurnout"));
            editLayoutTurnoutHiddenCheckBox.setText(Bundle.getMessage("HideTurnout"));
        }
        editLayoutTurnoutHiddenCheckBox.setSelected(layoutTurnout.isHidden());

        List<Turnout> currentTurnouts = new ArrayList<>();
        currentTurnouts.add(layoutTurnout.getTurnout());
        currentTurnouts.add(layoutTurnout.getSecondTurnout());

        editLayoutTurnout1stTurnoutComboBox.setSelectedItem(layoutTurnout.getTurnout());
        editLayoutTurnout1stTurnoutComboBox.addPopupMenuListener(
                layoutEditor.newTurnoutComboBoxPopupMenuListener(editLayoutTurnout1stTurnoutComboBox, currentTurnouts));

        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        editLayoutTurnoutBlockNameComboBox.getEditor().setItem(bm.getBlock(layoutTurnout.getBlockName()));
        editLayoutTurnoutBlockNameComboBox.setEnabled(!hasNxSensorPairs(layoutTurnout.getLayoutBlock()));
        if (layoutTurnout.isTurnoutTypeXover()) {
            editLayoutTurnoutBlockBNameComboBox.getEditor().setItem(bm.getBlock(layoutTurnout.getBlockBName()));
            editLayoutTurnoutBlockCNameComboBox.getEditor().setItem(bm.getBlock(layoutTurnout.getBlockCName()));
            editLayoutTurnoutBlockDNameComboBox.getEditor().setItem(bm.getBlock(layoutTurnout.getBlockDName()));
            editLayoutTurnoutBlockBNameComboBox.setEnabled(!hasNxSensorPairs(layoutTurnout.getLayoutBlockB()));
            editLayoutTurnoutBlockCNameComboBox.setEnabled(!hasNxSensorPairs(layoutTurnout.getLayoutBlockC()));
            editLayoutTurnoutBlockDNameComboBox.setEnabled(!hasNxSensorPairs(layoutTurnout.getLayoutBlockD()));
        } else {
            editLayoutTurnout2ndTurnoutCheckBox.setText(Bundle.getMessage("ThrowTwoTurnouts"));  // NOI18N
        }

        boolean enable2nd = !layoutTurnout.getSecondTurnoutName().isEmpty();
        editLayoutTurnout2ndTurnoutCheckBox.setSelected(enable2nd);
        editLayoutTurnout2ndTurnoutInvertCheckBox.setEnabled(enable2nd);
        editLayoutTurnout2ndTurnoutLabel.setEnabled(enable2nd);
        editLayoutTurnout2ndTurnoutComboBox.setEnabled(enable2nd);
        if (enable2nd) {
            editLayoutTurnout2ndTurnoutInvertCheckBox.setSelected(layoutTurnout.isSecondTurnoutInverted());
            editLayoutTurnout2ndTurnoutComboBox.setSelectedItem(layoutTurnout.getSecondTurnout());
        } else {
            editLayoutTurnout2ndTurnoutInvertCheckBox.setSelected(false);
            editLayoutTurnout2ndTurnoutComboBox.setSelectedItem(null);
        }

        if (!layoutTurnout.isTurnoutTypeXover()) {
            if (layoutTurnout.getContinuingSense() == Turnout.CLOSED) {
                editLayoutTurnoutStateComboBox.setSelectedIndex(editLayoutTurnoutClosedIndex);
            } else {
                editLayoutTurnoutStateComboBox.setSelectedIndex(editLayoutTurnoutThrownIndex);
            }
        }

        editLayoutTurnoutFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                editLayoutTurnoutCancelPressed(null);
            }
        });
        editLayoutTurnoutFrame.pack();
        editLayoutTurnoutFrame.setVisible(true);
        editLayoutTurnoutOpen = true;
        editLayoutTurnoutNeedsBlockUpdate = false;

        showSensorMessage();
    }   // editLayoutTurnout

    private void editLayoutTurnoutEditBlockPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutTurnoutBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutTurnout.getBlockName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutTurnout.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editLayoutTurnoutNeedRedraw = true;
            editLayoutTurnoutNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (layoutTurnout.getLayoutBlock() == null) {
            JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
                    Bundle.getMessage("Error1"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);  // NOI18N
            return;
        }
        layoutTurnout.getLayoutBlock().editLayoutBlock(editLayoutTurnoutFrame);
        editLayoutTurnoutNeedRedraw = true;
        layoutEditor.setDirty();
    }

    private void editLayoutTurnoutEditBlockBPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutTurnoutBlockBNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutTurnout.getBlockBName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutTurnout.setLayoutBlockB(layoutEditor.provideLayoutBlock(newName));
            editLayoutTurnoutNeedRedraw = true;
            editLayoutTurnoutNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (layoutTurnout.getLayoutBlockB() == null) {
            JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
                    Bundle.getMessage("Error1"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);  // NOI18N
            return;
        }
        layoutTurnout.getLayoutBlockB().editLayoutBlock(editLayoutTurnoutFrame);
        editLayoutTurnoutNeedRedraw = true;
        layoutEditor.setDirty();
    }

    private void editLayoutTurnoutEditBlockCPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutTurnoutBlockCNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutTurnout.getBlockCName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutTurnout.setLayoutBlockC(layoutEditor.provideLayoutBlock(newName));
            editLayoutTurnoutNeedRedraw = true;
            editLayoutTurnoutNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (layoutTurnout.getLayoutBlockC() == null) {
            JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
                    Bundle.getMessage("Error1"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);  // NOI18N
            return;
        }
        layoutTurnout.getLayoutBlockC().editLayoutBlock(editLayoutTurnoutFrame);
        editLayoutTurnoutNeedRedraw = true;
        layoutEditor.setDirty();
    }

    private void editLayoutTurnoutEditBlockDPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutTurnoutBlockDNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutTurnout.getBlockDName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutTurnout.setLayoutBlockD(layoutEditor.provideLayoutBlock(newName));
            editLayoutTurnoutNeedRedraw = true;
            editLayoutTurnoutNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (layoutTurnout.getLayoutBlockD() == null) {
            JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
                    Bundle.getMessage("Error1"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);  // NOI18N
            return;
        }
        layoutTurnout.getLayoutBlockD().editLayoutBlock(editLayoutTurnoutFrame);
        editLayoutTurnoutNeedRedraw = true;
        layoutEditor.setDirty();
    }

    private void editLayoutTurnoutDonePressed(ActionEvent a) {
        // check if Turnout changed
        String newName = editLayoutTurnout1stTurnoutComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutTurnout.getTurnoutName().equals(newName)) {
            // turnout has changed
            if (layoutEditor.validatePhysicalTurnout(
                    newName, editLayoutTurnoutFrame)) {
                layoutTurnout.setTurnout(newName);
            } else {
                layoutTurnout.setTurnout(null);
                editLayoutTurnout1stTurnoutComboBox.setSelectedItem(null);
            }
            editLayoutTurnoutNeedRedraw = true;
        }

        if (editLayoutTurnout2ndTurnoutCheckBox.isSelected()) {
            newName = editLayoutTurnout2ndTurnoutComboBox.getSelectedItemDisplayName();
            if (newName == null) {
                newName = "";
            }
            if (!layoutTurnout.getSecondTurnoutName().equals(newName)) {
                if (layoutTurnout.isTurnoutTypeXover()) {
                    // turnout has changed
                    if (layoutEditor.validatePhysicalTurnout(
                            newName, editLayoutTurnoutFrame)) {
                        layoutTurnout.setSecondTurnout(newName);
                    } else {
                        editLayoutTurnout2ndTurnoutCheckBox.setSelected(false);
                        layoutTurnout.setSecondTurnout(null);
                        editLayoutTurnout2ndTurnoutComboBox.setSelectedItem(null);
                    }
                    editLayoutTurnoutNeedRedraw = true;
                } else {
                    layoutTurnout.setSecondTurnout(newName);
                }
            }
        } else {
            layoutTurnout.setSecondTurnout(null);
        }

        // set the continuing route Turnout State
        if ((layoutTurnout.getTurnoutType() == LayoutTurnout.RH_TURNOUT)
                || (layoutTurnout.getTurnoutType() == LayoutTurnout.LH_TURNOUT)
                || (layoutTurnout.getTurnoutType() == LayoutTurnout.WYE_TURNOUT)) {
            layoutTurnout.setContinuingSense(Turnout.CLOSED);
            if (editLayoutTurnoutStateComboBox.getSelectedIndex() == editLayoutTurnoutThrownIndex) {
                layoutTurnout.setContinuingSense(Turnout.THROWN);
            }
        }

        // check if Block changed
        newName = editLayoutTurnoutBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutTurnout.getBlockName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutTurnout.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editLayoutTurnoutNeedRedraw = true;
            editLayoutTurnoutNeedsBlockUpdate = true;
        }
        if (layoutTurnout.isTurnoutTypeXover()) {
            // check if Block 2 changed
            newName = editLayoutTurnoutBlockBNameComboBox.getSelectedItemDisplayName();
            if (newName == null) {
                newName = "";
            }
            if (!layoutTurnout.getBlockBName().equals(newName)) {
                // get new block, or null if block has been removed
                layoutTurnout.setLayoutBlockB(layoutEditor.provideLayoutBlock(newName));
                editLayoutTurnoutNeedRedraw = true;
                editLayoutTurnoutNeedsBlockUpdate = true;
            }
            // check if Block 3 changed
            newName = editLayoutTurnoutBlockCNameComboBox.getSelectedItemDisplayName();
            if (newName == null) {
                newName = "";
            }
            if (!layoutTurnout.getBlockCName().equals(newName)) {
                // get new block, or null if block has been removed
                layoutTurnout.setLayoutBlockC(layoutEditor.provideLayoutBlock(newName));
                editLayoutTurnoutNeedRedraw = true;
                editLayoutTurnoutNeedsBlockUpdate = true;
            }
            // check if Block 4 changed
            newName = editLayoutTurnoutBlockDNameComboBox.getSelectedItemDisplayName();
            if (newName == null) {
                newName = "";
            }
            if (!layoutTurnout.getBlockDName().equals(newName)) {
                // get new block, or null if block has been removed
                layoutTurnout.setLayoutBlockD(layoutEditor.provideLayoutBlock(newName));
                editLayoutTurnoutNeedRedraw = true;
                editLayoutTurnoutNeedsBlockUpdate = true;
            }
        }
        // set hidden
        boolean oldHidden = layoutTurnout.isHidden();
        layoutTurnout.setHidden(editLayoutTurnoutHiddenCheckBox.isSelected());
        if (oldHidden != layoutTurnout.isHidden()) {
            editLayoutTurnoutNeedRedraw = true;
        }
        editLayoutTurnoutOpen = false;
        editLayoutTurnoutFrame.setVisible(false);
        editLayoutTurnoutFrame.dispose();
        editLayoutTurnoutFrame = null;
        if (editLayoutTurnoutNeedsBlockUpdate) {
            layoutTurnout.updateBlockInfo();
            layoutTurnout.reCheckBlockBoundary();
        }
        if (editLayoutTurnoutNeedRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editLayoutTurnoutNeedRedraw = false;
        }
    }   // editLayoutTurnoutDonePressed

    private void editLayoutTurnoutCancelPressed(ActionEvent a) {
        editLayoutTurnoutOpen = false;
        editLayoutTurnoutFrame.setVisible(false);
        editLayoutTurnoutFrame.dispose();
        editLayoutTurnoutFrame = null;
        if (editLayoutTurnoutNeedsBlockUpdate) {
            layoutTurnout.updateBlockInfo();
        }
        if (editLayoutTurnoutNeedRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editLayoutTurnoutNeedRedraw = false;
        }
    }

    /*================*\
    | Edit Layout Slip |
    \*================*/
    // variables for Edit slip Crossing pane
    private LayoutSlip layoutSlip = null;

    private JmriJFrame editLayoutSlipFrame = null;
    private JButton editLayoutSlipBlockButton;
    private NamedBeanComboBox<Turnout> editLayoutSlipTurnoutAComboBox;
    private NamedBeanComboBox<Turnout> editLayoutSlipTurnoutBComboBox;
    private JCheckBox editLayoutSlipHiddenBox = new JCheckBox(Bundle.getMessage("HideSlip"));
    private NamedBeanComboBox<Block> editLayoutSlipBlockNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);

    private boolean editLayoutSlipOpen = false;
    private boolean editLayoutSlipNeedsRedraw = false;
    private boolean editLayoutSlipNeedsBlockUpdate = false;

    /**
     * Edit a Slip.
     */
    protected void editLayoutSlip(LayoutSlip layoutSlip) {
        sensorList.clear();

        this.layoutSlip = layoutSlip;
        if (editLayoutSlipOpen) {
            editLayoutSlipFrame.setVisible(true);
        } else if (editLayoutSlipFrame == null) {   // Initialize if needed
            editLayoutSlipFrame = new JmriJFrame(Bundle.getMessage("EditSlip"), false, true);  // NOI18N
            editLayoutSlipFrame.addHelpMenu("package.jmri.jmrit.display.EditLayoutSlip", true);  // NOI18N
            editLayoutSlipFrame.setLocation(50, 30);

            Container contentPane = editLayoutSlipFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

            // Setup turnout A
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel turnoutNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " A");  // NOI18N
            panel1.add(turnoutNameLabel);
            editLayoutSlipTurnoutAComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(TurnoutManager.class));
            editLayoutSlipTurnoutAComboBox.setToolTipText(Bundle.getMessage("EditTurnoutToolTip"));
            LayoutEditor.setupComboBox(editLayoutSlipTurnoutAComboBox, false, true, false);
            turnoutNameLabel.setLabelFor(editLayoutSlipTurnoutAComboBox);
            panel1.add(editLayoutSlipTurnoutAComboBox);
            contentPane.add(panel1);

            // Setup turnout B
            JPanel panel1a = new JPanel();
            panel1a.setLayout(new FlowLayout());
            JLabel turnoutBNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " B");  // NOI18N
            panel1a.add(turnoutBNameLabel);
            editLayoutSlipTurnoutBComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(TurnoutManager.class));
            editLayoutSlipTurnoutBComboBox.setToolTipText(Bundle.getMessage("EditTurnoutToolTip"));
            LayoutEditor.setupComboBox(editLayoutSlipTurnoutBComboBox, false, true, false);
            turnoutBNameLabel.setLabelFor(editLayoutSlipTurnoutBComboBox);
            panel1a.add(editLayoutSlipTurnoutBComboBox);

            contentPane.add(panel1a);

            JPanel panel2 = new JPanel();
            panel2.setLayout(new GridLayout(0, 3, 2, 2));

            panel2.add(new Label("   "));
            panel2.add(new Label(Bundle.getMessage("BeanNameTurnout") + " A:"));  // NOI18N
            panel2.add(new Label(Bundle.getMessage("BeanNameTurnout") + " B:"));  // NOI18N
            for (Map.Entry<Integer, LayoutSlip.TurnoutState> ts : layoutSlip.getTurnoutStates().entrySet()) {
                SampleStates draw = new SampleStates(ts.getKey());
                draw.repaint();
                draw.setPreferredSize(new Dimension(40, 40));
                panel2.add(draw);

                panel2.add(ts.getValue().getComboA());
                panel2.add(ts.getValue().getComboB());
            }

            testPanel = new TestState();
            testPanel.setSize(40, 40);
            testPanel.setPreferredSize(new Dimension(40, 40));
            panel2.add(testPanel);
            JButton testButton = new JButton("Test");  // NOI18N
            testButton.addActionListener((ActionEvent e) -> {
                toggleStateTest();
            });
            panel2.add(testButton);
            contentPane.add(panel2);

            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            editLayoutSlipHiddenBox.setToolTipText(Bundle.getMessage("HiddenToolTip"));  // NOI18N
            panel33.add(editLayoutSlipHiddenBox);
            contentPane.add(panel33);

            // setup block name
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel block1NameLabel = new JLabel(Bundle.getMessage("BlockID"));  // NOI18N
            panel3.add(block1NameLabel);
            block1NameLabel.setLabelFor(editLayoutSlipBlockNameComboBox);
            panel3.add(editLayoutSlipBlockNameComboBox);
            LayoutEditor.setupComboBox(editLayoutSlipBlockNameComboBox, false, true, true);
            editLayoutSlipBlockNameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));  // NOI18N

            contentPane.add(panel3);
            // set up Edit Block buttons
            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
            // Edit Block
            panel4.add(editLayoutSlipBlockButton = new JButton(Bundle.getMessage("EditBlock", "")));  // NOI18N
            editLayoutSlipBlockButton.addActionListener(
                    (ActionEvent event) -> {
                        editLayoutSlipEditBlockPressed(event);
                    }
            );
            editLayoutSlipBlockButton.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1  // NOI18N

            contentPane.add(panel4);

            // set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            addDoneCancelButtons(panel5, editLayoutSlipFrame.getRootPane(),
                    this::editLayoutSlipDonePressed, this::editLayoutSlipCancelPressed);
            contentPane.add(panel5);
        }

        editLayoutSlipHiddenBox.setSelected(layoutSlip.isHidden());

        // Set up for Edit
        List<Turnout> currentTurnouts = new ArrayList<>();
        currentTurnouts.add(layoutSlip.getTurnout());
        currentTurnouts.add(layoutSlip.getTurnoutB());

        editLayoutSlipTurnoutAComboBox.setSelectedItem(layoutSlip.getTurnout());
        editLayoutSlipTurnoutAComboBox.addPopupMenuListener(
                layoutEditor.newTurnoutComboBoxPopupMenuListener(editLayoutSlipTurnoutAComboBox, currentTurnouts));

        editLayoutSlipTurnoutBComboBox.setSelectedItem(layoutSlip.getTurnoutB());
        editLayoutSlipTurnoutBComboBox.addPopupMenuListener(
                layoutEditor.newTurnoutComboBoxPopupMenuListener(editLayoutSlipTurnoutBComboBox, currentTurnouts));

        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        editLayoutSlipBlockNameComboBox.getEditor().setItem(bm.getBlock(layoutSlip.getBlockName()));
        editLayoutSlipBlockNameComboBox.setEnabled(!hasNxSensorPairs(layoutSlip.getLayoutBlock()));

        editLayoutSlipFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                editLayoutSlipCancelPressed(null);
            }
        });
        editLayoutSlipFrame.pack();
        editLayoutSlipFrame.setVisible(true);
        editLayoutSlipOpen = true;
        editLayoutSlipNeedsBlockUpdate = false;

        showSensorMessage();
    }   // editLayoutSlip

    private void drawSlipState(Graphics2D g2, int state) {
        Point2D cenP = layoutSlip.getCoordsCenter();
        Point2D A = MathUtil.subtract(layoutSlip.getCoordsA(), cenP);
        Point2D B = MathUtil.subtract(layoutSlip.getCoordsB(), cenP);
        Point2D C = MathUtil.subtract(layoutSlip.getCoordsC(), cenP);
        Point2D D = MathUtil.subtract(layoutSlip.getCoordsD(), cenP);

        Point2D ctrP = new Point2D.Double(20.0, 20.0);
        A = MathUtil.add(MathUtil.normalize(A, 18.0), ctrP);
        B = MathUtil.add(MathUtil.normalize(B, 18.0), ctrP);
        C = MathUtil.add(MathUtil.normalize(C, 18.0), ctrP);
        D = MathUtil.add(MathUtil.normalize(D, 18.0), ctrP);

        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        g2.draw(new Line2D.Double(A, MathUtil.oneThirdPoint(A, C)));
        g2.draw(new Line2D.Double(C, MathUtil.oneThirdPoint(C, A)));

        if (state == LayoutTurnout.STATE_AC || state == LayoutTurnout.STATE_BD || state == LayoutTurnout.UNKNOWN) {
            g2.draw(new Line2D.Double(A, MathUtil.oneThirdPoint(A, D)));
            g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, A)));

            if (layoutSlip.getSlipType() == LayoutTurnout.DOUBLE_SLIP) {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, C)));
                g2.draw(new Line2D.Double(C, MathUtil.oneThirdPoint(C, B)));
            }
        } else {
            g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
            g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));
        }

        if (layoutSlip.getSlipType() == LayoutTurnout.DOUBLE_SLIP) {
            if (state == LayoutTurnout.STATE_AC) {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));

                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, C));
            } else if (state == LayoutTurnout.STATE_BD) {
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B, D));
            } else if (state == LayoutTurnout.STATE_AD) {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, C)));

                g2.draw(new Line2D.Double(C, MathUtil.oneThirdPoint(C, B)));

                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, D));
            } else if (state == LayoutTurnout.STATE_BC) {
                g2.draw(new Line2D.Double(A, MathUtil.oneThirdPoint(A, D)));

                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, A)));
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B, C));
            } else {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));
            }
        } else {
            g2.draw(new Line2D.Double(A, MathUtil.oneThirdPoint(A, D)));
            g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, A)));

            if (state == LayoutTurnout.STATE_AD) {
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, D));
            } else if (state == LayoutTurnout.STATE_AC) {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));

                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, C));
            } else if (state == LayoutTurnout.STATE_BD) {
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B, D));
            } else {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));
            }
        }
    }   // drawSlipState

    class SampleStates extends JPanel {

        // Methods, constructors, fields.
        SampleStates(int state) {
            super();
            this.state = state;
        }
        int state;

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);    // paints background
            if (g instanceof Graphics2D) {
                drawSlipState((Graphics2D) g, state);
            }
        }
    }

    private int testState = LayoutTurnout.UNKNOWN;

    /**
     * Toggle slip states if clicked on, physical turnout exists, and not
     * disabled.
     */
    public void toggleStateTest() {
        int turnAState;
        int turnBState;
        switch (testState) {
            default:
            case LayoutTurnout.STATE_AC: {
                testState = LayoutTurnout.STATE_AD;
                break;
            }

            case LayoutTurnout.STATE_BD: {
                if (layoutSlip.getSlipType() == LayoutTurnout.SINGLE_SLIP) {
                    testState = LayoutTurnout.STATE_AC;
                } else {
                    testState = LayoutTurnout.STATE_BC;
                }
                break;
            }

            case LayoutTurnout.STATE_AD: {
                testState = LayoutTurnout.STATE_BD;
                break;
            }

            case LayoutTurnout.STATE_BC: {
                testState = LayoutTurnout.STATE_AC;
                break;
            }
        }
        turnAState = layoutSlip.getTurnoutStates().get(testState).getTestTurnoutAState();
        turnBState = layoutSlip.getTurnoutStates().get(testState).getTestTurnoutBState();

        if (editLayoutSlipTurnoutAComboBox.getSelectedItem() != null) {
            editLayoutSlipTurnoutAComboBox.getSelectedItem().setCommandedState(turnAState);
        }
        if (editLayoutSlipTurnoutBComboBox.getSelectedItem() != null) {
            editLayoutSlipTurnoutBComboBox.getSelectedItem().setCommandedState(turnBState);
        }
        if (testPanel != null) {
            testPanel.repaint();
        }
    }

    class TestState extends JPanel {

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (g instanceof Graphics2D) {
                drawSlipState((Graphics2D) g, testState);
            }
        }
    }

    private TestState testPanel;

    private void editLayoutSlipEditBlockPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutSlipBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutSlip.getBlockName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutSlip.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editLayoutSlipNeedsRedraw = true;
            editLayoutSlipNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (layoutSlip.getLayoutBlock() == null) {
            JOptionPane.showMessageDialog(editLayoutSlipFrame,
                    Bundle.getMessage("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        layoutSlip.getLayoutBlock().editLayoutBlock(editLayoutSlipFrame);
        editLayoutSlipNeedsRedraw = true;
        layoutEditor.setDirty();
    }

    private void editLayoutSlipDonePressed(ActionEvent a) {
        String newName = editLayoutSlipTurnoutAComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutSlip.getTurnoutName().equals(newName)) {
            if (layoutEditor.validatePhysicalTurnout(newName, editLayoutSlipFrame)) {
                layoutSlip.setTurnout(newName);
            } else {
                layoutSlip.setTurnout("");
            }
            editLayoutSlipNeedsRedraw = true;
        }

        newName = editLayoutSlipTurnoutBComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutSlip.getTurnoutBName().equals(newName)) {
            if (layoutEditor.validatePhysicalTurnout(newName, editLayoutSlipFrame)) {
                layoutSlip.setTurnoutB(newName);
            } else {
                layoutSlip.setTurnoutB("");
            }
            editLayoutSlipNeedsRedraw = true;
        }

        newName = editLayoutSlipBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutSlip.getBlockName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutSlip.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editLayoutSlipNeedsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            editLayoutSlipNeedsBlockUpdate = true;
        }
        for (LayoutSlip.TurnoutState ts : layoutSlip.getTurnoutStates().values()) {
            ts.updateStatesFromCombo();
        }

        // set hidden
        boolean oldHidden = layoutSlip.isHidden();
        layoutSlip.setHidden(editLayoutSlipHiddenBox.isSelected());
        if (oldHidden != layoutSlip.isHidden()) {
            editLayoutSlipNeedsRedraw = true;
        }

        editLayoutSlipOpen = false;
        editLayoutSlipFrame.setVisible(false);
        editLayoutSlipFrame.dispose();
        editLayoutSlipFrame = null;
        if (editLayoutSlipNeedsBlockUpdate) {
            layoutSlip.updateBlockInfo();
        }
        if (editLayoutSlipNeedsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editLayoutSlipNeedsRedraw = false;
        }
    }

    private void editLayoutSlipCancelPressed(ActionEvent a) {
        editLayoutSlipOpen = false;
        editLayoutSlipFrame.setVisible(false);
        editLayoutSlipFrame.dispose();
        editLayoutSlipFrame = null;
        if (editLayoutSlipNeedsBlockUpdate) {
            layoutSlip.updateBlockInfo();
        }
        if (editLayoutSlipNeedsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editLayoutSlipNeedsRedraw = false;
        }
    }

    /*===============*\
    | Edit Level Xing |
    \*===============*/
    // variables for Edit Track Segment pane
    private LevelXing levelXing;

    // variables for Edit Level Crossing pane
    private JmriJFrame editLevelXingFrame = null;
    private JCheckBox editLevelXingHiddenCheckBox = new JCheckBox(Bundle.getMessage("HideCrossing"));  // NOI18N

    private NamedBeanComboBox<Block> editLevelXingBlock1NameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private NamedBeanComboBox<Block> editLevelXingBlock2NameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private JButton editLevelXingBlock1Button;
    private JButton editLevelXingBlock2Button;

    private boolean editLevelXingOpen = false;
    private boolean editLevelXingNeedsRedraw = false;
    private boolean editLevelXingNeedsBlockUpdate = false;

    /**
     * Edit a Level Crossing.
     */
    protected void editLevelXing(LevelXing levelXing) {
        sensorList.clear();

        this.levelXing = levelXing;
        if (editLevelXingOpen) {
            editLevelXingFrame.setVisible(true);
        } else // Initialize if needed
        if (editLevelXingFrame == null) {
            editLevelXingFrame = new JmriJFrame(Bundle.getMessage("EditXing"), false, true);  // NOI18N
            editLevelXingFrame.addHelpMenu("package.jmri.jmrit.display.EditLevelXing", true);  // NOI18N
            editLevelXingFrame.setLocation(50, 30);
            Container contentPane = editLevelXingFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            editLevelXingHiddenCheckBox.setToolTipText(Bundle.getMessage("HiddenToolTip"));  // NOI18N
            panel33.add(editLevelXingHiddenCheckBox);
            contentPane.add(panel33);

            // setup block AC name
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel block1NameLabel = new JLabel(Bundle.getMessage("Block_ID", "AC"));  // NOI18N
            panel1.add(block1NameLabel);
            block1NameLabel.setLabelFor(editLevelXingBlock1NameComboBox);
            panel1.add(editLevelXingBlock1NameComboBox);
            editLevelXingBlock1NameComboBox.setName("Block AC");
            LayoutEditor.setupComboBox(editLevelXingBlock1NameComboBox, false, true, true);
            editLevelXingBlock1NameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));  // NOI18N
            contentPane.add(panel1);

            // setup block BD name
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel block2NameLabel = new JLabel(Bundle.getMessage("Block_ID", "BD"));  // NOI18N
            panel2.add(block2NameLabel);
            block2NameLabel.setLabelFor(editLevelXingBlock2NameComboBox);
            panel2.add(editLevelXingBlock2NameComboBox);
            editLevelXingBlock2NameComboBox.setName("Block BD");
            LayoutEditor.setupComboBox(editLevelXingBlock2NameComboBox, false, true, true);
            editLevelXingBlock2NameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));  // NOI18N
            contentPane.add(panel2);

            // set up Edit 1 Block and Edit 2 Block buttons
            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
            // Edit 1 Block
            panel4.add(editLevelXingBlock1Button = new JButton(Bundle.getMessage("EditBlock", "AC")));  // NOI18N
            editLevelXingBlock1Button.addActionListener((ActionEvent e) -> {
                editLevelXingBlockACPressed(e);
            });
            editLevelXingBlock1Button.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1  // NOI18N
            // Edit 2 Block
            panel4.add(editLevelXingBlock2Button = new JButton(Bundle.getMessage("EditBlock", "BD")));  // NOI18N
            editLevelXingBlock2Button.addActionListener((ActionEvent e) -> {
                editLevelXingBlockBDPressed(e);
            });
            editLevelXingBlock2Button.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1  // NOI18N
            contentPane.add(panel4);

            // set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            addDoneCancelButtons(panel5, editLevelXingFrame.getRootPane(),
                    this::editLevelXingDonePressed, this::editLevelXingCancelPressed);
            contentPane.add(panel5);
        }

        editLevelXingHiddenCheckBox.setSelected(levelXing.isHidden());

        // Set up for Edit
        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        editLevelXingBlock1NameComboBox.getEditor().setItem(bm.getBlock(levelXing.getBlockNameAC()));
        editLevelXingBlock2NameComboBox.getEditor().setItem(bm.getBlock(levelXing.getBlockNameBD()));
        editLevelXingBlock1NameComboBox.setEnabled(!hasNxSensorPairs(levelXing.getLayoutBlockAC()));  // NOI18N
        editLevelXingBlock2NameComboBox.setEnabled(!hasNxSensorPairs(levelXing.getLayoutBlockBD()));  // NOI18N
        editLevelXingFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                editLevelXingCancelPressed(null);
            }
        });
        editLevelXingFrame.pack();
        editLevelXingFrame.setVisible(true);
        editLevelXingOpen = true;
        editLevelXingNeedsBlockUpdate = false;

        showSensorMessage();
    }   // editLevelXing

    private void editLevelXingBlockACPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLevelXingBlock1NameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!levelXing.getBlockNameAC().equals(newName)) {
            // get new block, or null if block has been removed
            levelXing.setLayoutBlockAC(layoutEditor.provideLayoutBlock(newName));
            editLevelXingNeedsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            editLevelXingNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (levelXing.getLayoutBlockAC() == null) {
            JOptionPane.showMessageDialog(editLevelXingFrame,
                    Bundle.getMessage("Error1"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);  // NOI18N
            return;
        }
        levelXing.getLayoutBlockAC().editLayoutBlock(editLevelXingFrame);
        editLevelXingNeedsRedraw = true;
    }

    private void editLevelXingBlockBDPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLevelXingBlock2NameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!levelXing.getBlockNameBD().equals(newName)) {
            // get new block, or null if block has been removed
            levelXing.setLayoutBlockBD(layoutEditor.provideLayoutBlock(newName));
            editLevelXingNeedsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            editLevelXingNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (levelXing.getLayoutBlockBD() == null) {
            JOptionPane.showMessageDialog(editLevelXingFrame,
                    Bundle.getMessage("Error1"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);  // NOI18N
            return;
        }
        levelXing.getLayoutBlockBD().editLayoutBlock(editLevelXingFrame);
        editLevelXingNeedsRedraw = true;
    }

    private void editLevelXingDonePressed(ActionEvent a) {
        // check if Blocks changed
        String newName = editLevelXingBlock1NameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!levelXing.getBlockNameAC().equals(newName)) {
            // get new block, or null if block has been removed
            levelXing.setLayoutBlockAC(layoutEditor.provideLayoutBlock(newName));
            editLevelXingNeedsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            editLevelXingNeedsBlockUpdate = true;
        }
        newName = editLevelXingBlock2NameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!levelXing.getBlockNameBD().equals(newName)) {
            // get new block, or null if block has been removed
            levelXing.setLayoutBlockBD(layoutEditor.provideLayoutBlock(newName));
            editLevelXingNeedsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            editLevelXingNeedsBlockUpdate = true;
        }

        // set hidden
        boolean oldHidden = levelXing.isHidden();
        levelXing.setHidden(editLevelXingHiddenCheckBox.isSelected());
        if (oldHidden != levelXing.isHidden()) {
            editLevelXingNeedsRedraw = true;
        }

        editLevelXingOpen = false;
        editLevelXingFrame.setVisible(false);
        editLevelXingFrame.dispose();
        editLevelXingFrame = null;
        if (editLevelXingNeedsBlockUpdate) {
            levelXing.updateBlockInfo();
        }
        if (editLevelXingNeedsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editLevelXingNeedsRedraw = false;
        }
    }

    private void editLevelXingCancelPressed(ActionEvent a) {
        editLevelXingOpen = false;
        editLevelXingFrame.setVisible(false);
        editLevelXingFrame.dispose();
        editLevelXingFrame = null;
        if (editLevelXingNeedsBlockUpdate) {
            levelXing.updateBlockInfo();
        }
        if (editLevelXingNeedsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editLevelXingNeedsRedraw = false;
        }
    }

    /*==============*\
    | Edit Turntable |
    \*==============*/
    // variables for Edit Turntable pane
    private LayoutTurntable layoutTurntable = null;

    private JmriJFrame editLayoutTurntableFrame = null;
    private JTextField editLayoutTurntableRadiusTextField = new JTextField(8);
    private JTextField editLayoutTurntableAngleTextField = new JTextField(8);

    private JPanel editLayoutTurntableRayPanel;
    private JButton editLayoutTurntableAddRayTrackButton;
    private JCheckBox editLayoutTurntableDccControlledCheckBox;

    private String editLayoutTurntableOldRadius = "";
    private boolean editLayoutTurntableOpen = false;
    private boolean editLayoutTurntableNeedsRedraw = false;

    private List<Turnout> turntableTurnouts = new ArrayList<>();

    /**
     * Edit a Turntable.
     */
    protected void editLayoutTurntable(LayoutTurntable layoutTurntable) {
        this.layoutTurntable = layoutTurntable;
        if (editLayoutTurntableOpen) {
            editLayoutTurntableFrame.setVisible(true);
        } else // Initialize if needed
        if (editLayoutTurntableFrame == null) {
            editLayoutTurntableFrame = new JmriJFrame(Bundle.getMessage("EditTurntable"), false, true);  // NOI18N
            editLayoutTurntableFrame.addHelpMenu("package.jmri.jmrit.display.EditTurntable", true);  // NOI18N
            editLayoutTurntableFrame.setLocation(50, 30);

            Container contentPane = editLayoutTurntableFrame.getContentPane();
            JPanel headerPane = new JPanel();
            JPanel footerPane = new JPanel();
            headerPane.setLayout(new BoxLayout(headerPane, BoxLayout.Y_AXIS));
            footerPane.setLayout(new BoxLayout(footerPane, BoxLayout.Y_AXIS));
            contentPane.setLayout(new BorderLayout());
            contentPane.add(headerPane, BorderLayout.NORTH);
            contentPane.add(footerPane, BorderLayout.SOUTH);

            // setup radius text field
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel radiusLabel = new JLabel(Bundle.getMessage("TurntableRadius"));  // NOI18N
            panel1.add(radiusLabel);
            radiusLabel.setLabelFor(editLayoutTurntableRadiusTextField);
            panel1.add(editLayoutTurntableRadiusTextField);
            editLayoutTurntableRadiusTextField.setToolTipText(Bundle.getMessage("TurntableRadiusHint"));  // NOI18N
            headerPane.add(panel1);

            // setup ray track angle text field
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel rayAngleLabel = new JLabel(Bundle.getMessage("RayAngle"));  // NOI18N
            panel2.add(rayAngleLabel);
            rayAngleLabel.setLabelFor(editLayoutTurntableAngleTextField);
            panel2.add(editLayoutTurntableAngleTextField);
            editLayoutTurntableAngleTextField.setToolTipText(Bundle.getMessage("RayAngleHint"));  // NOI18N
            headerPane.add(panel2);

            // setup add ray track button
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            panel3.add(editLayoutTurntableAddRayTrackButton = new JButton(Bundle.getMessage("AddRayTrack")));  // NOI18N
            editLayoutTurntableAddRayTrackButton.setToolTipText(Bundle.getMessage("AddRayTrackHint"));  // NOI18N
            editLayoutTurntableAddRayTrackButton.addActionListener((ActionEvent e) -> {
                addRayTrackPressed(e);
                updateRayPanel();
            });

            panel3.add(editLayoutTurntableDccControlledCheckBox = new JCheckBox(Bundle.getMessage("TurntableDCCControlled")));  // NOI18N
            headerPane.add(panel3);

            // set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            addDoneCancelButtons(panel5, editLayoutTurntableFrame.getRootPane(),
                    this::editLayoutTurntableDonePressed, this::turntableEditCancelPressed);
            footerPane.add(panel5);

            editLayoutTurntableRayPanel = new JPanel();
            editLayoutTurntableRayPanel.setLayout(new BoxLayout(editLayoutTurntableRayPanel, BoxLayout.Y_AXIS));
            JScrollPane rayScrollPane = new JScrollPane(editLayoutTurntableRayPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            contentPane.add(rayScrollPane, BorderLayout.CENTER);
        }
        editLayoutTurntableDccControlledCheckBox.setSelected(layoutTurntable.isTurnoutControlled());
        editLayoutTurntableDccControlledCheckBox.addActionListener((ActionEvent e) -> {
            layoutTurntable.setTurnoutControlled(editLayoutTurntableDccControlledCheckBox.isSelected());

            for (Component comp : editLayoutTurntableRayPanel.getComponents()) {
                if (comp instanceof TurntableRayPanel) {
                    TurntableRayPanel trp = (TurntableRayPanel) comp;
                    trp.showTurnoutDetails();
                }
            }
            editLayoutTurntableFrame.pack();
        });
        updateRayPanel();
        // Set up for Edit
        editLayoutTurntableRadiusTextField.setText(" " + layoutTurntable.getRadius());
        editLayoutTurntableOldRadius = editLayoutTurntableRadiusTextField.getText();
        editLayoutTurntableAngleTextField.setText("0");
        editLayoutTurntableFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                turntableEditCancelPressed(null);
            }
        });
        editLayoutTurntableFrame.pack();
        editLayoutTurntableFrame.setVisible(true);
        editLayoutTurntableOpen = true;
    }   // editLayoutTurntable

    //Remove old rays and add them back in
    private void updateRayPanel() {
        for (Component comp : editLayoutTurntableRayPanel.getComponents()) {
            editLayoutTurntableRayPanel.remove(comp);
        }

        // Create list of turnouts to be retained in the NamedBeanComboBox
        turntableTurnouts.clear();
        for (LayoutTurntable.RayTrack rt : layoutTurntable.getRayList()) {
            turntableTurnouts.add(rt.getTurnout());
        }

        editLayoutTurntableRayPanel.setLayout(new BoxLayout(editLayoutTurntableRayPanel, BoxLayout.Y_AXIS));
        for (LayoutTurntable.RayTrack rt : layoutTurntable.getRayList()) {
            editLayoutTurntableRayPanel.add(new TurntableRayPanel(rt));
        }
        editLayoutTurntableRayPanel.revalidate();
        editLayoutTurntableRayPanel.repaint();
        editLayoutTurntableFrame.pack();
    }

    private void saveRayPanelDetail() {
        for (Component comp : editLayoutTurntableRayPanel.getComponents()) {
            if (comp instanceof TurntableRayPanel) {
                TurntableRayPanel trp = (TurntableRayPanel) comp;
                trp.updateDetails();
            }
        }
    }

    private void addRayTrackPressed(ActionEvent a) {
        double ang = 0.0;
        try {
            ang = Float.parseFloat(editLayoutTurntableAngleTextField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(editLayoutTurntableFrame, Bundle.getMessage("EntryError") + ": " // NOI18N
                    + e + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        layoutTurntable.addRay(ang);
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
        editLayoutTurntableNeedsRedraw = false;
    }

    private void editLayoutTurntableDonePressed(ActionEvent a) {
        // check if new radius was entered
        String str = editLayoutTurntableRadiusTextField.getText();
        if (!str.equals(editLayoutTurntableOldRadius)) {
            double rad = 0.0;
            try {
                rad = Float.parseFloat(str);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(editLayoutTurntableFrame, Bundle.getMessage("EntryError") + ": " // NOI18N
                        + e + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            layoutTurntable.setRadius(rad);
            editLayoutTurntableNeedsRedraw = true;
        }
        // clean up
        editLayoutTurntableOpen = false;
        editLayoutTurntableFrame.setVisible(false);
        editLayoutTurntableFrame.dispose();
        editLayoutTurntableFrame = null;
        saveRayPanelDetail();
        if (editLayoutTurntableNeedsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editLayoutTurntableNeedsRedraw = false;
        }
    }

    private void turntableEditCancelPressed(ActionEvent a) {
        editLayoutTurntableOpen = false;
        editLayoutTurntableFrame.setVisible(false);
        editLayoutTurntableFrame.dispose();
        editLayoutTurntableFrame = null;
        if (editLayoutTurntableNeedsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editLayoutTurntableNeedsRedraw = false;
        }
    }

    /*===================*\
    | Turntable Ray Panel |
    \*===================*/
    public class TurntableRayPanel extends JPanel {

        // variables for Edit Turntable ray pane
        private RayTrack rayTrack = null;
        private JPanel rayTurnoutPanel;
        private transient NamedBeanComboBox<Turnout> turnoutNameComboBox;
        private TitledBorder rayTitledBorder;
        private JComboBox<String> rayTurnoutStateComboBox;
        private JLabel rayTurnoutStateLabel;
        private JTextField rayAngleTextField;
        private final int[] rayTurnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};
        private final DecimalFormat twoDForm = new DecimalFormat("#.00");

        /**
         * constructor method
         */
        public TurntableRayPanel(@Nonnull RayTrack rayTrack) {
            this.rayTrack = rayTrack;

            JPanel top = new JPanel();

            JLabel rayAngleLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("RayAngle")));
            top.add(rayAngleLabel);
            top.add(rayAngleTextField = new JTextField(5));
            rayAngleLabel.setLabelFor(rayAngleTextField);

            rayAngleTextField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        Float.parseFloat(rayAngleTextField.getText());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(editLayoutTurntableFrame, Bundle.getMessage("EntryError") + ": " // NOI18N
                                + ex + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"), // NOI18N
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            );
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(top);

            turnoutNameComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(TurnoutManager.class));
            turnoutNameComboBox.setToolTipText(Bundle.getMessage("EditTurnoutToolTip"));
            LayoutEditor.setupComboBox(turnoutNameComboBox, false, true, false);
            turnoutNameComboBox.setSelectedItem(rayTrack.getTurnout());
            turnoutNameComboBox.addPopupMenuListener(
                    layoutEditor.newTurnoutComboBoxPopupMenuListener(turnoutNameComboBox, turntableTurnouts));
            String turnoutStateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
            String turnoutStateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
            String[] turnoutStates = new String[]{turnoutStateClosed, turnoutStateThrown};

            rayTurnoutStateComboBox = new JComboBox<String>(turnoutStates);
            rayTurnoutStateLabel = new JLabel(Bundle.getMessage("TurnoutState"));  // NOI18N
            rayTurnoutPanel = new JPanel();

            rayTurnoutPanel.setBorder(new EtchedBorder());
            rayTurnoutPanel.add(turnoutNameComboBox);
            rayTurnoutPanel.add(rayTurnoutStateLabel);
            rayTurnoutStateLabel.setLabelFor(rayTurnoutStateComboBox);
            rayTurnoutPanel.add(rayTurnoutStateComboBox);
            if (rayTrack.getTurnoutState() == Turnout.CLOSED) {
                rayTurnoutStateComboBox.setSelectedItem(turnoutStateClosed);
            } else {
                rayTurnoutStateComboBox.setSelectedItem(turnoutStateThrown);
            }
            this.add(rayTurnoutPanel);

            JButton deleteRayButton;
            top.add(deleteRayButton = new JButton(Bundle.getMessage("ButtonDelete")));  // NOI18N
            deleteRayButton.setToolTipText(Bundle.getMessage("DeleteRayTrack"));  // NOI18N
            deleteRayButton.addActionListener((ActionEvent e) -> {
                delete();
                updateRayPanel();
            });
            rayTitledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));

            this.setBorder(rayTitledBorder);

            showTurnoutDetails();

            rayAngleTextField.setText(twoDForm.format(rayTrack.getAngle()));
            rayTitledBorder.setTitle(Bundle.getMessage("Ray") + " : " + rayTrack.getConnectionIndex());  // NOI18N
            if (rayTrack.getConnect() == null) {
                rayTitledBorder.setTitle(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("Unconnected")) + " "
                        + rayTrack.getConnectionIndex());  // NOI18N
            } else if (rayTrack.getConnect().getLayoutBlock() != null) {
                rayTitledBorder.setTitle(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("Connected")) + " "
                        + rayTrack.getConnect().getLayoutBlock().getDisplayName());  // NOI18N
            }
        }

        private void delete() {
            int n = JOptionPane.showConfirmDialog(null,
                    Bundle.getMessage("Question7"), // NOI18N
                    Bundle.getMessage("WarningTitle"), // NOI18N
                    JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                layoutTurntable.deleteRay(rayTrack);
            }
        }

        private void updateDetails() {
            if (turnoutNameComboBox == null || rayTurnoutStateComboBox == null) {
                return;
            }
            String turnoutName = turnoutNameComboBox.getSelectedItemDisplayName();
            if (turnoutName == null) {
                turnoutName = "";
            }
            rayTrack.setTurnout(turnoutName, rayTurnoutStateValues[rayTurnoutStateComboBox.getSelectedIndex()]);
            if (!rayAngleTextField.getText().equals(twoDForm.format(rayTrack.getAngle()))) {
                try {
                    double ang = Float.parseFloat(rayAngleTextField.getText());
                    rayTrack.setAngle(ang);
                } catch (Exception e) {
                    log.error("Angle is not in correct format so will skip " + rayAngleTextField.getText());  // NOI18N
                }
            }
        }

        private void showTurnoutDetails() {
            boolean vis = layoutTurntable.isTurnoutControlled();
            rayTurnoutPanel.setVisible(vis);
            turnoutNameComboBox.setVisible(vis);
            rayTurnoutStateComboBox.setVisible(vis);
            rayTurnoutStateLabel.setVisible(vis);
        }
    }   // class TurntableRayPanel

    private final static Logger log = LoggerFactory.getLogger(LayoutTrackEditors.class);

}
