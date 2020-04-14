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
import jmri.*;
import jmri.swing.NamedBeanComboBox;
import org.slf4j.*;

/**
 * This is the base class for the horizontal, vertical and floating toolbar
 * panels
 *
 * @author George Warner Copyright: (c) 2017-2019
 */
@SuppressWarnings("serial")
@SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED") //no Serializable support at present
public class LayoutEditorToolBarPanel extends JPanel {

    protected LayoutEditor layoutEditor = null;

    //top row of radio buttons
    protected transient JLabel turnoutLabel = new JLabel();
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
    protected transient FlowLayout leftRowLayout = new FlowLayout(FlowLayout.LEFT, 5, 0);       //5 pixel gap between items, no vertical gap
    protected transient FlowLayout centerRowLayout = new FlowLayout(FlowLayout.CENTER, 5, 0);   //5 pixel gap between items, no vertical gap
    protected transient FlowLayout rightRowLayout = new FlowLayout(FlowLayout.RIGHT, 5, 0);     //5 pixel gap between items, no vertical gap

    //top row of check boxes
    protected transient NamedBeanComboBox<Turnout> turnoutNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(), null, NamedBean.DisplayOptions.DISPLAYNAME);

    protected transient JLabel turnoutNameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Name")));
    protected transient JPanel turnoutNamePanel = new JPanel(leftRowLayout);
    protected transient JPanel extraTurnoutPanel = new JPanel(leftRowLayout);
    protected transient NamedBeanComboBox<Turnout> extraTurnoutNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(), null, NamedBean.DisplayOptions.DISPLAYNAME);
    protected transient JComboBox<String> rotationComboBox = null;
    protected transient JPanel rotationPanel = new JPanel(leftRowLayout);

    //2nd row of radio buttons
    protected transient JLabel trackLabel = new JLabel();
    protected transient JRadioButton levelXingButton = new JRadioButton(Bundle.getMessage("LevelCrossing"));
    protected transient JRadioButton trackButton = new JRadioButton(Bundle.getMessage("TrackSegment"));

    //2nd row of check boxes
    protected transient JPanel trackSegmentPropertiesPanel = new JPanel(leftRowLayout);
    protected transient JCheckBox mainlineTrack = new JCheckBox(Bundle.getMessage("MainlineBox"));
    protected transient JCheckBox dashedLine = new JCheckBox(Bundle.getMessage("Dashed"));

    protected transient JLabel blockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BlockID")));
    protected transient NamedBeanComboBox<Block> blockIDComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    protected transient JCheckBox highlightBlockCheckBox = new JCheckBox(Bundle.getMessage("HighlightSelectedBlockTitle"));

    protected transient JLabel blockSensorLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BlockSensorName")));
    protected transient NamedBeanComboBox<Sensor> blockSensorComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    //3rd row of radio buttons (and any associated text fields)
    protected transient JRadioButton endBumperButton = new JRadioButton(Bundle.getMessage("EndBumper"));
    protected transient JRadioButton anchorButton = new JRadioButton(Bundle.getMessage("Anchor"));
    protected transient JRadioButton edgeButton = new JRadioButton(Bundle.getMessage("EdgeConnector"));

    protected transient JLabel labelsLabel = new JLabel();
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

    protected transient JButton changeIconsButton = new JButton(Bundle.getMessage("ChangeIcons") + "...");

    protected transient MultiIconEditor sensorIconEditor = null;
    protected transient JFrame sensorFrame = null;

    protected transient MultiIconEditor signalIconEditor = null;
    protected transient JFrame signalFrame = null;

    protected transient MultiIconEditor iconEditor = null;
    protected transient JFrame iconFrame = null;

    protected transient MultiSensorIconFrame multiSensorFrame = null;

    protected transient JLabel xLabel = new JLabel("00");
    protected transient JLabel yLabel = new JLabel("00");

    protected transient JPanel zoomPanel = new JPanel();
    protected transient JLabel zoomLabel = new JLabel("x1");

    protected transient JPanel locationPanel = new JPanel();

    protected transient JPanel blockPropertiesPanel = null;

    //non-GUI variables
    protected transient boolean toolBarIsWide = true;
    protected transient ButtonGroup itemGroup = null;

    /**
     * constructor for LayoutEditorToolBarPanel
     *
     * @param layoutEditor the layout editor that this is for
     */
    public LayoutEditorToolBarPanel(@Nonnull LayoutEditor layoutEditor) {
        this.layoutEditor = layoutEditor;

        setupComponents();

        layoutComponents();
    }   //constructor

    protected void setupComponents() {
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
                blockLabel.setEnabled(e);
                blockIDComboBox.setEnabled(e);
                blockSensorLabel.setEnabled(e);
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
        setupComboBox(blockIDComboBox, false, true, true);
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
            LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(newName);
            if (lb != null) {
                //if there is an occupancy sensor assigned already
                String sensorName = lb.getOccupancySensorName();

                if (!sensorName.isEmpty()) {
                    //update the block sensor ComboBox
                    blockSensorComboBox.setSelectedItem(lb.getOccupancySensor());
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
    }   //setupComponents()

    /**
     * layout the components in this panel
     */
    protected void layoutComponents() {
        log.error("layoutComponents called in LayoutEditorToolBarPanel base class");
    }

    //initialize logging
    private transient final static Logger log = LoggerFactory.getLogger(LayoutEditorToolBarPanel.class);
}
