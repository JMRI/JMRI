package jmri.jmrix.lenz.xnetsimulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetInitializationManager;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetPacketizer;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetSimulatorPortController;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to a simulated XPressNet system.
 *
 * Currently, the XNetSimulator reacts to commands sent from the user interface
 * with messages an appropriate reply message.
 *
 **NOTE: Most XPressNet commands are still unsupported in this implementation.
 *
 * Normally controlled by the lenz.XNetSimulator.XNetSimulatorFrame class.
 *
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author	Paul Bender, Copyright (C) 2009-2010
 */
public class XNetSimulatorAdapter extends XNetSimulatorPortController implements Runnable {

    private boolean OutputBufferEmpty = true;
    private boolean CheckBuffer = true;

    private int csStatus;
    // status flags from the XPressNet Documentation.
    private final static int csEmergencyStop = 0x01; // bit 0
    // 0x00 means normal mode.
    private final static int csNormalMode = 0x00;

    // information about the last throttle command(s).
    private int lastLocoAddressHigh = 0;
    private int lastLocoAddressLow = 0;
    private int CurrentSpeedStepMode = XNetConstants.LOCO_SPEED_128;
    private int CurrentSpeedStep= 0; 
    private int FunctionGroup1 = 0;
    private int FunctionGroup2 = 0;
    private int FunctionGroup3 = 0;
    private int FunctionGroup4 = 0;
    private int FunctionGroup5 = 0;
    private int MomentaryGroup1 = 0;
    private int MomentaryGroup2 = 0;
    private int MomentaryGroup3 = 0;
    private int MomentaryGroup4 = 0;
    private int MomentaryGroup5 = 0;

