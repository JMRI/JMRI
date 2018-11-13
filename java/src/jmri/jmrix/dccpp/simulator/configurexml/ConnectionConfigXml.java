package jmri.jmrix.dccpp.simulator.configurexml;

import jmri.jmrix.SerialPortAdapter;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.dccpp.simulator.ConnectionConfig;
import jmri.jmrix.dccpp.simulator.DCCppSimulatorAdapter;
import org.jdom2.Element;

/**
 * Handle XML persistence of layout connections by persisting the
 * DCCppSimulatorAdapter (and connections). Note this is named as the XML version
 * of a ConnectionConfig object, but it's actually persisting the
 * DCCppSimulatorAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Paul Bender Copyright: Copyright (c) 2009
 * @author Mark Underwood Copyright: Copyright (c) 2015
 */
public class ConnectionConfigXml extends AbstractConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected SerialPortAdapter adapter;

    /**
     * A Simulator connection needs no extra information, so we reimplement the
     * superclass method to just write the necessary parts.
     *
     * @return Formatted element containing no attributes except the class name
     */
    @Override
    public Element store(Object o) {
        getInstance(o);

        Element e = new Element("connection");
        storeCommon(e, adapter);

        e.setAttribute("class", this.getClass().getName());

        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        // start the "connection"
        getInstance();

        loadCommon(shared, perNode, adapter);

        // register, so can be picked up next time
        register();

        if (adapter.getDisabled()) {
            unpackElement(shared, perNode);
            return result;
        }

        adapter.configure();

        return result;
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new DCCppSimulatorAdapter();
        }
    }

    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}
