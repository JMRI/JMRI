package jmri.jmrit.dispatcher;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.Transit;
import jmri.TransitManager;
import jmri.jmrit.dispatcher.ActiveTrain.TrainDetection;
import jmri.jmrit.dispatcher.DispatcherFrame.TrainsFrom;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;
import jmri.util.swing.JComboBoxUtil;
import jmri.util.swing.JmriJOptionPane;

/**
 * Displays the Activate New Train dialog and processes information entered
 * there.
 * <p>
 * This module works with Dispatcher, which initiates the display of the dialog.
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
    private TrainsFrom _TrainsFrom;
    private List<ActiveTrain> _ActiveTrainsList = null;
    private final TransitManager _TransitManager = InstanceManager.getDefault(jmri.TransitManager.class);
    private String _trainInfoName = "";

    // initiate train window variables
    private Transit selectedTransit = null;
    //private String selectedTrain = "";
    private JmriJFrame initiateFrame = null;
    private Container initiatePane = null;
    private final jmri.swing.NamedBeanComboBox<Transit> transitSelectBox = new jmri.swing.NamedBeanComboBox<>(_TransitManager);
    private final JLabel trainBoxLabel = new JLabel("     " + Bundle.getMessage("TrainBoxLabel") + ":");
    private final JComboBox<Object> trainSelectBox = new JComboBox<>();
    // private final List<RosterEntry> trainBoxList = new ArrayList<>();
    private RosterEntryComboBox rosterComboBox = null;
    private final JLabel trainFieldLabel = new JLabel(Bundle.getMessage("TrainBoxLabel") + ":");
    private final JTextField trainNameField = new JTextField(10);
    private final JLabel dccAddressFieldLabel = new JLabel("     " + Bundle.getMessage("DccAddressFieldLabel") + ":");
    private final JSpinner dccAddressSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 9999, 1));
    private final JCheckBox inTransitBox = new JCheckBox(Bundle.getMessage("TrainInTransit"));
    private final JComboBox<String> startingBlockBox = new JComboBox<>();
    private List<Block> startingBlockBoxList = new ArrayList<>();
    private List<Integer> startingBlockSeqList = new ArrayList<>();
    private final JComboBox<String> destinationBlockBox = new JComboBox<>();
    private List<Block> destinationBlockBoxList = new ArrayList<>();
    private List<Integer> destinationBlockSeqList = new ArrayList<>();
    private JButton addNewTrainButton = null;
    private JButton loadButton = null;
    private JButton saveButton = null;
    private JButton deleteButton = null;
    private final JCheckBox autoRunBox = new JCheckBox(Bundle.getMessage("AutoRun"));
    private final JCheckBox loadAtStartupBox = new JCheckBox(Bundle.getMessage("LoadAtStartup"));

    private final JRadioButton radioTrainsFromRoster = new JRadioButton(Bundle.getMessage("TrainsFromRoster"));
    private final JRadioButton radioTrainsFromOps = new JRadioButton(Bundle.getMessage("TrainsFromTrains"));
    private final JRadioButton radioTrainsFromUser = new JRadioButton(Bundle.getMessage("TrainsFromUser"));
    private final JRadioButton radioTrainsFromSetLater = new JRadioButton(Bundle.getMessage("TrainsFromSetLater"));
    private final ButtonGroup trainsFromButtonGroup = new ButtonGroup();

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

    int delayedStartInt[] = new int[]{ActiveTrain.NODELAY, ActiveTrain.TIMEDDELAY, ActiveTrain.SENSORDELAY};
    String delayedStartString[] = new String[]{Bundle.getMessage("DelayedStartNone"), Bundle.getMessage("DelayedStartTimed"), Bundle.getMessage("DelayedStartSensor")};

    private final JComboBox<String> reverseDelayedRestartType = new JComboBox<>(delayedStartString);
    private final JLabel delayReverseReStartLabel = new JLabel(Bundle.getMessage("DelayRestart"));
    private final JLabel delayReverseReStartSensorLabel = new JLabel(Bundle.getMessage("RestartSensor"));
    private final JCheckBox delayReverseResetSensorBox = new JCheckBox(Bundle.getMessage("ResetRestartSensor"));
    private final NamedBeanComboBox<Sensor> delayReverseReStartSensor = new NamedBeanComboBox<>(jmri.InstanceManager.sensorManagerInstance());
    private final JSpinner delayReverseMinSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
    private final JLabel delayReverseMinLabel = new JLabel(Bundle.getMessage("RestartTimed"));

    private final JCheckBox resetStartSensorBox = new JCheckBox(Bundle.getMessage("ResetStartSensor"));
    private final JComboBox<String> delayedStartBox = new JComboBox<>(delayedStartString);
    private final JLabel delayedReStartLabel = new JLabel(Bundle.getMessage("DelayRestart"));
    private final JLabel delayReStartSensorLabel = new JLabel(Bundle.getMessage("RestartSensor"));
    private final JCheckBox resetRestartSensorBox = new JCheckBox(Bundle.getMessage("ResetRestartSensor"));
    private final JComboBox<String> delayedReStartBox = new JComboBox<>(delayedStartString);
    private final NamedBeanComboBox<Sensor> delaySensor = new NamedBeanComboBox<>(jmri.InstanceManager.sensorManagerInstance());
    private final NamedBeanComboBox<Sensor> delayReStartSensor = new NamedBeanComboBox<>(jmri.InstanceManager.sensorManagerInstance());

    private final JSpinner departureHrSpinner = new JSpinner(new SpinnerNumberModel(8, 0, 23, 1));
    private final JSpinner departureMinSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
    private final JLabel departureTimeLabel = new JLabel(Bundle.getMessage("DepartureTime"));
    private final JLabel departureSepLabel = new JLabel(":");

    private final JSpinner delayMinSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
    private final JLabel delayMinLabel = new JLabel(Bundle.getMessage("RestartTimed"));

    private final JComboBox<String> trainTypeBox = new JComboBox<>();
    // Note: See also items related to automatically running trains near the end of this module

    boolean transitsFromSpecificBlock = false;

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
        if (_TrainsFrom == TrainsFrom.TRAINSFROMROSTER && re != null) {
            setRosterComboBox(rosterComboBox, re.getId());
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
        _TrainsFrom = _dispatcher.getTrainsFrom();
        _ActiveTrainsList = _dispatcher.getActiveTrainsList();
        // create window if needed
        if (initiateFrame == null) {
            initiateFrame = this;
            initiateFrame.setTitle(Bundle.getMessage("AddTrainTitle"));
            initiateFrame.addHelpMenu("package.jmri.jmrit.dispatcher.NewTrain", true);
            initiatePane = initiateFrame.getContentPane();
            initiatePane.setLayout(new BoxLayout(initiatePane, BoxLayout.Y_AXIS));

            // add buttons to load and save train information
            JPanel p0 = new JPanel();
            p0.setLayout(new FlowLayout());
            p0.add(loadButton = new JButton(Bundle.getMessage("LoadButton")));
            loadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadTrainInfo(e);
                }
            });
            loadButton.setToolTipText(Bundle.getMessage("LoadButtonHint"));
            p0.add(saveButton = new JButton(Bundle.getMessage("SaveButton")));
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveTrainInfo(e,_TrainsFrom == TrainsFrom.TRAINSFROMSETLATER ? true : false );
                }
            });
            saveButton.setToolTipText(Bundle.getMessage("SaveButtonHint"));
            p0.add(deleteButton = new JButton(Bundle.getMessage("DeleteButton")));
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteTrainInfo(e);
                }
            });
            deleteButton.setToolTipText(Bundle.getMessage("DeleteButtonHint"));

            // add items relating to both manually run and automatic trains.

            // Trains From choices.
            JPanel p0a = new JPanel();
            p0a.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainsFrom")));
            p0a.setLayout(new FlowLayout());
            trainsFromButtonGroup.add(radioTrainsFromRoster);
            trainsFromButtonGroup.add(radioTrainsFromOps);
            trainsFromButtonGroup.add(radioTrainsFromUser);
            trainsFromButtonGroup.add(radioTrainsFromSetLater);
            p0a.add(radioTrainsFromRoster);
            radioTrainsFromRoster.setToolTipText(Bundle.getMessage("TrainsFromRosterHint"));
            p0a.add(radioTrainsFromOps);
            radioTrainsFromOps.setToolTipText(Bundle.getMessage("TrainsFromTrainsHint"));
            p0a.add(radioTrainsFromUser);
            radioTrainsFromUser.setToolTipText(Bundle.getMessage("TrainsFromUserHint"));
            p0a.add(radioTrainsFromSetLater);
            radioTrainsFromSetLater.setToolTipText(Bundle.getMessage("TrainsFromSetLaterHint"));

            radioTrainsFromOps.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e)  {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        _TrainsFrom = TrainsFrom.TRAINSFROMOPS;
                        setTrainsFromOptions(_TrainsFrom);
                    }
                }
            });

            radioTrainsFromRoster.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e)  {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        _TrainsFrom = TrainsFrom.TRAINSFROMROSTER;
                         setTrainsFromOptions(_TrainsFrom);
                    }
                }
            });
            radioTrainsFromUser.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e)  {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        _TrainsFrom = TrainsFrom.TRAINSFROMUSER;
                        setTrainsFromOptions(_TrainsFrom);
                    }
                }
            });
            radioTrainsFromSetLater.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e)  {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        _TrainsFrom = TrainsFrom.TRAINSFROMSETLATER;
                        setTrainsFromOptions(_TrainsFrom);
                    }
                }
            });
            p0a.add(allocateCustomSpinner);
            initiatePane.add(p0a);

            JPanel p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel(Bundle.getMessage("TransitBoxLabel") + " :"));
            p1.add(transitSelectBox);
            transitSelectBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleTransitSelectionChanged(e);
                }
            });
            transitSelectBox.setToolTipText(Bundle.getMessage("TransitBoxHint"));
            p1.add(trainBoxLabel);
            p1.add(trainSelectBox);
            trainSelectBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)  {
                        handleTrainSelectionChanged();
                }
            });
            trainSelectBox.setToolTipText(Bundle.getMessage("TrainBoxHint"));

            rosterComboBox = new RosterEntryComboBox();
            initializeFreeRosterEntriesCombo();
            rosterComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleRosterSelectionChanged(e);
                }
            });
            p1.add(rosterComboBox);

            initiatePane.add(p1);
            JPanel p1a = new JPanel();
            p1a.setLayout(new FlowLayout());
            p1a.add(trainFieldLabel);
            p1a.add(trainNameField);
            trainNameField.setToolTipText(Bundle.getMessage("TrainFieldHint"));
            p1a.add(dccAddressFieldLabel);
            p1a.add(dccAddressSpinner);
            dccAddressSpinner.setToolTipText(Bundle.getMessage("DccAddressFieldHint"));
            initiatePane.add(p1a);
            JPanel p2 = new JPanel();
            p2.setLayout(new FlowLayout());
            p2.add(inTransitBox);
            inTransitBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleInTransitClick(e);
                }
            });
            inTransitBox.setToolTipText(Bundle.getMessage("InTransitBoxHint"));
            initiatePane.add(p2);
            JPanel p3 = new JPanel();
            p3.setLayout(new FlowLayout());
            p3.add(new JLabel(Bundle.getMessage("StartingBlockBoxLabel") + " :"));
            p3.add(startingBlockBox);
            startingBlockBox.setToolTipText(Bundle.getMessage("StartingBlockBoxHint"));
            startingBlockBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleStartingBlockSelectionChanged(e);
                }
            });
            initiatePane.add(p3);
            JPanel p4 = new JPanel();
            p4.setLayout(new FlowLayout());
            p4.add(new JLabel(Bundle.getMessage("DestinationBlockBoxLabel") + ":"));
            p4.add(destinationBlockBox);
            destinationBlockBox.setToolTipText(Bundle.getMessage("DestinationBlockBoxHint"));
            JPanel p4a = new JPanel();
            initiatePane.add(p4);
            p4a.add(trainDetectionLabel);
            initializeTrainDetectionBox();
            p4a.add(trainDetectionComboBox);
            trainDetectionComboBox.setToolTipText(Bundle.getMessage("TrainDetectionBoxHint"));
            initiatePane.add(p4a);
            JPanel p4b = new JPanel();
            p4b.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("AllocateMethodLabel")));
            p4b.setLayout(new FlowLayout());
            allocateMethodButtonGroup.add(allocateAllTheWayRadioButton);
            allocateMethodButtonGroup.add(allocateBySafeRadioButton);
            allocateMethodButtonGroup.add(allocateNumberOfBlocks);
            p4b.add(allocateAllTheWayRadioButton);
            allocateAllTheWayRadioButton.setToolTipText(Bundle.getMessage("AllocateAllTheWayHint"));
            p4b.add(allocateBySafeRadioButton);
            allocateBySafeRadioButton.setToolTipText(Bundle.getMessage("AllocateSafeHint"));
            p4b.add(allocateNumberOfBlocks);
            allocateNumberOfBlocks.setToolTipText(Bundle.getMessage("AllocateMethodHint"));
            allocateAllTheWayRadioButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleAllocateAllTheWayButtonChanged(e);
                }
            });
            allocateBySafeRadioButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleAllocateBySafeButtonChanged(e);
                }
            });
            allocateNumberOfBlocks.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleAllocateNumberOfBlocksButtonChanged(e);
                }
            });
            p4b.add(allocateCustomSpinner);
            allocateCustomSpinner.setToolTipText(Bundle.getMessage("AllocateMethodHint"));
            initiatePane.add(p4b);
            JPanel p6 = new JPanel();
            p6.setLayout(new FlowLayout());
            p6.add(resetWhenDoneBox);
            resetWhenDoneBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleResetWhenDoneClick(e);
                }
            });
            resetWhenDoneBox.setToolTipText(Bundle.getMessage("ResetWhenDoneBoxHint"));
            initiatePane.add(p6);
            JPanel p6a = new JPanel();
            p6a.setLayout(new FlowLayout());
            ((FlowLayout) p6a.getLayout()).setVgap(1);
            p6a.add(delayedReStartLabel);
            p6a.add(delayedReStartBox);
            p6a.add(resetRestartSensorBox);
            resetRestartSensorBox.setToolTipText(Bundle.getMessage("ResetRestartSensorHint"));
            resetRestartSensorBox.setSelected(true);
            delayedReStartBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleResetWhenDoneClick(e);
                }
            });
            delayedReStartBox.setToolTipText(Bundle.getMessage("DelayedReStartHint"));
            initiatePane.add(p6a);

            JPanel p6b = new JPanel();
            p6b.setLayout(new FlowLayout());
            ((FlowLayout) p6b.getLayout()).setVgap(1);
            p6b.add(delayMinLabel);
            p6b.add(delayMinSpinner); // already set to 0
            delayMinSpinner.setToolTipText(Bundle.getMessage("RestartTimedHint"));
            p6b.add(delayReStartSensorLabel);
            p6b.add(delayReStartSensor);
            delayReStartSensor.setAllowNull(true);
            handleResetWhenDoneClick(null);
            initiatePane.add(p6b);

            initiatePane.add(new JSeparator());

            JPanel p10 = new JPanel();
            p10.setLayout(new FlowLayout());
            p10.add(reverseAtEndBox);
            reverseAtEndBox.setToolTipText(Bundle.getMessage("ReverseAtEndBoxHint"));
            initiatePane.add(p10);
            reverseAtEndBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleReverseAtEndBoxClick(e);
                }
            });
            JPanel pDelayReverseRestartDetails = new JPanel();
            pDelayReverseRestartDetails.setLayout(new FlowLayout());
            ((FlowLayout) pDelayReverseRestartDetails.getLayout()).setVgap(1);
            pDelayReverseRestartDetails.add(delayReverseReStartLabel);
            pDelayReverseRestartDetails.add(reverseDelayedRestartType);
            pDelayReverseRestartDetails.add(delayReverseResetSensorBox);
            delayReverseResetSensorBox.setToolTipText(Bundle.getMessage("ReverseResetRestartSensorHint"));
            delayReverseResetSensorBox.setSelected(true);
            reverseDelayedRestartType.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleReverseAtEndBoxClick(e);
                }
            });
            reverseDelayedRestartType.setToolTipText(Bundle.getMessage("ReverseDelayedReStartHint"));
            initiatePane.add(pDelayReverseRestartDetails);

            JPanel pDelayReverseRestartDetails2 = new JPanel();
            pDelayReverseRestartDetails2.setLayout(new FlowLayout());
            ((FlowLayout) pDelayReverseRestartDetails2.getLayout()).setVgap(1);
            pDelayReverseRestartDetails2.add(delayReverseMinLabel);
            pDelayReverseRestartDetails2.add(delayReverseMinSpinner); // already set to 0
            delayReverseMinSpinner.setToolTipText(Bundle.getMessage("ReverseRestartTimedHint"));
            pDelayReverseRestartDetails2.add(delayReverseReStartSensorLabel);
            pDelayReverseRestartDetails2.add(delayReverseReStartSensor);
            delayReverseReStartSensor.setAllowNull(true);
            handleReverseAtEndBoxClick(null);
            initiatePane.add(pDelayReverseRestartDetails2);

            initiatePane.add(new JSeparator());

            JPanel p10a = new JPanel();
            p10a.setLayout(new FlowLayout());
            p10a.add(terminateWhenDoneBox);
            terminateWhenDoneBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleTerminateWhenDoneBoxClick(e);
                }
            });
            initiatePane.add(p10a);
            terminateWhenDoneDetails.setLayout(new FlowLayout());
            terminateWhenDoneDetails.add(nextTrainLabel);
            terminateWhenDoneDetails.add(nextTrain);
            nextTrain.setToolTipText(Bundle.getMessage("TerminateWhenDoneNextTrainHint"));
            initiatePane.add(terminateWhenDoneDetails);
            handleTerminateWhenDoneBoxClick(null);

            initiatePane.add(new JSeparator());

            JPanel p8 = new JPanel();
            p8.setLayout(new FlowLayout());
            p8.add(new JLabel(Bundle.getMessage("PriorityLabel") + ":"));
            p8.add(prioritySpinner); // already set to 5
            prioritySpinner.setToolTipText(Bundle.getMessage("PriorityHint"));
            p8.add(new JLabel("     "));
            p8.add(new JLabel(Bundle.getMessage("TrainTypeBoxLabel")));
            initializeTrainTypeBox();
            p8.add(trainTypeBox);
            trainTypeBox.setSelectedIndex(1);
            trainTypeBox.setToolTipText(Bundle.getMessage("TrainTypeBoxHint"));
            initiatePane.add(p8);
            JPanel p9 = new JPanel();
            p9.setLayout(new FlowLayout());
            p9.add(new JLabel(Bundle.getMessage("DelayedStart")));
            p9.add(delayedStartBox);
            delayedStartBox.setToolTipText(Bundle.getMessage("DelayedStartHint"));
            delayedStartBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleDelayStartClick(e);
                }
            });
            p9.add(departureTimeLabel);
            departureHrSpinner.setEditor(new JSpinner.NumberEditor(departureHrSpinner, "00"));
            p9.add(departureHrSpinner);
            departureHrSpinner.setValue(8);
            departureHrSpinner.setToolTipText(Bundle.getMessage("DepartureTimeHrHint"));
            p9.add(departureSepLabel);
            departureMinSpinner.setEditor(new JSpinner.NumberEditor(departureMinSpinner, "00"));
            p9.add(departureMinSpinner);
            departureMinSpinner.setValue(0);
            departureMinSpinner.setToolTipText(Bundle.getMessage("DepartureTimeMinHint"));
            p9.add(delaySensor);
            delaySensor.setAllowNull(true);
            p9.add(resetStartSensorBox);
            resetStartSensorBox.setToolTipText(Bundle.getMessage("ResetStartSensorHint"));
            resetStartSensorBox.setSelected(true);
            handleDelayStartClick(null);
            initiatePane.add(p9);

            JPanel p11 = new JPanel();
            p11.setLayout(new FlowLayout());
            p11.add(loadAtStartupBox);
            loadAtStartupBox.setToolTipText(Bundle.getMessage("LoadAtStartupBoxHint"));
            loadAtStartupBox.setSelected(false);
            initiatePane.add(p11);

            initiatePane.add(new JSeparator());
            JPanel p5 = new JPanel();
            p5.setLayout(new FlowLayout());
            p5.add(autoRunBox);
            autoRunBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleAutoRunClick(e);
                }
            });
            autoRunBox.setToolTipText(Bundle.getMessage("AutoRunBoxHint"));
            autoRunBox.setSelected(false);
            initiatePane.add(p5);

            initializeAutoRunItems();

            JPanel p7 = new JPanel();
            p7.setLayout(new FlowLayout());
            JButton cancelButton = null;
            p7.add(cancelButton = new JButton(Bundle.getMessage("ButtonCancel")));
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelInitiateTrain(e);
                }
            });
            cancelButton.setToolTipText(Bundle.getMessage("CancelButtonHint"));
            p7.add(addNewTrainButton = new JButton(Bundle.getMessage("ButtonCreate")));
            addNewTrainButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addNewTrain(e);
                }
            });
            addNewTrainButton.setToolTipText(Bundle.getMessage("AddNewTrainButtonHint"));

            JPanel mainPane = new JPanel();
            mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
            JScrollPane scrPane = new JScrollPane(initiatePane);
            mainPane.add(p0);
            mainPane.add(scrPane);
            mainPane.add(p7);
            initiateFrame.setContentPane(mainPane);
            switch (_TrainsFrom) {
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
        setAutoRunDefaults();
        autoRunBox.setSelected(false);
        loadAtStartupBox.setSelected(false);
        initializeFreeTransitsCombo(new ArrayList<Transit>());
        nextTrain.addItem("");
        refreshNextTrainCombo();
        setTrainsFromOptions(_TrainsFrom);
        initiateFrame.pack();
        initiateFrame.setVisible(true);
    }

    private void refreshNextTrainCombo() {
        Object saveEntry = null;
        if (nextTrain.getSelectedIndex() > 0) {
            saveEntry=nextTrain.getSelectedItem();
        }
        nextTrain.removeAll();
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
                trainBoxLabel.setVisible(true);
                rosterComboBox.setVisible(true);
                trainSelectBox.setVisible(false);
                trainFieldLabel.setVisible(false);
                trainNameField.setVisible(true);
                dccAddressFieldLabel.setVisible(false);
                dccAddressSpinner.setVisible(false);
                break;
            case TRAINSFROMOPS:
                initializeFreeTrainsCombo();
                trainBoxLabel.setVisible(true);
                trainSelectBox.setVisible(true);
                rosterComboBox.setVisible(false);
                trainFieldLabel.setVisible(false);
                trainNameField.setVisible(true);
                dccAddressFieldLabel.setVisible(true);
                dccAddressSpinner.setVisible(true);
                setSpeedProfileOptions(false);
                break;
            case TRAINSFROMUSER:
                trainNameField.setText("");
                trainBoxLabel.setVisible(false);
                trainSelectBox.setVisible(false);
                rosterComboBox.setVisible(false);
                trainFieldLabel.setVisible(true);
                trainNameField.setVisible(true);
                dccAddressFieldLabel.setVisible(true);
                dccAddressSpinner.setVisible(true);
                dccAddressSpinner.setEnabled(true);
                setSpeedProfileOptions(false);
                break;
            case TRAINSFROMSETLATER:
            default:
                trainBoxLabel.setVisible(false);
                rosterComboBox.setVisible(false);
                trainSelectBox.setVisible(false);
                trainFieldLabel.setVisible(false);
                trainNameField.setVisible(true);
                dccAddressFieldLabel.setVisible(false);
                dccAddressSpinner.setVisible(false);
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

    private void handleInTransitClick(ActionEvent e) {
        if (!inTransitBox.isSelected() && selectedTransit.getEntryBlocksList().isEmpty()) {
            JmriJOptionPane.showMessageDialog(initiateFrame, Bundle
                    .getMessage("NoEntryBlocks"), Bundle.getMessage("MessageTitle"),
                    JmriJOptionPane.INFORMATION_MESSAGE);
            inTransitBox.setSelected(true);
        }
        initializeStartingBlockCombo();
        initializeDestinationBlockCombo();
        initiateFrame.pack();
    }

 //   private void handleTrainSelectionChanged(ActionEvent e) {
      private void handleTrainSelectionChanged() {
        if (_TrainsFrom != TrainsFrom.TRAINSFROMOPS) {
            return;
        }
        int ix = trainSelectBox.getSelectedIndex();
        if (ix < 1) { // no train selected
            dccAddressSpinner.setEnabled(false);
            return;
        }
        dccAddressSpinner.setEnabled(true);
        int dccAddress;
        try {
            dccAddress = Integer.parseInt((((Train) trainSelectBox.getSelectedItem()).getLeadEngineDccAddress()));
        } catch (NumberFormatException Ex) {
            JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error43"),
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        dccAddressSpinner.setValue (dccAddress);
    }

    private void handleRosterSelectionChanged(ActionEvent e) {
        if (_TrainsFrom != TrainsFrom.TRAINSFROMROSTER) {
            return;
        }
        int ix = rosterComboBox.getSelectedIndex();
        if (ix > 0) { // first item is "Select Loco" string
            RosterEntry r = (RosterEntry) rosterComboBox.getItemAt(ix);
            // check to see if speed profile exists and is not empty
            if (r.getSpeedProfile() == null || r.getSpeedProfile().getProfileSize() < 1) {
                // disable profile boxes etc.
                setSpeedProfileOptions(false);
            } else {
                // enable profile boxes
                setSpeedProfileOptions(true);
            }
            trainNameField.setText(r.titleString());
            if (r.getAttribute("DispatcherTrainType") != null && !r.getAttribute("DispatcherTrainType").equals("")) {
                trainTypeBox.setSelectedItem(r.getAttribute("DispatcherTrainType"));
            }
        } else {
            setSpeedProfileOptions(false);
        }
    }

    private boolean checkResetWhenDone() {
        if ((!reverseAtEndBox.isSelected()) && resetWhenDoneBox.isSelected()
                && (!selectedTransit.canBeResetWhenDone())) {
            resetWhenDoneBox.setSelected(false);
            JmriJOptionPane.showMessageDialog(initiateFrame, Bundle
                    .getMessage("NoResetMessage"), Bundle.getMessage("MessageTitle"),
                    JmriJOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
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

    private void handleResetWhenDoneClick(ActionEvent e) {
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

    private void handleTerminateWhenDoneBoxClick(ActionEvent e) {
        if (terminateWhenDoneBox.isSelected()) {
            refreshNextTrainCombo();
            resetWhenDoneBox.setSelected(false);
            terminateWhenDoneDetails.setVisible(true);
        } else {
            terminateWhenDoneDetails.setVisible(false);
            // leave it
            //nextTrain.setSelectedItem("");
        }
    }
    private void handleReverseAtEndBoxClick(ActionEvent e) {
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

    private void handleAutoRunClick(ActionEvent e) {
        if (autoRunBox.isSelected()) {
            showAutoRunItems();
        } else {
            hideAutoRunItems();
        }
        initiateFrame.pack();
    }

    private void handleStartingBlockSelectionChanged(ActionEvent e) {
        initializeDestinationBlockCombo();
        initiateFrame.pack();
    }

    private void handleAllocateAllTheWayButtonChanged(ActionEvent e) {
        allocateCustomSpinner.setVisible(false);
    }

    private void handleAllocateBySafeButtonChanged(ActionEvent e) {
        allocateCustomSpinner.setVisible(false);
    }

    private void handleAllocateNumberOfBlocksButtonChanged(ActionEvent e) {
        allocateCustomSpinner.setVisible(true);
    }

    private void cancelInitiateTrain(ActionEvent e) {
        _dispatcher.newTrainDone(null);
    }

    /**
     * Handles press of "Add New Train" button by edit-checking populated values
     * then (if no errors) creating an ActiveTrain and (optionally) an
     * AutoActiveTrain
     */
    private void addNewTrain(ActionEvent e) {
        // get information
        if (selectedTransit == null) {
            // no transits available
            JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error15"),
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            cancelInitiateTrain(null);
            return;
        }
        String transitName = selectedTransit.getDisplayName();
        String trainName = "";
        int index = startingBlockBox.getSelectedIndex();
        if (index < 0) {
            return;
        }
        String startBlockName = startingBlockBoxList.get(index).getDisplayName();
        int startBlockSeq = startingBlockSeqList.get(index).intValue();
        index = destinationBlockBox.getSelectedIndex();
        if (index < 0) {
            return;
        }
        String endBlockName = destinationBlockBoxList.get(index).getDisplayName();
        int endBlockSeq = destinationBlockSeqList.get(index).intValue();
        boolean autoRun = autoRunBox.isSelected();
        if (!checkResetWhenDone()) {
            return;
        }
        boolean resetWhenDone = resetWhenDoneBox.isSelected();
        boolean reverseAtEnd = reverseAtEndBox.isSelected();
        index = trainDetectionComboBox.getSelectedIndex();
        if (index < 0) {
            return;
        }
        TrainDetection trainDetection = ((TrainDetectionItem)trainDetectionComboBox.getSelectedItem()).value;
        int allocateMethod = 3;
        if (allocateAllTheWayRadioButton.isSelected()) {
            allocateMethod = ActiveTrain.ALLOCATE_AS_FAR_AS_IT_CAN;
        } else if (allocateBySafeRadioButton.isSelected()) {
            allocateMethod = ActiveTrain.ALLOCATE_BY_SAFE_SECTIONS;
        } else {
            allocateMethod = (Integer) allocateCustomSpinner.getValue();
        }
        int delayedStart = delayModeFromBox(delayedStartBox);
        int delayedReStart = delayModeFromBox(delayedReStartBox);
        int delayedReverseReStart = delayModeFromBox(reverseDelayedRestartType);
        int departureTimeHours = 8;
        departureTimeHours = (Integer) departureHrSpinner.getValue();
        int departureTimeMinutes = 8;
        departureTimeMinutes = (Integer) departureMinSpinner.getValue();
        int delayRestartMinutes = 0;
        delayRestartMinutes = (Integer) delayMinSpinner.getValue();
        if ((delayRestartMinutes < 0)) {
            JmriJOptionPane.showMessageDialog(initiateFrame, delayMinSpinner.getValue(),
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            log.warn("Range error in Delay Restart Time Minutes field");
            return;
        }
        int delayReverseRestartMinutes = 0;
        delayReverseRestartMinutes = (Integer) delayReverseMinSpinner.getValue();
        if ((delayReverseRestartMinutes < 0)) {
            JmriJOptionPane.showMessageDialog(initiateFrame, delayReverseMinSpinner.getValue(),
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            log.warn("Range error in Reverse Delay Restart Time Minutes field");
            return;
        }
        int tSource = 0;
        String dccAddress = "unknown";
        switch (_TrainsFrom) {
            case TRAINSFROMROSTER:
                index = rosterComboBox.getSelectedIndex();
                if (index < 1) { // first item is the "Select Loco" message
                    // no train selected
                    JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error41"),
                            Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }
                RosterEntry r = (RosterEntry) rosterComboBox.getSelectedItem();
                dccAddress = r.getDccAddress();
                if (trainNameField.getText().isEmpty()) {
                    JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                            "Error45", dccAddress), Bundle.getMessage("ErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }
                trainName=trainNameField.getText();
                if (!_dispatcher.isTrainFree(trainName)) {
                    // train name is already in use by an Active Train
                    JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                            "Error24", trainName), Bundle.getMessage("ErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }
                trainName = trainNameField.getText();
                if (!isAddressFree(r.getDccLocoAddress().getNumber())) {
                    // DCC address is already in use by an Active Train
                    JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                            "Error40", dccAddress), Bundle.getMessage("ErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }

                tSource = ActiveTrain.ROSTER;

                if (trainTypeBox.getSelectedIndex() != 0 &&
                        (r.getAttribute("DispatcherTrainType") == null ||
                                !r.getAttribute("DispatcherTrainType").equals("" + trainTypeBox.getSelectedItem()))) {
                    r.putAttribute("DispatcherTrainType", "" + trainTypeBox.getSelectedItem());
                    r.updateFile();
                    Roster.getDefault().writeRoster();
                }
                break;
            case TRAINSFROMOPS:
                tSource = ActiveTrain.OPERATIONS;
                index = trainSelectBox.getSelectedIndex();
                if (index < 1) { // first item is Select Train
                    // Train not selected
                    JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error42"),
                            Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (trainNameField.getText().isEmpty()) {
                    JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                            "Error45", dccAddress), Bundle.getMessage("ErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!_dispatcher.isTrainFree(trainName)) {
                    // train name is already in use by an Active Train
                    JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                            "Error24", trainName), Bundle.getMessage("ErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }
                trainName = trainSelectBox.getSelectedItem().toString();
                dccAddress = getDCCAddressFromSpinner();
                if (dccAddress == null) {
                    return;
                }
                break;
            case TRAINSFROMUSER:
                trainName = trainNameField.getText();
                if ((trainName == null) || trainName.equals("")) {
                    // no train name entered
                    JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error14"),
                            Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (trainNameField.getText().isEmpty()) {
                    JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                            "Error45", dccAddress), Bundle.getMessage("ErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!_dispatcher.isTrainFree(trainName)) {
                    // train name is already in use by an Active Train
                    JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                            "Error24", trainName), Bundle.getMessage("ErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }
                dccAddress = getDCCAddressFromSpinner();
                if (dccAddress == null) {
                    return;
                }
                tSource = ActiveTrain.USER;
                break;
            case TRAINSFROMSETLATER:
            default:
                trainName = "";
        }
        int priority = 5;
        priority = (Integer) prioritySpinner.getValue();
        int trainType = trainTypeBox.getSelectedIndex();
        if (autoRunBox.isSelected()) {
            if (!readAutoRunItems()) {
                return;
            }
        }

        // create a new Active Train
        ActiveTrain at = _dispatcher.createActiveTrain(transitName, trainName, tSource, startBlockName,
                startBlockSeq, endBlockName, endBlockSeq, autoRun, dccAddress, priority,
                resetWhenDone, reverseAtEnd,  true, initiateFrame, allocateMethod);
        if (at == null) {
            return;  // error message sent by createActiveTrain
        }
        if (tSource == ActiveTrain.ROSTER) {
            at.setRosterEntry((RosterEntry)rosterComboBox.getSelectedItem());
        }
        at.setTrainDetection(trainDetection);
        at.setAllocateMethod(allocateMethod);
        at.setDelayedStart(delayedStart);
        at.setDelayedRestart(delayedReStart);
        at.setDepartureTimeHr(departureTimeHours);
        at.setDepartureTimeMin(departureTimeMinutes);
        at.setRestartDelay(delayRestartMinutes);
        at.setDelaySensor(delaySensor.getSelectedItem());
        at.setReverseDelayRestart(delayedReverseReStart);
        at.setReverseRestartDelay(delayReverseRestartMinutes);
        at.setReverseDelaySensor(delayReverseReStartSensor.getSelectedItem());
        at.setReverseResetRestartSensor(delayReverseResetSensorBox.isSelected());
        if ((_dispatcher.isFastClockTimeGE(departureTimeHours, departureTimeMinutes) && delayedStart != ActiveTrain.SENSORDELAY)
                || delayedStart == ActiveTrain.NODELAY) {
            at.setStarted();
        }
        at.setRestartSensor(delayReStartSensor.getSelectedItem());
        at.setResetRestartSensor(resetRestartSensorBox.isSelected());
        at.setTrainType(trainType);
        at.setTerminateWhenDone(terminateWhenDoneBox.isSelected());
        at.setNextTrain(_nextTrain);
        if (autoRunBox.isSelected()) {
            AutoActiveTrain aat = new AutoActiveTrain(at);
            setAutoRunItems(aat);
            _dispatcher.getAutoTrainsFrame().addAutoActiveTrain(aat);
            if (!aat.initialize()) {
                JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                        "Error27", at.getTrainName()), Bundle.getMessage("MessageTitle"),
                        JmriJOptionPane.INFORMATION_MESSAGE);
            }
        }
        _dispatcher.allocateNewActiveTrain(at);
        _dispatcher.newTrainDone(at);
    }

    private String getDCCAddressFromSpinner() {
        int address = (Integer) dccAddressSpinner.getValue(); // SpinnerNumberModel
                                                          // limits
                                                          // address to
                                                          // 1 - 9999
                                                          // inclusive
        if (!isAddressFree(address)) {
            // DCC address is already in use by an Active Train
            JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                    "Error40", address), Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
            return null;
        }
        return String.valueOf(address);
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
        rosterComboBox.update();
        // remove used entries
        for (int ix = rosterComboBox.getItemCount() - 1; ix > 1; ix--) {  // remove from back first item is the "select loco" message
            if ( !isAddressFree( ((RosterEntry)rosterComboBox.getItemAt(ix)).getDccLocoAddress().getNumber() ) ) {
                rosterComboBox.removeItemAt(ix);
            }
        }
    }

    private void initializeFreeTrainsCombo() {
        ActionListener[] als = trainSelectBox.getActionListeners();
        for ( ActionListener al: als) {
            trainSelectBox.removeActionListener(al);
        }
        trainSelectBox.removeAllItems();
        trainSelectBox.addItem("Select Train");
        // initialize free trains from operations
        List<Train> trains = jmri.InstanceManager.getDefault(TrainManager.class).getTrainsByNameList();
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
        for ( ActionListener al: als) {
            trainSelectBox.addActionListener(al);
        }
    }

    /**
     * Sets the labels and inputs for speed profile running
     * @param b True if the roster entry has valid speed profile else false
     */
    private void setSpeedProfileOptions(boolean b) {
        useSpeedProfileLabel.setEnabled(b);
        useSpeedProfileCheckBox.setEnabled(b);
        stopBySpeedProfileLabel.setEnabled(b);
        stopBySpeedProfileCheckBox.setEnabled(b);
        stopBySpeedProfileAdjustLabel.setEnabled(b);
        stopBySpeedProfileAdjustSpinner.setEnabled(b);
        if (!b) {
            useSpeedProfileCheckBox.setSelected(false);
            stopBySpeedProfileCheckBox.setSelected(false);
            _useSpeedProfile = false;
            _stopBySpeedProfile = false;
        }
    }


    private boolean isAddressFree(int addr) {
        for (int j = 0; j < _ActiveTrainsList.size(); j++) {
            ActiveTrain at = _ActiveTrainsList.get(j);
            if (addr == Integer.parseInt(at.getDccAddress())) {
                return false;
            }
        }
        return true;
    }

    private void initializeStartingBlockCombo() {
        startingBlockBox.removeAllItems();
        startingBlockBoxList.clear();
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
        JComboBoxUtil.setupComboBoxMaxRows(startingBlockBox);
    }

    private void initializeDestinationBlockCombo() {
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
            initializeFreeTransitsCombo(new ArrayList<Transit>());
            initiateFrame.setVisible(true);
        } else {
            _dispatcher.newTrainDone(null);
        }
    }

    public void showActivateFrame(RosterEntry re) {
        showActivateFrame();
    }

    private void loadTrainInfo(ActionEvent e) {
        String[] names = _tiFile.getTrainInfoFileNames();
        TrainInfo info = null;
        if (names.length > 0) {
            //prompt user to select a single train info filename from directory list
            Object selName = JmriJOptionPane.showInputDialog(initiateFrame,
                    Bundle.getMessage("LoadTrainChoice"), Bundle.getMessage("LoadTrainTitle"),
                    JmriJOptionPane.QUESTION_MESSAGE, null, names, names[0]);
            if ((selName == null) || (((String) selName).equals(""))) {
                return;
            }
            //read xml data from selected filename and move it into the new train dialog box
            _trainInfoName = (String) selName;
            try {
                info = _tiFile.readTrainInfo((String) selName);
                if (info != null) {
                    // process the information just read
                    trainInfoToDialog(info);
                }
            } catch (java.io.IOException ioe) {
                log.error("IO Exception when reading train info file", ioe);
            } catch (org.jdom2.JDOMException jde) {
                log.error("JDOM Exception when reading train info file", jde);
            }
        }
        handleDelayStartClick(null);
        handleReverseAtEndBoxClick(null);
    }

    private void saveTrainInfo(ActionEvent e, boolean locoOptional) {
        TrainInfo info = null;
        try {
            info = dialogToTrainInfo(locoOptional);
        } catch (IllegalArgumentException ide) {
            JmriJOptionPane.showMessageDialog(initiateFrame, ide.getMessage(),
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        // get file name
        String eName = "";
        eName = JmriJOptionPane.showInputDialog(initiateFrame,
                Bundle.getMessage("EnterFileName") + " :", _trainInfoName);
        if (eName == null) {  //Cancel pressed
            return;
        }
        if (eName.length() < 1) {
            JmriJOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error25"),
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        String fileName = normalizeXmlFileName(eName);
        _trainInfoName = fileName;
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
            _tiFile.writeTrainInfo(info, fileName);
        } //catch (org.jdom2.JDOMException jde) {
        // log.error("JDOM exception writing Train Info: "+jde);
        //}
        catch (java.io.IOException ioe) {
            log.error("IO exception writing Train Info", ioe);
        }
    }

    private void deleteTrainInfo(ActionEvent e) {
        String[] names = _tiFile.getTrainInfoFileNames();
        if (names.length > 0) {
            Object selName = JmriJOptionPane.showInputDialog(initiateFrame,
                    Bundle.getMessage("DeleteTrainChoice"), Bundle.getMessage("DeleteTrainTitle"),
                    JmriJOptionPane.QUESTION_MESSAGE, null, names, names[0]);
            if ((selName == null) || (((String) selName).equals(""))) {
                return;
            }
            _tiFile.deleteTrainInfoFile((String) selName);
        }
    }

    private void trainInfoToDialog(TrainInfo info) {
        try {
            transitSelectBox.setSelectedItemByName(info.getTransitName());
        } catch (Exception ex) {
            log.warn("Transit {} from file not in Transit menu", info.getTransitName());
            JmriJOptionPane.showMessageDialog(initiateFrame,
                    Bundle.getMessage("TransitWarn", info.getTransitName()),
                    null, JmriJOptionPane.WARNING_MESSAGE);
        }
        _TrainsFrom = info.getTrainsFrom();
        switch (_TrainsFrom) {
            case TRAINSFROMROSTER:
                radioTrainsFromRoster.setSelected(true);
                if (!setRosterComboBox(rosterComboBox, info.getRosterId())) {
                    log.warn("Roster {} from file not in Roster Combo", info.getRosterId());
                    JmriJOptionPane.showMessageDialog(initiateFrame,
                            Bundle.getMessage("TrainWarn", info.getRosterId()),
                            null, JmriJOptionPane.WARNING_MESSAGE);
                }
                break;
            case TRAINSFROMOPS:
                radioTrainsFromOps.setSelected(true);
                if (!setTrainComboBox(trainSelectBox, info.getTrainName())) {
                    log.warn("Train {} from file not in Train Combo", info.getTrainName());
                    JmriJOptionPane.showMessageDialog(initiateFrame,
                            Bundle.getMessage("TrainWarn", info.getTrainName()),
                            null, JmriJOptionPane.WARNING_MESSAGE);
                }
                break;
            case TRAINSFROMUSER:
                radioTrainsFromUser.setSelected(true);
                dccAddressSpinner.setValue(Integer.parseInt(info.getDccAddress()));
                break;
            case TRAINSFROMSETLATER:
            default:
                radioTrainsFromSetLater.setSelected(true);
        }
        trainNameField.setText(info.getTrainUserName());
        trainDetectionComboBox.setSelectedItemByValue(info.getTrainDetection());
        inTransitBox.setSelected(info.getTrainInTransit());
        initializeStartingBlockCombo();
        initializeDestinationBlockCombo();
        setComboBox(startingBlockBox, info.getStartBlockName());
        setComboBox(destinationBlockBox, info.getDestinationBlockName());
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
        handleTerminateWhenDoneBoxClick(null);
        setComboBox(trainTypeBox, info.getTrainType());
        autoRunBox.setSelected(info.getAutoRun());
        loadAtStartupBox.setSelected(info.getLoadAtStartup());
        setAllocateMethodButtons(info.getAllocationMethod());
        autoTrainInfoToDialog(info);
    }

    private TrainInfo dialogToTrainInfo(boolean locoOptional) throws IllegalArgumentException {
        TrainInfo info = new TrainInfo();
        int index = transitSelectBox.getSelectedIndex();
        if (index < 0) {
            throw new IllegalArgumentException(Bundle.getMessage("Error44"));
        } else {
            info.setTransitName(transitSelectBox.getSelectedItem().getDisplayName());
            info.setTransitId(transitSelectBox.getSelectedItem().getDisplayName());
        }
        switch (_TrainsFrom) {
            case TRAINSFROMROSTER:
                if (rosterComboBox.getSelectedIndex() < 1 ) {
                    throw new IllegalArgumentException(Bundle.getMessage("Error41"));
                }
                info.setRosterId(((RosterEntry) rosterComboBox.getSelectedItem()).getId());
                info.setDccAddress(((RosterEntry) rosterComboBox.getSelectedItem()).getDccAddress());
                break;
            case TRAINSFROMOPS:
                if (trainSelectBox.getSelectedIndex() < 1) {
                    throw new IllegalArgumentException(Bundle.getMessage("Error42"));
                }
                info.setTrainName(((Train) trainSelectBox.getSelectedItem()).getId());
                info.setDccAddress(String.valueOf(dccAddressSpinner.getValue()));
                break;
            case TRAINSFROMUSER:
                if (trainNameField.getText().isEmpty()) {
                    throw new IllegalArgumentException(Bundle.getMessage("Error22"));
                }
                info.setDccAddress(String.valueOf(dccAddressSpinner.getValue()));
                break;
            case TRAINSFROMSETLATER:
            default:
                info.setTrainName("");
                info.setDccAddress("");
        }
        info.setTrainUserName(trainNameField.getText());
        info.setTrainInTransit(inTransitBox.isSelected());
        info.setStartBlockName((String) startingBlockBox.getSelectedItem());
        index = startingBlockBox.getSelectedIndex();
        if (index < 0) {
            throw new IllegalArgumentException(Bundle.getMessage("Error13"));
        } else {
            info.setStartBlockId(startingBlockBoxList.get(index).getDisplayName());
            info.setStartBlockSeq(startingBlockSeqList.get(index).intValue());
        }
        info.setDestinationBlockName((String) destinationBlockBox.getSelectedItem());
        index = destinationBlockBox.getSelectedIndex();
        if (index < 0) {
            throw new IllegalArgumentException(Bundle.getMessage("Error8"));
        } else {
            info.setDestinationBlockId(destinationBlockBoxList.get(index).getDisplayName());
            info.setDestinationBlockSeq(destinationBlockSeqList.get(index).intValue());
        }
        info.setTrainsFrom(_TrainsFrom);
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
        return info;
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
    private final JLabel maxSpeedLabel = new JLabel(Bundle.getMessage("MaxSpeedLabel"));
    private final JSpinner maxSpeedSpinner = new JSpinner();
    private final JPanel pa2 = new JPanel();
    private final JLabel rampRateLabel = new JLabel(Bundle.getMessage("RampRateBoxLabel"));
    private final JComboBox<String> rampRateBox = new JComboBox<>();
    private final JPanel pa2a = new JPanel();
    private final JLabel useSpeedProfileLabel = new JLabel(Bundle.getMessage("UseSpeedProfileLabel"));
    private final JCheckBox useSpeedProfileCheckBox = new JCheckBox( );
    private final JLabel stopBySpeedProfileLabel = new JLabel(Bundle.getMessage("StopBySpeedProfileLabel"));
    private final JCheckBox stopBySpeedProfileCheckBox = new JCheckBox( );
    private final JLabel stopBySpeedProfileAdjustLabel = new JLabel(Bundle.getMessage("StopBySpeedProfileAdjustLabel"));
    private final JSpinner stopBySpeedProfileAdjustSpinner = new JSpinner();
    private final JPanel pa3 = new JPanel();
    private final JCheckBox soundDecoderBox = new JCheckBox(Bundle.getMessage("SoundDecoder"));
    private final JCheckBox runInReverseBox = new JCheckBox(Bundle.getMessage("RunInReverse"));
    private final JPanel pa4 = new JPanel();
    protected static class TrainDetectionJCombo extends JComboBox<TrainDetectionItem> {
        public void setSelectedItemByValue(TrainDetection var) {
            for ( int ix = 0; ix < getItemCount() ; ix ++ ) {
                if (getItemAt(ix).value == var) {
                    this.setSelectedIndex(ix);
                    break;
                }
            }
        }
    }
    public final TrainDetectionJCombo trainDetectionComboBox
                = new TrainDetectionJCombo();
    private final JLabel trainDetectionLabel = new JLabel(Bundle.getMessage("TrainDetection"));
    private final JLabel trainLengthLabel = new JLabel(Bundle.getMessage("MaxTrainLengthLabel"));
    private final JSpinner maxTrainLengthSpinner = new JSpinner(); // initialized later
    // auto run variables
    float _speedFactor = 1.0f;
    float _maxSpeed = 0.6f;
    int _rampRate = AutoActiveTrain.RAMP_NONE;
    TrainDetection _trainDetection = TrainDetection.TRAINDETECTION_HEADONLY;
    boolean _runInReverse = false;
    boolean _soundDecoder = false;
    float _maxTrainLength = 200.0f;
    boolean _stopBySpeedProfile = false;
    float _stopBySpeedProfileAdjust = 1.0f;
    boolean _useSpeedProfile = true;
    String _nextTrain = "";

    private void setAutoRunDefaults() {
        _speedFactor = 1.0f;
        _maxSpeed = 0.6f;
        _rampRate = AutoActiveTrain.RAMP_NONE;
        _runInReverse = false;
        _soundDecoder = false;
        _maxTrainLength = 100.0f;
        _stopBySpeedProfile = false;
        _stopBySpeedProfileAdjust = 1.0f;
        _useSpeedProfile = true;
        _nextTrain = "";

    }

    private void initializeAutoRunItems() {
        initializeRampCombo();
        pa1.setLayout(new FlowLayout());
        pa1.add(speedFactorLabel);
        speedFactorSpinner.setModel(new SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(2.0f), Float.valueOf(0.01f)));
        speedFactorSpinner.setEditor(new JSpinner.NumberEditor(speedFactorSpinner, "# %"));
        pa1.add(speedFactorSpinner);
        speedFactorSpinner.setToolTipText(Bundle.getMessage("SpeedFactorHint"));
        pa1.add(new JLabel("   "));
        pa1.add(maxSpeedLabel);
        maxSpeedSpinner.setModel(new SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(2.0f), Float.valueOf(0.01f)));
        maxSpeedSpinner.setEditor(new JSpinner.NumberEditor(maxSpeedSpinner, "# %"));
        pa1.add(maxSpeedSpinner);
        maxSpeedSpinner.setToolTipText(Bundle.getMessage("MaxSpeedHint"));
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
        pa3.setLayout(new FlowLayout());
        pa3.add(soundDecoderBox);
        soundDecoderBox.setToolTipText(Bundle.getMessage("SoundDecoderBoxHint"));
        pa3.add(new JLabel("   "));
        pa3.add(runInReverseBox);
        runInReverseBox.setToolTipText(Bundle.getMessage("RunInReverseBoxHint"));
        initiatePane.add(pa3);
        pa4.setLayout(new FlowLayout());
        pa4.add(trainLengthLabel);
        maxTrainLengthSpinner.setModel(new SpinnerNumberModel(Float.valueOf(18.0f), Float.valueOf(0.0f), Float.valueOf(10000.0f), Float.valueOf(0.5f)));
        maxTrainLengthSpinner.setEditor(new JSpinner.NumberEditor(maxTrainLengthSpinner, "###0.0"));
        pa4.add(maxTrainLengthSpinner);
        boolean unitIsMeter = InstanceManager.getDefault(DispatcherFrame.class).getUseScaleMeters(); // read from user setting
        maxTrainLengthSpinner.setToolTipText(Bundle.getMessage("MaxTrainLengthHint",
                (unitIsMeter ? Bundle.getMessage("ScaleMeters") : Bundle.getMessage("ScaleFeet")))); // won't be updated while Dispatcher is open
        initiatePane.add(pa4);
        hideAutoRunItems();   // initialize with auto run items hidden
        initializeAutoRunValues();
    }

    private void initializeAutoRunValues() {
        speedFactorSpinner.setValue(_speedFactor);
        maxSpeedSpinner.setValue(_maxSpeed);
        rampRateBox.setSelectedIndex(_rampRate);
        soundDecoderBox.setSelected(_soundDecoder);
        runInReverseBox.setSelected(_runInReverse);
        useSpeedProfileCheckBox.setSelected(_useSpeedProfile);
        stopBySpeedProfileAdjustSpinner.setValue(_stopBySpeedProfileAdjust);
        stopBySpeedProfileCheckBox.setSelected(_stopBySpeedProfile);
        maxTrainLengthSpinner.setValue(Math.round(_maxTrainLength * 2) * 0.5f); // set in spinner as 0.5 increments

    }

    private void hideAutoRunItems() {
        pa1.setVisible(false);
        pa2.setVisible(false);
        pa2a.setVisible(false);
        pa3.setVisible(false);
        pa4.setVisible(false);
    }

    private void showAutoRunItems() {
        pa1.setVisible(true);
        pa2.setVisible(true);
        pa2a.setVisible(true);
        pa3.setVisible(true);
        pa4.setVisible(true);
    }

    private void autoTrainInfoToDialog(TrainInfo info) {
        speedFactorSpinner.setValue(info.getSpeedFactor());
        maxSpeedSpinner.setValue(info.getMaxSpeed());
        setComboBox(rampRateBox, info.getRampRate());
        trainDetectionComboBox.setSelectedItemByValue(info.getTrainDetection());
        runInReverseBox.setSelected(info.getRunInReverse());
        soundDecoderBox.setSelected(info.getSoundDecoder());
        maxTrainLengthSpinner.setValue(info.getMaxTrainLength());
        useSpeedProfileCheckBox.setSelected(info.getUseSpeedProfile());
        stopBySpeedProfileCheckBox.setSelected(info.getStopBySpeedProfile());
        stopBySpeedProfileAdjustSpinner.setValue(info.getStopBySpeedProfileAdjust());
        if (autoRunBox.isSelected()) {
            showAutoRunItems();
        } else {
            hideAutoRunItems();
        }
        initiateFrame.pack();
    }

    private void autoRunItemsToTrainInfo(TrainInfo info) {
        info.setSpeedFactor((float) speedFactorSpinner.getValue());
        info.setMaxSpeed((float) maxSpeedSpinner.getValue());
        info.setRampRate((String) rampRateBox.getSelectedItem());
        info.setRunInReverse(runInReverseBox.isSelected());
        info.setSoundDecoder(soundDecoderBox.isSelected());
        info.setMaxTrainLength((float) maxTrainLengthSpinner.getValue());
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
    }

    private boolean readAutoRunItems() {
        boolean success = true;
        _speedFactor = (float) speedFactorSpinner.getValue();
        _maxSpeed = (float) maxSpeedSpinner.getValue();
        _rampRate = rampRateBox.getSelectedIndex();
        _trainDetection = ((TrainDetectionItem)trainDetectionComboBox.getSelectedItem()).value;
        _runInReverse = runInReverseBox.isSelected();
        _soundDecoder = soundDecoderBox.isSelected();
        _maxTrainLength = (float) maxTrainLengthSpinner.getValue();
        _useSpeedProfile = useSpeedProfileCheckBox.isSelected();
        _stopBySpeedProfile = stopBySpeedProfileCheckBox.isSelected();
        if (_stopBySpeedProfile) {
            _stopBySpeedProfileAdjust = (Float) stopBySpeedProfileAdjustSpinner.getValue();
        }
        if (nextTrain.getSelectedIndex() < 0) {
            _nextTrain="";
        } else {
            _nextTrain = (String)nextTrain.getSelectedItem();
        }
        return success;
    }

    private void setAutoRunItems(AutoActiveTrain aaf) {
        aaf.setSpeedFactor(_speedFactor);
        aaf.setMaxSpeed(_maxSpeed);
        aaf.setRampRate(_rampRate);
        aaf.setRunInReverse(_runInReverse);
        aaf.setSoundDecoder(_soundDecoder);
        aaf.setMaxTrainLength(_maxTrainLength);
        aaf.setStopBySpeedProfile(_stopBySpeedProfile);
        aaf.setStopBySpeedProfileAdjust(_stopBySpeedProfileAdjust);
        aaf.setUseSpeedProfile(_useSpeedProfile);
    }

    private void initializeRampCombo() {
        rampRateBox.removeAllItems();
        rampRateBox.addItem(Bundle.getMessage("RAMP_NONE"));
        rampRateBox.addItem(Bundle.getMessage("RAMP_FAST"));
        rampRateBox.addItem(Bundle.getMessage("RAMP_MEDIUM"));
        rampRateBox.addItem(Bundle.getMessage("RAMP_MED_SLOW"));
        rampRateBox.addItem(Bundle.getMessage("RAMP_SLOW"));
        rampRateBox.addItem(Bundle.getMessage("RAMP_SPEEDPROFILE"));
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
     * ComboBox item.
     */
    protected static class TrainDetectionItem {
        private String key;
        private TrainDetection value;
        public TrainDetectionItem(String text, TrainDetection trainDetection ) {
            this.key = text;
            this.value = trainDetection;
        }
        @Override
        public String toString()
        {
            return key;
        }
        public String getKey()
        {
            return key;
        }
        public TrainDetection getValue()
        {
            return value;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActivateTrainFrame.class);

}
