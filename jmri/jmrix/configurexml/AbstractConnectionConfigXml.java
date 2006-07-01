package jmri.jmrix.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrix.SerialPortAdapter;

import org.jdom.Element;

/**
 * Abstract base (and partial implementation) for
 * classes persisting the status of serial port adapters.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.3 $
 */
abstract public class AbstractConnectionConfigXml implements XmlAdapter {

    public AbstractConnectionConfigXml() {
    }

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

        if (adapter.getCurrentPortName()!=null)
            e.addAttribute("port", adapter.getCurrentPortName());
        else e.addAttribute("port", "(None selected)");

        if (adapter.getCurrentBaudRate()!=null)
            e.addAttribute("speed", adapter.getCurrentBaudRate());
        else e.addAttribute("speed", "(None selected)");

        if (adapter.getCurrentOption1Setting()!=null)
            e.addAttribute("option1", adapter.getCurrentOption1Setting());
        else e.addAttribute("option1", "(None selected)");

        if (adapter.getCurrentOption2Setting()!=null)
            e.addAttribute("option2", adapter.getCurrentOption2Setting());
        else e.addAttribute("option2", "(None selected)");

        e.addAttribute("class", this.getClass().getName());

        return e;
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
      */
    public void load(Element e) {

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
        // open the port
        adapter.openPort(portName, "JMRI app");
        adapter.configure();

        // register, so can be picked up
        register();
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
      */
    public void load(Element e, Object o) {
        log.error("method with two args invoked");
    }


    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractConnectionConfigXml.class.getName());

}