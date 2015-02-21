// PowerPanelAction.java
package jmri.jmrit.powerpanel;

import java.util.ResourceBundle;

/**
 * Swing action to create and register a PowerPanelFrame object.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2010
 * @version $Revision$
 */
public class PowerPanelAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     *
     */
    private static final long serialVersionUID = -6470371810613141456L;
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle");

    public PowerPanelAction(String s, jmri.util.swing.WindowInterface wi) {
        super(s, wi, "jmri.jmrit.powerpanel.PowerPane");
        checkManager();
    }

    public PowerPanelAction(String s, javax.swing.Icon i, jmri.util.swing.WindowInterface wi) {
        super(s, i, wi, "jmri.jmrit.powerpanel.PowerPane");
        checkManager();
    }

    public PowerPanelAction(String s) {
        super(s, "jmri.jmrit.powerpanel.PowerPane");
        checkManager();
    }

    public PowerPanelAction() {
        this(res.getString("TitlePowerPanel"));
    }

    void checkManager() {
        // disable ourself if there is no power Manager
        if (jmri.InstanceManager.powerManagerInstance() == null) {
            setEnabled(false);
        }
    }

}

/* @(#)PowerPanelAction.java */
