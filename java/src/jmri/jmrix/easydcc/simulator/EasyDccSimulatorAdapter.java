package jmri.jmrix.easydcc.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.easydcc.EasyDccCommandStation;
import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccReply;
import jmri.jmrix.easydcc.EasyDccPortController; // no extra simulatorcontroller
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import jmri.jmrix.easydcc.EasyDccTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to a simulated EasyDCC system.
 * <p>
 * Currently, the EasyDccSimulator reacts to commands sent from the user interface
 * with messages an appropriate reply message.
 * Based on jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter / DCCppSimulatorAdapter 2017
 * <p>
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 * @author Mark Underwood, Copyright (C) 2015
 * @author Egbert Broerse, Copyright (C) 2017
 */
public class EasyDccSimulatorAdapter extends EasyDccPortController implements Runnable {

    final static int SENSOR_MSG_RATE = 10;

    private boolean outputBufferEmpty = true;
    private boolean checkBuffer = true;
    private boolean trackPowerState = false;

    public EasyDccSimulatorAdapter() {
        super(new EasyDccSystemConnectionMemo("E", "EasyDCC Simulator")); // pass customized user name
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
    }

    @Override
    public String openPort(String portName, String appName) {
        // open the port in EasyDcc mode, check ability to set moderators
        setPort(portName);
        log.error("openPort should not have been invoked", new Exception());
        return null;
    }

    /**
     * Set if the output buffer is empty or full. This should only be set to
     * false by external processes.
     *
     * @param s true if output buffer is empty; false otherwise
     */
    synchronized public void setOutputBufferEmpty(boolean s) {
        outputBufferEmpty = s;
    }

    /**
     * Can the port accept additional characters? The state of CTS determines
     * this, as there seems to be no way to check the number of queued bytes and
     * buffer length. This might go false for short intervals, but it might also
     * stick off if something goes wrong.
     *
     * @return true if port can accept additional characters; false otherwise
     */
    public boolean okToSend() {
        if (checkBuffer) {
            log.debug("Buffer Empty: " + outputBufferEmpty);
            return (outputBufferEmpty);
        } else {
            log.debug("No Flow Control or Buffer Check");
            return (true);
        }
    }

    /**
     * Set up all of the other objects to operate with an EasyDccSimulator
     * connected to this port.
     */
    @Override
    public void configure() {
        // Connect to a traffic controller
        EasyDccTrafficController control = new EasyDccTrafficController(getSystemConnectionMemo());
        control.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setEasyDccTrafficController(control);

        // do the common manager config
        this.getSystemConnectionMemo().configureManagers();

        sourceThread = new Thread(this);
        sourceThread.start();
    }

    // Base class methods for the EasyDccSimulatorPortController interface

