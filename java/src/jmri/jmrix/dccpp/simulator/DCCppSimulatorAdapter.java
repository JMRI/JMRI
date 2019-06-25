package jmri.jmrix.dccpp.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
 * NOTE: Most DCC++ commands are still unsupported in this implementation.
 *
 * Normally controlled by the dccpp.DCCppSimulator.DCCppSimulatorFrame class.
 *
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 * @author Mark Underwood, Copyright (C) 2015
 *
 * Based on {@link jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter}
 */
public class DCCppSimulatorAdapter extends DCCppSimulatorPortController implements Runnable {

    final static int SENSOR_MSG_RATE = 10;

    private boolean outputBufferEmpty = true;
    private boolean checkBuffer = true;
    private boolean trackPowerState = false;
    // One extra array element so that i can index directly from the
    // CV value, ignoring CVs[0].
    private int[] CVs = new int[DCCppConstants.MAX_DIRECT_CV + 1];

    private Random rgen = null;

    public DCCppSimulatorAdapter() {
        setPort(Bundle.getMessage("None"));
        try {
            PipedOutputStream tempPipeI = new PipedOutputStream();
            pout = new DataOutputStream(tempPipeI);
            inpipe = new DataInputStream(new PipedInputStream(tempPipeI));
            PipedOutputStream tempPipeO = new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        } catch (java.io.IOException e) {
            log.error("init (pipe): Exception: {}", e.toString());
            return;
        }
        // Zero out the CV table.
        for (int i = 0; i < DCCppConstants.MAX_DIRECT_CV + 1; i++) {
            CVs[i] = 0;
        }

        rgen = new Random(); // used to generate randomized output for current meter
    }

    @Override
    public String openPort(String portName, String appName) {
        // open the port in XpressNet mode, check ability to set moderators
        setPort(portName);
        return null; // normal operation
    }

    /**
     * Set if the output buffer is empty or full. This should only be set to
     * false by external processes.
     *
     * @param s true if output buffer is empty; false otherwise
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
     *
     * @return true if port can accept additional characters; false otherwise
     */
    @Override
    public boolean okToSend() {
        if (checkBuffer) {
            log.debug("Buffer Empty: {}", outputBufferEmpty);
            return (outputBufferEmpty);
        } else {
            log.debug("No Flow Control or Buffer Check");
            return (true);
        }
    }

