package jmri.jmrix.loconet.bluetooth;

import java.util.Vector;

/**
 * Definition of objects to handle configuring a LocoNet Bluetooth layout
 * connection via a LocoNetBluetoothAdapter object.
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p serial port adapter.
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
        return "BT Locobridge";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new LocoNetBluetoothAdapter();
        }
    }

    /**
     * Overrides super method to remove unnecessary ui components (baud rate)
     * and change the label "Serial Port: " to "Bluetooth adapter: ".
     */
    @Override
    protected void showAdvancedItems() {
        super.showAdvancedItems();
        _details.remove(baudBoxLabel);
        _details.remove(baudBox);
        portBoxLabel.setText("Bluetooth adapter: ");
    }

    /**
     * Overrides super method to remove unnecessary ui components (baud rate)
     * and change the label "Serial Port: " to "Bluetooth adapter: ".
     */
    @Override
    protected int addStandardDetails(boolean incAdvanced, int i) {
        int out = super.addStandardDetails(incAdvanced, i);
        _details.remove(baudBoxLabel);
        _details.remove(baudBox);
        portBoxLabel.setText("Bluetooth adapter: ");
        return out;
    }

    @Override
    protected Vector<String> getPortNames() {
        return LocoNetBluetoothAdapter.discoverPortNames();
    }

    @Override
    protected String[] getPortFriendlyNames() {
        return new String[]{};
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionConfig.class);

}
