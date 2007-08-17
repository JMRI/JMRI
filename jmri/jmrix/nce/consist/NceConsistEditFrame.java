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
 * @version $Revision: 1.3 $
 */

public class NceConsistEditFrame extends jmri.util.JmriJFrame implements jmri.jmrix.nce.NceListener {

	private static final int CS_CONSIST_MEM = 0xF500;	// start of NCE CS Consist memory 
	private static final int CS_CON_MEM_REAR = 0xF600;	// start of rear consist engines
	private static final int CS_CON_MEM_MID = 0xF700;	// start of mid consist engines
	private static final int CONSIST_MIN = 0;
	private static final int CONSIST_MAX = 127;
	private int consistNum = 127;					// consist being worked
	private int engineNum = 0; 						// which engine 
	private static final int REPLY_1 = 1;			// reply length of 1 byte expected
	private static final int REPLY_16 = 16;			// reply length of 16 bytes expected
	private int replyLen = 0;						// expected byte length
	private int waiting = 0;						// to catch responses not intended for this module
	
	private static final String DELETE = "Delete";
	private static final String ADD = " Add ";
	private static final String QUESTION = "  ??  ";
	private static final String FWD = "Forward";
	private static final String REV = "Reverse";
	
	
	private boolean consistSearchInc = false;		// next search
	private boolean consistSearchDec = false;		// previous search
	private int consistCount = 0;						// search count not to exceed CONSIST_MAX
	private boolean secondRead = false;				// when true, another 16 byte read expected
	private boolean consistValid = false;			// when true, NCE CS has responed to consist read
	private boolean consistModified = false;		// when true, consist has been modified by user
	
	// member declarations
    javax.swing.JLabel textConsist = new javax.swing.JLabel();
    javax.swing.JLabel textReply = new javax.swing.JLabel();
    javax.swing.JLabel consistReply = new javax.swing.JLabel();
    
    // major buttons
    javax.swing.JButton previousButton = new javax.swing.JButton();
    javax.swing.JButton nextButton = new javax.swing.JButton();
    javax.swing.JButton getButton = new javax.swing.JButton();
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
    javax.swing.JLabel textDirection = new javax.swing.JLabel();
    
    // for padding out panel
    javax.swing.JLabel space1 = new javax.swing.JLabel();
    javax.swing.JLabel space2 = new javax.swing.JLabel();
    
    // lead engine
    javax.swing.JLabel num1 = new javax.swing.JLabel();
    javax.swing.JLabel textEng1 = new javax.swing.JLabel();
    javax.swing.JTextField engTextField1 = new javax.swing.JTextField(4);
    javax.swing.JButton cmdButton1 = new javax.swing.JButton();
    javax.swing.JButton dirButton1 = new javax.swing.JButton();
    
    //  rear engine
    javax.swing.JLabel num2 = new javax.swing.JLabel();
    javax.swing.JLabel textEng2 = new javax.swing.JLabel();
    javax.swing.JTextField engTextField2 = new javax.swing.JTextField(4);
    javax.swing.JButton cmdButton2 = new javax.swing.JButton();
    javax.swing.JButton dirButton2 = new javax.swing.JButton();
    
    //  mid engine
    javax.swing.JLabel num3 = new javax.swing.JLabel();
    javax.swing.JLabel textEng3 = new javax.swing.JLabel();
    javax.swing.JTextField engTextField3 = new javax.swing.JTextField(4);
    javax.swing.JButton cmdButton3 = new javax.swing.JButton();
    javax.swing.JButton dirButton3 = new javax.swing.JButton();
    
    //  mid engine
    javax.swing.JLabel num4 = new javax.swing.JLabel();
    javax.swing.JLabel textEng4 = new javax.swing.JLabel();
    javax.swing.JTextField engTextField4 = new javax.swing.JTextField(4);
    javax.swing.JButton cmdButton4 = new javax.swing.JButton();
    javax.swing.JButton dirButton4 = new javax.swing.JButton();
    
    //  mid engine
    javax.swing.JLabel num5 = new javax.swing.JLabel();
    javax.swing.JLabel textEng5 = new javax.swing.JLabel();
    javax.swing.JTextField engTextField5 = new javax.swing.JTextField(4);
    javax.swing.JButton cmdButton5 = new javax.swing.JButton();
    javax.swing.JButton dirButton5 = new javax.swing.JButton();
    
