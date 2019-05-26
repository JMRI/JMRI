package jmri.jmrix.loconet.Intellibox;

/**
 * Definition of objects to handle configuring an Intellibox serial port layout
 * connection via an IntelliboxAdapter object.
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
     * Ctor for a functional Swing object with no pre-existing adapter.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "Intellibox-I (Serial)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new IntelliboxAdapter();
        }
    }

}
