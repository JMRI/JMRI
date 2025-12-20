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
public class LayoutTurntableEditor extends LayoutTrackEditor {

    /**
     * constructor method.
     * @param layoutEditor main layout editor.
     */
    public LayoutTurntableEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    /*==============*\
    | Edit Turntable |
    \*==============*/
    // variables for Edit Turntable pane
    private LayoutTurntable layoutTurntable = null;
    private LayoutTurntableView layoutTurntableView = null;

    private JmriJFrame editLayoutTurntableFrame = null;
    private final JTextField editLayoutTurntableRadiusTextField = new JTextField(8);
    private final JTextField editLayoutTurntableAngleTextField = new JTextField(8);
    private final NamedBeanComboBox<Block> editLayoutTurntableBlockNameComboBox = new NamedBeanComboBox<>(
             InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private JButton editLayoutTurntableSegmentEditBlockButton;

    private JPanel editLayoutTurntableRayPanel;
    private JButton editLayoutTurntableAddRayTrackButton;
    private JCheckBox editLayoutTurntableDccControlledCheckBox;
    private JCheckBox editLayoutTurntableUseSignalMastsCheckBox;
    private JPanel signalMastParametersPanel;
    private NamedBeanComboBox<SignalMast> exitMastComboBox;
    private NamedBeanComboBox<SignalMast> bufferMastComboBox;

    private String editLayoutTurntableOldRadius = "";
    private final List<NamedBeanComboBox<SignalMast>> approachMastComboBoxes = new ArrayList<>();
    private final java.util.Set<SignalMast> mastsUsedElsewhere = new java.util.HashSet<>();
    private boolean editLayoutTurntableOpen = false;
    private boolean editLayoutTurntableNeedsRedraw = false;

    private JRadioButton doNotPlaceIcons;
    private JRadioButton placeIconsLeft;
    private JRadioButton placeIconsRight;

    private final List<Turnout> turntableTurnouts = new ArrayList<>();

    /**
     * Edit a Turntable.
     */
    @Override
    public void editLayoutTrack(@Nonnull LayoutTrackView layoutTrackView) {
        if ( layoutTrackView instanceof LayoutTurntableView ) {
            this.layoutTurntableView = (LayoutTurntableView) layoutTrackView;
            this.layoutTurntable = this.layoutTurntableView.getTurntable();
        } else {
            log.error("editLayoutTrack called with wrong type {}", layoutTrackView, new Exception("traceback"));
        }
        sensorList.clear();

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

            // setup block name
            JPanel panel2a = new JPanel();
            panel2a.setLayout(new FlowLayout());
            JLabel blockNameLabel = new JLabel(Bundle.getMessage("BlockID"));  // NOI18N
            panel2a.add(blockNameLabel);
            blockNameLabel.setLabelFor(editLayoutTurntableBlockNameComboBox);
            LayoutEditor.setupComboBox(editLayoutTurntableBlockNameComboBox, false, true, true);
            editLayoutTurntableBlockNameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));  // NOI18N
            panel2a.add(editLayoutTurntableBlockNameComboBox);

            // Edit Block
            panel2a.add(editLayoutTurntableSegmentEditBlockButton = new JButton(Bundle.getMessage("EditBlock", "")));  // NOI18N
            editLayoutTurntableSegmentEditBlockButton.addActionListener(this::editLayoutTurntableEditBlockPressed);
            editLayoutTurntableSegmentEditBlockButton.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1  // NOI18N
            headerPane.add(panel2a);

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

            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
            panel4.add(editLayoutTurntableUseSignalMastsCheckBox = new JCheckBox(Bundle.getMessage("TurntableUseSignalMasts"))); // NOI18N
            headerPane.add(panel4);

            // setup signal mast parameters panel
            signalMastParametersPanel = new JPanel();
            signalMastParametersPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TurntableSignalMastAssignmentsTitle"))); // NOI18N
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
            addDoneCancelButtons(panel5, editLayoutTurntableFrame.getRootPane(),
                    this::editLayoutTurntableDonePressed, this::turntableEditCancelPressed);
            footerPane.add(panel5);

