package jmri.jmrix.configurexml;

import jmri.configurexml.*;
import jmri.jmrix.NetworkPortAdapter;

import org.jdom.Element;

/**
 * Abstract base (and partial implementation) for
 * classes persisting the status of Network port adapters.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 */
abstract public class AbstractNetworkConnectionConfigXml extends AbstractConnectionConfigXml {

    public AbstractNetworkConnectionConfigXml() {
    }

    static protected java.util.ResourceBundle rb = 
        java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");
    
    protected NetworkPortAdapter adapter;
    abstract protected void getInstance();
    abstract protected void register();

    protected void getInstance(Object object) {
       getInstance(); // over-ridden during migration
    }

    /**
     * Default implementation for storing the static contents of the Network port implementation
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        getInstance(o);

        Element e = new Element("connection");
        // many of the following are required by the DTD; failing to include
        // them makes the XML file unreadable, but at least the next
        // invocation of the program can then continue.
        if (adapter.getSystemConnectionMemo()!=null){
            e.setAttribute("userName", adapter.getSystemConnectionMemo().getUserName());
            e.setAttribute("systemPrefix", adapter.getSystemConnectionMemo().getSystemPrefix());
        }
        if (adapter.getManufacturer()!=null)
            e.setAttribute("manufacturer", adapter.getManufacturer());
        if (adapter.getHostName()!=null)
            e.setAttribute("address", adapter.getHostName());
        else e.setAttribute("address", rb.getString("noneSelected"));

        if (adapter.getPort()!=0)
            e.setAttribute("port", ""+adapter.getPort());
        else e.setAttribute("port", rb.getString("noneSelected"));
        
        if (adapter.getDisabled())
            e.setAttribute("disabled", "yes");
        else e.setAttribute("disabled", "no");
        saveOptions(e, adapter);

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

        String hostName=null;
        try {
            hostName = e.getAttribute("address").getValue();
        } catch ( NullPointerException ex) {  // considered normal if the attributes are not present
        }
        adapter.setHostName(hostName);
        
        try {
            int port = e.getAttribute("port").getIntValue();
            adapter.setPort(port);
        } catch (org.jdom.DataConversionException ex) {
            log.warn("Could not parse port attribute");
        } catch ( NullPointerException ex) {  // considered normal if the attributes are not present
        }
        
        if (e.getAttribute("option1")!=null) {
            String option1Setting = e.getAttribute("option1").getValue();
            adapter.configureOption1(option1Setting);
        }
        if (e.getAttribute("option2")!=null) {
            String option2Setting = e.getAttribute("option2").getValue();
            adapter.configureOption2(option2Setting);
        }
        if (e.getAttribute("option3")!=null) {
            String option3Setting = e.getAttribute("option3").getValue();
            adapter.configureOption3(option3Setting);
        }
        if (e.getAttribute("option4")!=null) {
            String option4Setting = e.getAttribute("option4").getValue();
            adapter.configureOption4(option4Setting);
        }
        
        loadOptions(e.getChild("options"), adapter);
        
        String manufacturer;
        try { 
            manufacturer = e.getAttribute("manufacturer").getValue();
            adapter.setManufacturer(manufacturer);
        } catch ( NullPointerException ex) { //Considered normal if not present
            
        }


        if (adapter.getSystemConnectionMemo()!=null){
            if (e.getAttribute("userName")!=null){
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
        try{
            adapter.connect();
        } catch (Exception ex) {
            ConfigXmlManager.creationErrorEncountered(
                                        null, "opening connection",
                                        org.apache.log4j.Level.ERROR,
                                        ex.getMessage(),
                                        null,null,null
                                    );            
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractNetworkConnectionConfigXml.class.getName());

}
