package jmri.jmrix.dccpp.swing.mon;

import jmri.jmrix.dccpp.DCCppListener;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.jmrix.dccpp.serial.SerialDCCppPacketizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Frame displaying (and logging) DCCpp command messages.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Chuck Catania  Copyright (C) 2014, 2016, 2017
 * @author mstevetodd  Copyright (C) 2021
 */
public class DCCppMonFrame extends jmri.jmrix.AbstractMonFrame implements DCCppListener {

    // member declarations
    final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrix.dccpp.swing.DCCppSwingBundle"); // NOI18N
      
    protected DCCppTrafficController tc = null;
    protected DCCppSystemConnectionMemo _memo = null;

    protected SerialDCCppPacketizer serialDCCppTC = null;

    protected final JPanel serialPane = new JPanel();
    protected final JLabel queuedEntriesLabel = new JLabel("", SwingConstants.LEFT); // NOI18N
    protected final JToggleButton pauseRefreshButton = new JToggleButton();
    protected final JButton clearRefreshQueueButton = new JButton();
    
    public DCCppMonFrame(DCCppSystemConnectionMemo memo) {
        super();
        _memo = memo;        
    }

    @Override
    public void dispose() { 
        super.dispose();
  }

    @Override
    protected String title() {
        return rb.getString("DCCppMonFrameTitle")+" (" + _memo.getSystemPrefix() + ")";  // NOI18N
    }

    @Override
    protected void init() {

        // connect to TrafficController
        tc = _memo.getDCCppTrafficController();
        tc.addDCCppListener(~0, this);
        
        if ((tc instanceof SerialDCCppPacketizer) && tc.getCommandStation().isFunctionRefreshRequired()) {
            serialDCCppTC = (SerialDCCppPacketizer) tc;

            pauseRefreshButton.setSelected(!serialDCCppTC.isActiveRefresh());

            refreshQueuedMessages();

            add(serialPane, BorderLayout.PAGE_END);
        }

        // Create the background function refreshing-related buttons and add
        // them to a panel. Will be hidden if not required by command station.
        final JLabel functionLabel = new JLabel(Bundle.getMessage("LabelFunctionRefresh"), SwingConstants.LEFT); // NOI18N

        pauseRefreshButton.setText(Bundle.getMessage("ButtonPauseRefresh")); // NOI18N
        pauseRefreshButton.setVisible(true);
        pauseRefreshButton.setToolTipText(Bundle.getMessage("TooltipPauseRefresh")); // NOI18N
        // the selected state of pauseRefreshButton will be set when the context
        // is created

        clearRefreshQueueButton.setText(Bundle.getMessage("ButtonClearRefreshQueue")); // NOI18N
        clearRefreshQueueButton.setVisible(true);
        clearRefreshQueueButton.setToolTipText(Bundle.getMessage("TooltipClearRefreshQueue")); // NOI18N

        serialPane.setLayout(new BoxLayout(serialPane, BoxLayout.LINE_AXIS));
        serialPane.add(functionLabel);
        serialPane.add(Box.createRigidArea(new Dimension(5, 0)));
        serialPane.add(pauseRefreshButton);
        serialPane.add(Box.createRigidArea(new Dimension(5, 0)));
        serialPane.add(clearRefreshQueueButton);
        serialPane.add(Box.createRigidArea(new Dimension(5, 0)));
        serialPane.add(queuedEntriesLabel);

        pauseRefreshButton.addActionListener((final java.awt.event.ActionEvent e) -> {
            pauseButtonEvent(e);
        });

        clearRefreshQueueButton.addActionListener((final java.awt.event.ActionEvent e) -> {
            clearButtonEvent(e);
        });
        
        pack();        
    }
    
    /**
     * Define system-specific help item.
     */
//    @Override
//    protected void setHelp() {
//        addHelpMenu("package.jmri.jmrix.DCCpp.DCCpp.DCCppmon.DCCppMonFrame", true);  // NOI18N
//    }
    
    //-------------------
    //  Transmit Packets
    //-------------------
    @Override
    public synchronized void message(DCCppMessage l) {
        // display the raw data if requested  
        StringBuilder raw = new StringBuilder();
        if (rawCheckBox.isSelected()) {
            raw.append(l.toString());
        }

        // display the decoded data
        String text = l.toMonitorString();
        nextLine("TX: " + text + "\n", raw.toString());

        refreshQueuedMessages();

    }

    volatile PrintStream logStream = null;

    // to get a time string
    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    StringBuffer linesBuffer = new StringBuffer();
    private final static Logger log = LoggerFactory.getLogger(DCCppMonFrame.class);

    @Override
    public void message(DCCppReply l) {
        // receive a DCC++ message and log it
        // display the raw data if requested
        if (log.isDebugEnabled()) {
            log.debug("Message in Monitor: '{}' opcode {}", l, Character.toString(l.getOpCodeChar()));
        }

        // display the raw data if requested  
        StringBuilder raw = new StringBuilder();
        if (rawCheckBox.isSelected()) {
            raw.append(l.toString());
        }

        // display the decoded data
        String text = l.toMonitorString();
        nextLine("RX: " + text + "\n", raw.toString());

        //enable or disable the refresh pane based on support by command station
        if (l.isStatusReply()) { 
            if (tc.getCommandStation().isFunctionRefreshRequired()) { 
                serialPane.setVisible(true);
            } else {
                serialPane.setVisible(false);
            }
        }
       
    }

    @Override
    public void notifyTimeout(DCCppMessage msg) {
        // TODO Auto-generated method stub
        
    }

    private void clearButtonEvent(final ActionEvent e) {
        if (serialDCCppTC != null)
            serialDCCppTC.clearRefreshQueue();

        refreshQueuedMessages();
    }
    
    private void pauseButtonEvent(final ActionEvent e) {
        final JToggleButton source = (JToggleButton) e.getSource();

        if (serialDCCppTC != null)
            serialDCCppTC.setActiveRefresh(!source.isSelected());
    }

    private int previouslyQueuedMessages = -1;

    public synchronized void refreshQueuedMessages() {
        if (serialDCCppTC != null) {
            final int currentlyQueuedMessages = serialDCCppTC.getQueueLength();

            if (currentlyQueuedMessages != previouslyQueuedMessages) {
                queuedEntriesLabel.setText(Bundle.getMessage("LabelQueuedEntries", String.valueOf(currentlyQueuedMessages))); // NOI18N

                clearRefreshQueueButton.setEnabled(currentlyQueuedMessages > 0);

                previouslyQueuedMessages = currentlyQueuedMessages;
            }
        }
    }
}

