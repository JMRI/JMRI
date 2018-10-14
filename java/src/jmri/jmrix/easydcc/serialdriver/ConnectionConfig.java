package jmri.jmrix.easydcc.serialdriver;

/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via an EasyDccSerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
  */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process.
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no preexisting adapter.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("AdapterSerialName");
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }

}
