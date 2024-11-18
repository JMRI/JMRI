package jmri.jmrix.ncemonitor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.*;
import java.util.Vector;

import javax.swing.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.jmrix.AbstractSerialPortController;
import jmri.jmrix.SerialPort;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.swing.NcePanelInterface;

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
    protected JRadioButton origHex0Button = new JRadioButton(Bundle.getMessage("OrigHex0Label"));
    protected JRadioButton origHex1Button = new JRadioButton(Bundle.getMessage("OrigHex1Label"));
    protected JRadioButton origHex2Button = new JRadioButton(Bundle.getMessage("OrigHex2Label"));
    protected JRadioButton origHex3Button = new JRadioButton(Bundle.getMessage("OrigHex3Label"));
    protected JRadioButton origHex4Button = new JRadioButton(Bundle.getMessage("OrigHex4Label"));
    protected JRadioButton origHex5Button = new JRadioButton(Bundle.getMessage("OrigHex5Label"));
    protected JRadioButton newHex0Button = new JRadioButton(Bundle.getMessage("NewHex0Label"));
    protected JRadioButton newHex1Button = new JRadioButton(Bundle.getMessage("NewHex1Label"));
    protected JRadioButton accOnButton = new JRadioButton(Bundle.getMessage("AccOnLabel"));
    protected JRadioButton idleOnButton = new JRadioButton(Bundle.getMessage("IdleOnLabel"));
    protected JRadioButton locoOnButton = new JRadioButton(Bundle.getMessage("LocoOnLabel"));
    protected JRadioButton resetOnButton = new JRadioButton(Bundle.getMessage("ResetOnLabel"));
    protected JRadioButton signalOnButton = new JRadioButton(Bundle.getMessage("SignalOnLabel"));
    protected JRadioButton accSingleButton = new JRadioButton(Bundle.getMessage("AccSingleLabel"));
    protected JRadioButton accPairedButton = new JRadioButton(Bundle.getMessage("AccPairedLabel"));

    protected JComboBox<String> modelBox = new JComboBox<>();
    protected JLabel modelBoxLabel;
    private String[] validModelNames = new String[]{Bundle.getMessage("PacketAnalyzer"), Bundle.getMessage("DccMeter/Analyzer")};
    private final static int MODELORIG = 0;
    private final static int MODELNEW = 1;
    private int[] validModelValues = new int[]{MODELORIG, MODELNEW};
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
     * The minimum frame size for font size 16
     */
    @Override
    public Dimension getMinimumDimension() {
        return new Dimension(500, 500);
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
                    log.error("Error while opening port.  Did you select the right one?\nException: ", ex);
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
                    log.error("Error while closing port.  Did you select the right one?\\nException: ", ex);
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
        JPanel p2A = new JPanel();
        p2A.setLayout(new BoxLayout(p2A, BoxLayout.Y_AXIS));
        JPanel p2B = new JPanel();
        JPanel p2C = new JPanel();
        JPanel p2D = new JPanel();
        ButtonGroup gD = new ButtonGroup();
        {   // begin dup group
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            dupFilterCheckBox.setToolTipText(Bundle.getMessage("DupFilterCheckBoxToolTip"));
            p.add(dupFilterCheckBox);
            p2.add(p);
        }   // end dup group

        {   // begin verbose group
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            verboseButton.setToolTipText(Bundle.getMessage("VerboseButtonToolTip"));
            gD.add(verboseButton);
            p.add(verboseButton);
            verboseButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'V'});
                }
            });
            p2A.add(p);
        }   // end verbose group

        {   // begin old hex group
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            origHex0Button.setToolTipText(Bundle.getMessage("OrigHex0ButtonToolTip"));
            gD.add(origHex0Button);
            p.add(origHex0Button);
            origHex0Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '0'});
                }
            });
            p2B.add(p);
            origHex1Button.setToolTipText(Bundle.getMessage("OrigHex1ButtonToolTip"));
            gD.add(origHex1Button);
            p.add(origHex1Button);
            origHex1Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '1'});
                }
            });
            p2B.add(p);
            origHex2Button.setToolTipText(Bundle.getMessage("OrigHex2ButtonToolTip"));
            gD.add(origHex2Button);
            p.add(origHex2Button);
            origHex2Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '2'});
                }
            });
            p2.add(p);
            origHex3Button.setToolTipText(Bundle.getMessage("OrigHex3ButtonToolTip"));
            gD.add(origHex3Button);
            p.add(origHex3Button);
            origHex3Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '3'});
                }
            });
            p2B.add(p);
            origHex4Button.setToolTipText(Bundle.getMessage("OrigHex4ButtonToolTip"));
            gD.add(origHex4Button);
            p.add(origHex4Button);
            origHex4Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '4'});
                }
            });
            p2.add(p);
            origHex5Button.setToolTipText(Bundle.getMessage("OrigHex5ButtonToolTip"));
            gD.add(origHex5Button);
            p.add(origHex5Button);
            origHex5Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '5'});
                }
            });
            p2B.add(p);
        }  // end old hex group

        {   // begin new hex group
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            newHex0Button.setToolTipText(Bundle.getMessage("NewHex0ButtonToolTip"));
            gD.add(newHex0Button);
            p.add(newHex0Button);
            newHex0Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '0'});
                }
            });
            p2C.add(p);
            newHex1Button.setToolTipText(Bundle.getMessage("NewHex1ButtonToolTip"));
            gD.add(newHex1Button);
            p.add(newHex1Button);
            newHex1Button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'H', (byte) '1'});
                }
            });
            p2C.add(p);
        }  // end new hex group
        p2D.setLayout(new BoxLayout(p2D, BoxLayout.X_AXIS));
        p2D.add(p2B);
        p2D.add(p2C);
        p2A.add(p2D);
        p2.add(p2A);

        { // start on
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            accOnButton.setToolTipText(Bundle.getMessage("AccOnButtonToolTip"));
            p.add(accOnButton);
            accOnButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (accOnButton.isSelected()) {
                        sendBytes(new byte[]{(byte) 'A', (byte) '+'});
                    } else {
                        sendBytes(new byte[]{(byte) 'A', (byte) '-'});
                    }
                }
            });
            idleOnButton.setToolTipText(Bundle.getMessage("IdleOnButtonToolTip"));
            p.add(idleOnButton);
            idleOnButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (idleOnButton.isSelected()) {
                        sendBytes(new byte[]{(byte) 'I', (byte) '+'});
                    } else {
                        sendBytes(new byte[]{(byte) 'I', (byte) '-'});
                    }
                }
            });
            locoOnButton.setToolTipText(Bundle.getMessage("LocoOnButtonToolTip"));
            p.add(locoOnButton);
            locoOnButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (locoOnButton.isSelected()) {
                        sendBytes(new byte[]{(byte) 'L', (byte) '+'});
                    } else {
                        sendBytes(new byte[]{(byte) 'L', (byte) '-'});
                    }
                }
            });
            resetOnButton.setToolTipText(Bundle.getMessage("ResetOnButtonToolTip"));
            p.add(resetOnButton);
            resetOnButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (resetOnButton.isSelected()) {
                        sendBytes(new byte[]{(byte) 'R', (byte) '+'});
                    } else {
                        sendBytes(new byte[]{(byte) 'R', (byte) '-'});
                    }
                }
            });
            signalOnButton.setToolTipText(Bundle.getMessage("SignalOnButtonToolTip"));
            p.add(signalOnButton);
            signalOnButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (signalOnButton.isSelected()) {
                        sendBytes(new byte[]{(byte) 'S', (byte) '+'});
                    } else {
                        sendBytes(new byte[]{(byte) 'S', (byte) '-'});
                    }
                }
            });
            p2.add(p);
        }  // end on

        { // Monitor command acc single/double
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            JLabel t = new JLabel(Bundle.getMessage("MonitorCmdLabel"));
            p.add(t);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup gA = new ButtonGroup();
            accSingleButton.setToolTipText(Bundle.getMessage("AccSingleButtonToolTip"));
            gA.add(accSingleButton);
            p.add(accSingleButton);
            accSingleButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'A', (byte) 'S'});
                }
            });
            accPairedButton.setToolTipText(Bundle.getMessage("AccPairedButtonToolTip"));
            gA.add(accPairedButton);
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
            log.error("Exception on output: ", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // retain if needed later
            log.error("Interrupted output: ", e);
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
        if (!isOpen || (modelBox.getSelectedIndex() == MODELORIG)) {
            origHex0Button.setEnabled(isOpen);
            origHex1Button.setEnabled(isOpen);
            origHex2Button.setEnabled(isOpen);
            origHex3Button.setEnabled(isOpen);
            origHex4Button.setEnabled(isOpen);
            origHex5Button.setEnabled(isOpen);
        }
        if (!isOpen || (modelBox.getSelectedIndex() == MODELNEW)) {
            newHex0Button.setEnabled(isOpen);
            newHex1Button.setEnabled(isOpen);
        }
        accOnButton.setEnabled(isOpen);
        idleOnButton.setEnabled(isOpen);
        locoOnButton.setEnabled(isOpen);
        resetOnButton.setEnabled(isOpen);
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
        String portName = (String) portBox.getSelectedItem();
        int modelValue = validModelValues[modelBox.getSelectedIndex()];
        int numDataBits = modelBitValues[modelValue];
        int numStopBits = modelStopValues[modelValue];
        int parity = modelParityValues[modelValue];
        int baudrate = modelBaudRates[modelValue];
        activeSerialPort = AbstractSerialPortController.activatePort(
                null, portName, log, numStopBits, SerialPort.Parity.getParity(parity));

        activeSerialPort.setNumDataBits(numDataBits);
        activeSerialPort.setBaudRate(baudrate);

        // set RTS high, DTR high
        activeSerialPort.setRTS(); // not connected in some serial ports and adapters
        activeSerialPort.setDTR(); // pin 1 in DIN8; on main connector, this is DTR

        // get and save stream
        serialStream = new DataInputStream(activeSerialPort.getInputStream());
        ostream = activeSerialPort.getOutputStream();

        // report status?
        if (log.isInfoEnabled()) {
            log.info("Port {} {} opened at {} baud, sees DTR: {} RTS: {} DSR: {} CTS: {} DCD: {}",
                    portName, activeSerialPort.getDescriptivePortName(),
                    activeSerialPort.getBaudRate(), activeSerialPort.getDTR(),
                    activeSerialPort.getRTS(), activeSerialPort.getDSR(), activeSerialPort.getCTS(),
                    activeSerialPort.getDCD());
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
        return jmri.jmrix.AbstractSerialPortController.getActualPortNames();
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
                    log.info("{} thread ending, port closed", Thread.currentThread().getName());
                    return;
                } catch (java.io.IOException e) {
                    log.warn("{} thread ending: Exception: {}", Thread.currentThread().getName(), e.toString());
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
