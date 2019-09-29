package jmri.jmrit.dispatcher;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.Transit;
import jmri.TransitManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;

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
public class ActivateTrainFrame {

    public ActivateTrainFrame(DispatcherFrame d) {
        _dispatcher = d;
        _tiFile = new TrainInfoFile();
    }

    // operational instance variables
    private DispatcherFrame _dispatcher = null;
    private TrainInfoFile _tiFile = null;
    private boolean _TrainsFromUser = false;
    private boolean _TrainsFromRoster = true;
    private boolean _TrainsFromOperations = false;
    private List<ActiveTrain> _ActiveTrainsList = null;
    private final TransitManager _TransitManager = InstanceManager.getDefault(jmri.TransitManager.class);
    private String _trainInfoName = "";

    // initiate train window variables
    private Transit selectedTransit = null;
    //private String selectedTrain = "";
    private JmriJFrame initiateFrame = null;
    private JPanel initiatePane = null;
    private final JComboBox<String> transitSelectBox = new JComboBox<>();
    private final List<Transit> transitBoxList = new ArrayList<>();
    private final JLabel trainBoxLabel = new JLabel("     " + Bundle.getMessage("TrainBoxLabel") + ":");
    private final JComboBox<String> trainSelectBox = new JComboBox<>();
    private final List<RosterEntry> trainBoxList = new ArrayList<>();
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
    private final JRadioButton allocateBySafeRadioButton = new JRadioButton(Bundle.getMessage("ToSafeSections"));
    private final JRadioButton allocateAllTheWayRadioButton = new JRadioButton(Bundle.getMessage("AsFarAsPos"));
    private final JRadioButton allocateNumberOfBlocks = new JRadioButton(Bundle.getMessage("NumberOfBlocks") + ":");
    private final ButtonGroup allocateMethodButtonGroup = new ButtonGroup();
    private final JSpinner allocateCustomSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
    private final JCheckBox terminateWhenDoneBox = new JCheckBox(Bundle.getMessage("TerminateWhenDone"));
    private final JSpinner prioritySpinner = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
    private final JCheckBox resetWhenDoneBox = new JCheckBox(Bundle.getMessage("ResetWhenDone"));
    private final JCheckBox reverseAtEndBox = new JCheckBox(Bundle.getMessage("ReverseAtEnd"));
    int delayedStartInt[] = new int[]{ActiveTrain.NODELAY, ActiveTrain.TIMEDDELAY, ActiveTrain.SENSORDELAY};
    String delayedStartString[] = new String[]{Bundle.getMessage("DelayedStartNone"), Bundle.getMessage("DelayedStartTimed"), Bundle.getMessage("DelayedStartSensor")};
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
        if (_TrainsFromRoster && re != null) {
            setComboBox(trainSelectBox, re.getId());
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
        _TrainsFromRoster = _dispatcher.getTrainsFromRoster();
        _TrainsFromOperations = _dispatcher.getTrainsFromTrains();
        _TrainsFromUser = _dispatcher.getTrainsFromUser();
        _ActiveTrainsList = _dispatcher.getActiveTrainsList();
        // create window if needed
        if (initiateFrame == null) {
            initiateFrame = new JmriJFrame(Bundle.getMessage("AddTrainTitle"), false, true);
            initiateFrame.addHelpMenu("package.jmri.jmrit.dispatcher.NewTrain", true);
            
            initiatePane = new JPanel();
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
                    saveTrainInfo(e);
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
            trainSelectBox.setToolTipText(Bundle.getMessage("TrainBoxHint"));
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
            initiatePane.add(p4);
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
            JPanel p10a = new JPanel();
            p10a.setLayout(new FlowLayout());
            p10a.add(terminateWhenDoneBox);
            initiatePane.add(p10a);

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
            
        }
        if (_TrainsFromRoster || _TrainsFromOperations) {
            trainBoxLabel.setVisible(true);
            trainSelectBox.setVisible(true);
            trainFieldLabel.setVisible(false);
            trainNameField.setVisible(false);
            dccAddressFieldLabel.setVisible(false);
            dccAddressSpinner.setVisible(false);
        } else if (_TrainsFromUser) {
            trainNameField.setText("");
            trainBoxLabel.setVisible(false);
            trainSelectBox.setVisible(false);
            trainFieldLabel.setVisible(true);
            trainNameField.setVisible(true);
            dccAddressFieldLabel.setVisible(true);
            dccAddressSpinner.setVisible(true);
        }
        setAutoRunDefaults();
        autoRunBox.setSelected(false);
        loadAtStartupBox.setSelected(false);
        initializeFreeTransitsCombo(new ArrayList<Transit>());
        initializeFreeTrainsCombo();
        initiateFrame.pack();
        initiateFrame.setVisible(true);
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

    private void handleTransitSelectionChanged(ActionEvent e) {
        int index = transitSelectBox.getSelectedIndex();
        if (index < 0) {
            return;
        }
        Transit t = transitBoxList.get(index);
        if ((t != null) && (t != selectedTransit)) {
            selectedTransit = t;
            initializeStartingBlockCombo();
            initializeDestinationBlockCombo();
            initiateFrame.pack();
        }
    }

    private void handleInTransitClick(ActionEvent e) {
        if (!inTransitBox.isSelected() && selectedTransit.getEntryBlocksList().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(initiateFrame, Bundle
                    .getMessage("NoEntryBlocks"), Bundle.getMessage("MessageTitle"),
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            inTransitBox.setSelected(true);
        }
        initializeStartingBlockCombo();
        initializeDestinationBlockCombo();
        initiateFrame.pack();
    }

    private boolean checkResetWhenDone() {
        if ((!reverseAtEndBox.isSelected()) && resetWhenDoneBox.isSelected()
                && (!selectedTransit.canBeResetWhenDone())) {
            resetWhenDoneBox.setSelected(false);
            javax.swing.JOptionPane.showMessageDialog(initiateFrame, Bundle
                    .getMessage("NoResetMessage"), Bundle.getMessage("MessageTitle"),
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
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
            if (delayedReStartBox.getSelectedItem().equals(Bundle.getMessage("DelayedStartTimed"))) {
                delayMinSpinner.setVisible(true);
                delayMinLabel.setVisible(true);
            } else if (delayedReStartBox.getSelectedItem().equals(Bundle.getMessage("DelayedStartSensor"))) {
                delayReStartSensor.setVisible(true);
                delayReStartSensorLabel.setVisible(true);
                resetRestartSensorBox.setVisible(true);
            }
        }
        handleReverseAtEndBoxClick(e);
        initiateFrame.pack();
    }

    private void handleReverseAtEndBoxClick(ActionEvent e) {
        if (reverseAtEndBox.isSelected() || resetWhenDoneBox.isSelected()) {
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
        initiateFrame.setVisible(false);
        initiateFrame.dispose();  // prevent this window from being listed in the Window menu.
        initiateFrame = null;
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
            JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error15"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            cancelInitiateTrain(null);
            return;
        }
        String transitName = selectedTransit.getSystemName();
        String trainName = "";
        int index = startingBlockBox.getSelectedIndex();
        if (index < 0) {
            return;
        }
        String startBlockName = startingBlockBoxList.get(index).getSystemName();
        int startBlockSeq = startingBlockSeqList.get(index).intValue();
        index = destinationBlockBox.getSelectedIndex();
        if (index < 0) {
            return;
        }
        String endBlockName = destinationBlockBoxList.get(index).getSystemName();
        int endBlockSeq = destinationBlockSeqList.get(index).intValue();
        boolean autoRun = autoRunBox.isSelected();
        if (!checkResetWhenDone()) {
            return;
        }
        boolean resetWhenDone = resetWhenDoneBox.isSelected();
        boolean reverseAtEnd = reverseAtEndBox.isSelected();
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
        int departureTimeHours = 8;
        departureTimeHours = (Integer) departureHrSpinner.getValue();
        int departureTimeMinutes = 8;
        departureTimeMinutes = (Integer) departureMinSpinner.getValue();
        int delayRestartMinutes = 0;
        delayRestartMinutes = (Integer) delayMinSpinner.getValue();
        if ((delayRestartMinutes < 0)) {
            JOptionPane.showMessageDialog(initiateFrame, delayMinSpinner.getValue(),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.warn("Range error in Delay Restart Time Minutes field");
            return;
        }
        int tSource = 0;
        String dccAddress = "unknown";
        if (_TrainsFromRoster) {
            index = trainSelectBox.getSelectedIndex();
            if (index < 0) {
                // no trains available
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error14"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                cancelInitiateTrain(null);
                return;
            }
            trainName = (String) trainSelectBox.getSelectedItem();
            RosterEntry r = trainBoxList.get(index);
            dccAddress = r.getDccAddress();
            if (!isAddressFree(r.getDccLocoAddress().getNumber())) {
                // DCC address is already in use by an Active Train
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                        "Error40", dccAddress), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            tSource = ActiveTrain.ROSTER;

            if (trainTypeBox.getSelectedIndex() != 0
                    && (r.getAttribute("DisptacherTrainType") == null
                    || !r.getAttribute("DispatcherTrainType").equals("" + trainTypeBox.getSelectedItem()))) {
                r.putAttribute("DispatcherTrainType", "" + trainTypeBox.getSelectedItem());
                r.updateFile();
                Roster.getDefault().writeRoster();
            }
        } else if (_TrainsFromOperations) {
            tSource = ActiveTrain.OPERATIONS;
            index = trainSelectBox.getSelectedIndex();
            if (index < 0) {
                // no trains available
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error14"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                cancelInitiateTrain(null);
                return;
            }
            trainName = (String) trainSelectBox.getSelectedItem();
            // get lead engine for this train
            Train train = jmri.InstanceManager.getDefault(TrainManager.class).getTrainByName(trainName);
            if (train != null) {
                dccAddress = train.getLeadEngineDccAddress();
            }
        } else if (_TrainsFromUser) {
            trainName = trainNameField.getText();
            if ((trainName == null) || trainName.equals("")) {
                // no train name entered
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error14"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!isTrainFree(trainName)) {
                // train name is already in use by an Active Train
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                        "Error24", trainName), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int address = -1;
            address = (Integer) dccAddressSpinner.getValue(); // SpinnerNumberModel limits address to 1 - 9999 inclusive
            dccAddress = String.valueOf(address);
            if (!isAddressFree(address)) {
                // DCC address is already in use by an Active Train
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                        "Error40", address), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            tSource = ActiveTrain.USER;
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
            at.setRosterEntry(trainBoxList.get(trainSelectBox.getSelectedIndex()));
        }
        at.setAllocateMethod(allocateMethod);
        at.setDelayedStart(delayedStart);
        at.setDelayedRestart(delayedReStart);
        at.setDepartureTimeHr(departureTimeHours);
        at.setDepartureTimeMin(departureTimeMinutes);
        at.setRestartDelay(delayRestartMinutes);
        at.setDelaySensor(delaySensor.getSelectedItem());
        at.setResetStartSensor(resetStartSensorBox.isSelected());
        if ((_dispatcher.isFastClockTimeGE(departureTimeHours, departureTimeMinutes) && delayedStart != ActiveTrain.SENSORDELAY)
                || delayedStart == ActiveTrain.NODELAY) {
            at.setStarted();
        }
        at.setRestartSensor(delayReStartSensor.getSelectedItem());
        at.setResetRestartSensor(resetRestartSensorBox.isSelected());
        at.setTrainType(trainType);
        at.setTerminateWhenDone(terminateWhenDoneBox.isSelected());
        if (autoRunBox.isSelected()) {
            AutoActiveTrain aat = new AutoActiveTrain(at);
            setAutoRunItems(aat);
            if (!aat.initialize()) {
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                        "Error27", at.getTrainName()), Bundle.getMessage("MessageTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
            _dispatcher.getAutoTrainsFrame().addAutoActiveTrain(aat);
        }
        _dispatcher.allocateNewActiveTrain(at);
        initiateFrame.setVisible(false);
        initiateFrame.dispose();  // prevent this window from being listed in the Window menu.
        initiateFrame = null;
        _dispatcher.newTrainDone(at);
    }

    private void initializeFreeTransitsCombo(List<Transit> transitList) {
        transitSelectBox.removeAllItems();
        transitBoxList.clear();
        if (transitList.isEmpty()) {
            for (Transit t : _TransitManager.getNamedBeanSet()) {
                transitList.add(t);
            }

        }
        for (Transit t : transitList) {
            boolean free = true;
            for (int j = 0; j < _ActiveTrainsList.size(); j++) {
                ActiveTrain at = _ActiveTrainsList.get(j);
                if (t == at.getTransit()) {
                    free = false;
                }
            }
            if (free) {
                String tName = t.getSystemName();
                transitBoxList.add(t);
                String uname = t.getUserName();
                if ((uname != null) && (!uname.equals("")) && (!uname.equals(tName))) {
                    tName = tName + "(" + uname + ")";
                }
                transitSelectBox.addItem(tName);
            }
        }
        if (transitBoxList.size() > 0) {
            transitSelectBox.setSelectedIndex(0);
            selectedTransit = transitBoxList.get(0);
        } else {
            selectedTransit = null;
        }
    }

    ActionListener trainSelectBoxListener = null;

    private void initializeFreeTrainsCombo() {
        trainSelectBox.removeActionListener(trainSelectBoxListener);
        trainSelectBox.removeAllItems();
        trainBoxList.clear();
        if (_TrainsFromRoster) {
            // initialize free trains from roster
            List<RosterEntry> l = Roster.getDefault().matchingList(null, null, null, null, null, null, null);
            if (l.size() > 0) {
                for (int i = 0; i < l.size(); i++) {
                    RosterEntry r = l.get(i);
                    String rName = r.titleString();
                    int rAddr = r.getDccLocoAddress().getNumber();
                    if (isTrainFree(rName) && isAddressFree(rAddr)) {
                        trainBoxList.add(r);
                        trainSelectBox.addItem(rName);
                    }
                }
            }
            if (trainSelectBoxListener == null) {
                trainSelectBoxListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        RosterEntry r = trainBoxList.get(trainSelectBox.getSelectedIndex());
                        // check to see if speed profile exists and is not empty
                        if (r.getSpeedProfile() == null || r.getSpeedProfile().getProfileSize() < 1) {
                            // disable profile boxes etc.
                            setSpeedProfileOptions(false);
                            // turnoff options
                            _useSpeedProfile = false;
                            _stopBySpeedProfile = false;
                        } else {
                            // enable profile boxes
                            setSpeedProfileOptions(true);
                        }
                        if (transitsFromSpecificBlock) {
                            //resets the transit box if required
                            transitsFromSpecificBlock = false;
                            initializeFreeTransitsCombo(new ArrayList<Transit>());
                        }
                        if (r.getAttribute("DispatcherTrainType") != null && !r.getAttribute("DispatcherTrainType").equals("")) {
                            trainTypeBox.setSelectedItem(r.getAttribute("DispatcherTrainType"));
                        }
                    }
                };
            }
            trainSelectBox.addActionListener(trainSelectBoxListener);
        } else if (_TrainsFromOperations) {
            // initialize free trains from operations
            List<Train> trains = jmri.InstanceManager.getDefault(TrainManager.class).getTrainsByNameList();
            if (trains.size() > 0) {
                for (int i = 0; i < trains.size(); i++) {
                    Train t = trains.get(i);
                    if (t != null) {
                        String tName = t.getName();
                        if (isTrainFree(tName)) {
                            trainSelectBox.addItem(tName);
                        }
                    }
                }
            }
        }
        if (trainBoxList.size() > 0) {
            trainSelectBox.setSelectedIndex(0);
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
        }
    }

    private boolean isTrainFree(String rName) {
        for (int j = 0; j < _ActiveTrainsList.size(); j++) {
            ActiveTrain at = _ActiveTrainsList.get(j);
            if (rName.equals(at.getTrainName())) {
                return false;
            }
        }
        return true;
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
    }

    private String getBlockName(Block b) {
        if (b != null) {
            String sName = b.getSystemName();
            String uName = b.getUserName();
            if ((uName != null) && (!uName.equals("")) && (!uName.equals(sName))) {
                return (sName + "(" + uName + ")");
            }
            return sName;
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
            Object selName = JOptionPane.showInputDialog(initiateFrame,
                    Bundle.getMessage("LoadTrainChoice"), Bundle.getMessage("LoadTrainTitle"),
                    JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
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
                log.error("IO Exception when reading train info file " + ioe);
            } catch (org.jdom2.JDOMException jde) {
                log.error("JDOM Exception when reading train info file " + jde);
            }
        }
        handleDelayStartClick(null);
        handleReverseAtEndBoxClick(null);
    }

    private void saveTrainInfo(ActionEvent e) {
        TrainInfo info = dialogToTrainInfo();

        // get file name
        String eName = "";
        eName = JOptionPane.showInputDialog(initiateFrame,
                Bundle.getMessage("EnterFileName") + " :", _trainInfoName);
        if (eName == null) {  //Cancel pressed
            return;
        }
        if (eName.length() < 1) {
            JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error25"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                int selectedValue = JOptionPane.showOptionDialog(initiateFrame,
                        Bundle.getMessage("Question3", fileName),
                        Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, new Object[]{Bundle.getMessage("ButtonReplace"),
                            Bundle.getMessage("ButtonNo")}, Bundle.getMessage("ButtonNo"));
                if (selectedValue == 1) {
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
            log.error("IO exception writing Train Info: " + ioe);
        }
    }

    private void deleteTrainInfo(ActionEvent e) {
        String[] names = _tiFile.getTrainInfoFileNames();
        if (names.length > 0) {
            Object selName = JOptionPane.showInputDialog(initiateFrame,
                    Bundle.getMessage("DeleteTrainChoice"), Bundle.getMessage("DeleteTrainTitle"),
                    JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
            if ((selName == null) || (((String) selName).equals(""))) {
                return;
            }
            _tiFile.deleteTrainInfoFile((String) selName);
        }
    }

    private void trainInfoToDialog(TrainInfo info) {
        if (!setComboBox(transitSelectBox, info.getTransitName())) {
            log.warn("Transit " + info.getTransitName() + " from file not in Transit menu");
            JOptionPane.showMessageDialog(initiateFrame,
                    Bundle.getMessage("TransitWarn", info.getTransitName()),
                    null, JOptionPane.WARNING_MESSAGE);
        }
        _TrainsFromRoster = info.getTrainFromRoster();
        _TrainsFromOperations = info.getTrainFromTrains();
        _TrainsFromUser = info.getTrainFromUser();
        if (_TrainsFromRoster || _TrainsFromOperations) {
            initializeFreeTrainsCombo();
            if (!setComboBox(trainSelectBox, info.getTrainName())) {
                log.warn("Train " + info.getTrainName() + " from file not in Train menu");
                JOptionPane.showMessageDialog(initiateFrame,
                        Bundle.getMessage("TrainWarn", info.getTrainName()),
                        null, JOptionPane.WARNING_MESSAGE);
            }
        } else if (_TrainsFromUser) {
            trainNameField.setText(info.getTrainName());
            dccAddressSpinner.setValue(Integer.parseInt(info.getDccAddress()));
        }
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
        terminateWhenDoneBox.setSelected(info.getTerminateWhenDone());
        setComboBox(trainTypeBox, info.getTrainType());
        autoRunBox.setSelected(info.getAutoRun());
        loadAtStartupBox.setSelected(info.getLoadAtStartup());
        setAllocateMethodButtons(info.getAllocationMethod());
        autoTrainInfoToDialog(info);
    }

    private TrainInfo dialogToTrainInfo() {
        TrainInfo info = new TrainInfo();
        info.setTransitName((String) transitSelectBox.getSelectedItem());
        info.setTransitId(selectedTransit.getSystemName());
        if (_TrainsFromRoster || _TrainsFromOperations) {
            info.setTrainName((String) trainSelectBox.getSelectedItem());
            info.setDccAddress(" ");
        } else if (_TrainsFromUser) {
            info.setTrainName(trainNameField.getText());
            info.setDccAddress(String.valueOf(dccAddressSpinner.getValue()));
        }
        info.setTrainInTransit(inTransitBox.isSelected());
        info.setStartBlockName((String) startingBlockBox.getSelectedItem());
        int index = startingBlockBox.getSelectedIndex();
        if (index < 0) {
            log.error("No Starting Block.");
        } else {
            info.setStartBlockId(startingBlockBoxList.get(index).getSystemName());
            info.setStartBlockSeq(startingBlockSeqList.get(index).intValue());
        }
        info.setDestinationBlockName((String) destinationBlockBox.getSelectedItem());
        index = destinationBlockBox.getSelectedIndex();
        if (index < 0) {
            log.error("No Destination Block.");
        } else {
            info.setDestinationBlockId(destinationBlockBoxList.get(index).getSystemName());
            info.setDestinationBlockSeq(destinationBlockSeqList.get(index).intValue());
        }
        info.setTrainFromRoster(_TrainsFromRoster);
        info.setTrainFromTrains(_TrainsFromOperations);
        info.setTrainFromUser(_TrainsFromUser);
        info.setPriority((Integer) prioritySpinner.getValue());
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
        info.setTerminateWhenDone(terminateWhenDoneBox.isSelected());
        autoRunItemsToTrainInfo(info);
        return info;
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

    private boolean setComboBox(JComboBox<String> box, String txt) {
        boolean found = false;
        for (int i = 0; i < box.getItemCount(); i++) {
            if (txt.equals(box.getItemAt(i))) {
                box.setSelectedIndex(i);
                found = true;
                break;
            }
        }
        if (!found) {
            box.setSelectedIndex(0);
        }
        return found;
    }

    int delayModeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, delayedStartInt, delayedStartString);

        if (result < 0) {
            log.warn("unexpected mode string in turnoutMode: " + mode);
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
    private final JCheckBox resistanceWheelsBox = new JCheckBox(Bundle.getMessage("ResistanceWheels"));
    private final JLabel trainLengthLabel = new JLabel(Bundle.getMessage("MaxTrainLengthLabel"));
    private final JSpinner maxTrainLengthSpinner = new JSpinner(); // initialized later
    // auto run variables
    float _speedFactor = 1.0f;
    float _maxSpeed = 0.6f;
    int _rampRate = AutoActiveTrain.RAMP_NONE;
    boolean _resistanceWheels = true;
    boolean _runInReverse = false;
    boolean _soundDecoder = false;
    float _maxTrainLength = 200.0f;
    boolean _stopBySpeedProfile = false;
    float _stopBySpeedProfileAdjust = 1.0f;
    boolean _useSpeedProfile = true;

    private void setAutoRunDefaults() {
        _speedFactor = 1.0f;
        _maxSpeed = 0.6f;
        _rampRate = AutoActiveTrain.RAMP_NONE;
        _resistanceWheels = true;
        _runInReverse = false;
        _soundDecoder = false;
        _maxTrainLength = 100.0f;
        _stopBySpeedProfile = false;
        _stopBySpeedProfileAdjust = 1.0f;
        _useSpeedProfile = true;

    }

    private void initializeAutoRunItems() {
        initializeRampCombo();
        pa1.setLayout(new FlowLayout());
        pa1.add(speedFactorLabel);
        speedFactorSpinner.setModel(new SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(1.5f), Float.valueOf(0.01f)));
        speedFactorSpinner.setEditor(new JSpinner.NumberEditor(speedFactorSpinner, "# %"));
        pa1.add(speedFactorSpinner);
        speedFactorSpinner.setToolTipText(Bundle.getMessage("SpeedFactorHint"));
        pa1.add(new JLabel("   "));
        pa1.add(maxSpeedLabel);
        maxSpeedSpinner.setModel(new SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(1.5f), Float.valueOf(0.01f)));
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
        stopBySpeedProfileAdjustSpinner.setModel(new SpinnerNumberModel( Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(1.5f), Float.valueOf(0.01f)));
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
        pa4.add(resistanceWheelsBox);
        resistanceWheelsBox.setToolTipText(Bundle.getMessage("ResistanceWheelsBoxHint"));
        pa4.add(new JLabel("   "));
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
        resistanceWheelsBox.setSelected(_resistanceWheels);
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
        resistanceWheelsBox.setSelected(info.getResistanceWheels());
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
        info.setResistanceWheels(resistanceWheelsBox.isSelected());
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
        _resistanceWheels = resistanceWheelsBox.isSelected();
        _runInReverse = runInReverseBox.isSelected();
        _soundDecoder = soundDecoderBox.isSelected();
        _maxTrainLength = (float) maxTrainLengthSpinner.getValue();
        _useSpeedProfile = useSpeedProfileCheckBox.isSelected();
        _stopBySpeedProfile = stopBySpeedProfileCheckBox.isSelected();
        if (_stopBySpeedProfile) {
            _stopBySpeedProfileAdjust = (Float) stopBySpeedProfileAdjustSpinner.getValue();
        }
        return success;
    }

    private void setAutoRunItems(AutoActiveTrain aaf) {
        aaf.setSpeedFactor(_speedFactor);
        aaf.setMaxSpeed(_maxSpeed);
        aaf.setRampRate(_rampRate);
        aaf.setResistanceWheels(_resistanceWheels);
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

    private final static Logger log = LoggerFactory.getLogger(ActivateTrainFrame.class);
}
