// CbusConsolePane.java

package jmri.jmrix.can.cbus.swing.console;

import org.apache.log4j.Logger;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.text.*;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Frame;

import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.util.FileUtil;

/**
 * Frame for Cbus Console
 *
 * @author			Andrew Crosland   Copyright (C) 2008
 * @version			$Revision: 17977 $
 */
public class CbusConsolePane extends jmri.jmrix.can.swing.CanPanel implements CanListener {
    
    // member declarations
    protected JButton clearButton = new JButton();
    protected JToggleButton freezeButton = new JToggleButton();
    protected JScrollPane jScrollPane1Can = new JScrollPane();
    protected JScrollPane jScrollPane1Cbus = new JScrollPane();
    protected JTextArea monTextPaneCan = new JTextArea();
    protected JTextArea monTextPaneCbus = new JTextArea();
    protected Highlighter cbusHighlighter;
    protected JButton startLogButton = new JButton();
    protected JButton stopLogButton = new JButton();
    protected JCheckBox timeCheckBox = new JCheckBox();
    protected JCheckBox priCheckBox = new JCheckBox();
    protected JButton openFileChooserButton = new JButton();
    protected JTextField entryField = new JTextField();
    protected JButton enterButton = new JButton();
    
    protected JCheckBox showStatsCheckBox = new JCheckBox();
    protected JCheckBox showPacketCheckBox = new JCheckBox();
    protected JCheckBox showEventCheckBox = new JCheckBox();
    protected JButton filterButton = new JButton();
    protected JCheckBox decimalCheckBox = new JCheckBox();
    
    protected JTextField sentCountField = new JTextField("0", 8);
    protected JTextField rcvdCountField = new JTextField("0", 8);
    protected JButton statsClearButton = new JButton();
    
    protected JTextField lastDynPriField = new JTextField();
    protected JTextField lastMinPriField = new JTextField();
    protected JTextField[] lastRxDataFields = new JTextField[8];
    protected JButton copyButton = new JButton();
    
    protected JTextField dynPriField = new JTextField();
    protected JTextField minPriField = new JTextField();
    protected JTextField[] dataFields = new JTextField[8];
    protected JButton sendButton = new JButton();
    protected JButton dataClearButton = new JButton();
    
    protected JPanel statsPane;
    protected JPanel rxPane;
    protected JPanel sendPane;
    protected JPanel evPane;
    
    protected JRadioButton onButton = new JRadioButton();
    protected JRadioButton offButton = new JRadioButton();
    protected ButtonGroup onOffGroup = new ButtonGroup();
    protected JLabel nnLabel = new JLabel("Node Number:");
    protected JLabel evLabel = new JLabel("Event:");
    protected JTextField nnField = new JTextField();
    protected JTextField evField = new JTextField();
    protected JButton sendEvButton = new JButton();
    
    protected int i;
    
    // to find and remember the log file
    final javax.swing.JFileChooser logFileChooser = new JFileChooser(FileUtil.getUserFilesPath());
    
    // members for handling the CBUS interface
    //CanMessage msg;
    
    TrafficController tc = null;
    
    String replyString;
    
    public CbusConsolePane() {
        super();
        _filterFrame = null;
    }
    
    @Override
    public String getTitle() {
        if(memo!=null) {
            return (memo.getUserName() + " CBUS Console");
        
        }
        return "CBUS Console";
    }
    
    @Override
    public String getHelpTarget() { return "package.jmri.jmrix.can.cbus.swing.console.CbusConsolePane"; }
    
    public void init() {}
    
