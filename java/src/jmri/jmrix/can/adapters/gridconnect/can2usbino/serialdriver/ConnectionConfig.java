// ConnectionConfig.java

package jmri.jmrix.can.adapters.gridconnect.can2usbino.serialdriver;

/**
 * Definition of objects to handle configuring a layout connection
 * via a Canusb SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2012
 * @author      Andrew Crosland 2008
 * @version	$Revision: 19909 $
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

    public String name() { return "CAN via TCH Tech CAN/USB adapter"; }
    
    protected void setInstance() { 
        if(adapter ==null){
            adapter = new SerialDriverAdapter();
        }
    }
}

