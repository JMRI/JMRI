// ConnectionConfig.java

package jmri.jmrix.loconet.locormi;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Definition of objects to handle configuring  the layout connection
 * via LocoNet RMI.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision$
 */
 //@todo This class could ideally do with refactoring to the NetworkConnectionConfig and also multi-connection
public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(String p, String m){
        super();
        hostName = p;
        if (m!=null) manufacturerName = m;
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public JTextField host;
    String hostName ="";

    public String name() { 
        return "LocoNet Server";
    }
    
    public String getConnectionName() { 
        if((lmc!=null) && (lmc.getAdapterMemo()!=null)){
            return lmc.getAdapterMemo().getUserName();
        }
        return name();
    }
    
    public String getInfo(){
        return hostName;
    }

    public void setLnMessageClient(LnMessageClient ln){
        lmc = ln;
    }
    
    LnMessageClient lmc;
    
    public LnMessageClient getLnMessageClient() {
        return lmc;
    }
    
    public void loadDetails(JPanel details) {
        //details.setLayout(new BoxLayout(details, BoxLayout.X_AXIS));
        details.add(new JLabel("Server hostname:"));
        host = new JTextField(20);
        host.setText(hostName);
        details.add(host);
    }
    
    public boolean isOptList2Advanced() { return false; }
    
    protected void setInstance() {
        log.error("Unexpected call to setInstance");
        new Exception().printStackTrace();
    }
    
    String manufacturerName = jmri.jmrix.DCCManufacturerList.DIGITRAX;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }
    
    boolean disabled = false;
    
    public boolean getDisabled() {
        return disabled;
    }
    
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        if((lmc!=null) && (lmc.getAdapterMemo()!=null)){
            lmc.getAdapterMemo().setDisabled(disabled);
        }
    }
    
    
}

