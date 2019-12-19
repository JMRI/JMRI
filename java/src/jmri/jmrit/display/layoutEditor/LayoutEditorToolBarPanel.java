package jmri.jmrit.display.layoutEditor;

import static jmri.jmrit.display.layoutEditor.LayoutEditor.setupComboBox;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.*;
import jmri.*;
import jmri.swing.NamedBeanComboBox;
import org.slf4j.*;

/**
 * This is the base class for the horizontal, vertical and floating toolbar
 * panels
 *
 * @author Dave Duchamp Copyright: (c) 2004-2007
 * @author George Warner Copyright: (c) 2017-2019
 */
@SuppressWarnings("serial")
@SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED") //no Serializable support at present
public class LayoutEditorToolBarPanel extends JPanel {

    protected LayoutEditor layoutEditor = null;

    //TODO: move all these into LayoutEditorFloatingToolBarPanel subclass of this class
    protected JPanel floatingEditPanel = new JPanel();
    protected JPanel floatEditTabsPanel = new JPanel();
    protected JTabbedPane floatEditTabsPane = new JTabbedPane();
    protected JPanel blockPropertiesPanel = null;

    //top row of radio buttons
    private transient JLabel turnoutLabel = new JLabel();
    protected transient JRadioButton turnoutRHButton = new JRadioButton(Bundle.getMessage("RightHandAbbreviation"));
    protected transient JRadioButton turnoutLHButton = new JRadioButton(Bundle.getMessage("LeftHandAbbreviation"));
    protected transient JRadioButton turnoutWYEButton = new JRadioButton(Bundle.getMessage("WYEAbbreviation"));
    protected transient JRadioButton doubleXoverButton = new JRadioButton(Bundle.getMessage("DoubleCrossoverAbbreviation"));
    protected transient JRadioButton rhXoverButton = new JRadioButton(Bundle.getMessage("RightCrossover")); //key is also used by Control Panel
    //Editor, placed in DisplayBundle
    protected transient JRadioButton lhXoverButton = new JRadioButton(Bundle.getMessage("LeftCrossover")); //idem
    protected transient JRadioButton layoutSingleSlipButton = new JRadioButton(Bundle.getMessage("LayoutSingleSlip"));
    protected transient JRadioButton layoutDoubleSlipButton = new JRadioButton(Bundle.getMessage("LayoutDoubleSlip"));

    //Default flow layout definitions for JPanels
    private transient FlowLayout leftRowLayout = new FlowLayout(FlowLayout.LEFT, 5, 0);       //5 pixel gap between items, no vertical gap
    private transient FlowLayout centerRowLayout = new FlowLayout(FlowLayout.CENTER, 5, 0);   //5 pixel gap between items, no vertical gap
    private transient FlowLayout rightRowLayout = new FlowLayout(FlowLayout.RIGHT, 5, 0);     //5 pixel gap between items, no vertical gap

