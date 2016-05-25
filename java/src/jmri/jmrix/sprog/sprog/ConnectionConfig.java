// ConnectionConfig.java
package jmri.jmrix.sprog.sprog;

import jmri.jmrix.sprog.serialdriver.SerialDriverAdapter;
import jmri.util.SystemType;

/**
 * Definition of objects to handle configuring a layout connection via an SPROG
 * SerialDriverAdapter object.
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

    public String name() {
        return "SPROG";
    }

    @Override
    protected String[] getPortFriendlyNames() {
        if (SystemType.isWindows()) {
            return new String[]{"SPROG"};
        }
        return new String[]{};
    }

    protected void setInstance() {
        adapter = SerialDriverAdapter.instance();
    }
}
