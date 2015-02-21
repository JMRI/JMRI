// ConnectionConfig.java
package jmri.jmrix.loconet.pr3;

import jmri.util.SystemType;

/**
 * Definition of objects to handle configuring a PR3 layout connection via a
 * PR2Adapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2008, 2010
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
        return "LocoNet PR3";
    }

    public boolean isOptList2Advanced() {
        return false;
    }

    @Override
    protected String[] getPortFriendlyNames() {
        if (SystemType.isWindows()) {
            return new String[]{"Communications Port"};
        }
        return new String[]{};
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new PR3Adapter();
        }
    }
}
