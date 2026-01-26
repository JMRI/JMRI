package jmri.jmrit.dispatcher;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import jmri.Block;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.Transit;
import jmri.TransitManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.dispatcher.ActiveTrain.TrainDetection;
import jmri.jmrit.dispatcher.ActiveTrain.TrainLengthUnits;
import jmri.jmrit.dispatcher.DispatcherFrame.TrainsFrom;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.jmrit.roster.swing.RosterGroupComboBox;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;
import jmri.util.swing.JComboBoxUtil;
import jmri.util.swing.JmriJOptionPane;

/**
 * Displays the Activate New Train Frame and processes information entered
 * there.
 * <p>
 * This module works with Dispatcher, which initiates the display of this Frame.
 * Dispatcher also creates the ActiveTrain.
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is open source software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Dave Duchamp Copyright (C) 2009
 */
public class ActivateTrainFrame extends JmriJFrame {

    public ActivateTrainFrame(DispatcherFrame d) {
        super(true,true);
        _dispatcher = d;
        _tiFile = new TrainInfoFile();
    }

    // operational instance variables
    private DispatcherFrame _dispatcher = null;
    private TrainInfoFile _tiFile = null;
    private final TransitManager _TransitManager = InstanceManager.getDefault(jmri.TransitManager.class);
    private String _trainInfoName = "";
    UserPreferencesManager upm = InstanceManager.getDefault(UserPreferencesManager.class);
    String upmGroupName = this.getClass().getName() + ".rosterGroupSelector";

