// CbusConsoleFrame.java

package jmri.jmrix.can.cbus.swing.console;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.text.*;
import javax.swing.JOptionPane;
import javax.swing.JLabel;

import jmri.util.JmriJFrame;

import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.CbusConstants;

/**
 * Frame for Cbus Console
 * 
 * @author			Andrew Crosland   Copyright (C) 2008
 * @version			$Revision: 1.7 $
 */
public class CbusConsoleFrame extends JmriJFrame implements CanListener {
    
    // member declarations
    protected JButton clearButton = new JButton();
    protected JToggleButton freezeButton = new JToggleButton();
    protected JScrollPane jScrollPane1Can = new JScrollPane();
    protected JScrollPane jScrollPane1Cbus = new JScrollPane();
    protected JTextArea monTextPaneCan = new JTextArea();
    protected JTextArea monTextPaneCbus = new JTextArea();
    protected JButton startLogButton = new JButton();
    protected JButton stopLogButton = new JButton();
    protected JCheckBox timeCheckBox = new JCheckBox();
    protected JCheckBox priCheckBox = new JCheckBox();
    protected JButton openFileChooserButton = new JButton();
    protected JTextField entryField = new JTextField();
    protected JButton enterButton = new JButton();
    protected JTextField sentCountField = new JTextField("0", 8);
    protected JTextField rcvdCountField = new JTextField("0", 8);
    protected JButton statsClearButton = new JButton();
    protected JCheckBox decimalCheckBox = new JCheckBox();
    protected JTextField lastDynPriField = new JTextField();
    protected JTextField lastMinPriField = new JTextField();
    protected JTextField[] lastRxDataFields = new JTextField[8];
    protected JButton copyButton = new JButton();
    protected JTextField dynPriField = new JTextField();
    protected JTextField minPriField = new JTextField();
    protected JTextField[] dataFields = new JTextField[8];
    protected JButton sendButton = new JButton();
    protected JButton dataClearButton = new JButton();

    protected int i;
    
