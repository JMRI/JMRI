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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
    private boolean TrackPowerState = false;
    // One extra array element so that i can index directly from the
    // CV value, ignoring CVs[0].
    private int[] CVs = new int[DCCppConstants.MAX_DIRECT_CV+1];

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
	// Zero out the CV table.
	for (int i = 0; i < DCCppConstants.MAX_DIRECT_CV+1; i++)
	    CVs[i] = 0;
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
	    // If generateReply() returns null, do nothing. No reply to send.
	    if (r != null) {
		writeReply(r);
		if (log.isDebugEnabled()) {
		    log.debug("Simulator Thread sent Reply" + r.toString());
		}
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
    private DCCppReply generateReply(DCCppMessage msg) {
	String s, r;
	Pattern p;
	Matcher m;
        DCCppReply reply = null;

        switch (msg.getElement(0)) {

	case DCCppConstants.THROTTLE_CMD:
	    s = msg.toString();
	    try {
		p = Pattern.compile("t\\s(\\d+)\\s(\\d+)\\s([1,0])");
		m = p.matcher(s);
		if (!m.matches()) {
		    log.error("Malformed Throttle Command: {}", s);
		    reply = null;
		    break;
		}
		r = "T " + m.group(1) + " " + m.group(2) + " " + m.group(3);
		reply = new DCCppReply(r);
	    } catch (PatternSyntaxException e) {
		log.error("Malformed pattern syntax! ");
		return(null);
	    } catch (IllegalStateException e) {
		log.error("Group called before match operation executed string= " + s);
		return(null);
	    } catch (IndexOutOfBoundsException e) {
		log.error("Index out of bounds string= " + s);
		return(null);
	    }
	    break;

	case DCCppConstants.TURNOUT_CMD:
	    s = msg.toString();
	    try {
		p = Pattern.compile("T\\s(\\d+)\\s([1,0])");
		m = p.matcher(s);
		if (!m.matches()) {
		    log.error("Malformed Turnout Command: {}", s);
		    reply = null;
		    break;
		}
		r = "H " + m.group(1) + " " + m.group(2);
		reply = new DCCppReply(r);
	    } catch (PatternSyntaxException e) {
		log.error("Malformed pattern syntax! ");
		return(null);
	    } catch (IllegalStateException e) {
		log.error("Group called before match operation executed string= " + s);
		return(null);
	    } catch (IndexOutOfBoundsException e) {
		log.error("Index out of bounds string= " + s);
		return(null);
	    }
	    break;

	case DCCppConstants.PROG_WRITE_CV_BYTE:
	    s = msg.toString();
	    try {
		p = Pattern.compile("W\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)");
		m = p.matcher(s);
		if (!m.matches()) {
		    log.error("Malformed Turnout Command: {}", s);
		    reply = null;
		    break;
		}
		r = "r " + m.group(3) + " " + 
		    m.group(4) + " " +
		    m.group(2);
		CVs[Integer.parseInt(m.group(1))] = Integer.parseInt(m.group(2));
		reply = new DCCppReply(r);
	    } catch (PatternSyntaxException e) {
		log.error("Malformed pattern syntax! ");
		return(null);
	    } catch (IllegalStateException e) {
		log.error("Group called before match operation executed string= " + s);
		return(null);
	    } catch (IndexOutOfBoundsException e) {
		log.error("Index out of bounds string= " + s);
		return(null);
	    }
	    break;

	case DCCppConstants.PROG_WRITE_CV_BIT:
	    s = msg.toString();
	    try {
		p = Pattern.compile("B\\s(\\d+)\\s([0-7])\\s([1,0])\\s(\\d+)\\s(\\d+)");
		m = p.matcher(s);
		if (!m.matches()) {
		    log.error("Malformed Turnout Command: {}", s);
		    reply = null;
		    break;
		}
		r = "r " + m.group(4) + " " + 
		    m.group(5) + " " +
		    m.group(3);
		int idx = Integer.parseInt(m.group(1));
		int bit = Integer.parseInt(m.group(2));
		int v = Integer.parseInt(m.group(3));
		if (v == 1)
		    CVs[idx] = CVs[idx] | (0x0001 << bit);
		else
		    CVs[idx] = CVs[idx] & ~(0x0001 << bit);
		reply = new DCCppReply(r);
	    } catch (PatternSyntaxException e) {
		log.error("Malformed pattern syntax! ");
		return(null);
	    } catch (IllegalStateException e) {
		log.error("Group called before match operation executed string= " + s);
		return(null);
	    } catch (IndexOutOfBoundsException e) {
		log.error("Index out of bounds string= " + s);
		return(null);
	    }
	    break;

	case DCCppConstants.PROG_READ_CV:
	    s = msg.toString();
	    try {
		p = Pattern.compile("R\\s(\\d+)\\s\\s(\\d+)\\s(\\d+)");
		m = p.matcher(s);
		if (!m.matches()) {
		    log.error("Malformed Turnout Command: {}", s);
		    reply = null;
		    break;
		}
		// TODO: Work Magic Here to retrieve stored value.
		int cv = CVs[Integer.parseInt(m.group(1))];
		r = "r " + m.group(2) + " " + m.group(3) + " " + Integer.toString(cv);
		reply = new DCCppReply(r);
	    } catch (PatternSyntaxException e) {
		log.error("Malformed pattern syntax! ");
		return(null);
	    } catch (IllegalStateException e) {
		log.error("Group called before match operation executed string= " + s);
		return(null);
	    } catch (IndexOutOfBoundsException e) {
		log.error("Index out of bounds string= " + s);
		return(null);
	    }
	    break;

	case DCCppConstants.TRACK_POWER_ON:
	    TrackPowerState = true;
	    reply = new DCCppReply("p1");
	    break;

	case DCCppConstants.TRACK_POWER_OFF:
	    TrackPowerState = false;
	    reply = new DCCppReply("p0");
	    break;

	case DCCppConstants.READ_TRACK_CURRENT:
	    reply = new DCCppReply("a " + (TrackPowerState ? "512" : "0"));
	    break;

	case DCCppConstants.READ_CS_STATUS:
	    generateReadCSStatusReply(); // Handle this special.
	    break;

	case DCCppConstants.FUNCTION_CMD:
	case DCCppConstants.STATIONARY_DECODER_CMD:
	case DCCppConstants.OPS_WRITE_CV_BYTE:
	case DCCppConstants.OPS_WRITE_CV_BIT:
	case DCCppConstants.WRITE_DCC_PACKET_MAIN:
	case DCCppConstants.WRITE_DCC_PACKET_PROG:
	    // Send no reply.
	    reply = null;;

            default:
                reply = null;
        }
        return (reply);
    }

    private void generateReadCSStatusReply() {
	// for now, do nothing. This is a biggie.
    }

    private void writeReply(DCCppReply r) {
        int i;
        int len = (r.getElement(0) & 0x0f) + 2;  // opCode+Nbytes+ECC
	// If r == null, there is no reply to be sent.
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
