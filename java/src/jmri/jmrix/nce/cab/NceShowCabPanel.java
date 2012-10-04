// NceShowCabPanel.java

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
 * @author Ken Cameron Copyright (C) 2012
 * @version $Revision$
 */

package jmri.jmrix.nce.cab;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

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
	public static final int FLAGS1_CABISACTIVE = 0x02;	// if cab is active
	public static final int FLAGS1_MASK_CABID = 0x81;	// Only bits 0 and 7.
	public static final int FLAGS1_MASK_CABISACTIVE = 0x02;	// if cab is active
	
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
	
	public static int[] cabFlag1Array = new int[CAB_MAX];
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
    
    // check boxes
    static JCheckBox checkBoxShowActive = new JCheckBox(rb.getString("CheckBoxLabelShowActive"));
    static JCheckBox checkBoxShowDisplayText = new JCheckBox(rb.getString("CheckBoxLabelShowDisplayText"));
    static JCheckBox checkBoxShowAllFunctions = new JCheckBox(rb.getString("CheckBoxLabelShowAllFunctions"));
    
    // text fields
    
    // for padding out panel
    JLabel space1a = new JLabel("    ");
    JLabel space1b = new JLabel("    ");
    JLabel space1c = new JLabel("    ");
    JLabel space1d = new JLabel("    ");
    JLabel space2 = new JLabel(" ");
    JLabel space3 = new JLabel(" ");
    JLabel space4 = new JLabel(" ");
    JLabel space5 = new JLabel(" ");
        
    class dataRow	{
    	int			cab;
    	String		type;
    	JButton 	buttonPurgeCab;
    	int			loco;
    	int			speed; 
    	String		dir;
    	String		mode;
    	String		consist;
    	boolean		F0;
    	boolean		F1;
    	boolean		F2;
    	boolean		F3;
    	boolean		F4;
    	boolean		F5;
    	boolean		F6;
    	boolean		F7;
    	boolean		F8;
    	boolean		F9;
    	boolean		F10;
    	boolean		F11;
    	boolean		F12;
    	boolean		F13;
    	boolean		F14;
    	boolean		F15;
    	boolean		F16;
    	boolean		F17;
    	boolean		F18;
    	boolean		F19;
    	boolean		F20;
    	boolean		F21;
    	boolean		F22;
    	boolean		F23;
    	boolean		F24;
    	boolean		F25;
    	boolean		F26;
    	boolean		F27;
    	boolean		F28;
    	String		text1;
    	String		text2;
    	String		lastChange;
    };

    dataRow[] cabData = new dataRow[CAB_MAX];

    nceCabTableModel cabModel = new nceCabTableModel(cabData);
    JTable cabTable = new JTable(cabModel);
    
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

        // fill in cab array
        for (int i = 0; i < CAB_MAX; i++) {
    		cabData[i] = new dataRow();
        }
    	// the following code sets the frame's initial state
    	
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
    	p1.setPreferredSize(new Dimension(700, 40));
    	// row 1
    	refreshButton.setToolTipText(rb.getString("RefreshToolTip"));
    	addButtonAction(refreshButton);
    	checkBoxShowActive.setToolTipText(rb.getString("CheckBoxActiveToolTip"));
    	checkBoxShowActive.setSelected(true);
    	addCheckBoxAction(checkBoxShowActive);
    	checkBoxShowAllFunctions.setToolTipText(rb.getString("CheckBoxShowAllFunctionsToolTip"));
    	checkBoxShowAllFunctions.setSelected(false);
    	checkBoxShowDisplayText.setToolTipText(rb.getString("CheckBoxShowDisplayToolTip"));
    	checkBoxShowDisplayText.setSelected(false);
    	addItem(p1, refreshButton, 2, 1);
    	addItem(p1, space1a, 3, 1);
    	addItem(p1, textStatus, 4, 1);
    	addItem(p1, space1b, 5, 1);
    	addItem(p1, checkBoxShowActive, 6, 1);
