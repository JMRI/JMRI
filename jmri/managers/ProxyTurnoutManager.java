// ProxyTurnoutManager.java

package jmri.managers;

import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;

import jmri.Manager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.TurnoutOperationManager;

/**
 * Implementation of a TurnoutManager that can serves as a proxy
 * for multiple system-specific implementations.  The first to
 * be added is the "Primary".
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.8 $
 */
public class ProxyTurnoutManager extends AbstractProxyManager implements TurnoutManager {

    final java.util.ResourceBundle rbt = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");

    public ProxyTurnoutManager() {
    	super();
    }
    
    /**
     * override of generic class, hook to support TurnoutOPerations
     */
    public void addManager(Manager m) {
    	super.addManager(m);
    	TurnoutOperationManager.getInstance().loadOperationTypes();
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

    public Turnout provideTurnout(String name) {
        Turnout t = getTurnout(name);
        if (t!=null) return t;
        // if the systemName is specified, find that system
		String sName = name.toUpperCase();
        for (int i=0; i<mgrs.size(); i++) {
            if ( ( (TurnoutManager)mgrs.get(i)).systemLetter() == sName.charAt(0) )
                return ((TurnoutManager)mgrs.get(i)).newTurnout(sName, null);
        }
        // did not find a manager, allow it to default to the primary, if there is one
        log.debug("Did not find manager for name "+sName+", assume it's a number");
		if (mgrs.size()>0) {		
			return ((TurnoutManager)mgrs.get(0)).newTurnout(
                    ((TurnoutManager)mgrs.get(0)).makeSystemName(sName), null);
		} else {
			log.debug("Did not find a primary turnout manager for name "+sName);
			return (null);
		}
    }

    /**
     * Locate an instance based on a system name.  Returns null if no
     * instance already exists.
     * @return requested Turnout object or null if none exists
     */
    public Turnout getBySystemName(String systemName) {
		String sName = systemName.toUpperCase();
        Turnout t = null;
        for (int i=0; i<mgrs.size(); i++) {
            t = ( (TurnoutManager)mgrs.get(i)).getBySystemName(sName);
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
    public Turnout newTurnout(String sysName, String userName) {
        // if the systemName is specified, find that system
		String systemName = sysName.toUpperCase();
        if (systemName != null) {
            Sensor t = null;
            for (int i=0; i<mgrs.size(); i++) {
                if ( ( (TurnoutManager)mgrs.get(i)).systemLetter() == systemName.charAt(0) )
                    return ( (TurnoutManager)mgrs.get(i)).newTurnout(systemName, userName);
            }
            // did not find a manager, allow it to default to the primary, if there is one
            log.debug("Did not find manager for system name "+systemName+", assume it's a number");
			if (mgrs.size()>0) {
				return ( (TurnoutManager)mgrs.get(0)).newTurnout(systemName, userName);
			} else {
				log.debug("Did not find a primary turnout manager for system name "+systemName);
				return (null);
			}
        } else {  // no systemName specified, use primary, if there is one
      		if (mgrs.size()>0) {
				return ( (TurnoutManager)mgrs.get(0)).newTurnout(systemName, userName);
			} else {
				log.debug("Did not find a primary turnout manager");
				return (null);
			}
        }
    }
    	
	/**
	 * Get text to be used for the Turnout.CLOSED state in user communication.
	 * Allows text other than "CLOSED" to be use with certain hardware system 
	 * to represent the Turnout.CLOSED state.  
	 * Defaults to the primary manager.  This means that the primary manager sets the terminology
	 * used.  Note: the primary manager need not override the method in AbstractTurnoutManager if
	 * "CLOSED" is the desired terminology.
	 */
	public String getClosedText() { 
		if (mgrs.size()>0)
			return ( (TurnoutManager)mgrs.get(0)).getClosedText(); 
		else 
			return rbt.getString("TurnoutStateClosed");
	}
	
	/**
	 * Get text to be used for the Turnout.THROWN state in user communication.
	 * Allows text other than "THROWN" to be use with certain hardware system 
	 * to represent the Turnout.THROWN state.
	 * Defaults to the primary manager.  This means that the primary manager sets the terminology
	 * used.  Note: the primary manager need not override the method in AbstractTurnoutManager if
	 * "THROWN" is the desired terminology.
	 */
	public String getThrownText() { 
		if (mgrs.size()>0)
			return ( (TurnoutManager)mgrs.get(0)).getThrownText(); 
		else 
			return rbt.getString("TurnoutStateThrown");
	}

	/**
	 * TurnoutOperation support. Return a list which is just the concatenation of
	 * all the valid operation types
	 */
	public String[] getValidOperationTypes() {
		List typeList = new LinkedList();
		for (int i=0; i<mgrs.size(); ++i) {
			String[] thisTypes = ((TurnoutManager)mgrs.get(i)).getValidOperationTypes();
			typeList.addAll(Arrays.asList(thisTypes));
		}
		return TurnoutOperationManager.concatenateTypeLists((String[])typeList.toArray(new String[0]));
	}
	
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProxyTurnoutManager.class.getName());
}

/* @(#)ProxyTurnoutManager.java */
