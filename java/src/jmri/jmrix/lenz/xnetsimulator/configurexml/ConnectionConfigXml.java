package jmri.jmrix.lenz.xnetsimulator.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.SerialPortAdapter;
import jmri.jmrix.lenz.xnetsimulator.ConnectionConfig;
import jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter;

import org.jdom.Element;

/**
 * Handle XML persistance of layout connections by persistening
 * the XNetSimulatorAdapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the XNetSimulatorAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Paul Bender  Copyright: Copyright (c) 2009
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }
    
    protected SerialPortAdapter adapter;

    /**
     * A Simulator connection needs no extra information, so
     * we reimplement the superclass method to just write the
     * necessary parts.
     * @param o
     * @return Formatted element containing no attributes except the class name
     */
    public Element store(Object o) {
        getInstance(o);

        Element e = new Element("connection");
        storeCommon(e, adapter);
        
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
        // start the "connection"
        getInstance();
        
        loadCommon(e, adapter);
        
        // register, so can be picked up next time
        register();
        
        if (adapter.getDisabled()){
            unpackElement(e);
            return result;
        }
        
        adapter.configure();

        return result;
    }


    protected void getInstance() {
        if(adapter==null){
           adapter = new XNetSimulatorAdapter();
        }
    }
    
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig)object).getAdapter();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class.getName());

}
