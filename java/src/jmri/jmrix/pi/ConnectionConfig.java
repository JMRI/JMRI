// ConnectionConfig.java

package jmri.jmrix.pi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuring a Raspberry Pi layout connection.
 * <P>
 * This uses the {@link RaspberryPiAdapter} class to do the actual
 * connection.
 *
 * @author      Paul Bender  Copyright (C) 2015
 * @version	$Revision$
 *
 * @see RaspberryPiAdapter
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {

    private boolean disabled = false;
    private RaspberryPiAdapter adapter = null;

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(RaspberryPiAdapter p){
        super();
        adapter = p;
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
        adapter = new RaspberryPiAdapter();
    }


    protected boolean init = false;

    @Override
    protected void checkInitDone(){
        if (log.isDebugEnabled()) {
            log.debug("init called for " + name());
        }
        if (init) {
            return;
        }
        if (adapter.getSystemConnectionMemo() != null) {
            systemPrefixField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                }
            });
            systemPrefixField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                }
            });
            connectionNameField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                }
            });
            connectionNameField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                }
                public void focusGained(FocusEvent e) {
                }
            });

        }
        init = true;

    }

    @Override
    public void updateAdapter(){
        if (adapter.getSystemConnectionMemo() != null && !adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }

    }

    @Override
    protected void showAdvancedItems(){
    }

    @Override
    public void loadDetails(final javax.swing.JPanel details){
       _details = details;
       setInstance();
       if(!init) {
          if (adapter.getSystemConnectionMemo() != null) {
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
            NUMOPTIONS = NUMOPTIONS + 2;
          }
          addStandardDetails(adapter,false,NUMOPTIONS);
          init = false;
          checkInitDone();
       } 
    }

    @Override
    protected void setInstance(){
       if(adapter==null){
          adapter = new RaspberryPiAdapter();
       }
    }

    @Override
    public jmri.jmrix.PortAdapter getAdapter(){
      return adapter;
    }

    @Override
    public String getInfo() { return "GPIO"; }

    @Override
    public String getManufacturer(){
      return RaspberryPiConnectionTypeList.PI;
    }

    @Override
    public void setManufacturer(String manufacturer){
    }

    @Override
    public String name(){
       return getConnectionName();
    }

    @Override
    public String getConnectionName(){
       return "Raspberry Pi GPIO";
    }

    @Override
    public boolean getDisabled(){
       return disabled;
    }

    @Override
    public void setDisabled(boolean disable){
       this.disabled=disable;
    }

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfig.class.getName());
}


