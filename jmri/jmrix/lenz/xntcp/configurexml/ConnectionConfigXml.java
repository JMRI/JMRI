package jmri.jmrix.lenz.xntcp.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.lenz.xntcp.ConnectionConfig;
import jmri.jmrix.lenz.xntcp.XnTcpAdapter;

/**
 * Handle XML persistance of layout connections by persistening
 * the XnTcpAdapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the XnTcpAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author	Giorgio Terdina Copyright (C) 2008, based on LI100 Action by Bob Jacobsen, Copyright (C) 2003
 * @version $Revision: 1.4 $
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        adapter = XnTcpAdapter.instance();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfigXml.class.getName());

}
