package jmri.jmrix.roco.z21.configurexml;

import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.roco.z21.ConnectionConfig;
import jmri.jmrix.roco.z21.z21Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of layout connections by persistening the Z21 (and
 * connections).
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Paul Bender Copyright (C) 2014
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new z21Adapter();
        }
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class.getName());

}
