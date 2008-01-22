// NceConsistEditFrame.java

package jmri.jmrix.nce.consist;

import jmri.DccLocoAddress;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.nce.*;

import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Frame for user edit of NCE Consists
 * 
 * NCE Consists are stored in Command Station (CS) memory starting at address
 * xF500 and ending xFAFF. NCE supports up to 127 consists, numbered 1 to 127.
 * They track the lead engine, rear engine, and four mid engines in the consist
 * file. NCE cabs start at consist 127 when building and reviewing consists, so
 * we also start with 127. Consist lead engines are stored in memory locations
 * xF500 through xF5FF. Consist rear engines are stored in memory locations
 * xF600 through xF6FF. Mid consist engines (four max) are stored in memory
 * locations xF700 through xFAFF. If a long address is in use, bits 6 and 7 of
 * the high byte are set. Example: Long address 3 = 0xc0 0x03 Short address 3 =
 * 0x00 0x03
 * 
 * NCE file format:
 * 
 * :F500 (con 0 lead engine) (con 1 lead engine) ....... (con 7 lead engine)
 * :F510 (con 8 lead engine) ........ (con 15 lead engine) . . :F5F0 (con 120
 * lead engine) ..... (con 127 lead engine)
 * 
 * :F600 (con 0 rear engine) (con 1 rear engine) ....... (con 7 rear engine) . .
 * :F6F0 (con 120 rear engine) ..... (con 127 rear engine)
 * 
 * :F700 (con 0 mid eng1) (con 0 mid eng2) (con 0 mid eng3) (con 0 mid eng4) . .
 * :FAF0 (con 126 mid eng1) .. (con 126 mid eng4)(con 127 mid eng1) .. (con 127
 * mid eng4) :0000
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision: 1.17 $
 */

