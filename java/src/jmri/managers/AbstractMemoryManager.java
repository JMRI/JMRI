package jmri.managers;

import java.text.DecimalFormat;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.Manager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.SignalHead;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract partial implementation of a MemoryManager.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 */
public abstract class AbstractMemoryManager extends AbstractManager<Memory>
        implements MemoryManager {

    /**
     * Create a new MemoryManager instance.
     * 
     * @param memo the system connection
     */
    public AbstractMemoryManager(SystemConnectionMemo memo) {
        super(memo);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.MEMORIES;
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        return 'M';
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull Memory provideMemory(@Nonnull String sName) {
        Memory t = getMemory(sName);
        if (t != null) {
            return t;
        }
        if (sName.startsWith(getSystemNamePrefix())) {
            return newMemory(sName, null);
        } else {
            return newMemory(makeSystemName(sName), null);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Memory getMemory(@Nonnull String name) {
        Memory t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    public Memory getBySystemName(@Nonnull String name) {
        return _tsys.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public Memory getByUserName(@Nonnull String key) {
        return _tuser.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull Memory newMemory(@Nonnull String systemName, @CheckForNull String userName) {
        log.debug("new Memory: {}; {}", systemName, userName); // NOI18N
        Objects.requireNonNull(systemName, "Value of requested systemName cannot be null");

        // return existing if there is one
        Memory s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user ({}) and system name ({}) results; userName related to ({})", userName, systemName, s.getSystemName()); // NOI18N
            }
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
            // handle user name from request
            if (userName != null) {
                // check if already on set in Object, might be inconsistent
                if (!userName.equals(s.getUserName())) {
                    // this is a problem
                    log.warn("newMemory request for system name \"{}\" user name \"{}\" found memory with existing user name \"{}\"", systemName, userName, s.getUserName());
                } else {
                    s.setUserName(userName);
                }
            }
            return s;
        }

        // doesn't exist, make a new one
        s = createNewMemory(systemName, userName);

        // if that failed, blame it on the input arguments
        if (s == null) {
            throw new IllegalArgumentException();
        }

        // save in the maps
        register(s);

        // Keep track of the last created auto system name
        updateAutoNumber(systemName);

        return s;
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull Memory newMemory(@Nonnull String userName) {
        return newMemory(getAutoSystemName(), userName);
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @param systemName Memory system name
     * @param userName   Memory user name
     * @return a new Memory
     */
    @Nonnull
    abstract protected Memory createNewMemory(@Nonnull String systemName, @CheckForNull String userName);

    /** {@inheritDoc} */
    @Override
    @Nonnull 
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameMemories" : "BeanNameMemory");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Memory> getNamedBeanClass() {
        return Memory.class;
    }

    @Override
    @Nonnull
    public Memory provide(String name) throws IllegalArgumentException {
        return provideMemory(name);
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractMemoryManager.class);

}
