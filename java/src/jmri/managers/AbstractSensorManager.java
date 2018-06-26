package jmri.managers;

import java.util.Enumeration;
import javax.annotation.*;
import jmri.JmriException;
import jmri.Manager;
import jmri.Sensor;
import jmri.SensorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base implementation of the SensorManager interface.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
public abstract class AbstractSensorManager extends AbstractManager<Sensor> implements SensorManager {

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.SENSORS;
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        return 'S';
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public Sensor getBeanBySystemName(String key) {
        return this.getBySystemName(key);
    }
    
    /** {@inheritDoc} */
    @Override
    public Sensor getBySystemName(String key) {
        if (isNumber(key)) {
            key = makeSystemName(key);
        }
        String name = normalizeSystemName(key);
        return _tsys.get(name);
    }

    /**
     * {@inheritDoc}
     * 
     * Forces upper case and trims leading and trailing whitespace.
     * Does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException.
     */
    @CheckReturnValue
    @Override
    public @Nonnull
    String normalizeSystemName(@Nonnull String inputName) {
        // does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException
        return inputName.toUpperCase().trim();
    }

    /** {@inheritDoc} */
    @Override
    protected Sensor getInstanceBySystemName(String systemName) {
        return getBySystemName(systemName);
    }

    /** {@inheritDoc} */
    @Override
    public Sensor getByUserName(String key) {
        return _tuser.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Sensor newSensor(String sysName, String userName) throws IllegalArgumentException {
        log.debug(" newSensor(\"{}\", \"{}\")", sysName, userName);
        String systemName = normalizeSystemName(sysName);
        log.debug("    normalized name: \"{}\"", systemName);

        java.util.Objects.requireNonNull(systemName, "Generated systemName may not be null, started with "+systemName);

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

    /** {@inheritDoc} */
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
     * {@inheritDoc}
     * Note that this null implementation only needs be implemented in
     * system-specific Sensor Managers where readout of sensor status from the
     * layout is possible.
     */
    @Override
    public void updateAll() {
    }

    /** {@inheritDoc} */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public long getDefaultSensorDebounceGoingActive() {
        return sensorDebounceGoingActive;
    }

    /** {@inheritDoc} */
    @Override
    public long getDefaultSensorDebounceGoingInActive() {
        return sensorDebounceGoingInActive;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
     * {@inheritDoc}
     * This default implementation always returns false.
     *
     * @return true if pull up/pull down configuration is supported.
     */
    @Override
    public boolean isPullResistanceConfigurable(){
       return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = "Enter a number from 1 to 9999"; // Basic number format help
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractSensorManager.class);

}
