package jmri.jmrix.can.adapters.loopback.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }
    
    static java.util.ResourceBundle rb = 
        java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");

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

        if (adapter.getCurrentPortName()!=null)
            e.setAttribute("port", adapter.getCurrentPortName());
        else e.setAttribute("port", rb.getString("noneSelected"));
        if (adapter.getManufacturer()!=null)
            e.setAttribute("manufacturer", adapter.getManufacturer());
        if (adapter.getSystemConnectionMemo()!=null){
            e.setAttribute("userName", adapter.getSystemConnectionMemo().getUserName());
            e.setAttribute("systemPrefix", adapter.getSystemConnectionMemo().getSystemPrefix());
        }
        if (adapter.getDisabled())
            e.setAttribute("disabled", "yes");
        else e.setAttribute("disabled", "no");
        
        saveOptions(e, adapter);
        
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
        getInstance();
        // simulator has fewer options in the XML, so implement
        // just needed ones       
        if (adapter.getSystemConnectionMemo()!=null){
            if (e.getAttribute("userName")!=null) {
                adapter.getSystemConnectionMemo().setUserName(e.getAttribute("userName").getValue());
            }
            
            if (e.getAttribute("systemPrefix")!=null) {
                adapter.getSystemConnectionMemo().setSystemPrefix(e.getAttribute("systemPrefix").getValue());
            }
        }
        if (e.getAttribute("option1")!=null) {
            String option1Setting = e.getAttribute("option1").getValue();
            adapter.configureOption1(option1Setting);
        }
        
        if (e.getAttribute("manufacturer")!=null) {
            String mfg = e.getAttribute("manufacturer").getValue();
            adapter.setManufacturer(mfg);
        }
        if (e.getAttribute("port")!=null) {
            String portName = e.getAttribute("port").getValue();
            adapter.setPort(portName);
        }
        
        if (e.getAttribute("disabled")!=null) {
            String yesno = e.getAttribute("disabled").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("no")) adapter.setDisabled(false);
                    else if (yesno.equals("yes")) adapter.setDisabled(true);
                }
        }
        loadOptions(e.getChild("options"), adapter);
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
        adapter = new Port();
    }
    
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig)object).getAdapter();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
        log.info("CAN Simulator Started");     
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class.getName());

}
