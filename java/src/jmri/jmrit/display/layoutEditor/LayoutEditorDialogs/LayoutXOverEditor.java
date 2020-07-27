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
 * MVC Editor component for LayoutXOver objects.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutXOverEditor extends LayoutTurnoutEditor {

    /**
     * constructor method.
     * @param layoutEditor main layout editor.
     */
    public LayoutXOverEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    /**
     * Edit a XOver
     */
    @Override
    public void editLayoutTrack(@Nonnull LayoutTrack layoutTrack) {
        log.trace("LayoutXOverEditor.editLayoutTrack({}) of a {}", layoutTrack, layoutTrack.getClass());
        super.editLayoutTrack(layoutTrack);
    }
    

    // not used by crossover
    @Override
    protected void extendAddContinuingStateChoice(Container contentPane) {
    }

    @Override
    protected void extendBlockBCDSetup(Container contentPane) {
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
    
    @Override
    protected void configureCheckBoxes(BlockManager bm) {
        editLayoutTurnoutBlockBNameComboBox.getEditor().setItem(bm.getBlock(layoutTurnout.getBlockBName()));
        editLayoutTurnoutBlockCNameComboBox.getEditor().setItem(bm.getBlock(layoutTurnout.getBlockCName()));
        editLayoutTurnoutBlockDNameComboBox.getEditor().setItem(bm.getBlock(layoutTurnout.getBlockDName()));
        editLayoutTurnoutBlockBNameComboBox.setEnabled(!hasNxSensorPairs(layoutTurnout.getLayoutBlockB()));
        editLayoutTurnoutBlockCNameComboBox.setEnabled(!hasNxSensorPairs(layoutTurnout.getLayoutBlockC()));
        editLayoutTurnoutBlockDNameComboBox.setEnabled(!hasNxSensorPairs(layoutTurnout.getLayoutBlockD()));
    }

    // Set up for Edit
    @Override
    protected void setUpForEdit() {
            editLayoutTurnoutFrame.setTitle(Bundle.getMessage("EditXover"));
            editLayoutTurnoutHiddenCheckBox.setText(Bundle.getMessage("HideXover"));
    }

    @Override
    protected void setUpContinuingSense() {}

    @Override
    protected void donePressedSecondTurnoutName(String newName) {
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
    }

    // set the continuing route Turnout State
    @Override
    protected void setContinuingRouteTurnoutState() {
        // this had content in LayoutTurnoutEditor superclass, which we don't want to do here.
    }

    @Override
     protected void checkBlock234Changed() {
        String newName;
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutXOverEditor.class);
}