            editLayoutTurntableRayPanel = new JPanel();
            editLayoutTurntableRayPanel.setLayout(new BoxLayout(editLayoutTurntableRayPanel, BoxLayout.Y_AXIS));
            JScrollPane rayScrollPane = new JScrollPane(editLayoutTurntableRayPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            contentPane.add(rayScrollPane, BorderLayout.CENTER);
        }

        editLayoutTurntableBlockNameComboBox.setSelectedIndex(-1);
        LayoutBlock lb = layoutTurntable.getLayoutBlock();
        if (lb != null) {
            Block blk = lb.getBlock();
            if (blk != null) {
                editLayoutTurntableBlockNameComboBox.setSelectedItem(blk);
            }
        }

        editLayoutTurntableDccControlledCheckBox.setSelected(layoutTurntable.isTurnoutControlled());
        editLayoutTurntableUseSignalMastsCheckBox.setSelected(layoutTurntable.isDispatcherManaged());
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

        signalMastParametersPanel.setVisible(layoutTurntable.isDispatcherManaged());
        editLayoutTurntableUseSignalMastsCheckBox.addActionListener((ActionEvent e) -> {
            boolean isSelected = editLayoutTurntableUseSignalMastsCheckBox.isSelected();
            layoutTurntable.setDispatcherManaged(isSelected); // Update the model
            signalMastParametersPanel.setVisible(layoutTurntable.isDispatcherManaged());
            updateRayPanel(); // Rebuild to show/hide mast details, which also handles visibility
            editLayoutTurntableFrame.pack();
        });

        // Cache the list of masts used on other parts of the layout.
        // This is the performance-critical change, preventing a full layout scan on every click.
        mastsUsedElsewhere.clear();
        layoutEditor.getLETools().createListUsedSignalMasts();
        mastsUsedElsewhere.addAll(layoutEditor.getLETools().usedMasts);

        // Remove masts assigned to the current turntable from the "used elsewhere" list
        if (layoutTurntable.getBufferMast() != null) {
            mastsUsedElsewhere.remove(layoutTurntable.getBufferMast());
        }
        if (layoutTurntable.getExitSignalMast() != null) {
            mastsUsedElsewhere.remove(layoutTurntable.getExitSignalMast());
        }
        for (LayoutTurntable.RayTrack ray : layoutTurntable.getRayTrackList()) {
            if (ray.getApproachMast() != null) {
                mastsUsedElsewhere.remove(ray.getApproachMast());
            }
        }

        exitMastComboBox.setExcludedItems(mastsUsedElsewhere);
        exitMastComboBox.addActionListener(e -> {
            SignalMast newMast = exitMastComboBox.getSelectedItem();
            layoutTurntable.setExitSignalMast( (newMast != null) ? newMast.getSystemName() : null );
        });

        bufferMastComboBox.setExcludedItems(mastsUsedElsewhere);
        bufferMastComboBox.addActionListener(e -> {
            SignalMast newMast = bufferMastComboBox.getSelectedItem();
            layoutTurntable.setBufferSignalMast( (newMast != null) ? newMast.getSystemName() : null );
        });

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
        updateRayPanel();
        SignalMast exitMast = layoutTurntable.getExitSignalMast();
        SignalMast bufferMast = layoutTurntable.getBufferMast();
        if (exitMast != null) {
            exitMastComboBox.setSelectedItem(exitMast);
        }
        if (bufferMast != null) {
            bufferMastComboBox.setSelectedItem(bufferMast);
        }

