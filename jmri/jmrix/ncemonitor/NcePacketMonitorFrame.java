// NcePacketMonitorFrame.java

package jmri.jmrix.ncemonitor;

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
 * Simple GUI for access to an NCE monitor card
 * <P>
 * When opened, the user must first select a serial port and click "Start".
 * The rest of the GUI then appears.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.5 $
 */
public class NcePacketMonitorFrame extends jmri.jmrix.AbstractMonFrame {

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
        p1.add(openPortButton);
        getContentPane().add(p1);

        // add user part of GUI
        getContentPane().add(new JSeparator());
        JPanel p2 = new JPanel();
        p2.add(checkButton);
        checkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendBytes(new byte[]{(byte)'?'});
                sendBytes(new byte[]{(byte)'L',(byte)'-'});
            }
        });

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b= new JRadioButton("Verbose");
            g.add(b);
            p.add(b);
            b.setSelected(true);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'V'});
                }
            });
            b= new JRadioButton("Hex with preamble symbol");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'H',(byte)'0'});
                }
            });
            p2.add(p);
            b= new JRadioButton("Hex without preamble symbol");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'H',(byte)'2'});
                }
            });
            p2.add(p);
            b= new JRadioButton("Hex with preamble in hex");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'H',(byte)'4'});
                }
            });
            p2.add(p);
        }  // end hex/verbose group

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b= new JRadioButton("Hide speed packets");
            g.add(b);
            p.add(b);
            b.setSelected(true);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'L',(byte)'-'});
                }
            });
            b= new JRadioButton("Show speed packets");
            g.add(b);
            p.add(b);
            b.setToolTipText("This setting overloads the display with data");
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'L',(byte)'+'});
                }
            });
            p2.add(p);
        }  // speed off/on

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b= new JRadioButton("Hide acc packets");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'A',(byte)'-'});
                }
            });
            b= new JRadioButton("Show acc packets");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'A',(byte)'+'});
                }
            });
            b.setSelected(true);
            p2.add(p);
        }  // acc off/on

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b= new JRadioButton("Acc addresses single");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'A',(byte)'S'});
                }
            });
            b= new JRadioButton("Acc addresses paired");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'A',(byte)'P'});
                }
            });
            b.setSelected(true);
            p2.add(p);
        }  // acc single/double

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b= new JRadioButton("Hide reset packets");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'R',(byte)'-'});
                }
            });
            b= new JRadioButton("Show reset packets");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'R',(byte)'+'});
                }
            });
            b.setSelected(true);
            p2.add(p);
        }  // reset off/on

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            ButtonGroup g = new ButtonGroup();
            JRadioButton b;
            b= new JRadioButton("Hide idle packets");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'I',(byte)'-'});
                }
            });
            b= new JRadioButton("Show idle packets");
            g.add(b);
            p.add(b);
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendBytes(new byte[]{(byte)'I',(byte)'+'});
                }
            });
            b.setSelected(true);
            p2.add(p);
        }  // idle off/on

        getContentPane().add(p2);
    }

    JButton checkButton = new JButton("Init");

    protected String title() {
        return "Packet Monitor";
    }

    synchronized void sendBytes(byte[] bytes) {
        try {
            for (int i=0; i<bytes.length; i++) {
                ostream.write(bytes[i]);
                wait(3);
            }
            final byte endbyte = 13;
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
        // Open the port
        openPort((String)portBox.getSelectedItem(), "JMRI");
        // start the reader
        readerThread = new Thread(new Reader());
        readerThread.start();
        log.info("Open button processing complete");
    }

    Thread readerThread;

    protected javax.swing.JComboBox portBox = new javax.swing.JComboBox();
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
                // Doc says 7 bits, but 8 seems needed
                activeSerialPort.setSerialPortParams(38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (javax.comm.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
                return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
            }

            // set RTS high, DTR high
            activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
            activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            activeSerialPort.setFlowControlMode(0);

            // set timeout
            log.debug("Serial timeout was observed as: "+activeSerialPort.getReceiveTimeout()
                      +" "+activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialStream = new DataInputStream(activeSerialPort.getInputStream());
            ostream = activeSerialPort.getOutputStream();

            // make less verbose
            sendBytes(new byte[]{(byte)'L',(byte)'-',10,13});
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NcePacketMonitorFrame.class.getName());

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
                if (char1 == 13) {  // 13 is the CR at the end; done this
                                    // way to be coding-independent
                    break;
                }
                msg.append(char1);
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