    // to find and remember the log file
    final javax.swing.JFileChooser logFileChooser = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());

    // members for handling the CBUS interface
    CanMessage msg;
    
    AbstractCanTrafficController tc = null;
    
    String replyString;

    public CbusConsoleFrame() {
        super();
    }
    
    protected String title() { return "CBUS Console"; }
    
    protected void init() {
        // connect to the CanTrafficController
        tc = jmri.jmrix.can.TrafficController.instance();
        tc.addCanListener(this);
    }
    
    public void dispose() {
        tc.removeCanListener(this);
        super.dispose();
    }
    
    public void initComponents() throws Exception {
        // the following code sets the frame's initial state
        _sent = 0;
        _rcvd = 0;
        
        clearButton.setText("Clear screen");
        clearButton.setVisible(true);
        clearButton.setToolTipText("Clear monitoring history");
        
        freezeButton.setText("Freeze screen");
        freezeButton.setVisible(true);
        freezeButton.setToolTipText("Stop display scrolling");
        
        enterButton.setText("Add Message");
        enterButton.setVisible(true);
        enterButton.setToolTipText("Add a text message to the log");
        
        monTextPaneCan.setVisible(true);
        monTextPaneCan.setToolTipText("Command and reply monitoring information appears here");
        monTextPaneCan.setEditable(false);
        
        monTextPaneCbus.setVisible(true);
        monTextPaneCbus.setToolTipText("Command and reply monitoring information appears here");
        monTextPaneCbus.setEditable(false);
        
        entryField.setToolTipText("Enter text here, then click button to include it in log");
        
        // fix a width for raw field for current character set
        JTextField tCan = new JTextField(30);
        int x = jScrollPane1Can.getPreferredSize().width+tCan.getPreferredSize().width;
        int y = jScrollPane1Can.getPreferredSize().height+10*tCan.getPreferredSize().height;
        
        jScrollPane1Can.getViewport().add(monTextPaneCan);
        jScrollPane1Can.setPreferredSize(new Dimension(x, y));
        jScrollPane1Can.setVisible(true);
        
        // Add a nice border
        jScrollPane1Can.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "CAN Frame"));
        
        // fix a width for Cbus field for current character set
        JTextField tCbus = new JTextField(30);
        x = jScrollPane1Cbus.getPreferredSize().width+tCbus.getPreferredSize().width;
        y = jScrollPane1Cbus.getPreferredSize().height+10*tCbus.getPreferredSize().height;
        
        // Add a nice border
        jScrollPane1Cbus.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "CBUS Message"));
        
        jScrollPane1Cbus.getViewport().add(monTextPaneCbus);
        jScrollPane1Cbus.setPreferredSize(new Dimension(x, y));
        jScrollPane1Cbus.setVisible(true);
        
        startLogButton.setText("Start logging");
        startLogButton.setVisible(true);
        startLogButton.setToolTipText("start logging to file");
        
        stopLogButton.setText("Stop logging");
        stopLogButton.setVisible(true);
        stopLogButton.setToolTipText("Stop logging to file");
        
        timeCheckBox.setText("Show timestamps");
        timeCheckBox.setVisible(true);
        timeCheckBox.setToolTipText("If checked, show timestamps before each message");
        
        priCheckBox.setText("Show priorities");
        priCheckBox.setVisible(true);
        priCheckBox.setToolTipText("If checked, show CBUS priorities before each message");
        
        openFileChooserButton.setText("Choose log file");
        openFileChooserButton.setVisible(true);
        openFileChooserButton.setToolTipText("Click here to select a new output log file");
        
        sentCountField.setToolTipText("The number of packet sent");
        sentCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Sent"));
        
        rcvdCountField.setToolTipText("The number of packet received");
        rcvdCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Received"));
        
        statsClearButton.setText("Clear");
        statsClearButton.setVisible(true);
        statsClearButton.setToolTipText("Clear the sent and received packet counters");
        
        decimalCheckBox.setText("Decimal Data Entry/Display");
        decimalCheckBox.setVisible(true);
        decimalCheckBox.setToolTipText("If checked, Data entry/display is decimal\n"
                                    +"if unchecked, hexadecimal");
        _decimal = false;
        decimalCheckBox.setSelected(_decimal);
        
        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");
        
        dataClearButton.setText("Clear");
        dataClearButton.setVisible(true);
        dataClearButton.setToolTipText("Clear all data fields");
        
        copyButton.setText("Copy");
        copyButton.setVisible(true);
        copyButton.setToolTipText("Copy most recently received packet");
        
        setTitle(title());
        // Panels will be added downwards
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // add items to GUI
        // Pane to hold packet history
        JPanel historyPane = new JPanel();
        historyPane.setLayout(new BoxLayout(historyPane, BoxLayout.Y_AXIS));
        historyPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Packet history"));
        
        // sub-pane to hold scrolling text boxes
        JPanel paneB = new JPanel();
        // Constrain scroll panels to be side-by-side
        paneB.setLayout(new BoxLayout(paneB, BoxLayout.X_AXIS));
        paneB.add(jScrollPane1Can);
        paneB.add(jScrollPane1Cbus);
        historyPane.add(paneB);
        
        // Sub-pane to hold buttons
        JPanel paneA = new JPanel();
        paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));
        
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        pane1.add(clearButton);
        pane1.add(freezeButton);
        pane1.add(timeCheckBox);
        pane1.add(priCheckBox);
        paneA.add(pane1);
        
        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
        pane2.add(openFileChooserButton);
        pane2.add(startLogButton);
        pane2.add(stopLogButton);
        paneA.add(pane2);
        
        JPanel pane3 = new JPanel();
        pane3.setLayout(new BoxLayout(pane3, BoxLayout.X_AXIS));
        pane3.add(enterButton);
        pane3.add(entryField);
        paneA.add(pane3);
        
        historyPane.add(paneA);
        getContentPane().add(historyPane);
        
        // Pane for network statistics
        JPanel statsPane = new JPanel();
        statsPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Packets"));
        statsPane.add(sentCountField);
        statsPane.add(rcvdCountField);
        statsPane.add(statsClearButton);
        statsPane.add(decimalCheckBox);
        getContentPane().add(statsPane);
        
        // Pane for most recently recived packet
        JPanel rxPane = new JPanel();
        rxPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Most Recently Received Packet"));

        // Construct data fields for Priority and up to 8 bytes
        lastDynPriField = new JTextField("", 4);
        lastDynPriField.setToolTipText("Dynamic Priority, 0, 1 or 2");
        lastDynPriField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Dyn Pri"));
        rxPane.add(lastDynPriField);
        lastMinPriField = new JTextField("", 4);
        lastMinPriField.setToolTipText("Minor Priority");
        lastMinPriField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Min Pri"));
        rxPane.add(lastMinPriField);
        for (i=0; i<8; i++) {
            lastRxDataFields[i] = new JTextField("", 6);
            if (i==0) {
                lastRxDataFields[i].setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "d0 (OPC)"));
                lastRxDataFields[i].setToolTipText("Byte count and Op code - BBBCCCCC");
            } else {
                lastRxDataFields[i].setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "d"+i));
                lastRxDataFields[i].setToolTipText("Data byte "+i);
            }
            rxPane.add(lastRxDataFields[i]);
        }
        rxPane.add(copyButton);
        getContentPane().add(rxPane);
        
        
        // Pane for constructing packet to send
        JPanel sendPane = new JPanel();
        sendPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Send Packet"));

        // Construct data fields for Priority and up to 8 bytes
        dynPriField = new JTextField("2", 4);
        dynPriField.setToolTipText("Dynamic Priority, 0, 1 or 2");
        dynPriField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Dyn Pri"));
        sendPane.add(dynPriField);
        minPriField = new JTextField("3", 4);
        minPriField.setToolTipText("Minor Priority, 0, 1, 2 or 3");
        minPriField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Min Pri"));
        sendPane.add(minPriField);
        for (i=0; i<8; i++) {
            dataFields[i] = new JTextField("", 6);
            if (i==0) {
                dataFields[i].setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "d0 (OPC)"));
                dataFields[i].setToolTipText("Byte count and Op code - BBBCCCCC");
            } else {
                dataFields[i].setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "d"+i));
                dataFields[i].setToolTipText("Data byte "+i);
            }
            sendPane.add(dataFields[i]);
        }
        sendPane.add(sendButton);
        sendPane.add(dataClearButton);
        getContentPane().add(sendPane);
        

        // connect actions to buttons
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearButtonActionPerformed(e);
            }
        });
        startLogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startLogButtonActionPerformed(e);
            }
        });
        stopLogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                stopLogButtonActionPerformed(e);
            }
        });
        openFileChooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openFileChooserButtonActionPerformed(e);
            }
        });
        
        enterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                enterButtonActionPerformed(e);
            }
        });
        
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                copyButtonActionPerformed(e);
            }
        });
        
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });
        
        dataClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dataClearButtonActionPerformed(e);
            }
        });
        
        statsClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                statsClearButtonActionPerformed(e);
            }
        });
        
        decimalCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                decimalCheckBoxActionPerformed(e);
            }
        });
        
        // set file chooser to a default
        logFileChooser.setSelectedFile(new File("monitorLog.txt"));
        
        // connect to data source
        init();
        
        // add help menu to window
        addHelpMenu();
        
        // prevent button areas from expanding
        pack();
        paneA.setMaximumSize(paneA.getSize());
        pack();
    }

    /**
     * Define help menu for this window.
     * <p>
     * By default, provides a generic help page
     * that covers general features.  Specific
     * implementations can override this to 
     * show their own help page if desired.
     */
    protected void addHelpMenu() {
    	addHelpMenu("package.jmri.jmrix.can.cbus.swing.console.CbusConsoleFrame", true);
    }
    
    public void nextLine(String line, String decoded, String priorities) {
        // handle display of traffic
        // line is the traffic in 'normal form', decoded is the decoded,
        // protocol specific, form
        // Both should contain the same number of well-formed lines, e.g. end
        // with \n
        StringBuffer sbCan = new StringBuffer(80);
        StringBuffer sbCbus = new StringBuffer(80);
        
        // display the timestamp if requested
        if ( timeCheckBox.isSelected() ) {
            sbCan.append(df.format(new Date())).append( ": " ) ;
            sbCbus.append(df.format(new Date())).append( ": " ) ;
        }
        
        // display CBUS the priorities if requested
        if ( priCheckBox.isSelected() ) {
            sbCbus.append(priorities+" ");
        }
        
        // display decoded data
        sbCan.append(line);
        sbCbus.append(decoded);
        synchronized( linesBufferCan ) {
            linesBufferCan.append( sbCan.toString() );
            linesBufferCbus.append( sbCbus.toString() );
        }
        
        // if not frozen, display it in the Swing thread
        if (!freezeButton.isSelected()) {
            Runnable r = new Runnable() {
                public void run() {
                    synchronized( linesBufferCan ) {
                        monTextPaneCan.append( linesBufferCan.toString() );
                        monTextPaneCbus.append( linesBufferCbus.toString() );
                        int LineCount = monTextPaneCan.getLineCount() ;
                        if( LineCount > MAX_LINES ) {
                            LineCount -= MAX_LINES ;
                            try {
                                int offset = monTextPaneCan.getLineStartOffset(LineCount);
                                monTextPaneCan.getDocument().remove(0, offset ) ;
                                monTextPaneCbus.getDocument().remove(0, offset ) ;
                            } catch (BadLocationException ex) {
                            }
                        }
                        linesBufferCan.setLength(0) ;
                        linesBufferCbus.setLength(0) ;
                    }
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);
        }

        // if requested, log to a file.
        if (logStream != null) {
            String logLine = sbCbus.toString();
            if (!newline.equals("\n")) {
                // have to massage the line-ends
                int i = 0;
                int lim = sbCbus.length();
                StringBuffer out = new StringBuffer(sbCbus.length()+10);  // arbitrary guess at space
                for ( i = 0; i<lim; i++) {
                    if (sbCbus.charAt(i) == '\n')
                        out.append(newline);
                    else
                        out.append(sbCbus.charAt(i));
                }
                logLine = new String(out);
            }
            logStream.print(logLine);
        }
    }

    String newline = System.getProperty("line.separator");

    public synchronized void clearButtonActionPerformed(java.awt.event.ActionEvent e) {
        // clear the monitoring history
        synchronized( linesBufferCan ) {
            linesBufferCan.setLength(0);
            linesBufferCbus.setLength(0);
            monTextPaneCan.setText("");
            monTextPaneCbus.setText("");
        }
    }
    
    public synchronized void startLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start logging by creating the stream
        if ( logStream==null) {  // successive clicks don't restart the file
            // start logging
            try {
                logStream = new PrintStream(new FileOutputStream(logFileChooser.getSelectedFile()));
            } catch (Exception ex) {
                log.error("exception "+ex);
            }
        }
    }
    
    public synchronized void stopLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        // stop logging by removing the stream
        if (logStream!=null) {
            logStream.flush();
            logStream.close();
        }
        logStream = null;
    }
    
    public void openFileChooserButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start at current file, show dialog
        int retVal = logFileChooser.showSaveDialog(this);
        
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            boolean loggingNow = (logStream != null);
            stopLogButtonActionPerformed(e);  // stop before changing file
            File file = logFileChooser.getSelectedFile();
            // if we were currently logging, start the new file
            if (loggingNow) startLogButtonActionPerformed(e);
        }
    }
    
    public void enterButtonActionPerformed(java.awt.event.ActionEvent e) {
        nextLine(entryField.getText() + "\n", entryField.getText() + "\n", "");
    }
    
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        int i;
        int data, data2;
        CanMessage m = new CanMessage();
        data = parseBinDecHexByte(dynPriField.getText(), 2, _decimal, "CBUS Console", "Invalid Dynamic Priority Value");
        if (data == -1) return;
        data2 = parseBinDecHexByte(minPriField.getText(), 3, _decimal, "CBUS Console", "Invalid Minor Priority Value");
        if (data2 == -1) return;
        m.setPri(data*4 + data2);
        for (i=0; i<8; i++) {
            if (!dataFields[i].getText().equals("")) {
                data = parseBinDecHexByte(dataFields[i].getText(), 255, _decimal, "CBUS Console", 
                        "Invalid Data Value in d"+i);
                if (data == -1) return;
                m.setElement(i, data);
                if (i==0) data2 = data;     // save OPC(d0) for later
            } else {
                break;
            }
        }
        if (i==0) {
            JOptionPane.showMessageDialog(null, "You must enter at least an opcode",
                    "CBUS Console", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Does the number of data match the opcode?
        // Subtract one as loop variable will have incremented
        if ((i-1) != (data2>>5)) {
            JOptionPane.showMessageDialog(null, "Number of data bytes entered\n"
                    +"does not match count in d0(OPC):"+(data2>>5),
                    "CBUS Console", JOptionPane.ERROR_MESSAGE);
            return;
        }
        m.setNumDataElements(i);
        // Messages sent by us will not be forwarded back so add to display manually
        message(m);
        tc.sendCanMessage(m, this);
    }
    
    public void dataClearButtonActionPerformed(java.awt.event.ActionEvent e) {
        int i;
        dynPriField.setText("2");
        minPriField.setText("3");
        for (i=0; i<8; i++) {
            dataFields[i].setText("");
        }
    }
    
    public void statsClearButtonActionPerformed(java.awt.event.ActionEvent e) {
        _sent = 0;
        _rcvd = 0;
        sentCountField.setText("0");
        rcvdCountField.setText("0");
    }
    
    public void decimalCheckBoxActionPerformed(java.awt.event.ActionEvent e) {
        if (decimalCheckBox.isSelected()) {
            _decimal = true;
        } else {
            _decimal = false;
        }
    }
    
    public void copyButtonActionPerformed(java.awt.event.ActionEvent e) {
        dynPriField.setText(lastDynPriField.getText());
        minPriField.setText(lastMinPriField.getText());
        for (i=0; i<8; i++) {
            dataFields[i].setText(lastRxDataFields[i].getText());
        }
    }
    
    public synchronized String getCanFrameText() {
        return new String(linesBufferCan);
    }
    
    public synchronized String getCbusFrameText() {
        return new String(linesBufferCbus);
    }
    
    PrintStream logStream = null;
    
    // to get a time string
    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
    
    StringBuffer linesBufferCan = new StringBuffer();
    StringBuffer linesBufferCbus = new StringBuffer();
    static private int MAX_LINES = 500 ;
    
    public synchronized void message(CanMessage l) {  // receive a message and log it
        nextLine("sent: "+l.toString()+"\n",
                "ID:---"+" "+(l.isRtr() ? "R " : "N ")+decode(l),
                "Dyn Pri:"+l.getPri()/4+" Min Pri:"+(l.getPri()&3));
        sentCountField.setText(Integer.toString(++_sent));
    }
    
    public synchronized void reply(CanReply l) {  // receive a reply message and log it
        int i;
        // Capture most recent received packet
        if (_decimal) {
            lastDynPriField.setText(Integer.toString(l.getPri()/4));
            lastMinPriField.setText(Integer.toString(l.getPri()&3));
        } else {
            lastDynPriField.setText(Integer.toHexString(l.getPri()/4));
            lastMinPriField.setText(Integer.toHexString(l.getPri()%3));
        }
        // Pay attention to data length in op-code
        for(i=0; i<(l.getElement(0)>>5)+1; i++){
            if (_decimal) {
                lastRxDataFields[i].setText(Integer.toString(l.getElement(i)));
            } else {
                lastRxDataFields[i].setText(Integer.toHexString(l.getElement(i)));
            }
        }
        nextLine("rcvd: "+l.toString()+"\n", 
                "ID:"+l.getId()+" "+(l.isRtr() ? "R " : "N ")+decode(l),
                "Dyn Pri:"+l.getPri()/4+" Min Pri:"+(l.getPri()&3));
        rcvdCountField.setText(Integer.toString(++_rcvd));
    }
    
    /**
     * Return a string representation of a decoded canMessage
     *
     * @param msg CanMessage to be decoded
     * Return String decoded message
     */
    public String decode(AbstractMessage msg) {
        String str = "";
        int bytes = msg.getElement(0) >> 5;
        int op = msg.getElement(0);
        int node = msg.getElement(1)*256 + msg.getElement(2);
        int event = msg.getElement(3)*256 + msg.getElement(4);
        switch (op) {
            case CbusConstants.CBUS_OP_EV_ON: {
                // ON event
                str = str+"Event ON NN:"+node+" Ev:"+event;
                break;
            }
               
            case CbusConstants.CBUS_OP_EV_OFF: {
                // OFF event
                str = str+"Event OFF NN:"+node+" Ev:"+event;
                break;
            }
             case CbusConstants.CBUS_OP_EV_ON_DATA: {
                // ON event
                str = str+"Event ON NN:"+node+" Ev:"+event+" Data:"+msg.getElement(5);
                break;
            }
               
            case CbusConstants.CBUS_OP_EV_OFF_DATA: {
                // OFF event
                str = str+"Event OFF NN:"+node+" Ev:"+event+" Data:"+msg.getElement(5);
                break;
            }
            
            default: {
                str = str+"Unrecognised: "+msg.toString();
                break;
            }
        }
        return (str+"\n");
    }
    
    /**
     * Parse a string for binary, decimal or hex byte value
     * <P>
     * 0b, 0d or 0x prefix will force parsing of binary, decimal or hex,
     * respectively. Otherwies, if decimal is true:
     *      Up to three digits will be parsed as decimal, e.g. 10 or 127
     *      more than three digits will be parsed as binary, e.g. 0010 or 1011
     * if decimal is clear:
     *      up to two digits will be treated as hex, e.g. F or B1
     *      more than two digits will be treated as binary, e.g. 001 or 110
     *
     * @param s string to be parsed
     * @param limit upper bound of value to be parsed
     * @param decimal flag for decimnal or hex default
     * @param errTitle Title of error dialogue box if Number FormatException encountered
     * @param errMsg Message to be displayed if Number FormatException encountered
     * @return the byte value, -1 indicates failure
    */
    public int parseBinDecHexByte(String s, int limit, boolean decimal, String errTitle, String errMsg) {
        int data = -1;
        boolean error = false;
        int radix = 16;

        if ((s.length() > 3) && s.substring(0, 2).equalsIgnoreCase("0x")) {
            // hex, remove the prefix
            s = s.substring(2);
            radix = 16;
        } else if ((s.length() > 3) && s.substring(0, 2).equalsIgnoreCase("0d")) {
            // decimal, remove the prefix
            s = s.substring(2);
            radix = 10;
        } else if ((s.length() > 3) && s.substring(0, 2).equalsIgnoreCase("0b")) {
            // binary, remove the prefix
            s = s.substring(2);
            radix = 2;
        } else if (decimal) {
            if (s.length() > 3) {
                // Assumed to be binary
                radix = 2;
            } else {
                radix = 10;
            }
        } else {
            if (s.length() > 2) {
                // Assumed to be binary
                radix = 2;
            } else {
                radix = 16;
            }
        }

        try {
            data = Integer.parseInt(s, radix);
        } catch (NumberFormatException ex) {
            error = true;
        }
        if ((data < 0) || (data > limit))
            error = true;

        if (error) {
            JOptionPane.showMessageDialog(null, errMsg,
                    "CBUS Console", JOptionPane.ERROR_MESSAGE);
            data = -1;
        }
        return data;
    }
    
    private int _sent;
    private int _rcvd;
    private boolean _decimal;
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CbusConsoleFrame.class.getName());
}
