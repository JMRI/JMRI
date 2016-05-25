// ActivateTrainFrame.java
package jmri.jmrit.dispatcher;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.Block;
import jmri.InstanceManager;
import jmri.Transit;
import jmri.TransitManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays the Activate New Train dialog and processes information entered
 * there.
 *
 * <P>
 * This module works with Dispatcher, which initiates the display of the dialog.
 * Dispatcher also creates the ActiveTrain.
 *
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is open source software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author	Dave Duchamp Copyright (C) 2009
 * @version	$Revision$
 */
public class ActivateTrainFrame {

    public ActivateTrainFrame(DispatcherFrame d) {
        _dispatcher = d;
        _tiFile = new TrainInfoFile();
        if (_tiFile == null) {
            log.error("Failed to create TrainInfoFile object when constructing ActivateTrainFrame");
        }
    }

    static final ResourceBundle rb = ResourceBundle
            .getBundle("jmri.jmrit.dispatcher.DispatcherBundle");

    // operational instance variables
    private DispatcherFrame _dispatcher = null;
    private TrainInfoFile _tiFile = null;
    private boolean _TrainsFromUser = false;
    private boolean _TrainsFromRoster = true;
    private boolean _TrainsFromTrains = false;
    private ArrayList<ActiveTrain> _ActiveTrainsList = null;
    private TransitManager _TransitManager = InstanceManager.transitManagerInstance();
    private String _trainInfoName = "";

    // initiate train window variables
    private Transit selectedTransit = null;
    //private String selectedTrain = "";
    private JmriJFrame initiateFrame = null;
    private Container initiatePane = null;
    private JComboBox<String> transitSelectBox = new JComboBox<String>();
    private ArrayList<Transit> transitBoxList = new ArrayList<Transit>();
    private JLabel trainBoxLabel = new JLabel("     " + Bundle.getMessage("TrainBoxLabel") + ":");
    private JComboBox<String> trainSelectBox = new JComboBox<String>();
    private ArrayList<RosterEntry> trainBoxList = new ArrayList<RosterEntry>();
    private JLabel trainFieldLabel = new JLabel(Bundle.getMessage("TrainBoxLabel") + ":");
    private JTextField trainNameField = new JTextField(10);
    private JLabel dccAddressFieldLabel = new JLabel("     " + Bundle.getMessage("DccAddressFieldLabel") + ":");
    private JTextField dccAddressField = new JTextField(6);
    private JCheckBox inTransitBox = new JCheckBox(Bundle.getMessage("TrainInTransit"));
    private JComboBox<String> startingBlockBox = new JComboBox<String>();
    private ArrayList<Block> startingBlockBoxList = new ArrayList<Block>();
    private ArrayList<Integer> startingBlockSeqList = new ArrayList<Integer>();
    private JComboBox<String> destinationBlockBox = new JComboBox<String>();
    private ArrayList<Block> destinationBlockBoxList = new ArrayList<Block>();
    private ArrayList<Integer> destinationBlockSeqList = new ArrayList<Integer>();
    private JButton addNewTrainButton = null;
    private JButton loadButton = null;
    private JButton saveButton = null;
    private JButton deleteButton = null;
    private JCheckBox autoRunBox = new JCheckBox(Bundle.getMessage("AutoRun"));
    private JCheckBox terminateWhenDoneBox = new JCheckBox(Bundle.getMessage("TerminateWhenDone"));
    private JTextField priorityField = new JTextField(6);
    private JCheckBox resetWhenDoneBox = new JCheckBox(Bundle.getMessage("ResetWhenDone"));
    private JCheckBox reverseAtEndBox = new JCheckBox(Bundle.getMessage("ReverseAtEnd"));
    int delayedStartInt[] = new int[]{ActiveTrain.NODELAY, ActiveTrain.TIMEDDELAY, ActiveTrain.SENSORDELAY};
    String delayedStartString[] = new String[]{Bundle.getMessage("DelayedStartNone"), Bundle.getMessage("DelayedStartTimed"), Bundle.getMessage("DelayedStartSensor")};
    private JComboBox<String> delayedStartBox = new JComboBox<String>(delayedStartString);
    private JLabel delayedReStartLabel = new JLabel(Bundle.getMessage("DelayRestart"));
    private JLabel delayReStartSensorLabel = new JLabel(Bundle.getMessage("RestartSensor"));
    private JComboBox<String> delayedReStartBox = new JComboBox<String>(delayedStartString);
    private jmri.util.swing.JmriBeanComboBox delaySensor = new jmri.util.swing.JmriBeanComboBox(jmri.InstanceManager.sensorManagerInstance());
    private jmri.util.swing.JmriBeanComboBox delayReStartSensor = new jmri.util.swing.JmriBeanComboBox(jmri.InstanceManager.sensorManagerInstance());

