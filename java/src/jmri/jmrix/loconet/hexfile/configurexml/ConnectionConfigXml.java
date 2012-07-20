package jmri.jmrix.loconet.hexfile.configurexml;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.loconet.hexfile.ConnectionConfig;
import jmri.jmrix.loconet.hexfile.LnHexFilePort;

import org.jdom.Element;

/**
 * Handle XML persistance of layout connections by persistening
 * the HexFIle LocoNet emuilator (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the HexFile info.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    /**
     * A HexFile connection needs no extra information, so
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

        if (adapter.getCurrentOption1Setting()!=null)
            e.setAttribute("option1", adapter.getCurrentOption1Setting());
        else e.setAttribute("option1", rb.getString("noneSelected"));

        if (adapter.getCurrentOption2Setting()!=null)
            e.setAttribute("option2", adapter.getCurrentOption2Setting());
        else e.setAttribute("option2", rb.getString("noneSelected"));
        
        if (adapter.getCurrentOption3Setting()!=null)
            e.setAttribute("option3", adapter.getCurrentOption3Setting());
        else e.setAttribute("option3", rb.getString("noneSelected"));
        
        if (adapter.getCurrentOption4Setting()!=null)
            e.setAttribute("option4", adapter.getCurrentOption4Setting());
        else e.setAttribute("option4", rb.getString("noneSelected"));
        
        if (adapter.getDisabled())
            e.setAttribute("disabled", "yes");
        else e.setAttribute("disabled", "no");

        e.setAttribute("class", this.getClass().getName());

        return e;
    }

    /**
     * Update instance data from XML file
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element e) {
        jmri.jmrix.loconet.hexfile.HexFileFrame f = null;
        jmri.jmrix.loconet.hexfile.HexFileServer hfs = null;
    	
    	getInstance();
        // hex file has no options in the XML

        GraphicsEnvironment.getLocalGraphicsEnvironment();
        // create GUI, unless running in headless mode
        if (!GraphicsEnvironment.isHeadless()) {
        	f = new jmri.jmrix.loconet.hexfile.HexFileFrame();
        	f.setAdapter((LnHexFilePort)adapter);
            try {
                f.initComponents();
            } catch (Exception ex) {
                //log.error("starting HexFileFrame exception: "+ex.toString());
            }
            f.pack();
            f.setVisible(true);
        } else {  // create and configure the headless server 
        	hfs = new jmri.jmrix.loconet.hexfile.HexFileServer();
        	hfs.setAdapter((LnHexFilePort)adapter);
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

        // register, so can be picked up
        register();
        if (adapter.getDisabled()){
        	if (!GraphicsEnvironment.isHeadless()) {
        		f.setVisible(false);
        	}
            return true;
        }
        if (!GraphicsEnvironment.isHeadless()) {
        	f.configure();
        } else {
        	hfs.configure();
        }
        return true;
    }


    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig)object).getAdapter();
    }

    protected void getInstance() {
        adapter = new LnHexFilePort();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfigXml.class.getName());

}