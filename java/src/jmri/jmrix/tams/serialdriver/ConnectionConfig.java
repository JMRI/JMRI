package jmri.jmrix.tams.serialdriver;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Definition of objects to handle configuring a layout connection via an TAMS
 * SerialDriverAdapter object.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
@API(status = EXPERIMENTAL)
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    public final static String NAME = "MasterControl"; // NOI18N

    /**
     * Create a connection configuration with a preexisting adapter. This is
     * used principally when loading a configuration that defines this
     * connection.
     *
     * @param p the adapter to create a connection configuration for
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }

}
