// PowerPanelAction.java

package jmri.jmrit.powerpanel;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import java.util.ResourceBundle;

/**
 * Swing action to create and register a
 * PowerPanelFrame object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2001
 * @version         $Revision: 1.4 $
 */
public class PowerPanelAction extends AbstractAction {
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle");

    public PowerPanelAction(String s) { super(s);}

    public PowerPanelAction() {
        this(res.getString("TitlePowerPanel"));
    }

    public void actionPerformed(ActionEvent e) {
        // create a PowerPanelFrame
        PowerPanelFrame f = new PowerPanelFrame();
        f.show();
    }
}

/* @(#)PowerPanelAction.java */