    @Override
    public DataInputStream getInputStream() {
        if (pin == null) {
            log.error("getInputStream called before load(), stream not available");
            ConnectionStatus.instance().setConnectionState(this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
        }
        return pin;
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (pout == null) {
            log.error("getOutputStream called before load(), stream not available");
            ConnectionStatus.instance().setConnectionState(this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
        }
        return pout;
    }

    @Override
    public boolean status() {
        return (pout != null && pin != null);
    }

    /**
     * Get an array of valid baud rates. This is currently just a message saying
     * its fixed.
     *
     * @return null
     */
    @Override
    public String[] validBaudRates() {
        return null;
    }

    @Override
    public void run() { // start a new thread
        // this thread has one task.  It repeatedly reads from the input pipe
        // and writes modified data to the output pipe. This is the heart
        // of the command station simulation.
        log.debug("Simulator Thread Started");

        ConnectionStatus.instance().setConnectionState(this.getCurrentPortName(), ConnectionStatus.CONNECTION_UP);
        for (;;) {
            EasyDccMessage m = readMessage();
            log.debug("Simulator Thread received message {}", m.toString());
            EasyDccReply r = generateReply(m);
            // If generateReply() returns null, do nothing. No reply to send.
            if (r != null) {
                writeReply(r);
                log.debug("Simulator Thread sent Reply {}", r.toString());
            }
        }
    }

    /**
     * Read one incoming message from the buffer
     * and set outputBufferEmpty to true.
     */
    private EasyDccMessage readMessage() {
        EasyDccMessage msg = null;
        try {
            msg = loadChars();
        } catch (java.io.IOException e) {
            // should do something meaningful here.
            ConnectionStatus.instance().setConnectionState(this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);

        }
        setOutputBufferEmpty(true);
        return (msg);
    }

    /**
     * This is the heart of the simulation.  It translates an
     * incoming EasyDccMessage into an outgoing EasyDccReply.
     *
     * As yet, no meaningful replies are returned. TODO
     */
    @SuppressWarnings("fallthrough")
    private EasyDccReply generateReply(EasyDccMessage msg) {
        String s, r;
        EasyDccReply reply = null;

        log.debug("Generate Reply to message type {} string = {}", msg.getElement(0), msg.toString());

        switch (msg.getOpCode()) {

//            case DCCppConstants.THROTTLE_CMD:
//                log.debug("THROTTLE_CMD detected");
//                s = msg.toString();
//                try {
//                    p = Pattern.compile(DCCppConstants.THROTTLE_CMD_REGEX);
//                    m = p.matcher(s);
//                    if (!m.matches()) {
//                        log.error("Malformed Throttle Command: {}", s);
//                        return (null);
//                    }
//                    r = "T " + m.group(1) + " " + m.group(3) + " " + m.group(4);
//                    reply = new EasyDccReply(r);
//                    log.debug("Reply generated = {}", reply.toString());
//                } catch (PatternSyntaxException e) {
//                    log.error("Malformed pattern syntax! ");
//                    return (null);
//                } catch (IllegalStateException e) {
//                    log.error("Group called before match operation executed string= " + s);
//                    return (null);
//                } catch (IndexOutOfBoundsException e) {
//                    log.error("Index out of bounds string= " + s);
//                    return (null);
//                }
//                break;

            case 'V':
//                if (msg.isTurnoutAddMessage()) {
//                    log.debug("Add Turnout Message");
                    r = "O";
//                } else if (msg.isTurnoutDeleteMessage()) {
//                    log.debug("Delete Turnout Message");
//                    r = "O";
//                } else if (msg.isListTurnoutsMessage()) {
//                    log.debug("List Turnouts Message");
//                    r = "H 1 27 3 1";
//                } else {
//                    log.debug("TURNOUT_CMD detected");
//                    r = "H" + msg.getTOIDString() + " " + Integer.toString(msg.getTOStateInt());
//                }
                reply = new EasyDccReply(r);
                log.debug("Reply generated = {}", reply.toString());
                break;

//            case DCCppConstants.OUTPUT_CMD:
//                if (msg.isOutputCmdMessage()) {
//                    log.debug("Output Command Message: {}", msg.toString());
//                    r = "Y" + msg.getOutputIDString() + " " + (msg.getOutputStateBool() ? "1" : "0");
//                    log.debug("Reply String: {}", r);
//                    reply = new EasyDccReply(r);
//                    log.debug("Reply generated = {}", reply.toString());
//                } else if (msg.isOutputAddMessage() || msg.isOutputDeleteMessage()) {
//                    log.debug("Output Add/Delete Message");
//                    r = "O";
//                } else if (msg.isListOutputsMessage()) {
//                    log.debug("Output List Message");
//                    r = "Y 1 2 3 4"; // Totally fake, but the right number of arguments.
//                } else {
//                    log.error("Invalid Output Command: {}{", msg.toString());
//                    r = "Y 1 2";
//                }
//                reply = new EasyDccReply(r);
//                log.debug("Reply generated = {}", reply.toString());
//                break;

//            case DCCppConstants.PROG_WRITE_CV_BYTE:
//                log.debug("PROG_WRITE_CV_BYTE detected");
//                s = msg.toString();
//                try {
//                    p = Pattern.compile(DCCppConstants.PROG_WRITE_BYTE_REGEX);
//                    m = p.matcher(s);
//                    if (!m.matches()) {
//                        log.error("Malformed ProgWriteCVByte Command: {}", s);
//                        return (null);
//                    }
//                    // CMD: <W CV Value CALLBACKNUM CALLBACKSUB>
//                    // Response: <r CALLBACKNUM|CALLBACKSUB|CV Value>
//                    r = "r " + m.group(3) + "|" + m.group(4) + "|" + m.group(1) +
//                            " " + m.group(2);
//                    CVs[Integer.parseInt(m.group(1))] = Integer.parseInt(m.group(2));
//                    reply = new EasyDccReply(r);
//                    log.debug("Reply generated = {}", reply.toString());
//                } catch (PatternSyntaxException e) {
//                    log.error("Malformed pattern syntax! ");
//                    return (null);
//                } catch (IllegalStateException e) {
//                    log.error("Group called before match operation executed string= " + s);
//                    return (null);
//                } catch (IndexOutOfBoundsException e) {
//                    log.error("Index out of bounds string= " + s);
//                    return (null);
//                }
//                break;

//            case DCCppConstants.PROG_WRITE_CV_BIT:
//                log.debug("PROG_WRITE_CV_BIT detected");
//                s = msg.toString();
//                try {
//                    p = Pattern.compile(DCCppConstants.PROG_WRITE_BIT_REGEX);
//                    m = p.matcher(s);
//                    if (!m.matches()) {
//                        log.error("Malformed ProgWriteCVBit Command: {}", s);
//                        return (null);
//                    }
//                    // CMD: <B CV BIT Value CALLBACKNUM CALLBACKSUB>
//                    // Response: <r CALLBACKNUM|CALLBACKSUB|CV BIT Value>
//                    r = "r " + m.group(4) + "|" + m.group(5) + "|" + m.group(1) + " "
//                            + m.group(2) + m.group(3);
//                    int idx = Integer.parseInt(m.group(1));
//                    int bit = Integer.parseInt(m.group(2));
//                    int v = Integer.parseInt(m.group(3));
//                    if (v == 1) {
//                        CVs[idx] = CVs[idx] | (0x0001 << bit);
//                    } else {
//                        CVs[idx] = CVs[idx] & ~(0x0001 << bit);
//                    }
//                    reply = new EasyDccReply(r);
//                    log.debug("Reply generated = {}", reply.toString());
//                } catch (PatternSyntaxException e) {
//                    log.error("Malformed pattern syntax! ");
//                    return (null);
//                } catch (IllegalStateException e) {
//                    log.error("Group called before match operation executed string= " + s);
//                    return (null);
//                } catch (IndexOutOfBoundsException e) {
//                    log.error("Index out of bounds string= " + s);
//                    return (null);
//                }
//                break;

//            case DCCppConstants.PROG_READ_CV:
//                log.debug("PROG_READ_CV detected");
//                s = msg.toString();
//                try {
//                    p = Pattern.compile(DCCppConstants.PROG_READ_REGEX);
//                    m = p.matcher(s);
//                    if (!m.matches()) {
//                        log.error("Malformed PROG_READ_CV Command: {}", s);
//                        return (null);
//                    }
//                    // TODO: Work Magic Here to retrieve stored value.
//                    // Make sure that CV exists
//                    int cv = Integer.parseInt(m.group(1));
//                    int cvVal = 0; // Default to 0 if they're reading out of bounds.
//                    if (cv < CVs.length) {
//                        cvVal = CVs[Integer.parseInt(m.group(1))];
//                    }
//                    // CMD: <R CV CALLBACKNUM CALLBACKSUB>
//                    // Response: <r CALLBACKNUM|CALLBACKSUB|CV Value>
//                    r = "r " + m.group(2) + "|" + m.group(3) + "|" + m.group(1) + " "
//                            + Integer.toString(cvVal);
//
//                    reply = new EasyDccReply(r);
//                    log.debug("Reply generated = {}", reply.toString());
//                } catch (PatternSyntaxException e) {
//                    log.error("Malformed pattern syntax! ");
//                    return (null);
//                } catch (IllegalStateException e) {
//                    log.error("Group called before match operation executed string= " + s);
//                    return (null);
//                } catch (IndexOutOfBoundsException e) {
//                    log.error("Index out of bounds string= " + s);
//                    return (null);
//                }
//                break;

//            case 'E':
//                log.debug("TRACK_POWER_ON detected");
//                trackPowerState = true;
//                reply = EasyDccReply("p1");
//                log.debug("Reply generated = {}", reply.toString());
//                break;

//            case 'K':
//                log.debug("TRACK_POWER_OFF detected");
//                trackPowerState = false;
//                reply = new EasyDccReply("p0");
//                log.debug("Reply generated = {}", reply.toString());
//                break;

//            case DCCppConstants.READ_TRACK_CURRENT:
//                log.debug("READ_TRACK_CURRENT detected");
//                int randint = 480 + rgen.nextInt(64);
//                reply = new EasyDccReply("a " + (trackPowerState ? Integer.toString(randint) : "0"));
//                log.debug("Reply generated = {}", reply.toString());
//                break;

//            case DCCppConstants.READ_CS_STATUS:
//                log.debug("READ_CS_STATUS detected");
//                generateReadCSStatusReply(); // Handle this special.
//                break;

            default:
                log.debug("non-reply message detected");
                // Send no reply.
                return (null);
        }
        return (reply);
    }

    private void generateReadCSStatusReply() {
        EasyDccReply r = new EasyDccReply("iDCC++ BASE STATION FOR ARDUINO MEGA / ARDUINO MOTOR SHIELD: BUILD 23 Feb 2015 09:23:57");
        writeReply(r);
        log.debug("Simulator Thread sent Reply {}", r.toString());
        r = new EasyDccReply("N0: SERIAL");
        writeReply(r);
        log.debug("Simulator Thread sent Reply {}", r.toString());
        // Generate the other messages too...
    }

    private void writeReply(EasyDccReply r) {
        int i;
        int len = r.getNumDataElements();  // opCode+Nbytes+ECC
        // If r == null, there is no reply to be sent.
        try {
            outpipe.writeByte((byte) '<');
            for (i = 0; i < len; i++) {
                outpipe.writeByte((byte) r.getElement(i));
            }
            outpipe.writeByte((byte) '>');
        } catch (java.io.IOException ex) {
            ConnectionStatus.instance().setConnectionState(this.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
        }
    }

    /**
     * Get characters from the input source, and file a message.
     * <p>
     * Only used in the Receive thread.
     *
     * @returns filled message, only when the message is complete.
     * @throws IOException when presented by the input source.
     */
    private EasyDccMessage loadChars() throws java.io.IOException {
        // Spin waiting for start-of-frame '<' character (and toss it)
        StringBuilder s = new StringBuilder("");
        byte char1;
        boolean found_start = false;

        // this loop reads every other character; is that the desired behavior?
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
        // Expects a message of the format "CVnnnvv" where vv is
        // the hexadecimal value or "Vnvv" where vv is the hexadecimal value.
        for (int i = 0; i < 7; i++) { // or 5
            char1 = readByteProtected(inpipe);
            if (char1 == '>') {
                log.debug("msg found > ");
                // Don't store the >
                break;
            } else {
                log.debug("msg read byte {}", char1);
                char c = (char) (char1 & 0x00FF);
                s.append(Character.toString(c));
            }
        }
        log.debug("Complete message = {}", s);
        // BUG FIX: Incoming EasyDCC messages are already formatted for EasyDCC and don't
        // need to be parsed. Indeed, trying to parse them will screw them up.
        // So instead, we de-@Deprecated the string constructor so that we can
        // directly create an EasyDccReply from the incoming string without translation/parsing.
        return (new EasyDccMessage(s.toString()));
    }

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <P>
     * When a port is set to have a receive timeout (via the
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

    private DataOutputStream pout = null; // for output to other classes
    private DataInputStream pin = null; // for input from other classes
    // internal ends of the pipes
    private DataOutputStream outpipe = null;  // feed pin
    private DataInputStream inpipe = null; // feed pout
    private Thread sourceThread;

    private final static Logger log = LoggerFactory.getLogger(EasyDccSimulatorAdapter.class);

}
