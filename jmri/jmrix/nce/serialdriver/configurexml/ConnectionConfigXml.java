package jmri.jmrix.nce.serialdriver.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrix.nce.serialdriver.SerialDriverAdapter;

import org.jdom.Element;

/**
 * Handle XML persistance of layout connections by persistening
 * the SerialAdapterDriver (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the SerialDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.1 $
 */
public class ConnectionConfigXml implements XmlAdapter {

    public ConnectionConfigXml() {
    }

    /**
     * Default implementation for storing the static contents of the Swing LAF
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        SerialDriverAdapter c = SerialDriverAdapter.instance();
        Element e = new Element("connection");
        // many of the following are required by the DTD; failing to include
        // them makes the XML file unreadable, but at least the next
        // invocation of the program can then continue.

        if (c.getCurrentPortName()!=null)
            e.addAttribute("port", c.getCurrentPortName());
        else e.addAttribute("port", "(None selected)");

        if (c.getCurrentBaudRate()!=null)
            e.addAttribute("speed", c.getCurrentBaudRate());
        else e.addAttribute("speed", "(None selected)");

        if (c.getCurrentOption1Setting()!=null)
            e.addAttribute("option1", c.getCurrentOption1Setting());
        else e.addAttribute("option1", "(None selected)");

        if (c.getCurrentOption2Setting()!=null)
            e.addAttribute("option2", c.getCurrentOption2Setting());
        else e.addAttribute("option2", "(None selected)");

        e.addAttribute("class", this.getClass().getName());

        return e;
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
      */
    public void load(Element e) {

        SerialDriverAdapter s = SerialDriverAdapter.instance();
        // configure port name
        String portName = e.getAttribute("port").getValue();
        s.setPort(portName);
        String baudRate = e.getAttribute("speed").getValue();
        s.configureBaudRate(baudRate);
        if (e.getAttribute("option1")!=null) {
            String option1Setting = e.getAttribute("option1").getValue();
            s.configureOption1(option1Setting);
        }
        if (e.getAttribute("option2")!=null) {
            String option2Setting = e.getAttribute("option2").getValue();
            s.configureOption2(option2Setting);
        }
        // open the port
        s.openPort(portName, "JMRI app");
        s.configure();

    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
      */
    public void load(Element e, Object o) {
        log.error("method with two args invoked");
    }


    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConnectionConfigXml.class.getName());

}