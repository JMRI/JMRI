package jmri.jmrix.qsi.serialdriver;

import java.util.ResourceBundle;

/**
 * Definition of objects to handle configuring a layout connection via a QSI
 * SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2007
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
        return "Quantum Programmer";
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.qsi.QsiActionListBundle");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if(adapter == null) {
           adapter = new SerialDriverAdapter();
        }
    }

}
