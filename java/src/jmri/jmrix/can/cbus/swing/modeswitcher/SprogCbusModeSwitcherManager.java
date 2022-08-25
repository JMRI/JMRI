package jmri.jmrix.can.cbus.swing.modeswitcher;

import static jmri.PowerManager.PROGPOWERENABLE;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrix.can.ConfigurationManager;

/**
 * Class to manage power mode switcher
 * 
 * Want to add property change support to mode switcher, which already extends 
 * another class so this manager extend PCS and will allow firing of property 
 * changes from the mode switcher.
 * 
 * @author Andrew Crosland (C) 2022
 */
public class SprogCbusModeSwitcherManager extends PropertyChangeSupport {

    /**
     * Fires a {@link java.beans.PropertyChangeEvent} for the programming track
     * power state using property name "progpowerenable".
     *
     * @param enable true if prog track power control is enabled
     */
    protected final void fireProgPowerPropertyChange(boolean enable) {
        firePropertyChange(PROGPOWERENABLE, null, enable);
    }
        
}
