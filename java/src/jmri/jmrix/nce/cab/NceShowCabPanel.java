// NceShowCabPanel.java

package jmri.jmrix.nce.cab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import javax.swing.JButton;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;


/**
 * Frame to display NCE cabs
 * 
 * Note, NCE bit layout MSB = bit 7, LSB = bit 0.
 * 
 * From Jim Scorse at NCE:
 * 
 * Each cab has a 256 byte "context page" in system RAM These pages start at
 * 0x8000 in system RAM with Cab 0 at 0x8800, cab 1 at 0x8900, Cab 2 at 0x8a00,
 * etc.
 * 
 * Below is a list of offsets (in decimal) into the cab context page for useful
 * memory locations.
 * 
 * For example if you want to know the current speed of cab address 2's
 * currently selected loco look at memory location 0x8a00 + 32 dec (0x20). This
 * will be address 0x8a20.
 * 
 * 
 * To determine if a cab is active (plugged in at any point this session) you
 * will need to look at the byte "FLAGS1" (offset 101)
 * 
 * If bit 1 of FLAGS1 = 1 then the cab has been talked to at least once by the
 * command station.
 * 
 * Bits 0 and 7 indicate the type of cab being use this session at this cab
 * address
 * 
 * Bit 7,0 = 0,0 Procab or other cab with an LCD display (type A)
 * Bit 7,0 = 0,1 Cab04 other cab without an LCD (type B)
 * Bit 7,0 = 1,0 USB or similar device  (type C)
 * Bit 7,0 = 1,1 AIU or similar device (type D)
 * 
 * 
 * CAB_BASE EQU 0 ; 
 * LCD_TOP_LINE EQU 0 ;16 chars (in ASCII) for top line of LCD
 * LCD_BOT_LINE EQU 16 ;16 chars (in ASCII) for bottom line of LCD
 * 
 * CURR_SPEED EQU 32 ;this cab's current speed 
 * ADDR_H EQU 33 ;loco address, high byte 
 * ADDR_L EQU 34 ;loco address, low byte 
 * FLAGS EQU 35 ;bit 0 - Do not use 
 * 				;bit 1 - 1=128 speed mode, 0=28 speed mode 
 * 				;bit 2 - 1=forward, 0=reverse 
 * 				;bit 3 - Do not use 
 * 				;bit 4 - Do not use 
 * 				;bit 5 - Do not use 
 * 				;bit 6 - Do not use
 * 				;bit 7 - 1=rear loco of consist is active address use reverse speeds
 * 
 * FUNCTION_L EQU 36 
 * 				;bit 0 = function 1, 1=on, 0=off 
 * 				;bit 1 = function 2, 1=on, 0=off 
 * 				;bit 2 = function 3, 1=on, 0=off 
 * 				;bit 3 = function 4, 1=on, 0=off 
 * 				;bit 4 = headlight, 1=on, 0=off
 * 
 * FUNCTION_H EQU 37 
 * 				;bit 0 = function 5, 1=on, 0=off 
 * 				;bit 1 = function 6, 1=on, 0=off 
 * 				;bit 2 = function 7, 1=on, 0=off 
 * 				;bit 3 = function 8, 1=on, 0=off 
 * 				;bit 4 = function 9, 1=on, 0=off 
 * 				;bit 5 = function 10, 1=on, 0=off 
 * 				;bit 6 = function 11, 1=on, 0=off 
 * 				;bit 7 = function 12, 1=on, 0=off
 * 
 * ALIAS EQU 38 ;If loco is in consist this is the consist address
 * 
 * 
 * FUNC13_20 EQU 82 ;bit map of current functions (bit 0=F13) 
 * FUNC21_28 EQU 83 ;bit map of current functions (bit 0=F21)
 * 
 * ACC_AD_H EQU 90 ;current accessory address high byte 
 * ACC_AD_L EQU 91 ;current accessory address low byte
 * 
 * ;lower nibble bit 0 =1 if setup advanced consist in process 
 * 
 * FLAGS2 EQU 93 
 * 				;bit 0 = \ 
 * 				;bit 1 =  >Number of recalls for this cab 
 * 				;bit 2 = / 1-6 valid 
 * 				;bit 3 = 1=refresh LCD on ProCab 
 * 				;bit 4 = Do not use 
 * 				;bit 5 = Do not use 
 * 				;bit 6 = Do not use 
 * 				;bit 7 = Do not use
 * 
 * FLAGS1 EQU 101 
 * 				;bit0 - 0 = type a or type C cab, 1 = type b or type d 
 * 				;bit1 - 0 = cab type not determined, 1 = it has 
 * 				;bit2 - 0 = Do not use 
 * 				;bit3 - 0 = Do not use 
 * 				;bit4 - 0 = Do not use 
 * 				;bit5 - 0 = Do not use 
 * 				;bit6 - 0 = Do not use
 * 				;bit7 - 0 = type a or type b cab, 1=type c or d
 * Writing zero to FLAGS1 will remove the cab from the 'active' list
 * 
 * @author Dan Boudreau Copyright (C) 2009, 2010
 * @author Ken Cameron Copyright (C) 2010
 * @version $Revision$
 */

