package jmri.jmrix.loconet.pr2.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.loconet.pr2.ConnectionConfig;
import jmri.jmrix.loconet.pr2.PR2Adapter;

/**
 * Handle XML persistance of layout connections by persisting
 * the PR2Adapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the PR2Adapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2005, 2006
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        adapter = new PR2Adapter();
    }
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig)object).getAdapter();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class.getName());

}