    public XNetSimulatorAdapter() {
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
     * set up all of the other objects to operate with a XNetSimulator connected
     * to this port
     */
    public void configure() {
        // connect to a packetizing traffic controller
        XNetTrafficController packets = new XNetPacketizer(new LenzCommandStation());
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setXNetTrafficController(packets);

        sourceThread = new Thread(this);
        sourceThread.start();

        new XNetInitializationManager(this.getSystemConnectionMemo());
    }

    // base class methods for the XNetSimulatorPortController interface
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
    static public XNetSimulatorAdapter instance() {
        if (mInstance == null) {
            mInstance = new XNetSimulatorAdapter();
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
            XNetMessage m = readMessage();
            if (log.isDebugEnabled()) {
                log.debug("Simulator Thread received message " + m.toString());
            }
            XNetReply r = generateReply(m);
            writeReply(r);
            if (log.isDebugEnabled()) {
                log.debug("Simulator Thread sent Reply" + r.toString());
            }
        }
    }

    // readMessage reads one incoming message from the buffer
    // and sets outputBufferEmpty to true.
    private XNetMessage readMessage() {
        XNetMessage msg = null;
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
    // incoming XNetMessage into an outgoing XNetReply.
    @SuppressWarnings("fallthrough")
    private XNetReply generateReply(XNetMessage m) {
        XNetReply reply = new XNetReply();
        switch (m.getElement(0)) {

            case XNetConstants.CS_REQUEST:
                switch (m.getElement(1)) {
                    case XNetConstants.CS_VERSION:
                        reply = xNetVersionReply();
                        break;
                    case XNetConstants.RESUME_OPS:
                        csStatus=csNormalMode;
                        reply = normalOpsReply();
                        break;
                    case XNetConstants.EMERGENCY_OFF:
                        csStatus=csEmergencyStop;
                        reply = everythingOffReply();
                        break;
                    case XNetConstants.CS_STATUS:
                        reply=csStatusReply();
                        break;
                    case XNetConstants.SERVICE_MODE_CSRESULT:
                    default:
                        reply = notSupportedReply();
                }
                break;
            case XNetConstants.LI_VERSION_REQUEST:
                reply.setOpCode(XNetConstants.LI_VERSION_RESPONSE);
                reply.setElement(1, 0x00);  // set the hardware type to 0
                reply.setElement(2, 0x00);  // set the firmware version to 0
                reply.setElement(3, 0x00);  // set the parity byte to 0
                reply.setParity();
                break;
            case XNetConstants.LOCO_OPER_REQ:
                lastLocoAddressHigh = m.getElement(2);
                lastLocoAddressLow = m.getElement(3);
                switch(m.getElement(1)) {
                     case XNetConstants.LOCO_SPEED_14:
                          CurrentSpeedStepMode = XNetConstants.LOCO_SPEED_14;
                          CurrentSpeedStep = m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SPEED_27:
                          CurrentSpeedStepMode = XNetConstants.LOCO_SPEED_27;
                          CurrentSpeedStep = m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SPEED_28:
                          CurrentSpeedStepMode = XNetConstants.LOCO_SPEED_28;
                          CurrentSpeedStep = m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SPEED_128:
                          CurrentSpeedStepMode = XNetConstants.LOCO_SPEED_128;
                          CurrentSpeedStep = m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SET_FUNC_GROUP1:
                          FunctionGroup1=m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SET_FUNC_GROUP2:
                          FunctionGroup2=m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SET_FUNC_GROUP3:
                          FunctionGroup3=m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SET_FUNC_GROUP4:
                          FunctionGroup4=m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SET_FUNC_GROUP5:
                          FunctionGroup5=m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SET_FUNC_Group1:
                          MomentaryGroup1=m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SET_FUNC_Group2:
                          MomentaryGroup2=m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SET_FUNC_Group3:
                          MomentaryGroup3=m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SET_FUNC_Group4:
                          MomentaryGroup4=m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SET_FUNC_Group5:
                          MomentaryGroup5=m.getElement(4);
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_ADD_MULTI_UNIT_REQ:
                     case XNetConstants.LOCO_REM_MULTI_UNIT_REQ:
                     case XNetConstants.LOCO_IN_MULTI_UNIT_REQ_FORWARD:
                     case XNetConstants.LOCO_IN_MULTI_UNIT_REQ_BACKWARD:
                     default:
                        reply = notSupportedReply();
                        break;
                }
                break;
            case XNetConstants.EMERGENCY_STOP:
                reply = emergencyStopReply();
                break;
            case XNetConstants.ACC_OPER_REQ:
                reply = okReply();
                break;
            case XNetConstants.LOCO_STATUS_REQ:
                switch (m.getElement(1)) {
                    case XNetConstants.LOCO_INFO_REQ_V3:
                        reply.setOpCode(XNetConstants.LOCO_INFO_NORMAL_UNIT);
                        reply.setElement(1, CurrentSpeedStepMode);  
                        reply.setElement(2, CurrentSpeedStep);  // set the speed 
                        // direction reverse
                        reply.setElement(3, FunctionGroup1);  // set function group 1
                        reply.setElement(4, (FunctionGroup2& 0x0f) + ((FunctionGroup3 &0x0f)<<4) );  // set function group 2 and 3
                        reply.setElement(5, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                    case XNetConstants.LOCO_INFO_REQ_FUNC:
                        reply.setOpCode(XNetConstants.LOCO_INFO_RESPONSE);
                        reply.setElement(1, XNetConstants.LOCO_FUNCTION_STATUS);  // momentary function status
                        reply.setElement(2, MomentaryGroup1);  // set function group 1
                        reply.setElement(3, (MomentaryGroup2&0x0f) + ((MomentaryGroup3*0x0f)<<4) );  // set function group 2 and 3
                        reply.setElement(4, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                    case XNetConstants.LOCO_INFO_REQ_FUNC_HI_ON:
                        reply.setOpCode(XNetConstants.LOCO_INFO_RESPONSE);
                        reply.setElement(1, XNetConstants.LOCO_FUNCTION_STATUS_HIGH);  // F13-F28 function on/off status
                        reply.setElement(2, FunctionGroup4);  // set function group 4
                        reply.setElement(3, FunctionGroup5);  // set function group 5
                        reply.setElement(4, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                    case XNetConstants.LOCO_INFO_REQ_FUNC_HI_MOM:
                        reply.setOpCode(XNetConstants.LOCO_INFO_NORMAL_UNIT);
                        reply.setElement(1, XNetConstants.LOCO_FUNCTION_STATUS_HIGH_MOM);  // F13-F28 momentary function status
                        reply.setElement(2, MomentaryGroup4);  // set function group 4
                        reply.setElement(3, MomentaryGroup5);  // set function group 5
                        reply.setElement(4, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                    default:
                        reply = notSupportedReply();
                }
                break;
            case XNetConstants.ACC_INFO_REQ:
                reply.setOpCode(XNetConstants.ACC_INFO_RESPONSE);
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
                reply.setParity();
                break;
            case XNetConstants.LI101_REQUEST:
            case XNetConstants.CS_SET_POWERMODE:
            //case XNetConstants.PROG_READ_REQUEST:  //PROG_READ_REQUEST 
            //and CS_SET_POWERMODE 
            //have the same value
            case XNetConstants.PROG_WRITE_REQUEST:
            case XNetConstants.OPS_MODE_PROG_REQ:
            case XNetConstants.LOCO_DOUBLEHEAD:
            default:
                reply = notSupportedReply();
        }
        return (reply);
    }

    // We have a few canned response messages.
    // Create an Unsupported XNetReply message
    private XNetReply notSupportedReply() {
        XNetReply r = new XNetReply();
        r.setOpCode(XNetConstants.CS_INFO);
        r.setElement(1, XNetConstants.CS_NOT_SUPPORTED);
        r.setElement(2, 0x00); // set the parity byte to 0
        r.setParity();
        return r;
    }

    // Create an OK XNetReply message
    private XNetReply okReply() {
        XNetReply r = new XNetReply();
        r.setOpCode(XNetConstants.LI_MESSAGE_RESPONSE_HEADER);
        r.setElement(1, XNetConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS);
        r.setElement(2, 0x00); // set the parity byte to 0
        r.setParity();
        return r;
    }

    // Create a "Normal Operations Resumed" message
    private XNetReply normalOpsReply() {
        XNetReply r = new XNetReply();
        r.setOpCode(XNetConstants.CS_INFO);
        r.setElement(1, XNetConstants.BC_NORMAL_OPERATIONS);
        r.setElement(2, 0x00); // set the parity byte to 0
        r.setParity();
        return r;
    }

    // Create a broadcast "Everything Off" reply
    private XNetReply everythingOffReply() {
        XNetReply r = new XNetReply();
        r.setOpCode(XNetConstants.CS_INFO);
        r.setElement(1, XNetConstants.BC_EVERYTHING_OFF);
        r.setElement(2, 0x00); // set the parity byte to 0
        r.setParity();
        return r;
    }

    // Create a broadcast "Emergecy Stop" reply
    private XNetReply emergencyStopReply() {
        XNetReply r = new XNetReply();
        r.setOpCode(XNetConstants.BC_EMERGENCY_STOP);
        r.setElement(1, XNetConstants.BC_EVERYTHING_OFF);
        r.setElement(2, 0x00); // set the parity byte to 0
        r.setParity();
        return r;
    }

    // Create a reply to a request for the XPressNet Version
    private XNetReply xNetVersionReply(){
        XNetReply reply=new XNetReply();
        reply.setOpCode(XNetConstants.CS_SERVICE_MODE_RESPONSE);
        reply.setElement(1, XNetConstants.CS_SOFTWARE_VERSION);
        reply.setElement(2, 0x36 & 0xff); // indicate we are version 3.6
        reply.setElement(3, 0x00 & 0xff); // indicate we are an LZ100;
        reply.setElement(4, 0x00); // set the parity byte to 0
        reply.setParity();
        return reply;
    }

    // Create a reply to a request for the Command Station Status
    private XNetReply csStatusReply(){
        XNetReply reply=new XNetReply();
        reply.setOpCode(XNetConstants.CS_REQUEST_RESPONSE);
        reply.setElement(1, XNetConstants.CS_STATUS_RESPONSE);
        reply.setElement(2, csStatus);
        reply.setElement(3, 0x00); // set the parity byte to 0
        reply.setParity();
        return reply;
    }

    private void writeReply(XNetReply r) {
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
    private XNetMessage loadChars() throws java.io.IOException {
        int i;
        byte char1;
        char1 = readByteProtected(inpipe);
        int len = (char1 & 0x0f) + 2;  // opCode+Nbytes+ECC
        XNetMessage msg = new XNetMessage(len);
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

    volatile static XNetSimulatorAdapter mInstance = null;
    private DataOutputStream pout = null; // for output to other classes
    private DataInputStream pin = null; // for input from other classes    
    // internal ends of the pipes
    private DataOutputStream outpipe = null;  // feed pin
    private DataInputStream inpipe = null; // feed pout
    private Thread sourceThread;

    private final static Logger log = LoggerFactory.getLogger(XNetSimulatorAdapter.class.getName());

}
