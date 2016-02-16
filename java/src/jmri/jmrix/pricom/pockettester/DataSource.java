// pricom.pockettester.DataSource.java
package jmri.jmrix.pricom.pockettester;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import java.awt.FlowLayout;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple GUI for controlling the PRICOM Pocket Tester.
 * <P>
 * When opened, the user must first select a serial port and click "Start". The
 * rest of the GUI then appears.
 * <P>
 * For more info on the product, see http://www.pricom.com
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 * @version	$Revision$
 */
public class DataSource extends jmri.util.JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = -6253194020952470551L;
    static DataSource existingInstance;

    /**
     * Provide access to a defined DataSource object.
     * <p>
     * Note that this can be used to get the DataSource object once it's been
     * created, even if it's not connected to the hardware yet.
     *
     * @return null until a DataSource has been created.
     */
    static public DataSource instance() {
        return existingInstance;
    }

    void setInstance(DataSource source) {
        if (existingInstance != null) {
            log.error("Setting instance after it has already been set");
        } else {
            existingInstance = source;
        }
    }

    Vector<String> portNameVector = null;
    SerialPort activeSerialPort = null;
    static java.util.ResourceBundle rb
            = java.util.ResourceBundle.getBundle("jmri.jmrix.pricom.pockettester.TesterBundle");

    JLabel version = new JLabel("");  // hold version label when returned

    /**
     * Populate the GUI.
     *
     * @since 1.7.7
     */
    public void initComponents() {
        setTitle(rb.getString("TitleSource"));

        // set layout manager
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // load the port selection part
        portBox.setToolTipText(rb.getString("TooltipSelectPort"));
        portBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        Vector<String> v = getPortNames();
        for (int i = 0; i < v.size(); i++) {
            portBox.addItem(v.elementAt(i));
        }
        speedBox.setToolTipText(rb.getString("TooltipSelectBaud"));
        speedBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        speedBox.setSelectedItem("115200");
        openPortButton.setText(rb.getString("ButtonOpen"));
        openPortButton.setToolTipText(rb.getString("TooltipOpen"));
        openPortButton.addActionListener(new java.awt.event.ActionListener() {
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
        getContentPane().add(new JSeparator());
        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel(rb.getString("LabelSerialPort")));
        p1.add(portBox);
        p1.add(new JLabel(rb.getString("LabelSpeed")));
        p1.add(speedBox);
        p1.add(openPortButton);
        getContentPane().add(p1);

        setInstance(this);  // not done until init is basically complete

        // Done, get ready to display
        pack();
    }

    void addUserGui() {
        // add user part of GUI
        getContentPane().add(new JSeparator());
        JPanel p2 = new JPanel();
        p2.add(checkButton);
        checkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendBytes(new byte[]{(byte) 'G'});
                sendBytes(new byte[]{(byte) 'F'});
            }
        });

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b = new JRadioButton(rb.getString("ButtonShowAll"));
            g.add(b);
            p.add(b);
            b.setSelected(true);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'F'});
                }
            });
            b = new JRadioButton(rb.getString("ButtonShowAcc"));
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'A'});
                }
            });
            p2.add(p);
            b = new JRadioButton(rb.getString("ButtonShowMobile"));
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte) 'M'});
                }
            });
            p2.add(p);
        }  // end group controlling filtering

        {
            JButton b = new JButton(rb.getString("ButtonGetVersion"));
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    version.setText(rb.getString("LabelWaitVersion"));
                    sendBytes(new byte[]{(byte) 'V'});
                }
            });
            p2.add(b);
        }

        getContentPane().add(p2);

        // space for version string
        version = new JLabel(rb.getString("LabelNoVersion"));  // hold version label when returned
        JPanel p3 = new JPanel();
        p3.add(version);
        getContentPane().add(p3);

        getContentPane().add(new JSeparator());

        JPanel p4 = new JPanel();
        p4.setLayout(new BoxLayout(p4, BoxLayout.X_AXIS));
        p4.add(new JLabel(rb.getString("LabelToOpen")));

        {
            MonitorAction a = new MonitorAction() {
                /**
                 *
                 */
                private static final long serialVersionUID = -1633077392080446971L;

                public void connect(DataListener l) {
                    DataSource.this.addListener(l);
                }
            };
            JButton b = new JButton((String) a.getValue(Action.NAME));
            b.addActionListener(a);
            p4.add(b);
        }

        {
            PacketTableAction p = new PacketTableAction() {
                /**
                 *
                 */
                private static final long serialVersionUID = 6577986261540429898L;

                public void connect(DataListener l) {
                    DataSource.this.addListener(l);
                    ((PacketTableFrame) l).setSource(DataSource.this);
                }
            };
            JButton b = new JButton((String) p.getValue(Action.NAME));
            b.addActionListener(p);
            p4.add(b);
        }

        {
            StatusAction a = new StatusAction() {
                /**
                 *
                 */
                private static final long serialVersionUID = -8975013830132142941L;

                public void connect(StatusFrame l) {
                    DataSource.this.addListener(l);
                    l.setSource(DataSource.this);
                }
            };
            JButton b = new JButton((String) a.getValue(Action.NAME));
            b.addActionListener(a);
            p4.add(b);
            getContentPane().add(p4);
        }

        // Done, get ready to display
        pack();
    }

    JButton checkButton = new JButton(rb.getString("ButtonInit"));

    /**
     * Send output bytes, e.g. characters controlling operation, to the tester
     * with small delays between the characters. This is used to reduce overrrun
     * problems.
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

    /**
     * Open button has been pushed, create the actual display connection
     */
    void openPortButtonActionPerformed(java.awt.event.ActionEvent e) {
        log.info("Open button pushed");
        // can't change this anymore
        openPortButton.setEnabled(false);
        portBox.setEnabled(false);
        speedBox.setEnabled(false);
        // Open the port
        openPort((String) portBox.getSelectedItem(), "JMRI");
        // start the reader
        readerThread = new Thread(new Reader());
        readerThread.start();
        log.info("Open button processing complete");
        addUserGui();
    }

    Thread readerThread;

    protected javax.swing.JComboBox<String> portBox = new javax.swing.JComboBox<String>();
    protected javax.swing.JComboBox<String> speedBox
            = new javax.swing.JComboBox<String>(new String[]{"9600", "19200", "38400", "57600", "115200"});
    protected javax.swing.JButton openPortButton = new javax.swing.JButton();

    @SuppressWarnings("deprecation")
    public void dispose() {
        // stop operations here. This is a deprecated method, but OK for us.
        if (readerThread != null) {
            readerThread.stop();
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

    @SuppressWarnings("unchecked")
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

    public String openPort(String portName, String appName) {
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
                // get selected speed
                int speed = 115200;
                speed = Integer.parseInt((String) speedBox.getSelectedItem());
                // 8-bits, 1-stop, no parity
                activeSerialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (gnu.io.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // NO hardware handshaking, but for consistancy, set the Modem Control Lines
            // set RTS high, DTR high
            activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
            activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; None is needed or used
            activeSerialPort.setFlowControlMode(0);

            // set timeout
            log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                    + " " + activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialStream = new DataInputStream(activeSerialPort.getInputStream());
            ostream = activeSerialPort.getOutputStream();

            // start the DUMP
            sendBytes(new byte[]{(byte) 'g'});
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
            log.error("Unexpected I/O exception while opening port " + portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        } catch (gnu.io.NoSuchPortException ex) {
            log.error("No such port while opening port " + portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        } catch (gnu.io.UnsupportedCommOperationException ex) {
            log.error("Unexpected comm exception while opening port " + portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        }
        return null; // indicates OK return
    }

    void handlePortBusy(gnu.io.PortInUseException p, String port) {
        log.error("Port " + p + " in use, cannot open");
    }

    DataInputStream serialStream = null;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "IS2_INCONSISTENT_SYNC",
            justification = "Class is no longer active, no hardware with which to test fix")
    OutputStream ostream = null;

    private final static Logger log = LoggerFactory.getLogger(DataSource.class.getName());

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
                char char1 = (char) serialStream.readByte();
                if (char1 == 10) {  // 10 is the LF at the end; done this
                    // way to be coding-independent
                    break;
                }
                // Strip off the CR and LF
                if (char1 != 10 && char1 != 13) {
                    msg.append(char1);
                }
            }

            // create the String to display (as String has .equals)
            msg.append("\n");
            msgString = msg.toString();

            // return a notification via the queue to ensure end
            Runnable r = new Runnable() {

                // retain a copy of the message at startup
                String msgForLater = msgString;

                public void run() {
                    nextLine(msgForLater);
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);
        }

    } // end class Reader

    // data members to hold contact with the listeners
    final private Vector<DataListener> listeners = new Vector<DataListener>();

    public synchronized void addListener(DataListener l) {
        // add only if not already registered
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
    }

    public synchronized void removeListener(DataListener l) {
        if (listeners.contains(l)) {
            listeners.removeElement(l);
        }
    }

    /**
     * Handle a new line from the device.
     * <UL>
     * <LI>Check for version number and display
     * <LI>Trigger the notification of all listeners.
     * </UL>
     * <P>
     * This needs to execute on the Swing GUI thread.
     *
     * @param s The new message to distribute
     */
    protected void nextLine(String s) {
        // Check for version string
        if (s.startsWith("PRICOM Design DCC")) {
            // save as version string & suppress
            version.setText(s);
            return;
        }
        // Distribute the result
        // make a copy of the listener vector so synchronized not needed for transmit
        Vector<DataListener> v;
        synchronized (this) {
            v = new Vector<DataListener>(listeners);
        }
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            DataListener client = v.elementAt(i);
            client.asciiFormattedMessage(s);
        }
    }

}
