package jmri.jmrix.ecos.networkdriver.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.ecos.networkdriver.ConnectionConfig;
import jmri.jmrix.ecos.networkdriver.NetworkDriverAdapter;
import jmri.jmrix.ecos.EcosPreferences;
import java.util.List;

import org.jdom.*;
import javax.swing.*;

/**
 * Handle XML persistance of layout connections by persistening
 * the NetworkDriverAdapter (and connections).
 * <P>
 * Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the NetworkDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 208
 * @version $Revision: 1.5 $
 */
public class ConnectionConfigXml extends AbstractConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        log.error("unexpected call to getInstance");
        new Exception().printStackTrace();
    }

    public Element store(Object o) {
        ConnectionConfig c = (ConnectionConfig)o;
        Element e = new Element("connection");

        e.setAttribute("port", c.host.getText());
        e.setAttribute("option1", c.port.getText());
        e.setAttribute("option2", c.getMode());
        
        Element ecosPrefElem = new Element("commandStationPreferences");
        //EcosPreferences p = (EcosPreferences)jmri.InstanceManager.getDefault(EcosPreferences.class);
        EcosPreferences p = EcosPreferences.instance();
        //Element e = new Element("ECOSPreferences");
        if(p.getAddTurnoutsToEcos()==0x01) ecosPrefElem.setAttribute("addTurnoutToCS", "no");
        else if(p.getAddTurnoutsToEcos()==0x02) ecosPrefElem.setAttribute("addTurnoutToCS", "yes");
        
        if(p.getRemoveTurnoutsFromEcos()==0x01) ecosPrefElem.setAttribute("removeTurnoutFromCS", "no");
        else if(p.getRemoveTurnoutsFromEcos()==0x02) ecosPrefElem.setAttribute("removeTurnoutFromCS", "yes");
        
        if(p.getAddTurnoutsToJMRI()==0x01) ecosPrefElem.setAttribute("addTurnoutToJMRI", "no");
        else if(p.getAddTurnoutsToJMRI()==0x02) ecosPrefElem.setAttribute("addTurnoutToJMRI", "yes");
        
        if(p.getRemoveTurnoutsFromJMRI()==0x01) ecosPrefElem.setAttribute("removeTurnoutFromJMRI", "no");
        else if(p.getRemoveTurnoutsFromJMRI()==0x02) ecosPrefElem.setAttribute("removeTurnoutFromJMRI", "yes");
        
        if(p.getLocoMaster()>0x00) ecosPrefElem.setAttribute("locoMaster", p.getLocoMasterAsString());
        
        if(p.getAddLocoToEcos()==0x01) ecosPrefElem.setAttribute("addLocoToCS", "no");
        if(p.getAddLocoToEcos()==0x02) ecosPrefElem.setAttribute("addLocoToCS", "yes");
        
        if(p.getRemoveLocoFromEcos()==0x01) ecosPrefElem.setAttribute("removeLocoFromCS", "no");
        else if(p.getRemoveLocoFromEcos()==0x02) ecosPrefElem.setAttribute("removeLocoFromCS", "yes");
        
        if(p.getAddLocoToJMRI()==0x01) ecosPrefElem.setAttribute("addLocoToJMRI", "no");
        else if(p.getAddLocoToJMRI()==0x02) ecosPrefElem.setAttribute("addLocoToJMRI", "yes");
        
        if(p.getRemoveLocoFromJMRI()==0x01) ecosPrefElem.setAttribute("removeLocoFromJMRI", "no");
        else if(p.getRemoveLocoFromJMRI()==0x01) ecosPrefElem.setAttribute("removeLocoFromJMRI", "yes");
        
        if(p.getAdhocLocoFromEcos()==0x01) ecosPrefElem.setAttribute("removeAdhocLocoFromCS", "no");
        else if (p.getAdhocLocoFromEcos()==0x02) ecosPrefElem.setAttribute("removeAdhocLocoFromCS", "yes");
        
        if(p.getForceControlFromEcos()==0x01) ecosPrefElem.setAttribute("forceControlFromCS", "no");
        else if (p.getForceControlFromEcos()==0x02) ecosPrefElem.setAttribute("forceControlCS", "yes");
        
        if(!p.getDefaultEcosProtocol().equals("DCC128")) ecosPrefElem.setAttribute("defaultCSProtocol", p.getDefaultEcosProtocol());
        if(p.getEcosLocoDescription()!=null){
            if(!p.getEcosLocoDescription().equals(""))
                ecosPrefElem.setAttribute("defaultCSLocoDescription",p.getEcosLocoDescription());
        }
        e.addContent(ecosPrefElem);
        e.setAttribute("class", this.getClass().getName());

        return e;
    }
    /**
     * Port name carries the hostname for the network connection
     * @param e Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
    public boolean load(Element e) {
    	boolean result = true;
        // configure port name
        String hostName = e.getAttribute("port").getValue();
        String portNumber = e.getAttribute("option1").getValue();
        String mode = "";
        if (e.getAttribute("option2") != null) {
            mode = e.getAttribute("option2").getValue();
        }
        
        // notify
        JFrame f = new JFrame("ECOS network connection");
        f.getContentPane().add(new JLabel("Connecting to "+hostName+":"+portNumber));
        f.pack();
        f.setVisible(true);

        // slightly different, as not based on a serial port...
        // create the adapter
        NetworkDriverAdapter client = new NetworkDriverAdapter();
        
        // load configuration
        client.configureOption2(mode);
        client.setHostName(hostName);

        // start the connection
        try {
            client.connect(hostName, Integer.parseInt(portNumber));
        } catch (Exception ex) {
            log.error("Error opening connection to "+hostName+" was: "+ex);
            result = false;
        }

        // configure the other instance objects
        client.configure();

        f.setVisible(false);
        f.dispose();

        // register, so can be picked up
        register(hostName, portNumber, mode);
        
        List<Element> ecosPref = e.getChildren("commandStationPreferences");
        EcosPreferences p = EcosPreferences.instance();
        for (int i=0; i<ecosPref.size();i++){
            if (ecosPref.get(i).getAttribute("addTurnoutToCS") != null){
                String yesno = ecosPref.get(i).getAttribute("addTurnoutToCS").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setAddTurnoutsToEcos(0x02);
                    else if (yesno.equals("no")) p.setAddTurnoutsToEcos(0x01);
                }
            }
            if (ecosPref.get(i).getAttribute("removeTurnoutFromCS") != null){
                String yesno = ecosPref.get(i).getAttribute("removeTurnoutFromCS").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setRemoveTurnoutsFromEcos(0x02);
                    else if (yesno.equals("no")) p.setRemoveTurnoutsFromEcos(0x01);
                }
            }
            
            if (ecosPref.get(i).getAttribute("addTurnoutToJMRI") != null){
                String yesno = ecosPref.get(i).getAttribute("addTurnoutToJMRI").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setAddTurnoutsToJMRI(0x02);
                    else if (yesno.equals("no")) p.setAddTurnoutsToJMRI(0x01);
                }
            }
            
            if (ecosPref.get(i).getAttribute("removeTurnoutFromJMRI") != null){
                String yesno = ecosPref.get(i).getAttribute("removeTurnoutFromJMRI").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setRemoveTurnoutsFromJMRI(0x02);
                    else if (yesno.equals("no")) p.setRemoveTurnoutsFromJMRI(0x01);
                }
            }
            
            if (ecosPref.get(i).getAttribute("addLocoToCS") != null){
                String yesno = ecosPref.get(i).getAttribute("addLocoToCS").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setAddLocoToEcos(0x02);
                    else if (yesno.equals("no")) p.setAddLocoToEcos(0x01);
                }
            }

            if (ecosPref.get(i).getAttribute("removeLocoFromCS") != null){
                String yesno = ecosPref.get(i).getAttribute("removeLocoFromCS").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setRemoveLocoFromEcos(0x02);
                    else if (yesno.equals("no")) p.setRemoveLocoFromEcos(0x01);
                }
            }
            
            if (ecosPref.get(i).getAttribute("addLocoToJMRI") != null){
                String yesno = ecosPref.get(i).getAttribute("addLocoToJMRI").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setAddLocoToJMRI(0x02);
                    else if (yesno.equals("no")) p.setAddLocoToJMRI(0x01);
                }
            }
            
            if (ecosPref.get(i).getAttribute("removeLocoFromJMRI") != null){
                String yesno = ecosPref.get(i).getAttribute("removeLocoFromJMRI").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setRemoveLocoFromJMRI(0x02);
                    else if (yesno.equals("no")) p.setRemoveLocoFromJMRI(0x01);
                }
            }
            
            if (ecosPref.get(i).getAttribute("removeLocoFromJMRI") != null){
                String yesno = ecosPref.get(i).getAttribute("removeLocoFromJMRI").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setRemoveLocoFromJMRI(0x02);
                    else if (yesno.equals("no")) p.setRemoveLocoFromJMRI(0x01);
                }
            }
            
            if (ecosPref.get(i).getAttribute("locoMaster") != null){
                p.setLocoMaster(ecosPref.get(i).getAttribute("locoMaster").getValue());
            }
            
            if (ecosPref.get(i).getAttribute("removeAdhocLocoFromCS") != null){
                String yesno = ecosPref.get(i).getAttribute("removeAdhocLocoFromCS").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setAdhocLocoFromEcos(0x02);
                    else if (yesno.equals("no")) p.setAdhocLocoFromEcos(0x01);
                }
            }
            if (ecosPref.get(i).getAttribute("defaultCSProtocol") != null){
                p.setDefaultEcosProtocol(ecosPref.get(i).getAttribute("defaultCSProtocol").getValue());
            }
            if (ecosPref.get(i).getAttribute("defaultCSLocoDescription") != null){
                p.setEcosLocoDescription(ecosPref.get(i).getAttribute("defaultCSLocoDescription").getValue());
            }
            
            p.resetChangeMade();
         }
        return result;
    }

    protected void register() {
        log.error("unexpected call to register()");
        new Exception().printStackTrace();
    }
    protected void register(String host, String port, String mode) {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(host, port, mode));
        //InstanceManager.configureManagerInstance().registerPref(new jmri.jmrix.ecos.EcosPreferences());
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfigXml.class.getName());

}