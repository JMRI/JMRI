package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

/**
 * Definition of objects to handle configuring a layout connection via a SPROG 
 * Generation 5 SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Andrew Crosland 2008
 * @author Andrew Crosland 2019
 */
public class PiSprog3ConnectionConfig extends jmri.jmrix.can.adapters.ConnectionConfig {

    /**
     * Create a connection configuration with a preexisting adapter. This is
     * used principally when loading a configuration that defines this
     * connection.
     *
     * @param p the adapter to create a connection configuration for
     */
    public PiSprog3ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    // Needed for instantiation by reflection, do not remove.
    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public PiSprog3ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("PiSprog3Title");
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
            adapter = new PiSprog3SerialDriverAdapter();
        }
    }

}
