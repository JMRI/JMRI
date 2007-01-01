// ProxyTurnoutManager.java

package jmri.managers;

import com.sun.java.util.collections.List;
import com.sun.java.util.collections.Arrays;
import com.sun.java.util.collections.LinkedList;

import jmri.Manager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.TurnoutOperationManager;

/**
 * Implementation of a TurnoutManager that can serves as a proxy
 * for multiple system-specific implementations.  The first to
 * be added is the "Primary".
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.13 $
 */
public class ProxyTurnoutManager extends AbstractProxyManager implements TurnoutManager {

    final java.util.ResourceBundle rbt = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");

    public ProxyTurnoutManager() {
    	super();
    }
    
	/**
	 * Revise superclass behavior: Added managers mean that the
	 * default internal manager is not the primary. 
	 * Also support TurnoutOperations
	 */
    public void addManager(Manager m) {
        if (mgrs.size() == 0) { 
            log.debug("initial addmanager");
            mgrs.add(m);
            mgrs.add(new InternalTurnoutManager());
        } else {
            mgrs.add(m);
        }
        log.debug("added manager");
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
	 * Get from the user, the number of addressed bits used to control a turnout. 
	 * Normally this is 1, and the default routine returns 1 automatically.  
	 * Turnout Managers for systems that can handle multiple control bits 
	 * should override this method with one which asks the user to specify the
	 * number of control bits.
	 * If the user specifies more than one control bit, this method should 
	 * check if the additional bits are available (not assigned to another object).
	 * If the bits are not available, this method should return 0 for number of 
	 * control bits, after informing the user of the problem.
	 */
	 public int askNumControlBits(String sysName) {
        // if the systemName is specified, find that system
		String systemName = sysName.toUpperCase();
        if (systemName != null) {
            for (int i=0; i<mgrs.size(); i++) {
                if ( ( (TurnoutManager)mgrs.get(i)).systemLetter() == systemName.charAt(0) )
                    return ((TurnoutManager)mgrs.get(i)).askNumControlBits(systemName);
            }
            // did not find a manager, allow it to default to the primary, if there is one
            log.debug("Did not find manager for system name "+systemName+", assume it's a number");
			if (mgrs.size()>0) {
				return ((TurnoutManager)mgrs.get(0)).askNumControlBits(systemName);
			} else {
				log.debug("Did not find a primary turnout manager for system name "+systemName);
				return (1);
			}
        } else {  // no systemName specified, use primary, if there is one
      		if (mgrs.size()>0) {
				return ((TurnoutManager)mgrs.get(0)).askNumControlBits(systemName);
			} else {
				log.debug("Did not find a primary turnout manager");
				return (1);
			}
        }
    }

	/**
	 * Get from the user, the type of output to be used bits to control a turnout. 
	 * Normally this is 0 for 'steady state' control, and the default routine 
	 * returns 0 automatically.  
	 * Turnout Managers for systems that can handle pulsed control as well as  
	 * steady state control should override this method with one which asks 
	 * the user to specify the type of control to be used.  The routine should 
	 * return 0 for 'steady state' control, or n for 'pulsed' control, where n
	 * specifies the duration of the pulse (normally in seconds).  
	 */
	 public int askControlType(String sysName) {
        // if the systemName is specified, find that system
		String systemName = sysName.toUpperCase();
        if (systemName != null) {
            for (int i=0; i<mgrs.size(); i++) {
                if ( ( (TurnoutManager)mgrs.get(i)).systemLetter() == systemName.charAt(0) )
                    return ((TurnoutManager)mgrs.get(i)).askControlType(systemName);
            }
            // did not find a manager, allow it to default to the primary, if there is one
            log.debug("Did not find manager for system name "+systemName+", assume it's a number");
			if (mgrs.size()>0) {
				return ((TurnoutManager)mgrs.get(0)).askControlType(systemName);
			} else {
				log.debug("Did not find a primary turnout manager for system name "+systemName);
				return (0);
			}
        } else {  // no systemName specified, use primary, if there is one
      		if (mgrs.size()>0) {
				return ((TurnoutManager)mgrs.get(0)).askControlType(systemName);
			} else {
				log.debug("Did not find a primary turnout manager");
				return (0);
			}
        }
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
