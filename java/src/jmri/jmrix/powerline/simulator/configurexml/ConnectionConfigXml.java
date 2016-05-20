package jmri.jmrix.powerline.simulator.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.powerline.simulator.ConnectionConfig;
import jmri.jmrix.powerline.simulator.SimulatorAdapter;

/**
 * Handle XML persistence of layout connections by persisting
 * the SerialDriverAdapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the SerialDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Ken Cameron Copyright: Copyright (c) 2011
 * Copied from NCE simulator by Bob Jacobsen
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig)object).getAdapter();
    }
    
    protected void getInstance() {
        adapter = new SimulatorAdapter();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class.getName());

}
