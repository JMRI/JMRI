package jmri.jmrix.dccpp.network.configurexml;

import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.dccpp.network.ConnectionConfig;
import jmri.jmrix.dccpp.network.DCCppEthernetAdapter;

/**
 * Handle XML persistence of layout connections by persisting the DCC++ Server
 * (and connections). Note this is named as the XML version of a
 * ConnectionConfig object, but it's actually persisting the DCC++ Server.
 * <p>
 * NOTE: The DCC++ Server currently has no options, so this class does not store
 * any.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Paul Bender Copyright (C) 2011
 * @author Mark Underwood Copyright (C) 2015
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new DCCppEthernetAdapter();
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
