package jmri.managers;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.Light;
import jmri.LightManager;
import jmri.Manager;
import jmri.SignalSystem;
import jmri.jmrix.SystemConnectionMemo;
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
        implements LightManager {

    /**
     * Create a new LightManager instance.
     * 
     * @param memo the system connection
     */
    public AbstractLightManager(SystemConnectionMemo memo) {
        super(memo);
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
        Light light = getLight(name);
        // makeSystemName checks for validity
        return light == null ? newLight(makeSystemName(name, true), null) : light;
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
    public Light getBySystemName(@Nonnull String name) {
        return _tsys.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public Light getByUserName(@Nonnull String key) {
        return _tuser.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Light newLight(@Nonnull String systemName, @CheckForNull String userName) {
        log.debug("newLight: {};{}",
                ((systemName == null) ? "null" : systemName),
                ((userName == null) ? "null" : userName));
        systemName = validateSystemNameFormat(systemName);

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
        java.util.Iterator<Light> iter
                = getNamedBeanSet().iterator();
        while (iter.hasNext()) {
            Light l = iter.next();
            log.debug("Activated Light system name is {}", l.getSystemName());
            l.activateLight();
        }
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
     * Get bean type handled.
     *
     * @return a string for the type of object handled by this manager
     */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameLights" : "BeanNameLight");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Light> getNamedBeanClass() {
        return Light.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public String getEntryToolTip() {
        return "Enter a number from 1 to 9999"; // Basic number format help
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractLightManager.class);

}
