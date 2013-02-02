// ConnectionConfig.java

package jmri.jmrix.can.adapters.gridconnect.net;

import org.apache.log4j.Logger;
import java.util.ResourceBundle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import jmri.jmrix.can.ConfigurationManager;

/**
 * Definition of objects to handle configuring a connection
 * via a NetworkDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @version	$Revision$
 */
 public class ConnectionConfig  extends jmri.jmrix.AbstractNetworkConnectionConfig {

	public final static String NAME = "CAN via GridConnect Network Interface";
    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p){
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }
    
    public String name() { return NAME; }
    
    @Override
    protected void checkInitDone(){
        if (log.isDebugEnabled()) log.debug("init called for "+name());
        if (init) return;
        super.checkInitDone();

        updateUserNameField();

        ((JComboBox)options.get("Protocol").getComponent()).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateUserNameField();
            }
        });
    }
    
    void updateUserNameField() {
        String selection = options.get("Protocol").getItem();
        String newUserName = "MERG";
        if(ConfigurationManager.OPENLCB.equals(selection)){
            newUserName = "OpenLCB";
        } else if (ConfigurationManager.RAWCAN.equals(selection)){
            newUserName = "CANraw";
        } else if(ConfigurationManager.TEST.equals(selection)){
            newUserName = "CANtest";
        }
        connectionNameField.setText(newUserName);
        
        if(!adapter.getSystemConnectionMemo().setUserName(newUserName)){
            for (int x = 2; x<50; x++){
                if(adapter.getSystemConnectionMemo().setUserName(newUserName+x)){
                    connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    break;
                }
            }
        }
    }
    /**
     * Access to current selected command station mode
     */
    /*public String getMode() {
        return opt2Box.getSelectedItem().toString();
    }*/
    
    public boolean isPortAdvanced() { return false; }
    public boolean isOptList1Advanced() { return false; }
    
    protected void setInstance() {
        if (adapter==null){
            adapter = new NetworkDriverAdapter();
        }
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }
    
    static Logger log = Logger.getLogger(ConnectionConfig.class.getName());
}

