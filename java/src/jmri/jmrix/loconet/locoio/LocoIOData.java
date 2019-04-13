package jmri.jmrix.loconet.locoio;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data associated with a LocoIO device.
 * 
 * @author John Plocher, January 28, 2007
 */
public class LocoIOData
        implements LocoNetListener, java.beans.PropertyChangeListener {

    private int sv0;
    private int unitAddress;
    private int unitSubAddress;
    private LnTrafficController tc;

    /*
     * This data model is shared between several views; each
     * needs to know when the data changes out from under it.
     */
    private PropertyChangeSupport dataListeners = new PropertyChangeSupport(this);

    /**
     * Define the number of rows in the table, which is also the number of
     * "channels" in a single LocoIO unit.
     */
    private int _numRows = 16;
    /**
     * LocoBuffer always has address 0x01 0x50.
     */
    private static final int LocoBufferAddress = 0x0150;
    private String locoBufferVersion = Bundle.getMessage("StateUnknown");
    private String locoIOVersion = Bundle.getMessage("StateUnknown");
    private String status = Bundle.getMessage("StateUnknown");  // LocoIO activity status
    /**
     * Per-port SV data.
     */
    private LocoIOMode[] lim = new LocoIOMode[_numRows];
    private int[] addr = new int[_numRows];
    private int[] sv = new int[_numRows];
    private int[] v1 = new int[_numRows];
    private int[] v2 = new int[_numRows];
    private int[] readState = new int[_numRows];
    private int[] writeState = new int[_numRows];

    /**
     * Record whether this pin is looking to capture a value from the LocoNet.
     */
    private boolean[] capture = new boolean[_numRows];
    private String[] mode = new String[_numRows];

    private LocoIOModeList validmodes;

    /**
     * Create a new instance of LocoIOData.
     */
    public LocoIOData(int unitAddr, int unitSubAddr, LnTrafficController tc) {
        timeoutcounter = 0;
        unitAddress = unitAddr;
        unitSubAddress = unitSubAddr;
        validmodes = new LocoIOModeList();

        for (int i = 0; i < _numRows; i++) {
            setMode(i, "<none>"); // NOI18N
            lim[i] = null;
            setAddr(i, 0);
            setSV(i, 0);
            setV1(i, 0);
            setV2(i, 0);
            readState[i] = NONE;
            writeState[i] = NONE;
            capture[i] = false;
        }
        // addPropertyChangeListener(this);
        this.tc = tc;
        // for now, we're always listening to LocoNet
        if (tc != null) {
            tc.addLocoNetListener(~0, this);
        } else {
            log.error("No LocoNet interface available"); // NOI18N
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        dataListeners.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        dataListeners.removePropertyChangeListener(pcl);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        log.info("LocoIOData: {} := {} from {}", // NOI18N
                evt.getPropertyName(), evt.getNewValue(), evt.getSource());
    }

    /**
     * Address and SubAddress of this device.
     * <p>
     * High byte of the Address is fixed to 0x01
     * <br>
     * Low byte Address must be in the range of 0x01 .. 0x4F, 0x51 .. 0x7F
     * <br>
     * (0x50 is reserved for the LocoBuffer)
     * <br>
     * The subAddress is in the range of 0x01 .. 0x7E
     * <br>
     * (0x7F is reserved)
     */
    public synchronized void setUnitAddress(int unit, int unitSub) {
        setUnitAddress(unit);
        setUnitSubAddress(unitSub);
    }

    public synchronized void setUnitAddress(int unit) {
        dataListeners.firePropertyChange("UnitAddress", Integer.valueOf(unitAddress), Integer.valueOf(0x0100 | (unit & 0x07F))); // NOI18N
        unitAddress = 0x0100 | (unit & 0x07F);  // protect against high bits set
    }

    public synchronized void setUnitSubAddress(int unitSub) {
        dataListeners.firePropertyChange("UnitSubAddress", Integer.valueOf(unitSubAddress), Integer.valueOf(unitSub & 0x07F)); // NOI18N
        unitSubAddress = unitSub & 0x07F;
    }

    public synchronized int getUnitAddress() {
        return unitAddress & 0x07F;
    }

    public synchronized int getUnitSubAddress() {
        return unitSubAddress & 0x07F;
    }

    /**
     * TODO: LocoIO Board level configuration.
     * <pre>
     * Bit 0: 0 = default, 1 = Port Refresh
     * Bit 1: 0 = Fixed code PBs, 1 = Alternated code PBs
     * Bit 2: 0 = default - Not used
     * Bit 3: 0 = default, 1 = Ports 5-12 are Servo Ports
     * Bit 4-7: Blink Rate
     *
     * If possibe add/support the additional config options for HDL boards
     * </pre>
     */
    public void setUnitConfig(int portRefresh, int altCodePBs, int isServo, int blinkRate) {
        int newsv0 = ((portRefresh & 0x01)) |   // bit 0
                ((altCodePBs & 0x01) << 0x01) | // bit 1
                // bit 2 is left at zero
                ((isServo & 0x01) << 0x03) |    // bit 3
                ((blinkRate & 0x0F) << 0x04);   // bits 4-7
        dataListeners.firePropertyChange("UnitConfig", Integer.valueOf(sv0), Integer.valueOf(newsv0)); // NOI18N
        sv0 = newsv0;
    }

    public int getUnitConfig() {
        return sv0 & 0xFF;
    }

    public void setLBVersion(String version) {
        locoBufferVersion = version;
        dataListeners.firePropertyChange("LBVersionChange", "", locoBufferVersion); // NOI18N
    }

    public String getLBVersion() {
        return locoBufferVersion;
    }

    public void setLIOVersion(String version) {
        locoIOVersion = version;
        dataListeners.firePropertyChange("LIOVersionChange", "", locoIOVersion); // NOI18N
    }

    public String getLIOVersion() {
        return locoBufferVersion;
    }

    public void setStatus(String msg) {
        status = msg;
        dataListeners.firePropertyChange("StatusChange", "", status); // NOI18N
    }

    public String getStatus() {
        return status;
    }

    public void setSV(int channel, int value) {
        sv[channel] = value & 0xFF;
        dataListeners.firePropertyChange("PortChange", Integer.valueOf(-1), Integer.valueOf(channel)); // NOI18N
    }

    public int getSV(int channel) {
        return sv[channel] & 0xFF;
    }

    public void setV1(int channel, LocoIOMode l, int address) {
        setV1(channel, validmodes.addressToValue1(l, getAddr(channel)));
    }

    public void setV1(int channel, int value) {
        v1[channel] = value & 0xFF;
        dataListeners.firePropertyChange("PortChange", Integer.valueOf(-1), Integer.valueOf(channel)); // NOI18N
    }

    public int getV1(int channel) {
        return v1[channel] & 0xFF;
    }

    public void setV2(int channel, LocoIOMode l, int address) {
        setV2(channel, validmodes.addressToValue2(l, getAddr(channel)));
    }

    public void setV2(int channel, int value) {
        v2[channel] = value & 0xFF;
        dataListeners.firePropertyChange("PortChange", Integer.valueOf(-1), Integer.valueOf(channel)); // NOI18N
    }

    public int getV2(int channel) {
        return v2[channel] & 0xFF;
    }

    /**
     * Set new value in addr field (for the address info used in each LocoIO channel).
     *
     * @param channel integer value of the addresses in use for this row
     *                (0 = invalid)
     */
    public void setAddr(int channel, int value) {
        addr[channel] = value & 0x7FF;
        dataListeners.firePropertyChange("PortChange", Integer.valueOf(-1), Integer.valueOf(channel)); // NOI18N
    }

    public int getAddr(int channel) {
        return addr[channel] & 0x7FF;
    }

    public void setMode(int channel, String m) {
        mode[channel] = m;
        dataListeners.firePropertyChange("PortChange", Integer.valueOf(-1), Integer.valueOf(channel)); // NOI18N
    }

    public String getMode(int channel) {
        return mode[channel];
    }

    public void setLIM(int channel, String s) {
        if (validmodes != null) {
            setLIM(channel, validmodes.getLocoIOModeFor(s));
        }
    }

    public void setLIM(int channel) {
        if (validmodes != null) {
            setLIM(channel, validmodes.getLocoIOModeFor(getSV(channel), getV1(channel), getV2(channel)));
        }
    }

    public void setLIM(int channel, LocoIOMode m) {
        lim[channel] = m;
        dataListeners.firePropertyChange("PortChange", Integer.valueOf(-1), Integer.valueOf(channel)); // NOI18N
    }

    public LocoIOMode getLIM(int channel) {
        return lim[channel];
    }

    public void readValues(int channel) {
        readState[channel] = READ;
        issueNextOperation();
    }

    public void captureValues(int channel) {
        capture[channel] = true;
    }

    public void writeValues(int channel) {
        writeState[channel] = WRITE;
        issueNextOperation();
    }

    /**
     * Start reading all rows back.
     */
    public void readAll() {
        for (int row = 0; row < _numRows; row++) {
            readState[row] = READ;
        }
        issueNextOperation();
    }

    /**
     * Start writing all rows out.
     */
    public void writeAll() {
        for (int row = 0; row < _numRows; row++) {
            writeState[row] = WRITE;
        }
        issueNextOperation();
    }

    public LocoIOModeList getLocoIOModeList() {
        return validmodes;
    }

    /**
     * Code for read activity needed.
     * See states NONE, READMODE, READINGMODE, READVALUE1,
     * READINGVALUE1, READVALUE2, READINGVALUE2
     */
    // int[] needRead = new int[_numRows];
    protected final int NONE = 0;
    protected final int READVALUE1 = 1;
    protected final int READINGVALUE1 = 2;
    protected final int READVALUE2 = 3;
    protected final int READINGVALUE2 = 4;
    protected final int READMODE = 5;
    protected final int READINGMODE = 6;

    protected final int READ = READVALUE1;  // starting state

    /**
     * Code for write activity needed. See states NONE, WRITEMODE, WRITINGMODE,
     * WRITEVALUE1, WRITINGVALUE1, WRITEVALUE2, WRITINGVALUE2
     */
    // int[] needWrite = new int[_numRows];
    protected final int WRITEVALUE1 = 11;
    protected final int WRITINGVALUE1 = 12;
    protected final int WRITEVALUE2 = 13;
    protected final int WRITINGVALUE2 = 14;
    protected final int WRITEMODE = 15;
    protected final int WRITINGMODE = 16;

    protected final int WRITE = WRITEVALUE1;  // starting state

    private int lastOpCv = -1;
    private boolean reading = false;  // false means write in progress
    //private boolean writing = false;

    protected int highPart(int value) { // generally value 1
        return value / 256;
    }

    protected int lowPart(int value) { // generally value 2
        return value - 256 * highPart(value);
    }

    private String dotme(int val) {
        int dit;
        int x = val;
        StringBuffer ret = new StringBuffer();
        if (val == 0) {
            return "0"; // NOI18N
        }
        while (x != 0) {
            dit = x % 10;
            ret.insert(0, Integer.toString(dit));
            x = x / 10;
            if (x != 0) {
                ret.insert(0, ".");
            }
        }
        return ret.toString();
    }

    /**
     * Listen to the LocoNet. We're listening for incoming OPC_PEER_XFR
     * messages, which might be part of a read or write sequence. We're also
     * _sometimes_ listening for commands as part of a "capture" operation.
     * <p>
     * The incoming LocoNet OPC_PEER_XFR messages don't retain any information
     * about the CV number or whether it was a read or write operation. We store
     * the data regardless of whether it was read or write, but we need to
     * remember the cv number in the lastOpCv member.
     *
     * @param m Incoming message
     */
    @Override
    public synchronized void message(LocoNetMessage m) {
        // sort out the opCode
        int opCode = m.getOpCode();
        switch (opCode) {
            case LnConstants.OPC_PEER_XFER:
                // could be read or write operation
                // check that src_low_address is our unit, and
                // dst is "our" LocoBufferAddress
                int src = m.getElement(2);
                int dst = m.getElement(3) + m.getElement(4) * 256;
                int[] packet = m.getPeerXfrData();

                if (src == lowPart(LocoBufferAddress)) {
                    String lbv = ((packet[2] != 0) ? dotme(packet[2]) : "1.0");
                    setLBVersion(lbv);
                }

                if (dst == LocoBufferAddress && src == lowPart(unitAddress) && (packet[4] == unitSubAddress)) {
                    // yes, we assume this is a reply to us
                    stopTimer();
                    replyReceived(); // advance state

                    String fw = ((packet[2] != 0) ? dotme(packet[2]) : "1.3.2"); // NOI18N
                    setLIOVersion(fw);
                    if (packet[0] == LocoIO.LOCOIO_SV_READ || reading) {  // read command
                        // get data and store
                        if (lastOpCv >= 0 && lastOpCv <= 50) {

                            // there are two formats of the return packet...
                            int data = (packet[2] != 0) ? packet[5] : packet[7];
                            int channel = (lastOpCv / 3) - 1;
                            if (channel < 0) {
                                log.warn("... channel is less than zero!!!"); // NOI18N
                                channel = 0;
                            }
                            int type = lastOpCv - (channel * 3 + 3);
                            // type = 0 for cv, 1 for value1, 2 for value2
                            // We can't update the mode until we have all three values
                            // Sequence (from state machine below) is V2, V1, Mode
                            log.debug("... updating port " + channel // NOI18N
                                    + " SV" + type // NOI18N
                                    + "(" // NOI18N
                                    + (type == 1 ? "value1" // NOI18N
                                            : type == 2 ? "value2" // NOI18N
                                                    : type == 0 ? "mode" // NOI18N
                                                            : "unknown") // NOI18N
                                    + ") = 0x" // NOI18N
                                    + Integer.toHexString(data));
                            if (type == 2) {            // v2
                                setV2(channel, data);
                                setMode(channel, "<none>"); // NOI18N
                            } else if (type == 1) {     // v1
                                setV1(channel, data);
                                setMode(channel, "<none>"); // NOI18N
                            } else if (type == 0) {       // cv
                                setSV(channel, data);
                                // Now that we have all the pieces, recalculate mode
                                LocoIOMode lim = validmodes.getLocoIOModeFor(getSV(channel), getV1(channel), getV2(channel));
                                if (lim == null) {
                                    setMode(channel, "<none>"); // NOI18N
                                    setAddr(channel, 0);
                                    log.debug("Could not find mode!"); // NOI18N
                                } else {
                                    setMode(channel, lim.getFullMode());
                                    setAddr(channel, validmodes.valuesToAddress(lim.getOpCode(), getSV(channel), getV1(channel), getV2(channel)));
                                }
                                log.debug("... decoded address (" // NOI18N
                                        + "cv=" + Integer.toHexString(getSV(channel)) + " " // NOI18N
                                        + "v1=" + Integer.toHexString(getV1(channel)) + " " // NOI18N
                                        + "v2=" + Integer.toHexString(getV2(channel)) + ") " // NOI18N
                                        + "is " + getAddr(channel) + "(0x" + Integer.toHexString(getAddr(channel)) + ")"); // NOI18N
                            } else {
                                log.warn("OPC_PEER_XFR: Type ({}) is not {0,1,2} for channel {}", type, channel); // NOI18N
                            }
                        } else {
                            // log.error("last CV recorded is invalid: "+lastOpCv);
                        }
                    }  // end of read processing

                    // check for anything else to do
                    issueNextOperation();
                    return;
                } else {
                    return;
                }
            case LnConstants.OPC_INPUT_REP: // Block Sensors and other general sensor codes
                log.debug("{} received", LnConstants.OPC_NAME(opCode)); // NOI18N
                // these might require capture
                for (int i = 0; i < _numRows; i++) {
                    if (capture[i]) {
                        log.debug("row set for capture: {}", i); // NOI18N
                        // This is a capture request, get address bytes
                        int val1 = m.getElement(1);
                        int val2 = m.getElement(2);
                        // calculate address from val's, save result, mark as done
                        // INPUT_REP's use val2's OPC_SW_REQ_DIR bit as LSB...'
                        setAddr(i, ((val2 & 0x0F) << 5) * 256 + ((val1 & 0x7f) << 1)
                                | (((val2 & LnConstants.OPC_SW_REQ_DIR) == LnConstants.OPC_SW_REQ_DIR) ? 0x01 : 0x00));
                        capture[i] = false;
                    }
                }
                return;

            case LnConstants.OPC_SW_REQ:    // Turnout SWITCH Request
                log.debug("{} received", LnConstants.OPC_NAME(opCode)); // NOI18N
                // these might require capture
                for (int i = 0; i < _numRows; i++) {
                    if (capture[i]) {
                        log.debug("row set for capture: {}", i); // NOI18N
                        // This is a capture request, get address bytes
                        int val1 = m.getElement(1);
                        int val2 = m.getElement(2);
                        // calculate address from val's, save result, mark as done
                        int addr = LocoIO.SENSOR_ADR(val1, val2);
                        setAddr(i, addr);
                        capture[i] = false;
                    }
                }
                return;
            default:    // we ignore all other LocoNet messages
            log.debug("{} received (ignored)", LnConstants.OPC_NAME(opCode));
        }
    }

    /**
     * A valid reply has been received, so the read/write worked, and the state
     * should be advanced.
     */
    protected synchronized void replyReceived() {
        timeoutcounter = 0;
        // READ operations state machine
        switch (readState[currentPin]) {
            case NONE:
                break;   // try the write operations
            case READVALUE1:
            case READINGVALUE1:
                readState[currentPin] = READVALUE2;
                return;
            case READVALUE2:
            case READINGVALUE2:
                readState[currentPin] = READMODE;
                return;
            case READMODE:
            case READINGMODE:
                readState[currentPin] = NONE;
                return;
            default:
                log.error("Pin {} unexpected read state, can't advance {}", currentPin, readState[currentPin]); // NOI18N
                readState[currentPin] = NONE;
                return;
        }
        // WRITE operations state machine
        switch (writeState[currentPin]) {
            case NONE:
                return;
            case WRITEVALUE1:
            case WRITINGVALUE1:
                writeState[currentPin] = WRITEVALUE2;
                return;
            case WRITEVALUE2:
            case WRITINGVALUE2:
                writeState[currentPin] = WRITEMODE;
                return;
            case WRITEMODE:
            case WRITINGMODE:
                writeState[currentPin] = NONE;
                return;
            default:
                log.error("Pin {} unexpected write state, can't advance {}", currentPin, writeState[currentPin]); // NOI18N
                writeState[currentPin] = NONE;
                return;
        }
    }

    private int currentPin = 0;

    /**
     * Look through the table to find the next thing that needs to be read.
     */
    protected synchronized void issueNextOperation() {
        // stop the timer while we figure this out
        stopTimer();
        // find the first item that needs to be read
        for (int i = 0; i < _numRows; i++) {
            currentPin = i;
            if (readState[i] != NONE) {
                // yes, needs read. Find what kind
                log.debug("iNO: readState[{}] = {}", i, readState[i]);
                switch (readState[i]) {
                    case READVALUE1:
                    case READINGVALUE1:
                        // set new state, send read, then done
                        readState[i] = READINGVALUE1;
                        lastOpCv = i * 3 + 4;
                        setStatus(Bundle.getMessage("StatusReading", lastOpCv, i + 1, 1)); // number port like table
                        sendReadCommand(unitAddress, unitSubAddress, lastOpCv);
                        return;
                    case READVALUE2:
                    case READINGVALUE2:
                        // set new state, send read, then done
                        readState[i] = READINGVALUE2;
                        lastOpCv = i * 3 + 5;
                        setStatus(Bundle.getMessage("StatusReading", lastOpCv, i + 1, 2));
                        sendReadCommand(unitAddress, unitSubAddress, lastOpCv);
                        return;
                    case READMODE:
                    case READINGMODE:
                        // set new state, send read, then done
                        readState[i] = READINGMODE;
                        lastOpCv = i * 3 + 3;
                        setStatus(Bundle.getMessage("StatusReadMode", lastOpCv, i + 1));
                        sendReadCommand(unitAddress, unitSubAddress, lastOpCv);
                        return;
                    default:
                        log.error("found an unexpected state: {} on port {}", readState[1], i + 1); // NOI18N
                        return;
                }
            }
        }
        // no reads, so continue to check writes
        for (int i = 0; i < _numRows; i++) {
            currentPin = i;
            if (writeState[i] != NONE) {
                // yes, needs read.  Find what kind
                log.debug("iNO: writeState[{}] = {}", i, readState[i]);
                switch (writeState[i]) {
                    case WRITEVALUE1:
                    case WRITINGVALUE1:
                        // set new state, send read, then done
                        writeState[i] = WRITINGVALUE1;
                        lastOpCv = i * 3 + 4;
                        setStatus(Bundle.getMessage("StatusWriting", lastOpCv, i + 1, 1));
                        sendWriteCommand(unitAddress, unitSubAddress, lastOpCv, getV1(i));
                        return;
                    case WRITEVALUE2:
                    case WRITINGVALUE2:
                        // set new state, send read, then done
                        writeState[i] = WRITINGVALUE2;
                        lastOpCv = i * 3 + 5;
                        setStatus(Bundle.getMessage("StatusWriting", lastOpCv, i + 1, 2));
                        sendWriteCommand(unitAddress, unitSubAddress, lastOpCv, getV2(i));
                        return;
                    case WRITEMODE:
                    case WRITINGMODE:
                        // set new state, send write, then done
                        writeState[i] = WRITINGMODE;
                        lastOpCv = i * 3 + 3;
                        setStatus(Bundle.getMessage("StatusWriteMode", lastOpCv, i + 1));
                        sendWriteCommand(unitAddress, unitSubAddress, lastOpCv, getSV(i));
                        return;

                    default:
                        log.error("found an unexpected state: {} on port {}", writeState[1], i + 1); // NOI18N
                        return;
                }
            }
        }
        // nothing of interest found, so just end gracefully
        log.debug("No operation needed");
        setStatus(Bundle.getMessage("StatusOK"));
        lastOpCv = -1;
        currentPin = 0;
    }

    /**
     * Timer Management Protect against communication failures, addressing
     * mixups and the like.
     */
    static private int TIMEOUT = 2000;    // ms
    protected javax.swing.Timer timer = null;
    private int timeoutcounter;

    /**
     * Internal routine to handle a timeout during read/write by retrying the
     * same operation.
     */
    synchronized protected void timeout() {
        log.debug("timeout!"); // NOI18N
        setStatus(Bundle.getMessage("Timeout"));
        if (timeoutcounter++ == 5) {
            for (int i = 0; i < _numRows; i++) {
                readState[i] = NONE;
                writeState[i] = NONE;
            }
            setStatus(Bundle.getMessage("StateAborted"));
            setLIOVersion(Bundle.getMessage("StateUnknown")); // NOI18N
            timeoutcounter = 0;
            stopTimer();
        } else {
            issueNextOperation();
        }
    }

    /**
     * Internal routine to start timer to protect the mode-change.
     */
    protected void startTimer() {
        restartTimer(TIMEOUT);
    }

    /**
     * Internal routine to stop timer, as all is well.
     */
    protected void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Internal routine to handle timer starts {@literal &} restarts.
     */
    protected void restartTimer(int delay) {
        if (timer == null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                @Override
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

    /**
     * Read an SV from a given LocoIO device.
     *
     */
    void sendReadCommand(int locoIOAddress, int locoIOSubAddress, int cv) {
        // remember current op is read
        reading = true;
        log.debug("sendReadCommand(to {}/{} SV {}",
                Integer.toHexString(locoIOAddress), Integer.toHexString(locoIOSubAddress), cv);
        tc.sendLocoNetMessage(
                LocoIO.readCV(locoIOAddress, locoIOSubAddress, cv));
        startTimer();        // and set timeout on reply
    }

    /**
     * Write an SV to a given LocoIO device.
     *
     */
    void sendWriteCommand(int locoIOAddress, int locoIOSubAddress, int cv, int data) {
        // remember current op is write
        reading = false;

        tc.sendLocoNetMessage(
                LocoIO.writeCV(locoIOAddress, locoIOSubAddress, cv, data));
        startTimer();        // and set timeout on reply
    }

    public void dispose() {
        log.debug("dispose"); // NOI18N
        // disconnect from future events
        stopTimer();
        tc.removeLocoNetListener(~0, this);

        // null references, so that they can be gc'd even if this isn't.
        addr = null;
        mode = null;
        sv = null;
        v1 = null;
        v2 = null;
        lim = null;
    }

    private final static Logger log = LoggerFactory.getLogger(LocoIOData.class);

}
