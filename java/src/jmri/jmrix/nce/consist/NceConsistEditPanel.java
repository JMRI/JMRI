// NceConsistEditPanel.java

package jmri.jmrix.nce.consist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jmri.DccLocoAddress;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceCmdStationMemory;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Pane for user edit of NCE Consists
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
 * Cloned from NceConsistEditFrame by
 * @author kcameron Copyright (C) 2010
 * @version $Revision$
 */

public class NceConsistEditPanel extends jmri.jmrix.nce.swing.NcePanel implements
		jmri.jmrix.nce.NceListener {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.consist.NceConsistBundle");
    
	private static final int CONSIST_MIN = 1; 			// NCE doesn't use consist 0
	private static final int CONSIST_MAX = 127;
	private static final int LOC_ADR_MIN = 0; 			// loco address range
	private static final int LOC_ADR_MAX = 10239;
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

	private int replyLen = 0; 					// expected byte length
	private int waiting = 0; 					// to catch responses not intended for this module

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
	JLabel space1 = new JLabel("            ");
	JLabel space2 = new JLabel(" ");
	JLabel space3a = new JLabel("                            ");
	JLabel space3b = new JLabel("                            ");
	JLabel space3c = new JLabel("                            ");
	JLabel space3d = new JLabel("                            ");
	
	JLabel space15 = new JLabel(" ");

	// lead loco
	JLabel textLoco1 = new JLabel();
	JTextField locoTextField1 = new JTextField(4);
	JComboBox locoRosterBox1 = new RosterEntryComboBox();
	JButton adrButton1 = new JButton();
	JButton cmdButton1 = new JButton();
	JButton dirButton1 = new JButton();

	// rear loco
	JLabel textLoco2 = new JLabel();
	JTextField locoTextField2 = new JTextField(4);
	JComboBox locoRosterBox2 = new RosterEntryComboBox();
	JButton adrButton2 = new JButton();
	JButton cmdButton2 = new JButton();
	JButton dirButton2 = new JButton();

	// mid loco
	JLabel textLoco3 = new JLabel();
	JTextField locoTextField3 = new JTextField(4);
	JComboBox locoRosterBox3 = new RosterEntryComboBox();
	JButton adrButton3 = new JButton();
	JButton cmdButton3 = new JButton();
	JButton dirButton3 = new JButton();

	// mid loco
	JLabel textLoco4 = new JLabel();
	JTextField locoTextField4 = new JTextField(4);
	JComboBox locoRosterBox4 = new RosterEntryComboBox();
	JButton adrButton4 = new JButton();
	JButton cmdButton4 = new JButton();
	JButton dirButton4 = new JButton();

	// mid loco
	JLabel textLoco5 = new JLabel();
	JTextField locoTextField5 = new JTextField(4);
	JComboBox locoRosterBox5 = new RosterEntryComboBox();
	JButton adrButton5 = new JButton();
	JButton cmdButton5 = new JButton();
	JButton dirButton5 = new JButton();

	// mid loco
	JLabel textLoco6 = new JLabel();
	JTextField locoTextField6 = new JTextField(4);
	JComboBox locoRosterBox6 = new RosterEntryComboBox();
	JButton adrButton6 = new JButton();
	JButton cmdButton6 = new JButton();
	JButton dirButton6 = new JButton();
	
	private NceTrafficController tc = null;

	public NceConsistEditPanel() {
		super();
	}

    public void initContext(Object context) throws Exception {
        if (context instanceof NceSystemConnectionMemo ) {
            try {
				initComponents((NceSystemConnectionMemo) context);
			} catch (Exception e) {
				log.error("NceConsistEdit initContext failed");
			}
        }
    }

    public String getHelpTarget() { return "package.jmri.jmrix.nce.consist.NceConsistEditFrame"; }

    public String getTitle() { 
    	StringBuilder x = new StringBuilder();
    	if (memo != null) {
    		x.append(memo.getUserName());
    	} else {
    		x.append("NCE_");
    	}
		x.append(": ");
    	x.append(rb.getString("NceConsistEditTitle"));
        return x.toString(); 
    }
    
    public List<JMenu> getMenus() {
		// build menu
		JMenu toolMenu = new JMenu("Tools");
		toolMenu.add(new NceConsistRosterMenu("Roster", jmri.jmrit.roster.swing.RosterMenu.MAINMENU, this));
		List<JMenu> l = new ArrayList<JMenu>();
		l.add(toolMenu);
		return l;
    }
    
    public void initComponents(NceSystemConnectionMemo m) throws Exception {
    	this.memo = m;
    	this.tc = m.getNceTrafficController();
		// the following code sets the frame's initial state

		textConsist.setText(rb.getString("L_Consist"));
		textConsist.setVisible(true);

		textStatus.setText(rb.getString("L_Status"));
		textStatus.setVisible(true);

		consistStatus.setText(rb.getString("UNKNOWN"));
		consistStatus.setVisible(true);

		previousButton.setText(rb.getString("KeyPREVIOUS"));
		previousButton.setVisible(true);
		previousButton.setToolTipText(rb.getString("ToolTipPrevious"));

		nextButton.setText(rb.getString("KeyNEXT"));
		nextButton.setVisible(true);
		nextButton.setToolTipText(rb.getString("ToolTipNext"));

		getButton.setText(rb.getString("KeyGET"));
		getButton.setVisible(true);
		getButton.setToolTipText(rb.getString("ToolTipGet"));

		consistTextField.setText(Integer.toString(CONSIST_MAX));
		consistTextField.setToolTipText(rb.getString("ToolTipConsist"));
		consistTextField.setMaximumSize(new Dimension(consistTextField
				.getMaximumSize().width,
				consistTextField.getPreferredSize().height));

		textLocomotive.setText(rb.getString("L_Loco"));
		textLocomotive.setVisible(true);
		textRoster.setText(rb.getString("L_Roster"));
		textRoster.setVisible(true);
		textAddress.setText(rb.getString("L_Address"));
		textAddress.setVisible(true);
		textAddrType.setText(rb.getString("L_Type"));
		textAddrType.setVisible(true);
		textDirection.setText(rb.getString("L_Direction"));
		textDirection.setVisible(true);

		textConRoster.setText(rb.getString("L_Consist"));
		textConRoster.setVisible(true);
		textConRoadName.setText("");
		textConRoadName.setVisible(true);
		textConRoadNumber.setText("");
		textConRoadNumber.setVisible(true);
		textConModel.setText("");
		textConModel.setVisible(true);
		
		throttleButton.setText(rb.getString("L_Throttle"));
		throttleButton.setVisible(true);
		throttleButton.setEnabled(true);
		throttleButton.setToolTipText(rb.getString("ToolTipThrottle"));

		clearCancelButton.setText(rb.getString("KeyCLEAR"));
		clearCancelButton.setVisible(true);
		clearCancelButton.setEnabled(false);
		clearCancelButton.setToolTipText(rb.getString("ToolTipClear"));

		saveLoadButton.setText(rb.getString("KeySAVE"));
		saveLoadButton.setVisible(false);
		saveLoadButton.setEnabled(false);
		saveLoadButton.setToolTipText(rb.getString("ToolTipSave"));

		deleteButton.setText(rb.getString("KeyDELETE"));
		deleteButton.setVisible(false);
		deleteButton.setEnabled(false);
		deleteButton.setToolTipText(rb.getString("ToolTipDelete"));

		backUpButton.setText(rb.getString("KeyBACKUP"));
		backUpButton.setVisible(true);
		backUpButton.setToolTipText(rb.getString("ToolTipBackup"));

		restoreButton.setText(rb.getString("KeyRESTORE"));
		restoreButton.setVisible(true);
		restoreButton.setToolTipText(rb.getString("ToolTipRestore"));

		checkBoxEmpty.setText(rb.getString("KeyEMPTY"));
		checkBoxEmpty.setVisible(true);
		checkBoxEmpty.setToolTipText(rb.getString("ToolTipEmpty"));

		checkBoxVerify.setText(rb.getString("KeyVERIFY"));
		checkBoxVerify.setVisible(true);
		checkBoxVerify.setSelected(true);
		checkBoxVerify.setToolTipText(rb.getString("ToolTipVerify"));

		checkBoxConsist.setText(rb.getString("KeyCONSIST"));
		checkBoxConsist.setVisible(true);
		checkBoxConsist.setSelected(true);
		checkBoxConsist.setToolTipText(rb.getString("ToolTipConsistCkBox"));

		initLocoFields();

		setLayout(new GridBagLayout());

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
		addItem(space3a, 1, 3);
		addItem(space3b, 2, 3);
		addItem(space3c, 3, 3);
		addItem(space3d, 4, 3);
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
		addItem(space15, 2, 15);
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
			boolean isLong = (adrButton1.getText() == rb.getString("KeyLONG"));
			if (locoAddr < 0)
				return;
			consistNum = validConsist(consistTextField.getText());
			jmri.jmrit.throttle.ThrottleFrame tf=
				jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleFrame();
			tf.getAddressPanel().setAddress(consistNum, false);	// use consist address
			if (JOptionPane.showConfirmDialog(null,
					rb.getString("DIALOG_Funct2Lead"), rb.getString("DIALOG_NceThrottle"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				tf.getAddressPanel().setAddress(locoAddr, isLong); 	// use lead loco address
			}			
			tf.toFront();
			return;
		}
		// clear or cancel button
		if (ae.getSource() == clearCancelButton) {
			// button can be Clear or Cancel
			if (clearCancelButton.getText().equals(rb.getString("KeyCLEAR"))) {
				updateRoster(rb.getString("CLEARED"));
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
				consistStatus.setText(rb.getString("OKAY"));
			} else {
				consistStatus.setText(rb.getString("ERROR"));
				saveLoadButton.setEnabled(false);
				return;
			}
			enableAllLocoRows(false);
			if (saveLoadButton.getText().equals(rb.getString("KeyLOAD"))){ 
				loadShift (); // get rid of empty mids!
				updateRoster(consistTextField.getText());
				consistNum = validConsist(consistTextField.getText());
				// load right away or verify?
				if(!verifyAllLocoAddr())
					fullLoad();
			}else if (updateRoster(consistTextField.getText())){
				saveLoadButton.setEnabled(false);
				consistNum = getConsist(); // reload panel
			}
			return;
		}

		// delete button
		if (ae.getSource() == deleteButton) {
			if (JOptionPane.showConfirmDialog(null,
					rb.getString("DIALOG_ConfirmDel1") + " "
					+ conRosterBox.getSelectedItem().toString()
					+ " " + rb.getString("DIALOG_ConfirmDel2"), rb.getString("DIALOG_NceDelete"),
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
			Thread mb = new NceConsistBackup(tc);
			mb.setName("Consist Backup");
			mb.start();
		}
		if (ae.getSource() == restoreButton) {
			Thread mr = new NceConsistRestore(tc);
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
		if (locoTextField.getText().equals("")) {
			JOptionPane.showMessageDialog(this,
					rb.getString("DIALOG_EnterLocoB4AddrChg"),
					rb.getString("DIALOG_NceConsist"), JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			if (adrButton.getText().equals(rb.getString("KeyLONG"))) {
				if ( (Integer.parseInt(locoTextField.getText()) < 128) &&
					(Integer.parseInt(locoTextField.getText()) > 0) ) {
						adrButton.setText(rb.getString("KeySHORT"));
				}
			} else {
				adrButton.setText(rb.getString("KeyLONG"));
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
		if (locoTextField.getText().equals("")) {
			JOptionPane.showMessageDialog(this,
					rb.getString("DIALOG_EnterLocoB4DirChg"),
					rb.getString("DIALOG_NceConsist"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		cmdButton.setEnabled(true);
		if (dirButton.getText().equals(rb.getString("KeyFWD"))) {
			dirButton.setText(rb.getString("KeyREV"));
		} else {
			dirButton.setText(rb.getString("KeyFWD"));
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
		RosterEntry entry = null;
		Object o = locoRosterBox.getSelectedItem();
		if (o.getClass().equals(RosterEntry.class))
			entry = (RosterEntry)o;
		if (entry != null){
			DccLocoAddress a = entry.getDccLocoAddress();
			if (a != null) {
				locoTextField.setText("" + a.getNumber());
				if (a.isLongAddress())
					adrButton.setText(rb.getString("KeyLONG"));
				else
					adrButton.setText(rb.getString("KeySHORT"));
				// if lead loco get road number and name
				if (locoRosterBox == locoRosterBox1){
					textConRoadName.setText(entry.getRoadName());
					textConRoadNumber.setText(entry.getRoadNumber());
					textConModel.setText(entry.getModel());
				}
			}
		}
	}

	// load a consist from roster
	public void consistRosterSelected(java.awt.event.ActionEvent ae) {
		if (consistSearchNext || consistSearchPrevious || locoSearch)
			return;
		String entry = "";
		try {
			entry = conRosterBox.getSelectedItem().toString();
		} catch (Exception e) {

		}
		log.debug("load consist " + entry + " from roster ");
		if (entry.equals("")) {
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
			consistStatus.setText(rb.getString("SEARCH"));
		} else {
			consistStatus.setText(rb.getString("WAIT"));
			if (cN == consistNum) {
				newConsist = false;
			}
		}

		// if busy don't request
		if (waiting > 0)
			return cN;

		if (consistSearchNext) {
			readConsistMemory(cN - 1, LEAD);	// decrement consist number
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
			consistStatus.setText(rb.getString("ERROR"));
			JOptionPane.showMessageDialog(this,
					rb.getString("ToolTipConsist"), rb.getString("DIALOG_NceConsist"),
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
			JOptionPane.showMessageDialog(this,
					rb.getString("DIALOG_AddrRange"), rb.getString("DIALOG_NceConsist"),
					JOptionPane.ERROR_MESSAGE);
			return -1;
		} else
			return lA;
	}

	// check to see if user modified consist number
	private boolean consistChanged() {
		if (consistNum != validConsist(consistTextField.getText())) {
			JOptionPane.showMessageDialog(this,
					rb.getString("DIALOG_PressRead"), rb.getString("DIALOG_NceConsist"),
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
		int nceMemAddr = (consistNum * 2) + NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM;
		if (eNum == REAR)
			nceMemAddr = (consistNum * 2) + NceCmdStationMemory.CabMemorySerial.CS_CON_MEM_REAR;
		if (eNum == MID) 
			nceMemAddr = (consistNum * 8) + NceCmdStationMemory.CabMemorySerial.CS_CON_MEM_MID;
		byte[] bl = NceBinaryCommand.accMemoryRead(nceMemAddr);
		sendNceMessage(bl, NceMessage.REPLY_16);
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
			if (cre.getConsistNumber().equals(rb.getString("CLEARED")) || cre.getConsistNumber().equals("0")){
				log.debug("search for empty consist");
				consistTextField.setText(Integer.toString(emptyConsistSearchStart));
				emptyConsistSearch = true;
				consistSearchNext = true;
				consistNum = getConsist();
				loadFullRoster (cre);
				saveLoadButton.setEnabled(false);
			} else {
				log.error("roster consist number is out of range: " + consistNum);
				consistStatus.setText(rb.getString("ERROR"));
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
		adrButton1.setText(cre.isLoco1LongAddress() ? rb.getString("KeyLONG") : rb.getString("KeySHORT"));
		dirButton1.setText(convertDTD(cre.getLoco1Direction()));
		locoRosterBox1.setEnabled(true);
		locoTextField1.setEnabled(true);
		adrButton1.setEnabled(true);
		dirButton1.setEnabled(true);

		// load rear loco
		locoTextField2.setText(cre.getLoco2DccAddress());
		adrButton2.setText(cre.isLoco2LongAddress() ? rb.getString("KeyLONG") : rb.getString("KeySHORT"));
		dirButton2.setText(convertDTD(cre.getLoco2Direction()));
		locoRosterBox2.setEnabled(true);
		locoTextField2.setEnabled(true);
		adrButton2.setEnabled(true);
		dirButton2.setEnabled(true);

		// load Mid1 loco
		locoTextField3.setText(cre.getLoco3DccAddress());
		adrButton3.setText(cre.isLoco3LongAddress() ? rb.getString("KeyLONG") : rb.getString("KeySHORT"));
		dirButton3.setText(convertDTD(cre.getLoco3Direction()));
		locoRosterBox3.setEnabled(true);
		locoTextField3.setEnabled(true);
		adrButton3.setEnabled(true);
		dirButton3.setEnabled(true);

		// load Mid2 loco
		locoTextField4.setText(cre.getLoco4DccAddress());
		adrButton4.setText(cre.isLoco4LongAddress() ? rb.getString("KeyLONG") : rb.getString("KeySHORT"));
		dirButton4.setText(convertDTD(cre.getLoco4Direction()));
		locoRosterBox4.setEnabled(true);
		locoTextField4.setEnabled(true);
		adrButton4.setEnabled(true);
		dirButton4.setEnabled(true);

		// load Mid3 loco
		locoTextField5.setText(cre.getLoco5DccAddress());
		adrButton5.setText(cre.isLoco5LongAddress() ? rb.getString("KeyLONG") : rb.getString("KeySHORT"));
		dirButton5.setText(convertDTD(cre.getLoco5Direction()));
		locoRosterBox5.setEnabled(true);
		locoTextField5.setEnabled(true);
		adrButton5.setEnabled(true);
		dirButton5.setEnabled(true);

		// load Mid4 loco
		locoTextField6.setText(cre.getLoco6DccAddress());
		adrButton6.setText(cre.isLoco6LongAddress() ? rb.getString("KeyLONG") : rb.getString("KeySHORT"));
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
			consistStatus.setText(rb.getString("UNKNOWN"));
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
		consistStatus.setText(rb.getString("MODIFIED"));
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
		if (id.equals("")) {
			log.debug("Attempt to modify consist without valid id");
			return false;
		}
		// need rear loco to form a consist
		if (locoTextField2.getText().equals(""))
			return false;
		NceConsistRosterEntry cre;
		consistList = NceConsistRoster.instance().matchingList(null, null,
				null, null, null, null, null, null, null, id);
		// if consist doesn't exist in roster ask user if they want to create one
		if (consistList.isEmpty()) {
			if (JOptionPane.showConfirmDialog(null, rb.getString("DIALOG_ConfirmAdd1") + " " + id
					+ " " + rb.getString("DIALOG_ConfirmAdd2"), rb.getString("DIALOG_NceSave"),
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
				if (JOptionPane.showConfirmDialog(null, rb.getString("DIALOG_ConfirmUpd1") + " " + id
						+ " " + rb.getString("DIALOG_ConfirmUpd2") + getRosterText(cre),
						rb.getString("DIALOG_NceUpdate"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					// update consist if command was to clear
					if (consistNumber.equals(rb.getString("CLEARED"))){
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
		cre.setLoco1LongAddress(adrButton1.getText().equals(rb.getString("KeyLONG")));
		cre.setLoco1Direction(directionDTD(dirButton1));
		// save rear loco
		cre.setLoco2DccAddress(locoTextField2.getText());
		cre.setLoco2LongAddress(adrButton2.getText().equals(rb.getString("KeyLONG")));
		cre.setLoco2Direction(directionDTD(dirButton2));
		// save Mid1 loco
		cre.setLoco3DccAddress(locoTextField3.getText());
		cre.setLoco3LongAddress(adrButton3.getText().equals(rb.getString("KeyLONG")));
		cre.setLoco3Direction(directionDTD(dirButton3));
		// save Mid2 loco
		cre.setLoco4DccAddress(locoTextField4.getText());
		cre.setLoco4LongAddress(adrButton4.getText().equals(rb.getString("KeyLONG")));
		cre.setLoco4Direction(directionDTD(dirButton4));
		// save Mid3 loco
		cre.setLoco5DccAddress(locoTextField5.getText());
		cre.setLoco5LongAddress(adrButton5.getText().equals(rb.getString("KeyLONG")));
		cre.setLoco5Direction(directionDTD(dirButton5));
		// save Mid4 loco
		cre.setLoco6DccAddress(locoTextField6.getText());
		cre.setLoco6LongAddress(adrButton6.getText().equals(rb.getString("KeyLONG")));
		cre.setLoco6Direction(directionDTD(dirButton6));

		writeRosterFile();
		return true;
	}
	
	/**
	 * @return DTD direction format based on the loco direction button
	 */
	private String directionDTD(JButton dirButton) {
		String formatDTD = rb.getString("DTD_UNKNOWN");
		if (dirButton.getText().equals(rb.getString("KeyFWD")))
			formatDTD = rb.getString("DTD_NORMAL");
		if (dirButton.getText().equals(rb.getString("KeyREV")))
			formatDTD = rb.getString("DTD_REVERSE");
		return formatDTD;
	}
	/**
	 * @return converts DTD direction to FWD, REV, and ??
	 */
	private String convertDTD(String formatDTD) {
		String word = rb.getString("KeyQUESTION");
		if (formatDTD.equals(rb.getString("DTD_NORMAL")))
			word = rb.getString("KeyFWD");
		if (formatDTD.equals(rb.getString("DTD_REVERSE")))
			word = rb.getString("KeyREV");
		return word;
	}
	
	/**
	 * @return converts DTD direction to FWD, REV, and ""
	 */
	private String shortHandConvertDTD(String formatDTD) {
		String word = "";
		if (formatDTD.equals(rb.getString("DTD_NORMAL")))
			word = rb.getString("KeyFWD");
		if (formatDTD.equals(rb.getString("DTD_REVERSE")))
			word = rb.getString("KeyREV");
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
		conRosterBox.insertItemAt("", 0);
		conRosterBox.setSelectedIndex(0);
		conRosterBox.addActionListener(consistRosterListener);
	}

	// can the consist be loading into NCE memory?
	private boolean canLoad() {
		if (locoTextField1.getText().equals(""))
			return false;
		if (dirButton1.getText().equals(rb.getString("KeyQUESTION")))
			return false;
		if (locoTextField2.getText().equals(""))
			return false;
		if (dirButton2.getText().equals(rb.getString("KeyQUESTION")))
			return false;
		if (!locoTextField3.getText().equals("")
				&& dirButton3.getText().equals(rb.getString("KeyQUESTION")))
			return false;
		if (!locoTextField4.getText().equals("")
				&& dirButton4.getText().equals(rb.getString("KeyQUESTION")))
			return false;
		if (!locoTextField5.getText().equals("")
				&& dirButton5.getText().equals(rb.getString("KeyQUESTION")))
			return false;
		if (!locoTextField6.getText().equals("")
				&& dirButton6.getText().equals(rb.getString("KeyQUESTION")))
			return false;
		// okay to load, clean up empty loco fields
		if (locoTextField3.getText().equals(""))
			dirButton3.setText(rb.getString("KeyQUESTION"));
		if (locoTextField4.getText().equals(""))
			dirButton4.setText(rb.getString("KeyQUESTION"));
		if (locoTextField5.getText().equals(""))
			dirButton5.setText(rb.getString("KeyQUESTION"));
		if (locoTextField6.getText().equals(""))
			dirButton6.setText(rb.getString("KeyQUESTION"));
		if (saveLoadButton.getText().equals(rb.getString("KeyLOAD")))
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
		if (locoTextFieldLow.getText().equals("") && !locoTextFieldHigh.getText().equals((""))){
			locoTextFieldLow.setText(locoTextFieldHigh.getText());
			adrButtonLow.setText(adrButtonHigh.getText());
			dirButtonLow.setText(dirButtonHigh.getText());
			dirButtonHigh.setText(rb.getString("KeyQUESTION"));
			locoTextFieldHigh.setText("");
		} else {
			return;
		}
	}

	// change button operation during load consist from roster
	private void changeButtons(boolean rosterDisplay) {
		if (rosterDisplay) {
			clearCancelButton.setText(rb.getString("KeyCANCEL"));
			clearCancelButton.setToolTipText(rb.getString("ToolTipCancel"));
			clearCancelButton.setEnabled(true);
			saveLoadButton.setText(rb.getString("KeyLOAD"));
			saveLoadButton.setToolTipText(rb.getString("ToolTipLoad"));
		} else {
			clearCancelButton.setText(rb.getString("KeyCLEAR"));
			clearCancelButton.setToolTipText(rb.getString("ToolTipClear"));
			saveLoadButton.setText(rb.getString("KeySAVE"));
			saveLoadButton.setToolTipText(rb.getString("ToolTipSave"));
			clearCancelButton.setEnabled(!locoTextField1.getText().equals(""));
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
		sendNceMessage(bl, NceMessage.REPLY_1);
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
			consistStatus.setText(rb.getString("ERROR"));
			log.error("reply length error, expecting: " + replyLen + " got: "
					+ r.getNumDataElements());
			return;
		}

		// response to commands
		if (replyLen == NceMessage.REPLY_1) {
			// Looking for proper response
			int recChar = r.getElement(0);
			log.debug ("command reply: " + recChar);
			if (recChar == '!') {
				if (locoSearch && waiting == 0) {
					readConsistMemory(consistNumVerify, LEAD);
					consistStatus.setText(rb.getString("VERIFY"));
					return;
				}
				if (refresh && waiting == 0) {
					refresh = false;
					// update panel
					readConsistMemory(consistNum, LEAD);
					return;
				}
				consistStatus.setText(rb.getString("OKAY"));
			} else {
				consistStatus.setText(rb.getString("ERROR"));
			}
			return;
		}

		// Consist memory read
		if (replyLen == NceMessage.REPLY_16) {
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
								consistStatus.setText(rb.getString("ERROR"));
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
						consistStatus.setText(rb.getString("OKAY"));
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
							consistStatus.setText(rb.getString("OKAY"));
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
						consistStatus.setText(rb.getString("NONE"));
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
						consistStatus.setText(rb.getString("NONE"));
						consistSearchPrevious = false;
						return;		// don't update the panel
					}
					consistNum++;
					if (consistNum > CONSIST_MAX)
						consistNum = CONSIST_MIN;
					consistTextField.setText(Integer.toString(consistNum));
					// have we wrapped? if yes, need to read NCE memory
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
				if (clearCancelButton.getText().equals(rb.getString("KeyCLEAR")))
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
				consistStatus.setText(rb.getString("OKAY"));
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
			if (checkBoxConsist.isSelected() && !locoTextField1.getText().equals(""))
				consistStatus.setText(rb.getString("UNKNOWN"));
			else
			textConRoadName.setText("");
			textConRoadNumber.setText("");	
			textConModel.setText("");	
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
				textConRoadName.setText("");
				textConRoadNumber.setText("");	
				textConModel.setText("");	
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

		if (locoAddrText.equals("") || locoAddrText.equals(rb.getString("REPLACE_LOCO"))) {
			locoRosterBox.setEnabled(true);
			locoTextField.setEnabled(true);
			cmdButton.setText(rb.getString("KeyADD"));
			cmdButton.setVisible(true);
			cmdButton.setEnabled(false);
			cmdButton.setToolTipText(rb.getString("ToolTipAdd"));
			dirButton.setText(rb.getString("KeyQUESTION"));
			dirButton.setEnabled(true);
			adrButton.setText(rb.getString("KeyLONG"));
			adrButton.setEnabled(true);
			return false;
		} else {
			locoTextField.setText(locoAddrText);
			locoRosterBox.setEnabled(false);
			locoTextField.setEnabled(false);
			cmdButton.setEnabled(true);
			dirButton.setText(locoDirection);
			dirButton.setEnabled(false);
			adrButton.setText((locoType) ? rb.getString("KeyLONG") : rb.getString("KeySHORT"));
			adrButton.setEnabled(false);
			
			// can not delete lead or rear locos, but can replace
			if (locoTextField == locoTextField1 || locoTextField == locoTextField2) {
				cmdButton.setText(rb.getString("KeyREPLACE"));
				cmdButton.setToolTipText("Press to delete and replace this loco");
			} else {
				cmdButton.setText(rb.getString("KeyDELETE"));
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
		if (locoTextField.getText().equals("")) {
			JOptionPane.showMessageDialog(this,
					rb.getString("DIALOG_EnterLocoB4Add"), rb.getString("DIALOG_NceConsist"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// set reflesh flag to update panel
		refresh = true;
		int locoAddr = getLocoAddr (locoTextField, adrButton);

		if (cmdButton.getText().equals(rb.getString("KeyDELETE"))) {
			sendNceBinaryCommand(locoAddr,
					NceBinaryCommand.LOCO_CMD_DELETE_LOCO_CONSIST, (byte) 0);

		} else if (cmdButton.getText().equals(rb.getString("KeyREPLACE"))) {

			// Kill refresh flag, no update when replacing loco
			refresh = false;

			// allow user to add loco to lead or rear consist

			locoRosterBox.setEnabled(true);
			locoTextField.setText("");
			locoTextField.setEnabled(true);
			adrButton.setText(rb.getString("KeyLONG"));
			adrButton.setEnabled(true);
			dirButton.setText(rb.getString("KeyQUESTION"));
			dirButton.setEnabled(true);
			cmdButton.setText(rb.getString("KeyADD"));
			cmdButton.setToolTipText(rb.getString("ToolTipAdd"));

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
			if (dirButton.getText().equals(rb.getString("KeyQUESTION"))) {
				JOptionPane.showMessageDialog(this,
						rb.getString("DIALOG_SetDirB4Consist"),
						rb.getString("DIALOG_NceConsist"), JOptionPane.ERROR_MESSAGE);

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
				if (dirButton.getText().equals(rb.getString("KeyFWD"))) {
					sendNceBinaryCommand(locoAddr,
							NceBinaryCommand.LOCO_CMD_FWD_CONSIST_LEAD, cN);
				}
				if (dirButton.getText().equals(rb.getString("KeyREV"))) {
					sendNceBinaryCommand(locoAddr,
							NceBinaryCommand.LOCO_CMD_REV_CONSIST_LEAD, cN);
				}
			// rear loco?
			} else if (locoTextField == locoTextField2) {
				if (dirButton.getText().equals(rb.getString("KeyFWD"))) {
					sendNceBinaryCommand(locoAddr,
							NceBinaryCommand.LOCO_CMD_FWD_CONSIST_REAR, cN);
				}
				if (dirButton.getText().equals(rb.getString("KeyREV"))) {
					sendNceBinaryCommand(locoAddr,
							NceBinaryCommand.LOCO_CMD_REV_CONSIST_REAR, cN);
				}
			// must be mid loco
			} else {
				// wait for verify to complete before updating mid loco
				if (locoSearch) {
					if (dirButton.getText().equals(rb.getString("KeyFWD")))
						verifyType = VERIFY_MID_FWD;
					else
						verifyType = VERIFY_MID_REV;
				// no verify, just load and go!
				} else {
					if (dirButton.getText().equals(rb.getString("KeyFWD"))) {
						sendNceBinaryCommand(locoAddr,
								NceBinaryCommand.LOCO_CMD_FWD_CONSIST_MID, cN);
					}
					if (dirButton.getText().equals(rb.getString("KeyREV"))) {
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
		if (locoTextField.getText().equals(""))
			return;
		if (validLocoAdr(locoTextField.getText()) < 0)
			return;
		byte cN = (byte) validConsist(consistTextField.getText());
		if (cN < 0)
			return;

		int locoAddr = getLocoAddr (locoTextField, adrButton);

		// ADD loco to consist
		if (dirButton.getText().equals(rb.getString("KeyQUESTION"))) {
			JOptionPane.showMessageDialog(this,
					rb.getString("DIALOG_SetDirB4Consist"), rb.getString("DIALOG_NceConsist"),
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
			if (dirButton.getText().equals(rb.getString("KeyFWD"))) {
				sendNceBinaryCommand(locoAddr,
						NceBinaryCommand.LOCO_CMD_FWD_CONSIST_LEAD, cN);
			}
			if (dirButton.getText().equals(rb.getString("KeyREV"))) {
				sendNceBinaryCommand(locoAddr,
						NceBinaryCommand.LOCO_CMD_REV_CONSIST_LEAD, cN);
			}
			// rear loco?
		} else if (locoTextField == locoTextField2) {
			if (dirButton.getText().equals(rb.getString("KeyFWD"))) {
				sendNceBinaryCommand(locoAddr,
						NceBinaryCommand.LOCO_CMD_FWD_CONSIST_REAR, cN);
			}
			if (dirButton.getText().equals(rb.getString("KeyREV"))) {
				sendNceBinaryCommand(locoAddr,
						NceBinaryCommand.LOCO_CMD_REV_CONSIST_REAR, cN);
			}
			// must be mid loco
		} else {
			if (dirButton.getText().equals(rb.getString("KeyFWD"))) {
				sendNceBinaryCommand(locoAddr,
						NceBinaryCommand.LOCO_CMD_FWD_CONSIST_MID, cN);
			}
			if (dirButton.getText().equals(rb.getString("KeyREV"))) {
				sendNceBinaryCommand(locoAddr,
						NceBinaryCommand.LOCO_CMD_REV_CONSIST_MID, cN);
			}
		}
	}
	
	private int getLocoAddr (JTextField locoTextField, JButton adrButton){
		int locoAddr = Integer.parseInt(locoTextField.getText());
		if (locoAddr >= 128)
			locoAddr += 0xC000;
		else if (adrButton.getText().equals(rb.getString("KeyLONG")))
			locoAddr += 0xC000;
		return locoAddr;
	}

	private void sendNceMessage(byte[] b, int replyLength) {
		NceMessage m = NceMessage.createBinaryMessage(tc, b, replyLength);
		waiting++;
		replyLen = replyLength; // Expect n byte response
		tc.sendNceMessage(m, this);
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
		int rC_u = r.getElement(i++);
		int rC = (rC_u << 8) & 0x3F00;
		int rC_l = r.getElement(i);
		rC = rC + (rC_l & 0xFF);
		String locoAddrText = "";
		if ( (rC_u != 0) || (rC_l != 0) ) 
			locoAddrText = Integer.toString(rC);
		if (rC == LOC_ADR_REPLACE) 
			locoAddrText = rb.getString("REPLACE_LOCO");
		return locoAddrText;
	}

	private String getLocoDirection(JButton dirButton) {
		if (newConsist)
			return rb.getString("KeyQUESTION");
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
			if (!locoTextField1.getText().equals("") && validLocoAdr(locoTextField1.getText()) > 0)
				locoVerifyList[i++] = getLocoAddr (locoTextField1, adrButton1);
			if (!locoTextField2.getText().equals("") && validLocoAdr(locoTextField2.getText()) > 0)
				locoVerifyList[i++] = getLocoAddr (locoTextField2, adrButton2);
			if (!locoTextField3.getText().equals("") && validLocoAdr(locoTextField3.getText()) > 0)
				locoVerifyList[i++] = getLocoAddr (locoTextField3, adrButton3);
			if (!locoTextField4.getText().equals("") && validLocoAdr(locoTextField4.getText()) > 0)
				locoVerifyList[i++] = getLocoAddr (locoTextField4, adrButton4);
			if (!locoTextField5.getText().equals("") && validLocoAdr(locoTextField5.getText()) > 0)
				locoVerifyList[i++] = getLocoAddr (locoTextField5, adrButton5);
			if (!locoTextField6.getText().equals("") && validLocoAdr(locoTextField6.getText()) > 0)
				locoVerifyList[i++] = getLocoAddr (locoTextField6, adrButton6);
			else
				locoVerifyList[i] = 0;
			locoSearch = true;
			consistNumVerify = 0;
			consistStatus.setText(rb.getString("VERIFY"));
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
		add(c, gc);
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

		adrButton.setText(rb.getString("KeyLONG"));
		adrButton.setVisible(true);
		adrButton.setEnabled(false);
		adrButton.setToolTipText("Press to change address type");
		adrButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionAdrPerformed(e);
			}
		});

		locoRosterBox.setVisible(true);
		locoRosterBox.setEnabled(false);
		locoRosterBox.setToolTipText("Select loco from roster");
		locoRosterBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				locoSelected(e);
			}
		});

		dirButton.setText(rb.getString("KeyQUESTION"));
		dirButton.setVisible(true);
		dirButton.setEnabled(false);
		dirButton.setToolTipText("Press to change loco direction");
		dirButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionDirPerformed(e);
			}
		});

		cmdButton.setText(rb.getString("KeyADD"));
		cmdButton.setVisible(true);
		cmdButton.setEnabled(false);
		cmdButton.setToolTipText(rb.getString("ToolTipAdd"));
		cmdButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionCmdPerformed(e);
			}
		});

		locoTextField.setText("");
		locoTextField.setEnabled(false);
		locoTextField.setToolTipText("Enter loco address");
		locoTextField.setMaximumSize(new Dimension(
				locoTextField.getMaximumSize().width, locoTextField
						.getPreferredSize().height));
	}

	ActionListener consistRosterListener;

	private void initConsistRoster(JComboBox conRosterBox) {
		conRosterBox.insertItemAt("", 0);
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
			JOptionPane.showMessageDialog(this, rb.getString("DIALOG_LocoInUse1") + " "
					+ locoNumInUse + " " + rb.getString("DIALOG_LocoInUse2") + " " + consistNumVerify,
					rb.getString("DIALOG_NceConsist"), JOptionPane.ERROR_MESSAGE);
			break;

		case ERROR_NO_EMPTY_CONSIST:
			JOptionPane.showMessageDialog(this,
					rb.getString("DIALOG_NoEmptyConsist"),
					rb.getString("DIALOG_NceConsist"), JOptionPane.ERROR_MESSAGE);
			break;
			
		case ERROR_CONSIST_DOESNT_MATCH:
			if (JOptionPane.showConfirmDialog(null,
					rb.getString("DIALOG_RosterNotMatch") + " "
					+ getRosterText(cre),
					rb.getString("DIALOG_NceContinue"),
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				if (JOptionPane.showConfirmDialog(null,
						rb.getString("DIALOG_RosterNotMatch1") + " " + cre.getId() + 
						" " + rb.getString("DIALOG_RosterNotMatch2") +
						" " + cre.getConsistNumber() +
						"\n " + rb.getString("DIALOG_RosterNotMatch3"),
						rb.getString("DIALOG_NceReset"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					cre.setConsistNumber(rb.getString("CLEARED"));
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
			JOptionPane.showMessageDialog(this,
					rb.getString("DIALOG_ConsistWasLoaded"),
					rb.getString("DIALOG_NceConsist"), JOptionPane.WARNING_MESSAGE);
			break;
		default:
			log.error("Error code out of range");
		}	
		errorCode = 0;
	}
	
	private String getRosterText (NceConsistRosterEntry cre) {
		return "\n" 
		+ "\n" 
		+ rb.getString("ROSTER_ConsistNum") 
		+ " " + cre.getConsistNumber()
		+ "\n"
		+ rb.getString("ROSTER_LeadLoco")
		+ " " + cre.getLoco1DccAddress() 
		+ " " + shortHandConvertDTD(cre.getLoco1Direction())  
		+ "\n"
		+ rb.getString("ROSTER_RearLoco")
		+ " " + cre.getLoco2DccAddress()
		+ " " + shortHandConvertDTD(cre.getLoco2Direction())  
		+ "\n"
		+ rb.getString("ROSTER_Mid1Loco")
		+ " " + cre.getLoco3DccAddress()
		+ " " + shortHandConvertDTD(cre.getLoco3Direction())  
		+ "\n"
		+ rb.getString("ROSTER_Mid2Loco")
		+ " " + cre.getLoco4DccAddress()
		+ " " + shortHandConvertDTD(cre.getLoco4Direction())  
		+ "\n"
		+  rb.getString("ROSTER_Mid3Loco")
		+ " " + cre.getLoco5DccAddress()
		+ " " + shortHandConvertDTD(cre.getLoco5Direction())  
		+ "\n"
		+ rb.getString("ROSTER_Mid4Loco")
		+ " " + cre.getLoco6DccAddress()
		+ " " + shortHandConvertDTD(cre.getLoco6Direction());
	}
    
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.nce.swing.NceNamedPaneAction {


		public Default() {
            super("Open NCE Consist Editor", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                NceConsistEditPanel.class.getName(), 
                jmri.InstanceManager.getDefault(NceSystemConnectionMemo.class));
        }
    }

	static Logger log = LoggerFactory
	.getLogger(NceConsistEditPanel.class.getName());
}
