// DCCppSimulatorAdapter.java
package jmri.jmrix.dccpp.dccppsimulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppConstants;
import jmri.jmrix.dccpp.DCCppInitializationManager;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppPacketizer;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppSimulatorPortController;
import jmri.jmrix.dccpp.DCCppTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to a simulated DCC++ system.
 *
 * Currently, the DCCppSimulator reacts to commands sent from the user interface
 * with messages an appropriate reply message.
 *
 **NOTE: Most DCC++ commands are still unsupported in this implementation.
 *
 * Normally controlled by the dccpp.DCCppSimulator.DCCppSimulatorFrame class.
 *
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author	Paul Bender, Copyright (C) 2009-2010
 * @author	Mark Underwood, Copyright (C) 2015
 * @version	$Revision$
 *
 * Based on jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter
 */
public class DCCppSimulatorAdapter extends DCCppSimulatorPortController implements Runnable {

    private boolean OutputBufferEmpty = true;
    private boolean CheckBuffer = true;

    private int csStatus;
    // status flags from the XPressNet Documentation.
    private final static int csEmergencyStop = 0x01; // bit 0
    private final static int csTrackVoltageOff = 0x02; // bit 1
    private final static int csAutomaticMode = 0x04; // bit 2 
    private final static int csServiceMode = 0x08; // bit 3
    // bit 4 is reserved
    // bit 5 is reserved
    private final static int csPowerUpMode = 0x40; // bit 6
    private final static int csRamCheckError = 0x80; // bit 7
    
    // 0x00 means normal mode.
    private final static int csNormalMode = 0x00;

    public DCCppSimulatorAdapter() {
        setPort("None");
        try {
            PipedOutputStream tempPipeI = new PipedOutputStream();
            pout = new DataOutputStream(tempPipeI);
            inpipe = new DataInputStream(new PipedInputStream(tempPipeI));
            PipedOutputStream tempPipeO = new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        } catch (java.io.IOException e) {
            log.error("init (pipe): Exception: " + e.toString());
            return;
        }
        csStatus = csNormalMode;
    }

    public String openPort(String portName, String appName) {
        // open the port in XPressNet mode, check ability to set moderators
        setPort(portName);
        return null; // normal operation
    }

    /**
     * we need a way to say if the output buffer is empty or full this should
     * only be set to false by external processes
     *
     */
    synchronized public void setOutputBufferEmpty(boolean s) {
        OutputBufferEmpty = s;
    }

