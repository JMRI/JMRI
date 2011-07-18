package jmri.jmrix.configurexml;

import jmri.configurexml.*;
import jmri.jmrix.SerialPortAdapter;

import org.jdom.Element;

/**
 * Abstract base (and partial implementation) for
 * classes persisting the status of serial port adapters.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 */
abstract public class AbstractConnectionConfigXml extends AbstractXmlAdapter {

    public AbstractConnectionConfigXml() {
    }

    static java.util.ResourceBundle rb = 
        java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");
    
    protected SerialPortAdapter adapter;
    abstract protected void getInstance();
    abstract protected void register();

    /**
     * Default implementation for storing the static contents of the serial port implementation
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        getInstance();

        Element e = new Element("connection");
        // many of the following are required by the DTD; failing to include
        // them makes the XML file unreadable, but at least the next
        // invocation of the program can then continue.
        if (adapter.getManufacturer()!=null)
            e.setAttribute("manufacturer", adapter.getManufacturer());
        if (adapter.getCurrentPortName()!=null)
            e.setAttribute("port", adapter.getCurrentPortName());
        else e.setAttribute("port", rb.getString("noneSelected"));

        if (adapter.getCurrentBaudRate()!=null)
            e.setAttribute("speed", adapter.getCurrentBaudRate());
        else e.setAttribute("speed", rb.getString("noneSelected"));

        if (adapter.getCurrentOption1Setting()!=null)
            e.setAttribute("option1", adapter.getCurrentOption1Setting());
        else e.setAttribute("option1", rb.getString("noneSelected"));

        if (adapter.getCurrentOption2Setting()!=null)
            e.setAttribute("option2", adapter.getCurrentOption2Setting());
        else e.setAttribute("option2", rb.getString("noneSelected"));

        e.setAttribute("class", this.getClass().getName());

        extendElement(e);

        return e;
    }

    /**
     * Customizable method if you need to add anything more
     * @param e Element being created, update as needed
     */
    protected void extendElement(Element e) {}

    /**
     * Update static data from XML file
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element e) throws Exception {
    	boolean result = true;
        getInstance();
        // configure port name
        String portName = e.getAttribute("port").getValue();
        adapter.setPort(portName);
        String baudRate = e.getAttribute("speed").getValue();
        adapter.configureBaudRate(baudRate);

        if (e.getAttribute("option1")!=null) {
            String option1Setting = e.getAttribute("option1").getValue();
            adapter.configureOption1(option1Setting);
        }
        if (e.getAttribute("option2")!=null) {
            String option2Setting = e.getAttribute("option2").getValue();
            adapter.configureOption2(option2Setting);
        }
        String manufacturer;
        try { 
            manufacturer = e.getAttribute("manufacturer").getValue();
            adapter.setManufacturer(manufacturer);
        } catch ( NullPointerException ex) { //Considered normal if not present
            
        }
        // register, so can be picked up next time
        register();

        // try to open the port
        String status = adapter.openPort(portName, "JMRI app");
        if (status != null ) {
            // indicates an error, return it
            ConfigXmlManager.creationErrorEncountered(
                                        null, "opening connection",
                                        org.apache.log4j.Level.ERROR,
                                        status,
                                        null,null,null
                                    );
            // now force end to operation
            return false;
        }
        
        // if successful so far, go ahead and configure
        adapter.configure();

        // once all the configure processing has happened, do any
        // extra config
        unpackElement(e);
        return result;
    }

    /**
     * Customizable method if you need to add anything more
     * @param e Element being created, update as needed
     */
    protected void unpackElement(Element e) {}

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
      */
    public void load(Element element, Object o) {
        log.error("method with two args invoked");
    }


    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractConnectionConfigXml.class.getName());

}