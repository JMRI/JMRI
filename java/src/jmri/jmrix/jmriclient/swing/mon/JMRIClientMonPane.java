package jmri.jmrix.jmriclient.swing.mon;

import jmri.jmrix.jmriclient.JMRIClientListener;
import jmri.jmrix.jmriclient.JMRIClientMessage;
import jmri.jmrix.jmriclient.JMRIClientReply;
import jmri.jmrix.jmriclient.JMRIClientTrafficController;
import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;

/**
 * Pane displaying (and logging) JMRIClient command messages.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class JMRIClientMonPane extends jmri.jmrix.AbstractMonPane implements JMRIClientListener {

    protected JMRIClientTrafficController tc = null;

    public JMRIClientMonPane() {
        super();
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("MenuItemJmriClientCommandMonitorTitle");
    }

    @Override
    protected void init() {
    }
    
    @Override
    public void initContext(Object context) {
        if (context instanceof JMRIClientSystemConnectionMemo) {
            JMRIClientSystemConnectionMemo memo = (JMRIClientSystemConnectionMemo) context;
            // connect to TrafficController
            tc = memo.getJMRIClientTrafficController();
            tc.addJMRIClientListener(this);
	}
    }

    @Override
    public void dispose() {
        tc.removeJMRIClientListener(this);
        tc = null;
	super.dispose();
    }

    @Override
    public synchronized void message(JMRIClientMessage l) {  // receive a message and log it
        logMessage("cmd: ",l);
    }

    @Override
    public synchronized void reply(JMRIClientReply l) {  // receive a reply message and log it
        logMessage("rep: ",l);
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.util.swing.JmriNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemJmriClientCommandMonitorTitle"), JMRIClientMonPane.class.getName());
            setContext(jmri.InstanceManager.
                    getDefault(JMRIClientSystemConnectionMemo.class));
        }
    }

}
