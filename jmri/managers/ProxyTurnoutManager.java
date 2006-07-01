// ProxyTurnoutManager.java

package jmri.managers;

import jmri.Sensor;
import jmri.Turnout;
import jmri.TurnoutManager;

/**
 * Implementation of a TurnoutManager that can serves as a proxy
 * for multiple system-specific implementations.  The first to
 * be added is the "Primary".
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.3 $
 */
public class ProxyTurnoutManager extends AbstractProxyManager implements TurnoutManager {
    /**
     * Locate via user name, then system name if needed.
     *
     * @param name
     * @return Null if nothing by that name exists
     */
    public Turnout getTurnout(String name) {
        Turnout t = getByUserName(name);
        if (t != null) return t;
        return getBySystemName(name);
    }

    public Turnout provideTurnout(String name) {
        Turnout t = getTurnout(name);
        if (t!=null) return t;
        // if the systemName is specified, find that system
        for (int i=0; i<mgrs.size(); i++) {
            if ( ( (TurnoutManager)mgrs.get(i)).systemLetter() == name.charAt(0) )
                return ((TurnoutManager)mgrs.get(i)).newTurnout(name, null);
        }
        // did not find a manager, allow it to default to the primary
        log.debug("Did not find manager for name "+name+", assume it's a number");
        return ((TurnoutManager)mgrs.get(0)).newTurnout(
                    ((TurnoutManager)mgrs.get(0)).makeSystemName(name), null);
    }


    /**
     * Locate an instance based on a system name.  Returns null if no
     * instance already exists.
     * @return requested Turnout object or null if none exists
     */
    public Turnout getBySystemName(String systemName) {
        Turnout t = null;
        for (int i=0; i<mgrs.size(); i++) {
            t = ( (TurnoutManager)mgrs.get(i)).getBySystemName(systemName);
            if (t!=null) return t;
        }
        return null;
    }

    /**
     * Locate an instance based on a user name.  Returns null if no
     * instance already exists.
     * @return requested Turnout object or null if none exists
     */
    public Turnout getByUserName(String userName) {
        Turnout t = null;
        for (int i=0; i<mgrs.size(); i++) {
            t = ( (TurnoutManager)mgrs.get(i)).getByUserName(userName);
            if (t!=null) return t;
        }
        return null;
    }

    /**
     * Return an instance with the specified system and user names.
     * Note that two calls with the same arguments will get the same instance;
     * there is only one Sensor object representing a given physical turnout
     * and therefore only one with a specific system or user name.
     *<P>
     * This will always return a valid object reference for a valid request;
     * a new object will be
     * created if necessary. In that case:
     *<UL>
     *<LI>If a null reference is given for user name, no user name will be associated
     *    with the Turnout object created; a valid system name must be provided
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
     * This will mostly happen if you're creating Sensors when you should
     * be looking them up.
     * @return requested Sensor object (never null)
     */
    public Turnout newTurnout(String systemName, String userName) {
        // if the systemName is specified, find that system
        if (systemName != null) {
            Sensor t = null;
            for (int i=0; i<mgrs.size(); i++) {
                if ( ( (TurnoutManager)mgrs.get(i)).systemLetter() == systemName.charAt(0) )
                    return ( (TurnoutManager)mgrs.get(i)).newTurnout(systemName, userName);
            }
            // did not find a manager, allow it to default to the primary
            log.debug("Did not find manager for system name "+systemName+", assume it's a number");
            return ( (TurnoutManager)mgrs.get(0)).newTurnout(systemName, userName);
        } else {  // no systemName specified, use primary
            return ( (TurnoutManager)mgrs.get(0)).newTurnout(systemName, userName);
        }
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProxyTurnoutManager.class.getName());
}

/* @(#)ProxyTurnoutManager.java */
