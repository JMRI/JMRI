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
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;

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

    private String editLayoutTurntableOldRadius = "";
    private boolean editLayoutTurntableOpen = false;
    private boolean editLayoutTurntableNeedsRedraw = false;

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
             JOptionPane.showMessageDialog(editLayoutTurntableFrame,
                     Bundle.getMessage("Error1"), // NOI18N
                     Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);  // NOI18N
             return;
         }
         blockToEdit.editLayoutBlock(editLayoutTurntableFrame);
         layoutEditor.setDirty();
         editLayoutTurntableNeedsRedraw = true;
     }

    // Remove old rays and add them back in
    private void updateRayPanel() {
        for (Component comp : editLayoutTurntableRayPanel.getComponents()) {
            editLayoutTurntableRayPanel.remove(comp);
        }

        // Create list of turnouts to be retained in the NamedBeanComboBox
        turntableTurnouts.clear();
        for (LayoutTurntable.RayTrack rt : layoutTurntable.getRayTrackList()) {
            turntableTurnouts.add(rt.getTurnout());
        }

        editLayoutTurntableRayPanel.setLayout(new BoxLayout(editLayoutTurntableRayPanel, BoxLayout.Y_AXIS));
        for (LayoutTurntable.RayTrack rt : layoutTurntable.getRayTrackList()) {
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
                        JOptionPane.showMessageDialog(editLayoutTurntableFrame, Bundle.getMessage("EntryError") + ": " // NOI18N
                                + ex + Bundle.getMessage("TryAgain"), Bundle.getMessage("ErrorTitle"), // NOI18N
                                JOptionPane.ERROR_MESSAGE);
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
