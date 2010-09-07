// NceConsistEditFrame.java

package jmri.jmrix.nce.consist;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jmri.DccLocoAddress;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Frame for user edit of NCE Consists
 * 
 * NCE Consists are stored in Command Station (CS) memory starting at address
 * xF500 and ending xFAFF. NCE supports up to 127 consists, numbered 1 to 127.
 * They track the lead loco, rear loco, and four mid locos in the consist
 * file. NCE cabs start at consist 127 when building and reviewing consists, so
 * we also start with 127. Consist lead locos are stored in memory locations
 * xF500 through xF5FF. Consist rear locos are stored in memory locations
 * xF600 through xF6FF. Mid consist locos (four max) are stored in memory
 * locations xF700 through xFAFF. If a long address is in use, bits 6 and 7 of
 * the high byte are set. Example: Long address 3 = 0xc0 0x03 Short address 3 =
 * 0x00 0x03
 * 
 * NCE file format:
 * 
 * :F500 (con 0 lead loco) (con 1 lead loco) ....... (con 7 lead loco)
 * :F510 (con 8 lead loco) ........ (con 15 lead loco) . . :F5F0 (con 120
 * lead loco) ..... (con 127 lead loco)
 * 
 * :F600 (con 0 rear loco) (con 1 rear loco) ....... (con 7 rear loco) . .
 * :F6F0 (con 120 rear loco) ..... (con 127 rear loco)
 * 
 * :F700 (con 0 mid loco1) (con 0 mid loco2) (con 0 mid loco3) (con 0 mid loco4) . .
 * :FAF0 (con 126 mid loco1) .. (con 126 mid loco4)(con 127 mid loco1) .. (con 127
 * mid loco4) :0000
 * 
 * @author Dan Boudreau Copyright (C) 2007 2008
 * @version $Revision: 1.35 $
 */

