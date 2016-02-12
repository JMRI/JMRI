package jmri.jmrix.can.adapters.gridconnect.can2usbino.serialdriver.configurexml;

import jmri.jmrix.can.adapters.gridconnect.can2usbino.serialdriver.ConnectionConfig;
import jmri.jmrix.can.adapters.gridconnect.can2usbino.serialdriver.SerialDriverAdapter;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of layout connections by persistening the
 * SerialDriverAdapter (and connections). Note this is named as the XML version
 * of a ConnectionConfig object, but it's actually persisting the
 * SerialDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2012
 * @author Andrew Crosland 2008
 * @version $Revision: 19698 $
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        adapter = new SerialDriverAdapter();
    }

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
