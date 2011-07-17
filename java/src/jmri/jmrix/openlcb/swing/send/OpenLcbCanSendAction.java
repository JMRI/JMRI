// OpenLcbCanSendAction.java

package jmri.jmrix.openlcb.swing.send;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.can.TrafficController;

/**
 * Create and register a tool to send OpenLCB CAN frames.
 *
 * @author			Bob Jacobsen    Copyright (C) 2010
 * @version         $Revision: 1.1 $
 */
public class OpenLcbCanSendAction extends AbstractAction {

    public OpenLcbCanSendAction(String s) { super(s);}

    public OpenLcbCanSendAction() { this("Send OpenLCB CAN Frame");}

    public void actionPerformed(ActionEvent e) {
        OpenLcbCanSendFrame f = new OpenLcbCanSendFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.setVisible(true);
        
        // connect to the CanInterface
        f.connect(TrafficController.instance());
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OpenLcbCanSendAction.class.getName());
}


/* @(#)OpenLcbCanSendAction.java */
