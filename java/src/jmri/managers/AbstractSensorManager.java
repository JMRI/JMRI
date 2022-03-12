package jmri.managers;

import java.util.Enumeration;
import java.util.Objects;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Manager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    boolean isNumber(@Nonnull String s) {
        synchronized (numberMatcher) {
            return numberMatcher.reset(s).matches();
        }
    }

    /** {@inheritDoc}
     * Special handling for numeric argument, which is treated as the suffix of a new system name
    */
    @Override

    public Sensor getBySystemName(@Nonnull String key) {
        if (isNumber(key)) {
            key = makeSystemName(key);
        }
        return _tsys.get(key);
    }

    /**
     * Create a New Sensor.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Sensor newSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        log.debug(" newSensor(\"{}\", \"{}\")", systemName, (userName == null ? "null" : userName));
        Objects.requireNonNull(systemName, "SystemName cannot be null. UserName was "
                + (userName == null ? "null" : userName));  // NOI18N
        systemName = validateSystemNameFormat(systemName);
        // return existing if there is one
        Sensor s;
        if (userName != null) {
            s = getByUserName(userName);
            if (s != null) {
                if (getBySystemName(systemName) != s) {
                    log.error("inconsistent user ({}) and system name ({}) results; userName related to ({})", userName, systemName, s.getSystemName());
                }
                return s;
            }
        }
        s = getBySystemName(systemName);
        if (s != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found sensor via system name ({}) with non-null user name ({}). Sensor \"{}({})\" cannot be used.",
                        systemName, s.getUserName(), systemName, userName);
            }
            return s;
        }
        // doesn't exist, make a new one
        s = createNewSensor(systemName, userName);
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
     * Internal method to invoke the factory and create a new Sensor.
     * 
     * Called after all the logic for returning an existing Sensor
     * has been invoked.
     * An existing SystemName is not found, existing UserName not found.
     * 
     * Implementing classes should base Sensor on the system name, then add user name.
     * 
     * @param systemName the system name to use for the new Sensor
     * @param userName   the optional user name to use for the new Sensor
     * @return the new Sensor
     * @throws IllegalArgumentException if unsuccessful with reason for fail.
     */
    @Nonnull
    abstract protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException;

    /**
     * {@inheritDoc}
     * Note that this null implementation only needs be implemented in
     * system-specific SensorManagers where readout of sensor status from the
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

    /**
     * Default Sensor ensures a numeric only system name.
     * {@inheritDoc} 
     */
    @Nonnull
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        return prefix + typeLetter() + checkNumeric(curAddress);
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
        return Bundle.getMessage("EnterNumber1to9999ToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractSensorManager.class);

}
