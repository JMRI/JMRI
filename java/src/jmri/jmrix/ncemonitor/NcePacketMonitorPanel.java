package jmri.jmrix.ncemonitor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.swing.NcePanelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Simple GUI for access to an NCE monitor card
 * <p>
 * When opened, the user must first select a serial port and click "Start". The
 * rest of the GUI then appears.
 *
 * @author Ken Cameron Copyright (C) 2010 derived from -
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
@SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "serialStream is access from separate thread, and this class isn't used much")
public class NcePacketMonitorPanel extends jmri.jmrix.AbstractMonPane implements NcePanelInterface {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.ncemonitor.NcePacketMonitorBundle");

    Vector<String> portNameVector = null;
    SerialPort activeSerialPort = null;
    NceSystemConnectionMemo memo = null;

    JToggleButton checkButton = new JToggleButton("Info");
    JRadioButton locoSpeedButton = new JRadioButton("Hide loco packets");
    JCheckBox truncateCheckBox = new JCheckBox("+ on");

    public NcePacketMonitorPanel() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContext(Object context) {
        if (context instanceof NceSystemConnectionMemo) {
            initComponents((NceSystemConnectionMemo) context);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.nce.analyzer.NcePacketMonitorFrame";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        StringBuilder x = new StringBuilder();
        if (memo != null) {
            x.append(memo.getUserName());
        } else {
            x.append("NCE_");
        }
        x.append(": ");
        x.append(rb.getString("Title"));
        return x.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(NceSystemConnectionMemo m) {
        this.memo = m;

        // populate the GUI, invoked as part of startup
        // load the port selection part
        portBox.setToolTipText("Select the port to use");
        portBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        Vector<String> v = getPortNames();
        for (int i = 0; i < v.size(); i++) {
            portBox.addItem(v.elementAt(i));
        }
        openPortButton.setText("Open");
        openPortButton.setToolTipText("Configure program to use selected port");
        openPortButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    openPortButtonActionPerformed(evt);
                    //} catch (jmri.jmrix.SerialConfigException ex) {
                    //    log.error("Error while opening port.  Did you select the right one?\n"+ex);
                } catch (java.lang.UnsatisfiedLinkError ex) {
                    log.error("Error while opening port.  Did you select the right one?\n" + ex);
                }
            }
        });
        {
            JSeparator js = new JSeparator();
            js.setMaximumSize(new Dimension(10000, 10));
            add(js);
        }
        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel("Serial port: "));
        p1.add(portBox);
        p1.add(openPortButton);
        p1.setMaximumSize(p1.getPreferredSize());
        add(p1);

        // add user part of GUI
        {
            JSeparator js = new JSeparator();
            js.setMaximumSize(new Dimension(10000, 10));
            add(js);
        }
        JPanel p2 = new JPanel();
        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            checkButton.setToolTipText("?");
            checkButton.setEnabled(false);
            p.add(checkButton);
            checkButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (checkButton.isSelected()) {
                        sendBytes(new byte[]{(byte) '?'});
                        checkButton.setText("Res.");
                        checkButton.setToolTipText("Resume packet monitoring");
                    } else {
                        sendBytes(new byte[]{(byte) ' '});
                        checkButton.setText("Info");
                        checkButton.setToolTipText("?");
                    }
                }
            });
            truncateCheckBox
                    .setToolTipText("Check this box to suppress identical packets");
            p.add(truncateCheckBox);
            p2.add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b = new JRadioButton("Verbose");
            b.setToolTipText("V");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'V'});
                }
            });
            b = new JRadioButton("Hex with preamble symbol");
            b.setToolTipText("H0");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '0'});
                }
            });
            p2.add(p);
            b = new JRadioButton("(as above with spaces)");
            b.setToolTipText("H1");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '1'});
                }
            });
            p2.add(p);
            b = new JRadioButton("Hex without preamble symbol");
            b.setToolTipText("H2");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '2'});
                }
            });
            p2.add(p);
            b = new JRadioButton("(as above with spaces)");
            b.setToolTipText("H3");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '3'});
                }
            });
            p2.add(p);
            b = new JRadioButton("Hex with preamble count in hex");
            b.setToolTipText("H4");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '4'});
                }
            });
            p2.add(p);
            b = new JRadioButton("(as above with spaces)");
            b.setToolTipText("H5");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '5'});
                }
            });
            p2.add(p);
        }  // end hex/verbose group

        { // start acc off/on
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b = new JRadioButton("Hide acc packets");
            b.setToolTipText("A-");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'A', (byte) '-'});
                }
            });
            b = new JRadioButton("Show acc packets");
            b.setToolTipText("A+");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'A', (byte) '+'});
                }
            });
            p2.add(p);
        }  // end acc off/on

        { // start idle off/on
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b = new JRadioButton("Hide idle packets");
            b.setToolTipText("I-");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'I', (byte) '-'});
                }
            });
            b = new JRadioButton("Show idle packets");
            b.setToolTipText("I+");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'I', (byte) '+'});
                }
            });
            p2.add(p);
        }  // end idle off/on

        { // start loco off/on
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            locoSpeedButton.setToolTipText("L-");
            g.add(locoSpeedButton);
            p.add(locoSpeedButton);
            locoSpeedButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'L', (byte) '-'});
                }
            });
            b = new JRadioButton("Show loco packets");
            b.setToolTipText("L+");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'L', (byte) '+'});
                }
            });
            p2.add(p);
        }  // end loco off/on

        { // start reset off/on
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b = new JRadioButton("Hide reset packets");
            b.setToolTipText("R-");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'R', (byte) '-'});
                }
            });
            b = new JRadioButton("Show reset packets");
            b.setToolTipText("R+");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'R', (byte) '+'});
                }
            });
            p2.add(p);
        }  // end reset off/on

        { // start signal on/off
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b = new JRadioButton("Hide signal packets");
            b.setToolTipText("S-");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'S', (byte) '-'});
                }
            });
            b = new JRadioButton("Show signal packets");
            b.setToolTipText("S+");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'S', (byte) '+'});
                }
            });
            p2.add(p);
        }  // end signal off/on

        { // Monitor command acc single/double
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            JLabel t = new JLabel("Monitor Command");
            p.add(t);
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b = new JRadioButton("Acc addresses single");
            b.setToolTipText("AS");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'A', (byte) 'S'});
                }
            });
            b = new JRadioButton("Acc addresses paired");
            b.setToolTipText("AP");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'A', (byte) 'P'});
                }
            });
            p2.add(p);
        }  // end acc single/double

        p2.setMaximumSize(p2.getPreferredSize());
        JScrollPane ps = new JScrollPane(p2);
        ps.setMaximumSize(ps.getPreferredSize());
        ps.setVisible(true);
        add(ps);
    }

    /**
     * Sends stream of bytes to the command station
     *
     * @param bytes  array of bytes to send
     */
    synchronized void sendBytes(byte[] bytes) {
        try {
            // only attempt to send data if output stream is not null (i.e. it
            // was opened successfully)
            if (ostream == null) {
                throw new IOException(
                        "Unable to send data to command station: output stream is null");
            } else {
                for (int i = 0; i < bytes.length; i++) {
                    ostream.write(bytes[i]);
                    wait(3);
                }
                final byte endbyte = 13;
                ostream.write(endbyte);
            }
        } catch (IOException e) {
            log.error("Exception on output: " + e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // retain if needed later
            log.error("Interrupted output: " + e);
        }
    }

    /**
     * Open button has been pushed, create the actual display connection
     * @param e open button event
     */
    void openPortButtonActionPerformed(java.awt.event.ActionEvent e) {
        log.info("Open button pushed");
        // can't change this anymore
        openPortButton.setEnabled(false);
        portBox.setEnabled(false);
        // Open the port
        openPort((String) portBox.getSelectedItem(), "JMRI");
        // start the reader
        readerThread = new Thread(new Reader());
        readerThread.start();
        // enable buttons
        checkButton.setEnabled(true);
        log.info("Open button processing complete");
    }

    Thread readerThread;

    protected JComboBox<String> portBox = new javax.swing.JComboBox<String>();
    protected javax.swing.JButton openPortButton = new javax.swing.JButton();

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
        serialStream = null;
        ostream = null;
        activeSerialPort = null;
        portNameVector = null;

        // and clean up parent
        super.dispose();
    }

    public Vector<String> getPortNames() {
        // first, check that the comm package can be opened and ports seen
        portNameVector = new Vector<String>();
        Enumeration<CommPortIdentifier> portIDs = CommPortIdentifier.getPortIdentifiers();
        // find the names of suitable ports
        while (portIDs.hasMoreElements()) {
            CommPortIdentifier id = portIDs.nextElement();
            // filter out line printers 
            if (id.getPortType() != CommPortIdentifier.PORT_PARALLEL) // accumulate the names in a vector
            {
                portNameVector.addElement(id.getName());
            }
        }
        return portNameVector;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="SR_NOT_CHECKED",
                                        justification="this is for skip-chars while loop: no matter how many, we're skipping")
    public synchronized String openPort(String portName, String appName) {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                handlePortBusy(p, portName);
                return "Port " + p + " in use already";
            }

            // try to set it for communication via SerialDriver
            try {
                // Doc says 7 bits, but 8 seems needed
                activeSerialPort.setSerialPortParams(38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // set RTS high, DTR high
            activeSerialPort.setRTS(true);  // not connected in some serial ports and adapters
            activeSerialPort.setDTR(true);  // pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            activeSerialPort.setFlowControlMode(0);

            // set timeout
            log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                    + " " + activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialStream = new DataInputStream(activeSerialPort.getInputStream());
            ostream = activeSerialPort.getOutputStream();

            // make less verbose
            sendBytes(new byte[]{(byte) 'L', (byte) '-', 10, 13});
            // purge contents, if any
            int count = serialStream.available();
            log.debug("input stream shows " + count + " bytes available");
            while (count > 0) {
                serialStream.skip(count);
                count = serialStream.available();
            }

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

        } catch (java.io.IOException ex) {
            log.error("IO error while opening port " + portName, ex);
            return "IO error while opening port " + portName + ": " + ex;
        } catch (UnsupportedCommOperationException ex) {
            log.error("Unsupported communications operation while opening port " + portName, ex);
            return "Unsupported communications operation while opening port " + portName + ": " + ex;
        } catch (NoSuchPortException ex) {
            log.error("No such port: " + portName, ex);
            return "No such port: " + portName + ": " + ex;
        }
        return null; // indicates OK return
    }

    void handlePortBusy(PortInUseException p, String port) {
        log.error("Port " + p + " in use, cannot open");
    }

    DataInputStream serialStream = null;
    OutputStream ostream = null;

    private final static Logger log = LoggerFactory.getLogger(NcePacketMonitorPanel.class);

    /**
     * Internal class to handle the separate character-receive thread
     *
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
                    log.warn("run: Exception: " + e.toString());
                }
            }
        }

        static final int maxMsg = 80;
        StringBuffer msg;
        StringBuffer duplicates = new StringBuffer(maxMsg);
        String msgString;
        String matchString = "";

        void handleIncomingData() throws java.io.IOException {
            // we sit in this until the message is complete, relying on
            // threading to let other stuff happen

            // Create output message
            msg = new StringBuffer(maxMsg);
            // message exists, now fill it
            int i;
            for (i = 0; i < maxMsg; i++) {
                char char1 = (char) serialStream.readByte();
                if (char1 == 13) {  // 13 is the CR at the end; done this
                    // way to be coding-independent
                    break;
                }
                msg.append(char1);
            }

            // create the String to display (as String has .equals)
            msgString = msg.toString();

            // is this a duplicate?
            if (msgString.equals(matchString) && truncateCheckBox.isSelected()) {
                // yes, suppress and represent with a '+'
                duplicates.append('+');
            } else {
                // no, message is complete, dispatch it!!

                // prepend the duplicate info
                matchString = msgString;
                if (duplicates.length() != 0) {
                    duplicates.append('\n');
                    msgString = " " + (new String(duplicates)) + (msgString);
                } else {
                    msgString = "\n" + msgString;
                }
                duplicates.setLength(0);

                // return a notification via the queue to ensure end
                Runnable r = new Runnable() {
                    // reset the duplicates

                    // retain a copy of the message at startup
                    String msgForLater = msgString;

                    @Override
                    public void run() {
                        nextLine(msgForLater, "");
                    }
                };
                javax.swing.SwingUtilities.invokeLater(r);
            }
        }

    } // end class Reader

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.nce.swing.NceNamedPaneAction {

        public Default() {
            super("Open NCE DCC Packet Analyzer",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    NcePacketMonitorPanel.class.getName(),
                    jmri.InstanceManager.getDefault(NceSystemConnectionMemo.class));
        }
    }
}
