package jmri.jmrix.srcp.swing.srcpmon;

import jmri.jmrix.srcp.SRCPListener;
import jmri.jmrix.srcp.SRCPMessage;
import jmri.jmrix.srcp.SRCPReply;
import jmri.jmrix.srcp.SRCPSystemConnectionMemo;
import jmri.jmrix.srcp.SRCPTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane displaying (and logging) SRCP command messages
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @author	Paul Bender Copyright (C) 2018
 */
public class SRCPMonPane extends jmri.jmrix.AbstractMonPane implements SRCPListener {

    private SRCPSystemConnectionMemo _memo = null;
    private SRCPTrafficController tc = null;
    
    public SRCPMonPane() {
        super();
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("MenuItemSRCPCommandMonitorTitle");
    }

    @Override
    protected void init() {
    }

    @Override
    public void initContext(Object context){
        if (context instanceof SRCPSystemConnectionMemo) {
            _memo = (SRCPSystemConnectionMemo) context;
            tc = _memo.getTrafficController();
            // connect to TrafficController
            tc.addSRCPListener(this);
	}
    }

    @Override
    public void dispose() {
        tc.removeSRCPListener(this);
        super.dispose();
    }

    @Override
    public synchronized void message(SRCPMessage l) {  // receive a message and log it

        logMessage("cmd: ",l);
    }

    @Override
    public synchronized void reply(SRCPReply l) {  // receive a reply message and log it
        logMessage("reply: ",l);
    }

    @Override
    public synchronized void reply(jmri.jmrix.srcp.parser.SimpleNode n) {  // receive a reply message and log it
        if (log.isDebugEnabled()) {
            log.debug("reply called with simpleNode " + n.jjtGetValue());
        }
        reply(new SRCPReply(n));
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.util.swing.JmriNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemSRCPCommandMonitorTitle"), SRCPMonPane.class.getName());
            setContext(jmri.InstanceManager.
                    getDefault(SRCPSystemConnectionMemo.class));
        }
    }


    private final static Logger log = LoggerFactory.getLogger(SRCPMonPane.class);

}