public class NceConsistEditFrame extends jmri.util.JmriJFrame implements
		jmri.jmrix.nce.NceListener {

	public static final int CS_CONSIST_MEM = 0xF500; 	// start of NCE CS Consist memory
	private static final int CS_CON_MEM_REAR = 0xF600; 	// start of rear consist locos
	private static final int CS_CON_MEM_MID = 0xF700; 	// start of mid consist locos
	private static final int CONSIST_MIN = 1; 			// NCE doesn't use consist 0
	private static final int CONSIST_MAX = 127;
	private static final int LOC_ADR_MIN = 1; 			// loco address range
	private static final int LOC_ADR_MAX = 9999;
	private static final int LOC_ADR_REPLACE = 0x3FFF; 	// dummy loco address 
	
	private int consistNum = 0; 				// consist being worked
	private boolean newConsist = true; 			// new consist is displayed
	
	private int locoNum = LEAD; 				// which loco, 0 = lead, 1 = rear, 2 = mid
	private static final int LEAD = 0;
	private static final int REAR = 1;
	private static final int MID = 2;
	
	// Verify that loco isn't already a lead or rear loco
	private int consistNumVerify; 				// which consist number we're checking
	private int [] locoVerifyList = new int[6];	// list of locos to verify
	private int verifyType; 					// type of verification
	private static final int VERIFY_DONE = 0;
	private static final int VERIFY_LEAD_REAR = 1; 	// lead or rear loco  
	private static final int VERIFY_MID_FWD = 2; 	// mid loco foward 
	private static final int VERIFY_MID_REV = 4; 	// mid loco reverse 
	private static final int VERIFY_ALL = 8;		// verify all locos

	private static final String WAIT = "waiting"; // NCE consist editor states
	private static final String OKAY = "okay";
	private static final String SEARCH = "searching";
	private static final String ERROR = "error";
	private static final String VERIFY = "verifying";
	private static final String UNKNOWN = "unknown";
	private static final String NONE = "none";
	private static final String MODIFIED = "Modified!";

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
	
	private static final String EMPTY = "";				// loco field values
	private static final String REPLACE_LOCO = "NONE";
	
	private static final String CLEARED = "0 <cleared>";

	private static final String ToolTipAdd = "Press to add loco to consist";
	private static final String ToolTipClear = "Press to kill consist";
	private static final String ToolTipCancel = "Press to cancel consist load from roster";
	private static final String ToolTipSave = "Press to save consist to roster";
	private static final String ToolTipLoad = "Press to load consist into NCE CS and program locos";
	private static final String ToolTipConsist = "Enter consist 1 to 127";
	private static final String ToolTipThrottle = "Press to get a throttle for your consist";
	
	// the 16 byte reply states
	private boolean consistSearchNext = false; 		// next search
	private boolean consistSearchPrevious = false; 	// previous search
	private boolean locoSearch = false; 			// when true searching for lead or rear loco in consist
	
	private boolean emptyConsistSearch = false; 	// when true searching for an empty consist
	private boolean verifyRosterMatch = false; 		// when true verify that roster matches consist in NCE CS
	
	private int emptyConsistSearchStart = CONSIST_MAX;	// where to begin search for empty consist
	
	private int consistCount = 0; 					// search count not to exceed CONSIST_MAX

	private boolean refresh = false; 				// when true, refresh loco info from CS

	// member declarations
	JLabel textConsist = new JLabel();
	JLabel textStatus = new JLabel();
	JLabel consistStatus = new JLabel();

	// major buttons
	JButton previousButton = new JButton();
	JButton nextButton = new JButton();
	JButton getButton = new JButton();
	JButton throttleButton = new JButton();
	JButton clearCancelButton = new JButton();
	JButton saveLoadButton = new JButton();
	JButton deleteButton = new JButton();
	JButton backUpButton = new JButton();
	JButton restoreButton = new JButton();

	// check boxes
	JCheckBox checkBoxEmpty = new JCheckBox();
	JCheckBox checkBoxVerify = new JCheckBox();
	JCheckBox checkBoxConsist = new JCheckBox();

	// consist text field
	JTextField consistTextField = new JTextField(4);

	// labels
	JLabel textLocomotive = new JLabel();
	JLabel textRoster = new JLabel();
	JLabel textAddress = new JLabel();
	JLabel textAddrType = new JLabel();
	JLabel textDirection = new JLabel();

	JLabel textConRoster = new JLabel();
	JLabel textConRoadName = new JLabel();
	JLabel textConRoadNumber = new JLabel();
	JLabel textConModel = new JLabel();
	
	JComboBox conRosterBox = NceConsistRoster.instance().fullRosterComboBox();

	// for padding out panel
	JLabel space1 = new JLabel();
	JLabel space2 = new JLabel();
	JLabel space3 = new JLabel();

	// lead loco
	JLabel textLoco1 = new JLabel();
	JTextField locoTextField1 = new JTextField(4);
	JComboBox locoRosterBox1 = Roster.instance().fullRosterComboBox();
	JButton adrButton1 = new JButton();
	JButton cmdButton1 = new JButton();
	JButton dirButton1 = new JButton();

	// rear loco
	JLabel textLoco2 = new JLabel();
	JTextField locoTextField2 = new JTextField(4);
	JComboBox locoRosterBox2 = Roster.instance().fullRosterComboBox();
	JButton adrButton2 = new JButton();
	JButton cmdButton2 = new JButton();
	JButton dirButton2 = new JButton();

	// mid loco
	JLabel textLoco3 = new JLabel();
	JTextField locoTextField3 = new JTextField(4);
	JComboBox locoRosterBox3 = Roster.instance().fullRosterComboBox();
	JButton adrButton3 = new JButton();
	JButton cmdButton3 = new JButton();
	JButton dirButton3 = new JButton();

	// mid loco
	JLabel textLoco4 = new JLabel();
	JTextField locoTextField4 = new JTextField(4);
	JComboBox locoRosterBox4 = Roster.instance().fullRosterComboBox();
	JButton adrButton4 = new JButton();
	JButton cmdButton4 = new JButton();
	JButton dirButton4 = new JButton();

	// mid loco
	JLabel textLoco5 = new JLabel();
	JTextField locoTextField5 = new JTextField(4);
	JComboBox locoRosterBox5 = Roster.instance().fullRosterComboBox();
	JButton adrButton5 = new JButton();
	JButton cmdButton5 = new JButton();
	JButton dirButton5 = new JButton();

	// mid loco
	JLabel textLoco6 = new JLabel();
	JTextField locoTextField6 = new JTextField(4);
	JComboBox locoRosterBox6 = Roster.instance().fullRosterComboBox();
	JButton adrButton6 = new JButton();
	JButton cmdButton6 = new JButton();
	JButton dirButton6 = new JButton();

	public NceConsistEditFrame() {
		super();
	}

	public void initComponents() throws Exception {
		// the following code sets the frame's initial state

		textConsist.setText("Consist");
		textConsist.setVisible(true);

		textStatus.setText("Status:");
		textStatus.setVisible(true);

		consistStatus.setText(UNKNOWN);
		consistStatus.setVisible(true);

		previousButton.setText("Previous");
		previousButton.setVisible(true);
		previousButton.setToolTipText("Search for consist incrementing");

		nextButton.setText("   Next   ");
		nextButton.setVisible(true);
		nextButton.setToolTipText("Search for consist decrementing");

		getButton.setText("  Get  ");
		getButton.setVisible(true);
		getButton.setToolTipText("Read consist from NCE CS");

		consistTextField.setText(Integer.toString(CONSIST_MAX));
		consistTextField.setToolTipText(ToolTipConsist);
		consistTextField.setMaximumSize(new Dimension(consistTextField
				.getMaximumSize().width,
				consistTextField.getPreferredSize().height));

		textLocomotive.setText("Locomotive");
		textLocomotive.setVisible(true);
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
		textConModel.setText(EMPTY);
		textConModel.setVisible(true);
		
		throttleButton.setText("Throttle");
		throttleButton.setVisible(true);
		throttleButton.setEnabled(true);
		throttleButton.setToolTipText(ToolTipThrottle);

		clearCancelButton.setText(CLEAR);
		clearCancelButton.setVisible(true);
		clearCancelButton.setEnabled(false);
		clearCancelButton.setToolTipText(ToolTipClear);

		saveLoadButton.setText(SAVE);
		saveLoadButton.setVisible(false);
		saveLoadButton.setEnabled(false);
		saveLoadButton.setToolTipText(ToolTipSave);

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

		checkBoxEmpty.setText("Empty consist");
		checkBoxEmpty.setVisible(true);
		checkBoxEmpty.setToolTipText("Check to search for empty consists");

		checkBoxVerify.setText("Verify loco     ");
		checkBoxVerify.setVisible(true);
		checkBoxVerify.setSelected(true);
		checkBoxVerify
				.setToolTipText("Verify that add loco isn't already a consist lead or rear loco");

		checkBoxConsist.setText("Consist roster");
		checkBoxConsist.setVisible(true);
		checkBoxConsist.setSelected(true);
		checkBoxConsist.setToolTipText("Check to build a consist roster");

		space1.setText("            ");
		space1.setVisible(true);
		space2.setText(" ");
		space2.setVisible(true);
		space3.setText(" ");
		space3.setVisible(true);

		initLocoFields();

		setTitle("Edit NCE Consist");
		getContentPane().setLayout(new GridBagLayout());

		// Layout the panel by rows
		// row 0
		addItem(textConsist, 2, 0);
		// row 1
		addItem(previousButton, 1, 1);
		addItem(consistTextField, 2, 1);
		addItem(nextButton, 3, 1);
		addItem(checkBoxEmpty, 5, 1);
		// row 2
		addItem(textStatus, 0, 2);
		addItem(consistStatus, 1, 2);
		addItem(getButton, 2, 2);
		addItem(checkBoxVerify, 5, 2);
		// row 3
		addItem(space3, 1, 3);
		// row 4
		addItem(textConRoster, 1, 4);
		// row 5
		addItem(conRosterBox, 1, 5);
		addItem (textConRoadName, 2, 5);
		addItem (textConRoadNumber, 3, 5);
		addItem (textConModel, 4, 5);
		addItem(checkBoxConsist, 5, 5);
		initConsistRoster(conRosterBox);

		// row 6 padding for looks
		addItem(space1, 1, 6);
		// row 7 labels
		addItem(textLocomotive, 0, 7);
		addItem(textRoster, 1, 7);
		addItem(textAddress, 2, 7);
		addItem(textAddrType, 3, 7);
		addItem(textDirection, 4, 7);

		// row 8 Lead Locomotive
		addLocoRow(textLoco1, locoRosterBox1, locoTextField1, adrButton1,
				dirButton1, cmdButton1, 8);
		// row 9 Rear Locomotive
		addLocoRow(textLoco2, locoRosterBox2, locoTextField2, adrButton2,
				dirButton2, cmdButton2, 9);
		// row 10 Mid Locomotive
		addLocoRow(textLoco3, locoRosterBox3, locoTextField3, adrButton3,
				dirButton3, cmdButton3, 10);
		// row 11 Mid Locomotive
		addLocoRow(textLoco4, locoRosterBox4, locoTextField4, adrButton4,
				dirButton4, cmdButton4, 11);
		// row 12 Mid Locomotive
		addLocoRow(textLoco5, locoRosterBox5, locoTextField5, adrButton5,
				dirButton5, cmdButton5, 12);
		// row 13 Mid Locomotive
		addLocoRow(textLoco6, locoRosterBox6, locoTextField6, adrButton6,
				dirButton6, cmdButton6, 13);

		// row 15 padding for looks
		addItem(space2, 2, 15);
		// row 16
		addItem(throttleButton, 0, 16);
		addItem(clearCancelButton, 1, 16);
		addItem(saveLoadButton, 2, 16);
		addItem(deleteButton, 3, 16);
		addItem(backUpButton, 4, 16);
		addItem(restoreButton, 5, 16);

		// setup buttons
		addButtonAction(previousButton);
		addButtonAction(nextButton);
		addButtonAction(getButton);
		addButtonAction(throttleButton);
		addButtonAction(clearCancelButton);
		addButtonAction(saveLoadButton);
		addButtonAction(deleteButton);
		addButtonAction(backUpButton);
		addButtonAction(restoreButton);

		// setup checkboxes
		addCheckBoxAction(checkBoxConsist);
		checkBoxConsist ();
		
		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu("Tools");
		toolMenu.add(new NceConsistRosterMenu("Roster", jmri.jmrit.roster.RosterMenu.MAINMENU, this));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrix.nce.consist.NceConsistEditFrame", true);

		// set frame size for display
		pack();
		if ( (getWidth()<450) && (getHeight()<380)) setSize(450, 380);
	}

	// Previous, Next, Get, Throttle, Clear/Cancel, Save/Load, Delete, Restore & Backup buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		// if we're searching ignore user
		if (consistSearchNext || consistSearchPrevious || locoSearch)
			return;
		// throttle button
		if (ae.getSource() == throttleButton) {
			if (!validConsist())
				return;
			int locoAddr = validLocoAdr(locoTextField1.getText());
			boolean isLong = (adrButton1.getText() == LONG);
			if (locoAddr < 0)
				return;
			consistNum = validConsist(consistTextField.getText());
			jmri.jmrit.throttle.ThrottleFrame tf=
				jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleFrame();
			if (JOptionPane.showConfirmDialog(null,
					"Send function commands to lead loco?", "NCE Consist Throttle",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				tf.getAddressPanel().setAddress(locoAddr, isLong); 	// first notify for func button
			}
			tf.getAddressPanel().setAddress(consistNum, false);	// second notify for consist address
			tf.toFront();
			return;
		}
		// clear or cancel button
		if (ae.getSource() == clearCancelButton) {
			// button can be Clear or Cancel
			if (clearCancelButton.getText().equals(CLEAR)) {
				updateRoster(CLEARED);
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
		if (ae.getSource() == saveLoadButton) {
			if (!validConsist())
				return;
			// check to see if user modified the roster
			if (canLoad()){
				consistStatus.setText(OKAY);
			} else {
				consistStatus.setText(ERROR);
				saveLoadButton.setEnabled(false);
				return;
			}
			enableAllLocoRows(false);
			if (saveLoadButton.getText().equals(LOAD)){ 
				loadShift (); // get rid of empty mids!
				updateRoster(consistTextField.getText());
				consistNum = validConsist(consistTextField.getText());
				// load right away or verify?
				if(!verifyAllLocoAddr())
					fullLoad();
			}else if (updateRoster(consistTextField.getText()))
				saveLoadButton.setEnabled(false);
			return;
		}

		// delete button
		if (ae.getSource() == deleteButton) {
			if (JOptionPane.showConfirmDialog(null,
					"Are you sure you want to delete consist "
					+ conRosterBox.getSelectedItem().toString()
					+ " from roster?", "Delete consist from roster?",
					JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
				return;
			}
			deleteRoster();
			changeButtons(false); // yes, clear delete button
			return;
		}
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

	// One of six loco command buttons, add, replace or delete
	public void buttonActionCmdPerformed(java.awt.event.ActionEvent ae) {
		// if we're searching ignore user
		if (consistSearchNext || consistSearchPrevious || locoSearch)
			return;
		if (consistChanged())
			return;
		if (ae.getSource() == cmdButton1)
			modifyLocoFields(locoRosterBox1, locoTextField1, adrButton1,
					dirButton1, cmdButton1);
		if (ae.getSource() == cmdButton2)
			modifyLocoFields(locoRosterBox2, locoTextField2, adrButton2,
					dirButton2, cmdButton2);
		if (ae.getSource() == cmdButton3)
			modifyLocoFields(locoRosterBox3, locoTextField3, adrButton3,
					dirButton3, cmdButton3);
		if (ae.getSource() == cmdButton4)
			modifyLocoFields(locoRosterBox4, locoTextField4, adrButton4,
					dirButton4, cmdButton4);
		if (ae.getSource() == cmdButton5)
			modifyLocoFields(locoRosterBox5, locoTextField5, adrButton5,
					dirButton5, cmdButton5);
		if (ae.getSource() == cmdButton6)
			modifyLocoFields(locoRosterBox6, locoTextField6, adrButton6,
					dirButton6, cmdButton6);
		if (updateRoster(consistTextField.getText()))
			saveLoadButton.setEnabled(false);
	}

	// one of six loco address type buttons
	public void buttonActionAdrPerformed(java.awt.event.ActionEvent ae) {
		// if we're searching ignore user
		if (consistSearchNext || consistSearchPrevious || locoSearch)
			return;
		if (consistChanged())
			return;
		if (ae.getSource() == adrButton1)
			toggleAdrButton(locoTextField1, adrButton1);
		if (ae.getSource() == adrButton2)
			toggleAdrButton(locoTextField2, adrButton2);
		if (ae.getSource() == adrButton3)
			toggleAdrButton(locoTextField3, adrButton3);
		if (ae.getSource() == adrButton4)
			toggleAdrButton(locoTextField4, adrButton4);
		if (ae.getSource() == adrButton5)
			toggleAdrButton(locoTextField5, adrButton5);
		if (ae.getSource() == adrButton6)
			toggleAdrButton(locoTextField6, adrButton6);
	}

	private void toggleAdrButton(JTextField locoTextField, JButton adrButton) {
		if (validLocoAdr(locoTextField.getText()) < 0)
			return;
		if (locoTextField.getText().equals(EMPTY)) {
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter loco address before changing address type",
					"NCE Consist", JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			if (adrButton.getText() == LONG) {
				if (Integer.parseInt(locoTextField.getText()) < 128) {
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
		if (consistSearchNext || consistSearchPrevious || locoSearch)
			return;
		if (consistChanged())
			return;
		if (ae.getSource() == dirButton1)
			toggleDirButton(locoTextField1, dirButton1, cmdButton1);
		if (ae.getSource() == dirButton2)
			toggleDirButton(locoTextField2, dirButton2, cmdButton2);
		if (ae.getSource() == dirButton3)
			toggleDirButton(locoTextField3, dirButton3, cmdButton3);
		if (ae.getSource() == dirButton4)
			toggleDirButton(locoTextField4, dirButton4, cmdButton4);
		if (ae.getSource() == dirButton5)
			toggleDirButton(locoTextField5, dirButton5, cmdButton5);
		if (ae.getSource() == dirButton6)
			toggleDirButton(locoTextField6, dirButton6, cmdButton6);
		saveLoadButton.setEnabled(canLoad());
	}

	private void toggleDirButton(JTextField locoTextField, JButton dirButton,
			JButton cmdButton) {
		if (validLocoAdr(locoTextField.getText()) < 0)
			return;
		if (locoTextField.getText().equals(EMPTY)) {
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

	// one of six roster select, load loco number and address length
	public void locoSelected(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == locoRosterBox1)
			rosterBoxSelect(locoRosterBox1, locoTextField1, adrButton1);
		if (ae.getSource() == locoRosterBox2)
			rosterBoxSelect(locoRosterBox2, locoTextField2, adrButton2);
		if (ae.getSource() == locoRosterBox3)
			rosterBoxSelect(locoRosterBox3, locoTextField3, adrButton3);
		if (ae.getSource() == locoRosterBox4)
			rosterBoxSelect(locoRosterBox4, locoTextField4, adrButton4);
		if (ae.getSource() == locoRosterBox5)
			rosterBoxSelect(locoRosterBox5, locoTextField5, adrButton5);
		if (ae.getSource() == locoRosterBox6)
			rosterBoxSelect(locoRosterBox6, locoTextField6, adrButton6);
	}
	// load a loco from roster
	private void rosterBoxSelect(JComboBox locoRosterBox,
			JTextField locoTextField, JButton adrButton) {
		String rosterEntryTitle = locoRosterBox.getSelectedItem().toString();
		if (rosterEntryTitle.equals(EMPTY))
			return;
		RosterEntry entry = Roster.instance().entryFromTitle(rosterEntryTitle);
		DccLocoAddress a = entry.getDccLocoAddress();
		if (a != null) {
			locoTextField.setText("" + a.getNumber());
			if (a.isLongAddress())
				adrButton.setText(LONG);
			else
				adrButton.setText(SHORT);
			// if lead loco get road number and name
			if (locoRosterBox == locoRosterBox1){
				textConRoadName.setText(entry.getRoadName());
				textConRoadNumber.setText(entry.getRoadNumber());
				textConModel.setText(entry.getModel());
			}
		}
	}

	// load a consist from roster
	public void consistRosterSelected(java.awt.event.ActionEvent ae) {
		if (consistSearchNext || consistSearchPrevious || locoSearch)
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
		if (ae.getSource() == checkBoxConsist) 
			checkBoxConsist ();
	}
	
	private void checkBoxConsist () {
		if (checkBoxConsist.isSelected()) {
			conRosterBox.setEnabled(true);
			saveLoadButton.setVisible(true);
			saveLoadButton.setEnabled(canLoad());
			deleteButton.setVisible(true);
		} else {
			conRosterBox.setEnabled(false);
			conRosterBox.removeActionListener(consistRosterListener);
			conRosterBox.setSelectedIndex(0);
			conRosterBox.addActionListener(consistRosterListener);
			saveLoadButton.setVisible(false);
			saveLoadButton.setEnabled(false);
			deleteButton.setVisible(false);
			deleteButton.setEnabled(false);
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
			consistStatus.setText(SEARCH);
		} else {
			consistStatus.setText(WAIT);
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
			consistStatus.setText(ERROR);
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					ToolTipConsist, "NCE Consist",
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
	 * Reads 16 bytes of NCE consist memory based on consist number and loco
	 * number 0=lead 1=rear 2=mid
	 */
	private void readConsistMemory(int consistNum, int eNum) {
		locoNum = eNum;
		int nceMemAddr = (consistNum * 2) + CS_CONSIST_MEM;
		if (eNum == REAR)
			nceMemAddr = (consistNum * 2) + CS_CON_MEM_REAR;
		if (eNum == MID) 
			nceMemAddr = (consistNum * 8) + CS_CON_MEM_MID;
		byte[] bl = NceBinaryCommand.accMemoryRead(nceMemAddr);
		sendNceMessage(bl, REPLY_16);
	}
	
	NceConsistRosterEntry cre;

	private void loadRosterEntry(String entry) {
		cre = NceConsistRoster.instance().entryFromTitle(entry);
		consistTextField.setText(cre.getConsistNumber());
		int cNum = validConsist(cre.getConsistNumber());

		if (0 < cNum) {
			log.debug("verify consist matches roster selection");
			verifyRosterMatch = true;
			consistNum = getConsist();
		} else {
			if (cre.getConsistNumber().equals(CLEARED) || cre.getConsistNumber().equals("0")){
				log.debug("search for empty consist");
				consistTextField.setText(Integer.toString(emptyConsistSearchStart));
				emptyConsistSearch = true;
				consistSearchNext = true;
				consistNum = getConsist();
				loadFullRoster (cre);
				saveLoadButton.setEnabled(false);
			} else {
				log.error("roster consist number is out of range: " + consistNum);
				consistStatus.setText(ERROR);
			}
		}
	}
	
	private void loadFullRoster (NceConsistRosterEntry cre){
		// get road name, number and model
		textConRoadName.setText(cre.getRoadName());
		textConRoadNumber.setText(cre.getRoadNumber());	
		textConModel.setText(cre.getModel());	
		
		// load lead loco
		locoTextField1.setText(cre.getLoco1DccAddress());
		adrButton1.setText(cre.isLoco1LongAddress() ? LONG : SHORT);
		dirButton1.setText(convertDTD(cre.getLoco1Direction()));
		locoRosterBox1.setEnabled(true);
		locoTextField1.setEnabled(true);
		adrButton1.setEnabled(true);
		dirButton1.setEnabled(true);

		// load rear loco
		locoTextField2.setText(cre.getLoco2DccAddress());
		adrButton2.setText(cre.isLoco2LongAddress() ? LONG : SHORT);
		dirButton2.setText(convertDTD(cre.getLoco2Direction()));
		locoRosterBox2.setEnabled(true);
		locoTextField2.setEnabled(true);
		adrButton2.setEnabled(true);
		dirButton2.setEnabled(true);

		// load Mid1 loco
		locoTextField3.setText(cre.getLoco3DccAddress());
		adrButton3.setText(cre.isLoco3LongAddress() ? LONG : SHORT);
		dirButton3.setText(convertDTD(cre.getLoco3Direction()));
		locoRosterBox3.setEnabled(true);
		locoTextField3.setEnabled(true);
		adrButton3.setEnabled(true);
		dirButton3.setEnabled(true);

		// load Mid2 loco
		locoTextField4.setText(cre.getLoco4DccAddress());
		adrButton4.setText(cre.isLoco4LongAddress() ? LONG : SHORT);
		dirButton4.setText(convertDTD(cre.getLoco4Direction()));
		locoRosterBox4.setEnabled(true);
		locoTextField4.setEnabled(true);
		adrButton4.setEnabled(true);
		dirButton4.setEnabled(true);

		// load Mid3 loco
		locoTextField5.setText(cre.getLoco5DccAddress());
		adrButton5.setText(cre.isLoco5LongAddress() ? LONG : SHORT);
		dirButton5.setText(convertDTD(cre.getLoco5Direction()));
		locoRosterBox5.setEnabled(true);
		locoTextField5.setEnabled(true);
		adrButton5.setEnabled(true);
		dirButton5.setEnabled(true);

		// load Mid4 loco
		locoTextField6.setText(cre.getLoco6DccAddress());
		adrButton6.setText(cre.isLoco6LongAddress() ? LONG : SHORT);
		dirButton6.setText(convertDTD(cre.getLoco6Direction()));
		locoRosterBox6.setEnabled(true);
		locoTextField6.setEnabled(true);
		adrButton6.setEnabled(true);
		dirButton6.setEnabled(true);
	}
	
	/**
	 * checks to see if all loco addresses in NCE consist match roster
	 * updates road name, road number, and loco direction fields
	 * @return true if match
	 */
	private boolean consistRosterMatch (NceConsistRosterEntry cre){
		if (consistTextField.getText().equals(cre.getConsistNumber()) 
				&& locoTextField1.getText().equals(cre.getLoco1DccAddress())
				&& locoTextField2.getText().equals(cre.getLoco2DccAddress())
				&& locoTextField3.getText().equals(cre.getLoco3DccAddress())
				&& locoTextField4.getText().equals(cre.getLoco4DccAddress())
				&& locoTextField5.getText().equals(cre.getLoco5DccAddress())
				&& locoTextField6.getText().equals(cre.getLoco6DccAddress())
						){
			// match!  Only load the elements needed
			if (newConsist){
				textConRoadName.setText(cre.getRoadName());
				textConRoadNumber.setText(cre.getRoadNumber());	
				textConModel.setText(cre.getModel());	
				dirButton1.setText(convertDTD(cre.getLoco1Direction()));
				dirButton2.setText(convertDTD(cre.getLoco2Direction()));
				dirButton3.setText(convertDTD(cre.getLoco3Direction()));
				dirButton4.setText(convertDTD(cre.getLoco4Direction()));
				dirButton5.setText(convertDTD(cre.getLoco5Direction()));
				dirButton6.setText(convertDTD(cre.getLoco6Direction()));
			}
			return true;
		} else {
			return false;
		}
	}
	
	private boolean enablePartialMatch = true;
	
	/**
	 * checks to see if some loco addresses in NCE consist match roster
	 * updates road name, road number, and loco direction fields
	 * @return true if there was at least one match
	 */
	private boolean consistRosterPartialMatch (NceConsistRosterEntry cre){
		if (!enablePartialMatch)
			return false;
		// does loco1 match?
		if (consistTextField.getText().equals(cre.getConsistNumber()) 
				&& locoTextField1.getText().equals(cre.getLoco1DccAddress())){
			dirButton1.setText(convertDTD(cre.getLoco1Direction()));
			textConRoadName.setText(cre.getRoadName());
			textConRoadNumber.setText(cre.getRoadNumber());	
			textConModel.setText(cre.getModel());	
		} else {
			consistStatus.setText(UNKNOWN);
			return false;
		}
		if (locoTextField2.getText().equals(cre.getLoco2DccAddress())){
			dirButton2.setText(convertDTD(cre.getLoco2Direction()));
		}
		if (locoTextField3.getText().equals(cre.getLoco3DccAddress())){
			dirButton3.setText(convertDTD(cre.getLoco3Direction()));
		}
		if (locoTextField4.getText().equals(cre.getLoco4DccAddress())){
			dirButton4.setText(convertDTD(cre.getLoco4Direction()));
		}
		if (locoTextField5.getText().equals(cre.getLoco5DccAddress())){
			dirButton5.setText(convertDTD(cre.getLoco5Direction()));
		}
		if (locoTextField6.getText().equals(cre.getLoco6DccAddress())){
			dirButton6.setText(convertDTD(cre.getLoco6Direction()));
		}
		consistStatus.setText(MODIFIED);
		return true;
	}

	protected List<NceConsistRosterEntry> consistList = new ArrayList<NceConsistRosterEntry>();

	/**
	 * returns true if update successful
	 */
	private boolean updateRoster(String consistNumber) {
		if (!checkBoxConsist.isSelected())
			return false;
		String id = locoTextField1.getText(); // lead loco is the consist id
		if (id.equals(EMPTY)) {
			log.debug("Attempt to modify consist without valid id");
			return false;
		}
		// need rear loco to form a consist
		if (locoTextField2.getText().equals(EMPTY))
			return false;
		NceConsistRosterEntry cre;
		consistList = NceConsistRoster.instance().matchingList(null, null,
				null, null, null, null, null, null, null, id);
		// if consist doesn't exist in roster ask user if they want to create one
		if (consistList.isEmpty()) {
			if (JOptionPane.showConfirmDialog(null, "The consist " + id
					+ " doesn't exist in your roster,"
					+ " do you want to add it?", "Save consist to roster?",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return false;
			}
			cre = new NceConsistRosterEntry();
			NceConsistRoster.instance().addEntry(cre);
			// roster entry exists, does it match?
		} else {
			cre = NceConsistRoster.instance().entryFromTitle(id);
			// if all of the loco addresses match, just update without telling user
			consistList = NceConsistRoster.instance()
					.matchingList(null, null, null, locoTextField1.getText(),
							locoTextField2.getText(), locoTextField3.getText(),
							locoTextField4.getText(), locoTextField5.getText(),
							locoTextField6.getText(), id);
			// if it doesn't match, do we want to modify it?
			if (consistList.isEmpty()) {
				if (JOptionPane.showConfirmDialog(null, "Consist " + id
						+ " already exists, update?" + getRosterText(cre),
						"Modify roster?", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					// update consist if command was to clear
					if (consistNumber.equals(CLEARED)){
						cre.setConsistNumber(consistNumber);
						writeRosterFile();
					}
					return false;
				}
			}
			log.debug("Modify consist " + id);
		}
		// save all elements of a consist roster
		cre.setId(id);
		cre.setConsistNumber(consistNumber);
		cre.setRoadName(textConRoadName.getText());
		cre.setRoadNumber(textConRoadNumber.getText());
		cre.setModel(textConModel.getText());
		// save lead loco
		cre.setLoco1DccAddress(locoTextField1.getText());
		cre.setLoco1LongAddress(adrButton1.getText().equals(LONG));
		cre.setLoco1Direction(directionDTD(dirButton1));
		// save rear loco
		cre.setLoco2DccAddress(locoTextField2.getText());
		cre.setLoco2LongAddress(adrButton2.getText().equals(LONG));
		cre.setLoco2Direction(directionDTD(dirButton2));
		// save Mid1 loco
		cre.setLoco3DccAddress(locoTextField3.getText());
		cre.setLoco3LongAddress(adrButton3.getText().equals(LONG));
		cre.setLoco3Direction(directionDTD(dirButton3));
		// save Mid2 loco
		cre.setLoco4DccAddress(locoTextField4.getText());
		cre.setLoco4LongAddress(adrButton4.getText().equals(LONG));
		cre.setLoco4Direction(directionDTD(dirButton4));
		// save Mid3 loco
		cre.setLoco5DccAddress(locoTextField5.getText());
		cre.setLoco5LongAddress(adrButton5.getText().equals(LONG));
		cre.setLoco5Direction(directionDTD(dirButton5));
		// save Mid4 loco
		cre.setLoco6DccAddress(locoTextField6.getText());
		cre.setLoco6LongAddress(adrButton6.getText().equals(LONG));
		cre.setLoco6Direction(directionDTD(dirButton6));

		writeRosterFile();
		return true;
	}
	
	// DTD format for loco direction
	private static final String DTD_NORMAL = "normal";
	private static final String DTD_REVERSE = "reverse";
	private static final String DTD_UNKNOWN = "unknown";

	/**
	 * @return DTD direction format based on the loco direction button
	 */
	private String directionDTD(JButton dirButton) {
		String formatDTD = DTD_UNKNOWN;
		if (dirButton.getText().equals(FWD))
			formatDTD = DTD_NORMAL;
		if (dirButton.getText().equals(REV))
			formatDTD = DTD_REVERSE;
		return formatDTD;
	}
	/**
	 * @return converts DTD direction to FWD, REV, and ??
	 */
	private String convertDTD(String formatDTD) {
		String word = QUESTION;
		if (formatDTD.equals(DTD_NORMAL))
			word = FWD;
		if (formatDTD.equals(DTD_REVERSE))
			word = REV;
		return word;
	}
	
	/**
	 * @return converts DTD direction to FWD, REV, and ""
	 */
	private String shortHandConvertDTD(String formatDTD) {
		String word = EMPTY;
		if (formatDTD.equals(DTD_NORMAL))
			word = FWD;
		if (formatDTD.equals(DTD_REVERSE))
			word = REV;
		return word;
	}

	// remove selected consist from roster
	private void deleteRoster() {
		String entry = conRosterBox.getSelectedItem().toString();
		log.debug("remove consist " + entry + " from roster ");
		// delete it from roster
		NceConsistRoster.instance().removeEntry(
				NceConsistRoster.instance().entryFromTitle(entry));
		writeRosterFile();
	}

	private void writeRosterFile() {
		conRosterBox.removeActionListener(consistRosterListener);
		NceConsistRoster.writeRosterFile();
		NceConsistRoster.instance().updateComboBox(conRosterBox);
		conRosterBox.insertItemAt(EMPTY, 0);
		conRosterBox.setSelectedIndex(0);
		conRosterBox.addActionListener(consistRosterListener);
	}

	// can the consist be loading into NCE memory?
	private boolean canLoad() {
		if (locoTextField1.getText().equals(EMPTY))
			return false;
		if (dirButton1.getText().equals(QUESTION))
			return false;
		if (locoTextField2.getText().equals(EMPTY))
			return false;
		if (dirButton2.getText().equals(QUESTION))
			return false;
		if (!locoTextField3.getText().equals(EMPTY)
				&& dirButton3.getText().equals(QUESTION))
			return false;
		if (!locoTextField4.getText().equals(EMPTY)
				&& dirButton4.getText().equals(QUESTION))
			return false;
		if (!locoTextField5.getText().equals(EMPTY)
				&& dirButton5.getText().equals(QUESTION))
			return false;
		if (!locoTextField6.getText().equals(EMPTY)
				&& dirButton6.getText().equals(QUESTION))
			return false;
		// okay to load, clean up empty loco fields
		if (locoTextField3.getText().equals(EMPTY))
			dirButton3.setText(QUESTION);
		if (locoTextField4.getText().equals(EMPTY))
			dirButton4.setText(QUESTION);
		if (locoTextField5.getText().equals(EMPTY))
			dirButton5.setText(QUESTION);
		if (locoTextField6.getText().equals(EMPTY))
			dirButton6.setText(QUESTION);
		if (saveLoadButton.getText().equals(LOAD))
			return true;
		else if (exactMatch)
			// no need to save, exact match!
			return false;
		else
			return true;
	}
	
	// mimic NCE mid loco shift when there's empties
	private void loadShift() {
		for (int i = 0; i < 3; i++) {
			shiftOneLine(locoTextField5, adrButton5, dirButton5, locoTextField6,
					adrButton6, dirButton6);
			shiftOneLine(locoTextField4, adrButton4, dirButton4, locoTextField5,
					adrButton5, dirButton5);
			shiftOneLine(locoTextField3, adrButton3, dirButton3, locoTextField4,
					adrButton4, dirButton4);
			shiftOneLine(locoTextField2, adrButton2, dirButton2, locoTextField3,
					adrButton3, dirButton3);
		}
	}
	
	private void shiftOneLine(JTextField locoTextFieldLow, JButton adrButtonLow,
			JButton dirButtonLow, JTextField locoTextFieldHigh,
			JButton adrButtonHigh, JButton dirButtonHigh) {
		if (locoTextFieldLow.getText().equals(EMPTY) && !locoTextFieldHigh.getText().equals((EMPTY))){
			locoTextFieldLow.setText(locoTextFieldHigh.getText());
			adrButtonLow.setText(adrButtonHigh.getText());
			dirButtonLow.setText(dirButtonHigh.getText());
			dirButtonHigh.setText(QUESTION);
			locoTextFieldHigh.setText(EMPTY);
		} else {
			return;
		}
	}

	// change button operation during load consist from roster
	private void changeButtons(boolean rosterDisplay) {
		if (rosterDisplay) {
			clearCancelButton.setText(CANCEL);
			clearCancelButton.setToolTipText(ToolTipCancel);
			clearCancelButton.setEnabled(true);
			saveLoadButton.setText(LOAD);
			saveLoadButton.setToolTipText(ToolTipLoad);
		} else {
			clearCancelButton.setText(CLEAR);
			clearCancelButton.setToolTipText(ToolTipClear);
			saveLoadButton.setText(SAVE);
			saveLoadButton.setToolTipText(ToolTipSave);
			clearCancelButton.setEnabled(!locoTextField1.getText().equals(EMPTY));
		}

		// toggle (on if we're loading a consist from roster)
		deleteButton.setEnabled(rosterDisplay);

		// toggle (off if we're loading a consist from roster)
		previousButton.setEnabled(!rosterDisplay);
		nextButton.setEnabled(!rosterDisplay);
		getButton.setEnabled(!rosterDisplay);
		backUpButton.setEnabled(!rosterDisplay);
		restoreButton.setEnabled(!rosterDisplay);
		saveLoadButton.setEnabled(!rosterDisplay);

		cmdButton1.setVisible(!rosterDisplay);
		cmdButton2.setVisible(!rosterDisplay);
		cmdButton3.setVisible(!rosterDisplay);
		cmdButton4.setVisible(!rosterDisplay);
		cmdButton5.setVisible(!rosterDisplay);
		cmdButton6.setVisible(!rosterDisplay);
	}

	/**
	 * Kills consist using lead loco address
	 */
	private void killConsist() {
		if (validLocoAdr(locoTextField1.getText()) < 0)
		// special case where lead or rear loco was being replaced
			return;
		int locoAddr = getLocoAddr (locoTextField1, adrButton1);
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
			consistStatus.setText(ERROR);
			log.error("reply length error, expecting: " + replyLen + " got: "
					+ r.getNumDataElements());
			return;
		}

		// response to commands
		if (replyLen == REPLY_1) {
			// Looking for proper response
			int recChar = r.getElement(0);
			log.debug ("command reply: " + recChar);
			if (recChar == '!') {
				if (locoSearch && waiting == 0) {
					readConsistMemory(consistNumVerify, LEAD);
					consistStatus.setText(VERIFY);
					return;
				}
				if (refresh && waiting == 0) {
					refresh = false;
					// update panel
					readConsistMemory(consistNum, LEAD);
					return;
				}
				consistStatus.setText(OKAY);
			} else {
				consistStatus.setText(ERROR);
			}
			return;
		}

		// Consist memory read
		if (replyLen == REPLY_16) {
			// are we verifying that loco isn't already part of a consist?
			if (locoSearch) {
				// search the 16 bytes for a loco match
				for (int i = 0; i < 16;) {
					int rC = r.getElement(i++);
					rC = (rC << 8) & 0xFF00;
					int rC_l = r.getElement(i++);
					rC_l = rC_l & 0xFF;
					rC = rC + rC_l;
					// does it match any of the locos?
					for (int j = 0; j < locoVerifyList.length; j++){
						if (locoVerifyList[j] == 0)	// done searching?
							break;
						if (rC == locoVerifyList[j]) {
							// ignore matching the consist that we're adding the
							// loco
							if (consistNumVerify != consistNum) {
								locoSearch = false; // quit the search
								consistStatus.setText(ERROR);
								locoNumInUse = rC & 0x3FFF;
								queueError (ERROR_LOCO_IN_USE);
								return;
							}
						}
					}
					consistNumVerify++;
				}
				if (consistNumVerify > CONSIST_MAX) {
					if (locoNum == LEAD) {
						// now verify the rear loco consist
						locoNum = REAR;
						consistNumVerify = 0;
					} else {
						// verify complete, loco address is unique
						locoSearch = false;
						consistStatus.setText(OKAY);
						// determine the type of verification
						if (verifyType == VERIFY_LEAD_REAR){
							if (refresh && waiting == 0) {
								refresh = false;
								// update panel
								readConsistMemory(consistNum, LEAD);
							}
						} else if (verifyType == VERIFY_MID_FWD) {
							sendNceBinaryCommand(locoVerifyList[0],
									NceBinaryCommand.LOCO_CMD_FWD_CONSIST_MID,
									(byte) consistNum);
						} else if (verifyType == VERIFY_MID_REV){
							sendNceBinaryCommand(locoVerifyList[0],
									NceBinaryCommand.LOCO_CMD_REV_CONSIST_MID,
									(byte) consistNum);
						} else if (verifyType == VERIFY_ALL){
							fullLoad();
						} else {
							log.debug("verifyType out of range");
						}
						verifyType = VERIFY_DONE;
						return;
					}
				}
				// continue verify
				readConsistMemory(consistNumVerify, locoNum);
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

					if (emptyConsistSearch){
						if( rC == 0 ){
							// found an empty consist!
							consistSearchNext = false;
							emptyConsistSearch = false;
							consistStatus.setText(OKAY);
							saveLoadButton.setEnabled(canLoad());
							return;
						}
					}
					else if (checkBoxEmpty.isSelected()) {
						if( rC == 0 && consistCount > 0) {
							// found an empty consist!
							consistSearchNext = false;
							// update the panel
							readConsistMemory(consistNum, LEAD);
							return;
						}
					}
					else if (rC != 0 && consistCount > 0) {
						// found a consist!	
						consistSearchNext = false;
						readConsistMemory(consistNum, LEAD);
						return;
					}
					if (++consistCount > CONSIST_MAX) {
						// could not find a consist
						consistSearchNext = false;
						consistStatus.setText(NONE);
						if (emptyConsistSearch) {
							emptyConsistSearch = false;
							queueError(ERROR_NO_EMPTY_CONSIST);
						}
						return;		// don't update panel
					}
					// look for next consist
					consistNum--;
					if (consistNum < CONSIST_MIN)
						consistNum = CONSIST_MAX;
					consistTextField.setText(Integer.toString(consistNum));
					if (consistNum == CONSIST_MAX) {
						// we need to read NCE memory to continue
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
						consistStatus.setText(NONE);
						consistSearchPrevious = false;
						return;		// don't update the panel
					}
					consistNum++;
					if (consistNum > CONSIST_MAX)
						consistNum = CONSIST_MIN;
					consistTextField.setText(Integer.toString(consistNum));
					// have we wraped? if yes, need to read NCE memory
					if (consistNum == CONSIST_MIN)
						break;
				}
				readConsistMemory(consistNum, LEAD);
				return;
			}

			// Panel update, load lead loco
			if (locoNum == LEAD) {
				boolean loco1exists = updateLocoFields(r, 0, locoRosterBox1,
						locoTextField1, adrButton1, dirButton1, cmdButton1);
				if (clearCancelButton.getText().equals(CLEAR))
					clearCancelButton.setEnabled(loco1exists);

				// load rear loco
			} else if (locoNum == REAR) {
				updateLocoFields(r, 0, locoRosterBox2, locoTextField2,
						adrButton2, dirButton2, cmdButton2);

				// load mid locos
			} else {
				updateLocoFields(r, 0, locoRosterBox3, locoTextField3,
						adrButton3, dirButton3, cmdButton3);
				updateLocoFields(r, 2, locoRosterBox4, locoTextField4,
						adrButton4, dirButton4, cmdButton4);
				updateLocoFields(r, 4, locoRosterBox5, locoTextField5,
						adrButton5, dirButton5, cmdButton5);
				updateLocoFields(r, 6, locoRosterBox6, locoTextField6,
						adrButton6, dirButton6, cmdButton6);
				consistStatus.setText(OKAY);
				checkForRosterMatch();
				saveLoadButton.setEnabled(canLoad());
			}
			// read the next loco number in the consist
			if (locoNum == LEAD || locoNum == REAR) {
				locoNum++;
				readConsistMemory(consistNum, locoNum);
			}
		}
	}
	
	private boolean exactMatch = false;
	
	private void checkForRosterMatch(){
		exactMatch = false;
		if (!verifyRosterMatch)
			cre = NceConsistRoster.instance().entryFromTitle(locoTextField1.getText());
		if (cre == null){
			if (checkBoxConsist.isSelected() && !locoTextField1.getText().equals(EMPTY))
				consistStatus.setText(UNKNOWN);
			else
			textConRoadName.setText(EMPTY);
			textConRoadNumber.setText(EMPTY);	
			textConModel.setText(EMPTY);	
			return;
		}
		if (consistRosterMatch(cre)){
			exactMatch = true;
			// exact match!
			if (verifyRosterMatch)
				queueError(WARN_CONSIST_ALREADY_LOADED);
			verifyRosterMatch = false;
		} else {
			// not an exact match!
			if (verifyRosterMatch)
				queueError(ERROR_CONSIST_DOESNT_MATCH);
			verifyRosterMatch = false;
			if (!consistRosterPartialMatch(cre)){
				textConRoadName.setText(EMPTY);
				textConRoadNumber.setText(EMPTY);	
				textConModel.setText(EMPTY);	
			}
		}
	}

	// update loco fields, returns false if loco address is null
	private boolean updateLocoFields(NceReply r, int index,
			JComboBox locoRosterBox, JTextField locoTextField,
			JButton adrButton, JButton dirButton, JButton cmdButton) {
		// index = 0 for lead and rear locos, 0,2,4,6 for mid
		String locoAddrText = getLocoAddrText(r, index);
		boolean locoType = getLocoAddressType(r, index); // Long or short address?
		String locoDirection = getLocoDirection(dirButton);
		
		locoTextField.setText(locoAddrText);
		locoRosterBox.setSelectedIndex(0);

		if (locoAddrText.equals(EMPTY) || locoAddrText.equals(REPLACE_LOCO)) {
			locoRosterBox.setEnabled(true);
			locoTextField.setEnabled(true);
			cmdButton.setText(ADD);
			cmdButton.setVisible(true);
			cmdButton.setEnabled(false);
			cmdButton.setToolTipText(ToolTipAdd);
			dirButton.setText(QUESTION);
			dirButton.setEnabled(true);
			adrButton.setText(LONG);
			adrButton.setEnabled(true);
			return false;
		} else {
			locoTextField.setText(locoAddrText);
			locoRosterBox.setEnabled(false);
			locoTextField.setEnabled(false);
			cmdButton.setEnabled(true);
			dirButton.setText(locoDirection);
			dirButton.setEnabled(false);
			adrButton.setText((locoType) ? LONG : SHORT);
			adrButton.setEnabled(false);
			
			// can not delete lead or rear locos, but can replace
			if (locoTextField == locoTextField1 || locoTextField == locoTextField2) {
				cmdButton.setText(REPLACE);
				cmdButton.setToolTipText("Press to delete and replace this loco");
			} else {
				cmdButton.setText(DELETE);
				cmdButton.setToolTipText("Press to delete this loco from consist");
			}
			return true;
		}
	}

	// modify loco fields because an add, replace, delete button has been pressed
	private void modifyLocoFields(JComboBox locoRosterBox,
			JTextField locoTextField, JButton adrButton, JButton dirButton,
			JButton cmdButton) {
		if (validLocoAdr(locoTextField.getText()) < 0)
			return;
		byte cN = (byte) validConsist(consistTextField.getText());
		if (cN < 0)
			return;
		if (locoTextField.getText().equals(EMPTY)) {
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter loco address before pressing add", "NCE Consist",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// set reflesh flag to update panel
		refresh = true;
		int locoAddr = getLocoAddr (locoTextField, adrButton);

		if (cmdButton.getText() == DELETE) {
			sendNceBinaryCommand(locoAddr,
					NceBinaryCommand.LOCO_CMD_DELETE_LOCO_CONSIST, (byte) 0);

		} else if (cmdButton.getText() == REPLACE) {

			// Kill refresh flag, no update when replacing loco
			refresh = false;

			// allow user to add loco to lead or rear consist

			locoRosterBox.setEnabled(true);
			locoTextField.setText(EMPTY);
			locoTextField.setEnabled(true);
			adrButton.setText(LONG);
			adrButton.setEnabled(true);
			dirButton.setText(QUESTION);
			dirButton.setEnabled(true);
			cmdButton.setText(ADD);
			cmdButton.setToolTipText(ToolTipAdd);

			// now update CS memory in case user doesn't use the Add button
			// this will also allow us to delete the loco from the layout
			if (locoTextField == locoTextField1) {
				// replace lead loco
				sendNceBinaryCommand(LOC_ADR_REPLACE,
						NceBinaryCommand.LOCO_CMD_FWD_CONSIST_LEAD, cN);
				// no lead loco so we can't kill the consist
				clearCancelButton.setEnabled(false);
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
			if (locoTextField == locoTextField1) {
				if (dirButton.getText() == FWD) {
					sendNceBinaryCommand(locoAddr,
							NceBinaryCommand.LOCO_CMD_FWD_CONSIST_LEAD, cN);
				}
				if (dirButton.getText() == REV) {
					sendNceBinaryCommand(locoAddr,
							NceBinaryCommand.LOCO_CMD_REV_CONSIST_LEAD, cN);
				}
			// rear loco?
			} else if (locoTextField == locoTextField2) {
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
				if (locoSearch) {
					if (dirButton.getText() == FWD)
						verifyType = VERIFY_MID_FWD;
					else
						verifyType = VERIFY_MID_REV;
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
	
	private void fullLoad(){
		refresh = true;
		loadOneLine(locoRosterBox1, locoTextField1, adrButton1,
				dirButton1, cmdButton1);
		loadOneLine(locoRosterBox2, locoTextField2, adrButton2,
				dirButton2, cmdButton2);
		loadOneLine(locoRosterBox3, locoTextField3, adrButton3,
				dirButton3, cmdButton3);
		loadOneLine(locoRosterBox4, locoTextField4, adrButton4,
				dirButton4, cmdButton4);
		loadOneLine(locoRosterBox5, locoTextField5, adrButton5,
				dirButton5, cmdButton5);
		loadOneLine(locoRosterBox6, locoTextField6, adrButton6,
				dirButton6, cmdButton6);
		changeButtons(false);
	}

	/**
	 * updates NCE CS based on the loco line supplied
	 * called by load button
	 * @param locoRosterBox
	 * @param locoTextField
	 * @param adrButton
	 * @param dirButton
	 * @param cmdButton
	 */
	private void loadOneLine(JComboBox locoRosterBox, JTextField locoTextField,
			JButton adrButton, JButton dirButton, JButton cmdButton) {
		if (locoTextField.getText().equals(EMPTY))
			return;
		if (validLocoAdr(locoTextField.getText()) < 0)
			return;
		byte cN = (byte) validConsist(consistTextField.getText());
		if (cN < 0)
			return;

		int locoAddr = getLocoAddr (locoTextField, adrButton);

		// ADD loco to consist
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
		if (locoTextField == locoTextField1) {
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
		} else if (locoTextField == locoTextField2) {
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
	
	private int getLocoAddr (JTextField locoTextField, JButton adrButton){
		int locoAddr = Integer.parseInt(locoTextField.getText());
		if (locoAddr >= 128)
			locoAddr += 0xC000;
		else if (adrButton.getText() == LONG)
			locoAddr += 0xC000;
		return locoAddr;
	}

	private void sendNceMessage(byte[] b, int replyLength) {
		NceMessage m = NceMessage.createBinaryMessage(b, replyLength);
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

	private String getLocoAddrText(NceReply r, int i) {
		int rC = r.getElement(i++);
		rC = (rC << 8) & 0x3F00;
		int rC_l = r.getElement(i);
		rC_l = rC_l & 0xFF;
		rC = rC + rC_l;
		String locoAddrText = EMPTY;
		if (rC != 0) 
			locoAddrText = Integer.toString(rC);
		if (rC == LOC_ADR_REPLACE) 
			locoAddrText = REPLACE_LOCO;
		return locoAddrText;
	}

	private String getLocoDirection(JButton dirButton) {
		if (newConsist)
			return QUESTION;
		else
			return dirButton.getText();
	}

	// check command station memory for lead or rear loco match
	private void verifyLocoAddr(int locoAddr) {
		verifyType = VERIFY_LEAD_REAR;
		if (checkBoxVerify.isSelected()) {
			locoVerifyList[0] = locoAddr;
			locoVerifyList[1] = 0;		// end of list
			locoSearch = true;
			consistNumVerify = 0;
		}
	}
	
	// check command station memory for lead or rear loco match
	private boolean verifyAllLocoAddr() {
		verifyType = VERIFY_ALL;
		if (checkBoxVerify.isSelected()) {
			int i = 0;
			if (!locoTextField1.getText().equals(EMPTY) && validLocoAdr(locoTextField1.getText()) > 0)
				locoVerifyList[i++] = getLocoAddr (locoTextField1, adrButton1);
			if (!locoTextField2.getText().equals(EMPTY) && validLocoAdr(locoTextField2.getText()) > 0)
				locoVerifyList[i++] = getLocoAddr (locoTextField2, adrButton2);
			if (!locoTextField3.getText().equals(EMPTY) && validLocoAdr(locoTextField3.getText()) > 0)
				locoVerifyList[i++] = getLocoAddr (locoTextField3, adrButton3);
			if (!locoTextField4.getText().equals(EMPTY) && validLocoAdr(locoTextField4.getText()) > 0)
				locoVerifyList[i++] = getLocoAddr (locoTextField4, adrButton4);
			if (!locoTextField5.getText().equals(EMPTY) && validLocoAdr(locoTextField5.getText()) > 0)
				locoVerifyList[i++] = getLocoAddr (locoTextField5, adrButton5);
			if (!locoTextField6.getText().equals(EMPTY) && validLocoAdr(locoTextField6.getText()) > 0)
				locoVerifyList[i++] = getLocoAddr (locoTextField6, adrButton6);
			else
				locoVerifyList[i] = 0;
			locoSearch = true;
			consistNumVerify = 0;
			consistStatus.setText(VERIFY);
			readConsistMemory(consistNumVerify, LEAD);
			return true;
		}
		return false;
	}

	private void addLocoRow(JComponent col1, JComponent col2, JComponent col3,
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
	
	private void enableAllLocoRows (boolean flag){
		enableLocoRow(flag, locoTextField1, locoRosterBox1,
				adrButton1, dirButton1, cmdButton1);
		enableLocoRow(flag, locoTextField2, locoRosterBox2,
				adrButton2, dirButton2, cmdButton2);
		enableLocoRow(flag, locoTextField3, locoRosterBox3,
				adrButton3, dirButton3, cmdButton3);
		enableLocoRow(flag, locoTextField4, locoRosterBox4,
				adrButton4, dirButton4, cmdButton4);
		enableLocoRow(flag, locoTextField5, locoRosterBox5,
				adrButton5, dirButton5, cmdButton5);
		enableLocoRow(flag, locoTextField6, locoRosterBox6,
				adrButton6, dirButton6, cmdButton6);
	}
	
	private void enableLocoRow(boolean flag, JTextField locoTextField,
			JComboBox locoRosterBox, JButton adrButton, JButton dirButton,
			JButton cmdButton) {
		locoTextField.setEnabled(flag);
		locoRosterBox.setEnabled(flag);
		adrButton.setEnabled(flag);
		dirButton.setEnabled(flag);
		cmdButton.setEnabled(flag);
	}

	// initialize loco fields
	private void initLocoFields() {
		initLocoRow(1, "Lead", textLoco1, locoTextField1, locoRosterBox1,
				adrButton1, dirButton1, cmdButton1);
		initLocoRow(2, "Rear", textLoco2, locoTextField2, locoRosterBox2,
				adrButton2, dirButton2, cmdButton2);
		initLocoRow(3, "Mid 1", textLoco3, locoTextField3, locoRosterBox3,
				adrButton3, dirButton3, cmdButton3);
		initLocoRow(4, "Mid 2", textLoco4, locoTextField4, locoRosterBox4,
				adrButton4, dirButton4, cmdButton4);
		initLocoRow(5, "Mid 3", textLoco5, locoTextField5, locoRosterBox5,
				adrButton5, dirButton5, cmdButton5);
		initLocoRow(6, "Mid 4", textLoco6, locoTextField6, locoRosterBox6,
				adrButton6, dirButton6, cmdButton6);
	}

	private void initLocoRow(int row, String s, JLabel textLoco,
			JTextField locoTextField, JComboBox locoRosterBox,
			JButton adrButton, JButton dirButton, JButton cmdButton) {

		textLoco.setText(s);
		textLoco.setVisible(true);

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
		locoRosterBox.setToolTipText("Select loco from roster");
		locoRosterBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				locoSelected(e);
			}
		});

		dirButton.setText(QUESTION);
		dirButton.setVisible(true);
		dirButton.setEnabled(false);
		dirButton.setToolTipText("Press to change loco direction");
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

		locoTextField.setText(EMPTY);
		locoTextField.setEnabled(false);
		locoTextField.setToolTipText("Enter loco address");
		locoTextField.setMaximumSize(new Dimension(
				locoTextField.getMaximumSize().width, locoTextField
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
	
	private static final int ERROR_LOCO_IN_USE = 1;
	private static final int ERROR_NO_EMPTY_CONSIST = 2;
	private static final int ERROR_CONSIST_DOESNT_MATCH = 3;
	private static final int WARN_CONSIST_ALREADY_LOADED = 4;
	private int locoNumInUse; 						// report loco alreay in use
	private int errorCode = 0;
	
	private void queueError (int errorCode){
		log.debug ("queue warning/error message: " + errorCode);
		if (this.errorCode != 0){
			log.debug ("multiple errors reported " + this.errorCode);
			return;
		}
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

		case ERROR_LOCO_IN_USE: 
			JOptionPane.showMessageDialog(NceConsistEditFrame.this, "Loco address "
					+ locoNumInUse + " is part of consist " + consistNumVerify,
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
					+ getRosterText(cre),
					"Continue loading consist from roster?",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				if (JOptionPane.showConfirmDialog(null,
						"Consist " + cre.getId() + " was assigned NCE consist number " + cre.getConsistNumber() +
						"\n Do you want to reset it so you can load next time?",
						"Reset NCE consist number?",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					cre.setConsistNumber(CLEARED);
				}
				changeButtons (false);
				saveLoadButton.setEnabled(canLoad());
				break;
			}
			changeButtons (true);
			loadFullRoster (cre);
			saveLoadButton.setEnabled(canLoad());
			break;
		case WARN_CONSIST_ALREADY_LOADED:
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Consist has already been loaded!",
					"NCE Consist", JOptionPane.WARNING_MESSAGE);
			break;
		}
		errorCode = 0;
	}
	
	private String getRosterText (NceConsistRosterEntry cre) {
		return "\n" 
		+ "\n NCE Consist Number: "	+ cre.getConsistNumber()
		+ "\n"
		+ "\n Lead Locomotive: " + cre.getLoco1DccAddress() 
		+" "+ shortHandConvertDTD(cre.getLoco1Direction())  
		+ "\n Rear Locomotive: " + cre.getLoco2DccAddress()
		+" "+ shortHandConvertDTD(cre.getLoco2Direction())  
		+ "\n Mid1 Locomotive: " + cre.getLoco3DccAddress()
		+" "+ shortHandConvertDTD(cre.getLoco3Direction())  
		+ "\n Mid2 Locomotive: " + cre.getLoco4DccAddress()
		+" "+ shortHandConvertDTD(cre.getLoco4Direction())  
		+ "\n Mid3 Locomotive: " + cre.getLoco5DccAddress()
		+" "+ shortHandConvertDTD(cre.getLoco5Direction())  
		+ "\n Mid4 Locomotive: " + cre.getLoco6DccAddress()
		+" "+ shortHandConvertDTD(cre.getLoco6Direction());
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(NceConsistEditFrame.class.getName());
}
