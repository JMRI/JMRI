package jmri.jmrix.dccpp.dccppovertcp.configurexml;

import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.dccpp.dccppovertcp.ConnectionConfig;
import jmri.jmrix.dccpp.dccppovertcp.DCCppTcpDriverAdapter;

/**
 * Handle XML persistance of layout connections by persistening the
 * LnTcpDriverAdapter (and connections).
 * <P>
 * Note this is named as the XML version of a ConnectionConfig object, but it's
 * actually persisting the LnTcpDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Mark Underwood Copyright: Copyright (c) 2015
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        adapter = new DCCppTcpDriverAdapter();
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
