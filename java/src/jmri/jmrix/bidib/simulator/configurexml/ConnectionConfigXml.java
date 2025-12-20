package jmri.jmrix.bidib.simulator.configurexml;

import jmri.jmrix.SerialPortAdapter;
//import jmri.jmrix.PortAdapter;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.bidib.simulator.ConnectionConfig;
import jmri.jmrix.bidib.simulator.BiDiBSimulatorAdapter;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistence of layout connections by persisting the
 * BiDiBSimulatorAdapter (and connections). Note this is named as the XML version
 * of a ConnectionConfig object, but it's actually persisting the
 * BiDiBSimulatorAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Eckart Meyer Copyright (C) 2019
 */
public class ConnectionConfigXml extends AbstractConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected SerialPortAdapter adapter;

    /**
     * A Simulator connection (normaly) needs no extra information, so we reimplement the
     * superclass method to just write the necessary parts plus the simulator file name.
     *
     * @return Formatted element containing no attributes except the class name
     */
    @Override
    public Element store(Object o) {
        log.debug("store");
        getInstance(o);

        Element e = new Element("connection");
        storeCommon(e, adapter);

        if (adapter.getCurrentPortName() != null) {
            e.setAttribute("simulationFile", ((BiDiBSimulatorAdapter)adapter).getSimulationFile());
        } else {
            e.setAttribute("simulationFile", "noneSelected");
        }

        e.setAttribute("class", this.getClass().getName());

        extendElement(e);

        return e;
    }

    // TODO: should be reworked ...
    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        // start the "connection"
        getInstance();
        log.debug("load, adapter: {}", adapter);

        java.util.List<Attribute> al = perNode.getAttributes();
        log.debug("load: attr list: {}", al);
        //Attribute a = perNode.getAttribute("simulationFile");
        //boolean b = a.isSpecified();
        String simulationFile = perNode.getAttribute("simulationFile").getValue();
        ((BiDiBSimulatorAdapter)adapter).setSimulationFile(simulationFile);

        loadCommon(shared, perNode, adapter);

        // register, so can be picked up next time
        register();

        if (adapter.getDisabled()) {
            unpackElement(shared, perNode);
            return result;
        }

//        // check if the simulation file exists. - CAN'T DO
//        String status = adapter.openPort(simulationFile, "JMRI app");
//        if (status != null) {
//            // indicates an error, return it
//            handleException(status, "opening connection", null, null, null);
//            // now force end to operation
//            log.debug("load failed");
//            return false;
//        }


        adapter.configure();

        // once all the configure processing has happened, do any
        // extra config
        unpackElement(shared, perNode);

        return result;
    }

    @Override
    protected void getInstance() {
        log.debug("BiDiB ConnectionConfigXml.getInstance: {}", adapter);
        if (adapter == null) {
            adapter = new BiDiBSimulatorAdapter();
            log.debug("-- adapter created: {}", adapter);
        }
    }

    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

    @Override
    protected void dispose() {
        adapter.dispose();
    }

//    /**
//     * Customizable method if you need to add anything more
//     *
//     * @param e Element being created, update as needed
//     */
//    @Override
//    protected void extendElement(Element e) {
//        if (adapter.getSystemConnectionMemo() != null) {
//            e.setAttribute("simulationFile", ((BiDiBSimulatorAdapter)adapter).getSimulationFile());
//        }
//    }
//
//    @Override
//    protected void unpackElement(Element shared, Element perNode) {
//        if (shared.getAttribute("simulationFile") != null) {
//            ((BiDiBSimulatorAdapter)adapter).setSimulationFile(shared.getAttribute("simulationFile").getValue());
//        }
//    }
    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class);

}
