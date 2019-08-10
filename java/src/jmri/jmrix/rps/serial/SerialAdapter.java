package jmri.jmrix.rps.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import jmri.InvokeOnGuiThread;
import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for the RPS system.
 * <p>
 * Unlike many other SerialPortAdapters, this also converts the input stream
 * into Readings that can be passed to the Distributor.
 * <p>
 * This version expects that the "A" command will send back "DATA,," followed by
 * a list of receivers numbers, and data lines will be "0,0,0,0": A value for
 * each address up to the max receiver, even if some are missing (0 in that
 * case)
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2008
 */
public class SerialAdapter extends jmri.jmrix.AbstractSerialPortController {

    public SerialAdapter() {
        super(new RpsSystemConnectionMemo());
        option1Name = "Protocol"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ProtocolVersionLabel"), validOptions1));
        this.manufacturerName = jmri.jmrix.rps.RpsConnectionTypeList.NAC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpsSystemConnectionMemo getSystemConnectionMemo() {
        return (RpsSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    /**
     * Set up all of the other objects to operate.
     */
    @Override
    public void configure() {
        // Connect the control objects:
        //   connect an Engine to the Distributor
        Engine e = Engine.instance();
        Distributor.instance().addReadingListener(e);

        // start the reader
        readerThread = new Thread(new Reader());
        readerThread.start();

        this.getSystemConnectionMemo().configureManagers();
    }

    transient SerialPort activeSerialPort = null;

    @Override
    public synchronized String openPort(String portName, String appName) {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for communication via SerialDriver
            try {
                // find the baud rate value, configure comm options
                int baud = currentBaudNumber(mBaudRate);
                activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            configureLeadsAndFlowControl(activeSerialPort, 0);

            // set timeout
            log.debug("Serial timeout was observed as: {} {}", activeSerialPort.getReceiveTimeout(),
                    activeSerialPort.isReceiveTimeoutEnabled());

            // get and save streams
            serialStream = new DataInputStream(activeSerialPort.getInputStream());
            ostream = activeSerialPort.getOutputStream();

            // start getting the initial message
            sendBytes(new byte[]{(byte) 'A', 13});

            // purge contents, if any
            purgeStream(serialStream);

            // report status?
            if (log.isInfoEnabled()) {
                log.info(portName + " port opened at "
                        + activeSerialPort.getBaudRate() + " baud, sees "
                        + " DTR: " + activeSerialPort.isDTR()
                        + " RTS: " + activeSerialPort.isRTS()
                        + " DSR: " + activeSerialPort.isDSR()
                        + " CTS: " + activeSerialPort.isCTS()
                        + "  CD: " + activeSerialPort.isCD()
                );
            }

            opened = true;

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException ex) {
            log.error("Unexpected exception while opening port {}", portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // indicates OK return
    }

    /**
     * Send output bytes, e.g. characters controlling operation, with small
     * delays between the characters. This is used to reduce overrrun problems.
     */
    synchronized void sendBytes(byte[] bytes) {
        try {
            for (int i = 0; i < bytes.length - 1; i++) {
                ostream.write(bytes[i]);
                wait(3);
            }
            final byte endbyte = bytes[bytes.length - 1];
            ostream.write(endbyte);
        } catch (java.io.IOException e) {
            log.error("Exception on output: " + e);
        } catch (java.lang.InterruptedException e) {
            Thread.currentThread().interrupt(); // retain if needed later
            log.error("Interrupted output: " + e);
        }
    }

    // base class methods for the PortController interface
    @Override
    public synchronized DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialStream);
    }

    @Override
    public synchronized DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        } catch (java.io.IOException e) {
            log.error("getOutputStream exception: " + e);
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return Arrays.copyOf(validSpeedValues, validSpeedValues.length);
    }

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud115200")};
    protected int[] validSpeedValues = new int[]{115200};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    String[] validOptions1 = new String[]{Bundle.getMessage("Version1Choice"), Bundle.getMessage("Version2Choice")};

    /**
     * Set the second port option.
     */
    @Override
    public void configureOption1(String value) {
        setOptionState(option1Name, value);
        if (value.equals(validOptions1[0])) {
            version = 1;
        } else if (value.equals(validOptions1[1])) {
            version = 2;
        }
    }

    // private control members
    private boolean opened = false;
    transient DataInputStream serialStream = null;
    volatile OutputStream ostream = null;
    int[] offsetArray = null;

    @Deprecated
    static public SerialAdapter instance() {
        if (mInstance == null) {
            mInstance = new SerialAdapter();
        }
        return mInstance;
    }
    static volatile SerialAdapter mInstance = null;

    // code for handling the input characters
    Thread readerThread;

    // flag for protocol version
    int version = 1;

    // use deprecated stop method to stop thread,
    // which will be sitting waiting for input
    @SuppressWarnings("deprecation")
    void stopThread(Thread t) {
        t.stop();
    }

    @Override
    public synchronized void dispose() {
        // stop operations here. This is a deprecated method, but OK for us.
        if (readerThread != null) {
            stopThread(readerThread);
        }

        // release port
        if (activeSerialPort != null) {
            activeSerialPort.close();
        }
        serialStream = null;    // flags state
        activeSerialPort = null; // flags state
        super.dispose();
    }

    /**
     * Internal class to handle the separate character-receive thread.
     */
    class Reader implements Runnable {

        /**
         * Handle incoming characters. This is a permanent loop, looking for
         * input messages in character form on the stream connected to the
         * PortController via <code>connectPort</code>. Terminates with the
         * input stream breaking out of the try block.
         */
        @Override
        public void run() {
            // have to limit verbosity!

            while (true) {   // loop permanently, stream close will exit via exception
                try {
                    handleIncomingData();
                } catch (java.io.IOException e) {
                    log.warn("run: Exception: ", e);
                }
            }
        }

        static final int maxMsg = 200;
        StringBuffer msg;
        String msgString;

        void handleIncomingData() throws java.io.IOException {
            // we sit in this until the message is complete, relying on
            // threading to let other stuff happen

            // Create output message
            msg = new StringBuffer(maxMsg);
            // message exists, now fill it
            int i;
            for (i = 0; i < maxMsg; i++) {
                char char1;

                synchronized (SerialAdapter.this) {
                    char1 = (char) serialStream.readByte();
                }

                if (char1 == 13) {  // 13 is the CR at the end; done this
                    // way to be coding-independent
                    break;
                }
                // Strip off the CR and LF
                if (char1 != 10 && char1 != 13) {
                    msg.append(char1);
                }
            }

            // create the String to display (as String has .equals)
            msgString = msg.toString();
            log.debug("Msg <{}>", msgString);

            // return a notification via the queue to ensure end
            Runnable r = new Runnable() {

                // retain a copy of the message at startup
                String msgForLater = msgString;

                @Override
                public void run() {
                    nextLine(msgForLater);
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);
        }

    } // end class Reader

    /**
     * Handle a new line from the device.
     * <p>
     * This needs to execute on the Swing GUI thread. It forwards via the
     * Distributor object.
     *
     * @param s The new message to distribute
     */
    @InvokeOnGuiThread
    protected void nextLine(String s) {
        // check for startup lines we ignore
        if (s.length() < 5) {
            return;
        }
        if (s.startsWith("DATA,,")) {
            // skip reply to A
            setReceivers(s);
            return;
        }

        Reading r;
        try {
            r = makeReading(s);
        } catch (Exception e) {
            log.error("Exception formatting input line \"{}\": ", s, e);
            // r = new Reading(-1, new double[]{-1, -1, -1, -1} );
            // skip handling this line
            return;
        }

        if (r == null) {
            return;  // nothing useful
        }
        // forward
        try {
            Distributor.instance().submitReading(r);
        } catch (Exception e) {
            log.error("Exception forwarding reading: ", e);
        }
    }

    /**
     * Handle the message which lists the receiver numbers. Just makes an array
     * of those, which is not actually used.
     */
    void setReceivers(String s) {
        try {
            // parse string
            java.io.StringReader b = new java.io.StringReader(s);
            com.csvreader.CsvReader c = new com.csvreader.CsvReader(b);
            c.readRecord();

            // first two are "Data, ," so are ignored.
            // rest are receiver numbers
            // Find how many
            int n = c.getColumnCount() - 2;
            log.debug("Found {} receivers", n);

            // find max receiver number
            int max = Integer.valueOf(c.get(c.getColumnCount() - 1)).intValue();
            log.debug("Highest receiver address is {}", max);

            offsetArray = new int[n];
            for (int i = 0; i < n; i++) {
                offsetArray[i] = Integer.valueOf(c.get(i + 2)).intValue();
            }

        } catch (IOException e) {
            log.debug("Did not handle init message <{}> due to {}", s, e);
        }
    }

    static private int SKIPCOLS = 0; // used to skip DATA,TIME; was there a trailing "'"?
    private boolean first = true;

    /**
     * Convert input line to Reading object.
     */
    Reading makeReading(String s) throws IOException {
        if (first) {
            log.info("RPS starts, using protocol version {}", version);
            first = false;
        }

        if (version == 1) {
            // parse string
            java.io.StringReader b = new java.io.StringReader(s);
            com.csvreader.CsvReader c = new com.csvreader.CsvReader(b);
            c.readRecord();

            // values are stored in 1-N of the output array; 0 not used
            int count = c.getColumnCount() - SKIPCOLS;
            double[] vals = new double[count + 1];
            for (int i = 1; i < count + 1; i++) {
                vals[i] = Double.valueOf(c.get(i + SKIPCOLS - 1)).doubleValue();
            }

            Reading r = new Reading(Engine.instance().getPolledID(), vals, s);
            return r;
        } else if (version == 2) {
            // parse string
            java.io.StringReader b = new java.io.StringReader(s);
            com.csvreader.CsvReader c = new com.csvreader.CsvReader(b);
            c.readRecord();

            int count = (c.getColumnCount() - 2) / 2;  // skip 'ADR, DAT,'
            double[] vals = new double[Engine.instance().getMaxReceiverNumber() + 1]; // Receiver 2 goes in element 2
            for (int i = 0; i < vals.length; i++) {
                vals[i] = 0.0;
            }
            try {
                for (int i = 0; i < count; i++) {  // i is zero-based count of input pairs
                    int index = Integer.parseInt(c.get(2 + i * 2));  // index is receiver number
                    // numbers are from one for valid receivers
                    // the null message starts with index zero
                    if (index < 0) {
                        continue;
                    }
                    if (index >= vals.length) { // data for undefined Receiver
                        log.warn("Data from unexpected receiver " + index + ", creating receiver");
                        Engine.instance().setMaxReceiverNumber(index + 1);
                        //
                        // Originally, we made vals[] longer if we got
                        // a response from an unexpected receiver.
                        // This caused terrible trouble at Kesen's layout,
                        // so was commented-out here.
                        //
                        //double[] temp = new double[index+1];
                        //for (int j = 0; j<vals.length; j++) temp[j] = vals[j];
                        //vals = temp;
                    }
                    if (index < vals.length) {
                        vals[index] = Double.valueOf(c.get(2 + i * 2 + 1)).doubleValue();
                    }
                }
            } catch (IOException | NumberFormatException e) {
                log.warn("Exception handling input.", e);
                return null;
            }
            Reading r = new Reading(Engine.instance().getPolledID(), vals, s);
            return r;
        } else {
            log.error("can't handle version " + version);
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialAdapter.class);

}
