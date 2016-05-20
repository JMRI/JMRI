// NceMacroEditPanel.java

package jmri.jmrix.nce.macro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jmri.InstanceManager;
import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceCmdStationMemory;
import jmri.jmrix.nce.NceCmdStationMemory.CabMemorySerial;
import jmri.jmrix.nce.NceCmdStationMemory.CabMemoryUsb;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.swing.NcePanelInterface;

/**
 * Frame for user edit of NCE macros
 * 
 * NCE macros are stored in Command Station (CS) memory starting at address
 * xC800. Each macro consists of 20 bytes. The last macro 255 is at address
 * xDBEC.
 * 
 * Macro addr
 * 0	xC800
 * 1	xC814
 * 2	xC828
 * 3	xC83C
 * .      .
 * .      .
 * 255	xDBEC
 * 
 * Each macro can close or throw up to ten accessories.  Macros can also be linked
 * together.  Two bytes (16 bit word) define an accessory address and command, or the
 * address of the next macro to be executed.  If the upper byte of the macro data word
 * is xFF, then the next byte contains the address of the next macro to be executed by
 * the NCE CS.  For example, xFF08 means link to macro 8.  NCE uses the NMRA DCC accessory
 * decoder packet format for the word defination of their macros.
 * 
 * Macro data byte:
 * 
 * bit	     15 14 13 12 11 10  9  8  7  6  5  4  3  2  1  0
 *                                 _     _  _  _
 *  	      1  0  A  A  A  A  A  A  1  A  A  A  C  D  D  D
 * addr bit         7  6  5  4  3  2    10  9  8     1  0  
 * turnout												   T
 * 
 * By convention, MSB address bits 10 - 8 are one's complement.  NCE macros always set the C bit to 1.
 * The LSB "D" (0) determines if the accessory is to be thrown (0) or closed (1).  The next two bits
 * "D D" are the LSBs of the accessory address. Note that NCE display addresses are 1 greater than 
 * NMRA DCC. Note that address bit 2 isn't supposed to be inverted, but it is the way NCE implemented
 * their macros.
 * 
 * Examples:
 * 
 * 81F8 = accessory 1 thrown
 * 9FFC = accessory 123 thrown
 * B5FD = accessory 211 close
 * BF8F = accessory 2044 close
 * 
 * FF10 = link macro 16 
 *
 * Updated for including the USB 7.* for 1.65 command station
 * 
 *               Variables found on cab context page 14 (Cab address 14)
 *
 *              ;macro table
 * MACRO_TBL    ;table of macros, 16 entries of 16 bytes organized as: 
 *              ;        macro 0,  high byte, low byte - 7 more times (8 accy commands total)
 *              ;        macro 1,  high byte, low byte - 7 more times (8 accy commands total)
 *              ;          
 *              ;        macro 16, high byte, low byte - 7 more times (8 accy commands total)
 *               
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @author Ken Cameron Copyright (C) 2013
 * @version $Revision$
 */

public class NceMacroEditPanel extends jmri.jmrix.nce.swing.NcePanel implements NcePanelInterface, jmri.jmrix.nce.NceListener  {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.macro.NceMacroBundle");
	
	private int macroNum = 0;						// macro being worked
	private int replyLen = 0;						// expected byte length
	private int waiting = 0;						// to catch responses not intended for this module
	
	private static final String QUESTION = rb.getString("Add");// The three possible states for a turnout
	private static final String CLOSED = InstanceManager.turnoutManagerInstance().getClosedText();
	private static final String THROWN = InstanceManager.turnoutManagerInstance().getThrownText();	
	private static final String CLOSED_NCE = rb.getString("Normal");
	private static final String THROWN_NCE = rb.getString("Reverse");	
	
	private static final String DELETE = rb.getString("Delete");
	
	private static final String EMPTY = rb.getString("empty");	// One of two accessory states
	private static final String ACCESSORY = rb.getString("accessory");
	
	private static final String LINK = rb.getString("LinkMacro");// Line 10 alternative to Delete

	private boolean macroSearchInc = false;		// next search
	private boolean macroSearchDec = false;		// previous search
	private int macroCount;						// search count not to exceed MAX_MACRO
	private boolean secondRead = false;			// when true, another 16 byte read expected
	private boolean macroValid = false;			// when true, NCE CS has responed to macro read
	private boolean macroModified = false;		// when true, macro has been modified by user
	
	// member declarations
    JLabel textMacro = new JLabel(rb.getString("Macro"));
    JLabel textReply = new JLabel(rb.getString("Reply"));
    JLabel macroReply = new JLabel();
    
    // major buttons
    JButton previousButton = new JButton(rb.getString("Previous"));
    JButton nextButton = new JButton(rb.getString("Next"));
    JButton getButton = new JButton(rb.getString("Get"));
    JButton saveButton = new JButton(rb.getString("Save"));
    JButton backUpButton = new JButton(rb.getString("Backup"));
    JButton restoreButton = new JButton(rb.getString("Restore"));
    
