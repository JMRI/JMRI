/**
 * LocoGenAction.java
 *
 * Description:		Swing action to create and register a
 *       			Loco(Mon)GenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version
 */

package jmri.jmrix.loconet.locogen;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;

public class LocoGenAction 			extends AbstractAction {

    public LocoGenAction(String s) { super(s);}

    public LocoGenAction() { this("Send LocoNet message");}

    public void actionPerformed(ActionEvent e) {
        // create a LocoGenFrame
        LocoGenFrame f = new LocoGenFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.setVisible(true);
        
        // connect to the LnTrafficController
        f.connect(LnTrafficController.instance());
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoGenAction.class.getName());
}


/* @(#)LocoGenAction.java */
