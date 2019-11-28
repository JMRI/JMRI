package jmri.jmrix.maple.simulator;

import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.jmrix.maple.nodeconfig.NodeConfigAction;

/**
 * Handle configuring an Maple layout connection via a MapleSimulator
 * adapter.
 * <p>
 * This uses the {@link SimulatorAdapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
 * @author Mark Underwood Copyright (C) 2015
 *
 * @see SimulatorAdapter
 *
 * Based on jmri.jmrix.lenz.xnetsimulator.ConnectionConfig, copied from EasyDCC
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSimulatorConnectionConfig {

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

    JButton b = new JButton(Bundle.getMessage("ConfigNodesTitle"));

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        setInstance();

        // have to embed the usual one in a new JPanel
        b.addActionListener(new NodeConfigAction((MapleSystemConnectionMemo)adapter.getSystemConnectionMemo()));
        if (!additionalItems.contains(b)) {
            additionalItems.add(b);
        }
        super.loadDetails(details);
    }

    @Override
    public String name() {
        return "Maple Simulator";
    }

    String manufacturerName = jmri.jmrix.maple.SerialConnectionTypeList.MAPLE;

    @Override
    public String getManufacturer() {
        return manufacturerName;
    }

    @Override
    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SimulatorAdapter();
        }
    }

}
