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

import javax.swing.*;

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

    private DCCppTrafficController tc = null;
    private DCCppSystemConnectionMemo _memo = null;

    private SerialDCCppPacketizer serialDCCppTC = null;

    private final JPanel serialPane = new JPanel();
    private final JLabel queuedEntriesLabel = new JLabel("", SwingConstants.LEFT); // NOI18N
    private final JToggleButton pauseRefreshButton = new JToggleButton();
    private final JButton clearRefreshQueueButton = new JButton();
    private final JCheckBox displayTranslatedCheckBox = new JCheckBox(Bundle.getMessage("ButtonShowTranslation"));

    private final String doNotDisplayTranslatedCheck = this.getClass().getName() + ".DoNotDisplayTranslated"; // NOI18N

    public DCCppMonFrame(DCCppSystemConnectionMemo memo) {
        super();
        _memo = memo;        
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

        // display the decoded data
        StringBuilder text = new StringBuilder();
        if ( displayTranslatedCheckBox.isSelected() ) {
            text.append("TX: ");
            text.append(l.toMonitorString());
        }
        text.append("\n");

        nextLine(text.toString(), (rawCheckBox.isSelected() ? l.toString() : ""));
        refreshQueuedMessages();

    }

    @Override
    public void message(DCCppReply l) {
        // receive a DCC++ message and log it
        // display the raw data if requested
        if (log.isDebugEnabled()) {
            log.debug("Message in Monitor: '{}' opcode {}", l, Character.toString(l.getOpCodeChar()));
        }

        StringBuilder text = new StringBuilder();
        if ( displayTranslatedCheckBox.isSelected() ) {
            text.append(" RX: ");
            text.append(l.toMonitorString());
        }
        text.append("\n");

        nextLine( text.toString(), (rawCheckBox.isSelected() ? l.toString() : ""));

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

    private void displayTranslatedEvent(final ActionEvent e) {
        if ( neitherCheckBoxSelected() ) {
            rawCheckBox.setSelected(true);
        }
    }

    private void rawCheckBoxEvent(final ActionEvent e) {
        if ( neitherCheckBoxSelected() ) {
            displayTranslatedCheckBox.setSelected(true);
        }
    }

    private boolean neitherCheckBoxSelected() {
        return (!( displayTranslatedCheckBox.isSelected()) ) && (!(rawCheckBox.isSelected()));
    }

    @Override
    public JPanel getCheckBoxPanel(){
        JPanel a = super.getCheckBoxPanel();
        a.add(displayTranslatedCheckBox,0);
        displayTranslatedCheckBox.addActionListener(this::displayTranslatedEvent);
        rawCheckBox.addActionListener(this::rawCheckBoxEvent);
        
        displayTranslatedCheckBox.setSelected(!userPrefs.getSimplePreferenceState(doNotDisplayTranslatedCheck));
        rawCheckBoxEvent(null); // if neither raw on tranalated selected, display translated.
        return a;
    }

    @Override
    public void dispose() {
        if ( tc != null ) {
            tc.removeDCCppListener(~0, this);
        }
        if (userPrefs!=null) {
            userPrefs.setSimplePreferenceState(doNotDisplayTranslatedCheck, !displayTranslatedCheckBox.isSelected());
        }
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppMonFrame.class);

}
