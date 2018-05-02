package jmri.jmrix.dccpp.swing.mon;

import jmri.jmrix.dccpp.DCCppListener;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel displaying (and logging) DCC++ messages derived from DCCppMonFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2004-2014
 * @author Giorgio Terdina Copyright (C) 2007
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppMonPane extends jmri.jmrix.AbstractMonPane implements DCCppListener {

    final java.util.ResourceBundle rb
            = java.util.ResourceBundle.
                    getBundle("jmri.jmrix.dccpp.swing.DCCppSwingBundle");

    protected DCCppTrafficController tc = null;
    protected DCCppSystemConnectionMemo memo = null;

    @Override
    public String getTitle() {
        return (rb.getString("DCCppMonFrameTitle"));
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof DCCppSystemConnectionMemo) {
            memo = (DCCppSystemConnectionMemo) context;
            tc = memo.getDCCppTrafficController();
            // connect to the TrafficController
            tc.addDCCppListener(~0, this);
        }
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

    @Override
    public synchronized void message(DCCppReply l) {
        // receive a DCC++ message and log it
        // display the raw data if requested
        // Since DCC++ is text-based traffic, this is good enough for now.
        // TODO: Provide "beautified" output later.
        StringBuilder raw = new StringBuilder("RX: ");
        if (rawCheckBox.isSelected()) {
            raw.append(l.toString());
        }

        log.debug("Message in Monitor: {} opcode {}", l.toString(), Character.toString(l.getOpCodeChar()));

        String text = l.toMonitorString();

        nextLine(text + "\n", raw.toString());
    }

    // listen for the messages to the Base Station
    @SuppressWarnings("fallthrough")
    @Override
    public synchronized void message(DCCppMessage l) {
        // display the raw data if requested  
        // Since DCC++ is text-based traffic, this is good enough for now.
        // TODO: Provide "beautified" output later.
        StringBuilder raw = new StringBuilder("TX: ");
        if (rawCheckBox.isSelected()) {
            raw.append(l.toString());
        }

        String text = l.toMonitorString();

        nextLine(text + "\n", raw.toString());

    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    /**
     * We need to calculate the locomotive address when doing the translations
     * back to text. XpressNet Messages will have these as two elements, which
     * need to get translated back into a single address by reversing the
     * formulas used to calculate them in the first place.
     */
    /* NOT USED
    private int calcLocoAddress(int AH, int AL) {
        if (AH == 0x00) {
            // if AH is 0, this is a short address
            return (AL);
        } else {
            // This must be a long address
            int address = 0;
            address = ((AH * 256) & 0xFF00);
            address += (AL & 0xFF);
            address -= 0xC000;
            return (address);
        }
    }
     */
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.util.swing.JmriNamedPaneAction {

        public Default() {
            super(java.util.ResourceBundle.
                    getBundle("jmri.jmrix.dccpp.swing.DCCppSwingBundle").
                    getString("DCCppMonFrameTitle"), DCCppMonPane.class.getName());
            setContext(jmri.InstanceManager.
                    getDefault(DCCppSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppMonPane.class);

}
