package jmri.jmrix.can.adapters.gridconnect.net.configurexml;

import jmri.jmrix.can.adapters.gridconnect.net.MergConnectionConfig;
import jmri.jmrix.can.adapters.gridconnect.net.MergNetworkDriverAdapter;

/**
 * Handle XML persistance of layout connections by persistening the
 * NetworkDriverAdapter (and connections).
 * <p>
 * Note this is named as the XML version of a ConnectionConfig object, but it's
 * actually persisting the NetworkDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 */
public class MergConnectionConfigXml extends ConnectionConfigXml {

    public MergConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        adapter = new MergNetworkDriverAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((MergConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new MergConnectionConfig(adapter));
    }

}
