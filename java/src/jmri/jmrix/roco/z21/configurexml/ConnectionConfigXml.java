package jmri.jmrix.roco.z21.configurexml;

import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.roco.z21.ConnectionConfig;
import jmri.jmrix.roco.z21.Z21Adapter;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Handle XML persistance of layout connections by persistening the Z21 (and
 * connections).
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Paul Bender Copyright (C) 2014
 */
@API(status = EXPERIMENTAL)
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new Z21Adapter();
        }
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
