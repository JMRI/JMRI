// ConnectionConfig.java
package jmri.jmrix.loconet.hexfile;

/**
 * Definition of objects to handle configuring a layout connection via a LocoNet
 * hexfile emulator
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @version	$Revision$
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSimulatorConnectionConfig {

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
        return "LocoNet Simulator";
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new LnHexFilePort();
        }
    }
}
