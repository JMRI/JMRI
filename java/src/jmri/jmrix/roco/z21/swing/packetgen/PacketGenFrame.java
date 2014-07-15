// PacketGenFrame.java

package jmri.jmrix.roco.z21.swing.packetgen;

import jmri.jmrix.roco.z21.z21Message;
import jmri.jmrix.roco.z21.z21TrafficController;

/**
 * Frame for user input of XpressNet messages
 * @author			Bob Jacobsen   Copyright (C) 2001,2002
 * @version			$Revision$
 */
public class PacketGenFrame extends jmri.jmrix.swing.AbstractPacketGenFrame {

    final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrix.roco.z21.z21ActionListBundle");

    public void initComponents() throws Exception {
        super.initComponents();
       
        // all we need to do is set the title 
        setTitle(rb.getString("jmri.jmrix.roco.z21.swing.packetgen.PacketGenActio"));
        
        // pack to cause display
        pack();
    }
    
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        tc.sendz21Message(createPacket(packetTextField.getSelectedItem().toString()), null);
    }
    
    z21Message createPacket(String s) {
        if(s.equals("")) return null; // message cannot be empty
        z21Message m = new z21Message(s,s.length()); 
        return m;
    }
    
    // connect to the TrafficController
    public void connect(z21TrafficController t) {
        tc = t;
    }
    
    
    // private data
    private z21TrafficController tc = null;
}
