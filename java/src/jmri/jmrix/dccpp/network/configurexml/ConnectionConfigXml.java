package jmri.jmrix.dccpp.network.configurexml;

import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.dccpp.network.ConnectionConfig;
import jmri.jmrix.dccpp.network.DCCppEthernetAdapter;
import org.jdom2.Element;

/**
 * Handle XML persistence of layout connections by persisting the DCC-EX Server
 * (and connections). Note this is named as the XML version of a
 * ConnectionConfig object, but it's actually persisting the DCC-EX Server.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Paul Bender Copyright (C) 2011
 * @author Mark Underwood Copyright (C) 2015
 * @author Chad Francis Copyright (C) 2026
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

    @Override
    protected void extendElement(Element e) {
        e.setAttribute("reconnectEnabled",
                adapter.getAllowConnectionRecovery() ? "true" : "false");
    }

    @Override
    protected void unpackElement(Element shared, Element perNode) {
        if (shared.getAttribute("reconnectEnabled") != null) {
            boolean enabled = shared.getAttribute("reconnectEnabled").getValue().equals("true");
            adapter.setAllowConnectionRecovery(enabled);
            if (enabled) {
                adapter.setReconnectMaxAttempts(-1);
            }
        }
    }

}
