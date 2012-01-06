// CanSendAction.java

package jmri.jmrix.can.swing.send;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.can.TrafficController;

/**
 * Create and register a tool to send CAN frames.
 *
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version         $Revision$
 * @deprecated 2.99.2
 */
@Deprecated
public class CanSendAction extends AbstractAction {

    public CanSendAction(String s) { super(s);}

    public CanSendAction() { this("Send CAN Frame");}

    public void actionPerformed(ActionEvent e) {
        CanSendFrame f = new CanSendFrame();
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CanSendAction.class.getName());
}


/* @(#)CanSendAction.java */
