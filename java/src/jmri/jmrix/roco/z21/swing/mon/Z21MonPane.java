package jmri.jmrix.roco.z21.swing.mon;

import jmri.jmrix.roco.z21.Z21Listener;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21Reply;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import jmri.jmrix.roco.z21.Z21TrafficController;

/**
 * Panel displaying (and logging) Z21 messages derived from Z21MonFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2004-2014
 * @author Giorgio Terdina Copyright (C) 2007
 */
public class Z21MonPane extends jmri.jmrix.AbstractMonPane implements Z21Listener {

    protected Z21SystemConnectionMemo memo = null;

    @Override
    public String getTitle() {
        return (Bundle.getMessage("Z21TrafficTitle"));
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof Z21SystemConnectionMemo) {
            memo = (Z21SystemConnectionMemo) context;
            // connect to the TrafficController
            memo.getTrafficController().addz21Listener(this);
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
        // disconnect from the TrafficController
        memo.getTrafficController().removez21Listener(this);
        // and unwind swing
        super.dispose();
    }

    @Override
    public synchronized void reply(Z21Reply l) { // receive an XpressNet message and log it
        logMessage(l);
    }

    /**
     * Listen for the messages to the LI100/LI101
     */
    @Override
    public synchronized void message(Z21Message l) {
	logMessage(l);
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.util.swing.JmriNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("Z21TrafficTitle"), 
			    Z21MonPane.class.getName());
            setContext(jmri.InstanceManager.
                    getDefault(Z21SystemConnectionMemo.class));
        }
    }

}
