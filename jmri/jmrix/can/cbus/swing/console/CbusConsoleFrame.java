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

import jmri.util.JmriJFrame;

import jmri.jmrix.can.*;

/**
 * Frame for Cbus Console
 * 
 * @author			Andrew Crosland   Copyright (C) 2008
 * @version			$Revision: 1.1 $
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
    protected JButton openFileChooserButton = new JButton();
    protected JTextField entryField = new JTextField();
    protected JButton enterButton = new JButton();
    protected JButton sendButton = new JButton();

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
        tc = jmri.jmrix.can.adapters.gridconnect.GcTrafficController.instance();
        tc.addCanListener(this);
    }
    
    public void dispose() {
        tc.removeCanListener(this);
        super.dispose();
    }
    
    public void initComponents() throws Exception {
        // the following code sets the frame's initial state
        
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
        
        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");
        
        entryField.setToolTipText("Enter text here, then click button to include it in log");
        
        // fix a width for raw field for current character set
        JTextField tCan = new JTextField(50);
        int x = jScrollPane1Can.getPreferredSize().width+tCan.getPreferredSize().width;
        int y = jScrollPane1Can.getPreferredSize().height+10*tCan.getPreferredSize().height;
        
        jScrollPane1Can.getViewport().add(monTextPaneCan);
        jScrollPane1Can.setPreferredSize(new Dimension(x, y));
        jScrollPane1Can.setVisible(true);
        
        // fix a width for Cbus field for current character set
        JTextField tCbus = new JTextField(50);
        x = jScrollPane1Cbus.getPreferredSize().width+tCbus.getPreferredSize().width;
        y = jScrollPane1Cbus.getPreferredSize().height+10*tCbus.getPreferredSize().height;
        
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
        
        openFileChooserButton.setText("Choose log file");
        openFileChooserButton.setVisible(true);
        openFileChooserButton.setToolTipText("Click here to select a new output log file");
        
        setTitle(title());
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        
        // add items to GUI
        // Constrain scroll panels to be side-by-side
        JPanel paneB = new JPanel();
        paneB.setLayout(new BoxLayout(paneB, BoxLayout.X_AXIS));
        paneB.add(jScrollPane1Can);
        paneB.add(jScrollPane1Cbus);
        getContentPane().add(paneB);
        
        JPanel paneA = new JPanel();
        paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));
        
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        pane1.add(clearButton);
        pane1.add(freezeButton);
        pane1.add(timeCheckBox);
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
        
        JPanel pane4 = new JPanel();
        pane4.setLayout(new BoxLayout(pane4, BoxLayout.X_AXIS));
        pane4.add(sendButton);
        paneA.add(pane4);

        getContentPane().add(paneA);
        
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
        
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
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
    	addHelpMenu("package.jmri.jmrix.AbstractMonFrame", true);
    }
    
    public void nextLine(String line, String decoded) {
        // handle display of traffic
        // line is the traffic in 'normal form', decoded is the decoded,
        // protocol specific, form
        // Both should contain the same number of well-formed lines, e.g. end
        // with \n
        StringBuffer sbCan = new StringBuffer(120);
        StringBuffer sbCbus = new StringBuffer(120);
        
        // display the timestamp if requested
        if ( timeCheckBox.isSelected() ) {
            sbCan.append(df.format(new Date())).append( ": " ) ;
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
        nextLine(entryField.getText() + "\n", "");
    }
    
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        CanMessage m = new CanMessage(2);
        m.setId(100);
        m.setElement(0,1);
        m.setElement(1,2);
        // Messages sent by us will not be forwarded back so add to display manually
        message(m);
        tc.sendCanMessage(m, this);
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
        nextLine("sent: "+l.toString()+"\n", decode(l));
    }
    
    public synchronized void reply(CanReply l) {  // receive a reply message and log it
        replyString = l.toString();
        nextLine("rcvd: "+replyString+"\n", decode(l));
    }
    
    /**
     * Return a string representation of a decoded canMessage
     *
     * @param msg CanMessage to be decoded
     * Return String decoded message
     */
    public String decode(CanMessage msg) {
        // *** temporary
        return ("decoded: "+msg.toString()+"\n");
    }
    
    /**
     * Return a string representation of a decoded canReply
     *
     * @param rep CanReply to be decoded
     * Return String decoded message
     */
    public String decode(CanReply rep) {
        // *** temporary
        String ret = "";
        return ("decoded: "+ret);
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CbusConsoleFrame.class.getName());
}
