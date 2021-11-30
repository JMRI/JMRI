package jmri.managers;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.Manager;

import jmri.Sensor;
import jmri.SensorManager;

/**
 * Implementation of a SensorManager that can serve as a proxy for multiple
 * system-specific implementations.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2010
 */
public class ProxySensorManager extends AbstractProvidingProxyManager<Sensor>
        implements SensorManager {

    public ProxySensorManager() {
        super();
    }

    @Override
    protected AbstractManager<Sensor> makeInternalManager() {
        return jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class).getSensorManager();
    }

    /**
     * Locate via user name, then system name if needed.
     *
     * @return Null if nothing by that name exists
     */
    @Override
    @CheckForNull
    public Sensor getSensor(@Nonnull String name) {
        return super.getNamedBean(name);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    protected Sensor makeBean(Manager<Sensor> manager, String systemName, String userName) throws IllegalArgumentException {
        return ((SensorManager) manager).newSensor(systemName, userName);
    }

    @Override
    @Nonnull
    public Sensor provideSensor(@Nonnull String sName) throws IllegalArgumentException {
        return super.provideNamedBean(sName);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Sensor provide(@Nonnull String name) throws IllegalArgumentException { return provideSensor(name); }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Sensor newSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        return newNamedBean(systemName, userName);
    }

    /**
     * Triggers #updateAll on all SensorManagers.
     */
    @Override
    public void updateAll() {
        getManagerList().forEach(m -> ((SensorManager) m).updateAll());
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return ((SensorManager) getManagerOrDefault(systemName)).allowMultipleAdditions(systemName);
    }

    @SuppressWarnings("deprecation") // user warned by actual manager class
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
        return getNextValidAddress(curAddress, prefix, typeLetter());
    }
    
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws jmri.JmriException {
        return getNextValidAddress(curAddress, prefix, ignoreInitialExisting, typeLetter());
    }

    @Override
    public long getDefaultSensorDebounceGoingActive() {
        return ((SensorManager) getDefaultManager()).getDefaultSensorDebounceGoingActive();
    }

    @Override
    public long getDefaultSensorDebounceGoingInActive() {
        return ((SensorManager) getDefaultManager()).getDefaultSensorDebounceGoingInActive();
    }

    @Override
    public void setDefaultSensorDebounceGoingActive(long timer) {
        getManagerList().forEach(m -> ((SensorManager) m).setDefaultSensorDebounceGoingActive(timer));
    }

    @Override
    public void setDefaultSensorDebounceGoingInActive(long timer) {
        getManagerList().forEach(m -> ((SensorManager) m).setDefaultSensorDebounceGoingInActive(timer));
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.SENSORS;
    }

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
     * Do the sensor objects provided by this manager support configuring
     * an internal pull up or pull down resistor?
     *
     * @return true if pull up/pull down configuration is supported,
     * default to false to satisfy the SensorManager interface
     */
    @Override
    public boolean isPullResistanceConfigurable(){
       return false;
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxySensorManager.class);
}
