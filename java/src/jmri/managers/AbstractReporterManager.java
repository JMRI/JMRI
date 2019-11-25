package jmri.managers;

import java.util.Objects;

import jmri.Manager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.SignalSystem;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract partial implementation of a ReporterManager.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 */
public abstract class AbstractReporterManager extends AbstractManager<Reporter>
        implements ReporterManager {

    /**
     * Create a new ReporterManager instance.
     * 
     * @param memo the system connection
     */
    public AbstractReporterManager(SystemConnectionMemo memo) {
        super(memo);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.REPORTERS;
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        return 'R';
    }

    /** {@inheritDoc} */
    @Override
    public Reporter provideReporter(String sName) {
        Reporter t = getReporter(sName);
        if (t != null) {
            return t;
        }
        if (sName.startsWith(getSystemPrefix() + typeLetter())) {
            return newReporter(sName, null);
        } else {
            return newReporter(makeSystemName(sName), null);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Reporter getReporter(String name) {
        Reporter t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    public Reporter getBySystemName(String name) {
        return _tsys.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public Reporter getByUserName(String key) {
        return _tuser.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameReporters" : "BeanNameReporter");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Reporter> getNamedBeanClass() {
        return Reporter.class;
    }

    /** {@inheritDoc} */
    @Override
    public Reporter getByDisplayName(String key) {
        // First try to find it in the user list.
        // If that fails, look it up in the system list
        Reporter retv = this.getByUserName(key);
        if (retv == null) {
            retv = this.getBySystemName(key);
        }
        // If it's not in the system list, go ahead and return null
        return (retv);
    }

    /** {@inheritDoc} */
    @Override
    public Reporter newReporter(String systemName, String userName) {
        Objects.requireNonNull(systemName, "SystemName cannot be null. UserName was "+ ((userName == null) ? "null" : userName));  // NOI18N

        log.debug("new Reporter: {} {}", systemName, userName);

       // is system name in correct format?
        if (!systemName.startsWith(getSystemPrefix() + typeLetter())
                || !(systemName.length() > (getSystemPrefix() + typeLetter()).length())) {
            log.error("Invalid system name for reporter: {} needed {}{}",
                    systemName, getSystemPrefix(), typeLetter());
            throw new IllegalArgumentException("Invalid system name for turnout: " + systemName
                    + " needed " + getSystemPrefix() + typeLetter());
        }

        // return existing if there is one
        Reporter r;
        if ((userName != null) && ((r = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != r) {
                log.error("inconsistent user (" + userName + ") and system name (" + systemName + ") results; userName related to (" + r.getSystemName() + ")");
            }
            return r;
        }
        if ((r = getBySystemName(systemName)) != null) {
            if ((r.getUserName() == null) && (userName != null)) {
                r.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found reporter via system name (" + systemName
                        + ") with non-null user name (" + userName + ")");
            }
            return r;
        }

        // doesn't exist, make a new one
        r = createNewReporter(systemName, userName);

        // Some implementations of createNewReporter() registers the bean, some
        // don't. Check if the bean is registered and register it if it isn't
        // registered.
        if (getBeanBySystemName(systemName) == null) {
            // save in the maps
            register(r);
        }

        return r;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @return Never null
     */
    abstract protected Reporter createNewReporter(String systemName, String userName);

    /** {@inheritDoc} */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getNextValidAddress(String curAddress, String prefix) {
        // If the hardware address passed does not already exist then this can
        // be considered the next valid address.
        Reporter r = getBySystemName(prefix + typeLetter() + curAddress);
        if (r == null) {
            return curAddress;
        }

        // This bit deals with handling the curAddress, and how to get the next address.
        int iName = 0;
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
        r = getBySystemName(prefix + typeLetter() + iName);
        if (r != null) {
            for (int x = 1; x < 10; x++) {
                iName++;
                r = getBySystemName(prefix + typeLetter() + iName);
                if (r == null) {
                    return Integer.toString(iName);
                }
            }
            // feedback when next address is also in use
            log.warn("10 hardware addresses starting at {} already in use. No new Reporters added", curAddress);
            return null;
        } else {
            return Integer.toString(iName);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return "Enter a number from 1 to 9999"; // Basic number format help
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractReporterManager.class);

}
