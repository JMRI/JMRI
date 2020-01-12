package jmri.jmrix.configurexml;

import jmri.jmrix.SerialPortAdapter;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base (and partial implementation) for classes persisting the status
 * of serial port adapters.
 *
 * @author Bob Jacobsen Copyright (c) 2003
 */
abstract public class AbstractSerialConnectionConfigXml extends AbstractConnectionConfigXml {

    public AbstractSerialConnectionConfigXml() {
    }

    protected SerialPortAdapter adapter;

    protected void getInstance(Object object) {
        getInstance(); // over-ridden during migration
    }

    /**
     * Default implementation for storing the static contents of the serial port
     * implementation.
     *
     * @param object Object to store, of type AbstractSerialConnectionConfig
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object object) {
        getInstance(object);
        Element e = new Element("connection");

        if (adapter == null) {
            log.warn("No adapter found while saving serial port configuration {}", object.toString());
            return null;
        }

        // many of the following are required by the DTD; failing to include
        // them makes the XML file unreadable, but at least the next
        // invocation of the program can then continue.
        storeCommon(e, adapter);

        if (adapter.getCurrentPortName() != null) {
            e.setAttribute("port", adapter.getCurrentPortName());
        } else {
            e.setAttribute("port", Bundle.getMessage("noneSelected"));
        }

        if (adapter.getCurrentBaudRate() != null) {
            e.setAttribute("speed", adapter.getCurrentBaudNumber()); // store by baud number, not by i18n combo display string
        } else {
            e.setAttribute("speed", Bundle.getMessage("noneSelected"));
        }

        e.setAttribute("class", this.getClass().getName());

        extendElement(e);

        return e;
    }

    /**
     * Customizable method if you need to add anything more.
     *
     * @param e Element being created, update as needed
     */
    @Override
    protected void extendElement(Element e) {
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        getInstance();
        log.info("Starting to connect for \"{}\"", adapter.getSystemConnectionMemo()!=null ? adapter.getSystemConnectionMemo().getUserName() : "(Unknown Connection)");
        
        // configure port name
        String portName = perNode.getAttribute("port").getValue();
        adapter.setPort(portName);
        String baudNumber = perNode.getAttribute("speed").getValue(); // updated number string format since JMRI 4.16
        adapter.configureBaudRateFromNumber(baudNumber);
        loadCommon(shared, perNode, adapter);
        // register, so can be picked up next time
        register();
        // try to open the port
        if (adapter.getDisabled()) {
            unpackElement(shared, perNode);
            return result;
        }

        String status = adapter.openPort(portName, "JMRI app");
        if (status != null) {
            // indicates an error, return it
            handleException(status, "opening connection", null, null, null);
            // now force end to operation
            log.debug("load failed");
            return false;
        }

        // if successful so far, go ahead and configure
        adapter.configure();

        // once all the configure processing has happened, do any
        // extra config
        unpackElement(shared, perNode);
        return result;
    }

    /**
     * Update static data from XML file.
     *
     * @param element Top level Element to unpack.
     */
    @Override
    public void load(Element element, Object o) {
        log.error("method with two args invoked");
    }

    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(AbstractSerialConnectionConfigXml.class);

}
