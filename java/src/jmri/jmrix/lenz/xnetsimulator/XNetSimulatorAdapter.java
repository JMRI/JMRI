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
 * Provide access to a simulated XpressNet system.
 * <p>
 * Currently, the XNetSimulator reacts to commands sent from the user interface
 * with messages an appropriate reply message.
 * <p>
 * NOTE: Most XpressNet commands are still unsupported in this implementation.
 * <p>
 * Normally controlled by the lenz.XNetSimulator.XNetSimulatorFrame class.
 * <p>
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 */
public class XNetSimulatorAdapter extends XNetSimulatorPortController implements Runnable {

    private boolean outputBufferEmpty = true;
    private boolean checkBuffer = true;

    private int csStatus;
    // status flags from the XpressNet Documentation.
    private final static int CS_EMERGENCY_STOP = 0x01; // bit 0
    // 0x00 means normal mode.
    private final static int CS_NORMAL_MODE = 0x00;

    // information about the last throttle command(s).
    private int currentSpeedStepMode = XNetConstants.LOCO_SPEED_128;
    private int currentSpeedStep = 0;
    private int functionGroup1 = 0;
    private int functionGroup2 = 0;
    private int functionGroup3 = 0;
    private int functionGroup4 = 0;
    private int functionGroup5 = 0;
    private int momentaryGroup1 = 0;
    private int momentaryGroup2 = 0;
    private int momentaryGroup3 = 0;
    private int momentaryGroup4 = 0;
    private int momentaryGroup5 = 0;

    public XNetSimulatorAdapter() {
        setPort(Bundle.getMessage("None"));
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
        csStatus = CS_NORMAL_MODE;
    }

    @Override
    public String openPort(String portName, String appName) {
        // open the port in XpressNet mode, check ability to set moderators
        setPort(portName);
        return null; // normal operation
    }

    /**
     * Tell if the output buffer is empty or full. This should only be set to
     * false by external processes.
     *
     * @param s true if the buffer is empty; false otherwise
     */
    @Override
    synchronized public void setOutputBufferEmpty(boolean s) {
        outputBufferEmpty = s;
    }

    /**
     * Can the port accept additional characters? The state of CTS determines
     * this, as there seems to be no way to check the number of queued bytes and
     * buffer length. This might go false for short intervals, but it might also
     * stick off if something goes wrong.
     */
    @Override
    public boolean okToSend() {
        if (checkBuffer) {
            log.debug("Buffer Empty: {}", outputBufferEmpty);
            return (outputBufferEmpty && super.okToSend());
        } else {
            log.debug("No Flow Control or Buffer Check");
            return (super.okToSend());
        }
    }

    /**
     * Set up all of the other objects to operate with an XNetSimulator connected
     * to this port.
     */
    @Override
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

    // Base class methods for the XNetSimulatorPortController interface