    private JTextField departureHrField = new JTextField(2);
    private JTextField departureMinField = new JTextField(2);
    private JLabel departureTimeLabel = new JLabel(Bundle.getMessage("DepartureTime"));
    private JLabel departureSepLabel = new JLabel(":");

    private JTextField delayMinField = new JTextField(3);
    private JLabel delayMinLabel = new JLabel(Bundle.getMessage("RestartTimed"));

    private JComboBox<String> trainTypeBox = new JComboBox<String>();
    // Note: See also items related to automatically running trains near the end of this module

    boolean transitsFromSpecificBlock = false;

    /**
     * Open up a new train window, for a given roster entry located in a
     * specific block
     */
    public void initiateTrain(ActionEvent e, RosterEntry re, Block b) {
        initiateTrain(e);
        if (_TrainsFromRoster && re != null) {
            setComboBox(trainSelectBox, re.getId());
            //Add in some bits of code as some point to filter down the transits that can be used.
        }
        if (b != null && selectedTransit != null) {
            ArrayList<Transit> transitList = _TransitManager.getListUsingBlock(b);
            ArrayList<Transit> transitEntryList = _TransitManager.getListEntryBlock(b);
            for (Transit t : transitEntryList) {
                if (!transitList.contains(t)) {
                    transitList.add(t);
                }
            }
            transitsFromSpecificBlock = true;
            initializeFreeTransitsCombo(transitList);
            ArrayList<Block> tmpBlkList = new ArrayList<Block>();
            if (selectedTransit.getEntryBlocksList().contains(b)) {
                tmpBlkList = selectedTransit.getEntryBlocksList();
                inTransitBox.setSelected(false);
            } else if (selectedTransit.containsBlock(b)) {
                tmpBlkList = selectedTransit.getInternalBlocksList();
                inTransitBox.setSelected(true);
            }
            ArrayList<Integer> tmpSeqList = selectedTransit.getBlockSeqList();
            for (int i = 0; i < tmpBlkList.size(); i++) {
                if (tmpBlkList.get(i) == b) {
                    setComboBox(startingBlockBox, getBlockName(b) + "-" + tmpSeqList.get(i));
                    break;
                }
            }
        }
    }

