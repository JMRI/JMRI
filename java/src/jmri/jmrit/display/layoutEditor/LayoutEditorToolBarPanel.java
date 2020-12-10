package jmri.jmrit.display.layoutEditor;

import static java.awt.event.KeyEvent.KEY_PRESSED;

import static jmri.jmrit.display.layoutEditor.LayoutEditor.setupComboBox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.swing.NamedBeanComboBox;
import jmri.util.MathUtil;

import org.apache.commons.lang3.StringUtils;

/**
 * This is the base class for the horizontal, vertical and floating toolbar
 * panels
 *
 * @author George Warner Copyright: (c) 2017-2019
 */
public class LayoutEditorToolBarPanel extends JPanel {

    final protected LayoutEditor layoutEditor; // initialized in constuctor

    // top row of radio buttons
    protected JLabel turnoutLabel = new JLabel();
    protected JRadioButton turnoutRHButton = new JRadioButton(Bundle.getMessage("RightHandAbbreviation"));
    protected JRadioButton turnoutLHButton = new JRadioButton(Bundle.getMessage("LeftHandAbbreviation"));
    protected JRadioButton turnoutWYEButton = new JRadioButton(Bundle.getMessage("WYEAbbreviation"));
    protected JRadioButton doubleXoverButton = new JRadioButton(Bundle.getMessage("DoubleCrossoverAbbreviation"));
    protected JRadioButton rhXoverButton = new JRadioButton(Bundle.getMessage("RightCrossover")); //key is also used by Control Panel
    // Editor, placed in DisplayBundle
    protected JRadioButton lhXoverButton = new JRadioButton(Bundle.getMessage("LeftCrossover")); //idem
    protected JRadioButton layoutSingleSlipButton = new JRadioButton(Bundle.getMessage("LayoutSingleSlip"));
    protected JRadioButton layoutDoubleSlipButton = new JRadioButton(Bundle.getMessage("LayoutDoubleSlip"));

    // Default flow layout definitions for JPanels
    protected FlowLayout leftRowLayout = new FlowLayout(FlowLayout.LEFT, 5, 0);       //5 pixel gap between items, no vertical gap
    protected FlowLayout centerRowLayout = new FlowLayout(FlowLayout.CENTER, 5, 0);   //5 pixel gap between items, no vertical gap
    protected FlowLayout rightRowLayout = new FlowLayout(FlowLayout.RIGHT, 5, 0);     //5 pixel gap between items, no vertical gap

