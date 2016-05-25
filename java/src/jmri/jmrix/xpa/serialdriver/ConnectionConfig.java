// ConnectionConfig.java
package jmri.jmrix.xpa.serialdriver;

/**
 * Definition of objects to handle configuring a layout connection via an
 * XPA+Modem SerialDriverAdapter object.
 *
 * @author Paul Bender Copyright (C) 2004
 * @version	$Revision$
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() {
        return "XPA-MODEM";
    }

    protected void setInstance() {
        if(adapter == null) {
           adapter = new SerialDriverAdapter();
        }
    }
}
