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

    private JPanel editLayoutTraverserSlotPanel;
    private JButton editLayoutTraverserAddSlotButton;
    private JCheckBox editLayoutTraverserDccControlledCheckBox;
    private JCheckBox editLayoutTraverserUseSignalMastsCheckBox;
    private JPanel signalMastAssignmentsPanel;
    private NamedBeanComboBox<SignalMast> exitMastComboBox;
    private NamedBeanComboBox<SignalMast> bufferMastComboBox;

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
        deckWidthTextField.setEnabled(false);
        geometryPanel.add(deckWidthTextField);
        geometryPanel.add(new JLabel(Bundle.getMessage("Orientation")));
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

        // Add listeners
        editLayoutTraverserAddSlotButton.addActionListener(this::addTrackPairPressed);
        editLayoutTraverserSegmentEditBlockButton.addActionListener(this::editLayoutTraverserEditBlockPressed);
        slotOffsetTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    double offset = Double.parseDouble(slotOffsetTextField.getText());
                    if (layoutTraverser.getSlotOffset() != offset) {
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

        JPanel turnoutAssignmentsPanel = new JPanel();
        turnoutAssignmentsPanel.setLayout(new BoxLayout(turnoutAssignmentsPanel, BoxLayout.Y_AXIS));
        turnoutAssignmentsPanel.setBorder(new TitledBorder(new EtchedBorder(), Bundle.getMessage("TurnoutAssignments")));
        traverserTurnouts.clear();
        layoutTraverser.getSlotList().forEach(rt -> traverserTurnouts.add(rt.getTurnout()));
        for (int i = 0; i < layoutTraverser.getNumberSlots() / 2; i++) {
            turnoutAssignmentsPanel.add(new TraverserPairPanel(i));
        }
        editLayoutTraverserSlotPanel.add(turnoutAssignmentsPanel);

        signalMastAssignmentsPanel = new JPanel();
        approachMastComboBoxes.clear();

        if (layoutTraverser.isDispatcherManaged()) {
            signalMastAssignmentsPanel.setLayout(new BoxLayout(signalMastAssignmentsPanel, BoxLayout.Y_AXIS));
            signalMastAssignmentsPanel.setBorder(new TitledBorder(new EtchedBorder(), Bundle.getMessage("TraverserSignalMastAssignmentsTitle")));

            for (int i = 0; i < layoutTraverser.getNumberSlots() / 2; i++) {
                JPanel p = new JPanel(new GridBagLayout());
                p.setBorder(new TitledBorder(new EtchedBorder(), Bundle.getMessage("SlotPair") + " " + (i + 1)));
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.anchor = GridBagConstraints.LINE_START;
                c.insets = new Insets(2, 2, 2, 2);

                JLabel labelA = new JLabel();
                JLabel labelB = new JLabel();
                if (layoutTraverser.getOrientation() == LayoutTraverser.HORIZONTAL) {
                    labelA.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("ApproachMastSlotLeft")));
                    labelB.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("ApproachMastSlotRight")));
                } else {
                    labelA.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("ApproachMastSlotUp")));
                    labelB.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("ApproachMastSlotDown")));
                }
                p.add(labelA, c);

                c.gridy = 1;
                p.add(labelB, c);

                c.gridx = 1;
                c.gridy = 0;
                c.insets = new Insets(2, 5, 2, 2);
                NamedBeanComboBox<SignalMast> comboA = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalMastManager.class), layoutTraverser.getSlotList().get(i * 2).getApproachMast(), DisplayOptions.DISPLAYNAME);
                LayoutEditor.setupComboBox(comboA, false, true, true);
                comboA.setEditable(false);
                comboA.setAllowNull(true);
                p.add(comboA, c);
                approachMastComboBoxes.add(comboA);

                c.gridy = 1;
                NamedBeanComboBox<SignalMast> comboB = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalMastManager.class), layoutTraverser.getSlotList().get(i * 2 + 1).getApproachMast(), DisplayOptions.DISPLAYNAME);
                LayoutEditor.setupComboBox(comboB, false, true, true);
                comboB.setEditable(false);
                comboB.setAllowNull(true);
                p.add(comboB, c);
                approachMastComboBoxes.add(comboB);

                signalMastAssignmentsPanel.add(p);
            }

            if (!approachMastComboBoxes.isEmpty()) {
                signalMastAssignmentsPanel.add(new JSeparator());
            }

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
            signalMastAssignmentsPanel.add(placementPanel);

            signalMastAssignmentsPanel.add(new JSeparator());

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

            signalMastAssignmentsPanel.add(mastPanel);
            editLayoutTraverserSlotPanel.add(signalMastAssignmentsPanel);
        }

        editLayoutTraverserSlotPanel.revalidate();
        editLayoutTraverserSlotPanel.repaint();
        editLayoutTraverserFrame.pack();
    }

    private void saveSlotPanelDetail() {
        for (Component mainComp : editLayoutTraverserSlotPanel.getComponents()) {
            if (mainComp instanceof JPanel && ((JPanel)mainComp).getBorder() instanceof TitledBorder) {
                TitledBorder border = (TitledBorder)((JPanel)mainComp).getBorder();
                if (Bundle.getMessage("TurnoutAssignments").equals(border.getTitle())) {
                    JPanel turnoutAssignmentsPanel = (JPanel) mainComp;
                    for (Component tppComp : turnoutAssignmentsPanel.getComponents()) {
                        if (tppComp instanceof TraverserPairPanel) {
                            TraverserPairPanel tpp = (TraverserPairPanel) tppComp;
                            tpp.updateDetails();
                        }
                    }
                }
            }
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

            for (int i = 0; i < approachMastComboBoxes.size(); i++) {
                SignalMast newMast = approachMastComboBoxes.get(i).getSelectedItem();
                layoutTraverser.getSlotList().get(i).setApproachMast( (newMast != null) ? newMast.getSystemName() : null );
            }
        }
    }

    private void editLayoutTraverserDonePressed(ActionEvent a) {
        layoutTraverser.setOrientation(orientationComboBox.getSelectedIndex());

        String newName = editLayoutTraverserBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if ((layoutTraverser.getBlockName().isEmpty()) || !layoutTraverser.getBlockName().equals(newName)) {
            layoutTraverser.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editLayoutTraverserNeedsRedraw = true;
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
        private final NamedBeanComboBox<Turnout> turnoutNameComboBoxA;
        private final NamedBeanComboBox<Turnout> turnoutNameComboBoxB;
        private final TitledBorder slotTitledBorder;
        private final JComboBox<String> slotTurnoutStateComboBoxA;
        private final JComboBox<String> slotTurnoutStateComboBoxB;
        private final JLabel slotTurnoutLabelA;
        private final JLabel slotTurnoutLabelB;
        private final JCheckBox disabledCheckBoxA;
        private final JCheckBox disabledCheckBoxB;
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

            // Side A
            turnoutNameComboBoxA = new NamedBeanComboBox<>(InstanceManager.getDefault(TurnoutManager.class));
            LayoutEditor.setupComboBox(turnoutNameComboBoxA, false, true, false);
            turnoutNameComboBoxA.setSelectedItem(slotA.getTurnout());
            turnoutNameComboBoxA.addPopupMenuListener(
                    layoutEditor.newTurnoutComboBoxPopupMenuListener(turnoutNameComboBoxA, traverserTurnouts));
            slotTurnoutStateComboBoxA = new JComboBox<>(turnoutStates);
            slotTurnoutLabelA = new JLabel();
            disabledCheckBoxA = new JCheckBox(Bundle.getMessage("Disabled"));
            disabledCheckBoxA.setSelected(slotA.isDisabled());
            disabledCheckBoxA.addActionListener((ActionEvent e) -> {
                if (disabledCheckBoxA.isSelected() && (slotA.getConnect() != null)) {
                    JmriJOptionPane.showMessageDialog(editLayoutTraverserFrame,
                            Bundle.getMessage("ErrorTraverserSlotConnected"),
                            Bundle.getMessage("ErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    disabledCheckBoxA.setSelected(false);
                }
            });

            c.gridy = 0;
            c.gridx = 0;
            turnoutDetailsPanel.add(slotTurnoutLabelA, c);
            c.gridx = 1;
            turnoutDetailsPanel.add(turnoutNameComboBoxA, c);
            c.gridx = 2;
            turnoutDetailsPanel.add(new JLabel(Bundle.getMessage("TurnoutState")), c);
            c.gridx = 3;
            turnoutDetailsPanel.add(slotTurnoutStateComboBoxA, c);
            c.gridx = 4;
            turnoutDetailsPanel.add(disabledCheckBoxA, c);

            if (slotA.getTurnoutState() == Turnout.CLOSED) {
                slotTurnoutStateComboBoxA.setSelectedItem(turnoutStateClosed);
            } else {
                slotTurnoutStateComboBoxA.setSelectedItem(turnoutStateThrown);
            }

            // Side B
            turnoutNameComboBoxB = new NamedBeanComboBox<>(InstanceManager.getDefault(TurnoutManager.class));
            LayoutEditor.setupComboBox(turnoutNameComboBoxB, false, true, false);
            turnoutNameComboBoxB.setSelectedItem(slotB.getTurnout());
            turnoutNameComboBoxB.addPopupMenuListener(
                    layoutEditor.newTurnoutComboBoxPopupMenuListener(turnoutNameComboBoxB, traverserTurnouts));
            slotTurnoutStateComboBoxB = new JComboBox<>(turnoutStates);
            slotTurnoutLabelB = new JLabel();
            disabledCheckBoxB = new JCheckBox(Bundle.getMessage("Disabled"));
            disabledCheckBoxB.setSelected(slotB.isDisabled());
            disabledCheckBoxB.addActionListener((ActionEvent e) -> {
                if (disabledCheckBoxB.isSelected() && (slotB.getConnect() != null)) {
                    JmriJOptionPane.showMessageDialog(editLayoutTraverserFrame,
                            Bundle.getMessage("ErrorTraverserSlotConnected"),
                            Bundle.getMessage("ErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    disabledCheckBoxB.setSelected(false);
                }
            });

            c.gridy = 1;
            c.gridx = 0;
            turnoutDetailsPanel.add(slotTurnoutLabelB, c);
            c.gridx = 1;
            turnoutDetailsPanel.add(turnoutNameComboBoxB, c);
            c.gridx = 2;
            turnoutDetailsPanel.add(new JLabel(Bundle.getMessage("TurnoutState")), c);
            c.gridx = 3;
            turnoutDetailsPanel.add(slotTurnoutStateComboBoxB, c);
            c.gridx = 4;
            turnoutDetailsPanel.add(disabledCheckBoxB, c);

            if (slotB.getTurnoutState() == Turnout.CLOSED) {
                slotTurnoutStateComboBoxB.setSelectedItem(turnoutStateClosed);
            } else {
                slotTurnoutStateComboBoxB.setSelectedItem(turnoutStateThrown);
            }
            this.add(turnoutDetailsPanel);

            setTurnoutLabels();

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

        private void setTurnoutLabels() {
            if (layoutTraverser.getOrientation() == LayoutTraverser.HORIZONTAL) {
                slotTurnoutLabelA.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("TurnoutLeft")));
                slotTurnoutLabelB.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("TurnoutRight")));
            } else {
                slotTurnoutLabelA.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("TurnoutUp")));
                slotTurnoutLabelB.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("TurnoutDown")));
            }
        }

        private void delete() {
            int n = JmriJOptionPane.showConfirmDialog(null,
                    Bundle.getMessage("Question7"),
                    Bundle.getMessage("WarningTitle"),
                    JmriJOptionPane.YES_NO_OPTION);
            if (n == JmriJOptionPane.YES_OPTION) {
                layoutTraverser.deleteTrackPair(pairIndex);
            }
        }

        private void updateDetails() {
            if (layoutTraverser.isTurnoutControlled()) {
                String turnoutNameA = turnoutNameComboBoxA.getSelectedItemDisplayName();
                if (turnoutNameA == null) turnoutNameA = "";
                slotA.setTurnout(turnoutNameA, slotTurnoutStateValues[slotTurnoutStateComboBoxA.getSelectedIndex()]);

                String turnoutNameB = turnoutNameComboBoxB.getSelectedItemDisplayName();
                if (turnoutNameB == null) turnoutNameB = "";
                slotB.setTurnout(turnoutNameB, slotTurnoutStateValues[slotTurnoutStateComboBoxB.getSelectedIndex()]);
            }
            slotA.setDisabled(disabledCheckBoxA.isSelected());
            slotB.setDisabled(disabledCheckBoxB.isSelected());
        }

        private void showTurnoutDetails() {
            boolean visible = layoutTraverser.isTurnoutControlled();
            turnoutDetailsPanel.setVisible(visible);
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTraverserEditor.class);
}
