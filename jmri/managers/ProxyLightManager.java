// ProxyLightManager.java

package jmri.managers;

import jmri.Light;
import jmri.LightManager;

/**
 * Implementation of a LightManager that can serves as a proxy
 * for multiple system-specific implementations.  The first to
 * be added is the "Primary".
 * <P>
 * Based on ProxySensorManager
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @version	$Revision: 1.2 $
 */
public class ProxyLightManager extends AbstractProxyManager
                            implements LightManager {

    /**
     * Locate via user name, then system name if needed.
     *
     * @param name
     * @return Null if nothing by that name exists
     */
    public Light getLight(String name) {
        Light t = getByUserName(name);
        if (t != null) return t;
        return getBySystemName(name);
    }

    /**
     * Locate via user name, then system name if needed.
     * If that fails, create a new Light: If the name
     * is a valid system name, it will be used for the new
     * Light.  Otherwise, the makeSystemName method
     * will attempt to turn it into a valid system name.
     *
     * @param name
     * @return Never null under normal circumstances
     */
    public Light provideLight(String name) {
        Light t = getLight(name);
        if (t!=null) return t;
		String sName = name.toUpperCase();
        // if the systemName is specified, find that system
        for (int i=0; i<mgrs.size(); i++) {
            if ( ( (LightManager)mgrs.get(i)).systemLetter() == sName.charAt(0) )
                return ((LightManager)mgrs.get(i)).newLight(sName, null);
        }
        // did not find a manager, allow it to default to the primary
        log.debug("Did not find manager for name "+sName+", assume it's a number");
        return ((LightManager)mgrs.get(0)).newLight(
                    ((LightManager)mgrs.get(0)).makeSystemName(sName), null);
    }

    /**
     * Locate an instance based on a system name.  Returns null if no
     * instance already exists.
     * @return requested Light object or null if none exists
     */
    public Light getBySystemName(String systemName) {
        Light t = null;
		String sName = systemName.toUpperCase();
        for (int i=0; i<mgrs.size(); i++) {
            t = ( (LightManager)mgrs.get(i)).getBySystemName(sName);
            if (t!=null) return t;
        }
        return null;
    }

    /**
     * Locate an instance based on a user name.  Returns null if no
     * instance already exists.
     * @return requested Turnout object or null if none exists
     */
    public Light getByUserName(String userName) {
        Light t = null;
        for (int i=0; i<mgrs.size(); i++) {
            t = ( (LightManager)mgrs.get(i)).getByUserName(userName);
            if (t!=null) return t;
        }
        return null;
    }

    /**
     * Return an instance with the specified system and user names.
     * Note that two calls with the same arguments will get the same instance;
     * there is only one Light object representing a given physical light
     * and therefore only one with a specific system or user name.
     *<P>
     * This will always return a valid object reference for a valid request;
     * a new object will be
     * created if necessary. In that case:
     *<UL>
     *<LI>If a null reference is given for user name, no user name will be associated
     *    with the Light object created; a valid system name must be provided
     *<LI>If a null reference is given for the system name, a system name
     *    will _somehow_ be inferred from the user name.  How this is done
     *    is system specific.  Note: a future extension of this interface
     *    will add an exception to signal that this was not possible.
     *<LI>If both names are provided, the system name defines the
     *    hardware access of the desired turnout, and the user address
     *    is associated with it.
     *</UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects.  This is a problem, and we don't have a
     * good solution except to issue warnings.
     * This will mostly happen if you're creating Lights when you should
     * be looking them up.
     * @return requested Light object (never null)
     */
    public Light newLight(String sysName, String userName) {
		String systemName = sysName.toUpperCase();
        // if the systemName is specified, find that system
        if (systemName != null) {
            Light t = null;
            for (int i=0; i<mgrs.size(); i++) {
                if ( ( (LightManager)mgrs.get(i)).systemLetter() == systemName.charAt(0) )
                    return ( (LightManager)mgrs.get(i)).newLight(systemName, userName);
            }
            // did not find a manager, allow it to default to the primary
            log.debug("Did not find manager for system name "+systemName+", assume it's a number");
            return ( (LightManager)mgrs.get(0)).newLight(systemName, userName);
        } else {  // no systemName specified, use primary
            return ( (LightManager)mgrs.get(0)).newLight(systemName, userName);
        }
    }

    /**
     * Validate system name format
     * Locate a system specfic LightManager based on a system name.  Returns false if no
     *      manager exists.
     * If a manager is found, return its determination of validity of system name format
     */
    public boolean validSystemNameFormat(String systemName) {
        for (int i=0; i<mgrs.size(); i++) {
            if ( ( (LightManager)mgrs.get(i)).systemLetter() == systemName.charAt(0) ) {
                return ( (LightManager)mgrs.get(i)).validSystemNameFormat(systemName);
            }
        }
        return false;
    }

    /**
     * Validate system name against the hardware configuration
     * Locate a system specfic LightManager based on a system name.  Returns false if no
     *      manager exists.
     * If a manager is found, return its determination of validity of system name relative
     *      to the hardware configuration
     */
    public boolean validSystemNameConfig(String systemName) {
        for (int i=0; i<mgrs.size(); i++) {
            if ( ( (LightManager)mgrs.get(i)).systemLetter() == systemName.charAt(0) ) {
                return ( (LightManager)mgrs.get(i)).validSystemNameConfig(systemName);
            }
        }
        return false;
    }

    /**
     * Normalize a system name
     * Locate a system specfic LightManager based on a system name.  Returns "" if no
     * manager exists.
     * If a manager is found, return its determination of a normalized system name
     */
    public String normalizeSystemName(String systemName) {
        for (int i=0; i<mgrs.size(); i++) {
            if ( ( (LightManager)mgrs.get(i)).systemLetter() == systemName.charAt(0) ) {
                return ( (LightManager)mgrs.get(i)).normalizeSystemName(systemName);
            }
        }
        return "";
    }

    /**
     * Convert a system name to an alternate format
     * Locate a system specfic LightManager based on a system name.  Returns "" if no
     * manager exists.
     * If a manager is found, return its determination of an alternate system name
     */
    public String convertSystemNameToAlternate(String systemName) {
        for (int i=0; i<mgrs.size(); i++) {
            if ( ( (LightManager)mgrs.get(i)).systemLetter() == systemName.charAt(0) ) {
                return ( (LightManager)mgrs.get(i)).convertSystemNameToAlternate(systemName);
            }
        }
        return "";
    }

    /**
     * Activate the control mechanism for each Light controlled by
     *    this LightManager.  
     * Relay this call to all LightManagers.
     */
    public void activateAllLights() {
        for (int i=0; i<mgrs.size(); i++) {
            ((LightManager)mgrs.get(i)).activateAllLights();
        }
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProxyLightManager.class.getName());
}

/* @(#)ProxyLightManager.java */
