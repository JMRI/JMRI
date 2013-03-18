package jmri.jmrix.nce.usbinterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.*;

import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Panel for configuring a NCE USB interface
 *
 * @author	ken cameron Copyright (C) 2013
 * @version             $Revision: 1 $
 */

public class UsbInterfacePanel extends jmri.jmrix.nce.swing.NcePanel implements jmri.jmrix.nce.NceListener {

	private static final long serialVersionUID = -4800241983719851490L;
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.usbinterface.UsbInterfaceBundle");
	
	private int replyLen = 0;						// expected byte length
	private int waiting = 0;						// to catch responses not
													// intended for this module
	private int foundCabId = -1;
	private int minCabNum = -1;		// either the USB or serial size depending on what we connect to
	private int maxCabNum = -1;		// either the USB or serial size depending on what we connect to
	private int minCabSetNum = -1;
	private int maxCabSetNum = -1;
	private boolean supportGet = false;	// can only get cab data if memory commands are supported

	private static final int CAB_MIN_USB = 2;			// USB cabs start at 2
	private static final int CAB_MIN_PRO = 2;			// Serial cabs start at 2
	private static final int CAB_MAX_USB_128 = 4;			// There are up to 10 cabs on 1.28
	private static final int CAB_MAX_USB_165 = 10;			// There are up to 10 cabs on 1.65
	private static final int CAB_MAX_PRO = 64;			// There are up to 64 cabs
	private static int SER_CAB_FLAGS1 = 101;		// NCE flag 1
	private static int USB_CAB_FLAGS1 = 70;			// NCE flag 1
	private static int CS_CAB_MEM_PRO = 0x8800;	// start of NCE CS cab context page for cab 0, PowerHouse/CS2
	private static int CAB_SIZE = 256;		// Each cab has 256 bytes
	private static final int FLAGS1_CABID_USB = 0x80;		// bit 0=0, bit 7=1;
	private static final int FLAGS1_CABISACTIVE = 0x02;	// if cab is active
	private static final int FLAGS1_MASK_CABID = 0x81;	// Only bits 0 and 7.
	private static final int FLAGS1_MASK_CABISACTIVE = 0x02;	// if cab is active

	private static final int REPLY_1 = 1;			// reply length of 1 byte
	private static final int REPLY_2 = 2;			// reply length of 2 byte
	private static final int REPLY_4 = 4;			// reply length of 4 byte
	    
	Thread NceCabUpdateThread;
	
    private NceTrafficController tc = null;

	JTextField newCabId = new JTextField(5);
    JLabel oldCabId = new JLabel("     ");
    JButton getButton = new JButton(rb.getString("ButtonGet"));
    JButton setButton = new JButton(rb.getString("ButtonSet"));
    
    JLabel space1 = new JLabel(" ");
    JLabel space2 = new JLabel("  ");
    JLabel space3 = new JLabel("   ");
    JLabel space4 = new JLabel("    ");
    JLabel space5 = new JLabel("     ");

    JLabel statusText = new JLabel();
    
    public UsbInterfacePanel() {
    	super();
    }
    
    public void initContext(Object context) throws Exception {
        if (context instanceof NceSystemConnectionMemo ) {
            try {
				initComponents((NceSystemConnectionMemo) context);
			} catch (Exception e) {
				//log.error("UsbInterface initContext failed");
			}
        }
    }

    public String getHelpTarget() { return "package.jmri.jmrix.nce.usbinterface.UsbInterfacePanel"; }
    
    public String getTitle() { 
    	StringBuilder x = new StringBuilder();
    	if (memo != null) {
    		x.append(memo.getUserName());
    	} else {
    		x.append("NCE_");
    	}
		x.append(": ");
    	x.append(rb.getString("TitleUsbInterface"));
        return x.toString(); 
    }
    
