package jmri.jmrix.zimo.mx1.configurexml;

import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.zimo.mx1.ConnectionConfig;
import jmri.jmrix.zimo.mx1.Mx1Adapter;

/**
 * Handle XML persistance of layout connections by persistening the Mx1Adapter
 * (and connections). Note this is named as the XML version of a
 * ConnectionConfig object, but it's actually persisting the Mx1Adapter.
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
    @SuppressWarnings("deprecation") // until DCC4PC is migrated to multiple systems
    protected void getInstance() {
        adapter = Mx1Adapter.instance();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}
