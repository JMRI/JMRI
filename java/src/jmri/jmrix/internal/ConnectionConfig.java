package jmri.jmrix.internal;

import jmri.jmrix.AbstractSimulatorConnectionConfig;
import jmri.jmrix.SerialPortAdapter;

/**
 * Definition of objects to handle configuring a virtual layout connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2010
 */
public class ConnectionConfig extends AbstractSimulatorConnectionConfig {

    /**
     * Ctor for an object being created during load process.
     *
     * @param p serial port adapter
     */
    public ConnectionConfig(SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no existing adapter.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "No Connection";
    }
//    public String name() {
//        return Bundle.getMessage("NoConnectionChoice");
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new InternalAdapter();
        }
    }

}