    @Override
    public void dispose() {
        if (tc!=null) tc.removeCanListener(this);
        super.dispose();
    }
    
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        tc = memo.getTrafficController();
        tc.addCanListener(this);
    }
    
    @Override
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
        cbusHighlighter = monTextPaneCbus.getHighlighter();
        
        entryField.setToolTipText("Enter text here, then click button to include it in log");
        
        // fix a width for raw field for current character set
        JTextField tCan = new JTextField(35);
        tCan.setDragEnabled(true);
        int x = jScrollPane1Can.getPreferredSize().width+tCan.getPreferredSize().width;
        int y = jScrollPane1Can.getPreferredSize().height+10*tCan.getPreferredSize().height;
        
        jScrollPane1Can.getViewport().add(monTextPaneCan);
        jScrollPane1Can.setPreferredSize(new Dimension(x, y));
        jScrollPane1Can.setVisible(true);
        
        // Add a nice border
        jScrollPane1Can.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "CAN Frame"));
        
        // fix a width for Cbus field for current character set
        JTextField tCbus = new JTextField(40);
        tCbus.setDragEnabled(true);
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
        
        showStatsCheckBox.setText("Show Statistics");
        showStatsCheckBox.setVisible(true);
        showStatsCheckBox.setToolTipText("Select to show packet statistics");
        
        showPacketCheckBox.setText("Show Packets");
        showPacketCheckBox.setVisible(true);
        showPacketCheckBox.setToolTipText("Select to show packets");
        
        showEventCheckBox.setText("Show Events");
        showEventCheckBox.setVisible(true);
        showEventCheckBox.setToolTipText("Select to show events");
        
        filterButton.setText("Filter...");
        filterButton.setVisible(true);
        filterButton.setToolTipText("Click for a new event filter");
        
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
        decimalCheckBox.setToolTipText("If checked, Data entry/display is decimal."
                +" If unchecked, hexadecimal");
        _decimal = true;
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
        
        onButton.setText("ON");
        onButton.setVisible(true);
        onButton.setToolTipText("Send an ON event");
        onButton.setSelected(true);

        offButton.setText("OFF");
        offButton.setVisible(true);
        offButton.setToolTipText("Send an OFF event");

        sendEvButton.setText("Send");
        sendEvButton.setVisible(true);
        sendEvButton.setToolTipText("Send event");

        //setTitle(getTitle());
        // Panels will be added downwards
//        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());
        
        // add items to GUI
        // Pane to hold packet history
        JPanel historyPane = new JPanel();
        historyPane.setLayout(new BorderLayout());
        historyPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Packet history"));
        
        // sub-pane to hold scrolling text boxes
        JPanel paneB = new JPanel();
        // Constrain scroll panels to be side-by-side
        paneB.setLayout(new BoxLayout(paneB, BoxLayout.X_AXIS));
        paneB.add(jScrollPane1Can);
        paneB.add(jScrollPane1Cbus);
        historyPane.add(paneB, BorderLayout.CENTER);
        
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
        pane2.add(filterButton);
        paneA.add(pane2);
        
        JPanel pane3 = new JPanel();
        pane3.setLayout(new BoxLayout(pane3, BoxLayout.X_AXIS));
        pane3.add(enterButton);
        pane3.add(entryField);
        paneA.add(pane3);
        
        historyPane.add(paneA, BorderLayout.SOUTH);
        add(historyPane, BorderLayout.CENTER);
        
        JPanel southPane = new JPanel();
        southPane.setLayout(new BoxLayout(southPane, BoxLayout.Y_AXIS));
        
        // Pane for network statistics
        statsPane = new JPanel();

        statsPane.setVisible(false); // initial state is not displayed
        showStatsCheckBox.setSelected(false);

        statsPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Statistics"));
        statsPane.add(sentCountField);
        statsPane.add(rcvdCountField);
        statsPane.add(statsClearButton);
//        getContentPane().add(statsPane);
        southPane.add(statsPane);
        showStatsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showStatsCheckBox.isSelected()) {
                    statsPane.setVisible(true);
                    //statsPane.validate();
                    packInside();
                    //statsPane.repaint();
                }
                else {
                    statsPane.setVisible(false);
                    //statsPane.validate();
                    packInside();
                    //statsPane.repaint();
                }
            }
        });
        
        // Pane for most recently recived packet
        rxPane = new JPanel();
        rxPane.setLayout(new BoxLayout(rxPane, BoxLayout.X_AXIS));
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
            lastRxDataFields[i] = new JTextField("", 4);
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
//        rxPane.setVisible(false);
//        getContentPane().add(rxPane);
        southPane.add(rxPane);
        
        
        // Pane for constructing packet to send
        sendPane = new JPanel();
        sendPane.setLayout(new BoxLayout(sendPane, BoxLayout.X_AXIS));
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
            dataFields[i] = new JTextField("", 4);
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
        sendPane.setVisible(false);
