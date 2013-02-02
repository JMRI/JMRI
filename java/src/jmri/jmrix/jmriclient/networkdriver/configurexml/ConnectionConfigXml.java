package jmri.jmrix.jmriclient.networkdriver.configurexml;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.jmriclient.networkdriver.ConnectionConfig;
import jmri.jmrix.jmriclient.networkdriver.NetworkDriverAdapter;

/**
 * Handle XML persistance of layout connections by persistening
 * the NetworkDriverAdapter (and connections).
 * <P>
 * Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the NetworkDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Paul Bender Copyright: Copyright (c) 2010
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if(adapter == null)
           adapter = new NetworkDriverAdapter();
    }

    @Override
    protected void getInstance(Object object) {
           adapter = ((ConnectionConfig)object).getAdapter();
    }

    @Override
    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }


    // initialize logging
    static Logger log = Logger.getLogger(ConnectionConfigXml.class.getName());

}
