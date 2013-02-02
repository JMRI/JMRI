// BasePanel.java

package jmri.jmrit.ussctc;

import org.apache.log4j.Logger;
import jmri.*;
import javax.swing.*;

/**
 * Refactored common routines and data for the GUI panels in this package.
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2007
 * @version			$Revision$
 */
public class BasePanel extends JPanel implements Constants {

    BasePanel() {
        if (rb == null) rb = java.util.ResourceBundle.getBundle("jmri.jmrit.ussctc.UssCtcBundle");
    }

    static java.util.ResourceBundle rb = null;

    void complain(String message, String value) {
        javax.swing.JOptionPane.showMessageDialog(this,
            java.text.MessageFormat.format(
                rb.getString(message),
                new Object[]{value}),
            rb.getString("ErrorTitle"),
            javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    boolean validateTurnout(String name) {
        Turnout t = null;
        try {
            t = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        } catch (Exception e) {
            // no action taken; will recover later when t is null
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
        } catch (Exception e) {
            // no action taken; will recover later when t is null
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
        } catch (Exception e) {
            // no action taken; will recover later when t is null
        }
        if (t == null) { 
            complain("ErrorNoMemoryMatch", name);
            return false;
        }
        return true;
    }
    
    static Logger log = Logger.getLogger(BasePanel.class.getName());

}
