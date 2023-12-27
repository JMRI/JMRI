package jmri.jmrix.ncemonitor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
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

//import javax.swing.JToggleButton;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.swing.NcePanelInterface;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Simple GUI for access to an NCE monitor card
 * <p>
 * When opened, the user must first select a serial port and click "Start". The
 * rest of the GUI then appears.
 *
 * @author Ken Cameron Copyright (C) 2010 derived from -
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2023
 * @author Ken Cameron Copyright (C) 2023
 */
@SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "serialStream is access from separate thread, and this class isn't used much")
public class NcePacketMonitorPanel extends jmri.jmrix.AbstractMonPane implements NcePanelInterface {
    
    Vector<String> portNameVector = null;
    SerialPort activeSerialPort = null;
    NceSystemConnectionMemo memo = null;

    protected JCheckBox dupFilterCheckBox = new JCheckBox(Bundle.getMessage("DupFilterCheckBoxLabel"));
    protected JComboBox<String> portBox = new javax.swing.JComboBox<String>();
    protected javax.swing.JButton openButton = new javax.swing.JButton(Bundle.getMessage("OpenButtonLabel"));
    protected javax.swing.JButton closePortButton = new javax.swing.JButton(Bundle.getMessage("CloseButtonLabel"));
    protected JRadioButton verboseButton = new JRadioButton(Bundle.getMessage("VerboseButtonLabel"));
    protected JRadioButton hex0Button = new JRadioButton(Bundle.getMessage("Hex0Label"));
    protected JRadioButton hex1Button = new JRadioButton(Bundle.getMessage("Hex1Label"));
    protected JRadioButton hex2Button = new JRadioButton(Bundle.getMessage("Hex2Label"));
    protected JRadioButton hex3Button = new JRadioButton(Bundle.getMessage("Hex3Label"));
    protected JRadioButton hex4Button = new JRadioButton(Bundle.getMessage("Hex4Label"));
    protected JRadioButton hex5Button = new JRadioButton(Bundle.getMessage("Hex5Label"));
    protected JRadioButton accOffButton = new JRadioButton(Bundle.getMessage("AccOffLabel"));
    protected JRadioButton accOnButton = new JRadioButton(Bundle.getMessage("AccOnLabel"));
    protected JRadioButton idleOffButton = new JRadioButton(Bundle.getMessage("IdleOffLabel"));
    protected JRadioButton idleOnButton = new JRadioButton(Bundle.getMessage("IdleOnLabel"));
    protected JRadioButton locoOffButton = new JRadioButton(Bundle.getMessage("LocoOffLabel"));
    protected JRadioButton locoOnButton = new JRadioButton(Bundle.getMessage("LocoOnLabel"));
    protected JRadioButton resetOffButton = new JRadioButton(Bundle.getMessage("ResetOffLabel"));
    protected JRadioButton resetOnButton = new JRadioButton(Bundle.getMessage("ResetOffLabel"));
    protected JRadioButton signalOffButton = new JRadioButton(Bundle.getMessage("SignalOffLabel"));
    protected JRadioButton signalOnButton = new JRadioButton(Bundle.getMessage("SignalOnLabel"));
    protected JRadioButton accSingleButton = new JRadioButton(Bundle.getMessage("AccSingleLabel"));
    protected JRadioButton accPairedButton = new JRadioButton(Bundle.getMessage("AccPairedLabel"));

