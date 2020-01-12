package jmri.jmrix.configurexml;

import jmri.jmrix.UsbPortAdapter;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base (and partial implementation) for classes persisting the status
 * of (non-serial) USB adapters.
 * <p>
 * IOW: if you're just using usb to access a serial buss on the other side then
 * you should be using AbstractSerialConnectionConfigXml instead.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author George Warner Copyright: Copyright (c) 2017
 */
abstract public class AbstractUsbConnectionConfigXml extends AbstractConnectionConfigXml {

    /**
     * constructor
     */
    public AbstractUsbConnectionConfigXml() {
    }

    protected UsbPortAdapter adapter;

    /**
     * set the usb port adapter
     *
     * @param usbPortAdapter the usb port adapter to set
     */
    protected void setAdapter(UsbPortAdapter usbPortAdapter) {
        log.debug("setAdapter({})", usbPortAdapter);
        adapter = usbPortAdapter;
    }

    /**
     * get the usb port adapter
     *
     * @return the usb port adapter
     */
    protected UsbPortAdapter getAdapter() {
        log.debug("getAdapter()");
        return adapter;
    }

    /**
     * get instance
     *
     * @param object to get the instance of
     */
    protected abstract void getInstance(Object object);

    /**
     * {@inheritDoc}
     */
    @Override
    public Element store(Object object) {
        log.debug("store({})", object);
        getInstance(object);
        Element e = new Element("connection");

        if (adapter == null) {
            log.warn("No adapter found while saving usb port configuration {}", object.toString());
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
        if (adapter.getSerialNumber() != null) {
            e.setAttribute("serialNumber", adapter.getSerialNumber());
        }

        e.setAttribute("class", this.getClass().getName());

        extendElement(e);

        return e;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void extendElement(Element e) {
        log.debug("extendElement({})", e);
    }

    /**
     * load from xml elements
     *
     * @param shared  element
     * @param perNode element
     * @return boolean true if successful
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        log.debug("load({}, {})", shared, perNode);
        boolean result = true;  // assume success (optimist!)

        getInstance();
        // configure port name
        String portName = perNode.getAttributeValue("port");
        String serialNumber = perNode.getAttributeValue("serialNumber");
        if (serialNumber != null && serialNumber.isEmpty()) {
            serialNumber = null;
        }
        adapter.setPort(portName);
        adapter.setSerialNumber(serialNumber);

        loadCommon(shared, perNode, adapter);

        // register, so can be picked up next time
        register();

        // try to open the port
        if (adapter.getDisabled()) {
            unpackElement(shared, perNode);
            return result;
        }

        String status = adapter.openPort(portName, serialNumber);
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
     * Update static data from XML file
     *
     * @param element Top level Element to unpack.
     */
    @Override
    public void load(Element element, Object o) {
        log.error("method with two args invoked");
    }

    // initialize logging
    private static final Logger log
            = LoggerFactory.getLogger(AbstractUsbConnectionConfigXml.class);
}
