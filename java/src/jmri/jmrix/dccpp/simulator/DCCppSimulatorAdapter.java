package jmri.jmrix.dccpp.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;
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
import jmri.util.ImmediatePipedOutputStream;
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
 * @author M Steve Todd, 2021
 *
 * Based on {@link jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter}
 */
public class DCCppSimulatorAdapter extends DCCppSimulatorPortController implements Runnable {

    final static int SENSOR_MSG_RATE = 10;

    private boolean outputBufferEmpty = true;
    private final boolean checkBuffer = true;
    private boolean trackPowerState = false;
    // One extra array element so that i can index directly from the
    // CV value, ignoring CVs[0].
    private final int[] CVs = new int[DCCppConstants.MAX_DIRECT_CV + 1];

    private java.util.TimerTask keepAliveTimer; // Timer used to periodically
    private static final long keepAliveTimeoutValue = 30000; // Interval
    //keep track of recreation command, including state, for each turnout and output
    private LinkedHashMap<Integer,String> turnouts = new LinkedHashMap<Integer, String>();
    //keep track of speed, direction and functions for each loco address
    private LinkedHashMap<Integer,Integer> locoSpeedByte = new LinkedHashMap<Integer,Integer>();
    private LinkedHashMap<Integer,Integer> locoFunctions = new LinkedHashMap<Integer,Integer>();

