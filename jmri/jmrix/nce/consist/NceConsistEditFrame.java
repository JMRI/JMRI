// NceConsistEditFrame.java


package jmri.jmrix.nce.consist;

import jmri.jmrix.nce.*;

import java.awt.*;
import javax.swing.*;

import java.io.*;



/**
 * Frame for user edit of NCE Consists
 * 
 * NCE Consists are stored in Command Station (CS) memory starting at address
 * xF500 and ending xFAFF.  NCE supports up to 128 consists, numbered 0 to 127.
 * They track the lead engine, rear engine, and four mid engines in the consist file.
 * NCE cabs start at consist 127 when building and reviewing consists, so we also
 * start with 127.  Consist lead engines are stored in memory locations xF500 through 
 * xF5FF.  Consist rear engines are stored in memory locations xF600 through xF6FF.
 * Mid consist engines (four max) are stored in memory locations xF700 through xFAFF.
 * If a long address is in use, bits 6 and 7 of the high byte are set.
 * Example: Long address 3 = 0xc0 0x03
 * Short address 3 = 0x00 0x03
 * 
 *   NCE file format:
 * 
 * :F500 (con 0 lead engine) (con 1 lead engine) ....... (con 7 lead engine)
 * :F510 (con 8 lead engine) ........ (con 15 lead engine)
 *   .
 *   .
 * :F5F0 (con 120 lead engine) ..... (con 127 lead engine)
 * 
 * :F600 (con 0 rear engine) (con 1 rear engine) ....... (con 7 rear engine)
 *   .
 *   .
 * :F6F0 (con 120 rear engine) ..... (con 127 rear engine)
 * 
 * :F700 (con 0 mid eng1) (con 0 mid eng2) (con 0 mid eng3) (con 0 mid eng4)     
 *   .
 *   .
 * :FAF0 (con 126 mid eng1) .. (con 126 mid eng4)(con 127 mid eng1) .. (con 127 mid eng4)
 * :0000
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision: 1.7 $
 */

public class NceConsistEditFrame extends jmri.util.JmriJFrame implements jmri.jmrix.nce.NceListener {

	private static final int CS_CONSIST_MEM = 0xF500;	// start of NCE CS Consist memory 
	private static final int CS_CON_MEM_REAR = 0xF600;	// start of rear consist engines
	private static final int CS_CON_MEM_MID = 0xF700;	// start of mid consist engines
	private static final int CONSIST_MIN = 1;
	private static final int CONSIST_MAX = 127;
	private static final int LOC_ADR_MIN = 1;
	private static final int LOC_ADR_MAX = 9999;
	private int consistNum = 127;					// consist being worked
	private int engineNum = 0; 						// which engine 
	private static final int REPLY_1 = 1;			// reply length of 1 byte expected
	private static final int REPLY_16 = 16;			// reply length of 16 bytes expected
	private int replyLen = 0;						// expected byte length
	private int waiting = 0;						// to catch responses not intended for this module
	
	private static final String DELETE = "Delete";
	private static final String ADD = " Add ";
	private static final String REPLACE = "Replace";
	private static final String QUESTION = "  ??  ";
	private static final String FWD = "Forward";
	private static final String REV = "Reverse";
	private static final String LONG = "Long";
	private static final String SHORT = "Short";
	private static final String EMPTY = "";
	
	private static final String ToolTipAdd = "Press to add engine to consist";
	
	
	private boolean consistSearchInc = false;		// next search
	private boolean consistSearchDec = false;		// previous search
	private int consistCount = 0;					// search count not to exceed CONSIST_MAX
	private boolean secondRead = false;				// when true, another 16 byte read expected
	private boolean consistValid = false;			// when true, NCE CS has responed to consist read
	private boolean consistModified = false;		// when true, consist has been modified by user
	
	private boolean refresh = false;				// when true, refresh loco address from CS
	
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
    javax.swing.JButton backUpButton = new javax.swing.JButton();
    javax.swing.JButton restoreButton = new javax.swing.JButton();
    
    // check boxes
    javax.swing.JCheckBox checkBoxEmpty = new javax.swing.JCheckBox ();
  
