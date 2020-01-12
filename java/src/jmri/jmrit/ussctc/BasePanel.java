package jmri.jmrit.ussctc;

import javax.swing.JPanel;
import jmri.*;

/**
 * Refactored common routines and data for the GUI panels in this package.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class BasePanel extends JPanel implements Constants {

    BasePanel() {
    }

    void complain(String message, String value) {
        javax.swing.JOptionPane.showMessageDialog(this,
                java.text.MessageFormat.format(
                        Bundle.getMessage(message),
                        new Object[]{value}),
                Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    boolean validateTurnout(String name) {
        Turnout t = null;
        try {
            t = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        } catch (IllegalArgumentException e) {
            // no action taken; will recover later when t is null
            log.debug("could not create turnout \"{}\"", name);
        }
        if (t == null) {
            complain("ErrorNoTurnoutMatch", name);
            return false;
        }
        return true;
    }

    boolean validateSensor(String name) {
        Sensor t = null;
        try {
            t = InstanceManager.sensorManagerInstance().provideSensor(name);
        } catch (IllegalArgumentException e) {
            // no action taken; will recover later when t is null
            log.debug("could not create sensor \"{}\"", name);
        }
        if (t == null) {
            complain("ErrorNoSensorMatch", name);
            return false;
        }
        return true;
    }

    boolean validateMemory(String name) {
        Memory t = null;
        try {
            t = InstanceManager.memoryManagerInstance().provideMemory(name);
        } catch (IllegalArgumentException e) {
            // no action taken; will recover later when t is null
            log.debug("could not create memory \"{}\"", name);
        }
        if (t == null) {
            complain("ErrorNoMemoryMatch", name);
            return false;
        }
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BasePanel.class);
}