    /**
     * Can the port accept additional characters? The state of CTS determines
     * this, as there seems to be no way to check the number of queued bytes and
     * buffer length. This might go false for short intervals, but it might also
     * stick off if something goes wrong.
     */
    public boolean okToSend() {
        if (CheckBuffer) {
            if (log.isDebugEnabled()) {
                log.debug("Buffer Empty: " + OutputBufferEmpty);
            }
            return (OutputBufferEmpty);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No Flow Control or Buffer Check");
            }
            return (true);
        }
    }

    /**
     * set up all of the other objects to operate with a DCCppSimulator connected
     * to this port
     */
    public void configure() {
        // connect to a packetizing traffic controller
        DCCppTrafficController packets = new DCCppPacketizer(new DCCppCommandStation());
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setDCCppTrafficController(packets);

        sourceThread = new Thread(this);
        sourceThread.start();

        new DCCppInitializationManager(this.getSystemConnectionMemo());

        jmri.jmrix.dccpp.ActiveFlag.setActive();
    }

    // base class methods for the DCCppSimulatorPortController interface
    public DataInputStream getInputStream() {
        if (pin == null) {
            log.error("getInputStream called before load(), stream not available");
            ConnectionStatus.instance().setConnectionState(this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
        }
        return pin;
    }

    public DataOutputStream getOutputStream() {
        if (pout == null) {
            log.error("getOutputStream called before load(), stream not available");
            ConnectionStatus.instance().setConnectionState(this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
        }
        return pout;
    }

    public boolean status() {
        return (pout != null && pin != null);
    }

    /**
     * Get an array of valid baud rates. This is currently just a message saying
     * its fixed
     */
    public String[] validBaudRates() {
        return null;
    }

    @Deprecated
    static public DCCppSimulatorAdapter instance() {
        if (mInstance == null) {
            mInstance = new DCCppSimulatorAdapter();
        }
        return mInstance;
    }

    public void run() { // start a new thread
        // this thread has one task.  It repeatedly reads from the input pipe
        // and writes modified data to the output pipe.  This is the heart
        // of the command station simulation.
        if (log.isDebugEnabled()) {
            log.debug("Simulator Thread Started");
        }
        ConnectionStatus.instance().setConnectionState(this.getCurrentPortName(), ConnectionStatus.CONNECTION_UP);
        for (;;) {
            DCCppMessage m = readMessage();
            if (log.isDebugEnabled()) {
                log.debug("Simulator Thread received message " + m.toString());
            }
            DCCppReply r = generateReply(m);
            writeReply(r);
            if (log.isDebugEnabled()) {
                log.debug("Simulator Thread sent Reply" + r.toString());
            }
        }
    }

    // readMessage reads one incoming message from the buffer
    // and sets outputBufferEmpty to true.
    private DCCppMessage readMessage() {
        DCCppMessage msg = null;
        try {
            msg = loadChars();
        } catch (java.io.IOException e) {
            // should do something meaningful here.
            ConnectionStatus.instance().setConnectionState(this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);

        }
        setOutputBufferEmpty(true);
        return (msg);
    }

    // generateReply is the heart of the simulation.  It translates an 
    // incoming DCCppMessage into an outgoing DCCppReply.
    @SuppressWarnings("fallthrough")
    private DCCppReply generateReply(DCCppMessage m) {
        DCCppReply reply = new DCCppReply();
        switch (m.getElement(0)) {

            case DCCppConstants.CS_REQUEST:
                switch (m.getElement(1)) {
                    case DCCppConstants.CS_VERSION:
                        reply = xNetVersionReply();
                        break;
                    case DCCppConstants.RESUME_OPS:
                        csStatus=csNormalMode;
                        reply = normalOpsReply();
                        break;
                    case DCCppConstants.EMERGENCY_OFF:
                        csStatus=csEmergencyStop;
                        reply = everythingOffReply();
                        break;
                    case DCCppConstants.CS_STATUS:
                        reply=csStatusReply();
                        break;
                    case DCCppConstants.SERVICE_MODE_CSRESULT:
                    default:
                        reply = notSupportedReply();
                }
                break;
            case DCCppConstants.LI_VERSION_REQUEST:
                reply.setOpCode(DCCppConstants.LI_VERSION_RESPONSE);
                reply.setElement(1, 0x00);  // set the hardware type to 0
                reply.setElement(2, 0x00);  // set the firmware version to 0
                reply.setElement(3, 0x00);  // set the parity byte to 0
                break;
            case DCCppConstants.LOCO_OPER_REQ:
                switch(m.getElement(1)) {
                     case DCCppConstants.LOCO_SPEED_14:
                     case DCCppConstants.LOCO_SPEED_27:
                     case DCCppConstants.LOCO_SPEED_28:
                     case DCCppConstants.LOCO_SPEED_128:
                          reply = okReply();
                          break;
                     case DCCppConstants.LOCO_SET_FUNC_GROUP1:
                     case DCCppConstants.LOCO_SET_FUNC_GROUP2:
                     case DCCppConstants.LOCO_SET_FUNC_GROUP3:
                     case DCCppConstants.LOCO_SET_FUNC_GROUP4:
                     case DCCppConstants.LOCO_SET_FUNC_GROUP5:
                          reply = okReply();
                          break;
                     case DCCppConstants.LOCO_SET_FUNC_Group1:
                     case DCCppConstants.LOCO_SET_FUNC_Group2:
                     case DCCppConstants.LOCO_SET_FUNC_Group3:
                     case DCCppConstants.LOCO_SET_FUNC_Group4:
                     case DCCppConstants.LOCO_SET_FUNC_Group5:
                          reply = okReply();
                          break;
                     case DCCppConstants.LOCO_ADD_MULTI_UNIT_REQ:
                     case DCCppConstants.LOCO_REM_MULTI_UNIT_REQ:
                     case DCCppConstants.LOCO_IN_MULTI_UNIT_REQ_FORWARD:
                     case DCCppConstants.LOCO_IN_MULTI_UNIT_REQ_BACKWARD:
                     default:
                        reply = notSupportedReply();
                        break;
                }
                break;
            case DCCppConstants.EMERGENCY_STOP:
                reply = emergencyStopReply();
                break;
            case DCCppConstants.ACC_OPER_REQ:
                reply = okReply();
                break;
            case DCCppConstants.LOCO_STATUS_REQ:
                switch (m.getElement(1)) {
                    case DCCppConstants.LOCO_INFO_REQ_V3:
                        reply.setOpCode(DCCppConstants.LOCO_INFO_NORMAL_UNIT);
                        reply.setElement(1, 0x04);  // set to 128 speed step mode
                        reply.setElement(2, 0x00);  // set the speed to 0 
                        // direction reverse
                        reply.setElement(3, 0x00);  // set function group a off
                        reply.setElement(4, 0x00);  // set function group b off
                        reply.setElement(5, 0x00);  // set the parity byte to 0
                        break;
                    case DCCppConstants.LOCO_INFO_REQ_FUNC:
                        reply.setOpCode(DCCppConstants.LOCO_INFO_RESPONSE);
                        reply.setElement(1, DCCppConstants.LOCO_FUNCTION_STATUS);  // momentary function status
                        reply.setElement(2, 0x00);  // set function group a continuous
                        reply.setElement(3, 0x00);  // set function group b continuous
                        reply.setElement(4, 0x00);  // set the parity byte to 0
                        break;
                    case DCCppConstants.LOCO_INFO_REQ_FUNC_HI_ON:
                        reply.setOpCode(DCCppConstants.LOCO_INFO_RESPONSE);
                        reply.setElement(1, DCCppConstants.LOCO_FUNCTION_STATUS_HIGH);  // F13-F28 function on/off status
                        reply.setElement(2, 0x00);  // set function group a continuous
                        reply.setElement(3, 0x00);  // set function group b continuous
                        reply.setElement(4, 0x00);  // set the parity byte to 0
                        break;
                    case DCCppConstants.LOCO_INFO_REQ_FUNC_HI_MOM:
                        reply.setOpCode(DCCppConstants.LOCO_INFO_NORMAL_UNIT);
                        reply.setElement(1, DCCppConstants.LOCO_FUNCTION_STATUS_HIGH_MOM);  // F13-F28 momentary function status
                        reply.setElement(2, 0x00);  // set function group a continuous
                        reply.setElement(3, 0x00);  // set function group b continuous
                        reply.setElement(4, 0x00);  // set the parity byte to 0
                        break;
                    default:
                        reply = notSupportedReply();
                }
                break;
            case DCCppConstants.ACC_INFO_REQ:
                reply.setOpCode(DCCppConstants.ACC_INFO_RESPONSE);
                reply.setElement(1, m.getElement(1));
                if (m.getElement(1) < 64) {
                    // treat as turnout feedback request.
                    if (m.getElement(2) == 0x80) {
                        reply.setElement(2, 0x00);
                    } else {
                        reply.setElement(2, 0x10);
                    }
                } else {
                    // treat as feedback encoder request.
                    if (m.getElement(2) == 0x80) {
                        reply.setElement(2, 0x40);
                    } else {
                        reply.setElement(2, 0x50);
                    }
                }
                reply.setElement(3, 0x00);
                break;
            case DCCppConstants.LI101_REQUEST:
            case DCCppConstants.CS_SET_POWERMODE:
            //case DCCppConstants.PROG_READ_REQUEST:  //PROG_READ_REQUEST 
            //and CS_SET_POWERMODE 
            //have the same value
            case DCCppConstants.PROG_WRITE_REQUEST:
            case DCCppConstants.OPS_MODE_PROG_REQ:
            case DCCppConstants.LOCO_DOUBLEHEAD:
            default:
                reply = notSupportedReply();
        }
        return (reply);
    }

    // We have a few canned response messages.
    // Create an Unsupported DCCppReply message
    private DCCppReply notSupportedReply() {
        DCCppReply r = new DCCppReply();
        r.setOpCode(DCCppConstants.CS_INFO);
        r.setElement(1, DCCppConstants.CS_NOT_SUPPORTED);
        r.setElement(2, 0x00); // set the parity byte to 0
        return r;
    }

    // Create an OK DCCppReply message
    private DCCppReply okReply() {
        DCCppReply r = new DCCppReply();
        r.setOpCode(DCCppConstants.LI_MESSAGE_RESPONSE_HEADER);
        r.setElement(1, DCCppConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS);
        r.setElement(2, 0x00); // set the parity byte to 0
        return r;
    }

    // Create a "Normal Operations Resumed" message
    private DCCppReply normalOpsReply() {
        DCCppReply r = new DCCppReply();
        r.setOpCode(DCCppConstants.CS_INFO);
        r.setElement(1, DCCppConstants.BC_NORMAL_OPERATIONS);
        r.setElement(2, 0x00); // set the parity byte to 0
        return r;
    }

    // Create a broadcast "Everything Off" reply
    private DCCppReply everythingOffReply() {
        DCCppReply r = new DCCppReply();
        r.setOpCode(DCCppConstants.CS_INFO);
        r.setElement(1, DCCppConstants.BC_EVERYTHING_OFF);
        r.setElement(2, 0x00); // set the parity byte to 0
        return r;
    }

    // Create a broadcast "Emergecy Stop" reply
    private DCCppReply emergencyStopReply() {
        DCCppReply r = new DCCppReply();
        r.setOpCode(DCCppConstants.BC_EMERGENCY_STOP);
        r.setElement(1, DCCppConstants.BC_EVERYTHING_OFF);
        r.setElement(2, 0x00); // set the parity byte to 0
        return r;
    }

    // Create a reply to a request for the XPressNet Version
    private DCCppReply xNetVersionReply(){
        DCCppReply reply=new DCCppReply();
        reply.setOpCode(DCCppConstants.CS_SERVICE_MODE_RESPONSE);
        reply.setElement(1, DCCppConstants.CS_SOFTWARE_VERSION);
        reply.setElement(2, 0x36 & 0xff); // indicate we are version 3.6
        reply.setElement(3, 0x00 & 0xff); // indicate we are an LZ100;
        reply.setElement(4, 0x00); // set the parity byte to 0
        return reply;
    }

    // Create a reply to a request for the Command Station Status
    private DCCppReply csStatusReply(){
        DCCppReply reply=new DCCppReply();
        reply.setOpCode(DCCppConstants.CS_REQUEST_RESPONSE);
        reply.setElement(1, DCCppConstants.CS_STATUS_RESPONSE);
        reply.setElement(2, csStatus);
        reply.setElement(3, 0x00); // set the parity byte to 0
        return reply;
    }

    private void writeReply(DCCppReply r) {
        int i;
        int len = (r.getElement(0) & 0x0f) + 2;  // opCode+Nbytes+ECC
        for (i = 0; i < len; i++) {
            try {
                outpipe.writeByte((byte) r.getElement(i));
            } catch (java.io.IOException ex) {
                ConnectionStatus.instance().setConnectionState(this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
            }
        }
    }

    /**
     * Get characters from the input source, and file a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread.
     *
     * @returns filled message
     * @throws IOException when presented by the input source.
     */
    private DCCppMessage loadChars() throws java.io.IOException {
        int i;
        byte char1;
	// TODO: Make sure this handles the brackets correctly.
        char1 = readByteProtected(inpipe);
        int len = (char1 & 0x0f);  // opCode+Nbytes
        DCCppMessage msg = new DCCppMessage(len);
        msg.setElement(0, char1 & 0xFF);
        for (i = 1; i < len; i++) {
            char1 = readByteProtected(inpipe);
            msg.setElement(i, char1 & 0xFF);
        }
        return msg;
    }

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <P>
     * When a gnu.io port is set to have a receive timeout (via the
     * enableReceiveTimeout() method), some will return zero bytes or an
     * EOFException at the end of the timeout. In that case, the read should be
     * repeated to get the next real character.
     *
     */
    protected byte readByteProtected(DataInputStream istream) throws java.io.IOException {
        byte[] rcvBuffer = new byte[1];
        while (true) { // loop will repeat until character found
            int nchars;
            nchars = istream.read(rcvBuffer, 0, 1);
            if (nchars > 0) {
                return rcvBuffer[0];
            }
        }
    }

    volatile static DCCppSimulatorAdapter mInstance = null;
    private DataOutputStream pout = null; // for output to other classes
    private DataInputStream pin = null; // for input from other classes    
    // internal ends of the pipes
    private DataOutputStream outpipe = null;  // feed pin
    private DataInputStream inpipe = null; // feed pout
    private Thread sourceThread;

    static Logger log = LoggerFactory.getLogger(DCCppSimulatorAdapter.class.getName());

}
