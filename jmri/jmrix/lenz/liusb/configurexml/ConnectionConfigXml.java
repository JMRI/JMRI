package jmri.jmrix.lenz.liusb.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.lenz.liusb.ConnectionConfig;
import jmri.jmrix.lenz.liusb.LIUSBAdapter;

/**
 * Handle XML persistance of layout connections by persistening
 * the LIUSBAdapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the LIUSBAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Paul Bender Copyright: Copyright (c) 2005
 * @version $Revision: 1.0 $
 */
public class ConnectionConfigXml extends AbstractConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        adapter = LIUSBAdapter.instance();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConnectionConfigXml.class.getName());

}
