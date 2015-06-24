// ConnectionConfig.java
package jmri.jmrix.roco.z21.simulator;

import javax.swing.JPanel;

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
    public ConnectionConfig(z21SimulatorAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() {
        return "Z21 Simulator";
    }

    String manufacturerName = "Roco";

    public String getManufacturer() {
        return manufacturerName;
    }

    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new z21SimulatorAdapter();
        }
    }

    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);
        hostNameField.setText("localhost");
        portField.setEnabled(false); // we don't change this on the simulator.
        portFieldLabel.setText("Communication Port");
        portField.setText(String.valueOf(adapter.getPort()));
        portField.setEnabled(false); // we can't change this now.
    }


}
