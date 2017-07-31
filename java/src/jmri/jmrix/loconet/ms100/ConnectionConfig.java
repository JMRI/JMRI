package jmri.jmrix.loconet.ms100;

/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via an LocoNet MS100Adapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
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
    @Override
    public String name() {
        return "LocoNet MS100";
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new MS100Adapter();
        }
    }
}