    protected JComboBox<String> modelBox = new JComboBox<>();
    protected JLabel modelBoxLabel;
    private String[] validModelNames = new String[]{Bundle.getMessage("PacketAnalyzer"), Bundle.getMessage("DccMeter/Analyzer")};
    private int[] validModelValues = new int[]{0, 1};
    private int[] modelBaudRates = new int[]{38400, 115200};
    // For old model, Doc says 7 bits, but 8 seems needed, new calls for 115,200 n 8 1
    private int[] modelBitValues = new int[] {8, 8};
    private int[] modelStopValues = new int[] {SerialPort.ONE_STOP_BIT, SerialPort.ONE_STOP_BIT};
    private int[] modelParityValues = new int[] {SerialPort.NO_PARITY, SerialPort.NO_PARITY};

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
        x.append(Bundle.getMessage("Title"));
        return x.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(NceSystemConnectionMemo m) {
        this.memo = m;

        // populate the GUI, invoked as part of startup
        enableDisableWhenOpen(false);
        // load the port selection part
        portBox.setToolTipText(Bundle.getMessage("PortBoxToolTip"));
        portBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        Vector<String> v = getPortNames();
        for (int i = 0; i < v.size(); i++) {
            portBox.addItem(v.elementAt(i));
        }
        // offer model choice
        modelBox.setToolTipText(Bundle.getMessage("ModelBoxToolTip"));
        modelBox.setAlignmentX(LEFT_ALIGNMENT);
        for (int i = 0; i < validModelNames.length; i++) {
            modelBox.addItem(validModelNames[i]);
        }
        openButton.setToolTipText(Bundle.getMessage("OpenButtonToolTip"));
        openButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    openPortButtonActionPerformed(evt);
                } catch (java.lang.UnsatisfiedLinkError ex) {
                    log.error("Error while opening port.  Did you select the right one?", ex);
                }
            }
        });
        closePortButton.setToolTipText(Bundle.getMessage("CloseButtonToolTip"));
        closePortButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    closePortButtonActionPerformed();
                } catch (java.lang.UnsatisfiedLinkError ex) {
                    log.error("Error while closing port.  Did you select the right one?", ex);
                }
            }
        });
        {
            JSeparator js = new JSeparator();
            js.setMaximumSize(new Dimension(10000, 10));
            add(js);
        }
        {
            JPanel p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel(Bundle.getMessage("SerialPortLabel")));
            p1.add(portBox);
            p1.add(new JLabel(Bundle.getMessage("ModelBoxLabel")));
            p1.add(modelBox);
            p1.add(openButton);
            p1.add(closePortButton);
            //p1.setMaximumSize(p1.getPreferredSize());
            add(p1);
        }

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
            dupFilterCheckBox.setToolTipText(Bundle.getMessage("DupFilterCheckBoxToolTip"));
            p.add(dupFilterCheckBox);
            p2.add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            verboseButton.setToolTipText(Bundle.getMessage("VerboseButtonToolTip"));
            g.add(verboseButton);
            p.add(verboseButton);
            verboseButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'V'});
                }
            });
            //hex0Button.setToolTipText(Bundle.getMessage("Hex0ButtonToolTip"));
            g.add(hex0Button);
            p.add(hex0Button);
            hex0Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '0'});
                }
            });
            p2.add(p);
            //hex1Button.setToolTipText(Bundle.getMessage("Hex1ButtonToolTip"));
            g.add(hex1Button);
            p.add(hex1Button);
            hex1Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '1'});
                }
            });
            p2.add(p);
            //hex2Button.setToolTipText(Bundle.getMessage("Hex2ButtonToolTip"));
            g.add(hex2Button);
            p.add(hex2Button);
            hex2Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '2'});
                }
            });
            p2.add(p);
            //hex3Button.setToolTipText(Bundle.getMessage("Hex3ButtonToolTip"));
            g.add(hex3Button);
            p.add(hex3Button);
            hex3Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '3'});
                }
            });
            p2.add(p);
            //hex4Button.setToolTipText(Bundle.getMessage("Hex4ButtonToolTip"));
            g.add(hex4Button);
            p.add(hex4Button);
            hex4Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '4'});
                }
            });
            p2.add(p);
            //hex5Button.setToolTipText(Bundle.getMessage("Hex5ButtonToolTip"));
            g.add(hex5Button);
            p.add(hex5Button);
            hex5Button.addActionListener(new java.awt.event.ActionListener() {
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
            //accOffButton.setToolTipText(Bundle.getMessage("AccOffButtonToolTip"));
            g.add(accOffButton);
            p.add(accOffButton);
            accOffButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'A', (byte) '-'});
                }
            });
            //accOnButton.setToolTipText(Bundle.getMessage("AccOnButtonToolTip"));
            g.add(accOnButton);
            p.add(accOnButton);
            accOnButton.addActionListener(new java.awt.event.ActionListener() {
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
            //idleOffButton.setToolTipText(Bundle.getMessage("IdleOffButtonToolTip"));
            g.add(idleOffButton);
            p.add(idleOffButton);
            idleOffButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'I', (byte) '-'});
                }
            });
            //idleOnButton.setToolTipText(Bundle.getMessage("IdleOnButtonToolTip"));
            g.add(idleOnButton);
            p.add(idleOnButton);
            idleOnButton.addActionListener(new java.awt.event.ActionListener() {
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
            //locoOffButton.setToolTipText(Bundle.getMessage("LocoOffButtonToolTip"));
            g.add(locoOffButton);
            p.add(locoOffButton);
            locoOffButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'L', (byte) '-'});
                }
            });
            //locoOnButton.setToolTipText(Bundle.getMessage("LocoOnButtonToolTip"));
            g.add(locoOnButton);
            p.add(locoOnButton);
            locoOnButton.addActionListener(new java.awt.event.ActionListener() {
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
            //resetOffButton.setToolTipText(Bundle.getMessage("ResetOffButtonToolTip"));
            g.add(resetOffButton);
            p.add(resetOffButton);
            resetOffButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'R', (byte) '-'});
                }
            });
            //resetOnButton.setToolTipText(Bundle.getMessage("ResetOnButtonToolTip"));
            g.add(resetOnButton);
            p.add(resetOnButton);
            resetOnButton.addActionListener(new java.awt.event.ActionListener() {
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
            //signalOffButton.setToolTipText(Bundle.getMessage("SignalOffButtonToolTip"));
            g.add(signalOffButton);
            p.add(signalOffButton);
            signalOffButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'S', (byte) '-'});
                }
            });
            //signalOnButton.setToolTipText(Bundle.getMessage("SignalOnButtonToolTip"));
            g.add(signalOnButton);
            p.add(signalOnButton);
            signalOnButton.addActionListener(new java.awt.event.ActionListener() {
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
            //accSingleButton.setToolTipText(Bundle.getMessage("AccSingleButtonToolTip"));
            g.add(accSingleButton);
            p.add(accSingleButton);
            accSingleButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'A', (byte) 'S'});
                }
            });
            //accPairedButton.setToolTipText(Bundle.getMessage("AccPairedButtonToolTip"));
            g.add(accPairedButton);
            p.add(accPairedButton);
            accPairedButton.addActionListener(new java.awt.event.ActionListener() {
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
            log.error("Exception on output", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // retain if needed later
            log.error("Interrupted output", e);
        }
    }
    
    /**
     * Enable/Disable options depending on port open/closed status
     * @param isOpen enables/disables buttons/checkbox when connection is open/closed
     */
    void enableDisableWhenOpen(boolean isOpen) {
        openButton.setEnabled(!isOpen);
        closePortButton.setEnabled(isOpen);
        portBox.setEnabled(!isOpen);
        modelBox.setEnabled(!isOpen);
        verboseButton.setEnabled(isOpen);
        hex0Button.setEnabled(isOpen);
        hex1Button.setEnabled(isOpen);
        hex2Button.setEnabled(isOpen);
        hex3Button.setEnabled(isOpen);
        hex4Button.setEnabled(isOpen);
        hex5Button.setEnabled(isOpen);
        accOffButton.setEnabled(isOpen);
        accOnButton.setEnabled(isOpen);
        idleOffButton.setEnabled(isOpen);
        idleOnButton.setEnabled(isOpen);
        locoOffButton.setEnabled(isOpen);
        locoOnButton.setEnabled(isOpen);
        resetOffButton.setEnabled(isOpen);
        resetOnButton.setEnabled(isOpen);
        signalOffButton.setEnabled(isOpen);
        signalOnButton.setEnabled(isOpen);
        accSingleButton.setEnabled(isOpen);
        accPairedButton.setEnabled(isOpen);
    }

    /**
     * Open button has been pushed, create the actual display connection
     * @param e open button event
     */
    void openPortButtonActionPerformed(java.awt.event.ActionEvent e) {
        //log.info("Open button pushed");
        // can't change this anymore
        String openStatus = openPort((String) portBox.getSelectedItem(), validModelValues[modelBox.getSelectedIndex()], "JMRI");
        if (openStatus != null) {
            log.debug("Open Returned: {} ", openStatus);
            return;
        }
        // start the reader
        readerThread = new Thread(new Reader());
        readerThread.start();
        readerThread.setName("NCE Packet Monitor");
        // enable buttons
        enableDisableWhenOpen(true);
        //log.info("Open button processing complete");
    }

    /**
     * Open button has been pushed, create the actual display connection
     */
    void closePortButtonActionPerformed() {
        //log.info("Close button pushed");
        if (readerThread != null) {
            stopThread(readerThread);
        }

        // release port
        if (activeSerialPort != null) {
            activeSerialPort.closePort();
            log.info("{} port closed", portBox.getSelectedItem());
        }
        serialStream = null;
        ostream = null;
        activeSerialPort = null;
        portNameVector = null;
        // enable buttons
        enableDisableWhenOpen(false);
    }
    
    Thread readerThread;

    /*
     * tell the reader thread to close down
     */
    void stopThread(Thread t) {
        t.interrupt();
    }

    @Override
    public synchronized void dispose() {
        // stop operations here. This is a deprecated method, but OK for us.
        closePortButtonActionPerformed();

        // and clean up parent
        super.dispose();
    }

    public Vector<String> getPortNames() {
        // first, check that the comm package can be opened and ports seen
        portNameVector = new Vector<String>();

        SerialPort[] portIDs = SerialPort.getCommPorts();
                // find the names of suitable ports
        for (SerialPort portID : portIDs) {
            portNameVector.addElement(portID.getSystemPortName());
        }
        return portNameVector;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="SR_NOT_CHECKED",
                                        justification="this is for skip-chars while loop: no matter how many, we're skipping")
    public synchronized String openPort(String portName, int modelValue, String appName) {
        // open the port, check ability to set moderators

        // get and open the primary port
        activeSerialPort = com.fazecast.jSerialComm.SerialPort.getCommPort(portName);
        activeSerialPort.openPort();
        
        // set it for communication
        activeSerialPort.setNumDataBits(modelBitValues[modelValue]);
        activeSerialPort.setNumStopBits(modelStopValues[modelValue]);
        activeSerialPort.setParity(modelParityValues[modelValue]);
        activeSerialPort.setBaudRate(modelBaudRates[modelValue]);
        
        // set RTS high, DTR high
        activeSerialPort.setRTS(); // not connected in some serial ports and adapters
        activeSerialPort.setDTR(); // pin 1 in DIN8; on main connector, this is DTR

        // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
        activeSerialPort.setFlowControl(com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_DISABLED);

        // set timeout
        activeSerialPort.setComPortTimeouts(com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

        // get and save stream
        serialStream = new DataInputStream(activeSerialPort.getInputStream());
        ostream = activeSerialPort.getOutputStream();

        // purge contents, if any
        try {
            int count = serialStream.available();
            log.debug("input stream shows {} bytes available", count);
            while (count > 0) {
                serialStream.skip(count);
                count = serialStream.available();
            }
        } catch (IOException e) {
            log.error("problem purging port at startup", e);
        }

        // report status?
        if (log.isInfoEnabled()) {
            log.info("Port {} {} opened at {} baud, sees DTR: {} RTS: {} DSR: {} CTS: {} DCD: {}", 
                    portName, activeSerialPort.getDescriptivePortName(),
                    activeSerialPort.getBaudRate(), activeSerialPort.getDTR(), 
                    activeSerialPort.getRTS(), activeSerialPort.getDSR(), activeSerialPort.getCTS(),
                    activeSerialPort.getDCD());
        }

        return null; // indicates OK return
    }

    DataInputStream serialStream = null;
    OutputStream ostream = null;

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
                } catch (java.io.EOFException e) {
                    log.info(Thread.currentThread().getName() + " thread ending, port closed");
                    return;
                } catch (java.io.IOException e) {
                    log.warn(Thread.currentThread().getName() + " thread ending: Exception: {}", e.toString());
                    return;
                }
            }
        }

        static final int maxMsg = 80;
        StringBuffer msg;
        private int duplicates = 0;
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
            if (msgString.equals(matchString) && dupFilterCheckBox.isSelected()) {
                // yes, keep count
                duplicates++;
            } else {
                // no, message is complete, dispatch it!!
                if (!msgString.equals(matchString) && dupFilterCheckBox.isSelected() && (duplicates > 0)) {
                    // prepend the duplicate info
                    String dupString = matchString + " [" + duplicates + "]\n";
                    // return a notification via the queue to ensure end
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            nextLine(dupString, "");
                        }
                    };
                    javax.swing.SwingUtilities.invokeLater(r);
                }
                duplicates = 0;
                matchString = msgString;
                msgString = msgString + "\n";
                // return a notification via the queue to ensure end
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        nextLine(msgString, "");
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NcePacketMonitorPanel.class);
}
