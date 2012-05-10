// ConnectionConfig.java

package jmri.jmrix.can.adapters.lawicell.canusb.serialdriver;

import java.util.ResourceBundle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import jmri.jmrix.can.ConfigurationManager;

/**
 * Definition of objects to handle configuring a layout connection
 * via a Canusb CanUsbDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2008
 * @author      Andrew Crosland 2008
 * @version	    $Revision$
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        super(p);
    }

    public String name() { return "CAN via Lawicell CANUSB"; }
    
    protected void setInstance() { 
        if(adapter ==null){
            adapter = new CanUsbDriverAdapter();
        }
    }
}