    // consist text field
    javax.swing.JTextField consistTextField = new javax.swing.JTextField(4);
    
    // labels
    javax.swing.JLabel textEngine = new javax.swing.JLabel();
    javax.swing.JLabel textAddress = new javax.swing.JLabel();
    javax.swing.JLabel textAddrType = new javax.swing.JLabel();
    javax.swing.JLabel textDirection = new javax.swing.JLabel();
    
    // for padding out panel
    javax.swing.JLabel space1 = new javax.swing.JLabel();
    javax.swing.JLabel space2 = new javax.swing.JLabel();
    
    // lead engine
    javax.swing.JLabel textEng1 = new javax.swing.JLabel();
    javax.swing.JTextField engTextField1 = new javax.swing.JTextField(4);
    javax.swing.JButton adrButton1 = new javax.swing.JButton();
    javax.swing.JButton cmdButton1 = new javax.swing.JButton();
    javax.swing.JButton dirButton1 = new javax.swing.JButton();
    
    //  rear engine
    javax.swing.JLabel textEng2 = new javax.swing.JLabel();
    javax.swing.JTextField engTextField2 = new javax.swing.JTextField(4);
    javax.swing.JButton adrButton2 = new javax.swing.JButton();
    javax.swing.JButton cmdButton2 = new javax.swing.JButton();
    javax.swing.JButton dirButton2 = new javax.swing.JButton();
    
    //  mid engine
    javax.swing.JLabel textEng3 = new javax.swing.JLabel();
    javax.swing.JTextField engTextField3 = new javax.swing.JTextField(4);
    javax.swing.JButton adrButton3 = new javax.swing.JButton();
    javax.swing.JButton cmdButton3 = new javax.swing.JButton();
    javax.swing.JButton dirButton3 = new javax.swing.JButton();
    
    //  mid engine
    javax.swing.JLabel textEng4 = new javax.swing.JLabel();
    javax.swing.JTextField engTextField4 = new javax.swing.JTextField(4);
    javax.swing.JButton adrButton4 = new javax.swing.JButton();
    javax.swing.JButton cmdButton4 = new javax.swing.JButton();
    javax.swing.JButton dirButton4 = new javax.swing.JButton();
    
    //  mid engine
    javax.swing.JLabel textEng5 = new javax.swing.JLabel();
    javax.swing.JTextField engTextField5 = new javax.swing.JTextField(4);
    javax.swing.JButton adrButton5 = new javax.swing.JButton();
    javax.swing.JButton cmdButton5 = new javax.swing.JButton();
    javax.swing.JButton dirButton5 = new javax.swing.JButton();
    
    //  mid engine
    javax.swing.JLabel textEng6 = new javax.swing.JLabel();
    javax.swing.JTextField engTextField6 = new javax.swing.JTextField(4);
    javax.swing.JButton adrButton6 = new javax.swing.JButton();
    javax.swing.JButton cmdButton6 = new javax.swing.JButton();
    javax.swing.JButton dirButton6 = new javax.swing.JButton();
    
    public NceConsistEditFrame() {
    }
 

