// AbstractTurnoutManager.java

package jmri;

/**
 * Abstract partial implementation of a TurnoutManager.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.16 $
 */
public abstract class AbstractTurnoutManager extends AbstractManager
    implements TurnoutManager {

    public char typeLetter() { return 'T'; }

    public Turnout provideTurnout(String name) {
        Turnout t = getTurnout(name);
        if (t!=null) return t;
        if (name.startsWith(""+systemLetter()+typeLetter()))
            return newTurnout(name, null);
        else
            return newTurnout(makeSystemName(name), null);
    }

    public Turnout getTurnout(String name) {
        Turnout t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public Turnout getBySystemName(String key) {
        return (Turnout)_tsys.get(key);
    }

    public Turnout getByUserName(String key) {
        return (Turnout)_tuser.get(key);
    }

    public Turnout newTurnout(String systemName, String userName) {
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
            if (userName != null) log.warn("Found turnout via system name ("+systemName
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
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return never null
     */
    abstract protected Turnout createNewTurnout(String systemName, String userName);

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractTurnoutManager.class.getName());
}

/* @(#)AbstractTurnoutManager.java */
