package jmri.jmrix.can.adapters.loopback.configurexml;

import jmri.jmrix.PortAdapter;
import jmri.jmrix.can.adapters.loopback.ConnectionConfig;
import jmri.jmrix.can.adapters.loopback.Port;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of layout connections by persistening the CAN
 * simulator (and connections).
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008, 2010
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    /**
     * A simulated connection needs no extra information, so we reimplement the
     * superclass method to just write the necessary parts.
     *
     * @return formatted element containing no attributes except the class name
     */
    @Override
    public Element store(Object o) {

        adapter = ((ConnectionConfig) o).getAdapter();
        Element e = new Element("connection");

        if (adapter.getCurrentPortName() != null) { // port not functional in loopback Sim, hidden in UI. Remove in store?
            e.setAttribute("port", adapter.getCurrentPortName());
        } else {
            e.setAttribute("port", Bundle.getMessage("noneSelected"));
        }
        if (adapter.getManufacturer() != null) {
            e.setAttribute("manufacturer", adapter.getManufacturer());
        }
        if (adapter.getSystemConnectionMemo() != null) {
            e.setAttribute("userName", adapter.getSystemConnectionMemo().getUserName());
            e.setAttribute("systemPrefix", adapter.getSystemConnectionMemo().getSystemPrefix());
        }
        if (adapter.getDisabled()) {
            e.setAttribute("disabled", "yes");
        } else {
            e.setAttribute("disabled", "no");
        }

        saveOptions(e, adapter);

        e.setAttribute("class", this.getClass().getName());

        extendElement(e);

        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        getInstance();
        // simulator has fewer options in the XML, so implement
        // just needed ones       
        if (adapter.getSystemConnectionMemo() != null) {
            if (shared.getAttribute("userName") != null) {
                adapter.getSystemConnectionMemo().setUserName(shared.getAttribute("userName").getValue());
            }

            if (shared.getAttribute("systemPrefix") != null) {
                adapter.getSystemConnectionMemo().setSystemPrefix(shared.getAttribute("systemPrefix").getValue());
            }
        }
        if (shared.getAttribute("option1") != null) {
            String option1Setting = shared.getAttribute("option1").getValue();
            adapter.configureOption1(option1Setting);
        }

        if (shared.getAttribute("manufacturer") != null) {
            String mfg = shared.getAttribute("manufacturer").getValue();
            adapter.setManufacturer(mfg);
        }
        if (shared.getAttribute("port") != null) { // port not functional in loopback Sim, hidden in UI. Remove in load?
            String portName = shared.getAttribute("port").getValue();
            adapter.setPort(portName);
        }

        if (shared.getAttribute("disabled") != null) {
            String yesno = shared.getAttribute("disabled").getValue();
            if ((yesno != null) && (!yesno.equals(""))) {
                if (yesno.equals("no")) {
                    adapter.setDisabled(false);
                } else if (yesno.equals("yes")) {
                    adapter.setDisabled(true);
                }
            }
        }
        loadOptions(shared.getChild("options"), perNode.getChild("options"), adapter);
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
        adapter = new Port();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
        log.info("CAN Simulator Started");
    }

    @Override
    protected void loadOptions(Element shared, Element perNode, PortAdapter adapter) {
        super.loadOptions(shared, perNode, adapter);

        jmri.jmrix.openlcb.configurexml.ConnectionConfigXml.maybeLoadOlcbProfileSettings(
                shared.getParentElement(), perNode.getParentElement(), adapter);
    }

    @Override
    protected void extendElement(Element e) {
        jmri.jmrix.openlcb.configurexml.ConnectionConfigXml.maybeSaveOlcbProfileSettings(
                e, adapter);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class);

}
