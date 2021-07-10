package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import jmri.NamedBean.DisplayOptions;
import jmri.*;
import jmri.jmrit.display.layoutEditor.*;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;

/**
 * MVC Editor component for LayoutTurnout objects.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutTurnoutEditor extends LayoutTrackEditor {

    /**
     * constructor method.
     * @param layoutEditor main layout editor.
     */
    public LayoutTurnoutEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    /*===================*\
    | Edit Layout Turnout |
    \*===================*/
    // variables for Edit Layout Turnout pane
    protected LayoutTurnoutView layoutTurnoutView = null;
    protected LayoutTurnout layoutTurnout = null;

    protected JmriJFrame editLayoutTurnoutFrame = null;
    protected NamedBeanComboBox<Turnout> editLayoutTurnout1stTurnoutComboBox = null;
    protected NamedBeanComboBox<Turnout> editLayoutTurnout2ndTurnoutComboBox = null;
    protected JLabel editLayoutTurnout2ndTurnoutLabel = null;
    protected final NamedBeanComboBox<Block> editLayoutTurnoutBlockNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    protected final NamedBeanComboBox<Block> editLayoutTurnoutBlockBNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    protected final NamedBeanComboBox<Block> editLayoutTurnoutBlockCNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    protected final NamedBeanComboBox<Block> editLayoutTurnoutBlockDNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    protected final JComboBox<String> editLayoutTurnoutStateComboBox = new JComboBox<>();
    protected JCheckBox editLayoutTurnoutHiddenCheckBox = null;
    protected JButton editLayoutTurnoutBlockButton;
    protected JButton editLayoutTurnoutBlockBButton;
    protected JButton editLayoutTurnoutBlockCButton;
    protected JButton editLayoutTurnoutBlockDButton;
    protected final JCheckBox editLayoutTurnout2ndTurnoutCheckBox = new JCheckBox(Bundle.getMessage("SupportingTurnout"));  // NOI18N
    protected final JCheckBox editLayoutTurnout2ndTurnoutInvertCheckBox = new JCheckBox(Bundle.getMessage("SecondTurnoutInvert"));  // NOI18N

    protected boolean editLayoutTurnoutOpen = false;
    protected boolean editLayoutTurnoutNeedRedraw = false;
    protected boolean editLayoutTurnoutNeedsBlockUpdate = false;
    protected int editLayoutTurnoutClosedIndex;
    protected int editLayoutTurnoutThrownIndex;

    /**
     * Edit a Layout Turnout.
     * Invoked for any of the subtypes, has conditional code for crossovers
     */
    @Override
    public void editLayoutTrack(@Nonnull LayoutTrackView layoutTrackView) {
        log.trace("LayoutTurnoutEditor.editLayoutTrack({}) of a {}", layoutTrackView, layoutTrackView.getClass());
        if ( layoutTrackView instanceof LayoutTurnoutView ) {
            this.layoutTurnoutView = (LayoutTurnoutView) layoutTrackView;
            this.layoutTurnout = this.layoutTurnoutView.getLayoutTurnout();
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

            // add continuing state choice
            extendAddContinuingStateChoice(contentPane);

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

            extendBlockBCDSetup(contentPane);
            
            // set up Edit Block, Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            // Edit Block

            addDoneCancelButtons(panel5, editLayoutTurnoutFrame.getRootPane(),
                    this::editLayoutTurnoutDonePressed, this::editLayoutTurnoutCancelPressed);
            contentPane.add(panel5);
        }

        setUpForEdit();
        
        editLayoutTurnoutHiddenCheckBox.setSelected(layoutTurnoutView.isHidden());

        List<Turnout> currentTurnouts = new ArrayList<>();
        currentTurnouts.add(layoutTurnout.getTurnout());
        currentTurnouts.add(layoutTurnout.getSecondTurnout());

        editLayoutTurnout1stTurnoutComboBox.setSelectedItem(layoutTurnout.getTurnout());
        editLayoutTurnout1stTurnoutComboBox.addPopupMenuListener(
                layoutEditor.newTurnoutComboBoxPopupMenuListener(editLayoutTurnout1stTurnoutComboBox, currentTurnouts));

        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        editLayoutTurnoutBlockNameComboBox.getEditor().setItem(bm.getBlock(layoutTurnout.getBlockName()));
        editLayoutTurnoutBlockNameComboBox.setEnabled(!hasNxSensorPairs(layoutTurnout.getLayoutBlock()));
        
        configureCheckBoxes(bm);

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

        setUpContinuingSense();
        
        editLayoutTurnoutFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                editLayoutTurnoutCancelPressed(null);
            }
        });

        editLayoutTurnoutFrame.pack();
        editLayoutTurnoutFrame.setVisible(true);
        editLayoutTurnoutOpen = true;
        editLayoutTurnoutNeedsBlockUpdate = false;

        showSensorMessage();
    }


    // add continuing state choice
    protected void extendAddContinuingStateChoice(Container contentPane) {
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

    // for extension in e.g. XOver Editor
    protected void extendBlockBCDSetup(Container contentPane) {}

    protected void configureCheckBoxes(BlockManager bm) {
        editLayoutTurnout2ndTurnoutCheckBox.setText(Bundle.getMessage("ThrowTwoTurnouts"));  // NOI18N
    }

    // Set up for Edit
    protected void setUpForEdit() {
        editLayoutTurnoutFrame.setTitle(Bundle.getMessage("EditTurnout"));
        editLayoutTurnoutHiddenCheckBox.setText(Bundle.getMessage("HideTurnout"));
    }

    protected void setUpContinuingSense() {
        if (layoutTurnout.getContinuingSense() == Turnout.CLOSED) {
            editLayoutTurnoutStateComboBox.setSelectedIndex(editLayoutTurnoutClosedIndex);
        } else {
            editLayoutTurnoutStateComboBox.setSelectedIndex(editLayoutTurnoutThrownIndex);
        }
    }

    private void editLayoutTurnoutEditBlockPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutTurnoutBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutTurnout.getBlockName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutTurnoutView.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
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

    protected void editLayoutTurnoutEditBlockBPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutTurnoutBlockBNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutTurnout.getBlockBName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutTurnoutView.setLayoutBlockB(layoutEditor.provideLayoutBlock(newName));
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

    protected void editLayoutTurnoutEditBlockCPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutTurnoutBlockCNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutTurnout.getBlockCName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutTurnoutView.setLayoutBlockC(layoutEditor.provideLayoutBlock(newName));
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

    protected void editLayoutTurnoutEditBlockDPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutTurnoutBlockDNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutTurnout.getBlockDName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutTurnoutView.setLayoutBlockD(layoutEditor.provideLayoutBlock(newName));
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
            // turnout has changed, is it valid?
            if (layoutEditor.validatePhysicalTurnout(
                    newName, editLayoutTurnoutFrame)) {
                layoutTurnout.setTurnout(newName);
            } else {
                layoutTurnout.setTurnout("");
                editLayoutTurnout1stTurnoutComboBox.setSelectedItem(null);
            }
            editLayoutTurnoutNeedRedraw = true;
        }

        // check if 2nd Turnout changed
        if (editLayoutTurnout2ndTurnoutCheckBox.isSelected()) {
            newName = editLayoutTurnout2ndTurnoutComboBox.getSelectedItemDisplayName();
            if (newName == null) {
                newName = "";
            }
            if (!layoutTurnout.getSecondTurnoutName().equals(newName)) {
                donePressedSecondTurnoutName(newName);
            }
        } else {
            layoutTurnout.setSecondTurnout("");
        }

        // Do we have an infinite invert loop?
        if (editLayoutTurnout2ndTurnoutCheckBox.isSelected()) {
            Turnout turnout1 = layoutTurnout.getTurnout();
            LayoutTurnout layoutTurnout2 = null;
            int t1state = Turnout.CLOSED;
            int t2state = t1state;  // start off the same
            
            // invert if 2nd is inverted
            if (layoutTurnout.isSecondTurnoutInverted()) {
                t2state = Turnout.invertTurnoutState(t2state);
            }

            LayoutEditorFindItems lf = layoutEditor.getFinder();
            Turnout turnout2 = layoutTurnout.getSecondTurnout();
            while ((turnout2 != null) && (turnout1 != turnout2)) {
                // first try to find it using the system name
                layoutTurnout2 = lf.findLayoutTurnoutByTurnoutName(turnout2.getSystemName());
                if (layoutTurnout2 == null) {   // if we didn't find it
                    //  then try to find it using the user name
                    layoutTurnout2 = lf.findLayoutTurnoutByTurnoutName(turnout2.getUserName());
                }
                // if we found it
                if (layoutTurnout2 != null) {
                    // and its 2nd turnout is inverted
                    if (layoutTurnout2.isSecondTurnoutInverted()) {
                        //  then invert the expected state
                        t2state = Turnout.invertTurnoutState(t2state);
                    }
                    // and get the next 2nd turnout
                    turnout2 = layoutTurnout2.getSecondTurnout();
                } else {
                    break;  // we didn't find the next LayoutTurnout
                }
            }
            // if we've come full circle
            if (turnout1 == turnout2) {
                // are the states different?
                if (t1state != t2state) {
                    // yes (infinite loop)
                    if (layoutTurnout2 != null) {   // if 2nd LayoutTurnout is defined (to get here it should be)
                        // then flip its 2nd turnout's inverted condition
                        layoutTurnout2.setSecondTurnoutInverted(!layoutTurnout2.isSecondTurnoutInverted());
                    } else {    // (but just in case)
                        // otherwise flip our 2nd turnout's inverted condition
                        layoutTurnout.setSecondTurnoutInverted(!layoutTurnout.isSecondTurnoutInverted());
                    }
                }
            }
        }

        setContinuingRouteTurnoutState();
        
        // check if Block changed
        newName = editLayoutTurnoutBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutTurnout.getBlockName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutTurnoutView.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editLayoutTurnoutNeedRedraw = true;
            editLayoutTurnoutNeedsBlockUpdate = true;
        }
        
        checkBlock234Changed();
                
        // set hidden
        boolean oldHidden = layoutTurnoutView.isHidden();
        layoutTurnoutView.setHidden(editLayoutTurnoutHiddenCheckBox.isSelected());
        if (oldHidden != layoutTurnoutView.isHidden()) {
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
    }

    protected void donePressedSecondTurnoutName(String newName) {
        layoutTurnout.setSecondTurnout(newName);
    }

    // set the continuing route Turnout State
    protected void setContinuingRouteTurnoutState() {
        log.info("LayoutTurnoutEditor#setContinuingRouteTurnoutState should have been overridden in class {} object {}", this.getClass(), this);
        if ((layoutTurnout.getTurnoutType() == LayoutTurnout.TurnoutType.RH_TURNOUT)
                || (layoutTurnout.getTurnoutType() == LayoutTurnout.TurnoutType.LH_TURNOUT)
                || (layoutTurnout.getTurnoutType() == LayoutTurnout.TurnoutType.WYE_TURNOUT)) {
            layoutTurnout.setContinuingSense(Turnout.CLOSED);
            if (editLayoutTurnoutStateComboBox.getSelectedIndex() == editLayoutTurnoutThrownIndex) {
                layoutTurnout.setContinuingSense(Turnout.THROWN);
            }
        }
    }

    protected void checkBlock234Changed() {} 

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
