package jmri.managers;

import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.Manager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract partial implementation of a ReporterManager.
 *
 * @author Bob Jacobsen Copyright (C) 2004
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
    @Nonnull
    public Reporter provideReporter(@Nonnull String sName) throws IllegalArgumentException {
        Reporter r = getReporter(sName);
        return r == null ? newReporter(makeSystemName(sName, true), null) : r;
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public Reporter getReporter(@Nonnull String name) {
        Reporter r = getByUserName(name);
        return r == null ? getBySystemName(name) : r;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
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
    @CheckForNull
    public Reporter getByDisplayName(@Nonnull String key) {
        return getReporter(key);        
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Reporter newReporter(@Nonnull String systemName, @CheckForNull String userName) throws IllegalArgumentException {
        Objects.requireNonNull(systemName, "SystemName cannot be null. UserName was "
               + ((userName == null) ? "null" : userName));  // NOI18N
        log.debug("new Reporter: {} {}", systemName, userName);

       // is system name in correct format?
        if (!systemName.startsWith(getSystemNamePrefix())
                || !(systemName.length() > (getSystemNamePrefix()).length())) {
            log.error("Invalid system name for reporter: {} needed {}{}",
                    systemName, getSystemPrefix(), typeLetter());
            throw new IllegalArgumentException("Invalid system name for turnout: " + systemName
                    + " needed " + getSystemNamePrefix());
        }

        // return existing if there is one
        Reporter r;
        if (userName != null) {
            r = getByUserName(userName);
            if (r!=null) {
                if (getBySystemName(systemName) != r) {
                    log.error("inconsistent user ({}) and system name ({}) results; userName related to ({})",
                            userName, systemName, r.getSystemName());
                }
                return r;
            }
        }
        r = getBySystemName(systemName);
        if (r != null) {
            if ((r.getUserName() == null) && (userName != null)) {
                r.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found reporter via system name ({}}) with non-null user name ({}})", systemName, userName);
            }
            return r;
        }

        // doesn't exist, make a new one
        r = createNewReporter(systemName, userName);

        // Some implementations of createNewReporter() registers the bean, some
        // don't. Check if the bean is registered and register it if it isn't
        // registered.
        if (getBySystemName(systemName) == null) {
            // save in the maps
            register(r);
        }
        return r;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing Reporter has been invoked.
     *
     * @param systemName system name.
     * @param userName username.
     * @return never null
     */
    @Nonnull
    abstract protected Reporter createNewReporter(@Nonnull String systemName, String userName) throws IllegalArgumentException;

    /** {@inheritDoc} */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("EnterNumber1to9999ToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractReporterManager.class);

}
