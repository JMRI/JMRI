package jmri.managers;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.Light;
import jmri.LightManager;
import jmri.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract partial implementation of a LightManager.
 * <p>
 * Based on AbstractSignalHeadManager.java and AbstractSensorManager.java
 *
 * @author Dave Duchamp Copyright (C) 2004
 */
public abstract class AbstractLightManager extends AbstractManager<Light>
        implements LightManager, java.beans.PropertyChangeListener {

    /**
     * Create a new LightManager instance.
     */
    public AbstractLightManager() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.LIGHTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char typeLetter() {
        return 'L';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Light provideLight(@Nonnull String name) {
        Light t = getLight(name);
        if (t == null) {
            if (name.startsWith(getSystemPrefix() + typeLetter())) {
                return newLight(name, null);
            } else if (name.length() > 0) {
                return newLight(makeSystemName(name), null);
            } else {
                throw new IllegalArgumentException("\"" + name + "\" is invalid");
            }
        }
        return t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public Light getLight(@Nonnull String name) {
        Light result = getByUserName(name);
        if (result == null) {
            result = getBySystemName(name);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public Light getBySystemName(@Nonnull String name
    ) {
        return _tsys.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public Light getByUserName(@Nonnull String key
    ) {
        return _tuser.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Light newLight(@Nonnull String systemName, @CheckForNull String userName) {
        if (log.isDebugEnabled()) {
            log.debug("newLight:"
                    + ((systemName == null) ? "null" : systemName)
                    + ";" + ((userName == null) ? "null" : userName));
        }
        // is system name in correct format?
        if (validSystemNameFormat(systemName) != NameValidity.VALID) {
            log.error("Invalid system name for newLight: {}", systemName);
            throw new IllegalArgumentException("\"" + systemName + "\" is invalid");
        }

        // return existing if there is one
        Light s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user '{}' and system name '{}' results; user name related to {}",
                        userName, systemName, s.getSystemName());
            }
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found light via system name '{}' with non-null user name '{}'",
                        systemName, userName);
            }
            return s;
        }

        // doesn't exist, make a new one
        s = createNewLight(systemName, userName);

        // if that failed, blame it on the input arguments
        if (s == null) {
            throw new IllegalArgumentException("cannot create new light " + systemName);
        }

        // save in the maps
        register(s);

        return s;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @param systemName the system name to use for this light
     * @param userName   the user name to use for this light
     * @return the new light
     */
    @CheckForNull
    abstract protected Light createNewLight(
            @Nonnull String systemName,
            @Nonnull String userName);

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateAllLights() {
        // Set up an iterator over all Lights contained in this manager
        java.util.Iterator<String> iter
                = getSystemNameList().iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            if (systemName == null) {
                log.error("System name null during activation of Lights");
            } else {
                log.debug("Activated Light system name is " + systemName);
                Light l = getBySystemName(systemName);
                if (l == null) {
                    log.error("light null during activation of lights");
                } else {
                    l.activateLight();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String normalizeSystemName(@Nonnull String systemName) {
        return systemName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String convertSystemNameToAlternate(@Nonnull String systemName) {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsVariableLights(@Nonnull String systemName) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return false;
    }

    /**
     * get bean type handled
     *
     * @return a string for the type of object handled by this manager
     */
    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameLight");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public String getEntryToolTip() {
        return "Enter a number from 1 to 9999"; // Basic number format help
    }

    private final static Logger log
            = LoggerFactory.getLogger(AbstractLightManager.class);

}
