package jmri.jmrix.loconet.hexfile;

import java.io.*;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.LnPortController;
import jmri.jmrix.loconet.lnsvf2.LnSv2MessageContents;
import jmri.jmrix.loconet.uhlenbrock.LncvMessageContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LnHexFilePort implements a LnPortController via an ASCII-hex input file. See
 * below for the file format. There are user-level controls for send next message
 * how long to wait between messages
 *
 * An object of this class should run in a thread of its own so that it can fill
 * the output pipe as needed.
 *
 * The input file is expected to have one message per line. Each line can
 * contain as many bytes as needed, each represented by two Hex characters and
 * separated by a space. Variable whitespace is not (yet) supported.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class LnHexFilePort extends LnPortController implements Runnable {

    volatile BufferedReader sFile = null;

    public LnHexFilePort() {
        this(new HexFileSystemConnectionMemo());
    }

    public LnHexFilePort(LocoNetSystemConnectionMemo memo) {
        super(memo);
        try {
            PipedInputStream tempPipe = new PipedInputStream();
            pin = new DataInputStream(tempPipe);
            outpipe = new DataOutputStream(new PipedOutputStream(tempPipe));
            pout = outpipe;
        } catch (java.io.IOException e) {
            log.error("init (pipe): Exception: {}", e.toString());
        }
        options.put("MaxSlots", // NOI18N
                new Option(Bundle.getMessage("MaxSlots")
                        + ":", // NOI18N
                        new String[] {"5","10","21","120","400"}));
        options.put("SensorDefaultState", // NOI18N
                new Option(Bundle.getMessage("DefaultSensorState")
                        + ":", // NOI18N
                        new String[]{Bundle.getMessage("BeanStateUnknown"),
                            Bundle.getMessage("SensorStateInactive"),
                            Bundle.getMessage("SensorStateActive")}, true));
    }

    /**
     * Fill the contents from a file.
     *
     * @param file the file to be read
     */
    public void load(File file) {
        log.debug("file: {}", file); // NOI18N
        // create the pipe stream for output, also store as the input stream if somebody wants to send
        // (This will emulate the LocoNet echo)
        try {
            sFile = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (Exception e) {
            log.error("load (pipe): Exception: {}", e.toString()); // NOI18N
        }
    }

    @Override
    public void connect() {
        jmri.jmrix.loconet.hexfile.HexFileFrame f
                = new jmri.jmrix.loconet.hexfile.HexFileFrame();

        f.setAdapter(this);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("starting HexFileFrame exception: {}", ex.toString());
        }
        f.configure();
    }

    @Override
    public void run() { // invoked in a new thread
        log.info("LocoNet Simulator Started"); // NOI18N
        while (true) {
            while (sFile == null) {
                // Wait for a file to be available. We have nothing else to do, so we can sleep
                // until we are interrupted
                try {
                    synchronized (this) {
                        wait(1000);
                    }
                } catch (InterruptedException e) {
                    log.info("LnHexFilePort.run: woken from sleep"); // NOI18N
                    if (sFile == null) {
                        log.error("LnHexFilePort.run: unexpected InterruptedException, exiting"); // NOI18N
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            log.info("LnHexFilePort.run: changing input file..."); // NOI18N

            // process the input file into the output side of pipe
            _running = true;
            try {
                // Take ownership of the current file, it will automatically go out of scope
                // when we leave this scope block.  Set sFile to null so we can detect a new file
                // being set in load() while we are running the current file.
                BufferedReader currFile = sFile;
                sFile = null;

                String s;
                while ((s = currFile.readLine()) != null) {
                    // this loop reads one line per turn
                    // ErrLog.msg(ErrLog.debugging, "LnHexFilePort", "run", "string=<" + s + ">");
                    int len = s.length();
                    for (int i = 0; i < len; i += 3) {
                        // parse as hex into integer, then convert to byte
                        int ival = Integer.valueOf(s.substring(i, i + 2), 16);
                        // send each byte to the output pipe (input to consumer)
                        byte bval = (byte) ival;
                        outpipe.writeByte(bval);
                    }

                    // flush the pipe so other threads can see the message
                    outpipe.flush();

                    // finished that line, wait
                    synchronized (this) {
                        wait(delay);
                    }
                    //Thread.sleep(delay);
                }

                // here we're done processing the file
                log.info("LnHexFilePort.run: normal finish to file"); // NOI18N

            } catch (InterruptedException e) {
                if (sFile != null) { // changed in another thread before the interrupt
                    log.info("LnHexFilePort.run: user selected new file"); // NOI18N
                    // swallow the exception since we have handled its intent
                } else {
                    log.error("LnHexFilePort.run: unexpected InterruptedException, exiting"); // NOI18N
                    Thread.currentThread().interrupt();
                    return;
                }
            } catch (Exception e) {
                log.error("run: Exception: {}", e.toString()); // NOI18N
            }
            _running = false;
        }
    }

    /**
     * Provide a new message delay value, but don't allow it to go below 2 msec.
     *
     * @param newDelay delay, in milliseconds
     **/
    public void setDelay(int newDelay) {
        delay = Math.max(2, newDelay);
    }

    // base class methods

    /**
     * {@inheritDoc}
     **/
    @Override
    public DataInputStream getInputStream() {
        if (pin == null) {
            log.error("getInputStream: called before load(), stream not available"); // NOI18N
        }
        return pin;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DataOutputStream getOutputStream() {
        if (pout == null) {
            log.error("getOutputStream: called before load(), stream not available"); // NOI18N
        }
        return pout;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean status() {
        return (pout != null) && (pin != null);
    }

    // to tell if we're currently putting out data
    public boolean running() {
        return _running;
    }

    // private data
    private boolean _running = false;

    // streams to share with user class
    private DataOutputStream pout = null; // this is provided to classes who want to write to us
    private DataInputStream pin = null;  // this is provided to classes who want data from us
    // internal ends of the pipes
    private DataOutputStream outpipe = null;  // feed pin

    @Override
    public boolean okToSend() {
        return true;
    }
    // define operation
    private int delay = 100;      // units are milliseconds; default is quiet a busy LocoNet

    @Override
    public java.util.Vector<String> getPortNames() {
        log.error("getPortNames should not have been invoked", new Exception());
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String openPort(String portName, String appName) {
        log.error("openPort should not have been invoked", new Exception());
        return null;
    }

    @Override
    public void configure() {
        log.error("configure should not have been invoked");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        log.error("validBaudRates should not have been invoked", new Exception());
        return new String[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{};
    }

    /**
     * Get an array of valid values for "option 3"; used to display valid
     * options. May not be null, but may have zero entries.
     *
     * @return the options
     */
    public String[] validOption3() {
        return new String[]{Bundle.getMessage("HandleNormal"),
                Bundle.getMessage("HandleSpread"),
                Bundle.getMessage("HandleOneOnly"),
                Bundle.getMessage("HandleBoth")}; // I18N
    }

    /**
     * Get a String that says what Option 3 represents. May be an empty string,
     * but will not be null
     *
     * @return string containing the text for "Option 3"
     */
    public String option3Name() {
        return "Turnout command handling: ";
    }

    /**
     * Set the third port option. Only to be used after construction, but before
     * the openPort call.
     */
    @Override
    public void configureOption3(String value) {
        super.configureOption3(value);
        log.debug("configureOption3: {}", value); // NOI18N
        setTurnoutHandling(value);
    }

    private boolean simReply = false;

    /**
     * Turn on/off replying to LocoNet messages to simulate devices.
     * @param state new state for simReplies
     */
    public void simReply(boolean state) {
        simReply = state;
        log.debug("SimReply is {}", simReply);
    }

    public boolean simReply() {
        return simReply;
    }

    /**
     * Choose from a subset of hardware replies to send in HexFile simulator mode in response to specific messages.
     * Supported message types:
     * <ul>
     *     <li>LN SV rev2 {@link jmri.jmrix.loconet.lnsvf2.LnSv2MessageContents}</li>
     *     <li>LNCV {@link jmri.jmrix.loconet.uhlenbrock.LncvMessageContents} ReadReply</li>
     * </ul>
     * Listener is attached to jmri.jmrix.loconet.hexfile.HexFileFrame with GUI box to turn this option on/off
     *
     * @param m the message to respond to
     * @return an appropriate reply by type and values
     */
    static public LocoNetMessage generateReply(LocoNetMessage m) {
        LocoNetMessage reply = null;
        //log.debug("generateReply for {}", m.toMonitorString());

        if (LnSv2MessageContents.isSupportedSv2Message(m)) {
            //log.debug("generate reply for SV2 message");
            LnSv2MessageContents c = new LnSv2MessageContents(m);
            if (c.getDestAddr() == -1) { // Sv2 QueryAll, reply (content includes no address)
                log.debug("generate LNSV2 query reply message");
                int dest = 1; // keep it simple, don't fetch src from m
                int myId = 11; // a random value
                int mf = 129; // Digitrax
                int dev = 1;
                int type = 3055;
                int serial = 111;
                reply = LnSv2MessageContents.createSv2DeviceDiscoveryReply(myId, dest, mf, dev, type, serial);
            }
        } else if (LncvMessageContents.isSupportedLncvMessage(m)) {
            if (LncvMessageContents.extractMessageType(m) == LncvMessageContents.LncvCommand.LNCV_READ) {
                // generate READ REPLY
                reply = LncvMessageContents.createLncvReadReply(m);
            } else if (LncvMessageContents.extractMessageType(m) == LncvMessageContents.LncvCommand.LNCV_WRITE) {
                // generate WRITE reply LACK
                reply = new LocoNetMessage(new int[]{LnConstants.OPC_LONG_ACK, 0x6d, 0x7f, 0x1});
            } else if (LncvMessageContents.extractMessageType(m) == LncvMessageContents.LncvCommand.LNCV_PROG_START) {
                // generate STARTPROGALL reply
                reply = LncvMessageContents.createLncvProgStartReply(m);
            }
            // ignore LncvMessageContents.LncvCommand.LNCV_PROG_END, no response expected
        }
        return reply;
    }

    private final static Logger log = LoggerFactory.getLogger(LnHexFilePort.class);

}
