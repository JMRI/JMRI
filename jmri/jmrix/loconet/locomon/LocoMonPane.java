// LocoMonPane.java

package jmri.jmrix.loconet.locomon;

import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.swing.*;

/**
 * LocoNet Monitor pane displaying (and logging) LocoNet messages
 * @author	   Bob Jacobsen   Copyright (C) 2001, 2008, 2010
 * @version   $Revision: 1.3 $
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
    
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the LnTrafficController
        memo.getLnTrafficController().addLocoNetListener(~0, this);
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoMonPane.class.getName());
}
