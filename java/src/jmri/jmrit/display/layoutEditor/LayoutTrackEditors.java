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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.LayoutTurntable.RayTrack;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import jmri.util.swing.JmriBeanComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Editors for all layout track objects (PositionablePoint, TrackSegment,
 * LayoutTurnout, LayoutSlip, LevelXing and LayoutTurntable)
 *
 * @author George Warner Copyright (c) 2017
 */
public class LayoutTrackEditors {

    private LayoutEditor layoutEditor = null;

    /**
     * constructor method
     */
    public LayoutTrackEditors(@Nonnull LayoutEditor layoutEditor) {
        this.layoutEditor = layoutEditor;
    }

    /*==================*\
    | Edit Track Segment |
    \*==================*/
    // variables for Edit Track Segment pane
    private TrackSegment trackSegment;

    private JmriJFrame editTrackSegmentFrame = null;

    private JComboBox<String> editTrackSegmentMainlineComboBox = new JComboBox<String>();

    private JComboBox<String> editTrackSegmentDashedComboBox = new JComboBox<String>();

    private JCheckBox editTrackSegmentHiddenCheckBox = new JCheckBox(Bundle.getMessage("HideTrack"));

    private JmriBeanComboBox editTrackSegmentBlockNameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JTextField editTrackSegmentArcTextField = new JTextField(5);
    private JButton editTrackSegmentSegmentEditBlockButton;
    private JButton editTrackSegmentSegmentEditDoneButton;
    private JButton editTrackSegmentSegmentEditCancelButton;

    private int editTrackSegmentMainlineTrackIndex;
    private int editTrackSegmentSideTrackIndex;
    private int editTrackSegmentDashedIndex;
    private int editTrackSegmentSolidIndex;
    private boolean editTrackSegmentOpen = false;
    private boolean editTrackSegmentNeedsRedraw = false;

    /**
     * Edit a Track Segment.
     */
    protected void editTrackSegment(@Nonnull TrackSegment trackSegment) {
        this.trackSegment = trackSegment;

        if (editTrackSegmentOpen) {
            editTrackSegmentFrame.setVisible(true);
        } else if (editTrackSegmentFrame == null) { // Initialize if needed
            editTrackSegmentFrame = new JmriJFrame(Bundle.getMessage("EditTrackSegment"), false, true); // key moved to DisplayBundle to be found by CircuitBuilder.java
            editTrackSegmentFrame.addHelpMenu("package.jmri.jmrit.display.EditTrackSegment", true);
            editTrackSegmentFrame.setLocation(50, 30);
            Container contentPane = editTrackSegmentFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

            // add dashed choice
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            editTrackSegmentDashedComboBox.removeAllItems();
            editTrackSegmentDashedComboBox.addItem(Bundle.getMessage("Solid"));
            editTrackSegmentSolidIndex = 0;
            editTrackSegmentDashedComboBox.addItem(Bundle.getMessage("Dashed"));
            editTrackSegmentDashedIndex = 1;
            editTrackSegmentDashedComboBox.setToolTipText(Bundle.getMessage("DashedToolTip"));
            panel31.add(new JLabel(Bundle.getMessage("Style") + " : "));
            panel31.add(editTrackSegmentDashedComboBox);
            contentPane.add(panel31);

            // add mainline choice
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            editTrackSegmentMainlineComboBox.removeAllItems();
            editTrackSegmentMainlineComboBox.addItem(Bundle.getMessage("Mainline"));
            editTrackSegmentMainlineTrackIndex = 0;
            editTrackSegmentMainlineComboBox.addItem(Bundle.getMessage("NotMainline"));
            editTrackSegmentSideTrackIndex = 1;
            editTrackSegmentMainlineComboBox.setToolTipText(Bundle.getMessage("MainlineToolTip"));
            panel32.add(editTrackSegmentMainlineComboBox);
            contentPane.add(panel32);

            // add hidden choice
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            editTrackSegmentHiddenCheckBox.setToolTipText(Bundle.getMessage("HiddenToolTip"));
            panel33.add(editTrackSegmentHiddenCheckBox);
            contentPane.add(panel33);

            // setup block name
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel blockNameLabel = new JLabel(Bundle.getMessage("BlockID"));
            panel2.add(blockNameLabel);
            LayoutEditor.setupComboBox(editTrackSegmentBlockNameComboBox, false, true);
            editTrackSegmentBlockNameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));
            panel2.add(editTrackSegmentBlockNameComboBox);

            contentPane.add(panel2);

            JPanel panel20 = new JPanel();
            panel20.setLayout(new FlowLayout());
            JLabel arcLabel = new JLabel("Set Arc Angle");
            panel20.add(arcLabel);
            panel20.add(editTrackSegmentArcTextField);
            editTrackSegmentArcTextField.setToolTipText("Set Arc Angle");
            contentPane.add(panel20);

            // set up Edit Block, Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());

