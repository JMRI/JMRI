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
import jmri.tracktiles.NotATile;
import jmri.tracktiles.TrackTile;
import jmri.tracktiles.UnknownTile;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;
import java.awt.Color;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.util.List;

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
    private final JLabel editLevelXingTileInfoLabel = new JLabel();
    private final JTextArea editLevelXingPathLengthsArea = new JTextArea(3, 40);

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

            // add tile information display
            JPanel panelTile = new JPanel();
            panelTile.setLayout(new FlowLayout());
            JLabel tileLabel = new JLabel(Bundle.getMessage("TrackTile") + ": ");  // NOI18N
            panelTile.add(tileLabel);
            editLevelXingTileInfoLabel.setToolTipText(Bundle.getMessage("TrackTileInfoHint"));  // NOI18N
            panelTile.add(editLevelXingTileInfoLabel);
            contentPane.add(panelTile);

            // add path lengths display
            TitledBorder pathBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
            pathBorder.setTitle("Path Lengths");  // NOI18N
            JPanel panelPathLengths = new JPanel();
            panelPathLengths.setBorder(pathBorder);
            panelPathLengths.setLayout(new java.awt.BorderLayout());
            editLevelXingPathLengthsArea.setEditable(false);
            editLevelXingPathLengthsArea.setBackground(UIManager.getColor("Panel.background"));
            editLevelXingPathLengthsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            editLevelXingPathLengthsArea.setToolTipText("Calculated path lengths based on track tile geometry");  // NOI18N
            JScrollPane pathScrollPane = new JScrollPane(editLevelXingPathLengthsArea);
            pathScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            pathScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            panelPathLengths.add(pathScrollPane, java.awt.BorderLayout.CENTER);
            contentPane.add(panelPathLengths);

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

        // Update tile information display
        TrackTile tile = levelXing.getTrackTile();
        if (tile instanceof NotATile) {
            editLevelXingTileInfoLabel.setText(Bundle.getMessage("NoTile"));  // NOI18N
            editLevelXingPathLengthsArea.setText("No track tile associated");
        } else if (tile instanceof UnknownTile) {
            editLevelXingTileInfoLabel.setText(Bundle.getMessage("UnknownTile") + " (" + tile.getVendor() + " / " + tile.getFamily() + " / " + tile.getPartCode() + ")");  // NOI18N
            editLevelXingPathLengthsArea.setText("Path lengths not available for unknown tiles");
        } else {
            editLevelXingTileInfoLabel.setText(tile.getVendor() + " / " + tile.getFamily() + " / " + tile.getPartCode());
            updateLevelXingPathLengthsDisplay();
        }

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
            JmriJOptionPane.showMessageDialog(editLevelXingFrame,
                    Bundle.getMessage("Error1"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);  // NOI18N
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
            JmriJOptionPane.showMessageDialog(editLevelXingFrame,
                    Bundle.getMessage("Error1"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);  // NOI18N
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

    

    /**
     * Update the path lengths display area for level crossings.
     */
    protected void updateLevelXingPathLengthsDisplay() {
        TrackTile tile = levelXing.getTrackTile();
        List<String> pathLengths = calculateLevelXingPathLengths(tile);
        
        StringBuilder sb = new StringBuilder();
        for (String pathLength : pathLengths) {
            sb.append(pathLength).append("\n");
        }
        
        editLevelXingPathLengthsArea.setText(sb.toString());
        editLevelXingPathLengthsArea.setCaretPosition(0);  // Scroll to top
    }

    /**
     * Calculate path lengths for level crossings.
     */
    private List<String> calculateLevelXingPathLengths(TrackTile tile) {
        List<String> pathLengths = new java.util.ArrayList<>();
        
        if (tile instanceof NotATile) {
            pathLengths.add("No track tile assigned");
            return pathLengths;
        }
        
        if (tile instanceof UnknownTile) {
            pathLengths.add("Unknown track tile - no geometry available");
            return pathLengths;
        }
        
        if (!tile.hasPaths()) {
            pathLengths.add("No path geometry available");
            return pathLengths;
        }

        List<jmri.tracktiles.TrackTilePath> paths = tile.getPaths();
        
        for (jmri.tracktiles.TrackTilePath path : paths) {
            StringBuilder sb = new StringBuilder();
            
            // For level crossings, show direction-based path names
            if ("straight".equals(path.getDirection())) {
                sb.append("Path A-C: ");
            } else if ("crossing".equals(path.getDirection())) {
                sb.append("Path B-D: ");
            } else {
                sb.append(path.getDirection()).append(" path: ");
            }
            
            // Add length information
            double length = path.calculateLength();
            if (length > 0) {
                sb.append(String.format("%.1f mm", length));
                
                // Add additional details for curved paths
                if (path.isCurved()) {
                    sb.append(String.format(" (R%.1f, ∠%.1f°)", 
                        path.getRadius(), path.getArc()));
                }
            } else if (path.isCrossingWithLength()) {
                // Crossing path with explicit length and angle
                sb.append(String.format("%.1f mm ∠%.1f° crossing", 
                    path.getLength(), path.getArc()));
            } else if (path.isAngleOnly()) {
                // This is an angle-only crossing specification
                // For crossings, we should assume the crossing path has the same length as the straight path
                // but we only have the crossing angle
                sb.append(String.format("∠%.1f° crossing", path.getArc()));
                
                // Try to find the straight path length to use for the crossing path
                List<jmri.tracktiles.TrackTilePath> allPaths = tile.getPaths();
                for (jmri.tracktiles.TrackTilePath otherPath : allPaths) {
                    if ("straight".equals(otherPath.getDirection()) && otherPath.getLength() > 0) {
                        sb.append(String.format(" (%.1f mm)", otherPath.getLength()));
                        break;
                    }
                }
            } else {
                sb.append("No geometry data");
            }
            
            pathLengths.add(sb.toString());
        }
        
        if (pathLengths.isEmpty()) {
            pathLengths.add("No valid path geometry found");
        }

        return pathLengths;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LevelXingEditor.class);
}
