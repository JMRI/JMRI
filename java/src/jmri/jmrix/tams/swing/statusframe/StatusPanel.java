// StatusPanel.java

package jmri.jmrix.tams.swing.statusframe;

import jmri.JmriException;
import jmri.jmrix.tams.*;
import java.util.ResourceBundle;

import javax.swing.*;

/**
 * Panel to show TAMS status
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version	$Revision: 19655 $
 */
public class StatusPanel extends jmri.jmrix.tams.swing.TamsPanel implements TamsListener {

    String appString = "Application Version : ";
    String serString = "Serial Number : ";
    JLabel appVersion = new JLabel(appString+"<unknown>");
    JLabel serVersion = new JLabel(serString+"<unknown>");

    
    JButton sendButton;
    
    public StatusPanel() {
        super();
    }
    
    public void initComponents(TamsSystemConnectionMemo memo) {
        super.initComponents(memo);
        //memo.getTrafficController().addTamsListener(this);
        tc = memo.getTrafficController();
        // Create GUI
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(appVersion);
        add(serVersion);
        
        // ask to be notified
        TamsMessage m = new TamsMessage("xV");
        tc.sendTamsMessage(m, this);
        
        sendButton = new JButton("Update");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Request status update from TAMS System");
        
        add(sendButton);
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });
    }
    
    void reset() {
        appVersion.setText(appString+"<unknown>");
    }
    // to free resources when no longer used
    public void dispose(){
        tc.removeTamsListener(this);
        tc = null;
    }
    
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        reset();
        TamsMessage m = new TamsMessage("xV");
        tc.sendTamsMessage(m, this);

	}

    @SuppressWarnings("unused")
	private void checkTC() throws JmriException {
        if (tc == null) throw new JmriException("attempt to use TamsPowerManager after dispose");
    }

    TamsTrafficController tc;

    // to listen for status changes from Tams system
    public void reply(TamsReply m) {
        // power message?
        String msg = m.toString();
        String[] version = msg.split("\\r");
        appVersion.setText(appString + version[0]);
        serVersion.setText(serString + version[1]);
    }

    public void message(TamsMessage m) {
        // messages are ignored
    }
   
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.tams.swing.TamsNamedPaneAction {
        public Default() {
            super(ResourceBundle.getBundle("jmri.jmrix.tams.TamsBundle").getString("MenuItemInfo"), 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                StatusPanel.class.getName(), 
                jmri.InstanceManager.getDefault(TamsSystemConnectionMemo.class));
        }
    }

}


/* @(#)StatusPane.java */