    //  mid engine
    javax.swing.JLabel num6 = new javax.swing.JLabel();
    javax.swing.JLabel textEng6 = new javax.swing.JLabel();
    javax.swing.JTextField engTextField6 = new javax.swing.JTextField(4);
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
		consistTextField.setToolTipText("Enter consist 0 to 127");
		consistTextField.setMaximumSize(new Dimension(consistTextField
				.getMaximumSize().width, consistTextField.getPreferredSize().height));
        
		textEngine.setText("Engine");
		textEngine.setVisible(true);
		textAddress.setText("Address");
		textAddress.setVisible(true);
		textDirection.setText("Direction");
		textDirection.setVisible(true);
		
		
		
		saveButton.setText("Save");
        saveButton.setVisible(true);
        saveButton.setEnabled (false);
        saveButton.setToolTipText("Update consist in NCE CS");
        
        backUpButton.setText("Backup");
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
        
        addItem(textEngine, 1,4);
        addItem(textAddress, 2,4);
        addItem(textDirection, 3,4);
        
        // row 5 Lead Engine
        addEngRow (num1, textEng1, engTextField1, dirButton1, cmdButton1,  5);
          
        // row 6 Rear Engine
        addEngRow (num2, textEng2, engTextField2, dirButton2, cmdButton2,  6);
        
        // row 7 Mid Engine
        addEngRow (num3, textEng3, engTextField3, dirButton3, cmdButton3,  7);
        
        // row 8 Mid Engine
        addEngRow (num4, textEng4, engTextField4, dirButton4, cmdButton4,  8);
        
        // row 9 Mid Engine
        addEngRow (num5, textEng5, engTextField5, dirButton5, cmdButton5,  9);
        
        // row 10 Mid Engine
        addEngRow (num6, textEng6, engTextField6, dirButton6, cmdButton6,  10);
        
        // row 15 padding for looks
        addItem(space2, 2,15);
        
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
 
    // Previous, Next, Get, Save, Restore & Backup buttons
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
    	
		// if we're searching ignore user 
		if (consistSearchInc || consistSearchDec)
			return;

		if (ae.getSource() == saveButton) {
//			boolean status = saveConsist();
//			if (status) // was save successful?
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
				consistCount = 0; // used to determine if all 128 consist have been read
				consistSearchDec = true;
				consistNum = getConsist();	// check for valid and kick off read process
				if (consistNum == -1) 	// Error user input incorrect
					consistSearchDec = false;
			}
			if (ae.getSource() == nextButton) {
				consistCount = 0; // used to determine if all 256 macros have been
				// read
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

	
	public void buttonActionCmdPerformed(java.awt.event.ActionEvent ae) {
		
		// if we're searching ignore user 
		if (consistSearchInc || consistSearchDec)
			return;
		

	}
	
	public void buttonActionDirPerformed(java.awt.event.ActionEvent ae) {
		
		// if we're searching ignore user 
		if (consistSearchInc || consistSearchDec)
			return;
		

	}


	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		getConsist();
	}
    
