// ProxyTurnoutManager.java

package jmri.managers;

import java.util.*;
import com.sun.java.util.collections.*;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;
import jmri.*;

/**
 * Implementation of a TurnoutManager that can serves as a proxy
 * for multiple system-specific implementations.  The first to
 * be added is the "Primary".
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.1 $
 */
public class ProxyTurnoutManager implements TurnoutManager {

    public void dispose() {
        for (int i=0; i<mgrs.size(); i++)
            ( (TurnoutManager)mgrs.get(i)).dispose();
        mgrs.clear();
    }

    /**
     * @return The system-specific prefix letter for the primary implementation
     */
    public char systemLetter() {
        return ((TurnoutManager)mgrs.get(0)).systemLetter();
    }

    /**
     * @return The type letter for turnouts
     */
    public char typeLetter() {
        return ((TurnoutManager)mgrs.get(0)).typeLetter();
    }

    /**
     * @return A system name from a user input, typically a number.
     */
    public String makeSystemName(String s) {
        return ((TurnoutManager)mgrs.get(0)).makeSystemName(s);
    }

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
     * Note that two calls with the same arguments will get the same instance
     * and generate a warning;
     * there is only one Turnout object representing a given physical turnout
     * and therefore only one with a specific system or user name.
     *<P>
     * This will always return a valid object reference; a new object will be
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
     * This will mostly happen if you're creating Turnouts when you should
     * be looking them up.
     * @return requested Turnout object (never null)
     */
    public Turnout newTurnout(String systemName, String userName) {
        // if the systemName is specified, find that system
        if (systemName != null) {
            Turnout t = null;
            for (int i=0; i<mgrs.size(); i++) {
                if ( ( (TurnoutManager)mgrs.get(i)).systemLetter() == systemName.charAt(0) )
                    return ( (TurnoutManager)mgrs.get(i)).newTurnout(systemName, userName);
            }
            log.warn("Did not find manager for system name "+systemName);
            return null;
        } else {  // no systemName specified, use primary
            return ( (TurnoutManager)mgrs.get(0)).newTurnout(systemName, userName);
        }
    }

    /**
     * Get a list of all system names.
     */
    public List getSystemNameList() {
        ArrayList result = new ArrayList();
        for (int i = 0; i<mgrs.size(); i++)
            result.addAll( ((TurnoutManager)mgrs.get(i)).getSystemNameList() );
        return result;
    }

    List mgrs = new ArrayList();

    public void addManager(TurnoutManager m) { mgrs.add(m); }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProxyTurnoutManager.class.getName());
}

/* @(#)ProxyTurnoutManager.java */
