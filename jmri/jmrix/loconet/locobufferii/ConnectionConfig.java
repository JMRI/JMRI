// ConnectionConfig.java

package jmri.jmrix.loconet.locobufferii;


/**
 * Definition of objects to handle configuring an LocoBufferII layout connection
 * via a LocoBufferIIAdapter object.
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

    public String name() { return "LocoNet LocoBufferII"; }

    protected void setInstance() { adapter = jmri.jmrix.loconet.locobufferii.LocoBufferIIAdapter.instance(); }
}

