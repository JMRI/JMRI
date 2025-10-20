package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import jmri.NamedBean.DisplayOptions;
import jmri.*;
import jmri.jmrit.display.layoutEditor.*;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.SignalMastIcon;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

/**
 * MVC Editor component for PositionablePoint objects.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 *
 */
public class LayoutTraverserEditor extends LayoutTrackEditor {

    /**
     * constructor method.
     * @param layoutEditor main layout editor.
     */
    public LayoutTraverserEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    /*==============*\
    | Edit Traverser |
    \*==============*/
    // variables for Edit Traverser pane
    private LayoutTraverser layoutTraverser = null;
    private LayoutTraverserView layoutTraverserView = null;

    private JmriJFrame editLayoutTraverserFrame = null;
    private final JTextField editLayoutTraverserRadiusTextField = new JTextField(8);
    private final JTextField editLayoutTraverserAngleTextField = new JTextField(8);
    private final NamedBeanComboBox<Block> editLayoutTraverserBlockNameComboBox = new NamedBeanComboBox<>(
             InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private JButton editLayoutTraverserSegmentEditBlockButton;

    private JPanel editLayoutTraverserRayPanel;
    private JButton editLayoutTraverserAddRayTrackButton;
    private JCheckBox editLayoutTraverserDccControlledCheckBox;
    private JCheckBox editLayoutTraverserUseSignalMastsCheckBox;
    private JPanel signalMastParametersPanel;
    private NamedBeanComboBox<SignalMast> exitMastComboBox;
    private NamedBeanComboBox<SignalMast> bufferMastComboBox;

    private String editLayoutTraverserOldRadius = "";
    private final List<NamedBeanComboBox<SignalMast>> approachMastComboBoxes = new ArrayList<>();
    private final java.util.Set<SignalMast> mastsUsedElsewhere = new java.util.HashSet<>();
    private boolean editLayoutTraverserOpen = false;
    private boolean editLayoutTraverserNeedsRedraw = false;

    private JRadioButton doNotPlaceIcons;
    private JRadioButton placeIconsLeft;
    private JRadioButton placeIconsRight;

    private final List<Turnout> traverserTurnouts = new ArrayList<>();

    /**
     * Edit a Traverser.
     */
    @Override
    public void editLayoutTrack(@Nonnull LayoutTrackView layoutTrackView) {
        if ( layoutTrackView instanceof LayoutTraverserView ) {
            this.layoutTraverserView = (LayoutTraverserView) layoutTrackView;
            this.layoutTraverser = this.layoutTraverserView.getTraverser();
        } else {
            log.error("editLayoutTrack called with wrong type {}", layoutTrackView, new Exception("traceback"));
        }
        sensorList.clear();

        if (editLayoutTraverserOpen) {
            editLayoutTraverserFrame.setVisible(true);
        } else // Initialize if needed
        if (editLayoutTraverserFrame == null) {
            editLayoutTraverserFrame = new JmriJFrame(Bundle.getMessage("EditTraverser"), false, true);  // NOI18N
            editLayoutTraverserFrame.addHelpMenu("package.jmri.jmrit.display.EditTraverser", true);  // NOI18N
            editLayoutTraverserFrame.setLocation(50, 30);

            Container contentPane = editLayoutTraverserFrame.getContentPane();
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
            JLabel radiusLabel = new JLabel(Bundle.getMessage("TraverserRadius"));  // NOI18N
            panel1.add(radiusLabel);
            radiusLabel.setLabelFor(editLayoutTraverserRadiusTextField);
            panel1.add(editLayoutTraverserRadiusTextField);
            editLayoutTraverserRadiusTextField.setToolTipText(Bundle.getMessage("TraverserRadiusHint"));  // NOI18N
            headerPane.add(panel1);

            // setup ray track angle text field
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel rayAngleLabel = new JLabel(Bundle.getMessage("RayAngle"));  // NOI18N
            panel2.add(rayAngleLabel);
            rayAngleLabel.setLabelFor(editLayoutTraverserAngleTextField);
            panel2.add(editLayoutTraverserAngleTextField);
            editLayoutTraverserAngleTextField.setToolTipText(Bundle.getMessage("RayAngleHint"));  // NOI18N
            headerPane.add(panel2);

            // setup block name
            JPanel panel2a = new JPanel();
            panel2a.setLayout(new FlowLayout());
            JLabel blockNameLabel = new JLabel(Bundle.getMessage("BlockID"));  // NOI18N
            panel2a.add(blockNameLabel);
            blockNameLabel.setLabelFor(editLayoutTraverserBlockNameComboBox);
            LayoutEditor.setupComboBox(editLayoutTraverserBlockNameComboBox, false, true, true);
            editLayoutTraverserBlockNameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));  // NOI18N
            panel2a.add(editLayoutTraverserBlockNameComboBox);

            // Edit Block
            panel2a.add(editLayoutTraverserSegmentEditBlockButton = new JButton(Bundle.getMessage("EditBlock", "")));  // NOI18N
            editLayoutTraverserSegmentEditBlockButton.addActionListener(this::editLayoutTraverserEditBlockPressed);
            editLayoutTraverserSegmentEditBlockButton.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1  // NOI18N
            headerPane.add(panel2a);

            // setup add ray track button
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            panel3.add(editLayoutTraverserAddRayTrackButton = new JButton(Bundle.getMessage("AddRayTrack")));  // NOI18N
            editLayoutTraverserAddRayTrackButton.setToolTipText(Bundle.getMessage("AddRayTrackHint"));  // NOI18N
            editLayoutTraverserAddRayTrackButton.addActionListener((ActionEvent e) -> {
                addRayTrackPressed(e);
                updateRayPanel();
            });

            panel3.add(editLayoutTraverserDccControlledCheckBox = new JCheckBox(Bundle.getMessage("TraverserDCCControlled")));  // NOI18N
            headerPane.add(panel3);

            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
            panel4.add(editLayoutTraverserUseSignalMastsCheckBox = new JCheckBox(Bundle.getMessage("TraverserUseSignalMasts"))); // NOI18N
            headerPane.add(panel4);

            // setup signal mast parameters panel
            signalMastParametersPanel = new JPanel();
            signalMastParametersPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TraverserSignalMastAssignmentsTitle"))); // NOI18N
            exitMastComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalMastManager.class), null, DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(exitMastComboBox, false, true, true);
            exitMastComboBox.setEditable(false);
            exitMastComboBox.setAllowNull(true);
            bufferMastComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalMastManager.class), null, DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(bufferMastComboBox, false, true, true);
            bufferMastComboBox.setEditable(false);
            bufferMastComboBox.setAllowNull(true);

            // set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            addDoneCancelButtons(panel5, editLayoutTraverserFrame.getRootPane(),
                    this::editLayoutTraverserDonePressed, this::traverserEditCancelPressed);
            footerPane.add(panel5);

            editLayoutTraverserRayPanel = new JPanel();
            editLayoutTraverserRayPanel.setLayout(new BoxLayout(editLayoutTraverserRayPanel, BoxLayout.Y_AXIS));
            JScrollPane rayScrollPane = new JScrollPane(editLayoutTraverserRayPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            contentPane.add(rayScrollPane, BorderLayout.CENTER);
        }

        editLayoutTraverserBlockNameComboBox.setSelectedIndex(-1);
        LayoutBlock lb = layoutTraverser.getLayoutBlock();
        if (lb != null) {
            Block blk = lb.getBlock();
            if (blk != null) {
                editLayoutTraverserBlockNameComboBox.setSelectedItem(blk);
            }
        }

        editLayoutTraverserDccControlledCheckBox.setSelected(layoutTraverser.isTurnoutControlled());
        editLayoutTraverserUseSignalMastsCheckBox.setSelected(layoutTraverser.isDispatcherManaged());
        editLayoutTraverserDccControlledCheckBox.addActionListener((ActionEvent e) -> {
            layoutTraverser.setTurnoutControlled(editLayoutTraverserDccControlledCheckBox.isSelected());

            for (Component comp : editLayoutTraverserRayPanel.getComponents()) {
                if (comp instanceof TraverserRayPanel) {
                    TraverserRayPanel trp = (TraverserRayPanel) comp;
                    trp.showTurnoutDetails();
                }
            }
            editLayoutTraverserFrame.pack();
        });

        signalMastParametersPanel.setVisible(layoutTraverser.isDispatcherManaged());
        editLayoutTraverserUseSignalMastsCheckBox.addActionListener((ActionEvent e) -> {
            boolean isSelected = editLayoutTraverserUseSignalMastsCheckBox.isSelected();
            layoutTraverser.setDispatcherManaged(isSelected); // Update the model
            signalMastParametersPanel.setVisible(layoutTraverser.isDispatcherManaged());
            updateRayPanel(); // Rebuild to show/hide mast details, which also handles visibility
            editLayoutTraverserFrame.pack();
        });

        // Cache the list of masts used on other parts of the layout.
        // This is the performance-critical change, preventing a full layout scan on every click.
        mastsUsedElsewhere.clear();
        layoutEditor.getLETools().createListUsedSignalMasts();
        mastsUsedElsewhere.addAll(layoutEditor.getLETools().usedMasts);

        // Remove masts assigned to the current traverser from the "used elsewhere" list
        if (layoutTraverser.getBufferMast() != null) {
            mastsUsedElsewhere.remove(layoutTraverser.getBufferMast());
        }
        if (layoutTraverser.getExitSignalMast() != null) {
            mastsUsedElsewhere.remove(layoutTraverser.getExitSignalMast());
        }
        for (LayoutTraverser.RayTrack ray : layoutTraverser.getRayTrackList()) {
            if (ray.getApproachMast() != null) {
                mastsUsedElsewhere.remove(ray.getApproachMast());
            }
        }

        exitMastComboBox.setExcludedItems(mastsUsedElsewhere);
        exitMastComboBox.addActionListener(e -> {
            SignalMast newMast = exitMastComboBox.getSelectedItem();
            layoutTraverser.setExitSignalMast( (newMast != null) ? newMast.getSystemName() : null );
        });

        bufferMastComboBox.setExcludedItems(mastsUsedElsewhere);
        bufferMastComboBox.addActionListener(e -> {
            SignalMast newMast = bufferMastComboBox.getSelectedItem();
            layoutTraverser.setBufferSignalMast( (newMast != null) ? newMast.getSystemName() : null );
        });

        // Set up for Edit
        editLayoutTraverserRadiusTextField.setText(" " + layoutTraverser.getRadius());
        editLayoutTraverserOldRadius = editLayoutTraverserRadiusTextField.getText();
        editLayoutTraverserAngleTextField.setText("0");
        editLayoutTraverserFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                traverserEditCancelPressed(null);
            }
        });
        updateRayPanel();
        SignalMast exitMast = layoutTraverser.getExitSignalMast();
        SignalMast bufferMast = layoutTraverser.getBufferMast();
        if (exitMast != null) {
            exitMastComboBox.setSelectedItem(exitMast);
        }
        if (bufferMast != null) {
            bufferMastComboBox.setSelectedItem(bufferMast);
        }

        editLayoutTraverserFrame.pack();
        editLayoutTraverserFrame.setVisible(true);
        editLayoutTraverserOpen = true;
    }   // editLayoutTraverser

    @InvokeOnGuiThread
    private void editLayoutTraverserEditBlockPressed(ActionEvent a) {
         // check if a block name has been entered
         String newName = editLayoutTraverserBlockNameComboBox.getSelectedItemDisplayName();
         if (newName == null) {
             newName = "";
         }
         if ((layoutTraverser.getBlockName().isEmpty())
                 || !layoutTraverser.getBlockName().equals(newName)) {
             // get new block, or null if block has been removed
             layoutTraverser.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
             editLayoutTraverserNeedsRedraw = true;
             ///layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
             ///layoutTraverser.updateBlockInfo();
         }
         // check if a block exists to edit
         LayoutBlock blockToEdit = layoutTraverser.getLayoutBlock();
         if (blockToEdit == null) {
             JmriJOptionPane.showMessageDialog(editLayoutTraverserFrame,
                     Bundle.getMessage("Error1"), // NOI18N
                     Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
             return;
         }
         blockToEdit.editLayoutBlock(editLayoutTraverserFrame);
         layoutEditor.setDirty();
         editLayoutTraverserNeedsRedraw = true;
     }

    // Remove old rays and add them back in
    private void updateRayPanel() {
        // Create list of turnouts to be retained in the NamedBeanComboBox
        traverserTurnouts.clear();
        layoutTraverser.getRayTrackList().forEach(rt -> traverserTurnouts.add(rt.getTurnout()));

        editLayoutTraverserRayPanel.removeAll();
        editLayoutTraverserRayPanel.setLayout(new BoxLayout(editLayoutTraverserRayPanel, BoxLayout.Y_AXIS));
        for (LayoutTraverser.RayTrack rt : layoutTraverser.getRayTrackList()) {
            editLayoutTraverserRayPanel.add(new TraverserRayPanel(rt));
        }

        // Rebuild signal mast panel
        signalMastParametersPanel.removeAll();
        approachMastComboBoxes.clear();

        if (layoutTraverser.isDispatcherManaged()) {
            signalMastParametersPanel.setLayout(new BoxLayout(signalMastParametersPanel, BoxLayout.Y_AXIS));

            // Add approach masts for each ray
            for (LayoutTraverser.RayTrack rt : layoutTraverser.getRayTrackList()) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
                p.add(new JLabel(Bundle.getMessage("ApproachMastRay", rt.getConnectionIndex() + 1)));
                NamedBeanComboBox<SignalMast> combo = new NamedBeanComboBox<>(
                        InstanceManager.getDefault(SignalMastManager.class), rt.getApproachMast(), DisplayOptions.DISPLAYNAME);
                LayoutEditor.setupComboBox(combo, false, true, true);
                combo.setEditable(false);
                combo.setAllowNull(true);
                p.add(combo);
                signalMastParametersPanel.add(p);
                approachMastComboBoxes.add(combo);
            }

            // Add action listeners now that the list is complete
            for (int i = 0; i < approachMastComboBoxes.size(); i++) {
                final int index = i; // final variable for use in lambda
                approachMastComboBoxes.get(i).addActionListener(e -> {
                    SignalMast newMast = approachMastComboBoxes.get(index).getSelectedItem();
                    layoutTraverser.getRayTrackList().get(index).setApproachMast( (newMast != null) ? newMast.getSystemName() : null );
                });
            }
            if (!approachMastComboBoxes.isEmpty()) {
                signalMastParametersPanel.add(new JSeparator());
            }

            // Add shared icon placement controls
            JPanel placementPanel = new JPanel();
            placementPanel.setLayout(new BoxLayout(placementPanel, BoxLayout.Y_AXIS)); // NOI18N
            placementPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TraverserAddMastIconsTitle")));

            doNotPlaceIcons = new JRadioButton(Bundle.getMessage("DoNotPlace")); // NOI18N
            placeIconsLeft = new JRadioButton(Bundle.getMessage("LeftHandSide")); // NOI18N
            placeIconsRight = new JRadioButton(Bundle.getMessage("RightHandSide")); // NOI18N
            ButtonGroup bg = new ButtonGroup();
            bg.add(doNotPlaceIcons);
            bg.add(placeIconsLeft);
            bg.add(placeIconsRight);
            switch (layoutTraverser.getSignalIconPlacement()) {
                case 1:
                    placeIconsLeft.setSelected(true);
                    break;
                case 2:
                    placeIconsRight.setSelected(true);
                    break;
                default: doNotPlaceIcons.setSelected(true);
            }

            JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            radioPanel.add(doNotPlaceIcons);
            radioPanel.add(placeIconsLeft);
            radioPanel.add(placeIconsRight);
            placementPanel.add(radioPanel);
            signalMastParametersPanel.add(placementPanel);

            signalMastParametersPanel.add(new JSeparator());

            JPanel mastPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(2, 2, 2, 2); // Default insets
            mastPanel.add(new JLabel(Bundle.getMessage("TraverserExitMastLabel")), c);
            c.gridx = 1;
            c.insets = new Insets(2, 5, 2, 2); // Add left padding
            mastPanel.add(exitMastComboBox, c);

            c.gridx = 0;
            c.gridy = 1;
            c.insets = new Insets(2, 2, 2, 2); // Reset for label
            mastPanel.add(new JLabel(Bundle.getMessage("TraverserBufferMastLabel")), c);
            c.gridx = 1;
            c.insets = new Insets(2, 5, 2, 2); // Add left padding
            mastPanel.add(bufferMastComboBox, c);

            signalMastParametersPanel.add(mastPanel);
            editLayoutTraverserRayPanel.add(signalMastParametersPanel);
        }
        editLayoutTraverserRayPanel.revalidate();
        editLayoutTraverserRayPanel.repaint();
        editLayoutTraverserFrame.pack();
    }

    private void saveRayPanelDetail() {
        for (Component comp : editLayoutTraverserRayPanel.getComponents()) {
            if (comp instanceof TraverserRayPanel) {
                TraverserRayPanel trp = (TraverserRayPanel) comp;
                trp.updateDetails();
            }
        }
    }

    private void addRayTrackPressed(ActionEvent a) {
        double ang = 0.0;
        try {
            ang = Float.parseFloat(editLayoutTraverserAngleTextField.getText());
        } catch (Exception e) {
            JmriJOptionPane.showMessageDialog(editLayoutTraverserFrame, Bundle.getMessage("EntryError") + ": "
                    + e + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        layoutTraverser.addRay(ang);
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
        editLayoutTraverserNeedsRedraw = false;
    }

    private void editLayoutTraverserDonePressed(ActionEvent a) {
        // check if Block changed
        String newName = editLayoutTraverserBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }

        if ((layoutTraverser.getBlockName().isEmpty()) || !layoutTraverser.getBlockName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutTraverser.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editLayoutTraverserNeedsRedraw = true;
            ///layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            ///layoutTraverser.updateBlockInfo();
        }

        layoutTraverser.setDispatcherManaged(editLayoutTraverserUseSignalMastsCheckBox.isSelected());
        if (editLayoutTraverserUseSignalMastsCheckBox.isSelected()) {
            String exitMastName = exitMastComboBox.getSelectedItemDisplayName();
            String bufferMastName = bufferMastComboBox.getSelectedItemDisplayName();
            layoutTraverser.setExitSignalMast(exitMastName);
            layoutTraverser.setBufferSignalMast(bufferMastName);

            for (int i = 0; i < approachMastComboBoxes.size(); i++) {
                LayoutTraverser.RayTrack ray = layoutTraverser.getRayTrackList().get(i);
                NamedBeanComboBox<SignalMast> combo = approachMastComboBoxes.get(i);
                ray.setApproachMast(combo.getSelectedItemDisplayName());
            }

            // Always remove any existing icons for this traverser's approach masts first.
            // This ensures that moving icons from right-to-left works correctly.
            List<SignalMastIcon> iconsToRemove = new ArrayList<>();
            for (Positionable p : layoutEditor.getContents()) {
                if (p instanceof SignalMastIcon) {
                    SignalMastIcon icon = (SignalMastIcon) p;
                    if (layoutTraverser.isApproachMast(icon.getSignalMast())) {
                        iconsToRemove.add(icon);
                    }
                }
            }
            for (SignalMastIcon icon : iconsToRemove) {
                icon.remove();
                editLayoutTraverserNeedsRedraw = true;
            }

            // Now, if requested, place the new icons.
            if (!doNotPlaceIcons.isSelected()) { // placeIconsLeft or placeIconsRight is selected
                for (int i = 0; i < approachMastComboBoxes.size(); i++) {
                    LayoutTraverser.RayTrack ray = layoutTraverser.getRayTrackList().get(i);
                    SignalMast mast = approachMastComboBoxes.get(i).getSelectedItem();
                    if (mast != null) {
                        if (ray.getConnect() != null) {
                            SignalMastIcon icon = new SignalMastIcon(layoutEditor);
                            icon.setSignalMast(mast.getDisplayName());
                            log.info("Placing mast for traverser ray, connected to track segment: {}", ray.getConnect().getName()); // NOI18N
                            layoutEditor.getLETools().placingBlockForTurntable(icon, placeIconsRight.isSelected(), 0.0,
                                    ray.getConnect(), layoutTraverserView.getRayCoordsIndexed(ray.getConnectionIndex()));
                            editLayoutTraverserNeedsRedraw = true;
                        }
                    }
                }
            }

            // Save placement selection
            int placementSelection;
            if (placeIconsLeft.isSelected()) {
                placementSelection = 1;
            } else if (placeIconsRight.isSelected()) {
                placementSelection = 2;
            } else {
                placementSelection = 0;
            }
            layoutTraverser.setSignalIconPlacement(placementSelection);
        }

        // check if new radius was entered
        String str = editLayoutTraverserRadiusTextField.getText();
        if (!str.equals(editLayoutTraverserOldRadius)) {
            double rad = 0.0;
            try {
                rad = Float.parseFloat(str);
            } catch (Exception e) {
                JmriJOptionPane.showMessageDialog(editLayoutTraverserFrame, Bundle.getMessage("EntryError") + ": " // NOI18N
                        + e + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"), // NOI18N
                        JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            layoutTraverser.setRadius(rad);
            editLayoutTraverserNeedsRedraw = true;
        }
        // clean up
        saveRayPanelDetail();
        editLayoutTraverserOpen = false;
        editLayoutTraverserFrame.setVisible(false);
        editLayoutTraverserFrame.dispose();
        editLayoutTraverserFrame = null;
        if (editLayoutTraverserNeedsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editLayoutTraverserNeedsRedraw = false;
        }
    }

    private void traverserEditCancelPressed(ActionEvent a) {
        editLayoutTraverserOpen = false;
        editLayoutTraverserFrame.setVisible(false);
        editLayoutTraverserFrame.dispose();
        editLayoutTraverserFrame = null;
        if (editLayoutTraverserNeedsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editLayoutTraverserNeedsRedraw = false;
        }
    }

    /*===================*\
    | Traverser Ray Panel |
    \*===================*/
    public class TraverserRayPanel extends JPanel {

        // variables for Edit Traverser ray pane
        private LayoutTraverser.RayTrack rayTrack = null;
        private final JPanel rayTurnoutPanel;
        private final NamedBeanComboBox<Turnout> turnoutNameComboBox;
        private final TitledBorder rayTitledBorder;
        private final JComboBox<String> rayTurnoutStateComboBox;
        private final JLabel rayTurnoutStateLabel;
        private final JTextField rayAngleTextField;
        private final int[] rayTurnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};
        private final DecimalFormat twoDForm = new DecimalFormat("#.00");

        /**
         * constructor method.
         * @param rayTrack the single ray track to edit.
         */
        public TraverserRayPanel(@Nonnull LayoutTraverser.RayTrack rayTrack) {
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
                        JmriJOptionPane.showMessageDialog(editLayoutTraverserFrame, Bundle.getMessage("EntryError") + ": " // NOI18N
                                + ex + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"), // NOI18N
                                JmriJOptionPane.ERROR_MESSAGE);
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
                    layoutEditor.newTurnoutComboBoxPopupMenuListener(turnoutNameComboBox, traverserTurnouts));
            String turnoutStateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
            String turnoutStateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
            String[] turnoutStates = new String[]{turnoutStateClosed, turnoutStateThrown};

            rayTurnoutStateComboBox = new JComboBox<>(turnoutStates);
            rayTurnoutStateLabel = new JLabel(Bundle.getMessage("TurnoutState"));  // NOI18N
            rayTurnoutPanel = new JPanel();

            rayTurnoutPanel.setBorder(new EtchedBorder());
            JLabel turnoutLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))); // NOI18N
            rayTurnoutPanel.add(turnoutLabel);
            turnoutLabel.setLabelFor(turnoutNameComboBox);
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
            int rayNumber = rayTrack.getConnectionIndex() + 1;
            rayTitledBorder.setTitle(Bundle.getMessage("Ray") + " : " + rayNumber);  // NOI18N
            if (rayTrack.getConnect() == null) {
                rayTitledBorder.setTitle(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("Unconnected")) + " "
                        + rayNumber);  // NOI18N
            } else if (rayTrack.getConnect().getLayoutBlock() != null) {
                rayTitledBorder.setTitle(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("Connected")) + " "
                        + rayTrack.getConnect().getLayoutBlock().getDisplayName()
                        + " (" + Bundle.getMessage("Ray") + " " + rayNumber + ")");  // NOI18N
            }
        }

        private void delete() {
            int n = JmriJOptionPane.showConfirmDialog(null,
                    Bundle.getMessage("Question7"), // NOI18N
                    Bundle.getMessage("WarningTitle"), // NOI18N
                    JmriJOptionPane.YES_NO_OPTION);
            if (n == JmriJOptionPane.YES_OPTION) {
                layoutTraverser.deleteRay(rayTrack);
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
                    log.error("Angle is not in correct format so will skip {}", rayAngleTextField.getText());  // NOI18N
                }
            }
        }

        private void showTurnoutDetails() {
            boolean vis = layoutTraverser.isTurnoutControlled();
            rayTurnoutPanel.setVisible(vis);
            turnoutNameComboBox.setVisible(vis);
            rayTurnoutStateComboBox.setVisible(vis);
            rayTurnoutStateLabel.setVisible(vis);
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTraverserEditor.class);
}
