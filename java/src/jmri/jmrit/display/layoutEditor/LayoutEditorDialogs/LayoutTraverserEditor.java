package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.*;
import java.awt.event.*;
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
import java.awt.geom.Point2D;

/**
 * MVC Editor component for LayoutTraverser objects.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * @author Dave Sand Copyright (c) 2024
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
    private final JTextField deckLengthTextField = new JTextField(8);
    private final JTextField deckWidthTextField = new JTextField(8);
    private final JComboBox<String> orientationComboBox = new JComboBox<>();
    private final JTextField slotOffsetTextField = new JTextField(8);

    private final NamedBeanComboBox<Block> editLayoutTraverserBlockNameComboBox = new NamedBeanComboBox<>(
             InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private JButton editLayoutTraverserSegmentEditBlockButton;
    private final JComboBox<String> editLayoutTraverserMainlineComboBox = new JComboBox<>();

    private JPanel editLayoutTraverserSlotPanel;
    private JButton editLayoutTraverserAddSlotButton;
    private JCheckBox editLayoutTraverserDccControlledCheckBox;
    private JCheckBox editLayoutTraverserUseSignalMastsCheckBox;
    private JPanel footerAssignmentsPanel;
    private NamedBeanComboBox<SignalMast> exitMastComboBox;
    private NamedBeanComboBox<SignalMast> bufferMastComboBox;

    private boolean editLayoutTraverserOpen = false;
    private boolean editLayoutTraverserNeedsRedraw = false;

    private JRadioButton doNotPlaceIcons;
    private JRadioButton placeIconsLeft;
    private JRadioButton placeIconsRight;

    private JRadioButton bridgeDoNotPlaceIcons;
    private JRadioButton bridgePlaceIconsLeft;
    private JRadioButton bridgePlaceIconsRight;

    private final List<Turnout> traverserTurnouts = new ArrayList<>();
    private final List<TraverserPairPanel> pairPanels = new ArrayList<>();

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
            return;
        }
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

        // Geometry Panel
        JPanel geometryPanel = new JPanel();
        geometryPanel.setLayout(new FlowLayout());
        geometryPanel.add(new JLabel(Bundle.getMessage("Length")));
        deckLengthTextField.setEnabled(false);
        geometryPanel.add(deckLengthTextField);
        geometryPanel.add(new JLabel(Bundle.getMessage("Width")));
        geometryPanel.add(deckWidthTextField);
        geometryPanel.add(new JLabel(Bundle.getMessage("Orientation")));
        orientationComboBox.removeAllItems();
        orientationComboBox.addItem(Bundle.getMessage("Horizontal"));
        orientationComboBox.addItem(Bundle.getMessage("Vertical"));
        geometryPanel.add(orientationComboBox);
        headerPane.add(geometryPanel);

        // Slot Panel
        JPanel slotPanel = new JPanel();
        slotPanel.setLayout(new FlowLayout());
        slotPanel.add(new JLabel(Bundle.getMessage("SlotOffset")));
        slotPanel.add(slotOffsetTextField);
        editLayoutTraverserAddSlotButton = new JButton(Bundle.getMessage("AddSlotPair"));
        slotPanel.add(editLayoutTraverserAddSlotButton);
        headerPane.add(slotPanel);

        // Block Name Panel
        JPanel blockPanel = new JPanel();
        blockPanel.setLayout(new FlowLayout());
        blockPanel.add(new JLabel(Bundle.getMessage("BlockID")));
        LayoutEditor.setupComboBox(editLayoutTraverserBlockNameComboBox, false, true, true);
        blockPanel.add(editLayoutTraverserBlockNameComboBox);
        editLayoutTraverserSegmentEditBlockButton = new JButton(Bundle.getMessage("EditBlock", ""));
        blockPanel.add(editLayoutTraverserSegmentEditBlockButton);

        boolean mainlineSaved = layoutTraverser.isMainline();
        editLayoutTraverserMainlineComboBox.removeAllItems();
        editLayoutTraverserMainlineComboBox.addItem(Bundle.getMessage("Mainline"));
        editLayoutTraverserMainlineComboBox.addItem(Bundle.getMessage("NotMainline"));
        layoutTraverser.setMainline(mainlineSaved);  // restore the current state

        blockPanel.add(editLayoutTraverserMainlineComboBox);
        headerPane.add(blockPanel);

        // Controls Panel
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new FlowLayout());
        editLayoutTraverserDccControlledCheckBox = new JCheckBox(Bundle.getMessage("TraverserDCCControlled"));
        controlsPanel.add(editLayoutTraverserDccControlledCheckBox);
        editLayoutTraverserUseSignalMastsCheckBox = new JCheckBox(Bundle.getMessage("TraverserUseSignalMasts"));
        controlsPanel.add(editLayoutTraverserUseSignalMastsCheckBox);
        headerPane.add(controlsPanel);

        // set up Done and Cancel buttons
        JPanel donePanel = new JPanel();
        donePanel.setLayout(new FlowLayout());
        addDoneCancelButtons(donePanel, editLayoutTraverserFrame.getRootPane(),
                this::editLayoutTraverserDonePressed, this::traverserEditCancelPressed);
        footerPane.add(donePanel);

        editLayoutTraverserSlotPanel = new JPanel();
        editLayoutTraverserSlotPanel.setLayout(new BoxLayout(editLayoutTraverserSlotPanel, BoxLayout.Y_AXIS));
        JScrollPane slotScrollPane = new JScrollPane(editLayoutTraverserSlotPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentPane.add(slotScrollPane, BorderLayout.CENTER);

        // Set initial values
        deckLengthTextField.setText(String.valueOf(layoutTraverser.getDeckLength()));
        deckWidthTextField.setText(String.valueOf(layoutTraverser.getDeckWidth()));
        orientationComboBox.setSelectedIndex(layoutTraverser.getOrientation());
        slotOffsetTextField.setText(String.valueOf(layoutTraverser.getSlotOffset()));

        editLayoutTraverserBlockNameComboBox.setSelectedItem(layoutTraverser.getLayoutBlock() != null ? layoutTraverser.getLayoutBlock().getBlock() : null);
        editLayoutTraverserDccControlledCheckBox.setSelected(layoutTraverser.isTurnoutControlled());
        editLayoutTraverserUseSignalMastsCheckBox.setSelected(layoutTraverser.isDispatcherManaged());
        if (layoutTraverser.isMainline()) {
            editLayoutTraverserMainlineComboBox.setSelectedIndex(0);
        } else {
            editLayoutTraverserMainlineComboBox.setSelectedIndex(1);
        }
        for (ActionListener al : editLayoutTraverserMainlineComboBox.getActionListeners()) {
            editLayoutTraverserMainlineComboBox.removeActionListener(al);
        }
        editLayoutTraverserMainlineComboBox.addActionListener((java.awt.event.ActionEvent e) -> {
            if (layoutTraverser != null) {
                layoutTraverser.setMainline(editLayoutTraverserMainlineComboBox.getSelectedIndex() == 0);
            }
        });

        // Add listeners
        editLayoutTraverserAddSlotButton.addActionListener(this::addTrackPairPressed);
        editLayoutTraverserSegmentEditBlockButton.addActionListener(this::editLayoutTraverserEditBlockPressed);
        slotOffsetTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    double offset = Double.parseDouble(slotOffsetTextField.getText());
                    if (!jmri.util.MathUtil.equals(layoutTraverser.getSlotOffset(), offset)) {
                        layoutTraverser.setSlotOffset(offset);
                        updateSlotPanel();
                        layoutEditor.redrawPanel();
                        layoutEditor.setDirty();
                    }
                } catch (NumberFormatException ex) {
                    // ignore invalid input
                }
            }
        });
        for (ActionListener al : orientationComboBox.getActionListeners()) {
            orientationComboBox.removeActionListener(al);
        }
        orientationComboBox.addActionListener(e -> {
            layoutTraverser.setOrientation(orientationComboBox.getSelectedIndex());
            updateSlotPanel();
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        });
        editLayoutTraverserDccControlledCheckBox.addActionListener(e -> {
            layoutTraverser.setTurnoutControlled(editLayoutTraverserDccControlledCheckBox.isSelected());
            updateSlotPanel();
        });
        editLayoutTraverserUseSignalMastsCheckBox.addActionListener(e -> {
            layoutTraverser.setDispatcherManaged(editLayoutTraverserUseSignalMastsCheckBox.isSelected());
            updateSlotPanel();
        });

        updateSlotPanel();
        editLayoutTraverserFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                traverserEditCancelPressed(null);
            }
        });
        editLayoutTraverserFrame.pack();
        editLayoutTraverserFrame.setVisible(true);
        editLayoutTraverserOpen = true;
    }

    private void addTrackPairPressed(ActionEvent e) {
        layoutTraverser.addSlotPair();
        updateSlotPanel();
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
    }

    @InvokeOnGuiThread
    private void editLayoutTraverserEditBlockPressed(ActionEvent a) {
         String newName = editLayoutTraverserBlockNameComboBox.getSelectedItemDisplayName();
         if (newName == null) {
             newName = "";
         }
         if ((layoutTraverser.getBlockName().isEmpty())
                 || !layoutTraverser.getBlockName().equals(newName)) {
             layoutTraverser.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
             editLayoutTraverserNeedsRedraw = true;
         }
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

    private void updateSlotPanel() {
        deckLengthTextField.setText(String.valueOf(layoutTraverser.getDeckLength()));
        deckWidthTextField.setText(String.valueOf(layoutTraverser.getDeckWidth()));

        editLayoutTraverserSlotPanel.removeAll();
        pairPanels.clear();

        JPanel mainAssignmentsPanel = new JPanel();
        mainAssignmentsPanel.setLayout(new BoxLayout(mainAssignmentsPanel, BoxLayout.Y_AXIS));
        mainAssignmentsPanel.setBorder(new TitledBorder(new EtchedBorder(), Bundle.getMessage("TurnoutAssignments")));
        traverserTurnouts.clear();
        layoutTraverser.getSlotList().forEach(rt -> traverserTurnouts.add(rt.getTurnout()));
        for (int i = 0; i < layoutTraverser.getNumberSlots() / 2; i++) {
            TraverserPairPanel pairPanel = new TraverserPairPanel(i);
            pairPanels.add(pairPanel);
            mainAssignmentsPanel.add(pairPanel);
        }
        editLayoutTraverserSlotPanel.add(mainAssignmentsPanel);

        footerAssignmentsPanel = new JPanel();
        if (layoutTraverser.isDispatcherManaged()) {
            footerAssignmentsPanel.setLayout(new BoxLayout(footerAssignmentsPanel, BoxLayout.Y_AXIS));

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
            footerAssignmentsPanel.add(placementPanel);

            footerAssignmentsPanel.add(new JSeparator());

            JPanel mastPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(2, 2, 2, 2); // Default insets
            mastPanel.add(new JLabel(Bundle.getMessage("TraverserExitMastLabel")), c);
            c.gridx = 1;
            c.insets = new Insets(2, 5, 2, 2); // Add left padding
            exitMastComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalMastManager.class), layoutTraverser.getExitSignalMast(), DisplayOptions.DISPLAYNAME);
            exitMastComboBox.setAllowNull(true);
            mastPanel.add(exitMastComboBox, c);

            c.gridx = 0;
            c.gridy = 1;
            c.insets = new Insets(2, 2, 2, 2); // Reset for label
            mastPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("TraverserBufferMastLabel"))), c);
            c.gridx = 1;
            c.insets = new Insets(2, 5, 2, 2); // Add left padding
            bufferMastComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalMastManager.class), layoutTraverser.getBufferMast(), DisplayOptions.DISPLAYNAME);
            bufferMastComboBox.setAllowNull(true);
            mastPanel.add(bufferMastComboBox, c);

            footerAssignmentsPanel.add(mastPanel);

            JPanel bridgePlacementPanel = new JPanel();
            bridgePlacementPanel.setLayout(new BoxLayout(bridgePlacementPanel, BoxLayout.Y_AXIS)); // NOI18N
            bridgePlacementPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TraverserAddMastIconsTitle")));

            bridgeDoNotPlaceIcons = new JRadioButton(Bundle.getMessage("DoNotPlace")); // NOI18N
            bridgePlaceIconsLeft = new JRadioButton(Bundle.getMessage("LeftHandSide")); // NOI18N
            bridgePlaceIconsRight = new JRadioButton(Bundle.getMessage("RightHandSide")); // NOI18N
            ButtonGroup bridgeBg = new ButtonGroup();
            bridgeBg.add(bridgeDoNotPlaceIcons);
            bridgeBg.add(bridgePlaceIconsLeft);
            bridgeBg.add(bridgePlaceIconsRight);
            switch (layoutTraverser.getBridgeSignalIconPlacement()) {
                case 1:
                    bridgePlaceIconsLeft.setSelected(true);
                    break;
                case 2:
                    bridgePlaceIconsRight.setSelected(true);
                    break;
                default: bridgeDoNotPlaceIcons.setSelected(true);
            }

            JPanel bridgeRadioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bridgeRadioPanel.add(bridgeDoNotPlaceIcons);
            bridgeRadioPanel.add(bridgePlaceIconsLeft);
            bridgeRadioPanel.add(bridgePlaceIconsRight);
            bridgePlacementPanel.add(bridgeRadioPanel);
            footerAssignmentsPanel.add(bridgePlacementPanel);

            editLayoutTraverserSlotPanel.add(footerAssignmentsPanel);
        }

        editLayoutTraverserSlotPanel.revalidate();
        editLayoutTraverserSlotPanel.repaint();
        editLayoutTraverserFrame.pack();
    }

    private void saveSlotPanelDetail() {
        for (TraverserPairPanel pairPanel : pairPanels) {
            pairPanel.updateDetails();
        }
        if (layoutTraverser.isDispatcherManaged()) {
            layoutTraverser.setExitSignalMast(exitMastComboBox.getSelectedItemDisplayName());
            layoutTraverser.setBufferSignalMast(bufferMastComboBox.getSelectedItemDisplayName());
            int placement = 0;
            if (placeIconsLeft.isSelected()) {
                placement = 1;
            } else if (placeIconsRight.isSelected()) {
                placement = 2;
            }
            layoutTraverser.setSignalIconPlacement(placement);

            int bridgePlacement = 0;
            if (bridgePlaceIconsLeft.isSelected()) {
                bridgePlacement = 1;
            } else if (bridgePlaceIconsRight.isSelected()) {
                bridgePlacement = 2;
            }
            layoutTraverser.setBridgeSignalIconPlacement(bridgePlacement);
        }
    }

    private void editLayoutTraverserDonePressed(ActionEvent a) {
        layoutTraverser.setOrientation(orientationComboBox.getSelectedIndex());
        try {
            double width = Double.parseDouble(deckWidthTextField.getText());
            if (!jmri.util.MathUtil.equals(layoutTraverser.getDeckWidth(), width)) {
                layoutTraverser.setDeckWidth(width);
                editLayoutTraverserNeedsRedraw = true;
            }
        } catch (NumberFormatException ex) {
            JmriJOptionPane.showMessageDialog(editLayoutTraverserFrame, Bundle.getMessage("EntryError") + ": "
                    + ex, Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            double offset = Double.parseDouble(slotOffsetTextField.getText());
            if (!jmri.util.MathUtil.equals(layoutTraverser.getSlotOffset(), offset)) {
                layoutTraverser.setSlotOffset(offset);
                editLayoutTraverserNeedsRedraw = true;
            }
        } catch (NumberFormatException ex) {
            JmriJOptionPane.showMessageDialog(editLayoutTraverserFrame, Bundle.getMessage("EntryError") + ": "
                    + ex, Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            return;
        }

        String newName = editLayoutTraverserBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if ((layoutTraverser.getBlockName().isEmpty()) || !layoutTraverser.getBlockName().equals(newName)) {
            layoutTraverser.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editLayoutTraverserNeedsRedraw = true;
        }
        layoutTraverser.setMainline(editLayoutTraverserMainlineComboBox.getSelectedIndex() == 0);

        if (layoutTraverser.isDispatcherManaged()) {
            // Always remove any existing icons for this traverser's approach masts first.
            List<SignalMastIcon> iconsToRemove = new ArrayList<>();
            for (Positionable p : layoutEditor.getContents()) {
                if (p instanceof SignalMastIcon) {
                    SignalMastIcon icon = (SignalMastIcon) p;
                    if (layoutTraverser.isApproachMast(icon.getSignalMast()) 
                        || (layoutTraverser.getExitSignalMast() != null && layoutTraverser.getExitSignalMast().equals(icon.getSignalMast()))
                        || (layoutTraverser.getBufferMast() != null && layoutTraverser.getBufferMast().equals(icon.getSignalMast()))) {
                        iconsToRemove.add(icon);
                    }
                }
            }
            for (SignalMastIcon icon : iconsToRemove) {
                icon.remove();
                editLayoutTraverserNeedsRedraw = true;
            }

            // Now, if requested, place the new icons for slots.
            if (!doNotPlaceIcons.isSelected()) { // placeIconsLeft or placeIconsRight is selected
                for (int i = 0; i < layoutTraverser.getNumberSlots(); i++) {
                    LayoutTraverser.SlotTrack slot = layoutTraverser.getSlotList().get(i);
                    SignalMast mast = slot.getApproachMast();
                    if (mast != null) {
                        if (slot.getConnect() != null) {
                            SignalMastIcon icon = new SignalMastIcon(layoutEditor);
                            icon.setSignalMast(mast.getDisplayName());
                            layoutEditor.getLETools().placingBlockForTurntable(icon, placeIconsRight.isSelected(),
                                    0.0,
                                    slot.getConnect(), layoutTraverserView.getSlotCoordsOrdered(i));
                            editLayoutTraverserNeedsRedraw = true;
                        }
                    }
                }
            }

            // Now place Exit and Buffer masts if requested.
            if (!bridgeDoNotPlaceIcons.isSelected()) {
                Point2D center = layoutTraverserView.getControlPointCenter();
                int orientation = layoutTraverser.getOrientation();

                // Place Buffer Mast
                if (layoutTraverser.getBufferMast() != null) {
                    SignalMastIcon icon = new SignalMastIcon(layoutEditor);
                    icon.setSignalMast(layoutTraverser.getBufferMast().getDisplayName());
                    boolean sideRight = bridgePlaceIconsRight.isSelected();
                    // Pass orientation (Integer) instead of TrackSegment
                    layoutEditor.getLETools().placingBlockForTurntable(icon, !sideRight, 
                            0.0, orientation, center);
                }
                // Place Exit Mast
                if (layoutTraverser.getExitSignalMast() != null) {
                    SignalMastIcon icon = new SignalMastIcon(layoutEditor);
                    icon.setSignalMast(layoutTraverser.getExitSignalMast().getDisplayName());
                    boolean sideRight = bridgePlaceIconsRight.isSelected();
                    // Pass orientation (Integer) instead of TrackSegment
                    layoutEditor.getLETools().placingBlockForTurntable(icon, sideRight, 
                            0.0, orientation, center);
                }
                editLayoutTraverserNeedsRedraw = true;
            }
        }

        saveSlotPanelDetail();
        editLayoutTraverserOpen = false;
        editLayoutTraverserFrame.setVisible(false);
        editLayoutTraverserFrame.dispose();
        editLayoutTraverserFrame = null;
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
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

    public class TraverserPairPanel extends JPanel {

        private final LayoutTraverser.SlotTrack slotA;
        private final LayoutTraverser.SlotTrack slotB;
        private final int pairIndex;

        private final JPanel turnoutDetailsPanel;
        private final NamedBeanComboBox<Turnout> turnoutNameComboBox;
        private final TitledBorder slotTitledBorder;
        private final JComboBox<String> slotTurnoutStateComboBox;
        private final JCheckBox disabledCheckBoxA;
        private final JCheckBox disabledCheckBoxB;
        private final NamedBeanComboBox<SignalMast> approachMastComboBoxA;
        private final NamedBeanComboBox<SignalMast> approachMastComboBoxB;
        private final int[] slotTurnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};

        public TraverserPairPanel(int pairIndex) {
            this.pairIndex = pairIndex;
            this.slotA = layoutTraverser.getSlotList().get(pairIndex * 2);
            this.slotB = layoutTraverser.getSlotList().get(pairIndex * 2 + 1);

            JPanel top = new JPanel();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(top);

            String turnoutStateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
            String turnoutStateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
            String[] turnoutStates = new String[]{turnoutStateClosed, turnoutStateThrown};

            turnoutDetailsPanel = new JPanel(new GridBagLayout());
            turnoutDetailsPanel.setBorder(new EtchedBorder());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(2, 5, 2, 5);
            c.anchor = GridBagConstraints.LINE_START;

            // Row 0: Shared Turnout Assignment
            turnoutNameComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(TurnoutManager.class));
            LayoutEditor.setupComboBox(turnoutNameComboBox, false, true, false);
            turnoutNameComboBox.setSelectedItem(slotA.getTurnout());
            turnoutNameComboBox.addPopupMenuListener(
                    layoutEditor.newTurnoutComboBoxPopupMenuListener(turnoutNameComboBox, traverserTurnouts));
            slotTurnoutStateComboBox = new JComboBox<>(turnoutStates);
            if (slotA.getTurnoutState() == Turnout.CLOSED) {
                slotTurnoutStateComboBox.setSelectedItem(turnoutStateClosed);
            } else {
                slotTurnoutStateComboBox.setSelectedItem(turnoutStateThrown);
            }

            c.gridy = 0;
            c.gridx = 0;
            turnoutDetailsPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("TypeName_Turnout"))), c);
            c.gridx = 1;
            turnoutDetailsPanel.add(turnoutNameComboBox, c);
            c.gridx = 2;
            turnoutDetailsPanel.add(new JLabel(Bundle.getMessage("TurnoutState")), c);
            c.gridx = 3;
            turnoutDetailsPanel.add(slotTurnoutStateComboBox, c);

            // Side A Setup
            approachMastComboBoxA = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalMastManager.class), slotA.getApproachMast(), DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(approachMastComboBoxA, false, true, true);
            approachMastComboBoxA.setAllowNull(true);
            
            disabledCheckBoxA = new JCheckBox(Bundle.getMessage("Disabled"));
            disabledCheckBoxA.setSelected(slotA.isDisabled());
            disabledCheckBoxA.addActionListener((ActionEvent e) -> {
                if (disabledCheckBoxA.isSelected()) {
                    var msg = layoutTraverser.isSlotConnectionClear(slotA);
                    if (msg.length() > 0) {
                        msg.insert(0, Bundle.getMessage("TV_Message_Header"));
                        msg.append(Bundle.getMessage("TV_Message_Slot_Disable"));
                        JmriJOptionPane.showMessageDialog(editLayoutTraverserFrame,
                                msg.toString(),
                                Bundle.getMessage("ErrorTitle"),
                                JmriJOptionPane.ERROR_MESSAGE);
                        disabledCheckBoxA.setSelected(false);
                    }
                }
                slotA.setDisabled(disabledCheckBoxA.isSelected());
                layoutEditor.redrawPanel();
                layoutEditor.setDirty();
            });

            // Side B Setup
            approachMastComboBoxB = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalMastManager.class), slotB.getApproachMast(), DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(approachMastComboBoxB, false, true, true);
            approachMastComboBoxB.setAllowNull(true);

            disabledCheckBoxB = new JCheckBox(Bundle.getMessage("Disabled"));
            disabledCheckBoxB.setSelected(slotB.isDisabled());
            disabledCheckBoxB.addActionListener((ActionEvent e) -> {
                if (disabledCheckBoxB.isSelected()) {
                    var msg = layoutTraverser.isSlotConnectionClear(slotB);
                    if (msg.length() > 0) {
                        msg.insert(0, Bundle.getMessage("TV_Message_Header"));
                        JmriJOptionPane.showMessageDialog(editLayoutTraverserFrame,
                                msg.toString(),
                                Bundle.getMessage("ErrorTitle"),
                                JmriJOptionPane.ERROR_MESSAGE);
                        disabledCheckBoxB.setSelected(false);
                    }
                }
                slotB.setDisabled(disabledCheckBoxB.isSelected());
                layoutEditor.redrawPanel();
                layoutEditor.setDirty();
            });

            // Orientation Labels
            JLabel labelA = new JLabel();
            JLabel labelB = new JLabel();
            if (layoutTraverser.getOrientation() == LayoutTraverser.HORIZONTAL) {
                labelA.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("ApproachMastSlotLeft")));
                labelB.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("ApproachMastSlotRight")));
            } else {
                labelA.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("ApproachMastSlotUp")));
                labelB.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("ApproachMastSlotDown")));
            }

            // Row 1: Side A Signal Mast & Disable
            c.gridy = 1;
            c.gridx = 0;
            turnoutDetailsPanel.add(labelA, c);
            c.gridx = 1;
            turnoutDetailsPanel.add(approachMastComboBoxA, c);
            c.gridx = 2;
            turnoutDetailsPanel.add(disabledCheckBoxA, c);
            approachMastComboBoxA.setVisible(layoutTraverser.isDispatcherManaged());

            // Row 2: Side B Signal Mast & Disable
            c.gridy = 2;
            c.gridx = 0;
            turnoutDetailsPanel.add(labelB, c);
            c.gridx = 1;
            turnoutDetailsPanel.add(approachMastComboBoxB, c);
            c.gridx = 2;
            turnoutDetailsPanel.add(disabledCheckBoxB, c);
            approachMastComboBoxB.setVisible(layoutTraverser.isDispatcherManaged());

            this.add(turnoutDetailsPanel);

            JButton deleteButton = new JButton(Bundle.getMessage("Delete"));
            top.add(deleteButton);
            deleteButton.addActionListener((ActionEvent e) -> {
                delete();
                updateSlotPanel();
            });

            JButton moveUpButton = new JButton(Bundle.getMessage("MoveUp"));
            top.add(moveUpButton);
            moveUpButton.addActionListener((ActionEvent e) -> {
                layoutTraverser.moveSlotPairUp(pairIndex);
                updateSlotPanel();
            });
            moveUpButton.setVisible(layoutTraverser.isTurnoutControlled() && pairIndex > 0);

            JButton moveDownButton = new JButton(Bundle.getMessage("MoveDown"));
            top.add(moveDownButton);
            moveDownButton.addActionListener((ActionEvent e) -> {
                layoutTraverser.moveSlotPairDown(pairIndex);
                updateSlotPanel();
            });
            moveDownButton.setVisible(layoutTraverser.isTurnoutControlled() && pairIndex < (layoutTraverser.getNumberSlots() / 2) - 1);

            slotTitledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
            this.setBorder(slotTitledBorder);

            showTurnoutDetails();

            slotTitledBorder.setTitle(Bundle.getMessage("SlotPair") + " : " + (pairIndex + 1));
        }

        private void delete() {
            int n = JmriJOptionPane.showConfirmDialog(null,
                    Bundle.getMessage("Question8s"),
                    Bundle.getMessage("WarningTitle"),
                    JmriJOptionPane.YES_NO_OPTION);
            if (n == JmriJOptionPane.YES_OPTION) {
                if (layoutTraverser.isSlotDeleteAllowed(pairIndex)) {
                    layoutTraverser.deleteTrackPair(pairIndex);
                }
            }
        }

        public void updateDetails() {
            if (layoutTraverser.isTurnoutControlled()) {
                String turnoutName = turnoutNameComboBox.getSelectedItemDisplayName();
                if (turnoutName == null) turnoutName = "";
                int state = slotTurnoutStateValues[slotTurnoutStateComboBox.getSelectedIndex()];
                layoutTraverser.setSlotTurnout(pairIndex * 2, turnoutName, state);
            }
            if (layoutTraverser.isDispatcherManaged()) {
                SignalMast mastA = approachMastComboBoxA.getSelectedItem();
                slotA.setApproachMast((mastA != null) ? mastA.getSystemName() : null);
                SignalMast mastB = approachMastComboBoxB.getSelectedItem();
                slotB.setApproachMast((mastB != null) ? mastB.getSystemName() : null);
            }
            slotA.setDisabled(disabledCheckBoxA.isSelected());
            slotB.setDisabled(disabledCheckBoxB.isSelected());
        }

        private void showTurnoutDetails() {
            boolean visible = layoutTraverser.isTurnoutControlled() || layoutTraverser.isDispatcherManaged();
            turnoutDetailsPanel.setVisible(visible);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTraverserEditor.class);
}
