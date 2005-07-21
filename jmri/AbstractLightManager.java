// AbstractLightManager.java

package jmri;

import java.util.Hashtable;
import java.util.Enumeration;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Collections;

/**
 * Abstract partial implementation of a LightManager.
 * <P>
 * Based on AbstractSignalHeadManager.java and AbstractSensorManager.java
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @version	$Revision: 1.3 $
 */
public abstract class AbstractLightManager extends AbstractManager
    implements LightManager, java.beans.PropertyChangeListener {

    public AbstractLightManager() {
        super();
    }
    
    /**
     * Returns the second letter in the system name for a Light
     */
    public char typeLetter() { return 'L'; }

    /**
     * Locate via user name, then system name if needed.
     * If that fails, create a new Light: If the name
     * is a valid system name, it will be used for the new
     * Light.  Otherwise, the makeSystemName method
     * will attempt to turn it into a valid system name.
     *
     * @param name
     * @return Never null unless valid systemName cannot
     *     be found
     */
    public Light provideLight(String name) {
        Light t = getLight(name);
        if (t!=null) return t;
		String sName = name.toUpperCase();
        if (sName.startsWith(""+systemLetter()+typeLetter()))
            return newLight(sName, null);
        else
            return newLight(makeSystemName(sName), null);
    }

    /**
     * Locate via user name, then system name if needed.
     * Does not create a new one if nothing found
     *
     * @param name
     * @return null if no match found
     */
    public Light getLight(String name) {
        Light t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    /**
     * Locate a Light by its system name
     */
    public Light getBySystemName(String name) {
		String key = name.toUpperCase();
        return (Light)_tsys.get(key);
    }

    /**
     * Locate a Light by its user name
     */
    public Light getByUserName(String key) {
        return (Light)_tuser.get(key);
    }

    /**
     * Return an instance with the specified system and user names.
     * Note that two calls with the same arguments will get the same instance;
     * there is only one Light object representing a given physical Light
     * and therefore only one with a specific system or user name.
     *<P>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     *<UL>
     *<LI>If a null reference is given for user name, no user name will be associated
     *    with the Light object created; a valid system name must be provided
     *<LI>If both names are provided, the system name defines the
     *    hardware access of the desired sensor, and the user address
     *    is associated with it. The system name must be valid.
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
        if (log.isDebugEnabled()) log.debug("newLight:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));
        if (systemName == null) {
            log.error("SystemName cannot be null. UserName was "
                                        +( (userName==null) ? "null" : userName));
            return null;
        }
        // is system name in correct format?
        if ( !validSystemNameFormat(systemName) ) {
            log.error("Invalid system name for newLight: "+systemName);
            return null;
        }

        // return existing if there is one
        Light s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName)!=s)
                log.error("inconsistent user ("+userName+") and system name ("
                        +systemName+") results; userName related to ("+s.getSystemName()+")");
            return s;
        }
        if ( (s = getBySystemName(systemName)) != null) {
			if ((s.getUserName() == null) && (userName != null))
				s.setUserName(userName);
            else if (userName != null) log.warn("Found light via system name ("+systemName
                                    +") with non-null user name ("+userName+")");
            return s;
        }

        // doesn't exist, make a new one
        s = createNewLight(systemName, userName);
        if (s!=null) {
            // save in the maps
            register(s);
        }

        return s;
    }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return new null
     */
    abstract protected Light createNewLight(String systemName, String userName);

    /**
     * Activate the control mechanism for each Light controlled by
     *    this LightManager.  Note that some Lights don't require
     *    any activation.  The activateLight method in AbstractLight.java
     *    determines what needs to be done for each Light.
     */
    public void activateAllLights() {
        // Set up an iterator over all Lights contained in this manager
        com.sun.java.util.collections.Iterator iter =
                                    getSystemNameList().iterator();
        while (iter.hasNext()) {
            String systemName = (String)iter.next();
            if (systemName==null) { 
                log.error("System name null during activation of Lights");
            }
            else {
                log.debug("Activated Light system name is "+systemName);
                getBySystemName(systemName).activateLight();
            }
        }
    }

    /**
     * Normalize the system name
     * <P>
     * This routine is used to ensure that each system name is uniquely linked to
     *      one C/MRI bit, by removing extra zeros inserted by the user.
     * <P>
     * If a system implementation has names that could be normalized, the 
     *      system-specific Light Manager should override this routine and supply 
     *      a normalized system name.
     */
    public String normalizeSystemName(String systemName) {
        return systemName;
    }

    /**
     * Convert the system name to a normalized alternate name
     * <P>
     * This routine is to allow testing to ensure that two Lights with
     *      alternate names that refer to the same output bit are not
     *      created.
     * <P>
     * If a system implementation has alternate names, the system specific
     *      Light Manager should override this routine and supply the alternate
     *      name.
     */
    public String convertSystemNameToAlternate(String systemName) {
        return "";
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractLightManager.class.getName());
}

/* @(#)AbstractLightManager.java */