    // check boxes
    JCheckBox checkBoxEmpty = new JCheckBox(rb.getString("EmptyMacro"));
    JCheckBox checkBoxNce = new JCheckBox(rb.getString("NCETurnout"));
    
    // macro text field
    JTextField macroTextField = new JTextField(4);
    
    // for padding out panel
    JLabel space1 = new JLabel("                          ");
    JLabel space2 = new JLabel("                          ");
    JLabel space3 = new JLabel("                          ");
    JLabel space4 = new JLabel("                          ");
    JLabel space5 = new JLabel("                          ");
    JLabel space15 = new JLabel(" ");
    
    // accessory row 1
    JLabel num1 = new JLabel();
    JLabel textAccy1 = new JLabel();
    JTextField accyTextField1 = new JTextField(4);
    JButton cmdButton1 = new JButton();
    JButton deleteButton1 = new JButton();
    
    //  accessory row 2
    JLabel num2 = new JLabel();
    JLabel textAccy2 = new JLabel();
    JTextField accyTextField2 = new JTextField(4);
    JButton cmdButton2 = new JButton();
    JButton deleteButton2 = new JButton();
    
    //  accessory row 3
    JLabel num3 = new JLabel();
    JLabel textAccy3 = new JLabel();
    JTextField accyTextField3 = new JTextField(4);
    JButton cmdButton3 = new JButton();
    JButton deleteButton3 = new JButton();	
    
    //  accessory row 4
    JLabel num4 = new JLabel();
    JLabel textAccy4 = new JLabel();
    JTextField accyTextField4 = new JTextField(4);
    JButton cmdButton4 = new JButton();
    JButton deleteButton4 = new JButton();	
    
    //  accessory row 5
    JLabel num5 = new JLabel();
    JLabel textAccy5 = new JLabel();
    JTextField accyTextField5 = new JTextField(4);
    JButton cmdButton5 = new JButton();
    JButton deleteButton5 = new JButton();	
    
    //  accessory row 6
    JLabel num6 = new JLabel();
    JLabel textAccy6 = new JLabel();
    JTextField accyTextField6 = new JTextField(4);
    JButton cmdButton6 = new JButton();
    JButton deleteButton6 = new JButton();	
    
    //  accessory row 7
    JLabel num7 = new JLabel();
    JLabel textAccy7 = new JLabel();
    JTextField accyTextField7 = new JTextField(4);
    JButton cmdButton7 = new JButton();
    JButton deleteButton7 = new JButton();	
    
    //  accessory row 8
    JLabel num8 = new JLabel();
    JLabel textAccy8 = new JLabel();
    JTextField accyTextField8 = new JTextField(4);
    JButton cmdButton8 = new JButton();
    JButton deleteButton8 = new JButton();	
    
    //  accessory row 9
    JLabel num9 = new JLabel();
    JLabel textAccy9 = new JLabel();
    JTextField accyTextField9 = new JTextField(4);
    JButton cmdButton9 = new JButton();
    JButton deleteButton9 = new JButton();	
    
    //  accessory row 10
    JLabel num10 = new JLabel();
    JLabel textAccy10 = new JLabel();
    JTextField accyTextField10 = new JTextField(4);
    JButton cmdButton10 = new JButton();
    JButton deleteButton10 = new JButton();	
    
    private NceTrafficController tc = null;
    private int maxNumMacros = 0;
    private int macroSize = 0;
    private boolean isUsb = false;
    
    public NceMacroEditPanel() {
        super();
    }
    
    public void initContext(Object context) throws Exception{
        if (context instanceof NceSystemConnectionMemo ) {
            initComponents((NceSystemConnectionMemo) context);
        }
    }
    
    public String getHelpTarget() { return "package.jmri.jmrix.nce.macro.NceMacroEditFrame"; }

    public String getTitle() { 
    	StringBuilder x = new StringBuilder();
    	if (memo != null) {
    		x.append(memo.getUserName());
    	} else {
    		x.append("NCE_");
    	}
		x.append(": ");
    	x.append(rb.getString("TitleEditNCEMacro"));
        return x.toString(); 
    }

