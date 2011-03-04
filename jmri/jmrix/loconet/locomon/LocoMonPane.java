// LocoMonPane.java

package jmri.jmrix.loconet.locomon;

import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.swing.*;

/**
 * LocoNet Monitor pane displaying (and logging) LocoNet messages
 * @author	   Bob Jacobsen   Copyright (C) 2001, 2008, 2010
 * @version   $Revision: 1.8 $
 */
public class LocoMonPane extends jmri.jmrix.AbstractMonPane implements LocoNetListener, LnPanelInterface {

    public LocoMonPane() {
        super();
    }

    public String getHelpTarget() { return "package.jmri.jmrix.loconet.locomon.LocoMonFrame"; }
    public String getTitle() { 
        return LocoNetBundle.bundle().getString("MenuItemLocoNetMonitor");
    }
    
    public void dispose() {
        // disconnect from the LnTrafficController
        memo.getLnTrafficController().removeLocoNetListener(~0,this);
        // and unwind swing
        super.dispose();
    }

    public void init() {}
    
    LocoNetSystemConnectionMemo memo;
    
    public void initContext(Object context) {
        if (context instanceof LocoNetSystemConnectionMemo ) {
            initComponents((LocoNetSystemConnectionMemo) context);
        }
    }

    public void initComponents(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the LnTrafficController
        memo.getLnTrafficController().addLocoNetListener(~0, this);
        jmri.TurnoutManager tm = jmri.InstanceManager.turnoutManagerInstance();
        llnmon.setLocoNetTurnoutManager(tm);
        llnmon.setLocoNetSensorManager(jmri.InstanceManager.sensorManagerInstance());
        llnmon.setLocoNetReporterManager(jmri.InstanceManager.reporterManagerInstance());
    }

    
    public synchronized void message(LocoNetMessage l) {  // receive a LocoNet message and log it
        // display the raw data if requested
        String raw = null ;
        if( rawCheckBox.isSelected() )
			raw = l.toString();

        // display the decoded data
        // we use Llnmon to format, expect it to provide consistent \n after each line
        nextLine(llnmon.displayMessage(l), raw );
    }

    jmri.jmrix.loconet.locomon.Llnmon llnmon = new jmri.jmrix.loconet.locomon.Llnmon();

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {
        public Default() {
            super(LocoNetBundle.bundle().getString("MenuItemLocoNetMonitor"), 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                LocoMonPane.class.getName(), 
                jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoMonPane.class.getName());
}