public class NceShowCabPanel extends jmri.jmrix.nce.swing.NcePanel implements jmri.jmrix.nce.NceListener {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.cab.NceShowCabBundle");
	
	private static final int CS_CAB_MEM = 0x8800;	// start of NCE CS cab context page for cab 0
													// memory
	private static final int CAB_LINE_1 = 0;		// start of first line for cab display
	private static final int CAB_LINE_2 = 16;		// start of second line for cab display
	private static final int CAB_SIZE = 256;		// Each cab has 256 bytes
	private static final int CAB_CURR_SPEED = 32;	// NCE cab speed
	private static final int CAB_ADDR_H = 33; 		// loco address, high byte
	private static final int CAB_ADDR_L = 34; 		// loco address, low byte
	private static final int CAB_FLAGS = 35;		// FLAGS
	private static final int CAB_FUNC_L = 36;		// Function keys low
	private static final int CAB_FUNC_H = 37;		// Function keys high
	private static final int CAB_ALIAS = 38;		// Consist address
	private static final int CAB_FUNC_13_20 = 82;	// Function keys 13 - 30
	private static final int CAB_FUNC_21_28 = 83;	// Function keys 21 - 28
	private static final int CAB_FLAGS1 = 101;		// NCE flag 1
	private static final int CAB_MAX = 64;			// There are up to 64 cabs
	private static final int REPLY_1 = 1;			// reply length of 1 byte
	private static final int REPLY_16 = 16;			// reply length of 16 bytes	
	private static final int CAB_LINE_LEN = 16;			// display line length of 16 bytes			
	
	private int replyLen = 0;						// expected byte length
	private int waiting = 0;						// to catch responses not
													// intended for this module
	private static final int FLAGS1_CABID_DISPLAY = 0x00;	// bit 0=0, bit 7=0;
	private static final int FLAGS1_CABID_NODISP = 0x01;	// bit 0=1, bit 7=0;
	private static final int FLAGS1_CABID_USB = 0x80;		// bit 0=0, bit 7=1;
	private static final int FLAGS1_CABID_AIU = 0x81;		// bit 0=1, bit 7=1;
	private static final int FLAGS1_CABISACTIVE = 0x02;	// if cab is active
	private static final int FLAGS1_MASK_CABID = 0x81;	// Only bits 0 and 7.
	private static final int FLAGS1_MASK_CABISACTIVE = 0x02;	// if cab is active
	
	private static final int FUNC_L_F0 = 0x10;		// F0 or headlight
	private static final int FUNC_L_F1 = 0x01;		// F1
	private static final int FUNC_L_F2 = 0x02;		// F2
	private static final int FUNC_L_F3 = 0x04;		// F3
	private static final int FUNC_L_F4 = 0x08;		// F4
	
	private static final int FUNC_H_F5 = 0x01;		// F5
	private static final int FUNC_H_F6 = 0x02;		// F6
	private static final int FUNC_H_F7 = 0x04;		// F7
	private static final int FUNC_H_F8 = 0x08;		// F8
	private static final int FUNC_H_F9 = 0x10;		// F9
	private static final int FUNC_H_F10 = 0x20;		// F10
	private static final int FUNC_H_F11 = 0x40;		// F11
	private static final int FUNC_H_F12 = 0x80;		// F12
	
	private static final int FUNC_H_F13 = 0x01;		// F13
	private static final int FUNC_H_F14 = 0x02;		// F14
	private static final int FUNC_H_F15 = 0x04;		// F15
	private static final int FUNC_H_F16 = 0x08;		// F16
	private static final int FUNC_H_F17 = 0x10;		// F17
	private static final int FUNC_H_F18 = 0x20;		// F18
	private static final int FUNC_H_F19 = 0x40;		// F10
	private static final int FUNC_H_F20 = 0x80;		// F20
	
