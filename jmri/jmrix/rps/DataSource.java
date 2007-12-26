// rps.DataSource.java

package jmri.jmrix.rps;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.*;

/**
 * Simple GUI for controlling the interface to the RPS system
 * <P>
 * When opened, the user must first select a serial port and click "Start".
 *
 * @author			Bob Jacobsen   Copyright (C) 2006
 * @version			$Revision: 1.3 $
 */
public class DataSource extends jmri.util.JmriJFrame implements ThrottleListener {

    DataSource self;
    Vector portNameVector = null;
    SerialPort activeSerialPort = null;
    static java.util.ResourceBundle rb 
            = java.util.ResourceBundle.getBundle("jmri.jmrix.rps.RpsBundle");
    
    // populate the GUI, invoked as part of startup
    protected void init() {
        setTitle(rb.getString("TitleSource"));
        
        // set layout manager
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // load the port selection part
        portBox.setToolTipText(rb.getString("TooltipSelectPort"));
        portBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        Vector v = getPortNames();
        for (int i=0; i<v.size(); i++)
            portBox.addItem(v.elementAt(i));
        portBox.setSelectedIndex(1);
        speedBox.setToolTipText(rb.getString("TooltipSelectBaud"));
        speedBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        speedBox.setSelectedItem("9600");
        openPortButton.setText(rb.getString("ButtonOpen"));
        openPortButton.setToolTipText(rb.getString("TooltipOpen"));
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
        p1.add(new JLabel(rb.getString("LabelSerialPort")));
        p1.add(portBox);
        p1.add(new JLabel(rb.getString("LabelSpeed")));
        p1.add(speedBox);
        p1.add(openPortButton);
        getContentPane().add(p1);

        self = this;
        
        // add poll controls
        JPanel p3 = new JPanel();
        p3.setLayout(new FlowLayout());
        p3.add(new JLabel("Poll off time:"));
        offTime.setText("900");
        p3.add(offTime);
        p3.add(new JLabel("On time: "));
        p3.add(onTime);
        onTime.setText("100");
        p3.add(poll);
        poll.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                        pollChange();
                }
        });
        poll.setSelected(false);
        p3.add(new JLabel(" Decoder Address:"));
        addr1.setText("137");
        p3.add(addr1);
        addr2.setText("5510");
        p3.add(addr2);
        // addr3.setText("603");
        p3.add(addr3);
        
        getContentPane().add(p3);
        
        // Done, get ready to display
        pack();
    }
    
    JCheckBox poll = new JCheckBox("Poll");

    JTextField addr1 = new JTextField(5);
    int num1 = -1;
    boolean long1;
    DccThrottle throttle1 = null;
    
    JTextField addr2 = new JTextField(5);
    int num2 = -1;
    boolean long2;
    DccThrottle throttle2 = null;

    JTextField addr3 = new JTextField(5);
    int num3 = -1;
    boolean long3;
    DccThrottle throttle3 = null;
    
    JTextField offTime = new JTextField(5);
    int offDelay;
    JTextField onTime = new JTextField(5);
    int onDelay;
    
    int nextThrottle = 0;
    
    void pollChange() {
        if (poll.isSelected()) {

            num1 = -1;
            throttle1 = null;
            try{
                // get loco address information
                num1 = Integer.parseInt(addr1.getText());
                long1 = (num1>128);
            } catch (Exception e1) {}
            
            num2 = -1;
            throttle2 = null;
            try{
                // get loco address information
                num2 = Integer.parseInt(addr2.getText());
                long2 = (num2>128);
            } catch (Exception e2) {}
            
            num3 = -1;
            throttle3 = null;
            try {
                // get loco address information
                num3 = Integer.parseInt(addr3.getText());
                long3 = (num3>128);
            } catch (Exception e3) {}
            
            // start
            offDelay = Integer.parseInt(offTime.getText());
            onDelay = Integer.parseInt(onTime.getText());
            
            if (getNextThrottle()) 
                startpoll();
            
        } else {
            // stop
            if (throttle1 != null || throttle2 !=null || throttle3 != null) {
                // cancel polling, drop any active F3 settings
                pollThread.interrupt();
                // release throttle
                if (throttle1!=null) {
                    throttle1.setF2(false);
                    throttle1.release();
                    throttle1 = null;
                }
                if (throttle2!=null) {
                    throttle2.setF2(false);
                    throttle2.release();
                    throttle2 = null;
                }
                if (throttle3!=null) {
                    throttle3.setF2(false);
                    throttle3.release();
                    throttle3 = null;
                }
            }
        }
    }
    
    Thread pollThread;
    
    // true when done getting throttles
    boolean getNextThrottle() {
        log.debug("getNextThrottle start");
        if (num1>0 && throttle1 == null) {
            nextThrottle = 1;
            if (log.isDebugEnabled()) log.debug("request 1 is "+num1);
            InstanceManager.throttleManagerInstance().requestThrottle(num1,long1, this);
            return false;
        } if (num2>0 && throttle2 == null) {
            nextThrottle = 2;
            if (log.isDebugEnabled()) log.debug("request 2 is "+num2);
            InstanceManager.throttleManagerInstance().requestThrottle(num2,long2, this);
            return false;
        } if (num3>0 && throttle3 == null) {
            nextThrottle = 3;
            if (log.isDebugEnabled()) log.debug("request 3 is "+num3);
            InstanceManager.throttleManagerInstance().requestThrottle(num3,long3, this);
            return false;
        } 
        log.debug("getNextThrottle done");
        return true;

    }
    
    public void notifyThrottleFound(DccThrottle t) {
        if (log.isDebugEnabled()) log.debug("notifyThrottle "+nextThrottle+":"+t);
        switch (nextThrottle) {
            case 1:
                throttle1 = t;
                break;
            case 2:
                throttle2 = t;
                break;
            case 3:
                throttle3 = t;
                break;
            default:
                log.error("Unexpected nextThrottle: "+nextThrottle);
                return;
        }
        // notification of a throttle OK, check if other's needed
        if (getNextThrottle()) 
            startpoll();
    }
    
    void startpoll() {
        log.debug("start poll");
        // time to start operation
        pollThread = new Thread(){
            public void run() {
                try {
                    while (true) {
                        if (throttle1 != null) {
                            polledAddress = num1;
                            if (log.isDebugEnabled()) log.debug("Set polledAddress = "+polledAddress);
                            throttle1.setF2(true);
                            synchronized (this) { wait(onDelay); }
                            throttle1.setF2(false);
                            synchronized (this) { wait(offDelay); }
                        }
                        
                        if (throttle2 !=null) {
                            polledAddress = num2;
                            if (log.isDebugEnabled()) log.debug("Set polledAddress = "+polledAddress);
                            throttle2.setF2(true);
                            synchronized (this) { wait(onDelay); }
                            throttle2.setF2(false);
                            synchronized (this) { wait(offDelay); }
                        }
                        
                        if (throttle3 != null ) {
                            polledAddress = num3;
                            if (log.isDebugEnabled()) log.debug("Set polledAddress = "+polledAddress);
                            throttle3.setF2(true);
                            synchronized (this) { wait(onDelay); }
                            throttle3.setF2(false);
                            synchronized (this) { wait(offDelay); }
                        }
                    }
                } catch (InterruptedException e) { 
                    return;
                }
            }
        };
        pollThread.start();
    }
    
    /**
     * Send output bytes, e.g. characters controlling operation, 
     * with small delays between the characters.  This is 
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
                int speed = 9600;
                speed = Integer.parseInt((String)speedBox.getSelectedItem());
                speed = 9600;
                // 8-bits, 1-stop, no parity
                activeSerialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, 
                                                            SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
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

    DataInputStream serialStream = null;
    OutputStream ostream = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DataSource.class.getName());

    int polledAddress = -1;
    
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
            msg.append("\n");
            msgString = new String(msg);

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

 
    /**
     * Handle a new line from the device.
     *
     * This needs to execute on the Swing GUI thread.
     * It forwards via the Distributor object.
     *
     * @param s The new message to distribute
     */
    protected void nextLine(String s) {
        // check for startup lines we ignore
        if (s.length() <5) return;
        if (s.startsWith("LABEL,DAYTIME")) return;

        Reading r;
        try {
            r = makeReading(s);
        } catch (Exception e) {
            log.error("Exception formatting input line \""+s+"\": "+e);
            // r = new Reading(-1, new double[]{-1, -1, -1, -1} );
            // skip handling this line
            return;
        }
        r.setRawData(s);
        // forward
        try {
            Distributor.getInstance().submitReading(r);
        } catch (Exception e) {
            log.error("Exception forwarding reading: "+e);
        }
    }

    /**
     * Convert input line to Reading object
     */
    Reading makeReading(String s) throws java.io.IOException {
        // parse string
        java.io.StringReader b = new java.io.StringReader(s);
        com.csvreader.CsvReader c = new com.csvreader.CsvReader(b);
        c.readRecord();

        int count = c.getColumnCount()-2;  // skip DATA,TIME; was there a trailing "'"?
        double[] vals = new double[count];
        for (int i=0; i<count; i++) {
            vals[i] = Double.valueOf(c.get(i+2)).doubleValue();
        }
        if (log.isDebugEnabled()) log.debug("Got polledAddress = "+polledAddress);
        Reading r = new Reading(polledAddress, vals);
        
        return r;
    }
}