//    	addItem(p1, space1c, 7, 1);
//    	addItem(p1, checkBoxShowAllFunctions, 8, 1);
//    	addItem(p1, space1d, 9, 1);
//    	addItem(p1, checkBoxShowDisplayText, 10, 1);
    	
    	JScrollPane cabScrollPane = new JScrollPane(cabTable);
    	cabTable.setFillsViewportHeight(true);
    	cabTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    	cabModel.setShowAllCabs(!checkBoxShowActive.isSelected());
    	for (int col = 0; col < cabTable.getColumnCount(); col++) {
    		int width = cabModel.getPreferredWidth(col);
    		TableColumn c = cabTable.getColumnModel().getColumn(col);
    		c.setPreferredWidth(width);
    	}
		cabTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	cabScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    	cabScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    	setColumnToHoldButton(cabTable, 2);
    	add(p1);
    	add(cabScrollPane);
    	
    	// pad out panel
    	cabScrollPane.setVisible(true);

    	refreshPanel();   	
 
    }

    // button actions
    public void buttonActionPerformed(ActionEvent ae) {
    	Object src = ae.getSource();
    	if (src == refreshButton) {
    		refreshPanel();
    	} else {
    		log.error("unknown action performed: " + src);
    	}
	}

    // checkboxes
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
    	Object src = ae.getSource();
    	if (src == checkBoxShowActive) {
    		cabModel.setShowAllCabs(!checkBoxShowActive.isSelected());
    		refreshPanel();
    	} else {
    		log.error("unknown checkbox action performed: " + src);
    	}
	}
    
    public void purgeCab(int cab) {
    	if (cab < 1 || cab > cabData.length) {
            log.error(rb.getString("ErrorValueRange") + cab);
    		return;
    	}
    	// if id is active
    	int act = cabFlag1Array[cab] & FLAGS1_MASK_CABISACTIVE;
    	if (act != FLAGS1_CABISACTIVE) {
            log.error(rb.getString("ErrorCabNotActive") + cab);
    	}
    	// clear bit for active and cab type details
    	cabFlag1Array[cab] = 0;
    	writeCabMemory1(cab, CAB_FLAGS1, 0);
    	// update the display
    	refreshPanel();
        textStatus.setText(rb.getString("StatusCabPurged") + " " + cab);
    	return;
    }
    
    private void refreshPanel(){
    	// Set up a separate thread to read CS memory
        if (NceCabUpdateThread != null && NceCabUpdateThread.isAlive())	
        	return; // thread is already running
    	textStatus.setText(rb.getString("StatusReadingMemory"));
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
    	int cabsFound = 0;
        // build table of cabs
        for (int currCabId=0; currCabId < cabData.length; currCabId++){
           	
            textStatus.setText(rb.getString("StatusReadingCabId") + currCabId);
            cabData[currCabId].cab = currCabId;
           	int foundChange = 0;
           	recChar = -1;
           	// create cab type by reading the FLAGS1 byte
        	readCabMemory1(currCabId, CAB_FLAGS1);
           	if (!waitNce())
        		return;
           	if (log.isDebugEnabled()) log.debug("ID = " + currCabId + " Read flag1 character " + recChar);
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
	            	continue;
	        	}
	        	if (currCabId >= 1 || !checkBoxShowActive.isSelected()) {
	           		cabsFound++;
	        	}
	        	int cabId = recChar & FLAGS1_MASK_CABID; // mask off don't care bits
	        	if (cabId == FLAGS1_CABID_DISPLAY){
	        		cabData[currCabId].type =  rb.getString("TypeProCab");
	        	}
	        	else if (cabId == FLAGS1_CABID_NODISP){ 
	        		cabData[currCabId].type = rb.getString("TypeCab04");	// Cab04 or Cab06
	        	}
	           	else if (cabId == FLAGS1_CABID_USB){
	           		cabData[currCabId].type = rb.getString("TypeUSB");	// USB or Mini-Panel
	           	}
	            else if (cabId == FLAGS1_CABID_AIU){
	            	cabData[currCabId].type = rb.getString("TypeAIU");
	            }
	            else {
	            	cabData[currCabId].type = rb.getString("TypeUnknownCab") + ": " + recChar;
	            }

	        	cabData[currCabId].cab = currCabId;
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
		        	cabData[currCabId].speed = readChar;
		        	
		        	// read the FLAGS byte
		        	readChar = recChars[CAB_FLAGS-CAB_CURR_SPEED];
		        	if (cabFlagsArray[currCabId] != readChar) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": Flags " + readChar + "<->" + cabFlagsArray[currCabId]);
		        	}
		        	cabFlagsArray[currCabId] = readChar;
		        	int direction = readChar & 0x04;
		        	if (direction > 0)
		        		cabData[currCabId].dir = rb.getString("DirForward");
		        	else
		        		cabData[currCabId].dir = rb.getString("DirReverse");
		        	int mode = readChar & 0x02;
		        	// USB doesn't use the 28/128 bit
		        	if (cabId != FLAGS1_CABID_USB){
		        		if (mode > 0)
		        			cabData[currCabId].mode = "128";
		        		else
		        			cabData[currCabId].mode = "28";
		        	}
		        	
		        	// create loco address, read the high address byte
		        	readChar = recChars[CAB_ADDR_H-CAB_CURR_SPEED];
		        	if (log.isDebugEnabled()) log.debug("Read address high character "+readChar);
		        	int locoAddress = (readChar & 0x3F) * 256;
		        	// read the low address byte
		        	readChar = recChars[CAB_ADDR_L-CAB_CURR_SPEED];
		        	if (log.isDebugEnabled()) log.debug("Read address low character "+readChar);
		        	locoAddress = locoAddress + (readChar & 0xFF);
		        	if (cabLocoArray[currCabId] != locoAddress) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": Loco " + locoAddress + "<->" + cabLocoArray[currCabId]);
		        	}
		        	cabLocoArray[currCabId] = locoAddress;
		        	cabData[currCabId].loco = locoAddress;
		        	
		        	// create consist address
		        	readChar = recChars[CAB_ALIAS-CAB_CURR_SPEED];
		        	if (cabConsistArray[currCabId] != readChar) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": Consist " + readChar + "<->" + cabConsistArray[currCabId]);
		        	}
		        	cabConsistArray[currCabId] = readChar;
		        	if(readChar == 0)
		        		cabData[currCabId].consist = " ";
		        	else
		        		cabData[currCabId].consist = Integer.toString(readChar);
		        	
		        	// create function keys
		        	readChar = recChars[CAB_FUNC_L-CAB_CURR_SPEED];
		        	if (cabF0Array[currCabId] != readChar) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": F0 " + readChar + "<->" + cabF0Array[currCabId]);
		        	}
		        	cabF0Array[currCabId] = readChar;
		        	if (log.isDebugEnabled()) log.debug("Function low character "+readChar);
		        	if ((readChar & FUNC_L_F0) > 0)
		        		cabData[currCabId].F0 = true;
		        	else
		        		cabData[currCabId].F0 = false;
		           	if ((readChar & FUNC_L_F1) > 0)
		           		cabData[currCabId].F1 = true;
		        	else
		        		cabData[currCabId].F1 = false;
		           	if ((readChar & FUNC_L_F2) > 0)
		           		cabData[currCabId].F2 = true;
		        	else
		        		cabData[currCabId].F2 = false;
		           	if ((readChar & FUNC_L_F3) > 0)
		           		cabData[currCabId].F3 = true;
		        	else
		        		cabData[currCabId].F3 = false;
		           	if ((readChar & FUNC_L_F4) > 0)
		           		cabData[currCabId].F4 = true;
		        	else
		        		cabData[currCabId].F4 = false;
		        	readChar = recChars[CAB_FUNC_H-CAB_CURR_SPEED];
		        	if (cabF5Array[currCabId] != readChar) {
		        		foundChange++;
		        		if (log.isDebugEnabled()) log.debug(currCabId + ": F5 " + readChar + "<->" + cabF5Array[currCabId]);
		        	}
		        	cabF5Array[currCabId] = readChar;
		        	if (log.isDebugEnabled()) log.debug("Function high character "+readChar);
		           	if ((readChar & FUNC_H_F5) > 0)
		           		cabData[currCabId].F5 = true;
		        	else
		        		cabData[currCabId].F5 = false;
		           	if ((readChar & FUNC_H_F6) > 0)
		           		cabData[currCabId].F6 = true;
		        	else
		        		cabData[currCabId].F6 = false;
		           	if ((readChar & FUNC_H_F7) > 0)
		           		cabData[currCabId].F7 = true;
		        	else
		        		cabData[currCabId].F7 = false;
		           	if ((readChar & FUNC_H_F8) > 0)
		           		cabData[currCabId].F8 = true;
		        	else
		        		cabData[currCabId].F8 = false;
		           	if ((readChar & FUNC_H_F9) > 0)
		           		cabData[currCabId].F9 = true;
		        	else
		        		cabData[currCabId].F9 = false;
		           	if ((readChar & FUNC_H_F10) > 0)
		           		cabData[currCabId].F10 = true;
		        	else
		        		cabData[currCabId].F10 = false;
		          	if ((readChar & FUNC_H_F11) > 0)
		          		cabData[currCabId].F11 = true;
		        	else
		        		cabData[currCabId].F11 = false;
		          	if ((readChar & FUNC_H_F12) > 0)
		          		cabData[currCabId].F12 = true;
		        	else
		        		cabData[currCabId].F12 = false;

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
		           		cabData[currCabId].F13 = true;
		        	else
		        		cabData[currCabId].F13 = false;
		           	if ((recChar & FUNC_H_F14) > 0)
		           		cabData[currCabId].F14 = true;
		        	else
		        		cabData[currCabId].F14 = false;
		           	if ((recChar & FUNC_H_F15) > 0)
		           		cabData[currCabId].F15 = true;
		        	else
		        		cabData[currCabId].F15 = false;
		           	if ((recChar & FUNC_H_F16) > 0)
		           		cabData[currCabId].F16 = true;
		        	else
		        		cabData[currCabId].F16 = false;
		           	if ((recChar & FUNC_H_F17) > 0)
		           		cabData[currCabId].F17 = true;
		        	else
		        		cabData[currCabId].F17 = false;
		           	if ((recChar & FUNC_H_F18) > 0)
		           		cabData[currCabId].F18 = true;
		        	else
		        		cabData[currCabId].F18 = false;
		          	if ((recChar & FUNC_H_F19) > 0)
		          		cabData[currCabId].F19 = true;
		        	else
		        		cabData[currCabId].F19 = false;
		          	if ((recChar & FUNC_H_F20) > 0)
		          		cabData[currCabId].F20 = true;
		        	else
		        		cabData[currCabId].F20 = false;

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
		           		cabData[currCabId].F21 = true;
		        	else
		        		cabData[currCabId].F21 = false;
		           	if ((recChar & FUNC_H_F22) > 0)
		           		cabData[currCabId].F22 = true;
		        	else
		        		cabData[currCabId].F22 = false;
		           	if ((recChar & FUNC_H_F23) > 0)
		           		cabData[currCabId].F23 = true;
		        	else
		        		cabData[currCabId].F23 = false;
		           	if ((recChar & FUNC_H_F24) > 0)
		           		cabData[currCabId].F24 = true;
		        	else
		        		cabData[currCabId].F24 = false;
		           	if ((recChar & FUNC_H_F25) > 0)
		           		cabData[currCabId].F25 = true;
		        	else
		        		cabData[currCabId].F25 = false;
		           	if ((recChar & FUNC_H_F26) > 0)
		           		cabData[currCabId].F26 = true;
		        	else
		        		cabData[currCabId].F26 = false;
		          	if ((recChar & FUNC_H_F27) > 0)
		          		cabData[currCabId].F27 = true;
		        	else
		        		cabData[currCabId].F27 = false;
		          	if ((recChar & FUNC_H_F28) > 0)
		          		cabData[currCabId].F28 = true;
		        	else
		        		cabData[currCabId].F28 = false;
		        	
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
		        	cabData[currCabId].text1 = text1.toString();
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
		        	cabData[currCabId].text2 = text2.toString();
		        	if (log.isDebugEnabled()) log.debug("TextLine2Debug: " + debug2);
		        	
		        	Calendar now = Calendar.getInstance();
		          	if (foundChange > 0 || cabLastChangeArray[currCabId] == null) {
		            	cabLastChangeArray[currCabId] = now;
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
			            cabData[currCabId].lastChange = txt.toString();
		            }
	          	}
           	}
        }
 
        textStatus.setText(MessageFormat.format(rb.getString("StatusReadingDone") + ", " + rb.getString("FoundCabs"), cabsFound));
        cabModel.fireTableDataChanged();
    	this.setVisible(true);
    	this.repaint();
    }
    
    // puts the thread to sleep while we wait for the read CS memory to complete
    private boolean waitNce(){
    	int count = 100;
    	if (log.isDebugEnabled()) {
    		log.debug("Going to sleep");
    	}
       	while (waiting > 0){
       		synchronized (this) {
	    		try{
	    			wait(100);
	    		} catch (InterruptedException e){
	    			//nothing to see here, move along
	    		}
       		}
    		count--;
    		if (count < 0){
    			textStatus.setText("Error");
    			return false;
    		}
    	}
    	if (log.isDebugEnabled()) {
    		log.debug("awake!");
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
	
	protected void addItemTop(JPanel p, JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		gc.anchor = GridBagConstraints.NORTH;
		p.add(c, gc);
	}
    
    private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}

    private void addCheckBoxAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionPerformed(e);
			}
		});
	}

    void setColumnToHoldButton(JTable table, int column) {
        TableColumnModel tcm = table.getColumnModel();
        // install the button renderers & editors in this column
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(column).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton(rb.getString("ButtonPurgeCab")));
        tcm.getColumn(column).setCellEditor(buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(new JButton("  " + cabModel.getValueAt(1, column)).getPreferredSize().height);
        table.getColumnModel().getColumn(column)
                        .setPreferredWidth(new JButton(rb.getString("ButtonPurgeCab")).getPreferredSize().width + 1);
    }

    public void dispose() {
        cabModel = null;
        cabData = null;
        super.dispose();
    }	
    
	class nceCabTableModel extends AbstractTableModel {
				
		dataRow[] cabData;
	
		nceCabTableModel(dataRow[] cabDataPtr) {
			this.cabData = cabDataPtr;
		}
		
	    private String[] columnNames = {
			rb.getString("ColHeaderCabId"),
			rb.getString("ColHeaderType"),
			rb.getString("ColHeaderPurge"),
			rb.getString("ColHeaderLoco"),
			rb.getString("ColHeaderSpeed"),
			rb.getString("ColHeaderDir"),
			rb.getString("ColHeaderMode"),
			rb.getString("ColHeaderConsist"),
			rb.getString("ColHeaderF0"),
			rb.getString("ColHeaderF1"),
			rb.getString("ColHeaderF2"),
			rb.getString("ColHeaderF3"),
			rb.getString("ColHeaderF4"),
			rb.getString("ColHeaderF5"),
			rb.getString("ColHeaderF6"),
			rb.getString("ColHeaderF7"),
			rb.getString("ColHeaderF8"),
			rb.getString("ColHeaderF9"),
			rb.getString("ColHeaderText"),
			rb.getString("ColHeaderLastUsed")
	    };
	
	    private boolean showAllCabs = false;
	    private boolean showAllFunctions = true;
	    private boolean showCabDisplay = true;
	    
	    public int getColumnCount() {
	        return columnNames.length;
	    }
	
	    public int getRowCount() {
			int activeRows = 0;
	    	if (!getShowAllCabs()) {
	    		for (int i = 1; i < cabData.length; i++) {
	    			if ((NceShowCabPanel.cabFlag1Array[i] & NceShowCabPanel.FLAGS1_MASK_CABISACTIVE) == NceShowCabPanel.FLAGS1_CABISACTIVE) {
	    				activeRows++;
	    			}
	    		}
	    	} else {
	    		activeRows = cabData.length;
	    	}
	        return activeRows * 3;
	    }
	    
	    /** 
	     * Return cabId for row number
	     */
	    protected int getCabIdForRow(int row) {
	    	int activeRows = -1;
	    	if (!getShowAllCabs()) {
	    		for (int i = 1; i < cabData.length; i++) {
	    			if ((NceShowCabPanel.cabFlag1Array[i] & NceShowCabPanel.FLAGS1_MASK_CABISACTIVE) == NceShowCabPanel.FLAGS1_CABISACTIVE) {
	    				activeRows++;
	    				if (row == activeRows) {
	    					return i;
	    				}
	    			}
	    		}
	    	}
	    	return row;
	    }
	
	    public String getColumnName(int col) {
	        return columnNames[col];
	    }
	
	    public Object getValueAt(int row, int col) {
	    	int dRow = getCabIdForRow(row / 3);
	    	int sRow = row % 3;
	    	dataRow r = cabData[dRow];
	    	boolean activeCab = (NceShowCabPanel.cabFlag1Array[dRow] & NceShowCabPanel.FLAGS1_MASK_CABISACTIVE) == NceShowCabPanel.FLAGS1_CABISACTIVE;
	    	if (r == null) {
	    		return null;
	    	}
	    	if (!activeCab && (col != 0)) {
	    		return null;
	    	}
	    	switch (col) {
	    	case 0:
	    		if (sRow == 0) {
	        		return r.cab;
	    		}
	    		break;
	    	case 1:
	    		if (sRow == 0) {
	        		return r.type;
	    		}
	    		break;
	    	case 2:
	    		if (col == 0) {
	    			return null;
	    		}
	    		if (sRow == 0) {
	    			return rb.getString("ButtonPurgeCab");
	    		}
	    		break;
	    	case 3:
	    		if (sRow == 0) {
	        		return r.loco;
	    		}
	    		break;
	    	case 4:
	    		if (sRow == 0) {
	        		return r.speed;
	    		}
	    		break;
	    	case 5:
	    		if (sRow == 0) {
	        		return r.dir;
	    		}
	    		break;
	    	case 6:
	    		if (sRow == 0) {
	        		return r.mode;
	    		}
	    		break;
	    	case 7:
	    		if (sRow == 0) {
	        		return r.consist;
	    		}
	    		break;
	    	case 8:
	    		switch (sRow) {
	    		case 0:
	        		return r.F0;
	    		case 1:
	    			return r.F10;
	    		case 2:
	    			return r.F20;
	    		}
	    		break;
	    	case 9:
	    		switch (sRow) {
	    		case 0:
	        		return r.F1;
	    		case 1:
	    			return r.F11;
	    		case 2:
	    			return r.F21;
	    		}
	    		break;
	    	case 10:
	    		switch (sRow) {
	    		case 0:
	        		return r.F2;
	    		case 1:
	    			return r.F12;
	    		case 2:
	    			return r.F22;
	    		}
	    		break;
	    	case 11:
	    		switch (sRow) {
	    		case 0:
	        		return r.F3;
	    		case 1:
	    			return r.F13;
	    		case 2:
	    			return r.F23;
	    		}
	    	case 12:
	    		switch (sRow) {
	    		case 0:
	        		return r.F4;
	    		case 1:
	    			return r.F14;
	    		case 2:
	    			return r.F24;
	    		}
	    		break;
	    	case 13:
	    		switch (sRow) {
	    		case 0:
	        		return r.F5;
	    		case 1:
	    			return r.F15;
	    		case 2:
	    			return r.F25;
	    		}
	    		break;
	    	case 14:
	    		switch (sRow) {
	    		case 0:
	        		return r.F6;
	    		case 1:
	    			return r.F16;
	    		case 2:
	    			return r.F26;
	    		}
	    		break;
	    	case 15:
	    		switch (sRow) {
	    		case 0:
	        		return r.F7;
	    		case 1:
	    			return r.F17;
	    		case 2:
	    			return r.F27;
	    		}
	    		break;
	    	case 16:
	    		switch (sRow) {
	    		case 0:
	        		return r.F8;
	    		case 1:
	    			return r.F18;
	    		case 2:
	    			return r.F28;
	    		}
	    	case 17:
	    		switch (sRow) {
	    		case 0:
	        		return r.F9;
	    		case 1:
	    			return r.F19;
	    		}
	    		break;
	    	case 18:
	    		switch (sRow) {
	    		case 0:
	    			return r.text1;
	    		case 1:
	        		return r.text2;
	    		}
	    		break;
	    	case 19:
	    		if (sRow == 0) {
	        		return r.lastChange;
	    		}
	    		break;
	    	}
	    	return null;
	    }
	
	    public void setValueAt(Object value, int row, int col) {
	    	int dRow = getCabIdForRow(row / 3);
	    	if (col == 2) {
	    		purgeCab(dRow);
	    	}
	    }
	    
	    @SuppressWarnings("unchecked")
		public Class getColumnClass(int c) {
	    	if (c == 0 || c == 3  || c == 5) {
	    		return Integer.class;
	    	} else if (c == 1 || c == 4 || (c >= 6 && c <= 7) || (c >= 18 && c <= 19)){
	    		return String.class;
	    	} else if (c >= 8 && c <= 17) {
	    		return Boolean.class;
	    	} else if (c == 2) {
	    		return JButton.class;
	    	} else {
	    		return null;
	    	}
	    }
	    
	    public int getPreferredWidth(int col) {
	    	int width = 0;
			if (col == 0) {
				width = new JTextField(3).getPreferredSize().width;
			} else if (col <= 1){
				width = new JTextField(4).getPreferredSize().width;
			} else if (col <= 2){
				width = new JButton(rb.getString("ButtonPurgeCab")).getPreferredSize().width;
			} else if (col <= 7){
				width = new JTextField(3).getPreferredSize().width;
			} else if (col <= 17){
				width = new JCheckBox().getPreferredSize().width;
			} else if (col <= 18){
				width = new JTextField(9).getPreferredSize().width;
			} else if (col <= 19){
				width = new JTextField(5).getPreferredSize().width;
			} else {
				width = 0;
			}
			return width;
		}
	    
	    public boolean isCellEditable(int row, int col) {
	        if (col == 2) {
	            return true;
	        } else {
	            return false;
	        }
	    }
	
		public boolean getShowAllCabs() {
			return showAllCabs;
		}
	
		public void setShowAllCabs(boolean b) {
			this.showAllCabs = b;
		}
		
		public boolean getShowAllFunctions() {
			return showAllFunctions;
		}
	
		public void setshowAllFunctions(boolean showAllFunctions) {
			this.showAllFunctions = showAllFunctions;
		}
	
		public boolean getShowCabDisplay() {
			return showCabDisplay;
		}
	
		public void setshowCabDisplay(boolean showCabDisplay) {
			this.showCabDisplay = showCabDisplay;
		}
	}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceShowCabPanel.class.getName());
}

