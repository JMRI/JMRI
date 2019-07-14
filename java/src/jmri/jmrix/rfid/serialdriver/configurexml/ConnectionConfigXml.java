package jmri.jmrix.rfid.serialdriver.configurexml;

import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.rfid.serialdriver.ConnectionConfig;
import jmri.jmrix.rfid.serialdriver.SerialDriverAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handle XML persistence of layout connections by persisting the
 * SerialDriverAdapter (and connections). Note this is named as the XML version
 * of a ConnectionConfig object, but it's actually persisting the
 * SerialDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2006, 2007, 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        log.debug("getInstance without parameter called");
        adapter = new SerialDriverAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        log.debug("getInstance with parameter called");
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class);
}
