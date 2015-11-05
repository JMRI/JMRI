// DCCppSimulatorAdapter.java
package jmri.jmrix.dccpp.simulator;

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

	log.debug("Generate Reply to message type {} string = {}", msg.getElement(0), msg.toString());

        switch (msg.getElement(0)) {

	case DCCppConstants.THROTTLE_CMD:
	    log.debug("THROTTLE_CMD detected");
	    s = msg.toString();
	    try {
		p = Pattern.compile(DCCppConstants.THROTTLE_CMD_REGEX);
		m = p.matcher(s);
		if (!m.matches()) {
		    log.error("Malformed Throttle Command: {}", s);
		    reply = null;
		    break;
		}
		r = "T " + m.group(1) + " " + m.group(3) + " " + m.group(4);
		reply = new DCCppReply(r);
		log.debug("Reply generated = {}", reply.toString());
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
	    log.debug("TURNOUT_CMD detected");
	    s = msg.toString();
	    try {
		p = Pattern.compile(DCCppConstants.TURNOUT_CMD_REGEX);
		m = p.matcher(s);
		if (!m.matches()) {
		    log.error("Malformed Turnout Command: {}", s);
		    reply = null;
		    break;
		}
		r = "H " + m.group(1) + " " + m.group(2);
		reply = new DCCppReply(r);
		log.debug("Reply generated = {}", reply.toString());
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
	    log.debug("PROG_WRITE_CV_BYTE detected");
	    s = msg.toString();
	    try {
		p = Pattern.compile(DCCppConstants.PROG_WRITE_BYTE_REGEX);
		m = p.matcher(s);
		if (!m.matches()) {
		    log.error("Malformed ProgWriteCVByte Command: {}", s);
		    reply = null;
		    break;
		}
		r = "r " + m.group(3) + " " + 
		    m.group(4) + " " +
		    m.group(2);
		CVs[Integer.parseInt(m.group(1))] = Integer.parseInt(m.group(2));
		reply = new DCCppReply(r);
		log.debug("Reply generated = {}", reply.toString());
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
	    log.debug("PROG_WRITE_CV_BIT detected");
	    s = msg.toString();
	    try {
		p = Pattern.compile(DCCppConstants.PROG_WRITE_BIT_REGEX);
		m = p.matcher(s);
		if (!m.matches()) {
		    log.error("Malformed ProgWriteCVBit Command: {}", s);
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
		log.debug("Reply generated = {}", reply.toString());
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
	    log.debug("PROG_READ_CV detected");
	    s = msg.toString();
	    try {
		p = Pattern.compile(DCCppConstants.PROG_READ_REGEX);
		m = p.matcher(s);
		if (!m.matches()) {
		    log.error("Malformed PROG_READ_CV Command: {}", s);
		    reply = null;
		    break;
		}
		// TODO: Work Magic Here to retrieve stored value.
		int cv = CVs[Integer.parseInt(m.group(1))];
		r = "r " + m.group(2) + " " + m.group(3) + " " + Integer.toString(cv);
		reply = new DCCppReply(r);
		log.debug("Reply generated = {}", reply.toString());
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
	    log.debug("TRACK_POWER_ON detected");
	    TrackPowerState = true;
	    reply = new DCCppReply("p1");
	    log.debug("Reply generated = {}", reply.toString());
	    break;

	case DCCppConstants.TRACK_POWER_OFF:
	    log.debug("TRACK_POWER_OFF detected");
	    TrackPowerState = false;
	    reply = new DCCppReply("p0");
	    log.debug("Reply generated = {}", reply.toString());
	    break;

	case DCCppConstants.READ_TRACK_CURRENT:
	    log.debug("READ_TRACK_CURRENT detected");
	    reply = new DCCppReply("a " + (TrackPowerState ? "512" : "0"));
	    log.debug("Reply generated = {}", reply.toString());
	    break;

	case DCCppConstants.READ_CS_STATUS:
	    log.debug("READ_CS_STATUS detected");
	    generateReadCSStatusReply(); // Handle this special.
	    break;

	case DCCppConstants.FUNCTION_CMD:
	case DCCppConstants.STATIONARY_DECODER_CMD:
	case DCCppConstants.OPS_WRITE_CV_BYTE:
	case DCCppConstants.OPS_WRITE_CV_BIT:
	case DCCppConstants.WRITE_DCC_PACKET_MAIN:
	case DCCppConstants.WRITE_DCC_PACKET_PROG:
	    log.debug("non-reply message detected");
	    // Send no reply.
	    reply = null;;

            default:
                reply = null;
        }
        return (reply);
    }

    private void generateReadCSStatusReply() {
	/*
	String s = new String("<p" + (TrackPowerState ? "1" : "0") + ">");
	DCCppReply r = new DCCppReply(s);
	writeReply(r);
	if (log.isDebugEnabled()) {
	    log.debug("Simulator Thread sent Reply" + r.toString());
	}
	*/

	DCCppReply r = new DCCppReply("<iDCC++ BASE STATION vUNO_1.0: BUILD 05 Nov 2015 00:09:57");
	writeReply(r);
	if (log.isDebugEnabled()) {
	    log.debug("Simulator Thread sent Reply" + r.toString());
	}

	// Generate the other messages too...
    }

    private void writeReply(DCCppReply r) {
        int i;
        int len = r.getLength();  // opCode+Nbytes+ECC
	// If r == null, there is no reply to be sent.
	try {
	    outpipe.writeByte((byte)'<');
	    for (i = 0; i < len; i++) {
		outpipe.writeByte((byte) r.getElement(i));
	    }
	    outpipe.writeByte((byte)'>');
	} catch (java.io.IOException ex) {
	    ConnectionStatus.instance().setConnectionState(this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
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
	// Spin waiting for start-of-frame '<' character (and toss it)
	String s = new String();
	byte char1;
	boolean found_start = false;

	while (!found_start) {
	    char1 = readByteProtected(inpipe);
	    if ((char1 & 0xFF) == '<') {
		found_start = true;
		log.debug("Found starting < ");
		break; // A bit redundant with setting the loop condition true (false)
	    } else {
		char1 = readByteProtected(inpipe);
	    }
	}
	// Now, suck in the rest of the message...
        for (int i = 0; i < DCCppConstants.MAX_MESSAGE_SIZE; i++) {
            char1 = readByteProtected(inpipe);
	    if (char1 == '>') {
		log.debug("msg found > ");
		// Don't store the >
		break;
	    } else {
		log.debug("msg read byte {}", char1);
		char c = (char) (char1 & 0x00FF);
		s += Character.toString(c);
	    }
	}
	// TODO: Still need to strip leading and trailing whitespace.
	log.debug("Complete message = {}", s.toString());
	return(new DCCppMessage(s));
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
