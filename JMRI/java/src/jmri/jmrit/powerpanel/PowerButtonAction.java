package jmri.jmrit.powerpanel;

import javax.swing.Action;
import jmri.PowerManager;

/**
 * Swing action to create and register a PowerPanelFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2010
 */
public class PowerButtonAction extends javax.swing.AbstractAction implements java.beans.PropertyChangeListener {

    public PowerButtonAction(String title) {
        super(title);
        checkManager();
        updateLabel();
    }

    public PowerButtonAction() {
        this(Bundle.getMessage("ButtonPowerOnOff"));
    }

    void checkManager() {
        // disable ourself if there is no power Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.PowerManager.class) == null) {
            setEnabled(false);
        } else {
            jmri.InstanceManager.getDefault(jmri.PowerManager.class).addPropertyChangeListener(this);
        }
    }

    void updateLabel() {
        try {
            PowerManager p = jmri.InstanceManager.getDefault(jmri.PowerManager.class);
            if (p.getPower() != PowerManager.ON) {
                putValue(Action.NAME, Bundle.getMessage("ButtonSetOn"));
            } else {
                putValue(Action.NAME, Bundle.getMessage("ButtonSetOff"));
            }
            firePropertyChange(Action.NAME, "", getValue(Action.NAME));
        } catch (jmri.JmriException ex) {
            return;
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        updateLabel();
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        try {
            // alternate power state, updating name
            PowerManager p = jmri.InstanceManager.getNullableDefault(jmri.PowerManager.class);
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