    public void initComponents(NceSystemConnectionMemo m) throws Exception {
    	this.memo = m;
        this.tc = m.getNceTrafficController();

        minCabNum = CAB_MIN_PRO;
        maxCabNum = CAB_MAX_PRO;
        minCabSetNum = CAB_MIN_PRO + 1;
        maxCabSetNum = CAB_MAX_PRO;
        if ((tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) &&
        		(tc.getCmdGroups() & NceTrafficController.CMDS_MEM) != 0) {
        	minCabNum = CAB_MIN_USB;
        	maxCabNum = CAB_MAX_USB_165;
        	supportGet = true;
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_1_65) {
        	maxCabSetNum = CAB_MAX_USB_165;
        } else {
        	maxCabSetNum = CAB_MAX_USB_128;
        }
        // general GUI config

    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	
        JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
    	p1.setPreferredSize(new Dimension(400, 75));
        oldCabId.setText(" ");
        if (supportGet) {
        	addItem(p1, new JLabel(rb.getString("LabelCurrentCabId")), 1, 1);
        	addItem(p1, oldCabId, 2, 1);
        	addItem(p1, getButton, 3, 1);
        	addButtonAction(getButton);
        } else {
        	addItem(p1, new JLabel(rb.getString("LabelGetNotSupported")), 2, 1);
        }

    	addItem(p1, new JLabel(rb.getString("LabelCurrentCabId")), 1, 2);
        newCabId.setText(" ");
    	addItem(p1, newCabId, 2, 2);
    	addItem(p1, setButton, 3, 2);
    	add(p1);

        JPanel p2 = new JPanel();
    	p2.setLayout(new GridBagLayout());
    	addItem(p2, new JLabel(rb.getString("LabelStatus")), 1, 1);
        statusText.setText(" ");
    	addItem(p2, statusText, 2, 1);
    	add(p2);

        if (supportGet) {
	        JPanel p3 = new JPanel();
	    	p3.setLayout(new GridBagLayout());

	    	addItem(p3, new JLabel(rb.getString("LabelWarning1")), 1, 1);
	    	addItem(p3, new JLabel(rb.getString("LabelWarning2")), 1, 2);
	    	add(p3);
        }
    	
    	addButtonAction(setButton);
    	
    	refreshPanel();
    }

