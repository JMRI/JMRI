// HubPane.java

package jmri.jmrix.openlcb.swing.hub;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import jmri.util.JmriJFrame;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.swing.CanPanelInterface;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;

import org.openlcb.MimicNodeStore;
import org.openlcb.Connection;

import org.openlcb.hub.*;

/**
 * Frame displaying,and more importantly starting, an OpenLCB TCP/IP hub
 *
 * @author	    Bob Jacobsen   Copyright (C) 2009, 2010, 2012
 * @version         $Revision: 17977 $
 */

public class HubPane extends jmri.util.swing.JmriPanel implements CanListener, CanPanelInterface {

    public HubPane() {
        super();
        hub = new Hub();
    }

    CanSystemConnectionMemo memo;
    
    Hub hub;
    
    public void initContext(Object context) {
        if (context instanceof CanSystemConnectionMemo ) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }
    
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;

        memo.getTrafficController().addCanListener(this);
        
        // add GUI components
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        add(new JLabel("Port: "+hub.PORT));
        
        startHubThread();
    }
     
    void startHubThread() {
        Thread t = new Thread(){
            public void run() {
                hub.start();
            }
        };
        t.setDaemon(true);
        t.start();
    }
       
    public String getTitle() {
        return "OpenLCB Hub Control";
    }

    protected void init() {
    }

    public void dispose() {
       memo.getTrafficController().removeCanListener(this);
    }

    public synchronized void message(CanMessage l) {  // receive a message and log it
    }

    public synchronized void reply(CanReply l) {  // receive a reply and log it
    }
    
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {
        public Default() {
            super("Openlcb Hub Control", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                HubPane.class.getName(), 
                jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HubPane.class.getName());

}
