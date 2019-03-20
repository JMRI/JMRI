package jmri.jmrix.lenz.swing.mon;

import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel displaying (and logging) XpressNet messages derived from XNetMonFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2004-2014
 * @author Giorgio Terdina Copyright (C) 2007
 */
public class XNetMonPane extends jmri.jmrix.AbstractMonPane implements XNetListener {

    protected XNetTrafficController tc = null;
    protected XNetSystemConnectionMemo memo = null;

    @Override
    public String getTitle() {
        return (Bundle.getMessage("MenuItemXNetCommandMonitor"));
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof XNetSystemConnectionMemo) {
            memo = (XNetSystemConnectionMemo) context;
            tc = memo.getXNetTrafficController();
            // connect to the TrafficController
            tc.addXNetListener(~0, this);
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
        tc.removeXNetListener(~0, this);
        // and unwind swing
        super.dispose();
    }

    @Override
    public synchronized void message(XNetReply l) { // receive an XpressNet message and log it
        logMessage(l);
    }

    /**
     * Listen for the messages to the LI100/LI101
     */
    @Override
    public synchronized void message(XNetMessage l) {
	logMessage("","packet:",l);
    }

    /**
     * Handle a timeout notification
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        log.debug("Notified of timeout on message {}", msg.toString());
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.util.swing.JmriNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemXNetCommandMonitor"), XNetMonPane.class.getName());
            setContext(jmri.InstanceManager.
                    getDefault(XNetSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(XNetMonPane.class);

}
