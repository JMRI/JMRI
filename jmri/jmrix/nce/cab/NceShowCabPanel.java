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
 * Bit 7,0 = 0,0 Procab or other cab with an LCD display 
 * Bit 7,0 = 0,1 Cab04 other cab without an LCD 
 * Bit 7,0 = 1,0 USB or similar device 
 * Bit 7,0 = 1,1 AIU or similar device
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
 * @version $Revision: 1.2 $
 */

public class NceShowCabPanel extends jmri.jmrix.nce.swing.NcePanel implements jmri.jmrix.nce.NceListener {
	
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.cab.NceShowCabBundle");
	
	private static final int CS_CAB_MEM = 0x8800;	// start of NCE CS cab context page for cab 0
													// memory
	private static final int CAB_SIZE = 256;		// Each cab has 256 bytes
	private static final int CAB_CURR_SPEED = 32;	// NCE cab speed
	private static final int CAB_ADDR_H = 33; 		// loco address, high byte
	private static final int CAB_ADDR_L = 34; 		// loco address, low byte
	private static final int CAB_FLAGS = 35;		// FLAGS
	private static final int CAB_FUNC_L = 36;		// Function keys low
	private static final int CAB_FUNC_H = 37;		// Function keys high
	private static final int CAB_ALIAS = 38;		// Consist address
	private static final int CAB_FLAGS1 = 101;		// NCE flag 1
	private static final int CAB_MAX = 64;			// There are up to 64 cabs
	private static final int REPLY_1 = 1;			// reply length of 1 byte
	private static final int REPLY_16 = 16;			// reply length of 16 bytes			
	
	private int replyLen = 0;						// expected byte length
	private int waiting = 0;						// to catch responses not
													// intended for this module
	private static final int FLAGS1_PROCAB = 0x02;	// bit 0=0, bit 1=1, bit 7=0;
	private static final int FLAGS1_CAB04 = 0x03;	// bit 0=1, bit 1=1, bit 7=0;
	private static final int FLAGS1_USB = 0x82;		// bit 0=0, bit 1=1, bit 7=1;
	private static final int FLAGS1_AIU = 0x83;		// bit 0=1, bit 1=1, bit 7=1;
	private static final int FLAGS1_MASK = 0x83;	// Only bits 0,1, and 7.
	private static final int FLAGS1_MASK_CABACTIVE = 0x01;	// if cab is active
	
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
	
	Thread NceCabUpdateThread;	
	private int[] cabFlag1Array = new int[CAB_MAX];
	private Calendar[] cabLastChangeArray = new Calendar[CAB_MAX];
	private int[] cabSpeedArray = new int[CAB_MAX];
	private int[] cabFlagsArray = new int[CAB_MAX];
	private int[] cabLocoArray = new int[CAB_MAX];
	private int[] cabConsistArray = new int[CAB_MAX];
	private int[] cabF0Array = new int[CAB_MAX];
	private int[] cabF5Array = new int[CAB_MAX];
	
	// member declarations
	JLabel textNumer = new JLabel(rb.getString("Number"));
    JLabel textCab = new JLabel(rb.getString("Type"));
    JLabel textAddress = new JLabel(rb.getString("Loco"));
    JLabel textSpeed = new JLabel(rb.getString("Speed"));
    JLabel textConsist = new JLabel(rb.getString("Consist"));
    JLabel textFunctions = new JLabel(rb.getString("Functions"));
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
    	addItem(p1, purgeCabId, 8, 1);
    	addItem(p1, space1d, 9, 1);
    	addItem(p1, purgeButton, 10, 1);
    	
    	// row 2
    	addItem(p1, space2, 4, 2);
    	
        // row 3
    	JPanel p2 = new JPanel();
    	p2.setLayout(new GridBagLayout());
        addItem(p2, textNumer, 1, 3);
        addItemLeft(p2, textCab, 2, 3);
        addItemLeft(p2, textAddress, 3, 3);
        addItemLeft(p2, textSpeed, 4, 3);
        addItemLeft(p2, textConsist, 5, 3);
        addItemLeft(p2, textFunctions, 6, 3);
        addItemLeft(p2, textLastUsed, 7, 3);
        
    	addButtonAction(refreshButton);
    	addButtonAction(purgeButton);
    	
