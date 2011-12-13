// ConnectionConfig.java

package jmri.jmrix.direct.serial;


/**
 * Definition of objects to handle configuring a layout connection
 * via a SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision$
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {

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

    public String name() { return "Direct Drive (Serial)"; }

    protected void setInstance() { adapter = SerialDriverAdapter.instance(); }
}