    public void initComponents(NceSystemConnectionMemo memo) throws Exception {
    	this.memo = memo;
        this.tc = memo.getNceTrafficController();
        maxNumMacros = CabMemorySerial.CS_MAX_MACRO;
        isUsb = false;
        macroSize = CabMemorySerial.CS_MACRO_SIZE;
        if ((tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) &&
        		(tc.getCmdGroups() & NceTrafficController.CMDS_MEM) != 0) {
        	maxNumMacros = CabMemoryUsb.CS_MAX_MACRO;
        	isUsb = true;
            macroSize = CabMemoryUsb.CS_MACRO_SIZE;
        }
        
        // the following code sets the frame's initial state
     	
        // default at startup
        macroReply.setText(rb.getString("unknown"));
        macroTextField.setText("");
        saveButton.setEnabled (false);

        // load tool tips
        previousButton.setToolTipText(rb.getString("toolTipSearchDecrementing"));
        nextButton.setToolTipText(rb.getString("toolTipSearchIncrementing"));
        getButton.setToolTipText(rb.getString("toolTipReadMacro"));
        macroTextField.setToolTipText(rb.getString("toolTipEnterMacro"));
        saveButton.setToolTipText(rb.getString("toolTipUpdateMacro"));
        backUpButton.setToolTipText(rb.getString("toolTipBackUp"));
        restoreButton.setToolTipText(rb.getString("toolTipRestore"));
        checkBoxEmpty.setToolTipText(rb.getString("toolTipSearchEmpty"));
        checkBoxNce.setToolTipText(rb.getString("toolTipUseNce"));

        initAccyFields();
        
        setLayout(new GridBagLayout());
        
        // Layout the panel by rows
        // row 0
        addItem(textMacro, 2,0);
        
        // row 1
        addItem(previousButton, 1,1);
        addItem(macroTextField, 2,1);
        addItem(nextButton, 3,1);
        addItem(checkBoxEmpty, 4,1);
        
        // row 2
        addItem(textReply, 0,2);
        addItem(macroReply, 1,2);
        addItem(getButton, 2,2);
        addItem(checkBoxNce, 4,2);
        
        // row 3 padding for looks
        //addItem(space1, 0,3);
        addItem(space2, 1,3);
        addItem(space3, 2,3);
        addItem(space4, 3,3);
        //addItem(space5, 4,3);
        
        // row 4 RFU
        
        // row 5 accessory 1
        addAccyRow (num1, textAccy1, accyTextField1, cmdButton1, deleteButton1, 5);
          
        // row 6 accessory 2
        addAccyRow (num2, textAccy2, accyTextField2, cmdButton2, deleteButton2, 6);
        
        // row 7 accessory 3
        addAccyRow (num3, textAccy3, accyTextField3, cmdButton3, deleteButton3, 7);
        
        // row 8 accessory 4
        addAccyRow (num4, textAccy4, accyTextField4, cmdButton4, deleteButton4, 8);
        
        // row 9 accessory 5
        addAccyRow (num5, textAccy5, accyTextField5, cmdButton5, deleteButton5, 9);
        
        // row 10 accessory 6
        addAccyRow (num6, textAccy6, accyTextField6, cmdButton6, deleteButton6, 10);
        
        // row 11 accessory 7
        addAccyRow (num7, textAccy7, accyTextField7, cmdButton7, deleteButton7, 11);
        
        // row 12 accessory 8
        addAccyRow (num8, textAccy8, accyTextField8, cmdButton8, deleteButton8, 12);
        
        if (!isUsb) {
            // row 13 accessory 9
            addAccyRow (num9, textAccy9, accyTextField9, cmdButton9, deleteButton9, 13);
            
            // row 14 accessory 10
            addAccyRow (num10, textAccy10, accyTextField10, cmdButton10, deleteButton10, 14);
        }
        
        // row 15 padding for looks
        addItem(space15, 2,15);
        
        // row 16
        addItem(saveButton, 2,16);
        addItem(backUpButton, 3,16);
        addItem(restoreButton, 4,16);
        
        // setup buttons
        addButtonAction(previousButton);
        addButtonAction(nextButton);
        addButtonAction(getButton);
        addButtonAction(saveButton);
        addButtonAction(backUpButton);
        addButtonAction(restoreButton);
        
        // accessory command buttons
        addButtonCmdAction(cmdButton1);
        addButtonCmdAction(cmdButton2);
        addButtonCmdAction(cmdButton3);
        addButtonCmdAction(cmdButton4);
        addButtonCmdAction(cmdButton5);
        addButtonCmdAction(cmdButton6);
        addButtonCmdAction(cmdButton7);
        addButtonCmdAction(cmdButton8);
        addButtonCmdAction(cmdButton9);
        addButtonCmdAction(cmdButton10);
        
        // accessory delete buttons
        addButtonDelAction(deleteButton1);
        addButtonDelAction(deleteButton2);
        addButtonDelAction(deleteButton3);
        addButtonDelAction(deleteButton4);
        addButtonDelAction(deleteButton5);
        addButtonDelAction(deleteButton6);
        addButtonDelAction(deleteButton7);
        addButtonDelAction(deleteButton8);
        addButtonDelAction(deleteButton9);
        addButtonDelAction(deleteButton10);
        
        // NCE checkbox
        addCheckBoxAction(checkBoxNce);

    }
 
    // Previous, Next, Get, Save, Restore & Backup buttons
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
    	
		// if we're searching ignore user 
		if (macroSearchInc || macroSearchDec)
			return;

		if (ae.getSource() == saveButton) {
			boolean status = saveMacro();
			if (status) // was save successful?
				setSaveButton(false); // yes, clear save button
			return;
		}

