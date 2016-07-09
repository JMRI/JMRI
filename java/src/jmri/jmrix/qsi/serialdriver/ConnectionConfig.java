// ConnectionConfig.java
package jmri.jmrix.qsi.serialdriver;

import java.util.ResourceBundle;

/**
 * Definition of objects to handle configuring a layout connection via an QSI
 * SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 * @version	$Revision$
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

    public String name() {
        return "Quantum Programmer";
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.qsi.QsiActionListBundle");
    }

    protected void setInstance() {
        if(adapter == null) {
           adapter = new SerialDriverAdapter();
        }
    }
}
