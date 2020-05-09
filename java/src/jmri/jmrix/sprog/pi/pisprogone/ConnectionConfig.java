package jmri.jmrix.sprog.pi.pisprogone;

import jmri.util.SystemType;

/**
 * Definition of objects to handle configuring a layout connection via an SPROG
 * SerialDriverAdapter object.
 *
 * @author Andrew Crosland Copyright (C) 2016
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p Serial port adapter.
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
        return Bundle.getMessage("PiSprog1ProgrammerTitle");
    }

    @Override
    protected String[] getPortFriendlyNames() {
        if (SystemType.isWindows()) {
            return new String[]{"SPROG"};
        }
        return new String[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if(adapter == null) {
           adapter = new PiSprogOneSerialDriverAdapter();
        }
    }

}