		if (macroModified) {
			// warn user that macro has been modified
			JOptionPane.showMessageDialog(this,
					rb.getString("MacroModified"), rb.getString("NceMacro"),
					JOptionPane.WARNING_MESSAGE);
			macroModified = false;		// only one warning!!!

		} else {
			
			setSaveButton(false);		// Turn off save button
		
			if (ae.getSource() == previousButton) {
				macroCount = 0; // used to determine if all 256 macros have been
				// read
				macroSearchDec = true;
				macroNum = getMacro();	// check for valid and kick off read process
				if (macroNum == -1) 	// Error user input incorrect
					macroSearchDec = false;
			}
			if (ae.getSource() == nextButton) {
				macroCount = 0; // used to determine if all 256 macros have been
				// read
				macroSearchInc = true;
				macroNum = getMacro();	// check for valid and kick off read process
				if (macroNum == -1) 	// Error user input incorrect
					macroSearchInc = false;
			}

			if (ae.getSource() == getButton) {
				// Get Macro
				macroNum = getMacro();
			}
			
	    	if (ae.getSource() == backUpButton){
	    		
	            Thread mb = new NceMacroBackup(tc);
	            mb.setName("Macro Backup");
	            mb.start ();
	    	}
	   		
	    	if (ae.getSource() == restoreButton){
	            Thread mr = new NceMacroRestore(tc);
	            mr.setName("Macro Restore");
	            mr.start ();
	    	}
		}
	}

	// One of the ten accessory command buttons pressed
	public void buttonActionCmdPerformed(java.awt.event.ActionEvent ae) {
		
		// if we're searching ignore user 
		if (macroSearchInc || macroSearchDec)
			return;
		
		if (ae.getSource() == cmdButton1) {
			updateAccyCmdPerformed(accyTextField1, cmdButton1, textAccy1,
					deleteButton1);
		}
		if (ae.getSource() == cmdButton2) {
			updateAccyCmdPerformed(accyTextField2, cmdButton2, textAccy2,
					deleteButton2);
		}
		if (ae.getSource() == cmdButton3) {
			updateAccyCmdPerformed(accyTextField3, cmdButton3, textAccy3,
					deleteButton3);
		}
		if (ae.getSource() == cmdButton4) {
			updateAccyCmdPerformed(accyTextField4, cmdButton4, textAccy4,
					deleteButton4);
		}
		if (ae.getSource() == cmdButton5) {
			updateAccyCmdPerformed(accyTextField5, cmdButton5, textAccy5,
					deleteButton5);
		}
		if (ae.getSource() == cmdButton6) {
			updateAccyCmdPerformed(accyTextField6, cmdButton6, textAccy6,
					deleteButton6);
		}
		if (ae.getSource() == cmdButton7) {
			updateAccyCmdPerformed(accyTextField7, cmdButton7, textAccy7,
					deleteButton7);
		}
		if (ae.getSource() == cmdButton8) {
			updateAccyCmdPerformed(accyTextField8, cmdButton8, textAccy8,
					deleteButton8);
		}
		if (ae.getSource() == cmdButton9) {
			updateAccyCmdPerformed(accyTextField9, cmdButton9, textAccy9,
					deleteButton9);
		}
		if (ae.getSource() == cmdButton10) {
			updateAccyCmdPerformed(accyTextField10, cmdButton10, textAccy10,
					deleteButton10);
		}
	}

	// One of ten Delete buttons pressed
	public void buttonActionDeletePerformed(java.awt.event.ActionEvent ae) {
		
		// if we're searching ignore user 
		if (macroSearchInc || macroSearchDec)
			return;
		
		if (ae.getSource() == deleteButton1) {
			updateAccyDelPerformed(accyTextField1, cmdButton1, textAccy1,
					deleteButton1);
		}
		if (ae.getSource() == deleteButton2) {
			updateAccyDelPerformed(accyTextField2, cmdButton2, textAccy2,
					deleteButton2);
		}
		if (ae.getSource() == deleteButton3) {
			updateAccyDelPerformed(accyTextField3, cmdButton3, textAccy3,
					deleteButton3);
		}
		if (ae.getSource() == deleteButton4) {
			updateAccyDelPerformed(accyTextField4, cmdButton4, textAccy4,
					deleteButton4);
		}
		if (ae.getSource() == deleteButton5) {
			updateAccyDelPerformed(accyTextField5, cmdButton5, textAccy5,
					deleteButton5);
		}
		if (ae.getSource() == deleteButton6) {
			updateAccyDelPerformed(accyTextField6, cmdButton6, textAccy6,
					deleteButton6);
		}
		if (ae.getSource() == deleteButton7) {
			updateAccyDelPerformed(accyTextField7, cmdButton7, textAccy7,
					deleteButton7);
		}
		if (ae.getSource() == deleteButton8) {
			updateAccyDelPerformed(accyTextField8, cmdButton8, textAccy8,
					deleteButton8);
		}
		if (ae.getSource() == deleteButton9) {
			updateAccyDelPerformed(accyTextField9, cmdButton9, textAccy9,
					deleteButton9);
		}
		// row ten delete button behaves differently
		// could be link button
		if (ae.getSource() == deleteButton10) {
			
			// is the user trying to link a macro?
			if (deleteButton10.getText() == LINK){
				if (macroValid == false) { // Error user input incorrect
					JOptionPane.showMessageDialog(this,
							rb.getString("GetMacroNumber"), rb.getString("NceMacro"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				int linkMacro = validMacro (accyTextField10.getText());
				if (linkMacro == -1) {
					JOptionPane.showMessageDialog(this,
							rb.getString("EnterMacroNumberLine10"), rb.getString("NceMacro"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				// success, link a macro
				setSaveButton(true);
				textAccy10.setText(LINK); 
				cmdButton10.setVisible(false);
				deleteButton10.setText(DELETE);
				deleteButton10.setToolTipText(rb.getString("toolTipRemoveMacroLink"));
				
			// user wants to delete a accessory address or a link	
			}else{
			updateAccyDelPerformed(accyTextField10, cmdButton10, textAccy10,
					deleteButton10);
			initAccyRow10 ();
			}
		}
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		getMacro();
	}
    
    // gets the user supplied macro number and then reads NCE CS memory
    private int getMacro (){
    	int mN = validMacro (macroTextField.getText());
		if (mN == -1) {
			macroReply.setText(rb.getString("error"));
			JOptionPane.showMessageDialog(this,
					rb.getString("EnterMacroNumber"), rb.getString("NceMacro"),
					JOptionPane.ERROR_MESSAGE);
			macroValid = false;
			return mN;
		}
		if (macroSearchInc || macroSearchDec) {
			macroReply.setText(rb.getString("searching"));
		}else{
			macroReply.setText(rb.getString("waiting"));
		}
		
		NceMessage m = readMacroMemory (mN, false);
		tc.sendNceMessage(m, this);
		return mN;
    }
  
 
    
    // Updates the accessory line when the user hits the command button
    private void updateAccyCmdPerformed (JTextField accyTextField, JButton cmdButton, JLabel textAccy, JButton deleteButton){
    	if (macroValid == false) { // Error user input incorrect
			JOptionPane.showMessageDialog(this,
					rb.getString("GetMacroNumber"), rb.getString("NceMacro"),
					JOptionPane.ERROR_MESSAGE);
		} else {
			String accyText = accyTextField.getText();
			int accyNum = 0;
			try {
				accyNum = Integer.parseInt(accyText);
			} catch (NumberFormatException e) {
				accyNum = -1;
			}

			if (accyNum < 1 | accyNum > 2044){
				JOptionPane.showMessageDialog(this,
						rb.getString("EnterAccessoryNumber"), rb.getString("NceMacroAddress"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			String accyCmd = cmdButton.getText();
			
			// Use JMRI or NCE turnout terminology
			if (checkBoxNce.isSelected()) {

				if (accyCmd != THROWN_NCE)
					cmdButton.setText(THROWN_NCE);
				if (accyCmd != CLOSED_NCE)
					cmdButton.setText(CLOSED_NCE);

			} else {

				if (accyCmd != THROWN)
					cmdButton.setText(THROWN);
				if (accyCmd != CLOSED)
					cmdButton.setText(CLOSED);
			}
			
			setSaveButton(true);
			textAccy.setText(ACCESSORY);
			deleteButton.setText(DELETE);
			deleteButton.setToolTipText(rb.getString("toolTipRemoveAcessory"));
			deleteButton.setEnabled(true);
		}
    }
    
    // Delete an accessory from the macro
    private void updateAccyDelPerformed (JTextField accyTextField, JButton cmdButton, JLabel textAccy, JButton deleteButton){
    	setSaveButton(true);
		textAccy.setText(EMPTY);
		accyTextField.setText("");
		cmdButton.setText(QUESTION);
		deleteButton.setEnabled(false);
	}
    
    // Updates all bytes in NCE CS memory as long as there are no user input errors
    private boolean saveMacro(){
        byte [] macroAccy = new byte [macroSize];			// NCE Macro data
        int index = 0;
        int accyNum = 0;
        accyNum = getAccyRow (macroAccy, index, textAccy1, accyTextField1, cmdButton1);
        if (accyNum < 0)	//error
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy2, accyTextField2, cmdButton2);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy3, accyTextField3, cmdButton3);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy4, accyTextField4, cmdButton4);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy5, accyTextField5, cmdButton5);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy6, accyTextField6, cmdButton6);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy7, accyTextField7, cmdButton7);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy8, accyTextField8, cmdButton8);
        if (isUsb) {
	        if (accyNum < 0)
	        	return false;
	        if (accyNum > 0)
	        	index+=2;
	        accyNum = getAccyRow (macroAccy, index, textAccy9, accyTextField9, cmdButton9);
	        if (accyNum < 0)
	        	return false;
	        if (accyNum > 0)
	        	index+=2;
	        accyNum = getAccyRow (macroAccy, index, textAccy10, accyTextField10, cmdButton10);
        }
        if (accyNum < 0){
			JOptionPane.showMessageDialog(this,
					rb.getString("EnterMacroNumberLine10"), rb.getString("NceMacro"),
					JOptionPane.ERROR_MESSAGE);
			return false;
        }
          
        NceMessage m = writeMacroMemory(macroNum, macroAccy,  false);
        tc.sendNceMessage(m, this);
        NceMessage m2 = writeMacroMemory(macroNum, macroAccy,  true);
        tc.sendNceMessage(m2, this);
        return true;
     }
    
    private int getAccyRow (byte[] b, int i, JLabel textAccy, JTextField accyTextField, JButton cmdButton){
        int accyNum = 0;
    	if (textAccy.getText() == ACCESSORY){
        	accyNum = getAccyNum(accyTextField.getText());
        	if (accyNum < 0)
        		return accyNum;
        	accyNum = accyNum + 3;							// adjust for NCE's way of encoding
        	int upperByte = (accyNum&0xFF);
        	upperByte = (upperByte >>2)+ 0x80;
        	b[i] = (byte)upperByte;
        	int lowerByteH = (((accyNum ^ 0x0700) & 0x0700)>>4);// 3 MSB 1s complement
        	int lowerByteL = ((accyNum & 0x3)<<1);       	// 2 LSB
        	int lowerByte = (lowerByteH + lowerByteL + 0x88);
        	if (cmdButton.getText() == CLOSED)				// adjust for turnout command	
        		lowerByte++;
        	if (cmdButton.getText() == CLOSED_NCE)			// adjust for turnout command	
        		lowerByte++;
         	b[i+1] = (byte)(lowerByte);
        }
    	if (textAccy.getText() == LINK){
         	int macroLink = validMacro (accyTextField.getText());
         	if (macroLink < 0)
         		return macroLink;
         	b[i] = (byte) 0xFF;								// NCE macro link command
         	b[i+1] = (byte) macroLink;						// link macro number
    	}
        return accyNum;
    }
    
    private int getAccyNum(String accyText){
       	int accyNum = 0;
		try {
			accyNum = Integer.parseInt(accyText);
		} catch (NumberFormatException e) {
			accyNum = -1;
		}
		if (accyNum < 1 | accyNum > 2044){
			JOptionPane.showMessageDialog(this,
					rb.getString("EnterAccessoryNumber"), rb.getString("NceMacroAddress"),
					JOptionPane.ERROR_MESSAGE);
			accyNum = -1;
		}
		return accyNum;
    }
    
    // display save button
    private void setSaveButton(boolean display) {
		macroModified = display;
		saveButton.setEnabled(display);
		backUpButton.setEnabled(!display);
		restoreButton.setEnabled(!display);
	}
    
    public void  message(NceMessage m) {}  // ignore replies
    
    // response from save, get, next or previous
	public void reply(NceReply r) {
		if (waiting <= 0) {
			log.error("unexpected response");
			return;
		}
		waiting--;
		if (r.getNumDataElements() != replyLen) {
			macroReply.setText(rb.getString("error"));
			return;
		}
		// Macro command
		if (replyLen == NceMessage.REPLY_1) {
			// Looking for proper response
			int recChar = r.getElement(0);
			if (recChar == '!')
				macroReply.setText(rb.getString("okay"));
			if (recChar == '0')
				macroReply.setText(rb.getString("macroEmpty"));
		}
		// Macro memory read
		if (replyLen == NceMessage.REPLY_16) {
			// NCE macros consists of 20 bytes on serial, 16 on USB
			// so either 4 or 5 reads
			if (secondRead) {
				// Second memory read for accessories 9 and 10
				secondRead = false;
				loadAccy9and10(r);

			} else {
				int recChar = r.getElement(0);
				recChar = recChar << 8;
				recChar = recChar + r.getElement(1);
				if (recChar == 0) {
					if (checkBoxEmpty.isSelected()) {
						if (macroCount > 0) {
							macroSearchInc = false;
							macroSearchDec = false;
						}
					}
					// Macro is empty so init the accessory fields
					macroReply.setText(rb.getString("macroEmpty"));
					initAccyFields();
					macroValid = true;
				} else {
					if (checkBoxEmpty.isSelected() == false) {
						if (macroCount > 0) {
							macroSearchInc = false;
							macroSearchDec = false;
						}
					}
					macroReply.setText(rb.getString("macroFound"));
					secondRead = loadAccy1to8(r);
					macroValid = true;
				}
				// if we're searching, don't bother with second read
				if (macroSearchInc || macroSearchDec)
					secondRead = false;
				// Do we need to read more CS memory?
				if (secondRead)
					// force second read of CS memory
					getMacro2ndHalf(macroNum);
				// when searching, have we read all of the possible
				// macros?
				macroCount++;
				if (macroCount > maxNumMacros) {
					macroSearchInc = false;
					macroSearchDec = false;
				}
				if (macroSearchInc) {
					macroNum++;
					if (macroNum == maxNumMacros + 1)
						macroNum = 0;
				}
				if (macroSearchDec) {
					macroNum--;
					if (macroNum == -1)
						macroNum = maxNumMacros;
				}
				if (macroSearchInc || macroSearchDec) {
					macroTextField.setText(Integer.toString(macroNum));
					macroNum = getMacro();
				}
			}
		}
	}

  
	// Convert NCE macro hex data to accessory address
	// returns 0 if macro address is empty
	// returns a negative address if link address
	// & loads accessory 10 with link macro
    private int getNextMacroAccyAdr(int i, NceReply r) {
		int b = (i - 1) << 1;
		int accyAddrL = r.getElement(b);
		int accyAddr = 0;
		// check for null
		if ((accyAddrL == 0) && (r.getElement(b + 1) == 0)) {
			return accyAddr;
		}
		// Check to see if link address
		if ((accyAddrL & 0xFF) == 0xFF) {
			// Link address
			accyAddr = r.getElement(b + 1);
			linkAccessory10(accyAddr & 0xFF);
			accyAddr = -accyAddr;
			
		// must be an accessory address	
		} else {
			accyAddrL = (accyAddrL << 2) & 0xFC;			// accessory address bits 7 - 2
			int accyLSB = r.getElement(b + 1);
			accyLSB = (accyLSB & 0x06) >> 1;				// accessory address bits 1 - 0
			int accyAddrH = r.getElement(b + 1);
			accyAddrH = (0x70 - (accyAddrH & 0x70)) << 4; 	// One's completent of MSB of address 10 - 8
															// & multiply by 16
			accyAddr = accyAddrH + accyAddrL + accyLSB - 3; // adjust for the way NCE displays addresses
		}
		return accyAddr;
	}
    
    // whenever link macro is found, put it in the last location
    // this makes it easier for the user to edit the macro
    private void linkAccessory10(int accyAddr){
    	textAccy10.setText(LINK); 
		accyTextField10.setText(Integer.toString(accyAddr));
		cmdButton10.setVisible(false);
		deleteButton10.setText(DELETE);
		deleteButton10.setToolTipText(rb.getString("toolTipRemoveMacroLink"));
    }
    
    // update the panel first 8 accessories
	// returns true if 2nd read is needed
	private boolean loadAccy1to8(NceReply r) {
		// flag second read only necessary for accessories 9 and 10
		boolean req2ndRead = false;

		// Set all fields to default and build from there
		initAccyFields();

		// As soon as a macro link is found, stop reading the rest
		// of the macro
		// update the first accessory in the table
		// we know it exist or we wouldn't be here!

		int row = 1;
		//	1st word of macro
		int accyAddr = getNextMacroAccyAdr(row, r);
		// neg address = link address
		// null = empty 
		if (accyAddr <= 0)
			return req2ndRead;
		// enter accessory
		setAccy(row++, accyAddr, r, textAccy1, accyTextField1, cmdButton1,
				deleteButton1);

		// 2nd word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy2, accyTextField2, cmdButton2,
				deleteButton2);

		// 3rd word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy3, accyTextField3, cmdButton3,
				deleteButton3);

		// 4th word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy4, accyTextField4, cmdButton4,
				deleteButton4);

		// 5th word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy5, accyTextField5, cmdButton5,
				deleteButton5);

		// 6th word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy6, accyTextField6, cmdButton6,
				deleteButton6);

		// 7th word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy7, accyTextField7, cmdButton7,
				deleteButton7);

		// 8th word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy8, accyTextField8, cmdButton8,
				deleteButton8);
		req2ndRead = true; // Need to read NCE CS memory for 9 & 10
		return req2ndRead;
	}
    
    // update the panel 9 & 10 accessories
    private void loadAccy9and10(NceReply r){
		// 9th word of macro arrives in second read block
    	int pass2ndRow= 1;
		int accyAddr = getNextMacroAccyAdr(pass2ndRow, r);
		if (accyAddr <= 0) {
			return;
		}
		if (accyAddr > 0) {
			setAccy(pass2ndRow++, accyAddr, r, textAccy9, accyTextField9, cmdButton9,
					deleteButton9);
		}
		// 10th word of macro
		accyAddr = getNextMacroAccyAdr(pass2ndRow, r);
		if (accyAddr <= 0) {
			return;
		}
		if (accyAddr > 0) {
			setAccy(pass2ndRow++, accyAddr, r, textAccy10, accyTextField10, cmdButton10,
					deleteButton10);
			deleteButton10.setText(DELETE);
			deleteButton10.setToolTipText("Remove this accessory from the macro");
		}
		return;	// done with reading macro
      }
    
    // loads one row with a macro's accessory address and command
    private void setAccy(int row, int accyAddr, NceReply r, JLabel textAccy,
			JTextField accyTextField, JButton cmdButton, JButton deleteButton) {
		textAccy.setText(ACCESSORY);
		accyTextField.setText(Integer.toString(accyAddr));
		deleteButton.setEnabled(true);
		cmdButton.setText(getAccyCmd(row, r));
	}
    
    // returns the accessory command
    private String getAccyCmd (int row, NceReply r){
		int b = (row - 1) << 1;
		int accyCmd = r.getElement(b+1);
		String s = THROWN;
		if (checkBoxNce.isSelected())
			s = THROWN_NCE;
		accyCmd = accyCmd & 0x01;
		if (accyCmd == 0){
			return s;
		}else{
			s = CLOSED;
			if (checkBoxNce.isSelected())
				s = CLOSED_NCE;
		}
		return s;
    }
    
    // Check for valid macro, return number if valid, -1 if not.
    private int validMacro (String s){
    	int mN;
		try {
			mN = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return -1;
		}
		if (mN < 0 | mN > maxNumMacros)
			return -1;
		else
			return mN;
    }
    
    // gets the last 4 bytes of the macro by reading 16 bytes of data 
    private void getMacro2ndHalf (int mN){
    	NceMessage m = readMacroMemory (mN, true);
		tc.sendNceMessage(m, this);
    }
 
    // Reads 16 bytes of NCE macro memory, and adjusts for second read if needed 
    private NceMessage readMacroMemory(int macroNum, boolean second) {
       	int nceMacroAddr = (macroNum * macroSize) + maxNumMacros;
    	if(second){
    		nceMacroAddr = nceMacroAddr + 16;	//adjust for second memory read
    	}
    	replyLen = NceMessage.REPLY_16;			// Expect 16 byte response
    	waiting++;
		byte[] bl = NceBinaryCommand.accMemoryRead(nceMacroAddr);
		NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_16);
		return m;
    }
    
    // writes 20 bytes of NCE macro memory, and adjusts for second write 
	private NceMessage writeMacroMemory(int macroNum, byte[] b, boolean second) {
		int nceMacroAddr = (macroNum * macroSize) + maxNumMacros;
		replyLen = NceMessage.REPLY_1; // Expect 1 byte response
		waiting++;
		byte[] bl;

		// write 4 bytes
		if (second) {
			nceMacroAddr += 16; 	// adjust for second memory
			// write
			bl = NceBinaryCommand.accMemoryWriteN(nceMacroAddr, 4);
			int j = bl.length-16;
			for (int i = 0; i < 4; i++, j++)
				bl[j] = b[i+16];
			
		// write 16 bytes	
		} else {
			bl = NceBinaryCommand.accMemoryWriteN(nceMacroAddr, 16);
			int j = bl.length-16;
			for (int i = 0; i < 16; i++, j++)
				bl[j] = b[i];
		}
		NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_1);
		return m;
	}
    
    private void addAccyRow (JComponent col1, JComponent col2, JComponent col3, JComponent col4, JComponent col5, int row){
        addItem(col1,0,row); 
        addItem(col2,1,row);
        addItem(col3,2,row);
        addItem(col4,3,row);
        addItem(col5,4,row);	
    }
    
    private void addItem(JComponent c, int x, int y ){
    	GridBagConstraints gc = new GridBagConstraints ();
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

	private void addButtonCmdAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionCmdPerformed(e);
			}
		});
	}  
    
    private void addButtonDelAction (JButton b){
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionDeletePerformed(e);
			}
		});
    } 
    
    private void addCheckBoxAction (JCheckBox cb){
		cb.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionPerformed(e);
			}
		});
    } 
    
    //  initialize accessories 1 to 10
    private void initAccyFields() {
    	initAccyRow(1, num1, textAccy1, accyTextField1, cmdButton1, deleteButton1);
    	initAccyRow(2, num2, textAccy2, accyTextField2, cmdButton2, deleteButton2);
    	initAccyRow(3, num3, textAccy3, accyTextField3, cmdButton3, deleteButton3);
    	initAccyRow(4, num4, textAccy4, accyTextField4, cmdButton4, deleteButton4);
    	initAccyRow(5, num5, textAccy5, accyTextField5, cmdButton5, deleteButton5);
    	initAccyRow(6, num6, textAccy6, accyTextField6, cmdButton6, deleteButton6);
    	initAccyRow(7, num7, textAccy7, accyTextField7, cmdButton7, deleteButton7);
    	initAccyRow(8, num8, textAccy8, accyTextField8, cmdButton8, deleteButton8);
    	initAccyRow(9, num9, textAccy9, accyTextField9, cmdButton9, deleteButton9);
    	initAccyRow(10, num10, textAccy10, accyTextField10, cmdButton10, deleteButton10);
	}
    
    private void initAccyRow(int row, JLabel num, JLabel textAccy, JTextField accyTextField, JButton cmdButton, JButton deleteButton) {
		num.setText(Integer.toString(row));
		num.setVisible(true);
		textAccy.setText(EMPTY);
		textAccy.setVisible(true);
		cmdButton.setText(QUESTION);
		cmdButton.setVisible(true);
		cmdButton.setToolTipText(rb.getString("toolTipSetCommand"));
		deleteButton.setText(DELETE);
		deleteButton.setVisible(true);
		deleteButton.setEnabled(false);
		deleteButton.setToolTipText(rb.getString("toolTipRemoveAcessory"));
		accyTextField.setText("");
		accyTextField.setToolTipText(rb.getString("EnterAccessoryNumber"));
		accyTextField.setMaximumSize(new Dimension(accyTextField
				.getMaximumSize().width,
				accyTextField.getPreferredSize().height));
		if (row == 10)
			initAccyRow10 ();
    }
    
    private void initAccyRow10 (){
		cmdButton10.setVisible(true);
		deleteButton10.setText(LINK);
		deleteButton10.setEnabled(true);
		deleteButton10.setToolTipText(rb.getString("toolTipLink"));
		accyTextField10.setToolTipText(rb.getString("toolTip10"));
    }
    
   
    static Logger log = LoggerFactory.getLogger(NceMacroEditPanel.class.getName());	
}

