package jmri.jmrix.marklin.networkdriver.configurexml;

import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.marklin.networkdriver.ConnectionConfig;
import jmri.jmrix.marklin.networkdriver.NetworkDriverAdapter;

/**
 * Handle XML persistance of layout connections by persistening the
 * NetworkDriverAdapter (and connections).
 * <p>
 * Note this is named as the XML version of a ConnectionConfig object, but it's
 * actually persisting the NetworkDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 208
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    @Override
    protected void getInstance() {
        adapter = new NetworkDriverAdapter();
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