    private void refreshPanel(){
    	if (supportGet) {
        	// Set up a separate thread to read CS memory
            if (NceCabUpdateThread != null && NceCabUpdateThread.isAlive())	
            	return; // thread is already running
        	statusText.setText(rb.getString("StatusReadingMemory"));
        	NceCabUpdateThread = new Thread(new Runnable() {
        		public void run() {
                	if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_NONE) {
                		cabUpdateSerial();
                	} else {
                		cabUpdateUsb();
                	}
        		}
        	});
        	NceCabUpdateThread.setName(rb.getString("ThreadTitle"));
        	NceCabUpdateThread.setPriority(Thread.MIN_PRIORITY);
        	NceCabUpdateThread.start();
    	}
    }
    
    private boolean firstTime = true; // wait for panel to display

    // Thread to update cab info, allows the use of sleep or wait, for serial connection
    private void cabUpdateSerial() {
    	
    	if (firstTime){
    		try {
    			Thread.sleep(1000);	// wait for panel to display 
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	firstTime = false;
    	foundCabId = 0;
        // build table of cabs
        for (int currCabId=minCabNum; currCabId <= maxCabNum; currCabId++){
           	
            statusText.setText(MessageFormat.format(rb.getString("StatusReadingCabId"), currCabId));
           	recChar = -1;
           	// read cab type by reading the FLAGS1 byte
            readCabMemory1(currCabId, SER_CAB_FLAGS1);
           	if (!waitNce())
        		return;
           	if (log.isDebugEnabled()) log.debug("ID = " + currCabId + " Read flag1 character " + recChar);
           	// test it really changed
           	if (recChar != -1) {
	        	if ((recChar & FLAGS1_MASK_CABISACTIVE) != FLAGS1_CABISACTIVE) {
	        		// not active slot
	            	continue;
	        	}
	        	int cabId = recChar & FLAGS1_MASK_CABID; // mask off don't care bits
	           	if (cabId == FLAGS1_CABID_USB) {
	           		foundCabId = currCabId;	// USB or Mini-Panel
	                statusText.setText(MessageFormat.format(rb.getString("StatusFound"), currCabId));
	           		break;
	           	}
           	}
        }

        if (foundCabId == 0) {
            statusText.setText(rb.getString("StatusNotFound"));
        } else {
        	oldCabId.setText(Integer.toString(foundCabId));
        }
    	this.setVisible(true);
    	this.repaint();
    }

    // Thread to update cab info, allows the use of sleep or wait, for NCE-USB connection
    private void cabUpdateUsb() {
    	
    	if (firstTime){
    		try {
    			Thread.sleep(1000);	// wait for panel to display 
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	firstTime = false;
    	foundCabId = 0;
        // build table of cabs
        for (int currCabId=minCabNum; (currCabId <= maxCabNum) && (foundCabId == 0); currCabId++){

            statusText.setText(MessageFormat.format(rb.getString("StatusReadingCabId"), currCabId));
           	recChar = -1;
           	// read cab type by reading the FLAGS1 byte
    		setUsbCabMemoryPointer(currCabId, USB_CAB_FLAGS1);
           	if (!waitNce())
        		return;
    		readUsbCabMemoryN(1);
           	if (!waitNce())
        		return;
           	if (log.isDebugEnabled()) log.debug("ID = " + currCabId + " Read flag1 character " + recChar);
           	// test it really changed
           	if (recChar != -1) {
	        	if ((recChar & FLAGS1_MASK_CABISACTIVE) != FLAGS1_CABISACTIVE) {
	        		// not active slot
	            	continue;
	        	}
	        	int cabId = recChar & FLAGS1_MASK_CABID; // mask off don't care bits
	           	if (cabId == FLAGS1_CABID_USB) {
	           		foundCabId = currCabId;	// USB or Mini-Panel
	                statusText.setText(MessageFormat.format(rb.getString("StatusFound"), currCabId));
	           	}
           	}
        }
     
        if (foundCabId == 0) {
            statusText.setText(rb.getString("StatusNotFound"));
        } else {
        	oldCabId.setText(Integer.toString(foundCabId));
        }
    	this.setVisible(true);
    	this.repaint();
    }

    // Thread to set cab id, allows the use of sleep or wait, for NCE-USB connection
    private void setCabId() {
    	
    	if (firstTime){
    		try {
    			Thread.sleep(1000);	// wait for panel to display 
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	firstTime = false;
       	recChar = -1;
       	// read cab type by reading the FLAGS1 byte
       	int i = Integer.parseInt(newCabId.getText().trim());
       	if ((i >= minCabSetNum) && (i <= maxCabSetNum)) {
	        statusText.setText(MessageFormat.format(rb.getString("StatusSetIdStart"), i));
			writeUsbCabId(i);
	       	if (!waitNce())
	    		return;
	
	        statusText.setText(MessageFormat.format(rb.getString("StatusSetIdFinished"), i));
       	} else {
	        statusText.setText(MessageFormat.format(rb.getString("StatusInvalidCabId"), i, minCabSetNum, maxCabSetNum));
       	}
    	this.setVisible(true);
    	this.repaint();
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
			statusText.setText(rb.getString("StatusError"));
			return;
		}
		// Read one byte
		if (replyLen == REPLY_1) {
			// Looking for proper response
			recChar = r.getElement(0);
		}
		// Read two byte
		if (replyLen == REPLY_2) {
			// Looking for proper response
			for (int i=0; i<REPLY_2; i++){
				recChars[i] = r.getElement(i);
			}
		}
		// Read four byte
		if (replyLen == REPLY_4) {
			// Looking for proper response
			for (int i=0; i<REPLY_4; i++){
				recChars[i] = r.getElement(i);
			}
		}
		// wake up thread
		synchronized (this) {
			notify();
		}
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
    			statusText.setText("Error");
    			return false;
    		}
    	}
    	if (log.isDebugEnabled()) {
    		log.debug("awake!");
    	}
       	return true;
    }
    
    // USB set cab memory pointer
    private void setUsbCabMemoryPointer(int cab, int offset) {
    	replyLen = REPLY_1;			// Expect 1 byte response
    	waiting++;
		byte[] bl = NceBinaryCommand.usbMemoryPointer(cab, offset);
		NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_1);
		tc.sendNceMessage(m, this);
    }
    
    // USB Read N bytes of NCE cab memory 
    private void readUsbCabMemoryN(int num) {
    	switch (num) {
    	case 1:
    		replyLen = REPLY_1;	// Expect 1 byte response
    		break;
    	case 2:
    		replyLen = REPLY_2;	// Expect 2 byte response
    		break;
    	case 4:
    		replyLen = REPLY_4;	// Expect 4 byte response
    		break;
		default:
			log.error("Invalid usb read byte count");
			return;
    	}
    	waiting++;
		byte[] bl = NceBinaryCommand.usbMemoryRead((byte)num);
		NceMessage m = NceMessage.createBinaryMessage(tc, bl, replyLen);
		tc.sendNceMessage(m, this);
    }
    
    // USB set Cab Id in USB 
    private void writeUsbCabId(int value) {
    	replyLen = REPLY_1;			// Expect 1 byte response
    	waiting++;
		byte[] bl = NceBinaryCommand.usbSetCabId(value);
		NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_1);
		tc.sendNceMessage(m, this);
    }
    
    // Reads 1 byte of NCE cab memory 
    private void readCabMemory1(int cabNum, int offset) {
       	int nceCabAddr = (cabNum * CAB_SIZE) + CS_CAB_MEM_PRO + offset;
    	replyLen = REPLY_1;			// Expect 1 byte response
    	waiting++;
		byte[] bl = NceBinaryCommand.accMemoryRead1(nceCabAddr);
		NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_1);
		tc.sendNceMessage(m, this);
    }
        
    /**
     * add item to a panel
     * @param p Panel Id
     * @param c Component Id
     * @param x Column
     * @param y Row
     */
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

    // button actions
    public void buttonActionPerformed(ActionEvent ae) {
    	Object src = ae.getSource();
    	if (src == getButton) {
    		refreshPanel();
    	} else if (src == setButton) {
        		setCabId();
    	} else {
    		log.error("unknown action performed: " + src);
    	}
	}

    static Logger log = LoggerFactory
	.getLogger(UsbInterfacePanel.class.getName());
}
