package jmri.jmrix.internal.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.SerialPortAdapter;
import jmri.jmrix.internal.ConnectionConfig;
import jmri.jmrix.internal.InternalAdapter;

import org.jdom.*;

/**
 * Handle XML persistance of virtual layout connections
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2010
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }
    
    protected void getInstance() {
        adapter = new InternalAdapter();
    }
    
    protected SerialPortAdapter adapter;

    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig)object).getAdapter();
    }

    /*protected void getInstance() {
        log.error("unexpected call to getInstance");
        new Exception().printStackTrace();
    }*/

    public Element store(Object o) {
        getInstance(o);
        Element e = new Element("connection");
        storeCommon(e, adapter);
        
        e.setAttribute("class", this.getClass().getName());

        return e;
    }
    /**
     * Port name carries the hostname for the network connection
     * @param e Top level Element to unpack.
      */
    public boolean load(Element e) {
    	boolean result = true;
        getInstance();

        loadCommon(e, adapter);
        // register, so can be picked up
        register();
        
        if (adapter.getDisabled()){
            return result;
        }
        adapter.configure();
        
        return result;
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class.getName());

}
