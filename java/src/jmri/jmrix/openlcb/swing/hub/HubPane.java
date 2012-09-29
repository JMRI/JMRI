// HubPane.java

package jmri.jmrix.openlcb.swing.hub;

import javax.swing.*;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.swing.CanPanelInterface;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.adapters.gridconnect.GridConnectMessage;
import jmri.jmrix.can.adapters.gridconnect.GridConnectReply;

import org.openlcb.hub.*;

/**
 * Frame displaying,and more importantly starting, an OpenLCB TCP/IP hub
 *
 * @author	    Bob Jacobsen   Copyright (C) 2009, 2010, 2012
 * @version         $Revision: 17977 $
 */

public class HubPane extends jmri.util.swing.JmriPanel implements CanListener, CanPanelInterface {

    String nextLine; 
    public HubPane() {
        super();
        hub = new Hub() {
            public void notifyOwner(String line) {
                nextLine = line;
                SwingUtilities.invokeLater(
                    new Runnable() {
                        String message = nextLine;
                        public void run() {
                            try {
                              label.setText(message);
                            } catch (Exception x) {
                              x.printStackTrace();
                            }
                        } 
                    }
                );
            }
        };
    }

    CanSystemConnectionMemo memo;
    
    Hub hub;
    
    JLabel label = new JLabel("                                                 ");
    
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

        add(new JLabel("Port: "+hub.getPort()));
        add(label);
        
        startHubThread(hub.getPort());
    }
     
    void startHubThread(int port) {
        Thread t = new Thread(){
            public void run() {
                hub.start();
            }
        };
        t.setDaemon(true);
        
        // add forwarder
        hub.addForwarder(new Hub.Forwarding() {
            public void forward(Hub.Memo m) {
                if (m.source == null) return;  // was from this
                // process and forward m.line;
                GridConnectReply msg = new GridConnectReply();
                byte[] bytes = m.line.getBytes();
                for (int i = 0; i<m.line.length(); i++) {
                    msg.setElement(i, bytes[i]);
                }
                CanReply r = msg.createReply();
                
                CanMessage result = new CanMessage(r.getNumDataElements(), r.getHeader());
                for (int i = 0; i<r.getNumDataElements(); i++) {
                    result.setElement(i, r.getElement(i));
                }
                result.setExtended(r.isExtended());
                
                memo.getTrafficController().sendCanMessage(result, null);
                
            }
        });
        
        t.start();
        
        advertise(port);
    }
       
    void advertise(int port) {
        jmri.util.zeroconf.ZeroConfService.create("_openlcb-can._tcp.local.", port).publish();
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
        log.debug("message :"+l);
        GridConnectMessage gm = new GridConnectMessage(l);
        hub.putLine(gm.toString());
    }

    public synchronized void reply(CanReply l) {  // receive a reply and log it
        log.debug("reply :"+l);
        GridConnectMessage gm = new GridConnectMessage(new CanMessage(l));
        hub.putLine(gm.toString());
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
