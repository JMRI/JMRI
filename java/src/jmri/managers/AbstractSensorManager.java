package jmri.managers;

import java.util.Enumeration;
import jmri.JmriException;
import jmri.Manager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalSystem;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;

/**
 * Abstract base implementation of the SensorManager interface.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
public abstract class AbstractSensorManager extends AbstractManager<Sensor> implements SensorManager {

    /**
     * Create a new SensorManager instance.
     * 
     * @param memo the system connection
     */
    public AbstractSensorManager(SystemConnectionMemo memo) {
        super(memo);
    }

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
    @Nonnull
    public Sensor provideSensor(@Nonnull String name) {
        Sensor t = getSensor(name);
        if (t == null) {
            t = newSensor(makeSystemName(name), null);
        }
        return t;
    }

    /** {@inheritDoc} */
    @Override
    public Sensor getSensor(@Nonnull String name) {
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
    public Sensor getBeanBySystemName(@Nonnull String key) {
        return this.getBySystemName(key);
    }
    
    /** {@inheritDoc} */
    @Override
    public Sensor getBySystemName(@Nonnull String key) {
        if (isNumber(key)) {
            key = makeSystemName(key);
        }
        return _tsys.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Sensor getByUserName(@Nonnull String key) {
        return _tuser.get(key);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Sensor newSensor(@Nonnull String sysName, String userName) throws IllegalArgumentException {
        log.debug(" newSensor(\"{}\", \"{}\")", sysName, userName);

        java.util.Objects.requireNonNull(sysName, "Generated systemName may not be null, started with "+sysName);

        sysName = validateSystemNameFormat(sysName);
        // return existing if there is one
        Sensor s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(sysName) != s) {
                log.error("inconsistent user ({}) and system name ({}) results; userName related to ({})", userName, sysName, s.getSystemName());
            }
            return s;
        }
        if ((s = getBySystemName(sysName)) != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found sensor via system name ({}) with non-null user name ({}). Sensor \"{}({})\" cannot be used.",
                        sysName, s.getUserName(), sysName, userName);
            }
            return s;
        }

        // doesn't exist, make a new one
        s = createNewSensor(sysName, userName);

        // if that failed, blame it on the input arguments
        if (s == null) {
            throw new IllegalArgumentException();
        }

        // save in the maps
        register(s);

        return s;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameSensors" : "BeanNameSensor");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Sensor> getNamedBeanClass() {
        return Sensor.class;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing Sensor has been invoked.
     *
     * @param systemName the system name to use for the new Sensor
     * @param userName   the user name to use for the new Sensor
     * @return a new Sensor
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
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
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
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        // If the hardware address passed does not already exist then this can
        // be considered the next valid address.
        String tmpSName;

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ErrorConvertNumberX", curAddress), null, "", true, false);
            return null;
        }
        Sensor s = getBySystemName(tmpSName);
        if (s == null) {
            return curAddress;
        }

        // This bit deals with handling the curAddress, and how to get the next address.
        int iName;
        try {
            iName = Integer.parseInt(curAddress);
        } catch (NumberFormatException ex) {
            log.error("Unable to convert {} Hardware Address to a number", curAddress);
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false);
            return null;
        }

        // Check to determine if the systemName is in use, return null if it is,
        // otherwise return the next valid address.
        s = getBySystemName(prefix + typeLetter() + iName);
        if (s != null) {
            for (int x = 1; x < 10; x++) {
                iName++;
                s = getBySystemName(prefix + typeLetter() + iName);
                if (s == null) {
                    return Integer.toString(iName);
                }
            }
            // feedback when next address is also in use
            log.warn("10 hardware addresses starting at {} already in use. No new Sensors added", curAddress);
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
    public void setDefaultSensorDebounceGoingActive(long time) {
        if (time == sensorDebounceGoingActive) {
            return;
        }
        sensorDebounceGoingActive = time;
        Enumeration<String> en = _tsys.keys();
        while (en.hasMoreElements()) {
            Sensor sen = _tsys.get(en.nextElement());
            if (sen.getUseDefaultTimerSettings()) {
                sen.setSensorDebounceGoingActiveTimer(time);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setDefaultSensorDebounceGoingInActive(long time) {
        if (time == sensorDebounceGoingInActive) {
            return;
        }
        sensorDebounceGoingInActive = time;
        Enumeration<String> en = _tsys.keys();
        while (en.hasMoreElements()) {
            Sensor sen = _tsys.get(en.nextElement());
            if (sen.getUseDefaultTimerSettings()) {
                sen.setSensorDebounceGoingInActiveTimer(time);
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
        return "Enter a number from 1 to 9999"; // Basic number format help
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractSensorManager.class);

}
