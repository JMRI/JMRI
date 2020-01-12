package jmri.jmrix.dccpp.swing.mon;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.dccpp.DCCppListener;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.jmrix.dccpp.serial.SerialDCCppPacketizer;

/**
 * Panel displaying (and logging) DCC++ messages derived from DCCppMonFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2004-2014
 * @author Giorgio Terdina Copyright (C) 2007
 * @author Mark Underwood Copyright (C) 2015
 * @author Costin Grigoras Copyright (C) 2019
 */
public class DCCppMonPane extends jmri.jmrix.AbstractMonPane implements DCCppListener {
    private static final long serialVersionUID = 1L;

    final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrix.dccpp.swing.DCCppSwingBundle"); // NOI18N

    protected DCCppTrafficController tc = null;
    protected DCCppSystemConnectionMemo memo = null;

    protected SerialDCCppPacketizer serialDCCppTC = null;

    protected final JPanel serialPane = new JPanel();
    protected final JLabel queuedEntriesLabel = new JLabel("", SwingConstants.LEFT); // NOI18N
    protected final JToggleButton pauseRefreshButton = new JToggleButton();
    protected final JButton clearRefreshQueueButton = new JButton();

    @Override
    public String getTitle() {
        return (rb.getString("DCCppMonFrameTitle")); // NOI18N
    }

    @Override
    public void initContext(final Object context) {
        if (context instanceof DCCppSystemConnectionMemo) {
            memo = (DCCppSystemConnectionMemo) context;
            tc = memo.getDCCppTrafficController();
            // connect to the TrafficController
            tc.addDCCppListener(~0, this);

            if ((tc instanceof SerialDCCppPacketizer)) {
                serialDCCppTC = (SerialDCCppPacketizer) tc;

                pauseRefreshButton.setSelected(!serialDCCppTC.isActiveRefresh());

                refreshQueuedMessages();

                add(serialPane, BorderLayout.PAGE_END);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see jmri.jmrix.AbstractMonPane#initComponents()
     */
    @Override
    public void initComponents() {
        super.initComponents();

        // Create the background function refreshing-related buttons and add
        // them to a panel. The panel however will only be added if the traffic
        // controller is an instance of SerialDCCppPacketizer
        final JLabel functionLabel = new JLabel(Bundle.getMessage("LabelFunctionRefresh"), SwingConstants.LEFT); // NOI18N

        pauseRefreshButton.setText(Bundle.getMessage("ButtonPauseRefresh")); // NOI18N
        pauseRefreshButton.setVisible(true);
        pauseRefreshButton.setToolTipText(Bundle.getMessage("TooltipPauseRefresh")); // NOI18N
        // the selected state of pauseRefreshButton will be set when the context
        // is created, in initContext()

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
    }

    /**
     * @param e
     */
    private void clearButtonEvent(final ActionEvent e) {
        if (serialDCCppTC != null)
            serialDCCppTC.clearRefreshQueue();

        refreshQueuedMessages();
    }

    /**
     * @param e
     */
    private void pauseButtonEvent(final ActionEvent e) {
        final JToggleButton source = (JToggleButton) e.getSource();

        if (serialDCCppTC != null)
            serialDCCppTC.setActiveRefresh(!source.isSelected());
    }

    /**
     * Initialize the data source.
     */
    @Override
    protected void init() {
    }

    @Override
    public void dispose() {
        // disconnect from the LnTrafficController
        tc.removeDCCppListener(~0, this);
        // and unwind swing
        super.dispose();
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

    @Override
    public synchronized void message(final DCCppReply l) {
        // receive a DCC++ message and log it
        // display the raw data if requested
        log.debug("Message in Monitor: {} opcode {}", l.toString(), Character.toString(l.getOpCodeChar()));

        logMessage("", "RX: ", l);
    }

    // listen for the messages to the Base Station
    @Override
    public synchronized void message(final DCCppMessage l) {
        // display the raw data if requested
        logMessage("", "TX: ", l);

        refreshQueuedMessages();
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(final DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.util.swing.JmriNamedPaneAction {
        private static final long serialVersionUID = 1L;

        public Default() {
            super(java.util.ResourceBundle.getBundle("jmri.jmrix.dccpp.swing.DCCppSwingBundle").getString("DCCppMonFrameTitle"), DCCppMonPane.class.getName());
            setContext(jmri.InstanceManager.getDefault(DCCppSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppMonPane.class);
}
