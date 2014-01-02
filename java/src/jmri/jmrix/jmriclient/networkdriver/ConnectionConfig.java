// ConnectionConfig.java

package jmri.jmrix.jmriclient.networkdriver;

import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


/**
 * Definition of objects to handle configuring a connection to a remote
 * JMRI instance via the JMRI Network Protocol.
 *
 * @author      Paul Bender   Copyright (C) 2010
 * @version	$Revision$
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractNetworkConnectionConfig {

    protected JLabel transmitPrefixLabel = new JLabel("Server Connection Prefix");
    protected JTextField transmitPrefixField = new JTextField(10);


    /**
     * Constructor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p){
        super(p);
    }
    /**
     * Constructor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() { return "JMRI Network Connection"; }

    protected void setInstance() {
      if (adapter==null){
        adapter = new NetworkDriverAdapter();
      }
    }

    public boolean isPortAdvanced() {return true;}

    @Override
    protected void checkInitDone(){
       super.checkInitDone();
       if(adapter.getSystemConnectionMemo()!=null){
          transmitPrefixField.setText(((JMRIClientSystemConnectionMemo)adapter.getSystemConnectionMemo()).getTransmitPrefix());
          transmitPrefixField.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                ((JMRIClientSystemConnectionMemo)adapter.getSystemConnectionMemo()).setTransmitPrefix(transmitPrefixField.getText());
                transmitPrefixField.setText(((JMRIClientSystemConnectionMemo)adapter.getSystemConnectionMemo()).getTransmitPrefix());
             }
          });
          transmitPrefixField.addFocusListener( new FocusListener() {
             public void focusLost(FocusEvent e){
                ((JMRIClientSystemConnectionMemo)adapter.getSystemConnectionMemo()).setTransmitPrefix(transmitPrefixField.getText());
                transmitPrefixField.setText(((JMRIClientSystemConnectionMemo)adapter.getSystemConnectionMemo()).getTransmitPrefix());
             }     
             public void focusGained(FocusEvent e){ }
          });
       }
    }


    @Override
    protected void showAdvancedItems(){
       super.showAdvancedItems(); // we're adding to the normal advanced items.
       if(adapter.getSystemConnectionMemo()!=null){
            cR.gridy+=2;
            cL.gridy+=2;
            gbLayout.setConstraints(transmitPrefixLabel, cL);
            gbLayout.setConstraints(transmitPrefixField, cR);
            _details.add(transmitPrefixLabel);
            _details.add(transmitPrefixField); 
       }
        if (_details.getParent()!=null && _details.getParent() instanceof javax.swing.JViewport){
            javax.swing.JViewport vp = (javax.swing.JViewport)_details.getParent();
            vp.validate();
            vp.repaint();
        }

    }

    @Override public void updateAdapter(){
       super.updateAdapter(); // we're adding more details to the connection.
       if(adapter.getSystemConnectionMemo()!=null) {
          ((JMRIClientSystemConnectionMemo)adapter.getSystemConnectionMemo()).setTransmitPrefix(transmitPrefixField.getText());
       }
    } 
    
}

