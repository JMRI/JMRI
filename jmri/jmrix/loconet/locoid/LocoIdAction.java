package jmri.jmrix.loconet.locoid;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;

/**
 * LocoIdAction.java
 *
 * Swing action to create and register a
 *       			LocoidFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2006
 * @version         $Revision: 1.1 $
 */
public class LocoIdAction 			extends AbstractAction {

    public LocoIdAction(String s) { super(s);}

    public LocoIdAction() { this("Send LocoNet message");}

    public void actionPerformed(ActionEvent e) {
        // create a LocoIdFrame
        LocoIdFrame f = new LocoIdFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.show();
        
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIdAction.class.getName());
}


/* @(#)LocoidAction.java */
