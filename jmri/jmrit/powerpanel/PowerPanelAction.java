// PowerPanelAction.java

package jmri.jmrit.powerpanel;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * PowerPanelFrame object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2001
 * @version         $Revision: 1.3 $
 */
public class PowerPanelAction extends AbstractAction {

    public PowerPanelAction(String s) { super(s);}

    public PowerPanelAction() {
        this("Power Panel");
    }

    public void actionPerformed(ActionEvent e) {
        // create a PowerPanelFrame
        PowerPanelFrame f = new PowerPanelFrame();
        f.show();
    }
}

/* @(#)PowerPanelAction.java */
