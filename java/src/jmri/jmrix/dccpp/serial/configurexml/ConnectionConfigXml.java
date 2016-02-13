package jmri.jmrix.dccpp.serial.configurexml;

import jmri.jmrix.dccpp.configurexml.AbstractDCCppSerialConnectionConfigXml;
import jmri.jmrix.dccpp.serial.ConnectionConfig;
import jmri.jmrix.dccpp.serial.DCCppAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of layout connections by persistening the DCC++ serial adapter
 * (and connections). Note this is named as the XML version of a
 * ConnectionConfig object, but it's actually persisting the DCCppAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Paul Bender Copyright: Copyright (c) 2005
 * @author Mark Underwood Copyright: Copyright (c) 2015
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractDCCppSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new DCCppAdapter();
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
