// DispatcherFrame.java

package jmri.jmrit.dispatcher;

import jmri.*;
import jmri.util.*;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.List;

/**
 * Dispatcher functionality, working with Sections, Transits and ActiveTrain.
 *
 * <P>
 * Dispatcher serves as the manager for ActiveTrains. All allocation of Sections 
 *	to ActiveTrains is performed here.
 * <P> 
 * Programming Note: public methods may be addressed externally via 
 *			jmri.jmrit.dispatcher.DispatcherFrame.instance().  ...
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
 * @author			Dave Duchamp   Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */
public class DispatcherFrame extends jmri.util.JmriJFrame {

    public DispatcherFrame () {
		_instance = this;
		openDispatcherWindow();
		initializeOptions();
		autoTurnouts = new AutoTurnouts(this);
		if (autoTurnouts==null)
			log.error("Failed to create AutoTurnouts object when constructing Dispatcher");
     }

	static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");
			
	// Dispatcher options (saved to disk if user requests, and restored if present)
	private LayoutEditor _LE = null;
	private boolean _UseConnectivity = false;
	private boolean _HasOccupancyDetection = false; // "true" if blocks have occupancy detection
	private boolean _TrainsFromRoster = true;
	private boolean _TrainsFromTrains = false;
	private boolean _TrainsFromUser = false;
	private boolean _AutoAllocate = false;
	private boolean _AutoTurnouts = false;
	private boolean _ShortActiveTrainNames = false;
	private boolean _ShortNameInBlock = true;
			
	// operational instance variables
	private ArrayList activeTrainsList = new ArrayList();  // list of ActiveTrain objects
	private ArrayList allTransits = new ArrayList();  // list of Transit objects
	private TransitManager transitManager = InstanceManager.transitManagerInstance();
	private ArrayList allocationRequests = new ArrayList();  // List of AllocatedRequest objects
	private ArrayList allocatedSections = new ArrayList();  // List of AllocatedSection objects
	private boolean optionsRead = false;
	private AutoTurnouts autoTurnouts = null;
			
	// dispatcher window variables
	protected JmriJFrame dispatcherFrame=null;
	private Container contentPane = null;
	private ActiveTrainsTableModel activeTrainsTableModel = null;
	private JButton addTrainButton = null;
	private JButton showAllocatedButton = null;
	private JButton terminateTrainButton = null;
	private JButton allocateExtraButton = null;
	private AllocationRequestTableModel allocationRequestTableModel = null;
	