    /**
     * Set up all of the other objects to operate with a DCCppSimulator
     * connected to this port
     */
    @Override
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
    }

    // base class methods for the DCCppSimulatorPortController interface

    /**
     * {@inheritDoc}
     */
    @Override
    public DataInputStream getInputStream() {
        if (pin == null) {
            log.error("getInputStream called before load(), stream not available");
            ConnectionStatus.instance().setConnectionState(getUserName(), getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
        }
        return pin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataOutputStream getOutputStream() {
        if (pout == null) {
            log.error("getOutputStream called before load(), stream not available");
            ConnectionStatus.instance().setConnectionState(getUserName(), getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
        }
        return pout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean status() {
        return (pout != null && pin != null);
    }

    /**
     * {@inheritDoc}
     * Currently just a message saying it's fixed.
     *
     * @return null
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{};
    }

    @Deprecated
    static public DCCppSimulatorAdapter instance() {
        if (mInstance == null) {
            mInstance = new DCCppSimulatorAdapter();
        }
        return mInstance;
    }

    @Override
    public void run() { // start a new thread
        // this thread has one task.  It repeatedly reads from the input pipe
        // and writes modified data to the output pipe.  This is the heart
        // of the command station simulation.
        log.debug("Simulator Thread Started");

        rgen = new Random();

        ConnectionStatus.instance().setConnectionState(getUserName(), getCurrentPortName(), ConnectionStatus.CONNECTION_UP);
        for (;;) {
            DCCppMessage m = readMessage();
            if (log.isDebugEnabled()) {
                log.debug("Simulator Thread received message {}", m.toString());
            }
            DCCppReply r = generateReply(m);
            // If generateReply() returns null, do nothing. No reply to send.
            if (r != null) {
                writeReply(r);
                if (log.isDebugEnabled()) {
                    log.debug("Simulator Thread sent Reply {}", r.toString());
                }
            }

            // Once every SENSOR_MSG_RATE loops, generate a random Sensor message.
            int rand = rgen.nextInt(SENSOR_MSG_RATE);
            if (rand == 1) {
                generateRandomSensorReply();
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
            ConnectionStatus.instance().setConnectionState(getUserName(), getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);

        }
        setOutputBufferEmpty(true);
        return (msg);
    }

    // generateReply is the heart of the simulation.  It translates an
    // incoming DCCppMessage into an outgoing DCCppReply.
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
                        return (null);
                    }
                    r = "T " + m.group(1) + " " + m.group(3) + " " + m.group(4);
                    reply = DCCppReply.parseDCCppReply(r);
                    log.debug("Reply generated = {}", reply.toString());
                } catch (PatternSyntaxException e) {
                    log.error("Malformed pattern syntax! ");
                    return (null);
                } catch (IllegalStateException e) {
                    log.error("Group called before match operation executed string= {}", s);
                    return (null);
                } catch (IndexOutOfBoundsException e) {
                    log.error("Index out of bounds string= {}", s);
                    return (null);
                }
                break;

            case DCCppConstants.TURNOUT_CMD:
                if (msg.isTurnoutAddMessage()) {
                    log.debug("Add Turnout Message");
                    r = "O";
                } else if (msg.isTurnoutDeleteMessage()) {
                    log.debug("Delete Turnout Message");
                    r = "O";
                } else if (msg.isListTurnoutsMessage()) {
                    log.debug("List Turnouts Message");
                    r = "H 1 27 3 1";
                } else {
                    log.debug("TURNOUT_CMD detected");
                    r = "H" + msg.getTOIDString() + " " + Integer.toString(msg.getTOStateInt());
                }
                reply = DCCppReply.parseDCCppReply(r);
                log.debug("Reply generated = {}", reply.toString());
                break;

            case DCCppConstants.OUTPUT_CMD:
                if (msg.isOutputCmdMessage()) {
                    log.debug("Output Command Message: {}", msg.toString());
                    r = "Y" + msg.getOutputIDString() + " " + (msg.getOutputStateBool() ? "1" : "0");
                    log.debug("Reply String: {}", r);
                    reply = DCCppReply.parseDCCppReply(r);
                    log.debug("Reply generated = {}", reply.toString());
                } else if (msg.isOutputAddMessage() || msg.isOutputDeleteMessage()) {
                    log.debug("Output Add/Delete Message");
                    r = "O";
                } else if (msg.isListOutputsMessage()) {
                    log.debug("Output List Message");
                    r = "Y 1 2 3 4"; // Totally fake, but the right number of arguments.
                } else {
                    log.error("Invalid Output Command: {}", msg.toString());
                    r = "Y 1 2";
                }
                //reply = DCCppReplyParser.parseReply(r);
                reply = DCCppReply.parseDCCppReply(r);
                log.debug("Reply generated = {}", reply.toString());
                break;

            case DCCppConstants.SENSOR_CMD:
                if (msg.isSensorAddMessage()) {
                    log.debug("SENSOR_CMD Add detected");
                    //s = msg.toString();
                    r = "O"; // TODO: Randomize?
                } else if (msg.isSensorDeleteMessage()) {
                    log.debug("SENSOR_CMD Delete detected");
                    //s = msg.toString();
                    r = "O"; // TODO: Randomize?
                } else if (msg.isListSensorsMessage()) {
                    r = "Q 1 4 1"; // TODO: DO this for real.
                } else {
                    log.debug("Invalid SENSOR_CMD detected");
                    r = "X";
                }
                reply = DCCppReply.parseDCCppReply(r);
                log.debug("Reply generated = {}", reply.toString());
                break;

            case DCCppConstants.PROG_WRITE_CV_BYTE:
                log.debug("PROG_WRITE_CV_BYTE detected");
                s = msg.toString();
                try {
                    p = Pattern.compile(DCCppConstants.PROG_WRITE_BYTE_REGEX);
                    m = p.matcher(s);
                    if (!m.matches()) {
                        log.error("Malformed ProgWriteCVByte Command: {}", s);
                        return (null);
                    }
                    // CMD: <W CV Value CALLBACKNUM CALLBACKSUB>
                    // Response: <r CALLBACKNUM|CALLBACKSUB|CV Value>
                    r = "r " + m.group(3) + "|" + m.group(4) + "|" + m.group(1) +
                            " " + m.group(2);
                    CVs[Integer.parseInt(m.group(1))] = Integer.parseInt(m.group(2));
                    reply = DCCppReply.parseDCCppReply(r);
                    log.debug("Reply generated = {}", reply.toString());
                } catch (PatternSyntaxException e) {
                    log.error("Malformed pattern syntax!");
                    return (null);
                } catch (IllegalStateException e) {
                    log.error("Group called before match operation executed string= {}", s);
                    return (null);
                } catch (IndexOutOfBoundsException e) {
                    log.error("Index out of bounds string= {}", s);
                    return (null);
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
                        return (null);
                    }
                    // CMD: <B CV BIT Value CALLBACKNUM CALLBACKSUB>
                    // Response: <r CALLBACKNUM|CALLBACKSUB|CV BIT Value>
                    r = "r " + m.group(4) + "|" + m.group(5) + "|" + m.group(1) + " "
                            + m.group(2) + m.group(3);
                    int idx = Integer.parseInt(m.group(1));
                    int bit = Integer.parseInt(m.group(2));
                    int v = Integer.parseInt(m.group(3));
                    if (v == 1) {
                        CVs[idx] = CVs[idx] | (0x0001 << bit);
                    } else {
                        CVs[idx] = CVs[idx] & ~(0x0001 << bit);
                    }
                    reply = DCCppReply.parseDCCppReply(r);
                    log.debug("Reply generated = {}", reply.toString());
                } catch (PatternSyntaxException e) {
                    log.error("Malformed pattern syntax!");
                    return (null);
                } catch (IllegalStateException e) {
                    log.error("Group called before match operation executed string= {}", s);
                    return (null);
                } catch (IndexOutOfBoundsException e) {
                    log.error("Index out of bounds string= {}", s);
                    return (null);
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
                        return (null);
                    }
                    // TODO: Work Magic Here to retrieve stored value.
                    // Make sure that CV exists
                    int cv = Integer.parseInt(m.group(1));
                    int cvVal = 0; // Default to 0 if they're reading out of bounds.
                    if (cv < CVs.length) {
                        cvVal = CVs[Integer.parseInt(m.group(1))];
                    }
                    // CMD: <R CV CALLBACKNUM CALLBACKSUB>
                    // Response: <r CALLBACKNUM|CALLBACKSUB|CV Value>
                    r = "r " + m.group(2) + "|" + m.group(3) + "|" + m.group(1) + " "
                            + Integer.toString(cvVal);

                    reply = DCCppReply.parseDCCppReply(r);
                    log.debug("Reply generated = {}", reply.toString());
                } catch (PatternSyntaxException e) {
                    log.error("Malformed pattern syntax!");
                    return (null);
                } catch (IllegalStateException e) {
                    log.error("Group called before match operation executed string= {}", s);
                    return (null);
                } catch (IndexOutOfBoundsException e) {
                    log.error("Index out of bounds string= {}", s);
                    return (null);
                }
                break;

            case DCCppConstants.TRACK_POWER_ON:
                log.debug("TRACK_POWER_ON detected");
                trackPowerState = true;
                reply = DCCppReply.parseDCCppReply("p1");
                log.debug("Reply generated = {}", reply.toString());
                break;

            case DCCppConstants.TRACK_POWER_OFF:
                log.debug("TRACK_POWER_OFF detected");
                trackPowerState = false;
                reply = DCCppReply.parseDCCppReply("p0");
                log.debug("Reply generated = {}", reply.toString());
                break;

            case DCCppConstants.READ_TRACK_CURRENT:
                log.debug("READ_TRACK_CURRENT detected");
                int randint = 480 + rgen.nextInt(64);
                reply = DCCppReply.parseDCCppReply("a " + (trackPowerState ? Integer.toString(randint) : "0"));
                log.debug("Reply generated = {}", reply.toString());
                break;

            case DCCppConstants.READ_CS_STATUS:
                log.debug("READ_CS_STATUS detected");
                generateReadCSStatusReply(); // Handle this special.
                break;

            case DCCppConstants.FUNCTION_CMD:
            case DCCppConstants.ACCESSORY_CMD:
            case DCCppConstants.OPS_WRITE_CV_BYTE:
            case DCCppConstants.OPS_WRITE_CV_BIT:
            case DCCppConstants.WRITE_DCC_PACKET_MAIN:
            case DCCppConstants.WRITE_DCC_PACKET_PROG:
                log.debug("non-reply message detected");
                // Send no reply.
                return (null);

            default:
                return (null);
        }
        return (reply);
    }

    private void generateReadCSStatusReply() {
        /*
          String s = new String("<p" + (TrackPowerState ? "1" : "0") + ">");
          DCCppReply r = new DCCppReply(s);
          writeReply(r);
          if (log.isDebugEnabled()) {
          log.debug("Simulator Thread sent Reply {}", r.toString());
          }
        */

        DCCppReply r = DCCppReply.parseDCCppReply("iDCC++ BASE STATION FOR ARDUINO MEGA / ARDUINO MOTOR SHIELD: BUILD 23 Feb 2015 09:23:57");
        writeReply(r);
        if (log.isDebugEnabled()) {
            log.debug("Simulator Thread sent Reply {}", r.toString());
        }
        r = DCCppReply.parseDCCppReply("N0: SERIAL");
        writeReply(r);
        if (log.isDebugEnabled()) {
            log.debug("Simulator Thread sent Reply {}", r.toString());
        }

        // Generate the other messages too...
    }

    private void generateRandomSensorReply() {
        // Pick a random sensor number between 0 and 10;
        Random sNumGenerator = new Random();
        int sensorNum = sNumGenerator.nextInt(10); // Generate a random sensor number between 0 and 9
        Random valueGenerator = new Random();
        int value = valueGenerator.nextInt(2); // Generate state value between 0 and 1

        String reply = (value == 1 ? "Q " : "q ") + Integer.toString(sensorNum);

        DCCppReply r = DCCppReply.parseDCCppReply(reply);
        writeReply(r);
        if (log.isDebugEnabled()) {
            log.debug("Simulator Thread sent Reply {}", r.toString());
        }
    }

    private void writeReply(DCCppReply r) {
        int i;
        int len = r.getLength();  // opCode+Nbytes+ECC
        // If r == null, there is no reply to be sent.
        try {
            outpipe.writeByte((byte) '<');
            for (i = 0; i < len; i++) {
                outpipe.writeByte((byte) r.getElement(i));
            }
            outpipe.writeByte((byte) '>');
        } catch (java.io.IOException ex) {
            ConnectionStatus.instance().setConnectionState(getUserName(), getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
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
    private DCCppMessage loadChars() throws java.io.IOException {
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
                // drop next character before repeating
                readByteProtected(inpipe);
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
                s.append(Character.toString(c));
            }
        }
        // TODO: Still need to strip leading and trailing whitespace.
        log.debug("Complete message = {}", s);
        //return (DCCppMessage.parseDCCppMessage(s.toString()));
        // BUG FIX: Incoming DCCpp messages are already formatted for DCC++ and don't
        // need to be parsed. Indeed, trying to parse them will screw them up.
        // So instead, we de-@Deprecated the string constructor so that we can
        // directly create a DCCppReply from the incoming string without translation/parsing.
        return (new DCCppMessage(s.toString()));
    }

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <p>
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

    volatile static DCCppSimulatorAdapter mInstance = null;
    private DataOutputStream pout = null; // for output to other classes
    private DataInputStream pin = null; // for input from other classes
    // internal ends of the pipes
    private DataOutputStream outpipe = null;  // feed pin
    private DataInputStream inpipe = null; // feed pout
    private Thread sourceThread;

    private final static Logger log = LoggerFactory.getLogger(DCCppSimulatorAdapter.class);

}