    	add(p1);
    	add(p2);
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
    	if (ae.getSource() == refreshButton) {
    		refreshPanel();
    	} else if (ae.getSource() == purgeButton) {
    		purgeCab();
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
    	if ((cabFlag1Array[cab] & FLAGS1_MASK_CABACTIVE) != 0) {
            log.error(rb.getString("ErrorCabNotActive") + purgeCabId.getText().trim());
    		return;
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
    	int numberOfCabs = 0;
        // build table of cabs
        for (int i=1; i<CAB_MAX; i++){
        	JLabel number = new JLabel();
        	JLabel type = new JLabel();
         	JLabel address = new JLabel();
           	JLabel speed = new JLabel();
           	JLabel consist = new JLabel();
           	JLabel functions = new JLabel();
           	JLabel lastUsed = new JLabel();
           	
           	int foundChange = 0;
           	// create cab type by reading the FLAGS1 byte
        	readCabMemory1(i, CAB_FLAGS1);
           	if (!waitNce())
        		return;
           	if (log.isDebugEnabled()) log.debug("Read flag1 character "+recChar);
        	// save value for purge
        	if (recChar != cabFlag1Array[i]) {
        		foundChange++;
        		if (log.isDebugEnabled()) log.debug(i + ": Flag1 " + recChar + "<->" + cabFlag1Array[i]);
        	}
        	cabFlag1Array[i] = recChar;
        	int flags1 = recChar & FLAGS1_MASK; // mask off don't care bits
        	if (flags1 == FLAGS1_PROCAB){
        		type.setText("ProCab");
        		numberOfCabs++;
        	}
        	else if (flags1 == FLAGS1_CAB04){ 
        		type.setText("Cab04/06");	// Cab04 or Cab06
        		numberOfCabs++;
        	}
           	else if (flags1 == FLAGS1_USB){
        		type.setText("USB/M-P");	// USB or Mini-Panel
           	}
            else if (flags1 == FLAGS1_AIU){
        		type.setText("AIU");
            }
            else {
            	if (checkBoxActive.isSelected())
            		continue;
            	type.setText(rb.getString("UnknownCabType"));
            }
        	// add items to table
        	addItem(cabsPanel, number, 1, i);
        	number.setText(Integer.toString(i));
        	addItem(cabsPanel, type, 2, i);
        	addItem(cabsPanel, address, 3, i);
          	addItem(cabsPanel, speed, 4, i);
          	addItem(cabsPanel, consist, 5, i);
          	addItem(cabsPanel, functions, 6, i);
          	addItem(cabsPanel, lastUsed, 7, i);
          	if (flags1 != FLAGS1_AIU) {
	          	// read 16 bytes of memory, we'll use 7 of the 16
	        	readCabMemory16(i, CAB_CURR_SPEED);
	        	if (!waitNce())
	        		return;
	        	// read the Speed byte
	        	int readChar = recChars[0];
	        	if (cabSpeedArray[i] != readChar) {
	        		foundChange++;
	        		if (log.isDebugEnabled()) log.debug(i + ": Speed " + readChar + "<->" + cabSpeedArray[i]);
	        	}
	        	cabSpeedArray[i] = readChar;
	        	if (log.isDebugEnabled()) log.debug("Read speed character "+Integer.toString(readChar));
	        	String sped = Integer.toString(readChar);
	        	// read the FLAGS byte
	        	readChar = recChars[CAB_FLAGS-CAB_CURR_SPEED];
	        	if (cabFlagsArray[i] != readChar) {
	        		foundChange++;
	        		if (log.isDebugEnabled()) log.debug(i + ": Flags " + readChar + "<->" + cabFlagsArray[i]);
	        	}
	        	cabFlagsArray[i] = readChar;
	        	int direction = readChar & 0x04;
	        	if (direction > 0)
	        		sped = sped + " F";
	        	else
	        		sped = sped + " R";
	        	int mode = readChar & 0x02;
	        	// USB doesn't use the 28/128 bit
	        	if (flags1 != FLAGS1_USB){
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
	        	if (cabLocoArray[i] != locoAddress) {
	        		foundChange++;
	        		if (log.isDebugEnabled()) log.debug(i + ": Loco " + locoAddress + "<->" + cabLocoArray[i]);
	        	}
	        	cabLocoArray[i] = locoAddress;
	        	address.setText(Integer.toString(locoAddress));
	        	
	        	// create consist address
	        	readChar = recChars[CAB_ALIAS-CAB_CURR_SPEED];
	        	if (cabConsistArray[i] != readChar) {
	        		foundChange++;
	        		if (log.isDebugEnabled()) log.debug(i + ": Consist " + readChar + "<->" + cabConsistArray[i]);
	        	}
	        	cabConsistArray[i] = readChar;
	        	if(readChar == 0)
	        		consist.setText(" ");
	        	else
	        		consist.setText(Integer.toString(readChar));
	        	
	        	// create function keys
	        	readChar = recChars[CAB_FUNC_L-CAB_CURR_SPEED];
	        	if (cabF0Array[i] != readChar) {
	        		foundChange++;
	        		if (log.isDebugEnabled()) log.debug(i + ": F0 " + readChar + "<->" + cabF0Array[i]);
	        	}
	        	cabF0Array[i] = readChar;
	        	if (log.isDebugEnabled()) log.debug("Function low character "+readChar);
	        	StringBuilder func = new StringBuilder();
	        	if ((readChar & FUNC_L_F0) > 0)
	        		func.append("L");
	        	else
	        		func.append("-");
	           	if ((readChar & FUNC_L_F1) > 0)
	           		func.append("1");
	        	else
	        		func.append("-");
	           	if ((readChar & FUNC_L_F2) > 0)
	           		func.append("2");
	        	else
	        		func.append("-");
	           	if ((readChar & FUNC_L_F3) > 0)
	           		func.append("3");
	        	else
	        		func.append("-");
	           	if ((readChar & FUNC_L_F4) > 0)
	           		func.append("4");
	        	else
	        		func.append("-");
	        	readChar = recChars[CAB_FUNC_H-CAB_CURR_SPEED];
	        	if (cabF5Array[i] != readChar) {
	        		foundChange++;
	        		if (log.isDebugEnabled()) log.debug(i + ": F5 " + readChar + "<->" + cabF5Array[i]);
	        	}
	        	cabF5Array[i] = readChar;
	        	if (log.isDebugEnabled()) log.debug("Function high character "+readChar);
	           	if ((readChar & FUNC_H_F5) > 0)
	           		func.append("5");
	        	else
	        		func.append("-");
	           	if ((readChar & FUNC_H_F6) > 0)
	           		func.append("6");
	        	else
	        		func.append("-");
	           	if ((readChar & FUNC_H_F7) > 0)
	           		func.append("7");
	        	else
	        		func.append("-");
	           	if ((readChar & FUNC_H_F8) > 0)
	           		func.append("8");
	        	else
	        		func.append("-");
	           	if ((readChar & FUNC_H_F9) > 0)
	           		func.append("9");
	        	else
	        		func.append("-");
	           	if ((readChar & FUNC_H_F10) > 0)
	           		func.append("A");
	        	else
	        		func.append("-");
	          	if ((readChar & FUNC_H_F11) > 0)
	          		func.append("B");
	        	else
	        		func.append("-");
	          	if ((readChar & FUNC_H_F12) > 0)
	          		func.append("C");
	        	else
	        		func.append("-");
	          	functions.setText(func.toString());
          	}
          	if (foundChange > 0 || cabLastChangeArray[i] == null) {
            	cabLastChangeArray[i] = Calendar.getInstance();
            }
          	
          	StringBuilder txt = new StringBuilder();
          	int h = cabLastChangeArray[i].get(Calendar.HOUR_OF_DAY);
          	int m = cabLastChangeArray[i].get(Calendar.MINUTE);
          	int s = cabLastChangeArray[i].get(Calendar.SECOND);
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
 
    	cabsPane.setVisible(true);
    	cabsPane.repaint();
    	textStatus.setText(MessageFormat.format(rb.getString("FoundCabs"), new Object[]{numberOfCabs}));
    }
    
    // puts the thread to sleep while we wait for the read CS memory to complete
    private boolean waitNce(){
    	int count = 100;
       	while (waiting > 0){
    		try{
    			Thread.sleep(60);
    		} catch (Exception e){
    			//return false;
    		}
    		count--;
    		if (count < 0){
    			textStatus.setText("Error");
    			return false;
    		}
    	}
       	return true;
    }
    
    public void  message(NceMessage m) {}  // ignore replies
    
    // response from read
    int recChar = 0;
    int [] recChars = new int [16];
	public void reply(NceReply r) {
		if (log.isDebugEnabled()) log.debug("Receive character");
		if (waiting <= 0) {
			log.error("unexpected response");
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
		
	}

    // Write 1 byte of NCE cab memory 
    private void writeCabMemory1(int cabNum, int offset, int value) {
       	int nceCabAddr = (cabNum * CAB_SIZE) + CS_CAB_MEM + offset;
    	replyLen = REPLY_1;			// Expect 1 byte response
    	waiting++;
		byte[] bl = NceBinaryCommand.accMemoryWriteN(nceCabAddr, 1);
		bl[4] = (byte)value;
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

