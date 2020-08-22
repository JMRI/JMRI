package jmri.managers;

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
    public Sensor getSensor(@Nonnull String name) {
        return super.getNamedBean(name);
    }

    @Override
    protected Sensor makeBean(Manager<Sensor> manager, String systemName, String userName) {
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

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Sensor object representing a given physical turnout and
     * therefore only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference for a valid request; a
     * new object will be created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the Turnout object created; a valid system name must be
     * provided
     * <li>If a null reference is given for the system name, a system name will
     * _somehow_ be inferred from the user name. How this is done is system
     * specific. Note: a future extension of this interface will add an
     * exception to signal that this was not possible.
     * <li>If both names are provided, the system name defines the hardware
     * access of the desired turnout, and the user address is associated with
     * it.
     * </ul>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Sensors when you should be looking them up.
     *
     * @return requested Sensor object (never null)
     */
    @Override
    @Nonnull
    public Sensor newSensor(@Nonnull String systemName, String userName) {
        return newNamedBean(systemName, userName);
    }

    // null implementation to satisfy the SensorManager interface
    @Override
    public void updateAll() {
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return ((SensorManager) getManagerOrDefault(systemName)).allowMultipleAdditions(systemName);
    }

    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
        return createSystemName(curAddress, prefix, SensorManager.class);
    }

    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
        return getNextValidAddress(curAddress, prefix, typeLetter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return "Enter a number from 1 to 9999"; // Basic number format help
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
