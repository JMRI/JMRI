// ActivateTrainFrame.java

package jmri.jmrit.dispatcher;

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
 * @version			$Revision: 1.4 $
 */
public class ActivateTrainFrame extends jmri.util.JmriJFrame {

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
	private JCheckBox autoRunBox = new JCheckBox(rb.getString("AutoRun"));
	private JTextField priorityField = new JTextField(6);
	private JCheckBox resetWhenDoneBox = new JCheckBox(rb.getString("ResetWhenDone"));
	
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
            initiateFrame = new JmriJFrame(rb.getString("AddTrainTitle"));
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
			p6.add(new JLabel(rb.getString("PriorityLabel")+" :"));
			p6.add(priorityField);
			priorityField.setToolTipText(rb.getString("PriorityHint"));
			priorityField.setText("5");    
			p6.add(new JLabel("     "));
			p6.add(resetWhenDoneBox);
            resetWhenDoneBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handleResetWhenDoneClick(e);
                }
            });
			resetWhenDoneBox.setToolTipText(rb.getString("ResetWhenDoneBoxHint"));
			initiatePane.add(p6);
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
			initiatePane.add(p5);
			initiatePane.add(new JSeparator());
			// items related to auto run of trains  
			
			
			
			// end of items related to auto run of trains
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
// here add call to initialize auto run items

		initializeFreeTransitsCombo();
		initializeFreeTrainsCombo();	
		initiateFrame.pack();
		initiateFrame.setVisible(true);	
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
	private void handleResetWhenDoneClick(ActionEvent e) {
		if (!selectedTransit.canBeResetWhenDone()) {
			resetWhenDoneBox.setSelected(false);
			javax.swing.JOptionPane.showMessageDialog(initiateFrame, rb
					.getString("NoResetMessage"), rb.getString("InformationTitle"),
						javax.swing.JOptionPane.INFORMATION_MESSAGE);
		}			
	}
	private void handleAutoRunClick(ActionEvent e) {
// here add code for requesting automatic running and remove the message below.
		javax.swing.JOptionPane.showMessageDialog(initiateFrame, rb
				.getString("NoAutoRunMessage"), rb.getString("InformationTitle"),
						javax.swing.JOptionPane.INFORMATION_MESSAGE);
		autoRunBox.setSelected(false);
	}
	private void handleStartingBlockSelectionChanged(ActionEvent e) {
		initializeDestinationBlockCombo();
		initiateFrame.pack();
	}
	private void cancelInitiateTrain(ActionEvent e) {
		initiateFrame.setVisible(false);
		initiateFrame.dispose();  // prevent this window from being listed in the Window menu.
		initiateFrame = null;
		_dispatcher.newTrainDone();
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
		boolean resetWhenDone = resetWhenDoneBox.isSelected();
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
			catch (Exception ex) {
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
		catch (Exception ex) {
			JOptionPane.showMessageDialog(initiateFrame,java.text.MessageFormat.format(rb.getString(
						"BadEntry"),new Object[] { priorityField.getText() }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);		
			log.error ("Conversion exception in priority field");
			return;
		}
		// create a new Active Train
		ActiveTrain at = _dispatcher.createActiveTrain ( transitName, trainName, tSource, startBlockName, 
					startBlockSeq, endBlockName, endBlockSeq, autoRun, dccAddress, priority, 
						resetWhenDone, true, initiateFrame);
		if (at==null) return;  // error message sent by createActiveTrain
		else if (autoRunBox.isSelected()) {
		
// here add code to complete creation of an automatically running train
		
		}
		initiateFrame.setVisible(false);
		initiateFrame.dispose();  // prevent this window from being listed in the Window menu.
		initiateFrame = null;
		_dispatcher.newTrainDone();
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
	private void initializeFreeTrainsCombo() {
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
		}
		else if (_TrainsFromTrains) {
			// initialize free trains from operations
			List<String> l = TrainManager.instance().getTrainsByNameList();
			if (l.size()>0) {
				for (int i = 0; i<l.size(); i++) {
					String rName = l.get(i);
					Train t = TrainManager.instance().getTrainByName(rName);
					if (t!=null) {
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
			_dispatcher.newTrainDone();
		}
	}
	private void loadTrainInfo(ActionEvent e) {
		String[] names = _tiFile.getTrainInfoFileNames();
		TrainInfo info = null;
		if (names.length > 0) {
			Object selName = JOptionPane.showInputDialog(initiateFrame, 
					rb.getString("LoadTrainChoice"), rb.getString("LoadTrainTitle"), 
						JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
			if ( (selName == null) || (((String)selName).equals("")) ) return;
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
	}
	private void saveTrainInfo(ActionEvent e) {
		TrainInfo info = dialogToTrainInfo();
		if (info!=null) {
			// get file name
			String eName = "";
			eName = JOptionPane.showInputDialog(initiateFrame,
								rb.getString("EnterFileName")+" :");
			if (eName.length()<1) {
				JOptionPane.showMessageDialog(initiateFrame, rb.getString("Error25"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			String fileName = normalizeXmlFileName(eName);
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
			catch (org.jdom.JDOMException jde) { 
				log.error("JDOM exception writing Train Info: "+jde); 
			}                           
			catch (java.io.IOException ioe) { 
				log.error("IO exception writing Train Info: "+ioe); 
			}   
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
		autoRunBox.setSelected(info.getRunAuto());
// here add items for auto running		
		
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
		info.setRunAuto(autoRunBox.isSelected());
// here add auto run items to Train Info object
		
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
   
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ActivateTrainFrame.class.getName());
}
	
			
