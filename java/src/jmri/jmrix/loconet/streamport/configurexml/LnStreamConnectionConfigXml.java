package jmri.jmrix.loconet.streamport.configurexml;

import jmri.jmrix.configurexml.AbstractStreamConnectionConfigXml;
import jmri.jmrix.loconet.streamport.LnStreamConnectionConfig;
import jmri.jmrix.loconet.streamport.LnStreamPortController;

/**
 * Handle XML persistance of layout connections by persistening the
 * LnStreamConnectionConfig (and connections). Note this is named as the 
 * XML version of a ConnectionConfig object, but it's actually persisting the
 * LnStreamPortController.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Andrew Crosland Copyright: Copyright (c) 2006
 */
public class LnStreamConnectionConfigXml extends AbstractStreamConnectionConfigXml {

    public LnStreamConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new LnStreamPortController();
        }
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((LnStreamConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
	if(adapter!=null) {
	   return; // already registered.
	}
        this.register(new LnStreamConnectionConfig(adapter));
    }

}
