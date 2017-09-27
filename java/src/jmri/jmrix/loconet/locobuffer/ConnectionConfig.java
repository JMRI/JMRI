package jmri.jmrix.loconet.locobuffer;

/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via a LocoBufferAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2010
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
     * Ctor for a functional Swing object with no preexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public boolean isOptList2Advanced() {
        return false;
    }

    @Override
    public String name() {
        return "LocoNet LocoBuffer"; // NOI18N
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new LocoBufferAdapter();
        }
    }
}
