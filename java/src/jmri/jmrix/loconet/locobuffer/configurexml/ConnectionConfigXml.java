package jmri.jmrix.loconet.locobuffer.configurexml;

import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.loconet.locobuffer.ConnectionConfig;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;

/**
 * Handle XML persistance of layout connections by persistening the
 * LocoBufferAdapter (and connections). Note this is named as the XML version of
 * a ConnectionConfig object, but it's actually persisting the
 * LocoBufferAdapter.
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
        adapter = new LocoBufferAdapter();
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
