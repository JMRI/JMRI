package jmri.jmrix.loconet.bluetooth.configurexml;

import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.loconet.bluetooth.ConnectionConfig;
import jmri.jmrix.loconet.bluetooth.LocoNetBluetoothAdapter;

/**
 * Handle XML persistance of layout connections by persistening the
 * LocoNetBluetoothAdapter (and connections). Note this is named as the XML version of
 * a ConnectionConfig object, but it's actually persisting the
 * LocoNetBluetoothAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        adapter = new LocoNetBluetoothAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}
