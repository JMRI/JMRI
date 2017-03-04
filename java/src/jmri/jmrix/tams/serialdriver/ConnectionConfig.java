package jmri.jmrix.tams.serialdriver;

/**
 * Definition of objects to handle configuring a layout connection via an TAMS
 * SerialDriverAdapter object.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    public final static String NAME = "MasterControl";

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no existing adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }

}
