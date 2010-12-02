// StatusPane.java

package jmri.jmrix.ecos.swing.statusframe;

import jmri.JmriException;
import jmri.jmrix.ecos.*;

import javax.swing.*;

/**
 * Pane to show ECoS status
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision: 1.4 $
 */
public class StatusPane extends javax.swing.JPanel implements EcosListener {

    String appString = "Application Version: ";
    String proString = "   Protocol Version: ";
    String hrdString = "   Hardware Version: ";
    JLabel appVersion = new JLabel(appString+"<unknown>");
    JLabel proVersion = new JLabel(proString+"<unknown>");
    JLabel hrdVersion = new JLabel(hrdString+"<unknown>");
    
    public StatusPane(EcosTrafficController etc) {
        super();
        // Create GUI
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(appVersion);
        add(proVersion);
        add(hrdVersion);
        
        tc = etc;
        
        // connect to the TrafficManager
        tc.addEcosListener(this);
        
        // ask to be notified
        EcosMessage m = new EcosMessage("request(1, view)");
        tc.sendEcosMessage(m, this);
        
        // get initial state
        m = new EcosMessage("get(1, info)");
        tc.sendEcosMessage(m, this);
       
    }

    void reset() {
        appVersion.setText(appString+"<unknown>");
        proVersion.setText(proString+"<unknown>");
        hrdVersion.setText(hrdString+"<unknown>");
    }
    // to free resources when no longer used
    public void dispose(){
        tc.removeEcosListener(this);
        tc = null;
    }

    @SuppressWarnings("unused")
	private void checkTC() throws JmriException {
        if (tc == null) throw new JmriException("attempt to use EcosPowerManager after dispose");
    }

    EcosTrafficController tc = null;

    // to listen for status changes from Ecos system
    public void reply(EcosReply m) {
        // power message?
        String msg = m.toString();
        if (msg.contains("<EVENT 1>") || msg.contains("REPLY get(1,") ) {
            if (msg.contains("info")) {
                // probably right, extract info
                int first;
                int last;
                first = msg.indexOf("ProtocolVersion[");
                if (first > 0) {
                    last = msg.indexOf("]", first+16);
                    proVersion.setText(proString+msg.substring(first+16, last));
                }
                first = msg.indexOf("ApplicationVersion[");
                if (first > 0) {
                    last = msg.indexOf("]", first+19);
                    appVersion.setText(appString+msg.substring(first+19, last));
                }
                first = msg.indexOf("HardwareVersion[");
                if (first > 0) {
                    last = msg.indexOf("]", first+16);
                    hrdVersion.setText(hrdString+msg.substring(first+16, last));
                }
            }
        }
    }

    public void message(EcosMessage m) {
        // messages are ignored
    }

}


/* @(#)StatusPane.java */
