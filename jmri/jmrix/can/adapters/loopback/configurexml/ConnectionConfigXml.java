package jmri.jmrix.can.adapters.loopback.configurexml;

import jmri.jmrix.can.adapters.loopback.Port;
import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;

import jmri.jmrix.can.adapters.loopback.ConnectionConfig;

import org.jdom.Element;

/**
 * Handle XML persistance of layout connections by persistening
 * the CAN simulator (and connections). 
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @version $Revision: 1.1 $
 */
public class ConnectionConfigXml extends AbstractConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    /**
     * A simulated connection needs no extra information, so
     * we reimplement the superclass method to just write the
     * necessary parts.
     * @param o
     * @return Formatted element containing no attributes except the class name
     */
    public Element store(Object o) {
        getInstance();

        Element e = new Element("connection");

        e.setAttribute("class", this.getClass().getName());

        return e;
    }

    /**
     * Update static data from XML file
     * @param e Top level Element to unpack.
      */
    public void load(Element e) {
        // hex file has no options in the XML
        jmri.jmrix.can.adapters.loopback.ActiveFlag.setActive();
        
        // register, so can be picked up
        getInstance();
        register();
    }


    protected void getInstance() {  
        // do system initialization
        
        // initialize dummry port
        adapter = Port.instance();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConnectionConfigXml.class.getName());

}