	void initializeOptions() {
		if (optionsRead) return;
		optionsRead = true;
		try {
			new OptionsFile().readDispatcherOptions(this);
		} 
		catch (org.jdom.JDOMException jde) {
			log.error("JDOM Exception when retreiving dispatcher options "+jde);
		}				
		catch (java.io.IOException ioe) {
			log.error("I/O Exception when retreiving dispatcher options "+ioe);
		}
	}			
	void openDispatcherWindow() {
		if (dispatcherFrame==null) {
			dispatcherFrame = this;
			dispatcherFrame.setTitle(rb.getString("TitleDispatcher"));
			JMenuBar menuBar = new JMenuBar();
			menuBar.add(new OptionsMenu(this));
			setJMenuBar(menuBar);
            dispatcherFrame.addHelpMenu("package.jmri.jmrit.dispatcher.Dispatcher", true);
			contentPane = dispatcherFrame.getContentPane();
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			// set up active trains table
			JPanel p11 = new JPanel();
			p11.setLayout(new FlowLayout());
			p11.add(new JLabel(rb.getString("ActiveTrainTableTitle")));
			contentPane.add(p11);
			JPanel p12 = new JPanel();
			p12.setLayout(new FlowLayout());
			activeTrainsTableModel = new ActiveTrainsTableModel();
			JTable activeTrainsTable = new JTable(activeTrainsTableModel);
			activeTrainsTable.setRowSelectionAllowed(false);
			activeTrainsTable.setPreferredScrollableViewportSize(new java.awt.Dimension(830,100));
			TableColumnModel activeTrainsColumnModel = activeTrainsTable.getColumnModel();
			TableColumn transitColumn = activeTrainsColumnModel.getColumn(ActiveTrainsTableModel.TRANSIT_COLUMN);
			transitColumn.setResizable(true);
			transitColumn.setMinWidth(140);
			transitColumn.setMaxWidth(220);
			TableColumn trainColumn = activeTrainsColumnModel.getColumn(ActiveTrainsTableModel.TRAIN_COLUMN);
			trainColumn.setResizable(true);
			trainColumn.setMinWidth(90);
			trainColumn.setMaxWidth(160);
			TableColumn statusColumn = activeTrainsColumnModel.getColumn(ActiveTrainsTableModel.STATUS_COLUMN);
			statusColumn.setResizable(true);
			statusColumn.setMinWidth(90);
			statusColumn.setMaxWidth(140);
			TableColumn modeColumn = activeTrainsColumnModel.getColumn(ActiveTrainsTableModel.MODE_COLUMN);
			modeColumn.setResizable(true);
			modeColumn.setMinWidth(90);
			modeColumn.setMaxWidth(140);
			TableColumn allocatedColumn = activeTrainsColumnModel.getColumn(ActiveTrainsTableModel.ALLOCATED_COLUMN);
			allocatedColumn.setResizable(true);
			allocatedColumn.setMinWidth(120);
			allocatedColumn.setMaxWidth(200);
			TableColumn nextSectionColumn = activeTrainsColumnModel.getColumn(ActiveTrainsTableModel.NEXTSECTION_COLUMN);
			nextSectionColumn.setResizable(true);
			nextSectionColumn.setMinWidth(120);
			nextSectionColumn.setMaxWidth(200);
			TableColumn allocateButtonColumn = activeTrainsColumnModel.getColumn(ActiveTrainsTableModel.ALLOCATEBUTTON_COLUMN);
			allocateButtonColumn.setCellEditor(new ButtonEditor(new JButton()));
			allocateButtonColumn.setMinWidth(110);
			allocateButtonColumn.setMaxWidth(190);
			allocateButtonColumn.setResizable(false);
			ButtonRenderer buttonRenderer = new ButtonRenderer();
			activeTrainsTable.setDefaultRenderer(JButton.class,buttonRenderer);
			JButton sampleButton = new JButton(rb.getString("AllocateButtonName"));
			activeTrainsTable.setRowHeight(sampleButton.getPreferredSize().height);
			allocateButtonColumn.setPreferredWidth((sampleButton.getPreferredSize().width)+2);
			JScrollPane activeTrainsTableScrollPane = new JScrollPane(activeTrainsTable);
			p12.add(activeTrainsTableScrollPane, BorderLayout.CENTER);
			contentPane.add(p12);
			JPanel p13 = new JPanel();
			p13.setLayout(new FlowLayout());
			p13.add (addTrainButton = new JButton(rb.getString("InitiateTrain")+"..."));
            addTrainButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    initiateTrain(e);
                }
            });
			addTrainButton.setToolTipText(rb.getString("InitiateTrainButtonHint"));
			p13.add(new JLabel("   "));
			p13.add (showAllocatedButton = new JButton(rb.getString("ShowAllocated")+"..."));
            showAllocatedButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showAllocatedSections(e);
                }
            });
			showAllocatedButton.setToolTipText(rb.getString("ShowAllocatedButtonHint"));
			p13.add(new JLabel("   "));
			p13.add (allocateExtraButton = new JButton(rb.getString("AllocateExtra")+"..."));
            allocateExtraButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    allocateExtraSection(e);
                }
            });
			allocateExtraButton.setToolTipText(rb.getString("AllocateExtraButtonHint"));
			p13.add(new JLabel("   "));
			p13.add (terminateTrainButton = new JButton(rb.getString("TerminateTrain")+"..."));
            terminateTrainButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    terminateTrain(e);
                }
            });
			terminateTrainButton.setToolTipText(rb.getString("TerminateTrainButtonHint"));			
			contentPane.add(p13);
			// set up pending allocations table
			contentPane.add(new JSeparator());
			JPanel p21 = new JPanel();
			p21.setLayout(new FlowLayout());
			p21.add(new JLabel(rb.getString("RequestedAllocationsTableTitle")));
			contentPane.add(p21);
			JPanel p22 = new JPanel();
			p22.setLayout(new FlowLayout());
			allocationRequestTableModel = new AllocationRequestTableModel();
			JTable allocationRequestTable = new JTable(allocationRequestTableModel);
			allocationRequestTable.setRowSelectionAllowed(false);
			allocationRequestTable.setPreferredScrollableViewportSize(new java.awt.Dimension(800,100));
			TableColumnModel allocationRequestColumnModel = allocationRequestTable.getColumnModel();
			TableColumn activeColumn = allocationRequestColumnModel.getColumn(AllocationRequestTableModel.ACTIVE_COLUMN);
			activeColumn.setResizable(true);
			activeColumn.setMinWidth(200);
			activeColumn.setMaxWidth(250);
			TableColumn priorityColumn = allocationRequestColumnModel.getColumn(AllocationRequestTableModel.PRIORITY_COLUMN);
			priorityColumn.setResizable(true);
			priorityColumn.setMinWidth(70);
			priorityColumn.setMaxWidth(120);
			TableColumn sectionColumn = allocationRequestColumnModel.getColumn(AllocationRequestTableModel.SECTION_COLUMN);
			sectionColumn.setResizable(true);
			sectionColumn.setMinWidth(130);
			sectionColumn.setMaxWidth(200);
			TableColumn secStatusColumn = allocationRequestColumnModel.getColumn(AllocationRequestTableModel.STATUS_COLUMN);
			secStatusColumn.setResizable(true);
			secStatusColumn.setMinWidth(100);
			secStatusColumn.setMaxWidth(150);
			TableColumn occupancyColumn = allocationRequestColumnModel.getColumn(AllocationRequestTableModel.OCCUPANCY_COLUMN);
			occupancyColumn.setResizable(true);
			occupancyColumn.setMinWidth(80);
			occupancyColumn.setMaxWidth(140);
			TableColumn allocateColumn = allocationRequestColumnModel.getColumn(AllocationRequestTableModel.ALLOCATEBUTTON_COLUMN);
			allocateColumn.setCellEditor(new ButtonEditor(new JButton()));
			allocateColumn.setMinWidth(90);
			allocateColumn.setMaxWidth(170);
			allocateColumn.setResizable(false);
			allocationRequestTable.setDefaultRenderer(JButton.class,buttonRenderer);
			sampleButton = new JButton(rb.getString("AllocateButton"));
			allocationRequestTable.setRowHeight(sampleButton.getPreferredSize().height);
			allocateColumn.setPreferredWidth((sampleButton.getPreferredSize().width)+2);
			TableColumn cancelButtonColumn = allocationRequestColumnModel.getColumn(AllocationRequestTableModel.CANCELBUTTON_COLUMN);
			cancelButtonColumn.setCellEditor(new ButtonEditor(new JButton()));
			cancelButtonColumn.setMinWidth(90);
			cancelButtonColumn.setMaxWidth(170);
			cancelButtonColumn.setResizable(false);
			cancelButtonColumn.setPreferredWidth((sampleButton.getPreferredSize().width)+2);			
			JScrollPane allocationRequestTableScrollPane = new JScrollPane(allocationRequestTable);
			p22.add(allocationRequestTableScrollPane, BorderLayout.CENTER);
			contentPane.add(p22);
		}
        dispatcherFrame.pack();
        dispatcherFrame.setVisible(true);
    }
	
	// initiate train window variables
	private Transit selectedTransit = null;
	private String selectedTrain = "";
	private JmriJFrame initiateFrame=null;
	private Container initiatePane = null;
	private JComboBox transitSelectBox = new JComboBox();
	private ArrayList transitBoxList = new ArrayList();
	private JLabel trainBoxLabel = new JLabel("     "+rb.getString("TrainBoxLabel")+":");
	private JComboBox trainSelectBox = new JComboBox();
	private ArrayList trainBoxList = new ArrayList();
	private JLabel trainFieldLabel = new JLabel(rb.getString("TrainBoxLabel")+":");
	private JTextField trainNameField = new JTextField(10);
	private JLabel dccAddressFieldLabel = new JLabel("     "+rb.getString("DccAddressFieldLabel")+":");
	private JTextField dccAddressField = new JTextField(6);
	private JCheckBox inTransitBox = new JCheckBox(rb.getString("TrainInTransit"));
	private JComboBox startingBlockBox = new JComboBox();
	private ArrayList startingBlockBoxList = new ArrayList();
	private ArrayList startingBlockSeqList = new ArrayList();
	private JComboBox destinationBlockBox = new JComboBox();
	private ArrayList destinationBlockBoxList = new ArrayList();
	private ArrayList destinationBlockSeqList = new ArrayList();
	private JButton addNewTrainButton = null;
	private JCheckBox autoRunBox = new JCheckBox(rb.getString("AutoRun"));
	private JTextField priorityField = new JTextField(6);
	
	// display a window that allows a new Transit/Train to be activated
	void initiateTrain(ActionEvent e) {
        if (initiateFrame==null) {
            initiateFrame = new JmriJFrame(rb.getString("AddTrainTitle"));
            initiateFrame.addHelpMenu("package.jmri.jmrit.dispatcher.NewTrain", true);
			initiatePane = initiateFrame.getContentPane();
            initiatePane.setLayout(new BoxLayout(initiateFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p1 = new JPanel(); 
			p1.setLayout(new FlowLayout());
			p1.add(new JLabel(rb.getString("TransitBoxLabel")+":"));
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
			p3.add(new JLabel(rb.getString("StartingBlockBoxLabel")+":"));
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
            JPanel p5 = new JPanel(); 
			p5.setLayout(new FlowLayout());
			p5.add(new JLabel(rb.getString("PriorityLabel")+":"));
			p5.add(priorityField);
			priorityField.setToolTipText(rb.getString("PriorityHint"));
			priorityField.setText("5");    
			p5.add(new JLabel("     "));
			p5.add(autoRunBox);
            autoRunBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handleAutoRunClick(e);
                }
            });
			autoRunBox.setToolTipText(rb.getString("AutoRunBoxHint"));
			initiatePane.add(p5);
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
		initializeFreeTransitsCombo();
		initializeFreeTrainsCombo();	
		initiateFrame.pack();
		initiateFrame.setVisible(true);	
	}
	private void handleTransitSelectionChanged(ActionEvent e) {
		int index = transitSelectBox.getSelectedIndex();
		if (index<0) return;
		Transit t = (Transit)transitBoxList.get(index);
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
		String startBlockName = ((Block)startingBlockBoxList.get(index)).getSystemName();
		int startBlockSeq = ((Integer)startingBlockSeqList.get(index)).intValue();
		index = destinationBlockBox.getSelectedIndex();
		if (index<0) return;
		String endBlockName = ((Block)destinationBlockBoxList.get(index)).getSystemName();
		int endBlockSeq = ((Integer)destinationBlockSeqList.get(index)).intValue();
		boolean autoRun = autoRunBox.isSelected();
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
			RosterEntry r = (RosterEntry)trainBoxList.get(index);
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
						"Error24"),new String[] { trainName }), rb.getString("ErrorTitle"),
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
						"BadEntry"),new String[] { priorityField.getText() }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);		
			log.error ("Conversion exception in priority field");
			return;
		}
		// create a new Active Train
		ActiveTrain at = createActiveTrain ( transitName, trainName, tSource, startBlockName, startBlockSeq, 
					endBlockName, endBlockSeq, autoRun, dccAddress, priority, true, initiateFrame);
		if (at==null) return;  // error message sent by createActiveTrain
		initiateFrame.setVisible(false);
		initiateFrame.dispose();  // prevent this window from being listed in the Window menu.
	}	
	private void initializeFreeTransitsCombo() {
		ArrayList allTransits = (ArrayList)transitManager.getSystemNameList();
		transitSelectBox.removeAllItems();
		transitBoxList.clear();
		for (int i = 0; i<allTransits.size(); i++) {
			String tName = (String)allTransits.get(i);
			Transit t = transitManager.getBySystemName(tName);
			boolean free = true;
			for (int j = 0; j<activeTrainsList.size(); j++) {
				ActiveTrain at = (ActiveTrain)activeTrainsList.get(j);
				if (t == at.getTransit()) free = false;
			}
			if (free) {
				transitBoxList.add((Object)t);
				if ( (t.getUserName()!=null) && (!t.getUserName().equals("")) )
					tName = tName+"( "+t.getUserName()+" )";
				transitSelectBox.addItem(tName);
			}
		}
		if (transitBoxList.size()>0) {
			transitSelectBox.setSelectedIndex(0);
			selectedTransit = (Transit)transitBoxList.get(0);
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
			List l = Roster.instance().matchingList(null, null, null, null, null, null, null );
			if (l.size()>0) {
				for (int i = 0; i<l.size(); i++) {
					RosterEntry r = (RosterEntry)l.get(i);
					String rName = r.titleString();
					if (isTrainFree(rName)) {
						trainBoxList.add((Object)r);
						trainSelectBox.addItem(rName);
					}
				}
			}
		}
		else if (_TrainsFromTrains) {
			// initialize free trains from operations
			List l = TrainManager.instance().getTrainsByNameList();
			if (l.size()>0) {
				for (int i = 0; i<l.size(); i++) {
					String rName = (String)l.get(i);
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
			selectedTrain = (String)trainSelectBox.getSelectedItem();
		}
		else {
			selectedTrain = "";
		}
	}
	private boolean isTrainFree (String rName) {
		for (int j = 0; j<activeTrainsList.size(); j++) {
			ActiveTrain at = (ActiveTrain)activeTrainsList.get(j);
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
			Block b = (Block)startingBlockBoxList.get(i);
			int seq = ((Integer)startingBlockSeqList.get(i)).intValue();
			startingBlockBox.addItem(getBlockName(b)+"-"+seq);
		}
	}
	private void initializeDestinationBlockCombo() {
		destinationBlockBox.removeAllItems();
		destinationBlockBoxList.clear();
		int index = startingBlockBox.getSelectedIndex();
		if (index<0) return;
		Block startBlock = (Block)startingBlockBoxList.get(index);
		destinationBlockBoxList = selectedTransit.getDestinationBlocksList(
					startBlock, inTransitBox.isSelected());
		destinationBlockSeqList = selectedTransit.getDestBlocksSeqList();
		for (int i = 0; i<destinationBlockBoxList.size(); i++) {
			Block b = (Block)destinationBlockBoxList.get(i);
			String bName = getBlockName(b);
			if (selectedTransit.getBlockCount(b)>1) {
				int seq = ((Integer)destinationBlockSeqList.get(i)).intValue();
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
	
	// show all allocated Sections
	JmriJFrame allocatedSectionsFrame = null;
	AllocatedSectionTableModel allocatedSectionTableModel = null;
	void showAllocatedSections(ActionEvent e) {
        if (allocatedSectionsFrame==null) {
            allocatedSectionsFrame = new JmriJFrame(rb.getString("AllocatedSectionsTitle"));
            allocatedSectionsFrame.addHelpMenu("package.jmri.jmrit.dispatcher.AllocatedSections", true);
			Container allocatedSectionsPane = allocatedSectionsFrame.getContentPane();
            allocatedSectionsPane.setLayout(new BoxLayout(allocatedSectionsFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p1 = new JPanel(); 
			p1.setLayout(new FlowLayout());
			allocatedSectionTableModel = new AllocatedSectionTableModel();
			JTable allocatedSectionTable = new JTable(allocatedSectionTableModel);
			allocatedSectionTable.setRowSelectionAllowed(false);
			allocatedSectionTable.setPreferredScrollableViewportSize(new java.awt.Dimension(530,100));
			TableColumnModel allocatedSectionColumnModel = allocatedSectionTable.getColumnModel();
			TableColumn activeColumn = allocatedSectionColumnModel.getColumn(AllocatedSectionTableModel.ACTIVE_COLUMN);
			activeColumn.setResizable(true);
			activeColumn.setMinWidth(200);
			activeColumn.setMaxWidth(250);
			TableColumn sectionColumn = allocatedSectionColumnModel.getColumn(AllocatedSectionTableModel.SECTION_COLUMN);
			sectionColumn.setResizable(true);
			sectionColumn.setMinWidth(130);
			sectionColumn.setMaxWidth(200);
			TableColumn occupancyColumn = allocatedSectionColumnModel.getColumn(AllocatedSectionTableModel.OCCUPANCY_COLUMN);
			occupancyColumn.setResizable(true);
			occupancyColumn.setMinWidth(80);
			occupancyColumn.setMaxWidth(140);
			TableColumn releaseColumn = allocatedSectionColumnModel.getColumn(AllocatedSectionTableModel.RELEASEBUTTON_COLUMN);
			releaseColumn.setCellEditor(new ButtonEditor(new JButton()));
			releaseColumn.setMinWidth(90);
			releaseColumn.setMaxWidth(170);
			releaseColumn.setResizable(false);
			ButtonRenderer buttonRenderer = new ButtonRenderer();
			allocatedSectionTable.setDefaultRenderer(JButton.class,buttonRenderer);
			JButton sampleButton = new JButton(rb.getString("ReleaseButton"));
			allocatedSectionTable.setRowHeight(sampleButton.getPreferredSize().height);
			releaseColumn.setPreferredWidth((sampleButton.getPreferredSize().width)+2);
			JScrollPane allocatedSectionTableScrollPane = new JScrollPane(allocatedSectionTable);
			p1.add(allocatedSectionTableScrollPane, BorderLayout.CENTER);
			allocatedSectionsPane.add(p1);
		}
        allocatedSectionsFrame.pack();
        allocatedSectionsFrame.setVisible(true);
	}	
	void releaseAllocatedSectionFromTable(int index) {
		AllocatedSection as = (AllocatedSection)allocatedSections.get(index);
		releaseAllocatedSection(as);
	}		

	// allocate extra window variables
	private JmriJFrame extraFrame=null;
	private Container extraPane = null;
	private JComboBox atSelectBox = new JComboBox();
	private JComboBox extraBox = new JComboBox();
	private ArrayList extraBoxList = new ArrayList();
	private int atSelectedIndex = -1;
	
	// allocate an extra Section to an Active Train
	private void allocateExtraSection(ActionEvent e) {
        if (extraFrame==null) {
            extraFrame = new JmriJFrame(rb.getString("ExtraTitle"));
            extraFrame.addHelpMenu("package.jmri.jmrit.dispatcher.AllocateExtra", true);
			extraPane = extraFrame.getContentPane();
            extraPane.setLayout(new BoxLayout(extraFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p1 = new JPanel(); 
			p1.setLayout(new FlowLayout());
			p1.add(new JLabel(rb.getString("ActiveColumnTitle")+":"));
			p1.add(atSelectBox);
            atSelectBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handleATSelectionChanged(e);
                }
            });
			atSelectBox.setToolTipText(rb.getString("ATBoxHint"));
			extraPane.add(p1);
            JPanel p2 = new JPanel(); 
			p2.setLayout(new FlowLayout());
			p2.add(new JLabel(rb.getString("ExtraBoxLabel")+":"));
			p2.add(extraBox);
			extraBox.setToolTipText(rb.getString("ExtraBoxHint"));
			extraPane.add(p2);
			JPanel p7 = new JPanel();
			p7.setLayout(new FlowLayout());
			JButton cancelButton = null;
			p7.add(cancelButton = new JButton(rb.getString("CancelButton")));
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelExtraRequested(e);
                }
            });
			cancelButton.setToolTipText(rb.getString("CancelExtraHint"));
			p7.add(new JLabel("    "));
			JButton aExtraButton = null;
			p7.add (aExtraButton = new JButton(rb.getString("AllocateButton")));
            aExtraButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addExtraRequested(e);
                }
            });
			aExtraButton.setToolTipText(rb.getString("AllocateButtonHint"));
			extraPane.add(p7);
		}
		initializeATComboBox();
		initializeExtraComboBox();
		extraFrame.pack();
		extraFrame.setVisible(true);
	}
	private void handleATSelectionChanged(ActionEvent e) {
		atSelectedIndex = atSelectBox.getSelectedIndex();
		initializeExtraComboBox();
		extraFrame.pack();
		extraFrame.setVisible(true);
	}
	private void initializeATComboBox() {
		atSelectedIndex = -1;
		atSelectBox.removeAllItems();
		for (int i = 0; i<activeTrainsList.size(); i++) {
			ActiveTrain at = (ActiveTrain)activeTrainsList.get(i);
			if (_ShortActiveTrainNames)
				atSelectBox.addItem(at.getTrainName());
			else
				atSelectBox.addItem(at.getActiveTrainName());
		}
		if (activeTrainsList.size()>0) {
			atSelectBox.setSelectedIndex(0);
			atSelectedIndex = 0;
		}
	}
	private void initializeExtraComboBox() {
		extraBox.removeAllItems();
		extraBoxList.clear();
		if (atSelectedIndex<0) return;
		ActiveTrain at = (ActiveTrain)activeTrainsList.get(atSelectedIndex);
		Transit t = at.getTransit();
		ArrayList allocatedSectionList = at.getAllocatedSectionList();
		ArrayList allSections = (ArrayList)InstanceManager.sectionManagerInstance().getSystemNameList();
		for (int j = 0; j<allSections.size(); j++) {
			Section s = InstanceManager.sectionManagerInstance().getSection((String)allSections.get(j));
			if (s.getState()==Section.FREE) {
				// not already allocated, check connectivity to this train's allocated sections
				boolean connected = false;
				for (int k = 0; k<allocatedSectionList.size(); k++) {
					if (connected(s,((AllocatedSection)allocatedSectionList.get(k)).getSection())) {
						connected = true;
					}
				}
				if (connected) {
					// add to the combo box, not allocated and connected to allocated
					extraBoxList.add((Object)s);
					extraBox.addItem(getSectionName(s));
				}
			}
		}
		if (extraBoxList.size()>0) {
			extraBox.setSelectedIndex(0);
		}
	}
	private boolean connected(Section s1, Section s2) {
		if ( (s1!=null) && (s2!=null) ) {
			ArrayList s1Entries = (ArrayList)s1.getEntryPointList();
			ArrayList s2Entries = (ArrayList)s2.getEntryPointList();
			for (int i = 0; i<s1Entries.size(); i++) {
				Block b = ((EntryPoint)s1Entries.get(i)).getFromBlock();
				for (int j = 0; j<s2Entries.size(); j++) {
					if (b == ((EntryPoint)s2Entries.get(j)).getBlock()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	public String getSectionName(Section sec) {
		String s = sec.getSystemName();
		String u = sec.getUserName();
		if ( (u!=null) && (!u.equals("")) ) {
			return (s+"( "+u+" )");
		}
		return s;
	}
	private void cancelExtraRequested(ActionEvent e) {
		extraFrame.setVisible(false);
		extraFrame.dispose();   // prevent listing in the Window menu.
		extraFrame = null;
	}
	private void addExtraRequested(ActionEvent e) {
		int index = extraBox.getSelectedIndex();
		if ( (atSelectedIndex<0) || (index<0) ) {
			cancelExtraRequested(e);
			return;
		}
		ActiveTrain at = (ActiveTrain)activeTrainsList.get(atSelectedIndex);
		Transit t = at.getTransit();
		Section s = (Section)extraBoxList.get(index);
		Section ns = null;
		AllocationRequest ar = null;
		if (t.containsSection(s)) {
			if (s==at.getNextSectionToAllocate()) {
				// this is a request that the next section in the transit be allocated
				allocateNextRequested(atSelectedIndex);
				return;
			}
			else {
				// requesting allocation of a section in the Transit, but not the next Section
				int seq = -99;
				ArrayList seqList = t.getSeqListBySection(s);
				if (seqList.size() > 0) {
					seq = ((Integer)seqList.get(0)).intValue();
				}
				if (seqList.size() > 1) {
					// this section is in the Transit multiple times 
					int test = at.getNextSectionSeqNumber()-1;
					int diff = java.lang.Math.abs(seq - test);
					for (int i = 1; i<seqList.size(); i++) {
						if ( diff > java.lang.Math.abs(test - ((Integer)seqList.get(i)).intValue()) ) {
							seq = ((Integer)seqList.get(i)).intValue();
							diff = java.lang.Math.abs(seq - test);
						}
					}
				}
				ar = requestAllocation(at, s, t.getDirectionFromSectionAndSeq(s, seq), seq, true, extraFrame);
			}
		}
		else {
			// requesting allocation of a section outside of the Transit, direction set arbitrary
			ar = requestAllocation(at, s, Section.FORWARD, -99, true, extraFrame);
		}
		// if allocation request is OK, allocate the Section
		if (ar==null) return;
		AllocatedSection as = allocateSection(ar, null);	
		extraFrame.setVisible(false);
		extraFrame.dispose();   // prevent listing in the Window menu.
		extraFrame = null;
	}
	
	// terminate an Active Train from the button in the Dispatcher window
	void terminateTrain(ActionEvent e) {
		ActiveTrain at = null;
		if (activeTrainsList.size() == 1) {
			at = (ActiveTrain)activeTrainsList.get(0);
		}
		else if (activeTrainsList.size() > 1) {
			Object choices[] = new Object[activeTrainsList.size()];
			for (int i = 0; i<activeTrainsList.size(); i++) {
				if (_ShortActiveTrainNames) {
					choices[i] = (Object)((ActiveTrain)activeTrainsList.get(i)).getTrainName();
				}
				else {
					choices[i] = (Object)((ActiveTrain)activeTrainsList.get(i)).getActiveTrainName();
				}
			}
			Object selName = JOptionPane.showInputDialog(dispatcherFrame, 
						(Object)rb.getString("TerminateTrainChoice"),
						rb.getString("TerminateTrainTitle"), JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
			if (selName == null) return;
			for (int j = 0; j<activeTrainsList.size(); j++) {
				if (selName.equals(choices[j])) {
					at = (ActiveTrain)activeTrainsList.get(j);
				}
			}
		}
		if (at!=null) {
			terminateActiveTrain(at);
		}
	}

	// allocate the next section for an ActiveTrain 
	void allocateNextRequested(int index) {
		// set up an Allocation Request
		ActiveTrain at = (ActiveTrain)activeTrainsList.get(index);
		Section next = at.getNextSectionToAllocate();
		if (next==null) return;
		int seqNext = at.getNextSectionSeqNumber();
		int dirNext = at.getTransit().getDirectionFromSectionAndSeq(next,seqNext);
		AllocationRequest ar = requestAllocation(at, next, dirNext, seqNext, true, dispatcherFrame);
		if (ar==null) return;
		// attempt to allocate
		AllocatedSection as = allocateSection(ar, null);
	}
	
	/**
	 * Creates a new ActiveTrain, and registers it with Dispatcher
	 * <P>
	 *  Required input entries:
	 *    transitID - system or user name of a Transit in the Transit Table
	 *    trainID - any text that identifies the train
	 *    tSource - either ROSTER, OPERATIONS, or USER (see ActiveTrain.java) 
	 *	  startBlockName - system or user name of Block where train currently resides
	 *    startBlockSectionSequenceNumber - sequence number in the Transit of the Section containing
	 *            the startBlock (if the startBlock is within the Transit) , or of the Section 
	 *            the train will enter from the startBlock (if the startBlock is outside the Transit).
	 *    endBlockName - system or user name of Block where train will end up after its transit
	 *    endBlockSectionSequenceNumber - sequence number in the Transit of the Section containing
	 *            the endBlock.
	 *    autoRun - set to "true" if computer is to run the train automatically, otherwise "false"
	 *    dccAddress - required if "autoRun" is "true", set to null otherwise
	 *    priority - any integer, higher number is higher priority. Used to arbitrate allocation 
	 *            request conflicts
	 *    showErrorMessages - "true" if error message dialogs are to be displayed for detected errors
	 *            Set to "false" to suppress error message dialogs from this method.
	 *    frame - window request is from, or "null" if not from a window
	 * <P>
	 *    Returns an ActiveTrain object if successful, returns "null" otherwise
	 */
	public ActiveTrain createActiveTrain (String transitID, String trainID, int tSource, String startBlockName, 
				int startBlockSectionSequenceNumber, String endBlockName, int endBlockSectionSequenceNumber, 
				boolean autoRun, String dccAddress, int priority, boolean showErrorMessages, JmriJFrame frame) {
		// validate input
		Transit t = transitManager.getTransit(transitID);
		if (t==null) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error1"),new String[] { transitID }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
			}
			log.error("Bad Transit name '"+transitID+"' when attempting to create an Active Train");
			return null;
		}
		if (t.getState()!=Transit.IDLE) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error2"),new String[] { transitID }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
			}
			log.error("Transit '"+transitID+"' not IDLE, cannot create an Active Train");
			return null;
		}
		if ( (trainID==null) || trainID.equals("") ) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame, rb.getString("Error3"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			}
			log.error("TrainID string not provided, cannot create an Active Train");
			return null;
		}
		if ( (tSource!=ActiveTrain.ROSTER) && (tSource!=ActiveTrain.OPERATIONS) && 
				(tSource!=ActiveTrain.USER) ) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame, rb.getString("Error21"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			}
			log.error("Train source is invalid - "+tSource+" - cannot create an Active Train");
			return null;
		}
		Block startBlock = InstanceManager.blockManagerInstance().getBlock(startBlockName);
		if (startBlock==null) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error4"),new String[] { startBlockName }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
			}
			log.error("Bad startBlockName '"+startBlockName+"' when attempting to create an Active Train");
			return null;
		}
		if (isInAllocatedSection(startBlock)) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error5"),new String[] { startBlockName }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
			}
			log.error("Start block '"+startBlockName+"' in allocated Section, cannot create an Active Train");
			return null;
		}
		if ( _HasOccupancyDetection && (!(startBlock.getState()==Block.OCCUPIED)) ) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error6"),new String[] { startBlockName }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
			}
			log.error("No train in start block '"+startBlockName+"', cannot create an Active Train");
			return null;
		}
		if (startBlockSectionSequenceNumber<=0) {
			JOptionPane.showMessageDialog(frame, rb.getString("Error12"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);			
		}	
		else if (startBlockSectionSequenceNumber>t.getMaxSequence()) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error13"),new String[] {""+startBlockSectionSequenceNumber }), 
								rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			}
			log.error("Invalid sequence number '"+startBlockSectionSequenceNumber+"' when attempting to create an Active Train");
			return null;
		}
		Block endBlock = InstanceManager.blockManagerInstance().getBlock(endBlockName);
		if ( (endBlock==null) || (!t.containsBlock(endBlock)) ) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error7"),new String[] { endBlockName }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
			}
			log.error("Bad endBlockName '"+endBlockName+"' when attempting to create an Active Train");
			return null;
		}
		if ( (endBlockSectionSequenceNumber<=0) && (t.getBlockCount(endBlock)>1) ) {
				JOptionPane.showMessageDialog(frame, rb.getString("Error8"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);			
		}	
		else if (endBlockSectionSequenceNumber>t.getMaxSequence()) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error9"),new String[] {""+endBlockSectionSequenceNumber }), 
								rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			}
			log.error("Invalid sequence number '"+endBlockSectionSequenceNumber+"' when attempting to create an Active Train");
			return null;
		}
		if ( autoRun && ( (dccAddress==null) || dccAddress.equals("") ) ) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame, rb.getString("Error10"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			}
			log.error("AutoRun requested without a dccAddress when attempting to create an Active Train");
			return null;
		}
		// all information checks out - create	
		ActiveTrain at = new ActiveTrain(t,trainID,tSource);
		if (at==null) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error11"),new String[] { transitID, trainID }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
			}
			log.error("Creating Active Train failed, Transit - "+transitID+", train - "+trainID);
			return null;
		}
		activeTrainsList.add((Object)at);
		t.setState(Transit.ASSIGNED);
		at.setStartBlock(startBlock);
		at.setStartBlockSectionSequenceNumber(startBlockSectionSequenceNumber);
		at.setEndBlock(endBlock);
		at.setEndBlockSection(t.getSectionFromBlockAndSeq(endBlock,endBlockSectionSequenceNumber));
		at.setEndBlockSectionSequenceNumber(endBlockSectionSequenceNumber);
		at.setPriority(priority);
		at.setAutoRun(autoRun);
		at.setDccAddress(dccAddress);
		at.initializeFirstAllocation();
		activeTrainsTableModel.fireTableDataChanged();
		if (allocatedSectionTableModel!=null) {
			allocatedSectionTableModel.fireTableDataChanged();
		}
		return at;
	}
	private boolean isInAllocatedSection(jmri.Block b) {
		for (int i = 0; i<allocatedSections.size(); i++) {
			Section s = ((AllocatedSection)allocatedSections.get(i)).getSection();
			if (s.containsBlock(b)) return true;
		}
		return false;
	}
	
	/**
	 *  Terminates an Active Train and removes it from the Dispatcher
	 *     The ActiveTrain object should not be used again after this method is called
	 */
	public void terminateActiveTrain(ActiveTrain at) {
		// ensure there is a train to terminate
		if (at==null) {
			log.error("Null ActiveTrain pointer when attempting to terminate an ActiveTrain");
			return;
		}
		// terminate the train - remove any allocation requests
		for (int k = allocationRequests.size(); k>0;  k--) {
			if (at == ((AllocationRequest)allocationRequests.get(k-1)).getActiveTrain()) {
				((AllocationRequest)allocationRequests.get(k-1)).dispose();
				allocationRequests.remove(k-1);
			}
		}
		// remove any allocated sections
		for (int k = allocatedSections.size(); k>0;  k--) {
			if (at == ((AllocatedSection)allocatedSections.get(k-1)).getActiveTrain()) {
				releaseAllocatedSection ((AllocatedSection)allocatedSections.get(k-1));
			}
		}
		// terminate the train
		for (int m = activeTrainsList.size(); m>0; m--) {
			if ((Object)at == activeTrainsList.get(m-1)) {
				activeTrainsList.remove(m-1);
			}
		}
		at.terminate();
		at.dispose();
		activeTrainsTableModel.fireTableDataChanged();
		if (allocatedSectionTableModel!=null) {
			allocatedSectionTableModel.fireTableDataChanged();
		}
		allocationRequestTableModel.fireTableDataChanged();		
	}
	
	/**
	 * Creates an Allocation Request, and registers it with Dispatcher
	 * <P>
	 *  Required input entries:
	 *    activeTrain - ActiveTrain requesting the allocation
	 *    section - Section to be allocated
	 *    direction - direction of travel in the allocated Section
	 *    seqNumber - sequence number of the Section in the Transit of the ActiveTrain. If the requested Section
	 *			is not in the Transit, a sequence number of -99 should be entered.
	 *    showErrorMessages - "true" if error message dialogs are to be displayed for detected errors
	 *            Set to "false" to suppress error message dialogs from this method.
	 *    frame - window request is from, or "null" if not from a window
	 */
	public AllocationRequest requestAllocation (ActiveTrain activeTrain, Section section, int direction, 
				int seqNumber, boolean showErrorMessages, JmriJFrame frame) {
		// check input entries
		if (activeTrain==null) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame, rb.getString("Error16"), 
						rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			}
			log.error("Missing ActiveTrain specification");
			return null;
		}
		if (section==null) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error17"),new String[] { activeTrain.getActiveTrainName() }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
			}
			log.error("Missing Section specification in allocation request from "+activeTrain.getActiveTrainName());
			return null;
		}
		if ( (direction!=Section.FORWARD) && (direction!=Section.REVERSE) ) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error18"),new String[] { ""+direction,activeTrain.getActiveTrainName() }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
			}
			log.error("Invalid direction '"+direction+"' specification in allocation request");
			return null;
		}
		if ( ((seqNumber<=0) || (seqNumber>(activeTrain.getTransit().getMaxSequence()))) && (seqNumber != -99) ) {
			if (showErrorMessages) {
				JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error19"),new String[] { ""+seqNumber,activeTrain.getActiveTrainName() }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
			}
			log.error("Out-of-range sequence number *"+seqNumber+"* in allocation request");
			return null;
		}
		// check if this allocation has already been requested
		AllocationRequest ar = findAllocationRequestInQueue(section, seqNumber, direction, activeTrain);
		if (ar==null) {
			ar = new AllocationRequest(section, seqNumber, direction, activeTrain);
			if (ar==null) {
				if (showErrorMessages) {
					JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(rb.getString(
						"Error20"),new String[] { activeTrain.getActiveTrainName() }), rb.getString("ErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
				}
				log.error("Null return when creating new allocation request for "+activeTrain.getActiveTrainName());
				return null;
			}
			allocationRequests.add(ar);
		}
		activeTrainsTableModel.fireTableDataChanged();
		allocationRequestTableModel.fireTableDataChanged();
		return ar;
	}
	// ensures there will not be any duplicate allocation requests
	private AllocationRequest findAllocationRequestInQueue(Section s, int seq, int dir, ActiveTrain at) {
		for (int i = 0; i<allocationRequests.size(); i++) {
			AllocationRequest ar = (AllocationRequest)allocationRequests.get(i);
			if ( (ar.getActiveTrain() == at) && (ar.getSection() == s) && (ar.getSectionSeqNumber() == seq) &&
						(ar.getSectionDirection() == dir) ) return ar;
		}
		return null;
	}
	
	private void cancelAllocationRequest(int index) {
		AllocationRequest ar = (AllocationRequest)allocationRequests.get(index);
		allocationRequests.remove(index);
		ar.dispose();
		allocationRequestTableModel.fireTableDataChanged();
	}
	private void allocateRequested(int index) {
		AllocationRequest ar = (AllocationRequest)allocationRequests.get(index);
		AllocatedSection as = allocateSection(ar, null);
	}
	
	/**
	 * Allocates a Section to an Active Train according to the information in an AllocationRequest
	 *  If successful, returns an AllocatedSection and removes the AllocationRequest from the queue.
	 *  If not successful, returns null and leaves the AllocationRequest in the queue.
	 *  To be allocatable, a Section must be FREE and UNOCCUPIED.  If a Section is OCCUPIED, the allocation 
	 *     is rejected unless the dispatcher chooses to override this restriction.
	 *  The user may choose to specify the next Section by entering "ns". If this method is to determine the 
	 *     next Section, or if the next section is the last section, null should be entered for ns. Null should
	 *     also be entered for ns if allocating an Extra Section that is not the Next Section.
	 */
	public AllocatedSection allocateSection(AllocationRequest ar, Section ns) {
		AllocatedSection as = null;
		Section nextSection = null;
		if (ar!=null) {
			ActiveTrain at = ar.getActiveTrain();
			Section s = ar.getSection();
			if (s.getState()!=Section.FREE) return null;
			// skip occupancy check if this is the first allocation and the train is occupying the Section
			boolean checkOccupancy = true;
			if ( (at.getLastAllocatedSection()==null) && (s.containsBlock(at.getStartBlock())) ) {
				checkOccupancy = false;
			}
			// check if section is occupied
			if ( checkOccupancy && (s.getOccupancy()==Section.OCCUPIED) ) {
				if (_AutoAllocate) return null;  // autoAllocate never overrides occupancy
				int selectedValue = JOptionPane.showOptionDialog(dispatcherFrame,
						rb.getString("Question1"),rb.getString("WarningTitle"),
						JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,
						new Object[]{rb.getString("ButtonYes"),rb.getString("ButtonNo")},rb.getString("ButtonNo"));
				if (selectedValue == 1) return null;   // return without allocating if "No" response
			}
			// Programming Note: if ns is not null, the program will not check for end Block, but will use ns. Calling
			//		code must do all validity checks on a non-null ns.
			if (ns!=null) {
			     nextSection = ns;
			}
			else if ( (ar.getSectionSeqNumber() != -99) && (at.getNextSectionSeqNumber()==ar.getSectionSeqNumber()) &&
					( !((s==at.getEndBlockSection()) && (ar.getSectionSeqNumber()==at.getEndBlockSectionSequenceNumber())) ) ) {
				// determine the next section - check if there is only one next section
				ArrayList secList = at.getTransit().getSectionListBySeq(ar.getSectionSeqNumber()+1);
				if (secList.size()==1) {
					nextSection = (Section)secList.get(0);
				}
				else if (secList.size()>1) {
					if (_AutoAllocate) {
						nextSection = autoChoice(secList, ar);
					}
					else {
						nextSection = dispatcherChoice(secList, ar);
					}
				}
			}
			// allocate the section
			as = new AllocatedSection(s,at,ar.getSectionSeqNumber(),nextSection);
			if (as!=null) {
				s.setState(ar.getSectionDirection());
				at.addAllocatedSection(as);
				allocatedSections.add((Object)as);
				int ix = -1;
				for (int i = 0; i<allocationRequests.size(); i++) {
					if ((Object)ar==allocationRequests.get(i)) {
						ix = i;
					}
				}
				allocationRequests.remove(ix);
				ar.dispose();
				allocationRequestTableModel.fireTableDataChanged();
				activeTrainsTableModel.fireTableDataChanged();
				if (allocatedSectionTableModel!=null) {
					allocatedSectionTableModel.fireTableDataChanged();
				}
				if (_AutoTurnouts) autoTurnouts.setTurnouts(as,this);
			}
			if (extraFrame!=null) cancelExtraRequested(null);
		}
		else {
			log.error("Null Allocation Request provided in request to allocate a section");
		}
		return as;
	}	
	// automatically make a choice of next section
	private Section autoChoice(ArrayList sList, AllocationRequest ar) {
// here add code to make a real choice
// should start by eliminating Sections occupied by trains running in the opposite direction
// temporarily choose the first choice
		return (Section)sList.get(0);
	}	
	// manually make a choice of next section
	private Section dispatcherChoice(ArrayList sList, AllocationRequest ar) {
		Object choices[] = new Object[sList.size()];
		for (int i = 0; i<sList.size(); i++) {
			Section s = (Section)sList.get(i);
			String txt = s.getSystemName();
			String user = s.getUserName();
			if ( (user!=null) && (!user.equals("")) )
				txt = txt+"( "+user+" )";
			choices[i] = (Object)txt;
		}
		Object secName = JOptionPane.showInputDialog(dispatcherFrame, 
					(Object)(rb.getString("ExplainChoice")+" "+ar.getSectionName()+"."),
					rb.getString("ChoiceFrameTitle"), JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
		if (secName==null) {
			JOptionPane.showMessageDialog(dispatcherFrame, rb.getString("WarnCancel"));
			return autoChoice(sList,ar);
		}
		for (int j = 0; j<sList.size(); j++) {
			if (secName.equals(choices[j])) {
				return (Section)sList.get(j);
			}
		}
		return (Section)sList.get(0);
	}
	
	/**
	 * Releases an allocated Section, and removes it from the Dispatcher 
	 *   Input consists of the AllocatedSection object returned by "allocateSection"
	 */	
	public void releaseAllocatedSection(AllocatedSection as) {
		for (int i = allocatedSections.size(); i>0; i--) {
			if ((Object)as == allocatedSections.get(i-1))
				allocatedSections.remove(i-1);
		}
		as.getSection().setState(Section.FREE);
		as.getActiveTrain().removeAllocatedSection(as);
		if (allocatedSectionTableModel!=null) {
			allocatedSectionTableModel.fireTableDataChanged();
		}
		allocationRequestTableModel.fireTableDataChanged();
		activeTrainsTableModel.fireTableDataChanged();
	}
	
	/**
	 * Updates display when occupancy of an allocated section changes
	 */
	public void sectionOccupancyChanged() {
		if (allocatedSectionTableModel!=null) {
			allocatedSectionTableModel.fireTableDataChanged();
		}
		allocationRequestTableModel.fireTableDataChanged();
	}
	
	// option access methods
	protected LayoutEditor getLayoutEditor() {return _LE;}
	protected void setLayoutEditor(LayoutEditor editor) {_LE = editor;}
	protected boolean getUseConnectivity() {return _UseConnectivity;}
	protected void setUseConnectivity(boolean set) {_UseConnectivity = set;}
	protected boolean getTrainsFromRoster() {return _TrainsFromRoster;}
	protected void setTrainsFromRoster(boolean set) {_TrainsFromRoster = set;}
	protected boolean getTrainsFromTrains() {return _TrainsFromTrains;}
	protected void setTrainsFromTrains(boolean set) {_TrainsFromTrains = set;}
	protected boolean getTrainsFromUser() {return _TrainsFromUser;}
	protected void setTrainsFromUser(boolean set) {_TrainsFromUser = set;}
	protected boolean getAutoAllocate() {return _AutoAllocate;}
	protected void setAutoAllocate(boolean set) {_AutoAllocate = set;}
	protected boolean getAutoTurnouts() {return _AutoTurnouts;}
	protected void setAutoTurnouts(boolean set) {_AutoTurnouts = set;}
	protected boolean getHasOccupancyDetection() {return _HasOccupancyDetection;}
	protected void setHasOccupancyDetection(boolean set) {_HasOccupancyDetection = set;}
	protected boolean getShortActiveTrainNames() {return _ShortActiveTrainNames;}
	protected void setShortActiveTrainNames(boolean set) {
		_ShortActiveTrainNames = set;
		if (allocatedSectionTableModel!=null) {
			allocatedSectionTableModel.fireTableDataChanged();
		}
		allocationRequestTableModel.fireTableDataChanged();
	}
	protected boolean getShortNameInBlock() {return _ShortNameInBlock;}
	protected void setShortNameInBlock(boolean set) {_ShortNameInBlock = set;}
	
	static DispatcherFrame _instance = null;
    static public DispatcherFrame instance() {
        if (_instance == null) {
            _instance = new DispatcherFrame();
        }
        return (_instance);
    }

 	
	/**
	 * Table model for Active Trains Table in Dispatcher window
	 */
	public class ActiveTrainsTableModel extends javax.swing.table.AbstractTableModel implements
			java.beans.PropertyChangeListener {

		public static final int TRANSIT_COLUMN = 0;
		public static final int TRAIN_COLUMN = 1;
		public static final int STATUS_COLUMN = 2;
		public static final int MODE_COLUMN = 3;
		public static final int ALLOCATED_COLUMN = 4;
		public static final int NEXTSECTION_COLUMN = 5;
		public static final int ALLOCATEBUTTON_COLUMN = 6;

		public ActiveTrainsTableModel() {
			super();
		}

		public void propertyChange(java.beans.PropertyChangeEvent e) {
			if (e.getPropertyName().equals("length")) {
				fireTableDataChanged();
			}
		}

		public Class getColumnClass(int c) {
			if ( c==ALLOCATEBUTTON_COLUMN ) 
				return JButton.class;
			return String.class;
		}

		public int getColumnCount() {
			return ALLOCATEBUTTON_COLUMN+1;
		}

		public int getRowCount() {
			return (activeTrainsList.size());
		}

		public boolean isCellEditable(int r, int c) {
			if ( c==ALLOCATEBUTTON_COLUMN ) 
				return (true);
			return (false);
		}

		public String getColumnName(int col) {
			switch (col) {
			case TRANSIT_COLUMN:
				return rb.getString("TransitColumnTitle");
			case TRAIN_COLUMN:
				return rb.getString("TrainColumnTitle");
			case STATUS_COLUMN:
				return rb.getString("TrainStatusColumnTitle");
			case MODE_COLUMN:
				return rb.getString("TrainModeColumnTitle");
			case ALLOCATED_COLUMN:
				return rb.getString("AllocatedSectionColumnTitle");
			case NEXTSECTION_COLUMN:
				return rb.getString("NextSectionColumnTitle");
			case ALLOCATEBUTTON_COLUMN:
				return (" "); // button columns have no names
			default:
				return "";
			}
		}

		public int getPreferredWidth(int col) {
			switch (col) {
			case TRANSIT_COLUMN:
				return new JTextField(17).getPreferredSize().width;				
			case TRAIN_COLUMN:
				return new JTextField(17).getPreferredSize().width;
			case STATUS_COLUMN:
				return new JTextField(8).getPreferredSize().width;
			case MODE_COLUMN:
				return new JTextField(11).getPreferredSize().width;
			case ALLOCATED_COLUMN:
				return new JTextField(17).getPreferredSize().width;				
			case NEXTSECTION_COLUMN:
				return new JTextField(17).getPreferredSize().width;				
			case ALLOCATEBUTTON_COLUMN:
				return new JTextField(12).getPreferredSize().width;	
			}
			return new JTextField(5).getPreferredSize().width;
		}

		public Object getValueAt(int r, int c) {
			int rx = r;
			if (rx > activeTrainsList.size()) {
				return null;
			}
			ActiveTrain at = (ActiveTrain)activeTrainsList.get(rx);
			switch (c) {
				case TRANSIT_COLUMN:
					return (at.getTransitName());
				case TRAIN_COLUMN:
					return (at.getTrainName());
				case STATUS_COLUMN:
					return (at.getStatusText());
				case MODE_COLUMN:
					return (at.getModeText());
				case ALLOCATED_COLUMN:
					return (at.getLastAllocatedSectionName());
				case NEXTSECTION_COLUMN:
					return (at.getNextSectionToAllocateName());
				case ALLOCATEBUTTON_COLUMN:
					return rb.getString("AllocateButtonName");
				default:
					return (" ");
			}
		}

		public void setValueAt(Object value, int row, int col) {
			if (col==ALLOCATEBUTTON_COLUMN) {
				// open an allocate window
				allocateNextRequested(row);
			}
		}
	}
 	
	/**
	 * Table model for Allocation Request Table in Dispatcher window
	 */
	public class AllocationRequestTableModel extends javax.swing.table.AbstractTableModel implements
			java.beans.PropertyChangeListener {

		public static final int ACTIVE_COLUMN = 0;
		public static final int PRIORITY_COLUMN = 1;
		public static final int SECTION_COLUMN = 2;
		public static final int STATUS_COLUMN = 3;
		public static final int OCCUPANCY_COLUMN = 4;
		public static final int ALLOCATEBUTTON_COLUMN = 5;
		public static final int CANCELBUTTON_COLUMN = 6;

		public AllocationRequestTableModel() {
			super();
		}

		public void propertyChange(java.beans.PropertyChangeEvent e) {
			if (e.getPropertyName().equals("length")) {
				fireTableDataChanged();
			}
		}

		public Class getColumnClass(int c) {
			if ( c==CANCELBUTTON_COLUMN ) 
				return JButton.class;
			if ( c==ALLOCATEBUTTON_COLUMN ) 
				return JButton.class;
			return String.class;
		}

		public int getColumnCount() {
			return CANCELBUTTON_COLUMN+1;
		}

		public int getRowCount() {
			return (allocationRequests.size());
		}

		public boolean isCellEditable(int r, int c) {
			if ( c==CANCELBUTTON_COLUMN ) 
				return (true);
			if ( c==ALLOCATEBUTTON_COLUMN)
				return (true);
			return (false);
		}

		public String getColumnName(int col) {
			switch (col) {
			case ACTIVE_COLUMN:
				return rb.getString("ActiveColumnTitle");
			case PRIORITY_COLUMN:
				return rb.getString("PriorityLabel");			
			case SECTION_COLUMN:
				return rb.getString("SectionColumnTitle");
			case STATUS_COLUMN:
				return rb.getString("StatusColumnTitle");
			case OCCUPANCY_COLUMN:
				return rb.getString("OccupancyColumnTitle");
			case ALLOCATEBUTTON_COLUMN:
				return (" "); // button columns have no names
			case CANCELBUTTON_COLUMN:
				return (" "); // button columns have no names
			default:
				return "";
			}
		}

		public int getPreferredWidth(int col) {
			switch (col) {
			case ACTIVE_COLUMN:
				return new JTextField(30).getPreferredSize().width;				
			case PRIORITY_COLUMN:
				return new JTextField(10).getPreferredSize().width;
			case SECTION_COLUMN:
				return new JTextField(25).getPreferredSize().width;
			case STATUS_COLUMN:
				return new JTextField(15).getPreferredSize().width;
			case OCCUPANCY_COLUMN:
				return new JTextField(10).getPreferredSize().width;
			case ALLOCATEBUTTON_COLUMN:
				return new JTextField(12).getPreferredSize().width;	
			case CANCELBUTTON_COLUMN:
				return new JTextField(10).getPreferredSize().width;	
			}
			return new JTextField(5).getPreferredSize().width;
		}

		public Object getValueAt(int r, int c) {
			int rx = r;
			if (rx > allocationRequests.size()) {
				return null;
			}
			AllocationRequest ar = (AllocationRequest)allocationRequests.get(rx);
			switch (c) {
				case ACTIVE_COLUMN:
					if (_ShortActiveTrainNames) {
						return (ar.getActiveTrain().getTrainName());
					}
					return (ar.getActiveTrainName());
				case PRIORITY_COLUMN:
					return ("   "+ar.getActiveTrain().getPriority());
				case SECTION_COLUMN:
					return (ar.getSectionName());
				case STATUS_COLUMN:
					if (ar.getSection().getState() == Section.FREE) return rb.getString("FREE");
					return rb.getString("ALLOCATED");
				case OCCUPANCY_COLUMN:
					if (!_HasOccupancyDetection) return rb.getString("UNKNOWN");
					if (ar.getSection().getOccupancy() == Section.OCCUPIED) {
						return rb.getString("OCCUPIED");
					}
					return rb.getString("UNOCCUPIED");
				case ALLOCATEBUTTON_COLUMN:
					return rb.getString("AllocateButton");
				case CANCELBUTTON_COLUMN:
					return rb.getString("CancelButton");
				default:
					return (" ");
			}
		}

		public void setValueAt(Object value, int row, int col) {
			if (col==ALLOCATEBUTTON_COLUMN) {
				// open an allocate window
				allocateRequested(row);
			}
			if (col==CANCELBUTTON_COLUMN) {
				// open an allocate window
				cancelAllocationRequest(row);
			}
		}
	}
 	
	/**
	 * Table model for Allocated Section Table in a window requested from the Dispatcher
	 */
	public class AllocatedSectionTableModel extends javax.swing.table.AbstractTableModel implements
			java.beans.PropertyChangeListener {

		public static final int ACTIVE_COLUMN = 0;
		public static final int SECTION_COLUMN = 1;
		public static final int OCCUPANCY_COLUMN = 2;
		public static final int RELEASEBUTTON_COLUMN = 3;

		public AllocatedSectionTableModel() {
			super();
		}

		public void propertyChange(java.beans.PropertyChangeEvent e) {
			if (e.getPropertyName().equals("length")) {
				fireTableDataChanged();
			}
		}

		public Class getColumnClass(int c) {
			if ( c==RELEASEBUTTON_COLUMN ) 
				return JButton.class;
			return String.class;
		}

		public int getColumnCount() {
			return RELEASEBUTTON_COLUMN+1;
		}

		public int getRowCount() {
			return (allocatedSections.size());
		}

		public boolean isCellEditable(int r, int c) {
			if ( c==RELEASEBUTTON_COLUMN ) 
				return (true);
			return (false);
		}

		public String getColumnName(int col) {
			switch (col) {
			case ACTIVE_COLUMN:
				return rb.getString("ActiveColumnTitle");
			case SECTION_COLUMN:
				return rb.getString("AllocatedSectionColumnTitle");
			case OCCUPANCY_COLUMN:
				return rb.getString("OccupancyColumnTitle");
			case RELEASEBUTTON_COLUMN:
				return (" "); // button columns have no names
			default:
				return "";
			}
		}

		public int getPreferredWidth(int col) {
			switch (col) {
			case ACTIVE_COLUMN:
				return new JTextField(30).getPreferredSize().width;				
			case SECTION_COLUMN:
				return new JTextField(25).getPreferredSize().width;
			case OCCUPANCY_COLUMN:
				return new JTextField(10).getPreferredSize().width;
			case RELEASEBUTTON_COLUMN:
				return new JTextField(12).getPreferredSize().width;	
			}
			return new JTextField(5).getPreferredSize().width;
		}

		public Object getValueAt(int r, int c) {
			int rx = r;
			if (rx > allocatedSections.size()) {
				return null;
			}
			AllocatedSection as = (AllocatedSection)allocatedSections.get(rx);
			switch (c) {
				case ACTIVE_COLUMN:
					if (_ShortActiveTrainNames) {
						return (as.getActiveTrain().getTrainName());
					}
					return (as.getActiveTrainName());
				case SECTION_COLUMN:
					return (as.getSectionName());
				case OCCUPANCY_COLUMN:
					if (!_HasOccupancyDetection) return rb.getString("UNKNOWN");
					if (as.getSection().getOccupancy() == Section.OCCUPIED) {
						return rb.getString("OCCUPIED");
					}
					return rb.getString("UNOCCUPIED");
				case RELEASEBUTTON_COLUMN:
					return rb.getString("ReleaseButton");
				default:
					return (" ");
			}
		}

		public void setValueAt(Object value, int row, int col) {
			if (col==RELEASEBUTTON_COLUMN) {
				releaseAllocatedSectionFromTable(row);
			}
		}
	}
   
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DispatcherFrame.class.getName());

}
