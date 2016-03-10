package jmri.jmrix.configurexml;

import jmri.configurexml.ConfigXmlManager;
import jmri.jmrix.SerialPortAdapter;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base (and partial implementation) for classes persisting the status
 * of serial port adapters.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 */
abstract public class AbstractSerialConnectionConfigXml extends AbstractConnectionConfigXml {

    public AbstractSerialConnectionConfigXml() {
    }

    final static protected java.util.ResourceBundle rb
            = java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");

    protected SerialPortAdapter adapter;

    protected void getInstance(Object object) {
        getInstance(); // over-ridden during migration
    }

    /**
     * Default implementation for storing the static contents of the serial port
     * implementation
     *
     * @param object Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
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
            e.setAttribute("port", rb.getString("noneSelected"));
        }

        if (adapter.getCurrentBaudRate() != null) {
            e.setAttribute("speed", adapter.getCurrentBaudRate());
        } else {
            e.setAttribute("speed", rb.getString("noneSelected"));
        }

        e.setAttribute("class", this.getClass().getName());

        extendElement(e);

        return e;
    }

    /**
     * Customizable method if you need to add anything more
     *
     * @param e Element being created, update as needed
     */
    protected void extendElement(Element e) {
    }

    @Override
    public boolean load(Element shared, Element perNode) throws Exception {
        boolean result = true;
        getInstance();
        // configure port name
        String portName = null;
        try {
           portName = perNode.getAttribute("port").getValue();
           adapter.setPort(portName);
        } catch (java.lang.NullPointerException npe) {
           // when the storage format has not been upgraded to the new format,
           // the portName incorrectly gets added to the shared attributes.
           portName = shared.getAttribute("port").getValue();
           adapter.setPort(portName);
        }
        String baudRate = null;
        try {
           baudRate = perNode.getAttribute("speed").getValue();
           adapter.configureBaudRate(baudRate);
        } catch (java.lang.NullPointerException npe) {
           // when the storage format has not been upgraded to the new format,
           // the baudRate incorrectly gets added to the shared attributes.
           baudRate = shared.getAttribute("speed").getValue();
           adapter.configureBaudRate(baudRate);
        }

        //loadCommon(shared, perNode, adapter);
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
            ConfigXmlManager.creationErrorEncountered(
                    null, "opening connection",
                    status,
                    null, null, null
            );
            // now force end to operation
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
    public void load(Element element, Object o) {
        log.error("method with two args invoked");
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(AbstractSerialConnectionConfigXml.class.getName());

}
