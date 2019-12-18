package jmri.managers;

import javax.annotation.Nonnull;

import jmri.Light;
import jmri.LightManager;
import jmri.SignalHead;

/**
 * Implementation of a LightManager that can serve as a proxy for multiple
 * system-specific implementations.
 *
 * @author	Bob Jacobsen Copyright (C) 2010, 2018
 * @author	Dave Duchamp Copyright (C) 2004
 */
public class ProxyLightManager extends AbstractProxyManager<Light>
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
    public Light getLight(String name) {
        return super.getNamedBean(name);
    }

    @Override
    protected Light makeBean(int i, String systemName, String userName) {
        return ((LightManager) getMgr(i)).newLight(systemName, userName);
    }

    @Override
    /** {@inheritDoc} */
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
    public Light provideLight(String name) throws IllegalArgumentException {
        return super.provideNamedBean(name);
    }

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists.
     *
     * @return requested Light object or null if none exists
     */
    @Override
    public Light getBySystemName(String systemName) {
        return super.getBeanBySystemName(systemName);
    }

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists.
     *
     * @return requested Turnout object or null if none exists
     */
    @Override
    public Light getByUserName(String userName) {
        return super.getBeanByUserName(userName);
    }

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Light object representing a given physical light and therefore
     * only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference for a valid request; a
     * new object will be created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the Light object created; a valid system name must be
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
     * Lights when you should be looking them up.
     *
     * @return requested Light object (never null)
     */
    @Override
    public Light newLight(String systemName, String userName) {
        return newNamedBean(systemName, userName);
    }

    /**
     * Validate system name against the hardware configuration Locate a system
     * specfic LightManager based on a system name.
     *
     * @return if a manager is found, return its determination of validity of
     * system name formatrelative to the hardware configuration.
     * Return false if no manager exists.
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        int i = matchTentative(systemName);
        if (i >= 0) {
            return ((LightManager) getMgr(i)).validSystemNameConfig(systemName);
        }
        return false;
    }

    /**
     * Convert a system name to an alternate format Locate a system specfic
     * LightManager based on a system name. Returns "" if no manager exists. If
     * a manager is found, return its determination of an alternate system name
     */
    @Override
    public String convertSystemNameToAlternate(String systemName) {
        int i = matchTentative(systemName);
        if (i >= 0) {
            return ((LightManager) getMgr(i)).convertSystemNameToAlternate(systemName);
        }
        return "";
    }

    /**
     * Activate the control mechanism for each Light controlled by this
     * LightManager. Relay this call to all LightManagers.
     */
    @Override
    public void activateAllLights() {
        for (int i = 0; i < nMgrs(); i++) {
            ((LightManager) getMgr(i)).activateAllLights();
        }
    }

    /**
     * Responds 'true' if Light Manager is for a System that supports variable
     * Lights. Returns false if no manager exists. If a manager is found, return
     * its determination of support for variable lights.
     */
    @Override
    public boolean supportsVariableLights(String systemName) {
        int i = matchTentative(systemName);
        if (i >= 0) {
            return ((LightManager) getMgr(i)).supportsVariableLights(systemName);
        }
        return false;
    }

    /**
     * A method that determines if it is possible to add a range of lights in
     * numerical order eg 11 thru 18, primarily used to show/not show the add
     * range box in the add Light window.
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        int i = matchTentative(systemName);
        if (i >= 0) {
            return ((LightManager) getMgr(i)).allowMultipleAdditions(systemName);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return "Enter a number from 1 to 9999"; // Basic number format help
    }

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

}