            // Edit Block
            panel5.add(editTrackSegmentSegmentEditBlockButton = new JButton(Bundle.getMessage("EditBlock", "")));
            editTrackSegmentSegmentEditBlockButton.addActionListener((ActionEvent e) -> {
                segmentEditBlockPressed(e);
            });
            editTrackSegmentSegmentEditBlockButton.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1
            panel5.add(editTrackSegmentSegmentEditDoneButton = new JButton(Bundle.getMessage("ButtonDone")));
            editTrackSegmentSegmentEditDoneButton.addActionListener((ActionEvent e) -> {
                segmentEditDonePressed(e);
            });
            editTrackSegmentSegmentEditDoneButton.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(editTrackSegmentSegmentEditDoneButton);
                rootPane.setDefaultButton(editTrackSegmentSegmentEditDoneButton);
            });

            // Cancel
            panel5.add(editTrackSegmentSegmentEditCancelButton = new JButton(Bundle.getMessage("ButtonCancel")));
            editTrackSegmentSegmentEditCancelButton.addActionListener((ActionEvent e) -> {
                segmentEditCancelPressed(e);
            });
            editTrackSegmentSegmentEditCancelButton.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
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
        editTrackSegmentBlockNameComboBox.setText(trackSegment.getBlockName());

        if (trackSegment.isArc() && trackSegment.isCircle()) {
            editTrackSegmentArcTextField.setText("" + trackSegment.getAngle());
            editTrackSegmentArcTextField.setEnabled(true);
        } else {
            editTrackSegmentArcTextField.setEnabled(false);
        }

        editTrackSegmentFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                segmentEditCancelPressed(null);
            }
        });
        editTrackSegmentFrame.pack();
        editTrackSegmentFrame.setVisible(true);
        editTrackSegmentOpen = true;
    }

    void segmentEditBlockPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editTrackSegmentBlockNameComboBox.getUserName();
        if (!trackSegment.getBlockName().equals(newName)) {
            // block has changed, if old block exists, decrement use
            if (trackSegment.getLayoutBlock() != null) {
                trackSegment.getLayoutBlock().decrementUse();
            }
            // get new block, or null if block has been removed
            try {
                trackSegment.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            } catch (IllegalArgumentException ex) {
                trackSegment.setLayoutBlock(null);
            }
            editTrackSegmentNeedsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            trackSegment.updateBlockInfo();
        }
        // check if a block exists to edit
        if (trackSegment.getLayoutBlock() == null) {
            JOptionPane.showMessageDialog(editTrackSegmentFrame,
                    Bundle.getMessage("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        trackSegment.getLayoutBlock().editLayoutBlock(editTrackSegmentFrame);
        layoutEditor.setDirty();
        editTrackSegmentNeedsRedraw = true;
    }

    void segmentEditDonePressed(ActionEvent a) {
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
        String newName = editTrackSegmentBlockNameComboBox.getUserName();
        if ((trackSegment.getBlockName() == null)
                || !trackSegment.getBlockName().equals(newName)) {
            // block has changed, if old block exists, decrement use
            if (trackSegment.getLayoutBlock() != null) {
                trackSegment.getLayoutBlock().decrementUse();
            }
            // get new block, or null if block has been removed
            try {
                trackSegment.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            } catch (IllegalArgumentException ex) {
                trackSegment.setLayoutBlock(null);
            }
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

    void segmentEditCancelPressed(ActionEvent a) {
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
    private JmriBeanComboBox editLayoutTurnout1stTurnoutComboBox = null;
    private JmriBeanComboBox editLayoutTurnout2ndTurnoutComboBox = null;
    private JLabel editLayoutTurnout2ndTurnoutLabel = null;
    private JmriBeanComboBox editLayoutTurnoutBlockNameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox editLayoutTurnoutBlockBNameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox editLayoutTurnoutBlockCNameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox editLayoutTurnoutBlockDNameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JComboBox<String> editLayoutTurnoutStateComboBox = new JComboBox<String>();
    private JCheckBox editLayoutTurnoutHiddenCheckBox = new JCheckBox(Bundle.getMessage("HideTurnout"));
    private JButton editLayoutTurnoutBlockButton;
    private JButton editLayoutTurnoutDoneButton;
    private JButton editLayoutTurnoutCancelButton;
    private JButton editLayoutTurnoutBlockBButton;
    private JButton editLayoutTurnoutBlockCButton;
    private JButton editLayoutTurnoutBlockDButton;
    private JCheckBox editLayoutTurnout2ndTurnoutCheckBox = new JCheckBox(Bundle.getMessage("SupportingTurnout"));
    private JCheckBox editLayoutTurnout2ndTurnoutInvertCheckBox = new JCheckBox(Bundle.getMessage("SecondTurnoutInvert"));

    private boolean editLayoutTurnoutOpen = false;
    private boolean editLayoutTurnoutNeedRedraw = false;
    private boolean editLayoutTurnoutNeedsBlockUpdate = false;
    private int editLayoutTurnoutClosedIndex;
    private int editLayoutTurnoutThrownIndex;

    /**
     * Edit a Layout Turnout
     */
    protected void editLayoutTurnout(@Nonnull LayoutTurnout layoutTurnout) {
        this.layoutTurnout = layoutTurnout;

        if (editLayoutTurnoutOpen) {
            editLayoutTurnoutFrame.setVisible(true);
        } else if (editLayoutTurnoutFrame == null) { // Initialize if needed
            editLayoutTurnoutFrame = new JmriJFrame(Bundle.getMessage("EditTurnout"), false, true);
            editLayoutTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.EditLayoutTurnout", true);
            editLayoutTurnoutFrame.setLocation(50, 30);
            Container contentPane = editLayoutTurnoutFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            // setup turnout name
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel turnoutNameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));
            panel1.add(turnoutNameLabel);

            // add combobox to select turnout
            editLayoutTurnout1stTurnoutComboBox = new JmriBeanComboBox(
                    InstanceManager.turnoutManagerInstance(),
                    layoutTurnout.getTurnout(),
                    JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(editLayoutTurnout1stTurnoutComboBox, true, true);

            // disable items that are already in use
            PopupMenuListener pml = new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    // This method is called before the popup menu becomes visible.
                    log.debug("PopupMenuWillBecomeVisible");
                    Object o = e.getSource();
                    if (o instanceof JmriBeanComboBox) {
                        JmriBeanComboBox jbcb = (JmriBeanComboBox) o;
                        jmri.Manager m = jbcb.getManager();
                        if (m != null) {
                            String[] systemNames = m.getSystemNameArray();
                            for (int idx = 0; idx < systemNames.length; idx++) {
                                String systemName = systemNames[idx];
                                jbcb.setItemEnabled(idx, layoutEditor.validatePhysicalTurnout(systemName, null));
                            }
                        }
                    }
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    // This method is called before the popup menu becomes invisible
                    log.debug("PopupMenuWillBecomeInvisible");
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    // This method is called when the popup menu is canceled
                    log.debug("PopupMenuCanceled");
                }
            };

            editLayoutTurnout1stTurnoutComboBox.addPopupMenuListener(pml);
            editLayoutTurnout1stTurnoutComboBox.setEnabledColor(Color.green.darker().darker());
            editLayoutTurnout1stTurnoutComboBox.setDisabledColor(Color.red);

            panel1.add(editLayoutTurnout1stTurnoutComboBox);
            contentPane.add(panel1);

            JPanel panel1a = new JPanel();
            panel1a.setLayout(new BoxLayout(panel1a, BoxLayout.Y_AXIS));

            editLayoutTurnout2ndTurnoutComboBox = new JmriBeanComboBox(
                    InstanceManager.turnoutManagerInstance(),
                    layoutTurnout.getSecondTurnout(),
                    JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(editLayoutTurnout2ndTurnoutComboBox, true, false);

            editLayoutTurnout2ndTurnoutComboBox.addPopupMenuListener(pml);
            editLayoutTurnout2ndTurnoutComboBox.setEnabledColor(Color.green.darker().darker());
            editLayoutTurnout2ndTurnoutComboBox.setDisabledColor(Color.red);

            editLayoutTurnout2ndTurnoutCheckBox.addActionListener((ActionEvent e) -> {
                boolean additionalEnabled = editLayoutTurnout2ndTurnoutCheckBox.isSelected();
                editLayoutTurnout2ndTurnoutLabel.setEnabled(additionalEnabled);
                editLayoutTurnout2ndTurnoutComboBox.setEnabled(additionalEnabled);
                editLayoutTurnout2ndTurnoutInvertCheckBox.setEnabled(additionalEnabled);
            });
            panel1a.add(editLayoutTurnout2ndTurnoutCheckBox);
            contentPane.add(panel1a);

            editLayoutTurnout2ndTurnoutLabel = new JLabel(Bundle.getMessage("Supporting", Bundle.getMessage("BeanNameTurnout")));
            editLayoutTurnout2ndTurnoutLabel.setEnabled(false);
            JPanel panel1b = new JPanel();
            panel1b.add(editLayoutTurnout2ndTurnoutLabel);
            panel1b.add(editLayoutTurnout2ndTurnoutComboBox);
            editLayoutTurnout2ndTurnoutInvertCheckBox.addActionListener((ActionEvent e) -> {
                layoutTurnout.setSecondTurnoutInverted(editLayoutTurnout2ndTurnoutInvertCheckBox.isSelected());
            });
            editLayoutTurnout2ndTurnoutInvertCheckBox.setEnabled(false);
            panel1b.add(editLayoutTurnout2ndTurnoutInvertCheckBox);
            contentPane.add(panel1b);

            // add continuing state choice, if not crossover
            if ((layoutTurnout.getTurnoutType() != LayoutTurnout.DOUBLE_XOVER)
                    && (layoutTurnout.getTurnoutType() != LayoutTurnout.RH_XOVER)
                    && (layoutTurnout.getTurnoutType() != LayoutTurnout.LH_XOVER)) {
                JPanel panel3 = new JPanel();
                panel3.setLayout(new FlowLayout());
                editLayoutTurnoutStateComboBox.removeAllItems();
                editLayoutTurnoutStateComboBox.addItem(InstanceManager.turnoutManagerInstance().getClosedText());
                editLayoutTurnoutClosedIndex = 0;
                editLayoutTurnoutStateComboBox.addItem(InstanceManager.turnoutManagerInstance().getThrownText());
                editLayoutTurnoutThrownIndex = 1;
                editLayoutTurnoutStateComboBox.setToolTipText(Bundle.getMessage("StateToolTip"));
                panel3.add(new JLabel(Bundle.getMessage("ContinuingState")));
                panel3.add(editLayoutTurnoutStateComboBox);
                contentPane.add(panel3);
            }

            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            editLayoutTurnoutHiddenCheckBox.setToolTipText(Bundle.getMessage("HiddenToolTip"));
            panel33.add(editLayoutTurnoutHiddenCheckBox);
            contentPane.add(panel33);

            TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
            border.setTitle(Bundle.getMessage("BeanNameBlock"));
            // setup block name
            JPanel panel2 = new JPanel();
            panel2.setBorder(border);
            panel2.setLayout(new FlowLayout());
            panel2.add(editLayoutTurnoutBlockNameComboBox);
            LayoutEditor.setupComboBox(editLayoutTurnoutBlockNameComboBox, false, true);
            editLayoutTurnoutBlockNameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));
            panel2.add(editLayoutTurnoutBlockButton = new JButton(Bundle.getMessage("CreateEdit")));
            editLayoutTurnoutBlockButton.addActionListener((ActionEvent e) -> {
                turnoutEditBlockPressed(e);
            });
            contentPane.add(panel2);
            if ((layoutTurnout.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                    || (layoutTurnout.getTurnoutType() == LayoutTurnout.RH_XOVER)
                    || (layoutTurnout.getTurnoutType() == LayoutTurnout.LH_XOVER)) {
                JPanel panel21 = new JPanel();
                panel21.setLayout(new FlowLayout());
                TitledBorder borderblk2 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                borderblk2.setTitle(Bundle.getMessage("BeanNameBlock") + " 2");
                panel21.setBorder(borderblk2);
                LayoutEditor.setupComboBox(editLayoutTurnoutBlockBNameComboBox, false, true);
                editLayoutTurnoutBlockBNameComboBox.setToolTipText(Bundle.getMessage("EditBlockBNameHint"));
                panel21.add(editLayoutTurnoutBlockBNameComboBox);

                panel21.add(editLayoutTurnoutBlockBButton = new JButton(Bundle.getMessage("CreateEdit")));
                editLayoutTurnoutBlockBButton.addActionListener((ActionEvent e) -> {
                    turnoutEditBlockBPressed(e);
                });
                editLayoutTurnoutBlockBButton.setToolTipText(Bundle.getMessage("EditBlockHint", "2"));
                contentPane.add(panel21);

                JPanel panel22 = new JPanel();
                panel22.setLayout(new FlowLayout());
                TitledBorder borderblk3 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                borderblk3.setTitle(Bundle.getMessage("BeanNameBlock") + " 3");
                panel22.setBorder(borderblk3);
                LayoutEditor.setupComboBox(editLayoutTurnoutBlockCNameComboBox, false, true);
                editLayoutTurnoutBlockCNameComboBox.setToolTipText(Bundle.getMessage("EditBlockCNameHint"));
                panel22.add(editLayoutTurnoutBlockCNameComboBox);
                panel22.add(editLayoutTurnoutBlockCButton = new JButton(Bundle.getMessage("CreateEdit")));
                editLayoutTurnoutBlockCButton.addActionListener((ActionEvent e) -> {
                    turnoutEditBlockCPressed(e);
                });
                editLayoutTurnoutBlockCButton.setToolTipText(Bundle.getMessage("EditBlockHint", "3"));
                contentPane.add(panel22);

                JPanel panel23 = new JPanel();
                panel23.setLayout(new FlowLayout());
                TitledBorder borderblk4 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                borderblk4.setTitle(Bundle.getMessage("BeanNameBlock") + " 4");
                panel23.setBorder(borderblk4);
                LayoutEditor.setupComboBox(editLayoutTurnoutBlockDNameComboBox, false, true);
                editLayoutTurnoutBlockDNameComboBox.setToolTipText(Bundle.getMessage("EditBlockDNameHint"));
                panel23.add(editLayoutTurnoutBlockDNameComboBox);
                panel23.add(editLayoutTurnoutBlockDButton = new JButton(Bundle.getMessage("CreateEdit")));
                editLayoutTurnoutBlockDButton.addActionListener((ActionEvent e) -> {
                    turnoutEditBlockDPressed(e);
                });
                editLayoutTurnoutBlockDButton.setToolTipText(Bundle.getMessage("EditBlockHint", "4"));
                contentPane.add(panel23);
            }
            // set up Edit Block, Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            // Edit Block

            editLayoutTurnoutBlockButton.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1
            // Done
            panel5.add(editLayoutTurnoutDoneButton = new JButton(Bundle.getMessage("ButtonDone")));

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(editLayoutTurnoutDoneButton);
                rootPane.setDefaultButton(editLayoutTurnoutDoneButton);
            });

            editLayoutTurnoutDoneButton.addActionListener((ActionEvent e) -> {
                turnoutEditDonePressed(e);
            });
            editLayoutTurnoutDoneButton.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            // Cancel
            panel5.add(editLayoutTurnoutCancelButton = new JButton(Bundle.getMessage("ButtonCancel")));
            editLayoutTurnoutCancelButton.addActionListener((ActionEvent e) -> {
                turnoutEditCancelPressed(e);
            });
            editLayoutTurnoutCancelButton.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            contentPane.add(panel5);
        }

        editLayoutTurnout1stTurnoutComboBox.setText(layoutTurnout.getTurnoutName());

        editLayoutTurnoutHiddenCheckBox.setSelected(layoutTurnout.isHidden());

        // Set up for Edit
        editLayoutTurnoutBlockNameComboBox.setText(layoutTurnout.getBlockName());
        if ((layoutTurnout.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                || (layoutTurnout.getTurnoutType() == LayoutTurnout.RH_XOVER)
                || (layoutTurnout.getTurnoutType() == LayoutTurnout.LH_XOVER)) {
            editLayoutTurnoutBlockBNameComboBox.setText(layoutTurnout.getBlockBName());
            editLayoutTurnoutBlockCNameComboBox.setText(layoutTurnout.getBlockCName());
            editLayoutTurnoutBlockDNameComboBox.setText(layoutTurnout.getBlockDName());
        }

        if ((layoutTurnout.getTurnoutType() != LayoutTurnout.DOUBLE_XOVER)
                && (layoutTurnout.getTurnoutType() != LayoutTurnout.RH_XOVER)
                && (layoutTurnout.getTurnoutType() != LayoutTurnout.LH_XOVER)) {
            editLayoutTurnout2ndTurnoutCheckBox.setText(Bundle.getMessage("ThrowTwoTurnouts"));
        }

        boolean enable2nd = !layoutTurnout.getSecondTurnoutName().isEmpty();
        editLayoutTurnout2ndTurnoutCheckBox.setSelected(enable2nd);
        editLayoutTurnout2ndTurnoutInvertCheckBox.setEnabled(enable2nd);
        editLayoutTurnout2ndTurnoutLabel.setEnabled(enable2nd);
        editLayoutTurnout2ndTurnoutComboBox.setEnabled(enable2nd);
        if (enable2nd) {
            editLayoutTurnout2ndTurnoutInvertCheckBox.setSelected(layoutTurnout.isSecondTurnoutInverted());
            editLayoutTurnout2ndTurnoutComboBox.setText(layoutTurnout.getSecondTurnoutName());
        } else {
            editLayoutTurnout2ndTurnoutInvertCheckBox.setSelected(false);
            editLayoutTurnout2ndTurnoutComboBox.setText("");
        }

        if ((layoutTurnout.getTurnoutType() != LayoutTurnout.DOUBLE_XOVER)
                && (layoutTurnout.getTurnoutType() != LayoutTurnout.RH_XOVER)
                && (layoutTurnout.getTurnoutType() != LayoutTurnout.LH_XOVER)) {
            if (layoutTurnout.getContinuingSense() == Turnout.CLOSED) {
                editLayoutTurnoutStateComboBox.setSelectedIndex(editLayoutTurnoutClosedIndex);
            } else {
                editLayoutTurnoutStateComboBox.setSelectedIndex(editLayoutTurnoutThrownIndex);
            }
        }

        editLayoutTurnoutFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                turnoutEditCancelPressed(null);
            }
        });
        editLayoutTurnoutFrame.pack();
        editLayoutTurnoutFrame.setVisible(true);
        editLayoutTurnoutOpen = true;
        editLayoutTurnoutNeedsBlockUpdate = false;
    }   // editLayoutTurnout

    void turnoutEditBlockPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutTurnoutBlockNameComboBox.getUserName();
        if (!layoutTurnout.getBlockName().equals(newName)) {
            // block has changed, if old block exists, decrement use
            if ((layoutTurnout.getLayoutBlock() != null)
                    && (layoutTurnout.getLayoutBlock() != layoutTurnout.getLayoutBlockB())
                    && (layoutTurnout.getLayoutBlock() != layoutTurnout.getLayoutBlockC())
                    && (layoutTurnout.getLayoutBlock() != layoutTurnout.getLayoutBlockD())) {
                layoutTurnout.getLayoutBlock().decrementUse();
            }
            // get new block, or null if block has been removed
            try {
                layoutTurnout.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            } catch (IllegalArgumentException ex) {
                layoutTurnout.setLayoutBlock(null);
            }
            // decrement use if block was already counted
            if ((layoutTurnout.getLayoutBlock() != null)
                    && ((layoutTurnout.getLayoutBlock() == layoutTurnout.getLayoutBlockB())
                    || (layoutTurnout.getLayoutBlock() == layoutTurnout.getLayoutBlockC())
                    || (layoutTurnout.getLayoutBlock() == layoutTurnout.getLayoutBlockD()))) {
                layoutTurnout.getLayoutBlock().decrementUse();
            }
            editLayoutTurnoutNeedRedraw = true;
            editLayoutTurnoutNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (layoutTurnout.getLayoutBlock() == null) {
            JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
                    Bundle.getMessage("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        layoutTurnout.getLayoutBlock().editLayoutBlock(editLayoutTurnoutFrame);
        editLayoutTurnoutNeedRedraw = true;
        layoutEditor.setDirty();
    }   // turnoutEditBlockPressed

    void turnoutEditBlockBPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutTurnoutBlockBNameComboBox.getUserName();
        if (!layoutTurnout.getBlockBName().equals(newName)) {
            // block has changed, if old block exists, decrement use
            if ((layoutTurnout.getLayoutBlockB() != null)
                    && (layoutTurnout.getLayoutBlock() != layoutTurnout.getLayoutBlockB())
                    && (layoutTurnout.getLayoutBlockB() != layoutTurnout.getLayoutBlockC())
                    && (layoutTurnout.getLayoutBlockB() != layoutTurnout.getLayoutBlockD())) {
                layoutTurnout.getLayoutBlockB().decrementUse();
            }
            // get new block, or null if block has been removed
            try {
                layoutTurnout.setLayoutBlockB(layoutEditor.provideLayoutBlock(newName));
            } catch (IllegalArgumentException ex) {
                layoutTurnout.setLayoutBlockB(null);
            }
            // decrement use if block was already counted
            if ((layoutTurnout.getLayoutBlockB() != null)
                    && ((layoutTurnout.getLayoutBlock() == layoutTurnout.getLayoutBlockB())
                    || (layoutTurnout.getLayoutBlockB() == layoutTurnout.getLayoutBlockC())
                    || (layoutTurnout.getLayoutBlockB() == layoutTurnout.getLayoutBlockD()))) {
                layoutTurnout.getLayoutBlockB().decrementUse();
            }
            editLayoutTurnoutNeedRedraw = true;
            editLayoutTurnoutNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (layoutTurnout.getLayoutBlockB() == null) {
            JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
                    Bundle.getMessage("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        layoutTurnout.getLayoutBlockB().editLayoutBlock(editLayoutTurnoutFrame);
        editLayoutTurnoutNeedRedraw = true;
        layoutEditor.setDirty();
    }   // turnoutEditBlockBPressed

    void turnoutEditBlockCPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutTurnoutBlockCNameComboBox.getUserName();
        if (!layoutTurnout.getBlockCName().equals(newName)) {
            // block has changed, if old block exists, decrement use
            if ((layoutTurnout.getLayoutBlockC() != null)
                    && (layoutTurnout.getLayoutBlock() != layoutTurnout.getLayoutBlockC())
                    && (layoutTurnout.getLayoutBlockB() != layoutTurnout.getLayoutBlockC())
                    && (layoutTurnout.getLayoutBlockC() != layoutTurnout.getLayoutBlockD())) {
                layoutTurnout.getLayoutBlockC().decrementUse();
            }
            // get new block, or null if block has been removed
            try {
                layoutTurnout.setLayoutBlockC(layoutEditor.provideLayoutBlock(newName));
            } catch (IllegalArgumentException ex) {
                layoutTurnout.setLayoutBlockC(null);
            }
            // decrement use if block was already counted
            if ((layoutTurnout.getLayoutBlockC() != null)
                    && ((layoutTurnout.getLayoutBlock() == layoutTurnout.getLayoutBlockC())
                    || (layoutTurnout.getLayoutBlockB() == layoutTurnout.getLayoutBlockC())
                    || (layoutTurnout.getLayoutBlockC() == layoutTurnout.getLayoutBlockD()))) {
                layoutTurnout.getLayoutBlockD().decrementUse();
            }
            editLayoutTurnoutNeedRedraw = true;
            editLayoutTurnoutNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (layoutTurnout.getLayoutBlockC() == null) {
            JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
                    Bundle.getMessage("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        layoutTurnout.getLayoutBlockC().editLayoutBlock(editLayoutTurnoutFrame);
        editLayoutTurnoutNeedRedraw = true;
        layoutEditor.setDirty();
    }   // turnoutEditBlockCPressed

    void turnoutEditBlockDPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutTurnoutBlockDNameComboBox.getUserName();
        if (!layoutTurnout.getBlockDName().equals(newName)) {
            // block has changed, if old block exists, decrement use
            if ((layoutTurnout.getLayoutBlockD() != null)
                    && (layoutTurnout.getLayoutBlock() != layoutTurnout.getLayoutBlockD())
                    && (layoutTurnout.getLayoutBlockB() != layoutTurnout.getLayoutBlockD())
                    && (layoutTurnout.getLayoutBlockC() != layoutTurnout.getLayoutBlockD())) {
                layoutTurnout.getLayoutBlockD().decrementUse();
            }
            // get new block, or null if block has been removed
            try {
                layoutTurnout.setLayoutBlockD(layoutEditor.provideLayoutBlock(newName));
            } catch (IllegalArgumentException ex) {
                layoutTurnout.setLayoutBlockD(null);
            }
            // decrement use if block was already counted
            if ((layoutTurnout.getLayoutBlockD() != null) && ((layoutTurnout.getLayoutBlock() == layoutTurnout.getLayoutBlockD())
                    || (layoutTurnout.getLayoutBlockB() == layoutTurnout.getLayoutBlockD())
                    || (layoutTurnout.getLayoutBlockC() == layoutTurnout.getLayoutBlockD()))) {
                layoutTurnout.getLayoutBlockD().decrementUse();
            }
            editLayoutTurnoutNeedRedraw = true;
            editLayoutTurnoutNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (layoutTurnout.getLayoutBlockD() == null) {
            JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
                    Bundle.getMessage("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        layoutTurnout.getLayoutBlockD().editLayoutBlock(editLayoutTurnoutFrame);
        editLayoutTurnoutNeedRedraw = true;
        layoutEditor.setDirty();
    }   // turnoutEditBlockDPressed

    void turnoutEditDonePressed(ActionEvent a) {
        // check if Turnout changed
        String newName = editLayoutTurnout1stTurnoutComboBox.getDisplayName();
        if (!layoutTurnout.getTurnoutName().equals(newName)) {
            // turnout has changed
            if (layoutEditor.validatePhysicalTurnout(
                    newName, editLayoutTurnoutFrame)) {
                layoutTurnout.setTurnout(newName);
            } else {
                layoutTurnout.setTurnout(null);
                editLayoutTurnout1stTurnoutComboBox.setText("");
            }
            editLayoutTurnoutNeedRedraw = true;
        }

        if (editLayoutTurnout2ndTurnoutCheckBox.isSelected()) {
            newName = editLayoutTurnout2ndTurnoutComboBox.getDisplayName();
            if (!layoutTurnout.getSecondTurnoutName().equals(newName)) {
                if ((layoutTurnout.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                        || (layoutTurnout.getTurnoutType() == LayoutTurnout.RH_XOVER)
                        || (layoutTurnout.getTurnoutType() == LayoutTurnout.LH_XOVER)) {
                    // turnout has changed
                    if (layoutEditor.validatePhysicalTurnout(
                            newName, editLayoutTurnoutFrame)) {
                        layoutTurnout.setSecondTurnout(newName);
                    } else {
                        editLayoutTurnout2ndTurnoutCheckBox.setSelected(false);
                        layoutTurnout.setSecondTurnout(null);
                        editLayoutTurnout2ndTurnoutComboBox.setText("");
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
        newName = editLayoutTurnoutBlockNameComboBox.getUserName();
        if (!layoutTurnout.getBlockName().equals(newName)) {
            // block has changed, if old block exists, decrement use
            if ((layoutTurnout.getLayoutBlock() != null)
                    && (layoutTurnout.getLayoutBlock() != layoutTurnout.getLayoutBlockB())
                    && (layoutTurnout.getLayoutBlock() != layoutTurnout.getLayoutBlockC())
                    && (layoutTurnout.getLayoutBlock() != layoutTurnout.getLayoutBlockD())) {
                layoutTurnout.getLayoutBlock().decrementUse();
            }
            // get new block, or null if block has been removed
            try {
                layoutTurnout.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            } catch (IllegalArgumentException ex) {
                layoutTurnout.setLayoutBlock(null);
            }
            // decrement use if block was already counted
            if ((layoutTurnout.getLayoutBlock() != null)
                    && ((layoutTurnout.getLayoutBlock() == layoutTurnout.getLayoutBlockB())
                    || (layoutTurnout.getLayoutBlock() == layoutTurnout.getLayoutBlockC())
                    || (layoutTurnout.getLayoutBlock() == layoutTurnout.getLayoutBlockD()))) {
                layoutTurnout.getLayoutBlock().decrementUse();
            }
            editLayoutTurnoutNeedRedraw = true;
            editLayoutTurnoutNeedsBlockUpdate = true;
        }
        if ((layoutTurnout.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                || (layoutTurnout.getTurnoutType() == LayoutTurnout.LH_XOVER)
                || (layoutTurnout.getTurnoutType() == LayoutTurnout.RH_XOVER)) {
            // check if Block 2 changed
            newName = editLayoutTurnoutBlockBNameComboBox.getUserName();
            if (!layoutTurnout.getBlockBName().equals(newName)) {
                // block has changed, if old block exists, decrement use
                if ((layoutTurnout.getLayoutBlockB() != null)
                        && (layoutTurnout.getLayoutBlock() != layoutTurnout.getLayoutBlockB())
                        && (layoutTurnout.getLayoutBlockB() != layoutTurnout.getLayoutBlockC())
                        && (layoutTurnout.getLayoutBlockB() != layoutTurnout.getLayoutBlockD())) {
                    layoutTurnout.getLayoutBlockB().decrementUse();
                }
                // get new block, or null if block has been removed
                try {
                    layoutTurnout.setLayoutBlockB(layoutEditor.provideLayoutBlock(newName));
                } catch (IllegalArgumentException ex) {
                    layoutTurnout.setLayoutBlockB(null);
                }
                // decrement use if block was already counted
                if ((layoutTurnout.getLayoutBlockB() != null)
                        && ((layoutTurnout.getLayoutBlock() == layoutTurnout.getLayoutBlockB())
                        || (layoutTurnout.getLayoutBlockB() == layoutTurnout.getLayoutBlockC())
                        || (layoutTurnout.getLayoutBlockB() == layoutTurnout.getLayoutBlockD()))) {
                    layoutTurnout.getLayoutBlockB().decrementUse();
                }
                editLayoutTurnoutNeedRedraw = true;
                editLayoutTurnoutNeedsBlockUpdate = true;
            }
            // check if Block 3 changed
            newName = editLayoutTurnoutBlockCNameComboBox.getUserName();
            if (!layoutTurnout.getBlockCName().equals(newName)) {
                // block has changed, if old block exists, decrement use
                if ((layoutTurnout.getLayoutBlockC() != null)
                        && (layoutTurnout.getLayoutBlock() != layoutTurnout.getLayoutBlockC())
                        && (layoutTurnout.getLayoutBlockB() != layoutTurnout.getLayoutBlockC())
                        && (layoutTurnout.getLayoutBlockC() != layoutTurnout.getLayoutBlockD())) {
                    layoutTurnout.getLayoutBlockC().decrementUse();
                }
                // get new block, or null if block has been removed
                try {
                    layoutTurnout.setLayoutBlockC(layoutEditor.provideLayoutBlock(newName));
                } catch (IllegalArgumentException ex) {
                    layoutTurnout.setLayoutBlockC(null);
                }

                // decrement use if block was already counted
                if ((layoutTurnout.getLayoutBlockC() != null)
                        && ((layoutTurnout.getLayoutBlock() == layoutTurnout.getLayoutBlockC())
                        || (layoutTurnout.getLayoutBlockB() == layoutTurnout.getLayoutBlockC())
                        || (layoutTurnout.getLayoutBlockC() == layoutTurnout.getLayoutBlockD()))) {
                    layoutTurnout.getLayoutBlockC().decrementUse();
                }
                editLayoutTurnoutNeedRedraw = true;
                editLayoutTurnoutNeedsBlockUpdate = true;
            }
            // check if Block 4 changed
            newName = editLayoutTurnoutBlockDNameComboBox.getUserName();
            if (!layoutTurnout.getBlockDName().equals(newName)) {
                // block has changed, if old block exists, decrement use
                if ((layoutTurnout.getLayoutBlockD() != null)
                        && (layoutTurnout.getLayoutBlock() != layoutTurnout.getLayoutBlockD())
                        && (layoutTurnout.getLayoutBlockB() != layoutTurnout.getLayoutBlockD())
                        && (layoutTurnout.getLayoutBlockC() != layoutTurnout.getLayoutBlockD())) {
                    layoutTurnout.getLayoutBlockD().decrementUse();
                }
                // get new block, or null if block has been removed
                try {
                    layoutTurnout.setLayoutBlockD(layoutEditor.provideLayoutBlock(newName));
                } catch (IllegalArgumentException ex) {
                    layoutTurnout.setLayoutBlockD(null);
                }
                // decrement use if block was already counted
                if ((layoutTurnout.getLayoutBlockD() != null)
                        && ((layoutTurnout.getLayoutBlock() == layoutTurnout.getLayoutBlockD())
                        || (layoutTurnout.getLayoutBlockB() == layoutTurnout.getLayoutBlockD())
                        || (layoutTurnout.getLayoutBlockC() == layoutTurnout.getLayoutBlockD()))) {
                    layoutTurnout.getLayoutBlockD().decrementUse();
                }
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
    }   // turnoutEditDonePressed

    void turnoutEditCancelPressed(ActionEvent a) {
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
    private JButton editLayoutSlipDoneButton;
    private JButton editLayoutSlipCancelButton;
    private JButton editLayoutSlipBlockButton;
    private JmriBeanComboBox editLayoutSlipTurnoutAComboBox;
    private JmriBeanComboBox editLayoutSlipTurnoutBComboBox;
    private JCheckBox editLayoutSlipHiddenBox = new JCheckBox(Bundle.getMessage("HideSlip"));
    private JmriBeanComboBox editLayoutSlipBlockNameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private boolean editLayoutSlipOpen = false;
    private boolean editLayoutSlipNeedsRedraw = false;
    private boolean editLayoutSlipNeedsBlockUpdate = false;

    /**
     * Edit a Slip
     */
    protected void editLayoutSlip(LayoutSlip layoutSlip) {
        this.layoutSlip = layoutSlip;
        if (editLayoutSlipOpen) {
            editLayoutSlipFrame.setVisible(true);
        } else if (editLayoutSlipFrame == null) {   // Initialize if needed
            editLayoutSlipFrame = new JmriJFrame(Bundle.getMessage("EditSlip"), false, true);
            editLayoutSlipFrame.addHelpMenu("package.jmri.jmrit.display.EditLayoutSlip", true);
            editLayoutSlipFrame.setLocation(50, 30);

            Container contentPane = editLayoutSlipFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel turnoutNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " A " + Bundle.getMessage("Name"));
            panel1.add(turnoutNameLabel);
            editLayoutSlipTurnoutAComboBox = new JmriBeanComboBox(
                    InstanceManager.turnoutManagerInstance(),
                    layoutSlip.getTurnout(),
                    JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(editLayoutSlipTurnoutAComboBox, true, true);

            // disable items that are already in use
            PopupMenuListener pml = new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    // This method is called before the popup menu becomes visible.
                    log.debug("PopupMenuWillBecomeVisible");
                    Object o = e.getSource();
                    if (o instanceof JmriBeanComboBox) {
                        JmriBeanComboBox jbcb = (JmriBeanComboBox) o;
                        jmri.Manager m = jbcb.getManager();
                        if (m != null) {
                            String[] systemNames = m.getSystemNameArray();
                            for (int idx = 0; idx < systemNames.length; idx++) {
                                String systemName = systemNames[idx];
                                jbcb.setItemEnabled(idx, layoutEditor.validatePhysicalTurnout(systemName, null));
                            }
                        }
                    }
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    // This method is called before the popup menu becomes invisible
                    log.debug("PopupMenuWillBecomeInvisible");
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    // This method is called when the popup menu is canceled
                    log.debug("PopupMenuCanceled");
                }
            };

            editLayoutSlipTurnoutAComboBox.addPopupMenuListener(pml);
            editLayoutSlipTurnoutAComboBox.setEnabledColor(Color.green.darker().darker());
            editLayoutSlipTurnoutAComboBox.setDisabledColor(Color.red);

            panel1.add(editLayoutSlipTurnoutAComboBox);
            contentPane.add(panel1);

            JPanel panel1a = new JPanel();
            panel1a.setLayout(new FlowLayout());
            JLabel turnoutBNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " B " + Bundle.getMessage("Name"));
            panel1a.add(turnoutBNameLabel);

            editLayoutSlipTurnoutBComboBox = new JmriBeanComboBox(
                    InstanceManager.turnoutManagerInstance(),
                    layoutSlip.getTurnoutB(),
                    JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(editLayoutSlipTurnoutBComboBox, true, true);

            editLayoutSlipTurnoutBComboBox.addPopupMenuListener(pml);
            editLayoutSlipTurnoutBComboBox.setEnabledColor(Color.green.darker().darker());
            editLayoutSlipTurnoutBComboBox.setDisabledColor(Color.red);

            panel1a.add(editLayoutSlipTurnoutBComboBox);

            contentPane.add(panel1a);

            JPanel panel2 = new JPanel();
            panel2.setLayout(new GridLayout(0, 3, 2, 2));

            panel2.add(new Label("   "));
            panel2.add(new Label(Bundle.getMessage("BeanNameTurnout") + " A:"));
            panel2.add(new Label(Bundle.getMessage("BeanNameTurnout") + " B:"));
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
            JButton testButton = new JButton("Test");
            testButton.addActionListener((ActionEvent e) -> {
                toggleStateTest();
            });
            panel2.add(testButton);
            contentPane.add(panel2);

            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            editLayoutSlipHiddenBox.setToolTipText(Bundle.getMessage("HiddenToolTip"));
            panel33.add(editLayoutSlipHiddenBox);
            contentPane.add(panel33);

            // setup block name
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel block1NameLabel = new JLabel(Bundle.getMessage("BlockID"));
            panel3.add(block1NameLabel);
            panel3.add(editLayoutSlipBlockNameComboBox);
            LayoutEditor.setupComboBox(editLayoutSlipBlockNameComboBox, false, true);
            editLayoutSlipBlockNameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));

            contentPane.add(panel3);
            // set up Edit Block buttons
            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
            // Edit Block
            panel4.add(editLayoutSlipBlockButton = new JButton(Bundle.getMessage("EditBlock", "")));
            editLayoutSlipBlockButton.addActionListener(
                    (ActionEvent event) -> {
                        slipEditBlockPressed(event);
                    }
            );
            editLayoutSlipBlockButton.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1

            contentPane.add(panel4);
            // set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(editLayoutSlipDoneButton = new JButton(Bundle.getMessage("ButtonDone")));

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(editLayoutSlipDoneButton);
                rootPane.setDefaultButton(editLayoutSlipDoneButton);
            }
            );

            editLayoutSlipDoneButton.addActionListener((ActionEvent event) -> {
                slipEditDonePressed(event);
            });
            editLayoutSlipDoneButton.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            // Cancel
            panel5.add(editLayoutSlipCancelButton = new JButton(Bundle.getMessage("ButtonCancel")));
            editLayoutSlipCancelButton.addActionListener((ActionEvent event) -> {
                slipEditCancelPressed(event);
            });
            editLayoutSlipCancelButton.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            contentPane.add(panel5);
        }

        editLayoutSlipHiddenBox.setSelected(layoutSlip.isHidden());

        // Set up for Edit
        editLayoutSlipTurnoutAComboBox.setText(layoutSlip.getTurnoutName());
        editLayoutSlipTurnoutBComboBox.setText(layoutSlip.getTurnoutBName());
        editLayoutSlipBlockNameComboBox.setText(layoutSlip.getBlockName());

        editLayoutSlipFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                slipEditCancelPressed(null);
            }
        });
        editLayoutSlipFrame.pack();
        editLayoutSlipFrame.setVisible(true);
        editLayoutSlipOpen = true;
        editLayoutSlipNeedsBlockUpdate = false;
    }

    private void drawSlipState(Graphics2D g2, int state) {
        Point2D cenP = layoutSlip.getCoordsCenter();
        Point2D A = MathUtil.subtract(layoutSlip.getCoordsA(), cenP);
        Point2D B = MathUtil.subtract(layoutSlip.getCoordsB(), cenP);
        Point2D C = MathUtil.subtract(layoutSlip.getCoordsC(), cenP);
        Point2D D = MathUtil.subtract(layoutSlip.getCoordsD(), cenP);

        Point2D ctrP = new Point2D.Double(20.0, 20.0);
        A = MathUtil.add(MathUtil.multiply(MathUtil.normalize(A), 18.0), ctrP);
        B = MathUtil.add(MathUtil.multiply(MathUtil.normalize(B), 18.0), ctrP);
        C = MathUtil.add(MathUtil.multiply(MathUtil.normalize(C), 18.0), ctrP);
        D = MathUtil.add(MathUtil.multiply(MathUtil.normalize(D), 18.0), ctrP);

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
    }

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

    int testState = LayoutTurnout.UNKNOWN;

    /**
     * Toggle slip states if clicked on, physical turnout exists, and not
     * disabled
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

        if (editLayoutSlipTurnoutAComboBox.getSelectedBean() != null) {
            ((Turnout) editLayoutSlipTurnoutAComboBox.getSelectedBean()).setCommandedState(turnAState);
        }
        if (editLayoutSlipTurnoutBComboBox.getSelectedBean() != null) {
            ((Turnout) editLayoutSlipTurnoutBComboBox.getSelectedBean()).setCommandedState(turnBState);
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

    private void slipEditBlockPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutSlipBlockNameComboBox.getUserName();
        if (!layoutSlip.getBlockName().equals(newName)) {
            // block has changed, if old block exists, decrement use
            if ((layoutSlip.getLayoutBlock() != null)
                    && (layoutSlip.getLayoutBlock() != layoutSlip.getLayoutBlockB())
                    && (layoutSlip.getLayoutBlock() != layoutSlip.getLayoutBlockC())
                    && (layoutSlip.getLayoutBlock() != layoutSlip.getLayoutBlockD())) {
                layoutSlip.getLayoutBlock().decrementUse();
            }
            // get new block, or null if block has been removed
            try {
                layoutSlip.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            } catch (IllegalArgumentException ex) {
                layoutSlip.setLayoutBlock(null);
            }
            // decrement use if block was already counted
            if ((layoutSlip.getLayoutBlock() != null)
                    && ((layoutSlip.getLayoutBlock() == layoutSlip.getLayoutBlockB())
                    || (layoutSlip.getLayoutBlock() == layoutSlip.getLayoutBlockC())
                    || (layoutSlip.getLayoutBlock() == layoutSlip.getLayoutBlockD()))) {
                layoutSlip.getLayoutBlock().decrementUse();
            }
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
    }   // slipEditBlockPressed

    private void slipEditDonePressed(ActionEvent a) {
        String newName = editLayoutSlipTurnoutAComboBox.getDisplayName();
        if (!layoutSlip.getTurnoutName().equals(newName)) {
            if (layoutEditor.validatePhysicalTurnout(newName, editLayoutSlipFrame)) {
                layoutSlip.setTurnout(newName);
            } else {
                layoutSlip.setTurnout("");
            }
            editLayoutSlipNeedsRedraw = true;
        }

        newName = editLayoutSlipTurnoutBComboBox.getDisplayName();
        if (!layoutSlip.getTurnoutBName().equals(newName)) {
            if (layoutEditor.validatePhysicalTurnout(newName, editLayoutSlipFrame)) {
                layoutSlip.setTurnoutB(newName);
            } else {
                layoutSlip.setTurnoutB("");
            }
            editLayoutSlipNeedsRedraw = true;
        }

        newName = editLayoutSlipBlockNameComboBox.getUserName();
        if (!layoutSlip.getBlockName().equals(newName)) {
            // block 1 has changed, if old block exists, decrement use
            if ((layoutSlip.getLayoutBlock() != null)) {
                layoutSlip.getLayoutBlock().decrementUse();
            }
            // get new block, or null if block has been removed
            try {
                layoutSlip.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            } catch (IllegalArgumentException ex) {
                layoutSlip.setLayoutBlock(null);
                editLayoutSlipBlockNameComboBox.setText("");
                editLayoutSlipBlockNameComboBox.setSelectedIndex(-1);
            }
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
    }   // slipEditDonePressed

    private void slipEditCancelPressed(ActionEvent a) {
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
    private JCheckBox editLevelXingHiddenCheckBox = new JCheckBox(Bundle.getMessage("HideCrossing"));

    private JmriBeanComboBox editLevelXingBlock1NameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox editLevelXingBlock2NameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JButton editLevelXingDoneButton;
    private JButton editLevelXingCancelButton;
    private JButton editLevelXingBlock1Button;
    private JButton editLevelXingBlock2Button;

    private boolean editLevelXingOpen = false;
    private boolean editLevelXingNeedsRedraw = false;
    private boolean editLevelXingNeedsBlockUpdate = false;

    /**
     * Edit a Level Crossing
     */
    protected void editLevelXing(LevelXing levelXing) {
        this.levelXing = levelXing;
        if (editLevelXingOpen) {
            editLevelXingFrame.setVisible(true);
        } else // Initialize if needed
        if (editLevelXingFrame == null) {
            editLevelXingFrame = new JmriJFrame(Bundle.getMessage("EditXing"), false, true);
            editLevelXingFrame.addHelpMenu("package.jmri.jmrit.display.EditLevelXing", true);
            editLevelXingFrame.setLocation(50, 30);
            Container contentPane = editLevelXingFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            editLevelXingHiddenCheckBox.setToolTipText(Bundle.getMessage("HiddenToolTip"));
            panel33.add(editLevelXingHiddenCheckBox);
            contentPane.add(panel33);

            // setup block 1 name
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel block1NameLabel = new JLabel(Bundle.getMessage("Block_ID", 1));
            panel1.add(block1NameLabel);
            panel1.add(editLevelXingBlock1NameComboBox);
            LayoutEditor.setupComboBox(editLevelXingBlock1NameComboBox, false, true);
            editLevelXingBlock1NameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));
            contentPane.add(panel1);

            // setup block 2 name
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel block2NameLabel = new JLabel(Bundle.getMessage("Block_ID", 2));
            panel2.add(block2NameLabel);
            panel2.add(editLevelXingBlock2NameComboBox);
            LayoutEditor.setupComboBox(editLevelXingBlock2NameComboBox, false, true);
            editLevelXingBlock2NameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));
            contentPane.add(panel2);

            // set up Edit 1 Block and Edit 2 Block buttons
            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
            // Edit 1 Block
            panel4.add(editLevelXingBlock1Button = new JButton(Bundle.getMessage("EditBlock", 1)));
            editLevelXingBlock1Button.addActionListener((ActionEvent e) -> {
                xingEdit1BlockPressed(e);
            });
            editLevelXingBlock1Button.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1
            // Edit 2 Block
            panel4.add(editLevelXingBlock2Button = new JButton(Bundle.getMessage("EditBlock", 2)));
            editLevelXingBlock2Button.addActionListener((ActionEvent e) -> {
                xingEdit2BlockPressed(e);
            });
            editLevelXingBlock2Button.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1
            contentPane.add(panel4);
            // set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(editLevelXingDoneButton = new JButton(Bundle.getMessage("ButtonDone")));
            editLevelXingDoneButton.addActionListener((ActionEvent e) -> {
                xingEditDonePressed(e);
            });
            editLevelXingDoneButton.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(editLevelXingDoneButton);
                rootPane.setDefaultButton(editLevelXingDoneButton);
            });

            // Cancel
            panel5.add(editLevelXingCancelButton = new JButton(Bundle.getMessage("ButtonCancel")));
            editLevelXingCancelButton.addActionListener((ActionEvent e) -> {
                xingEditCancelPressed(e);
            });
            editLevelXingCancelButton.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            contentPane.add(panel5);
        }

        editLevelXingHiddenCheckBox.setSelected(levelXing.isHidden());

        // Set up for Edit
        editLevelXingBlock1NameComboBox.setText(levelXing.getBlockNameAC());
        editLevelXingBlock2NameComboBox.setText(levelXing.getBlockNameBD());
        editLevelXingFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                xingEditCancelPressed(null);
            }
        });
        editLevelXingFrame.pack();
        editLevelXingFrame.setVisible(true);
        editLevelXingOpen = true;
        editLevelXingNeedsBlockUpdate = false;
    }

    void xingEdit1BlockPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLevelXingBlock1NameComboBox.getUserName();
        if (!levelXing.getBlockNameAC().equals(newName)) {
            // block 1 has changed, if old block exists, decrement use
            if ((levelXing.getLayoutBlockAC() != null)
                    && (levelXing.getLayoutBlockAC() != levelXing.getLayoutBlockBD())) {
                levelXing.getLayoutBlockAC().decrementUse();
            }
            // get new block, or null if block has been removed
            if (!newName.isEmpty()) {
                try {
                    levelXing.setLayoutBlockAC(layoutEditor.provideLayoutBlock(newName));
                    // decrement use if block was previously counted
                    if ((levelXing.getLayoutBlockAC() != null)
                            && (levelXing.getLayoutBlockAC() == levelXing.getLayoutBlockBD())) {
                        levelXing.getLayoutBlockAC().decrementUse();
                    }
                } catch (IllegalArgumentException ex) {
                    levelXing.setLayoutBlockAC(null);
                    editLevelXingBlock1NameComboBox.setText("");
                    editLevelXingBlock1NameComboBox.setSelectedIndex(-1);
                }
            } else {
                levelXing.setLayoutBlockAC(null);
            }
            editLevelXingNeedsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            editLevelXingNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (levelXing.getLayoutBlockAC() == null) {
            JOptionPane.showMessageDialog(editLevelXingFrame,
                    Bundle.getMessage("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        levelXing.getLayoutBlockAC().editLayoutBlock(editLevelXingFrame);
        editLevelXingNeedsRedraw = true;
    }

    void xingEdit2BlockPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLevelXingBlock2NameComboBox.getUserName();
        if (-1 != editLevelXingBlock2NameComboBox.getSelectedIndex()) {
            newName = editLevelXingBlock2NameComboBox.getSelectedDisplayName();
        } else {
            newName = (null != newName) ? NamedBean.normalizeUserName(newName) : "";
        }
        if (!levelXing.getBlockNameBD().equals(newName)) {
            // block has changed, if old block exists, decrement use
            if ((levelXing.getLayoutBlockBD() != null) && (levelXing.getLayoutBlockBD() != levelXing.getLayoutBlockAC())) {
                levelXing.getLayoutBlockBD().decrementUse();
            }
            // get new block, or null if block has been removed
            if (!newName.isEmpty()) {
                try {
                    levelXing.setLayoutBlockBD(layoutEditor.provideLayoutBlock(newName));
                    // decrement use if block was previously counted
                    if ((levelXing.getLayoutBlockBD() != null)
                            && (levelXing.getLayoutBlockAC() == levelXing.getLayoutBlockBD())) {
                        levelXing.getLayoutBlockBD().decrementUse();
                    }
                } catch (IllegalArgumentException ex) {
                    levelXing.setLayoutBlockBD(null);
                    editLevelXingBlock2NameComboBox.setText("");
                    editLevelXingBlock2NameComboBox.setSelectedIndex(-1);
                }
            } else {
                levelXing.setLayoutBlockBD(null);
            }
            editLevelXingNeedsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            editLevelXingNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (levelXing.getLayoutBlockBD() == null) {
            JOptionPane.showMessageDialog(editLevelXingFrame,
                    Bundle.getMessage("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        levelXing.getLayoutBlockBD().editLayoutBlock(editLevelXingFrame);
        editLevelXingNeedsRedraw = true;
    }

    void xingEditDonePressed(ActionEvent a) {
        // check if Blocks changed
        String newName = editLevelXingBlock1NameComboBox.getUserName();
        if (!levelXing.getBlockNameAC().equals(newName)) {
            // block 1 has changed, if old block exists, decrement use
            if ((levelXing.getLayoutBlockAC() != null) && (levelXing.getLayoutBlockAC() != levelXing.getLayoutBlockBD())) {
                levelXing.getLayoutBlockAC().decrementUse();
            }
            // get new block, or null if block has been removed
            if (!newName.isEmpty()) {
                try {
                    levelXing.setLayoutBlockAC(layoutEditor.provideLayoutBlock(newName));
                    // decrement use if block was previously counted
                    if ((levelXing.getLayoutBlockAC() != null)
                            && (levelXing.getLayoutBlockAC() == levelXing.getLayoutBlockBD())) {
                        levelXing.getLayoutBlockAC().decrementUse();
                    }
                } catch (IllegalArgumentException ex) {
                    levelXing.setLayoutBlockAC(null);
                    editLevelXingBlock1NameComboBox.setText("");
                    editLevelXingBlock1NameComboBox.setSelectedIndex(-1);
                }
            } else {
                levelXing.setLayoutBlockAC(null);
            }
            editLevelXingNeedsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            editLevelXingNeedsBlockUpdate = true;
        }
        newName = editLevelXingBlock2NameComboBox.getUserName();
        if (!levelXing.getBlockNameBD().equals(newName)) {
            // block 2 has changed, if old block exists, decrement use
            if ((levelXing.getLayoutBlockBD() != null) && (levelXing.getLayoutBlockBD() != levelXing.getLayoutBlockAC())) {
                levelXing.getLayoutBlockBD().decrementUse();
            }
            // get new block, or null if block has been removed
            if (!newName.isEmpty()) {
                try {
                    levelXing.setLayoutBlockBD(layoutEditor.provideLayoutBlock(newName));
                    // decrement use if block was previously counted
                    if ((levelXing.getLayoutBlockBD() != null) && (levelXing.getLayoutBlockAC() == levelXing.getLayoutBlockBD())) {
                        levelXing.getLayoutBlockBD().decrementUse();
                    }
                } catch (IllegalArgumentException ex) {
                    levelXing.setLayoutBlockBD(null);
                    editLevelXingBlock2NameComboBox.setText("");
                    editLevelXingBlock2NameComboBox.setSelectedIndex(-1);
                }
            } else {
                levelXing.setLayoutBlockBD(null);
            }
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

    void xingEditCancelPressed(ActionEvent a) {
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

    private JmriJFrame editTurntableFrame = null;
    private JTextField editTurntableRadiusTextField = new JTextField(8);
    private JTextField editTurntableAngleTextField = new JTextField(8);
    private JButton editTurntableDoneButton;
    private JButton editTurntableCancelButton;

    private JPanel editTurntableRayPanel;
    private JButton editTurntableAddRayTrackButton;
    //private JButton editTurntableDeleteRayTrackButton;
    private JCheckBox editTurntableDccControlledCheckBox;

    private String editTurntableOldRadius = "";
    private boolean editTurntableOpen = false;
    private boolean editTurntableNeedsRedraw = false;

    /**
     * Edit a Turntable
     */
    protected void editTurntable(LayoutTurntable layoutTurntable) {
        this.layoutTurntable = layoutTurntable;
        if (editTurntableOpen) {
            editTurntableFrame.setVisible(true);
        } else // Initialize if needed
        if (editTurntableFrame == null) {
            editTurntableFrame = new JmriJFrame(Bundle.getMessage("EditTurntable"), false, true);
            editTurntableFrame.addHelpMenu("package.jmri.jmrit.display.EditTurntable", true);
            editTurntableFrame.setLocation(50, 30);

            Container contentPane = editTurntableFrame.getContentPane();
            JPanel headerPane = new JPanel();
            JPanel footerPane = new JPanel();
            headerPane.setLayout(new BoxLayout(headerPane, BoxLayout.Y_AXIS));
            footerPane.setLayout(new BoxLayout(footerPane, BoxLayout.Y_AXIS));
            contentPane.setLayout(new BorderLayout());
            contentPane.add(headerPane, BorderLayout.NORTH);
            contentPane.add(footerPane, BorderLayout.SOUTH);

            // setup radius
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel radiusLabel = new JLabel(Bundle.getMessage("TurntableRadius"));
            panel1.add(radiusLabel);
            panel1.add(editTurntableRadiusTextField);
            editTurntableRadiusTextField.setToolTipText(Bundle.getMessage("TurntableRadiusHint"));
            headerPane.add(panel1);

            // setup add ray track
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel rayAngleLabel = new JLabel(Bundle.getMessage("RayAngle"));
            panel2.add(rayAngleLabel);
            panel2.add(editTurntableAngleTextField);
            editTurntableAngleTextField.setToolTipText(Bundle.getMessage("RayAngleHint"));
            headerPane.add(panel2);

            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            panel3.add(editTurntableAddRayTrackButton = new JButton(Bundle.getMessage("AddRayTrack")));
            editTurntableAddRayTrackButton.setToolTipText(Bundle.getMessage("AddRayTrackHint"));
            editTurntableAddRayTrackButton.addActionListener((ActionEvent e) -> {
                addRayTrackPressed(e);
                updateRayPanel();
            });

            panel3.add(editTurntableDccControlledCheckBox = new JCheckBox(Bundle.getMessage("TurntableDCCControlled")));
            headerPane.add(panel3);

            // set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(editTurntableDoneButton = new JButton(Bundle.getMessage("ButtonDone")));
            editTurntableDoneButton.addActionListener((ActionEvent e) -> {
                turntableEditDonePressed(e);
            });

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(editTurntableDoneButton);
                rootPane.setDefaultButton(editTurntableDoneButton);
            });

            editTurntableDoneButton.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            // Cancel
            panel5.add(editTurntableCancelButton = new JButton(Bundle.getMessage("ButtonCancel")));
            editTurntableCancelButton.addActionListener((ActionEvent e) -> {
                turntableEditCancelPressed(e);
            });
            editTurntableCancelButton.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            footerPane.add(panel5);

            editTurntableRayPanel = new JPanel();
            editTurntableRayPanel.setLayout(new BoxLayout(editTurntableRayPanel, BoxLayout.Y_AXIS));
            JScrollPane rayScrollPane = new JScrollPane(editTurntableRayPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            contentPane.add(rayScrollPane, BorderLayout.CENTER);
        }
        editTurntableDccControlledCheckBox.setSelected(layoutTurntable.isTurnoutControlled());
        editTurntableDccControlledCheckBox.addActionListener((ActionEvent e) -> {
            layoutTurntable.setTurnoutControlled(editTurntableDccControlledCheckBox.isSelected());

            for (Component comp : editTurntableRayPanel.getComponents()) {
                if (comp instanceof TurntableRayPanel) {
                    TurntableRayPanel trp = (TurntableRayPanel) comp;
                    trp.showTurnoutDetails();
                }
            }
            editTurntableFrame.pack();
        });
        updateRayPanel();
        // Set up for Edit
        editTurntableRadiusTextField.setText(" " + layoutTurntable.getRadius());
        editTurntableOldRadius = editTurntableRadiusTextField.getText();
        editTurntableAngleTextField.setText("0");
        editTurntableFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                turntableEditCancelPressed(null);
            }
        });
        editTurntableFrame.pack();
        editTurntableFrame.setVisible(true);
        editTurntableOpen = true;
    }

    //Remove old rays and add them back in
    private void updateRayPanel() {
        for (Component comp : editTurntableRayPanel.getComponents()) {
            editTurntableRayPanel.remove(comp);
        }

        editTurntableRayPanel.setLayout(new BoxLayout(editTurntableRayPanel, BoxLayout.Y_AXIS));
        for (LayoutTurntable.RayTrack rt : layoutTurntable.getRayList()) {
            editTurntableRayPanel.add(new TurntableRayPanel(rt));
        }
        editTurntableRayPanel.revalidate();
        editTurntableRayPanel.repaint();
        editTurntableFrame.pack();
    }

    private void saveRayPanelDetail() {
        for (Component comp : editTurntableRayPanel.getComponents()) {
            if (comp instanceof TurntableRayPanel) {
                TurntableRayPanel trp = (TurntableRayPanel) comp;
                trp.updateDetails();
            }
        }
    }

    private void addRayTrackPressed(ActionEvent a) {
        double ang = 0.0;
        try {
            ang = Float.parseFloat(editTurntableAngleTextField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(editTurntableFrame, Bundle.getMessage("EntryError") + ": "
                    + e + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        layoutTurntable.addRay(ang);
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
        editTurntableNeedsRedraw = false;
    }

    void deleteRayTrackPressed(ActionEvent a) {
        double ang = 0.0;
        try {
            ang = Float.parseFloat(editTurntableAngleTextField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(editTurntableFrame, Bundle.getMessage("EntryError") + ": "
                    + e + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // scan rays to find the one to delete
        LayoutTurntable.RayTrack closest = null;
        double bestDel = 360.0;
        for (LayoutTurntable.RayTrack rt : layoutTurntable.getRayList()) {
            double del = MathUtil.absDiffAngleDEG(rt.getAngle(), ang);
            if (del < bestDel) {
                bestDel = del;
                closest = rt;
            }
        }
        if (bestDel > 30.0) {
            JOptionPane.showMessageDialog(editTurntableFrame, Bundle.getMessage("Error13"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        layoutTurntable.deleteRay(closest);
    }

    void turntableEditDonePressed(ActionEvent a) {
        // check if new radius was entered
        String str = editTurntableRadiusTextField.getText();
        if (!str.equals(editTurntableOldRadius)) {
            double rad = 0.0;
            try {
                rad = Float.parseFloat(str);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(editTurntableFrame, Bundle.getMessage("EntryError") + ": "
                        + e + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            layoutTurntable.setRadius(rad);
            editTurntableNeedsRedraw = true;
        }
        // clean up
        editTurntableOpen = false;
        editTurntableFrame.setVisible(false);
        editTurntableFrame.dispose();
        editTurntableFrame = null;
        saveRayPanelDetail();
        if (editTurntableNeedsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editTurntableNeedsRedraw = false;
        }
    }

    void turntableEditCancelPressed(ActionEvent a) {
        editTurntableOpen = false;
        editTurntableFrame.setVisible(false);
        editTurntableFrame.dispose();
        editTurntableFrame = null;
        if (editTurntableNeedsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editTurntableNeedsRedraw = false;
        }
    }

    /*===================*\
    | Turntable Ray Panel |
    \*===================*/
    public class TurntableRayPanel extends JPanel {

        // variables for Edit Turntable ray pane
        private RayTrack rayTrack = null;
        private JPanel rayTurnoutPanel;
        private transient JmriBeanComboBox turnoutNameComboBox;
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

            /*JLabel lbl = new JLabel("Index :"+connectionIndex);
                 top.add(lbl);*/
            top.add(new JLabel(Bundle.getMessage("RayAngle") + " : "));
            top.add(rayAngleTextField = new JTextField(5));
            rayAngleTextField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        Float.parseFloat(rayAngleTextField.getText());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(editTurntableFrame, Bundle.getMessage("EntryError") + ": "
                                + ex + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            );
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(top);

            turnoutNameComboBox = new JmriBeanComboBox(
                    InstanceManager.turnoutManagerInstance(),
                    rayTrack.getTurnout(),
                    JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(turnoutNameComboBox, true, true);
            turnoutNameComboBox.setSelectedBean(rayTrack.getTurnout());

            String turnoutStateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
            String turnoutStateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
            String[] turnoutStates = new String[]{turnoutStateClosed, turnoutStateThrown};

            rayTurnoutStateComboBox = new JComboBox<String>(turnoutStates);
            rayTurnoutStateLabel = new JLabel(Bundle.getMessage("TurnoutState"));
            rayTurnoutPanel = new JPanel();

            rayTurnoutPanel.setBorder(new EtchedBorder());
            rayTurnoutPanel.add(turnoutNameComboBox);
            rayTurnoutPanel.add(rayTurnoutStateLabel);
            rayTurnoutPanel.add(rayTurnoutStateComboBox);
            if (rayTrack.getTurnoutState() == Turnout.CLOSED) {
                rayTurnoutStateComboBox.setSelectedItem(turnoutStateClosed);
            } else {
                rayTurnoutStateComboBox.setSelectedItem(turnoutStateThrown);
            }
            this.add(rayTurnoutPanel);

            JButton deleteRayButton;
            top.add(deleteRayButton = new JButton(Bundle.getMessage("ButtonDelete")));
            deleteRayButton.setToolTipText(Bundle.getMessage("DeleteRayTrack"));
            deleteRayButton.addActionListener((ActionEvent e) -> {
                delete();
                updateRayPanel();
            });
            rayTitledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));

            this.setBorder(rayTitledBorder);

            showTurnoutDetails();

            rayAngleTextField.setText(twoDForm.format(rayTrack.getAngle()));
            rayTitledBorder.setTitle(Bundle.getMessage("Ray") + " : " + rayTrack.getConnectionIndex());
            if (rayTrack.getConnect() == null) {
                rayTitledBorder.setTitle(Bundle.getMessage("Unconnected") + " : " + rayTrack.getConnectionIndex());
            } else if (rayTrack.getConnect().getLayoutBlock() != null) {
                rayTitledBorder.setTitle(Bundle.getMessage("Connected") + " : " + rayTrack.getConnect().getLayoutBlock().getDisplayName());
            }
        }

        private void delete() {
            int n = JOptionPane.showConfirmDialog(null,
                    Bundle.getMessage("Question7"),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.NO_OPTION) {
                return;
            }
            layoutTurntable.deleteRay(rayTrack);
        }

        private void updateDetails() {
            if (turnoutNameComboBox == null || rayTurnoutStateComboBox == null) {
                return;
            }
            rayTrack.setTurnout(turnoutNameComboBox.getDisplayName(), rayTurnoutStateValues[rayTurnoutStateComboBox.getSelectedIndex()]);
            if (!rayAngleTextField.getText().equals(twoDForm.format(rayTrack.getAngle()))) {
                try {
                    double ang = Float.parseFloat(rayAngleTextField.getText());
                    rayTrack.setAngle(ang);
                } catch (Exception e) {
                    log.error("Angle is not in correct format so will skip " + rayAngleTextField.getText());
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
    }
    private final static Logger log = LoggerFactory.getLogger(LayoutTrackEditors.class);
}
