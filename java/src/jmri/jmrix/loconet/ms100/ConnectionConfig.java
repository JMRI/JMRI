// ConnectionConfig.java
package jmri.jmrix.loconet.ms100;

import jmri.util.SystemType;

/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via an LocoNet MS100Adapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
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

    /**
     * Provide this adapter name, if it's available on this system.
     *
     * @return null if this is a Mac OS X system that can't run MS100
     */
    public String name() {
        if (SystemType.isMacOSX()
                || (SystemType.isWindows() && Double.valueOf(System.getProperty("os.version")) >= 6)) {
            return "(LocoNet MS100 not available)";
        } else {
            return "LocoNet MS100";
        }
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new MS100Adapter();
        }
    }
}