    public void initComponents() throws Exception {
        // the following code sets the frame's initial state
     	
    	textConsist.setText("Consist");
        textConsist.setVisible(true);
        
        textReply.setText("Reply:"); 
        textReply.setVisible(true);
  
        consistReply.setText("unknown"); 
        consistReply.setVisible(true);

        previousButton.setText("Previous");
        previousButton.setVisible(true);
        previousButton.setToolTipText("Search for consist inccrementing");
        
        nextButton.setText("   Next   ");
        nextButton.setVisible(true);
        nextButton.setToolTipText("Search for consist decrementing");
        
        getButton.setText("  Get  ");
        getButton.setVisible(true);
        getButton.setToolTipText("Read consist from NCE CS");
        
        consistTextField.setText(Integer.toString(consistNum));
		consistTextField.setToolTipText("Enter consist 1 to 127");
		consistTextField.setMaximumSize(new Dimension(consistTextField
				.getMaximumSize().width, consistTextField.getPreferredSize().height));
        
		textEngine.setText("Engine");
		textEngine.setVisible(true);
		textAddress.setText("Address");
		textAddress.setVisible(true);
		textAddrType.setText("Type");
		textAddrType.setVisible(true);
		textDirection.setText("Direction");
		textDirection.setVisible(true);
		
		clearButton.setText("Clear");
        clearButton.setVisible(true);
        clearButton.setEnabled (false);
        clearButton.setToolTipText("Clear consist in NCE CS");
		
		saveButton.setText("Save");
        saveButton.setVisible(false);
        saveButton.setEnabled (false);
        saveButton.setToolTipText("Update consist in NCE CS");
        
        backUpButton.setText(" Backup ");
        backUpButton.setVisible(true);
        backUpButton.setToolTipText("Save all consists to a file");
   	   
        restoreButton.setText("Restore");
        restoreButton.setVisible(true);
        restoreButton.setToolTipText("Restore all consists from a file");
   	   
		checkBoxEmpty.setText("Empty Consist");
        checkBoxEmpty.setVisible(true);
        checkBoxEmpty.setToolTipText("Check to search for empty consists");
        
        space1.setText("            ");
        space1.setVisible(true);
        space2.setText(" ");
        space2.setVisible(true); 
        
        initEngFields();
        
        setTitle("Edit NCE Consist");
        getContentPane().setLayout(new GridBagLayout());
        
        // Layout the panel by rows
        // row 0
        addItem(textConsist, 2,0);
        
        // row 1
        addItem(previousButton, 1,1);
        addItem(consistTextField, 2,1);
        addItem(nextButton, 3,1);
        addItem(checkBoxEmpty, 4,1);
        
        // row 2
        addItem(textReply, 0,2);
        addItem(consistReply, 1,2);
        addItem(getButton, 2,2);
        
        // row 3 padding for looks
        addItem(space1, 1,3);
        
        // row 4 labels
        
        addItem(textEngine, 0,4);
        addItem(textAddress, 1,4);
        addItem(textAddrType, 2,4);
        addItem(textDirection, 3,4);
        
        // row 5 Lead Engine
        addEngRow (textEng1, engTextField1, adrButton1, dirButton1, cmdButton1,  5);
          
        // row 6 Rear Engine
        addEngRow (textEng2, engTextField2, adrButton2, dirButton2, cmdButton2,  6);
        
        // row 7 Mid Engine
        addEngRow (textEng3, engTextField3, adrButton3, dirButton3, cmdButton3,  7);
        
        // row 8 Mid Engine
        addEngRow (textEng4, engTextField4, adrButton4, dirButton4, cmdButton4,  8);
        
        // row 9 Mid Engine
        addEngRow (textEng5, engTextField5, adrButton5, dirButton5, cmdButton5,  9);
        
        // row 10 Mid Engine
        addEngRow (textEng6, engTextField6, adrButton6, dirButton6, cmdButton6,  10);
        
        // row 15 padding for looks
        addItem(space2, 2,15);
        
        // row 16
        addItem(clearButton, 1,16);
        addItem(saveButton, 2,16);
        addItem(backUpButton, 3,16);
        addItem(restoreButton, 4,16);
        
        // setup buttons
        addButtonAction(previousButton);
        addButtonAction(nextButton);
        addButtonAction(getButton);
        addButtonAction(clearButton);
        addButtonAction(saveButton);
        addButtonAction(backUpButton);
        addButtonAction(restoreButton);
        
        // engine address length buttons
        addButtonAdrAction(adrButton1);
        addButtonAdrAction(adrButton2);
        addButtonAdrAction(adrButton3);
        addButtonAdrAction(adrButton4);
        addButtonAdrAction(adrButton5);
        addButtonAdrAction(adrButton6);
        
        // engine direction buttons
        addButtonDirAction(dirButton1);
        addButtonDirAction(dirButton2);
        addButtonDirAction(dirButton3);
        addButtonDirAction(dirButton4);
        addButtonDirAction(dirButton5);
        addButtonDirAction(dirButton6);
        
        // engine command buttons
        addButtonCmdAction(cmdButton1);
        addButtonCmdAction(cmdButton2);
        addButtonCmdAction(cmdButton3);
        addButtonCmdAction(cmdButton4);
        addButtonCmdAction(cmdButton5);
        addButtonCmdAction(cmdButton6);
        
        // set frame size for display
        this.setSize (400,350);
    }
 
