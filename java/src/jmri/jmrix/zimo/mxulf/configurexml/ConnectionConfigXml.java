package jmri.jmrix.zimo.mxulf.configurexml;

import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.zimo.mxulf.ConnectionConfig;
import jmri.jmrix.zimo.mxulf.SerialDriverAdapter;

/**
 * Handle XML persistance of layout connections by persistening the mxulfAdapter
 * (and connections). Note this is named as the XML version of a
 * ConnectionConfig object, but it's actually persisting the mxulfAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2014
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

    @Override
    protected void getInstance() {
        adapter = new SerialDriverAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

}