    // initiate train window variables
    private Transit selectedTransit = null;
    //private String selectedTrain = "";
    private JmriJFrame initiateFrame = null;
    private Container initiatePane = null;
    private final jmri.swing.NamedBeanComboBox<Transit> transitSelectBox = new jmri.swing.NamedBeanComboBox<>(_TransitManager);
    private final JComboBox<Object> trainSelectBox = new JComboBox<>();
    // private final List<RosterEntry> trainBoxList = new ArrayList<>();
    private RosterEntrySelectorPanel rosterComboBox = null;
    private final JLabel trainFieldLabel = new JLabel(Bundle.getMessage("TrainBoxLabel") + ":");
    private final JTextField trainNameField = new JTextField(10);
    private final JLabel dccAddressFieldLabel = new JLabel("     " + Bundle.getMessage("DccAddressFieldLabel") + ":");
    private final JSpinner dccAddressSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 9999, 1));
    private final JCheckBox inTransitBox = new JCheckBox(Bundle.getMessage("TrainInTransit"));
    private final JComboBox<String> startingBlockBox = new JComboBox<>();
    private final JComboBox<String> viaBlockBox = new JComboBox<>();
    private final JLabel viaBlockBoxLabel = new JLabel(Bundle.getMessage("ViaBlockBoxLabel"));
    private List<Block> startingBlockBoxList = new ArrayList<>();
    private final List<Block> viaBlockBoxList = new ArrayList<>();
    private List<Integer> startingBlockSeqList = new ArrayList<>();
    private final JComboBox<String> destinationBlockBox = new JComboBox<>();

    private List<Block> destinationBlockBoxList = new ArrayList<>();
    private List<Integer> destinationBlockSeqList = new ArrayList<>();
    private JButton addNewTrainButton = null;
    private JButton loadButton = null;
    private JButton saveButton = null;
    private JButton saveAsTemplateButton  = null;
    private JButton deleteButton = null;
    private final JCheckBox autoRunBox = new JCheckBox(Bundle.getMessage("AutoRun"));
    private final JCheckBox loadAtStartupBox = new JCheckBox(Bundle.getMessage("LoadAtStartup"));

    private final JRadioButton radioTrainsFromRoster = new JRadioButton(Bundle.getMessage("TrainsFromRoster"));
    private final JRadioButton radioTrainsFromOps = new JRadioButton(Bundle.getMessage("TrainsFromTrains"));
    private final JRadioButton radioTrainsFromUser = new JRadioButton(Bundle.getMessage("TrainsFromUser"));
    private final JRadioButton radioTrainsFromSetLater = new JRadioButton(Bundle.getMessage("TrainsFromSetLater"));
    private final ButtonGroup trainsFromButtonGroup = new ButtonGroup();

    private final JRadioButton radioTransitsPredefined = new JRadioButton(Bundle.getMessage("TransitsPredefined"));
    private final JRadioButton radioTransitsAdHoc = new JRadioButton(Bundle.getMessage("TransitsAdHoc"));
    private final ButtonGroup transitsFromButtonGroup = new ButtonGroup();
    //private final JCheckBox adHocCloseLoop = new JCheckBox(Bundle.getMessage("TransitCloseLoop"));

    private final JRadioButton allocateBySafeRadioButton = new JRadioButton(Bundle.getMessage("ToSafeSections"));
    private final JRadioButton allocateAllTheWayRadioButton = new JRadioButton(Bundle.getMessage("AsFarAsPos"));
    private final JRadioButton allocateNumberOfBlocks = new JRadioButton(Bundle.getMessage("NumberOfBlocks") + ":");
    private final ButtonGroup allocateMethodButtonGroup = new ButtonGroup();
    private final JSpinner allocateCustomSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
    private final JCheckBox terminateWhenDoneBox = new JCheckBox(Bundle.getMessage("TerminateWhenDone"));
    private final JPanel terminateWhenDoneDetails = new JPanel();
    private final JComboBox<String> nextTrain = new JComboBox<>();
    private final JLabel nextTrainLabel = new JLabel(Bundle.getMessage("TerminateWhenDoneNextTrain"));
    private final JSpinner prioritySpinner = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
    private final JCheckBox resetWhenDoneBox = new JCheckBox(Bundle.getMessage("ResetWhenDone"));
    private final JCheckBox reverseAtEndBox = new JCheckBox(Bundle.getMessage("ReverseAtEnd"));

    int[] delayedStartInt = new int[]{ActiveTrain.NODELAY, ActiveTrain.TIMEDDELAY, ActiveTrain.SENSORDELAY};
    String[] delayedStartString = new String[]{Bundle.getMessage("DelayedStartNone"), Bundle.getMessage("DelayedStartTimed"), Bundle.getMessage("DelayedStartSensor")};

    private final JComboBox<String> reverseDelayedRestartType = new JComboBox<>(delayedStartString);
    private final JLabel delayReverseReStartLabel = new JLabel(Bundle.getMessage("DelayRestart"));
    private final JLabel delayReverseReStartSensorLabel = new JLabel(Bundle.getMessage("RestartSensor"));
    private final JCheckBox delayReverseResetSensorBox = new JCheckBox(Bundle.getMessage("ResetRestartSensor"));
    private final NamedBeanComboBox<Sensor> delayReverseReStartSensor = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance());
    private final JSpinner delayReverseMinSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
    private final JLabel delayReverseMinLabel = new JLabel(Bundle.getMessage("RestartTimed"));

    private final JCheckBox resetStartSensorBox = new JCheckBox(Bundle.getMessage("ResetStartSensor"));
    private final JComboBox<String> delayedStartBox = new JComboBox<>(delayedStartString);
    private final JLabel delayedReStartLabel = new JLabel(Bundle.getMessage("DelayRestart"));
    private final JLabel delayReStartSensorLabel = new JLabel(Bundle.getMessage("RestartSensor"));
    private final JCheckBox resetRestartSensorBox = new JCheckBox(Bundle.getMessage("ResetRestartSensor"));
    private final JComboBox<String> delayedReStartBox = new JComboBox<>(delayedStartString);
    private final NamedBeanComboBox<Sensor> delaySensor = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance());
    private final NamedBeanComboBox<Sensor> delayReStartSensor = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance());

    private final JSpinner departureHrSpinner = new JSpinner(new SpinnerNumberModel(8, 0, 23, 1));
    private final JSpinner departureMinSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
    private final JLabel departureTimeLabel = new JLabel(Bundle.getMessage("DepartureTime"));
    private final JLabel departureSepLabel = new JLabel(":");

    private final JSpinner delayMinSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
    private final JLabel delayMinLabel = new JLabel(Bundle.getMessage("RestartTimed"));

    private final JComboBox<String> trainTypeBox = new JComboBox<>();
    // Note: See also items related to automatically running trains near the end of this module

    boolean transitsFromSpecificBlock = false;

    private TrainInfo trainInfo;

    private final String nameOfTemplateFile="TrainInfoDefaultTemplate.xml";
    // to be added and removed.
    private final ActionListener viaBlockBoxListener = e -> handleViaBlockSelectionChanged();
    // roster entries excluded due to already in use.
    private ArrayList<RosterEntry> excludedRosterEntries;

    /**
     * Open up a new train window for a given roster entry located in a specific
     * block.
     *
     * @param e  the action event triggering the new window
     * @param re the roster entry to open the new window for
     * @param b  the block where the train is located
     */
    public void initiateTrain(ActionEvent e, RosterEntry re, Block b) {
        initiateTrain(e);
        if (trainInfo.getTrainsFrom() == TrainsFrom.TRAINSFROMROSTER && re != null) {
            setRosterEntryBox(rosterComboBox, re.getId());
            //Add in some bits of code as some point to filter down the transits that can be used.
        }
        if (b != null && selectedTransit != null) {
            List<Transit> transitList = _TransitManager.getListUsingBlock(b);
            List<Transit> transitEntryList = _TransitManager.getListEntryBlock(b);
            for (Transit t : transitEntryList) {
                if (!transitList.contains(t)) {
                    transitList.add(t);
                }
            }
            transitsFromSpecificBlock = true;
            initializeFreeTransitsCombo(transitList);
            List<Block> tmpBlkList = new ArrayList<>();
            if (selectedTransit.getEntryBlocksList().contains(b)) {
                tmpBlkList = selectedTransit.getEntryBlocksList();
                inTransitBox.setSelected(false);
            } else if (selectedTransit.containsBlock(b)) {
                tmpBlkList = selectedTransit.getInternalBlocksList();
                inTransitBox.setSelected(true);
            }
            List<Integer> tmpSeqList = selectedTransit.getBlockSeqList();
            for (int i = 0; i < tmpBlkList.size(); i++) {
                if (tmpBlkList.get(i) == b) {
                    setComboBox(startingBlockBox, getBlockName(b) + "-" + tmpSeqList.get(i));
                    break;
                }
            }
        }
    }

    /**
     * Displays a window that allows a new ActiveTrain to be activated.
     * <p>
     * Called by Dispatcher in response to the dispatcher clicking the New Train
     * button.
     *
     * @param e the action event triggering the window display
     */
    protected void initiateTrain(ActionEvent e) {
        // set Dispatcher defaults
        // create window if needed
        // if template exists open it
        try {
            trainInfo = _tiFile.readTrainInfo(nameOfTemplateFile);
            if (trainInfo == null) {
                trainInfo = new TrainInfo();
            }
        } catch (java.io.IOException ioe) {
            log.error("IO Exception when reading train info file", ioe);
            return;
        } catch (org.jdom2.JDOMException jde) {
            log.error("JDOM Exception when reading train info file", jde);
            return;
        }

        if (initiateFrame == null) {
            initiateFrame = this;
            initiateFrame.setTitle(Bundle.getMessage("AddTrainTitle"));
            initiateFrame.addHelpMenu("package.jmri.jmrit.dispatcher.NewTrain", true);
            initiatePane = initiateFrame.getContentPane();
            initiatePane.setLayout(new BoxLayout(initiatePane, BoxLayout.Y_AXIS));

            // add buttons to load and save train information
            JPanel hdr = new JPanel();
            hdr.add(loadButton = new JButton(Bundle.getMessage("LoadButton")));
            loadButton.addActionListener(this::loadTrainInfo);
            loadButton.setToolTipText(Bundle.getMessage("LoadButtonHint"));
            hdr.add(saveButton = new JButton(Bundle.getMessage("SaveButton")));
            saveButton.addActionListener( ev -> saveTrainInfo());
            saveButton.setToolTipText(Bundle.getMessage("SaveButtonHint"));
            hdr.add(saveAsTemplateButton = new JButton(Bundle.getMessage("SaveAsTemplateButton")));
            saveAsTemplateButton.addActionListener( ev -> saveTrainInfoAsTemplate());
            saveAsTemplateButton.setToolTipText(Bundle.getMessage("SaveAsTemplateButtonHint"));
            hdr.add(deleteButton = new JButton(Bundle.getMessage("DeleteButton")));
            deleteButton.addActionListener( ev -> deleteTrainInfo());
            deleteButton.setToolTipText(Bundle.getMessage("DeleteButtonHint"));

            // add items relating to both manually run and automatic trains.

            // Trains From choices.
            JPanel p1 = new JPanel();
            p1.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainsFrom")));
            radioTrainsFromRoster.setActionCommand("TRAINSFROMROSTER");
            trainsFromButtonGroup.add(radioTrainsFromRoster);
            radioTrainsFromOps.setActionCommand("TRAINSFROMOPS");
            trainsFromButtonGroup.add(radioTrainsFromOps);
            radioTrainsFromUser.setActionCommand("TRAINSFROMUSER");
            trainsFromButtonGroup.add(radioTrainsFromUser);
            radioTrainsFromSetLater.setActionCommand("TRAINSFROMSETLATER");
            trainsFromButtonGroup.add(radioTrainsFromSetLater);
            p1.add(radioTrainsFromRoster);
            radioTrainsFromRoster.setToolTipText(Bundle.getMessage("TrainsFromRosterHint"));
            p1.add(radioTrainsFromOps);
            radioTrainsFromOps.setToolTipText(Bundle.getMessage("TrainsFromTrainsHint"));
            p1.add(radioTrainsFromUser);
            radioTrainsFromUser.setToolTipText(Bundle.getMessage("TrainsFromUserHint"));
            p1.add(radioTrainsFromSetLater);
            radioTrainsFromSetLater.setToolTipText(Bundle.getMessage("TrainsFromSetLaterHint"));

            radioTrainsFromOps.addItemListener( e1 -> {
                if (e1.getStateChange() == ItemEvent.SELECTED) {
                    setTrainsFromOptions(TrainsFrom.TRAINSFROMOPS);
                }
            });
            radioTrainsFromRoster.addItemListener( e1 -> {
                if (e1.getStateChange() == ItemEvent.SELECTED) {
                    setTrainsFromOptions(TrainsFrom.TRAINSFROMROSTER);
                }
            });
            radioTrainsFromUser.addItemListener( e1 -> {
                if (e1.getStateChange() == ItemEvent.SELECTED) {
                    setTrainsFromOptions(TrainsFrom.TRAINSFROMUSER);
                }
            });
            radioTrainsFromSetLater.addItemListener( e1 -> {
                if (e1.getStateChange() == ItemEvent.SELECTED) {
                    setTrainsFromOptions(TrainsFrom.TRAINSFROMSETLATER);
                }
            });
            initiatePane.add(p1);

            // Select train
            JPanel p2 = new JPanel();

            // Dispatcher train name
            p2.add(trainFieldLabel);
            p2.add(trainNameField);
            trainNameField.setToolTipText(Bundle.getMessage("TrainFieldHint"));

            // Roster combo box
            rosterComboBox = new RosterEntrySelectorPanel(null,upm.getComboBoxLastSelection(upmGroupName));
            rosterComboBox.getRosterGroupComboBox().addActionListener( e3 -> {
                    String s =((RosterGroupComboBox) e3.getSource()).getSelectedItem();
                    upm.setComboBoxLastSelection(upmGroupName, s);
            });
            initializeFreeRosterEntriesCombo();
            rosterComboBox.getRosterEntryComboBox().addActionListener(this::handleRosterSelectionChanged);
            p2.add(rosterComboBox);

            // Operations combo box
            p2.add(trainSelectBox);
            trainSelectBox.addActionListener( e1 -> handleTrainSelectionChanged());
            trainSelectBox.setToolTipText(Bundle.getMessage("TrainBoxHint"));

            // DCC address selector
            p2.add(dccAddressFieldLabel);
            p2.add(dccAddressSpinner);
            dccAddressSpinner.setToolTipText(Bundle.getMessage("DccAddressFieldHint"));

            initiatePane.add(p2);

            // Select transit type
            JPanel p3 = new JPanel();
            p3.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TransitsFrom")));
            radioTransitsPredefined.setActionCommand("USETRANSITS");
            transitsFromButtonGroup.add(radioTransitsPredefined);
            radioTransitsAdHoc.setActionCommand("USEADHOC");
            transitsFromButtonGroup.add(radioTransitsAdHoc);
            p3.add(radioTransitsPredefined);
            radioTransitsPredefined.setToolTipText(Bundle.getMessage("TransitsPredefinedHint"));
            p3.add(radioTransitsAdHoc);
            radioTransitsAdHoc.setToolTipText(Bundle.getMessage("TransitsAdHocHint"));
            radioTransitsPredefined.addItemListener( e1 -> {
                if (e1.getStateChange() == ItemEvent.SELECTED) {
                    transitSelectBox.setEnabled(true);
                    //adHocCloseLoop.setEnabled(false);
                    inTransitBox.setEnabled(true);
                    handleInTransitClick();
                    viaBlockBox.setVisible(false);
                    viaBlockBoxLabel.setVisible(false);
                }
            });
            radioTransitsAdHoc.addItemListener( e1 -> {
                if (e1.getStateChange() == ItemEvent.SELECTED) {
                    checkAdvancedRouting();
                    transitSelectBox.setEnabled(false);
                    //adHocCloseLoop.setEnabled(true);
                    inTransitBox.setEnabled(false);
                    inTransitBox.setSelected(true);
                    initializeStartingBlockComboDynamic();
                    viaBlockBox.setVisible(true);
                    viaBlockBoxLabel.setVisible(true);
                }
            });

            //p3.add(adHocCloseLoop);
            //adHocCloseLoop.setToolTipText(Bundle.getMessage("TransitCloseLoopHint"));

            p3.add(new JLabel(Bundle.getMessage("TransitBoxLabel") + " :"));
            p3.add(transitSelectBox);
            transitSelectBox.addActionListener(this::handleTransitSelectionChanged);
            transitSelectBox.setToolTipText(Bundle.getMessage("TransitBoxHint"));
            initiatePane.add(p3);

            // Train in transit
            JPanel p4 = new JPanel();
            p4.add(inTransitBox);
            inTransitBox.addActionListener( ev -> handleInTransitClick());
            inTransitBox.setToolTipText(Bundle.getMessage("InTransitBoxHint"));
            initiatePane.add(p4);

            // Starting block, add Via for adhoc transits
            JPanel p5 = new JPanel();
            p5.add(new JLabel(Bundle.getMessage("StartingBlockBoxLabel") + " :"));
            p5.add(startingBlockBox);
            startingBlockBox.setToolTipText(Bundle.getMessage("StartingBlockBoxHint"));
            startingBlockBox.addActionListener( ev -> handleStartingBlockSelectionChanged());
            p5.add(viaBlockBoxLabel);
            p5.add(viaBlockBox);
            viaBlockBox.setToolTipText(Bundle.getMessage("ViaBlockBoxHint"));
            viaBlockBox.addActionListener(viaBlockBoxListener);
            initiatePane.add(p5);

            // Destination block
            JPanel p6 = new JPanel();
            p6.add(new JLabel(Bundle.getMessage("DestinationBlockBoxLabel") + ":"));
            p6.add(destinationBlockBox);
            destinationBlockBox.setToolTipText(Bundle.getMessage("DestinationBlockBoxHint"));
            initiatePane.add(p6);

            // Train detection scope
            JPanel p7 = new JPanel();
            p7.add(trainDetectionLabel);
            initializeTrainDetectionBox();
            p7.add(trainDetectionComboBox);
            trainDetectionComboBox.setToolTipText(Bundle.getMessage("TrainDetectionBoxHint"));
            initiatePane.add(p7);

            // Allocation method
            JPanel p8 = new JPanel();
            p8.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("AllocateMethodLabel")));
            allocateMethodButtonGroup.add(allocateAllTheWayRadioButton);
            allocateMethodButtonGroup.add(allocateBySafeRadioButton);
            allocateMethodButtonGroup.add(allocateNumberOfBlocks);
            p8.add(allocateAllTheWayRadioButton);
            allocateAllTheWayRadioButton.setToolTipText(Bundle.getMessage("AllocateAllTheWayHint"));
            p8.add(allocateBySafeRadioButton);
            allocateBySafeRadioButton.setToolTipText(Bundle.getMessage("AllocateSafeHint"));
            p8.add(allocateNumberOfBlocks);
            allocateNumberOfBlocks.setToolTipText(Bundle.getMessage("AllocateMethodHint"));
            allocateAllTheWayRadioButton.addActionListener( ev -> handleAllocateAllTheWayButtonChanged());
            allocateBySafeRadioButton.addActionListener( ev -> handleAllocateBySafeButtonChanged());
            allocateNumberOfBlocks.addActionListener( ev -> handleAllocateNumberOfBlocksButtonChanged());
            p8.add(allocateCustomSpinner);
            allocateCustomSpinner.setToolTipText(Bundle.getMessage("AllocateMethodHint"));
            initiatePane.add(p8);

            // Restart at end
            JPanel p9 = new JPanel();
            p9.add(resetWhenDoneBox);
            resetWhenDoneBox.addActionListener( ev -> handleResetWhenDoneClick());
            resetWhenDoneBox.setToolTipText(Bundle.getMessage("ResetWhenDoneBoxHint"));
            initiatePane.add(p9);

            // Restart using sensor
            JPanel p9a = new JPanel();
            ((FlowLayout) p9a.getLayout()).setVgap(1);
            p9a.add(delayedReStartLabel);
            p9a.add(delayedReStartBox);
            p9a.add(resetRestartSensorBox);
            resetRestartSensorBox.setToolTipText(Bundle.getMessage("ResetRestartSensorHint"));
            resetRestartSensorBox.setSelected(true);
            delayedReStartBox.addActionListener( ev -> handleResetWhenDoneClick());
            delayedReStartBox.setToolTipText(Bundle.getMessage("DelayedReStartHint"));
            initiatePane.add(p9a);

            // Restart using timer
            JPanel p9b = new JPanel();
            ((FlowLayout) p9b.getLayout()).setVgap(1);
            p9b.add(delayMinLabel);
            p9b.add(delayMinSpinner); // already set to 0
            delayMinSpinner.setToolTipText(Bundle.getMessage("RestartTimedHint"));
            p9b.add(delayReStartSensorLabel);
            p9b.add(delayReStartSensor);
            delayReStartSensor.setAllowNull(true);
            handleResetWhenDoneClick();
            initiatePane.add(p9b);

            initiatePane.add(new JSeparator());

            // Reverse at end
            JPanel p10 = new JPanel();
            p10.add(reverseAtEndBox);
            reverseAtEndBox.setToolTipText(Bundle.getMessage("ReverseAtEndBoxHint"));
            initiatePane.add(p10);
            reverseAtEndBox.addActionListener( ev -> handleReverseAtEndBoxClick());

            // Reverse using sensor
            JPanel pDelayReverseRestartDetails = new JPanel();
            ((FlowLayout) pDelayReverseRestartDetails.getLayout()).setVgap(1);
            pDelayReverseRestartDetails.add(delayReverseReStartLabel);
            pDelayReverseRestartDetails.add(reverseDelayedRestartType);
            pDelayReverseRestartDetails.add(delayReverseResetSensorBox);
            delayReverseResetSensorBox.setToolTipText(Bundle.getMessage("ReverseResetRestartSensorHint"));
            delayReverseResetSensorBox.setSelected(true);
            reverseDelayedRestartType.addActionListener( ev -> handleReverseAtEndBoxClick());
            reverseDelayedRestartType.setToolTipText(Bundle.getMessage("ReverseDelayedReStartHint"));
            initiatePane.add(pDelayReverseRestartDetails);

            // Reverse using timer
            JPanel pDelayReverseRestartDetails2 = new JPanel();
            ((FlowLayout) pDelayReverseRestartDetails2.getLayout()).setVgap(1);
            pDelayReverseRestartDetails2.add(delayReverseMinLabel);
            pDelayReverseRestartDetails2.add(delayReverseMinSpinner); // already set to 0
            delayReverseMinSpinner.setToolTipText(Bundle.getMessage("ReverseRestartTimedHint"));
            pDelayReverseRestartDetails2.add(delayReverseReStartSensorLabel);
            pDelayReverseRestartDetails2.add(delayReverseReStartSensor);
            delayReverseReStartSensor.setAllowNull(true);
            handleReverseAtEndBoxClick();
            initiatePane.add(pDelayReverseRestartDetails2);

            initiatePane.add(new JSeparator());

            // Terminate when done option
            JPanel p11 = new JPanel();
            p11.setLayout(new FlowLayout());
            p11.add(terminateWhenDoneBox);
            terminateWhenDoneBox.addActionListener( ev -> handleTerminateWhenDoneBoxClick());
            initiatePane.add(p11);

            // Optional next train, tied to terminate when done.
            terminateWhenDoneDetails.setLayout(new FlowLayout());
            terminateWhenDoneDetails.add(nextTrainLabel);
            terminateWhenDoneDetails.add(nextTrain);
            nextTrain.setToolTipText(Bundle.getMessage("TerminateWhenDoneNextTrainHint"));
            initiatePane.add(terminateWhenDoneDetails);
            handleTerminateWhenDoneBoxClick();

            initiatePane.add(new JSeparator());

            // Priority and train type.
            JPanel p12 = new JPanel();
            p12.setLayout(new FlowLayout());
            p12.add(new JLabel(Bundle.getMessage("PriorityLabel") + ":"));
            p12.add(prioritySpinner); // already set to 5
            prioritySpinner.setToolTipText(Bundle.getMessage("PriorityHint"));
            p12.add(new JLabel("     "));
            p12.add(new JLabel(Bundle.getMessage("TrainTypeBoxLabel")));
            initializeTrainTypeBox();
            p12.add(trainTypeBox);
            trainTypeBox.setSelectedIndex(1);
            trainTypeBox.setToolTipText(Bundle.getMessage("TrainTypeBoxHint"));
            initiatePane.add(p12);

            // Delayed start option
            JPanel p13 = new JPanel();
            p13.add(new JLabel(Bundle.getMessage("DelayedStart")));
            p13.add(delayedStartBox);
            delayedStartBox.setToolTipText(Bundle.getMessage("DelayedStartHint"));
            delayedStartBox.addActionListener(this::handleDelayStartClick);
            p13.add(departureTimeLabel);
            departureHrSpinner.setEditor(new JSpinner.NumberEditor(departureHrSpinner, "00"));
            p13.add(departureHrSpinner);
            departureHrSpinner.setValue(8);
            departureHrSpinner.setToolTipText(Bundle.getMessage("DepartureTimeHrHint"));
            p13.add(departureSepLabel);
            departureMinSpinner.setEditor(new JSpinner.NumberEditor(departureMinSpinner, "00"));
            p13.add(departureMinSpinner);
            departureMinSpinner.setValue(0);
            departureMinSpinner.setToolTipText(Bundle.getMessage("DepartureTimeMinHint"));
            p13.add(delaySensor);
            delaySensor.setAllowNull(true);
            p13.add(resetStartSensorBox);
            resetStartSensorBox.setToolTipText(Bundle.getMessage("ResetStartSensorHint"));
            resetStartSensorBox.setSelected(true);
            handleDelayStartClick(null);
            initiatePane.add(p13);

            // Load at startup option
            JPanel p14 = new JPanel();
            p14.setLayout(new FlowLayout());
            p14.add(loadAtStartupBox);
            loadAtStartupBox.setToolTipText(Bundle.getMessage("LoadAtStartupBoxHint"));
            loadAtStartupBox.setSelected(false);
            initiatePane.add(p14);

            // Auto run option
            initiatePane.add(new JSeparator());
            JPanel p15 = new JPanel();
            p15.add(autoRunBox);
            autoRunBox.addActionListener( ev -> handleAutoRunClick());
            autoRunBox.setToolTipText(Bundle.getMessage("AutoRunBoxHint"));
            autoRunBox.setSelected(false);
            initiatePane.add(p15);
            initializeAutoRunItems();

            // Footer buttons
            JPanel ftr = new JPanel();
            JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
            ftr.add(cancelButton);
            cancelButton.addActionListener( ev -> cancelInitiateTrain());
            cancelButton.setToolTipText(Bundle.getMessage("CancelButtonHint"));
            ftr.add(addNewTrainButton = new JButton(Bundle.getMessage("ButtonCreate")));
            addNewTrainButton.addActionListener( e1 -> addNewTrain());
            addNewTrainButton.setToolTipText(Bundle.getMessage("AddNewTrainButtonHint"));

            JPanel mainPane = new JPanel(new BorderLayout());
            JScrollPane scrPane = new JScrollPane(initiatePane);
            mainPane.add(hdr, BorderLayout.NORTH);
            mainPane.add(scrPane, BorderLayout.CENTER);
            mainPane.add(ftr, BorderLayout.SOUTH);
            initiateFrame.setContentPane(mainPane);
            switch (trainInfo.getTrainsFrom()) {
                case TRAINSFROMROSTER:
                    radioTrainsFromRoster.setSelected(true);
                    break;
                case TRAINSFROMOPS:
                    radioTrainsFromOps.setSelected(true);
                    break;
                case TRAINSFROMUSER:
                    radioTrainsFromUser.setSelected(true);
                    break;
                case TRAINSFROMSETLATER:
                default:
                    radioTrainsFromSetLater.setSelected(true);
            }

        }
        autoRunBox.setSelected(false);
        loadAtStartupBox.setSelected(false);
        initializeFreeTransitsCombo(new ArrayList<>());
        refreshNextTrainCombo();
        setTrainsFromOptions(trainInfo.getTrainsFrom());
        initiateFrame.pack();
        initiateFrame.setVisible(true);

        trainInfoToDialog(trainInfo);
    }

    private void refreshNextTrainCombo() {
        Object saveEntry = null;
        if (nextTrain.getSelectedIndex() > 0) {
            saveEntry=nextTrain.getSelectedItem();
        }
        nextTrain.removeAllItems();
        nextTrain.addItem(" ");
        for (String file: _tiFile.getTrainInfoFileNames()) {
            nextTrain.addItem(file);
        }
        if (saveEntry != null) {
            nextTrain.setSelectedItem(saveEntry);
        }
    }

    private void setTrainsFromOptions(TrainsFrom transFrom) {
        switch (transFrom) {
            case TRAINSFROMROSTER:
                initializeFreeRosterEntriesCombo();
                rosterComboBox.setVisible(true);
                trainSelectBox.setVisible(false);
                trainFieldLabel.setVisible(true);
                trainNameField.setVisible(true);
                dccAddressFieldLabel.setVisible(false);
                dccAddressSpinner.setVisible(false);
                break;
            case TRAINSFROMOPS:
                initializeFreeTrainsCombo();
                trainSelectBox.setVisible(true);
                rosterComboBox.setVisible(false);
                trainFieldLabel.setVisible(true);
                trainNameField.setVisible(true);
                dccAddressFieldLabel.setVisible(true);
                dccAddressSpinner.setVisible(true);
                setSpeedProfileOptions(trainInfo,false);
                break;
            case TRAINSFROMUSER:
                trainNameField.setText("");
                trainSelectBox.setVisible(false);
                rosterComboBox.setVisible(false);
                trainFieldLabel.setVisible(true);
                trainNameField.setVisible(true);
                dccAddressFieldLabel.setVisible(true);
                dccAddressSpinner.setVisible(true);
                dccAddressSpinner.setEnabled(true);
                setSpeedProfileOptions(trainInfo,false);
                break;
            case TRAINSFROMSETLATER:
            default:
                rosterComboBox.setVisible(false);
                trainSelectBox.setVisible(false);
                trainFieldLabel.setVisible(true);
                trainNameField.setVisible(true);
                dccAddressFieldLabel.setVisible(false);
                dccAddressSpinner.setVisible(false);
                setSpeedProfileOptions(trainInfo, true);
        }
    }
    

    // MPH↔KMH conversion helpers
    private static float mphToKmh(float mph) { return mph * 1.60934f; }
    private static float kmhToMph(float kmh) { return kmh / 1.60934f; }

    // Safe Bundle lookup with fallback; avoids MissingResourceException breaking the UI.
    private static String bundleOrDefault(String key, String fallback) {
        try {
            return Bundle.getMessage(key);
        } catch (Exception ex) {
            return fallback;
        }
    }
    

     // Safe access to current layout scale ratio (prototype/model length ratio)
     private float getScaleRatioSafe() {
         return (_dispatcher.getScale() != null)
                 ? (float) _dispatcher.getScale().getScaleRatio()
                 : 1.0f; // CI-safe default
     }
    
     // Do we have a concrete roster entry with a non-empty speed profile?
     private boolean isConcreteSpeedProfileAvailable() {
         Object sel = rosterComboBox.getRosterEntryComboBox().getSelectedItem();
         if (!(sel instanceof jmri.jmrit.roster.RosterEntry)) return false;
         jmri.jmrit.roster.RosterEntry re = (jmri.jmrit.roster.RosterEntry) sel;
         return re.getSpeedProfile() != null && re.getSpeedProfile().getProfileSize() > 0;
     }
    
     // Convert throttle % -> scale mph (via mm/s from profile)
     private float percentToScaleMph(float pct) {
         Object sel = rosterComboBox.getRosterEntryComboBox().getSelectedItem();
         if (!(sel instanceof jmri.jmrit.roster.RosterEntry)) return cachedScaleMph;
         jmri.jmrit.roster.RosterEntry re = (jmri.jmrit.roster.RosterEntry) sel;
         jmri.jmrit.roster.RosterSpeedProfile sp = re.getSpeedProfile();
         if (sp == null || sp.getProfileSize() < 1) return cachedScaleMph;
    
         float mms = sp.getSpeed(pct, true); // mm/s for this % (forward)
         float scaleRatio = getScaleRatioSafe();
         // mm/s -> m/s -> mph, then × scale ratio (scale speed)
         return (mms / 1000.0f) * 2.236936f * scaleRatio;
     }
    
     // Convert throttle % -> scale km/h
     private float percentToScaleKmh(float pct) {
         Object sel = rosterComboBox.getRosterEntryComboBox().getSelectedItem();
         if (!(sel instanceof jmri.jmrit.roster.RosterEntry)) return cachedScaleMph * 1.60934f;
         jmri.jmrit.roster.RosterEntry re = (jmri.jmrit.roster.RosterEntry) sel;
         jmri.jmrit.roster.RosterSpeedProfile sp = re.getSpeedProfile();
         if (sp == null || sp.getProfileSize() < 1) return cachedScaleMph * 1.60934f;
    
         float mms = sp.getSpeed(pct, true);
         float scaleRatio = getScaleRatioSafe();
         // mm/s -> m/s -> km/h, then × scale ratio
         return (mms / 1000.0f) * 3.6f * scaleRatio;
     }
    
     // Convert target scale speed (mph or km/h) -> throttle % by inverting the profile via bisection
     private float scaleSpeedToPercentFromProfile(float speedValue, boolean isKmh) {
         Object sel = rosterComboBox.getRosterEntryComboBox().getSelectedItem();
         if (!(sel instanceof jmri.jmrit.roster.RosterEntry)) return cachedThrottlePercent;
         jmri.jmrit.roster.RosterEntry re = (jmri.jmrit.roster.RosterEntry) sel;
         jmri.jmrit.roster.RosterSpeedProfile sp = re.getSpeedProfile();
         if (sp == null || sp.getProfileSize() < 1) return cachedThrottlePercent;
    
         float scaleRatio = getScaleRatioSafe();
         // scale mph/kmh -> m/s -> mm/s (model), divide by scale ratio to remove scale
         float mps = isKmh ? (speedValue / 3.6f) : (speedValue / 2.236936f);
         float targetMms = (mps * 1000.0f) / scaleRatio;
    
         // Bisection in [0.0 .. 1.0] on sp.getSpeed(%)
         float lo = 0.0f, hi = 1.0f;
         for (int i = 0; i < 24; i++) {
             float mid = 0.5f * (lo + hi);
             float midMms = sp.getSpeed(mid, true);
             if (midMms < targetMms) lo = mid; else hi = mid;
         }
         float pct = 0.5f * (lo + hi);
         // Clamp to spinner's [%] domain 0.10 .. 1.00 (the UI model)
         if (pct < 0.10f) pct = 0.10f;
         if (pct > 1.00f) pct = 1.00f;
         return pct;
     }
    
     // Keep the sticky caches aligned with user's edits on the numeric spinner
     private void updateMaxSpeedCachesFromSpinner() {
         if (suppressMaxSpeedSpinnerEvents) {
             return;
         }
         float v = ((Number) maxSpeedSpinner.getValue()).floatValue();
         switch (lastMaxSpeedCapMode) {
            case THROTTLE:
                // Clamp percent [0.10 .. 1.00] before caching; editor "# %" multiplies by 100 for display
                if (v < 0.10f) v = 0.10f;
                if (v > 1.00f) v = 1.00f;
                cachedThrottlePercent = v;
                break;
             case SCALE_MPH:  cachedScaleMph       = v;             break;
             case SCALE_KMH:  cachedScaleMph       = kmhToMph(v);   break;
             default: break;
             }
         }


     // Format the min-reliable operating speed label in the user's preferred units.
     // When the Max Speed dropdown is in SCALE_MPH or SCALE_KMH, show "scale mph" or "scale km/h" respectively.
     // Otherwise, fall back to the existing localized profile conversion with units.
     private String formatScaleSpeedWithPreferredUnits(float mms) {
         Object sel = maxSpeedCapModeBox.getSelectedItem();
         MaxSpeedCapMode mode = (sel instanceof MaxSpeedCapModeItem)
                 ? ((MaxSpeedCapModeItem) sel).getValue()
                 : MaxSpeedCapMode.THROTTLE;
    
         // Scale speed = actual speed × scale ratio (time same in model/prototype)
        float scaleRatio = (_dispatcher.getScale() != null)
            ? (float) _dispatcher.getScale().getScaleRatio()
            : 1.0f; // CI-safe default
    
         if (mode == MaxSpeedCapMode.SCALE_MPH) {
             // mm/s → m/s → mph, then × scaleRatio
             float mph = (mms / 1000.0f) * 2.236936f * scaleRatio;
             return String.format(
                     Locale.getDefault(),
                     "%.1f %s",
                     mph,
                     Bundle.getMessage("ScaleMilesPerHourShort")  // e.g., "scale mph"
             );
         } else if (mode == MaxSpeedCapMode.SCALE_KMH) {
             // mm/s → m/s → km/h, then × scaleRatio
             float kmh = (mms / 1000.0f) * 3.6f * scaleRatio;
             return String.format(
                     Locale.getDefault(),
                     "%.1f %s",
                     kmh,
                     Bundle.getMessage("ScaleKilometresPerHourShort")  // e.g., "scale km/h"
             );
         }
    
         // Default: use JMRI's existing localised conversion (includes units)
         return RosterSpeedProfile.convertMMSToScaleSpeedWithUnits(mms);
     }


    // Switch the spinner model & editor format to match the selected cap mode
    private void updateMaxSpeedSpinnerModelForMode(MaxSpeedCapMode mode) {
        switch (mode) {
            default:
            case THROTTLE:
                // 0.10 .. 1.00 (% throttle), step 0.01
                maxSpeedSpinner.setModel(new SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(1.0f), Float.valueOf(0.01f)));
                maxSpeedSpinner.setEditor(new JSpinner.NumberEditor(maxSpeedSpinner, "# %"));
                maxSpeedUnitLabel.setText("%");
                maxSpeedSpinner.setToolTipText(Bundle.getMessage("MaxSpeedHint"));
                break;
            case SCALE_MPH:
                // Typical scale speeds: 1 .. 200 mph, step 0.1
                maxSpeedSpinner.setModel(new SpinnerNumberModel(Float.valueOf(60.0f), Float.valueOf(1.0f), Float.valueOf(200.0f), Float.valueOf(0.1f)));
                maxSpeedSpinner.setEditor(new JSpinner.NumberEditor(maxSpeedSpinner, "0.0"));
                maxSpeedUnitLabel.setText(Bundle.getMessage("ScaleMilesPerHourShort"));
                maxSpeedSpinner.setToolTipText(Bundle.getMessage("MaxSpeedHint")); // reuse hint
                break;
            case SCALE_KMH:
                // Typical scale speeds: 1 .. 320 km/h, step 0.1
                maxSpeedSpinner.setModel(new SpinnerNumberModel(Float.valueOf(100.0f), Float.valueOf(1.0f), Float.valueOf(320.0f), Float.valueOf(0.1f)));
                maxSpeedSpinner.setEditor(new JSpinner.NumberEditor(maxSpeedSpinner, "0.0"));
                maxSpeedUnitLabel.setText(Bundle.getMessage("ScaleMilesPerHourShort"));
                maxSpeedSpinner.setToolTipText(Bundle.getMessage("MaxSpeedHint")); // reuse hint
                break;
        }   
    }

     // Enable/disable speed entries depending on speed-profile availability
     private void updateMaxSpeedCapModeAvailability(boolean speedProfileAvailable) {
         suppressMaxSpeedCapModeEvents = true;
         try {
             // Remember previous selection (if any)
             MaxSpeedCapMode prevMode = null;
             Object previous = maxSpeedCapModeBox.getSelectedItem();
             if (previous instanceof MaxSpeedCapModeItem) {
                 prevMode = ((MaxSpeedCapModeItem) previous).getValue();
             }
    
             // Rebuild the dropdown model to include/exclude the speed options
             maxSpeedCapModeBox.removeAllItems();
             maxSpeedCapModeBox.addItem(
                 new MaxSpeedCapModeItem(Bundle.getMessage("MaxSpeedLabel"), MaxSpeedCapMode.THROTTLE)
             );
            if (speedProfileAvailable) {
                maxSpeedCapModeBox.addItem(new MaxSpeedCapModeItem(
                    Bundle.getMessage("MaxSpeedScaleMph"), MaxSpeedCapMode.SCALE_MPH));
                maxSpeedCapModeBox.addItem(new MaxSpeedCapModeItem(
                    Bundle.getMessage("MaxSpeedScaleKmh"), MaxSpeedCapMode.SCALE_KMH));
            }
    
             // Restore the previous mode if still valid; otherwise default to THROTTLE
             int toSelect = 0; // THROTTLE
             if (speedProfileAvailable && (prevMode == MaxSpeedCapMode.SCALE_MPH || prevMode == MaxSpeedCapMode.SCALE_KMH)) {
                 toSelect = (prevMode == MaxSpeedCapMode.SCALE_MPH) ? 1 : 2;
             }
             maxSpeedCapModeBox.setSelectedIndex(toSelect);
    
             // Ensure spinner model matches the (programmatically) selected mode
             Object cur = maxSpeedCapModeBox.getSelectedItem();
             MaxSpeedCapMode mode = (cur instanceof MaxSpeedCapModeItem)
                     ? ((MaxSpeedCapModeItem) cur).getValue()
                     : MaxSpeedCapMode.THROTTLE;
             updateMaxSpeedSpinnerModelForMode(mode);
             lastMaxSpeedCapMode = mode;  // <— keep the tracker aligned with the programmatic selection
         } finally {
             suppressMaxSpeedCapModeEvents = false;
         }
     }

    private void initializeTrainTypeBox() {
        trainTypeBox.removeAllItems();
        trainTypeBox.addItem("<" + Bundle.getMessage("None").toLowerCase() + ">"); // <none>
        trainTypeBox.addItem(Bundle.getMessage("LOCAL_PASSENGER"));
        trainTypeBox.addItem(Bundle.getMessage("LOCAL_FREIGHT"));
        trainTypeBox.addItem(Bundle.getMessage("THROUGH_PASSENGER"));
        trainTypeBox.addItem(Bundle.getMessage("THROUGH_FREIGHT"));
        trainTypeBox.addItem(Bundle.getMessage("EXPRESS_PASSENGER"));
        trainTypeBox.addItem(Bundle.getMessage("EXPRESS_FREIGHT"));
        trainTypeBox.addItem(Bundle.getMessage("MOW"));
        // NOTE: The above must correspond in order and name to definitions in ActiveTrain.java.
    }

    private void initializeTrainDetectionBox() {
        trainDetectionComboBox.addItem(new TrainDetectionItem(Bundle.getMessage("TrainDetectionWholeTrain"),TrainDetection.TRAINDETECTION_WHOLETRAIN));
        trainDetectionComboBox.addItem(new TrainDetectionItem(Bundle.getMessage("TrainDetectionHeadAndTail"),TrainDetection.TRAINDETECTION_HEADANDTAIL));
        trainDetectionComboBox.addItem(new TrainDetectionItem(Bundle.getMessage("TrainDetectionHeadOnly"),TrainDetection.TRAINDETECTION_HEADONLY));
    }

    private void initializeScaleLengthBox() {
        trainLengthUnitsComboBox.addItem(new TrainLengthUnitsItem(Bundle.getMessage("TrainLengthInScaleFeet"), TrainLengthUnits.TRAINLENGTH_SCALEFEET));
        trainLengthUnitsComboBox.addItem(new TrainLengthUnitsItem(Bundle.getMessage("TrainLengthInScaleMeters"), TrainLengthUnits.TRAINLENGTH_SCALEMETERS));
        trainLengthUnitsComboBox.addItem(new TrainLengthUnitsItem(Bundle.getMessage("TrainLengthInActualInchs"), TrainLengthUnits.TRAINLENGTH_ACTUALINCHS));
        trainLengthUnitsComboBox.addItem(new TrainLengthUnitsItem(Bundle.getMessage("TrainLengthInActualcm"), TrainLengthUnits.TRAINLENGTH_ACTUALCM));
    }

    private void handleTransitSelectionChanged(ActionEvent e) {
        int index = transitSelectBox.getSelectedIndex();
        if (index < 0) {
            return;
        }
        Transit t = transitSelectBox.getSelectedItem();
        if ((t != null) && (t != selectedTransit)) {
            selectedTransit = t;
            initializeStartingBlockCombo();
            initializeDestinationBlockCombo();
            initiateFrame.pack();
        }
    }

    private void handleInTransitClick() {
        if (selectedTransit == null) {
            // No transit yet; avoid NPE and present empty combos.
            startingBlockBox.removeAllItems();
            destinationBlockBox.removeAllItems();
            return;
        }
        if (!inTransitBox.isSelected() && selectedTransit.getEntryBlocksList().isEmpty()) {
            JmriJOptionPane.showMessageDialog(
                initiateFrame,
                Bundle.getMessage("NoEntryBlocks"),
                Bundle.getMessage("MessageTitle"),
                JmriJOptionPane.INFORMATION_MESSAGE
            );
            inTransitBox.setSelected(true);
        }
        initializeStartingBlockCombo();
        initializeDestinationBlockCombo();
        initiateFrame.pack();
    }

    private void handleTrainSelectionChanged() {
        int ix = trainSelectBox.getSelectedIndex();
        if (ix < 1) { // no train selected
            dccAddressSpinner.setEnabled(false);
            return;
        }
        dccAddressSpinner.setEnabled(true);
        int dccAddress;
        try {
            dccAddress = Integer.parseInt((((Train) trainSelectBox.getSelectedItem()).getLeadEngineDccAddress()));
        } catch (NumberFormatException ex) {
            JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error43"),
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        dccAddressSpinner.setValue (dccAddress);
        trainNameField.setText(((Train) trainSelectBox.getSelectedItem()).getName());
    }

    private void handleRosterSelectionChanged(ActionEvent e) {
        RosterEntry r ;
        int ix = rosterComboBox.getRosterEntryComboBox().getSelectedIndex();
        if (ix > 0) { // first item is "Select Loco" string
             r = (RosterEntry) rosterComboBox.getRosterEntryComboBox().getSelectedItem();
            // check to see if speed profile exists and is not empty
            if (r.getSpeedProfile() == null || r.getSpeedProfile().getProfileSize() < 1) {
                // disable profile boxes etc.
                setSpeedProfileOptions(trainInfo,false);
            } else {
                // enable profile boxes
                setSpeedProfileOptions(trainInfo,true);
            }
            maxSpeedSpinner.setValue(r.getMaxSpeedPCT()/100.0f);
            trainNameField.setText(r.titleString());
            if (r.getAttribute("DispatcherTrainType") != null && !r.getAttribute("DispatcherTrainType").equals("")) {
                trainTypeBox.setSelectedItem(r.getAttribute("DispatcherTrainType"));
            }
        } else {
            setSpeedProfileOptions(trainInfo,false);
        }
        handleMinReliableOperatingSpeedUpdate(); // update the min-speed label to reflect current units
    }

    private void handleDelayStartClick(ActionEvent e) {
        departureHrSpinner.setVisible(false);
        departureMinSpinner.setVisible(false);
        departureTimeLabel.setVisible(false);
        departureSepLabel.setVisible(false);
        delaySensor.setVisible(false);
        resetStartSensorBox.setVisible(false);
        if (delayedStartBox.getSelectedItem().equals(Bundle.getMessage("DelayedStartTimed"))) {
            departureHrSpinner.setVisible(true);
            departureMinSpinner.setVisible(true);
            departureTimeLabel.setVisible(true);
            departureSepLabel.setVisible(true);
        } else if (delayedStartBox.getSelectedItem().equals(Bundle.getMessage("DelayedStartSensor"))) {
            delaySensor.setVisible(true);
            resetStartSensorBox.setVisible(true);
        }
        initiateFrame.pack(); // to fit extra hh:mm in window
    }

    private void handleResetWhenDoneClick() {
        delayMinSpinner.setVisible(false);
        delayMinLabel.setVisible(false);
        delayedReStartLabel.setVisible(false);
        delayedReStartBox.setVisible(false);
        delayReStartSensorLabel.setVisible(false);
        delayReStartSensor.setVisible(false);
        resetRestartSensorBox.setVisible(false);
        if (resetWhenDoneBox.isSelected()) {
            delayedReStartLabel.setVisible(true);
            delayedReStartBox.setVisible(true);
            terminateWhenDoneBox.setSelected(false);
            if (delayedReStartBox.getSelectedItem().equals(Bundle.getMessage("DelayedStartTimed"))) {
                delayMinSpinner.setVisible(true);
                delayMinLabel.setVisible(true);
            } else if (delayedReStartBox.getSelectedItem().equals(Bundle.getMessage("DelayedStartSensor"))) {
                delayReStartSensor.setVisible(true);
                delayReStartSensorLabel.setVisible(true);
                resetRestartSensorBox.setVisible(true);
            }
        } else {
            terminateWhenDoneBox.setEnabled(true);
        }
        initiateFrame.pack();
    }

    private void handleTerminateWhenDoneBoxClick() {
        if (terminateWhenDoneBox.isSelected()) {
            refreshNextTrainCombo();
            resetWhenDoneBox.setSelected(false);
            terminateWhenDoneDetails.setVisible(true);
        } else {
            terminateWhenDoneDetails.setVisible(false);
        }
    }

    private void handleReverseAtEndBoxClick() {
        delayReverseMinSpinner.setVisible(false);
        delayReverseMinLabel.setVisible(false);
        delayReverseReStartLabel.setVisible(false);
        reverseDelayedRestartType.setVisible(false);
        delayReverseReStartSensorLabel.setVisible(false);
        delayReverseReStartSensor.setVisible(false);
        delayReverseResetSensorBox.setVisible(false);
        if (reverseAtEndBox.isSelected()) {
            delayReverseReStartLabel.setVisible(true);
            reverseDelayedRestartType.setVisible(true);
            if (reverseDelayedRestartType.getSelectedItem().equals(Bundle.getMessage("DelayedStartTimed"))) {
                delayReverseMinSpinner.setVisible(true);
                delayReverseMinLabel.setVisible(true);
            } else if (reverseDelayedRestartType.getSelectedItem().equals(Bundle.getMessage("DelayedStartSensor"))) {
                delayReverseReStartSensor.setVisible(true);
                delayReStartSensorLabel.setVisible(true);
                delayReverseResetSensorBox.setVisible(true);
            }
        }
        initiateFrame.pack();

        if (resetWhenDoneBox.isSelected()) {
            terminateWhenDoneBox.setSelected(false);
            terminateWhenDoneBox.setEnabled(false);
        } else {
            terminateWhenDoneBox.setEnabled(true);
        }
    }

    private void handleAutoRunClick() {
        showHideAutoRunItems(autoRunBox.isSelected());
        initiateFrame.pack();
    }

    private void handleStartingBlockSelectionChanged() {
        if (radioTransitsAdHoc.isSelected() ) {
            initializeViaBlockDynamicCombo();
            initializeDestinationBlockDynamicCombo();
        } else {
            initializeDestinationBlockCombo();
        }
        initiateFrame.pack();
    }

    private void handleViaBlockSelectionChanged() {
        if (radioTransitsAdHoc.isSelected() ) {
            initializeDestinationBlockDynamicCombo();
        } else {
            initializeDestinationBlockCombo();
        }
        initiateFrame.pack();
    }

    private void handleAllocateAllTheWayButtonChanged() {
        allocateCustomSpinner.setVisible(false);
    }

    private void handleAllocateBySafeButtonChanged() {
        allocateCustomSpinner.setVisible(false);
    }

    private void handleAllocateNumberOfBlocksButtonChanged() {
        allocateCustomSpinner.setVisible(true);
    }

    private void cancelInitiateTrain() {
        _dispatcher.newTrainDone(null);
    }

    /*
     * Handles press of "Add New Train" button.
     * Move data to TrainInfo validating basic information
     * Call dispatcher to start the train from traininfo which
     * completes validation.
     */
    private void addNewTrain() {
        try {
            validateDialog();
            trainInfo = new TrainInfo();
            dialogToTrainInfo(trainInfo);
            if (radioTransitsAdHoc.isSelected()) {
                int ixStart, ixEnd, ixVia;
                ixStart = startingBlockBox.getSelectedIndex();
                ixEnd = destinationBlockBox.getSelectedIndex();
                ixVia = viaBlockBox.getSelectedIndex();
                // search for a transit if ones available.
                Transit tmpTransit = null;
                int routeCount = 9999;
                int startBlockSeq = 0;
                int endBlockSeq = 0;
                log.debug("Start[{}]Via[{}]Dest[{}}]",
                        startingBlockBoxList.get(ixStart).getDisplayName(),
                        viaBlockBoxList.get(ixVia).getDisplayName(),
                        destinationBlockBoxList.get(ixEnd).getDisplayName());
                for (Transit tr : InstanceManager.getDefault(jmri.TransitManager.class)
                        .getListUsingBlock(startingBlockBoxList.get(ixStart))) {
                    if (tr.getState() == Transit.IDLE
                            && tr.containsBlock(startingBlockBoxList.get(ixStart))
                            && tr.containsBlock(viaBlockBoxList.get(ixVia)) &&
                            tr.containsBlock(destinationBlockBoxList.get(ixEnd))) {
                        log.debug("[{}]  contains all blocks", tr.getDisplayName());
                        int ixCountStart = -1, ixCountVia = -1, ixCountDest = -1, ixCount = 0;
                        List<Block> transitBlocks = tr.getInternalBlocksList();
                        List<Integer> transitBlockSeq = tr.getBlockSeqList();
                        for (Block blk : transitBlocks) {
                            log.debug("Checking Block[{}] t[{}] BlockSequ[{}]",
                                    blk.getDisplayName(),
                                    ixCount,
                                    transitBlockSeq.get(ixCount));
                            if (ixCountStart == -1 && blk == startingBlockBoxList.get(ixStart)) {
                                log.trace("ixOne[{}]block[{}]",ixCount,blk.getDisplayName());
                                ixCountStart = ixCount;
                            } else if (ixCountStart != -1 && ixCountVia == -1 && blk == viaBlockBoxList.get(ixVia)) {
                                log.trace("ixTwo[{}]block[{}]",ixCount,blk.getDisplayName());
                                if (ixCount != ixCountStart + 1) {
                                    log.debug("AdHoc {}:via and start not ajacent",tr.getDisplayName());
                                    break;
                                }
                                ixCountVia = ixCount;
                            } else if (ixCountStart != -1 && ixCountVia != -1 && ixCountDest == -1 && blk == destinationBlockBoxList.get(ixEnd)) {
                                ixCountDest = ixCount;
                                log.trace("ixThree[{}]block[{}]",ixCountDest,blk.getDisplayName());
                                break;
                            }
                            ixCount++;
                        }
                        if (ixCountVia == (ixCountStart + 1) && ixCountDest > ixCountStart) {
                            log.debug("Canuse [{}", tr.getDisplayName());
                            Integer routeBlockLength =
                                    transitBlockSeq.get(ixCountDest) - transitBlockSeq.get(ixCountStart);
                            if (routeBlockLength < routeCount) {
                                routeCount = ixCountDest - ixCountStart;
                                tmpTransit = tr;
                                startBlockSeq = transitBlockSeq.get(ixCountStart).intValue();
                                endBlockSeq = transitBlockSeq.get(ixCountDest).intValue();
                            }
                        }
                    }
                }
                if (tmpTransit != null &&
                        (JmriJOptionPane.showConfirmDialog(this, Bundle.getMessage("Question6",tmpTransit.getDisplayName()),
                                "Question",
                                JmriJOptionPane.YES_NO_OPTION,
                                JmriJOptionPane.QUESTION_MESSAGE) == JmriJOptionPane.YES_OPTION)) {
                    // use transit found
                    trainInfo.setDynamicTransit(false);
                    trainInfo.setTransitName(tmpTransit.getDisplayName());
                    trainInfo.setTransitId(tmpTransit.getDisplayName());
                    trainInfo.setStartBlockSeq(startBlockSeq);
                    trainInfo.setStartBlockName(getBlockName(startingBlockBoxList.get(ixStart)) + "-" + startBlockSeq);
                    trainInfo.setDestinationBlockSeq(endBlockSeq);
                    trainInfo.setDestinationBlockName(getBlockName(destinationBlockBoxList.get(ixEnd)) + "-" + endBlockSeq);
                    trainInfoToDialog(trainInfo);
                } else {
                    // use a true ad-hoc
                    List<LayoutBlock> blockList = _dispatcher.getAdHocRoute(startingBlockBoxList.get(ixStart),
                            destinationBlockBoxList.get(ixEnd),
                            viaBlockBoxList.get(ixVia));
                    if (blockList == null) {
                        JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error51"),
                                Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            _dispatcher.loadTrainFromTrainInfoThrowsException(trainInfo,"NONE","");
        } catch (IllegalArgumentException ex) {
            JmriJOptionPane.showMessageDialog(initiateFrame, ex.getMessage(),
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeFreeTransitsCombo(List<Transit> transitList) {
        Set<Transit> excludeTransits = new HashSet<>();
        for (Transit t : _TransitManager.getNamedBeanSet()) {
            if (t.getState() != Transit.IDLE) {
                excludeTransits.add(t);
            }
        }
        transitSelectBox.setExcludedItems(excludeTransits);
        JComboBoxUtil.setupComboBoxMaxRows(transitSelectBox);

        if (transitSelectBox.getItemCount() > 0) {
            transitSelectBox.setSelectedIndex(0);
            selectedTransit = transitSelectBox.getItemAt(0);
        } else {
            selectedTransit = null;
        }
    }

    private void initializeFreeRosterEntriesCombo() {
        excludedRosterEntries = new ArrayList<RosterEntry>();
        // remove used entries
        for (int ix = rosterComboBox.getRosterEntryComboBox().getItemCount() - 1; ix > 1; ix--) {  // remove from back first item is the "select loco" message
            if ( !_dispatcher.isAddressFree( ((RosterEntry)rosterComboBox.getRosterEntryComboBox().getItemAt(ix)).getDccLocoAddress().getNumber() ) ) {
                excludedRosterEntries.add((RosterEntry)rosterComboBox.getRosterEntryComboBox().getItemAt(ix));
            }
        }
        rosterComboBox.getRosterEntryComboBox().setExcludeItems(excludedRosterEntries);
        rosterComboBox.getRosterEntryComboBox().update();
    }

    private void initializeFreeTrainsCombo() {
        Train prevValue = null;
        if (trainSelectBox.getSelectedIndex() > 0) {
            // item zero is a string
            prevValue = (Train)trainSelectBox.getSelectedItem();
        }
        ActionListener[] als = trainSelectBox.getActionListeners();
        for ( ActionListener al: als) {
            trainSelectBox.removeActionListener(al);
        }
        trainSelectBox.removeAllItems();
        trainSelectBox.addItem(Bundle.getMessage("SelectTrain"));
        // initialize free trains from operations
        List<Train> trains = InstanceManager.getDefault(TrainManager.class).getTrainsByNameList();
        if (trains.size() > 0) {
            for (int i = 0; i < trains.size(); i++) {
                Train t = trains.get(i);
                if (t != null) {
                    String tName = t.getName();
                    if (_dispatcher.isTrainFree(tName)) {
                        trainSelectBox.addItem(t);
                    }
                }
            }
        }
        if (prevValue != null) {
            trainSelectBox.setSelectedItem(prevValue);
        }
        for ( ActionListener al: als) {
            trainSelectBox.addActionListener(al);
        }
    }

    /**
     * Sets the labels and inputs for speed profile running
     * @param b True if the roster entry has valid speed profile else false
     */
    private void setSpeedProfileOptions(TrainInfo info,boolean b) {
        useSpeedProfileLabel.setEnabled(b);
        useSpeedProfileCheckBox.setEnabled(b);
        stopBySpeedProfileLabel.setEnabled(b);
        stopBySpeedProfileCheckBox.setEnabled(b);
        stopBySpeedProfileAdjustLabel.setEnabled(b);
        stopBySpeedProfileAdjustSpinner.setEnabled(b);
        minReliableOperatingScaleSpeedLabel.setVisible(b);
        if (!b) {
            useSpeedProfileCheckBox.setSelected(false);
            stopBySpeedProfileCheckBox.setSelected(false);

        }
        updateStopByDistanceEnable();
        // Physics option is available iff speed profile UI is enabled (availability only)
        updateRampPhysicsAvailability(b);
        updateMaxSpeedCapModeAvailability(b);
    }

    // Map between Stop-by-distance units and Max Train Length units.
    // Note: Max Train Length has no millimetres; ACTUAL_MM maps to ACTUALCM.
    private TrainLengthUnits mapStopDistanceUnitsToTrainLengthUnits(StopDistanceUnits units) {
        switch (units) {
            case SCALE_FEET:
                return TrainLengthUnits.TRAINLENGTH_SCALEFEET;
            case SCALE_METERS:
                return TrainLengthUnits.TRAINLENGTH_SCALEMETERS;
            case ACTUAL_INCHES:
                return TrainLengthUnits.TRAINLENGTH_ACTUALINCHS;
            case ACTUAL_CM:
            case ACTUAL_MM:
            default:
                return TrainLengthUnits.TRAINLENGTH_ACTUALCM;
        }
    }

    private StopDistanceUnits mapTrainLengthUnitsToStopDistanceUnits(TrainLengthUnits units) {
        switch (units) {
            case TRAINLENGTH_SCALEFEET:
                return StopDistanceUnits.SCALE_FEET;
            case TRAINLENGTH_SCALEMETERS:
                return StopDistanceUnits.SCALE_METERS;
            case TRAINLENGTH_ACTUALINCHS:
                return StopDistanceUnits.ACTUAL_INCHES;
            case TRAINLENGTH_ACTUALCM:
            default:
                return StopDistanceUnits.ACTUAL_CM;
        }
    }

    // Default Stop-by-distance units follow the current Max Train Length unit selection.
    private StopDistanceUnits getPreferredStopDistanceUnitsFromMaxTrainLengthUnits() {
        Object sel = trainLengthUnitsComboBox.getSelectedItem();
        TrainLengthUnits units = (sel instanceof TrainLengthUnitsItem) ? ((TrainLengthUnitsItem) sel).getValue()
                : TrainLengthUnits.TRAINLENGTH_SCALEMETERS;
        return mapTrainLengthUnitsToStopDistanceUnits(units);
    }

    private void setStopByDistanceUnitsSelection(StopDistanceUnits units) {
        for (int i = 0; i < stopByDistanceUnitsComboBox.getItemCount(); i++) {
            Object o = stopByDistanceUnitsComboBox.getItemAt(i);
            if (o instanceof StopDistanceUnitsItem && ((StopDistanceUnitsItem) o).getValue() == units) {
                stopByDistanceUnitsComboBox.setSelectedIndex(i);
                return;
            }
        }
        if (stopByDistanceUnitsComboBox.getItemCount() > 0) {
            stopByDistanceUnitsComboBox.setSelectedIndex(0);
        }
    }

    private TrainLengthUnits getSelectedMaxTrainLengthUnitsSafe() {
        Object sel = trainLengthUnitsComboBox.getSelectedItem();
        return (sel instanceof TrainLengthUnitsItem) ? ((TrainLengthUnitsItem) sel).getValue()
                : TrainLengthUnits.TRAINLENGTH_SCALEMETERS;
    }

    private StopDistanceUnits getSelectedStopByDistanceUnitsSafe() {
        Object sel = stopByDistanceUnitsComboBox.getSelectedItem();
        return (sel instanceof StopDistanceUnitsItem) ? ((StopDistanceUnitsItem) sel).getValue()
                : StopDistanceUnits.ACTUAL_CM;
    }

    private void handleStopByDistanceUnitsComboSelectionChanged() {
        handleStopByDistanceUnitsChanged();
        if (suppressDistanceAndTrainLengthUnitSync) {
            return;
        }
        TrainLengthUnits target = mapStopDistanceUnitsToTrainLengthUnits(getSelectedStopByDistanceUnitsSafe());
        TrainLengthUnits current = getSelectedMaxTrainLengthUnitsSafe();
        if (target == current) {
            return;
        }
        suppressDistanceAndTrainLengthUnitSync = true;
        try {
            trainLengthUnitsComboBox.setSelectedItemByValue(target);
        } finally {
            suppressDistanceAndTrainLengthUnitSync = false;
        }
    }

    private void handleTrainLengthUnitsComboSelectionChanged() {
        handleTrainLengthUnitsChanged();
        if (suppressDistanceAndTrainLengthUnitSync) {
            return;
        }
        StopDistanceUnits target = mapTrainLengthUnitsToStopDistanceUnits(getSelectedMaxTrainLengthUnitsSafe());
        StopDistanceUnits current = getSelectedStopByDistanceUnitsSafe();
        if (target == current) {
            return;
        }
        suppressDistanceAndTrainLengthUnitSync = true;
        try {
            setStopByDistanceUnitsSelection(target);
        } finally {
            suppressDistanceAndTrainLengthUnitSync = false;
        }
    }

    private void updateStopByDistanceEnable() {
         // Row is relevant only if Stop-by-speed-profile is available & selected
         boolean baseOn = stopBySpeedProfileCheckBox.isEnabled() && stopBySpeedProfileCheckBox.isSelected();
    
         stopByDistanceLabel.setEnabled(baseOn);
         stopByDistanceEnableCheckBox.setEnabled(baseOn);
    
         boolean distanceMode = baseOn && stopByDistanceEnableCheckBox.isSelected();
    
         // Distance controls active only in distanceMode
         stopByDistanceMmSpinner.setEnabled(distanceMode);
         stopByDistanceUnitsComboBox.setEnabled(distanceMode);
         stopByDistanceHead.setEnabled(distanceMode);
         stopByDistanceTail.setEnabled(distanceMode);
    
         // Adjust % spinner is the inverse (active when not in distanceMode, but only if baseOn)
         if (baseOn) {
             stopBySpeedProfileAdjustLabel.setEnabled(!distanceMode);
             stopBySpeedProfileAdjustSpinner.setEnabled(!distanceMode);
         }
     }
   
      // Dynamically adjust spinner precision & format to match selected units.
      // NOTE: This does not convert units; that’s handled by handleStopByDistanceUnitsChanged().
      private void updateStopByDistanceSpinnerModelForUnits(StopDistanceUnits units) {
          // Preserve current display value while we swap models/editors
          float displayValue = ((Number) stopByDistanceMmSpinner.getValue()).floatValue();
    
          float step;
          String pattern;
          switch (units) {
              case ACTUAL_MM:
                  step = 1.0f;          // whole millimetres
                  pattern = "0";
                  break;
              case ACTUAL_CM:
              default:
                  step = 0.1f; // tenths of a centimetre (1 mm)
                  pattern = "0.0";
                  break;
              case ACTUAL_INCHES:
              case SCALE_METERS:
              case SCALE_FEET:
                  step = 0.01f;         // hundredths
                  pattern = "0.00";
                  break;
          }
    
          // Keep wide range; only granularity changes
          stopByDistanceMmSpinner.setModel(
              new SpinnerNumberModel(Float.valueOf(displayValue),
                                     Float.valueOf(0.0f),
                                     Float.valueOf(1000000.0f),
                                     Float.valueOf(step))
          );
          stopByDistanceMmSpinner.setEditor(new JSpinner.NumberEditor(stopByDistanceMmSpinner, pattern));
      }     

        private void handleStopByDistanceUnitsChanged() {
            // Convert current display -> mm
            float currentDisplay = ((Number) stopByDistanceMmSpinner.getValue()).floatValue();
            float mm = convertStopDisplayToMm(currentStopDistanceUnits, currentDisplay);
        
            // Update selected units
            currentStopDistanceUnits = stopByDistanceUnitsComboBox.getSelectedUnits();
        
            // Convert mm -> new display units
            float newDisplay = convertMmToStopDisplay(currentStopDistanceUnits, mm);
        
            // Update precision & format for the new units, then show the new value
            updateStopByDistanceSpinnerModelForUnits(currentStopDistanceUnits);
            stopByDistanceMmSpinner.setValue(Float.valueOf(newDisplay));
        }
    
      /*
       * Convert underlying actual millimetres (mm) to the chosen display units.
       * Uses the same scale concepts as the Max Train Length UI:
       *  - "_dispatcher.getScale().getScaleRatio()" converts actual length to scale length
       *  - inches <-> mm conversions use constants 25.4 and 3.28084 as in the train-length panel
       */
      private float convertMmToStopDisplay(StopDistanceUnits units, float mm) {
          final float scaleRatio = (_dispatcher.getScale() != null) ? (float) _dispatcher.getScale().getScaleRatio() : 1.0f;
          switch (units) {
              case ACTUAL_MM:
                  return mm;
              case ACTUAL_CM:
                  return mm / 10.0f;
              case ACTUAL_INCHES:
                  return mm / 25.4f;
              case SCALE_METERS: {
                  // actual metres to scale metres -> (mm / 1000) * scaleRatio
                  float scaleMeters = (mm / 1000.0f) * scaleRatio;
                  return scaleMeters;
              }
              case SCALE_FEET: {
                  // scale feet = scale metres * 3.28084
                  float scaleFeet = ((mm / 1000.0f) * scaleRatio) * 3.28084f;
                  return scaleFeet;
              }
              default:
                  return mm;
          }
      }
    
      /*
       * Convert a displayed value in the chosen units back to underlying mm (actual).
       */
      private float convertStopDisplayToMm(StopDistanceUnits units, float value) {
          final float scaleRatio = (_dispatcher.getScale() != null) ? (float) _dispatcher.getScale().getScaleRatio() : 1.0f;
          switch (units) {
              case ACTUAL_MM:
                  return value;
              case ACTUAL_CM:
                  return value * 10.0f;
              case ACTUAL_INCHES:
                  return value * 25.4f;
              case SCALE_METERS: {
                  float mm = (value / scaleRatio) * 1000.0f;
                  return mm;
              }
              case SCALE_FEET: {
                  float mm = (value / 3.28084f / scaleRatio) * 1000.0f;
                  return mm;
              }
              default:
                  return value;
          }
      }
     
     private void initializeStartingBlockCombo() {
        String prevValue = (String)startingBlockBox.getSelectedItem();
        startingBlockBox.removeAllItems();
        startingBlockBoxList.clear();
        if (selectedTransit == null) {
            return;
        }
        if (!inTransitBox.isSelected() && selectedTransit.getEntryBlocksList().isEmpty()) {
            inTransitBox.setSelected(true);
        }
        if (inTransitBox.isSelected()) {
            startingBlockBoxList = selectedTransit.getInternalBlocksList();
        } else {
            startingBlockBoxList = selectedTransit.getEntryBlocksList();
        }
        startingBlockSeqList = selectedTransit.getBlockSeqList();
        boolean found = false;
        for (int i = 0; i < startingBlockBoxList.size(); i++) {
            Block b = startingBlockBoxList.get(i);
            int seq = startingBlockSeqList.get(i).intValue();
            startingBlockBox.addItem(getBlockName(b) + "-" + seq);
            if (!found && b.getState() == Block.OCCUPIED) {
                startingBlockBox.setSelectedItem(getBlockName(b) + "-" + seq);
                found = true;
            }
        }
        if (prevValue != null) {
            startingBlockBox.setSelectedItem(prevValue);
        }
        JComboBoxUtil.setupComboBoxMaxRows(startingBlockBox);
    }

    private void initializeDestinationBlockCombo() {
        String prevValue = (String)destinationBlockBox.getSelectedItem();
        destinationBlockBox.removeAllItems();
        destinationBlockBoxList.clear();
        int index = startingBlockBox.getSelectedIndex();
        if (index < 0) {
            return;
        }
        Block startBlock = startingBlockBoxList.get(index);
        destinationBlockBoxList = selectedTransit.getDestinationBlocksList(
                startBlock, inTransitBox.isSelected());
        destinationBlockSeqList = selectedTransit.getDestBlocksSeqList();
        for (int i = 0; i < destinationBlockBoxList.size(); i++) {
            Block b = destinationBlockBoxList.get(i);
            String bName = getBlockName(b);
            if (selectedTransit.getBlockCount(b) > 1) {
                int seq = destinationBlockSeqList.get(i).intValue();
                bName = bName + "-" + seq;
            }
            destinationBlockBox.addItem(bName);
        }
        if (prevValue != null) {
            destinationBlockBox.setSelectedItem(prevValue);
        }
        JComboBoxUtil.setupComboBoxMaxRows(destinationBlockBox);
    }

    private String getBlockName(Block b) {
        if (b != null) {
            return b.getDisplayName();
        }
        return " ";
    }

    protected void showActivateFrame() {
        if (initiateFrame != null) {
            initializeFreeTransitsCombo(new ArrayList<>());
            initiateFrame.setVisible(true);
        } else {
            _dispatcher.newTrainDone(null);
        }
    }

    /**
     * Show the Frame.
     * @param re currently unused.
     */
    public void showActivateFrame(RosterEntry re) {
        showActivateFrame();
    }

    protected void loadTrainInfo(ActionEvent e) {
        List<TrainInfoFileSummary> names = _tiFile.getTrainInfoFileSummaries();
        if (!names.isEmpty()) {
            JTable table = new JTable(){
                @Override
                public Dimension getPreferredScrollableViewportSize() {
                  return new Dimension(super.getPreferredSize().width,
                      super.getPreferredScrollableViewportSize().height);
                }
            };
            DefaultTableModel tm = new DefaultTableModel(
                    new Object[]{
                            Bundle.getMessage("FileNameColumnTitle"),
                            Bundle.getMessage("TrainColumnTitle"),
                            Bundle.getMessage("TransitColumnTitle"),
                            Bundle.getMessage("StartBlockColumnTitle"),
                            Bundle.getMessage("EndBlockColumnTitle"),
                            Bundle.getMessage("DccColumnTitleColumnTitle")
                    }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    //all cells false
                    return false;
                }
            };

            table.setModel(tm);
            for (TrainInfoFileSummary fs: names) {
                tm.addRow(new Object[] {fs.getFileName(),fs.getTrainName(),
                        fs.getTransitName(),fs.getStartBlockName()
                        ,fs.getEndBlockName(),fs.getDccAddress()});
            }
            JPanel jp = new JPanel(new BorderLayout());
            TableColumnModel columnModel = table.getColumnModel();
            table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
            for (int column = 0; column < table.getColumnCount(); column++) {
                int width = 30; // Min width
                for (int row = 0; row < table.getRowCount(); row++) {
                    TableCellRenderer renderer = table.getCellRenderer(row, column);
                    Component comp = table.prepareRenderer(renderer, row, column);
                    width = Math.max(comp.getPreferredSize().width +1 , width);
                }
                if(width > 300)
                    width=300;
                columnModel.getColumn(column).setPreferredWidth(width);
            }
            //jp.setPreferredSize(table.getPreferredSize());
            jp.add(table);
            table.setAutoCreateRowSorter(true);
            JScrollPane sp = new JScrollPane(table,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            int optionSelected = JmriJOptionPane.showOptionDialog(initiateFrame,
                    sp, Bundle.getMessage("LoadTrainTitle"), JmriJOptionPane.OK_CANCEL_OPTION, JmriJOptionPane.PLAIN_MESSAGE,
                    null,null,null);
            if (optionSelected != JmriJOptionPane.OK_OPTION) {
                //Canceled
                return;
            }
            if (table.getSelectedRow() < 0) {
                return;
            }
            String selName = (String)table.getModel().getValueAt( table.getRowSorter().convertRowIndexToModel(table.getSelectedRow()),0);
            if ((selName == null) || (selName.isEmpty())) {
                return;
            }
            //read xml data from selected filename and move it into the new train dialog box
            _trainInfoName = selName;
            try {
                trainInfo = _tiFile.readTrainInfo( selName);
                if (trainInfo != null) {
                    // process the information just read
                    trainInfoToDialog(trainInfo);
                }
            } catch (java.io.IOException ioe) {
                log.error("IO Exception when reading train info file", ioe);
            } catch (org.jdom2.JDOMException jde) {
                log.error("JDOM Exception when reading train info file", jde);
            }
            handleDelayStartClick(null);
            handleReverseAtEndBoxClick();
        }
    }

    private void saveTrainInfo() {
        saveTrainInfo(false);
        refreshNextTrainCombo();
    }

    private void saveTrainInfoAsTemplate() {
        saveTrainInfo(true);
    }

    private void saveTrainInfo(boolean asTemplate) {
        try {
            dialogToTrainInfo(trainInfo);
        } catch (IllegalArgumentException ide) {
            JmriJOptionPane.showMessageDialog(initiateFrame, ide.getMessage(),
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        // get file name
        String fileName;
        if (asTemplate) {
            fileName = normalizeXmlFileName(nameOfTemplateFile);
        } else {
            String eName = JmriJOptionPane.showInputDialog(initiateFrame,
                    Bundle.getMessage("EnterFileName") + " :", _trainInfoName);
            if (eName == null) {  //Cancel pressed
                return;
            }
            if (eName.length() < 1) {
                JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error25"),
                        Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            fileName = normalizeXmlFileName(eName);
            _trainInfoName = fileName;
        }
        // check if train info file name is in use
        String[] names = _tiFile.getTrainInfoFileNames();
        if (names.length > 0) {
            boolean found = false;
            for (int i = 0; i < names.length; i++) {
                if (fileName.equals(names[i])) {
                    found = true;
                }
            }
            if (found) {
                // file by that name is already present
                int selectedValue = JmriJOptionPane.showOptionDialog(initiateFrame,
                        Bundle.getMessage("Question3", fileName),
                        Bundle.getMessage("WarningTitle"), JmriJOptionPane.DEFAULT_OPTION,
                        JmriJOptionPane.QUESTION_MESSAGE, null,
                        new Object[]{Bundle.getMessage("ButtonReplace"),Bundle.getMessage("ButtonNo")},
                        Bundle.getMessage("ButtonNo"));
                if (selectedValue != 0 ) { // array position 0 , replace not selected
                    return;   // return without writing if "No" response
                }
            }
        }
        // write the Train Info file
        try {
            _tiFile.writeTrainInfo(trainInfo, fileName);
        } //catch (org.jdom2.JDOMException jde) {
        // log.error("JDOM exception writing Train Info: "+jde);
        //}
        catch (java.io.IOException ioe) {
            log.error("IO exception writing Train Info", ioe);
        }
    }

    private void deleteTrainInfo() {
        String[] names = _tiFile.getTrainInfoFileNames();
        if (names.length > 0) {
            Object selName = JmriJOptionPane.showInputDialog(initiateFrame,
                    Bundle.getMessage("DeleteTrainChoice"), Bundle.getMessage("DeleteTrainTitle"),
                    JmriJOptionPane.QUESTION_MESSAGE, null, names, names[0]);
            if ((selName == null) || (((String) selName).isEmpty())) {
                return;
            }
            _tiFile.deleteTrainInfoFile((String) selName);
        }
    }

    private void trainInfoToDialog(TrainInfo info) {
        if (!info.getDynamicTransit()) {
            radioTransitsPredefined.setSelected(true);
            if (!info.getTransitName().isEmpty()) {
                try {
                    transitSelectBox.setSelectedItemByName(info.getTransitName());
                } catch (Exception ex) {
                    log.warn("Transit {} from file not in Transit menu", info.getTransitName());
                    JmriJOptionPane.showMessageDialog(initiateFrame,
                            Bundle.getMessage("TransitWarn", info.getTransitName()),
                            null, JmriJOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            radioTransitsAdHoc.setSelected(true);
        }
        switch (info.getTrainsFrom()) {
            case TRAINSFROMROSTER:
                radioTrainsFromRoster.setSelected(true);
                if (!info.getRosterId().isEmpty()) {
                    if (!setRosterEntryBox(rosterComboBox, info.getRosterId())) {
                        log.warn("Roster {} from file not in Roster Combo", info.getRosterId());
                        JmriJOptionPane.showMessageDialog(initiateFrame,
                                Bundle.getMessage("TrainWarn", info.getRosterId()),
                                null, JmriJOptionPane.WARNING_MESSAGE);
                    }
                }
                break;
            case TRAINSFROMOPS:
                radioTrainsFromOps.setSelected(true);
                if (!info.getTrainName().isEmpty()) {
                    if (!setTrainComboBox(trainSelectBox, info.getTrainName())) {
                        log.warn("Train {} from file not in Train Combo", info.getTrainName());
                        JmriJOptionPane.showMessageDialog(initiateFrame,
                                Bundle.getMessage("TrainWarn", info.getTrainName()),
                                null, JmriJOptionPane.WARNING_MESSAGE);
                    }
                }
                break;
            case TRAINSFROMUSER:
                radioTrainsFromUser.setSelected(true);
                dccAddressSpinner.setValue(Integer.valueOf(info.getDccAddress()));
                break;
            case TRAINSFROMSETLATER:
            default:
                radioTrainsFromSetLater.setSelected(true);
        }
        setTrainsFromOptions(info.getTrainsFrom());
        trainNameField.setText(info.getTrainUserName());
        trainDetectionComboBox.setSelectedItemByValue(info.getTrainDetection());
        inTransitBox.setSelected(info.getTrainInTransit());
        if (radioTransitsAdHoc.isSelected()) {
            initializeStartingBlockComboDynamic();
        } else {
            initializeStartingBlockCombo();
        }
        setComboBox(startingBlockBox, info.getStartBlockName());
        if (radioTransitsAdHoc.isSelected()) {
            initializeViaBlockDynamicCombo();
            setComboBox(viaBlockBox, info.getViaBlockName());
        }
        if (radioTransitsAdHoc.isSelected()) {
            initializeDestinationBlockDynamicCombo();
        } else {
            initializeDestinationBlockCombo();
        }
        setComboBox(destinationBlockBox, info.getDestinationBlockName());

        setAllocateMethodButtons(info.getAllocationMethod());
        prioritySpinner.setValue(info.getPriority());
        resetWhenDoneBox.setSelected(info.getResetWhenDone());
        reverseAtEndBox.setSelected(info.getReverseAtEnd());
        setDelayModeBox(info.getDelayedStart(), delayedStartBox);
        //delayedStartBox.setSelected(info.getDelayedStart());
        departureHrSpinner.setValue(info.getDepartureTimeHr());
        departureMinSpinner.setValue(info.getDepartureTimeMin());
        delaySensor.setSelectedItem(info.getDelaySensor());
        resetStartSensorBox.setSelected(info.getResetStartSensor());
        setDelayModeBox(info.getDelayedRestart(), delayedReStartBox);
        delayMinSpinner.setValue(info.getRestartDelayMin());
        delayReStartSensor.setSelectedItem(info.getRestartSensor());
        resetRestartSensorBox.setSelected(info.getResetRestartSensor());

        resetStartSensorBox.setSelected(info.getResetStartSensor());
        setDelayModeBox(info.getReverseDelayedRestart(), reverseDelayedRestartType);
        delayReverseMinSpinner.setValue(info.getReverseRestartDelayMin());
        delayReverseReStartSensor.setSelectedItem(info.getReverseRestartSensor());
        delayReverseResetSensorBox.setSelected(info.getReverseResetRestartSensor());

        terminateWhenDoneBox.setSelected(info.getTerminateWhenDone());
        nextTrain.setSelectedIndex(-1);
        try {
            nextTrain.setSelectedItem(info.getNextTrain());
        } catch (Exception ex){
            nextTrain.setSelectedIndex(-1);
        }
        handleTerminateWhenDoneBoxClick();
        setComboBox(trainTypeBox, info.getTrainType());
        autoRunBox.setSelected(info.getAutoRun());
        loadAtStartupBox.setSelected(info.getLoadAtStartup());
        setAllocateMethodButtons(info.getAllocationMethod());
        autoTrainInfoToDialog(info);
    }

    private boolean validateDialog() throws IllegalArgumentException {
        int index = transitSelectBox.getSelectedIndex();
        if (index < 0) {
            throw new IllegalArgumentException(Bundle.getMessage("Error44"));
        }
        switch (trainsFromButtonGroup.getSelection().getActionCommand()) {
            case "TRAINSFROMROSTER":
                if (rosterComboBox.getRosterEntryComboBox().getSelectedIndex() < 1 ) {
                    throw new IllegalArgumentException(Bundle.getMessage("Error41"));
                }
                break;
            case "TRAINSFROMOPS":
                if (trainSelectBox.getSelectedIndex() < 1) {
                    throw new IllegalArgumentException(Bundle.getMessage("Error42"));
                }
                break;
            case "TRAINSFROMUSER":
                if (trainNameField.getText().isEmpty()) {
                    throw new IllegalArgumentException(Bundle.getMessage("Error22"));
                }
                break;
            case "TRAINSFROMSETLATER":
            default:
        }
        index = startingBlockBox.getSelectedIndex();
        if (index < 0) {
            throw new IllegalArgumentException(Bundle.getMessage("Error13"));
        }
        index = destinationBlockBox.getSelectedIndex();
        if (index < 0) {
            throw new IllegalArgumentException(Bundle.getMessage("Error8"));
        }
        if (radioTransitsAdHoc.isSelected()) {
            index = viaBlockBox.getSelectedIndex();
            if (index < 0) {
                throw new IllegalArgumentException(Bundle.getMessage("Error8"));
            }
        }
        if ((!reverseAtEndBox.isSelected()) && resetWhenDoneBox.isSelected()
                && (!selectedTransit.canBeResetWhenDone())) {
            resetWhenDoneBox.setSelected(false);
            throw new IllegalArgumentException(Bundle.getMessage("NoResetMessage"));
        }
        MaxSpeedCapMode mode = ((MaxSpeedCapModeItem) maxSpeedCapModeBox.getSelectedItem()).getValue();
        if (mode == MaxSpeedCapMode.THROTTLE) {
            int max = Math.round(((Number) maxSpeedSpinner.getValue()).floatValue()*100.0f);
            int min = Math.round(((Number) minReliableOperatingSpeedSpinner.getValue()).floatValue()*100.0f);
            if ((max - min) < 10) {
                throw new IllegalArgumentException(Bundle.getMessage("Error49", maxSpeedSpinner.getValue(), minReliableOperatingSpeedSpinner.getValue()));
            }
        }
        // In speed mode, we skip this percent-gap check; runtime will cap via the profile+scale conversion.
        return true;
    }

    private boolean dialogToTrainInfo(TrainInfo info) {
        int index = transitSelectBox.getSelectedIndex();
        info.setDynamicTransit(radioTransitsAdHoc.isSelected());
        if (!info.getDynamicTransit() && index >= 0 ) {
            info.setTransitName(transitSelectBox.getSelectedItem().getDisplayName());
            info.setTransitId(transitSelectBox.getSelectedItem().getDisplayName());
        }
        switch (trainsFromButtonGroup.getSelection().getActionCommand()) {
            case "TRAINSFROMROSTER":
                if (rosterComboBox.getRosterEntryComboBox().getSelectedItem() instanceof RosterEntry) {
                    info.setRosterId(((RosterEntry) rosterComboBox.getRosterEntryComboBox().getSelectedItem()).getId());
                    info.setDccAddress(((RosterEntry) rosterComboBox.getRosterEntryComboBox().getSelectedItem()).getDccAddress());
                }
                trainInfo.setTrainsFrom(TrainsFrom.TRAINSFROMROSTER);
                setTrainsFromOptions(trainInfo.getTrainsFrom());
                break;
            case "TRAINSFROMOPS":
                if (trainSelectBox.getSelectedIndex() > 0) {
                    info.setTrainName(((Train) trainSelectBox.getSelectedItem()).toString());
                    info.setDccAddress(String.valueOf(dccAddressSpinner.getValue()));
                }
                trainInfo.setTrainsFrom(TrainsFrom.TRAINSFROMOPS);
                setTrainsFromOptions(trainInfo.getTrainsFrom());
                break;
            case "TRAINSFROMUSER":
                trainInfo.setTrainsFrom(TrainsFrom.TRAINSFROMUSER);
                info.setDccAddress(String.valueOf(dccAddressSpinner.getValue()));
                break;
            case "TRAINSFROMSETLATER":
            default:
                trainInfo.setTrainsFrom(TrainsFrom.TRAINSFROMSETLATER);
                info.setTrainName("");
                info.setDccAddress("");
        }
        info.setTrainUserName(trainNameField.getText());
        info.setTrainInTransit(inTransitBox.isSelected());
        info.setStartBlockName((String) startingBlockBox.getSelectedItem());
        index = startingBlockBox.getSelectedIndex();
        info.setStartBlockId(startingBlockBoxList.get(index).getDisplayName());
        if (info.getDynamicTransit()) {
            info.setStartBlockSeq(1);
        } else {
            info.setStartBlockSeq(startingBlockSeqList.get(index).intValue());
        }
        index = destinationBlockBox.getSelectedIndex();
        info.setDestinationBlockId(destinationBlockBoxList.get(index).getDisplayName());
        info.setDestinationBlockName(destinationBlockBoxList.get(index).getDisplayName());
        if (info.getDynamicTransit()) {
            info.setViaBlockName(viaBlockBoxList.get(viaBlockBox.getSelectedIndex()).getDisplayName());
        } else {
            info.setDestinationBlockSeq(destinationBlockSeqList.get(index).intValue());
        }
        info.setPriority((Integer) prioritySpinner.getValue());
        info.setTrainDetection(((TrainDetectionItem)trainDetectionComboBox.getSelectedItem()).value);
        info.setResetWhenDone(resetWhenDoneBox.isSelected());
        info.setReverseAtEnd(reverseAtEndBox.isSelected());
        info.setDelayedStart(delayModeFromBox(delayedStartBox));
        info.setDelaySensorName(delaySensor.getSelectedItemDisplayName());
        info.setResetStartSensor(resetStartSensorBox.isSelected());
        info.setDepartureTimeHr((Integer) departureHrSpinner.getValue());
        info.setDepartureTimeMin((Integer) departureMinSpinner.getValue());
        info.setTrainType((String) trainTypeBox.getSelectedItem());
        info.setAutoRun(autoRunBox.isSelected());
        info.setLoadAtStartup(loadAtStartupBox.isSelected());
        info.setAllocateAllTheWay(false); // force to false next field is now used.
        if (allocateAllTheWayRadioButton.isSelected()) {
            info.setAllocationMethod(ActiveTrain.ALLOCATE_AS_FAR_AS_IT_CAN);
        } else if (allocateBySafeRadioButton.isSelected()) {
            info.setAllocationMethod(ActiveTrain.ALLOCATE_BY_SAFE_SECTIONS);
        } else {
            info.setAllocationMethod((Integer) allocateCustomSpinner.getValue());
        }
        info.setDelayedRestart(delayModeFromBox(delayedReStartBox));
        info.setRestartSensorName(delayReStartSensor.getSelectedItemDisplayName());
        info.setResetRestartSensor(resetRestartSensorBox.isSelected());
        info.setRestartDelayMin((Integer) delayMinSpinner.getValue());

        info.setReverseDelayedRestart(delayModeFromBox(reverseDelayedRestartType));
        info.setReverseRestartSensorName(delayReverseReStartSensor.getSelectedItemDisplayName());
        info.setReverseResetRestartSensor(delayReverseResetSensorBox.isSelected());
        info.setReverseRestartDelayMin((Integer) delayReverseMinSpinner.getValue());

        info.setTerminateWhenDone(terminateWhenDoneBox.isSelected());
        if (nextTrain.getSelectedIndex() > 0 ) {
            info.setNextTrain((String)nextTrain.getSelectedItem());
        } else {
            info.setNextTrain("None");
        }
        autoRunItemsToTrainInfo(info);
        return true;
    }

    private boolean setRosterEntryBox(RosterEntrySelectorPanel box, String txt) {
        /*
         * Due to the different behaviour of GUI comboboxs
         * we cannot just set the item and catch an exception.
         * We first inspect the combo items with the current filter,
         * if found well and good else we remove the filter and try again.
         */
        boolean found = false;
        setRosterComboBox(box.getRosterEntryComboBox(),txt);
        if (found) {
            return found;
        }
        box.setSelectedRosterGroup(null);
       return setRosterComboBox(box.getRosterEntryComboBox(),txt);
    }

    private boolean setRosterComboBox(RosterEntryComboBox box, String txt) {
        boolean found = false;
        for (int i = 1; i < box.getItemCount(); i++) {
            if (txt.equals(((RosterEntry) box.getItemAt(i)).getId())) {
                box.setSelectedIndex(i);
                found = true;
                break;
            }
        }
        if (!found && box.getItemCount() > 0) {
            box.setSelectedIndex(0);
        }
        return found;
    }

    // Normalizes a suggested xml file name.  Returns null string if a valid name cannot be assembled
    private String normalizeXmlFileName(String name) {
        if (name.length() < 1) {
            return "";
        }
        String newName = name;
        // strip off .xml or .XML if present
        if ((name.endsWith(".xml")) || (name.endsWith(".XML"))) {
            newName = name.substring(0, name.length() - 4);
            if (newName.length() < 1) {
                return "";
            }
        }
        // replace all non-alphanumeric characters with underscore
        newName = newName.replaceAll("[\\W]", "_");
        return (newName + ".xml");
    }

    private boolean setTrainComboBox(JComboBox<Object> box, String txt) {
        boolean found = false;
        for (int i = 1; i < box.getItemCount(); i++) { //skip the select train item
            if (txt.equals(box.getItemAt(i).toString())) {
                box.setSelectedIndex(i);
                found = true;
                break;
            }
        }
        if (!found && box.getItemCount() > 0) {
            box.setSelectedIndex(0);
        }
        return found;
    }

    private boolean setComboBox(JComboBox<String> box, String txt) {
        boolean found = false;
        for (int i = 0; i < box.getItemCount(); i++) {
            if (txt.equals(box.getItemAt(i))) {
                box.setSelectedIndex(i);
                found = true;
                break;
            }
        }
        if (!found && box.getItemCount() > 0) {
            box.setSelectedIndex(0);
        }
        return found;
    }

    int delayModeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, delayedStartInt, delayedStartString);

        if (result < 0) {
            log.warn("unexpected mode string in turnoutMode: {}", mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    void setDelayModeBox(int mode, JComboBox<String> box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, delayedStartInt, delayedStartString);
        box.setSelectedItem(result);
    }

    /**
     * The following are for items that are only for automatic running of
     * ActiveTrains They are isolated here to simplify changing them in the
     * future.
     * <ul>
     * <li>initializeAutoRunItems - initializes the display of auto run items in
     * this window
     * <li>initializeAutoRunValues - initializes the values of auto run items
     * from values in a saved train info file hideAutoRunItems - hides all auto
     * run items in this window showAutoRunItems - shows all auto run items in
     * this window
     * <li>autoTrainInfoToDialog - gets auto run items from a train info, puts
     * values in items, and initializes auto run dialog items
     * <li>autoTrainItemsToTrainInfo - copies values of auto run items to train
     * info for saving to a file
     * <li>readAutoRunItems - reads and checks values of all auto run items.
     * returns true if OK, sends appropriate messages and returns false if not
     * OK
     * <li>setAutoRunItems - sets the user entered auto run items in the new
     * AutoActiveTrain
     * </ul>
     */
    // auto run items in ActivateTrainFrame
    private final JPanel pa1 = new JPanel();
    private final JLabel speedFactorLabel = new JLabel(Bundle.getMessage("SpeedFactorLabel"));
    private final JSpinner speedFactorSpinner = new JSpinner();
    private final JLabel minReliableOperatingSpeedLabel = new JLabel(Bundle.getMessage("MinReliableOperatingSpeedLabel"));
    private final JSpinner minReliableOperatingSpeedSpinner = new JSpinner();
    private final JLabel minReliableOperatingScaleSpeedLabel = new JLabel();
    private final JSpinner maxSpeedSpinner = new JSpinner();
    private final JComboBox<MaxSpeedCapModeItem> maxSpeedCapModeBox = new JComboBox<>();
    private final JLabel maxSpeedUnitLabel = new JLabel("%"); // changes to "mph" or "km/h" when speed mode selected
    // Suppress mode-change events while programmatically rebuilding the dropdown
    private boolean suppressMaxSpeedCapModeEvents = false;
    private boolean suppressMaxSpeedSpinnerEvents = false;
    // Remember last user-visible mode so we can convert values on mode switches
    private MaxSpeedCapMode lastMaxSpeedCapMode = MaxSpeedCapMode.THROTTLE;
    private float cachedThrottlePercent = 1.0f; // spinner shows 0.10..1.00; we cache user's last % (0.0..1.0)
    private float cachedScaleMph       = 100.0f; // default "sensible" scale speed (mph)
    private final JPanel pa2 = new JPanel();
    private final JLabel rampRateLabel = new JLabel(Bundle.getMessage("RampRateBoxLabel"));
    private final JComboBox<String> rampRateBox = new JComboBox<>();
    private final JPanel pa2a = new JPanel();
    private final JLabel useSpeedProfileLabel = new JLabel(Bundle.getMessage("UseSpeedProfileLabel"));
    private final JCheckBox useSpeedProfileCheckBox = new JCheckBox( );
    private final JLabel stopBySpeedProfileLabel = new JLabel(Bundle.getMessage("StopBySpeedProfileLabel"));
    private final JCheckBox stopBySpeedProfileCheckBox = new JCheckBox( );
    private final JLabel stopBySpeedProfileAdjustLabel = new JLabel(Bundle.getMessage("StopBySpeedProfileAdjustLabel"));
    // Explicit override: ignore hardware Stop Sensors in Sections (default OFF = use sensors)
    private final JCheckBox overrideStopSensorCheckBox = new JCheckBox(Bundle.getMessage("OverrideStopSensorLabel"));
    private final JSpinner stopBySpeedProfileAdjustSpinner = new JSpinner();
    private final JPanel pa2b = new JPanel();
    private final JLabel stopByDistanceLabel = new JLabel(Bundle.getMessage("StopByDistanceLabel"));
    private final JSpinner stopByDistanceMmSpinner = new JSpinner();
    private final JRadioButton stopByDistanceHead = new JRadioButton(Bundle.getMessage("StopByDistanceHead"));
    private final JRadioButton stopByDistanceTail = new JRadioButton(Bundle.getMessage("StopByDistanceTail"));
    private final ButtonGroup stopByDistanceRefGroup = new ButtonGroup();
    private final JCheckBox stopByDistanceEnableCheckBox = new JCheckBox();

    private enum StopDistanceUnits {
        ACTUAL_CM,
        ACTUAL_MM,
        ACTUAL_INCHES,
        SCALE_METERS,
        SCALE_FEET
    }
    
    protected static class StopDistanceUnitsItem {
        private final String key;
        private final StopDistanceUnits value;
        public StopDistanceUnitsItem(String text, StopDistanceUnits units) { this.key = text; this.value = units; }
        @Override public String toString() { return key; }
        public StopDistanceUnits getValue() { return value; }
    }
    
    protected static class StopDistanceUnitsJCombo extends JComboBox<StopDistanceUnitsItem> {
        public StopDistanceUnits getSelectedUnits() {
            // getSelectedItem() is Object in Swing; use a narrow cast or index->getItemAt(i)
            StopDistanceUnitsItem it = (StopDistanceUnitsItem) getSelectedItem();
            return it != null ? it.getValue() : StopDistanceUnits.ACTUAL_CM;

        }
    }    
    
     // Helper: is "Physics" ramp selected?
    private boolean isPhysicsRampSelected() {
        // rampRateBox contains display strings from Bundle.getMessage(...)
        String sel = (String) rampRateBox.getSelectedItem();
        return sel != null && sel.equals(Bundle.getMessage("RAMP_PHYSICS"));
    }
  
     // Add/remove "Physics" in the ramp rate box depending on speed-profile being enabled & selected
     // Preserve the previous selection if it still exists after the rebuild.
     private void updateRampPhysicsAvailability(boolean speedProfileOn) {
         // Snapshot current selection text (display label)
         String prev = (String) rampRateBox.getSelectedItem();
    
         // Collect current items, excluding any existing "Physics" to avoid duplicates
         java.util.List<String> toKeep = new java.util.ArrayList<>();
         for (int i = 0; i < rampRateBox.getItemCount(); i++) {
             String it = rampRateBox.getItemAt(i);
             if (!Bundle.getMessage("RAMP_PHYSICS").equals(it)) {
                 toKeep.add(it);
             }
         }
    
         // Rebuild the list
         rampRateBox.removeAllItems();
         for (String it : toKeep) {
             rampRateBox.addItem(it);
         }
         if (speedProfileOn) {
             rampRateBox.addItem(Bundle.getMessage("RAMP_PHYSICS"));
         }
    
         // Try to restore previous selection if it still exists
         boolean restored = false;
         if (prev != null) {
             for (int i = 0; i < rampRateBox.getItemCount(); i++) {
                 if (prev.equals(rampRateBox.getItemAt(i))) {
                     rampRateBox.setSelectedIndex(i);
                     restored = true;
                     break;
                 }
             }
         }
    
         // If Physics was selected but is no longer available, fall back to SPEEDPROFILE (if present) or first item
         if (!restored && prev != null && Bundle.getMessage("RAMP_PHYSICS").equals(prev) && !speedProfileOn) {
             boolean set = false;
             for (int i = 0; i < rampRateBox.getItemCount(); i++) {
                 String candidate = rampRateBox.getItemAt(i);
                 if (Bundle.getMessage("RAMP_SPEEDPROFILE").equals(candidate)) {
                     rampRateBox.setSelectedIndex(i);
                     set = true;
                     break;
                 }
             }
             if (!set && rampRateBox.getItemCount() > 0) {
                 rampRateBox.setSelectedIndex(0);
             }
         }
    
         // Physics panel visibility follows current selection
         pa2Physics.setVisible(isPhysicsRampSelected());
     }
    
     // Selection changed -> toggle physics panel
     private void handleRampRateSelectionChanged() {
         pa2Physics.setVisible(isPhysicsRampSelected());
     }
  
    private final StopDistanceUnitsJCombo stopByDistanceUnitsComboBox = new StopDistanceUnitsJCombo();

     // --- Physics "Additional train weight" UI + units ---
     // Units: Metric tonnes (t), Long tons (imperial), Short tons (US)
    
     // Top-level enum: referenced across several methods
     private enum AdditionalWeightUnits { METRIC_TONNES, LONG_TONS, SHORT_TONS }
    
     protected static class AdditionalWeightUnitsItem {
         private final String key;
         private final AdditionalWeightUnits value;
         public AdditionalWeightUnitsItem(String text, AdditionalWeightUnits units) {
             this.key = text;
             this.value = units;
         }
         @Override public String toString() { return key; }
         public AdditionalWeightUnits getValue() { return value; }
     }
    
     protected static class AdditionalWeightUnitsJCombo extends JComboBox<AdditionalWeightUnitsItem> {
         public AdditionalWeightUnits getSelectedUnits() {
             AdditionalWeightUnitsItem it = (AdditionalWeightUnitsItem) getSelectedItem();
             return it != null ? it.getValue() : AdditionalWeightUnits.METRIC_TONNES;
         }
     }

     // Panel + controls
     private final JPanel pa2Physics = new JPanel();
     private final JLabel additionalWeightLabel = new JLabel(Bundle.getMessage("AdditionalTrainWeightLabel"));
     private final JSpinner additionalWeightSpinner = new JSpinner();
     private final AdditionalWeightUnitsJCombo additionalWeightUnitsComboBox = new AdditionalWeightUnitsJCombo();
     
     // Rolling resistance coefficient (dimensionless)
     private final JLabel rollingResistanceCoeffLabel = new JLabel(Bundle.getMessage("RollingResistanceCoeffLabel"));
     private final JSpinner rollingResistanceCoeffSpinner = new JSpinner();
     
    // Driver power (% of full power/regulator) used during acceleration when Physics ramp is selected
    private final JLabel driverPowerPercentLabel = new JLabel(Bundle.getMessage("DriverPowerPercentLabel"));
    private final JSpinner driverPowerPercentSpinner = new JSpinner();
        
     // Track current display units for conversion
     private AdditionalWeightUnits currentAdditionalWeightUnits = AdditionalWeightUnits.METRIC_TONNES;
    
     // Unit conversions: all values stored to TrainInfo as metric tonnes
     private static float convertTonnesToDisplay(AdditionalWeightUnits units, float tonnes) {
         switch (units) {
             case METRIC_TONNES: return tonnes;            // t
             case LONG_TONS:     return tonnes / 1.0160469f; // 1 long ton ≈ 1.0160469 t
             case SHORT_TONS:    return tonnes / 0.9071847f; // 1 short ton ≈ 0.9071847 t
             default:            return tonnes;
         }
     }
     private static float convertDisplayToTonnes(AdditionalWeightUnits units, float value) {
         switch (units) {
             case METRIC_TONNES: return value;            // t
             case LONG_TONS:     return value * 1.0160469f;
             case SHORT_TONS:    return value * 0.9071847f;
             default:            return value;
         }
     }

    // Track the “current UI units” so we can convert correctly when user changes the dropdown
    private StopDistanceUnits currentStopDistanceUnits = StopDistanceUnits.ACTUAL_CM;
    // Prevent recursion when synchronising Stop-by-distance units with Max Train Length units.
    private boolean suppressDistanceAndTrainLengthUnitSync = false;

    private final JPanel pa3 = new JPanel();
    private final JCheckBox soundDecoderBox = new JCheckBox(Bundle.getMessage("SoundDecoder"));
    private final JCheckBox runInReverseBox = new JCheckBox(Bundle.getMessage("RunInReverse"));
    private final JPanel pa4 = new JPanel();
    private final JLabel fNumberBellLabel = new JLabel(Bundle.getMessage("fnumberbelllabel"));
    private final JSpinner fNumberBellSpinner = new JSpinner();
    private final JLabel fNumberHornLabel = new JLabel(Bundle.getMessage("fnumberhornlabel"));
    private final JSpinner fNumberHornSpinner = new JSpinner();
    private final JLabel fNumberLightLabel = new JLabel(Bundle.getMessage("fnumberlightlabel"));
    private final JSpinner fNumberLightSpinner = new JSpinner();
    private final JPanel pa5_FNumbers = new JPanel();
    protected static class TrainDetectionJCombo extends JComboBox<TrainDetectionItem> {
        public void setSelectedItemByValue(TrainDetection trainDetVar) {
            for ( int ix = 0; ix < getItemCount() ; ix ++ ) {
                if (getItemAt(ix).value == trainDetVar) {
                    this.setSelectedIndex(ix);
                    break;
                }
            }
        }
    }

    private final JLabel trainDetectionLabel = new JLabel(Bundle.getMessage("TrainDetection"));
    public final TrainDetectionJCombo trainDetectionComboBox = new TrainDetectionJCombo();

    protected static class TrainLengthUnitsJCombo extends JComboBox<TrainLengthUnitsItem> {
        public void setSelectedItemByValue(TrainLengthUnits var) {
            for ( int ix = 0; ix < getItemCount() ; ix ++ ) {
                if (getItemAt(ix).value == var) {
                    this.setSelectedIndex(ix);
                    break;
                }
            }
        }
    }

    /* ComboBox item for speed-cap mode. */
    protected enum MaxSpeedCapMode { THROTTLE, SCALE_MPH, SCALE_KMH }

    protected static class MaxSpeedCapModeItem {
        private final String key;
        private final MaxSpeedCapMode value;
        public MaxSpeedCapModeItem(String text, MaxSpeedCapMode mode) { this.key = text; this.value = mode; }
        @Override public String toString() { return key; }
        public MaxSpeedCapMode getValue() { return value; }
    }

    public final TrainLengthUnitsJCombo trainLengthUnitsComboBox = new TrainLengthUnitsJCombo();
    private final JLabel trainLengthLabel = new JLabel(Bundle.getMessage("MaxTrainLengthLabel"));
    private JLabel trainLengthAltLengthLabel; // I18N Label
    private final JSpinner maxTrainLengthSpinner = new JSpinner(); // initialized later
    // Track current units displayed in the spinner and suppress conversions during programmatic updates
    private TrainLengthUnits currentTrainLengthUnits = TrainLengthUnits.TRAINLENGTH_SCALEMETERS;
    private boolean suppressTrainLengthUnitsEvents = false;

    private void initializeAutoRunItems() {
        initializeRampCombo();
        initializeScaleLengthBox();
        pa1.setLayout(new FlowLayout());
        pa1.add(speedFactorLabel);
        speedFactorSpinner.setModel(new SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(2.0f), Float.valueOf(0.01f)));
        speedFactorSpinner.setEditor(new JSpinner.NumberEditor(speedFactorSpinner, "# %"));
        pa1.add(speedFactorSpinner);
        speedFactorSpinner.setToolTipText(Bundle.getMessage("SpeedFactorHint"));
        pa1.add(new JLabel("   "));
     // Mode dropdown
     maxSpeedCapModeBox.addItem(
         new MaxSpeedCapModeItem(Bundle.getMessage("MaxSpeedLabel"), MaxSpeedCapMode.THROTTLE)
     ); // default; speed entries added later when roster profile is available
     pa1.add(maxSpeedCapModeBox);

     // Initial spinner/editor state: throttle % mode
     updateMaxSpeedSpinnerModelForMode(MaxSpeedCapMode.THROTTLE);

     // Spinner + unit label (unit changes with dropdown)
     pa1.add(maxSpeedSpinner);
     pa1.add(maxSpeedUnitLabel);
     maxSpeedSpinner.addChangeListener(e -> updateMaxSpeedCachesFromSpinner());

      // --- Max Speed mode change: % <-> mph/km/h (with profile-aware conversions and sticky fallbacks) ---
      maxSpeedCapModeBox.addActionListener(new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(java.awt.event.ActionEvent ev) {
    
              // Ignore programmatic changes while rebuilding the combo model.
              if (suppressMaxSpeedCapModeEvents) {
                  return;
              }
    
              Object sel = maxSpeedCapModeBox.getSelectedItem();
              if (!(sel instanceof MaxSpeedCapModeItem)) {
                  // Transient state while the model is being rebuilt.
                  return;
              }
    
              // 1) Capture current spinner value and the previous/new modes.
              float prevDisplay = ((Number) maxSpeedSpinner.getValue()).floatValue();
              MaxSpeedCapMode prevMode = lastMaxSpeedCapMode;
              MaxSpeedCapMode mode     = ((MaxSpeedCapModeItem) sel).getValue();
    
              // Keep our sticky caches aligned with what the user just had visible.
              switch (prevMode) {
                case THROTTLE:
                    // Clamp percent [0.10 .. 1.00] before caching
                    if (prevDisplay < 0.10f) prevDisplay = 0.10f;
                    if (prevDisplay > 1.00f) prevDisplay = 1.00f;
                    cachedThrottlePercent = prevDisplay;
                    break;
                  case SCALE_MPH:
                      cachedScaleMph = prevDisplay;
                      break;
                  case SCALE_KMH:
                      cachedScaleMph = kmhToMph(prevDisplay);
                      break;
                  default:
                      break;
              }
    
              // 2) Compute the new display value for the target mode.
              float newDisplay = prevDisplay;
    
              // mph <-> km/h always converts the number, then clamps to the new spinner model.
              if (prevMode == MaxSpeedCapMode.SCALE_KMH && mode == MaxSpeedCapMode.SCALE_MPH) {
                  newDisplay = kmhToMph(prevDisplay);
                  newDisplay = Math.max(1.0f, Math.min(200.0f, newDisplay)); // clamp to MPH model 1..200
    
              } else if (prevMode == MaxSpeedCapMode.SCALE_MPH && mode == MaxSpeedCapMode.SCALE_KMH) {
                  newDisplay = mphToKmh(prevDisplay);
                  newDisplay = Math.max(1.0f, Math.min(320.0f, newDisplay)); // clamp to KMH model 1..320
    
              // % -> mph/km/h : only convert if a concrete roster speed profile is available
              } else if (prevMode == MaxSpeedCapMode.THROTTLE
                      && (mode == MaxSpeedCapMode.SCALE_MPH || mode == MaxSpeedCapMode.SCALE_KMH)) {
    
                  if (isConcreteSpeedProfileAvailable()) {
                      if (mode == MaxSpeedCapMode.SCALE_MPH) {
                          newDisplay = percentToScaleMph(prevDisplay);
                          newDisplay = Math.max(1.0f, Math.min(200.0f, newDisplay));
                      } else {
                          newDisplay = percentToScaleKmh(prevDisplay);
                          newDisplay = Math.max(1.0f, Math.min(320.0f, newDisplay));
                      }
                  } else {
                      // No profile: do NOT convert. Show the last sticky scale speed.
                      newDisplay = (mode == MaxSpeedCapMode.SCALE_MPH)
                              ? cachedScaleMph
                              : mphToKmh(cachedScaleMph);
                  }
    
              // mph/km/h -> % : only convert if a concrete roster speed profile is available
              } else if ((prevMode == MaxSpeedCapMode.SCALE_MPH || prevMode == MaxSpeedCapMode.SCALE_KMH)
                      && mode == MaxSpeedCapMode.THROTTLE) {
    
                  if (isConcreteSpeedProfileAvailable()) {
                      if (prevMode == MaxSpeedCapMode.SCALE_MPH) {
                          newDisplay = scaleSpeedToPercentFromProfile(prevDisplay, false); // mph -> %
                      } else {
                          newDisplay = scaleSpeedToPercentFromProfile(prevDisplay, true);  // km/h -> %
                      }
                      // Clamp to spinner's % model 0.10..1.00
                      newDisplay = Math.max(0.10f, Math.min(1.00f, newDisplay));
                  } else {
                           // No profile: do NOT convert. Show the last sticky % value (clamped)
                           newDisplay = cachedThrottlePercent;
                           newDisplay = Math.max(0.10f, Math.min(1.00f, newDisplay));
                         }  
              } else {
                  // Same-mode selection or THROTTLE->THROTTLE: keep numeric as-is
              }
    
              // 3) Update spinner model/editor/unit to the new mode, then set the display value.
                suppressMaxSpeedSpinnerEvents = true;
                try {
                    // Set the visible mode first so any incidental listeners see the correct mode
                    lastMaxSpeedCapMode = mode;
                
                    // Now change the spinner model and the numeric value
                    updateMaxSpeedSpinnerModelForMode(mode);
                    maxSpeedSpinner.setValue(Float.valueOf(newDisplay));
                } finally {
                    suppressMaxSpeedSpinnerEvents = false;
                }
                handleMinReliableOperatingSpeedUpdate();
          }
        });
        maxSpeedSpinner.setToolTipText(Bundle.getMessage("MaxSpeedHint"));
        pa1.add(minReliableOperatingSpeedLabel);
        minReliableOperatingSpeedSpinner.setModel(new SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(1.0f), Float.valueOf(0.01f)));
        minReliableOperatingSpeedSpinner.setEditor(new JSpinner.NumberEditor(minReliableOperatingSpeedSpinner, "# %"));
        pa1.add(minReliableOperatingSpeedSpinner);
        minReliableOperatingSpeedSpinner.setToolTipText(Bundle.getMessage("MinReliableOperatingSpeedHint"));
        minReliableOperatingSpeedSpinner.addChangeListener( e -> handleMinReliableOperatingSpeedUpdate());
        pa1.add(minReliableOperatingScaleSpeedLabel);
        initiatePane.add(pa1);
        pa2.setLayout(new FlowLayout());
        pa2.add(rampRateLabel);
        pa2.add(rampRateBox);
        rampRateBox.setToolTipText(Bundle.getMessage("RampRateBoxHint"));
        pa2.add(useSpeedProfileLabel);
        pa2.add(useSpeedProfileCheckBox);
        useSpeedProfileCheckBox.setToolTipText(Bundle.getMessage("UseSpeedProfileHint"));
        initiatePane.add(pa2);
        pa2a.setLayout(new FlowLayout());
        pa2a.add(stopBySpeedProfileLabel);
        pa2a.add(stopBySpeedProfileCheckBox);
        stopBySpeedProfileCheckBox.setToolTipText(Bundle.getMessage("UseSpeedProfileHint")); // reuse identical hint for Stop
        pa2a.add(stopBySpeedProfileAdjustLabel);
        stopBySpeedProfileAdjustSpinner.setModel(new SpinnerNumberModel( Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(5.0f), Float.valueOf(0.01f)));
        stopBySpeedProfileAdjustSpinner.setEditor(new JSpinner.NumberEditor(stopBySpeedProfileAdjustSpinner, "# %"));
        pa2a.add(stopBySpeedProfileAdjustSpinner);
        stopBySpeedProfileAdjustSpinner.setToolTipText(Bundle.getMessage("StopBySpeedProfileAdjustHint"));
        initiatePane.add(pa2a);       

         // “Override stop sensor” (default OFF = use sensors when present).
         // NOTE: No mutual exclusion with Stop-by-speed-profile or distance mode.
         // When checked, ignore stop sensors and rely on distance/profile stopping.
         // When unchecked, use sensors if present; runtime will slow within distance and finally stop at the sensor.
         pa2a.add(overrideStopSensorCheckBox);
         overrideStopSensorCheckBox.setToolTipText(Bundle.getMessage("OverrideStopSensorHint"));
         overrideStopSensorCheckBox.addActionListener(ev -> {
             // Keep UI coherent, but do NOT disable profile/distance options here.
             updateStopByDistanceEnable();
         });
            
        pa2b.add(stopByDistanceLabel);
        pa2b.add(stopByDistanceEnableCheckBox);
        
        // Distance value first
        stopByDistanceMmSpinner.setModel(
            new SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(1000000.0f), Float.valueOf(0.1f))
        );
        stopByDistanceMmSpinner.setEditor(new JSpinner.NumberEditor(stopByDistanceMmSpinner, "0.0"));
        stopByDistanceMmSpinner.setToolTipText(Bundle.getMessage("StopByDistanceHint"));
        pa2b.add(stopByDistanceMmSpinner);
        
        // Units dropdown next 
        stopByDistanceUnitsComboBox.addItem(
                new StopDistanceUnitsItem(bundleOrDefault("StopByDistanceUnitsCm", "cm"), StopDistanceUnits.ACTUAL_CM));
        stopByDistanceUnitsComboBox.addItem(
            new StopDistanceUnitsItem(Bundle.getMessage("StopByDistanceUnitsMm"), StopDistanceUnits.ACTUAL_MM)
        );
        stopByDistanceUnitsComboBox.addItem(
            new StopDistanceUnitsItem(Bundle.getMessage("StopByDistanceUnitsInch"), StopDistanceUnits.ACTUAL_INCHES)
        );
        stopByDistanceUnitsComboBox.addItem(
            new StopDistanceUnitsItem(Bundle.getMessage("StopByDistanceUnitsScaleMeters"), StopDistanceUnits.SCALE_METERS)
        );
        stopByDistanceUnitsComboBox.addItem(
            new StopDistanceUnitsItem(Bundle.getMessage("StopByDistanceUnitsScaleFeet"), StopDistanceUnits.SCALE_FEET)
        );
        pa2b.add(stopByDistanceUnitsComboBox);

         // Initialize Physics visibility based on current availability (enabled state)
         updateRampPhysicsAvailability(useSpeedProfileCheckBox.isEnabled());
        
        // Head/Tail radios last (to the right of the units dropdown)
        stopByDistanceRefGroup.add(stopByDistanceHead);
        stopByDistanceRefGroup.add(stopByDistanceTail);
        
        // Localised tooltips for Head/Tail reference selection
        stopByDistanceHead.setToolTipText(Bundle.getMessage("StopByDistanceHeadHint"));
        stopByDistanceTail.setToolTipText(Bundle.getMessage("StopByDistanceTailHint"));
        
        stopByDistanceHead.setSelected(true);
        pa2b.add(stopByDistanceHead);
        pa2b.add(stopByDistanceTail);
        
        initiatePane.add(pa2b);

        // Event wiring:
        // - Toggle mutually-exclusive mode (adjust% vs. distance) and availability
        stopByDistanceEnableCheckBox.addActionListener(ev -> updateStopByDistanceEnable());
        stopBySpeedProfileCheckBox.addActionListener(ev -> updateStopByDistanceEnable());
    
        // - Units change: convert current displayed value from old units to new, preserving the underlying mm value
        stopByDistanceUnitsComboBox.addActionListener(ev -> handleStopByDistanceUnitsComboSelectionChanged());

        updateStopByDistanceEnable();
        StopDistanceUnits preferredStopUnits = getPreferredStopDistanceUnitsFromMaxTrainLengthUnits();
        currentStopDistanceUnits = preferredStopUnits;
        setStopByDistanceUnitsSelection(preferredStopUnits);
        updateStopByDistanceSpinnerModelForUnits(preferredStopUnits);
        pa2b.add(stopByDistanceUnitsComboBox);      

         // --- Physics: Additional train weight panel ---
         pa2Physics.setLayout(new FlowLayout());
         pa2Physics.add(additionalWeightLabel);
    
         // Numeric spinner: wide range, fine step; display value depends on chosen units
         additionalWeightSpinner.setModel(
             new SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(10000.0f), Float.valueOf(0.1f))
         );
         additionalWeightSpinner.setEditor(new JSpinner.NumberEditor(additionalWeightSpinner, "0.0"));
         additionalWeightSpinner.setToolTipText(Bundle.getMessage("AdditionalTrainWeightHint"));
         pa2Physics.add(additionalWeightSpinner);
    
         // Units dropdown (replaces a static label); conversions are UI-only; TrainInfo stores metric tonnes
        additionalWeightUnitsComboBox.addItem(new AdditionalWeightUnitsItem(Bundle.getMessage("AdditionalWeightUnitsMetricTonnes"), AdditionalWeightUnits.METRIC_TONNES));
        additionalWeightUnitsComboBox.addItem(new AdditionalWeightUnitsItem(Bundle.getMessage("AdditionalWeightUnitsLongTons"),   AdditionalWeightUnits.LONG_TONS));
        additionalWeightUnitsComboBox.addItem(new AdditionalWeightUnitsItem(Bundle.getMessage("AdditionalWeightUnitsShortTons"),  AdditionalWeightUnits.SHORT_TONS));
        pa2Physics.add(additionalWeightUnitsComboBox);     
         
         // Rolling resistance coefficient (dimensionless, default 0.002)
         rollingResistanceCoeffSpinner.setModel(
             new SpinnerNumberModel(Float.valueOf(0.002f), Float.valueOf(0.000f), Float.valueOf(0.020f), Float.valueOf(0.0001f))
         );
         rollingResistanceCoeffSpinner.setEditor(new JSpinner.NumberEditor(rollingResistanceCoeffSpinner, "0.0000"));
         rollingResistanceCoeffSpinner.setToolTipText(Bundle.getMessage("RollingResistanceCoeffHint"));
         pa2Physics.add(rollingResistanceCoeffLabel);
         pa2Physics.add(rollingResistanceCoeffSpinner);      

          // Driver power during acceleration (% of full power); stored 0..1 in TrainInfo
          driverPowerPercentSpinner.setModel(
              new SpinnerNumberModel(Float.valueOf(100.0f), Float.valueOf(10.0f), Float.valueOf(100.0f), Float.valueOf(1.0f))
          );
          driverPowerPercentSpinner.setEditor(new JSpinner.NumberEditor(driverPowerPercentSpinner, "##0'%'"));
          driverPowerPercentSpinner.setToolTipText(Bundle.getMessage("DriverPowerPercentHint"));
          pa2Physics.add(driverPowerPercentLabel);
          pa2Physics.add(driverPowerPercentSpinner);

         // Rolling resistance coefficient (dimensionless, default 0.002)
         rollingResistanceCoeffSpinner.setModel(
             new SpinnerNumberModel(Float.valueOf(0.002f), Float.valueOf(0.000f), Float.valueOf(0.020f), Float.valueOf(0.0001f))
         );
         rollingResistanceCoeffSpinner.setEditor(new JSpinner.NumberEditor(rollingResistanceCoeffSpinner, "0.0000"));
         rollingResistanceCoeffSpinner.setToolTipText(Bundle.getMessage("RollingResistanceCoeffHint"));
         pa2Physics.add(rollingResistanceCoeffLabel);
         pa2Physics.add(rollingResistanceCoeffSpinner);
            
         // Units change => convert displayed value to new units, preserving underlying tonnes
         additionalWeightUnitsComboBox.addActionListener(ev -> {
             float display = ((Number) additionalWeightSpinner.getValue()).floatValue();
             float tonnes = convertDisplayToTonnes(currentAdditionalWeightUnits, display);
             currentAdditionalWeightUnits = additionalWeightUnitsComboBox.getSelectedUnits();
             float newDisplay = convertTonnesToDisplay(currentAdditionalWeightUnits, tonnes);
             additionalWeightSpinner.setValue(Float.valueOf(newDisplay));
         });
    
         // Initially hidden; becomes visible only when speed profile is ON and ramp == Physics
         pa2Physics.setVisible(false);
         initiatePane.add(pa2Physics);
    
        // Events to keep Physics option availability in sync with "Use speed profile"
        useSpeedProfileCheckBox.addActionListener(ev -> {
            // Show/hide Physics purely based on availability (enabled), not checkbox selection.
            updateRampPhysicsAvailability(useSpeedProfileCheckBox.isEnabled());
        });
        rampRateBox.addActionListener(ev -> handleRampRateSelectionChanged());        
        pa3.setLayout(new FlowLayout());
        pa3.add(soundDecoderBox);
        soundDecoderBox.setToolTipText(Bundle.getMessage("SoundDecoderBoxHint"));
        pa3.add(new JLabel("   "));
        pa3.add(runInReverseBox);
        runInReverseBox.setToolTipText(Bundle.getMessage("RunInReverseBoxHint"));
        initiatePane.add(pa3);
        maxTrainLengthSpinner.setModel(new SpinnerNumberModel(Float.valueOf(18.0f), Float.valueOf(0.0f), Float.valueOf(10000.0f), Float.valueOf(0.5f)));
        maxTrainLengthSpinner.setEditor(new JSpinner.NumberEditor(maxTrainLengthSpinner, "###0.0"));
        maxTrainLengthSpinner.setToolTipText(Bundle.getMessage("MaxTrainLengthHint")); // won't be updated while Dispatcher is open
        maxTrainLengthSpinner.addChangeListener( e -> handlemaxTrainLengthChangeUnitsLength());
        trainLengthUnitsComboBox.addActionListener(e -> handleTrainLengthUnitsComboSelectionChanged());
        trainLengthAltLengthLabel=new JLabel();
        pa4.setLayout(new FlowLayout());
        pa4.add(trainLengthLabel);
        pa4.add(maxTrainLengthSpinner);
        pa4.add(trainLengthUnitsComboBox);
        pa4.add(trainLengthAltLengthLabel);
        initiatePane.add(pa4);
        pa5_FNumbers.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("fnumbers")));
        pa5_FNumbers.setLayout(new FlowLayout());
        fNumberLightSpinner.setModel(new SpinnerNumberModel(0,0,100,1));
        fNumberLightSpinner.setToolTipText(Bundle.getMessage("fnumberlighthint"));
        pa5_FNumbers.add(fNumberLightLabel);
        pa5_FNumbers.add(fNumberLightSpinner);
        fNumberBellSpinner.setModel(new SpinnerNumberModel(0,0,100,1));
        fNumberBellSpinner.setToolTipText(Bundle.getMessage("fnumberbellhint"));
        pa5_FNumbers.add(fNumberBellLabel);
        pa5_FNumbers.add(fNumberBellSpinner);
        fNumberHornSpinner.setModel(new SpinnerNumberModel(0,0,100,1));
        fNumberHornSpinner.setToolTipText(Bundle.getMessage("fnumberhornhint"));
        pa5_FNumbers.add(fNumberHornLabel);
        pa5_FNumbers.add(fNumberHornSpinner);
        initiatePane.add(pa5_FNumbers);
        showHideAutoRunItems(autoRunBox.isSelected());   // initialize with auto run items hidden
    }

    private void handleMinReliableOperatingSpeedUpdate() {
        // Read % as float
        float mROS = ((Number) minReliableOperatingSpeedSpinner.getValue()).floatValue();
        // Clear label by default
        minReliableOperatingScaleSpeedLabel.setText("");
    
        // Only attempt conversion when speed-profile UI is enabled
        if (!useSpeedProfileCheckBox.isEnabled()) {
            return;
        }
    
        // RosterEntryComboBox is JComboBox<Object>; first item is a String ("no selection").
        Object sel = rosterComboBox.getRosterEntryComboBox().getSelectedItem();
        if (!(sel instanceof RosterEntry)) {
            // No roster entry selected yet; nothing to display
            return;
        }
    
        RosterEntry re = (RosterEntry) sel;
        RosterSpeedProfile sp = re.getSpeedProfile();
        if (sp == null || sp.getProfileSize() < 1) {
            // No profile data; nothing to display
            return;
        }
    
        // Convert % -> mm/s, then format in the currently selected preferred units
        float mms = sp.getSpeed(mROS, true);
        minReliableOperatingScaleSpeedLabel.setText(formatScaleSpeedWithPreferredUnits(mms));
    }

    private void handlemaxTrainLengthChangeUnitsLength() {
        trainLengthAltLengthLabel.setText(maxTrainLengthCalculateAltFormatted(
                ((TrainLengthUnitsItem) trainLengthUnitsComboBox.getSelectedItem()).getValue(),
                (float) maxTrainLengthSpinner.getValue()));
    }

     // Convert the displayed length when the user changes the units combo.
     // We preserve the actual length by converting display -> scale meters -> new display units.
     private void handleTrainLengthUnitsChanged() {
         if (suppressTrainLengthUnitsEvents) {
             // Programmatic change (e.g., file load): just refresh the alternate label.
             handlemaxTrainLengthChangeUnitsLength();
             return;
         }
         TrainLengthUnits newUnits =
             ((TrainLengthUnitsItem) trainLengthUnitsComboBox.getSelectedItem()).getValue();
    
         // 1) Capture the current display value and convert it to scale meters.
         float currentDisplay = ((Number) maxTrainLengthSpinner.getValue()).floatValue();
         float scaleMeters = maxTrainLengthToScaleMeters(currentTrainLengthUnits, currentDisplay);
    
         // 2) Convert the common baseline (scale meters) to the newly selected display units.
         float newDisplay = scaleMetersToDisplay(newUnits, scaleMeters);
    
         // 3) Update spinner without re‑entering this handler; update current unit tracker.
         suppressTrainLengthUnitsEvents = true;
         try {
             maxTrainLengthSpinner.setValue(Float.valueOf(newDisplay));
             currentTrainLengthUnits = newUnits;
         } finally {
             suppressTrainLengthUnitsEvents = false;
         }
    
         // 4) Keep the alternate-length label in sync.
         handlemaxTrainLengthChangeUnitsLength();
     }


    /**
     * Get an I18N String of the max TrainLength.
     * @param fromUnits the Length Unit.
     * @param fromValue the length.
     * @return String format of the length.
     */
    private String maxTrainLengthCalculateAltFormatted(TrainLengthUnits fromUnits, float fromValue) {
        float value = maxTrainLengthCalculateAlt(fromUnits, fromValue);
        switch (fromUnits) {
            case TRAINLENGTH_ACTUALINCHS:
                return String.format(Locale.getDefault(), "%.2f %s",
                    value, Bundle.getMessage("TrainLengthInScaleFeet"));
            case TRAINLENGTH_ACTUALCM:
                return String.format(Locale.getDefault(), "%.1f %s",
                    value, Bundle.getMessage("TrainLengthInScaleMeters"));
            case TRAINLENGTH_SCALEFEET:
                return String.format(Locale.getDefault(), "%.1f %s",
                    value, Bundle.getMessage("TrainLengthInActualInchs"));
            case TRAINLENGTH_SCALEMETERS:
                return String.format(Locale.getDefault(), "%.0f %s",
                    value, Bundle.getMessage("TrainLengthInActualcm"));
            default:
                log.error("Invalid TrainLengthUnits must have been updated, fix maxTrainLengthCalculateAltFormatted");
        }
        return "";
    }

    private float maxTrainLengthToScaleMeters(TrainLengthUnits fromUnits, float fromValue) {
        float value;
        final float scaleRatio = (_dispatcher.getScale() != null)
                ? (float) _dispatcher.getScale().getScaleRatio()
                : 1.0f;
        // convert to meters.
        switch (fromUnits) {
            case TRAINLENGTH_ACTUALINCHS:
                value = fromValue / 12.0f * scaleRatio;
                value = value / 3.28084f;
                break;
            case TRAINLENGTH_ACTUALCM:
                value = fromValue / 100.0f * scaleRatio;
                break;
           case TRAINLENGTH_SCALEFEET:
               value = fromValue / 3.28084f;
               break;
           case TRAINLENGTH_SCALEMETERS:
               value = fromValue;
               break;
           default:
               value = 0;
               log.error("Invalid TrainLengthUnits has been updated, fix me");
        }
        return value;
    }
    
    /**
     * Convert from scale meters to the requested display units.
     */
    private float scaleMetersToDisplay(TrainLengthUnits toUnits, float scaleMeters) {
        final float scaleFactor = (_dispatcher.getScale() != null)
                ? (float) _dispatcher.getScale().getScaleFactor()
                : 1.0f; // CI-safe default
    
        switch (toUnits) {
            case TRAINLENGTH_SCALEMETERS:
                return scaleMeters;
            case TRAINLENGTH_SCALEFEET:
                return scaleMeters * 3.28084f;
            case TRAINLENGTH_ACTUALINCHS:
                // actual inches = scale meters × scaleFactor (scale→actual) × feet/m × 12 in/ft
                return scaleMeters * scaleFactor * 3.28084f * 12.0f;
            case TRAINLENGTH_ACTUALCM:
                // actual cm = scale meters × scaleFactor (scale→actual) × 100 cm/m
                return scaleMeters * scaleFactor * 100.0f;
            default:
                return scaleMeters;
        }
    }

    /**
     * Calculates the reciprocal unit. Actual to Scale and vice versa
     */
    private float maxTrainLengthCalculateAlt(TrainLengthUnits fromUnits, float fromValue) {
        final float scaleRatio = (_dispatcher.getScale() != null)
                ? (float) _dispatcher.getScale().getScaleRatio()
                : 1.0f;
        switch (fromUnits) {
            case TRAINLENGTH_ACTUALINCHS:
                // calc scale feet
                return (float) jmri.util.MathUtil.granulize(fromValue / 12 * scaleRatio, 0.1f);
            case TRAINLENGTH_ACTUALCM:
                // calc scale meter
                return fromValue / 100 * scaleRatio;
            case TRAINLENGTH_SCALEFEET: { // calc actual inches
                final float scaleFactor = (_dispatcher.getScale() != null)
                        ? (float) _dispatcher.getScale().getScaleFactor()
                        : 1.0f;
                return fromValue * 12.0f * scaleFactor;
            }
            case TRAINLENGTH_SCALEMETERS: { // calc actual cm.
                final float scaleFactor = (_dispatcher.getScale() != null)
                        ? (float) _dispatcher.getScale().getScaleFactor()
                        : 1.0f;
                return fromValue * 100.0f * scaleFactor;
            }
           default:
               log.error("Invalid TrainLengthUnits has been updated, fix me");
        }
        return 0;
    }

    private void showHideAutoRunItems(boolean value) {
        pa1.setVisible(value);
        pa2.setVisible(value);
        pa2a.setVisible(value);
        pa3.setVisible(value);
        pa4.setVisible(value);
        pa5_FNumbers.setVisible(value);
    }

    private void autoTrainInfoToDialog(TrainInfo info) {
        speedFactorSpinner.setValue(info.getSpeedFactor());
        // Choose mode by presence of scale km/h
        boolean hasScaleKmh = info.getMaxSpeedScaleKmh() > 0.0f;
        if (hasScaleKmh && useSpeedProfileCheckBox.isEnabled()) {
            // Default to km/h display when loading from file
            maxSpeedCapModeBox.setSelectedIndex(Math.min(2, maxSpeedCapModeBox.getItemCount()-1)); // item 2 is KMH when enabled
            updateMaxSpeedSpinnerModelForMode(MaxSpeedCapMode.SCALE_KMH);
            maxSpeedSpinner.setValue(info.getMaxSpeedScaleKmh());
        } else {
            maxSpeedCapModeBox.setSelectedIndex(0); // THROTTLE
            updateMaxSpeedSpinnerModelForMode(MaxSpeedCapMode.THROTTLE);
            maxSpeedSpinner.setValue(info.getMaxSpeed());
        }
        minReliableOperatingSpeedSpinner.setValue(info.getMinReliableOperatingSpeed());
        String rampLabel = normalizeRampLabel(info.getRampRate()); 

         // Physics: set additional weight spinner (convert stored metric tonnes to current UI units)
         currentAdditionalWeightUnits = AdditionalWeightUnits.METRIC_TONNES;
         additionalWeightUnitsComboBox.setSelectedIndex(0);
         additionalWeightSpinner.setValue(Float.valueOf(convertTonnesToDisplay(currentAdditionalWeightUnits, info.getAdditionalTrainWeightMetricTonnes())));       
         additionalWeightUnitsComboBox.setSelectedIndex(0);
         rollingResistanceCoeffSpinner.setValue(Float.valueOf(info.getRollingResistanceCoeff()));
         
         // Driver power % -> spinner uses 0..100; file stores 0..1
         float dp = info.getDriverPowerPercent();
         if (dp <= 0.0f) dp = 0.0f;
         if (dp > 1.0f) dp = 1.0f;
         driverPowerPercentSpinner.setValue(Float.valueOf(dp * 100.0f));
    
         // Physics availability & panel visibility based on current Speed-profile + ramp
        useSpeedProfileCheckBox.setSelected(info.getUseSpeedProfile());
        // Physics availability depends only on whether speed profile UI is enabled (availability), not on selection
        updateRampPhysicsAvailability(useSpeedProfileCheckBox.isEnabled());
        
        // Now that the items are rebuilt, set the ramp selection using the normalized label
        setComboBox(rampRateBox, rampLabel);
        
        // Physics weight row visibility follows the current ramp selection
        pa2Physics.setVisible(isPhysicsRampSelected());

      
        trainDetectionComboBox.setSelectedItemByValue(info.getTrainDetection());
        runInReverseBox.setSelected(info.getRunInReverse());
        soundDecoderBox.setSelected(info.getSoundDecoder());
        try {
            trainLengthUnitsComboBox.setSelectedItemByValue(info.getTrainLengthUnits());
            switch (info.getTrainLengthUnits()) {
                case TRAINLENGTH_SCALEFEET:
                    maxTrainLengthSpinner.setValue(info.getMaxTrainLengthScaleFeet());
                    break;
                case TRAINLENGTH_SCALEMETERS:
                    maxTrainLengthSpinner.setValue(info.getMaxTrainLengthScaleMeters());
                    break;
                case TRAINLENGTH_ACTUALINCHS: {
                    float sf = (_dispatcher.getScale() != null)
                        ? (float)_dispatcher.getScale().getScaleFactor()
                        : 1.0f; // CI-safe default
                    maxTrainLengthSpinner.setValue(info.getMaxTrainLengthScaleFeet() * 12.0f * sf);
                    break;
                }
                case TRAINLENGTH_ACTUALCM: {
                    float sf = (_dispatcher.getScale() != null)
                        ? (float)_dispatcher.getScale().getScaleFactor()
                        : 1.0f; // CI-safe default
                    maxTrainLengthSpinner.setValue(info.getMaxTrainLengthScaleMeters() * 100.0f * sf);
                    break;
                }
    
                default:
                    maxTrainLengthSpinner.setValue(0.0f);
            }
        } finally {
            suppressTrainLengthUnitsEvents = false;
        }
        
        useSpeedProfileCheckBox.setSelected(info.getUseSpeedProfile());
        stopBySpeedProfileCheckBox.setSelected(info.getStopBySpeedProfile());
        stopBySpeedProfileAdjustSpinner.setValue(info.getStopBySpeedProfileAdjust());
        overrideStopSensorCheckBox.setSelected(!info.getUseStopSensor());
        updateStopByDistanceEnable();
        stopByDistanceEnableCheckBox.setSelected(info.getStopByDistanceMm() > 0.0f);
    
        // Default Stop-by-distance units follow current Max Train Length units. Convert the stored mm to current display units.
        StopDistanceUnits preferredStopUnits = getPreferredStopDistanceUnitsFromMaxTrainLengthUnits();
        currentStopDistanceUnits = preferredStopUnits;
        setStopByDistanceUnitsSelection(preferredStopUnits);
        float displayValue = convertMmToStopDisplay(currentStopDistanceUnits, info.getStopByDistanceMm());
        stopByDistanceMmSpinner.setValue(Float.valueOf(displayValue));
        updateStopByDistanceSpinnerModelForUnits(currentStopDistanceUnits);
    
         if (info.getStopByDistanceRef() == TrainInfo.StopReference.TAIL) {
             stopByDistanceTail.setSelected(true);
         } else {
             stopByDistanceHead.setSelected(true);
         }
         updateStopByDistanceEnable();
        fNumberLightSpinner.setValue(info.getFNumberLight());
        fNumberBellSpinner.setValue(info.getFNumberBell());
        fNumberHornSpinner.setValue(info.getFNumberHorn());
         showHideAutoRunItems(autoRunBox.isSelected());
        initiateFrame.pack();
    }

    private void autoRunItemsToTrainInfo(TrainInfo info) {
        info.setSpeedFactor((float) speedFactorSpinner.getValue());
        MaxSpeedCapMode mode = ((MaxSpeedCapModeItem) maxSpeedCapModeBox.getSelectedItem()).getValue();
        if (mode == MaxSpeedCapMode.THROTTLE) {
            // Throttle mode: write % (0.0..1.0) and clear scale-speed
            info.setMaxSpeed((float) maxSpeedSpinner.getValue());
            info.setMaxSpeedScaleKmh(0.0f);
        } else if (mode == MaxSpeedCapMode.SCALE_MPH) {
            // Convert mph → km/h for storage
            float mph = ((Number) maxSpeedSpinner.getValue()).floatValue();
            info.setMaxSpeedScaleKmh(mphToKmh(mph));
            // Preserve existing throttle % (fallback) untouched
        } else { // SCALE_KMH
            float kmh = ((Number) maxSpeedSpinner.getValue()).floatValue();
            info.setMaxSpeedScaleKmh(kmh);
            // Preserve existing throttle % (fallback) untouched
        }
        info.setMinReliableOperatingSpeed((float) minReliableOperatingSpeedSpinner.getValue());
        info.setRampRate((String) rampRateBox.getSelectedItem());
         // Physics: when ramp == Physics, store additional weight (metric tonnes); else store 0.0f
         if (isPhysicsRampSelected()) {
             float display = ((Number) additionalWeightSpinner.getValue()).floatValue();
             float tonnes = convertDisplayToTonnes(currentAdditionalWeightUnits, display);
             info.setAdditionalTrainWeightMetricTonnes(tonnes);
         } else {
             info.setAdditionalTrainWeightMetricTonnes(0.0f);
         }


         // Driver power percent: only meaningful for Physics ramp, but we always persist (default 1.0 when not physics)
         float dpct = ((Number) driverPowerPercentSpinner.getValue()).floatValue() / 100.0f;
         if (dpct < 0.0f) dpct = 0.0f; else if (dpct > 1.0f) dpct = 1.0f;
         info.setDriverPowerPercent(isPhysicsRampSelected() ? dpct : 1.0f);
             
         // Always store c_rr (independent of ramp selection)
        info.setRollingResistanceCoeff(((Number) rollingResistanceCoeffSpinner.getValue()).floatValue());
        info.setRunInReverse(runInReverseBox.isSelected());
        info.setSoundDecoder(soundDecoderBox.isSelected());
        info.setTrainLengthUnits(((TrainLengthUnitsItem) trainLengthUnitsComboBox.getSelectedItem()).getValue());
        info.setMaxTrainLengthScaleMeters(maxTrainLengthToScaleMeters( info.getTrainLengthUnits(), (float) maxTrainLengthSpinner.getValue()));

        // Only use speed profile values if enabled
        if (useSpeedProfileCheckBox.isEnabled()) {
            info.setUseSpeedProfile(useSpeedProfileCheckBox.isSelected());
            info.setStopBySpeedProfile(stopBySpeedProfileCheckBox.isSelected());
            info.setStopBySpeedProfileAdjust((float) stopBySpeedProfileAdjustSpinner.getValue());
        } else {
            info.setUseSpeedProfile(false);
            info.setStopBySpeedProfile(false);
            info.setStopBySpeedProfileAdjust(1.0f);
        }       

         // Persist inverse of “Override stop sensor” (unchecked = use sensors)
         info.setUseStopSensor(!overrideStopSensorCheckBox.isSelected());

         // Only meaningful if Stop-by-speed-profile is enabled & selected
         boolean baseOn = stopBySpeedProfileCheckBox.isEnabled() && stopBySpeedProfileCheckBox.isSelected();
         if (baseOn && stopByDistanceEnableCheckBox.isSelected()) {
             float displayValue = ((Number) stopByDistanceMmSpinner.getValue()).floatValue();
             float mm = convertStopDisplayToMm(currentStopDistanceUnits, displayValue);
    
             info.setStopByDistanceMm(mm);
             info.setStopByDistanceRef(stopByDistanceTail.isSelected()
                 ? TrainInfo.StopReference.TAIL
                 : TrainInfo.StopReference.HEAD);
         } else {
             info.setStopByDistanceMm(0.0f);
             info.setStopByDistanceRef(TrainInfo.StopReference.HEAD);
         }
    
            info.setFNumberLight((int)fNumberLightSpinner.getValue());
            info.setFNumberBell((int)fNumberBellSpinner.getValue());
            info.setFNumberHorn((int)fNumberHornSpinner.getValue());
        }

    // Map legacy ramp values (numeric codes or Bundle keys) to the current display label used in rampRateBox.
     private String normalizeRampLabel(String raw) {
         if (raw == null || raw.trim().isEmpty()) {
             return Bundle.getMessage("RAMP_NONE");
         }
         String s = raw.trim();
    
         // Numeric legacy codes -> display labels; order must match AutoActiveTrain constants
         if (s.matches("\\d+")) {
             switch (Integer.parseInt(s)) {
                 case 0: return Bundle.getMessage("RAMP_NONE");
                 case 1: return Bundle.getMessage("RAMP_FAST");
                 case 2: return Bundle.getMessage("RAMP_MEDIUM");
                 case 3: return Bundle.getMessage("RAMP_MED_SLOW");
                 case 4: return Bundle.getMessage("RAMP_SLOW");
                 case 5: return Bundle.getMessage("RAMP_SPEEDPROFILE");
                 case 6: return Bundle.getMessage("RAMP_PHYSICS");
                 default: return Bundle.getMessage("RAMP_NONE");
             }
         }
    
         // Bundle key -> display label (e.g., "RAMP_MEDIUM")
         if ("RAMP_NONE".equals(s))         return Bundle.getMessage("RAMP_NONE");
         if ("RAMP_FAST".equals(s))         return Bundle.getMessage("RAMP_FAST");
         if ("RAMP_MEDIUM".equals(s))       return Bundle.getMessage("RAMP_MEDIUM");
         if ("RAMP_MED_SLOW".equals(s))     return Bundle.getMessage("RAMP_MED_SLOW");
         if ("RAMP_SLOW".equals(s))         return Bundle.getMessage("RAMP_SLOW");
         if ("RAMP_SPEEDPROFILE".equals(s)) return Bundle.getMessage("RAMP_SPEEDPROFILE");
         if ("RAMP_PHYSICS".equals(s))      return Bundle.getMessage("RAMP_PHYSICS");
    
         // Otherwise assume it's already a localized display label
         return s;
     }
  
    private void initializeRampCombo() {
        rampRateBox.removeAllItems();
        rampRateBox.addItem(Bundle.getMessage("RAMP_NONE"));
        rampRateBox.addItem(Bundle.getMessage("RAMP_FAST"));
        rampRateBox.addItem(Bundle.getMessage("RAMP_MEDIUM"));
        rampRateBox.addItem(Bundle.getMessage("RAMP_MED_SLOW"));
        rampRateBox.addItem(Bundle.getMessage("RAMP_SLOW"));
        rampRateBox.addItem(Bundle.getMessage("RAMP_SPEEDPROFILE"));
        rampRateBox.addItem(Bundle.getMessage("RAMP_PHYSICS")); // Visible only when speed-profile is enabled & selected
        // Note: the order above must correspond to the numbers in AutoActiveTrain.java
    }

    /**
     * Sets up the RadioButtons and visability of spinner for the allocation method
     *
     * @param value 0, Allocate by Safe spots, -1, allocate as far as possible Any
     *            other value the number of sections to allocate
     */
    private void setAllocateMethodButtons(int value) {
        switch (value) {
            case ActiveTrain.ALLOCATE_BY_SAFE_SECTIONS:
                allocateBySafeRadioButton.setSelected(true);
                allocateCustomSpinner.setVisible(false);
                break;
            case ActiveTrain.ALLOCATE_AS_FAR_AS_IT_CAN:
                allocateAllTheWayRadioButton.setSelected(true);
                allocateCustomSpinner.setVisible(false);
                break;
            default:
                allocateNumberOfBlocks.setSelected(true);
                allocateCustomSpinner.setVisible(true);
                allocateCustomSpinner.setValue(value);
        }
    }

    /*
     * Layout block stuff
     */
    private ArrayList<LayoutBlock> getOccupiedBlockList() {
        LayoutBlockManager lBM = InstanceManager.getDefault(LayoutBlockManager.class);
        ArrayList<LayoutBlock> lBlocks = new ArrayList<>();
        for (LayoutBlock lB : lBM.getNamedBeanSet()) {
            if (lB.getBlock().getState() == Block.OCCUPIED) {
                lBlocks.add(lB);
            }
        }
        return lBlocks;
    }

    private void initializeStartingBlockComboDynamic() {
        startingBlockBox.removeAllItems();
        startingBlockBoxList.clear();
        for (LayoutBlock lB: getOccupiedBlockList()) {
            if (!startingBlockBoxList.contains(lB.getBlock())) {
                startingBlockBoxList.add(lB.getBlock());
                startingBlockBox.addItem(getBlockName(lB.getBlock()));
            }
        }
        JComboBoxUtil.setupComboBoxMaxRows(startingBlockBox);
    }

    private void initializeViaBlockDynamicCombo() {
        String prevValue = (String) viaBlockBox.getSelectedItem();
        viaBlockBox.removeActionListener(viaBlockBoxListener);
        viaBlockBox.removeAllItems();
        viaBlockBoxList.clear();
        LayoutBlockManager lBM = InstanceManager.getDefault(LayoutBlockManager.class);
        if (startingBlockBox.getSelectedItem() != null) {
            LayoutBlock lBSrc;
            if (startingBlockBox.getSelectedIndex() >= 0) {
                lBSrc = lBM.getByUserName((String) startingBlockBox.getSelectedItem());
                if (lBSrc != null) {
                    int rX = lBSrc.getNumberOfNeighbours() - 1;
                    for (; rX > -1; rX--) {
                        viaBlockBox.addItem(lBSrc.getNeighbourAtIndex(rX).getDisplayName());
                        viaBlockBoxList.add(lBSrc.getNeighbourAtIndex(rX));
                    }
                }
            }
        }
        if (prevValue != null) {
            viaBlockBox.setSelectedItem(prevValue);
        }
        viaBlockBox.addActionListener(viaBlockBoxListener);
    }

    private void initializeDestinationBlockDynamicCombo() {
        destinationBlockBox.removeAllItems();
        destinationBlockBoxList.clear();
        LayoutBlockManager lBM = InstanceManager.getDefault(LayoutBlockManager.class);
        if (startingBlockBox.getSelectedItem() != null) {
            LayoutBlock lBSrc;
            if (startingBlockBox.getSelectedIndex() >= 0
                    && viaBlockBox.getSelectedIndex() >= 0) {
                lBSrc = lBM.getByUserName((String) startingBlockBox.getSelectedItem());
                Block b = viaBlockBoxList.get(viaBlockBox.getSelectedIndex());
                if (lBSrc != null) {
                    int rX = lBSrc.getNumberOfRoutes() - 1;
                    for (; rX > -1; rX--) {
                        if (lBSrc.getRouteNextBlockAtIndex(rX) == b) {
                            destinationBlockBox.addItem(lBSrc.getRouteDestBlockAtIndex(rX).getDisplayName());
                            destinationBlockBoxList.add(lBSrc.getRouteDestBlockAtIndex(rX));
                        }
                    }
                }
            }
        }
    }

    /*
     * Check Advanced routing
    */
    private boolean checkAdvancedRouting() {
        if (!InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
            int response = JmriJOptionPane.showConfirmDialog(this, Bundle.getMessage("AdHocNeedsEnableBlockRouting"),
                    Bundle.getMessage("AdHocNeedsBlockRouting"), JmriJOptionPane.YES_NO_OPTION);
            if (response == 0) {
                InstanceManager.getDefault(LayoutBlockManager.class).enableAdvancedRouting(true);
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("AdhocNeedsBlockRoutingEnabled"));
            } else {
                return false;
            }
        }
        return true;
    }
    
    

    /*
     * ComboBox item.
     */
    protected static class TrainDetectionItem {

        private final String key;
        private TrainDetection value;

        public TrainDetectionItem(String text, TrainDetection trainDetection ) {
            this.key = text;
            this.value = trainDetection;
        }

        @Override
        public String toString() {
            return key;
        }

        public String getKey() {
            return key;
        }

        public TrainDetection getValue() {
            return value;
        }
    }

    /*
     * ComboBox item.
     */
    protected static class TrainLengthUnitsItem {

        private final String key;
        private TrainLengthUnits value;

        public TrainLengthUnitsItem(String text, TrainLengthUnits trainLength ) {
            this.key = text;
            this.value = trainLength;
        }

        @Override
        public String toString() {
            return key;
        }

        public String getKey() {
            return key;
        }

        public TrainLengthUnits getValue() {
            return value;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActivateTrainFrame.class);

}
