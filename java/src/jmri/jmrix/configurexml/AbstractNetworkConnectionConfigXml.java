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

    final static protected java.util.ResourceBundle rb = 
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

        storeCommon(e, adapter);
        if (adapter.getHostName()!=null)
            e.setAttribute("address", adapter.getHostName());
        else e.setAttribute("address", rb.getString("noneSelected"));

        if (adapter.getPort()!=0)
            e.setAttribute("port", ""+adapter.getPort());
        else e.setAttribute("port", rb.getString("noneSelected"));

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
        
        loadCommon(e, adapter);
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
