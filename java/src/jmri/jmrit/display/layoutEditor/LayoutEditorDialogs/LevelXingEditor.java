package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.layoutEditor.*;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;

/**
 * MVC Editor component for LevelXing objects.
 *
 * Note there might not be anything for this class to do;
 * LayoutTrackEditors has a comment saying that PositionablePoint
 * doesn't have an editor.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LevelXingEditor extends LayoutTurntableEditor {

    /**
     * constructor method.
     * @param layoutEditor main layout editor.
     */
    public LevelXingEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    /*===============*\
    | Edit Level Xing |
    \*===============*/
    // variables for Edit Track Segment pane
    private LevelXingView levelXingView;
    private LevelXing levelXing;

    // variables for Edit Level Crossing pane
    private JmriJFrame editLevelXingFrame = null;
    private final JCheckBox editLevelXingHiddenCheckBox = new JCheckBox(Bundle.getMessage("HideCrossing"));  // NOI18N

    private final NamedBeanComboBox<Block> editLevelXingBlock1NameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> editLevelXingBlock2NameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);
    private JButton editLevelXingBlock1Button;
    private JButton editLevelXingBlock2Button;

    private boolean editLevelXingOpen = false;
    private boolean editLevelXingNeedsRedraw = false;
    private boolean editLevelXingNeedsBlockUpdate = false;

    /**
     * Edit a Level Crossing.
     * @param levelXingView the level crossing to edit.
     */
    public void editLayoutTrack(LevelXingView levelXingView) {
    try {
        sensorList.clear();

        this.levelXingView = levelXingView;
        this.levelXing = this.levelXingView.getLevelXing();
        
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
            editLevelXingBlock1Button.addActionListener(this::editLevelXingBlockACPressed);
            editLevelXingBlock1Button.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1  // NOI18N
            // Edit 2 Block
            panel4.add(editLevelXingBlock2Button = new JButton(Bundle.getMessage("EditBlock", "BD")));  // NOI18N
            editLevelXingBlock2Button.addActionListener(this::editLevelXingBlockBDPressed);
            editLevelXingBlock2Button.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1  // NOI18N
            contentPane.add(panel4);

            // set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            addDoneCancelButtons(panel5, editLevelXingFrame.getRootPane(),
                    this::editLevelXingDonePressed, this::editLevelXingCancelPressed);
            contentPane.add(panel5);
        }

        editLevelXingHiddenCheckBox.setSelected(levelXingView.isHidden());

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
    } catch (Throwable t) {
        log.error("temporary catch for test", t);
    }
    }

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
        boolean oldHidden = levelXingView.isHidden();
        levelXingView.setHidden(editLevelXingHiddenCheckBox.isSelected());
        if (oldHidden != levelXingView.isHidden()) {
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

    

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LevelXingEditor.class);
}
