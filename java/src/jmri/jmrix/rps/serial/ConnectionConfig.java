package jmri.jmrix.rps.serial;

import java.util.ResourceBundle;

/**
 * Definition of objects to handle configuring an RPS layout connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2008
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
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "RPS Base Station";
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.rps.RpsActionListBundle");
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation") // updated to multi-connection though RPS hardware obsolete
    @Override
    protected void setInstance() {
        if (adapter == null ) {
           adapter = SerialAdapter.instance();
        }
    }

}
