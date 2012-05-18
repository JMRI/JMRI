// ConnectionConfig.java

package jmri.jmrix.can.adapters.loopback;
import java.util.ResourceBundle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import jmri.jmrix.can.ConfigurationManager;

/**
 * Definition of objects to handle configuring a layout connection
 * via a LocoNet hexfile emulator
 *
 * @author      Bob Jacobsen   Copyright (C) 2008
 * @version	$Revision$
 */
public class ConnectionConfig  extends jmri.jmrix.can.adapters.ConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        super(p);
    }    
    
    // Needed for instantiation by reflection, do not remove.
    public ConnectionConfig() {
        super();
    }

    public String name() { return "CAN Simulation"; }

    protected void setInstance() {
        if(adapter ==null){
            adapter = new Port();
        }
    }    
}

