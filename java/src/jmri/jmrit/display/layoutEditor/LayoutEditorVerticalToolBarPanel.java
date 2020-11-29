package jmri.jmrit.display.layoutEditor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
        add(outerBorderPanel);

        JPanel nodesBorderPanel = new JPanel();
        nodesBorderPanel.setLayout(new BoxLayout(nodesBorderPanel, BoxLayout.PAGE_AXIS));
        TitledBorder nodesTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("Nodes"));
        nodesTitleBorder.setTitleJustification(TitledBorder.CENTER);
        nodesTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
        nodesBorderPanel.setBorder(nodesTitleBorder);

        JPanel vTop12Panel = new JPanel(verticalContentLayout);
        vTop12Panel.add(endBumperButton);
        vTop12Panel.add(anchorButton);
        vTop12Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop12Panel.getPreferredSize().height));
        nodesBorderPanel.add(vTop12Panel);

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
        vTop22Panel.add(shapeButton);
        vTop22Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop22Panel.getPreferredSize().height));
        vTop22Panel.add(changeIconsButton);
        iconsBorderPanel.add(vTop22Panel);

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
