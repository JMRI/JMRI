package jmri.managers;

import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the InternalSensorManager interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2003, 2006
 * @deprecated As of 4.3.5, use jmri.jmrix.internal classes
 */
@Deprecated
public class InternalSensorManager extends AbstractSensorManager {

    public InternalSensorManager() {
        log.debug("InternalSensorManager constructed");
    }
    
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * Create an internal (dummy) sensor object
     *
     * @return new null
     */
    @Override
    protected Sensor createNewSensor(String systemName, String userName) {
        Sensor sen = new AbstractSensor(systemName, userName) {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        try {
            sen.setKnownState(getDefaultStateForNewSensors());
        } catch (jmri.JmriException ex) {
            log.error("An error occurred while trying to set initial state for sensor " + sen.getDisplayName());
            log.error(ex.toString());
        }
        log.debug("Internal Sensor \"{}\", \"{}\" created", systemName, userName);
        return sen;
    }

    static int defaultState = Sensor.UNKNOWN;

    public static synchronized void setDefaultStateForNewSensors(int defaultSetting) {
        log.debug("Default new-Sensor state set to {}", defaultSetting);
        defaultState = defaultSetting;
    }

    public static synchronized int getDefaultStateForNewSensors() {
        return defaultState;
    }

    protected String prefix = "I";

    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return NameValidity.VALID;
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {
        //If the hardware address passed does not already exist then this can
        //be considered the next valid address.
        Sensor s = getBySystemName(prefix + typeLetter() + curAddress);
        if (s == null) {
            return curAddress;
        }

        // This bit deals with handling the curAddress, and how to get the next address.
        int iName = 0;
        try {
            iName = Integer.parseInt(curAddress);
        } catch (NumberFormatException ex) {
            log.error("Unable to convert " + curAddress + " Hardware Address to a number");
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage("Error", "Unable to convert " + curAddress + " to a valid Hardware Address", "" + ex, "", true, false);
            return null;
        }
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        s = getBySystemName(prefix + typeLetter() + iName);
        if (s != null) {
            for (int x = 1; x < 10; x++) {
                iName = iName + 1;
                s = getBySystemName(prefix + typeLetter() + iName);
                if (s == null) {
                    return Integer.toString(iName);
                }
            }
            return null;
        } else {
            return Integer.toString(iName);
        }
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    private final static Logger log = LoggerFactory.getLogger(InternalSensorManager.class);

}