    // gets the user supplied consist number and then reads NCE CS memory
    private int getConsist (){
    	int cN = validConsist (consistTextField.getText());
		if (cN == -1) {
			consistReply.setText("error");
			JOptionPane.showMessageDialog(NceConsistEditFrame.this,
					"Enter consist number 0 to 127", "NCE Consist",
					JOptionPane.ERROR_MESSAGE);
			consistValid = false;
			return cN;
		}
		if (consistSearchInc || consistSearchDec) {
			consistReply.setText("searching");
		}else{
			consistReply.setText("waiting");
		}
		
		NceMessage m1 = readConsistMemory (cN, 0);
		NceTrafficController.instance().sendNceMessage(m1, this);
		
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
    
    // Reads 16 bytes of NCE consist memory based on consist number and 
    // engine number 0=lead 1=rear 2=mid 
    private NceMessage readConsistMemory(int consistNum, int eNum) {
    	
    	engineNum = eNum;
    	
    	int nceConsistBaseMemory = CS_CONSIST_MEM;
    	if (eNum == 1)
    		nceConsistBaseMemory = CS_CON_MEM_REAR;
    	int nceMacroAddr = (consistNum * 2) + nceConsistBaseMemory;
    	if (eNum == 2){
    		nceConsistBaseMemory = CS_CON_MEM_MID;
    		nceMacroAddr = (consistNum * 8) + nceConsistBaseMemory;
    	}
 
    	replyLen = REPLY_16;			// Expect 16 byte response
    	waiting++;
		byte[] bl = NceBinaryCommand.accMemoryRead(nceMacroAddr);
		NceMessage m = NceMessage.createBinaryMessage(bl, REPLY_16);
		return m;
    }
 
    
    // display save button
    private void setSaveButton(boolean display) {
		consistModified = display;
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
			consistReply.setText("error");
			return;
		}
		// Consist command
		if (replyLen == REPLY_1) {
			// Looking for proper response
			int recChar = r.getElement(0);
			if (recChar == '!')
				consistReply.setText("okay");
			if (recChar == '0')
				consistReply.setText("empty");
		}
		// Consist memory read
		if (replyLen == REPLY_16) {
			String recChar = getChar(r, 0);

			// load lead engine
			if (engineNum == 0) {

				engTextField1.setText(recChar);

				// are we searching?
				if (consistSearchInc || consistSearchDec) {
					
					consistTextField.setText(Integer.toString(consistNum));
					
					if (checkBoxEmpty.isSelected()) {
						if (recChar == "Empty") {
							if (consistCount > 0) {
								consistSearchInc = false;
								consistSearchDec = false;
							}
						}

					} else {
						if (recChar != "Empty") {
							if (consistCount > 0) {
								consistSearchInc = false;
								consistSearchDec = false;
							}
						}
					}
					if (consistCount++ > CONSIST_MAX) {
						consistSearchInc = false;
						consistSearchDec = false;
					}
				}

				// load rear engine
			} else if (engineNum == 1) {

				engTextField2.setText(recChar);

				// load mid engines
			} else {

				engTextField3.setText(recChar);
				recChar = getChar(r, 2);
				engTextField4.setText(recChar);
				recChar = getChar(r, 4);
				engTextField5.setText(recChar);
				recChar = getChar(r, 6);
				engTextField6.setText(recChar);
				
			}

			// read the next consist engine number
			if (engineNum == 0 || engineNum == 1) {
				engineNum++;

				NceMessage m = readConsistMemory(consistNum, engineNum);
				NceTrafficController.instance().sendNceMessage(m, this);
			
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

				NceMessage m = readConsistMemory(consistNum, 0);
				NceTrafficController.instance().sendNceMessage(m, this);

			}
		}

	}

	private String getChar(NceReply r, int i){
		
		int rC = r.getElement(i++);
		rC = rC << 8;
		rC = rC + r.getElement(i);
		String recChar = "Empty";
		if (rC != 0){
			recChar = Integer.toString (rC);
		}
		return recChar;
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
    	initEngRow(1, "Lead", num1, textEng1, engTextField1, dirButton1, cmdButton1);
    	initEngRow(2, "Rear", num2, textEng2, engTextField2, dirButton2, cmdButton2);
    	initEngRow(3, "Mid 1", num3, textEng3, engTextField3, dirButton3, cmdButton3);
    	initEngRow(4, "Mid 2", num4, textEng4, engTextField4, dirButton4, cmdButton4);
    	initEngRow(5, "Mid 3", num5, textEng5, engTextField5, dirButton5, cmdButton5);
    	initEngRow(6, "Mid 4", num6, textEng6, engTextField6, dirButton6, cmdButton6);
	}
    
    private void initEngRow(int row, String s, JLabel num, JLabel textEng, JTextField engTextField, JButton dirButton, JButton cmdButton) {
		num.setText(Integer.toString(row));
		num.setVisible(true);
		
		textEng.setText(s);
		textEng.setVisible(true);
		
		dirButton.setText(QUESTION);
		dirButton.setVisible(true);
		dirButton.setEnabled(false);
		dirButton.setToolTipText("Press to change engine direction");
		
		cmdButton.setText(ADD);
		cmdButton.setVisible(false);
		cmdButton.setToolTipText("");

		engTextField.setText("");
		engTextField.setToolTipText("Enter engine address");
		engTextField.setMaximumSize(new Dimension(engTextField
				.getMaximumSize().width,
				engTextField.getPreferredSize().height));
    }
    
   
   
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceConsistEditFrame.class.getName());	
}