//        getContentPane().add(sendPane);
        southPane.add(sendPane);
        
        showPacketCheckBox.setSelected(false);
        rxPane.setVisible(false); // initial state is not displayed
        sendPane.setVisible(false);

        showPacketCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPacketCheckBox.isSelected()) {
                    rxPane.setVisible(true);
                    //rxPane.validate();
                    sendPane.setVisible(true);
                    //sendPane.validate();
                    packInside();
                    //rxPane.repaint();
                    //sendPane.repaint();
                }
                else {
                    rxPane.setVisible(false);
                    //rxPane.validate();
                    sendPane.setVisible(false);
                    //sendPane.validate();
                    packInside();
                    //rxPane.repaint();
                    //sendPane.repaint();
                }
            }
        });

        // Pane for constructing event to send
        evPane = new JPanel();

        evPane.setVisible(true); // initial state is not displayed
        showEventCheckBox.setSelected(true);

        evPane.setLayout(new BoxLayout(evPane, BoxLayout.X_AXIS));
        evPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Send Event"));
        
        nnField = new JTextField("0", 5);
        evPane.add(nnLabel);
        evPane.add(nnField);
        evField = new JTextField("0", 5);
        evPane.add(evLabel);
        evPane.add(evField);

        onOffGroup.add(onButton);
        onOffGroup.add(offButton);
        evPane.add(onButton);
        evPane.add(offButton);

        evPane.add(sendEvButton);
        evPane.setVisible(true);
//        getContentPane().add(evPane);
        southPane.add(evPane);

        // Pane to select display type
        JPanel showPane = new JPanel();
        showPane.setLayout(new BoxLayout(showPane, BoxLayout.X_AXIS));
        showPane.add(showStatsCheckBox);
        showPane.add(showPacketCheckBox);
        showPane.add(showEventCheckBox);
        showPane.add(decimalCheckBox);
