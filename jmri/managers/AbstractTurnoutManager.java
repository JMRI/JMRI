// AbstractTurnoutManager.java

package jmri.managers;

import jmri.*;
import jmri.managers.AbstractManager;


/**
 * Abstract partial implementation of a TurnoutManager.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.8 $
 */
public abstract class AbstractTurnoutManager extends AbstractManager
    implements TurnoutManager {
	
	public AbstractTurnoutManager() {
		TurnoutOperationManager.getInstance();		// force creation of an instance
	}

    final java.util.ResourceBundle rbt = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");

    public char typeLetter() { return 'T'; }

    public Turnout provideTurnout(String name) {
        Turnout t = getTurnout(name);
        if (t!=null) return t;
        if (name.startsWith(getSystemPrefix()+typeLetter()))
            return newTurnout(name, null);
        else
            return newTurnout(makeSystemName(name), null);
    }

    public Turnout getTurnout(String name) {
        Turnout t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public Turnout getBySystemName(String name) {
        return (Turnout)_tsys.get(name);
    }

    public Turnout getByUserName(String key) {
        return (Turnout)_tuser.get(key);
    }

    public Turnout newTurnout(String systemName, String userName) {
        if (log.isDebugEnabled()) log.debug("newTurnout:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));
        if (systemName == null){
        	log.error("SystemName cannot be null. UserName was "
        			+( (userName==null) ? "null" : userName));
        	return null;
        }
        // is system name in correct format?
        if (!systemName.startsWith(getSystemPrefix()+typeLetter())) {
            log.error("Invalid system name for turnout: "+systemName
                            +" needed "+getSystemPrefix()+typeLetter());
            return null;
        }

        // return existing if there is one
        Turnout s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName)!=s)
                log.error("inconsistent user ("+userName+") and system name ("+systemName+") results; userName related to ("+s.getSystemName()+")");
            return s;
        }
        if ( (s = getBySystemName(systemName)) != null) {
			if ((s.getUserName() == null) && (userName != null))
				s.setUserName(userName);
            else if (userName != null) log.warn("Found turnout via system name ("+systemName
                                    +") with non-null user name ("+userName+")");
            return s;
        }

        // doesn't exist, make a new one
        s = createNewTurnout(systemName, userName);

        // if that failed, blame it on the input arguements
        if (s == null) throw new IllegalArgumentException();

        // save in the maps if successful
        register(s);

        return s;
    }
    	
	/**
	 * Get text to be used for the Turnout.CLOSED state in user communication.
	 * Allows text other than "CLOSED" to be use with certain hardware system 
	 * to represent the Turnout.CLOSED state.
	 */
	public String getClosedText() { return rbt.getString("TurnoutStateClosed"); }
	
	/**
	 * Get text to be used for the Turnout.THROWN state in user communication.
	 * Allows text other than "THROWN" to be use with certain hardware system 
	 * to represent the Turnout.THROWN state.
	 */
	public String getThrownText() { return rbt.getString("TurnoutStateThrown"); }
	
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
	 public int askNumControlBits(String systemName) {return 1; }

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
	 public int askControlType(String systemName) {return 0; }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return never null
     */
    abstract protected Turnout createNewTurnout(String systemName, String userName);
    
    /*
     * Provide list of supported operation types.
     * <p>
     * Order is important because
     * they will be tried in the order specified.
     */
    public String[] getValidOperationTypes() {
        if (jmri.InstanceManager.commandStationInstance()!=null) {
            return new String[]{"Sensor", "Raw", "NoFeedback"};
       } else {
       	    return new String[]{"Sensor", "NoFeedback"};
       }
    }
    
    /**
    * A temporary method that determines if it is possible to add a range
    * of turnouts in numerical order eg 10 to 30
    **/
    
    public boolean allowMultipleAdditions() { return true;  }
    
    /**
    * A method that creates an array of systems names to allow bulk
    * creation of turnouts.
    */
    public String[] formatRangeOfAddresses(String start, int numberToAdd, String prefix){
        int iName = 0;
        String range[] = new String[numberToAdd];
        try {
            iName = Integer.parseInt(start);
        } catch (NumberFormatException ex) {
            log.error("Unable to convert Hardware Address to a number");
            return null;
        }
        for (int x = 0; x < numberToAdd; x++){
            range[x] = prefix+"T"+(iName+x);
        }
        return range;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractTurnoutManager.class.getName());
}

/* @(#)AbstractTurnoutManager.java */
