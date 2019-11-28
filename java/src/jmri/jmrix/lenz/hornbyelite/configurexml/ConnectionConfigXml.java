package jmri.jmrix.lenz.hornbyelite.configurexml;

import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.lenz.hornbyelite.ConnectionConfig;
import jmri.jmrix.lenz.hornbyelite.EliteAdapter;

/**
 * Handle XML persistance of layout connections by persistening the EliteAdapter
 * (and connections). Note this is named as the XML version of a
 * ConnectionConfig object, but it's actually persisting the EliteAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new EliteAdapter();
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
