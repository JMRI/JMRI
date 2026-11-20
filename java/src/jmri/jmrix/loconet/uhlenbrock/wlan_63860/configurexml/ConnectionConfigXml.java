package jmri.jmrix.loconet.uhlenbrock.wlan_63860.configurexml;

import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.loconet.uhlenbrock.wlan_63860.ConnectionConfig;
import jmri.jmrix.loconet.uhlenbrock.wlan_63860.Wlan63860Adapter;

/**
 * Handle XML persistence of layout connections by persisting the
 * Wlan63860Adapter (and connections). Note this is named as the XML version of
 * a ConnectionConfig object, but it's actually persisting the Wlan63860Adapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author detami01 Copyright (C) 2026
 * @see jmri.jmrix.loconet.loconetovertcp.configurexml.ConnectionConfigXml
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        adapter = new Wlan63860Adapter();
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
