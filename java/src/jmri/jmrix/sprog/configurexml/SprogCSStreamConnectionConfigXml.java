package jmri.jmrix.sprog.configurexml;

import jmri.jmrix.configurexml.AbstractStreamConnectionConfigXml;
import jmri.jmrix.sprog.SprogCSStreamConnectionConfig;
import jmri.jmrix.sprog.SprogCSStreamPortController;

/**
 * Handle XML persistance of layout connections by persistening the
 * SerialDriverAdapter (and connections). Note this is named as the XML version
 * of a ConnectionConfig object, but it's actually persisting the
 * SerialDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Andrew Crosland Copyright: Copyright (c) 2006
 */
public class SprogCSStreamConnectionConfigXml extends AbstractStreamConnectionConfigXml {

    public SprogCSStreamConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new SprogCSStreamPortController();
        }
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((SprogCSStreamConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
	if(adapter!=null) {
	   return; // already registered.
	}
        this.register(new SprogCSStreamConnectionConfig(adapter));
    }

}
