package jmri.jmrit.powerpanel;

import java.util.ResourceBundle;
import javax.swing.Action;
import jmri.PowerManager;

/**
 * Swing action to create and register a PowerPanelFrame object.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2010
 */
public class PowerButtonAction extends javax.swing.AbstractAction implements java.beans.PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -2336270044595139511L;

    public PowerButtonAction(String title) {
        super(title);
        checkManager();
        updateLabel();
    }

    public PowerButtonAction() {
        this(ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle").getString("ButtonPowerOnOff"));
    }

    void checkManager() {
        // disable ourself if there is no power Manager
        if (jmri.InstanceManager.powerManagerInstance() == null) {
            setEnabled(false);
        } else {
            jmri.InstanceManager.powerManagerInstance().addPropertyChangeListener(this);
        }
    }

    void updateLabel() {
        try {
            PowerManager p = jmri.InstanceManager.powerManagerInstance();
            if (p.getPower() != PowerManager.ON) {
                putValue(Action.NAME, ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle").getString("ButtonSetOn"));
            } else {
                putValue(Action.NAME, ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle").getString("ButtonSetOff"));
            }
            firePropertyChange(Action.NAME, "", getValue(Action.NAME));
        } catch (jmri.JmriException ex) {
            return;
        }
    }

    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        updateLabel();
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        try {
            // alternate power state, updating name
            PowerManager p = jmri.InstanceManager.powerManagerInstance();
            if (p == null) {
                return;
            }
            if (p.getPower() != PowerManager.ON) {
                p.setPower(PowerManager.ON);
            } else {
                p.setPower(PowerManager.OFF);
            }
        } catch (Exception ex) {
            return;
        }
        updateLabel();
    }

}
