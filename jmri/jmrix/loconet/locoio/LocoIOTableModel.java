// LocoIOTableModel.java

package jmri.jmrix.loconet.locoio;

import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

import jmri.jmrix.loconet.*;

/**
 * Configurer for LocoIO hardware.
 *<P>
 * The basic logic to display buttons
 * is described in Chapter 7 of "Core Swing Advanced Programming".
 * <P>
 * Note that the "mode" CV in the LocoIO doesn't fully specify the
 * operation being done.  For example, 0x80 is both turnout closed and
 * turnout high; the difference is in the address field.  We read and
 * write mode _last_ to handle this.
 * <P>
 * The "addr" field contains a four-character, two-byte hex number.
 * This is the visible two bytes from LocoNet OPC_INPUT_REP and OPC_SW_REQ
 * messages, so the "low bits of the address" are actually the left byte
 * of the field.  Value1 in the LocoIO is filled from the left byte,
 * Value2 from the right.
 * <P>
 * The timeout code here is modelled after that in jmri,jmrix.AbstractProgrammer,
 * though there are significant modifications.
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.14 $
 */
public class LocoIOTableModel extends javax.swing.table.AbstractTableModel
    implements LocoNetListener {
    
    /**
     * Address of this device
     */
    int unitAddress;
    public void setUnitAddress(int unit) {
        unitAddress = unit&0x7F7F;  // protect against high bits set
    }
    
    /**
     * Define the number of rows in the table, which is also
     * the number of "channels" in a signel LocoIO unit
     */
    private int _numRows = 16;
    
    /**
     * Define the contents of the individual columns
     */
    public static final int PINCOLUMN   	= 0;  // pin number
    public static final int ONMODECOLUMN   = 1;  // what makes this happen?
    public static final int ADDRCOLUMN 	= 2;  // what address is involved?
    public static final int CAPTURECOLUMN 	= 3;  // "capture" button
    public static final int READCOLUMN  	= 4;  // "read" button
    public static final int WRITECOLUMN 	= 5;  // "write" button
    public static final int HIGHESTCOLUMN 	= WRITECOLUMN+1;
    
    // store the modes
    Object[] addr = new Object[_numRows];
    Object[] set  = new Object[_numRows];
    Object[] onMode = {"<none>", "<none>", "<none>", "<none>", "<none>", "<none>", "<none>", "<none>",
                       "<none>", "<none>", "<none>", "<none>", "<none>", "<none>", "<none>", "<none>"};
    
    /**
     * The addr field (for the address info used in each LocoIO channel)
     * is stored as a string.  This converts it to an integer.
     * @param row
     * @return integer value of addr[row]
     */
    int addrFieldAsInt(int row) {
        return Integer.valueOf((String)addr[row],16).intValue();
    }
    
    /**
     * Code for read activity needed.  See states
     * NONE, READMODE, READINGMODE,
     * READVALUE1, READINGVALUE1,
     * READVALUE2, READINGVALUE2
     */
    int[] needRead = new int[_numRows];
    
    protected final int NONE = 0;
    protected final int READVALUE1 = 1;
    protected final int READINGVALUE1 = 2;
    protected final int READVALUE2 = 3;
    protected final int READINGVALUE2 = 4;
    protected final int READMODE = 5;
    protected final int READINGMODE = 6;
    
    protected final int READ = READVALUE1;  // starting state
    
    /**
     * Code for write activity needed.  See states
     * NONE, WRITEMODE, WRITINGMODE,
     * WRITEVALUE1, WRITINGVALUE1,
     * WRITEVALUE2, WRITINGVALUE2
     */
    int[] needWrite = new int[_numRows];
    
    protected final int WRITEVALUE1 = 11;
    protected final int WRITINGVALUE1 = 12;
    protected final int WRITEVALUE2 = 13;
    protected final int WRITINGVALUE2 = 14;
    protected final int WRITEMODE = 15;
    protected final int WRITINGMODE = 16;
    
    protected final int WRITE = WRITEVALUE1;  // starting state
    
    /**
     * Record whether this pin is looking to capture a value
     * from the LocoNet
     */
    protected boolean[] capture = new boolean[_numRows];
    
    /**
     * Reference to the JTextField which should receive status info
     */
    JTextField status = null;
    
    /**
     * Primary constructor.  Initializes all the arrays.
     * @param addr Address for this LocoIO unit.  Provided here,
     * but may also be changed later with setUnitAddress.
     */
    public LocoIOTableModel(int unitAddr, JTextField status) {
        super();
        unitAddress=unitAddr;
        this.status = status;
        for (int i=0; i<_numRows; i++) {
            onMode[i] = "<none>";
            addr[i]="0";
            needRead[i] = NONE;
            needWrite[i] = NONE;
            capture[i] = false;
        }
        // for now, we're always listening to LocoNet
        if (LnTrafficController.instance() != null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            log.error("No LocoNet interface available");
    }
    
    // basic methods for AbstractTableModel implementation
    public int getRowCount() { return _numRows; }
    
    public int getColumnCount( ){ return HIGHESTCOLUMN;}
    
    public String getColumnName(int col) {
        switch (col) {
        case PINCOLUMN: return "Pin";
        case ONMODECOLUMN: return "Action";
        case ADDRCOLUMN: return "Hex Value1,Value2";
        case CAPTURECOLUMN: return "";
        case READCOLUMN: return "";
        case WRITECOLUMN: return "";
        default: return "unknown";
        }
    }
    
    public Class getColumnClass(int col) {
        switch (col) {
        case PINCOLUMN: return String.class;
        case ONMODECOLUMN: return String.class;
        case ADDRCOLUMN: return String.class;
        case CAPTURECOLUMN: return JButton.class;
        case READCOLUMN: return JButton.class;
        case WRITECOLUMN: return JButton.class;
        default: return null;
        }
    }
    
    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case PINCOLUMN: return false;
        case ONMODECOLUMN: return true;
        case ADDRCOLUMN: return true;
        case CAPTURECOLUMN: return true;
        case READCOLUMN: return true;
        case WRITECOLUMN: return true;
        default: return false;
        }
    }
    
    public Object getValueAt(int row, int col) {
        switch (col) {
        case PINCOLUMN:
            return Integer.toString(row*2+1);  // 1 through 33 by 2
        case ONMODECOLUMN:
            return onMode[row];
        case ADDRCOLUMN:
            return addr[row];
        case CAPTURECOLUMN:
            return "Capture";
        case READCOLUMN:
            return "Read";
        case WRITECOLUMN:
            return "Write";
        default: return "unknown";
        }
    }
    
    public int getPreferredWidth(int col) {
        switch (col) {
        case PINCOLUMN:
            return  new JLabel(" 31 ").getPreferredSize().width;
        case ONMODECOLUMN:
            return  new JLabel("Turnout closed status message").getPreferredSize().width;
        case ADDRCOLUMN:
            return  new JLabel(getColumnName(ADDRCOLUMN)).getPreferredSize().width;
        case CAPTURECOLUMN:
        case READCOLUMN:
        case WRITECOLUMN:
            return new JButton("Capture").getPreferredSize().width;
        default: return new JLabel(" <unknown> ").getPreferredSize().width;
        }
    }
    
    public void setValueAt(Object value, int row, int col) {
        if (col == ONMODECOLUMN) {
            if (isValidOnValue(value)) {
                onMode[row] = value;
                // have to handle two cases specially:
                if (value.equals(TURNOUTCLOSE)) {
                    // turnout closed - force address
                    addr[row] = Integer.toHexString(addrFieldAsInt(row)|(LnConstants.OPC_SW_REQ_DIR));
                } else if (value.equals(TURNOUTTHROW)) {
                    // turnout open - force address
                    addr[row] = Integer.toHexString(addrFieldAsInt(row)&~(LnConstants.OPC_SW_REQ_DIR));
                }
                fireTableRowsUpdated(row,row);
            }
        } else if (col == ADDRCOLUMN) {
            addr[row] = value;
            // have to handle two cases specially:
            if (onMode[row].equals(TURNOUTCLOSE) || onMode[row].equals(TURNOUTTHROW))
                // turnout closed, turnout thrown - update from address
                if ( (addrFieldAsInt(row)&(LnConstants.OPC_SW_REQ_DIR)) !=0) {
                    // closed
                    onMode[row] = TURNOUTCLOSE;
                } else {
                    // thrown
                    onMode[row] = TURNOUTTHROW;
                }
            fireTableRowsUpdated(row,row);
        } else if (col == CAPTURECOLUMN) {
            // start a capture operation
            capture[row] = true;
        } else if (col == READCOLUMN) {
            // start a read operation
            needRead[row] = READ;
            issueNextOperation();
        } else if (col == WRITECOLUMN) {
            // start a read operation
            needWrite[row] = WRITE;
            issueNextOperation();
        }
    }
    
    /**
     * Start reading all rows back
     */
    public void readAll() {
        for (int row=0; row<_numRows; row++) {
            needRead[row] = READ;
        }
        issueNextOperation();
    }
    
    /**
     * Start writing all rows out
     */
    public void writeAll() {
        for (int row=0; row<_numRows; row++) {
            needWrite[row] = WRITE;
        }
        issueNextOperation();
    }
    
    protected int highPart(int value) { // generally value 1
        return value/256;
    }
    
    protected int lowPart(int value) { // generally value 2
        return value-256*highPart(value);
    }
    
    protected boolean isValidOnValue(Object value) {
        if (value instanceof String) {
            String sValue = (String) value;
            for (int i=0; i<validOnModes.length; i++) {
                if (sValue.equals(validOnModes[i])) return true;
            }
        }
        return false;
    }
    
    public static String[] getValidOnModes() { return validOnModes; }
    
    // define the various possible operation modes
    protected static final String TURNOUTCLOSE  = "Turnout close cmd sets output";
    protected static final String TURNOUTTHROW  = "Turnout throw cmd sets output";
    protected static final String STATUSMESSAGE = "Status message sets output";
    protected static final String TOGGLESWITCH  = "Toggle switch controls turnout";
    protected static final String PUSHBUTTONLO  = "Input low flips turnout";
    protected static final String PUSHBUTTONHI  = "Input high flips turnout";
    
    static String[] validOnModes = {
        TURNOUTCLOSE, TURNOUTTHROW,
        STATUSMESSAGE,
        TOGGLESWITCH,
        PUSHBUTTONLO, PUSHBUTTONHI,
    };
    static int[] codeForMode = {0x80, 0x80, 0xC0, 0x0F, 0x2F, 0x6F};
    
    int codeFromModeString(String mode) {
        for (int i=0; i<codeForMode.length; i++)
            if (mode.equals(validOnModes[i])) return codeForMode[i];
        return -1;
    }
    
    /**
     * Convert a configuration cv and two-byte address value to
     * a mode string.
     * @param cv The configuration CV from the LocoIO module
     * @param addr  The two-byte address from the LocoIO module
     * @return Mode name corresponding to these config values
     */
    String modeFromValues(int cv, int addr) {
        // have to handle "Turnout closed" and "Turnout thrown" with ugly code
        // which assumes a particular array layout.  Note that
        // our only hope of keeping this consistent is the JUnit tests
        if (cv==0x80) {
            if ( ((LnConstants.OPC_SW_REQ_DIR)&addr) !=0 ) {
                // closed
                return TURNOUTCLOSE;
            } else {
                // thrown
                return TURNOUTTHROW;
            }
        }
        for (int i=0; i<codeForMode.length; i++) {
            if (cv==codeForMode[i]) return validOnModes[i];
        }
        return "";
    }
    
    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        
        // disconnect from future events
        stopTimer();
        LnTrafficController.instance().removeLocoNetListener(~0, this);
        // null references, so that they can be gc'd even if this isn't.
        addr = null;
        set = null;
        onMode = null;
    }
    
    int lastOpCv = -1;
    boolean reading = false;  // false means write in progress
    
    /**
     * Listen to the LocoNet.
     * We're listening for incoming OPC_XFR messages, which might
     * be part of a read or write sequence.  We're also
     * _sometimes_ listening for commands as part of a "capture"
     * operation.
     *<P>
     * The incoming LocoNet OPC_PEER_XFR messages don't retain any information
     * about the CV number or whether it was a read or write operation.
     * We store the data regardless of whether it was read or write, but
     * we need to remember the cv number in the lastOpCv member.
     *<P>
     * @param m Incoming message
     */
    public void message(LocoNetMessage m) {
        // sort out the opCode
        int opCode = m.getOpCode();
        switch (opCode) {
        case LnConstants.OPC_PEER_XFER:
            // could be read or write operation
            // check that src_low_address is our unit, and
            // dst is our 0x0150
            int src = m.getElement(2);
            int dst = m.getElement(3)+m.getElement(4)*256;
            log.debug("OPC_PEER_XFER with src = "+src+" dest = "+dst);
            if (dst==0x0150 && src==lowPart(unitAddress)) {
                // yes, we assume this is a reply to us
                stopTimer();
                replyReceived(); // advance state
                int[] contents = m.getPeerXfrData();
                log.debug("accepted, contents = "
                          +contents[0]+","+contents[1]+","+contents[2]+","
                          +contents[3]+", "+contents[4]+","+contents[5]+","
                          +contents[6]+","+contents[7]);
                if (contents[0] == 2 || reading) {  // read command
                    // get data and store
                    if (lastOpCv<0 || lastOpCv>50)
                        log.error("last CV recorded is invalid: "+lastOpCv);
                    
                    // there are two formats of the return packet...
                    int data = 0;
                    if (contents[2] != 0){
                        data = contents[5];
                        log.debug("1.3.4 format, data = "+data);
                    } else if (contents[2] == 0) {
                        data = contents[7];
                        log.debug("1.3.2 format, data = "+data);
                    } else {
                        log.error("unexpected contents in byte 2: "+contents[2]);
                    }
                    int channel = (lastOpCv/3)-1;
                    if (channel<0) log.warn("channel is less than zero");
                    int type = lastOpCv - (channel*3+3);
                    // type = 0 for Mode, 1 for value 1, 2 for value 2
                    log.debug("channel = "+channel+" type = "+type);
                    if (type==0) {
                        // mode
                        onMode[channel] = modeFromValues(data, addrFieldAsInt(channel));
                    } else if (type==2) {
                        // value 2 - can't entirely replace anything that's there,
                        // as this happens on write, and high part not written yet
                        int oldAddr = addrFieldAsInt(channel);
                        int newAddr = highPart(oldAddr)*256+data;
                        addr[channel]=Integer.toHexString(newAddr);
                    } else {
                        // value 1
                        int oldAddr = addrFieldAsInt(channel);
                        int newAddr = lowPart(oldAddr)+256*data;
                        addr[channel]=Integer.toHexString(newAddr);
                    }
                    // tell the table to update
                    fireTableRowsUpdated(channel,channel);
                    
                }  // end of read processing
                
                // check for anything else to do
                issueNextOperation();
                return;
            } else return;
        case LnConstants.OPC_INPUT_REP:
            // these might require capture
            if (log.isDebugEnabled()) log.debug("OPC_INPUT_REP received");
            for (int i=0; i<_numRows; i++) {
                if (capture[i]) {
                    if (log.isDebugEnabled()) log.debug("row set for capture: "+i);
                    // This is a capture request, get address bytes
                    int val1 = m.getElement(1);
                    int val2 = m.getElement(2);
                    // save result, mark as done
                    addr[i] = Integer.toHexString(val1*256+val2);
                    capture[i] = false;
                    fireTableRowsUpdated(i,i);
                }
            }
            return;
            
        case LnConstants.OPC_SW_REQ:
            // these might require capture
            if (log.isDebugEnabled()) log.debug("OPC_INPUT_REP received");
            for (int i=0; i<_numRows; i++) {
                if (capture[i]) {
                    if (log.isDebugEnabled()) log.debug("row set for capture: "+i);
                    // This is a capture request, get address bytes
                    int val1 = m.getElement(1);
                    int val2 = m.getElement(2);
                    // the close/throw bit is suppressed, so that mode
                    // doesn't need to be changed
                    val2 = val2&(~LnConstants.OPC_SW_ACK_CLOSED);
                    // save result, mark as done
                    addr[i] = Integer.toHexString(val1*256+val2);
                    capture[i] = false;
                    fireTableRowsUpdated(i,i);
                }
            }
            return;
            
        default:
            // we ignore the default
        }
    }
    
    /**
     * A valid reply has been received, so the read/write
     * worked, and the state should be advanced.
     */
    protected void replyReceived() {
        switch (needRead[currentPin]) {
            // NONE means to try the write operations
        case NONE:
            break;
        case READVALUE1:
        case READINGVALUE1:
            needRead[currentPin] = READVALUE2;
            return;
        case READVALUE2:
        case READINGVALUE2:
            needRead[currentPin] = READMODE;
            return;
        case READMODE:
        case READINGMODE:
            needRead[currentPin] = NONE;
            return;
        default:
            log.error("Pin "+currentPin+" unexpected read state, can't advance "+needRead[currentPin]);
            needRead[currentPin] = NONE;
            return;
        }
        switch (needWrite[currentPin]) {
        case NONE:
            return;
        case WRITEVALUE1:
        case WRITINGVALUE1:
            needWrite[currentPin] = WRITEVALUE2;
            return;
        case WRITEVALUE2:
        case WRITINGVALUE2:
            needWrite[currentPin] = WRITEMODE;
            return;
        case WRITEMODE:
        case WRITINGMODE:
            needWrite[currentPin] = NONE;
            return;
        default:
            log.error("Pin "+currentPin+" unexpected write state, can't advance "+needWrite[currentPin]);
            needWrite[currentPin] = NONE;
            return;
        }
    }
    
    int currentPin = 0;
    
    /**
     * Look through the table to find the next thing that
     * needs to be read.
     */
    protected void issueNextOperation() {
        // stop the timer while we figure this out
        stopTimer();
        // find the first item that needs to be read
        for (int i=0; i<_numRows; i++) {
            currentPin = i;
            if (needRead[i]!=NONE) {
                // yes, needs read.  Find what kind
                switch (needRead[i]) {
                case READVALUE1:
                case READINGVALUE1:
                    // set new state, send read, then done
                    needRead[i] = READINGVALUE1;
                    if (status!=null) status.setText("read value 1");
                    lastOpCv = i*3+4;
                    sendReadCommand(lastOpCv);
                    return;
                case READVALUE2:
                case READINGVALUE2:
                    // set new state, send read, then done
                    needRead[i] = READINGVALUE2;
                    if (status!=null) status.setText("read value 2");
                    lastOpCv = i*3+5;
                    sendReadCommand(lastOpCv);
                    return;
                case READMODE:
                case READINGMODE:
                    // set new state, send read, then done
                    needRead[i] = READINGMODE;
                    if (status!=null) status.setText("read mode");
                    lastOpCv = i*3+3;
                    sendReadCommand(lastOpCv);
                    return;
                default:
                    log.error("found an unexpected state: "+needRead[1]+" "+i);
                    return;
                }
            }
        }
        // no reads, so continue to check writes
        for (int i=0; i<_numRows; i++) {
            currentPin = i;
            if (needWrite[i]!=NONE) {
                // yes, needs read.  Find what kind
                switch (needWrite[i]) {
                case WRITEVALUE1:
                case WRITINGVALUE1:
                    // set new state, send read, then done
                    needWrite[i] = WRITINGVALUE1;
                    if (status!=null) status.setText("write value 1");
                    lastOpCv = i*3+4;
                    sendWriteCommand(lastOpCv, highPart(addrFieldAsInt(i)));
                    return;
                case WRITEVALUE2:
                case WRITINGVALUE2:
                    // set new state, send read, then done
                    needWrite[i] = WRITINGVALUE2;
                    if (status!=null) status.setText("write value 2");
                    lastOpCv = i*3+5;
                    sendWriteCommand(lastOpCv, lowPart(addrFieldAsInt(i)));
                    return;
                case WRITEMODE:
                case WRITINGMODE:
                    // set new state, send write, then done
                    needWrite[i] = WRITINGMODE;
                    if (status!=null) status.setText("write mode");
                    lastOpCv = i*3+3;
                    sendWriteCommand(lastOpCv, codeFromModeString((String)onMode[i]));
                    return;
                    
                default:
                    log.error("found an unexpected state: "+needWrite[1]+" "+i);
                    return;
                }
            }
        }
        // nothing of interest found, so just end gracefully
        if (log.isDebugEnabled()) log.debug("No operation needed");
        if (status!=null) status.setText("OK");
        currentPin = 0;
    }
    
    /**
     * Internal routine to handle a timeout during read/write
     * by retrying the same operation.
     */
    synchronized protected void timeout() {
        if (log.isDebugEnabled()) log.debug("timeout!");
        issueNextOperation();
    }
    
    /**
     * Internal routine to start timer to protect the mode-change.
     */
    protected void startTimer() {
        restartTimer(TIMEOUT);
    }
    
    /**
     * Internal routine to stop timer, as all is well
     */
    protected void stopTimer() {
        if (timer!=null) timer.stop();
    }
    
    /**
     * Internal routine to handle timer starts & restarts
     */
    protected void restartTimer(int delay) {
        if (timer==null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        timeout();
                    }
                });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }
    
    static private int TIMEOUT=2000;
    
    javax.swing.Timer timer = null;
    
    /**
     * Format and send a read command to the LocoIO device
     * at the known unit address
     * @param cv
     */
    protected void sendReadCommand(int cv) {
        // remember current op is read
        reading = true;
        // format a read message
        int[] contents = {2,cv,0,0,  0,0,0,0};
        
        LocoNetMessage msg = LocoNetMessage.makePeerXfr(0x1050, unitAddress,
                                                        contents, 0x08);
        // send message
        LnTrafficController.instance().sendLocoNetMessage(msg);
        // and set timeout on reply
        startTimer();
    }
    
    protected void sendWriteCommand(int cv, int data) {
        // remember current op is write
        reading = false;
        // format a write message
        int[] contents = {1,cv,0,data,  0,0,0,0};
        
        LocoNetMessage msg = LocoNetMessage.makePeerXfr(0x1050, unitAddress,
                                                        contents, 0x08);
        // send message
        LnTrafficController.instance().sendLocoNetMessage(msg);
        // and set timeout on reply
        startTimer();
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIOTableModel.class.getName());
}
