package jmri.jmrix.sprog.swing;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a PowerPanelFrame object.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class PowerPanelAction extends AbstractAction {

    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle");

    public PowerPanelAction(String s) {
        super(s);

        // disable ourself if there is no power Manager
        if (jmri.InstanceManager.getDefault(jmri.PowerManager.class) == null) {
            setEnabled(false);
        }
    }

    public PowerPanelAction() {
        this(res.getString("TitlePowerPanel"));
    }

    public void actionPerformed(ActionEvent e) {
        // create a PowerPanelFrame
        PowerPanelFrame f = new PowerPanelFrame();
        f.setVisible(true);
    }
}

/* @(#)PowerPanelAction.java */
