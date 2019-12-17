package jmri.managers;

import javax.annotation.Nonnull;

import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a SensorManager that can serves as a proxy for multiple
 * system-specific implementations.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2010
 */
public class ProxySensorManager extends AbstractProxyManager<Sensor>
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
    public Sensor getSensor(String name) {
        return super.getNamedBean(name);
    }

    @Override
    protected Sensor makeBean(int i, String systemName, String userName) throws IllegalArgumentException {
        log.debug("makeBean({}, \"{}\", \"{}\"", i, systemName, userName);
        return ((SensorManager) getMgr(i)).newSensor(systemName, userName);
    }

    @Override
    public Sensor provideSensor(String sName) throws IllegalArgumentException {
        return super.provideNamedBean(sName);
    }

    @Override
    /** {@inheritDoc} */
    public Sensor provide(@Nonnull String name) throws IllegalArgumentException { return provideSensor(name); }

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists.
     *
     * @return requested Turnout object or null if none exists
     */
    @Override
    public Sensor getBySystemName(String sName) {
        return super.getBeanBySystemName(sName);
    }

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists.
     *
     * @return requested Turnout object or null if none exists
     */
    @Override
    public Sensor getByUserName(String userName) {
        return super.getBeanByUserName(userName);
    }

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
    public Sensor newSensor(String systemName, String userName) {
        return newNamedBean(systemName, userName);
    }

    // null implementation to satisfy the SensorManager interface
    @Override
    public void updateAll() {
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        int i = matchTentative(systemName);
        if (i >= 0) {
            return ((SensorManager) getMgr(i)).allowMultipleAdditions(systemName);
        }
        return ((SensorManager) getMgr(0)).allowMultipleAdditions(systemName);
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws jmri.JmriException {
        for (int i = 0; i < nMgrs(); i++) {
            if (prefix.equals(
                    ((SensorManager) getMgr(i)).getSystemPrefix())) {
                try {
                    return ((SensorManager) getMgr(i)).createSystemName(curAddress, prefix);
                } catch (jmri.JmriException ex) {
                    log.error(ex.toString());
                    throw ex;
                }
            }
        }
        throw new jmri.JmriException("Sensor Manager could not be found for System Prefix " + prefix);
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) throws jmri.JmriException {
        for (int i = 0; i < nMgrs(); i++) {
            if (prefix.equals(
                    ((SensorManager) getMgr(i)).getSystemPrefix())) {
                try {
                    return ((SensorManager) getMgr(i)).getNextValidAddress(curAddress, prefix);
                } catch (jmri.JmriException ex) {
                    throw ex;
                }
            }
        }
        return null;
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
        return ((SensorManager) getMgr(0)).getDefaultSensorDebounceGoingActive();
    }

    @Override
    public long getDefaultSensorDebounceGoingInActive() {
        return ((SensorManager) getMgr(0)).getDefaultSensorDebounceGoingInActive();
    }

    @Override
    public void setDefaultSensorDebounceGoingActive(long timer) {
        for (int i = 0; i < nMgrs(); i++) {
            ((SensorManager) getMgr(i)).setDefaultSensorDebounceGoingActive(timer);
        }
    }

    @Override
    public void setDefaultSensorDebounceGoingInActive(long timer) {
        for (int i = 0; i < nMgrs(); i++) {
            ((SensorManager) getMgr(i)).setDefaultSensorDebounceGoingInActive(timer);
        }
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.SENSORS;
    }

    @Override
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
     * an internal pullup or pull down resistor?
     * <p>
     * Return false to satisfy the SensorManager interface.
     *
     * @return true if pull up/pull down configuration is supported.
     */
    @Override
    public boolean isPullResistanceConfigurable(){
       return false;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ProxySensorManager.class);

}