	private static final int FUNC_H_F21 = 0x01;		// F21
	private static final int FUNC_H_F22 = 0x02;		// F22
	private static final int FUNC_H_F23 = 0x04;		// F23
	private static final int FUNC_H_F24 = 0x08;		// F24
	private static final int FUNC_H_F25 = 0x10;		// F25
	private static final int FUNC_H_F26 = 0x20;		// F26
	private static final int FUNC_H_F27 = 0x40;		// F27
	private static final int FUNC_H_F28 = 0x80;		// F28
	
	Thread NceCabUpdateThread;	
	private int[] cabFlag1Array = new int[CAB_MAX];
	private Calendar[] cabLastChangeArray = new Calendar[CAB_MAX];
	private int[] cabSpeedArray = new int[CAB_MAX];
	private int[] cabFlagsArray = new int[CAB_MAX];
	private int[] cabLocoArray = new int[CAB_MAX];
	private int[] cabConsistArray = new int[CAB_MAX];
	private int[] cabF0Array = new int[CAB_MAX];
	private int[] cabF5Array = new int[CAB_MAX];
	private int[] cabF13Array = new int[CAB_MAX];
	private int[] cabF21Array = new int[CAB_MAX];
	private int[][] cabLine1Array = new int[CAB_MAX][CAB_LINE_LEN];
	private int[][] cabLine2Array = new int[CAB_MAX][CAB_LINE_LEN];
	
	// member declarations
	JLabel textNumer = new JLabel(rb.getString("Number"));
    JLabel textCab = new JLabel(rb.getString("Type"));
    JLabel textAddress = new JLabel(rb.getString("Loco"));
    JLabel textSpeed = new JLabel(rb.getString("Speed"));
    JLabel textConsist = new JLabel(rb.getString("Consist"));
    JLabel textFunctions = new JLabel(rb.getString("Functions"));
    JLabel textDisplay1 = new JLabel(rb.getString("Display1"));
    JLabel textDisplay2 = new JLabel(rb.getString("Display2"));
    JLabel textReply = new JLabel(rb.getString("Reply"));
    JLabel textStatus = new JLabel("");
    JLabel textLastUsed = new JLabel(rb.getString("LastUsed"));
    
    // major buttons
    JButton refreshButton = new JButton(rb.getString("Refresh"));
    JButton purgeButton = new JButton(rb.getString("PurgeCab"));
    
    // check boxes
    JCheckBox checkBoxActive = new JCheckBox(rb.getString("Active"));
    // text field
    JTextField purgeCabId = new JTextField(3);
    
    // for padding out panel
    JLabel space1a = new JLabel("    ");
    JLabel space1b = new JLabel("    ");
    JLabel space1c = new JLabel("    ");
    JLabel space1d = new JLabel("    ");
    JLabel space2 = new JLabel(" ");
    JLabel space3 = new JLabel(" ");
    JLabel space4 = new JLabel(" ");
    JLabel space5 = new JLabel(" ");
    
    JPanel cabsPanel = new JPanel();
    JScrollPane cabsPane;

    private NceTrafficController tc = null;

    public NceShowCabPanel() {
    	super();
    }
    
    public void initContext(Object context) throws Exception {
        if (context instanceof NceSystemConnectionMemo ) {
            try {
				initComponents((NceSystemConnectionMemo) context);
			} catch (Exception e) {
				
			}
        }
    }

    public String getHelpTarget() { return "package.jmri.jmrix.nce.cab.NceShowCabFrame"; }

    public String getTitle() { 
    	StringBuilder x = new StringBuilder();
    	if (memo != null) {
    		x.append(memo.getUserName());
    	} else {
    		x.append("NCE_");
    	}
		x.append(": ");
    	x.append(rb.getString("Title"));
        return x.toString(); 
    }
    