//        getContentPane().add(showPane);
        southPane.add(showPane);
        
        add(southPane, BorderLayout.SOUTH);
        
        showEventCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showEventCheckBox.isSelected()) {
                    evPane.setVisible(true);
                    //evPane.validate();
                    packInside();
                    //evPane.repaint();
                }
                else {
                    evPane.setVisible(false);
                    //evPane.validate();
                    packInside();
                    //evPane.repaint();
                }
            }
        });

        // connect actions to buttons
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearButtonActionPerformed(e);
            }
        });
        startLogButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startLogButtonActionPerformed(e);
            }
        });
        stopLogButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                stopLogButtonActionPerformed(e);
            }
        });
        openFileChooserButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openFileChooserButtonActionPerformed(e);
            }
        });
        
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                filterButtonActionPerformed(e);
            }
        });
        
        enterButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                enterButtonActionPerformed(e);
            }
        });
        
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                copyButtonActionPerformed(e);
            }
        });
        
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });
        
        dataClearButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dataClearButtonActionPerformed(e);
            }
        });
        
        statsClearButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                statsClearButtonActionPerformed(e);
            }
        });
        
        decimalCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                decimalCheckBoxActionPerformed(e);
            }
        });
        
        sendEvButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendEvButtonActionPerformed(e);
            }
        });
        
        // set file chooser to a default
        logFileChooser.setSelectedFile(new File("monitorLog.txt"));
        
        linesBuffer[CAN] = new StringBuffer();
        linesBuffer[CBUS] = new StringBuffer();
    }
    
    /**
     * Special version of pack that holds the overall frame size constant.
     */
    void packInside() {
        if (getTopLevelAncestor()!=null){
            Dimension d = ((javax.swing.JFrame)getTopLevelAncestor()).getSize();
            ((javax.swing.JFrame)getTopLevelAncestor()).setMinimumSize(d);
            ((javax.swing.JFrame)getTopLevelAncestor()).setPreferredSize(d);
            ((javax.swing.JFrame)getTopLevelAncestor()).pack();
            
            
        }
    }
    

    
    public void nextLine(String line, String decoded, String priorities, int filter) {
        // handle display of traffic
        // line is the traffic in 'normal form', decoded is the decoded,
        // protocol specific, form
        // Both should contain the same number of well-formed lines, e.g. end
        // with \n
        StringBuffer sbCan = new StringBuffer(80);
        StringBuffer sbCbus = new StringBuffer(80);
        final int filterIndex = filter;
        log.debug("_filterFrame: "+_filterFrame+" filter: "+filter);
        if (filterIndex >= 0) {
            final Color filterColor = _filterFrame.getColor(filter);
            cbusHighlightPainter = new cbusHighlightPainter(filterColor);
        }
        
        // display the timestamp if requested
        if ( timeCheckBox.isSelected() ) {
            sbCan.append(df.format(new Date())).append( ": " ) ;
            sbCbus.append(df.format(new Date())).append( ": " ) ;
        }
        
        // display CBUS the priorities if requested
        if ( priCheckBox.isSelected() ) {
            sbCbus.append((priorities+" "));
        }
        
        if (filterIndex >= 0) {
            sbCan.append(("Filter "+(filterIndex+1)+": ")) ;
            sbCbus.append(("Filter "+(filterIndex+1)+": ")) ;
        }
        
        // display decoded data
        sbCan.append(line);
        sbCbus.append(decoded);
//        synchronized( linesBufferCbus ) {
        synchronized( linesBuffer ) {
            linesBuffer[CAN].append( sbCan.toString() );
            linesBuffer[CBUS].append( sbCbus.toString() );
        }
        
        // if not frozen, display it in the Swing thread
        if (!freezeButton.isSelected()) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    synchronized( linesBuffer ) {
                        final int start = monTextPaneCbus.getText().length();
                        monTextPaneCan.append( linesBuffer[CAN].toString() );
                        monTextPaneCbus.append( linesBuffer[CBUS].toString() );
                        final int end = monTextPaneCbus.getText().length();
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
                        try {
                            if (filterIndex >= 0) {
                                log.debug("Add highlight start: "+start+" end: "+end);
                                cbusHighlighter.addHighlight(start, end-1, cbusHighlightPainter);
                            }
                        } catch (BadLocationException e) {
                            // do nothing
                        }
                        linesBuffer[CAN].setLength(0) ;
                        linesBuffer[CBUS].setLength(0) ;
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
                int j = 0;
                int lim = sbCbus.length();
                StringBuffer out = new StringBuffer(sbCbus.length()+10);  // arbitrary guess at space
                for ( j = 0; j<lim; j++) {
                    if (sbCbus.charAt(j) == '\n')
                        out.append(newline);
                    else
                        out.append(sbCbus.charAt(j));
                }
                logLine = new String(out);
            }
            logStream.print(logLine);
        }
    }
    
    String newline = System.getProperty("line.separator");
    
    public synchronized void clearButtonActionPerformed(java.awt.event.ActionEvent e) {
        // clear the monitoring history
        synchronized( linesBuffer ) {
            linesBuffer[CAN].setLength(0);
            linesBuffer[CBUS].setLength(0);
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
            //File file = logFileChooser.getSelectedFile();
            // if we were currently logging, start the new file
            if (loggingNow) startLogButtonActionPerformed(e);
        }
    }
    
    public void enterButtonActionPerformed(java.awt.event.ActionEvent e) {
        nextLine(entryField.getText() + "\n", entryField.getText() + "\n", "", -1);
    }
    
    public void filterButtonActionPerformed(java.awt.event.ActionEvent e) {
        log.debug("Cbus Console filter button action performed");
        if (_filterFrame == null) {
            _filterFrame = new CbusEventFilterFrame(this);
            try {
                _filterFrame.initComponents();
            } catch (Exception ex) {
                log.error("Exception: "+ex.toString());
            }
            _filterFrame.setVisible(true);
        } else {
            _filterFrame.setState(Frame.NORMAL);
            _filterFrame.setVisible(true);
        }
    }
    
