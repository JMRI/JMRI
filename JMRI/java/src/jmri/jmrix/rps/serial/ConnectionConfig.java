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
     * Ctor for a functional Swing object with no prexisting adapter
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

    @Override
    protected void setInstance() {
        if(adapter == null ) {
           adapter = SerialAdapter.instance();
        }
    }
}
