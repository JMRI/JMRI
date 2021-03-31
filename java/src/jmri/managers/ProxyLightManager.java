package jmri.managers;

import javax.annotation.Nonnull;
import jmri.Light;
import jmri.LightManager;
import jmri.Manager;

/**
 * Implementation of a LightManager that can serve as a proxy for multiple
 * system-specific implementations.
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2018
 * @author Dave Duchamp Copyright (C) 2004
 */
public class ProxyLightManager extends AbstractProvidingProxyManager<Light>
        implements LightManager {

    public ProxyLightManager() {
        super();
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.LIGHTS;
    }

    @Override
    protected AbstractManager<Light> makeInternalManager() {
        return jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class).getLightManager();
    }

    /**
     * Locate via user name, then system name if needed.
     *
     * @return Null if nothing by that name exists
     */
    @Override
    public Light getLight(@Nonnull String name) {
        return super.getNamedBean(name);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    protected Light makeBean(Manager<Light> manager, String systemName, String userName) throws IllegalArgumentException {
        return ((LightManager) manager).newLight(systemName, userName);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Light provide(@Nonnull String name) throws IllegalArgumentException { return provideLight(name); }

    /**
     * Locate via user name, then system name if needed. If that fails, create a
     * new Light: If the name is a valid system name, it will be used for the
     * new Light. Otherwise, the makeSystemName method will attempt to turn it
     * into a valid system name.
     *
     * @return Never null under normal circumstances
     */
    @Override
    @Nonnull
    public Light provideLight(@Nonnull String name) throws IllegalArgumentException {
        return super.provideNamedBean(name);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Light newLight(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        return newNamedBean(systemName, userName);
    }

    /**
     * Validate system name against the hardware configuration Locate a system
     * specific LightManager based on a system name.
     *
     * @return if a manager is found, return its determination of validity of
     * system name format relative to the hardware configuration; false if no
     * manager exists.
     */
    @Override
    public boolean validSystemNameConfig(@Nonnull String systemName) {
        LightManager m = (LightManager) getManager(systemName);
        return (m == null) ? false : m.validSystemNameConfig(systemName);
    }

    /**
     * Convert a system name to an alternate format Locate a system specfic
     * LightManager based on a system name. Returns "" if no manager exists. If
     * a manager is found, return its determination of an alternate system name
     */
    @Override
    @Nonnull
    public String convertSystemNameToAlternate(@Nonnull String systemName) {
        LightManager m = (LightManager) getManager(systemName);
        return (m == null) ? "" : m.convertSystemNameToAlternate(systemName);
    }

    /**
     * Activate the control mechanism for each Light controlled by this
     * LightManager. Relay this call to all LightManagers.
     */
    @Override
    public void activateAllLights() {
        getManagerList().forEach(m -> ((LightManager) m).activateAllLights());
    }

    /**
     * Responds 'true' if Light Manager is for a System that supports variable
     * Lights. Returns false if no manager exists. If a manager is found, return
     * its determination of support for variable lights.
     */
    @Override
    public boolean supportsVariableLights(@Nonnull String systemName) {
        LightManager m = (LightManager) getManager(systemName);
        return (m == null) ? false : m.supportsVariableLights(systemName);
    }

    /**
     * A method that determines if it is possible to add a range of lights in
     * numerical order eg 11 thru 18, primarily used to show/not show the add
     * range box in the add Light window.
     */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        LightManager m = (LightManager) getManager(systemName);
        return (m == null) ? false : m.allowMultipleAdditions(systemName);
    }
    
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws jmri.JmriException {
        return getNextValidAddress(curAddress, prefix, ignoreInitialExisting, typeLetter());
    }

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

}