    public void initComponents(NceSystemConnectionMemo m) throws Exception {
    	this.memo = m;
        this.tc = m.getNceTrafficController();
        cabsPane = new JScrollPane(cabsPanel);
    	cabsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    	
    	// the following code sets the frame's initial state
    	
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	cabsPane.setVisible(false);
    	//cabsPane.setMinimumSize(new Dimension(300,300));
    	
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
    	// row 1
    	refreshButton.setToolTipText(rb.getString("RefreshToolTip"));
    	checkBoxActive.setToolTipText(rb.getString("CheckBoxActiveToolTip"));
    	checkBoxActive.setSelected(true);
    	purgeCabId.setToolTipText(rb.getString("PurgeCabIdToolTip"));
    	purgeButton.setToolTipText(rb.getString("PurgeButtonToolTip"));
    	addItem(p1, refreshButton, 2, 1);
    	addItem(p1, space1a, 3, 1);
    	addItem(p1, textStatus, 4, 1);
    	addItem(p1, space1b, 5, 1);
    	addItem(p1, checkBoxActive, 6, 1);
    	addItem(p1, space1c, 7, 1);
    	addItem(p1, purgeCabId, 10, 1);
    	addItem(p1, space1d, 11, 1);
    	addItem(p1, purgeButton, 12, 1);
    	
    	// row 2
    	addItem(p1, space2, 4, 2);
    	
        // row 3
//    	JPanel p2 = new JPanel();
//    	p2.setLayout(new GridBagLayout());
//        addItem(p2, textNumer, 1, 3);
//        addItemLeft(p2, textCab, 2, 3);
//        addItemLeft(p2, textAddress, 3, 3);
//        addItemLeft(p2, textSpeed, 4, 3);
//        addItemLeft(p2, textConsist, 5, 3);
//        addItemLeft(p2, textFunctions, 6, 3);
//        addItemLeft(p2, textDisplay1, 7, 3);
//        addItemLeft(p2, textDisplay2, 8, 3);
//        addItemLeft(p2, textLastUsed, 9, 3);
        
    	addButtonAction(refreshButton);
    	addButtonAction(purgeButton);
    	
    	add(p1);
//    	add(p2);
    	add(cabsPane);
    	
    	// pad out panel
    	cabsPanel.setLayout(new GridBagLayout());
    	cabsPanel.setVisible(true);
    	addItem(cabsPanel, space3, 0, 0);
    	addItem(cabsPanel, space4, 0, 1);
    	addItem(cabsPanel, space5, 0, 2);
    	cabsPane.setVisible(true);
    	refreshPanel();
    }

