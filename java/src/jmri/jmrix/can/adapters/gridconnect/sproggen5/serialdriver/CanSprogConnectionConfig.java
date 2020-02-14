package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Definition of objects to handle configuring a layout connection via a SPROG 
 * Generation 5 SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Andrew Crosland 2008
 * @author Andrew Crosland 2019
 */
public class CanSprogConnectionConfig extends jmri.jmrix.can.adapters.ConnectionConfig {

    /**
     * Create a connection configuration with a preexisting adapter. This is
     * used principally when loading a configuration that defines this
     * connection.
     *
     * @param p the adapter to create a connection configuration for
     */
    public CanSprogConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    // Needed for instantiation by reflection, do not remove.
    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public CanSprogConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("CanSprogTitle");
    }

    public boolean isOptList2Advanced() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new CanisbSerialDriverAdapter();
        }
    }

}
