package jmri.jmrix.internal.configurexml;

import jmri.jmrix.SerialPortAdapter;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.internal.ConnectionConfig;
import jmri.jmrix.internal.InternalAdapter;
import org.jdom2.Element;

/**
 * Handle XML persistance of virtual layout connections
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2010
 */
public class ConnectionConfigXml extends AbstractConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        adapter = new InternalAdapter();
    }

    protected SerialPortAdapter adapter;

    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }


    @Override
    public Element store(Object o) {
        getInstance(o);
        
        if (adapter == null) return null;
        
        Element e = new Element("connection");
        storeCommon(e, adapter);

        e.setAttribute("class", this.getClass().getName());

        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        getInstance();

        this.loadCommon(shared, perNode, adapter);
        // register, so can be picked up
        register();

        if (adapter.getDisabled()) {
            return result;
        }
        adapter.configure();

        return result;
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}
