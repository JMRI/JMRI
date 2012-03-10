package jmri.jmrix.lenz.xnetsimulator.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
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
        
        if (adapter.getSystemConnectionMemo()!=null){
            e.setAttribute("userName", adapter.getSystemConnectionMemo().getUserName());
            e.setAttribute("systemPrefix", adapter.getSystemConnectionMemo().getSystemPrefix());
        }
        if (adapter.getManufacturer()!=null)
            e.setAttribute("manufacturer", adapter.getManufacturer());
        
        if (adapter.getDisabled())
            e.setAttribute("disabled", "yes");
        else e.setAttribute("disabled", "no");
        
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
        //adapter = new jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter();
        
        String manufacturer;
        try { 
            manufacturer = e.getAttribute("manufacturer").getValue();
            adapter.setManufacturer(manufacturer);
        } catch ( NullPointerException ex) { //Considered normal if not present
            
        }
        if (adapter.getSystemConnectionMemo()!=null){
            if (e.getAttribute("userName")!=null) {
                adapter.getSystemConnectionMemo().setUserName(e.getAttribute("userName").getValue());
            }
            
            if (e.getAttribute("systemPrefix")!=null) {
                adapter.getSystemConnectionMemo().setSystemPrefix(e.getAttribute("systemPrefix").getValue());
            }
        }
        
        if (e.getAttribute("disabled")!=null) {
            String yesno = e.getAttribute("disabled").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("no")) adapter.setDisabled(false);
                    else if (yesno.equals("yes")) adapter.setDisabled(true);
                }
        }
        
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
           //adapter.configure();
        }
    }
    
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig)object).getAdapter();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfigXml.class.getName());

}
