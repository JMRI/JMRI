// ConnectionConfig.java

package jmri.jmrix.zimo.mx1;


/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via a zimo MX-1 SerialDriverAdapter object.
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

    public String name() { return "Zimo MX-1"; }

    protected void setInstance() { adapter = Mx1Adapter.instance(); }
}

