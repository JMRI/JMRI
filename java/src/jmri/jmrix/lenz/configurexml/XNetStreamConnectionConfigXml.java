package jmri.jmrix.lenz.configurexml;

import jmri.jmrix.configurexml.AbstractStreamConnectionConfigXml;
import jmri.jmrix.lenz.XNetStreamConnectionConfig;
import jmri.jmrix.lenz.XNetStreamPortController;

/**
 * Handle XML persistance of layout connections by persistening the
 * XNetStreamConnectionConfig (and connections). Note this is named as the 
 * XML version of a ConnectionConfig object, but it's actually persisting the
 * XNetStreamPortController.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Andrew Crosland Copyright: Copyright (c) 2006
 */
public class XNetStreamConnectionConfigXml extends AbstractStreamConnectionConfigXml {

    public XNetStreamConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new XNetStreamPortController();
        }
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((XNetStreamConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
	if(adapter!=null) {
	   return; // already registered.
	}
        this.register(new XNetStreamConnectionConfig(adapter));
    }

}
