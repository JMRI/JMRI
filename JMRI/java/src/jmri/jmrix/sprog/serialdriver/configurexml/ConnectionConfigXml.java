package jmri.jmrix.sprog.serialdriver.configurexml;

import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.sprog.serialdriver.ConnectionConfig;
import jmri.jmrix.sprog.serialdriver.SerialDriverAdapter;

/**
 * Handle XML persistance of layout connections by persistening the
 * SerialDriverAdapter (and connections).
 * <p>
 * Note this is named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the SerialDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright (c) 2003
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
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
