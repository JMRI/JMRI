// pricom.pockettester.MonitorFrame.java

package jmri.jmrix.pricom.pockettester;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.InputStream;

/**
 * Simple GUI for access to PRICOM Pocket Monitor
 * <P>
 * When opened, the user must first select a serial port and click "Start".
 * The rest of the GUI then appears.
 * <P>
 * For more info on the product, see http://www.pricom.com
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.4 $
 */
public class MonitorFrame extends jmri.jmrix.AbstractMonFrame {

    Vector portNameVector = null;
    SerialPort activeSerialPort = null;

    // populate the GUI, invoked as part of startup
    protected void init() {
        // load the port selection part
        portBox.setToolTipText("Select the port to use");
        portBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        Vector v = getPortNames();
        for (int i=0; i<v.size(); i++)
            portBox.addItem(v.elementAt(i));
        speedBox.setToolTipText("Select baud rate configured into the Pocket Tester");
        speedBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        speedBox.setSelectedItem("115200");
        openPortButton.setText("Open");
        openPortButton.setToolTipText("Configure program to use selected port");
        openPortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    openPortButtonActionPerformed(evt);
                } catch (jmri.jmrix.SerialConfigException ex) {
                    log.error("Error while opening port.  Did you select the right one?\n"+ex);
                }
                catch (java.lang.UnsatisfiedLinkError ex) {
                    log.error("Error while opening port.  Did you select the right one?\n"+ex);
                    }
                }
            });
        getContentPane().add(new JSeparator());
        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel("Serial port: "));
        p1.add(portBox);
        p1.add(new JLabel("Speed: "));
        p1.add(speedBox);
        p1.add(openPortButton);
        getContentPane().add(p1);

        // add user part of GUI
        getContentPane().add(new JSeparator());
        JPanel p2 = new JPanel();
        p2.add(checkButton);
        checkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendBytes(new byte[]{(byte)'G'});
                sendBytes(new byte[]{(byte)'F'});
            }
        });

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b= new JRadioButton("Show all");
            g.add(b);
            p.add(b);
            b.setSelected(true);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'F'});
                }
            });
            b= new JRadioButton("Only show accessory decoder packets");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'A'});
                }
            });
            p2.add(p);
            b= new JRadioButton("Only show mobile decoder packets");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'M'});
                }
            });
            p2.add(p);
        }  // end group controlling filtering

        {
            JButton b = new JButton("Get version");
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'V'});
                }
            });
            p2.add(b);
        }

        getContentPane().add(p2);
    }

    JButton checkButton = new JButton("Init");

    protected String title() {
        return "PRICOM Pocket Tester";
    }

    /**
     * Send output bytes, e.g. characters controlling operation, to the
     * tester with small delays between the characters.  This is 
     * used to reduce overrrun problems.
     */
    synchronized void sendBytes(byte[] bytes) {
        try {
            for (int i=0; i<bytes.length-1; i++) {
                ostream.write(bytes[i]);
                wait(3);
            }
            final byte endbyte = bytes[bytes.length-1];
            ostream.write(endbyte);
        } catch (java.io.IOException e) {
            log.error("Exception on output: "+e);
        } catch (java.lang.InterruptedException e) {
            log.error("Interrupted output: "+e);
        }
    }

    /**
     * Open button has been pushed, create the actual display connection
     */
    void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
        log.info("Open button pushed");
        // can't change this anymore
        openPortButton.setEnabled(false);
        portBox.setEnabled(false);
        speedBox.setEnabled(false);
        // Open the port
        openPort((String)portBox.getSelectedItem(), "JMRI");
        // start the reader
        readerThread = new Thread(new Reader());
        readerThread.start();
        log.info("Open button processing complete");
    }

    Thread readerThread;

    protected javax.swing.JComboBox portBox = new javax.swing.JComboBox();
    protected javax.swing.JComboBox speedBox 
            = new javax.swing.JComboBox(new String[]{"9600", "19200", "38400", "57600", "115200"});
    protected javax.swing.JButton openPortButton = new javax.swing.JButton();

    public void dispose() {
        // stop operations here. This is a deprecated method, but OK for us.
        if (readerThread!=null) readerThread.stop();

        // release port
        if (activeSerialPort != null) activeSerialPort.close();
        serialStream = null;
        ostream = null;
        activeSerialPort = null;
        portNameVector = null;
        opened = false;

        // and clean up parent
        super.dispose();
    }

    public Vector getPortNames() {
        // first, check that the comm package can be opened and ports seen
        portNameVector = new Vector();
        Enumeration portIDs = CommPortIdentifier.getPortIdentifiers();
        // find the names of suitable ports
        while (portIDs.hasMoreElements()) {
            CommPortIdentifier id = (CommPortIdentifier) portIDs.nextElement();
            // accumulate the names in a vector
            portNameVector.addElement(id.getName());
        }
        return portNameVector;
    }

    public String openPort(String portName, String appName)  {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 100);  // name of program, msec to wait
            }
            catch (PortInUseException p) {
                handlePortBusy(p, portName);
                return "Port "+p+" in use already";
            }

            // try to set it for communication via SerialDriver
            try {
                // get selected speed
                int speed = 115200;
                speed = Integer.parseInt((String)speedBox.getSelectedItem());
                // 8-bits, 1-stop, no parity
                activeSerialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (javax.comm.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
                return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
            }

            // NO hardware handshaking, but for consistancy, set the Modem Control Lines
            // set RTS high, DTR high
            activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
            activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; None is needed or used
            activeSerialPort.setFlowControlMode(0);

            // set timeout
            log.debug("Serial timeout was observed as: "+activeSerialPort.getReceiveTimeout()
                      +" "+activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialStream = new DataInputStream(activeSerialPort.getInputStream());
            ostream = activeSerialPort.getOutputStream();

            // start the DUMP
            sendBytes(new byte[]{(byte)'g'});
            // purge contents, if any
            int count = serialStream.available();
            log.debug("input stream shows "+count+" bytes available");
            while ( count > 0) {
                serialStream.skip(count);
                count = serialStream.available();
            }

            // report status?
            if (log.isInfoEnabled()) {
                log.info(portName+" port opened at "
                         +activeSerialPort.getBaudRate()+" baud, sees "
                         +" DTR: "+activeSerialPort.isDTR()
                         +" RTS: "+activeSerialPort.isRTS()
                         +" DSR: "+activeSerialPort.isDSR()
                         +" CTS: "+activeSerialPort.isCTS()
                         +"  CD: "+activeSerialPort.isCD()
                         );
            }

            opened = true;

        }
        catch (Exception ex) {
            log.error("Unexpected exception while opening port "+portName+" trace follows: "+ex);
            ex.printStackTrace();
            return "Unexpected error while opening port "+portName+": "+ex;
        }
        return null; // indicates OK return
    }

    void handlePortBusy(javax.comm.PortInUseException p, String port ) {
        log.error("Port "+p+" in use, cannot open");
    }

    private boolean opened = false;
    DataInputStream serialStream = null;
    OutputStream ostream = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MonitorFrame.class.getName());

    /**
     * Internal class to handle the separate character-receive thread
     *
     */
     class Reader implements Runnable {
        /**
         * Handle incoming characters.  This is a permanent loop,
         * looking for input messages in character form on the
         * stream connected to the PortController via <code>connectPort</code>.
         * Terminates with the input stream breaking out of the try block.
         */
        public void run() {
            // have to limit verbosity!

            while (true) {   // loop permanently, stream close will exit via exception
                try {
                    handleIncomingData();
                }
                catch (java.io.IOException e) {
                    log.warn("run: Exception: "+e.toString());
                }
            }
        }

        static final int maxMsg = 80;
        StringBuffer msg;
        StringBuffer duplicates = new StringBuffer(maxMsg);
        String msgString;
        String matchString ="";

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
            msgString = new String(msg);

            // is this a duplicate?
            if (msgString.equals(matchString)) {
                // yes, suppress and represent with a '+'
                duplicates.append('+');
            } else {
                // no, message is complete, dispatch it!!

                // prepend the duplicate info
                matchString = msgString;
                if (duplicates.length()!=0) {
                    duplicates.append('\n');
                    msgString = " "+(new String(duplicates))+(msgString);
                } else {
                    msgString = "\n"+msgString;
                }
                duplicates.setLength(0);

                // return a notification via the queue to ensure end
                Runnable r = new Runnable() {
                    // reset the duplicates

                    // retain a copy of the message at startup
                    String msgForLater = msgString;
                    public void run() {
                        nextLine(msgForLater, "");
                    }
                };
                javax.swing.SwingUtilities.invokeLater(r);
            }
        }

     } // end class Reader

}
