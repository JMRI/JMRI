// AbstractLightManager.java

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import jmri.managers.AbstractManager;

/**
 * Abstract partial implementation of a LightManager.
 * <P>
 * Based on AbstractSignalHeadManager.java and AbstractSensorManager.java
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @version	$Revision$
 */
public abstract class AbstractLightManager extends AbstractManager
    implements LightManager, java.beans.PropertyChangeListener, java.io.Serializable {

    public AbstractLightManager() {
        super();
    }
    
    public int getXMLOrder(){
        return Manager.LIGHTS;
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
        if (name.startsWith(getSystemPrefix()+typeLetter()))
            return newLight(name, null);
        else
            return newLight(makeSystemName(name), null);
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
        return (Light)_tsys.get(name);
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
    public Light newLight(String systemName, String userName) {
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

        // if that failed, blame it on the input arguements
        if (s == null) throw new IllegalArgumentException("cannot create new light "+systemName);

        // save in the maps
        register(s);

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
        java.util.Iterator<String> iter =
                                    getSystemNameList().iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
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
	
	/**
	 * Returns 'true' if the System can potentially support variable Lights
	 *    Note: LightManagers for Systems that can support variable Lights should 
	 *         override this method and return 'true'.
	 */
	public boolean supportsVariableLights(String systemName) {
		return false;
	}
    
   /**
    * A method that determines if it is possible to add a range of lights in numerical
    * order eg 11 thru 18, primarily used to show/not show the add range box in the add Light window
    **/
    public boolean allowMultipleAdditions(String systemName) { return false;  }

    static Logger log = LoggerFactory.getLogger(AbstractLightManager.class.getName());
}

/* @(#)AbstractLightManager.java */
