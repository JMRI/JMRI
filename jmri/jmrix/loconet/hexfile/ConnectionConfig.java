// ConnectionConfig.java

package jmri.jmrix.loconet.hexfile;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection
 * via a LocoNet hexfile emulator
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.1 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        super(p);
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() { return "LocoNet Hexfile"; }

    public void loadDetails(JPanel details) {
        details.add(new JLabel("No options"));
    }

    protected void setInstance() {
        log.error("Unexpected call to setInstance");
        new Exception().printStackTrace();
    }
}

