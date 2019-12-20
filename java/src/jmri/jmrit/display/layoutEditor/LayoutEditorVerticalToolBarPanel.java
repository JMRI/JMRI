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
@SuppressWarnings("serial")
@SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED") //no Serializable support at present
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

        JPanel outerBorderPanel = this;
        JPanel innerBorderPanel = this;

        Border blacklineBorder = BorderFactory.createLineBorder(Color.black);

        LayoutEditor.ToolBarSide toolBarSide = layoutEditor.getToolBarSide();
        boolean toolBarIsVertical = (toolBarSide.equals(LayoutEditor.ToolBarSide.eRIGHT)
                || toolBarSide.equals(LayoutEditor.ToolBarSide.eLEFT));

        if (toolBarIsVertical) {
            outerBorderPanel = new JPanel();
            outerBorderPanel.setLayout(new BoxLayout(outerBorderPanel, BoxLayout.PAGE_AXIS));
            TitledBorder outerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("Track"));
            outerTitleBorder.setTitleJustification(TitledBorder.CENTER);
            outerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
            outerBorderPanel.setBorder(outerTitleBorder);

            innerBorderPanel = new JPanel();
            innerBorderPanel.setLayout(new BoxLayout(innerBorderPanel, BoxLayout.PAGE_AXIS));
            TitledBorder innerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("BeanNameTurnouts"));
            innerTitleBorder.setTitleJustification(TitledBorder.CENTER);
            innerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
            innerBorderPanel.setBorder(innerTitleBorder);
        }

        String blockNameString = Bundle.getMessage("BlockID");

        if (toolBarIsVertical) {
            FlowLayout verticalTitleLayout = new FlowLayout(FlowLayout.CENTER, 5, 5); //5 pixel gap between items, 5 vertical gap
            FlowLayout verticalContentLayout = new FlowLayout(FlowLayout.LEFT, 5, 2); //5 pixel gap between items, 2 vertical gap

            turnoutLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("BeanNameTurnout")));

            if (!toolBarIsVertical) {
                JPanel vTop1TitlePanel = new JPanel(verticalTitleLayout);
                vTop1TitlePanel.add(turnoutLabel);
                vTop1TitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop1TitlePanel.getPreferredSize().height));
                innerBorderPanel.add(vTop1TitlePanel);
            }

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

            if (toolBarIsVertical) {
                innerBorderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, innerBorderPanel.getPreferredSize().height));
                outerBorderPanel.add(innerBorderPanel);
            }
            trackLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("Track")));

            if (!toolBarIsVertical) {
                JPanel vTop8TitlePanel = new JPanel(verticalTitleLayout);
                vTop8TitlePanel.add(trackLabel);
                vTop8TitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop8TitlePanel.getPreferredSize().height));
                outerBorderPanel.add(vTop8TitlePanel);
            }

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
            blockNameLabel = new JLabel(blockNameString);
            vTop10Panel.add(blockNameLabel);
            vTop10Panel.add(blockIDComboBox);
            vTop10Panel.add(highlightBlockCheckBox);
            vTop10Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop10Panel.getPreferredSize().height));
            outerBorderPanel.add(vTop10Panel);

            JPanel vTop11Panel = new JPanel(verticalContentLayout);
            vTop11Panel.add(blockSensorNameLabel);
            vTop11Panel.add(blockSensorComboBox);
            vTop11Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop11Panel.getPreferredSize().height));
            vTop11Panel.setBorder(new EmptyBorder(0, 10, 0, 0));

            outerBorderPanel.add(vTop11Panel);

            if (toolBarIsVertical) {
                add(outerBorderPanel);
            }

            JPanel nodesBorderPanel = this;
            nodesLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("Nodes")));

            if (toolBarIsVertical) {
                nodesBorderPanel = new JPanel();
                nodesBorderPanel.setLayout(new BoxLayout(nodesBorderPanel, BoxLayout.PAGE_AXIS));
                TitledBorder innerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("Nodes"));
                innerTitleBorder.setTitleJustification(TitledBorder.CENTER);
                innerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
                nodesBorderPanel.setBorder(innerTitleBorder);
            } else {
                JPanel vTop12TitlePanel = new JPanel(verticalTitleLayout);
                vTop12TitlePanel.add(nodesLabel);
                vTop12TitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop12TitlePanel.getPreferredSize().height));
                add(vTop12TitlePanel);
            }

            JPanel vTop12Panel = new JPanel(verticalContentLayout);
            vTop12Panel.add(endBumperButton);
            vTop12Panel.add(anchorButton);
            vTop12Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop12Panel.getPreferredSize().height));
            nodesBorderPanel.add(vTop12Panel);

            JPanel vTop13Panel = new JPanel(verticalContentLayout);
            vTop13Panel.add(edgeButton);
            vTop13Panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop13Panel.getPreferredSize().height));
            nodesBorderPanel.add(vTop13Panel);

            if (toolBarIsVertical) {
                add(nodesBorderPanel);
            }

            JPanel labelsBorderPanel = this;
            labelsLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("Labels")));

            if (toolBarIsVertical) {
                labelsBorderPanel = new JPanel();
                labelsBorderPanel.setLayout(new BoxLayout(labelsBorderPanel, BoxLayout.PAGE_AXIS));
                TitledBorder innerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("Labels"));
                innerTitleBorder.setTitleJustification(TitledBorder.CENTER);
                innerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
                labelsBorderPanel.setBorder(innerTitleBorder);
            } else {
                JPanel vTop14TitlePanel = new JPanel(verticalTitleLayout);
                vTop14TitlePanel.add(labelsLabel);
                vTop14TitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop14TitlePanel.getPreferredSize().height));
                add(vTop14TitlePanel);
            }

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

            if (toolBarIsVertical) {
                add(labelsBorderPanel);
            }

            JPanel iconsBorderPanel = this;

            if (toolBarIsVertical) {
                iconsBorderPanel = new JPanel();
                iconsBorderPanel.setLayout(new BoxLayout(iconsBorderPanel, BoxLayout.PAGE_AXIS));
                TitledBorder innerTitleBorder = BorderFactory.createTitledBorder(blacklineBorder, Bundle.getMessage("IconsTitle"));
                innerTitleBorder.setTitleJustification(TitledBorder.CENTER);
                innerTitleBorder.setTitlePosition(TitledBorder.BOTTOM);
                iconsBorderPanel.setBorder(innerTitleBorder);
            } else {
                JPanel vTop17TitlePanel = new JPanel(verticalTitleLayout);
                JLabel iconsLabel = new JLabel(String.format("-- %s --", Bundle.getMessage("IconsTitle")));
                vTop17TitlePanel.add(iconsLabel);
                vTop17TitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, vTop17TitlePanel.getPreferredSize().height));
                add(vTop17TitlePanel);
            }

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

            if (toolBarIsVertical) {
                add(iconsBorderPanel);
            }
            add(Box.createVerticalGlue());

            JPanel bottomPanel = new JPanel();
            zoomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, zoomPanel.getPreferredSize().height));
            locationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, locationPanel.getPreferredSize().height));
            bottomPanel.add(zoomPanel);
            bottomPanel.add(locationPanel);
            bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, bottomPanel.getPreferredSize().height));
            add(bottomPanel, BorderLayout.SOUTH);
        } else {
            //Row 1
            JPanel hTop1Panel = new JPanel();
            hTop1Panel.setLayout(new BoxLayout(hTop1Panel, BoxLayout.LINE_AXIS));

            //Row 1 : Left Components
            JPanel hTop1Left = new JPanel(leftRowLayout);
            turnoutLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));
            if (!toolBarIsVertical) {
                hTop1Left.add(turnoutLabel);
            }
            hTop1Left.add(turnoutRHButton);
            hTop1Left.add(turnoutLHButton);
            hTop1Left.add(turnoutWYEButton);
            hTop1Left.add(doubleXoverButton);
            hTop1Left.add(rhXoverButton);
            hTop1Left.add(lhXoverButton);
            hTop1Left.add(layoutSingleSlipButton);
            hTop1Left.add(layoutDoubleSlipButton);
            hTop1Panel.add(hTop1Left);

            if (toolBarIsWide) {
                hTop1Panel.add(Box.createHorizontalGlue());

                JPanel hTop1Right = new JPanel(rightRowLayout);
                hTop1Right.add(turnoutNamePanel);
                hTop1Right.add(extraTurnoutPanel);
                hTop1Right.add(rotationPanel);
                hTop1Panel.add(hTop1Right);
            }
            innerBorderPanel.add(hTop1Panel);

            //row 2
            if (!toolBarIsWide) {
                JPanel hTop2Panel = new JPanel();
                hTop2Panel.setLayout(new BoxLayout(hTop2Panel, BoxLayout.LINE_AXIS));

                //Row 2 : Left Components
                JPanel hTop2Center = new JPanel(centerRowLayout);
                hTop2Center.add(turnoutNamePanel);
                hTop2Center.add(extraTurnoutPanel);
                hTop2Center.add(rotationPanel);
                hTop2Panel.add(hTop2Center);

                innerBorderPanel.add(hTop2Panel);
            }

            if (toolBarIsVertical) {
                outerBorderPanel.add(innerBorderPanel);
            }

            //Row 3 (2 wide)
            JPanel hTop3Panel = new JPanel();
            hTop3Panel.setLayout(new BoxLayout(hTop3Panel, BoxLayout.LINE_AXIS));

            //Row 3 : Left Components
            JPanel hTop3Left = new JPanel(leftRowLayout);
            trackLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Track")));

            if (!toolBarIsVertical) {
                hTop3Left.add(trackLabel);
            }
            hTop3Left.add(levelXingButton);
            hTop3Left.add(trackButton);
            hTop3Left.add(trackSegmentPropertiesPanel);

            hTop3Panel.add(hTop3Left);
            hTop3Panel.add(Box.createHorizontalGlue());

            //Row 3 : Center Components
            JPanel hTop3Center = new JPanel(centerRowLayout);
            blockNameLabel = new JLabel(blockNameString);
            hTop3Center.add(blockNameLabel);
            hTop3Center.add(blockIDComboBox);
            hTop3Center.add(highlightBlockCheckBox);

            JPanel hTop3CenterA = new JPanel(centerRowLayout);
            hTop3CenterA.add(blockSensorLabel);
            hTop3CenterA.add(blockSensorComboBox);
            hTop3CenterA.setBorder(new EmptyBorder(0, 20, 0, 0));
            hTop3Center.add(hTop3CenterA);

            hTop3Panel.add(hTop3Center);
            hTop3Panel.add(Box.createHorizontalGlue());

            if (toolBarIsWide) {
                //row 3 : Right Components
                JPanel hTop3Right = new JPanel(rightRowLayout);
                hTop3Right.add(zoomPanel);
                hTop3Right.add(locationPanel);
                hTop3Panel.add(hTop3Right);
            }
            outerBorderPanel.add(hTop3Panel);

            if (toolBarIsVertical) {
                add(outerBorderPanel);
            }

            //Row 4
            JPanel hTop4Panel = new JPanel();
            hTop4Panel.setLayout(new BoxLayout(hTop4Panel, BoxLayout.LINE_AXIS));

            //Row 4 : Left Components
            JPanel hTop4Left = new JPanel(leftRowLayout);
            nodesLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Nodes")));
            hTop4Left.add(nodesLabel);
            hTop4Left.add(endBumperButton);
            hTop4Left.add(anchorButton);
            hTop4Left.add(edgeButton);
            hTop4Panel.add(hTop4Left);
            hTop4Panel.add(Box.createHorizontalGlue());

            if (!toolBarIsWide) {
                //Row 4 : Right Components
                JPanel hTop4Right = new JPanel(rightRowLayout);
                hTop4Right.add(zoomPanel);
                hTop4Right.add(locationPanel);
                hTop4Panel.add(hTop4Right);
            }
            add(hTop4Panel);

            //Row 5 Components (wide 4-center)
            labelsLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Labels")));
            if (toolBarIsWide) {
                JPanel hTop4Center = new JPanel(centerRowLayout);
                hTop4Center.add(labelsLabel);
                hTop4Center.add(textLabelButton);
                hTop4Center.add(textLabelTextField);
                hTop4Center.add(memoryButton);
                hTop4Center.add(textMemoryComboBox);
                hTop4Center.add(blockContentsButton);
                hTop4Center.add(blockContentsComboBox);
                hTop4Panel.add(hTop4Center);
                hTop4Panel.add(Box.createHorizontalGlue());
                add(hTop4Panel);
            } else {
                add(hTop4Panel);

                JPanel hTop5Left = new JPanel(leftRowLayout);
                hTop5Left.add(labelsLabel);
                hTop5Left.add(textLabelButton);
                hTop5Left.add(textLabelTextField);
                hTop5Left.add(memoryButton);
                hTop5Left.add(textMemoryComboBox);
                hTop5Left.add(blockContentsButton);
                hTop5Left.add(blockContentsComboBox);
                hTop5Left.add(Box.createHorizontalGlue());
                add(hTop5Left);
            }

            //Row 6
            JPanel hTop6Panel = new JPanel();
            hTop6Panel.setLayout(new BoxLayout(hTop6Panel, BoxLayout.LINE_AXIS));

            //Row 6 : Left Components
            //JPanel hTop6Left = new JPanel(centerRowLayout);
            JPanel hTop6Left = new JPanel(leftRowLayout);
            hTop6Left.add(multiSensorButton);
            hTop6Left.add(changeIconsButton);
            hTop6Left.add(sensorButton);
            hTop6Left.add(sensorComboBox);
            hTop6Left.add(signalMastButton);
            hTop6Left.add(signalMastComboBox);
            hTop6Left.add(signalButton);
            hTop6Left.add(signalHeadComboBox);
            hTop6Left.add(new JLabel(" "));
            hTop6Left.add(iconLabelButton);
            hTop6Left.add(shapeButton);

            hTop6Panel.add(hTop6Left);
            add(hTop6Panel);
        }
    }   //layoutComponents

    //initialize logging
    //private transient final static Logger log = LoggerFactory.getLogger(LayoutEditorVerticalToolBarPanel.class);
}