    // refresh button
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
    	Object src = ae.getSource();
    	if (src == refreshButton) {
    		refreshPanel();
    	} else if (src == purgeButton) {
    		purgeCab();
    	} else {
    		log.error("unknown action performed: " + src);
    	}
	}
 
    private void purgeCab() {
    	// if id provided
    	int cab = 0;
    	try {
        	cab = Integer.parseInt(purgeCabId.getText().trim());
    	} catch (NumberFormatException e) {
            log.error(rb.getString("ErrorInvalidValue") + purgeCabId.getText().trim());
    		return;
    	}
    	if (cab < 1 || cab >= CAB_MAX) {
            log.error(rb.getString("ErrorValueRange") + purgeCabId.getText().trim());
    		return;
    	}
    	// if id is active
    	int act = cabFlag1Array[cab] & FLAGS1_MASK_CABISACTIVE;
    	if (act != FLAGS1_CABISACTIVE) {
            log.error(rb.getString("ErrorCabNotActive") + purgeCabId.getText().trim());
    	}
    	// clear bit for active and cab type details
    	writeCabMemory1(cab, CAB_FLAGS1, 0);
    	purgeCabId.setText(" ");
    	// update the display
    	refreshPanel();
    	return;
    }
    
    private void refreshPanel(){
    	// Set up a separate thread to read CS memory
        if (NceCabUpdateThread != null && NceCabUpdateThread.isAlive())	
        	return; // thread is already running
    	textStatus.setText(rb.getString("StatusReadingMemory"));
        waiting = 0;
    	NceCabUpdateThread = new Thread(new Runnable() {
    		public void run() {
    			cabUpdate();
    		}
    	});
    	NceCabUpdateThread.setName(rb.getString("ThreadTitle"));
    	NceCabUpdateThread.setPriority(Thread.MIN_PRIORITY);
    	NceCabUpdateThread.start();
    }
    
    private boolean firstTime = true; // wait for panel to display
    // Thread to update cab info, allows the use of sleep or wait
    private void cabUpdate() {
    	if (firstTime){
    		try {
    			Thread.sleep(1000);	// wait for panel to display 
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	firstTime = false;
    	cabsPanel.removeAll();
    	// build header
    	addItem(cabsPanel, textNumer, 1, 0);	// number
    	addItem(cabsPanel, textCab, 2, 0);		// type
    	addItem(cabsPanel, textAddress, 3, 0);	// address
      	addItem(cabsPanel, textSpeed, 4, 0);	// speed
      	addItem(cabsPanel, textConsist, 5, 0);	// consist
      	addItem(cabsPanel, textFunctions, 6, 0);// functions
      	addItem(cabsPanel, textDisplay1, 7, 0);	// line1
      	addItem(cabsPanel, textDisplay2, 8, 0);	// line2
      	addItem(cabsPanel, textLastUsed, 9, 0);	// last used
      	
    	int numberOfCabs = 0;
        // build table of cabs
        for (int currCabId=2; currCabId<CAB_MAX; currCabId++){
        	JLabel number = new JLabel();
        	JLabel type = new JLabel();
         	JLabel address = new JLabel();
           	JLabel speed = new JLabel();
           	JLabel consist = new JLabel();
           	JLabel functions = new JLabel();
           	JLabel lastUsed = new JLabel();
           	JLabel line1 = new JLabel();
           	JLabel line2 = new JLabel();
           	
           	int foundChange = 0;
           	recChar = -1;
           	// create cab type by reading the FLAGS1 byte
        	readCabMemory1(currCabId, CAB_FLAGS1);
           	if (!waitNce())
        		return;
           	if (log.isDebugEnabled()) log.debug("ID = "+currCabId+" Read flag1 character " + recChar);
           	// test it really changed
           	if (recChar != -1) {
	        	// save value for purge
	        	if (recChar != cabFlag1Array[currCabId]) {
	        		foundChange++;
	        		if (log.isDebugEnabled()) log.debug(currCabId + ": Flag1 " + recChar + "<->" + cabFlag1Array[currCabId]);
	        	}
	        	cabFlag1Array[currCabId] = recChar;
	        	if ((recChar & FLAGS1_MASK_CABISACTIVE) != FLAGS1_CABISACTIVE) {
	        		// not active slot
	            	if (checkBoxActive.isSelected())
	            		continue;
	        	}
	        	int cabId = recChar & FLAGS1_MASK_CABID; // mask off don't care bits
	        	if (cabId == FLAGS1_CABID_DISPLAY){
	        		type.setText("ProCab");
	        		numberOfCabs++;
	        	}
	        	else if (cabId == FLAGS1_CABID_NODISP){ 
	        		type.setText("Cab04/06");	// Cab04 or Cab06
	        		numberOfCabs++;
	        	}
	           	else if (cabId == FLAGS1_CABID_USB){
	        		type.setText("USB/M-P");	// USB or Mini-Panel
	           	}
	            else if (cabId == FLAGS1_CABID_AIU){
	        		type.setText("AIU");
	            }
	            else {
	            	type.setText(rb.getString("UnknownCabType") + ": " + recChar);
	            }
	        	// add items to table
	        	addItem(cabsPanel, number, 1, currCabId);
	        	number.setText(Integer.toString(currCabId));
	        	addItem(cabsPanel, type, 2, currCabId);
	        	addItem(cabsPanel, address, 3, currCabId);
	          	addItem(cabsPanel, speed, 4, currCabId);
	          	addItem(cabsPanel, consist, 5, currCabId);
	          	addItem(cabsPanel, functions, 6, currCabId);
	          	addItem(cabsPanel, line1, 7, currCabId);
	          	addItem(cabsPanel, line2, 8, currCabId);
	          	addItem(cabsPanel, lastUsed, 9, currCabId);
	          	if (cabId != FLAGS1_CABID_AIU) {
		          	// read 16 bytes of memory, we'll use 7 of the 16
		        	readCabMemory16(currCabId, CAB_CURR_SPEED);
		        	if (!waitNce())
		        		return;
		        	// read the Speed byte
		        	int readChar = recChars[0];
		        	if (cabSpeedArray[currCabId] != readChar) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": Speed " + readChar + "<->" + cabSpeedArray[currCabId]);
		        	}
		        	cabSpeedArray[currCabId] = readChar;
		        	if (log.isDebugEnabled()) log.debug("Read speed character "+Integer.toString(readChar));
		        	String sped = Integer.toString(readChar);
		        	// read the FLAGS byte
		        	readChar = recChars[CAB_FLAGS-CAB_CURR_SPEED];
		        	if (cabFlagsArray[currCabId] != readChar) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": Flags " + readChar + "<->" + cabFlagsArray[currCabId]);
		        	}
		        	cabFlagsArray[currCabId] = readChar;
		        	int direction = readChar & 0x04;
		        	if (direction > 0)
		        		sped = sped + " F";
		        	else
		        		sped = sped + " R";
		        	int mode = readChar & 0x02;
		        	// USB doesn't use the 28/128 bit
		        	if (cabId != FLAGS1_CABID_USB){
		        		if (mode > 0)
		        			sped = sped + " / 128";
		        		else
		        			sped = sped + " / 28";
		        	}
		        	speed.setText(sped);
		        	
		        	// create loco address, read the high address byte
		        	readChar = recChars[CAB_ADDR_H-CAB_CURR_SPEED];
		        	if (log.isDebugEnabled()) log.debug("Read address high character "+readChar);
		        	int locoAddress = (readChar & 0x3F) *256;
		        	// read the low address byte
		        	readChar = recChars[CAB_ADDR_L-CAB_CURR_SPEED];
		        	if (log.isDebugEnabled()) log.debug("Read address low character "+readChar);
		        	locoAddress = locoAddress + (readChar & 0xFF);
		        	if (cabLocoArray[currCabId] != locoAddress) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": Loco " + locoAddress + "<->" + cabLocoArray[currCabId]);
		        	}
		        	cabLocoArray[currCabId] = locoAddress;
		        	address.setText(Integer.toString(locoAddress));
		        	
		        	// create consist address
		        	readChar = recChars[CAB_ALIAS-CAB_CURR_SPEED];
		        	if (cabConsistArray[currCabId] != readChar) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": Consist " + readChar + "<->" + cabConsistArray[currCabId]);
		        	}
		        	cabConsistArray[currCabId] = readChar;
		        	if(readChar == 0)
		        		consist.setText(" ");
		        	else
		        		consist.setText(Integer.toString(readChar));
		        	
		        	// create function keys
		        	readChar = recChars[CAB_FUNC_L-CAB_CURR_SPEED];
		        	if (cabF0Array[currCabId] != readChar) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": F0 " + readChar + "<->" + cabF0Array[currCabId]);
		        	}
		        	cabF0Array[currCabId] = readChar;
		        	if (log.isDebugEnabled()) log.debug("Function low character "+readChar);
		        	StringBuilder func = new StringBuilder();
		        	if ((readChar & FUNC_L_F0) > 0)
		        		func.append(" L");
		        	else
		        		func.append(" -");
		           	if ((readChar & FUNC_L_F1) > 0)
		           		func.append(" 1");
		        	else
		        		func.append(" -");
		           	if ((readChar & FUNC_L_F2) > 0)
		           		func.append(" 2");
		        	else
		        		func.append(" -");
		           	if ((readChar & FUNC_L_F3) > 0)
		           		func.append(" 3");
		        	else
		        		func.append(" -");
		           	if ((readChar & FUNC_L_F4) > 0)
		           		func.append(" 4");
		        	else
		        		func.append(" -");
		        	readChar = recChars[CAB_FUNC_H-CAB_CURR_SPEED];
		        	if (cabF5Array[currCabId] != readChar) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": F5 " + readChar + "<->" + cabF5Array[currCabId]);
		        	}
		        	cabF5Array[currCabId] = readChar;
		        	if (log.isDebugEnabled()) log.debug("Function high character "+readChar);
		           	if ((readChar & FUNC_H_F5) > 0)
		           		func.append(" 5");
		        	else
		        		func.append(" -");
		           	if ((readChar & FUNC_H_F6) > 0)
		           		func.append(" 6");
		        	else
		        		func.append(" -");
		           	if ((readChar & FUNC_H_F7) > 0)
		           		func.append(" 7");
		        	else
		        		func.append(" -");
		           	if ((readChar & FUNC_H_F8) > 0)
		           		func.append(" 8");
		        	else
		        		func.append(" -");
		           	if ((readChar & FUNC_H_F9) > 0)
		           		func.append(" 9");
		        	else
		        		func.append(" -");
		           	if ((readChar & FUNC_H_F10) > 0)
		           		func.append(" 10");
		        	else
		        		func.append(" - ");
		          	if ((readChar & FUNC_H_F11) > 0)
		          		func.append(" 11");
		        	else
		        		func.append(" - ");
		          	if ((readChar & FUNC_H_F12) > 0)
		          		func.append(" 12");
		        	else
		        		func.append(" - ");

		          	// get the functions 13-20 values
		        	readCabMemory1(currCabId, CAB_FUNC_13_20);
		        	if (!waitNce())
		        		return;
		        	if (cabF13Array[currCabId] != recChar) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": F13 " + recChar + "<->" + cabF13Array[currCabId]);
		        	}
		        	cabF13Array[currCabId] = recChar;
		           	if ((recChar & FUNC_H_F13) > 0)
		           		func.append(" 13");
		        	else
		        		func.append(" -");
		           	if ((recChar & FUNC_H_F14) > 0)
		           		func.append(" 14");
		        	else
		        		func.append(" -");
		           	if ((recChar & FUNC_H_F15) > 0)
		           		func.append(" 15");
		        	else
		        		func.append(" -");
		           	if ((recChar & FUNC_H_F16) > 0)
		           		func.append(" 16");
		        	else
		        		func.append(" -");
		           	if ((recChar & FUNC_H_F17) > 0)
		           		func.append(" 17");
		        	else
		        		func.append(" -");
		           	if ((recChar & FUNC_H_F18) > 0)
		           		func.append(" 18");
		        	else
		        		func.append(" - ");
		          	if ((recChar & FUNC_H_F19) > 0)
		          		func.append(" 19");
		        	else
		        		func.append(" - ");
		          	if ((recChar & FUNC_H_F20) > 0)
		          		func.append(" 20");
		        	else
		        		func.append(" - ");

		          	// get the functions 13-20 values
		        	readCabMemory1(currCabId, CAB_FUNC_21_28);
		        	if (!waitNce())
		        		return;
		        	if (cabF21Array[currCabId] != recChar) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": F21 " + recChar + "<->" + cabF21Array[currCabId]);
		        	}
		        	cabF21Array[currCabId] = recChar;
		           	if ((recChar & FUNC_H_F21) > 0)
		           		func.append(" 21");
		        	else
		        		func.append(" -");
		           	if ((recChar & FUNC_H_F22) > 0)
		           		func.append(" 22");
		        	else
		        		func.append(" -");
		           	if ((recChar & FUNC_H_F23) > 0)
		           		func.append(" 23");
		        	else
		        		func.append(" -");
		           	if ((recChar & FUNC_H_F24) > 0)
		           		func.append(" 24");
		        	else
		        		func.append(" -");
		           	if ((recChar & FUNC_H_F25) > 0)
		           		func.append(" 25");
		        	else
		        		func.append(" -");
		           	if ((recChar & FUNC_H_F26) > 0)
		           		func.append(" 26");
		        	else
		        		func.append(" - ");
		          	if ((recChar & FUNC_H_F27) > 0)
		          		func.append(" 27");
		        	else
		        		func.append(" - ");
		          	if ((recChar & FUNC_H_F28) > 0)
		          		func.append(" 28");
		        	else
		        		func.append(" - ");
		        	
		          	functions.setText(func.toString());
		          	
		          	// get the display values
		        	readCabMemory16(currCabId, CAB_LINE_1);
		        	if (!waitNce())
		        		return;
		        	StringBuilder text1 = new StringBuilder();
		        	StringBuilder debug1 = new StringBuilder();
		        	for(int i = 0; i < CAB_LINE_LEN; i++) {
			        	if (cabLine1Array[currCabId][i] != recChars[i]) {
			        		foundChange++;
			        		if (log.isDebugEnabled()) log.debug(currCabId + ": CabLine1[" + i + "] " + recChars[i] + "<->" + cabLine1Array[currCabId][i]);
			        	}
			        	cabLine1Array[currCabId][i] = recChars[i];
			        	if (recChars[i] >= 0x20 && recChars[i] <= 0x7F) {
			        		text1.append((char)recChars[i]);
			        	} else {
			        		text1.append(" ");
			        	}
		        		debug1.append(" " + recChars[i]);
		        	}
		        	line1.setText(text1.toString());
		        	if (log.isDebugEnabled()) log.debug("TextLine1Debug: " + debug1);
		        	
		        	readCabMemory16(currCabId, CAB_LINE_2);
		        	if (!waitNce())
		        		return;
		        	StringBuilder text2 = new StringBuilder();
		        	StringBuilder debug2 = new StringBuilder();
		        	for(int i = 0; i < CAB_LINE_LEN; i++) {
			        	if (cabLine2Array[currCabId][i] != recChars[i]) {
			        		foundChange++;
			        		if (log.isDebugEnabled()) log.debug(currCabId + ": CabLine2[" + i + "] " + recChars[i] + "<->" + cabLine2Array[currCabId][i]);
			        	}
			        	cabLine2Array[currCabId][i] = recChars[i];
			        	if (recChars[i] >= 0x20 && recChars[i] <= 0x7F) {
			        		text2.append((char)recChars[i]);
			        	} else {
			        		text2.append(" ");
			        	}
		        		debug2.append(" " + recChars[i]);
		        	}
		        	line2.setText(text2.toString());
		        	if (log.isDebugEnabled()) log.debug("TextLine2Debug: " + debug2);
		        	
		          	if (foundChange > 0 || cabLastChangeArray[currCabId] == null) {
		            	cabLastChangeArray[currCabId] = Calendar.getInstance();
		            }
	          	}
	          	
	          	StringBuilder txt = new StringBuilder();
	          	int h = cabLastChangeArray[currCabId].get(Calendar.HOUR_OF_DAY);
	          	int m = cabLastChangeArray[currCabId].get(Calendar.MINUTE);
	          	int s = cabLastChangeArray[currCabId].get(Calendar.SECOND);
	          	if (h < 10) {
	          		txt.append("0");
	          	}
	            txt.append(h);
	            txt.append(":");
	            if (m < 10) {
	            	txt.append("0");
	            }
	            txt.append(m);
	            txt.append(":");
	            if (s < 10) {
	            	txt.append("0");
	            }
	            txt.append(s);
	            lastUsed.setText(txt.toString());
           	}
        }
 
    	cabsPane.setVisible(true);
    	cabsPane.repaint();
    	textStatus.setText(MessageFormat.format(rb.getString("FoundCabs"), new Object[]{numberOfCabs}));
    }
    
    // puts the thread to sleep while we wait for the read CS memory to complete
    private boolean waitNce(){
    	int count = 100;
    	log.debug("going to sleep");
       	while (waiting > 0){
       		synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
				    
				}
			}
    		count--;
    		if (count < 0){
    			textStatus.setText("Error");
    			return false;
    		}
    	}
       	log.debug("awake!");
       	return true;
    }
    
    public void  message(NceMessage m) {}  // ignore replies
    
    // response from read
    int recChar = 0;
    int [] recChars = new int [16];
	public void reply(NceReply r) {
		if (log.isDebugEnabled()) log.debug("Receive character");
		if (waiting <= 0) {
			log.error("unexpected response. Len: " + r.getNumDataElements() + " code: " + r.getElement(0));
			return;
		}
		waiting--;
		if (r.getNumDataElements() != replyLen) {
			textStatus.setText("error");
			return;
		}
		// Read one byte
		if (replyLen == REPLY_1) {
			// Looking for proper response
			recChar = r.getElement(0);
		}
		// Read 16 bytes
		if (replyLen == REPLY_16) {
			// Looking for proper response
			for (int i=0; i<REPLY_16; i++){
				recChars[i] = r.getElement(i);
			}
		}
		// wake up thread
		synchronized (this) {
			notify();
		}
	}

    // Write 1 byte of NCE cab memory 
    private void writeCabMemory1(int cabNum, int offset, int value) {
       	int nceCabAddr = (cabNum * CAB_SIZE) + CS_CAB_MEM + offset;
    	replyLen = REPLY_1;			// Expect 1 byte response
    	waiting++;
		byte[] bl = NceBinaryCommand.accMemoryWrite1(nceCabAddr);
		bl[3] = (byte)value;
		NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_1);
		tc.sendNceMessage(m, this);
    }
    
    // Reads 1 byte of NCE cab memory 
    private void readCabMemory1(int cabNum, int offset) {
       	int nceCabAddr = (cabNum * CAB_SIZE) + CS_CAB_MEM + offset;
    	replyLen = REPLY_1;			// Expect 1 byte response
    	waiting++;
		byte[] bl = NceBinaryCommand.accMemoryRead1(nceCabAddr);
		NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_1);
		tc.sendNceMessage(m, this);
    }
    
    // Reads 16 bytes of NCE cab memory 
    private void readCabMemory16(int cabNum, int offset) {
       	int nceCabAddr = (cabNum * CAB_SIZE) + CS_CAB_MEM + offset;
    	replyLen = REPLY_16;			// Expect 16 byte response
    	waiting++;
		byte[] bl = NceBinaryCommand.accMemoryRead(nceCabAddr);
		NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_16);
		tc.sendNceMessage(m, this);
    }
    
	protected void addItem(JPanel p, JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		p.add(c, gc);
	}
	
	protected void addItemLeft(JPanel p, JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		gc.anchor = GridBagConstraints.WEST;
		p.add(c, gc);
	}
    
    private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}
   
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceShowCabPanel.class.getName());	
}

