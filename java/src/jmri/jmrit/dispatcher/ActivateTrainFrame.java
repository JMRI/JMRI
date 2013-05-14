// ActivateTrainFrame.java

package jmri.jmrit.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import jmri.util.*;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.List;

/**
 * Displays the Activate New Train dialog and processes information entered there.
 *
 * <P>
 * This module works with Dispatcher, which initiates the display of the dialog. 
 * Dispatcher also creates the ActiveTrain.
 *
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is open source software; you can redistribute it and/or modify it 
 * under the terms of version 2 of the GNU General Public License as 
 * published by the Free Software Foundation. See the "COPYING" file for 
 * a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author			Dave Duchamp   Copyright (C) 2009
 * @version			$Revision$
 */
public class ActivateTrainFrame {

    public ActivateTrainFrame (DispatcherFrame d) {
		_dispatcher = d;
		_tiFile = new TrainInfoFile();
		if (_tiFile==null)
			log.error("Failed to create TrainInfoFile object when constructing ActivateTrainFrame");
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
	private JmriJFrame initiateFrame=null;
	private Container initiatePane = null;
	private JComboBox transitSelectBox = new JComboBox();
	private ArrayList<Transit> transitBoxList = new ArrayList<Transit>();
	private JLabel trainBoxLabel = new JLabel("     "+rb.getString("TrainBoxLabel")+":");
	private JComboBox trainSelectBox = new JComboBox();
	private ArrayList<RosterEntry> trainBoxList = new ArrayList<RosterEntry>();
	private JLabel trainFieldLabel = new JLabel(rb.getString("TrainBoxLabel")+":");
	private JTextField trainNameField = new JTextField(10);
	private JLabel dccAddressFieldLabel = new JLabel("     "+rb.getString("DccAddressFieldLabel")+":");
	private JTextField dccAddressField = new JTextField(6);
	private JCheckBox inTransitBox = new JCheckBox(rb.getString("TrainInTransit"));
	private JComboBox startingBlockBox = new JComboBox();
	private ArrayList<Block> startingBlockBoxList = new ArrayList<Block>();
	private ArrayList<Integer> startingBlockSeqList = new ArrayList<Integer>();
	private JComboBox destinationBlockBox = new JComboBox();
	private ArrayList<Block> destinationBlockBoxList = new ArrayList<Block>();
	private ArrayList<Integer> destinationBlockSeqList = new ArrayList<Integer>();
	private JButton addNewTrainButton = null;
	private JButton loadButton = null;
	private JButton saveButton = null;
	private JButton deleteButton = null;
	private JCheckBox autoRunBox = new JCheckBox(rb.getString("AutoRun"));
	private JTextField priorityField = new JTextField(6);
	private JCheckBox resetWhenDoneBox = new JCheckBox(rb.getString("ResetWhenDone"));
	private JCheckBox reverseAtEndBox = new JCheckBox(rb.getString("ReverseAtEnd"));
    int delayedStartInt[] = new int[]{ActiveTrain.NODELAY, ActiveTrain.TIMEDDELAY, ActiveTrain.SENSORDELAY};
    String delayedStartString[] = new String[]{Bundle.getMessage("DelayedStartNone"), Bundle.getMessage("DelayedStartTimed"), Bundle.getMessage("DelayedStartSensor")};
    private JComboBox delayedStartBox = new JComboBox(delayedStartString);
    private jmri.util.swing.JmriBeanComboBox delaySensor = new jmri.util.swing.JmriBeanComboBox(jmri.InstanceManager.sensorManagerInstance());
    
	private JTextField departureHrField = new JTextField(2);
	private JTextField departureMinField = new JTextField(2);
    private JLabel departureTimeLabel = new JLabel(rb.getString("DepartureTime"));
    private JLabel departureSepLabel = new JLabel(":");
    
	private JComboBox trainTypeBox = new JComboBox();	
	// Note: See also items related to automatically running trains near the end of this module
	
    /**
    * Open up a new train window, for a given roster entry located in a specific block
    */
    public void initiateTrain(ActionEvent e, RosterEntry re, Block b){
        initiateTrain(e);
        if(_TrainsFromRoster){
            setComboBox(trainSelectBox, re.getId());
            //Add in some bits of code as some point to filter down the transits that can be used.
        }
    }
    
	/**
	 * Displays a window that allows a new ActiveTrain to be activated
	 *
	 * Called by Dispatcher in response to the dispatcher clicking the New Train button
	 */
	protected void initiateTrain(ActionEvent e) {
		// set Dispatcher defaults
		_TrainsFromRoster = _dispatcher.getTrainsFromRoster();
		_TrainsFromTrains = _dispatcher.getTrainsFromTrains();
		_TrainsFromUser = _dispatcher.getTrainsFromUser();
		_ActiveTrainsList = _dispatcher.getActiveTrainsList();
		// create window if needed
        if (initiateFrame==null) {
            initiateFrame = new JmriJFrame(rb.getString("AddTrainTitle"),false,true);
            initiateFrame.addHelpMenu("package.jmri.jmrit.dispatcher.NewTrain", true);
			initiatePane = initiateFrame.getContentPane();
            initiatePane.setLayout(new BoxLayout(initiateFrame.getContentPane(), BoxLayout.Y_AXIS));
			// add buttons to load and save train information
			JPanel p0 = new JPanel();
			p0.setLayout(new FlowLayout());
			p0.add(loadButton = new JButton(rb.getString("LoadButton")));
            loadButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadTrainInfo(e);
                }
            });
			loadButton.setToolTipText(rb.getString("LoadButtonHint"));
			p0.add(saveButton = new JButton(rb.getString("SaveButton")));
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveTrainInfo(e);
                }
            });
			saveButton.setToolTipText(rb.getString("SaveButtonHint"));
			p0.add(deleteButton = new JButton(rb.getString("DeleteButton")));
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteTrainInfo(e);
                }
            });
			deleteButton.setToolTipText(rb.getString("DeleteButtonHint"));
			initiatePane.add(p0);			
			initiatePane.add(new JSeparator());
			// add items relating to both manually run and automatic trains.
            JPanel p1 = new JPanel(); 
			p1.setLayout(new FlowLayout());
			p1.add(new JLabel(rb.getString("TransitBoxLabel")+" :"));
			p1.add(transitSelectBox);
            transitSelectBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handleTransitSelectionChanged(e);
                }
            });
			transitSelectBox.setToolTipText(rb.getString("TransitBoxHint"));
			p1.add(trainBoxLabel);
			p1.add(trainSelectBox);
			trainSelectBox.setToolTipText(rb.getString("TrainBoxHint"));
			initiatePane.add(p1);
            JPanel p1a = new JPanel(); 
			p1a.setLayout(new FlowLayout());
			p1a.add(trainFieldLabel);
			p1a.add(trainNameField);
			trainNameField.setToolTipText(rb.getString("TrainFieldHint"));
			p1a.add(dccAddressFieldLabel);
			p1a.add(dccAddressField);
			dccAddressField.setToolTipText(rb.getString("DccAddressFieldHint"));			
			initiatePane.add(p1a);
            JPanel p2 = new JPanel(); 
			p2.setLayout(new FlowLayout());
			p2.add(inTransitBox);
            inTransitBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handleInTransitClick(e);
                }
            });
			inTransitBox.setToolTipText(rb.getString("InTransitBoxHint"));
			initiatePane.add(p2);
            JPanel p3 = new JPanel(); 
			p3.setLayout(new FlowLayout());
			p3.add(new JLabel(rb.getString("StartingBlockBoxLabel")+" :"));
			p3.add(startingBlockBox);
			startingBlockBox.setToolTipText(rb.getString("StartingBlockBoxHint"));
            startingBlockBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handleStartingBlockSelectionChanged(e);
                }
            });
			initiatePane.add(p3);
            JPanel p4 = new JPanel(); 
			p4.setLayout(new FlowLayout());
			p4.add(new JLabel(rb.getString("DestinationBlockBoxLabel")+":"));
			p4.add(destinationBlockBox);
			destinationBlockBox.setToolTipText(rb.getString("DestinationBlockBoxHint"));
			initiatePane.add(p4);
            JPanel p6 = new JPanel(); 
			p6.setLayout(new FlowLayout());
			p6.add(resetWhenDoneBox);
			resetWhenDoneBox.setToolTipText(rb.getString("ResetWhenDoneBoxHint"));
			initiatePane.add(p6);
            JPanel p10 = new JPanel(); 
			p10.setLayout(new FlowLayout());
			p10.add(reverseAtEndBox);
			reverseAtEndBox.setToolTipText(rb.getString("ReverseAtEndBoxHint"));
			initiatePane.add(p10);
            JPanel p8 = new JPanel(); 
			p8.setLayout(new FlowLayout());
			p8.add(new JLabel(rb.getString("PriorityLabel")+" :"));
			p8.add(priorityField);
			priorityField.setToolTipText(rb.getString("PriorityHint"));
			priorityField.setText("5");    
			p8.add(new JLabel("     "));
			p8.add(new JLabel(rb.getString("TrainTypeBoxLabel")));
			initializeTrainTypeBox();
			p8.add(trainTypeBox);
			trainTypeBox.setSelectedIndex(1);
			trainTypeBox.setToolTipText(rb.getString("TrainTypeBoxHint"));
			initiatePane.add(p8);
            JPanel p9 = new JPanel(); 
			p9.setLayout(new FlowLayout());
            p9.add(new JLabel(Bundle.getMessage("DelayedStart")));
			p9.add(delayedStartBox);
			delayedStartBox.setToolTipText(rb.getString("DelayedStartHint"));
            delayedStartBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handleDelayStartClick(e);
                }
            });
			p9.add(departureTimeLabel);
			p9.add(departureHrField);
			departureHrField.setText("08");
			departureHrField.setToolTipText(rb.getString("DepartureTimeHrHint"));
			p9.add(departureSepLabel);
			p9.add(departureMinField);
			departureMinField.setText("00");
			departureMinField.setToolTipText(rb.getString("DepartureTimeMinHint"));
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
			autoRunBox.setToolTipText(rb.getString("AutoRunBoxHint"));
			autoRunBox.setSelected(false);
			initiatePane.add(p5);
			initiatePane.add(new JSeparator());
			initializeAutoRunItems();
			initiatePane.add(new JSeparator());
			JPanel p7 = new JPanel();
			p7.setLayout(new FlowLayout());
			JButton cancelButton = null;
			p7.add(cancelButton = new JButton(rb.getString("CancelButton")));
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelInitiateTrain(e);
                }
            });
			cancelButton.setToolTipText(rb.getString("CancelButtonHint"));
			p7.add (addNewTrainButton = new JButton(rb.getString("AddNewTrainButton")));
            addNewTrainButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addNewTrain(e);
                }
            });
			addNewTrainButton.setToolTipText(rb.getString("AddNewTrainButtonHint"));
			initiatePane.add(p7);
		}
		if (_TrainsFromRoster || _TrainsFromTrains) {
			trainBoxLabel.setVisible(true);
			trainSelectBox.setVisible(true);
			trainFieldLabel.setVisible(false);
			trainNameField.setVisible(false);
			dccAddressFieldLabel.setVisible(false);
			dccAddressField.setVisible(false);
		}
		else if (_TrainsFromUser) {
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
		initializeFreeTransitsCombo();
		initializeFreeTrainsCombo();
		initiateFrame.pack();
		initiateFrame.setVisible(true);	
	}
	private void initializeTrainTypeBox() {
		trainTypeBox.removeAllItems();
		trainTypeBox.addItem(rb.getString("None"));
		trainTypeBox.addItem(rb.getString("LOCAL_PASSENGER"));
		trainTypeBox.addItem(rb.getString("LOCAL_FREIGHT"));
		trainTypeBox.addItem(rb.getString("THROUGH_PASSENGER"));
		trainTypeBox.addItem(rb.getString("THROUGH_FREIGHT"));
		trainTypeBox.addItem(rb.getString("EXPRESS_PASSENGER"));
		trainTypeBox.addItem(rb.getString("EXPRESS_FREIGHT"));
		trainTypeBox.addItem(rb.getString("MOW"));
		// NOTE: The above must correspond in order and name to definitions in ActiveTrain.java.
	}	
	private void handleTransitSelectionChanged(ActionEvent e) {
		int index = transitSelectBox.getSelectedIndex();
		if (index<0) return;
		Transit t = transitBoxList.get(index);
		if ( (t!=null) && (t!=selectedTransit) ) {
			selectedTransit = t;
			initializeStartingBlockCombo();
			initializeDestinationBlockCombo();
			initiateFrame.pack();
		}
	}
	private void handleInTransitClick(ActionEvent e) {
		initializeStartingBlockCombo();
		initializeDestinationBlockCombo();
		initiateFrame.pack();
	}
	private boolean checkResetWhenDone() {
		if ((!reverseAtEndBox.isSelected()) && resetWhenDoneBox.isSelected() && 
					(!selectedTransit.canBeResetWhenDone())) {
			resetWhenDoneBox.setSelected(false);
			javax.swing.JOptionPane.showMessageDialog(initiateFrame, rb
					.getString("NoResetMessage"), rb.getString("InformationTitle"),
						javax.swing.JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		return true;
	}
    private void handleDelayStartClick(ActionEvent e){
        departureHrField.setVisible(false);
        departureMinField.setVisible(false);
        departureTimeLabel.setVisible(false);
        departureSepLabel.setVisible(false);
        delaySensor.setVisible(false);
        if (delayedStartBox.getSelectedItem().equals(Bundle.getMessage("DelayedStartTimed"))){
            departureHrField.setVisible(true);
            departureMinField.setVisible(true);
            departureTimeLabel.setVisible(true);
            departureSepLabel.setVisible(true);
        } else if (delayedStartBox.getSelectedItem().equals(Bundle.getMessage("DelayedStartSensor"))){
            delaySensor.setVisible(true);
        }
    }
    
	private void handleAutoRunClick(ActionEvent e) {
		if (autoRunBox.isSelected()) {
			showAutoRunItems();
		}
		else {
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
		if (selectedTransit==null) {
			// no transits available
			JOptionPane.showMessageDialog(initiateFrame, rb.getString("Error15"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			cancelInitiateTrain(null);
			return;
		}				
		String transitName = selectedTransit.getSystemName();
		String trainName = "";
		int index = startingBlockBox.getSelectedIndex();
		if (index<0) return;
		String startBlockName = startingBlockBoxList.get(index).getSystemName();
		int startBlockSeq = startingBlockSeqList.get(index).intValue();
		index = destinationBlockBox.getSelectedIndex();
		if (index<0) return;
		String endBlockName = destinationBlockBoxList.get(index).getSystemName();
		int endBlockSeq = destinationBlockSeqList.get(index).intValue();
		boolean autoRun = autoRunBox.isSelected();
		if (!checkResetWhenDone()) return;
		boolean resetWhenDone = resetWhenDoneBox.isSelected();
		boolean reverseAtEnd = reverseAtEndBox.isSelected();
		int delayedStart = delayModeFromBox(delayedStartBox);
		int departureTimeHours = 8;
		try {
			departureTimeHours = Integer.parseInt(departureHrField.getText());
			if ( (departureTimeHours<0) || (departureTimeHours>23) ) {
				JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
					"BadEntry3"),new Object[] { departureHrField.getText() }), 
							rb.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);		
				log.warn ("Range error in Departure Time Hours field");
				return;
			}
		}
		catch (NumberFormatException ehr) {
			JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
					"BadEntry2"),new Object[] { departureHrField.getText() }), 
							rb.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);		
			log.warn ("Conversion exception in departure time hours field");
			return;
		}
		int departureTimeMinutes = 8;
		try {
			departureTimeMinutes = Integer.parseInt(departureMinField.getText());
			if ( (departureTimeMinutes<0) || (departureTimeMinutes>59) ) {
				JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
					"BadEntry3"),new Object[] { departureMinField.getText() }), 
							rb.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
				log.warn ("Range error in Departure Time Minutes field");
				return;
			}
		}
		catch (NumberFormatException emn) {
			JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
					"BadEntry2"),new Object[] { departureMinField.getText() }), 
							rb.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);		
			log.warn ("Conversion exception in departure time minutes field");
			return;
		}
		int tSource = 0;
		String dccAddress = "unknown";
		if (_TrainsFromRoster) {
			index = trainSelectBox.getSelectedIndex();
			if (index<0) {
				// no trains available
				JOptionPane.showMessageDialog(initiateFrame, rb.getString("Error14"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
				cancelInitiateTrain(null);
				return;
			}
			trainName = (String)trainSelectBox.getSelectedItem();
			RosterEntry r = trainBoxList.get(index);
			dccAddress = r.getDccAddress();
			tSource = ActiveTrain.ROSTER;
            
            if(trainTypeBox.getSelectedIndex()!=0 && 
                (r.getAttribute("DisptacherTrainType")==null || 
                   !r.getAttribute("DispatcherTrainType").equals(""+trainTypeBox.getSelectedItem()))){
                r.putAttribute("DispatcherTrainType", ""+trainTypeBox.getSelectedItem());
                r.updateFile();
                Roster.writeRosterFile();
            }
		}
		else if (_TrainsFromTrains) {
			tSource = ActiveTrain.OPERATIONS;
			index = trainSelectBox.getSelectedIndex();
			if (index<0) {
				// no trains available
				JOptionPane.showMessageDialog(initiateFrame, rb.getString("Error14"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
				cancelInitiateTrain(null);
				return;
			}
			trainName = (String)trainSelectBox.getSelectedItem();
		}
		else if (_TrainsFromUser) {
			trainName = trainNameField.getText();
			if ( (trainName==null) || trainName.equals("") ) {
				// no train name entered
				JOptionPane.showMessageDialog(initiateFrame, rb.getString("Error14"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (!isTrainFree(trainName)) {
				// train name is already in use by an Active Train
				JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
						"Error24"),new Object[] { trainName }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);	
				return;
			}	
			dccAddress = dccAddressField.getText();
			int address = -1;
			try {
				address = Integer.parseInt(dccAddress);
			}
			catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(initiateFrame, rb.getString("Error23"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
				log.error ("Conversion exception in dccAddress field");
				return;
			}
			if ( (address<1) || (address>9999) ) {
				JOptionPane.showMessageDialog(initiateFrame, rb.getString("Error23"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			tSource = ActiveTrain.USER;
		}
		int priority = 5;
		try {
			priority = Integer.parseInt(priorityField.getText());
		} 
		catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
						"BadEntry"),new Object[] { priorityField.getText() }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);		
			log.error ("Conversion exception in priority field");
			return;
		}
		int trainType = trainTypeBox.getSelectedIndex();
		if (autoRunBox.isSelected()) {
			if (!readAutoRunItems()) {
				return;
			}
		}
		
		// create a new Active Train
		ActiveTrain at = _dispatcher.createActiveTrain ( transitName, trainName, tSource, startBlockName, 
					startBlockSeq, endBlockName, endBlockSeq, autoRun, dccAddress, priority, 
						resetWhenDone, reverseAtEnd, true, initiateFrame);
		if (at==null) return;  // error message sent by createActiveTrain
		if(tSource == ActiveTrain.ROSTER)
            at.setRosterEntry(trainBoxList.get(trainSelectBox.getSelectedIndex()));
        at.setDelayedStart(delayedStart);
		at.setDepartureTimeHr(departureTimeHours);
		at.setDepartureTimeMin(departureTimeMinutes);
        at.setDelaySensor((jmri.Sensor)delaySensor.getSelectedBean());
		if (_dispatcher.isFastClockTimeGE(departureTimeHours,departureTimeMinutes)) {
			at.setStarted();
		}
		at.setTrainType(trainType);
		if (autoRunBox.isSelected()) {
			AutoActiveTrain aat = new AutoActiveTrain(at);
			setAutoRunItems(aat);
			if (!aat.initialize()) {
				JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
						"Error27"),new Object[] { at.getTrainName() }), rb.getString("InformationTitle"),
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
	private void initializeFreeTransitsCombo() {
		ArrayList<String> allTransits = (ArrayList<String>)_TransitManager.getSystemNameList();
		transitSelectBox.removeAllItems();
		transitBoxList.clear();
		for (int i = 0; i<allTransits.size(); i++) {
			String tName = allTransits.get(i);
			Transit t = _TransitManager.getBySystemName(tName);
			boolean free = true;
			for (int j = 0; j<_ActiveTrainsList.size(); j++) {
				ActiveTrain at = _ActiveTrainsList.get(j);
				if (t == at.getTransit()) free = false;
			}
			if (free) {
				transitBoxList.add(t);
				if ( (t.getUserName()!=null) && (!t.getUserName().equals("")) )
					tName = tName+"( "+t.getUserName()+" )";
				transitSelectBox.addItem(tName);
			}
		}
		if (transitBoxList.size()>0) {
			transitSelectBox.setSelectedIndex(0);
			selectedTransit = transitBoxList.get(0);
		}
		else {
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
			List<RosterEntry> l = Roster.instance().matchingList(null, null, null, null, null, null, null );
			if (l.size()>0) {
				for (int i = 0; i<l.size(); i++) {
					RosterEntry r = l.get(i);
					String rName = r.titleString();
					if (isTrainFree(rName)) {
						trainBoxList.add(r);
						trainSelectBox.addItem(rName);
					}
				}
			}
            if(trainSelectBoxListener == null){
                trainSelectBoxListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        RosterEntry r = trainBoxList.get(trainSelectBox.getSelectedIndex());
                        if(r.getAttribute("DispatcherTrainType")!=null && !r.getAttribute("DispatcherTrainType").equals("")){
                            trainTypeBox.setSelectedItem(r.getAttribute("DispatcherTrainType"));
                        }
                    }
                };
            }
            trainSelectBox.addActionListener(trainSelectBoxListener);
		}
		else if (_TrainsFromTrains) {
			// initialize free trains from operations
			List<String> l = TrainManager.instance().getTrainsByNameList();
			if (l.size()>0) {
				for (int i = 0; i<l.size(); i++) {
					Train t = TrainManager.instance().getTrainById(l.get(i));
					if (t!=null) {
						String rName = t.getName();
						if (isTrainFree(rName)) {
							trainSelectBox.addItem(rName);
						}
					}
				}
			}	
		}
		if (trainBoxList.size()>0) {
			trainSelectBox.setSelectedIndex(0);
		}
	}
	
	private boolean isTrainFree (String rName) {
		for (int j = 0; j<_ActiveTrainsList.size(); j++) {
			ActiveTrain at = _ActiveTrainsList.get(j);
			if (rName.equals(at.getTrainName())) return false;
		}
		return true;
	}
		
	private void initializeStartingBlockCombo() {
		startingBlockBox.removeAllItems();
		startingBlockBoxList.clear();
		if (inTransitBox.isSelected()) {
			startingBlockBoxList = selectedTransit.getInternalBlocksList();
		}
		else {
			startingBlockBoxList = selectedTransit.getEntryBlocksList();
		}
		startingBlockSeqList = selectedTransit.getBlockSeqList();
		for (int i = 0; i<startingBlockBoxList.size(); i++) {
			Block b = startingBlockBoxList.get(i);
			int seq = startingBlockSeqList.get(i).intValue();
			startingBlockBox.addItem(getBlockName(b)+"-"+seq);
		}
	}
	private void initializeDestinationBlockCombo() {
		destinationBlockBox.removeAllItems();
		destinationBlockBoxList.clear();
		int index = startingBlockBox.getSelectedIndex();
		if (index<0) return;
		Block startBlock = startingBlockBoxList.get(index);
		destinationBlockBoxList = selectedTransit.getDestinationBlocksList(
					startBlock, inTransitBox.isSelected());
		destinationBlockSeqList = selectedTransit.getDestBlocksSeqList();
		for (int i = 0; i<destinationBlockBoxList.size(); i++) {
			Block b = destinationBlockBoxList.get(i);
			String bName = getBlockName(b);
			if (selectedTransit.getBlockCount(b)>1) {
				int seq = destinationBlockSeqList.get(i).intValue();
				bName = bName+"-"+seq;
			}
			destinationBlockBox.addItem(bName);
		}
	}
	private String getBlockName(Block b) {
		if (b!=null) {
			String sName = b.getSystemName();
			String uName = b.getUserName();
			if ( (uName!=null) && (uName!="") ) {
				return (sName+"( "+uName+" )");
			}
			return sName;
		}
		return " ";
	}
	protected void showActivateFrame() {
		if (initiateFrame!=null) {
			initiateFrame.setVisible(true);	
		}
		else {
			_dispatcher.newTrainDone(null);
		}
	}
    
    public void showActivateFrame(RosterEntry re){
        showActivateFrame();
    }
    
	private void loadTrainInfo(ActionEvent e) {
		String[] names = _tiFile.getTrainInfoFileNames();
		TrainInfo info = null;
		if (names.length > 0) {
			Object selName = JOptionPane.showInputDialog(initiateFrame, 
					rb.getString("LoadTrainChoice"), rb.getString("LoadTrainTitle"), 
						JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
			if ( (selName == null) || (((String)selName).equals("")) ) return;
			_trainInfoName = (String)selName;
			try {
				info = _tiFile.readTrainInfo((String)selName);
				if (info!=null) {
					// process the information just read
					trainInfoToDialog(info);			
				}	
			}
			catch (java.io.IOException ioe) {
				log.error("IO Exception when reading train info file "+ioe);
			}
			catch (org.jdom.JDOMException jde) {
				log.error("JDOM Exception when reading train info file "+jde);
			}
		}
        handleDelayStartClick(null);
	}
	private void saveTrainInfo(ActionEvent e) {
		TrainInfo info = dialogToTrainInfo();
		if (info!=null) {
			// get file name
			String eName = "";
			eName = JOptionPane.showInputDialog(initiateFrame,
								rb.getString("EnterFileName")+" :",_trainInfoName);
			if (eName.length()<1) {
				JOptionPane.showMessageDialog(initiateFrame, rb.getString("Error25"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			String fileName = normalizeXmlFileName(eName);
			_trainInfoName = fileName;
			// check if train info file name is in use
			String[] names = _tiFile.getTrainInfoFileNames();
			if (names.length > 0) {
				boolean found = false;
				for (int i = 0; i<names.length; i++) {
					if (fileName.equals(names[i])) found = true;
				}
				if (found) {
					// file by that name is already present
					int selectedValue = JOptionPane.showOptionDialog(initiateFrame,
						java.text.MessageFormat.format(rb.getString("Question3"), new Object[] {fileName}),						
							rb.getString("WarningTitle"), JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,null, new Object[]{rb.getString("FileYes"),
							rb.getString("FileNo")},rb.getString("FileNo"));
					if (selectedValue == 1) return;   // return without writing if "No" response
				}
			}
			// write the Train Info file
			try {
				_tiFile.writeTrainInfo(info, fileName);
			} 
			//catch (org.jdom.JDOMException jde) { 
			//	log.error("JDOM exception writing Train Info: "+jde); 
			//}                           
			catch (java.io.IOException ioe) { 
				log.error("IO exception writing Train Info: "+ioe); 
			}   
		}
	}
	private void deleteTrainInfo(ActionEvent e) {
		String[] names = _tiFile.getTrainInfoFileNames();
		if (names.length > 0) {
			Object selName = JOptionPane.showInputDialog(initiateFrame, 
					rb.getString("DeleteTrainChoice"), rb.getString("DeleteTrainTitle"), 
						JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
			if ( (selName == null) || (((String)selName).equals("")) ) return;
			_tiFile.deleteTrainInfoFile((String)selName);
		}
	}
	private void trainInfoToDialog(TrainInfo info) {
		if(!setComboBox(transitSelectBox, info.getTransitName())) {
			log.warn("Transit "+info.getTransitName()+" from file not in Transit menu");
			JOptionPane.showMessageDialog(initiateFrame,
					java.text.MessageFormat.format(rb.getString("TransitWarn"),
						new Object[]{info.getTransitName()}), 
							null,JOptionPane.WARNING_MESSAGE);
		}
		_TrainsFromRoster = info.getTrainFromRoster();
		_TrainsFromTrains = info.getTrainFromTrains();
		_TrainsFromUser = info.getTrainFromUser();
		if (_TrainsFromRoster || _TrainsFromTrains) {
			initializeFreeTrainsCombo();
			if(!setComboBox(trainSelectBox, info.getTrainName())) {
				log.warn("Train "+info.getTrainName()+" from file not in Train menu");
				JOptionPane.showMessageDialog(initiateFrame,
					java.text.MessageFormat.format(rb.getString("TrainWarn"),
						new Object[]{info.getTrainName()}), 
							null,JOptionPane.WARNING_MESSAGE);
			}
		}
		else if (_TrainsFromUser) {
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
		setComboBox(trainTypeBox,info.getTrainType());
		autoRunBox.setSelected(info.getRunAuto());
		autoTrainInfoToDialog(info);
	}
	private TrainInfo dialogToTrainInfo() {
		TrainInfo info = new TrainInfo();
		info.setTransitName((String)transitSelectBox.getSelectedItem());
		if (_TrainsFromRoster || _TrainsFromTrains) {
			info.setTrainName((String)trainSelectBox.getSelectedItem());
			info.setDCCAddress(" ");
		}
		else if (_TrainsFromUser) {
			info.setTrainName(trainNameField.getText());
			info.setDCCAddress(dccAddressField.getText());
		}
		info.setTrainInTransit(inTransitBox.isSelected());
		info.setStartBlockName((String)startingBlockBox.getSelectedItem());
		info.setDestinationBlockName((String)destinationBlockBox.getSelectedItem());
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
		info.setTrainType((String)trainTypeBox.getSelectedItem());
		info.setRunAuto(autoRunBox.isSelected());
		autoRunItemsToTrainInfo(info);
		return info;
	}
	// Normalizes a suggested xml file name.  Returns null string if a valid name cannot be assembled 
	private String normalizeXmlFileName(String name) {
		if (name.length()<1)
			return "";
		String newName = name;
		// strip off .xml or .XML if present
		if ( (name.endsWith(".xml")) || (name.endsWith(".XML")) ) {
			newName = name.substring(0,name.length()-4);
			if (newName.length()<1) 
				return "";
		}
		// replace all non-alphanumeric characters with underscore
		newName = newName.replaceAll("[\\W]","_");
		return (newName+".xml");
	}
	private boolean setComboBox(JComboBox box, String txt) {
		boolean found = false;
		for (int i = 0; i<box.getItemCount(); i++) {
			if (txt.equals(box.getItemAt(i))) {
				box.setSelectedIndex(i);
				found = true;
			}
		}
		if (!found) box.setSelectedIndex(0);				
		return found;
	}
    
    int delayModeFromBox(JComboBox box) {
        String mode = (String)box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, delayedStartInt, delayedStartString);
        
        if (result<0) { 
            log.warn("unexpected mode string in turnoutMode: "+mode);
            throw new IllegalArgumentException();
        }
        return result;
    }
    
    void setDelayModeBox(int mode, JComboBox box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, delayedStartInt, delayedStartString);
        box.setSelectedItem(result);
    }
	
	/**
	 * The following are for items that are only for automatic running of ActiveTrains
	 * They are isolated here to simplify changing them in the future
	 *		initializeAutoRunItems - initializes the display of auto run items in this window
	 *		initializeAutoRunValues - initializes the values of auto run items from values in a 
	 *				saved train info file
	 *		hideAutoRunItems - hides all auto run items in this window
	 *		showAutoRunItems - shows all auto run items in this window
	 *		autoTrainInfoToDialog - gets auto run items from a train info, puts values in items, 
	 *				and initializes auto run dialog items
	 *		autoTrainItemsToTrainInfo - copies values of auto run items to train info for saving
	 *				to a file
	 *		readAutoRunItems - reads and checks values of all auto run items.
	 *				returns true if OK, sends appropriate messages and returns false if not OK
	 *		setAutoRunItems - sets the user entered auto run items in the new AutoActiveTrain
	 */
	// auto run items in ActivateTrainFrame
	private	JPanel pa1 = new JPanel();
	private JLabel speedFactorLabel = new JLabel(rb.getString("SpeedFactorLabel"));
	private JTextField speedFactorField = new JTextField(5);
	private JLabel maxSpeedLabel = new JLabel(rb.getString("MaxSpeedLabel"));
	private JTextField maxSpeedField = new JTextField(5);
	private	JPanel pa2 = new JPanel();
	private JLabel rampRateLabel = new JLabel(rb.getString("RampRateBoxLabel"));
	private JComboBox rampRateBox = new JComboBox();
	private	JPanel pa3 = new JPanel();
	private JCheckBox soundDecoderBox = new JCheckBox(rb.getString("SoundDecoder"));
	private JCheckBox runInReverseBox = new JCheckBox(rb.getString("RunInReverse"));
	private	JPanel pa4 = new JPanel();
	private JCheckBox resistanceWheelsBox = new JCheckBox(rb.getString("ResistanceWheels"));
	private JLabel trainLengthLabel = new JLabel(rb.getString("MaxTrainLengthLabel"));
	private JTextField maxTrainLengthField = new JTextField(5);
	// auto run variables
	float _speedFactor =1.0f;
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
		speedFactorField.setToolTipText(rb.getString("SpeedFactorHint"));
		pa1.add(new JLabel("   "));
		pa1.add(maxSpeedLabel);
		pa1.add(maxSpeedField);
		maxSpeedField.setToolTipText(rb.getString("MaxSpeedHint"));
		initiatePane.add(pa1);
		pa2.setLayout(new FlowLayout());
		pa2.add(rampRateLabel);
		pa2.add(rampRateBox);
		rampRateBox.setToolTipText(rb.getString("RampRateBoxHint"));
		initiatePane.add(pa2);
		pa3.setLayout(new FlowLayout());
		pa3.add(soundDecoderBox);
		soundDecoderBox.setToolTipText(rb.getString("SoundDecoderBoxHint"));
		pa3.add(new JLabel("   "));
		pa3.add(runInReverseBox);
		runInReverseBox.setToolTipText(rb.getString("RunInReverseBoxHint"));
		initiatePane.add(pa3);
		pa4.setLayout(new FlowLayout());
		pa4.add(resistanceWheelsBox);
		resistanceWheelsBox.setToolTipText(rb.getString("ResistanceWheelsBoxHint"));
		pa4.add(new JLabel("   "));
		pa4.add(trainLengthLabel);
		pa4.add(maxTrainLengthField);
		maxTrainLengthField.setToolTipText(rb.getString("MaxTrainLengthHint"));
		initiatePane.add(pa4);
		hideAutoRunItems();   // initialize with auto run items hidden
		initializeAutoRunValues();
	}
	private void initializeAutoRunValues() {
		speedFactorField.setText(""+_speedFactor);
		maxSpeedField.setText(""+_maxSpeed);
		rampRateBox.setSelectedIndex(_rampRate);
		resistanceWheelsBox.setSelected(_resistanceWheels);
		soundDecoderBox.setSelected(_soundDecoder);
		runInReverseBox.setSelected(_runInReverse);
		maxTrainLengthField.setText(""+_maxTrainLength);
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
		}
		else {
			hideAutoRunItems();
		}
		initiateFrame.pack();
	}
	private void autoRunItemsToTrainInfo(TrainInfo info) {
		info.setSpeedFactor(speedFactorField.getText());
		info.setMaxSpeed(maxSpeedField.getText());
		info.setRampRate((String)rampRateBox.getSelectedItem());
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
			if ((factor<0.1f) || (factor>1.5f)) {
				JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
						"Error29"),new Object[] { speedFactorField.getText() }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);	
				speedFactorField.setText("1.0");
				return false;
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
				"Error30"),new Object[] { speedFactorField.getText() }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);	
				speedFactorField.setText("1.0");
				return false;
		}
		_speedFactor = factor;
		float max = 0.6f;
		try {
			max = Float.parseFloat(maxSpeedField.getText());
			if ((max<0.1f) || (max>1.5f)) {
				JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
						"Error37"),new Object[] { maxSpeedField.getText() }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);	
				speedFactorField.setText("0.6");
				return false;
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
				"Error38"),new Object[] { maxSpeedField.getText() }), rb.getString("ErrorTitle"),
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
			if ((factor<0.0f) || (factor>10000.0f)) {
				JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
						"Error31"),new Object[] { maxTrainLengthField.getText() }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);	
				maxTrainLengthField.setText("18.0");
				return false;
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
				"Error32"),new Object[] { maxTrainLengthField.getText() }), rb.getString("ErrorTitle"),
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
		rampRateBox.addItem(rb.getString("RAMP_NONE")); 
		rampRateBox.addItem(rb.getString("RAMP_FAST")); 
		rampRateBox.addItem(rb.getString("RAMP_MEDIUM")); 
		rampRateBox.addItem(rb.getString("RAMP_MED_SLOW")); 
		rampRateBox.addItem(rb.getString("RAMP_SLOW"));
		// Note: the order above must correspond to the numbers in AutoActiveTrain.java
	}
   
    static Logger log = LoggerFactory.getLogger(ActivateTrainFrame.class.getName());
}
	
			
