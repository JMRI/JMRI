package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

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
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.annotation.*;
import javax.swing.*;
import javax.swing.border.*;
import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.layoutEditor.*;
import jmri.jmrit.display.layoutEditor.LayoutTurntable.RayTrack;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;

/**
 * MVC Editor component for LayoutTurnout objects.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutTurnoutEditor extends LayoutTrackEditor {

    /**
     * constructor method
     */
    public LayoutTurnoutEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
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
    private final NamedBeanComboBox<Block> editLayoutTurnoutBlockNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> editLayoutTurnoutBlockBNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> editLayoutTurnoutBlockCNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> editLayoutTurnoutBlockDNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private final JComboBox<String> editLayoutTurnoutStateComboBox = new JComboBox<>();
    private JCheckBox editLayoutTurnoutHiddenCheckBox = null;
    private JButton editLayoutTurnoutBlockButton;
    private JButton editLayoutTurnoutBlockBButton;
    private JButton editLayoutTurnoutBlockCButton;
    private JButton editLayoutTurnoutBlockDButton;
    private final JCheckBox editLayoutTurnout2ndTurnoutCheckBox = new JCheckBox(Bundle.getMessage("SupportingTurnout"));  // NOI18N
    private final JCheckBox editLayoutTurnout2ndTurnoutInvertCheckBox = new JCheckBox(Bundle.getMessage("SecondTurnoutInvert"));  // NOI18N

    private boolean editLayoutTurnoutOpen = false;
    private boolean editLayoutTurnoutNeedRedraw = false;
    private boolean editLayoutTurnoutNeedsBlockUpdate = false;
    private int editLayoutTurnoutClosedIndex;
    private int editLayoutTurnoutThrownIndex;

    /**
     * Edit a Layout Turnout.
     * Invoked for any of the subtypes, has conditional code for crossovers
     */
    public void editLayoutTrack(@Nonnull LayoutTrack layoutTrack) {
        if ( layoutTrack instanceof LayoutTurnout ) {
            this.layoutTurnout = (LayoutTurnout) layoutTrack;
        } else {
            log.error("editLayoutTrack called with wrong type {}", layoutTurnout, new Exception("traceback"));
        }
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
            editLayoutTurnout2ndTurnoutInvertCheckBox.addActionListener((ActionEvent e) -> layoutTurnout.setSecondTurnoutInverted(editLayoutTurnout2ndTurnoutInvertCheckBox.isSelected()));
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
            editLayoutTurnoutBlockButton.addActionListener(this::editLayoutTurnoutEditBlockPressed);
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
                editLayoutTurnoutBlockBButton.addActionListener(this::editLayoutTurnoutEditBlockBPressed);
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
                editLayoutTurnoutBlockCButton.addActionListener(this::editLayoutTurnoutEditBlockCPressed);
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
                editLayoutTurnoutBlockDButton.addActionListener(this::editLayoutTurnoutEditBlockDPressed);
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
        if ((layoutTurnout.getTurnoutType() == LayoutTurnout.TurnoutType.RH_TURNOUT)
                || (layoutTurnout.getTurnoutType() == LayoutTurnout.TurnoutType.LH_TURNOUT)
                || (layoutTurnout.getTurnoutType() == LayoutTurnout.TurnoutType.WYE_TURNOUT)) {
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
    

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurnoutEditor.class);
}
