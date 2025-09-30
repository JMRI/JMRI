package jmri.managers;

import javax.annotation.Nonnull;
import java.util.Objects;

import jmri.StringIO;
import jmri.Manager;
import jmri.SystemConnectionMemo;
import jmri.StringIOManager;

/**
 * Abstract partial implementation of a StringIOManager.
 * <p>
 * Based on AbstractSignalHeadManager.java and AbstractSensorManager.java
 *
 * @author Dave Duchamp      Copyright (C) 2004
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public abstract class AbstractStringIOManager extends AbstractManager<StringIO>
        implements StringIOManager {

    /**
     * Create a new StringIOManager instance.
     * 
     * @param memo the system connection
     */
    public AbstractStringIOManager(SystemConnectionMemo memo) {
        super(memo);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.STRINGIOS;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public StringIO provideStringIO(@Nonnull String name) {
        var t = getStringIO(name);
        if (t == null) {
            t = newStringIO(makeSystemName(name), null);
        }
        return t;
    }

    @Override
    public StringIO getStringIO(@Nonnull String name) {
        var t = getByUserName(name);
        if (t != null) {
            return t;
        }
        return getBySystemName(name);
    }

    /**
     * Create a New StringIO.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    final public StringIO newStringIO(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        log.debug(" newStringIO(\"{}\", \"{}\")", systemName, (userName == null ? "null" : userName));
        Objects.requireNonNull(systemName, "SystemName cannot be null. UserName was "
                + (userName == null ? "null" : userName));  // NOI18N
        systemName = validateSystemNameFormat(systemName);
        // return existing if there is one
        StringIO s;
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
                log.warn("Found StringIO via system name ({}) with non-null user name ({}). StringIO \"{}({})\" cannot be used.",
                        systemName, s.getUserName(), systemName, userName);
            }
            return s;
        }
        // doesn't exist, make a new one
        s = createNewStringIO(systemName, userName);
        // save in the maps
        register(s);

        return s;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public char typeLetter() {
        return 'C';
    }

    /**
     * Get bean type handled.
     *
     * @return a string for the type of object handled by this manager
     */
    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameStringIOs" : "BeanNameStringIO");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<StringIO> getNamedBeanClass() {
        return StringIO.class;
    }
    
    /**
     * Internal method to invoke the factory and create a new StringIO.
     *
     * Called after all the logic for returning an existing StringIO
     * has been invoked.
     * An existing SystemName is not found, existing UserName not found.
     *
     * Implementing classes should base StringIO on the system name, then add user name.
     *
     * @param systemName the system name to use for the new StringIO
     * @param userName   the optional user name to use for the new StringIO
     * @return the new StringIO
     * @throws IllegalArgumentException if unsuccessful with reason for fail.
     */
    @Nonnull
    abstract protected StringIO createNewStringIO(@Nonnull String systemName, String userName) throws IllegalArgumentException;

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("EnterStringToolTip");
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractStringIOManager.class);

}
