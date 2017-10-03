package jmri.managers;

import java.util.Enumeration;
import jmri.JmriException;
import jmri.Manager;
import jmri.Sensor;
import jmri.SensorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base implementation of the SensorManager interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2003
 */
public abstract class AbstractSensorManager extends AbstractManager<Sensor> implements SensorManager {

    @Override
    public int getXMLOrder() {
        return Manager.SENSORS;
    }

    @Override
    public char typeLetter() {
        return 'S';
    }

    @Override
    public Sensor provideSensor(String name) {
        Sensor t = getSensor(name);
        if (t != null) {
            return t;
        }
        log.debug("check \"{}\" get {}", name, isNumber(name));
        if (isNumber(name)) {
            return newSensor(makeSystemName(name), null);
        } else if (name.length() > 0) {
            return newSensor(name, null);
        } else {
            throw new IllegalArgumentException("Name must have non-full length");
        }
    }

    @Override
    public Sensor getSensor(String name) {
        Sensor t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    static final java.util.regex.Matcher numberMatcher = java.util.regex.Pattern.compile("\\d++").matcher("");

    boolean isNumber(String s) {
        synchronized (numberMatcher) {
            return numberMatcher.reset(s).matches();
        }
    }

    @Override
    public Sensor getBeanBySystemName(String key) {
        return this.getBySystemName(key);
    }
    
    @Override
    public Sensor getBySystemName(String key) {
        if (isNumber(key)) {
            key = makeSystemName(key);
        }
        String name = normalizeSystemName(key);
        return _tsys.get(name);
    }

    @Override
    protected Sensor getInstanceBySystemName(String systemName) {
        return getBySystemName(systemName);
    }

    @Override
    public Sensor getByUserName(String key) {
        return _tuser.get(key);
    }

    @Override
    public Sensor newSensor(String sysName, String userName) throws IllegalArgumentException {
        log.debug(" newSensor(\"{}\", \"{}\"", sysName, userName);
        String systemName = normalizeSystemName(sysName);
        if (log.isDebugEnabled()) {
            log.debug("newSensor:"
                    + ((systemName == null) ? "null" : systemName)
                    + ";" + ((userName == null) ? "null" : userName));
        }
        if (systemName == null) {
            log.error("SystemName cannot be null. UserName was "
                    + ((userName == null) ? "null" : userName));
            throw new IllegalArgumentException("systemName null in newSensor");
        }
        // is system name in correct format?
        if (!systemName.startsWith(getSystemPrefix() + typeLetter()) 
                || !(systemName.length() > (getSystemPrefix() + typeLetter()).length())) {
            log.debug("Invalid system name for sensor: " + systemName
                    + " needed " + getSystemPrefix() + typeLetter());
            throw new IllegalArgumentException("systemName \""+systemName+"\" bad format in newSensor");
        }

        // return existing if there is one
        Sensor s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user (" + userName + ") and system name (" + systemName + ") results; userName related to (" + s.getSystemName() + ")");
            }
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found sensor via system name (" + systemName
                        + ") with non-null user name (" + s.getUserName() + "). Sensor \""
                        + systemName + "(" + userName + ")\" cannot be used.");
            }
            return s;
        }

        // doesn't exist, make a new one
        s = createNewSensor(systemName, userName);

        // if that failed, blame it on the input arguements
        if (s == null) {
            throw new IllegalArgumentException();
        }

        // save in the maps
        register(s);

        return s;
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameSensor");
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @return new null
     */
    abstract protected Sensor createNewSensor(String systemName, String userName);

    /**
     * Requests status of all layout sensors under this Sensor Manager. This
     * method may be invoked whenever the status of sensors needs to be updated
     * from the layout, for example, when an XML configuration file is read in.
     * Note that this null implementation only needs be implemented in
     * system-specific Sensor Managers where readout of sensor status from the
     * layout is possible.
     */
    @Override
    public void updateAll() {
    }

    /**
     * A method that determines if it is possible to add a range of sensors in
     * numerical order eg 10 to 30, primarily used to enable/disable the add
     * range box in the add sensor panel.
     *
     * @param systemName configured system connection name
     * @return false as default, unless overridden by implementations as supported
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        try {
            Integer.parseInt(curAddress);
        } catch (java.lang.NumberFormatException ex) {
            log.warn("Hardware Address passed should be a number, was {}", curAddress);
            throw new JmriException("Hardware Address passed should be a number");
        }
        return prefix + typeLetter() + curAddress;
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {
        // If the hardware address passed does not already exist then this can
        // be considered the next valid address.
        String tmpSName = "";

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("WarningTitle"), "Unable to convert " + curAddress + " to a valid Hardware Address", null, "", true, false);
            return null;
        }
        Sensor s = getBySystemName(tmpSName);
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
                    showErrorMessage(Bundle.getMessage("WarningTitle"), "Unable to convert " + curAddress + " to a valid Hardware Address", "" + ex, "", true, false);
            return null;
        }

        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        s = getBySystemName(prefix + typeLetter() + iName);
        if (s != null) {
            for (int x = 1; x < 10; x++) {
                iName++;
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

    protected long sensorDebounceGoingActive = 0L;
    protected long sensorDebounceGoingInActive = 0L;

    @Override
    public long getDefaultSensorDebounceGoingActive() {
        return sensorDebounceGoingActive;
    }

    @Override
    public long getDefaultSensorDebounceGoingInActive() {
        return sensorDebounceGoingInActive;
    }

    @Override
    public void setDefaultSensorDebounceGoingActive(long timer) {
        if (timer == sensorDebounceGoingActive) {
            return;
        }
        sensorDebounceGoingActive = timer;
        Enumeration<String> en = _tsys.keys();
        while (en.hasMoreElements()) {
            Sensor sen = _tsys.get(en.nextElement());
            if (sen.getUseDefaultTimerSettings()) {
                sen.setSensorDebounceGoingActiveTimer(timer);
            }
        }
    }

    @Override
    public void setDefaultSensorDebounceGoingInActive(long timer) {
        if (timer == sensorDebounceGoingInActive) {
            return;
        }
        sensorDebounceGoingInActive = timer;
        Enumeration<String> en = _tsys.keys();
        while (en.hasMoreElements()) {
            Sensor sen = _tsys.get(en.nextElement());
            if (sen.getUseDefaultTimerSettings()) {
                sen.setSensorDebounceGoingInActiveTimer(timer);
            }
        }
    }

    /**
     * Do the sensor objects provided by this manager support configuring
     * an internal pullup or pull down resistor?
     * <p>
     * This default implementation always returns false.
     *
     * @return true if pull up/pull down configuration is supported.
     */
    @Override
    public boolean isPullResistanceConfigurable(){
       return false;
    }

    /**
     * Provide a manager-agnostic tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = "Enter a number from 1 to 9999"; // Basic number format help
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractSensorManager.class);

}
