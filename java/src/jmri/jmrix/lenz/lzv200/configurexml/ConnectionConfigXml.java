package jmri.jmrix.lenz.lzv200.configurexml;

import jmri.jmrix.lenz.configurexml.AbstractXNetSerialConnectionConfigXml;
import jmri.jmrix.lenz.lzv200.ConnectionConfig;
import jmri.jmrix.lenz.lzv200.LZV200Adapter;

/**
 * Handle XML persistance of layout connections by persistening the LZV200Adapter
 * (and connections). Note this is named as the XML version of a
 * ConnectionConfig object, but it's actually persisting the LZV200Adapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Paul Bender Copyright: Copyright (c) 2005
 */
public class ConnectionConfigXml extends AbstractXNetSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new LZV200Adapter();
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
