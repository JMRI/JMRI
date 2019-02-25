package jmri.jmrix.loconet.pr4;

import jmri.util.SystemType;

/**
 * Definition of objects to handle configuring a PR4 layout connection via a
 * PR2Adapter object.
 * <p>
 * Copied from loconet.pr3.ConnectionConfig.java
 * <p>
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2008, 2010
 * @author B. Milhaupt Copyright (C) 2019
 */

public class ConnectionConfig extends jmri.jmrix.loconet.pr3.ConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no preexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "LocoNet PR4"; // NOI18N
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new PR4Adapter();
        }
    }
}