    public DCCppSimulatorAdapter() {
        setPort(Bundle.getMessage("None"));
        try {
            PipedOutputStream tempPipeI = new ImmediatePipedOutputStream();
            pout = new DataOutputStream(tempPipeI);
            inpipe = new DataInputStream(new PipedInputStream(tempPipeI));
            PipedOutputStream tempPipeO = new ImmediatePipedOutputStream();
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

        sourceThread = jmri.util.ThreadingUtil.newThread(this);
        sourceThread.start();

        new DCCppInitializationManager(this.getSystemConnectionMemo());
    }

    /**
     * Set up the keepAliveTimer, and start it.
     */
    private void keepAliveTimer() {
        if (keepAliveTimer == null) {
            keepAliveTimer = new java.util.TimerTask(){
                @Override
                public void run() {
                    // If the timer times out, send a request for status
                    DCCppSimulatorAdapter.this.getSystemConnectionMemo().getDCCppTrafficController()
                    .sendDCCppMessage(jmri.jmrix.dccpp.DCCppMessage.makeCSStatusMsg(), null);
                }
            };
        } else {
            keepAliveTimer.cancel();
        }
        jmri.util.TimerUtil.schedule(keepAliveTimer, keepAliveTimeoutValue, keepAliveTimeoutValue);
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

    @Override
    public void run() { // start a new thread
        // this thread has one task.  It repeatedly reads from the input pipe
        // and writes modified data to the output pipe.  This is the heart
        // of the command station simulation.
        log.debug("Simulator Thread Started");

        keepAliveTimer();

        ConnectionStatus.instance().setConnectionState(getUserName(), getCurrentPortName(), ConnectionStatus.CONNECTION_UP);
        for (;;) {
            DCCppMessage m = readMessage();
            log.debug("Simulator Thread received message '{}'", m);
            DCCppReply r = generateReply(m);
            // If generateReply() returns null, do nothing. No reply to send.
            if (r != null) {
                writeReply(r);
            }

            // Once every SENSOR_MSG_RATE loops, generate a random Sensor message.
            int rand = ThreadLocalRandom.current().nextInt(SENSOR_MSG_RATE);
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
        String s, r = null;
        Pattern p;
        Matcher m;
        DCCppReply reply = null;

        log.debug("Generate Reply to message type '{}' string = '{}'", msg.getElement(0), msg);

        switch (msg.getElement(0)) {

            case DCCppConstants.THROTTLE_CMD:
                log.debug("THROTTLE_CMD detected");
                s = msg.toString();
                try {
                    p = Pattern.compile(DCCppConstants.THROTTLE_CMD_REGEX);
                    m = p.matcher(s); //<t REG CAB SPEED DIR>
                    if (!m.matches()) {
                        p = Pattern.compile(DCCppConstants.THROTTLE_V3_CMD_REGEX);
                        m = p.matcher(s); //<t locoId speed dir>
                        if (!m.matches()) {
                            log.error("Malformed Throttle Command: {}", s);
                            return (null);
                        }                       
                        int locoId = Integer.parseInt(m.group(1));
                        int speed = Integer.parseInt(m.group(2));
                        int dir = Integer.parseInt(m.group(3));
                        storeLocoSpeedByte(locoId, speed, dir);
                        r = getLocoStateString(locoId);
                    } else {
                        r = "T " + m.group(1) + " " + m.group(3) + " " + m.group(4);
                    }
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
                reply = DCCppReply.parseDCCppReply(r);
                log.debug("Reply generated = '{}'", reply);
                break;

            case DCCppConstants.FUNCTION_V4_CMD:
                log.debug("FunctionV4Detected");
                s = msg.toString();
                r = "";
                try {
                    p = Pattern.compile(DCCppConstants.FUNCTION_V4_CMD_REGEX); 
                    m = p.matcher(s); //<F locoId func 1|0>
                    if (!m.matches()) {
                        log.error("Malformed FunctionV4 Command: {}", s);
                        return (null);
                    }                       
                    int locoId = Integer.parseInt(m.group(1));
                    int fn = Integer.parseInt(m.group(2));
                    int state = Integer.parseInt(m.group(3));
                    storeLocoFunction(locoId, fn, state);
                    r = getLocoStateString(locoId);
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
                reply = DCCppReply.parseDCCppReply(r);
                log.debug("Reply generated = '{}'", reply);
                break;

            case DCCppConstants.TURNOUT_CMD:
                if (msg.isTurnoutAddMessage()
                        || msg.isTurnoutAddDCCMessage()
                        || msg.isTurnoutAddServoMessage()
                        || msg.isTurnoutAddVpinMessage()) {
                    log.debug("Add Turnout Message");
                    s = "H" + msg.toString().substring(1) + " 0"; //T reply is H, init to closed
                    turnouts.put(msg.getTOIDInt(), s);
                    r = "O";
                } else if (msg.isTurnoutDeleteMessage()) {
                    log.debug("Delete Turnout Message");
                    turnouts.remove(msg.getTOIDInt());
                    r = "O";
                } else if (msg.isListTurnoutsMessage()) {
                    log.debug("List Turnouts Message");
                    generateTurnoutListReply();
                    break;
                } else if (msg.isTurnoutCmdMessage()) {
                    log.debug("Turnout Command Message");
                    s = turnouts.get(msg.getTOIDInt()); //retrieve the stored turnout def
                    if (s != null) {
                        s = s.substring(0, s.length()-1) + msg.getTOStateInt(); //replace the last char with new state
                        turnouts.put(msg.getTOIDInt(), s); //update the stored turnout
                        r = "H " + msg.getTOIDString() + " " + msg.getTOStateInt();
                    } else {
                        log.warn("Unknown turnout ID '{}'", msg.getTOIDInt());
                        r = "X";
                    }

                } else {
                    log.debug("Unknown TURNOUT_CMD detected");
                    r = "X";
                }
                reply = DCCppReply.parseDCCppReply(r);
                log.debug("Reply generated = '{}'", reply);
                break;

            case DCCppConstants.OUTPUT_CMD:
                if (msg.isOutputCmdMessage()) {
                    log.debug("Output Command Message: '{}'", msg);
                    s = turnouts.get(msg.getOutputIDInt()); //retrieve the stored turnout def
                    if (s != null) {
                        s = s.substring(0, s.length()-1) + (msg.getOutputStateBool() ? "1" : "0"); //replace the last char with new state
                        turnouts.put(msg.getOutputIDInt(), s); //update the stored turnout
                        r = "Y " + msg.getOutputIDInt() + " " + (msg.getOutputStateBool() ? "1" : "0");
                        reply = DCCppReply.parseDCCppReply(r);
                        log.debug("Reply generated = {}", reply.toString());
                    } else {
                        log.warn("Unknown output ID '{}'", msg.getOutputIDInt());
                        r = "X";
                    }
                } else if (msg.isOutputAddMessage()) {
                    log.debug("Output Add Message");
                    s = "Y" + msg.toString().substring(1) + " 0"; //Z reply is Y, init to closed
                    turnouts.put(msg.getOutputIDInt(), s);
                    r = "O";
                } else if (msg.isOutputDeleteMessage()) {
                    log.debug("Output Delete Message");
                    turnouts.remove(msg.getOutputIDInt());
                    r = "O";
                } else if (msg.isListOutputsMessage()) {
                    log.debug("Output List Message");
                    generateTurnoutListReply();
                    break;
                } else {
                    log.error("Unknown Output Command: '{}'", msg.toString());
                    r = "X";
                }
                reply = DCCppReply.parseDCCppReply(r);
                log.debug("Reply generated = '{}'", reply);
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
                log.debug("Reply generated = '{}'", reply);
                break;

            case DCCppConstants.PROG_WRITE_CV_BYTE:
                log.debug("PROG_WRITE_CV_BYTE detected");
                s = msg.toString();
                r = "";
                try {
                    if (s.matches(DCCppConstants.PROG_WRITE_BYTE_REGEX)) {
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
                    } else if (s.matches(DCCppConstants.PROG_WRITE_BYTE_V4_REGEX)) {
                        p = Pattern.compile(DCCppConstants.PROG_WRITE_BYTE_V4_REGEX);
                        m = p.matcher(s);
                        if (!m.matches()) {
                            log.error("Malformed ProgWriteCVByte Command: {}", s);
                            return (null);
                        }
                        // CMD: <W CV Value>
                        // Response: <r CV Value>
                        r = "r " + m.group(1) + " " + m.group(2);
                        CVs[Integer.parseInt(m.group(1))] = Integer.parseInt(m.group(2));
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
                r = "";
                try {
                    if (s.matches(DCCppConstants.PROG_READ_CV_REGEX)) {
                        p = Pattern.compile(DCCppConstants.PROG_READ_CV_REGEX);
                        m = p.matcher(s);
                        int cv = Integer.parseInt(m.group(1));
                        int cvVal = 0; // Default to 0 if they're reading out of bounds.
                        if (cv < CVs.length) {
                            cvVal = CVs[Integer.parseInt(m.group(1))];
                        }
                        // CMD: <R CV CALLBACKNUM CALLBACKSUB>
                        // Response: <r CALLBACKNUM|CALLBACKSUB|CV Value>
                        r = "r " + m.group(2) + "|" + m.group(3) + "|" + m.group(1) + " "
                                + cvVal;
                    } else if (s.matches(DCCppConstants.PROG_READ_CV_V4_REGEX)) {
                        p = Pattern.compile(DCCppConstants.PROG_READ_CV_V4_REGEX);
                        m = p.matcher(s);
                        if (!m.matches()) {
                            log.error("Malformed PROG_READ_CV Command: {}", s);
                            return (null);
                        }
                        int cv = Integer.parseInt(m.group(1));
                        int cvVal = 0; // Default to 0 if they're reading out of bounds.
                        if (cv < CVs.length) {
                            cvVal = CVs[Integer.parseInt(m.group(1))];
                        }
                        // CMD: <R CV>
                        // Response: <r CV Value>
                        r = "r " + m.group(1) + " " + cvVal;
                    } else if (s.matches(DCCppConstants.PROG_READ_LOCOID_REGEX)) {
                        int locoId = ThreadLocalRandom.current().nextInt(9999)+1; //get a random locoId between 1 and 9999
                        // CMD: <R>
                        // Response: <r LocoId>
                        r = "r " + locoId;
                    } else {
                        log.error("Malformed PROG_READ_CV Command: {}", s);
                        return (null);
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

            case DCCppConstants.PROG_VERIFY_CV:
                log.debug("PROG_VERIFY_CV detected");
                s = msg.toString();
                try {
                    p = Pattern.compile(DCCppConstants.PROG_VERIFY_REGEX);
                    m = p.matcher(s);
                    if (!m.matches()) {
                        log.error("Malformed PROG_VERIFY_CV Command: {}", s);
                        return (null);
                    }
                    // TODO: Work Magic Here to retrieve stored value.
                    // Make sure that CV exists
                    int cv = Integer.parseInt(m.group(1));
                    int cvVal = 0; // Default to 0 if they're reading out of bounds.
                    if (cv < CVs.length) {
                        cvVal = CVs[cv];
                    }
                    // CMD: <V CV STARTVAL>
                    // Response: <v CV Value>
                    r = "v " + cv + " " + cvVal;

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
                break;

            case DCCppConstants.TRACK_POWER_OFF:
                log.debug("TRACK_POWER_OFF detected");
                trackPowerState = false;
                reply = DCCppReply.parseDCCppReply("p0");
                break;

            case DCCppConstants.READ_MAXNUMSLOTS:
                log.debug("READ_MAXNUMSLOTS detected");
                reply = DCCppReply.parseDCCppReply("# 12");
                break;

            case DCCppConstants.READ_TRACK_CURRENT:
                log.debug("READ_TRACK_CURRENT detected");
                generateMeterReplies();
                break;

            case DCCppConstants.TRACKMANAGER_CMD:
                log.debug("TRACKMANAGER_CMD detected");
                reply = DCCppReply.parseDCCppReply("= A MAIN");
                writeReply(reply);
                reply = DCCppReply.parseDCCppReply("= B PROG");
                break;

            case DCCppConstants.LCD_TEXT_CMD:
                log.debug("LCD_TEXT_CMD detected");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss a");
                LocalDateTime now = LocalDateTime.now();
                String dateTimeString = now.format(formatter);
                reply = DCCppReply.parseDCCppReply("@ 0 0 \"Welcome to DCC-EX -- " + dateTimeString + "\"" );
                writeReply(reply);
                reply = DCCppReply.parseDCCppReply("@ 0 1 \"LCD Line 1\"");
                writeReply(reply);
                reply = DCCppReply.parseDCCppReply("@ 0 2 \"LCD Line 2\"");
                writeReply(reply);
                reply = DCCppReply.parseDCCppReply("@ 0 3 \"     LCD Line 3 with spaces   \"");
                writeReply(reply);
                reply = DCCppReply.parseDCCppReply("@ 0 4 \"1234567890123456789012345678901234567890\"");
                break;

            case DCCppConstants.READ_CS_STATUS:
                log.debug("READ_CS_STATUS detected");
                generateReadCSStatusReply(); // Handle this special.
                break;

            case DCCppConstants.FUNCTION_CMD:
            case DCCppConstants.FORGET_CAB_CMD:
            case DCCppConstants.ACCESSORY_CMD:
            case DCCppConstants.OPS_WRITE_CV_BYTE:
            case DCCppConstants.OPS_WRITE_CV_BIT:
            case DCCppConstants.WRITE_DCC_PACKET_MAIN:
            case DCCppConstants.WRITE_DCC_PACKET_PROG:
                log.debug("non-reply message detected: '{}'", msg);
                // Send no reply.
                return (null);

            default:
                log.debug("unknown message detected: '{}'", msg);
                return (null);
        }
        return (reply);
    }

    //calc speedByte value matching DCC++EX, then store it, so it can be used in the locoState replies
    private void storeLocoSpeedByte(int locoId, int speed, int dir) {
        if (speed>0) speed++; //add 1 to speed if not zero or estop
        if (speed<0) speed = 1; //eStop is actually 1
        int dirBit = dir*128; //calc value for direction bit
        int speedByte = dirBit + speed; //add dirBit to adjusted speed value
        locoSpeedByte.put(locoId, speedByte); //store it
        if (!locoFunctions.containsKey(locoId)) locoFunctions.put(locoId, 0); //init functions if not set
    }

    //stores the calculated value of the functionsByte as used by DCC++EX
    private void storeLocoFunction(int locoId, int function, int state) {
        int functions = 0; //init functions to all off if not stored
        if (locoFunctions.containsKey(locoId)) 
            functions = locoFunctions.get(locoId); //get stored value, if any
        int mask = 1 << function;
        if (state == 1) {
            functions = functions | mask; //apply ON
        } else {
            functions = functions & ~mask; //apply OFF            
        }
        locoFunctions.put(locoId, functions); //store new value
        if (!locoSpeedByte.containsKey(locoId)) 
            locoSpeedByte.put(locoId, 0); //init speedByte if not set
    }

    //retrieve stored values and calculate and format the locostate message text
    private String getLocoStateString(int locoId) {
        String s;
        int speedByte = locoSpeedByte.get(locoId);
        int functions = locoFunctions.get(locoId);
        s = "l " + locoId + " 0 " + speedByte + " " + functions;  //<l loco slot speedByte functions>
        return s;
    }

    /* 's'tatus message gets multiple reply messages */
    private void generateReadCSStatusReply() {
        DCCppReply r = new DCCppReply("p" + (trackPowerState ? "1" : "0"));
        writeReply(r);
        r = DCCppReply.parseDCCppReply("iDCC-EX V-4.0.1 / MEGA / STANDARD_MOTOR_SHIELD G-9db6d36");
        writeReply(r);
        generateTurnoutStatesReply();
    }

    /* Send list of creation command with states for all defined turnouts and outputs */
    private void generateTurnoutListReply() {
        if (!turnouts.isEmpty()) {
            turnouts.forEach((key, value) -> { //send back the full create string for each
                DCCppReply r = new DCCppReply(value);
                writeReply(r);
            });
        } else {
            writeReply(new DCCppReply("X No Turnouts Defined"));
        }
    }

    /* Send list of turnout states */
    private void generateTurnoutStatesReply() {
        if (!turnouts.isEmpty()) {
            turnouts.forEach((key, value) -> {
                String s = value.substring(0,2) + key + value.substring(value.length()-2); //command char + id + state
                DCCppReply r = new DCCppReply(s);
                writeReply(r);
            });
        } else {
            writeReply(new DCCppReply("X No Turnouts Defined"));
        }
    }

    /* 'c' current request message gets multiple reply messages */
    private void generateMeterReplies() {
        int currentmA = 1100 + ThreadLocalRandom.current().nextInt(64);
        double voltageV = 14.5 + ThreadLocalRandom.current().nextInt(10)/10.0;
        String rs = "c CurrentMAIN " + (trackPowerState ? Double.toString(currentmA) : "0") + " C Milli 0 1997 1 1997";
        DCCppReply r = new DCCppReply(rs);
        writeReply(r);
        r = new DCCppReply("c VoltageMAIN " + voltageV + " V NoPrefix 0 18.0 0.1 16.0");
        writeReply(r);
        rs = "a " + (trackPowerState ? Integer.toString((1997/currentmA)*100) : "0");
        r = DCCppReply.parseDCCppReply(rs);
        writeReply(r);
    }

    private void generateRandomSensorReply() {
        // Pick a random sensor number between 0 and 10;
        int sensorNum = ThreadLocalRandom.current().nextInt(10)+1; // Generate a random sensor number between 1 and 10
        int value = ThreadLocalRandom.current().nextInt(2); // Generate state value between 0 and 1

        String reply = (value == 1 ? "Q " : "q ") + sensorNum;

        DCCppReply r = DCCppReply.parseDCCppReply(reply);
        writeReply(r);
    }

    private void writeReply(DCCppReply r) {
        log.debug("Simulator Thread sending Reply '{}'", r);
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
        StringBuilder s = new StringBuilder();
        byte char1;
        boolean found_start = false;

        // this loop reads every other character; is that the desired behavior?
        while (!found_start) {
            char1 = readByteProtected(inpipe);
            if ((char1 & 0xFF) == '<') {
                found_start = true;
                log.trace("Found starting < ");
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
                log.trace("msg found > ");
                // Don't store the >
                break;
            } else {
                log.trace("msg read byte {}", char1);
                char c = (char) (char1 & 0x00FF);
                s.append(c);
            }
        }
        // TODO: Still need to strip leading and trailing whitespace.
        log.debug("Complete message = {}", s);
        return (new DCCppMessage(s.toString()));
    }

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <p>
     * When a port is set to have a receive timeout (via the
     * enableReceiveTimeout() method), some will return zero bytes or an
     * EOFException at the end of the timeout. In that case, the read should be
     * repeated to get the next real character.
     * @param istream source of data
     * @return next available byte, when available
     * @throws IOException from underlying operation
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
