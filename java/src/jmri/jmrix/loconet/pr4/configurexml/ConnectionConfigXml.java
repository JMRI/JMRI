package jmri.jmrix.loconet.pr4.configurexml;

import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.loconet.pr4.ConnectionConfig;
import jmri.jmrix.loconet.pr4.PR4Adapter;

/**
 * Handle XML persistance of layout connections by persisting the PR4Adapter
 * (and connections). Note this is named as the XML version of a
 * ConnectionConfig object, but it's actually persisting the PR4Adapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * Based on loconet.pr3.configurexml.ConnectionConfigXml.java
 * 
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2005, 2006, 2008
 * @author B. Milhaupt Copyright (C) 2019
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        adapter = new PR4Adapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}
