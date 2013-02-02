// LoaderPane.java

package jmri.jmrix.pricom.downloader;

import org.apache.log4j.Logger;
import java.awt.FlowLayout;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import javax.swing.*;
import java.util.ResourceBundle;
import java.io.*;

import java.util.Enumeration;
import java.util.Vector;
import java.io.OutputStream;
import java.io.DataInputStream;


/**
 * Pane for downloading software updates to PRICOM products
 * @author	    Bob Jacobsen   Copyright (C) 2005
 * @version	    $Revision$
 */
public class LoaderPane extends javax.swing.JPanel {

    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.pricom.downloader.Loader");

    Vector<String> portNameVector = null;
    SerialPort  activeSerialPort = null;

    Thread      readerThread;
    //private     boolean opened = false;
    DataInputStream serialStream = null;
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="IS2_INCONSISTENT_SYNC",
        justification="Class is no longer active, no hardware with which to test fix")
    OutputStream ostream = null;

    JComboBox   portBox = new JComboBox();
    JButton     openPortButton = new JButton();
    JTextArea   traffic = new JTextArea();

    JFileChooser chooser = jmri.jmrit.XmlFile.userFileChooser();
    JButton     fileButton;
    JLabel      inputFileName = new JLabel("");
    JTextArea   comment = new JTextArea();

    JButton         loadButton;
    JProgressBar    bar;
    JLabel          status = new JLabel("");
    
    PdiFile pdiFile;

    // populate the comm port part of GUI, invoked as part of startup
    protected void addCommGUI() {
        // load the port selection part
        portBox.setToolTipText(res.getString("TipSelectPort"));
        portBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        Vector<String> v = getPortNames();
        
        for (int i=0; i<v.size(); i++)
            portBox.addItem(v.elementAt(i));
        
        openPortButton.setText(res.getString("ButtonOpen"));
        openPortButton.setToolTipText(res.getString("TipOpenPort"));
        openPortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    openPortButtonActionPerformed(evt);
                //} catch (jmri.jmrix.SerialConfigException ex) {
                //    log.error("Error while opening port.  Did you select the right one?\n"+ex);
                }
                catch (java.lang.UnsatisfiedLinkError ex) {
                    log.error("Error while opening port.  Did you select the right one?\n"+ex);
                    }
                }
            });
            
        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel(res.getString("LabelSerialPort")));
        p1.add(portBox);
        p1.add(openPortButton);
        add(p1);

        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
            JLabel l = new JLabel(res.getString("LabelTraffic"));
            l.setAlignmentX(JLabel.LEFT_ALIGNMENT);
            p.add(l);
            add(p);
        }
        
        traffic.setEditable(false);
        traffic.setEnabled(true);
        traffic.setText("\n\n\n\n"); // just to save some space
        add(traffic);
    }

    /**
     * Open button has been pushed, create the actual display connection
     */
    void openPortButtonActionPerformed(java.awt.event.ActionEvent e){
        log.info("Open button pushed");
        // can't change this anymore
        openPortButton.setEnabled(false);
        portBox.setEnabled(false);
        // Open the port
        openPort((String)portBox.getSelectedItem(), "JMRI");
        //
        status.setText(res.getString("StatusSelectFile"));
        fileButton.setEnabled(true);
        fileButton.setToolTipText(res.getString("TipFileEnabled"));
        //
        log.info("Open button processing complete");
    }

    synchronized void sendBytes(byte[] bytes) {
        if (log.isDebugEnabled()) log.debug("Send "+bytes.length+": "+jmri.util.StringUtil.hexStringFromBytes(bytes));
        try {
            // send the STX at the start
            byte startbyte = 0x02;
            ostream.write(startbyte);

            // send the rest of the bytes
            for (int i=0; i<bytes.length; i++) {
                // expand as needed
                switch (bytes[i]) {
                case 0x01:
                case 0x02:
                case 0x03:
                case 0x06:
                case 0x15:
                    ostream.write(0x01);
                    ostream.write(bytes[i]+64);
                    break;
                default:
                    ostream.write(bytes[i]);
                    break;
                }
            }

            byte endbyte = 0x03;
            ostream.write(endbyte);
        } catch (java.io.IOException e) {
            log.error("Exception on output: "+e);
        }
    }

    /**
     * Internal class to handle the separate character-receive thread
     *
     */
     class LocalReader extends Thread {
        /**
         * Handle incoming characters.  This is a permanent loop,
         * looking for input messages in character form on the
         * stream connected to the PortController via <code>connectPort</code>.
         * Terminates with the input stream breaking out of the try block.
         */
        public void run() {
            // have to limit verbosity!

            try {
              nibbleIncomingData();            // remove any pending chars in queue
            }
            catch (java.io.IOException e) {
              log.warn("nibble: Exception: "+e.toString());
            }
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
        byte inBuffer[];

        void nibbleIncomingData() throws java.io.IOException{
            long nibbled = 0;                         // total chars chucked
            serialStream = new DataInputStream(activeSerialPort.getInputStream());
            ostream = activeSerialPort.getOutputStream();

            // purge contents, if any
            int count = serialStream.available();     // check for pending chars
            while ( count > 0) {                      // go until gone
                serialStream.skip(count);             // skip the pending chars
                nibbled += count;                     // add on this pass count
                count = serialStream.available();     // any more left?
            }

            if (log.isDebugEnabled()) log.debug("nibbled "+nibbled+" from input stream");
        }

        void handleIncomingData() throws java.io.IOException {
            // we sit in this until the message is complete, relying on
            // threading to let other stuff happen

            StringBuffer mbuff = new StringBuffer();
            // wait for start of message
            int dataChar; 
            while ( (dataChar = serialStream.readByte()) != 0x02) {
                mbuff.append(dataChar);
                if (log.isDebugEnabled()) log.debug(" rcv char "+dataChar);
                if (dataChar == 0x0d) {
                    // Queue the string for display
                    javax.swing.SwingUtilities.invokeLater(new Notify(mbuff));
                }
            }

            // Create output message
            inBuffer = new byte[maxMsg];

            // message started, now store it in buffer
            int i;
            for (i = 0; i < maxMsg; i++) {
                byte char1 = serialStream.readByte();
                if (char1 == 0x03) {  // 0x03 is the end of message
                    break;
                }
                inBuffer[i] = char1;
            }
            if (log.isDebugEnabled()) log.debug("received "+(i+1)+" bytes "+jmri.util.StringUtil.hexStringFromBytes(inBuffer));
            
            // and process the message for possible replies, etc
            nextMessage(inBuffer, i);
        }

        int msgCount = 0;
        int msgSize = 64;
        boolean init = false;

        /**
         * Send the next message of the download.
         */
        void nextMessage(byte[] buffer, int length) {
            
            // if first message, get size & start
            if (isUploadReady(buffer)) {
                msgSize = getDataSize(buffer);
                init = true;
            }

            // if not initialized yet, just ignore message
            if (!init) return;

            // see if its a request for more data
            if (! (isSendNext(buffer) || isUploadReady(buffer)) ) {
                if (log.isDebugEnabled()) log.debug("extra message, ignore");
                return;
            }

            // update progress bar via the queue to ensure synchronization
            Runnable r = new Runnable() {
                public void run() {
                    updateGUI();
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);

            // get the next message
            byte[] outBuffer = pdiFile.getNext(msgSize);

            // if really a message, send it
            if (outBuffer != null) {
                javax.swing.SwingUtilities.invokeLater(new Notify(outBuffer));
                CRC_block(outBuffer);
                sendBytes(outBuffer);
                return;
            }

            // if here, no next message, send end
            outBuffer = bootMessage();
            sendBytes(outBuffer);

            // signal end to GUI via the queue to ensure synchronization
            r = new Runnable() {
                public void run() {
                    enableGUI();
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);

            // stop this thread
            stopThread(readerThread);

        }

        /**
         * Update the GUI for progress
         * <P>
         * Should be invoked on the Swing thread
         */
        void updateGUI() {
            if (log.isDebugEnabled()) log.debug("updateGUI with "+msgCount+" / "+(pdiFile.length()/msgSize));
            if (!init) return;

            status.setText(res.getString("StatusDownloading"));
            // update progress bar
            msgCount++;
            bar.setValue(100*msgCount*msgSize/pdiFile.length());

        }

        /**
         * Signal GUI that it's the end of the download
         * <P>
         * Should be invoked on the Swing thread
         */
        void enableGUI() {
            if (log.isDebugEnabled()) log.debug("enableGUI");
            if (!init) log.error("enableGUI with init false");

            // enable GUI
            loadButton.setEnabled(true);
            loadButton.setToolTipText(res.getString("TipLoadEnabled"));
            status.setText(res.getString("StatusDone"));
        }

        class Notify implements Runnable {
            Notify(StringBuffer b) {
                message = b.toString();
            }
        
            Notify(byte[] b) {
                message = jmri.util.StringUtil.hexStringFromBytes(b);
            }
            
            Notify(byte[] b, int length) {
                byte [] temp = new byte[length];
                for (int i = 0; i< length ; i++)
                    temp[i] = b[i];
                message = jmri.util.StringUtil.hexStringFromBytes(temp);
            }
            
            String message;
        
            /**
             * when invoked, format and display the message
             */
            public void run() {
                traffic.setText(message);
            }
        } // end class Notify
     } // end class LocalReader

    // use deprecated stop method to stop thread,
    // which will be sitting waiting for input
    @SuppressWarnings("deprecation")
    void stopThread(Thread t) {
        t.stop();
    }
        
    public void dispose() {
        // stop operations if in process
        if (readerThread!=null) stopThread(readerThread);

        // release port
        if (activeSerialPort != null) activeSerialPort.close();
        serialStream = null;
        ostream = null;
        activeSerialPort = null;
        portNameVector = null;
        //opened = false;
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
            if (id.getPortType() != CommPortIdentifier.PORT_PARALLEL )
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
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            }
            catch (PortInUseException p) {
                handlePortBusy(p, portName);
                return "Port "+p+" in use already";
            }

            // try to set it for communication via SerialDriver
            try {
                // get selected speed
                int speed = 9600;
                // Doc says 7 bits, but 8 seems needed
                activeSerialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (gnu.io.UnsupportedCommOperationException e) {
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

            //opened = true;

        }
        catch (Exception ex) {
            log.error("Unexpected exception while opening port "+portName+" trace follows: "+ex);
            ex.printStackTrace();
            return "Unexpected error while opening port "+portName+": "+ex;
        }
        return null; // indicates OK return
    }

    void handlePortBusy(gnu.io.PortInUseException p, String port ) {
        log.error("Port "+p+" in use, cannot open");
    }

    public LoaderPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        addCommGUI();

        add(new JSeparator());

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

            fileButton = new JButton(res.getString("ButtonSelect"));
            fileButton.setEnabled(false);
            fileButton.setToolTipText(res.getString("TipFileDisabled"));
            fileButton.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selectInputFile();
                }
            });
            p.add(fileButton);
            p.add(new JLabel(res.getString("LabelInpFile")));
            p.add(inputFileName);

            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
            JLabel l = new JLabel(res.getString("LabelFileComment"));
            l.setAlignmentX(JLabel.LEFT_ALIGNMENT);
            p.add(l);
            add(p);
        }
        
        comment.setEditable(false);
        comment.setEnabled(true);
        comment.setText("\n\n\n\n"); // just to save some space
        add(comment);

        add(new JSeparator());

        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());

            loadButton = new JButton(res.getString("ButtonLoad"));
            loadButton.setEnabled(false);
            loadButton.setToolTipText(res.getString("TipLoadDisabled"));
            p.add(loadButton);
            loadButton.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doLoad();
                }
            });

            add(p);
        }
        

        bar = new JProgressBar();
        add(bar);
        
        add(new JSeparator());

        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
            status.setText(res.getString("StatusSelectPort"));
            status.setAlignmentX(JLabel.LEFT_ALIGNMENT);
            p.add(status);
            add(p);
        }

    }

    void selectInputFile() {
        chooser.rescanCurrentDirectory();
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        inputFileName.setText(chooser.getSelectedFile().getPath());

        // now read the file
        pdiFile = new PdiFile(chooser.getSelectedFile());
        try {
            pdiFile.open();
        } catch (IOException e) { log.error("Error opening file: "+e); }

        comment.setText(pdiFile.getComment());
        status.setText(res.getString("StatusDoDownload"));
        loadButton.setEnabled(true);
        loadButton.setToolTipText(res.getString("TipLoadEnabled"));
        validate();
    }

    void doLoad() {
        status.setText(res.getString("StatusRestartUnit"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipLoadGoing"));
        // start read/write thread
        readerThread = new LocalReader();
        readerThread.start();
    }

    long CRC_char(long crcin, byte ch) {
	    long crc;

	    crc = crcin;                    // copy incoming for local use

	    crc = swap(crc);                // swap crc bytes
            crc ^= ((long)ch & 0xff);       // XOR on the byte, no sign extension
	    crc ^= ((crc&0xFF) >> 4);

	    /*  crc:=crc xor (swap(lo(crc)) shl 4) xor (lo(crc) shl 5);     */
	    crc = (crc ^ (swap((crc&0xFF)) << 4)) ^ ((crc&0xFF) << 5);
            crc &= 0xffff;                  // make sure to mask off anything above 16 bits
	    return crc;
    }

    long swap(long val) {
        long low = val &0xFF;
        long high = (val>>8)&0xFF;
        return low*256+high;
    }


    /**
     * Insert the CRC for a block of characters in a buffer
     * <P>
     * The last two bytes of the buffer hold the checksum, and are
     * not included in the checksum.
     */
    void CRC_block(byte[] buffer) {
	    long crc = 0;

	    for (int r=0;r<buffer.length-2;r++) {
		    crc = CRC_char(crc, buffer[r]);	// do this character
	    }

	    // store into buffer
	    byte high = (byte) ((crc>>8)&0xFF);
	    byte low  = (byte) (crc&0xFF);
	    buffer[buffer.length-2] = low;
	    buffer[buffer.length-1] = high;
    }

    /**
     * Check to see if message starts transmission
     */
    boolean isUploadReady(byte[] buffer) {
        if (buffer[0] != 31) return false;
        if (buffer[1] != 32) return false;
        if (buffer[2] != 99) return false;
        if (buffer[3] != 00) return false;
        if ( ! ( (buffer[4] == 44) || (buffer[4] == 45) ) ) return false;
        return true;
    }

    /**
     * Check to see if this is a request for the next block
     */
    boolean isSendNext(byte[] buffer) {
        if (buffer[0] != 31) return false;
        if (buffer[1] != 32) return false;
        if (buffer[2] != 99) return false;
        if (buffer[3] != 00) return false;
        if (buffer[4] != 22) return false;
        if (log.isDebugEnabled()) log.debug("OK isSendNext");
        return true;
    }

    /**
     * Get output data length from 1st message
     */
    int getDataSize(byte[] buffer) {
        if (buffer[4] == 44) return 64;
        if (buffer[4] == 45) return 128;
        log.error("Bad length byte: "+buffer[3]);
        return 64;
    }

    /**
     * Return a properly formatted boot message, complete with CRC
     */
    byte[] bootMessage() {
        byte[] buffer = new byte[] {99,0,0,0,0};
        CRC_block(buffer);
        return buffer;
    }

    static Logger log = Logger.getLogger(LoaderPane.class.getName());

}