    // top row of check boxes
    protected NamedBeanComboBox<Turnout> turnoutNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(), null, NamedBean.DisplayOptions.DISPLAYNAME);

    protected JLabel turnoutNameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Name")));
    protected JPanel turnoutNamePanel = new JPanel(leftRowLayout);
    protected JPanel extraTurnoutPanel = new JPanel(leftRowLayout);
    protected NamedBeanComboBox<Turnout> extraTurnoutNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(), null, NamedBean.DisplayOptions.DISPLAYNAME);
    protected JComboBox<String> rotationComboBox = null;
    protected JPanel rotationPanel = new JPanel(leftRowLayout);

    // 2nd row of radio buttons
    protected JLabel trackLabel = new JLabel();
    protected JRadioButton levelXingButton = new JRadioButton(Bundle.getMessage("LevelCrossing"));
    protected JRadioButton trackButton = new JRadioButton(Bundle.getMessage("TrackSegment"));

    // 2nd row of check boxes
    protected JPanel trackSegmentPropertiesPanel = new JPanel(leftRowLayout);
    protected JCheckBox mainlineTrack = new JCheckBox(Bundle.getMessage("MainlineBox"));
    protected JCheckBox dashedLine = new JCheckBox(Bundle.getMessage("Dashed"));

    protected JLabel blockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BlockID")));
    protected NamedBeanComboBox<Block> blockIDComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    protected JCheckBox highlightBlockCheckBox = new JCheckBox(Bundle.getMessage("HighlightSelectedBlockTitle"));

    protected JLabel blockSensorLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BlockSensorName")));
    protected NamedBeanComboBox<Sensor> blockSensorComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    // 3rd row of radio buttons (and any associated text fields)
    protected JRadioButton endBumperButton = new JRadioButton(Bundle.getMessage("EndBumper"));
    protected JRadioButton anchorButton = new JRadioButton(Bundle.getMessage("Anchor"));
    protected JRadioButton edgeButton = new JRadioButton(Bundle.getMessage("EdgeConnector"));

    protected JLabel labelsLabel = new JLabel();
    protected JRadioButton textLabelButton = new JRadioButton(Bundle.getMessage("TextLabel"));
    protected JTextField textLabelTextField = new JTextField(12);

    protected JRadioButton memoryButton = new JRadioButton(Bundle.getMessage("BeanNameMemory"));
    protected NamedBeanComboBox<Memory> textMemoryComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(MemoryManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    protected JRadioButton blockContentsButton = new JRadioButton(Bundle.getMessage("BlockContentsLabel"));
    protected NamedBeanComboBox<Block> blockContentsComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    // 4th row of radio buttons (and any associated text fields)
    protected JRadioButton multiSensorButton = new JRadioButton(Bundle.getMessage("MultiSensor") + "...");

    protected JRadioButton signalMastButton = new JRadioButton(Bundle.getMessage("SignalMastIcon"));
    protected NamedBeanComboBox<SignalMast> signalMastComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalMastManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    protected JRadioButton sensorButton = new JRadioButton(Bundle.getMessage("SensorIcon"));
    protected NamedBeanComboBox<Sensor> sensorComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    protected JRadioButton signalButton = new JRadioButton(Bundle.getMessage("SignalIcon"));
    protected NamedBeanComboBox<SignalHead> signalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);

    protected JRadioButton iconLabelButton = new JRadioButton(Bundle.getMessage("IconLabel"));
    protected JRadioButton shapeButton = new JRadioButton(Bundle.getMessage("LayoutShape"));

    protected JButton changeIconsButton = new JButton(Bundle.getMessage("ChangeIcons") + "...");

    protected MultiIconEditor sensorIconEditor = null;
    protected JFrame sensorFrame = null;

    protected MultiIconEditor signalIconEditor = null;
    protected JFrame signalFrame = null;

    protected MultiIconEditor iconEditor = null;
    protected JFrame iconFrame = null;

    protected MultiSensorIconFrame multiSensorFrame = null;

    protected JPanel zoomPanel = new JPanel();
    protected JLabel zoomLabel = new JLabel("x1");

    protected JPanel locationPanel = new JPanel();
    protected JPopupMenu locationPopupMenu = new JPopupMenu();

    protected JLabel xLabel = new JLabel("00");
    protected JLabel yLabel = new JLabel("00");

    protected JPanel blockPropertiesPanel = null;

    // non-GUI variables
    protected boolean toolBarIsWide = true;
    protected ButtonGroup itemGroup = null;

    /**
     * Constructor for LayoutEditorToolBarPanel.
     * <p>
     * Note an unusual design feature: Since this calls the
     * {@link #setupComponents()} and {@link #layoutComponents()} non-final
     * methods in the constructor, any subclass reimplementing those must
     * provide versions that will work before the subclasses own initializers
     * and constructor is run.
     *
     * @param layoutEditor the layout editor that this is for
     */
    public LayoutEditorToolBarPanel(@Nonnull LayoutEditor layoutEditor) {
        this.layoutEditor = layoutEditor;

        setupComponents();
        layoutComponents();
    }

    protected void setupComponents() {
        // setup group for radio buttons selecting items to add and line style
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

        // This is used to enable/disable property controls depending on which (radio) button is selected
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

            // block properties
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

            // enable/disable text label, memory & block contents text fields
            textLabelTextField.setEnabled(textLabelButton.isSelected());
            textMemoryComboBox.setEnabled(memoryButton.isSelected());
            blockContentsComboBox.setEnabled(blockContentsButton.isSelected());

            // enable/disable signal mast, sensor & signal head text fields
            signalMastComboBox.setEnabled(signalMastButton.isSelected());
            sensorComboBox.setEnabled(sensorButton.isSelected());
            signalHeadComboBox.setEnabled(signalButton.isSelected());

            // changeIconsButton
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

        // first row of edit tool bar items
        // turnout items
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

        // this is enabled/disabled via selectionListAction above
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

        locationPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    locationPopupMenu.show(locationPanel, me.getX(), me.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    locationPopupMenu.show(locationPanel, me.getX(), me.getY());
                }
            }

            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    locationPopupMenu.show(locationPanel, me.getX(), me.getY());
                }
            }
        });

        // second row of edit tool bar items
        levelXingButton.setToolTipText(Bundle.getMessage("LevelCrossingToolTip"));
        trackButton.setToolTipText(Bundle.getMessage("TrackSegmentToolTip"));

        // this is enabled/disabled via selectionListAction above
        trackSegmentPropertiesPanel.add(mainlineTrack);

        mainlineTrack.setSelected(false);
        mainlineTrack.setEnabled(false);
        mainlineTrack.setToolTipText(Bundle.getMessage("MainlineCheckBoxTip"));

        trackSegmentPropertiesPanel.add(dashedLine);
        dashedLine.setSelected(false);
        dashedLine.setEnabled(false);
        dashedLine.setToolTipText(Bundle.getMessage("DashedCheckBoxTip"));

        // the blockPanel is enabled/disabled via selectionListAction above
        setupComboBox(blockIDComboBox, false, true, true);
        blockIDComboBox.setToolTipText(Bundle.getMessage("BlockIDToolTip"));

        highlightBlockCheckBox.setToolTipText(Bundle.getMessage("HighlightSelectedBlockToolTip"));
        highlightBlockCheckBox.addActionListener((ActionEvent event) -> layoutEditor.setHighlightSelectedBlock(highlightBlockCheckBox.isSelected()));
        highlightBlockCheckBox.setSelected(layoutEditor.getHighlightSelectedBlock());

        // change the block name
        blockIDComboBox.addActionListener((ActionEvent event) -> {
            //use the "Extra" color to highlight the selected block
            if (layoutEditor.getHighlightSelectedBlock()) {
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
            } else {
                blockSensorComboBox.setSelectedItem(null);
            }
        });

        setupComboBox(blockSensorComboBox, false, true, false);
        blockSensorComboBox.setToolTipText(Bundle.getMessage("OccupancySensorToolTip"));

        // third row of edit tool bar items
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
            // use the "Extra" color to highlight the selected block
            if (layoutEditor.getHighlightSelectedBlock()) {
                layoutEditor.highlightBlockInComboBox(blockContentsComboBox);
            }
        });

        // fourth row of edit tool bar items
        // multi sensor...
        multiSensorButton.setToolTipText(Bundle.getMessage("MultiSensorToolTip"));

        // Signal Mast & text
        signalMastButton.setToolTipText(Bundle.getMessage("SignalMastButtonToolTip"));
        setupComboBox(signalMastComboBox, true, false, false);

        // sensor icon & text
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

        // Signal icon & text
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

        // icon label
        iconLabelButton.setToolTipText(Bundle.getMessage("IconLabelToolTip"));
        shapeButton.setToolTipText(Bundle.getMessage("LayoutShapeToolTip"));

        // change icons...
        // this is enabled/disabled via selectionListAction above
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

        // ??
        iconEditor = new MultiIconEditor(1);
        iconEditor.setIcon(0, "", "resources/icons/smallschematics/tracksegments/block.gif");
        iconEditor.complete();
        iconFrame = new JFrame(Bundle.getMessage("EditIcon"));
        iconFrame.getContentPane().add(iconEditor);
        iconFrame.pack();
    }

    /*=========================*\
    |* toolbar location format *|
    \*=========================*/
    public enum LocationFormat {
        ePIXELS,
        eMETRIC_CM,
        eENGLISH_FEET_INCHES;

        LocationFormat() {
        }
    }

    private LocationFormat locationFormat = LocationFormat.ePIXELS;

    public LocationFormat getLocationFormat() {
        return locationFormat;
    }

    public void setLocationFormat(LocationFormat locationFormat) {
        if (this.locationFormat != locationFormat) {
            switch (locationFormat) {
                default:
                case ePIXELS: {
                    Dimension coordSize = new JLabel("10000").getPreferredSize();
                    xLabel.setPreferredSize(coordSize);
                    yLabel.setPreferredSize(coordSize);
                    break;
                }
                case eMETRIC_CM: {
                    Dimension coordSize = new JLabel(getMetricCMText(10005)).getPreferredSize();
                    xLabel.setPreferredSize(coordSize);
                    yLabel.setPreferredSize(coordSize);

                    layoutEditor.gContext.setGridSize(10);
                    layoutEditor.gContext.setGridSize2nd(10);
                    break;
                }
                case eENGLISH_FEET_INCHES: {
                    Dimension coordSize = new JLabel(getEnglishFeetInchesText(100008)).getPreferredSize();
                    xLabel.setPreferredSize(coordSize);
                    yLabel.setPreferredSize(coordSize);

                    layoutEditor.gContext.setGridSize(16);
                    layoutEditor.gContext.setGridSize2nd(12);
                    break;
                }
            }
            this.locationFormat = locationFormat;
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
                String windowFrameRef = layoutEditor.getWindowFrameRef();
                prefsMgr.setProperty(windowFrameRef, "LocationFormat", locationFormat.name());
            });
            setLocationText(lastLocation);
        }
    }

    private Point2D lastLocation = MathUtil.zeroPoint2D();

    public void setLocationText(Point2D p) {
        int x = (int) p.getX();
        int y = (int) p.getY();

        // default behaviour is pixels
        String xText = Integer.toString(x);
        String yText = Integer.toString(y);

        if (locationFormat.equals(LocationFormat.eENGLISH_FEET_INCHES)) {
            xText = getEnglishFeetInchesText(x);
            yText = getEnglishFeetInchesText(y);
        } else if (locationFormat.equals(LocationFormat.eMETRIC_CM)) {
            xText = getMetricCMText(x);
            yText = getMetricCMText(y);
        }
        xLabel.setText(xText);
        yLabel.setText(yText);
        lastLocation = p;
    }

    private String getEnglishFeetInchesText(int v) {
        String result = "";

        int denom = 16; // 16 pixels per inch
        int ipf = 12;   // 12 inches per foot

        int feet = v / (ipf * denom);
        int inches = (v / denom) % ipf;

        int numer = v % denom;
        int gcd = MathUtil.gcd(numer, denom);

        numer /= gcd;
        denom /= gcd;

        if (feet > 0) {
            result = String.format("%d'", feet);
        }

        boolean inchesFlag = false;
        if ((v == 0) || (inches > 0)) {
            result += String.format(" %d", inches);
            inchesFlag = true;
        }

        if (numer > 0) {
            result += String.format(" %d/%d", numer, denom);
            inchesFlag = true;
        }
        if (inchesFlag) {
            result += "\"";
        }

        return result;
    }

    private String getMetricCMText(int v) {
        return String.format("%d.%d cm", v / 10, v % 10);
    }

    /**
     * layout the components in this panel
     */
    protected void layoutComponents() {
        log.error("layoutComponents called in LayoutEditorToolBarPanel base class");
    }

    final Map<JRadioButton, String> quickKeyMap = new LinkedHashMap<JRadioButton, String>() {
        {   // NOTE: These are in the order that the space bar will select thru
            put(turnoutRHButton, Bundle.getMessage("TurnoutRH_QuickKeys"));
            put(turnoutLHButton, Bundle.getMessage("TurnoutLH_QuickKeys"));
            put(turnoutWYEButton, Bundle.getMessage("TurnoutWYE_QuickKeys"));
            put(doubleXoverButton, Bundle.getMessage("DoubleXover_QuickKeys"));
            put(rhXoverButton, Bundle.getMessage("RHXover_QuickKeys"));
            put(lhXoverButton, Bundle.getMessage("LHXover_QuickKeys"));
            put(layoutSingleSlipButton, Bundle.getMessage("LayoutSingleSlip_QuickKeys"));
            put(layoutDoubleSlipButton, Bundle.getMessage("LayoutDoubleSlip_QuickKeys"));
            put(levelXingButton, Bundle.getMessage("LevelXing_QuickKeys"));
            put(trackButton, Bundle.getMessage("TrackSegment_QuickKeys"));
            put(endBumperButton, Bundle.getMessage("EndBumper_QuickKeys"));
            put(anchorButton, Bundle.getMessage("Anchor_QuickKeys"));
            put(edgeButton, Bundle.getMessage("Edge_QuickKeys"));
            put(textLabelButton, Bundle.getMessage("TextLabel_QuickKeys"));
            put(memoryButton, Bundle.getMessage("Memory_QuickKeys"));
            put(blockContentsButton, Bundle.getMessage("BlockContents_QuickKeys"));
            put(multiSensorButton, Bundle.getMessage("MultiSensor_QuickKeys"));
            put(sensorButton, Bundle.getMessage("Sensor_QuickKeys"));
            put(signalMastButton, Bundle.getMessage("SignalMast_QuickKeys"));
            put(signalButton, Bundle.getMessage("Signal_QuickKeys"));
            put(iconLabelButton, Bundle.getMessage("IconLabel_QuickKeys"));
            put(shapeButton, Bundle.getMessage("Shape_QuickKeys"));
        }
    };

    public void keyPressed(@Nonnull KeyEvent event) {
        if (layoutEditor.isEditable()) {
            if (!event.isMetaDown() && !event.isAltDown() && !event.isControlDown()) {
                if (event.getID() == KEY_PRESSED) {
                    char keyChar = event.getKeyChar();
                    String keyString = String.valueOf(keyChar);
                    log.trace("KeyEvent.getKeyChar() == {}", KeyEvent.getKeyText(keyChar));

                    // find last radio button
                    JRadioButton lastRadioButton = null;
                    for (Map.Entry<JRadioButton, String> entry : quickKeyMap.entrySet()) {
                        JRadioButton thisRadioButton = entry.getKey();
                        if (thisRadioButton.isSelected()) {
                            lastRadioButton = thisRadioButton;
                            log.trace("lastRadioButton is {}", lastRadioButton.getText());
                            break;
                        }
                    }

                    JRadioButton firstRadioButton = null;   // the first one that matches
                    JRadioButton nextRadioButton = null;    // the next one to select
                    boolean foundLast = false;
                    for (Map.Entry<JRadioButton, String> entry : quickKeyMap.entrySet()) {
                        String quickKeys = entry.getValue();
                        if (keyString.equals(" ") || StringUtils.containsAny(keyString, quickKeys)) {    // found keyString
                            JRadioButton thisRadioButton = entry.getKey();
                            log.trace("Matched keyString to {}", thisRadioButton.getText());
                            if (foundLast) {
                                nextRadioButton = thisRadioButton;
                                break;
                            } else if (lastRadioButton == thisRadioButton) {
                                foundLast = true;
                            } else if (firstRadioButton == null) {
                                firstRadioButton = thisRadioButton;
                            }
                        }
                    }
                    // if we didn't find the next one...
                    if (nextRadioButton == null) {
                        // ...then use the first one
                        nextRadioButton = firstRadioButton;
                    }
                    // if we found one...
                    if (nextRadioButton != null) {
                        // ...then select it
                        nextRadioButton.setSelected(true);
                    }
                }   // if KEY_PRESSED event
            }   // if no modifier keys pressed
        }   // if is in edit mode
    }

    //initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorToolBarPanel.class);
}
