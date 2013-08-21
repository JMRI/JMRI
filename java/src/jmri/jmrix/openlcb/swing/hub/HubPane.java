// HubPane.java

package jmri.jmrix.openlcb.swing.hub;

import java.net.*;
import javax.swing.*;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.adapters.gridconnect.GridConnectMessage;
import jmri.jmrix.can.adapters.gridconnect.GridConnectReply;
import jmri.jmrix.can.swing.CanPanelInterface;
import org.openlcb.hub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    transient Hub hub;
    
    JLabel label = new JLabel("                                                 ");
    
    public void initContext(Object context) {
        if (context instanceof CanSystemConnectionMemo ) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }
    
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;

        // This hears OpenLCB traffic at packet level from traffic controller
        memo.getTrafficController().addCanListener(this);
        
        // add GUI components
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        
        try {
            add(new JLabel("Hub IP address "+InetAddress.getLocalHost().getHostAddress()+":"+hub.getPort()));
        } catch (UnknownHostException e) { log.error(e.getLocalizedMessage(), e); }
        add(label);
        
        startHubThread(hub.getPort());
    }
    
    Thread t;
    
    void startHubThread(int port) {
        t = new Thread(){
            public void run() {
                hub.start();
            }
        };
        t.setDaemon(true);
        
        // add forwarder for internal JMRI traffic
        hub.addForwarder(new Hub.Forwarding() {
            public void forward(Hub.Memo m) {
                if (m.source == null) return;  // was from this
                // process and forward m.line;
                GridConnectReply msg = new GridConnectReply();
                byte[] bytes;
                try {
                    bytes = m.line.getBytes("US-ASCII");  // GC adapters use ASCII // NOI18N
                } catch (java.io.UnsupportedEncodingException e) {
                    log.error("Cannot proceed with GC input message since US-ASCII not supported");
                    return;
                }
                for (int i = 0; i<m.line.length(); i++) {
                    msg.setElement(i, bytes[i]);
                }
                workingReply = msg.createReply();
                
                CanMessage result = new CanMessage(workingReply.getNumDataElements(), workingReply.getHeader());
                for (int i = 0; i<workingReply.getNumDataElements(); i++) {
                    result.setElement(i, workingReply.getElement(i));
                }
                result.setExtended(workingReply.isExtended());
                
                // Send over outbound link
                memo.getTrafficController().sendCanMessage(result, HubPane.this);
                // And send into JMRI
                memo.getTrafficController().distributeOneReply(workingReply, HubPane.this);
            }
        });
        
        t.start();
        
        advertise(port);
    }
    
    // For testing
    void stopHubThread() {
        if (t != null) {
            t.stop();
            t = null;
        }
    }

    CanReply workingReply;
    
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
        GridConnectMessage gm = new GridConnectMessage(l);
        if (log.isDebugEnabled()) log.debug("message "+gm.toString());
        hub.putLine(gm.toString());
    }

    public synchronized void reply(CanReply reply) {
        if (reply !=workingReply) {
            GridConnectMessage gm = new GridConnectMessage(new CanMessage(reply));
            if (log.isDebugEnabled()) log.debug("reply "+gm.toString());
            hub.putLine(gm.toString());
        }
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
    
    static Logger log = LoggerFactory.getLogger(HubPane.class.getName());

}