    //top row of check boxes
    protected transient NamedBeanComboBox<Turnout> turnoutNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(), null, NamedBean.DisplayOptions.DISPLAYNAME);

    private transient JPanel turnoutNamePanel = new JPanel(leftRowLayout);
    private transient JPanel extraTurnoutPanel = new JPanel(leftRowLayout);
    protected transient NamedBeanComboBox<Turnout> extraTurnoutNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(), null, NamedBean.DisplayOptions.DISPLAYNAME);
    protected transient JComboBox<String> rotationComboBox = null;
    private transient JPanel rotationPanel = new JPanel(leftRowLayout);

    //2nd row of radio buttons
    private transient JLabel trackLabel = new JLabel();
    protected transient JRadioButton levelXingButton = new JRadioButton(Bundle.getMessage("LevelCrossing"));
    protected transient JRadioButton trackButton = new JRadioButton(Bundle.getMessage("TrackSegment"));

    //2nd row of check boxes
    private transient JPanel trackSegmentPropertiesPanel = new JPanel(leftRowLayout);
    protected transient JCheckBox mainlineTrack = new JCheckBox(Bundle.getMessage("MainlineBox"));
    protected transient JCheckBox dashedLine = new JCheckBox(Bundle.getMessage("Dashed"));

    private transient JLabel blockNameLabel = new JLabel();
    protected transient NamedBeanComboBox<Block> blockIDComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    protected transient JCheckBox highlightBlockCheckBox = new JCheckBox(Bundle.getMessage("HighlightSelectedBlockTitle"));

    private transient JLabel blockSensorNameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BlockSensorName")));
    private transient JLabel blockSensorLabel = new JLabel(Bundle.getMessage("BeanNameSensor"));
    protected transient NamedBeanComboBox<Sensor> blockSensorComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    //3rd row of radio buttons (and any associated text fields)
    private transient JLabel nodesLabel = new JLabel();
    protected transient JRadioButton endBumperButton = new JRadioButton(Bundle.getMessage("EndBumper"));
    protected transient JRadioButton anchorButton = new JRadioButton(Bundle.getMessage("Anchor"));
    protected transient JRadioButton edgeButton = new JRadioButton(Bundle.getMessage("EdgeConnector"));

    private transient JLabel labelsLabel = new JLabel();
    protected transient JRadioButton textLabelButton = new JRadioButton(Bundle.getMessage("TextLabel"));
    protected transient JTextField textLabelTextField = new JTextField(12);

    protected transient JRadioButton memoryButton = new JRadioButton(Bundle.getMessage("BeanNameMemory"));
    protected transient NamedBeanComboBox<Memory> textMemoryComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(MemoryManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    protected transient JRadioButton blockContentsButton = new JRadioButton(Bundle.getMessage("BlockContentsLabel"));
    protected transient NamedBeanComboBox<Block> blockContentsComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    //4th row of radio buttons (and any associated text fields)
    protected transient JRadioButton multiSensorButton = new JRadioButton(Bundle.getMessage("MultiSensor") + "...");

    protected transient JRadioButton signalMastButton = new JRadioButton(Bundle.getMessage("SignalMastIcon"));
    protected transient NamedBeanComboBox<SignalMast> signalMastComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalMastManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    protected transient JRadioButton sensorButton = new JRadioButton(Bundle.getMessage("SensorIcon"));
    protected transient NamedBeanComboBox<Sensor> sensorComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    protected transient JRadioButton signalButton = new JRadioButton(Bundle.getMessage("SignalIcon"));
    protected transient NamedBeanComboBox<SignalHead> signalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    protected transient JRadioButton iconLabelButton = new JRadioButton(Bundle.getMessage("IconLabel"));
    protected transient JRadioButton shapeButton = new JRadioButton(Bundle.getMessage("LayoutShape"));

    private transient JButton changeIconsButton = new JButton(Bundle.getMessage("ChangeIcons") + "...");

    protected transient MultiIconEditor sensorIconEditor = null;
    protected transient JFrame sensorFrame = null;

    protected transient MultiIconEditor signalIconEditor = null;
    protected transient JFrame signalFrame = null;

    protected transient MultiIconEditor iconEditor = null;
    protected transient JFrame iconFrame = null;

    protected transient MultiSensorIconFrame multiSensorFrame = null;

    protected transient JLabel xLabel = new JLabel("00");
    protected transient JLabel yLabel = new JLabel("00");

    private transient JPanel zoomPanel = new JPanel();
    protected transient JLabel zoomLabel = new JLabel("x1");

    private transient JPanel locationPanel = new JPanel();

    //non-GUI variables
    protected transient boolean toolBarIsWide = true;
    private transient ButtonGroup itemGroup = null;

    /**
     * constructor for LayoutEditorToolBarPanel
     *
     * @param layoutEditor the layout editor that this is for
     */
    public LayoutEditorToolBarPanel(@Nonnull LayoutEditor layoutEditor) {
        this.layoutEditor = layoutEditor;

        setupComponents();

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
    }   //constructor

    private void setupComponents() {
        //setup group for radio buttons selecting items to add and line style
        itemGroup = new ButtonGroup();
        itemGroup.add(turnoutRHButton);
        itemGroup.add(turnoutLHButton);
        itemGroup.add(turnoutWYEButton);
        itemGroup.add(doubleXoverButton);
        itemGroup.add(rhXoverButton);
        itemGroup.add(lhXoverButton);
        itemGroup.add(levelXingButton);
        itemGroup.add(layoutSingleSlipButton);
        itemGroup.add(layoutDoubleSlipButton);
        itemGroup.add(endBumperButton);
        itemGroup.add(anchorButton);
        itemGroup.add(edgeButton);
        itemGroup.add(trackButton);
        itemGroup.add(multiSensorButton);
        itemGroup.add(sensorButton);
        itemGroup.add(signalButton);
        itemGroup.add(signalMastButton);
        itemGroup.add(textLabelButton);
        itemGroup.add(memoryButton);
        itemGroup.add(blockContentsButton);
        itemGroup.add(iconLabelButton);
        itemGroup.add(shapeButton);

        //This is used to enable/disable property controls depending on which (radio) button is selected
        ActionListener selectionListAction = (ActionEvent event) -> {
            //turnout properties
            boolean e = (turnoutRHButton.isSelected()
                    || turnoutLHButton.isSelected()
                    || turnoutWYEButton.isSelected()
                    || doubleXoverButton.isSelected()
                    || rhXoverButton.isSelected()
                    || lhXoverButton.isSelected()
                    || layoutSingleSlipButton.isSelected()
                    || layoutDoubleSlipButton.isSelected());
            log.debug("turnoutPropertiesPanel is {}", e ? "enabled" : "disabled");
            turnoutNamePanel.setEnabled(e);

            for (Component i : turnoutNamePanel.getComponents()) {
                i.setEnabled(e);
            }
            rotationPanel.setEnabled(e);

            for (Component i : rotationPanel.getComponents()) {
                i.setEnabled(e);
            }

            //second turnout property
            e = (layoutSingleSlipButton.isSelected() || layoutDoubleSlipButton.isSelected());
            log.debug("extraTurnoutPanel is {}", e ? "enabled" : "disabled");

            for (Component i : extraTurnoutPanel.getComponents()) {
                i.setEnabled(e);
            }

            //track Segment properties
            e = trackButton.isSelected();
            log.debug("trackSegmentPropertiesPanel is {}", e ? "enabled" : "disabled");

            for (Component i : trackSegmentPropertiesPanel.getComponents()) {
                i.setEnabled(e);
            }

            //block properties
            e = (turnoutRHButton.isSelected()
                    || turnoutLHButton.isSelected()
                    || turnoutWYEButton.isSelected()
                    || doubleXoverButton.isSelected()
                    || rhXoverButton.isSelected()
                    || lhXoverButton.isSelected()
                    || layoutSingleSlipButton.isSelected()
                    || layoutDoubleSlipButton.isSelected()
                    || levelXingButton.isSelected()
                    || trackButton.isSelected());
            log.debug("blockPanel is {}", e ? "enabled" : "disabled");

            if (blockPropertiesPanel != null) {
                for (Component i : blockPropertiesPanel.getComponents()) {
                    i.setEnabled(e);
                }

                if (e) {
                    blockPropertiesPanel.setBackground(Color.lightGray);
                } else {
                    blockPropertiesPanel.setBackground(new Color(238, 238, 238));
                }
            } else {
                blockNameLabel.setEnabled(e);
                blockIDComboBox.setEnabled(e);
                blockSensorNameLabel.setEnabled(e);
                blockSensorLabel.setEnabled(e);
                blockSensorComboBox.setEnabled(e);
            }

            //enable/disable text label, memory & block contents text fields
            textLabelTextField.setEnabled(textLabelButton.isSelected());
            textMemoryComboBox.setEnabled(memoryButton.isSelected());
            blockContentsComboBox.setEnabled(blockContentsButton.isSelected());

            //enable/disable signal mast, sensor & signal head text fields
            signalMastComboBox.setEnabled(signalMastButton.isSelected());
            sensorComboBox.setEnabled(sensorButton.isSelected());
            signalHeadComboBox.setEnabled(signalButton.isSelected());

            //changeIconsButton
            e = (sensorButton.isSelected()
                    || signalButton.isSelected()
                    || iconLabelButton.isSelected());
            log.debug("changeIconsButton is {}", e ? "enabled" : "disabled");
            changeIconsButton.setEnabled(e);
        };

        turnoutRHButton.addActionListener(selectionListAction);
        turnoutLHButton.addActionListener(selectionListAction);
        turnoutWYEButton.addActionListener(selectionListAction);
        doubleXoverButton.addActionListener(selectionListAction);
        rhXoverButton.addActionListener(selectionListAction);
        lhXoverButton.addActionListener(selectionListAction);
        levelXingButton.addActionListener(selectionListAction);
        layoutSingleSlipButton.addActionListener(selectionListAction);
        layoutDoubleSlipButton.addActionListener(selectionListAction);
        endBumperButton.addActionListener(selectionListAction);
        anchorButton.addActionListener(selectionListAction);
        edgeButton.addActionListener(selectionListAction);
        trackButton.addActionListener(selectionListAction);
        multiSensorButton.addActionListener(selectionListAction);
        sensorButton.addActionListener(selectionListAction);
        signalButton.addActionListener(selectionListAction);
        signalMastButton.addActionListener(selectionListAction);
        textLabelButton.addActionListener(selectionListAction);
        memoryButton.addActionListener(selectionListAction);
        blockContentsButton.addActionListener(selectionListAction);
        iconLabelButton.addActionListener(selectionListAction);
        shapeButton.addActionListener(selectionListAction);

        //first row of edit tool bar items
        //turnout items
        turnoutRHButton.setSelected(true);
        turnoutRHButton.setToolTipText(Bundle.getMessage("RHToolTip"));
        turnoutLHButton.setToolTipText(Bundle.getMessage("LHToolTip"));
        turnoutWYEButton.setToolTipText(Bundle.getMessage("WYEToolTip"));
        doubleXoverButton.setToolTipText(Bundle.getMessage("DoubleCrossoverToolTip"));
        rhXoverButton.setToolTipText(Bundle.getMessage("RHCrossoverToolTip"));
        lhXoverButton.setToolTipText(Bundle.getMessage("LHCrossoverToolTip"));
        layoutSingleSlipButton.setToolTipText(Bundle.getMessage("SingleSlipToolTip"));
        layoutDoubleSlipButton.setToolTipText(Bundle.getMessage("DoubleSlipToolTip"));

        String turnoutNameString = Bundle.getMessage("Name");
        JLabel turnoutNameLabel = new JLabel(turnoutNameString);
        turnoutNamePanel.add(turnoutNameLabel);

        setupComboBox(turnoutNameComboBox, false, true, false);
        turnoutNameComboBox.setToolTipText(Bundle.getMessage("TurnoutNameToolTip"));
        turnoutNamePanel.add(turnoutNameComboBox);

        // disable turnouts that are already in use
        turnoutNameComboBox.addPopupMenuListener(layoutEditor.newTurnoutComboBoxPopupMenuListener(turnoutNameComboBox));
        // turnoutNameComboBox.setEnabledColor(Color.green.darker().darker());
        // turnoutNameComboBox.setDisabledColor(Color.red);

        setupComboBox(extraTurnoutNameComboBox, false, true, false);
        extraTurnoutNameComboBox.setToolTipText(Bundle.getMessage("SecondTurnoutNameToolTip"));

        extraTurnoutNameComboBox.addPopupMenuListener(layoutEditor.newTurnoutComboBoxPopupMenuListener(extraTurnoutNameComboBox));
        // extraTurnoutNameComboBox.setEnabledColor(Color.green.darker().darker());
        // extraTurnoutNameComboBox.setDisabledColor(Color.red);

        //this is enabled/disabled via selectionListAction above
        JLabel extraTurnoutLabel = new JLabel(Bundle.getMessage("SecondName"));
        extraTurnoutLabel.setEnabled(false);
        extraTurnoutPanel.add(extraTurnoutLabel);
        extraTurnoutPanel.add(extraTurnoutNameComboBox);
        extraTurnoutPanel.setEnabled(false);

        String[] angleStrings = {"-180", "-135", "-90", "-45", "0", "+45", "+90", "+135", "+180"};
        rotationComboBox = new JComboBox<>(angleStrings);
        rotationComboBox.setEditable(true);
        rotationComboBox.setSelectedIndex(4);
        rotationComboBox.setMaximumRowCount(9);
        rotationComboBox.setToolTipText(Bundle.getMessage("RotationToolTip"));

        JLabel rotationLabel = new JLabel(Bundle.getMessage("Rotation"));
        rotationPanel.add(rotationLabel);
        rotationPanel.add(rotationComboBox);

        zoomPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ZoomLabel"))));
        zoomPanel.add(zoomLabel);

        Dimension coordSize = xLabel.getPreferredSize();
        coordSize.width *= 2;
        xLabel.setPreferredSize(coordSize);
        yLabel.setPreferredSize(coordSize);

        locationPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Location"))));
        locationPanel.add(new JLabel("{x:"));
        locationPanel.add(xLabel);
        locationPanel.add(new JLabel(", y:"));
        locationPanel.add(yLabel);
        locationPanel.add(new JLabel("}    "));

        //second row of edit tool bar items
        levelXingButton.setToolTipText(Bundle.getMessage("LevelCrossingToolTip"));
        trackButton.setToolTipText(Bundle.getMessage("TrackSegmentToolTip"));

        //this is enabled/disabled via selectionListAction above
        trackSegmentPropertiesPanel.add(mainlineTrack);

        mainlineTrack.setSelected(false);
        mainlineTrack.setEnabled(false);
        mainlineTrack.setToolTipText(Bundle.getMessage("MainlineCheckBoxTip"));

        trackSegmentPropertiesPanel.add(dashedLine);
        dashedLine.setSelected(false);
        dashedLine.setEnabled(false);
        dashedLine.setToolTipText(Bundle.getMessage("DashedCheckBoxTip"));

        //the blockPanel is enabled/disabled via selectionListAction above
        setupComboBox(blockIDComboBox, false, true, false);
        blockIDComboBox.setToolTipText(Bundle.getMessage("BlockIDToolTip"));

        highlightBlockCheckBox.setToolTipText(Bundle.getMessage("HighlightSelectedBlockToolTip"));
        highlightBlockCheckBox.addActionListener((ActionEvent event) -> {
            layoutEditor.setHighlightSelectedBlock(highlightBlockCheckBox.isSelected());
        });
        highlightBlockCheckBox.setSelected(layoutEditor.getHighlightSelectedBlock());

        //change the block name
        blockIDComboBox.addActionListener((ActionEvent event) -> {
            //use the "Extra" color to highlight the selected block
            if (layoutEditor.highlightSelectedBlockFlag) {
                layoutEditor.highlightBlockInComboBox(blockIDComboBox);
            }
            String newName = blockIDComboBox.getSelectedItemDisplayName();
            if (newName == null) {
                newName = "";
            }
            LayoutBlock b = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(newName);
            if (b != null) {
                //if there is an occupancy sensor assigned already
                String sensorName = b.getOccupancySensorName();

                if (!sensorName.isEmpty()) {
                    //update the block sensor ComboBox
                    blockSensorComboBox.setSelectedItem(b.getOccupancySensor());
                } else {
                    blockSensorComboBox.setSelectedItem(null);
                }
            }
        });

        setupComboBox(blockSensorComboBox, false, true, false);
        blockSensorComboBox.setToolTipText(Bundle.getMessage("OccupancySensorToolTip"));

        //third row of edit tool bar items
        endBumperButton.setToolTipText(Bundle.getMessage("EndBumperToolTip"));
        anchorButton.setToolTipText(Bundle.getMessage("AnchorToolTip"));
        edgeButton.setToolTipText(Bundle.getMessage("EdgeConnectorToolTip"));
        textLabelButton.setToolTipText(Bundle.getMessage("TextLabelToolTip"));

        textLabelTextField.setToolTipText(Bundle.getMessage("TextToolTip"));
        textLabelTextField.setEnabled(false);

        memoryButton.setToolTipText(Bundle.getMessage("MemoryButtonToolTip", Bundle.getMessage("Memory")));

        setupComboBox(textMemoryComboBox, true, false, false);
        textMemoryComboBox.setToolTipText(Bundle.getMessage("MemoryToolTip"));

        blockContentsButton.setToolTipText(Bundle.getMessage("BlockContentsButtonToolTip"));

        setupComboBox(blockContentsComboBox, true, false, false);
        blockContentsComboBox.setToolTipText(Bundle.getMessage("BlockContentsButtonToolTip"));
        blockContentsComboBox.addActionListener((ActionEvent event) -> {
            //use the "Extra" color to highlight the selected block
            if (layoutEditor.highlightSelectedBlockFlag) {
                layoutEditor.highlightBlockInComboBox(blockContentsComboBox);
            }
        });

        //fourth row of edit tool bar items
        //multi sensor...
        multiSensorButton.setToolTipText(Bundle.getMessage("MultiSensorToolTip"));

        //Signal Mast & text
        signalMastButton.setToolTipText(Bundle.getMessage("SignalMastButtonToolTip"));
        setupComboBox(signalMastComboBox, true, false, false);

        //sensor icon & text
        sensorButton.setToolTipText(Bundle.getMessage("SensorButtonToolTip"));

        setupComboBox(sensorComboBox, true, false, false);
        sensorComboBox.setToolTipText(Bundle.getMessage("SensorIconToolTip"));

        sensorIconEditor = new MultiIconEditor(4);
        sensorIconEditor.setIcon(0, Bundle.getMessage("MakeLabel", Bundle.getMessage("SensorStateActive")),
                "resources/icons/smallschematics/tracksegments/circuit-occupied.gif");
        sensorIconEditor.setIcon(1, Bundle.getMessage("MakeLabel", Bundle.getMessage("SensorStateInactive")),
                "resources/icons/smallschematics/tracksegments/circuit-empty.gif");
        sensorIconEditor.setIcon(2, Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanStateInconsistent")),
                "resources/icons/smallschematics/tracksegments/circuit-error.gif");
        sensorIconEditor.setIcon(3, Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanStateUnknown")),
                "resources/icons/smallschematics/tracksegments/circuit-error.gif");
        sensorIconEditor.complete();

        //Signal icon & text
        signalButton.setToolTipText(Bundle.getMessage("SignalButtonToolTip"));

        setupComboBox(signalHeadComboBox, true, false, false);
        signalHeadComboBox.setToolTipText(Bundle.getMessage("SignalIconToolTip"));

        signalIconEditor = new MultiIconEditor(10);
        signalIconEditor.setIcon(0, "Red:", "resources/icons/smallschematics/searchlights/left-red-short.gif");
        signalIconEditor.setIcon(1, "Flash red:", "resources/icons/smallschematics/searchlights/left-flashred-short.gif");
        signalIconEditor.setIcon(2, "Yellow:", "resources/icons/smallschematics/searchlights/left-yellow-short.gif");
        signalIconEditor.setIcon(3,
                "Flash yellow:",
                "resources/icons/smallschematics/searchlights/left-flashyellow-short.gif");
        signalIconEditor.setIcon(4, "Green:", "resources/icons/smallschematics/searchlights/left-green-short.gif");
        signalIconEditor.setIcon(5, "Flash green:",
                "resources/icons/smallschematics/searchlights/left-flashgreen-short.gif");
        signalIconEditor.setIcon(6, "Dark:", "resources/icons/smallschematics/searchlights/left-dark-short.gif");
        signalIconEditor.setIcon(7, "Held:", "resources/icons/smallschematics/searchlights/left-held-short.gif");
        signalIconEditor.setIcon(8,
                "Lunar",
                "resources/icons/smallschematics/searchlights/left-lunar-short-marker.gif");
        signalIconEditor.setIcon(9,
                "Flash Lunar",
                "resources/icons/smallschematics/searchlights/left-flashlunar-short-marker.gif");
        signalIconEditor.complete();

        sensorFrame = new JFrame(Bundle.getMessage("EditSensorIcons"));
        sensorFrame.getContentPane().add(new JLabel(Bundle.getMessage("IconChangeInfo")), BorderLayout.NORTH);
        sensorFrame.getContentPane().add(sensorIconEditor);
        sensorFrame.pack();

        signalFrame = new JFrame(Bundle.getMessage("EditSignalIcons"));
        signalFrame.getContentPane().add(new JLabel(Bundle.getMessage("IconChangeInfo")), BorderLayout.NORTH);
        // no spaces around Label as that breaks html formatting
        signalFrame.getContentPane().add(signalIconEditor);
        signalFrame.pack();
        signalFrame.setVisible(false);

        //icon label
        iconLabelButton.setToolTipText(Bundle.getMessage("IconLabelToolTip"));
        shapeButton.setToolTipText(Bundle.getMessage("LayoutShapeToolTip"));

        //change icons...
        //this is enabled/disabled via selectionListAction above
        changeIconsButton.addActionListener((ActionEvent event) -> {
            if (sensorButton.isSelected()) {
                sensorFrame.setVisible(true);
            } else if (signalButton.isSelected()) {
                signalFrame.setVisible(true);
            } else if (iconLabelButton.isSelected()) {
                iconFrame.setVisible(true);
            } else {
                //explain to the user why nothing happens
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ChangeIconNotApplied"),
                        Bundle.getMessage("ChangeIcons"), JOptionPane.INFORMATION_MESSAGE);
            }
        });

        changeIconsButton.setToolTipText(Bundle.getMessage("ChangeIconToolTip"));
        changeIconsButton.setEnabled(false);

        //??
        iconEditor = new MultiIconEditor(1);
        iconEditor.setIcon(0, "", "resources/icons/smallschematics/tracksegments/block.gif");
        iconEditor.complete();
        iconFrame = new JFrame(Bundle.getMessage("EditIcon"));
        iconFrame.getContentPane().add(iconEditor);
        iconFrame.pack();
    }

    protected void createFloatingEditContent() {
        /*
         * JFrame - floatingEditToolBoxFrame
         *     JScrollPane - floatingEditContent
         *         JPanel - floatingEditPanel
         *             JPanel - floatEditTabsPanel
         *                 JTabbedPane - floatEditTabsPane
         *                     ...
         *             JPanel - floatEditLocationPanel
         *                 ...
         *             JPanel - floatEditActionPanel  (currently disabled)
         *                 ...
         *             JPanel - floatEditHelpPanel
         *                 ...
         */

        FlowLayout floatContentLayout = new FlowLayout(FlowLayout.CENTER, 5, 2); //5 pixel gap between items, 2 vertical gap

        //Contains the block and sensor combo boxes.
        //It is moved to the appropriate detail pane when the tab changes.
        blockPropertiesPanel = new JPanel(floatContentLayout);
        blockPropertiesPanel.add(blockNameLabel);
        blockPropertiesPanel.add(blockIDComboBox);
        blockPropertiesPanel.add(highlightBlockCheckBox);

        JPanel blockSensorPanel = new JPanel();
        blockSensorPanel.add(blockSensorLabel);
        blockSensorPanel.add(blockSensorComboBox);
        blockSensorPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
        blockPropertiesPanel.add(blockSensorPanel);

        //Build the window content
        floatingEditPanel = new JPanel();
        floatingEditPanel.setLayout(new BoxLayout(floatingEditPanel, BoxLayout.Y_AXIS));

        //Begin the tabs structure
        //Tab 0 - Turnouts
        JPanel floatEditTurnout = new JPanel();
        floatEditTurnout.setLayout(new BoxLayout(floatEditTurnout, BoxLayout.Y_AXIS));

        JPanel turnoutGroup1 = new JPanel(floatContentLayout);
        turnoutGroup1.add(turnoutRHButton);
        turnoutGroup1.add(turnoutLHButton);
        turnoutGroup1.add(turnoutWYEButton);
        turnoutGroup1.add(layoutSingleSlipButton);
        turnoutGroup1.add(layoutDoubleSlipButton);
        floatEditTurnout.add(turnoutGroup1);

        JPanel turnoutGroup2 = new JPanel(floatContentLayout);
        turnoutGroup2.add(doubleXoverButton);
        turnoutGroup2.add(rhXoverButton);
        turnoutGroup2.add(lhXoverButton);
        floatEditTurnout.add(turnoutGroup2);

        JPanel turnoutGroup3 = new JPanel(floatContentLayout);
        turnoutGroup3.add(turnoutNamePanel);
        turnoutGroup3.add(extraTurnoutPanel);
        floatEditTurnout.add(turnoutGroup3);

        JPanel turnoutGroup4 = new JPanel(floatContentLayout);
        turnoutGroup4.add(rotationPanel);
        floatEditTurnout.add(turnoutGroup4);

        floatEditTurnout.add(blockPropertiesPanel);
        floatEditTabsPane.addTab(Bundle.getMessage("Turnouts"), null, floatEditTurnout, null);

        //Tab 1 - Track
        JPanel floatEditTrack = new JPanel();
        floatEditTrack.setLayout(new BoxLayout(floatEditTrack, BoxLayout.Y_AXIS));

        JPanel trackGroup1 = new JPanel(floatContentLayout);
        trackGroup1.add(endBumperButton);
        trackGroup1.add(anchorButton);
        trackGroup1.add(edgeButton);
        floatEditTrack.add(trackGroup1);

        JPanel trackGroup2 = new JPanel(floatContentLayout);
        trackGroup2.add(trackButton);
        trackGroup2.add(levelXingButton);
        floatEditTrack.add(trackGroup2);

        JPanel trackGroup3 = new JPanel(floatContentLayout);
        trackGroup3.add(trackSegmentPropertiesPanel);
        floatEditTrack.add(trackGroup3);

        floatEditTabsPane.addTab(Bundle.getMessage("TabTrack"), null, floatEditTrack, null);

        //Tab 2 - Labels
        JPanel floatEditLabel = new JPanel();
        floatEditLabel.setLayout(new BoxLayout(floatEditLabel, BoxLayout.Y_AXIS));

        JPanel labelGroup1 = new JPanel(floatContentLayout);
        labelGroup1.add(textLabelButton);
        labelGroup1.add(textLabelTextField);
        floatEditLabel.add(labelGroup1);

        JPanel labelGroup2 = new JPanel(floatContentLayout);
        labelGroup2.add(memoryButton);
        labelGroup2.add(textMemoryComboBox);
        floatEditLabel.add(labelGroup2);

        JPanel labelGroup3 = new JPanel(floatContentLayout);
        labelGroup3.add(blockContentsButton);
        labelGroup3.add(blockContentsComboBox);
        floatEditLabel.add(labelGroup3);

        floatEditTabsPane.addTab(Bundle.getMessage("TabLabel"), null, floatEditLabel, null);

        //Tab 3 - Icons
        JPanel floatEditIcon = new JPanel();
        floatEditIcon.setLayout(new BoxLayout(floatEditIcon, BoxLayout.Y_AXIS));

        JPanel iconGroup1 = new JPanel(floatContentLayout);
        iconGroup1.add(multiSensorButton);
        iconGroup1.add(changeIconsButton);
        floatEditIcon.add(iconGroup1);

        JPanel iconGroup2 = new JPanel(floatContentLayout);
        iconGroup2.add(sensorButton);
        iconGroup2.add(sensorComboBox);
        floatEditIcon.add(iconGroup2);

        JPanel iconGroup3 = new JPanel(floatContentLayout);
        iconGroup3.add(signalMastButton);
        iconGroup3.add(signalMastComboBox);
        floatEditIcon.add(iconGroup3);

        JPanel iconGroup4 = new JPanel(floatContentLayout);
        iconGroup4.add(signalButton);
        iconGroup4.add(signalHeadComboBox);
        floatEditIcon.add(iconGroup4);

        JPanel iconGroup5 = new JPanel(floatContentLayout);
        iconGroup5.add(iconLabelButton);
        iconGroup5.add(shapeButton);
        floatEditIcon.add(iconGroup5);

        floatEditTabsPane.addTab(Bundle.getMessage("TabIcon"), null, floatEditIcon, null);
        floatEditTabsPanel.add(floatEditTabsPane);
        floatingEditPanel.add(floatEditTabsPanel);

        //End the tabs structure
        //The next 3 groups reside under the tab secton
        JPanel floatEditLocationPanel = new JPanel();
        floatEditLocationPanel.add(zoomPanel);
        floatEditLocationPanel.add(locationPanel);
        floatingEditPanel.add(floatEditLocationPanel);

        floatEditTabsPane.addChangeListener((e) -> {
            //Move the block group between the turnouts and track tabs
            int selIndex = floatEditTabsPane.getSelectedIndex();

            if (selIndex == 0) {
                floatEditTurnout.add(blockPropertiesPanel);
            } else if (selIndex == 1) {
                floatEditTrack.add(blockPropertiesPanel);
            }
        });

        // JPanel floatEditActionPanel = new JPanel();
        // floatEditActionPanel.add(new JLabel("floatEditActionPanel", JLabel.CENTER));
        // floatingEditPanel.add(floatEditActionPanel);
    }

    //initialize logging
    private transient final static Logger log = LoggerFactory.getLogger(LayoutEditorToolBarPanel.class);
}
