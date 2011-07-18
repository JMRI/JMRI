package jmri.jmrix.internal.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
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
        if (adapter.getSystemConnectionMemo()!=null){
            e.setAttribute("userName", adapter.getSystemConnectionMemo().getUserName());
            e.setAttribute("systemPrefix", adapter.getSystemConnectionMemo().getSystemPrefix());
        }
        if (adapter.getDisabled())
            e.setAttribute("disabled", "yes");
        else e.setAttribute("disabled", "no");
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
//        jmri.jmrix.internal.InternalSystemConnectionMemo memo = new jmri.jmrix.internal.InternalSystemConnectionMemo();
//        memo.configureManagers();
        
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfigXml.class.getName());

}