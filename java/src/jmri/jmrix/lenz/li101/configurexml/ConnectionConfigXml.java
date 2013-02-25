package jmri.jmrix.lenz.li101.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.lenz.li101.ConnectionConfig;
import jmri.jmrix.lenz.li101.LI101Adapter;

/**
 * Handle XML persistance of layout connections by persistening
 * the LI101Adapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the LI101Adapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if(adapter == null) adapter=new LI101Adapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter=((ConnectionConfig) object).getAdapter();
    }


    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class.getName());

}