    // Previous, Next, Get, Clear, Save, Restore & Backup buttons
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {

		// if we're searching ignore user
		if (consistSearchInc || consistSearchDec)
			return;

		if (ae.getSource() == clearButton) {
			int locoAddr = Integer.parseInt(engTextField1.getText());
			if (adrButton1.getText() == LONG) {
				locoAddr += 0xC000;
			}

			//set refresh flag to update panel
			refresh = true;
			
			byte[] bl = NceBinaryCommand.nceLocoCmd(locoAddr,
					NceBinaryCommand.LOC_CMD_KILL_CONSIST, (byte) 0);
			sendNceMessage(bl,REPLY_1);

		}
		
		// save button not currently activated or used RFU
		if (ae.getSource() == saveButton) {
				setSaveButton(false); // yes, clear save button
			return;
		}

		if (consistModified) {
			// warn user that consist has been modified
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Consist has been modified, use Save to update NCE CS memory", "NCE Consist",
					JOptionPane.WARNING_MESSAGE);
			consistModified = false;		// only one warning!!!

		} else {
			
			setSaveButton(false);		// Turn off save button
		
			if (ae.getSource() == previousButton) {
				consistCount = 0; // used to determine if all 127 consist have been read
				consistSearchDec = true;
				consistNum = getConsist();	// check for valid and kick off read process
				if (consistNum == -1) 	// Error user input incorrect
					consistSearchDec = false;
			}
			if (ae.getSource() == nextButton) {
				consistCount = 0; // used to determine if all 127 consist have been read
				consistSearchInc = true;
				consistNum = getConsist();	// check for valid and kick off read process
				if (consistNum == -1) 	// Error user input incorrect
					consistSearchInc = false;
			}

			if (ae.getSource() == getButton) {
				// Get Consist
				consistNum = getConsist();
			}
			
	    	if (ae.getSource() == backUpButton){
	    		
	            Thread mb = new NceConsistBackup();
	            mb.setName("Consist Backup");
	            mb.start ();
	    	}
	   		
	    	if (ae.getSource() == restoreButton){
	            Thread mr = new NceConsistRestore();
	            mr.setName("Consist Restore");
	            mr.start ();
	    	}
		}
	}

	// One of six loco command buttons, add, replace or delete
	public void buttonActionCmdPerformed(java.awt.event.ActionEvent ae) {

		// if we're searching ignore user
		if (consistSearchInc || consistSearchDec)
			return;
		
		// set reflesh flag to update panel
		refresh = true;

		if (ae.getSource() == cmdButton1) {
			modifyLocoFields(engTextField1, adrButton1, dirButton1, cmdButton1);
		}
		if (ae.getSource() == cmdButton2) {
			modifyLocoFields(engTextField2, adrButton2, dirButton2, cmdButton2);
		}
		if (ae.getSource() == cmdButton3) {
			modifyLocoFields(engTextField3, adrButton3, dirButton3, cmdButton3);
		}
		if (ae.getSource() == cmdButton4) {
			modifyLocoFields(engTextField4, adrButton4, dirButton4, cmdButton4);
		}
		if (ae.getSource() == cmdButton5) {
			modifyLocoFields(engTextField5, adrButton5, dirButton5, cmdButton5);
		}
		if (ae.getSource() == cmdButton6) {
			modifyLocoFields(engTextField6, adrButton6, dirButton6, cmdButton6);
		}
	}
	
	// one of six loco address type buttons
	public void buttonActionAdrPerformed(java.awt.event.ActionEvent ae) {
		
		// if we're searching ignore user 
		if (consistSearchInc || consistSearchDec)
			return;
		
		if (ae.getSource() == adrButton1) {
			toggleAdrButton (engTextField1, adrButton1);
			
		}
		if (ae.getSource() == adrButton2) {
			toggleAdrButton (engTextField2, adrButton2);
			
		}
		if (ae.getSource() == adrButton3) {
			toggleAdrButton (engTextField3, adrButton3);
			
		}
		if (ae.getSource() == adrButton4) {
			toggleAdrButton (engTextField4, adrButton4);
			
		}
		if (ae.getSource() == adrButton5) {
			toggleAdrButton (engTextField5, adrButton5);
			
		}
		if (ae.getSource() == adrButton6) {
			toggleAdrButton (engTextField6, adrButton6);
			
		}

	}
	
	private void toggleAdrButton(JTextField engTextField, JButton adrButton) {
		
		if (validLocoAdr(engTextField.getText())<0)
			return;
		
		if (engTextField.getText().equals (EMPTY)) {
			
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter loco address before changing address type", "NCE Consist",
					JOptionPane.ERROR_MESSAGE);
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
		if (consistSearchInc || consistSearchDec)
			return;
		
		if (ae.getSource() == dirButton1) {
			toggleDirButton (engTextField1, dirButton1, cmdButton1);
			
		}
		if (ae.getSource() == dirButton2) {
			toggleDirButton (engTextField2, dirButton2, cmdButton2);
			
		}
		if (ae.getSource() == dirButton3) {
			toggleDirButton (engTextField3, dirButton3, cmdButton3);
			
		}
		if (ae.getSource() == dirButton4) {
			toggleDirButton (engTextField4, dirButton4, cmdButton4);
			
		}
		if (ae.getSource() == dirButton5) {
			toggleDirButton (engTextField5, dirButton5, cmdButton5);
			
		}
		if (ae.getSource() == dirButton6) {
			toggleDirButton (engTextField6, dirButton6, cmdButton6);
			
		}

	}
	
	private void toggleDirButton(JTextField engTextField, JButton dirButton, JButton cmdButton) {

		if (validLocoAdr(engTextField.getText())<0)
			return;
		
		if (engTextField.getText().equals(EMPTY)) {

			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter loco address before changing loco direction",
					"NCE Consist", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		cmdButton.setEnabled (true);

		if (dirButton.getText() == FWD) {
			dirButton.setText(REV);
		
		} else {
			dirButton.setText(FWD);
		}
	}

	//RFU
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
	}
    
    // gets the user supplied consist number and then reads NCE CS memory
    private int getConsist (){
    	int cN = validConsist (consistTextField.getText());
		if (cN == -1) {
			consistReply.setText("error");
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter consist number 1 to 127", "NCE Consist",
					JOptionPane.ERROR_MESSAGE);
			consistValid = false;
			return cN;
		}
		if (consistSearchInc || consistSearchDec) {
			consistReply.setText("searching");
		}else{
			consistReply.setText("waiting");
		}
		
		byte[] bl = readConsistMemory (cN, 0);
		sendNceMessage(bl, REPLY_16);
		
		return cN;
    }
 
  
    // Check for valid consist number, return number if valid, -1 if not.
    private int validConsist (String s){
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
    
//  Check for valid consist number, return number if valid, -1 if not.
    private int validLocoAdr (String s){
    	int lA;
		try {
			lA = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			lA = -1;
		}
		if (lA < LOC_ADR_MIN | lA > LOC_ADR_MAX){
			
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter loco address 1 to 9999",
					"NCE Consist", JOptionPane.ERROR_MESSAGE);
			return -1;
		}
		else
			return lA;
    }
    
    // Reads 16 bytes of NCE consist memory based on consist number and 
    // engine number 0=lead 1=rear 2=mid 
    private byte[] readConsistMemory(int consistNum, int eNum) {
    	
    	engineNum = eNum;
    	
    	int nceConsistBaseMemory = CS_CONSIST_MEM;
    	if (eNum == 1)
    		nceConsistBaseMemory = CS_CON_MEM_REAR;
    	int nceMacroAddr = (consistNum * 2) + nceConsistBaseMemory;
    	if (eNum == 2){
    		nceConsistBaseMemory = CS_CON_MEM_MID;
    		nceMacroAddr = (consistNum * 8) + nceConsistBaseMemory;
    	}
 
 		byte[] bl = NceBinaryCommand.accMemoryRead(nceMacroAddr);
		return bl;
    }
 
    
    // display save button
    private void setSaveButton(boolean display) {
		consistModified = display;
		saveButton.setEnabled(display);
		backUpButton.setEnabled(!display);
		restoreButton.setEnabled(!display);
	}
    
    public void  message(NceMessage m) {}  // ignore replies
    
    // NCE CS response from add, delete, save, get, next or previous
	public void reply(NceReply r) {
		if (waiting <= 0) {
			log.error("unexpected response");
			return;
		}
		
		waiting--;

		if (r.getNumDataElements() != replyLen) {
			consistReply.setText("error");
			log.error("reply length error, expecting: "+replyLen+" got: "+r.getNumDataElements());
			return;
		}

		// response to commands
		if (replyLen == REPLY_1) {
			// Looking for proper response
			int recChar = r.getElement(0);
			if (recChar == '!'){
				consistReply.setText("okay");
				if (refresh){
					refresh = false;
					// update panel
					byte[] bl = readConsistMemory(consistNum, 0);
					sendNceMessage(bl, REPLY_16);
				}
			}
			if (recChar == '0')
				consistReply.setText("empty");
			return;
		}
		
		// Consist memory read
		if (replyLen == REPLY_16) {
			
			// load lead engine
			if (engineNum == 0) {

				boolean eng1exists = updateLocoFields (r, 0, engTextField1, adrButton1, dirButton1, cmdButton1);

				clearButton.setEnabled (eng1exists);
				

				// are we searching?
				if (consistSearchInc || consistSearchDec) {
					
					consistTextField.setText(Integer.toString(consistNum));
					
					if (checkBoxEmpty.isSelected()) {
						if (!eng1exists) {
							if (consistCount > 0) {
								consistSearchInc = false;
								consistSearchDec = false;
							}
						}

					} else {
						if (eng1exists) {
							if (consistCount > 0) {
								consistSearchInc = false;
								consistSearchDec = false;
							}
						}
					}
					if (++consistCount > CONSIST_MAX) {
						consistSearchInc = false;
						consistSearchDec = false;
					}
				}

				// load rear engine
			} else if (engineNum == 1) {

				updateLocoFields (r, 0, engTextField2, adrButton2, dirButton2, cmdButton2);

				// load mid engines
			} else {

				updateLocoFields (r, 0, engTextField3, adrButton3, dirButton3, cmdButton3);
				updateLocoFields (r, 2, engTextField4, adrButton4, dirButton4, cmdButton4);
				updateLocoFields (r, 4, engTextField5, adrButton5, dirButton5, cmdButton5);
				updateLocoFields (r, 6, engTextField6, adrButton6, dirButton6, cmdButton6);
				
			}

			// read the next consist engine number
			if (engineNum == 0 || engineNum == 1) {
				engineNum++;

				byte[] bl = readConsistMemory(consistNum, engineNum);
				sendNceMessage(bl, REPLY_16);
			
			// done reading current consist
			}else {

				if (!consistSearchInc && !consistSearchDec){
					consistReply.setText("okay");
					return;
				}
				
				if (consistSearchInc) {
					consistNum--;
					if (consistNum < CONSIST_MIN)
						consistNum = CONSIST_MAX;
				}

				if (consistSearchDec) {
					consistNum++;
					if (consistNum > CONSIST_MAX)
						consistNum = CONSIST_MIN;
				}

				byte[] bl = readConsistMemory(consistNum, 0);
				sendNceMessage(bl, REPLY_16);

			}
		}
	}
	
	// update loco fields, returns false if loco address is null
	private boolean updateLocoFields (NceReply r, int i, JTextField engTextField, JButton adrButton, JButton dirButton, JButton cmdButton ){
		
		String locoAddr = getLocoAddr(r, i);
		boolean locoType = getLocoType (r, i); 
		
		engTextField.setText(locoAddr);
		
		
		if (locoAddr == EMPTY){
			
			engTextField.setEnabled(true);
			
			cmdButton.setText(ADD);
			cmdButton.setVisible(true);
			cmdButton.setEnabled(false);
			cmdButton.setToolTipText(ToolTipAdd);
			
			dirButton.setText(QUESTION);
			dirButton.setEnabled(true);
			
			adrButton.setText(LONG);
			adrButton.setEnabled(true);
			
			return false;
			
		}else{
			
			engTextField.setEnabled(false);
			
			// can not delete lead or rear locos, but can replace
			if (engTextField == engTextField1 || engTextField == engTextField2){
				
				cmdButton.setText(REPLACE);
				cmdButton.setEnabled(true);
				cmdButton.setToolTipText("Press to delete and replace this engine");
				
			} else {

				cmdButton.setText(DELETE);
				cmdButton.setEnabled(true);
				cmdButton.setToolTipText("Press to delete this engine from consist");

			}
				
			dirButton.setText(QUESTION);
			dirButton.setEnabled(false);
			
			adrButton.setText((locoType) ? LONG : SHORT);
			adrButton.setEnabled(false);
			
			return true;
		}
	}
	
	// modify loco fields
	private void modifyLocoFields(JTextField engTextField, JButton adrButton,
			JButton dirButton, JButton cmdButton) {
		
		if (engTextField.getText().equals (EMPTY)) {
			
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter loco address before pressing add", "NCE Consist",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		byte[] bl;
		byte cN = (byte) Integer.parseInt(consistTextField.getText());

		int locoAddr = Integer.parseInt(engTextField.getText());
		if (adrButton.getText() == LONG) 
			locoAddr += 0xC000;
		

		if (cmdButton.getText() == DELETE) {

			bl = NceBinaryCommand.nceLocoCmd(locoAddr,
					NceBinaryCommand.LOC_CMD_DELETE_LOC_CONSIST, (byte) 0);
			sendNceMessage(bl, REPLY_1);

		}else if (cmdButton.getText() == REPLACE){
			
			//Kill refresh flag, no update when replacing loco 
			refresh = false;
			
			bl = NceBinaryCommand.nceLocoCmd(locoAddr,
					NceBinaryCommand.LOC_CMD_DELETE_LOC_CONSIST, (byte) 0);
			sendNceMessage(bl, REPLY_1);
			
			//now allow user to add loco to lead or rear consist
	
			engTextField.setText(EMPTY);
			engTextField.setEnabled (true);
			adrButton.setEnabled (true);
			dirButton.setEnabled(true);
			cmdButton.setText(ADD);
			cmdButton.setToolTipText(ToolTipAdd);
			
			// now update CS memory in case user doesn't use the Add button
			
			if (engTextField == engTextField1) {

				bl = NceBinaryCommand.nceLocoCmd(0,
						NceBinaryCommand.LOC_CMD_FWD_CONSIST_LEAD, cN);
				sendNceMessage(bl, REPLY_1);
				
				// no lead loco so we can't kill the consist
				clearButton.setEnabled (false);
				
			}else{
				
				bl = NceBinaryCommand.nceLocoCmd(0,
						NceBinaryCommand.LOC_CMD_FWD_CONSIST_REAR, cN);
				sendNceMessage(bl, REPLY_1);
				
			}

		} else {
			
			// ADD button has been pressed
			if (dirButton.getText() == QUESTION){
				
				JOptionPane.showMessageDialog(NceConsistEditFrame.this,
						"Set loco direction before adding to consist", "NCE Consist",
						JOptionPane.ERROR_MESSAGE);
				
				return;
			}
			
			// now we need to determine if lead, rear, or mid loco
			
			// lead loco?
			if (engTextField == engTextField1){
				
				if (dirButton.getText() == FWD) {
					bl = NceBinaryCommand.nceLocoCmd(locoAddr,
							NceBinaryCommand.LOC_CMD_FWD_CONSIST_LEAD, cN);
					sendNceMessage(bl, REPLY_1);
				}
				if (dirButton.getText() == REV) {

					bl = NceBinaryCommand.nceLocoCmd(locoAddr,
							NceBinaryCommand.LOC_CMD_REV_CONSIST_LEAD, cN);
					sendNceMessage(bl, REPLY_1);
				}
			
			// rear loco?
			}else if (engTextField == engTextField2){
				
				if (dirButton.getText() == FWD) {
					bl = NceBinaryCommand.nceLocoCmd(locoAddr,
							NceBinaryCommand.LOC_CMD_FWD_CONSIST_REAR, cN);
					sendNceMessage(bl, REPLY_1);
				}
				if (dirButton.getText() == REV) {

					bl = NceBinaryCommand.nceLocoCmd(locoAddr,
							NceBinaryCommand.LOC_CMD_REV_CONSIST_REAR, cN);
					sendNceMessage(bl, REPLY_1);
				}
			
			// must be mid loco
			} else {

				if (dirButton.getText() == FWD) {
					bl = NceBinaryCommand.nceLocoCmd(locoAddr,
							NceBinaryCommand.LOC_CMD_FWD_CONSIST_MID, cN);
					sendNceMessage(bl, REPLY_1);
				}
				if (dirButton.getText() == REV) {

					bl = NceBinaryCommand.nceLocoCmd(locoAddr,
							NceBinaryCommand.LOC_CMD_REV_CONSIST_MID, cN);
					sendNceMessage(bl, REPLY_1);
				}
			}
		}
	}
	
	private void sendNceMessage (byte[] b, int replyLength){
		NceMessage m = NceMessage.createBinaryMessage(b, replyLength);
		int testcount = 0;
		waiting++;
		replyLen = replyLength;			// Expect 1 byte response

		NceTrafficController.instance().sendNceMessage(m, this);
		
	}
	
	// get loco address type, returns true if long
	private boolean getLocoType (NceReply r, int i){
		int rC = r.getElement(i);
		rC = rC&0xC0;		// long address if 2 msb are set
		if (rC == 0xC0){
			return true;
		}else{
			return false;
		}
	}

	private String getLocoAddr(NceReply r, int i){
		
		int rC = r.getElement(i++);
		rC = (rC << 8)&0x3F00;
		int rC_l = r.getElement(i);
		rC_l = rC_l&0xFF;
		rC = rC + rC_l;
		String locoAddr = EMPTY;
		if (rC != 0){
			locoAddr = Integer.toString (rC);
		}
		return locoAddr;
	}
 
    
    private void addEngRow (JComponent col1, JComponent col2, JComponent col3, JComponent col4, JComponent col5, int row){
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
    	getContentPane().add(c, gc);
    }
    
    private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}
    
	private void addButtonAdrAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionAdrPerformed(e);
			}
		});
	}  

	private void addButtonDirAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionDirPerformed(e);
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
     
    private void addCheckBoxAction (JCheckBox cb){
		cb.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionPerformed(e);
			}
		});
    } 
    
    //  initialize engine fields
    private void initEngFields() {
    	initEngRow(1, "Lead", textEng1, engTextField1, adrButton1, dirButton1, cmdButton1);
    	initEngRow(2, "Rear", textEng2, engTextField2, adrButton2, dirButton2, cmdButton2);
    	initEngRow(3, "Mid 1", textEng3, engTextField3, adrButton3, dirButton3, cmdButton3);
    	initEngRow(4, "Mid 2", textEng4, engTextField4, adrButton4, dirButton4, cmdButton4);
    	initEngRow(5, "Mid 3", textEng5, engTextField5, adrButton5, dirButton5, cmdButton5);
    	initEngRow(6, "Mid 4", textEng6, engTextField6, adrButton6, dirButton6, cmdButton6);
	}
    
    private void initEngRow(int row, String s, JLabel textEng, JTextField engTextField, JButton adrButton, JButton dirButton, JButton cmdButton) {
		
		textEng.setText(s);
		textEng.setVisible(true);
		
		
		adrButton.setText(LONG);
		adrButton.setVisible(true);
		adrButton.setEnabled(false);
		adrButton.setToolTipText("Press to change address type");
		
		dirButton.setText(QUESTION);
		dirButton.setVisible(true);
		dirButton.setEnabled(false);
		dirButton.setToolTipText("Press to change engine direction");
		
		cmdButton.setText(ADD);
		cmdButton.setVisible(true);
		cmdButton.setEnabled(false);
		cmdButton.setToolTipText(ToolTipAdd);

		engTextField.setText(EMPTY);
		engTextField.setToolTipText("Enter engine address");
		engTextField.setMaximumSize(new Dimension(engTextField
				.getMaximumSize().width,
				engTextField.getPreferredSize().height));
    }
    
   
   
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceConsistEditFrame.class.getName());	
}

