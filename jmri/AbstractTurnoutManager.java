// AbstractTurnoutManager.java

package jmri;


/**
 * Abstract partial implementation of a TurnoutManager.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.23 $
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
		String sName = name.toUpperCase();
        if (sName.startsWith(""+systemLetter()+typeLetter()))
            return newTurnout(sName, null);
        else
            return newTurnout(makeSystemName(sName), null);
    }

    public Turnout getTurnout(String name) {
        Turnout t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public Turnout getBySystemName(String key) {
		String name = key.toUpperCase();
        return (Turnout)_tsys.get(name);
    }

    public Turnout getByUserName(String key) {
        return (Turnout)_tuser.get(key);
    }

    public Turnout newTurnout(String sysName, String userName) {
		String systemName = sysName.toUpperCase();
        if (log.isDebugEnabled()) log.debug("newTurnout:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));
        if (systemName == null) log.error("SystemName cannot be null. UserName was "
                                        +( (userName==null) ? "null" : userName));
        // is system name in correct format?
        if (!systemName.startsWith(""+systemLetter()+typeLetter())) {
            log.error("Invalid system name for turnout: "+systemName
                            +" needed "+systemLetter()+typeLetter());
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
		if (s != null) {
			// save in the maps if successful
			register(s);
		}

        return s;
    }
    	
	/**
	 * Get text to be used for the Turnout.CLOSED state in user communication.
	 * Allows text other than "CLOSED" to be use with certain hardware system 
	 * to represent the Turnout.CLOSED state.
	 */
	public String getClosedText() { return rbt.getString("TurnoutStateClosed"); };
	
	/**
	 * Get text to be used for the Turnout.THROWN state in user communication.
	 * Allows text other than "THROWN" to be use with certain hardware system 
	 * to represent the Turnout.THROWN state.
	 */
	public String getThrownText() { return rbt.getString("TurnoutStateThrown"); };
	
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
	 public int askNumControlBits(String systemName) {return 1; };

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
	 public int askControlType(String systemName) {return 0; };

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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractTurnoutManager.class.getName());
}

/* @(#)AbstractTurnoutManager.java */
