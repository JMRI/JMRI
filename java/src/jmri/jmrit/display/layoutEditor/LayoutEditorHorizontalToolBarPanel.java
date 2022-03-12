package jmri.jmrit.display.layoutEditor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.*;

/**
 * This is the horizontal toolbar panel
 *
 * @author George Warner Copyright: (c) 2019
 */
public class LayoutEditorHorizontalToolBarPanel extends LayoutEditorToolBarPanel {

    /**
     * constructor for LayoutEditorHorizontalToolBarPanel
     *
     * @param layoutEditor the layout editor that this is for
     */
    public LayoutEditorHorizontalToolBarPanel(@Nonnull LayoutEditor layoutEditor) {
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

        //Row 1
        JPanel hTop1Panel = new JPanel();
        hTop1Panel.setLayout(new BoxLayout(hTop1Panel, BoxLayout.LINE_AXIS));

        //Row 1 : Left Components
        JPanel hTop1Left = new JPanel(leftRowLayout);
        turnoutLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));
        hTop1Left.add(turnoutLabel);
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

        //Row 3 (2 wide)
        JPanel hTop3Panel = new JPanel();
        hTop3Panel.setLayout(new BoxLayout(hTop3Panel, BoxLayout.LINE_AXIS));

        //Row 3 : Left Components
        JPanel hTop3Left = new JPanel(leftRowLayout);
        trackLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Track")));

        hTop3Left.add(trackLabel);
        hTop3Left.add(levelXingButton);
        hTop3Left.add(trackButton);
        hTop3Left.add(trackSegmentPropertiesPanel);

        hTop3Panel.add(hTop3Left);
        hTop3Panel.add(Box.createHorizontalGlue());

        //Row 3 : Center Components
        JPanel hTop3Center = new JPanel(centerRowLayout);
        hTop3Center.add(blockLabel);
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

        //Row 4
        JPanel hTop4Panel = new JPanel();
        hTop4Panel.setLayout(new BoxLayout(hTop4Panel, BoxLayout.LINE_AXIS));

        //Row 4 : Left Components
        JPanel hTop4Left = new JPanel(leftRowLayout);
        hTop4Left.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Nodes"))));
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
    }   //layoutComponents

    //initialize logging
    //private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorHorizontalToolBarPanel.class);
}
