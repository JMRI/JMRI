package jmri.managers;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Light;
import jmri.LightManager;
import jmri.Manager;
import jmri.SystemConnectionMemo;

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
     * Lookup Light by UserName, then provide by SystemName.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Light newLight(@Nonnull String systemName, @CheckForNull String userName) throws IllegalArgumentException {
        Objects.requireNonNull(systemName, "SystemName cannot be null. UserName was " + ((userName == null) ? "null" : userName));  // NOI18N
        log.debug("newLight: {};{}", systemName, userName);
        
        systemName = validateSystemNameFormat(systemName);
        // return existing if there is one
        Light l;
        if (userName != null) {
            l = getByUserName(userName);
            if (l != null) {
                if (getBySystemName(systemName) != l) {
                    log.error("inconsistent user '{}' and system name '{}' results; user name related to {}",
                        userName, systemName, l.getSystemName());
                }
                return l;
            }
        }
        l = getBySystemName(systemName);
        if (l != null) {
            if ((l.getUserName() == null) && (userName != null)) {
                l.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found light via system name '{}' with non-null user name '{}'",
                        systemName, userName);
            }
            return l;
        }
        // doesn't exist, make a new one
        l = createNewLight(systemName, userName);
        // save in the maps
        register(l);

        return l;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing Light has been invoked.
     *
     * @param systemName the system name to use for this light
     * @param userName   the user name to use for this light
     * @return the new light or null if unsuccessful
     */
    @Nonnull
    abstract protected Light createNewLight(@Nonnull String systemName, String userName) throws IllegalArgumentException;

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateAllLights() {
        // Set up an iterator over all Lights contained in this manager
        for (Light l : getNamedBeanSet()) {
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
    @Nonnull
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
        return Bundle.getMessage("EnterNumber1to9999ToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractLightManager.class);

}