    /**
     * Displays a window that allows a new ActiveTrain to be activated
     *
     * Called by Dispatcher in response to the dispatcher clicking the New Train
     * button
     */
    protected void initiateTrain(ActionEvent e) {
        // set Dispatcher defaults
        _TrainsFromRoster = _dispatcher.getTrainsFromRoster();
        _TrainsFromTrains = _dispatcher.getTrainsFromTrains();
        _TrainsFromUser = _dispatcher.getTrainsFromUser();
        _ActiveTrainsList = _dispatcher.getActiveTrainsList();
        // create window if needed
        if (initiateFrame == null) {
            initiateFrame = new JmriJFrame(Bundle.getMessage("AddTrainTitle"), false, true);
            initiateFrame.addHelpMenu("package.jmri.jmrit.dispatcher.NewTrain", true);
            initiatePane = initiateFrame.getContentPane();
            initiatePane.setLayout(new BoxLayout(initiateFrame.getContentPane(), BoxLayout.Y_AXIS));
            // add buttons to load and save train information
            JPanel p0 = new JPanel();
            p0.setLayout(new FlowLayout());
            p0.add(loadButton = new JButton(Bundle.getMessage("LoadButton")));
            loadButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadTrainInfo(e);
                }
            });
            loadButton.setToolTipText(Bundle.getMessage("LoadButtonHint"));
            p0.add(saveButton = new JButton(Bundle.getMessage("SaveButton")));
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveTrainInfo(e);
                }
            });
            saveButton.setToolTipText(Bundle.getMessage("SaveButtonHint"));
            p0.add(deleteButton = new JButton(Bundle.getMessage("DeleteButton")));
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteTrainInfo(e);
                }
            });
            deleteButton.setToolTipText(Bundle.getMessage("DeleteButtonHint"));
            initiatePane.add(p0);
            initiatePane.add(new JSeparator());
            // add items relating to both manually run and automatic trains.
            JPanel p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel(Bundle.getMessage("TransitBoxLabel") + " :"));
            p1.add(transitSelectBox);
            transitSelectBox.addActionListener(new ActionListener() {
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
            p1a.add(dccAddressField);
            dccAddressField.setToolTipText(Bundle.getMessage("DccAddressFieldHint"));
            initiatePane.add(p1a);
            JPanel p2 = new JPanel();
            p2.setLayout(new FlowLayout());
            p2.add(inTransitBox);
            inTransitBox.addActionListener(new ActionListener() {
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
            JPanel p6 = new JPanel();
            p6.setLayout(new FlowLayout());
            p6.add(resetWhenDoneBox);
            resetWhenDoneBox.addActionListener(new ActionListener() {
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
            delayedReStartBox.addActionListener(new ActionListener() {
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
            p6b.add(delayMinField);
            delayMinField.setText("0");
            delayMinField.setToolTipText(Bundle.getMessage("RestartTimedHint"));
            p6b.add(delayReStartSensorLabel);
            p6b.add(delayReStartSensor);
            delayReStartSensor.setFirstItemBlank(true);
            handleResetWhenDoneClick(null);
            initiatePane.add(p6b);

            JPanel p10 = new JPanel();
            p10.setLayout(new FlowLayout());
            p10.add(reverseAtEndBox);
            reverseAtEndBox.setToolTipText(Bundle.getMessage("ReverseAtEndBoxHint"));
            initiatePane.add(p10);
            reverseAtEndBox.addActionListener(new ActionListener() {
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
            p8.add(new JLabel(Bundle.getMessage("PriorityLabel") + " :"));
            p8.add(priorityField);
            priorityField.setToolTipText(Bundle.getMessage("PriorityHint"));
            priorityField.setText("5");
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
                public void actionPerformed(ActionEvent e) {
                    handleDelayStartClick(e);
                }
            });
            p9.add(departureTimeLabel);
            p9.add(departureHrField);
            departureHrField.setText("08");
            departureHrField.setToolTipText(Bundle.getMessage("DepartureTimeHrHint"));
            p9.add(departureSepLabel);
            p9.add(departureMinField);
            departureMinField.setText("00");
            departureMinField.setToolTipText(Bundle.getMessage("DepartureTimeMinHint"));
            p9.add(delaySensor);
            delaySensor.setFirstItemBlank(true);
            handleDelayStartClick(null);
            initiatePane.add(p9);
            initiatePane.add(new JSeparator());
            JPanel p5 = new JPanel();
            p5.setLayout(new FlowLayout());
            p5.add(autoRunBox);
            autoRunBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handleAutoRunClick(e);
                }
            });
            autoRunBox.setToolTipText(Bundle.getMessage("AutoRunBoxHint"));
            autoRunBox.setSelected(false);
            initiatePane.add(p5);
            initiatePane.add(new JSeparator());
            initializeAutoRunItems();
            initiatePane.add(new JSeparator());
            JPanel p7 = new JPanel();
            p7.setLayout(new FlowLayout());
            JButton cancelButton = null;
            p7.add(cancelButton = new JButton(Bundle.getMessage("CancelButton")));
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelInitiateTrain(e);
                }
            });
            cancelButton.setToolTipText(Bundle.getMessage("CancelButtonHint"));
            p7.add(addNewTrainButton = new JButton(Bundle.getMessage("AddNewTrainButton")));
            addNewTrainButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addNewTrain(e);
                }
            });
            addNewTrainButton.setToolTipText(Bundle.getMessage("AddNewTrainButtonHint"));
            initiatePane.add(p7);
        }
        if (_TrainsFromRoster || _TrainsFromTrains) {
            trainBoxLabel.setVisible(true);
            trainSelectBox.setVisible(true);
            trainFieldLabel.setVisible(false);
            trainNameField.setVisible(false);
            dccAddressFieldLabel.setVisible(false);
            dccAddressField.setVisible(false);
        } else if (_TrainsFromUser) {
            trainNameField.setText("");
            trainBoxLabel.setVisible(false);
            trainSelectBox.setVisible(false);
            trainFieldLabel.setVisible(true);
            trainNameField.setVisible(true);
            dccAddressFieldLabel.setVisible(true);
            dccAddressField.setVisible(true);
        }
        setAutoRunDefaults();
        autoRunBox.setSelected(false);
        initializeFreeTransitsCombo(new ArrayList<Transit>());
        initializeFreeTrainsCombo();
        initiateFrame.pack();
        initiateFrame.setVisible(true);
    }

    private void initializeTrainTypeBox() {
        trainTypeBox.removeAllItems();
        trainTypeBox.addItem(Bundle.getMessage("None"));
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
                    .getMessage("NoEntryBlocks"), Bundle.getMessage("InformationTitle"),
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
            javax.swing.JOptionPane.showMessageDialog(initiateFrame, rb
                    .getString("NoResetMessage"), Bundle.getMessage("InformationTitle"),
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

    private void handleDelayStartClick(ActionEvent e) {
        departureHrField.setVisible(false);
        departureMinField.setVisible(false);
        departureTimeLabel.setVisible(false);
        departureSepLabel.setVisible(false);
        delaySensor.setVisible(false);
        if (delayedStartBox.getSelectedItem().equals(Bundle.getMessage("DelayedStartTimed"))) {
            departureHrField.setVisible(true);
            departureMinField.setVisible(true);
            departureTimeLabel.setVisible(true);
            departureSepLabel.setVisible(true);
        } else if (delayedStartBox.getSelectedItem().equals(Bundle.getMessage("DelayedStartSensor"))) {
            delaySensor.setVisible(true);
        }
    }

    private void handleResetWhenDoneClick(ActionEvent e) {
        delayMinField.setVisible(false);
        delayMinLabel.setVisible(false);
        delayedReStartLabel.setVisible(false);
        delayedReStartBox.setVisible(false);
        delayReStartSensorLabel.setVisible(false);
        delayReStartSensor.setVisible(false);
        if (resetWhenDoneBox.isSelected()) {
            delayedReStartLabel.setVisible(true);
            delayedReStartBox.setVisible(true);
            if (delayedReStartBox.getSelectedItem().equals(Bundle.getMessage("DelayedStartTimed"))) {
                delayMinField.setVisible(true);
                delayMinLabel.setVisible(true);
            } else if (delayedReStartBox.getSelectedItem().equals(Bundle.getMessage("DelayedStartSensor"))) {
                delayReStartSensor.setVisible(true);
                delayReStartSensorLabel.setVisible(true);
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

    private void cancelInitiateTrain(ActionEvent e) {
        initiateFrame.setVisible(false);
        initiateFrame.dispose();  // prevent this window from being listed in the Window menu.
        initiateFrame = null;
        _dispatcher.newTrainDone(null);
    }

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
        int delayedStart = delayModeFromBox(delayedStartBox);
        int delayedReStart = delayModeFromBox(delayedReStartBox);
        int departureTimeHours = 8;
        try {
            departureTimeHours = Integer.parseInt(departureHrField.getText());
            if ((departureTimeHours < 0) || (departureTimeHours > 23)) {
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                        "BadEntry3", departureHrField.getText()),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                log.warn("Range error in Departure Time Hours field");
                return;
            }
        } catch (NumberFormatException ehr) {
            JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                    "BadEntry2", departureHrField.getText()),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.warn("Conversion exception in departure time hours field");
            return;
        }
        int departureTimeMinutes = 8;
        try {
            departureTimeMinutes = Integer.parseInt(departureMinField.getText());
            if ((departureTimeMinutes < 0) || (departureTimeMinutes > 59)) {
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                        "BadEntry3", departureMinField.getText()),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                log.warn("Range error in Departure Time Minutes field");
                return;
            }
        } catch (NumberFormatException emn) {
            JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                    "BadEntry2", departureMinField.getText()),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.warn("Conversion exception in departure time minutes field");
            return;
        }
        int delayRestartMinutes = 0;
        try {
            delayRestartMinutes = Integer.parseInt(delayMinField.getText());
            if ((delayRestartMinutes < 0)) {
                JOptionPane.showMessageDialog(initiateFrame, delayMinField.getText(),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                log.warn("Range error in Delay Restart Time Minutes field");
                return;
            }
        } catch (NumberFormatException emn) {
            JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                    "BadEntry2", departureMinField.getText()),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.warn("Conversion exception in departure time minutes field");
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
            tSource = ActiveTrain.ROSTER;

            if (trainTypeBox.getSelectedIndex() != 0
                    && (r.getAttribute("DisptacherTrainType") == null
                    || !r.getAttribute("DispatcherTrainType").equals("" + trainTypeBox.getSelectedItem()))) {
                r.putAttribute("DispatcherTrainType", "" + trainTypeBox.getSelectedItem());
                r.updateFile();
                Roster.writeRosterFile();
            }
        } else if (_TrainsFromTrains) {
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
            dccAddress = dccAddressField.getText();
            int address = -1;
            try {
                address = Integer.parseInt(dccAddress);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error23"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                log.error("Conversion exception in dccAddress field");
                return;
            }
            if ((address < 1) || (address > 9999)) {
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage("Error23"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            tSource = ActiveTrain.USER;
        }
        int priority = 5;
        try {
            priority = Integer.parseInt(priorityField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                    "BadEntry", priorityField.getText()), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            log.error("Conversion exception in priority field");
            return;
        }
        int trainType = trainTypeBox.getSelectedIndex();
        if (autoRunBox.isSelected()) {
            if (!readAutoRunItems()) {
                return;
            }
        }

        // create a new Active Train
        ActiveTrain at = _dispatcher.createActiveTrain(transitName, trainName, tSource, startBlockName,
                startBlockSeq, endBlockName, endBlockSeq, autoRun, dccAddress, priority,
                resetWhenDone, reverseAtEnd, true, initiateFrame);
        if (at == null) {
            return;  // error message sent by createActiveTrain
        }
        if (tSource == ActiveTrain.ROSTER) {
            at.setRosterEntry(trainBoxList.get(trainSelectBox.getSelectedIndex()));
        }
        at.setDelayedStart(delayedStart);
        at.setDelayedReStart(delayedReStart);
        at.setDepartureTimeHr(departureTimeHours);
        at.setDepartureTimeMin(departureTimeMinutes);
        at.setRestartDelay(delayRestartMinutes);
        at.setDelaySensor((jmri.Sensor) delaySensor.getSelectedBean());
        if (_dispatcher.isFastClockTimeGE(departureTimeHours, departureTimeMinutes) && delayedStart != ActiveTrain.SENSORDELAY) {
            at.setStarted();
        }
        at.setRestartDelaySensor((jmri.Sensor) delayReStartSensor.getSelectedBean());
        at.setTrainType(trainType);
        at.setTerminateWhenDone(terminateWhenDoneBox.isSelected());
        if (autoRunBox.isSelected()) {
            AutoActiveTrain aat = new AutoActiveTrain(at);
            setAutoRunItems(aat);
            if (!aat.initialize()) {
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                        "Error27", at.getTrainName()), Bundle.getMessage("InformationTitle"),
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

    private void initializeFreeTransitsCombo(ArrayList<Transit> transitList) {
        ArrayList<String> allTransits = (ArrayList<String>) _TransitManager.getSystemNameList();
        transitSelectBox.removeAllItems();
        transitBoxList.clear();
        if (transitList.isEmpty()) {
            for (int i = 0; i < allTransits.size(); i++) {
                String tName = allTransits.get(i);
                Transit t = _TransitManager.getBySystemName(tName);
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
                if ((t.getUserName() != null) && (!t.getUserName().equals(""))) {
                    tName = tName + "( " + t.getUserName() + " )";
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
            List<RosterEntry> l = Roster.instance().matchingList(null, null, null, null, null, null, null);
            if (l.size() > 0) {
                for (int i = 0; i < l.size(); i++) {
                    RosterEntry r = l.get(i);
                    String rName = r.titleString();
                    if (isTrainFree(rName)) {
                        trainBoxList.add(r);
                        trainSelectBox.addItem(rName);
                    }
                }
            }
            if (trainSelectBoxListener == null) {
                trainSelectBoxListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        RosterEntry r = trainBoxList.get(trainSelectBox.getSelectedIndex());
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
        } else if (_TrainsFromTrains) {
            // initialize free trains from operations
            List<Train> trains = TrainManager.instance().getTrainsByNameList();
            if (trains.size() > 0) {
                for (int i = 0; i < trains.size(); i++) {
                    Train t = trains.get(i);
                    if (t != null) {
                        String rName = t.getName();
                        if (isTrainFree(rName)) {
                            trainSelectBox.addItem(rName);
                        }
                    }
                }
            }
        }
        if (trainBoxList.size() > 0) {
            trainSelectBox.setSelectedIndex(0);
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
            if ((uName != null) && (uName != "")) {
                return (sName + "( " + uName + " )");
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
            Object selName = JOptionPane.showInputDialog(initiateFrame,
                    Bundle.getMessage("LoadTrainChoice"), Bundle.getMessage("LoadTrainTitle"),
                    JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
            if ((selName == null) || (((String) selName).equals(""))) {
                return;
            }
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
        if (info != null) {
            // get file name
            String eName = "";
            eName = JOptionPane.showInputDialog(initiateFrame,
                    Bundle.getMessage("EnterFileName") + " :", _trainInfoName);
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
                            JOptionPane.QUESTION_MESSAGE, null, new Object[]{Bundle.getMessage("FileYes"),
                                Bundle.getMessage("FileNo")}, Bundle.getMessage("FileNo"));
                    if (selectedValue == 1) {
                        return;   // return without writing if "No" response
                    }
                }
            }
            // write the Train Info file
            try {
                _tiFile.writeTrainInfo(info, fileName);
            } //catch (org.jdom2.JDOMException jde) { 
            //	log.error("JDOM exception writing Train Info: "+jde); 
            //}                           
            catch (java.io.IOException ioe) {
                log.error("IO exception writing Train Info: " + ioe);
            }
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
        _TrainsFromTrains = info.getTrainFromTrains();
        _TrainsFromUser = info.getTrainFromUser();
        if (_TrainsFromRoster || _TrainsFromTrains) {
            initializeFreeTrainsCombo();
            if (!setComboBox(trainSelectBox, info.getTrainName())) {
                log.warn("Train " + info.getTrainName() + " from file not in Train menu");
                JOptionPane.showMessageDialog(initiateFrame,
                        Bundle.getMessage("TrainWarn", info.getTrainName()),
                        null, JOptionPane.WARNING_MESSAGE);
            }
        } else if (_TrainsFromUser) {
            trainNameField.setText(info.getTrainName());
            dccAddressField.setText(info.getDCCAddress());
        }
        inTransitBox.setSelected(info.getTrainInTransit());
        initializeStartingBlockCombo();
        initializeDestinationBlockCombo();
        setComboBox(startingBlockBox, info.getStartBlockName());
        setComboBox(destinationBlockBox, info.getDestinationBlockName());
        priorityField.setText(info.getPriority());
        resetWhenDoneBox.setSelected(info.getResetWhenDone());
        reverseAtEndBox.setSelected(info.getReverseAtEnd());
        setDelayModeBox(info.getDelayedStart(), delayedStartBox);
        //delayedStartBox.setSelected(info.getDelayedStart());
        departureHrField.setText(info.getDepartureTimeHr());
        departureMinField.setText(info.getDepartureTimeMin());
        delaySensor.setSelectedBeanByName(info.getDelaySensor());

        setDelayModeBox(info.getDelayedRestart(), delayedReStartBox);
        delayMinField.setText(info.getRestartDelayTime());
        delayReStartSensor.setSelectedBeanByName(info.getRestartDelaySensor());
        terminateWhenDoneBox.setSelected(info.getTerminateWhenDone());
        setComboBox(trainTypeBox, info.getTrainType());
        autoRunBox.setSelected(info.getRunAuto());
        autoTrainInfoToDialog(info);
    }

    private TrainInfo dialogToTrainInfo() {
        TrainInfo info = new TrainInfo();
        info.setTransitName((String) transitSelectBox.getSelectedItem());
        if (_TrainsFromRoster || _TrainsFromTrains) {
            info.setTrainName((String) trainSelectBox.getSelectedItem());
            info.setDCCAddress(" ");
        } else if (_TrainsFromUser) {
            info.setTrainName(trainNameField.getText());
            info.setDCCAddress(dccAddressField.getText());
        }
        info.setTrainInTransit(inTransitBox.isSelected());
        info.setStartBlockName((String) startingBlockBox.getSelectedItem());
        info.setDestinationBlockName((String) destinationBlockBox.getSelectedItem());
        info.setTrainFromRoster(_TrainsFromRoster);
        info.setTrainFromTrains(_TrainsFromTrains);
        info.setTrainFromUser(_TrainsFromUser);
        info.setPriority(priorityField.getText());
        info.setResetWhenDone(resetWhenDoneBox.isSelected());
        info.setReverseAtEnd(reverseAtEndBox.isSelected());
        info.setDelayedStart(delayModeFromBox(delayedStartBox));
        info.setDelaySensor(delaySensor.getSelectedDisplayName());
        info.setDepartureTimeHr(departureHrField.getText());
        info.setDepartureTimeMin(departureMinField.getText());
        info.setTrainType((String) trainTypeBox.getSelectedItem());
        info.setRunAuto(autoRunBox.isSelected());
        info.setDelayedRestart(delayModeFromBox(delayedReStartBox));
        info.setRestartDelaySensor(delayReStartSensor.getSelectedDisplayName());
        info.setRestartDelayTime(delayMinField.getText());
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
     * future initializeAutoRunItems - initializes the display of auto run items
     * in this window initializeAutoRunValues - initializes the values of auto
     * run items from values in a saved train info file hideAutoRunItems - hides
     * all auto run items in this window showAutoRunItems - shows all auto run
     * items in this window autoTrainInfoToDialog - gets auto run items from a
     * train info, puts values in items, and initializes auto run dialog items
     * autoTrainItemsToTrainInfo - copies values of auto run items to train info
     * for saving to a file readAutoRunItems - reads and checks values of all
     * auto run items. returns true if OK, sends appropriate messages and
     * returns false if not OK setAutoRunItems - sets the user entered auto run
     * items in the new AutoActiveTrain
     */
    // auto run items in ActivateTrainFrame
    private JPanel pa1 = new JPanel();
    private JLabel speedFactorLabel = new JLabel(Bundle.getMessage("SpeedFactorLabel"));
    private JTextField speedFactorField = new JTextField(5);
    private JLabel maxSpeedLabel = new JLabel(Bundle.getMessage("MaxSpeedLabel"));
    private JTextField maxSpeedField = new JTextField(5);
    private JPanel pa2 = new JPanel();
    private JLabel rampRateLabel = new JLabel(Bundle.getMessage("RampRateBoxLabel"));
    private JComboBox<String> rampRateBox = new JComboBox<String>();
    private JPanel pa3 = new JPanel();
    private JCheckBox soundDecoderBox = new JCheckBox(Bundle.getMessage("SoundDecoder"));
    private JCheckBox runInReverseBox = new JCheckBox(Bundle.getMessage("RunInReverse"));
    private JPanel pa4 = new JPanel();
    private JCheckBox resistanceWheelsBox = new JCheckBox(Bundle.getMessage("ResistanceWheels"));
    private JLabel trainLengthLabel = new JLabel(Bundle.getMessage("MaxTrainLengthLabel"));
    private JTextField maxTrainLengthField = new JTextField(5);
    // auto run variables
    float _speedFactor = 1.0f;
    float _maxSpeed = 0.6f;
    int _rampRate = AutoActiveTrain.RAMP_NONE;
    boolean _resistanceWheels = true;
    boolean _runInReverse = false;
    boolean _soundDecoder = false;
    float _maxTrainLength = 200.0f;

    private void setAutoRunDefaults() {
        _speedFactor = 1.0f;
        _maxSpeed = 0.6f;
        _rampRate = AutoActiveTrain.RAMP_NONE;
        _resistanceWheels = true;
        _runInReverse = false;
        _soundDecoder = false;
        _maxTrainLength = 100.0f;
    }

    private void initializeAutoRunItems() {
        initializeRampCombo();
        pa1.setLayout(new FlowLayout());
        pa1.add(speedFactorLabel);
        pa1.add(speedFactorField);
        speedFactorField.setToolTipText(Bundle.getMessage("SpeedFactorHint"));
        pa1.add(new JLabel("   "));
        pa1.add(maxSpeedLabel);
        pa1.add(maxSpeedField);
        maxSpeedField.setToolTipText(Bundle.getMessage("MaxSpeedHint"));
        initiatePane.add(pa1);
        pa2.setLayout(new FlowLayout());
        pa2.add(rampRateLabel);
        pa2.add(rampRateBox);
        rampRateBox.setToolTipText(Bundle.getMessage("RampRateBoxHint"));
        initiatePane.add(pa2);
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
        pa4.add(maxTrainLengthField);
        maxTrainLengthField.setToolTipText(Bundle.getMessage("MaxTrainLengthHint"));
        initiatePane.add(pa4);
        hideAutoRunItems();   // initialize with auto run items hidden
        initializeAutoRunValues();
    }

    private void initializeAutoRunValues() {
        speedFactorField.setText("" + _speedFactor);
        maxSpeedField.setText("" + _maxSpeed);
        rampRateBox.setSelectedIndex(_rampRate);
        resistanceWheelsBox.setSelected(_resistanceWheels);
        soundDecoderBox.setSelected(_soundDecoder);
        runInReverseBox.setSelected(_runInReverse);
        maxTrainLengthField.setText("" + _maxTrainLength);
    }

    private void hideAutoRunItems() {
        pa1.setVisible(false);
        pa2.setVisible(false);
        pa3.setVisible(false);
        pa4.setVisible(false);
    }

    private void showAutoRunItems() {
        pa1.setVisible(true);
        pa2.setVisible(true);
        pa3.setVisible(true);
        pa4.setVisible(true);
    }

    private void autoTrainInfoToDialog(TrainInfo info) {
        speedFactorField.setText(info.getSpeedFactor());
        maxSpeedField.setText(info.getMaxSpeed());
        setComboBox(rampRateBox, info.getRampRate());
        resistanceWheelsBox.setSelected(info.getResistanceWheels());
        runInReverseBox.setSelected(info.getRunInReverse());
        soundDecoderBox.setSelected(info.getSoundDecoder());
        maxTrainLengthField.setText(info.getMaxTrainLength());
        if (autoRunBox.isSelected()) {
            showAutoRunItems();
        } else {
            hideAutoRunItems();
        }
        initiateFrame.pack();
    }

    private void autoRunItemsToTrainInfo(TrainInfo info) {
        info.setSpeedFactor(speedFactorField.getText());
        info.setMaxSpeed(maxSpeedField.getText());
        info.setRampRate((String) rampRateBox.getSelectedItem());
        info.setResistanceWheels(resistanceWheelsBox.isSelected());
        info.setRunInReverse(runInReverseBox.isSelected());
        info.setSoundDecoder(soundDecoderBox.isSelected());
        info.setMaxTrainLength(maxTrainLengthField.getText());
    }

    private boolean readAutoRunItems() {
        boolean success = true;
        float factor = 1.0f;
        try {
            factor = Float.parseFloat(speedFactorField.getText());
            if ((factor < 0.1f) || (factor > 1.5f)) {
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                        "Error29", speedFactorField.getText()), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                speedFactorField.setText("1.0");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                    "Error30", speedFactorField.getText()), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            speedFactorField.setText("1.0");
            return false;
        }
        _speedFactor = factor;
        float max = 0.6f;
        try {
            max = Float.parseFloat(maxSpeedField.getText());
            if ((max < 0.1f) || (max > 1.5f)) {
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                        "Error37", maxSpeedField.getText()), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                speedFactorField.setText("0.6");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                    "Error38", maxSpeedField.getText()), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            speedFactorField.setText("0.6");
            return false;
        }
        _maxSpeed = max;
        _rampRate = rampRateBox.getSelectedIndex();
        _resistanceWheels = resistanceWheelsBox.isSelected();
        _runInReverse = runInReverseBox.isSelected();
        _soundDecoder = soundDecoderBox.isSelected();
        try {
            factor = Float.parseFloat(maxTrainLengthField.getText());
            if ((factor < 0.0f) || (factor > 10000.0f)) {
                JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                        "Error31", maxTrainLengthField.getText()), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                maxTrainLengthField.setText("18.0");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                    "Error32", maxTrainLengthField.getText()), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            maxTrainLengthField.setText("18.0");
            return false;
        }
        _maxTrainLength = factor;
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

    private final static Logger log = LoggerFactory.getLogger(ActivateTrainFrame.class.getName());
}
