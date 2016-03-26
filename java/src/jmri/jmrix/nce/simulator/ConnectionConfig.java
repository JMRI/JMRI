// ConnectionConfig.java
package jmri.jmrix.nce.simulator;

import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection via an NCE
 * SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003 Convert to multiple connection
 * @author kcameron Copyright (C) 2010
 * @version	$Revision$
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSimulatorConnectionConfig {

    public final static String NAME = "Simulator";

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no existing adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() {
        return NAME;
    }

    public void loadDetails(JPanel details) {
        super.loadDetails(details);
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new SimulatorAdapter();
        }
    }
}
