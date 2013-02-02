package jmri.jmrix.lenz.ztc640.configurexml;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.lenz.ztc640.ConnectionConfig;
import jmri.jmrix.lenz.ztc640.ZTC640Adapter;

/**
 * Handle XML persistance of layout connections by persistening
 * the ZTC640Adapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the ZTC640Adapter.
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
        if(adapter == null) adapter=new ZTC640Adapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter=((ConnectionConfig) object).getAdapter();
    }


    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static Logger log = Logger.getLogger(ConnectionConfigXml.class.getName());

}
