package jmri.jmrix.dcc4pc.serialdriver.configurexml;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.dcc4pc.serialdriver.ConnectionConfig;
import jmri.jmrix.dcc4pc.serialdriver.SerialDriverAdapter;

/**
 * Handle XML persistance of layout connections by persistening
 * the SerialDriverAdapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the SerialDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2012
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 17977 $
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        adapter = SerialDriverAdapter.instance();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static Logger log = Logger.getLogger(ConnectionConfigXml.class.getName());

}
