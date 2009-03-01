// NceShowCabFrame.java

package jmri.jmrix.nce.cab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceTrafficController;
@SuppressWarnings("unused")

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
 * 
 * @author Dan Boudreau Copyright (C) 2009
 * @version $Revision: 1.2 $
 */

public class NceShowCabFrame extends jmri.util.JmriJFrame implements jmri.jmrix.nce.NceListener {
	
	private static final int CS_CAB_MEM = 0x8800;	// start of NCE CS cab context page for cab 0
													// memory
	private static final int CAB_SIZE = 256;		// Each cab has 256 bytes
	private static final int CAB_CURR_SPEED = 32;	// NCE cab speed
	private static final int CAB_ADDR_H = 33; 		//loco address, high byte
	private static final int CAB_ADDR_L = 34; 		//loco address, low byte
	private static final int CAB_FLAGS = 35;		//FLAGS
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
	
	Thread NceCabUpdateThread;	
	
	// member declarations
	JLabel textNumer = new JLabel("Number");
    JLabel textCab = new JLabel("Cab Type");
    JLabel textAddress = new JLabel("Loco Address");
    JLabel textSpeed = new JLabel("Speed");
    JLabel textReply = new JLabel("Reply:");
    JLabel textStatus = new JLabel("");
    
    // major buttons
    JButton refreshButton = new JButton("Refresh");
    
    // check boxes
    JCheckBox checkBoxActive = new JCheckBox ("Active");
    // text field
    
    // for padding out panel
    JLabel space1 = new JLabel(" ");
    JLabel space2 = new JLabel(" ");
    
    JPanel cabsPanel = new JPanel();
    JScrollPane cabsPane;
    
    public NceShowCabFrame() {
        super();
        cabsPane = new JScrollPane(cabsPanel);
    	cabsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    public void initComponents() throws Exception {
    	// the following code sets the frame's initial state
    	
    	setTitle("Show NCE Cabs");
    	getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
    	cabsPane.setVisible(false);
    	//cabsPane.setMinimumSize(new Dimension(300,300));
    	
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
    	// row 1
    	refreshButton.setToolTipText("Press to reload table");
    	checkBoxActive.setToolTipText("Show only active cabs when selected");
    	checkBoxActive.setSelected(true);
    	addItem(p1, refreshButton, 2, 1);
    	addItem(p1, textStatus, 3, 1);
    	addItem(p1, checkBoxActive, 4, 1);
    	
    	// row 2
    	addItem(p1, space1, 2, 2);
    	
        // row 3
        addItem(p1, textNumer, 1,3);
        addItem(p1, textCab, 2,3);
        addItem(p1, textAddress, 3,3);
        addItem(p1, textSpeed, 4,3);
        
    	addButtonAction(refreshButton);
    	
    	getContentPane().add(p1);
    	getContentPane().add(cabsPane);
    	refreshPanel();

    	// set frame size for display
    	pack();
    	setSize(350, 350);
    }

    // refresh button
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
    	if (ae.getSource() == refreshButton) {
    		refreshPanel();
    	}
	}
 
    private void refreshPanel(){
    	// Set up a separate thread to read CS memory
        if (NceCabUpdateThread != null && NceCabUpdateThread.isAlive())	
        	return; // thread is already running
    	textStatus.setText("Reading NCE memory");
        waiting = 0;
    	NceCabUpdateThread = new Thread(new Runnable() {
    		public void run() {
    			cabUpdate();
    		}
    	});
    	NceCabUpdateThread.setName("NCE Cab Update");
    	NceCabUpdateThread.setPriority(Thread.MIN_PRIORITY);
    	NceCabUpdateThread.start();
    }
    
    // Thread to update cab info, allows the use of sleep or wait
    private void cabUpdate() {	
    	cabsPanel.removeAll();
    	cabsPanel.setLayout(new GridBagLayout());
        // build table of cabs
        for (int i=1; i<CAB_MAX; i++){
        	JLabel number = new JLabel();
        	JLabel type = new JLabel();
         	JLabel address = new JLabel();
           	JLabel speed = new JLabel();
        	readCabMemory1(i, CAB_FLAGS1);
           	if (!waitNce())
        		return;
        	log.debug("Read flag1 character "+recChar);
        	int flags1 = recChar & FLAGS1_MASK; // mask off don't care bits
        	if (flags1 == FLAGS1_PROCAB)
        		type.setText("ProCab");
        	else if (flags1 == FLAGS1_CAB04) 
        		type.setText("Cab04");
           	else if (flags1 == FLAGS1_USB)
        		type.setText("USB");
            else if (flags1 == FLAGS1_AIU)
        		type.setText("AIU");
            else {
            	if (checkBoxActive.isSelected())
            		continue;
            	type.setText("unknown");
            }
        	// add items to table
        	addItem(cabsPanel, number, 1, 4+i);
        	number.setText(Integer.toString(i));
        	addItem(cabsPanel, type, 2, 4+i);
        	addItem(cabsPanel, address, 3, 4+i);
          	addItem(cabsPanel, speed, 4, 4+i);
          	if (flags1 == FLAGS1_AIU)
          		continue;
          	// read 16 bytes of memory, we need 4 of them
        	readCabMemory16(i, CAB_CURR_SPEED);
        	if (!waitNce())
        		return;
        	// read the Speed byte
        	int readChar = recChars[0];
        	log.debug("Read speed character "+Integer.toString(readChar));
        	String sped = Integer.toString(readChar);
        	// read the FLAG byte
        	readChar = recChars[3];
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
        	// read the high address byte
        	readChar = recChars[1];
        	log.debug("Read address high character "+readChar);
        	int locoAddress = (readChar & 0x3F) *256;
        	// read the low address byte
        	readChar = recChars[2];
        	log.debug("Read address low character "+readChar);
        	locoAddress = locoAddress + (readChar & 0xFF);
        	address.setText(Integer.toString(locoAddress));
        }
    	validate();
    	cabsPane.setVisible(true);
    	textStatus.setText("");
    }
    
    // puts the thread to sleep while we wait for the read CS memory to complete
    private boolean waitNce(){
    	int count = 100;
       	while (waiting > 0){
    		try{
    			NceCabUpdateThread.sleep(60);
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
		log.debug("Receive character");
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

    // Reads 1 byte of NCE cab memory 
    private void readCabMemory1(int cabNum, int offset) {
       	int nceCabAddr = (cabNum * CAB_SIZE) + CS_CAB_MEM + offset;
    	replyLen = REPLY_1;			// Expect 1 byte response
    	waiting++;
		byte[] bl = NceBinaryCommand.accMemoryRead1(nceCabAddr);
		NceMessage m = NceMessage.createBinaryMessage(bl, REPLY_1);
		NceTrafficController.instance().sendNceMessage(m, this);
    }
    
    // Reads 16 bytes of NCE cab memory 
    private void readCabMemory16(int cabNum, int offset) {
       	int nceCabAddr = (cabNum * CAB_SIZE) + CS_CAB_MEM + offset;
    	replyLen = REPLY_16;			// Expect 16 byte response
    	waiting++;
		byte[] bl = NceBinaryCommand.accMemoryRead(nceCabAddr);
		NceMessage m = NceMessage.createBinaryMessage(bl, REPLY_16);
		NceTrafficController.instance().sendNceMessage(m, this);
    }
    
	protected void addItem(JPanel p, JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		p.add(c, gc);
	}
    
    private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}
   
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceShowCabFrame.class.getName());	
}

