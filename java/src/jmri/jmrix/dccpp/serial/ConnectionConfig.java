// ConnectionConfig.java
package jmri.jmrix.dccpp.serial;

import jmri.util.SystemType;

/**
 * Handle configuring a DCC++ layout connection via a Serial adaptor.
 * <P>
 * This uses the {@link DCCppAdapter} class to do the actual connection.
 *
 * @author Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 *
 * @see DCCppAdapter
 *
 * Based on jmri.jmrix.lenz.liusb.ConnectionConfig by Paul Bender
 */
public class ConnectionConfig extends jmri.jmrix.dccpp.AbstractDCCppSerialConnectionConfig {

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
        return "DCC++ Serial Port";
    }

    @Override
    protected String[] getPortFriendlyNames() {
        if (SystemType.isWindows()) {
            return new String[]{"DCC++ Serial Port", "DCC++_Serial"};
        }
        return new String[]{};
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new DCCppAdapter();
        }
    }
}
