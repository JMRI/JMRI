package jmri.jmrix.can.adapters.gridconnect.net;

import java.util.ResourceBundle;

/**
 * Definition of objects to handle configuring a connection via a
 * NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class MergConnectionConfig extends ConnectionConfig {

    public final static String NAME = "CAN via MERG Network Interface";

    /**
     * Create a connection configuration with a preexisting adapter. This is
     * used principally when loading a configuration that defines this
     * connection.
     *
     * @param p the adapter to create a connection configuration for
     */
    public MergConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
    }

    @Override
    public String name() {
        return NAME;
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public MergConnectionConfig() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new MergNetworkDriverAdapter();
            adapter.setPort(5550);
        }
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }

}
