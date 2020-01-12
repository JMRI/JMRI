package jmri.jmrix.xpa.swing.xpamon;

import jmri.jmrix.xpa.XpaListener;
import jmri.jmrix.xpa.XpaMessage;
import jmri.jmrix.xpa.XpaSystemConnectionMemo;
import jmri.jmrix.xpa.XpaTrafficController;

/**
 * Panel displaying (and logging) Xpa+Modem command messages.  
 * Derived from XpaMonFrame.
 *
 * @author	Paul Bender Copyright (C) 2004,2016
 */
public class XpaMonPane extends jmri.jmrix.AbstractMonPane implements XpaListener {

    protected XpaTrafficController tc = null;
    protected XpaSystemConnectionMemo memo = null;

    @Override
    public String getTitle() {
        return (Bundle.getMessage("XpaMonFrameTitle"));
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof XpaSystemConnectionMemo) {
            memo = (XpaSystemConnectionMemo) context;
            tc = memo.getXpaTrafficController();
            // connect to the TrafficController
            tc.addXpaListener(this);
        }
    }

    @Override 
    protected void init() {
    }
    
    @Override
    public void dispose() {
        if (tc != null) tc.removeXpaListener(this);
        super.dispose();
    }

    @Override
    public synchronized void message(XpaMessage l) {  // receive a message and log it
        logMessage("Sent: ",l);
    }

    @Override
    public synchronized void reply(XpaMessage l) {  // receive a reply message and log it
        logMessage("Received: ",l);
    }

   /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.util.swing.JmriNamedPaneAction {

        public Default() {
            super(java.util.ResourceBundle.
                    getBundle("jmri.jmrix.xpa.swing.XpaSwingBundle").
                    getString("XpaMonFrameTitle"), XpaMonPane.class.getName());
            setContext(jmri.InstanceManager.
                    getDefault(XpaSystemConnectionMemo.class));
        }
    }

}