        editLayoutTurntableFrame.pack();
        editLayoutTurntableFrame.setVisible(true);
        editLayoutTurntableOpen = true;
    }   // editLayoutTurntable

    @InvokeOnGuiThread
    private void editLayoutTurntableEditBlockPressed(ActionEvent a) {
         // check if a block name has been entered
         String newName = editLayoutTurntableBlockNameComboBox.getSelectedItemDisplayName();
         if (newName == null) {
             newName = "";
         }
         if ((layoutTurntable.getBlockName().isEmpty())
                 || !layoutTurntable.getBlockName().equals(newName)) {
             // get new block, or null if block has been removed
             layoutTurntable.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
             editLayoutTurntableNeedsRedraw = true;
             ///layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
             ///layoutTurntable.updateBlockInfo();
         }
         // check if a block exists to edit
         LayoutBlock blockToEdit = layoutTurntable.getLayoutBlock();
         if (blockToEdit == null) {
             JmriJOptionPane.showMessageDialog(editLayoutTurntableFrame,
                     Bundle.getMessage("Error1"), // NOI18N
                     Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
             return;
         }
         blockToEdit.editLayoutBlock(editLayoutTurntableFrame);
         layoutEditor.setDirty();
         editLayoutTurntableNeedsRedraw = true;
     }

    // Remove old rays and add them back in
    private void updateRayPanel() {
        // Create list of turnouts to be retained in the NamedBeanComboBox
        turntableTurnouts.clear();
        layoutTurntable.getRayTrackList().forEach(rt -> turntableTurnouts.add(rt.getTurnout()));

        editLayoutTurntableRayPanel.removeAll();
        editLayoutTurntableRayPanel.setLayout(new BoxLayout(editLayoutTurntableRayPanel, BoxLayout.Y_AXIS));
        for (LayoutTurntable.RayTrack rt : layoutTurntable.getRayTrackList()) {
            editLayoutTurntableRayPanel.add(new TurntableRayPanel(rt));
        }

        // Rebuild signal mast panel
        signalMastParametersPanel.removeAll();
        approachMastComboBoxes.clear();

        if (layoutTurntable.isDispatcherManaged()) {
            signalMastParametersPanel.setLayout(new BoxLayout(signalMastParametersPanel, BoxLayout.Y_AXIS));

            // Add approach masts for each ray
            for (LayoutTurntable.RayTrack rt : layoutTurntable.getRayTrackList()) {
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
                approachMastComboBoxes.get(i).setExcludedItems(mastsUsedElsewhere);
                approachMastComboBoxes.get(i).addActionListener(e -> {
                    SignalMast newMast = approachMastComboBoxes.get(index).getSelectedItem();
                    layoutTurntable.getRayTrackList().get(index).setApproachMast( (newMast != null) ? newMast.getSystemName() : null );
                });
            }
            if (!approachMastComboBoxes.isEmpty()) {
                signalMastParametersPanel.add(new JSeparator());
            }

            // Add shared icon placement controls
            JPanel placementPanel = new JPanel();
            placementPanel.setLayout(new BoxLayout(placementPanel, BoxLayout.Y_AXIS)); // NOI18N
            placementPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TurntableAddMastIconsTitle")));

            doNotPlaceIcons = new JRadioButton(Bundle.getMessage("DoNotPlace")); // NOI18N
            placeIconsLeft = new JRadioButton(Bundle.getMessage("LeftHandSide")); // NOI18N
            placeIconsRight = new JRadioButton(Bundle.getMessage("RightHandSide")); // NOI18N
            ButtonGroup bg = new ButtonGroup();
            bg.add(doNotPlaceIcons);
            bg.add(placeIconsLeft);
            bg.add(placeIconsRight);
            switch (layoutTurntable.getSignalIconPlacement()) {
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
            mastPanel.add(new JLabel(Bundle.getMessage("TurntableExitMastLabel")), c);
            c.gridx = 1;
            c.insets = new Insets(2, 5, 2, 2); // Add left padding
            mastPanel.add(exitMastComboBox, c);

            c.gridx = 0;
            c.gridy = 1;
            c.insets = new Insets(2, 2, 2, 2); // Reset for label
            mastPanel.add(new JLabel(Bundle.getMessage("TurntableBufferMastLabel")), c);
            c.gridx = 1;
            c.insets = new Insets(2, 5, 2, 2); // Add left padding
            mastPanel.add(bufferMastComboBox, c);

            signalMastParametersPanel.add(mastPanel);
            editLayoutTurntableRayPanel.add(signalMastParametersPanel);
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
            JmriJOptionPane.showMessageDialog(editLayoutTurntableFrame, Bundle.getMessage("EntryError") + ": "
                    + e + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        layoutTurntable.addRay(ang);
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
        editLayoutTurntableNeedsRedraw = false;
    }

    private void editLayoutTurntableDonePressed(ActionEvent a) {
        // check if Block changed
        String newName = editLayoutTurntableBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }

        if ((layoutTurntable.getBlockName().isEmpty()) || !layoutTurntable.getBlockName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutTurntable.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editLayoutTurntableNeedsRedraw = true;
            ///layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            ///layoutTurntable.updateBlockInfo();
        }

        layoutTurntable.setDispatcherManaged(editLayoutTurntableUseSignalMastsCheckBox.isSelected());
        if (editLayoutTurntableUseSignalMastsCheckBox.isSelected()) {
            String exitMastName = exitMastComboBox.getSelectedItemDisplayName();
            String bufferMastName = bufferMastComboBox.getSelectedItemDisplayName();
            layoutTurntable.setExitSignalMast(exitMastName);
            layoutTurntable.setBufferSignalMast(bufferMastName);

            for (int i = 0; i < approachMastComboBoxes.size(); i++) {
                LayoutTurntable.RayTrack ray = layoutTurntable.getRayTrackList().get(i);
                NamedBeanComboBox<SignalMast> combo = approachMastComboBoxes.get(i);
                ray.setApproachMast(combo.getSelectedItemDisplayName());
            }

            // Always remove any existing icons for this turntable's approach masts first.
            // This ensures that moving icons from right-to-left works correctly.
            List<SignalMastIcon> iconsToRemove = new ArrayList<>();
            for (Positionable p : layoutEditor.getContents()) {
                if (p instanceof SignalMastIcon) {
                    SignalMastIcon icon = (SignalMastIcon) p;
                    if (layoutTurntable.isApproachMast(icon.getSignalMast())) {
                        iconsToRemove.add(icon);
                    }
                }
            }
            for (SignalMastIcon icon : iconsToRemove) {
                icon.remove();
                editLayoutTurntableNeedsRedraw = true;
            }

            // Now, if requested, place the new icons.
            if (!doNotPlaceIcons.isSelected()) { // placeIconsLeft or placeIconsRight is selected
                for (int i = 0; i < approachMastComboBoxes.size(); i++) {
                    LayoutTurntable.RayTrack ray = layoutTurntable.getRayTrackList().get(i);
                    SignalMast mast = approachMastComboBoxes.get(i).getSelectedItem();
                    if (mast != null) {
                        if (ray.getConnect() != null) {
                            SignalMastIcon icon = new SignalMastIcon(layoutEditor);
                            icon.setSignalMast(mast.getDisplayName());
                            log.debug("Placing mast for turntable ray, connected to track segment: {}", ray.getConnect().getName()); // NOI18N
                            layoutEditor.getLETools().placingBlockForTurntable(icon, placeIconsRight.isSelected(), 0.0,
                                    ray.getConnect(), layoutTurntableView.getRayCoordsIndexed(ray.getConnectionIndex()));
                            editLayoutTurntableNeedsRedraw = true;
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
            layoutTurntable.setSignalIconPlacement(placementSelection);
        }

        // check if new radius was entered
        String str = editLayoutTurntableRadiusTextField.getText();
        if (!str.equals(editLayoutTurntableOldRadius)) {
            double rad = 0.0;
            try {
                rad = Float.parseFloat(str);
            } catch (Exception e) {
                JmriJOptionPane.showMessageDialog(editLayoutTurntableFrame, Bundle.getMessage("EntryError") + ": " // NOI18N
                        + e + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"), // NOI18N
                        JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            layoutTurntable.setRadius(rad);
            editLayoutTurntableNeedsRedraw = true;
        }
        // clean up
        saveRayPanelDetail();
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
        private LayoutTurntable.RayTrack rayTrack = null;
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
        public TurntableRayPanel(@Nonnull LayoutTurntable.RayTrack rayTrack) {
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
                        JmriJOptionPane.showMessageDialog(editLayoutTurntableFrame, Bundle.getMessage("EntryError") + ": " // NOI18N
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
                    layoutEditor.newTurnoutComboBoxPopupMenuListener(turnoutNameComboBox, turntableTurnouts));
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
                    log.error("Angle is not in correct format so will skip {}", rayAngleTextField.getText());  // NOI18N
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


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurntableEditor.class);
}
