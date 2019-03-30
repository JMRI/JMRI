package jmri.jmrix.dccpp.configurexml;

import jmri.jmrix.configurexml.AbstractStreamConnectionConfigXml;
import jmri.jmrix.dccpp.DCCppStreamConnectionConfig;
import jmri.jmrix.dccpp.DCCppStreamPortController;

/**
 * Handle XML persistance of layout connections by persistening the
 * DCCppStreamConnectionConfig (and connections). Note this is named as the 
 * XML version of a ConnectionConfig object, but it's actually persisting the
 * DCCppStreamPortController.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Andrew Crosland Copyright: Copyright (c) 2006
 */
public class DCCppStreamConnectionConfigXml extends AbstractStreamConnectionConfigXml {

    public DCCppStreamConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new DCCppStreamPortController();
        }
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((DCCppStreamConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
	if(adapter!=null) {
	   return; // already registered.
	}
        this.register(new DCCppStreamConnectionConfig(adapter));
    }

}
