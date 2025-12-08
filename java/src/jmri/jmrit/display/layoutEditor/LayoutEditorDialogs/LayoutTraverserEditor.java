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
    private JPanel signalMastParametersPanel;
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
        geometryPanel.add(deckLengthTextField);
        geometryPanel.add(new JLabel(Bundle.getMessage("Width")));
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
        editLayoutTraverserAddSlotButton = new JButton(Bundle.getMessage("AddSlot"));
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
        JPanel donePanel = new JPanel();
        donePanel.setLayout(new FlowLayout());
        addDoneCancelButtons(donePanel, editLayoutTraverserFrame.getRootPane(),
                this::editLayoutTraverserDonePressed, this::traverserEditCancelPressed);
        footerPane.add(donePanel);

        editLayoutTraverserSlotPanel = new JPanel();
        editLayoutTraverserSlotPanel.setLayout(new BoxLayout(editLayoutTraverserSlotPanel, BoxLayout.Y_AXIS));
        JScrollPane slotScrollPane = new JScrollPane(editLayoutTraverserSlotPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentPane.add(slotScrollPane, BorderLayout.CENTER);

        // Set initial values
        deckLengthTextField.setText(String.valueOf(layoutTraverser.getDeckLength()));
        deckWidthTextField.setText(String.valueOf(layoutTraverser.getDeckWidth()));
        orientationComboBox.setSelectedIndex(layoutTraverser.getOrientation());
        slotOffsetTextField.setText("0.0");

        editLayoutTraverserBlockNameComboBox.setSelectedItem(layoutTraverser.getLayoutBlock() != null ? layoutTraverser.getLayoutBlock().getBlock() : null);
        editLayoutTraverserDccControlledCheckBox.setSelected(layoutTraverser.isTurnoutControlled());
        editLayoutTraverserUseSignalMastsCheckBox.setSelected(layoutTraverser.isDispatcherManaged());

        // Add listeners
        editLayoutTraverserAddSlotButton.addActionListener(this::addSlotTrackPressed);
        editLayoutTraverserSegmentEditBlockButton.addActionListener(this::editLayoutTraverserEditBlockPressed);
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

    private void addSlotTrackPressed(ActionEvent e) {
        try {
            double offset = Double.parseDouble(slotOffsetTextField.getText());
            layoutTraverser.addSlot(offset);
            updateSlotPanel();
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        } catch (NumberFormatException ex) {
            JmriJOptionPane.showMessageDialog(editLayoutTraverserFrame, Bundle.getMessage("EntryError"), Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
        }
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
        traverserTurnouts.clear();
        layoutTraverser.getSlotList().forEach(rt -> traverserTurnouts.add(rt.getTurnout()));

        editLayoutTraverserSlotPanel.removeAll();
        editLayoutTraverserSlotPanel.setLayout(new BoxLayout(editLayoutTraverserSlotPanel, BoxLayout.Y_AXIS));
        for (LayoutTraverser.SlotTrack rt : layoutTraverser.getSlotList()) {
            editLayoutTraverserSlotPanel.add(new TraverserSlotPanel(rt));
        }

        // Rebuild signal mast panel
        signalMastParametersPanel.removeAll();
        approachMastComboBoxes.clear();

        if (layoutTraverser.isDispatcherManaged()) {
            signalMastParametersPanel.setLayout(new BoxLayout(signalMastParametersPanel, BoxLayout.Y_AXIS));

            // Add approach masts for each Slot
            for (LayoutTraverser.SlotTrack rt : layoutTraverser.getSlotList()) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
                p.add(new JLabel(Bundle.getMessage("ApproachMastSlot", rt.getConnectionIndex() + 1)));
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
                    layoutTraverser.getSlotList().get(index).setApproachMast( (newMast != null) ? newMast.getSystemName() : null );
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
            editLayoutTraverserSlotPanel.add(signalMastParametersPanel);
        }

        editLayoutTraverserSlotPanel.revalidate();
        editLayoutTraverserSlotPanel.repaint();
        editLayoutTraverserFrame.pack();
    }

    private void saveSlotPanelDetail() {
        for (Component comp : editLayoutTraverserSlotPanel.getComponents()) {
            if (comp instanceof TraverserSlotPanel) {
                TraverserSlotPanel tsp = (TraverserSlotPanel) comp;
                tsp.updateDetails();
            }
        }
    }

    private void editLayoutTraverserDonePressed(ActionEvent a) {
        try {
            layoutTraverser.setDeckLength(Double.parseDouble(deckLengthTextField.getText()));
            layoutTraverser.setDeckWidth(Double.parseDouble(deckWidthTextField.getText()));
            layoutTraverser.setOrientation(orientationComboBox.getSelectedIndex());
        } catch (NumberFormatException e) {
            JmriJOptionPane.showMessageDialog(editLayoutTraverserFrame, Bundle.getMessage("EntryError"), Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
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

    public class TraverserSlotPanel extends JPanel {

        private LayoutTraverser.SlotTrack slotTrack;
        private final JPanel slotTurnoutPanel;
        private final NamedBeanComboBox<Turnout> turnoutNameComboBox;
        private final TitledBorder slotTitledBorder;
        private final JComboBox<String> slotTurnoutStateComboBox;
        private final JLabel slotTurnoutStateLabel;
        private final JTextField slotOffsetTextField;
        private final int[] slotTurnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};
        private final DecimalFormat twoDForm = new DecimalFormat("#.00");

        public TraverserSlotPanel(@Nonnull LayoutTraverser.SlotTrack slotTrack) {
            this.slotTrack = slotTrack;

            JPanel top = new JPanel();
            top.add(new JLabel(Bundle.getMessage("SlotOffset")));
            top.add(slotOffsetTextField = new JTextField(5));
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(top);

            turnoutNameComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(TurnoutManager.class));
            LayoutEditor.setupComboBox(turnoutNameComboBox, false, true, false);
            turnoutNameComboBox.setSelectedItem(slotTrack.getTurnout());
            turnoutNameComboBox.addPopupMenuListener(
                    layoutEditor.newTurnoutComboBoxPopupMenuListener(turnoutNameComboBox, traverserTurnouts));
            String turnoutStateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
            String turnoutStateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
            String[] turnoutStates = new String[]{turnoutStateClosed, turnoutStateThrown};

            slotTurnoutStateComboBox = new JComboBox<>(turnoutStates);
            slotTurnoutStateLabel = new JLabel(Bundle.getMessage("TurnoutState"));
            slotTurnoutPanel = new JPanel();

            slotTurnoutPanel.setBorder(new EtchedBorder());
            slotTurnoutPanel.add(new JLabel(Bundle.getMessage("BeanNameTurnout")));
            slotTurnoutPanel.add(turnoutNameComboBox);
            slotTurnoutPanel.add(slotTurnoutStateLabel);
            slotTurnoutPanel.add(slotTurnoutStateComboBox);
            if (slotTrack.getTurnoutState() == Turnout.CLOSED) {
                slotTurnoutStateComboBox.setSelectedItem(turnoutStateClosed);
            } else {
                slotTurnoutStateComboBox.setSelectedItem(turnoutStateThrown);
            }
            this.add(slotTurnoutPanel);

            JButton deleteSlotButton = new JButton(Bundle.getMessage("ButtonDelete"));
            top.add(deleteSlotButton);
            deleteSlotButton.addActionListener((ActionEvent e) -> {
                delete();
                updateSlotPanel();
            });
            slotTitledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
            this.setBorder(slotTitledBorder);

            showTurnoutDetails();

            slotOffsetTextField.setText(twoDForm.format(slotTrack.getOffset()));
            int slotNumber = slotTrack.getConnectionIndex() + 1;
            slotTitledBorder.setTitle(Bundle.getMessage("Slot") + " : " + slotNumber);
        }

        private void delete() {
            int n = JmriJOptionPane.showConfirmDialog(null,
                    Bundle.getMessage("Question7"),
                    Bundle.getMessage("WarningTitle"),
                    JmriJOptionPane.YES_NO_OPTION);
            if (n == JmriJOptionPane.YES_OPTION) {
                layoutTraverser.deleteSlot(slotTrack);
            }
        }

        private void updateDetails() {
            try {
                slotTrack.setOffset(Double.parseDouble(slotOffsetTextField.getText()));
            } catch (NumberFormatException e) {
                // ignore
            }
            if (layoutTraverser.isTurnoutControlled()) {
                String turnoutName = turnoutNameComboBox.getSelectedItemDisplayName();
                if (turnoutName == null) {
                    turnoutName = "";
                }
                slotTrack.setTurnout(turnoutName, slotTurnoutStateValues[slotTurnoutStateComboBox.getSelectedIndex()]);
            }
        }

        private void showTurnoutDetails() {
            slotTurnoutPanel.setVisible(layoutTraverser.isTurnoutControlled());
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTraverserEditor.class);
}
