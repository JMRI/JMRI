// AbstractTurnoutManager.java

package jmri;


/**
 * Abstract partial implementation of a TurnoutManager.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.20 $
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

        // save in the maps
        register(s);

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
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return never null
     */
    abstract protected Turnout createNewTurnout(String systemName, String userName);
    
    /*
     * Turnout operation support. Overrideable function to return the acceptable
     * turnout operation types for this system's turnouts. Order is important because
     * they will be tried in the order specified.
     */
    String[] validOperationTypes = {"Sensor", "NoFeedback"};
    
    public String[] getValidOperationTypes() { return validOperationTypes; }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractTurnoutManager.class.getName());
}

/* @(#)AbstractTurnoutManager.java */
