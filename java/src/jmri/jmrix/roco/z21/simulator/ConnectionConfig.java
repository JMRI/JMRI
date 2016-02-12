// ConnectionConfig.java
package jmri.jmrix.roco.z21.simulator;

import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handle configuring an z21 layout connection via a z21Simulator
 * adapter.
 * <P>
 * This uses the {@link z21SimulatorAdapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
 * @version	$Revision$
 *
 * @see z21SimulatorAdapter
 */
public class ConnectionConfig extends jmri.jmrix.roco.z21.ConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
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
        return "Z21 Simulator";
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new z21SimulatorAdapter();
        }
    }

    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);
        hostNameField.setText("localhost");
        hostNameField.setEnabled(false); // always localhost.
        portField.setEnabled(false); // we don't change this on the simulator.
        portFieldLabel.setText("Communication Port");
        portField.setText(String.valueOf(adapter.getPort()));
        portField.setEnabled(false); // we can't change this now.
    }

    @Override
    public boolean isHostNameAdvanced() {
        return true;  // hostname is always localhost.
    }

    @Override
    public boolean isAutoConfigPossible() {
        return false;  // always fixed, no reason to search.
    }

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfig.class.getName());

}