    @Override
    public DataInputStream getInputStream() {
        if (pin == null) {
            log.error("getInputStream called before load(), stream not available");
            ConnectionStatus.instance().setConnectionState(
                    this.getSystemConnectionMemo().getUserName(),
                    this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
        }
        return pin;
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (pout == null) {
            log.error("getOutputStream called before load(), stream not available");
            ConnectionStatus.instance().setConnectionState(
                    this.getSystemConnectionMemo().getUserName(),
                    this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
        }
        return pout;
    }

    @Override
    public boolean status() {
        return (pout != null && pin != null);
    }

    @Deprecated
    static public XNetSimulatorAdapter instance() {
        if (mInstance == null) {
            mInstance = new XNetSimulatorAdapter();
        }
        return mInstance;
    }

    @Override
    public void run() { // start a new thread
        // this thread has one task.  It repeatedly reads from the input pipe
        // and writes modified data to the output pipe.  This is the heart
        // of the command station simulation.
        log.debug("Simulator Thread Started");
        ConnectionStatus.instance().setConnectionState(
                this.getSystemConnectionMemo().getUserName(),
                this.getCurrentPortName(), ConnectionStatus.CONNECTION_UP);
        for (;;) {
            XNetMessage m = readMessage();
            log.debug("Simulator Thread received message {}", m);
            XNetReply r = generateReply(m);
            writeReply(r);
            log.debug("Simulator Thread sent Reply {}", r);
        }
    }

    // Read one incoming message from the buffer
    // and set outputBufferEmpty to true.
    private XNetMessage readMessage() {
        XNetMessage msg = null;
        try {
            msg = loadChars();
        } catch (java.io.IOException e) {
            // should do something meaningful here.
            ConnectionStatus.instance().setConnectionState(
                    this.getSystemConnectionMemo().getUserName(),
                    this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);

        }
        setOutputBufferEmpty(true);
        return (msg);
    }

    // This is the heart of the simulation. It translates an
    // incoming XNetMessage into an outgoing XNetReply.
    private XNetReply generateReply(XNetMessage m) {
        XNetReply reply = new XNetReply();
        switch (m.getElement(0) & 0xff) {

            case XNetConstants.CS_REQUEST:
                switch (m.getElement(1) & 0xff ) {
                    case XNetConstants.CS_VERSION:
                        reply = xNetVersionReply();
                        break;
                    case XNetConstants.RESUME_OPS:
                        csStatus = CS_NORMAL_MODE;
                        reply = normalOpsReply();
                        break;
                    case XNetConstants.EMERGENCY_OFF:
                        csStatus = CS_EMERGENCY_STOP;
                        reply = everythingOffReply();
                        break;
                    case XNetConstants.CS_STATUS:
                        reply = csStatusReply();
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
                switch (m.getElement(1) & 0xff ) {
                    case XNetConstants.LOCO_SPEED_14:
                        currentSpeedStepMode = XNetConstants.LOCO_SPEED_14;
                        currentSpeedStep = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SPEED_27:
                        currentSpeedStepMode = XNetConstants.LOCO_SPEED_27;
                        currentSpeedStep = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SPEED_28:
                        currentSpeedStepMode = XNetConstants.LOCO_SPEED_28;
                        currentSpeedStep = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SPEED_128:
                        currentSpeedStepMode = XNetConstants.LOCO_SPEED_128;
                        currentSpeedStep = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SET_FUNC_GROUP1:
                        functionGroup1 = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SET_FUNC_GROUP2:
                        functionGroup2 = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SET_FUNC_GROUP3:
                        functionGroup3 = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SET_FUNC_GROUP4:
                        functionGroup4 = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SET_FUNC_GROUP5:
                        functionGroup5 = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SET_FUNC_Group1:
                        momentaryGroup1 = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SET_FUNC_Group2:
                        momentaryGroup2 = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SET_FUNC_Group3:
                        momentaryGroup3 = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SET_FUNC_Group4:
                        momentaryGroup4 = m.getElement(4);
                        reply = okReply();
                        break;
                    case XNetConstants.LOCO_SET_FUNC_Group5:
                        momentaryGroup5 = m.getElement(4);
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
            case XNetConstants.ALL_ESTOP:    // ALL_ESTOP is XNet V4
                csStatus = CS_EMERGENCY_STOP;
                reply = emergencyStopReply();
                break;
            case XNetConstants.EMERGENCY_STOP:
            case XNetConstants.EMERGENCY_STOP_XNETV1V2:
                reply = okReply();
                break;
            case XNetConstants.ACC_OPER_REQ:
                // LZ100 and LZV100 respond with an ACC_INFO_RESPONSE.
                // but XpressNet standard says to no response (which causes
                // the interface to send an OK reply).
            case XNetConstants.ACC_INFO_REQ:
                reply = accInfoReply(m);
                break;
            case XNetConstants.LOCO_STATUS_REQ:
                switch (m.getElement(1) & 0xff ) {
                    case XNetConstants.LOCO_INFO_REQ_V3:
                        reply.setOpCode(XNetConstants.LOCO_INFO_NORMAL_UNIT);
                        reply.setElement(1, currentSpeedStepMode);
                        reply.setElement(2, currentSpeedStep);  // set the speed
                        // direction reverse
                        reply.setElement(3, functionGroup1);  // set function group 1
                        reply.setElement(4, (functionGroup2 & 0x0f) + ((functionGroup3 & 0x0f) << 4));  // set function group 2 and 3
                        reply.setElement(5, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                    case XNetConstants.LOCO_INFO_REQ_FUNC:
                        reply.setOpCode(XNetConstants.LOCO_INFO_RESPONSE);
                        reply.setElement(1, XNetConstants.LOCO_FUNCTION_STATUS);  // momentary function status
                        reply.setElement(2, momentaryGroup1);  // set function group 1
                        reply.setElement(3, (momentaryGroup2 & 0x0f) + ((momentaryGroup3 * 0x0f) << 4));  // set function group 2 and 3
                        reply.setElement(4, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                    case XNetConstants.LOCO_INFO_REQ_FUNC_HI_ON:
                        reply.setOpCode(XNetConstants.LOCO_INFO_RESPONSE);
                        reply.setElement(1, XNetConstants.LOCO_FUNCTION_STATUS_HIGH);  // F13-F28 function on/off status
                        reply.setElement(2, functionGroup4);  // set function group 4
                        reply.setElement(3, functionGroup5);  // set function group 5
                        reply.setElement(4, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                    case XNetConstants.LOCO_INFO_REQ_FUNC_HI_MOM:
                        reply.setOpCode(XNetConstants.LOCO_INFO_NORMAL_UNIT);
                        reply.setElement(1, XNetConstants.LOCO_FUNCTION_STATUS_HIGH_MOM);  // F13-F28 momentary function status
                        reply.setElement(2, momentaryGroup4);  // set function group 4
                        reply.setElement(3, momentaryGroup5);  // set function group 5
                        reply.setElement(4, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                    default:
                        reply = notSupportedReply();
                }
                break;
            case XNetConstants.OPS_MODE_PROG_REQ:
                    int operation = m.getElement(4) & 0xFC;
                    switch(operation & 0xff ) {
                         case 0xEC:
                           log.debug("Write CV in Ops Mode Request Received");
                           reply = okReply();
                           break;
                         case 0xE4:
                           log.debug("Verify CV in Ops Mode Request Received");
                           reply = okReply();
                           break;
                         case 0xE8:
                           log.debug("Ops Mode Bit Request Received");
                           reply = okReply();
                           break;
                         default:
                           reply=notSupportedReply();
                    }
                break;
            case XNetConstants.LI101_REQUEST:
            case XNetConstants.CS_SET_POWERMODE:
            //case XNetConstants.PROG_READ_REQUEST:  //PROG_READ_REQUEST
            //and CS_SET_POWERMODE
            //have the same value
            case XNetConstants.PROG_WRITE_REQUEST:
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

    // Create a broadcast "Emergency Stop" reply
    private XNetReply emergencyStopReply() {
        XNetReply r = new XNetReply();
        r.setOpCode(XNetConstants.BC_EMERGENCY_STOP);
        r.setElement(1, XNetConstants.BC_EVERYTHING_STOP);
        r.setElement(2, 0x00); // set the parity byte to 0
        r.setParity();
        return r;
    }

    // Create a reply to a request for the XpressNet Version
    private XNetReply xNetVersionReply() {
        XNetReply reply = new XNetReply();
        reply.setOpCode(XNetConstants.CS_SERVICE_MODE_RESPONSE);
        reply.setElement(1, XNetConstants.CS_SOFTWARE_VERSION);
        reply.setElement(2, 0x36 & 0xff); // indicate we are version 3.6
        reply.setElement(3, 0x00 & 0xff); // indicate we are an LZ100;
        reply.setElement(4, 0x00); // set the parity byte to 0
        reply.setParity();
        return reply;
    }

    // Create a reply to a request for the Command Station Status
    private XNetReply csStatusReply() {
        XNetReply reply = new XNetReply();
        reply.setOpCode(XNetConstants.CS_REQUEST_RESPONSE);
        reply.setElement(1, XNetConstants.CS_STATUS_RESPONSE);
        reply.setElement(2, csStatus);
        reply.setElement(3, 0x00); // set the parity byte to 0
        reply.setParity();
        return reply;
    }

    // Create a reply to a request for the accessory device Status
    private XNetReply accInfoReply(XNetMessage m) {
        XNetReply reply = new XNetReply();
        reply.setOpCode(XNetConstants.ACC_INFO_RESPONSE);
        reply.setElement(1, m.getElement(1));
           if (m.getOpCode() == XNetConstants.ACC_OPER_REQ || 
               m.getElement(1) < 64) {
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
        return reply;
    }

    private void writeReply(XNetReply r) {
        int i;
        int len = (r.getElement(0) & 0x0f) + 2;  // opCode+Nbytes+ECC
        for (i = 0; i < len; i++) {
            try {
                outpipe.writeByte((byte) r.getElement(i));
            } catch (java.io.IOException ex) {
                ConnectionStatus.instance().setConnectionState(
                        this.getSystemConnectionMemo().getUserName(),
                        this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
            }
        }
    }

    /**
     * Get characters from the input source, and file a message.
     * <p>
     * Returns only when the message is complete.
     * <p>
     * Only used in the Receive thread.
     *
     * @return filled message
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
     * <p>
     * When a port is set to have a receive timeout (via the
     * enableReceiveTimeout() method), some will return zero bytes or an
     * EOFException at the end of the timeout. In that case, the read should be
     * repeated to get the next real character.
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

    private final static Logger log = LoggerFactory.getLogger(XNetSimulatorAdapter.class);

}
