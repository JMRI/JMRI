// AbstractReporterManager.java

package jmri.managers;

import jmri.*;
import jmri.managers.AbstractManager;

/**
 * Abstract partial implementation of a ReporterManager.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.6 $
 */
public abstract class AbstractReporterManager extends AbstractManager
    implements ReporterManager {

    public char typeLetter() { return 'R'; }

    public Reporter provideReporter(String sName) {
        Reporter t = getReporter(sName);
        if (t!=null) return t;
        if (sName.startsWith(getSystemPrefix()+typeLetter()))
            return newReporter(sName, null);
        else
            return newReporter(makeSystemName(sName), null);
    }

    public Reporter getReporter(String name) {
        Reporter t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public Reporter getBySystemName(String name) {
        return (Reporter)_tsys.get(name);
    }

    public Reporter getByUserName(String key) {
        return (Reporter)_tuser.get(key);
    }

    public Reporter newReporter(String systemName, String userName) {
        if (log.isDebugEnabled()) log.debug("new Reporter:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));
        if (systemName == null){
        	log.error("SystemName cannot be null. UserName was "
        			+( (userName==null) ? "null" : userName));
        	throw new IllegalArgumentException("SystemName cannot be null. UserName was "
        			+( (userName==null) ? "null" : userName));
        }
        // return existing if there is one
        Reporter s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName)!=s)
                log.error("inconsistent user ("+userName+") and system name ("+systemName+") results; userName related to ("+s.getSystemName()+")");
            return s;
        }
        if ( (s = getBySystemName(systemName)) != null) {
			if ((s.getUserName() == null) && (userName != null))
				s.setUserName(userName);
            else if (userName != null) log.warn("Found reporter via system name ("+systemName
                                    +") with non-null user name ("+userName+")");
            return s;
        }

        // doesn't exist, make a new one
        s = createNewReporter(systemName, userName);

        // save in the maps
        register(s);

        // if that failed, blame it on the input arguements
        if (s == null) throw new IllegalArgumentException();

        return s;
    }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return never null
     */
    abstract protected Reporter createNewReporter(String systemName, String userName);

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractReporterManager.class.getName());
}

/* @(#)AbstractReporterManager.java */
