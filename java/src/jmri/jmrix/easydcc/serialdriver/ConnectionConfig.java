package jmri.jmrix.easydcc.serialdriver;

/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via an NCE SerialDriverAdapter object.
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

    @Override
    public String name() {
        return "EasyDCC via Serial";
    }

    @Override
    protected void setInstance() {
        adapter = SerialDriverAdapter.instance();
    }
}
