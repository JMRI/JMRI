// ClockMonAction.java

package jmri.jmrix.loconet.clockmon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;


/**
 * Create and register a ClockMonFrame object.
 *
 * @author			Bob Jacobsen    Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class ClockMonAction extends AbstractAction {

    public ClockMonAction(String s) { super(s);}
    public ClockMonAction() {
        this("LocoNet clock monitor");
    }

    public void actionPerformed(ActionEvent e) {
        ClockMonFrame f = new ClockMonFrame();
        f.show();
    }
}

/* @(#)ClockMonAction.java */