public class NceConsistEditFrame extends jmri.util.JmriJFrame implements
		jmri.jmrix.nce.NceListener {

	private static final int CS_CONSIST_MEM = 0xF500; 	// start of NCE CS Consist memory
	private static final int CS_CON_MEM_REAR = 0xF600; 	// start of rear consist engines
	private static final int CS_CON_MEM_MID = 0xF700; 	// start of mid consist engines
	private static final int CONSIST_MIN = 1; 			// NCE doesn't use consist 0
	private static final int CONSIST_MAX = 127;
	private static final int LOC_ADR_MIN = 1; 			// loco address range
	private static final int LOC_ADR_MAX = 9999;
	private static final int LOC_ADR_REPLACE = 0x3FFF; 	// dummy loco address
														// used when replacing
														// lead or rear loco
	private int consistNum = 127; 				// consist being worked
	private boolean newConsist = true; 			// new consist is displayed
	private int engineNum = 0; 					// which engine, 0 = lead, 1 = rear, 2 = mid
	private static final int LEAD = 0;
	private static final int REAR = 1;
	private static final int MID = 2;

	private int consistNumVerify; 				// which consist number we're checking
	private int engineVerify; 					// engine number being verified
	private int midEngineDir; 					// mid engine direction
	private int engNum; 						// report engine alreay in use
	private static final int MID_NONE = 0; 		// not mid engine being verified
	private static final int MID_FWD = 1; 		// mid engine foward being verified
	private static final int MID_REV = 2; 		// mid engine reverse being verified

	private static final String WAIT = "waiting"; // NCE consist editor states
	private static final String OKAY = "okay";
	private static final String SEARCH = "searching";
	private static final String ERROR = "error";
	private static final String VERIFY = "verifying";
	private static final String UNKNOWN = "unknown";
	private static final String NONE = "none";

	private static final int REPLY_1 = 1; 		// reply length of 1 byte expected
	private static final int REPLY_16 = 16; 	// reply length of 16 bytes expected
	private int replyLen = 0; 					// expected byte length
	private int waiting = 0; 					// to catch responses not intended for this module

	private static final String DELETE = "  Delete "; 	// Keys, with some padding for display purposes
	private static final String ADD = "    Add   ";
	private static final String REPLACE = "Replace";
	private static final String QUESTION = "  ??  ";
	private static final String FWD = "Forward";
	private static final String REV = "Reverse";
	private static final String LONG = "Long";
	private static final String SHORT = "Short";
	private static final String CLEAR = "Clear";
	private static final String CANCEL = "Cancel";
	private static final String SAVE = "Save";
	private static final String LOAD = "Load";
	private static final String EMPTY = "";
	private static final String REPLACE_LOCO = "NONE";

	private static final String ToolTipAdd = "Press to add engine to consist";
	private static final String ToolTipClear = "Press to kill consist";
	private static final String ToolTipCancel = "Press to cancel consist load from roster";
	private static final String ToolTipSave = "Press to save consist to roster";
	private static final String ToolTipLoad = "Press to load consist into NCE CS and program engines";

	// the reply states
	private boolean consistSearchNext = false; 		// next search
	private boolean consistSearchPrevious = false; 	// previous search
	private boolean engineSearch = false; 			// when true searching for lead or rear engine in consist
	private boolean emptyConsistSearch = false; 	// when true searching for an empty consist
	private boolean verifyRosterMatch = false; 	// when true verify that roster matches consist in NCE CS
	
	private int emptyConsistSearchStart = CONSIST_MAX;
	
	private boolean engineMatch = false; 			// when true found lead or rear engine in another consist
	private int consistCount = 0; 					// search count not to exceed CONSIST_MAX
	private boolean consistModified = false; 		// when true, consist has been modified by user

	private boolean refresh = false; 				// when true, refresh loco address from CS

	// member declarations
	javax.swing.JLabel textConsist = new javax.swing.JLabel();
	javax.swing.JLabel textReply = new javax.swing.JLabel();
	javax.swing.JLabel consistReply = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton previousButton = new javax.swing.JButton();
	javax.swing.JButton nextButton = new javax.swing.JButton();
	javax.swing.JButton getButton = new javax.swing.JButton();
	javax.swing.JButton clearButton = new javax.swing.JButton();
	javax.swing.JButton saveButton = new javax.swing.JButton();
	javax.swing.JButton deleteButton = new javax.swing.JButton();
	javax.swing.JButton backUpButton = new javax.swing.JButton();
	javax.swing.JButton restoreButton = new javax.swing.JButton();

	// check boxes
	javax.swing.JCheckBox checkBoxEmpty = new javax.swing.JCheckBox();
	javax.swing.JCheckBox checkBoxVerify = new javax.swing.JCheckBox();
	javax.swing.JCheckBox checkBoxConsist = new javax.swing.JCheckBox();

	// consist text field
	javax.swing.JTextField consistTextField = new javax.swing.JTextField(4);

	// labels
	javax.swing.JLabel textEngine = new javax.swing.JLabel();
	javax.swing.JLabel textRoster = new javax.swing.JLabel();
	javax.swing.JLabel textAddress = new javax.swing.JLabel();
	javax.swing.JLabel textAddrType = new javax.swing.JLabel();
	javax.swing.JLabel textDirection = new javax.swing.JLabel();

	javax.swing.JLabel textConRoster = new javax.swing.JLabel();
	javax.swing.JLabel textConRoadName = new javax.swing.JLabel();
	javax.swing.JLabel textConRoadNumber = new javax.swing.JLabel();
	
	javax.swing.JComboBox conRosterBox = ConsistRoster.instance().fullRosterComboBox();

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();

	// lead engine
	javax.swing.JLabel textEng1 = new javax.swing.JLabel();
	javax.swing.JTextField engTextField1 = new javax.swing.JTextField(4);
	javax.swing.JComboBox locoRosterBox1 = Roster.instance().fullRosterComboBox();
	javax.swing.JButton adrButton1 = new javax.swing.JButton();
	javax.swing.JButton cmdButton1 = new javax.swing.JButton();
	javax.swing.JButton dirButton1 = new javax.swing.JButton();

	// rear engine
	javax.swing.JLabel textEng2 = new javax.swing.JLabel();
	javax.swing.JTextField engTextField2 = new javax.swing.JTextField(4);
	javax.swing.JComboBox locoRosterBox2 = Roster.instance().fullRosterComboBox();
	javax.swing.JButton adrButton2 = new javax.swing.JButton();
	javax.swing.JButton cmdButton2 = new javax.swing.JButton();
	javax.swing.JButton dirButton2 = new javax.swing.JButton();

	// mid engine
	javax.swing.JLabel textEng3 = new javax.swing.JLabel();
	javax.swing.JTextField engTextField3 = new javax.swing.JTextField(4);
	javax.swing.JComboBox locoRosterBox3 = Roster.instance().fullRosterComboBox();
	javax.swing.JButton adrButton3 = new javax.swing.JButton();
	javax.swing.JButton cmdButton3 = new javax.swing.JButton();
	javax.swing.JButton dirButton3 = new javax.swing.JButton();

	// mid engine
	javax.swing.JLabel textEng4 = new javax.swing.JLabel();
	javax.swing.JTextField engTextField4 = new javax.swing.JTextField(4);
	javax.swing.JComboBox locoRosterBox4 = Roster.instance().fullRosterComboBox();
	javax.swing.JButton adrButton4 = new javax.swing.JButton();
	javax.swing.JButton cmdButton4 = new javax.swing.JButton();
	javax.swing.JButton dirButton4 = new javax.swing.JButton();

	// mid engine
	javax.swing.JLabel textEng5 = new javax.swing.JLabel();
	javax.swing.JTextField engTextField5 = new javax.swing.JTextField(4);
	javax.swing.JComboBox locoRosterBox5 = Roster.instance().fullRosterComboBox();
	javax.swing.JButton adrButton5 = new javax.swing.JButton();
	javax.swing.JButton cmdButton5 = new javax.swing.JButton();
	javax.swing.JButton dirButton5 = new javax.swing.JButton();

	// mid engine
	javax.swing.JLabel textEng6 = new javax.swing.JLabel();
	javax.swing.JTextField engTextField6 = new javax.swing.JTextField(4);
	javax.swing.JComboBox locoRosterBox6 = Roster.instance().fullRosterComboBox();
	javax.swing.JButton adrButton6 = new javax.swing.JButton();
	javax.swing.JButton cmdButton6 = new javax.swing.JButton();
	javax.swing.JButton dirButton6 = new javax.swing.JButton();

	public NceConsistEditFrame() {
		super();
	}

	public void initComponents() throws Exception {
		// the following code sets the frame's initial state

		textConsist.setText("Consist");
		textConsist.setVisible(true);

		textReply.setText("Reply:");
		textReply.setVisible(true);

		consistReply.setText(UNKNOWN);
		consistReply.setVisible(true);

		previousButton.setText("Previous");
		previousButton.setVisible(true);
		previousButton.setToolTipText("Search for consist incrementing");

		nextButton.setText("   Next   ");
		nextButton.setVisible(true);
		nextButton.setToolTipText("Search for consist decrementing");

		getButton.setText("  Get  ");
		getButton.setVisible(true);
		getButton.setToolTipText("Read consist from NCE CS");

		consistTextField.setText(Integer.toString(consistNum));
		consistTextField.setToolTipText("Enter consist 1 to 127");
		consistTextField.setMaximumSize(new Dimension(consistTextField
				.getMaximumSize().width,
				consistTextField.getPreferredSize().height));

		textEngine.setText("Engine");
		textEngine.setVisible(true);
		textRoster.setText("Roster");
		textRoster.setVisible(true);
		textAddress.setText("Address");
		textAddress.setVisible(true);
		textAddrType.setText("Type");
		textAddrType.setVisible(true);
		textDirection.setText("Direction");
		textDirection.setVisible(true);

		textConRoster.setText("Consist");
		textConRoster.setVisible(true);
		textConRoadName.setText(EMPTY);
		textConRoadName.setVisible(true);
		textConRoadNumber.setText(EMPTY);
		textConRoadNumber.setVisible(true);

		clearButton.setText(CLEAR);
		clearButton.setVisible(true);
		clearButton.setEnabled(false);
		clearButton.setToolTipText(ToolTipClear);

		saveButton.setText(SAVE);
		saveButton.setVisible(false);
		saveButton.setEnabled(false);
		saveButton.setToolTipText(ToolTipSave);

		deleteButton.setText("Delete");
		deleteButton.setVisible(false);
		deleteButton.setEnabled(false);
		deleteButton.setToolTipText("Press to remove consist from roster");

		backUpButton.setText(" Backup ");
		backUpButton.setVisible(true);
		backUpButton.setToolTipText("Save all consists to a file");

		restoreButton.setText(" Restore");
		restoreButton.setVisible(true);
		restoreButton.setToolTipText("Restore all consists from a file");

		checkBoxEmpty.setText("Empty Consist");
		checkBoxEmpty.setVisible(true);
		checkBoxEmpty.setToolTipText("Check to search for empty consists");

		checkBoxVerify.setText("Verify engine");
		checkBoxVerify.setVisible(true);
		checkBoxVerify.setSelected(true);
		checkBoxVerify
				.setToolTipText("Verify that add engine isn't already a consist lead or rear engine");

		checkBoxConsist.setText("Consist roster");
		checkBoxConsist.setVisible(true);
		checkBoxConsist.setSelected(false);
		checkBoxConsist.setToolTipText("Check to build a consist roster");

		space1.setText("            ");
		space1.setVisible(true);
		space2.setText(" ");
		space2.setVisible(true);
		space3.setText(" ");
		space3.setVisible(true);

		initEngFields();

		setTitle("Edit NCE Consist");
		getContentPane().setLayout(new GridBagLayout());

		// Layout the panel by rows
		// row 0
		addItem(textConsist, 2, 0);
		// row 1
		addItem(previousButton, 1, 1);
		addItem(consistTextField, 2, 1);
		addItem(nextButton, 3, 1);
		addItem(checkBoxEmpty, 4, 1);
		// row 2
		addItem(textReply, 0, 2);
		addItem(consistReply, 1, 2);
		addItem(getButton, 2, 2);
		addItem(checkBoxVerify, 4, 2);
		// row 3
		addItem(space3, 1, 3);
		// row 4
		addItem(textConRoster, 1, 4);
		// row 5
		addItem(conRosterBox, 1, 5);
		addItem (textConRoadName, 2, 5);
		addItem (textConRoadNumber, 3, 5);
		addItem(checkBoxConsist, 4, 5);
		initConsistRoster(conRosterBox);

		// row 6 padding for looks
		addItem(space1, 1, 6);
		// row 7 labels
		addItem(textEngine, 0, 7);
		addItem(textRoster, 1, 7);
		addItem(textAddress, 2, 7);
		addItem(textAddrType, 3, 7);
		addItem(textDirection, 4, 7);

		// row 8 Lead Engine
		addEngRow(textEng1, locoRosterBox1, engTextField1, adrButton1,
				dirButton1, cmdButton1, 8);
		// row 9 Rear Engine
		addEngRow(textEng2, locoRosterBox2, engTextField2, adrButton2,
				dirButton2, cmdButton2, 9);
		// row 10 Mid Engine
		addEngRow(textEng3, locoRosterBox3, engTextField3, adrButton3,
				dirButton3, cmdButton3, 10);
		// row 11 Mid Engine
		addEngRow(textEng4, locoRosterBox4, engTextField4, adrButton4,
				dirButton4, cmdButton4, 11);
		// row 12 Mid Engine
		addEngRow(textEng5, locoRosterBox5, engTextField5, adrButton5,
				dirButton5, cmdButton5, 12);
		// row 13 Mid Engine
		addEngRow(textEng6, locoRosterBox6, engTextField6, adrButton6,
				dirButton6, cmdButton6, 13);

		// row 15 padding for looks
		addItem(space2, 2, 15);
		// row 16
		addItem(clearButton, 1, 16);
		addItem(saveButton, 2, 16);
		addItem(deleteButton, 3, 16);
		addItem(backUpButton, 4, 16);
		addItem(restoreButton, 5, 16);

		// setup buttons
		addButtonAction(previousButton);
		addButtonAction(nextButton);
		addButtonAction(getButton);
		addButtonAction(clearButton);
		addButtonAction(saveButton);
		addButtonAction(deleteButton);
		addButtonAction(backUpButton);
		addButtonAction(restoreButton);

		// setup checkboxes
		addCheckBoxAction(checkBoxConsist);

		// set frame size for display
		this.setSize(450, 380);
	}

	// Previous, Next, Get, Clear/Cancel, Save/Load, Delete, Restore & Backup buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		// if we're searching ignore user
		if (consistSearchNext || consistSearchPrevious)
			return;

		// clear or cancel button
		if (ae.getSource() == clearButton) {
			// button can be Clear or Cancel
			if (clearButton.getText().equals(CLEAR)) {
				updateRoster("0");
				// set refresh flag to update panel
				refresh = true;
				killConsist();

				// must be cancel button
			} else {
				changeButtons(false);
				consistNum = getConsist(); // reload panel
			}
		}

		// save or load button
		if (ae.getSource() == saveButton) {
			if (!validConsist())
				return;
			updateRoster(consistTextField.getText());
			if (saveButton.getText().equals(LOAD)) {
				// set refresh flag to update panel
				consistNum = validConsist(consistTextField.getText());
				refresh = true;
				loadOneLine(locoRosterBox1, engTextField1, adrButton1,
						dirButton1, cmdButton1);
				loadOneLine(locoRosterBox2, engTextField2, adrButton2,
						dirButton2, cmdButton2);
				loadOneLine(locoRosterBox3, engTextField3, adrButton3,
						dirButton3, cmdButton3);
				loadOneLine(locoRosterBox4, engTextField4, adrButton4,
						dirButton4, cmdButton4);
				loadOneLine(locoRosterBox5, engTextField5, adrButton5,
						dirButton5, cmdButton5);
				loadOneLine(locoRosterBox6, engTextField6, adrButton6,
						dirButton6, cmdButton6);
			}
			changeButtons(false);
			return;
		}

		// delete button
		if (ae.getSource() == deleteButton) {
			if (JOptionPane.showConfirmDialog(null,
					"Are you sure you want to delete consist "
							+ conRosterBox.getSelectedItem().toString()
							+ " from roster?", "Delete consist?",
					JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
				return;
			}
			deleteRoster();
			changeButtons(false); // yes, clear delete button
			return;
		}

		if (consistModified) {
			// warn user that consist has been modified
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
							"Consist has been modified, use Save to update NCE CS memory",
							"NCE Consist", JOptionPane.WARNING_MESSAGE);
			consistModified = false; // only one warning!!!

		} else {

			if (ae.getSource() == previousButton) {
				consistSearchPrevious = true;
				consistNum = getConsist(); // check for valid and kick off read
			}
			if (ae.getSource() == nextButton) {
				consistSearchNext = true;
				consistNum = getConsist(); // check for valid and kick off read
			}
			if (ae.getSource() == getButton) {
				// Get Consist
				consistNum = getConsist();
			}
			if (ae.getSource() == backUpButton) {
				Thread mb = new NceConsistBackup();
				mb.setName("Consist Backup");
				mb.start();
			}
			if (ae.getSource() == restoreButton) {
				Thread mr = new NceConsistRestore();
				mr.setName("Consist Restore");
				mr.start();
			}
		}
	}

	// One of six loco command buttons, add, replace or delete
	public void buttonActionCmdPerformed(java.awt.event.ActionEvent ae) {
		// if we're searching ignore user
		if (consistSearchNext || consistSearchPrevious)
			return;
		if (consistChanged())
			return;
		if (ae.getSource() == cmdButton1)
			modifyLocoFields(locoRosterBox1, engTextField1, adrButton1,
					dirButton1, cmdButton1);
		if (ae.getSource() == cmdButton2)
			modifyLocoFields(locoRosterBox2, engTextField2, adrButton2,
					dirButton2, cmdButton2);
		if (ae.getSource() == cmdButton3)
			modifyLocoFields(locoRosterBox3, engTextField3, adrButton3,
					dirButton3, cmdButton3);
		if (ae.getSource() == cmdButton4)
			modifyLocoFields(locoRosterBox4, engTextField4, adrButton4,
					dirButton4, cmdButton4);
		if (ae.getSource() == cmdButton5)
			modifyLocoFields(locoRosterBox5, engTextField5, adrButton5,
					dirButton5, cmdButton5);
		if (ae.getSource() == cmdButton6)
			modifyLocoFields(locoRosterBox6, engTextField6, adrButton6,
					dirButton6, cmdButton6);
		updateRoster(consistTextField.getText());
	}

	// one of six loco address type buttons
	public void buttonActionAdrPerformed(java.awt.event.ActionEvent ae) {
		// if we're searching ignore user
		if (consistSearchNext || consistSearchPrevious)
			return;
		if (consistChanged())
			return;
		if (ae.getSource() == adrButton1)
			toggleAdrButton(engTextField1, adrButton1);
		if (ae.getSource() == adrButton2)
			toggleAdrButton(engTextField2, adrButton2);
		if (ae.getSource() == adrButton3)
			toggleAdrButton(engTextField3, adrButton3);
		if (ae.getSource() == adrButton4)
			toggleAdrButton(engTextField4, adrButton4);
		if (ae.getSource() == adrButton5)
			toggleAdrButton(engTextField5, adrButton5);
		if (ae.getSource() == adrButton6)
			toggleAdrButton(engTextField6, adrButton6);
	}

	private void toggleAdrButton(JTextField engTextField, JButton adrButton) {
		if (validLocoAdr(engTextField.getText()) < 0)
			return;
		if (engTextField.getText().equals(EMPTY)) {
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter loco address before changing address type",
					"NCE Consist", JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			if (adrButton.getText() == LONG) {
				if (Integer.parseInt(engTextField.getText()) < 128) {
					adrButton.setText(SHORT);
				}
			} else {
				adrButton.setText(LONG);
			}
		}
	}

	// one of six loco direction buttons
	public void buttonActionDirPerformed(java.awt.event.ActionEvent ae) {
		// if we're searching ignore user
		if (consistSearchNext || consistSearchPrevious)
			return;
		if (consistChanged())
			return;
		if (ae.getSource() == dirButton1)
			toggleDirButton(engTextField1, dirButton1, cmdButton1);
		if (ae.getSource() == dirButton2)
			toggleDirButton(engTextField2, dirButton2, cmdButton2);
		if (ae.getSource() == dirButton3)
			toggleDirButton(engTextField3, dirButton3, cmdButton3);
		if (ae.getSource() == dirButton4)
			toggleDirButton(engTextField4, dirButton4, cmdButton4);
		if (ae.getSource() == dirButton5)
			toggleDirButton(engTextField5, dirButton5, cmdButton5);
		if (ae.getSource() == dirButton6)
			toggleDirButton(engTextField6, dirButton6, cmdButton6);
		saveButton.setEnabled(canLoad());
	}

	private void toggleDirButton(JTextField engTextField, JButton dirButton,
			JButton cmdButton) {
		if (validLocoAdr(engTextField.getText()) < 0)
			return;
		if (engTextField.getText().equals(EMPTY)) {
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter loco address before changing loco direction",
					"NCE Consist", JOptionPane.ERROR_MESSAGE);
			return;
		}
		cmdButton.setEnabled(true);
		if (dirButton.getText() == FWD) {
			dirButton.setText(REV);
		} else {
			dirButton.setText(FWD);
		}
	}

	// one of six roster select, load engine number and address length
	public void locoSelected(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == locoRosterBox1)
			rosterBoxSelect(locoRosterBox1, engTextField1, adrButton1);
		if (ae.getSource() == locoRosterBox2)
			rosterBoxSelect(locoRosterBox2, engTextField2, adrButton2);
		if (ae.getSource() == locoRosterBox3)
			rosterBoxSelect(locoRosterBox3, engTextField3, adrButton3);
		if (ae.getSource() == locoRosterBox4)
			rosterBoxSelect(locoRosterBox4, engTextField4, adrButton4);
		if (ae.getSource() == locoRosterBox5)
			rosterBoxSelect(locoRosterBox5, engTextField5, adrButton5);
		if (ae.getSource() == locoRosterBox6)
			rosterBoxSelect(locoRosterBox6, engTextField6, adrButton6);
	}
	// load a loco from roster
	private void rosterBoxSelect(JComboBox locoRosterBox,
			JTextField engTextField, JButton adrButton) {
		String rosterEntryTitle = locoRosterBox.getSelectedItem().toString();
		if (rosterEntryTitle == EMPTY)
			return;
		RosterEntry entry = Roster.instance().entryFromTitle(rosterEntryTitle);
		DccLocoAddress a = entry.getDccLocoAddress();
		if (a != null) {
			engTextField.setText("" + a.getNumber());
			if (a.isLongAddress())
				adrButton.setText(LONG);
			else
				adrButton.setText(SHORT);
			// if lead engine get road number and name
			if (locoRosterBox == locoRosterBox1){
				textConRoadName.setText(entry.getRoadName());
				textConRoadNumber.setText(entry.getRoadNumber());	
			}
		}
	}

	// load a consist from roster
	public void consistRosterSelected(java.awt.event.ActionEvent ae) {
		if (consistSearchNext || consistSearchPrevious)
			return;
		String entry = EMPTY;
		try {
			entry = conRosterBox.getSelectedItem().toString();
		} catch (Exception e) {

		}
		log.debug("load consist " + entry + " from roster ");
		if (entry == EMPTY) {
			changeButtons(false);
			consistNum = getConsist(); // reload panel
			return;
		}
		changeButtons(true);
		loadRosterEntry(entry);
	}

	// checkbox modified
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == checkBoxConsist) {
			if (checkBoxConsist.isSelected()) {
				conRosterBox.setEnabled(true);
				saveButton.setVisible(true);
				saveButton.setEnabled(canLoad());
				deleteButton.setVisible(true);
			} else {
				conRosterBox.setEnabled(false);
				conRosterBox.removeActionListener(consistRosterListener);
				conRosterBox.setSelectedIndex(0);
				conRosterBox.addActionListener(consistRosterListener);
				saveButton.setVisible(false);
				saveButton.setEnabled(false);
				deleteButton.setVisible(false);
				deleteButton.setEnabled(false);
			}
		}
	}

	// gets the user supplied consist number and then reads NCE CS memory
	private int getConsist() {
		newConsist = true;
		int cN = validConsist(consistTextField.getText());
		if (!validConsist()) {
			consistSearchPrevious = false;
			consistSearchNext = false;
			return cN;
		}
		if (consistSearchNext || consistSearchPrevious) {
			consistCount = 0; // used to determine if all 127 consist have been read
			consistReply.setText(SEARCH);
		} else {
			consistReply.setText(WAIT);
			if (cN == consistNum) {
				newConsist = false;
			}
		}

		// if busy don't request
		if (waiting > 0)
			return cN;

		if (consistSearchNext) {
			readConsistMemory(cN - 7, LEAD);
		} else {
			readConsistMemory(cN, LEAD);
		}
		return cN;
	}

	/**
	 * Check for valid consist, popup error message if not
	 * @return true if valid
	 */
	private boolean validConsist() {
		int cN = validConsist(consistTextField.getText());
		if (cN == -1) {
			consistReply.setText(ERROR);
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter consist number 1 to 127", "NCE Consist",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	// Check for valid consist number, return number if valid, -1 if not.
	private int validConsist(String s) {
		int cN;
		try {
			cN = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return -1;
		}
		if (cN < CONSIST_MIN | cN > CONSIST_MAX)
			return -1;
		else
			return cN;
	}

	/**
	 * @param s loco address
	 * @return number if valid, -1 if not
	 */
	private int validLocoAdr(String s) {
		int lA;
		try {
			lA = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			lA = -1;
		}
		if (lA < LOC_ADR_MIN | lA > LOC_ADR_MAX) {
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter loco address 1 to 9999", "NCE Consist",
					JOptionPane.ERROR_MESSAGE);
			return -1;
		} else
			return lA;
	}

	// check to see if user modified consist number
	private boolean consistChanged() {
		if (consistNum != validConsist(consistTextField.getText())) {
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Press Get to read consist", "NCE Consist",
					JOptionPane.ERROR_MESSAGE);
			return true;
		} else {
			newConsist = false;
			return false;
		}
	}

	/**
	 * Reads 16 bytes of NCE consist memory based on consist number and engine
	 * number 0=lead 1=rear 2=mid
	 */
	private void readConsistMemory(int consistNum, int eNum) {
		engineNum = eNum;
		int nceConsistBaseMemory = CS_CONSIST_MEM;
		if (eNum == REAR)
			nceConsistBaseMemory = CS_CON_MEM_REAR;
		int nceMemAddr = (consistNum * 2) + nceConsistBaseMemory;
		if (eNum == MID) {
			nceConsistBaseMemory = CS_CON_MEM_MID;
			nceMemAddr = (consistNum * 8) + nceConsistBaseMemory;
		}
		byte[] bl = NceBinaryCommand.accMemoryRead(nceMemAddr);
		sendNceMessage(bl, REPLY_16);
	}
	
	ConsistRosterEntry cre;

	private void loadRosterEntry(String entry) {
		cre = ConsistRoster.instance().entryFromTitle(entry);
		consistTextField.setText(cre.getConsistNumber());
		int cNum = Integer.parseInt(cre.getConsistNumber());

		if (cNum > 0 && cNum < 128) {
			log.debug("verify consist matches roster selection");
			verifyRosterMatch = true;
			consistNum = getConsist();
		} else if (cNum == 0) {
			log.debug("search for empty consist");
			consistTextField.setText(Integer.toString(emptyConsistSearchStart));
			emptyConsistSearch = true;
			consistSearchNext = true;
			consistNum = getConsist();
			loadFullRoster (cre);
			saveButton.setEnabled(false);
		} else {
			log.error("roster consist number is out of range: " + consistNum);
			consistReply.setText(ERROR);
		}
	}
	
	private void loadFullRoster (ConsistRosterEntry cre){
		// get road name and number
		textConRoadName.setText(cre.getRoadName());
		textConRoadNumber.setText(cre.getRoadNumber());	
		
		// load lead loco
		engTextField1.setText(cre.getEng1DccAddress());
		adrButton1.setText(cre.isEng1LongAddress() ? LONG : SHORT);
		dirButton1.setText(directionWord(cre.getEng1Direction()));
		locoRosterBox1.setEnabled(true);
		engTextField1.setEnabled(true);
		adrButton1.setEnabled(true);
		dirButton1.setEnabled(true);

		// load rear loco
		engTextField2.setText(cre.getEng2DccAddress());
		adrButton2.setText(cre.isEng2LongAddress() ? LONG : SHORT);
		dirButton2.setText(directionWord(cre.getEng2Direction()));
		locoRosterBox2.setEnabled(true);
		engTextField2.setEnabled(true);
		adrButton2.setEnabled(true);
		dirButton2.setEnabled(true);

		// load Mid1 loco
		engTextField3.setText(cre.getEng3DccAddress());
		adrButton3.setText(cre.isEng3LongAddress() ? LONG : SHORT);
		dirButton3.setText(directionWord(cre.getEng3Direction()));
		locoRosterBox3.setEnabled(true);
		engTextField3.setEnabled(true);
		adrButton3.setEnabled(true);
		dirButton3.setEnabled(true);

		// load Mid2 loco
		engTextField4.setText(cre.getEng4DccAddress());
		adrButton4.setText(cre.isEng4LongAddress() ? LONG : SHORT);
		dirButton4.setText(directionWord(cre.getEng4Direction()));
		locoRosterBox4.setEnabled(true);
		engTextField4.setEnabled(true);
		adrButton4.setEnabled(true);
		dirButton4.setEnabled(true);

		// load Mid3 loco
		engTextField5.setText(cre.getEng5DccAddress());
		adrButton5.setText(cre.isEng5LongAddress() ? LONG : SHORT);
		dirButton5.setText(directionWord(cre.getEng5Direction()));
		locoRosterBox5.setEnabled(true);
		engTextField5.setEnabled(true);
		adrButton5.setEnabled(true);
		dirButton5.setEnabled(true);

		// load Mid4 loco
		engTextField6.setText(cre.getEng6DccAddress());
		adrButton6.setText(cre.isEng6LongAddress() ? LONG : SHORT);
		dirButton6.setText(directionWord(cre.getEng6Direction()));
		locoRosterBox6.setEnabled(true);
		engTextField6.setEnabled(true);
		adrButton6.setEnabled(true);
		dirButton6.setEnabled(true);
	}
	
	// checks to see if all engine addresses in consist match roster 
	private boolean consistRosterMatch (ConsistRosterEntry cre){
		if (engTextField1.getText().equals(cre.getEng1DccAddress())
				&& engTextField2.getText().equals(cre.getEng2DccAddress())
				&& engTextField3.getText().equals(cre.getEng3DccAddress())
				&& engTextField4.getText().equals(cre.getEng4DccAddress())
				&& engTextField5.getText().equals(cre.getEng5DccAddress())
				&& engTextField6.getText().equals(cre.getEng6DccAddress())
						){
			return true;
		} else {
			return false;
		}
	}

	protected List consistList = new ArrayList();

	/**
	 * returns true if update successful
	 */
	private boolean updateRoster(String consistNumber) {
		if (!checkBoxConsist.isSelected())
			return false;
		String id = engTextField1.getText(); // lead loco is the consist id
		if (id.equals(EMPTY)) {
			log.debug("Attempt to modify consist without valid id");
			return false;
		}
		// need rear loco to form a consist
		if (engTextField2.getText().equals(EMPTY))
			return false;
		ConsistRosterEntry cre;
		consistList = ConsistRoster.instance().matchingList(null, null,
				null, null, null, null, null, null, null, id);
		// if consist doesn't exist in roster ask user if they want to create one
		if (consistList.isEmpty()) {
			if (JOptionPane.showConfirmDialog(null, "The consist " + id
					+ " doesn't exist in your roster,"
					+ " do you want to add it?", "Save consist to roster?",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return false;
			}
			cre = new ConsistRosterEntry();
			ConsistRoster.instance().addEntry(cre);
			// roster entry exsists, does it match?
		} else {
			cre = ConsistRoster.instance().entryFromTitle(id);
			// if all of the loco addresses match, just update without telling user
			consistList = ConsistRoster.instance()
					.matchingList(null, null, null, engTextField1.getText(),
							engTextField2.getText(), engTextField3.getText(),
							engTextField4.getText(), engTextField5.getText(),
							engTextField6.getText(), id);
			// if it doesn't match, do we want to modify it?
			if (consistList.isEmpty()) {
				if (JOptionPane.showConfirmDialog(null, "Consist " + id
						+ " already exists, update?", "Modify consist?",
						JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
					return false;
				}
			}
			log.debug("Modify consist " + id);
			// delete consist from NCE CS only if we're loading
			if (saveButton.getText().equals(LOAD))
				killConsist();
		}
		// save all elements of a consist roster
		cre.setId(id);
		cre.setConsistNumber(consistNumber);
		cre.setRoadName(textConRoadName.getText());
		cre.setRoadNumber(textConRoadNumber.getText());
		// save lead loco
		cre.setEng1DccAddress(engTextField1.getText());
		cre.setEng1LongAddress(adrButton1.getText().equals(LONG));
		cre.setEng1Direction(lDirection(dirButton1));
		// save rear loco
		cre.setEng2DccAddress(engTextField2.getText());
		cre.setEng2LongAddress(adrButton2.getText().equals(LONG));
		cre.setEng2Direction(lDirection(dirButton2));
		// save Mid1 loco
		cre.setEng3DccAddress(engTextField3.getText());
		cre.setEng3LongAddress(adrButton3.getText().equals(LONG));
		cre.setEng3Direction(lDirection(dirButton3));
		// save Mid2 loco
		cre.setEng4DccAddress(engTextField4.getText());
		cre.setEng4LongAddress(adrButton4.getText().equals(LONG));
		cre.setEng4Direction(lDirection(dirButton4));
		// save Mid3 loco
		cre.setEng5DccAddress(engTextField5.getText());
		cre.setEng5LongAddress(adrButton5.getText().equals(LONG));
		cre.setEng5Direction(lDirection(dirButton5));
		// save Mid4 loco
		cre.setEng6DccAddress(engTextField6.getText());
		cre.setEng6LongAddress(adrButton6.getText().equals(LONG));
		cre.setEng6Direction(lDirection(dirButton6));

		writeRosterFile();
		return true;
	}

	private String lDirection(JButton dirButton) {
		String letter = EMPTY;
		if (dirButton.getText().equals(FWD))
			letter = "F";
		if (dirButton.getText().equals(REV))
			letter = "R";
		return letter;
	}

	private String directionWord(String letter) {
		String word = QUESTION;
		if (letter.equals("F"))
			word = FWD;
		if (letter.equals("R"))
			word = REV;
		return word;
	}

	// remove selected consist from roster
	private void deleteRoster() {
		String entry = conRosterBox.getSelectedItem().toString();
		log.debug("remove consist " + entry + " from roster ");
		// delete it from roster
		ConsistRoster.instance().removeEntry(
				ConsistRoster.instance().entryFromTitle(entry));
		writeRosterFile();
	}

	private void writeRosterFile() {
		conRosterBox.removeActionListener(consistRosterListener);
		ConsistRoster.writeRosterFile();
		ConsistRoster.instance().updateComboBox(conRosterBox);
		conRosterBox.insertItemAt(EMPTY, 0);
		conRosterBox.setSelectedIndex(0);
		conRosterBox.addActionListener(consistRosterListener);
	}

	// can the consist be loading into NCE memory?
	private boolean canLoad() {
		if (engTextField1.getText().equals(EMPTY))
			return false;
		if (dirButton1.getText().equals(QUESTION))
			return false;
		if (engTextField2.getText().equals(EMPTY))
			return false;
		if (dirButton2.getText().equals(QUESTION))
			return false;
		if (!engTextField3.getText().equals(EMPTY)
				& dirButton3.getText().equals(QUESTION))
			return false;
		if (!engTextField4.getText().equals(EMPTY)
				& dirButton4.getText().equals(QUESTION))
			return false;
		if (!engTextField5.getText().equals(EMPTY)
				& dirButton5.getText().equals(QUESTION))
			return false;
		if (!engTextField6.getText().equals(EMPTY)
				& dirButton6.getText().equals(QUESTION))
			return false;
		return true;
	}

	// change button operation during load consist from roster
	private void changeButtons(boolean rosterDisplay) {
		if (rosterDisplay) {
			clearButton.setText(CANCEL);
			clearButton.setToolTipText(ToolTipCancel);
			saveButton.setText(LOAD);
			saveButton.setToolTipText(ToolTipLoad);
		} else {
			clearButton.setText(CLEAR);
			clearButton.setToolTipText(ToolTipClear);
			saveButton.setText(SAVE);
			saveButton.setToolTipText(ToolTipSave);
		}
		clearButton.setEnabled(true);

		// toggle (on if we're loading a consist from roster)
		deleteButton.setEnabled(rosterDisplay);

		// toggle (off if we're loading a consist from roster)
		previousButton.setEnabled(!rosterDisplay);
		nextButton.setEnabled(!rosterDisplay);
		getButton.setEnabled(!rosterDisplay);
		backUpButton.setEnabled(!rosterDisplay);
		restoreButton.setEnabled(!rosterDisplay);
		saveButton.setEnabled(!rosterDisplay);

		cmdButton1.setVisible(!rosterDisplay);
		cmdButton2.setVisible(!rosterDisplay);
		cmdButton3.setVisible(!rosterDisplay);
		cmdButton4.setVisible(!rosterDisplay);
		cmdButton5.setVisible(!rosterDisplay);
		cmdButton6.setVisible(!rosterDisplay);
	}

	/**
	 * Kills consist using lead engine address
	 */
	private void killConsist() {
		int locoAddr = validLocoAdr(engTextField1.getText());
		// special case where lead or rear loco was being replaced
		if (locoAddr < 0)
			return;
		if (adrButton1.getText() == LONG)
			locoAddr += 0xC000;
		sendNceBinaryCommand(locoAddr, NceBinaryCommand.LOCO_CMD_KILL_CONSIST,
				(byte) 0);
	}

	private void sendNceBinaryCommand(int locoAddr, byte nceLocoCmd,
			byte consistNumber) {
		byte[] bl = NceBinaryCommand.nceLocoCmd(locoAddr, nceLocoCmd,
				consistNumber);
		sendNceMessage(bl, REPLY_1);
	}

	public void message(NceMessage m) {
	} // ignore replies

	// NCE CS response from add, delete, save, get, next, previous, etc
	// A single byte response is expected from commands
	// A 16 byte response is expected when loading a consist or searching
	public void reply(NceReply r) {
		if (waiting <= 0) {
			log.error("unexpected response");
			return;
		}
		waiting--;

		if (r.getNumDataElements() != replyLen) {
			consistReply.setText(ERROR);
			log.error("reply length error, expecting: " + replyLen + " got: "
					+ r.getNumDataElements());
			return;
		}

		// response to commands
		if (replyLen == REPLY_1) {
			// Looking for proper response
			int recChar = r.getElement(0);
			if (recChar == '!') {
				if (engineSearch && waiting == 0) {
					readConsistMemory(consistNumVerify, LEAD);
					consistReply.setText(VERIFY);
					return;
				}
				if (refresh && waiting == 0) {
					refresh = false;
					// update panel
					readConsistMemory(consistNum, LEAD);
					return;
				}
				consistReply.setText(OKAY);
			} else {
				consistReply.setText(ERROR);
			}
			return;
		}

		// Consist memory read
		if (replyLen == REPLY_16) {
			// are we verifying that engine isn't already part of a consist?
			if (engineSearch) {
				// search the 16 bytes for a loco match
				for (int i = 0; i < 16;) {
					int rC = r.getElement(i++);
					rC = (rC << 8) & 0xFF00;
					int rC_l = r.getElement(i++);
					rC_l = rC_l & 0xFF;
					rC = rC + rC_l;
					// does it match any of the engines?
					if (rC == engineVerify) {
						// ignore matching the consist that we're adding the
						// loco
						if (consistNumVerify != consistNum) {
							engineSearch = false; // quit the search
							consistReply.setText(ERROR);
							engNum = rC & 0x3FFF;
							queueError (ERROR_MID_ENG_IN_USE);
							return;
						}
					}
					consistNumVerify++;
				}
				if (consistNumVerify > CONSIST_MAX) {
					if (engineNum == LEAD) {
						// now verify the rear loco consist
						engineNum = REAR;
						consistNumVerify = 0;
					} else {
						// verify complete, loco address is unique
						engineSearch = false;
						consistReply.setText(OKAY);
						// load mid engine to this consist
						if (midEngineDir > 0) {
							if (midEngineDir == MID_FWD) {
								sendNceBinaryCommand(engineVerify,
										NceBinaryCommand.LOCO_CMD_FWD_CONSIST_MID,
										(byte) consistNum);
							} else {
								sendNceBinaryCommand(engineVerify,
										NceBinaryCommand.LOCO_CMD_REV_CONSIST_MID,
										(byte) consistNum);
							}
						}
						// must of been verifying lead or rear loco
						else if (refresh && waiting == 0) {
							refresh = false;
							// update panel
							readConsistMemory(consistNum, LEAD);
						}
						return;
					}
				}
				// continue verify
				readConsistMemory(consistNumVerify, engineNum);
				return;
			}
			// are we searching?
			if (consistSearchNext) {
				for (int i = 15; i > 0;) {
					int rC_l = r.getElement(i--);
					rC_l = rC_l & 0xFF;
					int rC = r.getElement(i--);
					rC = (rC << 8) & 0xFF00;
					rC = rC + rC_l;

					if (checkBoxEmpty.isSelected() || emptyConsistSearch) {
						if (rC == 0 && consistCount > 0) {
							// found an empty consist!
							consistSearchNext = false;
							if (emptyConsistSearch) {
								// no update, roster was loaded in panel
								emptyConsistSearch = false;
								consistReply.setText(OKAY);
								saveButton.setEnabled(canLoad());
								return;
							} else {
								// update the panel
								readConsistMemory(consistNum, LEAD);
								return;
							}
						}
					} else {
						if (rC != 0 && consistCount > 0) {
							consistSearchNext = false;
							readConsistMemory(consistNum, LEAD);
							return;
						}
					}
					if (++consistCount > CONSIST_MAX) {
						consistSearchNext = false;
						consistReply.setText(NONE);
						if (emptyConsistSearch) {
							emptyConsistSearch = false;;
							queueError(ERROR_NO_EMPTY_CONSIST);
						}
						return;		// don't update panel
					}
					consistNum--;
					if (consistNum < CONSIST_MIN)
						consistNum = CONSIST_MAX;
					consistTextField.setText(Integer.toString(consistNum));

					if (consistNum == CONSIST_MAX) {
						break;
					}
				}
				// continue searching
				readConsistMemory(consistNum - 7, LEAD);
				return;
			}
			// are we searching?
			if (consistSearchPrevious) {
				for (int i = 0; i < 16;) {
					int rC = r.getElement(i++);
					rC = (rC << 8) & 0xFF00;
					int rC_l = r.getElement(i++);
					rC_l = rC_l & 0xFF;
					rC = rC + rC_l;

					if (checkBoxEmpty.isSelected()) {
						if (rC == 0 && consistCount > 0) {
							consistSearchPrevious = false;
							break;
						}
					} else {
						if (rC != 0 && consistCount > 0) {
							consistSearchPrevious = false;
							break;
						}
					}
					if (++consistCount > CONSIST_MAX) {
						consistReply.setText(NONE);
						consistSearchPrevious = false;
						return;		// don't update the panel
					}
					consistNum++;
					if (consistNum > CONSIST_MAX)
						consistNum = CONSIST_MIN;
					consistTextField.setText(Integer.toString(consistNum));
					// have we wraped? if yes, need to read the next group
					if (consistNum == CONSIST_MIN)
						break;
				}
				readConsistMemory(consistNum, LEAD);
				return;
			}

			// load lead engine
			if (engineNum == LEAD) {
				boolean eng1exists = updateLocoFields(r, 0, locoRosterBox1,
						engTextField1, adrButton1, dirButton1, cmdButton1);
				clearButton.setEnabled(eng1exists);

				// load rear engine
			} else if (engineNum == REAR) {
				updateLocoFields(r, 0, locoRosterBox2, engTextField2,
						adrButton2, dirButton2, cmdButton2);

				// load mid engines
			} else {
				updateLocoFields(r, 0, locoRosterBox3, engTextField3,
						adrButton3, dirButton3, cmdButton3);
				updateLocoFields(r, 2, locoRosterBox4, engTextField4,
						adrButton4, dirButton4, cmdButton4);
				updateLocoFields(r, 4, locoRosterBox5, engTextField5,
						adrButton5, dirButton5, cmdButton5);
				updateLocoFields(r, 6, locoRosterBox6, engTextField6,
						adrButton6, dirButton6, cmdButton6);
				consistReply.setText(OKAY);
				checkForRosterMatch();
				saveButton.setEnabled(canLoad());
			}
			// read the next consist engine number
			if (engineNum == LEAD || engineNum == REAR) {
				engineNum++;
				readConsistMemory(consistNum, engineNum);
			}
		}
	}
	
	private void checkForRosterMatch(){
		if (!newConsist && !verifyRosterMatch)
			return;
		if (!verifyRosterMatch)
			cre = ConsistRoster.instance().entryFromTitle(engTextField1.getText());
		if (cre == null)
			return;
		if (consistRosterMatch(cre)){
			// match!  Only load the elements needed
			textConRoadName.setText(cre.getRoadName());
			textConRoadNumber.setText(cre.getRoadNumber());	
			dirButton1.setText(directionWord(cre.getEng1Direction()));
			dirButton2.setText(directionWord(cre.getEng2Direction()));
			dirButton3.setText(directionWord(cre.getEng3Direction()));
			dirButton4.setText(directionWord(cre.getEng4Direction()));
			dirButton5.setText(directionWord(cre.getEng5Direction()));
			dirButton6.setText(directionWord(cre.getEng6Direction()));
			if (verifyRosterMatch)
				queueError(WARN_CONSIST_ALREADY_LOADED);
			verifyRosterMatch = false;
		} else {
			// no match
			textConRoadName.setText(EMPTY);
			textConRoadNumber.setText(EMPTY);	
			if (verifyRosterMatch){
				verifyRosterMatch = false;
				queueError(ERROR_CONSIST_DOESNT_MATCH);
			}
		}
	}

	// update loco fields, returns false if loco address is null
	private boolean updateLocoFields(NceReply r, int index,
			JComboBox locoRosterBox, JTextField engTextField,
			JButton adrButton, JButton dirButton, JButton cmdButton) {
		// index = 0 for lead and rear locos, 0,2,4,6 for mid
		String locoAddr = getLocoAddr(r, index);
		boolean locoType = getLocoAddressType(r, index); // Long or short address?
		String locoDirection = getLocoDirection(dirButton);

		if (locoAddr == EMPTY
				|| locoAddr.equals(Integer.toString(LOC_ADR_REPLACE))) {
			locoRosterBox.setEnabled(true);
			locoRosterBox.setSelectedIndex(0);
			engTextField.setEnabled(true);
			cmdButton.setText(ADD);
			cmdButton.setVisible(true);
			cmdButton.setEnabled(false);
			cmdButton.setToolTipText(ToolTipAdd);
			dirButton.setText(QUESTION);
			dirButton.setEnabled(true);
			adrButton.setText(LONG);
			adrButton.setEnabled(true);

			if (locoAddr == EMPTY) {
				engTextField.setText(EMPTY);
				return false;
			} else {
				engTextField.setText(REPLACE_LOCO);
				return true;
			}
		} else {
			engTextField.setText(locoAddr);
			locoRosterBox.setEnabled(false);
			locoRosterBox.setSelectedIndex(0);
			engTextField.setEnabled(false);

			// can not delete lead or rear locos, but can replace
			if (engTextField == engTextField1 || engTextField == engTextField2) {
				cmdButton.setText(REPLACE);
				cmdButton.setEnabled(true);
				cmdButton.setToolTipText("Press to delete and replace this engine");
			} else {
				cmdButton.setText(DELETE);
				cmdButton.setEnabled(true);
				cmdButton.setToolTipText("Press to delete this engine from consist");
			}
			dirButton.setText(locoDirection);
			dirButton.setEnabled(false);
			adrButton.setText((locoType) ? LONG : SHORT);
			adrButton.setEnabled(false);
			return true;
		}
	}

	// modify loco fields
	private void modifyLocoFields(JComboBox locoRosterBox,
			JTextField engTextField, JButton adrButton, JButton dirButton,
			JButton cmdButton) {
		if (validLocoAdr(engTextField.getText()) < 0)
			return;
		if (engTextField.getText().equals(EMPTY)) {
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter loco address before pressing add", "NCE Consist",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// set reflesh flag to update panel
		refresh = true;
		byte cN = (byte) Integer.parseInt(consistTextField.getText());

		int locoAddr = Integer.parseInt(engTextField.getText());
		if (locoAddr >= 128)
			locoAddr += 0xC000;
		else if (adrButton.getText() == LONG)
			locoAddr += 0xC000;

		if (cmdButton.getText() == DELETE) {
			sendNceBinaryCommand(locoAddr,
					NceBinaryCommand.LOCO_CMD_DELETE_LOCO_CONSIST, (byte) 0);

		} else if (cmdButton.getText() == REPLACE) {

			// Kill refresh flag, no update when replacing loco
			refresh = false;

			// allow user to add loco to lead or rear consist

			locoRosterBox.setEnabled(true);
			engTextField.setText(EMPTY);
			engTextField.setEnabled(true);
			adrButton.setText(LONG);
			adrButton.setEnabled(true);
			dirButton.setText(QUESTION);
			dirButton.setEnabled(true);
			cmdButton.setText(ADD);
			cmdButton.setToolTipText(ToolTipAdd);

			// now update CS memory in case user doesn't use the Add button
			// this will also allow us to delete the loco from the layout
			if (engTextField == engTextField1) {
				// replace lead loco
				sendNceBinaryCommand(LOC_ADR_REPLACE,
						NceBinaryCommand.LOCO_CMD_FWD_CONSIST_LEAD, cN);
				// no lead loco so we can't kill the consist
				clearButton.setEnabled(false);
			} else {
				// replace rear loco
				sendNceBinaryCommand(LOC_ADR_REPLACE,
						NceBinaryCommand.LOCO_CMD_FWD_CONSIST_REAR, cN);
			}
			// now delete lead or rear loco from layout
			sendNceBinaryCommand(locoAddr,
					NceBinaryCommand.LOCO_CMD_DELETE_LOCO_CONSIST, (byte) 0);
		} else {
			// ADD button has been pressed
			if (dirButton.getText() == QUESTION) {
				JOptionPane.showMessageDialog(NceConsistEditFrame.this,
						"Set loco direction before adding to consist",
						"NCE Consist", JOptionPane.ERROR_MESSAGE);

				// kill refresh flag, no update if Add button is enabled
				// and loco direction isn't known (lead, rear, replacement)
				refresh = false;
				return;
			}

			// delete loco from any existing consists
			sendNceBinaryCommand(locoAddr,
					NceBinaryCommand.LOCO_CMD_DELETE_LOCO_CONSIST, (byte) 0);

			// check to see if loco is already a lead or rear in another consist
			verifyLocoAddr(locoAddr);

			// now we need to determine if lead, rear, or mid loco

			// lead loco?
			if (engTextField == engTextField1) {

				if (dirButton.getText() == FWD) {
					sendNceBinaryCommand(locoAddr,
							NceBinaryCommand.LOCO_CMD_FWD_CONSIST_LEAD, cN);
				}
				if (dirButton.getText() == REV) {
					sendNceBinaryCommand(locoAddr,
							NceBinaryCommand.LOCO_CMD_REV_CONSIST_LEAD, cN);
				}

				// rear loco?
			} else if (engTextField == engTextField2) {

				if (dirButton.getText() == FWD) {
					sendNceBinaryCommand(locoAddr,
							NceBinaryCommand.LOCO_CMD_FWD_CONSIST_REAR, cN);
				}
				if (dirButton.getText() == REV) {
					sendNceBinaryCommand(locoAddr,
							NceBinaryCommand.LOCO_CMD_REV_CONSIST_REAR, cN);
				}

				// must be mid loco
			} else {
				// wait for verify to complete before updating mid loco
				if (engineSearch) {
					if (dirButton.getText() == FWD)
						midEngineDir = MID_FWD;
					else
						midEngineDir = MID_REV;
					// no verify, just load and go!
				} else {

					if (dirButton.getText() == FWD) {
						sendNceBinaryCommand(locoAddr,
								NceBinaryCommand.LOCO_CMD_FWD_CONSIST_MID, cN);
					}
					if (dirButton.getText() == REV) {
						sendNceBinaryCommand(locoAddr,
								NceBinaryCommand.LOCO_CMD_REV_CONSIST_MID, cN);
					}
				}
			}
		}
	}

	private void loadOneLine(JComboBox locoRosterBox, JTextField engTextField,
			JButton adrButton, JButton dirButton, JButton cmdButton) {
		if (engTextField.getText().equals(EMPTY))
			return;
		if (validLocoAdr(engTextField.getText()) < 0)
			return;

		byte[] bl;
		byte cN = (byte) Integer.parseInt(consistTextField.getText());
		int locoAddr = Integer.parseInt(engTextField.getText());
		if (locoAddr >= 128)
			locoAddr += 0xC000;
		else if (adrButton.getText() == LONG)
			locoAddr += 0xC000;

		// ADD engine to consist
		if (dirButton.getText() == QUESTION) {
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Set loco direction before loading consist", "NCE Consist",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// delete loco from any existing consists
		sendNceBinaryCommand(locoAddr,
				NceBinaryCommand.LOCO_CMD_DELETE_LOCO_CONSIST, (byte) 0);
		// now we need to determine if lead, rear, or mid loco
		// lead loco?
		if (engTextField == engTextField1) {
			// kill the consist first to clear NCE CS memory
			sendNceBinaryCommand(locoAddr,
					NceBinaryCommand.LOCO_CMD_FWD_CONSIST_LEAD, cN);
			sendNceBinaryCommand(locoAddr, NceBinaryCommand.LOCO_CMD_KILL_CONSIST,
				(byte) 0);
			// now load
			if (dirButton.getText() == FWD) {
				sendNceBinaryCommand(locoAddr,
						NceBinaryCommand.LOCO_CMD_FWD_CONSIST_LEAD, cN);
			}
			if (dirButton.getText() == REV) {
				sendNceBinaryCommand(locoAddr,
						NceBinaryCommand.LOCO_CMD_REV_CONSIST_LEAD, cN);
			}
			// rear loco?
		} else if (engTextField == engTextField2) {

			if (dirButton.getText() == FWD) {
				sendNceBinaryCommand(locoAddr,
						NceBinaryCommand.LOCO_CMD_FWD_CONSIST_REAR, cN);
			}
			if (dirButton.getText() == REV) {
				sendNceBinaryCommand(locoAddr,
						NceBinaryCommand.LOCO_CMD_REV_CONSIST_REAR, cN);
			}
			// must be mid loco
		} else {
			if (dirButton.getText() == FWD) {
				sendNceBinaryCommand(locoAddr,
						NceBinaryCommand.LOCO_CMD_FWD_CONSIST_MID, cN);
			}
			if (dirButton.getText() == REV) {
				sendNceBinaryCommand(locoAddr,
						NceBinaryCommand.LOCO_CMD_REV_CONSIST_MID, cN);
			}
		}
	}

	private void sendNceMessage(byte[] b, int replyLength) {
		NceMessage m = NceMessage.createBinaryMessage(b, replyLength);
		int testcount = 0;
		waiting++;
		replyLen = replyLength; // Expect n byte response
		NceTrafficController.instance().sendNceMessage(m, this);
	}

	// get loco address type, returns true if long
	private boolean getLocoAddressType(NceReply r, int i) {
		int rC = r.getElement(i);
		rC = rC & 0xC0; // long address if 2 msb are set
		if (rC == 0xC0) {
			return true;
		} else {
			return false;
		}
	}

	private String getLocoAddr(NceReply r, int i) {
		int rC = r.getElement(i++);
		rC = (rC << 8) & 0x3F00;
		int rC_l = r.getElement(i);
		rC_l = rC_l & 0xFF;
		rC = rC + rC_l;
		String locoAddr = EMPTY;
		if (rC != 0) {
			locoAddr = Integer.toString(rC);
		}
		return locoAddr;
	}

	private String getLocoDirection(JButton dirButton) {
		if (newConsist)
			return QUESTION;
		else
			return dirButton.getText();
	}

	// check command station memory for lead or rear loco match
	private void verifyLocoAddr(int locoAddr) {
		midEngineDir = MID_NONE;
		if (checkBoxVerify.isSelected()) {
			engineVerify = locoAddr;
			engineSearch = true;
			consistNumVerify = 0;
		}
	}

	private void addEngRow(JComponent col1, JComponent col2, JComponent col3,
			JComponent col4, JComponent col5, JComponent col6, int row) {
		addItem(col1, 0, row);
		addItem(col2, 1, row);
		addItem(col3, 2, row);
		addItem(col4, 3, row);
		addItem(col5, 4, row);
		addItem(col6, 5, row);
	}

	private void addItem(JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		getContentPane().add(c, gc);
	}

	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}

	private void addCheckBoxAction(JCheckBox cb) {
		cb.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionPerformed(e);
			}
		});
	}

	// initialize engine fields
	private void initEngFields() {
		initEngRow(1, "Lead", textEng1, engTextField1, locoRosterBox1,
				adrButton1, dirButton1, cmdButton1);
		initEngRow(2, "Rear", textEng2, engTextField2, locoRosterBox2,
				adrButton2, dirButton2, cmdButton2);
		initEngRow(3, "Mid 1", textEng3, engTextField3, locoRosterBox3,
				adrButton3, dirButton3, cmdButton3);
		initEngRow(4, "Mid 2", textEng4, engTextField4, locoRosterBox4,
				adrButton4, dirButton4, cmdButton4);
		initEngRow(5, "Mid 3", textEng5, engTextField5, locoRosterBox5,
				adrButton5, dirButton5, cmdButton5);
		initEngRow(6, "Mid 4", textEng6, engTextField6, locoRosterBox6,
				adrButton6, dirButton6, cmdButton6);
	}

	private void initEngRow(int row, String s, JLabel textEng,
			JTextField engTextField, JComboBox locoRosterBox,
			JButton adrButton, JButton dirButton, JButton cmdButton) {

		textEng.setText(s);
		textEng.setVisible(true);

		adrButton.setText(LONG);
		adrButton.setVisible(true);
		adrButton.setEnabled(false);
		adrButton.setToolTipText("Press to change address type");
		adrButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionAdrPerformed(e);
			}
		});

		locoRosterBox.insertItemAt(EMPTY, 0);
		locoRosterBox.setSelectedIndex(0);
		locoRosterBox.setVisible(true);
		locoRosterBox.setEnabled(false);
		locoRosterBox.setToolTipText("Select engine from roster");
		locoRosterBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				locoSelected(e);
			}
		});

		dirButton.setText(QUESTION);
		dirButton.setVisible(true);
		dirButton.setEnabled(false);
		dirButton.setToolTipText("Press to change engine direction");
		dirButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionDirPerformed(e);
			}
		});

		cmdButton.setText(ADD);
		cmdButton.setVisible(true);
		cmdButton.setEnabled(false);
		cmdButton.setToolTipText(ToolTipAdd);
		cmdButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionCmdPerformed(e);
			}
		});

		engTextField.setText(EMPTY);
		engTextField.setEnabled(false);
		engTextField.setToolTipText("Enter engine address");
		engTextField.setMaximumSize(new Dimension(
				engTextField.getMaximumSize().width, engTextField
						.getPreferredSize().height));
	}

	ActionListener consistRosterListener;

	private void initConsistRoster(JComboBox conRosterBox) {
		conRosterBox.insertItemAt(EMPTY, 0);
		conRosterBox.setSelectedIndex(0);
		conRosterBox.setVisible(true);
		conRosterBox.setEnabled(false);
		conRosterBox.setToolTipText("Select consist from roster");
		conRosterBox.addActionListener(consistRosterListener = new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						consistRosterSelected(e);
					}
				});
	}
	
	private static final int ERROR_MID_ENG_IN_USE = 1;
	private static final int ERROR_NO_EMPTY_CONSIST = 2;
	private static final int ERROR_CONSIST_DOESNT_MATCH = 3;
	private static final int WARN_CONSIST_ALREADY_LOADED = 4;
	
	private int errorCode = 0;
	
	private void queueError (int errorCode){
		log.debug ("queue warning/error message: " + errorCode);
		if (this.errorCode != 0)
			log.debug ("multiple errors reported " + this.errorCode);
		this.errorCode = errorCode;
		// Bad to stop receive thread with JOptionPane error message
		// so start up a new thread to report error
		Thread errorThread = new Thread(new Runnable() {
			public void run() {
				reportError();
			}
		});
		errorThread.setName("Report Error");
		errorThread.start();
	}

	public void reportError() {
		switch (errorCode){

		case ERROR_MID_ENG_IN_USE: 
			JOptionPane.showMessageDialog(NceConsistEditFrame.this, "Loco address "
					+ engNum + " is part of consist " + consistNumVerify,
					"NCE Consist", JOptionPane.ERROR_MESSAGE);
			break;

		case ERROR_NO_EMPTY_CONSIST:
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
							"There are no empty consists, please clear a consist and try again",
							"NCE Consist", JOptionPane.ERROR_MESSAGE);
			break;
			
		case ERROR_CONSIST_DOESNT_MATCH:
			if (JOptionPane.showConfirmDialog(null,
							"The roster consist does not match the consist in NCE memory, continue loading?"
					+ "\n" +
					"\n Lead Engine Address: " + cre.getEng1DccAddress() +
					"\n Rear Engine Address: " + cre.getEng2DccAddress() +
					"\n Mid1 Engine Address: " + cre.getEng3DccAddress() +
					"\n Mid2 Engine Address: " + cre.getEng4DccAddress() +
					"\n Mid3 Engine Address: " + cre.getEng5DccAddress() +
					"\n Mid4 Engine Address: " + cre.getEng6DccAddress(),
							"Continue loading consist from roster?",
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				break;
			}
			changeButtons (true);
			loadFullRoster (cre);
			saveButton.setEnabled(canLoad());
			break;
		case WARN_CONSIST_ALREADY_LOADED:
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Consist has already been loaded!",
					"NCE Consist", JOptionPane.WARNING_MESSAGE);
			break;
		}
		errorCode = 0;
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(NceConsistEditFrame.class.getName());
}
