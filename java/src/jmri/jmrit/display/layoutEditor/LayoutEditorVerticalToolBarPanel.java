package jmri.jmrit.display.layoutEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.*;

/**
 * This is the vertical toolbar panel
 *
 * @author George Warner Copyright: (c) 2019
 */
public class LayoutEditorVerticalToolBarPanel extends LayoutEditorToolBarPanel {

    /**
     * constructor for LayoutEditorVerticalToolBarPanel
     *
     * @param layoutEditor the layout editor that this is for
     */
    public LayoutEditorVerticalToolBarPanel(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }   //constructor

    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutComponents() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        Border blacklineBorder = BorderFactory.createLineBorder(Color.black);

        JPanel outerBorderPanel = new JPanel();
        outerBorderPanel.setLayout(new BoxLayout(outerBorderPanel, BoxLayout.PAGE_AXIS));
        TitledBorder outerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("Track"));
        outerTitleBorder.setTitleJustification(TitledBorder.CENTER);
        outerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
        outerBorderPanel.setBorder(outerTitleBorder);

        JPanel innerBorderPanel = new JPanel();
        innerBorderPanel.setLayout(new BoxLayout(innerBorderPanel, BoxLayout.PAGE_AXIS));
        TitledBorder beanNameTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("BeanNameTurnouts"));
        beanNameTitleBorder.setTitleJustification(TitledBorder.CENTER);
        beanNameTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
        innerBorderPanel.setBorder(beanNameTitleBorder);

        FlowLayout verticalContentLayout = new FlowLayout(FlowLayout.LEFT, 5, 2); //5 pixel gap between items, 2 vertical gap

        turnoutLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("BeanNameTurnout")));

        JPanel vTop1Panel = new JPanel(verticalContentLayout);
        vTop1Panel.add(turnoutLHButton);
        vTop1Panel.add(turnoutRHButton);
        vTop1Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop1Panel.getPreferredSize().height));
        innerBorderPanel.add(vTop1Panel);

        JPanel vTop2Panel = new JPanel(verticalContentLayout);
        vTop2Panel.add(turnoutWYEButton);
        vTop2Panel.add(doubleXoverButton);
        vTop2Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop2Panel.getPreferredSize().height * 2));
        innerBorderPanel.add(vTop2Panel);

        JPanel vTop3Panel = new JPanel(verticalContentLayout);
        vTop3Panel.add(lhXoverButton);
        vTop3Panel.add(rhXoverButton);
        vTop3Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop3Panel.getPreferredSize().height));
        innerBorderPanel.add(vTop3Panel);

        JPanel vTop4Panel = new JPanel(verticalContentLayout);
        vTop4Panel.add(layoutSingleSlipButton);
        vTop4Panel.add(layoutDoubleSlipButton);
        vTop4Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop4Panel.getPreferredSize().height));
        innerBorderPanel.add(vTop4Panel);

        JPanel vTop5Panel = new JPanel(verticalContentLayout);
        vTop5Panel.add(turnoutNamePanel);
        vTop5Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop5Panel.getPreferredSize().height));
        innerBorderPanel.add(vTop5Panel);

        JPanel vTop6Panel = new JPanel(verticalContentLayout);
        vTop6Panel.add(extraTurnoutPanel);
        vTop6Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop6Panel.getPreferredSize().height));
        innerBorderPanel.add(vTop6Panel);

        JPanel vTop7Panel = new JPanel(verticalContentLayout);
        vTop7Panel.add(rotationPanel);
        vTop7Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop7Panel.getPreferredSize().height));
        innerBorderPanel.add(vTop7Panel);

        innerBorderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, innerBorderPanel.getPreferredSize().height));
        outerBorderPanel.add(innerBorderPanel);

        trackLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("Track")));

        JPanel vTop8Panel = new JPanel(verticalContentLayout);
        vTop8Panel.add(levelXingButton);
        vTop8Panel.add(trackButton);
        vTop8Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop8Panel.getPreferredSize().height));
        outerBorderPanel.add(vTop8Panel);

        //this would be vTop9Panel
        trackSegmentPropertiesPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                trackSegmentPropertiesPanel.getPreferredSize().height));
        outerBorderPanel.add(trackSegmentPropertiesPanel);

        JPanel vTop10Panel = new JPanel(verticalContentLayout);
        vTop10Panel.add(blockLabel);
        vTop10Panel.add(blockIDComboBox);
        vTop10Panel.add(highlightBlockCheckBox);
        vTop10Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop10Panel.getPreferredSize().height));
        outerBorderPanel.add(vTop10Panel);

        JPanel vTop11Panel = new JPanel(verticalContentLayout);
        vTop11Panel.add(blockSensorLabel);
        vTop11Panel.add(blockSensorComboBox);
        vTop11Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop11Panel.getPreferredSize().height));
        vTop11Panel.setBorder(new EmptyBorder(0, 10, 0, 0));

        outerBorderPanel.add(vTop11Panel);

        // Tiles section - create its own bordered panel at bottom of Track section
        JPanel tilesBorderPanel = new JPanel();
        tilesBorderPanel.setLayout(new GridBagLayout());
        TitledBorder tilesTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, "Tiles");
        tilesTitleBorder.setTitleJustification(TitledBorder.CENTER);
        tilesTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
        tilesBorderPanel.setBorder(tilesTitleBorder);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(2, 5, 2, 5);
        tilesBorderPanel.add(tileVendorLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        tilesBorderPanel.add(tileVendorComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        tilesBorderPanel.add(tileFamilyLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        tilesBorderPanel.add(tileFamilyComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        tilesBorderPanel.add(tileNameLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        tilesBorderPanel.add(tileComboBox, gbc);

        // Add left/right radio buttons for curved tiles
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JPanel directionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        directionPanel.add(tileLeftButton);
        directionPanel.add(tileRightButton);
        directionPanel.add(turnoutThroatButton);
        directionPanel.add(turnoutNormalButton);
        directionPanel.add(turnoutThrownButton);
        tilesBorderPanel.add(directionPanel, gbc);

        tilesBorderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, tilesBorderPanel.getPreferredSize().height));
        outerBorderPanel.add(tilesBorderPanel);

        add(outerBorderPanel);

        JPanel nodesBorderPanel = new JPanel();
        nodesBorderPanel.setLayout(new BoxLayout(nodesBorderPanel, BoxLayout.PAGE_AXIS));
        TitledBorder nodesTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("Nodes"));
        nodesTitleBorder.setTitleJustification(TitledBorder.CENTER);
        nodesTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
        nodesBorderPanel.setBorder(nodesTitleBorder);

        JPanel vTop12bPanel = new JPanel(verticalContentLayout);
        vTop12bPanel.add(endBumperButton);
        vTop12bPanel.add(anchorButton);
        vTop12bPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop12bPanel.getPreferredSize().height));
        nodesBorderPanel.add(vTop12bPanel);

        JPanel vTop13Panel = new JPanel(verticalContentLayout);
        vTop13Panel.add(edgeButton);
        vTop13Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop13Panel.getPreferredSize().height));
        nodesBorderPanel.add(vTop13Panel);

        add(nodesBorderPanel);

        labelsLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("Labels")));

        JPanel labelsBorderPanel = new JPanel();
        labelsBorderPanel.setLayout(new BoxLayout(labelsBorderPanel, BoxLayout.PAGE_AXIS));
        TitledBorder innerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("Labels"));
        innerTitleBorder.setTitleJustification(TitledBorder.CENTER);
        innerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
        labelsBorderPanel.setBorder(innerTitleBorder);

        JPanel vTop14Panel = new JPanel(verticalContentLayout);
        vTop14Panel.add(textLabelButton);
        vTop14Panel.add(textLabelTextField);
        vTop14Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop14Panel.getPreferredSize().height));
        labelsBorderPanel.add(vTop14Panel);

        JPanel vTop15Panel = new JPanel(verticalContentLayout);
        vTop15Panel.add(memoryButton);
        vTop15Panel.add(textMemoryComboBox);
        vTop15Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop15Panel.getPreferredSize().height));
        labelsBorderPanel.add(vTop15Panel);

        JPanel vTop17Panel = new JPanel(verticalContentLayout);
        vTop17Panel.add(globalVariableButton);
        vTop17Panel.add(textGlobalVariableComboBox);
        vTop17Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop17Panel.getPreferredSize().height));
        labelsBorderPanel.add(vTop17Panel);

        JPanel vTop16Panel = new JPanel(verticalContentLayout);
        vTop16Panel.add(blockContentsButton);
        vTop16Panel.add(blockContentsComboBox);
        vTop16Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop16Panel.getPreferredSize().height));
        labelsBorderPanel.add(vTop16Panel);

        add(labelsBorderPanel);

        JPanel iconsBorderPanel = new JPanel();
        iconsBorderPanel.setLayout(new BoxLayout(iconsBorderPanel, BoxLayout.PAGE_AXIS));
        TitledBorder iconsTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("IconsTitle"));
        iconsTitleBorder.setTitleJustification(TitledBorder.CENTER);
        iconsTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
        iconsBorderPanel.setBorder(iconsTitleBorder);

        JPanel vTop18Panel = new JPanel(verticalContentLayout);
        vTop18Panel.add(multiSensorButton);
        vTop18Panel.add(changeIconsButton);
        vTop18Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop18Panel.getPreferredSize().height));
        iconsBorderPanel.add(vTop18Panel);

        JPanel vTop20Panel = new JPanel(verticalContentLayout);
        vTop20Panel.add(sensorButton);
        vTop20Panel.add(sensorComboBox);
        vTop20Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop20Panel.getPreferredSize().height));
        iconsBorderPanel.add(vTop20Panel);

        JPanel vTop19Panel = new JPanel(verticalContentLayout);
        vTop19Panel.add(signalMastButton);
        vTop19Panel.add(signalMastComboBox);
        vTop19Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop19Panel.getPreferredSize().height));
        iconsBorderPanel.add(vTop19Panel);

        JPanel vTop21Panel = new JPanel(verticalContentLayout);
        vTop21Panel.add(signalButton);
        vTop21Panel.add(signalHeadComboBox);
        vTop21Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop21Panel.getPreferredSize().height));
        iconsBorderPanel.add(vTop21Panel);

        JPanel vTop22Panel = new JPanel(verticalContentLayout);
        vTop22Panel.add(iconLabelButton);
        vTop22Panel.add(logixngButton);
        vTop22Panel.add(shapeButton);
        vTop22Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop22Panel.getPreferredSize().height));
        iconsBorderPanel.add(vTop22Panel);

        JPanel vTop23Panel = new JPanel(verticalContentLayout);
        vTop23Panel.add(audioButton);
        vTop23Panel.add(textAudioComboBox);
        vTop23Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop23Panel.getPreferredSize().height));
        iconsBorderPanel.add(vTop23Panel);

        add(iconsBorderPanel);
        add(Box.createVerticalGlue());

        JPanel bottomPanel = new JPanel();
        zoomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, zoomPanel.getPreferredSize().height));
        locationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, locationPanel.getPreferredSize().height));
        bottomPanel.add(zoomPanel);
        bottomPanel.add(locationPanel);
        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, bottomPanel.getPreferredSize().height));
        add(bottomPanel, BorderLayout.SOUTH);
    }   //layoutComponents

    //initialize logging
    //private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorVerticalToolBarPanel.class);
}
