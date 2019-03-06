package jmri.jmrix.rfid.configurexml;

import jmri.jmrix.configurexml.AbstractStreamConnectionConfigXml;
import jmri.jmrix.rfid.RfidStreamConnectionConfig;
import jmri.jmrix.rfid.RfidStreamPortController;

/**
 * Handle XML persistance of layout connections by persistening the
 * RfidStreamConnectionConfig (and connections). Note this is named as the 
 * XML version of a ConnectionConfig object, but it's actually persisting the
 * RfidStreamPortController.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Andrew Crosland Copyright: Copyright (c) 2006
 */
public class RfidStreamConnectionConfigXml extends AbstractStreamConnectionConfigXml {

    public RfidStreamConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new RfidStreamPortController();
        }
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((RfidStreamConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
	if(adapter!=null) {
	   return; // already registered.
	}
        this.register(new RfidStreamConnectionConfig(adapter));
    }

}
