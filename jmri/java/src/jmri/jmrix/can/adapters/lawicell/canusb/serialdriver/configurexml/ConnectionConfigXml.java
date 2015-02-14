package jmri.jmrix.can.adapters.lawicell.canusb.serialdriver.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.can.adapters.lawicell.canusb.serialdriver.ConnectionConfig;
import jmri.jmrix.can.adapters.lawicell.canusb.serialdriver.CanUsbDriverAdapter;

/**
 * Handle XML persistance of layout connections by persistening
 * the CanUsbDriverAdapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the CanUsbDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 * @author Andrew Crosland 2008
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
        adapter = new CanUsbDriverAdapter();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }
}