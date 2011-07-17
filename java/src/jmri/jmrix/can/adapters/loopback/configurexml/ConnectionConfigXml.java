package jmri.jmrix.can.adapters.loopback.configurexml;

import jmri.jmrix.can.adapters.loopback.Port;
import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;

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
 * @author Bob Jacobsen Copyright: Copyright (c) 2008, 2010
 * @version $Revision: 1.6 $
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

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

        adapter = ((ConnectionConfig) o).getAdapter();
        Element e = new Element("connection");

        if (adapter.getCurrentOption1Setting()!=null)
            e.setAttribute("option1", adapter.getCurrentOption1Setting());

        if (adapter.getManufacturer()!=null)
            e.setAttribute("manufacturer", adapter.getManufacturer());

        e.setAttribute("class", this.getClass().getName());

        return e;
    }

    /**
     * Update static data from XML file
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element e) {
    	boolean result = true;
        adapter = new Port();
        // simulator has fewer options in the XML, so implement
        // just needed ones       
        if (e.getAttribute("option1")!=null) {
            String option1Setting = e.getAttribute("option1").getValue();
            adapter.configureOption1(option1Setting);
        }
        
        if (e.getAttribute("manufacturer")!=null) {
            String mfg = e.getAttribute("manufacturer").getValue();
            adapter.setManufacturer(mfg);
        }
        
        adapter.configure();
        
        // register, so can be picked up
        register();
        return result;
    }


    protected void getInstance() {  
        // obsolete?
        log.warn("getInstance called");
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfigXml.class.getName());

}