package jmri.jmrix.jmriclient.json.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.jmriclient.json.JsonClientSystemConnectionMemo;
import jmri.jmrix.jmriclient.json.JsonNetworkConnectionConfig;
import jmri.jmrix.jmriclient.json.JsonNetworkPortController;
import org.jdom.Element;

/**
 *
 * @author Randall Wood 2014
 */
public class JsonNetworkConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public JsonNetworkConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (this.adapter == null) {
            this.adapter = new JsonNetworkPortController();
        }
    }

    @Override
    protected void getInstance(Object object) {
        this.adapter = ((JsonNetworkConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new JsonNetworkConnectionConfig(this.adapter));
    }

    @Override
    protected void extendElement(Element e) {
        if (this.adapter.getSystemConnectionMemo() != null) {
            e.setAttribute("transmitPrefix", ((JsonClientSystemConnectionMemo) this.adapter.getSystemConnectionMemo()).getTransmitPrefix()); // NOI18N
        }
    }

    /**
     * Customizable method if you need to add anything more
     *
     * @param e Element being created, update as needed
     */
    @Override
    protected void unpackElement(Element e) {
        if (e.getAttribute("transmitPrefix") != null) { // NOI18N
            ((JsonClientSystemConnectionMemo) this.adapter.getSystemConnectionMemo()).setTransmitPrefix(e.getAttribute("transmitPrefix").getValue()); // NOI18N
        }
    }
}