//    public void filterFrameClosed() {
//        log.debug("Cbus Console filter frame closed");
//        nextLine("All filters removed\n", "All filters removed\n", "", -1);
//        _filterFrame = null;
//    }
    
    public void filterOn(int index, int nn, boolean nnEn, int ev, boolean evEn, int ty) {
        log.debug("Cbus Console filter applied");
        StringBuffer sb = new StringBuffer(80);
        if (nnEn) {
            sb.append(("Node "+nn+" "));
        }
        if (evEn) {
            sb.append(("Event "+ev+" "));
        }
        if (ty == CbusConstants.EVENT_ON) {
            sb.append("ON");
        } else if (ty == CbusConstants.EVENT_OFF) {
            sb.append("OFF");
        } else {
            sb.append("On or OFF");
        }
        sb.append("\n");
        nextLine(sb.toString(), sb.toString(), "", index);
    }
    
    public void filterOff(int index) {
       log.debug("Cbus Console filter removed");
       nextLine(" closed\n", " closed\n", "", index);
    }
    
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        int j;
        int data, data2;
        CanMessage m = new CanMessage(tc.getCanid());
        data = parseBinDecHexByte(dynPriField.getText(), 2, _decimal, "CBUS Console", "Invalid Dynamic Priority Value");
        if (data == -1) return;
        data2 = parseBinDecHexByte(minPriField.getText(), 3, _decimal, "CBUS Console", "Invalid Minor Priority Value");
        if (data2 == -1) return;
        m.setHeader(data*4 + data2);
        for (j=0; j<8; j++) {
            if (!dataFields[j].getText().equals("")) {
                data = parseBinDecHexByte(dataFields[j].getText(), 255, _decimal, "CBUS Console",
                        "Invalid Data Value in d"+j);
                if (data == -1) return;
                m.setElement(j, data);
                if (j==0) data2 = data;     // save OPC(d0) for later
            } else {
                break;
            }
        }
        if (j==0) {
            JOptionPane.showMessageDialog(null, "You must enter at least an opcode",
                    "CBUS Console", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Does the number of data match the opcode?
        // Subtract one as loop variable will have incremented
        if ((j-1) != (data2>>5)) {
            JOptionPane.showMessageDialog(null, "Number of data bytes entered\n"
                    +"does not match count in d0(OPC):"+(data2>>5),
                    "CBUS Console", JOptionPane.ERROR_MESSAGE);
            return;
        }
        m.setNumDataElements(j);
        // Messages sent by us will not be forwarded back so add to display manually
        message(m);
        tc.sendCanMessage(m, this);
    }
    
    public void dataClearButtonActionPerformed(java.awt.event.ActionEvent e) {
        int j;
        dynPriField.setText("2");
        minPriField.setText("3");
        for (j=0; j<8; j++) {
            dataFields[j].setText("");
        }

    }
    
    synchronized public void statsClearButtonActionPerformed(java.awt.event.ActionEvent e) {
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
        for (int j=0; j<8; j++) {
            dataFields[j].setText(lastRxDataFields[j].getText());
        }
    }
    
    public void sendEvButtonActionPerformed(java.awt.event.ActionEvent e) {
        int nn, ev;
        CanMessage m = new CanMessage(tc.getCanid());
        nn = parseBinDecHexByte(nnField.getText(), 65536, _decimal, "CBUS Console", "Invalid Node Number");
        if (nn == -1) return;
        ev = parseBinDecHexByte(evField.getText(), 65536, _decimal, "CBUS Console", "Invalid Event");
        if (ev == -1) return;
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY*4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        if (onButton.isSelected()) {
            if (nn > 0) {
                m.setElement(0, CbusConstants.CBUS_ACON);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASON);
            }
        } else {
            if (nn > 0) {
                m.setElement(0, CbusConstants.CBUS_ACOF);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASOF);
            }
        }
        m.setElement(1, nn>>8);
        m.setElement(2, nn&0xff);
        m.setElement(3, ev>>8);
        m.setElement(4, ev&0xff);
        m.setNumDataElements(5);
        // Messages sent by us will not be forwarded back so add to display manually
        message(m);
        tc.sendCanMessage(m, this);
    }
    
    public synchronized String getCanFrameText() {
        return new String(linesBuffer[CAN]);
    }
    
    public synchronized String getCbusFrameText() {
        return new String(linesBuffer[CBUS]);
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="IS2_INCONSISTENT_SYNC", justification="separately interlocked")
    PrintStream logStream = null;
    
    // to get a time string
    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
    
    StringBuffer[] linesBuffer = new StringBuffer[2];
    static private int CAN = 0;
    static private int CBUS = 1;
    static private int MAX_LINES = 500 ;
    
    @Override
    public synchronized void message(CanMessage m) {  // receive a message and log it
        nextLine("sent: "+m.toString()+"\n",
                "ID:"+CbusMessage.getId(m)+ " "+(m.isRtr() ? "R " : "N ")+decode(m, m.isExtended(), m.getHeader())+" ["+CbusMessage.toAddress(m)+"]\n",
                "Dyn Pri:"+CbusMessage.getPri(m)/4+" Min Pri:"+(CbusMessage.getPri(m)&3),
                (_filterFrame != null) ? _filterFrame.filter(m) : -1);
        sentCountField.setText(Integer.toString(++_sent));
    }
    
    @Override
    public synchronized void reply(CanReply r) {  // receive a reply message and log it
        int j;
        // Capture most recent received packet
        if (_decimal) {
            lastDynPriField.setText(Integer.toString(CbusMessage.getPri(r)/4));
            lastMinPriField.setText(Integer.toString(CbusMessage.getPri(r)&3));
        } else {
            lastDynPriField.setText(Integer.toHexString(CbusMessage.getPri(r)/4));
            lastMinPriField.setText(Integer.toHexString(CbusMessage.getPri(r)&3));
        }
        // Pay attention to data length in op-code
        for(j=0; j<(r.getElement(0)>>5)+1; j++){
            if (_decimal) {
                lastRxDataFields[j].setText(Integer.toString(r.getElement(j)));
            } else {
                lastRxDataFields[j].setText(Integer.toHexString(r.getElement(j)));
            }
        }
        nextLine("rcvd: "+r.toString()+"\n",
                "ID:"+CbusMessage.getId(r)+" "+(r.isRtr() ? "R " : "N ")+decode(r, r.isExtended(), r.getHeader())+" ["+CbusMessage.toAddress(r)+"]\n",
                "Dyn Pri:"+CbusMessage.getPri(r)/4+" Min Pri:"+(CbusMessage.getPri(r)&3),
                (_filterFrame != null) ? _filterFrame.filter(r) : -1);
        rcvdCountField.setText(Integer.toString(++_rcvd));
    }
    
    /**
     * Return a string representation of a decoded canMessage
     *
     * @param msg CanMessage to be decoded
     * Return String decoded message
     */
    public String decode(AbstractMessage msg, Boolean ext, int header) {
        String str = CbusOpCodes.decode(msg, ext, header);
        return (str);
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
                    errTitle, JOptionPane.ERROR_MESSAGE);
            data = -1;
        }
        return data;
    }
    
    // An instance of the private subclass of the default highlight painter
    Highlighter.HighlightPainter cbusHighlightPainter;
    
    // A private subclass of the default highlight painter
    class cbusHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public cbusHighlightPainter(Color color) {
            super(color);
        }
    }
    
    transient private int _sent;
    transient private int _rcvd;
    private boolean _decimal;
    private CbusEventFilterFrame _filterFrame;
    
        /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {
        public Default() {
            super("CBUS Console", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                CbusConsolePane.class.getName(), 
                jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }
    
    static Logger log = Logger.getLogger(CbusConsolePane.class.getName());
}
