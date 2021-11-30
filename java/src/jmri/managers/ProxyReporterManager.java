package jmri.managers;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Manager;
import jmri.Reporter;
import jmri.ReporterManager;

/**
 * Implementation of a ReporterManager that can serve as a proxy for multiple
 * system-specific implementations.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2010
 */
public class ProxyReporterManager extends AbstractProvidingProxyManager<Reporter> implements ReporterManager {

    public ProxyReporterManager() {
        super();
    }

    @Override
    protected AbstractManager<Reporter> makeInternalManager() {
        return jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class).getReporterManager();
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.REPORTERS;
    }

    /**
     * Locate via user name, then system name if needed.
     *
     * @return Null if nothing by that name exists
     */
    @Override
    @CheckForNull
    public Reporter getReporter(@Nonnull String name) {
        return super.getNamedBean(name);
    }

    @Override
    @Nonnull
    protected Reporter makeBean(Manager<Reporter> manager, String systemName, String userName) throws IllegalArgumentException {
        return ((ReporterManager) manager).newReporter(systemName, userName);
    }

    @Override
    @Nonnull
    public Reporter provideReporter(@Nonnull String sName) throws IllegalArgumentException {
        return super.provideNamedBean(sName);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Reporter provide(@Nonnull String name) throws IllegalArgumentException { return provideReporter(name); }

    @Override
    @CheckForNull
    public Reporter getByDisplayName(@Nonnull String key) {
        // First try to find it in the user list.
        // If that fails, look it up in the system list
        Reporter retv = this.getByUserName(key);
        if (retv == null) {
            retv = this.getBySystemName(key);
        }
        // If it's not in the system list, go ahead and return null
        return (retv);
    }

    /**
     * Get an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Reporter object representing a given physical Reporter and
     * therefore only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference for a valid request; a
     * new object will be created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the Reporter object created; a valid system name must be
     * provided
     * <li>If a null reference is given for the system name, a system name will
     * _somehow_ be inferred from the user name. How this is done is system
     * specific.
     * <li>If both names are provided, the system name defines the hardware
     * access of the desired Reporter, and the user address is associated with
     * it.
     * </ul>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Reporters when you should be looking them up.
     *
     * @return requested Reporter object (never null)
     */
    @Override
    @Nonnull
    public Reporter newReporter(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        return newNamedBean(systemName, userName);
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return ((ReporterManager) getManagerOrDefault(systemName)).allowMultipleAdditions(systemName);
    }

    @SuppressWarnings("deprecation") // user warned by actual manager class
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
        return getNextValidAddress(curAddress, prefix, typeLetter());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws jmri.JmriException {
        return getNextValidAddress(curAddress, prefix, ignoreInitialExisting, typeLetter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameReporters" : "BeanNameReporter"); // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Reporter> getNamedBeanClass() {
        return Reporter.class;
    }

}
