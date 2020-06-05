package jmri.managers;

import javax.annotation.Nonnull;
import jmri.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a Manager that can serves as a proxy for multiple
 * system-specific implementations.
 * <p>
 * Automatically includes an Internal system, which need not be separately added
 * any more.
 * <p>
 * Encapsulates access to the "Primary" manager, used by default, which is the
 * first one provided.
 * <p>
 * Internally, this is done by using an ordered list of all non-Internal
 * managers, plus a separate reference to the internal manager and default
 * manager.
 *
 * @param <E> the supported type of NamedBean
 * @author Bob Jacobsen Copyright (C) 2003, 2010, 2018
 */
@SuppressWarnings("deprecation")
abstract public class AbstractProvidingProxyManager<E extends NamedBean> extends AbstractProxyManager<E> implements ProvidingManager<E> {

    /**
     * Locate via user name, then system name if needed. If that fails, create a
     * new NamedBean: If the name is a valid system name, it will be used for
     * the new NamedBean. Otherwise, the makeSystemName method will attempt to
     * turn it into a valid system name. Subclasses use this to create provider methods such as
     * getSensor or getTurnout via casts.
     *
     * @param name the user name or system name of the bean
     * @return an existing or new NamedBean
     * @throws IllegalArgumentException if name is not usable in a bean
     */
    protected E provideNamedBean(String name) throws IllegalArgumentException {
        // make sure internal present
        initInternal();

        E t = getNamedBean(name);
        if (t != null) {
            return t;
        }
        // Doesn't exist. If the systemName was specified, find that system
        Manager<E> manager = getManager(name);
        if (manager != null) {
            return makeBean(manager, name, null);
        }
        log.debug("provideNamedBean did not find manager for name {}, defer to default", name); // NOI18N
        return makeBean(getDefaultManager(), getDefaultManager().makeSystemName(name), null);
    }

    /**
     * Defer creation of the proper type to the subclass.
     *
     * @param manager    the manager to invoke
     * @param systemName the system name
     * @param userName   the user name
     * @return a bean
     */
    abstract protected E makeBean(Manager<E> manager, String systemName, String userName);

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * i.e. only one Sensor object representing a given physical sensor and
     * therefore only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference for a valid request; a
     * new object will be created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the NamedBean object created; a valid system name must be
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
     * NamedBean when you should be looking them up.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return requested NamedBean object (never null)
     */
    public E newNamedBean(String systemName, String userName) {
        // make sure internal present
        initInternal();

        // if the systemName is specified, find that system
        Manager<E> m = getManager(systemName);
        if (m != null) {
            return makeBean(m, systemName, userName);
        }

        // did not find a manager, allow it to default to the primary
        log.debug("Did not find manager for system name {}, delegate to primary", systemName); // NOI18N
        return makeBean(getDefaultManager(), systemName, userName);
    }

    /**
     * Shared method to create a systemName based on the address base, the prefix and manager class.
     *
     * @param curAddress base address to use
     * @param prefix system prefix to use
     * @param managerType BeanType manager (method is used for Turnout and Sensor Managers)
     * @return a valid system name for this connection
     * @throws JmriException if systemName cannot be created
     */
    String createSystemName(String curAddress, String prefix, Class managerType) throws JmriException {
        for (Manager<E> m : mgrs) {
            if (prefix.equals(m.getSystemPrefix()) && managerType.equals(m.getClass())) {
                try {
                    if (managerType == TurnoutManager.class) {
                        return ((TurnoutManager) m).createSystemName(curAddress, prefix);
                    } else if (managerType == SensorManager.class) {
                        return ((SensorManager) m).createSystemName(curAddress, prefix);
                    } else {
                        log.warn("createSystemName requested for incompatible Manager");
                    }
                } catch (jmri.JmriException ex) {
                    throw ex;
                }
            }
        }
        throw new jmri.JmriException("Manager could not be found for System Prefix " + prefix);
    }

    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, char typeLetter) throws jmri.JmriException {
        for (Manager<E> m : mgrs) {
            log.debug("NextValidAddress requested for {}", curAddress);
            if (prefix.equals(m.getSystemPrefix()) && typeLetter == m.typeLetter()) {
                try {
                    switch (typeLetter) { // use #getDefaultManager() instead?
                        case 'T':
                            return ((TurnoutManager) m).getNextValidAddress(curAddress, prefix);
                        case 'S':
                            return ((SensorManager) m).getNextValidAddress(curAddress, prefix);
                        case 'R':
                            return ((ReporterManager) m).getNextValidAddress(curAddress, prefix);
                        default:
                            return null;
                    }
                } catch (jmri.JmriException ex) {
                    throw ex;
                }
            }
        }
        return null;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(AbstractProvidingProxyManager.class);

}
